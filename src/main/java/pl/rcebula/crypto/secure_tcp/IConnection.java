/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto.secure_tcp;

/**
 *
 * @author robert
 */
public interface IConnection extends IConnectionId
{
    public abstract void write(byte[] data) throws Exception;
    
    public abstract byte[] read() throws Exception;
    
    public void close();
}
