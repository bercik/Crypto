/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto.secure_tcp;

import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author robert
 */
abstract class Connection implements IConnection
{
    protected final Socket socket;
    
    public Connection(Socket socket)
    {
        this.socket = socket;
    }
    
    @Override
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
