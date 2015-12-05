/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto.secure_tcp.utils;

/**
 *
 * @author robert
 */
public class PortGiver
{
    private static int port = 14000;

    public static int getPort()
    {
        return port++;
    }
}