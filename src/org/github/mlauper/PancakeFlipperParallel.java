package org.github.mlauper;

import mpi.MPI;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;


public class PancakeFlipperParallel implements Serializable {

    public ArrayDeque<State> stateStack = new ArrayDeque<State>();

    // Describes a candidate for the new search limit
    public int candidateLimit = Integer.MAX_VALUE;
    int limit;
    int stateLimit;

    int rank;

    public class State implements Serializable {
        public int[] pancakeOrder;
        public int heuristicNumber;
        public int depth;
        public Stack<State> unexploredChilds = new Stack<>();
        public int flipFromParent;

        private int pancakeSize;

        public State(int[] pancakeOrder, int depth, int flipFromParent){
            this.depth = depth;
            this.pancakeOrder = pancakeOrder;

            this.pancakeSize = pancakeOrder.length;
            this.calculateHeuristicNumber();
            this.flipFromParent = flipFromParent;
        }

        public State(int[] pancakeOrder, int depth, int flipFromParent, int heuristicNumber){
            this.depth = depth;
            this.pancakeOrder = pancakeOrder;
            this.heuristicNumber = heuristicNumber;

            this.pancakeSize = pancakeOrder.length;
            this.flipFromParent = flipFromParent;
        }


        public void exploreChildNodes() {
            // Start at 2 because to flip no element or only the first element doesn't make much sense
            for (int i = 2; i < pancakeSize; i++){
                // Only add childState if we do not break up already good sequences
                if (Math.abs(pancakeOrder[i-1] - pancakeOrder[i]) > 1){
                    // We can calculate the heuristic number based on the flip we are going to do
                    // This removes the need to loop through the entire array again
                    if (Math.abs(pancakeOrder[i] - pancakeOrder[0]) > 1) {
                        // We do not add child if we would reach our limit anyway
                        // If we would step over the limit, we will step exactly one over the limit
                        // There is no need to calculate the heuristic explicitly
                        //if (this.heuristicNumber + this.depth+1 < limit){
                            unexploredChilds.push(new State(this.flippedPancakeOrder(i), this.depth + 1, i, this.heuristicNumber));
                        //} else {
                        //    if (limit == 0) {break;}
                        //    candidateLimit = limit + 1;
                        //    System.out.printf("Test global limit against: %d // limit: %d // currentCandidateLimit: %d\n", limit + 1, limit, candidateLimit);
                        //}
                    } else {
                        // If the heuristic is being improved, there is no need to check if the limit is reached,
                        // otherwise we wouldn't have reached the node in the first place
                        unexploredChilds.push(new State(this.flippedPancakeOrder(i), this.depth + 1, i, this.heuristicNumber - 1));
                    }
                }
                // this.heuristicNumber-1 + this.depth+1
            }
        }

        public int[] flippedPancakeOrder(int Position){
            int[] flippedPancakes = new int[this.pancakeSize];

            // Copy elements, that should not be flipped
            System.arraycopy(this.pancakeOrder, Position, flippedPancakes, Position, this.pancakeSize-Position);

            // Copy flipped elements in reverse order
            for (int i = 0; i < Position; i++){
                flippedPancakes[i] = this.pancakeOrder[pancakeSize - (pancakeSize - Position) - i - 1];
            }

            return flippedPancakes;
        }

        private void calculateHeuristicNumber(){
            this.heuristicNumber = 0;
            for (int i = 0; i < pancakeSize-1; i++){
                if (Math.abs(pancakeOrder[i] - pancakeOrder[i+1]) > 1){
                    this.heuristicNumber += 1;
                }
            }
        }

    }

    public PancakeFlipperParallel(int[] pancakeOrder) {
        int[] augmentedPancakeOrder = new int[pancakeOrder.length + 1];

        System.arraycopy(pancakeOrder, 0, augmentedPancakeOrder, 0, pancakeOrder.length);
        augmentedPancakeOrder[pancakeOrder.length] = pancakeOrder.length + 1;

        stateStack.push(new State(augmentedPancakeOrder, 0, 0));
        // Initially set heuristic number as limit
        limit = stateStack.peek().heuristicNumber;

        stateStack.peek().exploreChildNodes();
    }

