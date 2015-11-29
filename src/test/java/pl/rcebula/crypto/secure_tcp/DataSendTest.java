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
public class DataSendTest
{
    private static final String[] messages = new String[]
    {
        "Hello world!",
        "Some testing message",
        "Polskie znaki ąęźćðłłśąśð"
    };

    private static class ReadCallback implements IReadCallback
    {
        public static int counter = 0;

        @Override
        public void dataRead(byte[] data, IConnection connection)
        {
            try
            {
                String message = new String(data, Names.STRING_CODING);
                System.out.println(message);

                assertEquals(message, messages[counter++]);
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
        public void closeConnection(IConnection connection)
        {
            counter++;
        }
    }

    public DataSendTest()
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
    public void dataSendTest() throws Exception
    {
        int port = 14002;
        RSAKeyContainer serverRsakc = new RSAKeyContainer("/public_key.der",
                "/private_key.der");
        SecureTCPServer secureTCPServer = new SecureTCPServer(port, serverRsakc,
                new ReadCallback(), new CloseConnectionCallback());
        secureTCPServer.start();

        RSAKeyContainer clientRsakc = new RSAKeyContainer("/public_key.der");
        SecureTCPClient secureTCPClient = new SecureTCPClient("localhost",
                port, clientRsakc);

        SecureTCPClient secureTCPClient1 = new SecureTCPClient("localhost",
                port, serverRsakc);

        secureTCPClient.connect();
        secureTCPClient1.connect();

        secureTCPClient.write(messages[0].getBytes(Names.STRING_CODING));
        secureTCPClient1.write(messages[1].getBytes(Names.STRING_CODING));
        secureTCPClient.write(messages[2].getBytes(Names.STRING_CODING));

        Thread.sleep(50);

        secureTCPServer.stop();
        secureTCPClient.close();
        secureTCPClient1.close();

        assertEquals(ReadCallback.counter, 3);
        assertEquals(CloseConnectionCallback.counter, 0);
    }
}
