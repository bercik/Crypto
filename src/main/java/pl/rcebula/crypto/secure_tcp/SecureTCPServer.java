/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto.secure_tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import pl.rcebula.crypto.encryption.AES;
import pl.rcebula.crypto.encryption.RSAKeyContainer;

/**
 *
 * @author robert
 */
public class SecureTCPServer
{
    private final ServerSocket serverSocket;
    private final int port;
    private final RSAKeyContainer rsakc;

    private boolean running;

    // callback dla zdarzenia odczytania danych
    private final IReadCallback readCallback;
    // callback dla zdarzenia zamknięcia połączenia
    private final ICloseConnectionCallback closeConnectionCallback;

    // lista połączeń dla których nie został wykonany jeszcze handshake
    // i nie posiadają wymienionego klucza AES
    private final List<UnsecureConnection> unsecureConnections
            = new LinkedList<>();
    // lista połączeń dla których został wykonany handshake
    // i posiadają wymieniony klucz AES
    private final List<SecureConnection> secureConnections
            = new LinkedList<>();
//    private final List<IConnection> connections = new LinkedList<>();

    // kolejka LIFO która przechowuje dane, które mają zostać wysłane
    private final LinkedList<Tuple> dataToWrite = new LinkedList<>();

    // kolejka LIFO która przechowuje zaakceptowanych klientów, którzy nie
    // zostali odebrani
    private final LinkedList<IConnection> acceptedConnections
            = new LinkedList<>();

    public SecureTCPServer(int port, RSAKeyContainer rsakc,
            IReadCallback readCallback,
            ICloseConnectionCallback closeConnectionCallback) throws IOException
    {
        this.serverSocket = new ServerSocket(port);
        this.port = port;
        this.rsakc = rsakc;
        this.running = false;

        this.readCallback = readCallback;
        this.closeConnectionCallback = closeConnectionCallback;
    }

    public synchronized void start()
    {
        running = true;

        new Thread(new AcceptThread()).start();
        new Thread(new ReadThread()).start();
        new Thread(new WriteThread()).start();
    }

    public synchronized void stop()
    {
        try
        {
            running = false;

            synchronized (unsecureConnections)
            {
                Iterator<UnsecureConnection> it
                        = unsecureConnections.iterator();

                while (it.hasNext())
                {
                    UnsecureConnection uc = it.next();
                    uc.close();
                }
            }

            synchronized (secureConnections)
            {
                Iterator<SecureConnection> it
                        = secureConnections.iterator();

                while (it.hasNext())
                {
                    SecureConnection sc = it.next();
                    sc.close();
                }
            }

            serverSocket.close();
        }
        catch (Exception e)
        {
        }
    }

    private void closeUnsecureConnection(UnsecureConnection uc)
    {
        uc.close();
        unsecureConnections.remove(uc);
    }

    private void closeSecureConnection(SecureConnection sc)
    {
        sc.close();
        closeConnectionCallback.closeConnection(sc);
        secureConnections.remove(sc);
    }

    private class ReadThread implements Runnable
    {
        @Override
        public void run()
        {
            Thread.currentThread().setName("Reading thread");

            while (running)
            {
                synchronized (unsecureConnections)
                {
                    Iterator<UnsecureConnection> it
                            = unsecureConnections.iterator();

                    while (it.hasNext())
                    {
                        UnsecureConnection uc = it.next();
                        try
                        {
                            if (uc.read())
                            {
                                SecureConnection sc = uc.getSecureConnection();
                                secureConnections.add(sc);
                                synchronized (acceptedConnections)
                                {
                                    acceptedConnections.addLast(sc);
                                }
                            }
                        }
                        catch (Exception ex)
                        {
                            closeUnsecureConnection(uc);
                        }
                    }
                }

                synchronized (secureConnections)
                {
                    Iterator<SecureConnection> it
                            = secureConnections.iterator();

                    while (it.hasNext())
                    {
                        SecureConnection sc = it.next();

                        try
                        {
                            byte[] data = sc.read();
                            if (data.length != 0)
                            {
                                readCallback.dataRead(data, sc);
                            }
                        }
                        catch (AES.DecryptionError | IOException ex)
                        {
                            closeSecureConnection(sc);
                        }
                    }
                }
            }
        }
    }

