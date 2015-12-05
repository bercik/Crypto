/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto.encryption;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author robert
 */
public final class SHA
{
    public byte[] hash(byte[] toHash)
    {
        try
        {
            MessageDigest messageDigest = 
                    MessageDigest.getInstance(Names.HASH_ALGORITHM_NAME);
            messageDigest.update(toHash);
            return messageDigest.digest();
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new RuntimeException("Algorytm " + Names.HASH_ALGORITHM_NAME + 
                    " nie istnieje", ex);
        }
    }
}
