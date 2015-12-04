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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import pl.rcebula.crypto.encryption.RSAKeyContainer;

/**
 *
 * @author robert
 */
public class SecureTCPServer
{
    private final ServerSocket serverSocket;
    private final int port;

    // czas po którym połączenie z niekatywnym klientem (tj. niewysyłającym
    // i nie odbierającym żadnych danych) jest sprawdzane i ew. usuwane (w ms)
    private final int secureConnectionTimeout;
    // czas po którym połączenie z niekatywnym klientem (tj. niedokonującym
    // handshake'u) jest usuwane (w ms)
    private final int unsecureConnectionTimeout;

    private final RSAKeyContainer rsakc;

    private final AtomicBoolean running;
    
    private final AtomicBoolean hasAcceptedConnection;
    private final AtomicBoolean hasDataToWrite;

    // zmienne używane do testów
    private final AtomicInteger closedSecureConnections;
    private final AtomicInteger closedUnsecureConnections;
    
    // callback dla zdarzenia odczytania danych
    private final IReadCallback readCallback;
    // callback dla zdarzenia zamknięcia połączenia
    private final ICloseConnectionCallback closeConnectionCallback;

    // lista połączeń dla których nie został wykonany jeszcze handshake
    // i nie posiadają wymienionego klucza AES
    private final List<UnsecureConnectionTimeout> unsecureConnections = 
            new CopyOnWriteArrayList<>();
    // lista połączeń dla których został wykonany handshake
    // i posiadają wymieniony klucz AES
    private final List<SecureConnectionTimeout> secureConnections = 
            new CopyOnWriteArrayList<>();
//    private final List<IConnection> connections = new LinkedList<>();

    // kolejka LIFO która przechowuje dane, które mają zostać wysłane
    private final LinkedList<Tuple> dataToWrite = new LinkedList<>();

    // kolejka LIFO która przechowuje zaakceptowanych klientów, którzy nie
    // zostali odebrani
    private final LinkedList<IConnectionId> acceptedConnections
            = new LinkedList<>();

    public SecureTCPServer(int port, RSAKeyContainer rsakc,
            int secureConnectionTimeout, int unsecureConnectionTimeout,
            IReadCallback readCallback,
            ICloseConnectionCallback closeConnectionCallback) throws IOException
    {
        this.serverSocket = new ServerSocket(port);
        this.port = port;
        this.unsecureConnectionTimeout = unsecureConnectionTimeout;
        this.secureConnectionTimeout = secureConnectionTimeout;
        this.rsakc = rsakc;
        this.running = new AtomicBoolean(false);

        this.closedSecureConnections = new AtomicInteger(0);
        this.closedUnsecureConnections = new AtomicInteger(0);
        
        this.hasAcceptedConnection = new AtomicBoolean(false);
        this.hasDataToWrite = new AtomicBoolean(false);

        this.readCallback = readCallback;
        this.closeConnectionCallback = closeConnectionCallback;
    }

    public int getClosedSecureConnections()
    {
        return closedSecureConnections.get();
    }

    public int getClosedUnsecureConnections()
    {
        return closedUnsecureConnections.get();
    }

    public synchronized void start()
    {
        running.set(true);

        Thread t = new Thread(new AcceptThread());
        t.setDaemon(true);
        t.start();

        t = new Thread(new ReadThread());
        t.setDaemon(true);
        t.start();

        t = new Thread(new WriteThread());
        t.setDaemon(true);
        t.start();
    }

    public synchronized void stop()
    {
        try
        {
            running.set(false);

            {
                Iterator<UnsecureConnectionTimeout> it
                        = unsecureConnections.iterator();

                while (it.hasNext())
                {
                    UnsecureConnectionTimeout uc = it.next();

                    closeUnsecureConnection(uc);
                }
            }

            {
                Iterator<SecureConnectionTimeout> it
                        = secureConnections.iterator();

                while (it.hasNext())
                {
                    SecureConnectionTimeout sc = it.next();

                    closeSecureConnection(sc);
                }
            }

            serverSocket.close();
        }
        catch (Exception e)
        {
        }
    }
    
    private void removeUnsecureConnection(UnsecureConnectionTimeout uc)
    {
        unsecureConnections.remove(uc);
    }

    private void closeUnsecureConnection(UnsecureConnectionTimeout uc)
    {
        synchronized (unsecureConnections)
        {
            if (unsecureConnections.contains(uc))
            {
                uc.close();

                unsecureConnections.remove(uc);
                closedUnsecureConnections.incrementAndGet();
            }
        }
    }

    private void closeSecureConnection(SecureConnectionTimeout sc)
    {
        synchronized (secureConnections)
        {
            if (secureConnections.contains(sc))
            {
                sc.close();
                closeConnectionCallback.closeConnection(sc);

                secureConnections.remove(sc);
                closedSecureConnections.incrementAndGet();
            }
        }
    }

