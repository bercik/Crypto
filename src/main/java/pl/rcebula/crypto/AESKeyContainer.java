/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author robert
 */
public class AESKeyContainer
{
    private final SecretKey key;
    private final IvParameterSpec iv;

    // generate random AES Names.AES_KEY_SIZE bit key
    public AESKeyContainer()
    {
        try
        {
            KeyGenerator keyGen = 
                    KeyGenerator.getInstance(Names.AES_ALGORITHM_NAME);
            keyGen.init(Names.AES_KEY_SIZE);
            key = keyGen.generateKey();
            
            byte[] array = new byte[Names.AES_KEY_SIZE / 8];
            new SecureRandom().nextBytes(array);
            iv = new IvParameterSpec(array);
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    public AESKeyContainer(byte[] keyBytes, byte[] ivBytes) 
            throws BadKeyLength, IVLengthNotEqualKeyLengthError
    {
        if ((keyBytes.length * 8) != Names.AES_KEY_SIZE)
        {
            throw new BadKeyLength();
        }
        
        if (keyBytes.length != ivBytes.length)
        {
            throw new IVLengthNotEqualKeyLengthError();
        }
        
        key = new SecretKeySpec(keyBytes, Names.AES_ALGORITHM_NAME);
        iv = new IvParameterSpec(ivBytes);
    }

    public SecretKey getKey()
    {
        return key;
    }

    public IvParameterSpec getIv()
    {
        return iv;
    }
    
    static class BadKeyLength extends Exception
    {
        public BadKeyLength()
        {
        }
    }
    
    static class IVLengthNotEqualKeyLengthError extends Exception
    {
        public IVLengthNotEqualKeyLengthError()
        {
        }
    }
}
