/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

/**
 *
 * @author robert
 */
public class AES
{
    public byte[] decrypt(byte[] toDecrypt, Key key, IvParameterSpec iv) 
            throws DecryptionError
    {
        try
        {
            // Instantiate the cipher
            Cipher cipher = Cipher.getInstance(Names.AES_PADDING_ALGORITHM_NAME);

            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            return cipher.doFinal(toDecrypt);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException ex)
        {
            throw new RuntimeException(ex);
        }
        catch (InvalidKeyException | IllegalBlockSizeException | 
                BadPaddingException | InvalidAlgorithmParameterException ex)
        {
            throw new DecryptionError(ex);
        }
    }

    public byte[] encrypt(byte[] toEncrypt, Key key, IvParameterSpec iv) 
            throws EncryptionError
    {
        try
        {
            // Instantiate the cipher
            Cipher cipher = Cipher.getInstance(Names.AES_PADDING_ALGORITHM_NAME);

            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            return cipher.doFinal(toEncrypt);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException ex)
        {
            throw new RuntimeException(ex);
        }
        catch (InvalidKeyException | IllegalBlockSizeException | 
                BadPaddingException | InvalidAlgorithmParameterException ex)
        {
            throw new EncryptionError(ex);
        }
    }
    
    static class EncryptionError extends Exception
    {
        public EncryptionError()
        {
        }

        public EncryptionError(Throwable cause)
        {
            super(cause);
        }
    }
    
    static class DecryptionError extends Exception
    {
        public DecryptionError()
        {
        }

        public DecryptionError(Throwable cause)
        {
            super(cause);
        }
    }
}
