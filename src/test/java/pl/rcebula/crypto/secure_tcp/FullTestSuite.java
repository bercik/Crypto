/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto.secure_tcp;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author robert
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
{
    pl.rcebula.crypto.secure_tcp.PerformanceTest.class, 
    pl.rcebula.crypto.secure_tcp.SecureTCPServerTest.class, 
    pl.rcebula.crypto.secure_tcp.HandshakeRemotelyTest.class, 
    pl.rcebula.crypto.secure_tcp.TimeoutTest.class, 
    pl.rcebula.crypto.secure_tcp.DataSendTest.class, 
    pl.rcebula.crypto.secure_tcp.CloseConnectionTest.class, 
    pl.rcebula.crypto.secure_tcp.HandshakeLocallyTest.class, 
    pl.rcebula.crypto.secure_tcp.FastTestSuite.class
})
public class FullTestSuite
{

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }
    
}
