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
public class CloseConnectionTest
{
    private static class ReadCallback implements IReadCallback
    {
        public static int counter = 0;

        @Override
        public void dataRead(byte[] data, IConnection connection)
        {
            counter++;
        }
    }

    private static class CloseConnectionCallback implements
            ICloseConnectionCallback
    {
        private static int counter = 0;

        @Override
        public void closeConnection(IConnection connection)
        {
            counter++;
        }
    }

    public CloseConnectionTest()
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
    public void CloseConnectionTest() throws Exception
    {
        int port = 14001;
        RSAKeyContainer serverRsakc = new RSAKeyContainer("/public_key.der",
                "/private_key.der");
        SecureTCPServer secureTCPServer = new SecureTCPServer(port, serverRsakc,
                1000, new ReadCallback(), new CloseConnectionCallback());
        secureTCPServer.start();

        RSAKeyContainer clientRsakc = new RSAKeyContainer("/public_key.der");
        SecureTCPClient secureTCPClient = new SecureTCPClient("localhost",
                port, clientRsakc);

        SecureTCPClient secureTCPClient1 = new SecureTCPClient("localhost",
                port, serverRsakc);

        secureTCPClient.connect();
        secureTCPClient1.connect();

        IConnection connection1 = secureTCPServer.accept();
        IConnection connection2 = secureTCPServer.accept();

        Thread.sleep(20);

        secureTCPClient.close();
        secureTCPClient1.close();

        Thread.sleep(20);

        byte[] junk = new byte[]
        {
            1, 2, 3, 4
        };
        secureTCPServer.write(connection1, junk);
        secureTCPServer.write(connection2, junk);
        
        Thread.sleep(20);

        secureTCPServer.stop();

        assertEquals(ReadCallback.counter, 0);
        assertEquals(CloseConnectionCallback.counter, 2);
    }
}
