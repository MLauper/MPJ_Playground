package org.github.mlauper;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PancakeTest extends TestCase {
    public PancakeTest( String testName )
    {
        super( testName );
    }
    public static Test suite()
    {
        return new TestSuite( PancakeTest.class );
    }

    public void testPancake()
    {

        assertTrue( true );
    }
}
