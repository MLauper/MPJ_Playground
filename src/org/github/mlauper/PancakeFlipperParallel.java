package org.github.mlauper;

import mpi.MPI;
import mpi.Request;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;


public class PancakeFlipperParallel implements Serializable {

    public Deque<State> stateStack = new ArrayDeque<>();

    public static int[] initialPancakeOrder;

    // Describes a candidate for the new search limit
    public int candidateLimit = Integer.MAX_VALUE;
    int limit;
    int stateLimit;

    int rank;
    int numOfRanks;
    private State initialState;
    int globalCandidateLimit = Integer.MAX_VALUE;

    Object[] blockedRecvArr;
    Request blockedRequest;

    static class TAGS {
        public static final int INITIAL_SEED = 0;
        public static final int NEW_LIMIT = 1;
        public static final int REQUEST_SPLIT = 2;
        public static final int UNBLOCKED = 3;
        public static final int TERMINATE = 4;
        public static final int SHARE_STATESTACK = 5;
        public static final int BLOCKED = 6;
        public static final int NEW_SEED = 7;
        public static final int SOLUTION_FOUND = 99;
    }

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

    int[] augmentedPancakeOrder;
    public PancakeFlipperParallel(int[] pancakeOrder) {
        augmentedPancakeOrder = new int[pancakeOrder.length + 1];

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
        System.out.printf("Run required %d ms\n", (endTime-startTime)/1000/1000);
    }



    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        //initialPancakeOrder = new int[]{2, 1, 4, 3, 6, 5, 8, 7, 10, 9, 12, 11};
        // initialPancakeOrder = new int[]{2, 1, 4, 3, 6, 5, 8, 7, 10, 9, 12, 11, 14, 13, 16, 15};

        // initialPancakeOrder = new int[]{19, 13, 10, 16, 7, 14, 11, 12, 9, 4, 3, 1, 2, 20, 18, 5, 6, 17, 8, 15};
        // initialPancakeOrder = new int[]{13, 36, 38, 34, 5, 28, 16, 1, 4, 3, 33, 31, 24, 14, 21, 9, 18, 26, 8, 20, 17, 35, 7, 40, 19, 6, 23, 30, 11, 29, 27, 10, 25, 39, 22, 12, 15, 2, 37, 32};
        // initialPancakeOrder = new int[]{7, 25, 15, 2, 9, 34, 24, 19, 18, 16, 17, 10, 40, 39, 1, 41, 45, 30, 21, 11, 43, 22, 28, 27, 31, 4, 8, 20, 23, 12, 42, 26, 5, 6, 13, 29, 3, 33, 44, 35, 14, 32, 38, 37, 36};
        // initialPancakeOrder = new int[]{1, 40, 16, 32, 27, 14, 46, 19, 2, 35, 7, 38, 10, 44, 43, 36, 47, 17, 29, 23, 50, 39, 48, 37, 49, 33, 30, 34, 41, 15, 20, 28, 5, 22, 3, 12, 18, 31, 4, 6, 45, 13, 26, 11, 42, 21, 9, 24, 25, 8};
        // initialPancakeOrder = new int[]{25, 38, 52, 50, 3, 40, 11, 6, 36, 1, 47, 41, 7, 54, 49, 55, 15, 22, 16, 45, 48, 60, 5, 59, 10, 34, 56, 32, 2, 23, 29, 39, 20, 53, 43, 14, 58, 21, 9, 46, 12, 28, 24, 30, 44, 8, 4, 27, 37, 57, 18, 51, 26, 42, 31, 13, 19, 35, 17, 33};
        // initialPancakeOrder = new int[]{47, 57, 5, 59, 18, 38, 7, 27, 61, 32, 55, 22, 28, 23, 51, 37, 17, 43, 63, 67, 53, 45, 8, 54, 12, 35, 2, 15, 29, 41, 50, 16, 39, 69, 70, 19, 20, 36, 60, 62, 4, 42, 24, 40, 46, 14, 25, 56, 65, 66, 52, 10, 31, 3, 6, 68, 34, 49, 48, 33, 21, 64, 11, 9, 1, 44, 26, 13, 30, 58};

        // initialPancakeOrder = PancakeFlipper.generateRandomPancakeOrder(60);



