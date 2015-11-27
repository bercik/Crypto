/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rcebula.crypto;

import java.util.Random;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
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
public class AESKeyContainerTest
{

    public AESKeyContainerTest()
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

    /**
     * Test of getKey method, of class AESKeyContainer.
     *
     * @throws java.lang.Exception
     */
    @Test(expected = AESKeyContainer.BadKeyLength.class)
    public void testConstructor() throws Exception
    {
        AESKeyContainer instance = new AESKeyContainer();
        AESKeyContainer instance2
                = new AESKeyContainer(instance.getKey().getEncoded(),
                        instance.getIv().getIV());

        assertEquals(instance.getKey(), instance2.getKey());
        assertArrayEquals(instance.getIv().getIV(), instance2.getIv().getIV());

        AESKeyContainer instance3 = new AESKeyContainer();
        assertThat(instance.getKey().getEncoded(),
                IsNot.not(IsEqual.equalTo(instance3.getKey().getEncoded())));
        assertThat(instance.getIv().getIV(),
                IsNot.not(IsEqual.equalTo(instance3.getIv().getIV())));

        try
        {
            byte[] b = new byte[Names.AES_KEY_SIZE / 8];
            new Random().nextBytes(b);
            instance = new AESKeyContainer(b, b);
        }
        catch (AESKeyContainer.BadKeyLength ex)
        {
            fail();
        }

        byte[] b = new byte[Names.AES_KEY_SIZE];
        new Random().nextBytes(b);
        instance = new AESKeyContainer(b, b);
    }
}
