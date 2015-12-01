/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto.secure_tcp;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import pl.rcebula.crypto.encryption.Names;
import pl.rcebula.crypto.encryption.RSAKeyContainer;

/**
 *
 * @author robert
 */
public class TimeoutTest
{
    private static class ReadCallback implements IReadCallback
    {
        public static int counter = 0;

        @Override
        public void dataRead(byte[] data, IConnectionId connection,
                SecureTCPServer server)
        {
            try
            {
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
    
    public TimeoutTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    @Test
    public void timeoutTest() throws Exception
    {
        System.out.println("Timeout test");
        
        int secureConnectionTimeout = 100;
        int unsecureConnectionTimeout = 200;
        
        int port = 14004;
        RSAKeyContainer serverRsakc = new RSAKeyContainer("/public_key.der",
                "/private_key.der");
        SecureTCPServer secureTCPServer = new SecureTCPServer(port, serverRsakc,
                secureConnectionTimeout, unsecureConnectionTimeout, 
                new ReadCallback(), new CloseConnectionCallback());
        secureTCPServer.start();

        RSAKeyContainer clientRsakc = new RSAKeyContainer("/public_key.der");
        SecureTCPClient secureTCPClient = new SecureTCPClient("localhost",
                port, clientRsakc);

        SecureTCPClient secureTCPClient1 = new SecureTCPClient("localhost",
                port, serverRsakc);

        // symulujemy UnsecureConnection, które przekracza timeout
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("localhost", port));
        
        Thread.sleep(unsecureConnectionTimeout * 2);
        
        assertEquals(1, secureTCPServer.getClosedUnsecureConnections());
        
        long start1 = System.currentTimeMillis();
        secureTCPClient.connect();
        long end1 = System.currentTimeMillis();
        long start2 = System.currentTimeMillis();
        secureTCPClient1.connect();
        long end2 = System.currentTimeMillis();
        System.out.println("connection time 1: " + (end1 - start1) + " ms");
        System.out.println("connection time 2: " + (end2 - start2) + " ms");

        // symulujemy, że jedno połączenie zamyka połączenie, a drugie nie
        secureTCPClient.close();
        Thread.sleep(secureConnectionTimeout * 2);
        
        assertEquals(1, secureTCPServer.getClosedSecureConnections());
        
        // symulujemy pisanie co połowę czasu timeout
        byte[] junk = new byte[] { -1, 2, 3, 4, 5, 6, 7 };
        secureTCPClient1.write(junk);
        Thread.sleep(secureConnectionTimeout / 2);
        secureTCPClient1.write(junk);
        Thread.sleep(secureConnectionTimeout / 2);
        
        assertEquals(1, secureTCPServer.getClosedSecureConnections());
        
        // symulujemy otwarcie nowego połączenia i przekroczenie timeout na
        // starym
        long start3 = System.currentTimeMillis();
        secureTCPClient.connect();
        long end3 = System.currentTimeMillis();
        System.out.println("connection time 3: " + (end3 - start3) + " ms");
        
        secureTCPClient1.close();
        Thread.sleep(secureConnectionTimeout * 2);
        
        assertEquals(2, secureTCPServer.getClosedSecureConnections());

        secureTCPServer.stop();
        secureTCPClient.close();
        secureTCPClient1.close();
        
        System.out.println("End timeout test");
    }
}
