/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 *
 * @author robert
 */
public final class RSA
{
    /**
     * Funkcja służy do szyfrowania przesłanej wiadomości w postaci tablicy
     * bajtów
     *
     * @param toEncrypt jest to tablica bajtów które powinniśmy zaszygrować
     * @param key jest to klucz publiczny osoby z którą się komunikujemy
     * @return funkcja zwraca zaszyfrowaną tablicę bajtów
     * @throws pl.rcebula.crypto.RSA.EncryptionError
     */
    public byte[] encrypt(byte[] toEncrypt, Key key) 
            throws EncryptionError
    {
        try
        {
            Cipher rsa = Cipher.getInstance(Names.RSA_ALGORITHM_NAME);
            rsa.init(Cipher.ENCRYPT_MODE, key);
            return rsa.doFinal(toEncrypt);
        }
        catch (BadPaddingException | IllegalBlockSizeException | 
                InvalidKeyException | NoSuchPaddingException ex)
        {
            throw new EncryptionError(ex);
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new RuntimeException("Algorytm " + Names.RSA_ALGORITHM_NAME + 
                    " nie istnieje", ex);
        }
    }

    /**
     *
     * @param toDecrypt jest to tablica bajtów która została zaszyfrowana
     * funkcją encrypt
     * @param key jest to klucz prywatny osoby która odebrała wiadomość
     * @return zwraca rozszyfrowaną tablicę bajtów z której możemy np stworzyć
     * Stringa
     * @throws pl.rcebula.crypto.RSA.DecryptionError
     */
    public byte[] decrypt(byte[] toDecrypt, Key key) 
            throws DecryptionError
    {
        try
        {
            Cipher rsa = Cipher.getInstance(Names.RSA_ALGORITHM_NAME);
            rsa.init(Cipher.DECRYPT_MODE, key);
            return rsa.doFinal(toDecrypt);
        }
        catch (BadPaddingException | IllegalBlockSizeException | 
                InvalidKeyException | NoSuchPaddingException ex)
        {
            throw new DecryptionError(ex);
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new RuntimeException("Algorytm " + Names.RSA_ALGORITHM_NAME + 
                    " nie istnieje", ex);
        }
    }

    /**
     *
     * @param toSign jest to tablica bajtów, której chcemy nadać podpis cyfrowy
     * @param privateKey jest to klucz prywatny
     * @return funkcja zwraca podpisaną tablicę bajtów
     * @throws pl.rcebula.crypto.RSA.SignError
     */
    public byte[] sign(byte[] toSign, PrivateKey privateKey) 
            throws SignError
    {
        try
        {
            Signature signature = 
                    Signature.getInstance(Names.RSA_SIGN_ALGORITHM_NAME);
            signature.initSign(privateKey);
            signature.update(toSign);
            return signature.sign();
        }
        catch (InvalidKeyException | SignatureException ex)
        {
            throw new SignError(ex);
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new RuntimeException("Algorytm " + 
                    Names.RSA_SIGN_ALGORITHM_NAME + " nie istnieje", ex);
        }
    }

    public void checkSign(byte[] recvSign, byte[] message, 
            PublicKey publicKey) throws CheckSignError
    {
        try
        {
            Signature signature = 
                    Signature.getInstance(Names.RSA_SIGN_ALGORITHM_NAME);
            signature.initVerify(publicKey);
            signature.update(message);

            if (signature.verify(recvSign) == false)
            {
                throw new CheckSignError();
            }
        }
        catch (InvalidKeyException | SignatureException ex)
        {
            throw new CheckSignError(ex);
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new RuntimeException("Algorytm " + 
                    Names.RSA_SIGN_ALGORITHM_NAME + " nie istnieje", ex);
        }
    }

    static class CheckSignError extends Exception
    {
        public CheckSignError()
        {
        }

        public CheckSignError(Throwable cause)
        {
            super(cause);
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
    
    static class SignError extends Exception
    {
        public SignError()
        {
        }

        public SignError(Throwable cause)
        {
            super(cause);
        }
    }
}
