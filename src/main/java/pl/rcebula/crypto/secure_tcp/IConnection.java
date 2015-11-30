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
interface IConnection extends IConnectionId
{
    public abstract void write(byte[] data) throws Exception;
    
    public abstract int read(ByteArray data) throws Exception;
    
    public void close();
    
    static class ByteArray
    {
        public byte[] array;
    }
}
