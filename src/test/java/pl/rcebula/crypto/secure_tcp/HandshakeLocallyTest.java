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
import pl.rcebula.crypto.encryption.AESKeyContainer;
import pl.rcebula.crypto.encryption.RSA;
import pl.rcebula.crypto.encryption.RSAKeyContainer;

/**
 *
 * @author robert
 */
public class HandshakeLocallyTest
{
    
    public HandshakeLocallyTest()
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
    public void TestHandshakeLocally() throws Exception
    {
        // klucz publiczny i prywatny serwera
        RSAKeyContainer rsakc = 
                new RSAKeyContainer("/public_key.der", "/private_key.der");
        
        // instancja klasy rsa
        RSA rsa = new RSA();
        
        // 1. klient generuje klucz AES
        AESKeyContainer aeskc = new AESKeyContainer();
        // 2. szyfruje kluczem publicznym serwera
        byte[] encryptedAESKey = rsa.encrypt(aeskc.getKey().getEncoded(), 
                rsakc.getPublicKey());
        byte[] encryptedAESIv = rsa.encrypt(aeskc.getIv().getIV(), 
                rsakc.getPublicKey());
        
        // 3. klient wysyła dwie powyższe tablice bajtów do serwera
        // 4. serwer odszyfrowuje przysłane tablice i podpisuje swoim kluczem
        // prywatnym, zapamiętując je jako klucz używany do dalszej transmisji
        byte[] decryptedAESKey = rsa.decrypt(encryptedAESKey, 
                rsakc.getPrivateKey());
        byte[] decryptedAESIv = rsa.decrypt(encryptedAESIv,
                rsakc.getPrivateKey());
        
        AESKeyContainer serverAESKc = new AESKeyContainer(decryptedAESKey, 
                decryptedAESIv);
        
        assertArrayEquals(aeskc.getKey().getEncoded(), 
                serverAESKc.getKey().getEncoded());
        assertArrayEquals(aeskc.getIv().getIV(), serverAESKc.getIv().getIV());
        
        byte[] keySign = rsa.sign(decryptedAESKey, rsakc.getPrivateKey());
        byte[] ivSign = rsa.sign(decryptedAESIv, rsakc.getPrivateKey());
        
        // 5. serwer wysyła podpisane tablice do klienta
        // 6. klient weryfikuje podpis
        rsa.checkSign(keySign, aeskc.getKey().getEncoded(), 
                rsakc.getPublicKey());
        rsa.checkSign(ivSign, aeskc.getIv().getIV(), rsakc.getPublicKey());
    }
}