    private class WriteThread implements Runnable
    {
        @Override
        public void run()
        {
            Thread.currentThread().setName("Writing thread");

            while (running)
            {
                boolean hasDataToRead = false;
                while (!hasDataToRead)
                {
                    synchronized (dataToWrite)
                    {
                        hasDataToRead = (dataToWrite.size() != 0);
                    }
                }

                IConnection connection;
                byte[] data;
                boolean closeAfterWrite;

                synchronized (dataToWrite)
                {
                    Tuple tuple = dataToWrite.removeFirst();
                    connection = tuple.getConnection();
                    data = tuple.getData();
                    closeAfterWrite = tuple.getCloseAfterWrite();
                }

                int index;

                synchronized (unsecureConnections)
                {
                    index = unsecureConnections.indexOf(connection);
                    if (index != -1)
                    {
                        UnsecureConnection uc = unsecureConnections.get(index);
                        try
                        {
                            uc.write(data);
                            if (closeAfterWrite)
                            {
                                unsecureConnections.remove(uc);
                            }
                        }
                        catch (IOException ex)
                        {
                            closeUnsecureConnection(uc);
                        }
                    }
                }

                if (index == -1)
                {
                    synchronized (secureConnections)
                    {
                        index = secureConnections.indexOf(connection);
                        if (index != -1)
                        {
                            SecureConnection sc = secureConnections.get(index);
                            try
                            {
                                sc.write(data);
                                if (closeAfterWrite)
                                {
                                    closeSecureConnection(sc);
                                }
                            }
                            catch (IOException | AES.EncryptionError ex)
                            {
                                closeSecureConnection(sc);
                            }
                        }
                    }
                }
            }
        }
    }

    private class AcceptThread implements Runnable
    {
        @Override
        public void run()
        {
            Thread.currentThread().setName("Accepting thread");

            while (running)
            {
                try
                {
                    Socket socket = serverSocket.accept();
                    synchronized (unsecureConnections)
                    {
                        unsecureConnections.add(
                                new UnsecureConnection(socket,
                                        SecureTCPServer.this, rsakc));
                    }
                }
                catch (Exception ex)
                {
                    stop();
                }
            }
        }
    }

    public IConnection accept()
    {
        boolean hasAcceptedConnection = false;
        while (!hasAcceptedConnection)
        {
            synchronized (acceptedConnections)
            {
                hasAcceptedConnection = (acceptedConnections.size() != 0);
            }
        }

        synchronized (acceptedConnections)
        {
            return acceptedConnections.removeFirst();
        }

    }

    public void write(IConnection connection, byte[] data)
    {
        synchronized (dataToWrite)
        {
            dataToWrite.addLast(new Tuple(connection, data));
        }
    }

    public void write(IConnection connection, byte[] data,
            boolean closeAfterWrite)
    {
        synchronized (dataToWrite)
        {
            dataToWrite.addLast(new Tuple(connection, data, closeAfterWrite));
        }
    }

    private static class Tuple
    {
        private final IConnection connection;
        private final byte[] data;
        private final boolean closeAfterWrite;

        public Tuple(IConnection connection, byte[] data)
        {
            this(connection, data, false);
        }

        public Tuple(IConnection connection, byte[] data,
                boolean closeAfterWrite)
        {
            this.connection = connection;
            this.data = data;
            this.closeAfterWrite = closeAfterWrite;
        }

        public IConnection getConnection()
        {
            return connection;
        }

        public byte[] getData()
        {
            return data;
        }

        public boolean getCloseAfterWrite()
        {
            return closeAfterWrite;
        }
    }
}
