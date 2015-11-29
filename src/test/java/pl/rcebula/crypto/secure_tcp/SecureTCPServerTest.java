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
public class SecureTCPServerTest
{
    private static SecureTCPServer instance;
    
    public SecureTCPServerTest()
    {
    }
    
    private static class ReadCallback implements IReadCallback
    {
        @Override
        public void dataRead(byte[] data, IConnection connection)
        {
            try
            {
                String message = new String(data, Names.STRING_CODING);
                System.out.println(message);
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
        public void closeConnection(IConnection connection)
        {
            System.out.println("closed connection");
        }
        
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception
    {
        int port = 14000;
        RSAKeyContainer rsakc = 
                new RSAKeyContainer("/public_key.der", "/private_key.der");
        instance = new SecureTCPServer(port, rsakc, new ReadCallback(), 
                new CloseConnectionCallback());
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