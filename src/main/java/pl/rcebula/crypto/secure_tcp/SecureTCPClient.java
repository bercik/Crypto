/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto.secure_tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 *
 * @author robert
 */
public class SecureTCPClient
{
    private Socket socket;
    private final int port;
    private final String host;
    
    public SecureTCPClient(String host, int port) throws IOException
    {
        this.host = host;
        this.port = port;
        this.socket = new Socket();
    }
    
    public void connect(int timeout) throws IOException
    {
        socket.connect(new InetSocketAddress(host, port));
    }
    
    public byte[] read()
    {
        // TODO
        byte[] b = new byte[0];
        
        return b;
    }
    
    public void write(byte[] data)
    {
        // TODO
    }
    
    public void close()
    {
        try
        {
            socket.close();
        }
        catch (IOException ex)
        {
            
        }
    }
}
