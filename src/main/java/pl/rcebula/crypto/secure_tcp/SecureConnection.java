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
class SecureConnectionTimeout extends ConnectionTimeoutWrapper<SecureConnection>
{
    public SecureConnectionTimeout(SecureConnection connection, long timeout)
    {
        super(connection, timeout);
    }
}

class SecureConnection extends Connection
{
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    
    private final AESKeyContainer aeskc;

    public SecureConnection(Socket socket, AESKeyContainer aeskc) 
            throws IOException
    {
        super(socket);
        
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        
        this.aeskc = aeskc;
    }
    
    @Override
    public int read(ByteArray data) throws IOException, AES.DecryptionError
    {
        if (inputStream.available() > 0)
        {
            // perform reading
            int length = inputStream.readInt();
            data.array = new byte[length];
            inputStream.readFully(data.array);
            
            AES aes = new AES();
            data.array = aes.decrypt(data.array, aeskc.getKey(), 
                    aeskc.getIv());
            
            return length;
        }
        else
        {
            return -1;
        }
    }
    
    @Override
    public void write(byte[] data) throws AES.EncryptionError, IOException
    {
        if (data.length == 0)
        {
            outputStream.writeInt(0);
        }
        else
        {
            AES aes = new AES();
            byte[] encrypted = aes.encrypt(data, aeskc.getKey(), aeskc.getIv());

            outputStream.writeInt(encrypted.length);
            outputStream.write(encrypted);
        }
    }
}