        /* Reference Solution 1: */
        //initialPancakeOrder = new int[]{2, 1, 4, 3, 6, 5, 8, 7, 10, 9, 12, 11, 14, 13};
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
        initialPancakeOrder = new int[]{2, 1, 4, 3, 6, 5, 8, 7, 10, 9, 12, 11, 14, 13, 16, 15, 17};
        /*
            451 Solutions
            state 0: 2, 1, 4, 3, 6, 5, 8, 7, 10, 9, 12, 11, 14, 13, 16, 15, 17
            time: 5.907583366 sec
         */

        PancakeFlipperParallel pancakeFlipper = new PancakeFlipperParallel(initialPancakeOrder);

        MPI.Init(args);
        pancakeFlipper.rank = MPI.COMM_WORLD.Rank();
        pancakeFlipper.numOfRanks = MPI.COMM_WORLD.Size();
        int N = initialPancakeOrder.length;

        if (pancakeFlipper.rank == 0) {
            // Start control rank logic
            pancakeFlipper.startControlRank();
        } else {
            // Start worker rank logic
            pancakeFlipper.startWorkerRank();
        }

    }

    private void startControlRank() {
        this.log("Method start: startControlRank");

        long startTime = System.nanoTime();
            initialState = new State(this.stateStack.peek().pancakeOrder,this.stateStack.peek().depth,this.stateStack.peek().flipFromParent,this.stateStack.peek().heuristicNumber);
            this.buidInitialSearchStack();
            this.printCurrentState();
            this.splitAndDeployInitialStateStack();
            this.initializeControlRankListeners();
            this.waitForFirstSolution();
        long endTime = System.nanoTime();

        this.printSolution();
        System.out.println("Run to first solution required " + ((endTime-startTime)/1000/1000) + " ms");


        startTime = System.nanoTime();
            this.waitForAllSolutions();
        endTime = System.nanoTime();
        this.printAllSolutionStatistics();

        System.out.println("Run to count all solutions required " + ((endTime-startTime)/1000/1000) + " ms");

        this.log("Method end: startControlRank");
    }

    private void printAllSolutionStatistics() {
        System.out.println("Found a total of " + numberOfFoundSolutions + " solutions.");
    }

    int numberOfBlockedThreads = 0;
    private void waitForAllSolutions() {
        this.log("Method start: waitForAllSolutions");

        allSolutionsFound = false;
        while (true){
            this.handleSplitRequests();
            this.handleFinalizationBlockedRequests();

            if (allSolutionsFound){
                this.collectSolutionStatistics();
                break;
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //this.log("Method end: waitForAllSolutions");
    }

    private void collectSolutionStatistics() {
        this.log("Method start: collectSolutionStatistics");

        numberOfFoundSolutions = 0;
        for (int i = 1; i < numOfRanks; i++){
            terminateRequests[i-1].Wait();
            numberOfFoundSolutions += (int)termRecvArr[i-1][0];
        }

        this.log("Method end: collectSolutionStatistics");
    }

    private void waitForFirstSolution() {
        this.log("Method start: waitForFirstSolution");

        boolean solutionFound = false;
        while (true) {
            for (int i = 1; i < numOfRanks; i++){
                if (solutionFoundRequests[i-1].Test() != null){
                    solutionFound = true;
                }
            }
            if (solutionFound){
                this.log("Received a solution");
                this.stateStack = (ArrayDeque<State>) solutionFoundRecvArr[0];
                this.limit = (int) solutionFoundRecvArr[1];
                this.candidateLimit = (int) solutionFoundRecvArr[2];

                break;
            }

            this.handleSplitRequests();
            this.handleBlockedRequests();

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        this.log("Method end: waitForFirstSolution");
    }

    Object[] solutionFoundRecvArr;
    Request[] solutionFoundRequests;
    Object[] requestSplitRecvArr;
    Request[] requestSplitRequests;
    Object[][] termRecvArr;
    Request[] terminateRequests;
    private void initializeControlRankListeners() {
        // Start listening for an SOLUTION_FOUND request
        solutionFoundRecvArr = new Object[3];
        solutionFoundRequests = new Request[numOfRanks-1];
        for (int i = 1; i < numOfRanks; i++){
            solutionFoundRequests[i-1] = MPI.COMM_WORLD.Irecv(solutionFoundRecvArr, 0, 3, MPI.OBJECT, i, TAGS.SOLUTION_FOUND);
        }

        // Start listening for REQUEST_SPLIT request
        requestSplitRecvArr = new Object[2];
        requestSplitRequests = new Request[numOfRanks-1];
        for (int i = 1; i < numOfRanks; i++){
            requestSplitRequests[i-1] = MPI.COMM_WORLD.Irecv(requestSplitRecvArr, 0, 2, MPI.OBJECT, i, TAGS.REQUEST_SPLIT);
        }

        // Start listening for blocked request and send first blocked request
        blockedRecvArr = new Object[1];
        blockedRequest = MPI.COMM_WORLD.Irecv(blockedRecvArr, 0, 1, MPI.OBJECT, numOfRanks-1, TAGS.BLOCKED);
        Object[] blockedSendArr = new Object[1];
        blockedSendArr[0] = false;
        MPI.COMM_WORLD.Isend(blockedSendArr, 0, 1, MPI.OBJECT, 1, TAGS.BLOCKED);

        // Start listening for terminate requests
        termRecvArr = new Object[numOfRanks-1][1];
        terminateRequests = new Request[numOfRanks-1];
        for (int i = 1; i < numOfRanks; i++){
            terminateRequests[i-1] = MPI.COMM_WORLD.Irecv(termRecvArr[i-1], 0, 1, MPI.OBJECT, i, TAGS.TERMINATE);
        }
    }

    private void handleSplitRequests() {
        this.log("Method start: handleSplitRequests");

        for (int i = 1; i < numOfRanks; i++){
            if (requestSplitRequests[i-1].Test() != null){

                this.log("Received Split Request from Rank " + i + " with candidate limit " + (int) requestSplitRecvArr[1]);

                // Adjust global candidate limit
                globalCandidateLimit = (int) requestSplitRecvArr[1] < globalCandidateLimit? (int) requestSplitRecvArr[1] : globalCandidateLimit;

                // Restart listening for a Request Split request
                requestSplitRequests[i-1] = MPI.COMM_WORLD.Irecv(requestSplitRecvArr, 0, 2, MPI.OBJECT, i, TAGS.REQUEST_SPLIT);

                // Send the request to share stack with requesting rank
                Object[] shareStateStackSendArr = new Object[1];
                shareStateStackSendArr[0] = i;
                int recvRank = (i + 1) % numOfRanks;
                if (recvRank == 0) {recvRank = 1;}
                this.log("Send split request to worker " + recvRank + " for worker " + i);
                MPI.COMM_WORLD.Isend(shareStateStackSendArr, 0, 1, MPI.OBJECT, recvRank, TAGS.SHARE_STATESTACK);
            }
        }

        this.log("Method end: handleSplitRequests");
    }

    private void handleBlockedRequests() {
        this.log("Method start: handleBlockedRequests");

        if (this.blockedRequest.Test() != null){
            this.log("Received a blocked request");

            if ((boolean)blockedRecvArr[0]){
                this.log("All workers are locked, increase limit");
                this.increaseLimitAndReseed();
            } else {
                this.log("Not all workers are locked, continue");
            }

            blockedRecvArr = new Object[1];
            blockedRequest = MPI.COMM_WORLD.Irecv(blockedRecvArr, 0, 1, MPI.OBJECT, numOfRanks-1, TAGS.BLOCKED);

            Object[] blockedSendArr = new Object[1];
            blockedSendArr[0] = false;
            MPI.COMM_WORLD.Isend(blockedSendArr, 0, 1, MPI.OBJECT, 1, TAGS.BLOCKED);
        }

        this.log("Method end: handleBlockedRequests");
    }

    boolean allSolutionsFound = false;
    private void handleFinalizationBlockedRequests() {
        this.log("Method start: handleFinalizationBlockedRequests");

        if (this.blockedRequest.Test() != null){
            this.log("Received a blocked request");

            if ((boolean)blockedRecvArr[0]){
                this.log("All workers are locked, try to finalize");

                if ((boolean)blockedRecvArr[0]){
                    this.log("Verified lock of workers, finalize");
                    this.terminateAllWorkers();
                    allSolutionsFound = true;
                }
            } else {
                this.log("Not all workers are locked, continue");
            }



        }

        this.log("Method end: handleFinalizationBlockedRequests");
    }

    private void terminateAllWorkers() {

        Object[] terminateSendArr = new Object[1];
        terminateSendArr[0] = true;
        for (int i = 1; i < numOfRanks; i++){
            MPI.COMM_WORLD.Isend(terminateSendArr, 0,1,MPI.OBJECT, i, TAGS.TERMINATE);
        }

    }

    private void increaseLimitAndReseed() {
        this.log("Method start: increaseLimitAndReseed");

        // limit = candidateLimit;
        // candidateLimit = Integer.MAX_VALUE;
        // stateStack.peek().exploreChildNodes();

        this.stateStack = new ArrayDeque<>();
        this.stateStack.push(new State(initialState.pancakeOrder, initialState.depth, initialState.flipFromParent, initialState.heuristicNumber));
        this.limit = this.globalCandidateLimit;
        this.candidateLimit = Integer.MAX_VALUE;
        this.globalCandidateLimit = Integer.MAX_VALUE;
        this.stateStack.peek().exploreChildNodes();

        this.printCurrentState();
        this.log("New limit is " + this.limit);

        this.buidInitialSearchStack();
        this.printCurrentState();
        this.splitAndDeployNewStateStack();

        this.log("Method end: increaseLimitAndReseed");
    }

    private void splitAndDeployNewStateStack() {
        this.log("Method start: splitAndDeployNewStateStack");

        // Split stack, so every worker will receive one
        PancakeFlipperParallel tempPancakeFlipper = new PancakeFlipperParallel(initialPancakeOrder);
        Deque<State>[] splittedStateStacks = tempPancakeFlipper.splitLocalStateStack(numOfRanks - 1);

        // Send each Rank a part of the current search tree
        Object[] sendArr = new Object[3];
        sendArr[1] = this.limit;
        sendArr[2] = this.candidateLimit;
        for (int i = 1; i < numOfRanks; i++) {
            sendArr[0] = splittedStateStacks[i - 1];
            this.log("Send stack to rank " + i);

            MPI.COMM_WORLD.Send(new Object[]{sendArr[0],sendArr[1],sendArr[2]}, 0, 3, MPI.OBJECT, i, TAGS.NEW_SEED);
        }

        this.log("Method end: splitAndDeployNewStateStack");
    }

    private void splitAndDeployInitialStateStack() {
        this.log("Method start: splitAndDeployInitialStateStack");

        // Split stack, so every worker will receive one
        Deque<State>[] splittedStateStacks = this.splitLocalStateStack(numOfRanks - 1);

        // Send each Rank a part of the current search tree
        Object[] sendArr = new Object[3];
        sendArr[1] = this.limit;
        sendArr[2] = this.candidateLimit;
        for (int i = 1; i < numOfRanks; i++){
            sendArr[0] = splittedStateStacks[i-1];
            this.log("Initial distribute work to rank " + i);

            MPI.COMM_WORLD.Send(sendArr, 0, 3, MPI.OBJECT, i, TAGS.INITIAL_SEED);
        }

        this.log("Method end: splitAndDeployInitialStateStack");
    }

    private void buidInitialSearchStack() {

        int numberOfInitialSearches = 10;
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

    private void startWorkerRank() {
        this.log("Method start: startWorkerRank");

        this.receiveInitialStateStack();
        this.initializeWorkerRank();
        this.searchSolution();

        this.log("Method end: startWorkerRank");
    }

    Object[] splitRequestRecvArr;
    Request splitRequest;
    Object[] newStateStackRecvArr;
    Request newStateStackRequest;
    Object[] terminateRecvArr;
    Request terminateRequest;
    private void initializeWorkerRank() {
        this.log("Method start: initializeWorkerRank");

        splitRequestRecvArr = new Object[1];
        splitRequest = MPI.COMM_WORLD.Irecv(splitRequestRecvArr, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, TAGS.SHARE_STATESTACK);

        newStateStackRecvArr = new Object[3];
        newStateStackRequest = MPI.COMM_WORLD.Irecv(newStateStackRecvArr, 0, 3, MPI.OBJECT, MPI.ANY_SOURCE, TAGS.NEW_SEED);

        blockedRecvArr = new Object[1];
        blockedRequest = MPI.COMM_WORLD.Irecv(blockedRecvArr, 0, 1, MPI.OBJECT, this.rank -1, TAGS.BLOCKED);

        terminateRecvArr = new Object[1];
        terminateRequest = MPI.COMM_WORLD.Irecv(terminateRecvArr, 0, 1, MPI.OBJECT, 0, TAGS.TERMINATE);

        this.log("Method end: initializeWorkerRank");
    }

    int numberOfFoundSolutions;
    private void searchSolution() {
        this.log("Method start: searchSolution");

        int i;
        numberOfFoundSolutions = 0;
        while (true){

            i = 0;
            while (stateStack.peek().heuristicNumber != 0 && i < 10000) {

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
                        this.requestAndReceiveNewStateStack();
                    } else {
                        // No more paths to go, go up
                        stateStack.pop();
                    }
                } else {
                    // Try next path
                    this.stateStack.push(this.stateStack.peek().unexploredChilds.pop());
                    stateStack.peek().exploreChildNodes();
                }

                if (terminate){
                    return;
                }

                i += 1;

            }

            if (stateStack.peek().heuristicNumber == 0){
                numberOfFoundSolutions += 1;
                this.log("Found solution " + numberOfFoundSolutions);
                if (numberOfFoundSolutions == 1){
                    this.sendSolutionToControlRank();
                }
                this.stateStack.pop();
            }

        }

    }

    private void sendSolutionToControlRank() {
        this.log("Method start: sendSolutionToControlRank");

        Object[] sendArr = new Object[3];
        sendArr[0] = this.stateStack;
        sendArr[1] = this.limit;
        sendArr[2] = this.candidateLimit;
        MPI.COMM_WORLD.Isend(sendArr, 0, 3, MPI.OBJECT, 0, TAGS.SOLUTION_FOUND);
        this.log("Sent solution to control rank");

        this.log("Method end: sendSolutionToControlRank");
    }

    boolean isBlocked = false;
    private void requestAndReceiveNewStateStack() {
        this.log("Method start: requestAndReceiveNewStateStack");

        // limit = candidateLimit;
        // candidateLimit = Integer.MAX_VALUE;
        // stateStack.peek().exploreChildNodes();

        this.isBlocked = true;
        Object[] sendArr = new Object[2];
        sendArr[0] = this.rank;
        sendArr[1] = this.candidateLimit;
        MPI.COMM_WORLD.Isend(sendArr, 0, 2, MPI.OBJECT, 0, TAGS.REQUEST_SPLIT);

        while (true) {
            this.handleWorkerSplitRequests();
            this.tryToReceiveStateStack();
            this.handleWorkerBlockedRequests();
            this.handleTerminateRequest();

            if (terminate){
                return;
            }

            if (isBlocked){
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }

        this.log("Method end: requestAndReceiveNewStateStack");
    }

    boolean terminate = false;
    private void handleTerminateRequest() {
        this.log("Method start: handleTerminateRequest");

        if (terminateRequest.Test() != null){
            this.log("Received a terminate request");

            Object[] terminateSendArr = new Object[1];
            terminateSendArr[0] = this.numberOfFoundSolutions;

            MPI.COMM_WORLD.Isend(terminateSendArr, 0, 1, MPI.OBJECT, 0, TAGS.TERMINATE);

            terminate = true;
        }

        this.log("Method end: handleTerminateRequest");
    }

    private void handleWorkerBlockedRequests() {
        this.log("Method start: handleWorkerBlockedRequests");

        if (blockedRequest.Test() != null){
            this.log("Received a blocked request");

            Object[] blockedSendArr = new Object[1];
            if (this.isBlocked || (boolean)blockedRecvArr[0]) {
                blockedSendArr[0] = true;
            } else {
                blockedSendArr[0] = false;
            }
            int destRank = (this.rank + 1) % this.numOfRanks;

            MPI.COMM_WORLD.Isend(blockedSendArr, 0, 1, MPI.OBJECT, destRank, TAGS.BLOCKED);

            blockedRecvArr = new Object[1];
            blockedRequest = MPI.COMM_WORLD.Irecv(blockedRecvArr, 0, 1, MPI.OBJECT, this.rank -1, TAGS.BLOCKED);
        }

        this.log("Method end: handleWorkerBlockedRequests");
    }

    private void tryToReceiveStateStack() {
        this.log("Method start: tryToReceiveStateStack");

        if (newStateStackRequest.Test() != null){
            this.log("Received new state stack");

            this.candidateLimit = (int) newStateStackRecvArr[2];
            this.limit = (int) newStateStackRecvArr[1];
            this.stateStack = (ArrayDeque<State>) newStateStackRecvArr[0];

            newStateStackRecvArr = new Object[3];
            newStateStackRequest = MPI.COMM_WORLD.Irecv(newStateStackRecvArr, 0, 3, MPI.OBJECT, MPI.ANY_SOURCE, TAGS.NEW_SEED);

            this.isBlocked = false;
        }

        this.log("Method end: tryToReceiveStateStack");
    }

    private void handleWorkerSplitRequests() {
        this.log("Method start: handleWorkerSplitRequests");

        if (splitRequest.Test() != null){
            this.log("Received a split request");

            if ((int) splitRequestRecvArr[0] == this.rank){
                this.log("Received own rank id as split destination, ignore split request");
            } else {
                if (this.isBlocked){
                    this.log("Currently blocked, redirect request");

                    Object[] shareStateStackSendArr = new Object[1];
                    shareStateStackSendArr[0] = splitRequestRecvArr[0];
                    int recvRank = (this.rank + 1) % numOfRanks;
                    if (recvRank == 0) {recvRank = 1;}
                    MPI.COMM_WORLD.Isend(shareStateStackSendArr, 0, 1, MPI.OBJECT, recvRank, TAGS.SHARE_STATESTACK);

                } else {
                    this.log("Share state stack with rank " + (int)splitRequestRecvArr[0]);

                    Deque<State>[] splittedStateStacks = this.splitLocalStateStack(2);

                    Object[] sendArr = new Object[3];
                    sendArr[1] = this.limit;
                    sendArr[2] = this.candidateLimit;
                    sendArr[0] = splittedStateStacks[0];
                    this.stateStack = splittedStateStacks[1];
                    MPI.COMM_WORLD.Isend(sendArr, 0, 3, MPI.OBJECT, (int)splitRequestRecvArr[0], TAGS.INITIAL_SEED);
                }
            }

            splitRequestRecvArr = new Object[1];
            splitRequest = MPI.COMM_WORLD.Irecv(splitRequestRecvArr, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, TAGS.SHARE_STATESTACK);

        }

        this.log("Method end: handleWorkerSplitRequests");
    }

    private void receiveInitialStateStack() {
        this.log("Method start: receiveInitialStateStack");

        Object[] recvArr = new Object[3];
        MPI.COMM_WORLD.Recv(recvArr, 0, 3, MPI.OBJECT, MPI.ANY_SOURCE, TAGS.INITIAL_SEED);
        this.log("Initial seed received");

        this.stateStack = (ArrayDeque<State>) recvArr[0];
        this.limit = (int) recvArr[1];
        this.candidateLimit = (int) recvArr[2];

        this.printCurrentState();

        this.log("Method end: receiveInitialStateStack");
    }

    public Deque<State>[] splitLocalStateStack(int numberOfReturnedStacks){

        Deque<State> tempStateStack = new ArrayDeque<>();
        Deque[] returnStateStackArray = new ArrayDeque[numberOfReturnedStacks];

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

            for (int i = 0; i < numberOfReturnedStacks; i++){
                // Create a new State on each returned state stack
                returnStateStackArray[i].push(new State(tempState.pancakeOrder,tempState.depth,tempState.flipFromParent,tempState.heuristicNumber));
            }

            // Split all available unexplored childs across all return stacks
            int i = 0;
            Deque<State> pushStack = returnStateStackArray[0];
            while (!tempState.unexploredChilds.isEmpty()){

                pushStack.peek().unexploredChilds.push(tempState.unexploredChilds.pop());

                i += 1;
                i = i % numberOfReturnedStacks;

                pushStack = returnStateStackArray[i];
            }
        }

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
        this.log("=~=~=~=~=~=~=~=~=~=~=~=~RANK " + this.rank + "=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~");
        this.log("Current Limit: " + this.limit);
        this.log("Current Candidate Limit: " + this.candidateLimit);
        this.log("Current State Stack Size: " + this.stateStack.size());
        String message = "Top State Stack Content: ";
        for (int element:this.stateStack.peek().pancakeOrder) {
            message += element + " ";
        }
        this.log(message);
        this.log("Current Number of unexplored Childs: " + this.stateStack.peek().unexploredChilds.size());
        this.log("=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~");
    }

    private void log(String message) {
        //System.out.println("Rank " + this.rank + ": " + message);
    }

}
