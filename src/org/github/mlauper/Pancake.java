package github.mlauper;

import java.util.Arrays;
import java.util.LinkedList;

public class Pancake {

    private int[] pancakes;
    private int[] candidatePancakes = new int[0];
    private int[] failedPancakes = new int[0];
    private Boolean debug = false;
    private int pancakeSize;

    public Pancake(int[] pancakeOrder){
        if(pancakeOrder[0] != 0) {
            this.pancakes = new int[pancakeOrder.length+1];
            this.pancakes[0] = 0;
            System.arraycopy(pancakeOrder, 0, this.pancakes, 1, pancakeOrder.length);
        }else {
            this.pancakes = pancakeOrder;
        }

        this.pancakeSize = this.pancakes.length;
    }

    public void printPancakes (){
        System.out.println(Arrays.toString(pancakes));
    }

    //public void calculateFlips () {
    //    while(!this.isPancakeOrdered()){
    //        failedPancakes.add((LinkedList<Integer>) pancakes.clone());
    //        for (LinkedList<Integer> pancakeCandidate :
    //                this.getChildStates()) {
    //            if(!candidatePancakes.contains(pancakeCandidate) && !failedPancakes.contains(pancakeCandidate)){
    //                candidatePancakes.add(pancakeCandidate);
    //            }
    //        }
//
    //        this.pancakes = candidatePancakes.pop();
    //    }
    //}
//
    public boolean isPancakeOrdered(){
        int size = pancakes.length;
        for (int i = 0; i < size-1; i++) {
            if (pancakes[i] + 1 != pancakes[i+1]){
                return false;
            }
        }
        return true;
    }

    public int[] flipped(int Position){
        int[] flippedPancakes = new int[this.pancakeSize];

        System.arraycopy(this.pancakes, 0, flippedPancakes, 0, Position);

        for (int i = Position; i < pancakeSize; i++){
            flippedPancakes[i] = this.pancakes[pancakeSize - i];
        }

        return flippedPancakes;
    }

    public void flip(int Position){
        this.pancakes = this.flipped(Position);
    }

    public int[] getPancakes(){
        return pancakes;
    }

    public int[][] getChildStates() {
        // Reserve space for the maximum number of children
        int[][] childStates = new int[this.pancakeSize][this.pancakeSize];

        for (int i = 1; i < this.pancakeSize; i++) {
            childStates[i] = this.flipped(i+1);
        }
        return childStates;
    }

    public int getHeuristic() {
        int heuristicNumber = 0;
        int x,y;

        for (int i = 1; i < this.pancakeSize; i++){
            x = pancakes[i-1];
            y = pancakes[i];
            if (x + 1 != y && x - 1 != y){
                heuristicNumber += 1;
            }
        }

        return heuristicNumber;
    }
}
