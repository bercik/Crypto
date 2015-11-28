/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto.encryption;

/**
 *
 * @author robert
 */
public class Names
{
    private Names() { }
    
    public final static String RSA_ALGORITHM_NAME = "RSA";
    
    public final static String RSA_SIGN_ALGORITHM_NAME = "SHA256withRSA";
    
    public final static String HASH_ALGORITHM_NAME = "SHA-256";

    public final static String AES_ALGORITHM_NAME = "AES";
    
    public final static String AES_PADDING_ALGORITHM_NAME = 
            "AES/CBC/PKCS5Padding";
    
    public final static String STRING_CODING = "UTF-8";
    
    public final static int AES_KEY_SIZE = 128; // in bits
}
