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
public interface IReadCallback
{
    public void dataRead(byte[] data, IConnectionId connection, 
            SecureTCPServer server);
}
