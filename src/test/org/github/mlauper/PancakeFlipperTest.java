package org.github.mlauper;

import github.mlauper.PancakeFlipper;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PancakeFlipperTest extends TestCase {
    public PancakeFlipperTest( String testName )
    {
        super( testName );
    }
    public static Test suite()
    {
        return new TestSuite( PancakeTest.class );
    }

    public void dummy()
    {
        assertTrue( true );
    }

    public void testWithStandardPancakeOrder() {
        int[] pancakeOrder = new int[]{2,1,4,3,6,5,8,7,10,9,12,13};

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);

    }
}