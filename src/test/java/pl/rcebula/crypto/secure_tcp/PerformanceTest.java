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
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author robert
 */
public class PerformanceTest
{

    public PerformanceTest()
    {
    }

    @BeforeClass
    public static void setUpClass()
    {
    }

    @AfterClass
    public static void tearDownClass()
    {
    }

    @Before
    public void setUp()
    {
    }

    @After
    public void tearDown()
    {
    }

    @Test
    public void mediumLoadoutPerformanceTest() throws Exception
    {
        System.out.println("");
        System.out.println("mediumLoadoutPerformanceTest");
        new MultipleConnectionsTest().
                multipleConnectionsEchoServerTest(8, 4, 4000);
    }

    @Test
    public void highLoadoutPerformanceTest() throws Exception
    {
        System.out.println("");
        System.out.println("highLoadoutPerformanceTest");
        new MultipleConnectionsTest().
                multipleConnectionsEchoServerTest(8, 8, 7000);
    }

    @Test
     public void veryHighLoadoutPerformanceTest() throws Exception
     {
        System.out.println("");
        System.out.println("veryHighLoadoutPerformanceTest");
        new MultipleConnectionsTest().
                multipleConnectionsEchoServerTest(8, 20, 10000);
     }
    
    /* 
     * This test gives in result propably server writing thread blocked
     * (possibly cause number of threads used)
     *
     @Test
     public void ExtremeLoadoutPerformanceTest() throws Exception
     {
     System.out.println("");
     System.out.println("ExtremeLoadoutPerformanceTest");
     multipleConnectionsEchoServerTest(12, 20, 6000);
     }*/
}
