/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto.encryption;

import pl.rcebula.crypto.encryption.RSAKeyContainer;
import java.io.File;
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
public class RSAKeyContainerTest
{
    
    public RSAKeyContainerTest()
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
    public void testConstructor() throws Exception
    {
        String privateKeyResourceName = "/private_key.der";
        String publicKeyResourceName = "/public_key.der";
        
        RSAKeyContainer instance = 
                new RSAKeyContainer(publicKeyResourceName, 
                        privateKeyResourceName);
    }
}
