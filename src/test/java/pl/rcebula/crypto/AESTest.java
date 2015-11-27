/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto;

import java.security.Key;
import javax.crypto.spec.IvParameterSpec;
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
public class AESTest
{
    private static AESKeyContainer container;
    
    public AESTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
        container = new AESKeyContainer();
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
     * Test of decrypt method, of class AES.
     */
    @Test
    public void testEncryptAndDecrypt() throws Exception
    {
        System.out.println("decrypt");
        
        String text = "Hello world!";
        byte[] toEncrypt = text.getBytes(Names.STRING_CODING);
        Key key = container.getKey();
        IvParameterSpec iv = container.getIv();
        AES instance = new AES();
        byte[] encrypted = instance.encrypt(toEncrypt, key, iv);
        byte[] decrypted = instance.decrypt(encrypted, key, iv);
        assertArrayEquals(toEncrypt, decrypted);
        assertEquals(text, new String(decrypted, Names.STRING_CODING));
    }
}