    public void solveSequential(){

        long startTime = System.nanoTime();
        // Search until heuristicNumber is 0, which means that a solution was found
        while (stateStack.peek().heuristicNumber != 0) {

            // Check if limit is reached
            if ((stateStack.peek().heuristicNumber + stateStack.peek().depth) > limit){
                // Solution with current limit not available in current path, try going back
                // Record new candidate limit
                stateLimit = stateStack.peek().depth + stateStack.peek().heuristicNumber;
                candidateLimit = stateLimit < candidateLimit ? stateLimit : candidateLimit;
                stateStack.pop();
            } else if (stateStack.peek().unexploredChilds.empty()) {
                if (stateStack.peek().depth == 0){
                    // At root node, increase limit and explore children again
                    limit = candidateLimit;
                    candidateLimit = Integer.MAX_VALUE;
                    stateStack.peek().exploreChildNodes();
                } else {
                  // No more paths to go, go up
                  stateStack.pop();
                }
            } else {
                // Try next path
                this.stateStack.push(this.stateStack.peek().unexploredChilds.pop());
                stateStack.peek().exploreChildNodes();
            }

        }
        long endTime = System.nanoTime();

        this.printSolution();
        System.out.printf("Run required %d ms", (endTime-startTime)/1000/1000);
    }



    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        //int[] initialPancakeOrder = new int[]{2, 1, 4, 3, 6, 5, 8, 7, 10, 9, 12, 11};
        //int[] initialPancakeOrder = new int[]{2, 1, 4, 3, 6, 5, 8, 7, 10, 9, 12, 11, 14, 13, 16, 15};

        //int[] initialPancakeOrder = new int[]{19, 13, 10, 16, 7, 14, 11, 12, 9, 4, 3, 1, 2, 20, 18, 5, 6, 17, 8, 15};
        //int[] initialPancakeOrder = new int[]{13, 36, 38, 34, 5, 28, 16, 1, 4, 3, 33, 31, 24, 14, 21, 9, 18, 26, 8, 20, 17, 35, 7, 40, 19, 6, 23, 30, 11, 29, 27, 10, 25, 39, 22, 12, 15, 2, 37, 32};
        //int[] initialPancakeOrder = new int[]{7, 25, 15, 2, 9, 34, 24, 19, 18, 16, 17, 10, 40, 39, 1, 41, 45, 30, 21, 11, 43, 22, 28, 27, 31, 4, 8, 20, 23, 12, 42, 26, 5, 6, 13, 29, 3, 33, 44, 35, 14, 32, 38, 37, 36};
        //int[] initialPancakeOrder = new int[]{1, 40, 16, 32, 27, 14, 46, 19, 2, 35, 7, 38, 10, 44, 43, 36, 47, 17, 29, 23, 50, 39, 48, 37, 49, 33, 30, 34, 41, 15, 20, 28, 5, 22, 3, 12, 18, 31, 4, 6, 45, 13, 26, 11, 42, 21, 9, 24, 25, 8};
        //int[] initialPancakeOrder = new int[]{25, 38, 52, 50, 3, 40, 11, 6, 36, 1, 47, 41, 7, 54, 49, 55, 15, 22, 16, 45, 48, 60, 5, 59, 10, 34, 56, 32, 2, 23, 29, 39, 20, 53, 43, 14, 58, 21, 9, 46, 12, 28, 24, 30, 44, 8, 4, 27, 37, 57, 18, 51, 26, 42, 31, 13, 19, 35, 17, 33};
        //int[] initialPancakeOrder = new int[]{47, 57, 5, 59, 18, 38, 7, 27, 61, 32, 55, 22, 28, 23, 51, 37, 17, 43, 63, 67, 53, 45, 8, 54, 12, 35, 2, 15, 29, 41, 50, 16, 39, 69, 70, 19, 20, 36, 60, 62, 4, 42, 24, 40, 46, 14, 25, 56, 65, 66, 52, 10, 31, 3, 6, 68, 34, 49, 48, 33, 21, 64, 11, 9, 1, 44, 26, 13, 30, 58};

