/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto.secure_tcp;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import pl.rcebula.crypto.encryption.RSAKeyContainer;

/**
 *
 * @author robert
 */
public class PerformanceTest
{
    private static final int port = 14005;
    private static RSAKeyContainer rsakcServer;
    private static RSAKeyContainer rsakcClient;

    private static final AtomicInteger assertionFails = new AtomicInteger(0);
    private static final AtomicInteger assertionGoods = new AtomicInteger(0);

    private static float avgReadWriteTime = 0.0f;
    private static int nReadWriteTime = 0;
    private static final Object lockReadWriteTime = new Object();

    private static class ReadCallback implements IReadCallback
    {
        public static int counter = 0;

        @Override
        public void dataRead(byte[] data, IConnectionId connection,
                SecureTCPServer server)
        {
            try
            {
                server.write(connection, data);

                counter++;
            }
            catch (Exception ex)
            {

            }
        }
    }

    private static class CloseConnectionCallback implements
            ICloseConnectionCallback
    {
        private static int counter = 0;

        @Override
        public void closeConnection(IConnectionId connection)
        {
            counter++;
        }
    }

    public PerformanceTest()
    {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
        String publicKey = "/public_key.der";
        String privateKey = "/private_key.der";

        rsakcServer = new RSAKeyContainer(publicKey, privateKey);
        rsakcClient = new RSAKeyContainer(publicKey);
    }

    @AfterClass
    public static void tearDownClass()
    {
    }

    @Before
    public void setUp()
    {
        assertionFails.set(0);
        assertionGoods.set(0);

        avgReadWriteTime = 0.0f;
        nReadWriteTime = 0;
        
        ReadCallback.counter = 0;
        CloseConnectionCallback.counter = 0;
    }

    @After
    public void tearDown()
    {
    }

    private static class ClientThread implements Runnable
    {
        private final int numberOfClients;

        public ClientThread(int numberOfClients)
        {
            this.numberOfClients = numberOfClients;
        }

        @Override
        public void run()
        {
            try
            {
                Random rand = ThreadLocalRandom.current();

                SecureTCPClient[] secureTCPClients
                        = new SecureTCPClient[numberOfClients];

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

                for (SecureTCPClient client : secureTCPClients)
                {
                    client.close();
                }
            }
            catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
        }

        private static float countAvg(float avg, float new_sample, int N)
        {
            avg -= avg / N;
            avg += new_sample / N;

            return avg;
        }

        public static int randInt(int min, int max)
        {
            Random rand = ThreadLocalRandom.current();

            int randomNum = rand.nextInt((max - min) + 1) + min;

            return randomNum;
        }
    }

    private void multipleConnectionsEchoServerTest(int numberOfThreads,
            int numberOfClientsPerThread, int testTimeInMs) throws Exception
    {
        int scTimeout = 1000;
        int ucTimeout = 1000;

        ThreadPoolExecutor service
                = (ThreadPoolExecutor)Executors.newFixedThreadPool(numberOfThreads);

        SecureTCPServer server = new SecureTCPServer(port, rsakcServer,
                scTimeout, ucTimeout, new ReadCallback(),
                new CloseConnectionCallback());

        server.start();

        Thread.sleep(50);

        List<Future<?>> futures = new LinkedList<>();

        long start = System.currentTimeMillis();

        while ((System.currentTimeMillis() - start) < testTimeInMs)
        {
            ClientThread t = new ClientThread(numberOfClientsPerThread);
            Future<?> f = service.submit(t);
            futures.add(f);

            while (service.getQueue().size() > numberOfThreads)
            {
                Thread.sleep(1);
            }
        }
        
        service.shutdown();

        System.out.println("Wait for shutdown");
        
        try
        {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
        catch (InterruptedException e)
        {
        }

        boolean hasException = false;
        
        for (Future<?> f : futures)
        {
            try
            {
                f.get();
            }
            catch (ExecutionException ex)
            {
                hasException = true;
            }
        }

        IConnectionId[] acceptedConnections = server.acceptAll();
        assertEquals(acceptedConnections.length,
                futures.size() * numberOfClientsPerThread);

        server.stop();

        Thread.sleep(20);

        System.out.println("All accepted connections: "
                + acceptedConnections.length);

        System.out.println("Closed connections: "
                + CloseConnectionCallback.counter);

        System.out.println("All read-writes: " + nReadWriteTime);
        System.out.println("Average read-write time: " + avgReadWriteTime
                + " ms");

        System.out.println("Good assertions: " + assertionGoods.get());
        System.out.println("Failed assertions: " + assertionFails.get());

        float diffrenceInS = (System.currentTimeMillis() - start) / 1000.0f;
        System.out.println("Total test time: " + diffrenceInS + " s");
        
        if (hasException)
        {
            fail("Client thread throws exception");
        }
    }

    @Test
    public void oneClientAtTimeTest() throws Exception
    {
        System.out.println("");
        System.out.println("oneClientAtTimeTest");
        multipleConnectionsEchoServerTest(1, 1, 200);
    }

    @Test
    public void lowLoadoutPerformanceTest() throws Exception
    {
        System.out.println("");
        System.out.println("lowLoadoutPerformanceTest");
        multipleConnectionsEchoServerTest(4, 1, 500);
    }

    @Test
    public void mediumLoadoutPerformanceTest() throws Exception
    {
        System.out.println("");
        System.out.println("mediumLoadoutPerformanceTest");
        multipleConnectionsEchoServerTest(8, 4, 2000);
    }

    @Test
    public void highLoadoutPerformanceTest() throws Exception
    {
        System.out.println("");
        System.out.println("highLoadoutPerformanceTest");
        multipleConnectionsEchoServerTest(8, 8, 2000);
    }

    @Test
    public void highLoadoutLongPerformanceTest() throws Exception
    {
        System.out.println("");
        System.out.println("highLoadoutLongPerformanceTest");
        multipleConnectionsEchoServerTest(8, 8, 10000);
    }

    @Test
    public void ExtremeLoadoutPerformanceTest() throws Exception
    {
        System.out.println("");
        System.out.println("ExtremeLoadoutPerformanceTest");
        multipleConnectionsEchoServerTest(12, 20, 2000);
    }
}
