/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto.secure_tcp;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import static org.junit.Assert.*;
import pl.rcebula.crypto.encryption.RSAKeyContainer;

/**
 *
 * @author robert
 */
public class MultipleConnectionsTest
{
    private final int port = 14005;
    private RSAKeyContainer rsakcServer;
    private RSAKeyContainer rsakcClient;

    private final AtomicInteger assertionFails = new AtomicInteger(0);
    private final AtomicInteger assertionGoods = new AtomicInteger(0);
    private final AtomicInteger serverWriteReads
            = new AtomicInteger(0);
    private final AtomicInteger clientWrites
            = new AtomicInteger(0);

    private float avgReadWriteTime = 0.0f;
    private int nReadWriteTime = 0;
    private final Object lockReadWriteTime = new Object();

    private class ReadCallback implements IReadCallback
    {
        private int counter = 0;

        @Override
        public void dataRead(byte[] data, IConnectionId connection,
                SecureTCPServer server)
        {
            try
            {
                serverWriteReads.incrementAndGet();
                server.write(connection, data);

                counter++;
            }
            catch (Exception ex)
            {

            }
        }

        public int getCounter()
        {
            return counter;
        }
    }

    private class CloseConnectionCallback implements
            ICloseConnectionCallback
    {
        private int counter = 0;
        
        @Override
        public void closeConnection(IConnectionId connection)
        {
            ++counter;
        }

        public int getCounter()
        {
            return counter;
        }
    }

    public MultipleConnectionsTest() throws Exception
    {
        String publicKey = "/public_key.der";
        String privateKey = "/private_key.der";

        rsakcServer = new RSAKeyContainer(publicKey, privateKey);
        rsakcClient = new RSAKeyContainer(publicKey);
        
        assertionFails.set(0);
        assertionGoods.set(0);
        serverWriteReads.set(0);
        clientWrites.set(0);

        avgReadWriteTime = 0.0f;
        nReadWriteTime = 0;
    }

    private class ClientThread implements Runnable
    {
        private final int numberOfClients;

        private final SecureTCPClient[] secureTCPClients;

        public ClientThread(int numberOfClients)
        {
            this.numberOfClients = numberOfClients;
            this.secureTCPClients = new SecureTCPClient[numberOfClients];
        }

        private void closeClientConnections()
        {
            for (SecureTCPClient client : secureTCPClients)
            {
                if (client != null)
                {
                    client.close();
                }
            }
        }

        @Override
        public void run()
        {
            try
            {
                Random rand = ThreadLocalRandom.current();

                for (int i = 0; i < secureTCPClients.length; ++i)
                {
                    secureTCPClients[i]
                            = new SecureTCPClient("localhost", port, rsakcClient);
                    secureTCPClients[i].connect();
                }

                // rand how much time will thread executes tasks (in ms)
                int allTime = randInt(400, 1000);
                long startTime = System.currentTimeMillis();

                while ((System.currentTimeMillis() - startTime) < allTime)
                {
                    for (SecureTCPClient client : secureTCPClients)
                    {
                        int pings = randInt(1, 4);

                        while (pings-- > 0)
                        {
                            int bytes = randInt(10, 1000);
                            byte[] data = new byte[bytes];
                            rand.nextBytes(data);

                            long start = System.currentTimeMillis();
                            client.write(data);

                            clientWrites.incrementAndGet();

//                            System.out.println("read");
                            byte[] incData = client.read();
//                            System.out.println("after read");
                            long end = System.currentTimeMillis();
                            long diffrence = (end - start);

                            // calculate average read-write time 
                            synchronized (lockReadWriteTime)
                            {
                                avgReadWriteTime = countAvg(avgReadWriteTime,
                                        diffrence, ++nReadWriteTime);
                            }

                            try
                            {
                                assertArrayEquals(data, incData);
                            }
                            catch (AssertionError er)
                            {
                                assertionFails.incrementAndGet();

                                throw er;
                            }

                            assertionGoods.incrementAndGet();
                        }
                    }
                }
            }
            catch (InterruptedException | IOException ex)
            {
                // do nothing
            }
            catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
            finally
            {
                closeClientConnections();
            }
        }

        private float countAvg(float avg, float new_sample, int N)
        {
            avg -= avg / N;
            avg += new_sample / N;

            return avg;
        }

        public int randInt(int min, int max)
        {
            Random rand = ThreadLocalRandom.current();

            int randomNum = rand.nextInt((max - min) + 1) + min;

            return randomNum;
        }
    }

    public void multipleConnectionsEchoServerTest(int numberOfThreads,
            int numberOfClientsPerThread, int testTimeInMs) throws Exception
    {
        int scTimeout = 1000;
        int ucTimeout = 1000;

        ThreadPoolExecutor service = (ThreadPoolExecutor)Executors.newFixedThreadPool(
                numberOfThreads,
                new ThreadFactory()
                {
                    @Override
                    public Thread newThread(Runnable r)
                    {
                        Thread t
                        = Executors.defaultThreadFactory().newThread(r);
                        t.setDaemon(true);
                        return t;
                    }
                });

        SecureTCPServer server = new SecureTCPServer(port, rsakcServer,
                scTimeout, ucTimeout, new ReadCallback(),
                new CloseConnectionCallback());

        server.start();

        Thread.sleep(50);

        long start = System.currentTimeMillis();

        while ((System.currentTimeMillis() - start) < testTimeInMs)
        {
            ClientThread t = new ClientThread(numberOfClientsPerThread);
            service.execute(t);

            while (service.getQueue().size() > numberOfThreads
                    && (System.currentTimeMillis() - start) < testTimeInMs)
            {
                Thread.sleep(1);
            }
        }

        service.shutdownNow();

        server.stop();

        Thread.sleep(150);

        IConnectionId[] acceptedConnections = server.acceptAll();
        
        System.out.println("All accepted connections: "
                + acceptedConnections.length);

        System.out.println("Closed connections: "
                + server.getClosedSecureConnections());

        System.out.println("All read-writes: " + nReadWriteTime);
        System.out.println("Average read-write time: " + avgReadWriteTime
                + " ms");

        System.out.println("Still running (aproximetly): " + 
                service.getActiveCount());
        System.out.println("Completed (aproximetly): " + 
                service.getCompletedTaskCount());
        
        System.out.println("Good assertions: " + assertionGoods.get());
        System.out.println("Failed assertions: " + assertionFails.get());

        float diffrenceInS = (System.currentTimeMillis() - start) / 1000.0f;
        System.out.println("Total test time: " + diffrenceInS + " s");

        assertEquals("Number of Writes",
                serverWriteReads.get(), clientWrites.get());
    }
}