        //int[] initialPancakeOrder = PancakeFlipper.generateRandomPancakeOrder(60);



        /* Reference Solution 1: */
        //int[] initialPancakeOrder = new int[]{2, 1, 4, 3, 6, 5, 8, 7, 10, 9, 12, 11, 14, 13};
        /*
            state 0: 2, 1, 4, 3, 6, 5, 8, 7, 10, 9, 12, 11, 14, 13, 15
            state 1: 4, 1, 2| 3, 6, 5, 8, 7, 10, 9, 12, 11, 14, 13, 15
            state 2: 6, 3, 2, 1, 4| 5, 8, 7, 10, 9, 12, 11, 14, 13, 15
            state 3: 8, 5, 4, 1, 2, 3, 6| 7, 10, 9, 12, 11, 14, 13, 15
            state 4: 10, 7, 6, 3, 2, 1, 4, 5, 8| 9, 12, 11, 14, 13, 15
            state 5: 12, 9, 8, 5, 4, 1, 2, 3, 6, 7, 10| 11, 14, 13, 15
            state 6: 14, 11, 10, 7, 6, 3, 2, 1, 4, 5, 8, 9, 12| 13, 15
            state 7: 13, 12, 9, 8, 5, 4, 1, 2, 3, 6, 7, 10, 11, 14| 15
            state 8: 11, 10, 7, 6, 3, 2, 1, 4, 5, 8, 9, 12, 13| 14, 15
            state 9: 9, 8, 5, 4, 1, 2, 3, 6, 7, 10, 11| 12, 13, 14, 15
            state 10: 7, 6, 3, 2, 1, 4, 5, 8, 9| 10, 11, 12, 13, 14, 15
            state 11: 5, 4, 1, 2, 3, 6, 7| 8, 9, 10, 11, 12, 13, 14, 15
            state 12: 3, 2, 1, 4, 5| 6, 7, 8, 9, 10, 11, 12, 13, 14, 15
            state 13: 1, 2, 3| 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15
            time: 0.234752767 sec
        */

        /* Reference Solution 2: */
        int[] initialPancakeOrder = new int[]{2, 1, 4, 3, 6, 5, 8, 7, 10, 9, 12, 11, 14, 13, 16, 15};
        /*
            451 Solutions
            state 0: 2, 1, 4, 3, 6, 5, 8, 7, 10, 9, 12, 11, 14, 13, 16, 15, 17
            time: 5.907583366 sec
         */

        PancakeFlipperParallel pancakeFlipper = new PancakeFlipperParallel(initialPancakeOrder);

        MPI.Init(args);
        pancakeFlipper.rank = MPI.COMM_WORLD.Rank();
        int numOfRanks = MPI.COMM_WORLD.Size();
        int N = initialPancakeOrder.length;

