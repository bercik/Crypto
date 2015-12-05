/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto.secure_tcp;

import pl.rcebula.crypto.secure_tcp.utils.PortGiver;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.rcebula.crypto.encryption.RSAKeyContainer;

/**
 *
 * @author robert
 */
public class SecureTCPServerTest
{
    private static SecureTCPServer instance;
    
    public SecureTCPServerTest()
    {
    }
    
    private static class ReadCallback implements IReadCallback
    {
        @Override
        public void dataRead(byte[] data, IConnectionId connection,
                SecureTCPServer server)
        {
            try
            {
            }
            catch (Exception ex)
            {
            }
        }
    }
    
    private static class CloseConnectionCallback implements 
            ICloseConnectionCallback
    {
        @Override
        public void closeConnection(IConnectionId connection)
        {
            System.out.println("closed connection");
        }
        
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception
    {
        int port = PortGiver.getPort();
        RSAKeyContainer rsakc = 
                new RSAKeyContainer("/public_key.der", "/private_key.der");
        instance = new SecureTCPServer(port, rsakc, 1000, 500, 
                new ReadCallback(), new CloseConnectionCallback());
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

    /**
     * Test of start method, of class SecureTCPServer.
     */
    @Test
    public void testStartAndStop()
    {
        System.out.println("start");
        instance.start();
        instance.stop();
    }
}
