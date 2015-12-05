/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto.encryption;

import pl.rcebula.crypto.encryption.RSA;
import pl.rcebula.crypto.encryption.RSAKeyContainer;
import pl.rcebula.crypto.encryption.Names;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
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
public class RSATest
{
    private static RSAKeyContainer container;
    
    private static final String coding = "UTF-8";
    
    public RSATest()
    {
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception
    {
        container = new RSAKeyContainer("/public_key.der", "/private_key.der");
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
     * Test of encrypt method, of class RSA.
     */
    @Test
    public void testEncryptAndDecrypt() throws Exception
    {
        System.out.println("encrypt");
        String text = "Hello world!";
        byte[] toEncrypt = text.getBytes(coding);
        Key publicKey = container.getPublicKey();
        Key privateKey = container.getPrivateKey();
        RSA instance = new RSA();
        byte[] encrypted = instance.encrypt(toEncrypt, publicKey);
        byte[] decrypted = instance.decrypt(encrypted, privateKey);
        
        assertArrayEquals(decrypted, toEncrypt);
        assertEquals(text, new String(decrypted, coding));
    }

    /**
     * Test of sign method, of class RSA.
     */
    @Test
    public void testSignAndCheckSign() throws Exception
    {
        String text = "Hello world! This is a very very long text " + 
                "to test if sign and check sign methods working properly. " + 
                "This text needs to be more than 254 bytes because this is " + 
                "the maximum amount that RSA with 256 bytes key long can " +
                "encrypt.";
        
        byte[] toSign = text.getBytes(coding);
        PublicKey publicKey = container.getPublicKey();
        PrivateKey privateKey = container.getPrivateKey();
        RSA instance = new RSA();
        byte[] signed = instance.sign(toSign, privateKey);
        instance.checkSign(signed, toSign, publicKey);
    }
}