        if (pancakeFlipper.rank == 0) {
            // Start Control Rank logic
            pancakeFlipper.startControlRank();
        } else {
            pancakeFlipper.solveParallel();
        }

    }

    private void startControlRank() {
        long startTime = System.nanoTime();
            this.printCurrentState();
            this.buidInitialSearchStack();
            this.printCurrentState();
        long endTime = System.nanoTime();

        this.printSolution();
        System.out.printf("Run required %d ms", (endTime-startTime)/1000/1000);
    }

    private void buidInitialSearchStack() {

        int numberOfInitialSearches = 100;
        while (stateStack.peek().heuristicNumber != 0 && numberOfInitialSearches >= 0) {
            // Check if limit is reached
            if ((stateStack.peek().heuristicNumber + stateStack.peek().depth) > limit){
                // Solution with current limit not available in current path, try going back
                // Record new candidate limit
                stateLimit = stateStack.peek().depth + stateStack.peek().heuristicNumber;
                candidateLimit = stateLimit < candidateLimit ? stateLimit : candidateLimit;
                stateStack.pop();
            } else if (stateStack.peek().unexploredChilds.empty()) {
                if (stateStack.peek().depth == 0){
                    // At root node, increase limit and explore children again
                    limit = candidateLimit;
                    candidateLimit = Integer.MAX_VALUE;
                    stateStack.peek().exploreChildNodes();
                } else {
                    // No more paths to go, go up
                    stateStack.pop();
                }
            } else {
                // Try next path
                this.stateStack.push(this.stateStack.peek().unexploredChilds.pop());
                stateStack.peek().exploreChildNodes();
            }
            numberOfInitialSearches -= 1;

        }

    }

    private void solveParallel() {

    }

    public ArrayDeque<State>[] splitStateStack(int numberOfReturnedStacks){
        ArrayDeque<State> tempStateStack = new ArrayDeque<>();
        ArrayDeque<State> localStateStack = new ArrayDeque<>();
        ArrayDeque[] returnStateStackArray = new ArrayDeque[numberOfReturnedStacks];

        // Initialize return stacks
        for (int i = 0; i < numberOfReturnedStacks; i++){
            returnStateStackArray[i] = new ArrayDeque<>();
        }

        // Flip current state stack over
        while(!this.stateStack.isEmpty()){
            tempStateStack.push(stateStack.pop());
        }

        // Duplicate base element to return state stack and local state stack
        // and distribute child elements fair across all returned state stacks
        while(!tempStateStack.isEmpty()){
            State tempState = tempStateStack.pop();

            localStateStack.push(new State(tempState.pancakeOrder,tempState.depth,tempState.flipFromParent,tempState.heuristicNumber));
            for (int i = 0; i < numberOfReturnedStacks; i++){
                returnStateStackArray[i].push(new State(tempState.pancakeOrder,tempState.depth,tempState.flipFromParent,tempState.heuristicNumber));
            }

            // Split all available unexplored childs across all return stacks
            int i = 0;
            ArrayDeque<State> pushStack = localStateStack;
            while (!tempState.unexploredChilds.isEmpty()){
                pushStack.peek().unexploredChilds.push(tempState.unexploredChilds.pop());

                if (i < numberOfReturnedStacks){
                    pushStack = returnStateStackArray[i];
                    i++;
                } else {
                    pushStack = localStateStack;
                    i = 0;
                }
            }
        }

        this.stateStack = localStateStack;
        return returnStateStackArray;
    }

    public static int[] generateRandomPancakeOrder(int size){
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

    private void printSolution() {
        System.out.printf("Found Solution in %d moves, in reverse Order:\n", stateStack.size()-1);
        while(!this.stateStack.isEmpty()){
            State state = this.stateStack.pop();
            System.out.print(Arrays.toString(state.pancakeOrder));
            System.out.printf(", heuristic number: %d\n",state.heuristicNumber);
            if(state.flipFromParent != 0) {
                System.out.printf("From flip %d on ", state.flipFromParent);
            }
        }
    }

    private void printCurrentState() {
        System.out.println("=~=~=~=~=~=~=~=~=~=~=~=~RANK " + this.rank + "=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~");
        System.out.printf("Current Limit: " + this.limit + "\n");
        System.out.printf("Current Candidate Limit: " + this.candidateLimit + "\n");
        System.out.printf("Current State Stack Size: " + this.stateStack.size() + "\n");
        System.out.printf("Top State Stack Content: ");
        for (int element:this.stateStack.peek().pancakeOrder) {
            System.out.printf(element + " ");
        }
        System.out.printf("\n");
        System.out.println("=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~");
    }

}
