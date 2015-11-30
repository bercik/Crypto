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
import sun.java2d.xr.XRUtils;

/**
 *
 * @author robert
 */
class UnsecureConnectionTimeout 
    extends ConnectionTimeoutWrapper<UnsecureConnection>
{
    public UnsecureConnectionTimeout(UnsecureConnection connection, 
            long timeout)
    {
        super(connection, timeout);
    }
}

class UnsecureConnection extends Connection
{
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private final SecureTCPServer secureTCPServer;
    
    private boolean ready;
    private AESKeyContainer aeskc;
    private final RSAKeyContainer rsakc;
    
    public UnsecureConnection(Socket socket, SecureTCPServer secureTCPServer,
            RSAKeyContainer rsakc) throws IOException
    {
        super(socket);
        
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        
        this.secureTCPServer = secureTCPServer;
        this.rsakc = rsakc;
        
        this.ready = false;
    }

    @Override
    public byte[] read() throws Exception
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
            
            ready = true;
        }
        
        return new byte[0];
    }

    @Override
    public void write(byte[] data) throws IOException
    {
        outputStream.writeInt(data.length);
        outputStream.write(data);
    }
    
    public boolean isSecureConnectionEstablished(
            UnsecureConnectionTimeout referenceToWrite) throws Exception
    {
        // jeżeli otrzymano klucz AES od klienta to odsyłamy mu nasz podpis
        // klucza
        if (ready)
        {
            RSA rsa = new RSA();
            byte[] signKey = rsa.sign(aeskc.getKey().getEncoded(), 
                    rsakc.getPrivateKey());
            byte[] signIv = rsa.sign(aeskc.getIv().getIV(), 
                    rsakc.getPrivateKey());

            secureTCPServer.write(referenceToWrite, signKey);
            secureTCPServer.write(referenceToWrite, signIv, true);
            
            ready = false;
            
            return true;
        }
        
        return false;
    }
    
    public SecureConnection getSecureConnection() throws IOException
    {
        return new SecureConnection(socket, aeskc);
    }
}
