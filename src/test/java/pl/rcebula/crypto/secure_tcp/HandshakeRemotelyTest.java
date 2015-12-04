/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto.secure_tcp;

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
public class HandshakeRemotelyTest
{
    private static class ReadCallback implements IReadCallback
    {
        private static int counter = 0;
        
        @Override
        public void dataRead(byte[] data, IConnectionId connection, 
                SecureTCPServer server)
        {
            counter++;
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

    public HandshakeRemotelyTest()
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
    public void HandshakeClientServerTest() throws Exception
    {
        int port = 14002;
        RSAKeyContainer serverRsakc = new RSAKeyContainer("/public_key.der",
                "/private_key.der");
        SecureTCPServer secureTCPServer = new SecureTCPServer(port, serverRsakc,
                1000, 500, new ReadCallback(), new CloseConnectionCallback());
        secureTCPServer.start();
        
        RSAKeyContainer clientRsakc = new RSAKeyContainer("/public_key.der");
        SecureTCPClient secureTCPClient = new SecureTCPClient("localhost", 
                port, clientRsakc);
        
        SecureTCPClient secureTCPClient1 = new SecureTCPClient("localhost", 
                port, serverRsakc);
        
        secureTCPClient.connect();
        secureTCPClient1.connect();
        
        secureTCPServer.accept();
        secureTCPServer.accept();
        
        Thread.sleep(50);
        
        secureTCPServer.stop();
        secureTCPClient.close();
        secureTCPClient1.close();
        
        assertEquals(ReadCallback.counter, 0);
        assertEquals(CloseConnectionCallback.counter, 2);
    }
}
