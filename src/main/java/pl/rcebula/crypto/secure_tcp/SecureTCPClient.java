/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto.secure_tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import pl.rcebula.crypto.encryption.AES;
import pl.rcebula.crypto.encryption.AESKeyContainer;
import pl.rcebula.crypto.encryption.RSA;
import pl.rcebula.crypto.encryption.RSAKeyContainer;

/**
 *
 * @author robert
 */
public class SecureTCPClient
{
    private SecureConnection secureConnection;
    private AESKeyContainer aeskc;
    private final RSAKeyContainer rsakc;
    
    private final int port;
    private final String host;
    
    private boolean connected;
    
    public SecureTCPClient(String host, int port, RSAKeyContainer serverRsakc) 
            throws IOException
    {
        this.host = host;
        this.port = port;
        this.connected = false;
        this.rsakc = serverRsakc;
    }
    
    public void connect() throws Exception
    {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port));
        
        afterConnect(socket);
    }
    
    public void connect(int timeout) throws Exception
    {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), timeout);
        
        afterConnect(socket);
    }
    
    private void afterConnect(Socket socket) 
            throws RSA.EncryptionError, IOException, RSA.CheckSignError
    {
        // generujemy i wysyłamy do serwera zaszyfrowany klucz AES
        aeskc = new AESKeyContainer();
        RSA rsa = new RSA();
        byte[] encryptedKey = rsa.encrypt(aeskc.getKey().getEncoded(), 
                rsakc.getPublicKey());
        byte[] encryptedIv = rsa.encrypt(aeskc.getIv().getIV(), 
                rsakc.getPublicKey());
        
        DataInputStream inputStream = 
                new DataInputStream(socket.getInputStream());
        DataOutputStream outputStream = 
                new DataOutputStream(socket.getOutputStream());
        
        outputStream.writeInt(encryptedKey.length);
        outputStream.write(encryptedKey);
        
        outputStream.writeInt(encryptedIv.length);
        outputStream.write(encryptedIv);
        
        // odbieramy od serwera podpisany klucz AES i sprawdzamy czy 
        // jest poprawny
        int length = inputStream.readInt();
        byte[] signKey = new byte[length];
        inputStream.readFully(signKey);
        
        length = inputStream.readInt();
        byte[] signIv = new byte[length];
        inputStream.readFully(signIv);
        
        rsa.checkSign(signKey, aeskc.getKey().getEncoded(), 
                rsakc.getPublicKey());
        rsa.checkSign(signIv, aeskc.getIv().getIV(), rsakc.getPublicKey());
        
        connected = true;
        secureConnection = new SecureConnection(socket, aeskc);
    }
    
    public byte[] read() throws IOException, AES.DecryptionError
    {
        byte[] readData = new byte[0];
        
        while (readData.length == 0)
        {
            readData = secureConnection.read();
        }
        
        return readData;
    }
    
    public void write(byte[] data) throws AES.EncryptionError, IOException
    {
        secureConnection.write(data);
    }
    
    public void close()
    {
        connected = false;
        
        secureConnection.close();
    }
}
