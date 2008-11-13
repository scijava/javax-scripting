package com.sun.script.jruby.test;

import org.junit.Test;

/**
 * LocaScopeSampleTest.java
 * @author Yoko Harada
 */
public class LocalScopeSampleTest {

    public LocalScopeSampleTest() {
    }

    /**
     * Test of main method, of class LocalScopeSample.
     */
    @Test
    public void testMain() throws Exception {
        System.out.println("main");
        String[] args = null;
        LocalScopeSample.main(args);
    }

}