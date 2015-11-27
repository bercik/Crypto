/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 *
 * @author robert
 */
public final class RSAKeyContainer
{
    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    public RSAKeyContainer(String publicKeyResourceName) throws Exception
    {
        privateKey = null;
        publicKey = getPublicKeyFromFile(publicKeyResourceName);
    }

    public RSAKeyContainer(String publicKeyResourceName,
            String privateKeyResourceName)
            throws Exception
    {
        publicKey = getPublicKeyFromFile(publicKeyResourceName);
        privateKey = getPrivateKeyFromFile(privateKeyResourceName);
    }

    public PublicKey getPublicKey()
    {
        return publicKey;
    }

    public PrivateKey getPrivateKey()
    {
        return privateKey;
    }

    private byte[] readAllBytes(String resourceName) throws IOException,
            URISyntaxException
    {
        byte[] bytes;

        File f = new File(this.getClass().getResource(resourceName).toURI());
        FileInputStream fis = new FileInputStream(f);
        try (DataInputStream dis = new DataInputStream(fis))
        {
            bytes = new byte[(int)f.length()];
            dis.readFully(bytes);
        }

        return bytes;
    }

    private PrivateKey getPrivateKeyFromFile(String resourceName)
            throws Exception
    {
        byte[] keyBytes = readAllBytes(resourceName);

        PKCS8EncodedKeySpec spec
                = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance(Names.RSA_ALGORITHM_NAME);

        return kf.generatePrivate(spec);
    }

    private PublicKey getPublicKeyFromFile(String resourceName)
            throws Exception
    {
        byte[] keyBytes = readAllBytes(resourceName);

        X509EncodedKeySpec spec
                = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance(Names.RSA_ALGORITHM_NAME);
        
        return kf.generatePublic(spec);
    }
}
