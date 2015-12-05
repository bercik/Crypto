/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto.secure_tcp;

import java.io.IOError;
import pl.rcebula.crypto.secure_tcp.utils.PortGiver;
import pl.rcebula.crypto.secure_tcp.utils.RsakcGiver;
import java.io.IOException;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author robert
 */
public class BlockingClientTest
{
    private static class ReadCallback implements IReadCallback
    {
        public static int counter = 0;

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
    
    public BlockingClientTest()
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

    @Test(expected = IOException.class)
    public void blockingClientTest() throws Exception
    {
        final int port = PortGiver.getPort();
        int scTimeout = 200;
        int ucTimeout = 200;
        
        SecureTCPServer server = new SecureTCPServer(port, 
                RsakcGiver.getServerRsakc(), scTimeout, ucTimeout,
                new ReadCallback(), new CloseConnectionCallback());
        
        SecureTCPClient client = new SecureTCPClient("localhost", port, 
                RsakcGiver.getClientRsakc());
        
        server.start();
        
        client.connect();
        
        IConnectionId clientId = server.accept();
        
        while (true)
        {
            server.write(clientId, new byte[1024 * 4]);
            client.write(new byte[1]);
        }
    }
}
