package org.github.mlauper;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PancakeFlipperTest extends TestCase {

    public PancakeFlipperTest( String testName )
    {
        super( testName );
    }
    public static Test suite()
    {
        return new TestSuite( PancakeFlipperTest.class );
    }

    public void dummy()
    {
        assertTrue( true );
    }

    public void testWithStandardPancakeOrder() {
        int[] pancakeOrder = new int[]{2,1,4,3,6,5,8,7,10,9,12,11};

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);
        pancakeFlipper.solveSequencial();
    }

    public void testWithPancakeOrder2() {
        int[] pancakeOrder = new int[]{1,2,3,4,5,6,7};

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);
        pancakeFlipper.solveSequencial();
    }

    public void testWithPancakeOrder3() {
        int[] pancakeOrder = new int[]{3,2,1,4,5,6,7};

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);
        pancakeFlipper.solveSequencial();
    }

    public void testWithRandomPancakeOrderSize10(){
        int[] pancakeOrder = generateRandomPancakeOrder(10);

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);
        pancakeFlipper.solveSequencial();
    }

    public void testWithRandomPancakeOrderSize20(){
        int[] pancakeOrder = generateRandomPancakeOrder(20);

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);
        pancakeFlipper.solveSequencial();
    }

    public void testWithDeterministicPancakeOrderSize20(){
        int[] pancakeOrder = new int[]{19, 13, 10, 16, 7, 14, 11, 12, 9, 4, 3, 1, 2, 20, 18, 5, 6, 17, 8, 15};

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);
        pancakeFlipper.solveSequencial();
    }

    public void testWithRandomPancakeOrderSize30(){
        int[] pancakeOrder = generateRandomPancakeOrder(30);

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);
        pancakeFlipper.solveSequencial();
    }

    public void testWithDeterministicPancakeOrderSize40(){
        int[] pancakeOrder = new int[]{13, 36, 38, 34, 5, 28, 16, 1, 4, 3, 33, 31, 24, 14, 21, 9, 18, 26, 8, 20, 17, 35, 7, 40, 19, 6, 23, 30, 11, 29, 27, 10, 25, 39, 22, 12, 15, 2, 37, 32};

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);
        pancakeFlipper.solveSequencial();
    }

    public void testWithRandomPancakeOrderSize40(){
        int[] pancakeOrder = generateRandomPancakeOrder(40);

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);
        pancakeFlipper.solveSequencial();
    }

    public void testWithRandomPancakeOrderSize40Parallel(){
        int[] pancakeOrder = generateRandomPancakeOrder(40);

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);
        pancakeFlipper.solveParallel();
    }

    public void testWithDeterministicPancakeOrderSize44(){
        int[] pancakeOrder = new int[]{39, 27, 1, 43, 36, 33, 16, 6, 14, 26, 40, 32, 35, 22, 2, 44, 7, 8, 15, 41, 25, 21, 38, 24, 34, 19, 11, 31, 5, 4, 12, 18, 30, 17, 13, 3, 37, 9, 42, 10, 29, 23, 20, 28};

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);
        pancakeFlipper.solveSequencial();
    }

    public void testWithRandomPancakeOrderSize44(){
        int[] pancakeOrder = generateRandomPancakeOrder(44);

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);
        pancakeFlipper.solveSequencial();
    }

    public void testWithRandomPancakeOrderSize45(){
        int[] pancakeOrder = generateRandomPancakeOrder(45);

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);
        pancakeFlipper.solveSequencial();
    }

    public void testWithDeterministicPancakeOrderSize45(){
        int[] pancakeOrder = new int[]{7, 25, 15, 2, 9, 34, 24, 19, 18, 16, 17, 10, 40, 39, 1, 41, 45, 30, 21, 11, 43, 22, 28, 27, 31, 4, 8, 20, 23, 12, 42, 26, 5, 6, 13, 29, 3, 33, 44, 35, 14, 32, 38, 37, 36};

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);
        pancakeFlipper.solveSequencial();
    }

    public void testWithDeterministicPancakeOrderSize45Parallel(){
        int[] pancakeOrder = new int[]{7, 25, 15, 2, 9, 34, 24, 19, 18, 16, 17, 10, 40, 39, 1, 41, 45, 30, 21, 11, 43, 22, 28, 27, 31, 4, 8, 20, 23, 12, 42, 26, 5, 6, 13, 29, 3, 33, 44, 35, 14, 32, 38, 37, 36};

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);
        pancakeFlipper.solveParallel();
    }

    public void testWithRandomPancakeOrderSize50(){
        // Test Time: 53s
        int[] pancakeOrder = generateRandomPancakeOrder(50);

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);
        pancakeFlipper.solveSequencial();
    }

    public void testWithDeterministicPancakeOrderSize50(){
        int[] pancakeOrder = new int[]{1, 40, 16, 32, 27, 14, 46, 19, 2, 35, 7, 38, 10, 44, 43, 36, 47, 17, 29, 23, 50, 39, 48, 37, 49, 33, 30, 34, 41, 15, 20, 28, 5, 22, 3, 12, 18, 31, 4, 6, 45, 13, 26, 11, 42, 21, 9, 24, 25, 8};

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);
        pancakeFlipper.solveSequencial();
    }

    public void testWithRandomPancakeOrderSize60(){
        int[] pancakeOrder = generateRandomPancakeOrder(60);

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);
        pancakeFlipper.solveSequencial();
    }

    public void testWithDeterministicPancakeOrderSize60(){
        int[] pancakeOrder = new int[]{25, 38, 52, 50, 3, 40, 11, 6, 36, 1, 47, 41, 7, 54, 49, 55, 15, 22, 16, 45, 48, 60, 5, 59, 10, 34, 56, 32, 2, 23, 29, 39, 20, 53, 43, 14, 58, 21, 9, 46, 12, 28, 24, 30, 44, 8, 4, 27, 37, 57, 18, 51, 26, 42, 31, 13, 19, 35, 17, 33};

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);
        pancakeFlipper.solveSequencial();
    }

    public void testWithRandomPancakeOrderSize70(){
        int[] pancakeOrder = generateRandomPancakeOrder(70);

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);
        pancakeFlipper.solveSequencial();
    }

    public void testWithDeterministicPancakeOrderSize70(){
        int[] pancakeOrder = new int[]{47, 57, 5, 59, 18, 38, 7, 27, 61, 32, 55, 22, 28, 23, 51, 37, 17, 43, 63, 67, 53, 45, 8, 54, 12, 35, 2, 15, 29, 41, 50, 16, 39, 69, 70, 19, 20, 36, 60, 62, 4, 42, 24, 40, 46, 14, 25, 56, 65, 66, 52, 10, 31, 3, 6, 68, 34, 49, 48, 33, 21, 64, 11, 9, 1, 44, 26, 13, 30, 58};

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);
        pancakeFlipper.solveSequencial();
    }

    public void testStackSplitExecutesWithoutError(){
        int[] pancakeOrder = generateRandomPancakeOrder(70);

        PancakeFlipper pancakeFlipper = new PancakeFlipper(pancakeOrder);

        pancakeFlipper.splitStateStack(2);
    }


    public int[] generateRandomPancakeOrder(int size){
        int[] pancakeOrder = new int[size];

        List<Integer> pancakeOrderList = new LinkedList<>();
        for (int i = 1; i <= pancakeOrder.length; i++){
            pancakeOrderList.add(i);
        }
        Collections.shuffle(pancakeOrderList);
        for (int i = 0; i < pancakeOrder.length; i++){
            pancakeOrder[i] = pancakeOrderList.get(i);
        }

        return pancakeOrder;
    }
}