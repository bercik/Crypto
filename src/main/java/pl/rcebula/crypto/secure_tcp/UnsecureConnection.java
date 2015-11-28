/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto.secure_tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import pl.rcebula.crypto.encryption.AESKeyContainer;
import pl.rcebula.crypto.encryption.RSA;
import pl.rcebula.crypto.encryption.RSAKeyContainer;

/**
 *
 * @author robert
 */
public class UnsecureConnection implements IConnection
{
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private final SecureTCPServer secureTCPServer;
    
    private boolean ready;
    private AESKeyContainer aeskc;
    private final RSAKeyContainer rsakc;
    
    public UnsecureConnection(Socket socket, SecureTCPServer secureTCPServer,
            RSAKeyContainer rsakc) throws IOException
    {
        this.socket = socket;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        
        this.secureTCPServer = secureTCPServer;
        this.rsakc = rsakc;
        
        this.ready = false;
    }

    public boolean read() throws Exception
    {
        if (inputStream.available() > 0)
        {
            int length = inputStream.readInt();
            byte[] dataKey = new byte[length];
            inputStream.readFully(dataKey);
            
            length = inputStream.readInt();
            byte[] dataIv = new byte[length];
            inputStream.readFully(dataIv);
            
            RSA rsa = new RSA();
            byte[] decryptedKey = rsa.decrypt(dataKey, rsakc.getPrivateKey());
            byte[] decryptedIv = rsa.decrypt(dataIv, rsakc.getPrivateKey());
            
            aeskc = new AESKeyContainer(decryptedKey, decryptedIv);
            
            byte[] signKey = rsa.sign(decryptedKey, rsakc.getPrivateKey());
            byte[] signIv = rsa.sign(decryptedIv, rsakc.getPrivateKey());
            
            secureTCPServer.write(this, signKey);
            secureTCPServer.write(this, signIv);
            
            ready = true;
            
            return true;
        }
        else
        {
            return false;
        }
    }

    public void write(byte[] data) throws IOException
    {
        outputStream.write(data);
    }
    
    public boolean isReady()
    {
        return ready;
    }
    
    public SecureConnection getSecureConnection() throws IOException
    {
        return new SecureConnection(socket, aeskc);
    }
}