    private class ReadThread implements Runnable
    {
        @Override
        public void run()
        {
            Thread.currentThread().setName("Reading thread");

            while (running.get())
            {
                List<SecureConnectionTimeout> scToAdd
                        = new LinkedList<>();

                {
                    Iterator<UnsecureConnectionTimeout> it
                            = unsecureConnections.iterator();

                    while (it.hasNext())
                    {
                        UnsecureConnectionTimeout uc = it.next();
                        if (!uc.getConnection().isReady())
                        {
                            try
                            {
                                uc.read(new IConnection.ByteArray());
                                UnsecureConnection connection = 
                                        uc.getConnection();
                                
                                if (connection.isReady())
                                {
                                    SecureConnection sc
                                            = connection.getSecureConnection();

                                    SecureConnectionTimeout sct
                                            = new SecureConnectionTimeout(sc,
                                                    secureConnectionTimeout);

                                    scToAdd.add(sct);
                                    secureConnections.add(sct);
                                    
                                    connection.establishConnection(uc);
                                }
                                else if (uc.isTimeout())
                                {
                                    closeUnsecureConnection(uc);
                                }
                            }
                            catch (Exception ex)
                            {
                                closeUnsecureConnection(uc);
                            }
                        }
                    }
                }

                synchronized (acceptedConnections)
                {
                    acceptedConnections.addAll(scToAdd);
                    hasAcceptedConnection.set(true);
                }

                {
                    Iterator<SecureConnectionTimeout> it
                            = secureConnections.iterator();

                    while (it.hasNext())
                    {
                        SecureConnectionTimeout sc = it.next();

                        try
                        {
                            IConnection.ByteArray data
                                    = new IConnection.ByteArray();
                            int bytesRead = sc.read(data);
                            if (bytesRead > 0)
                            {
                                readCallback.dataRead(data.array, sc,
                                        SecureTCPServer.this);
                            }
                            else if (sc.isTimeout())
                            {
                                write(sc, new byte[0]);
                            }
                        }
                        catch (Exception ex)
                        {
                            closeSecureConnection(sc);
                        }
                    }
                }

                Thread.yield();
            }
        }
    }

    private class WriteThread implements Runnable
    {
        @Override
        public void run()
        {
            Thread.currentThread().setName("Writing thread");

            while (running.get())
            {
                while (!hasDataToWrite.get())
                {
                }
                
                IConnectionId connection;
                byte[] data;
                boolean closeAfterWrite;
                Tuple tuple;
                
                synchronized (dataToWrite)
                {
                    hasDataToWrite.set(dataToWrite.size() > 1);
                    
                    tuple = dataToWrite.removeFirst();
                }
                
                connection = tuple.getConnection();
                data = tuple.getData();
                closeAfterWrite = tuple.getCloseAfterWrite();

                boolean alreadyWrite = false;
                
                {
                    Iterator<SecureConnectionTimeout> it = 
                            secureConnections.iterator();
                    
                    while (it.hasNext())
                    {
                        SecureConnectionTimeout sct = it.next();
                        
                        if (sct == connection)
                        {
                            alreadyWrite = true;
                            
                            try
                            {
                                sct.write(data);
                                
                                if (closeAfterWrite)
                                {
                                    closeSecureConnection(sct);
                                }
                            }
                            catch (Exception ex)
                            {
                                closeSecureConnection(sct);
                            }
                        }
                    }
                }
                
                if (!alreadyWrite)
                {
                    Iterator<UnsecureConnectionTimeout> it = 
                            unsecureConnections.iterator();
                    
                    while (it.hasNext())
                    {
                        UnsecureConnectionTimeout uct = it.next();
                        
                        if (uct == connection)
                        {
                            try
                            {
                                uct.write(data);
                                
                                if (closeAfterWrite)
                                {
                                    removeUnsecureConnection(uct);
                                }
                            }
                            catch (Exception ex)
                            {
                                closeUnsecureConnection(uct);
                            }
                        }
                    }
                }

                Thread.yield();
            }
        }
    }

    private class AcceptThread implements Runnable
    {
        @Override
        public void run()
        {
            Thread.currentThread().setName("Accepting thread");

            while (running.get())
            {
                try
                {
                    Socket socket = serverSocket.accept();
                    socket.setSoTimeout(secureConnectionTimeout);
                    
                    UnsecureConnection uc = new UnsecureConnection(socket,
                                SecureTCPServer.this, rsakc);
                    
                    unsecureConnections.add(
                            new UnsecureConnectionTimeout(uc,
                                    unsecureConnectionTimeout));
                }
                catch (Exception ex)
                {
                    stop();
                }

                Thread.yield();
            }
        }
    }

    // blocking method that return one connection
    public IConnectionId accept()
    {
        while (!hasAcceptedConnection.get())
        {
            Thread.yield();
        }

        synchronized (acceptedConnections)
        {
            hasAcceptedConnection.set(acceptedConnections.size() > 1);
            
            return acceptedConnections.removeFirst();
        }
    }

    // non-blocking method that returns all accepted till this moment 
    // connections at once as array (return 0 element array if no connections)
    public IConnectionId[] acceptAll()
    {
        IConnectionId[] result = new IConnectionId[0];

        synchronized (acceptedConnections)
        {
            if (acceptedConnections.size() != 0)
            {
                result = acceptedConnections.toArray(new IConnectionId[0]);
                acceptedConnections.clear();
            }
        }

        return result;
    }

    public void write(IConnectionId connection, byte[] data)
    {
        synchronized (dataToWrite)
        {
            dataToWrite.addLast(new Tuple(connection, data, false));
            hasDataToWrite.set(true);
        }
    }

    public void write(IConnectionId connection, byte[] data,
            boolean closeAfterWrite)
    {
        synchronized (dataToWrite)
        {
            dataToWrite.addLast(new Tuple(connection, data, closeAfterWrite));
            hasDataToWrite.set(true);
        }
    }

    private static class Tuple
    {
        private final IConnectionId connection;
        private final byte[] data;
        private final boolean closeAfterWrite;

        public Tuple(IConnectionId connection, byte[] data,
                boolean closeAfterWrite)
        {
            this.connection = connection;
            this.data = data;
            this.closeAfterWrite = closeAfterWrite;
        }

        public IConnectionId getConnection()
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
