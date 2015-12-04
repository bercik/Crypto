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
public class DurabilityTest
{

    public DurabilityTest()
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
    public void lowLoadoutLongPerformanceTest() throws Exception
    {
        System.out.println("");
        System.out.println("lowLoadoutLongPerformanceTest");
        new MultipleConnectionsTest().
                multipleConnectionsEchoServerTest(4, 4, 60000);
    }

    @Test
    public void mediumLoadoutLongPerformanceTest() throws Exception
    {
        System.out.println("");
        System.out.println("mediumLoadoutLongPerformanceTest");
        new MultipleConnectionsTest().
                multipleConnectionsEchoServerTest(8, 4, 60000);
    }

    /*
    @Test
    public void highLoadoutLongPerformanceTest() throws Exception
    {
        System.out.println("");
        System.out.println("highLoadoutLongPerformanceTest");
        new MultipleConnectionsTest().
                multipleConnectionsEchoServerTest(8, 8, 30000);
    }*/

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
