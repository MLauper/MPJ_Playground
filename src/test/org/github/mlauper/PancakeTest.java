package org.github.mlauper;

import github.mlauper.Pancake;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class PancakeTest extends TestCase {
    public PancakeTest( String testName )
    {
        super( testName );
    }
    public static Test suite()
    {
        return new TestSuite( PancakeTest.class );
    }

    private LinkedList<Integer> pancakeList1 = new LinkedList<>(Arrays.asList(2, 1, 4, 3, 6, 5));

    public void dummy()
    {
        assertTrue( true );
    }

    public void testIfPancakeCanBeInitialized()
    {
        Pancake pancake = new Pancake(pancakeList1);
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

    public void testIfPancakeFlipsCanBeCalculated()
    {
        LinkedList<Integer> orderedPancakeList = new LinkedList<>(Arrays.asList(1,3,2,4,5,6));
        Pancake pancake = new Pancake(orderedPancakeList);

        pancake.calculateFlips();
        pancake.printPancakes();
    }

    public void testIfPancakeFlipsCanBeCalculatedForAlreadyOrderedPancakes()
    {
        LinkedList<Integer> orderedPancakeList = new LinkedList<>(Arrays.asList(1,2,3,4,5,6));
        Pancake pancake = new Pancake(orderedPancakeList);

        pancake.calculateFlips();
        pancake.printPancakes();
    }

    public void testIfPancakeChecksOrderingCorrectOnOrderedPancakes(){
        LinkedList<Integer> orderedPancakeList = new LinkedList<>(Arrays.asList(1,2,3,4,5,6));
        Pancake pancake = new Pancake(orderedPancakeList);
        assertTrue(pancake.isPancakeOrdered());
    }

    public void testIfPancakeChecksOrderingCorrectOnUnorderedPancakes(){
        LinkedList<Integer> orderedPancakeList = new LinkedList<>(Arrays.asList(1,2,3,6,5,4));
        Pancake pancake = new Pancake(orderedPancakeList);
        assertFalse(pancake.isPancakeOrdered());
    }

    public void testIfPancakesAreReturned(){
        LinkedList<Integer> pancakes = new LinkedList<>(Arrays.asList(1,2,3,6,5,4));
        Pancake pancake = new Pancake(pancakes);

        LinkedList<Integer> pancakes2 = pancake.getPancakes();

        assertEquals(pancakes,pancakes2);
    }

    public void testIfPancakesAreFlipped(){
        LinkedList<Integer> pancakes = new LinkedList<>(Arrays.asList(1,2,3,6,5,4));
        LinkedList<Integer> flipedPancakes = new LinkedList<>(Arrays.asList(1,2,3,4,5,6));
        Pancake pancake = new Pancake(pancakes);

        pancake.flip(3);

        assertEquals(flipedPancakes, pancake.getPancakes());
    }

    public void testIfChildStatesCanBeRetrieved(){
        LinkedList<Integer> pancakes = new LinkedList<>(Arrays.asList(1,2,3,4,5));
        Pancake pancake = new Pancake(pancakes);

        LinkedList<LinkedList<Integer>> childPancakes =
                new LinkedList<>(Arrays.asList(
                        new LinkedList<>(Arrays.asList(1,5,4,3,2)),
                        new LinkedList<>(Arrays.asList(1,2,5,4,3)),
                        new LinkedList<>(Arrays.asList(1,2,3,5,4))
                ));

        assertEquals(childPancakes, pancake.getChildStates());
        System.out.println(pancake.getChildStates());
    }
}
