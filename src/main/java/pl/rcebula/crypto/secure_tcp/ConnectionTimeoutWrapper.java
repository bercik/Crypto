/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto.secure_tcp;

/**
 *
 * @author robert
 * @param <T>
 */
class ConnectionTimeoutWrapper<T extends Connection> implements IConnection
{
    private final T connection;

    private final long timeout;
    private long lastWriteRead;

    private boolean bTimeout;

    public ConnectionTimeoutWrapper(T connection, long timeout)
    {
        this.connection = connection;
        this.timeout = timeout;
        this.lastWriteRead = System.currentTimeMillis();
        this.bTimeout = false;
    }

    @Override
    public void write(byte[] data) throws Exception
    {
        lastWriteRead = System.currentTimeMillis();

        connection.write(data);
    }

    @Override
    public int read(ByteArray data) throws Exception
    {
        int bytesRead = connection.read(data);

        if (bytesRead >= 0)
        {
            lastWriteRead = System.currentTimeMillis();
        }
        else if ((System.currentTimeMillis() - lastWriteRead) > timeout)
        {
            bTimeout = true;
        }

        return bytesRead;
    }

    @Override
    public void close()
    {
        connection.close();
    }

    public boolean isTimeout()
    {
        boolean tmp = bTimeout;
        bTimeout = false;

        return tmp;
    }

    public T getConnection()
    {
        return connection;
    }
}
