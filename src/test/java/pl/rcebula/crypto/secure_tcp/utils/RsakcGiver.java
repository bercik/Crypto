/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto.secure_tcp.utils;

import pl.rcebula.crypto.encryption.RSAKeyContainer;

/**
 *
 * @author robert
 */
public class RsakcGiver
{
    private static final String publicKey = "/public_key.der";
    private static final String privateKey = "/private_key.der";

    private static final RSAKeyContainer serverRsakc;
    private static final RSAKeyContainer clientRsakc;

    static
    {
        try
        {
            serverRsakc
                    = new RSAKeyContainer(publicKey, privateKey);
            clientRsakc
                    = new RSAKeyContainer(publicKey);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public static RSAKeyContainer getServerRsakc()
    {
        return serverRsakc;
    }

    public static RSAKeyContainer getClientRsakc()
    {
        return clientRsakc;
    }
}
