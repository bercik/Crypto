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
import pl.rcebula.crypto.encryption.AES;
import pl.rcebula.crypto.encryption.AESKeyContainer;

/**
 *
 * @author robert
 */
public class SecureConnection implements IConnection
{
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    
    private final AESKeyContainer aeskc;

    public SecureConnection(Socket socket, AESKeyContainer aeskc) 
            throws IOException
    {
        this.socket = socket;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        
        this.aeskc = aeskc;
    }
    
    public byte[] read() throws IOException, AES.DecryptionError
    {
        if (inputStream.available() > 0)
        {
            // perform reading
            int length = inputStream.readInt();
            byte[] data = new byte[length];
            inputStream.readFully(data);
            
            AES aes = new AES();
            byte[] decrypted = aes.decrypt(data, aeskc.getKey(), aeskc.getIv());
            
            return decrypted;
        }
        else
        {
            return new byte[0];
        }
    }
    
    public void write(byte[] data) throws AES.EncryptionError, IOException
    {
        AES aes = new AES();
        byte[] encrypted = aes.encrypt(data, aeskc.getKey(), aeskc.getIv());
        
        outputStream.write(encrypted);
    }
}
