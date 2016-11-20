package org.github.mlauper;

import github.mlauper.Pancake;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Arrays;

public class PancakeTest extends TestCase {
    public PancakeTest( String testName )
    {
        super( testName );
    }
    public static Test suite()
    {
        return new TestSuite( PancakeTest.class );
    }

    private int[] pancakeList1 = new int[]{2, 1, 4, 3, 6, 5};

    public void dummy()
    {
        assertTrue( true );
    }

    public void testIfPancakeCanBeInitialized()
    {
        Pancake pancake = new Pancake(pancakeList1);
        assertNotNull(pancake);
    }

    public void testIfPancakeCanBeInitializedWithIntArray()
    {
        int[] pancakeOrder = {1,2,3,4,5,6};
        Pancake pancake = new Pancake(pancakeOrder);
        assertNotNull(pancake);
    }

    public void testIfPancakeCanBePrinted()
    {
        Pancake pancake = new Pancake(pancakeList1);
        try {
            pancake.printPancakes();
        } catch (Exception ex){
            fail(ex.getMessage());
        }
    }

    //public void testIfPancakeFlipsCanBeCalculated()
    //{
    //    int[] orderedPancakeList = new int[]{1,6,2,3,5,4));
    //    Pancake pancake = new Pancake(orderedPancakeList);
//
    //    pancake.calculateFlips();
    //    pancake.printPancakes();
    //}
//
    //public void testIfPancakeFlipsCanBeCalculatedForAlreadyOrderedPancakes()
    //{
    //    int[] orderedPancakeList = new int[]{1,2,3,4,5,6));
    //    Pancake pancake = new Pancake(orderedPancakeList);
//
    //    pancake.calculateFlips();
    //    pancake.printPancakes();
    //}

    public void testIfPancakeChecksOrderingCorrectOnOrderedPancakes(){
        int[] orderedPancakeList = new int[]{1,2,3,4,5,6};
        Pancake pancake = new Pancake(orderedPancakeList);
        assertTrue(pancake.isPancakeOrdered());
    }

    public void testIfPancakeChecksOrderingCorrectOnUnorderedPancakes(){
        int[] orderedPancakeList = new int[]{1,2,3,6,5,4};
        Pancake pancake = new Pancake(orderedPancakeList);
        assertFalse(pancake.isPancakeOrdered());
    }

    public void testIfPancakesAreReturned(){
        int[] pancakes = new int[]{1,2,3,6,5,4};
        Pancake pancake = new Pancake(pancakes);

        int[] pancakes2 = pancake.getPancakes();


        assertTrue(Arrays.equals(pancakes2, pancakes));
    }

    public void testIfPancakesAreFlipped(){
        int[] pancakes = new int[]{1,2,3,6,5,4};
        int[] flippedPancakes = new int[]{0,1,2,3,4,5,6};
        Pancake pancake = new Pancake(pancakes);

        pancake.flip(3);

        pancake.printPancakes();
        System.out.println(Arrays.toString(flippedPancakes));
        assertTrue(Arrays.equals(flippedPancakes, pancake.getPancakes()));
    }

    public void testIfChildStatesCanBeRetrieved(){
        int[] pancakes = new int[]{1,2,3,4,5};
        Pancake pancake = new Pancake(pancakes);

        int[][] childPancakes =
                new int[][]{
                        new int[]{0,5,4,3,2,1},
                        new int[]{0,1,5,4,3,2},
                        new int[]{0,1,2,5,4,3},
                        new int[]{0,1,2,3,5,4}
                };

        assertEquals(childPancakes, pancake.getChildStates());
        System.out.println(pancake.getChildStates());
    }

    public void testIfHeuristicCanBeCalculatedOnSortedPancake() {
        int[] pancakes = new int[]{1,2,3,4,5};
        Pancake pancake = new Pancake(pancakes);

        assertEquals(0, pancake.getHeuristic());
    }

    public void testIfHeuristicCanBeCalculated() {
        int[] pancakeOrder1 = new int[]{1,3,2,4,5};
        int[] pancakeOrder2 = new int[]{5,4,3,2,1};
        int[] pancakeOrder3 = new int[]{1,2,3,6,5};
        int[] pancakeOrder4 = new int[]{2,1,4,3,6,5,8,7,10,9,12,13,14};
        Pancake pancake1 = new Pancake(pancakeOrder1);
        Pancake pancake2 = new Pancake(pancakeOrder2);
        Pancake pancake3 = new Pancake(pancakeOrder3);
        Pancake pancake4 = new Pancake(pancakeOrder4);

        assertEquals(2, pancake1.getHeuristic());
        assertEquals(1, pancake2.getHeuristic());
        assertEquals(1, pancake3.getHeuristic());
        assertEquals(6, pancake4.getHeuristic());
    }
}
