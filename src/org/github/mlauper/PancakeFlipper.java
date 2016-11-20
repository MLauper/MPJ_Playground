package org.github.mlauper;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

import mpi.MPI;
import mpi.Request;
import mpi.Status;


public class PancakeFlipper implements Serializable {

    public Stack<State> stateStack = new Stack<>();

    // Describes a candidate for the new search limit
    public int candidateLimit = Integer.MAX_VALUE;
    int limit;

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
                        unexploredChilds.push(new State(this.flippedPancakeOrder(i), this.depth + 1, i, this.heuristicNumber));
                    } else {
                        unexploredChilds.push(new State(this.flippedPancakeOrder(i), this.depth + 1, i, this.heuristicNumber - 1));
                    }
                }
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

    public PancakeFlipper(int[] pancakeOrder) {
        int[] augmentedPancakeOrder = new int[pancakeOrder.length + 1];

        System.arraycopy(pancakeOrder, 0, augmentedPancakeOrder, 0, pancakeOrder.length);
        augmentedPancakeOrder[pancakeOrder.length] = pancakeOrder.length + 1;

        stateStack.push(new State(augmentedPancakeOrder, 0, 0));
        stateStack.peek().exploreChildNodes();

        // Initially set heuristic number as limit
        limit = stateStack.peek().heuristicNumber;
    }

    public PancakeFlipper(int[] pancakeOrder, int limit) {
        int[] augmentedPancakeOrder = new int[pancakeOrder.length + 1];

        System.arraycopy(pancakeOrder, 0, augmentedPancakeOrder, 0, pancakeOrder.length);
        augmentedPancakeOrder[pancakeOrder.length] = pancakeOrder.length + 1;

        stateStack.push(new State(augmentedPancakeOrder, 0, 0));
        stateStack.peek().exploreChildNodes();

        this.limit = limit;
    }
    public PancakeFlipper(){

    }

    public void solveSequencial(){
        int stateLimit;

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
                    System.out.printf("New Limit: %d\n",limit);
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

        this.printSolution();
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

    private void printData(){
        System.out.println("=========================================");
        System.out.printf("Printing PacakeFlipper Data:\n");
        System.out.printf("Candidate Limit: %d\n", this.candidateLimit);
        System.out.printf("Current Limit: %d\n", this.limit);
        System.out.printf("StateStack Size: %d\n", this.stateStack.size());
        System.out.printf("Current depth: %d\n", this.stateStack.peek().depth);
        System.out.printf("Current heuristic number: %d\n", this.stateStack.peek().heuristicNumber);
        System.out.printf("Current pancake order: %s\n", Arrays.toString(this.stateStack.peek().pancakeOrder));
        System.out.printf("Current number of unexplored childs childs: %d\n", this.stateStack.peek().unexploredChilds.size());
        System.out.println("=========================================");
    }

    // Run with
    //          cd C:\code\src\github.com\MLauper\MPJ_Playground\target\classes\github\mlauper
    //          "c:\mpj\bin\mpjrun.bat" -np 12 org.github.mlauper.PancakeFlipper
    // WARNING: Do not forget to set MPJ_HOME environment variable.

    static class TAGS {
        public static final int INITIAL_SEED = 0;
        public static final int NEW_LIMIT = 1;
        public static final int REQUEST_SPLIT = 2;
        public static final int UNBLOCKED = 3;
        public static final int TERMINATE = 4;
        public static final int SOLUTION_FOUND = 99;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {



        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int numOfRanks = MPI.COMM_WORLD.Size();
        int RANK0 = 0;
        int RANK1 = 1;

        //int[] initialPancakeOrder = new int[]{19, 13, 10, 16, 7, 14, 11, 12, 9, 4, 3, 1, 2, 20, 18, 5, 6, 17, 8, 15};
        //int[] initialPancakeOrder = new int[]{13, 36, 38, 34, 5, 28, 16, 1, 4, 3, 33, 31, 24, 14, 21, 9, 18, 26, 8, 20, 17, 35, 7, 40, 19, 6, 23, 30, 11, 29, 27, 10, 25, 39, 22, 12, 15, 2, 37, 32};
        //int[] initialPancakeOrder = new int[]{7, 25, 15, 2, 9, 34, 24, 19, 18, 16, 17, 10, 40, 39, 1, 41, 45, 30, 21, 11, 43, 22, 28, 27, 31, 4, 8, 20, 23, 12, 42, 26, 5, 6, 13, 29, 3, 33, 44, 35, 14, 32, 38, 37, 36};
        //int[] initialPancakeOrder = new int[]{1, 40, 16, 32, 27, 14, 46, 19, 2, 35, 7, 38, 10, 44, 43, 36, 47, 17, 29, 23, 50, 39, 48, 37, 49, 33, 30, 34, 41, 15, 20, 28, 5, 22, 3, 12, 18, 31, 4, 6, 45, 13, 26, 11, 42, 21, 9, 24, 25, 8};
        int[] initialPancakeOrder = new int[]{25, 38, 52, 50, 3, 40, 11, 6, 36, 1, 47, 41, 7, 54, 49, 55, 15, 22, 16, 45, 48, 60, 5, 59, 10, 34, 56, 32, 2, 23, 29, 39, 20, 53, 43, 14, 58, 21, 9, 46, 12, 28, 24, 30, 44, 8, 4, 27, 37, 57, 18, 51, 26, 42, 31, 13, 19, 35, 17, 33};
        //int[] initialPancakeOrder = new int[]{47, 57, 5, 59, 18, 38, 7, 27, 61, 32, 55, 22, 28, 23, 51, 37, 17, 43, 63, 67, 53, 45, 8, 54, 12, 35, 2, 15, 29, 41, 50, 16, 39, 69, 70, 19, 20, 36, 60, 62, 4, 42, 24, 40, 46, 14, 25, 56, 65, 66, 52, 10, 31, 3, 6, 68, 34, 49, 48, 33, 21, 64, 11, 9, 1, 44, 26, 13, 30, 58};
        int N = initialPancakeOrder.length;

        if (rank == 0) {
            long startTime, endTime;
            startTime = System.nanoTime();

            System.out.println("Control Thread (Rank 0) started");

            System.out.println("Build initial search tree as seed...");
            PancakeFlipper pancakeFlipper = new PancakeFlipper(initialPancakeOrder);
            pancakeFlipper.searchSolution(51);

            System.out.println("Initial seed state:");
            pancakeFlipper.printData();

            // Send each Rank a part of the current search tree
            Object[] sendArr = new Object[3];
            sendArr[1] = (Object) pancakeFlipper.limit;
            sendArr[2] = (Object) pancakeFlipper.candidateLimit;
            Stack<State>[] splittedStateStacks = pancakeFlipper.splitStateStack(numOfRanks-2);
            for (int i = 1; i < numOfRanks-1; i++){
                sendArr[0] = splittedStateStacks[i-1];
                MPI.COMM_WORLD.Send(sendArr, 0, 3, MPI.OBJECT, i, TAGS.INITIAL_SEED);
            }
            sendArr[0] = pancakeFlipper.stateStack;
            MPI.COMM_WORLD.Send(sendArr, 0, 3, MPI.OBJECT, numOfRanks-1, TAGS.INITIAL_SEED);

            // Reserve space for two tags; SOLUTION_FOUND and INCREASE_LIMIT
            Request[] requests = new Request[3];
            // Start listening for a solution
            Request solutionRequest;
            Object[] recvArrSolution = new Object[1];
            solutionRequest = MPI.COMM_WORLD.Irecv(recvArrSolution, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, TAGS.SOLUTION_FOUND);
            // Start listening for an INCREASE_LIMIT request
            Request[] increaseLimitRequests = new Request[numOfRanks-1];
            int[] increaseLimitValue = new int[numOfRanks-1];
            for (int i = 1; i < numOfRanks; i++){
                increaseLimitRequests[i-1] = MPI.COMM_WORLD.Irecv(increaseLimitValue, i-1, 1, MPI.INT, i, TAGS.NEW_LIMIT);
            }
            // Start listening for an UNBLOCK request
            Request[] unblockRequests = new Request[numOfRanks-1];
            boolean[] unblockRequestsValue = new boolean[numOfRanks-1];
            for (int i = 1; i < numOfRanks; i++){
                unblockRequests[i-1] = MPI.COMM_WORLD.Irecv(unblockRequestsValue, i-1, 1, MPI.BOOLEAN, i, TAGS.UNBLOCKED);
            }

            int globalCandidateLimit = Integer.MAX_VALUE;
            Queue<Integer> freeWorker = new LinkedList<>();
            Queue<Integer> waitingWorker = new LinkedList<>();
            Queue<Request> splitStackRequests = new LinkedList<>();
            Integer workerToSplitFrom = 0;
            while(solutionRequest.Test() == null){
                // Test all increase limit requests and note a global request limit
                for (int i = 1; i < numOfRanks; i++){
                    if(increaseLimitRequests[i-1].Test() != null){
                        System.out.printf("Received request for new work from worker %d\n", i);
                        freeWorker.add(i);
                        globalCandidateLimit = increaseLimitValue[i-1] < globalCandidateLimit ? increaseLimitValue[i-1] : globalCandidateLimit;
                        increaseLimitValue[i-1] = 0;
                        System.out.printf("Listen again for New Limit Requests from %d\n", i);
                        increaseLimitRequests[i-1] = MPI.COMM_WORLD.Irecv(increaseLimitValue, i-1, 1, MPI.INT, i, TAGS.NEW_LIMIT);
                    }
                }

                // For each free worker, request a split from another worker
                for (Integer worker : freeWorker) {
                    // select good source
                    for (int i = 1; i < numOfRanks; i++){
                        workerToSplitFrom++;
                        workerToSplitFrom = workerToSplitFrom % numOfRanks;
                        if (workerToSplitFrom == 0) workerToSplitFrom += 1;
                        if (!freeWorker.contains(workerToSplitFrom) && !waitingWorker.contains(workerToSplitFrom) && workerToSplitFrom != worker){
                            break;
                        }
                    }

                    int[] sendBuf = new int[1];
                    sendBuf[0] = worker;
                    System.out.printf("Trying to send work from %d to %d\n", workerToSplitFrom, worker);
                    splitStackRequests.add(MPI.COMM_WORLD.Isend(sendBuf, 0, 1, MPI.INT, workerToSplitFrom, TAGS.REQUEST_SPLIT));
                    waitingWorker.add(worker);
                }
                freeWorker = new LinkedList<>();

                // Check for workers that are no longer waiting
                for (int i = 1; i < numOfRanks; i++){
                    if(unblockRequests[i-1].Test() != null){
                        System.out.printf("Removing worker %d from waiting workers...\n",i);
                        waitingWorker.remove(i);
                        unblockRequests[i-1] = MPI.COMM_WORLD.Irecv(unblockRequestsValue, i-1, 1, MPI.BOOLEAN, i, TAGS.UNBLOCKED);
                    }
                }

                // As soon as all worker are blocked, increase limit and restart search
                if(waitingWorker.size() == numOfRanks-1){
                    System.out.println("All workers waiting...");
                    for(Request request : splitStackRequests){
                        request.Cancel();
                    }
                    pancakeFlipper = new PancakeFlipper(initialPancakeOrder, globalCandidateLimit);
                    System.out.println("calculating seed tree...");
                    pancakeFlipper.searchSolution(51);
                    System.out.println("Finished calculating seed tree...");
                    globalCandidateLimit = Integer.MAX_VALUE;

                    sendArr = new Object[3];
                    sendArr[1] = (Object) pancakeFlipper.limit;
                    sendArr[2] = (Object) pancakeFlipper.candidateLimit;
                    splittedStateStacks = pancakeFlipper.splitStateStack(numOfRanks-2);
                    for (int i = 1; i < numOfRanks-1; i++){
                        sendArr[0] = splittedStateStacks[i-1];
                        System.out.printf("Sending part to rank %d\n",i);
                        MPI.COMM_WORLD.Isend(sendArr, 0, 3, MPI.OBJECT, i, TAGS.INITIAL_SEED);
                    }
                    sendArr[0] = pancakeFlipper.stateStack;
                    MPI.COMM_WORLD.Isend(sendArr, 0, 3, MPI.OBJECT, numOfRanks-1, TAGS.INITIAL_SEED);

                    System.out.println("New seeds were sent...");

                    waitingWorker = new LinkedList<>();
                }

            }

            System.out.println("--------------------ALL GOOD THINGS COME TO AN END -------------------------");

            pancakeFlipper.stateStack = (Stack<State>)recvArrSolution[0];
            pancakeFlipper.printSolution();

            endTime = System.nanoTime();
            System.out.printf("=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~\nSolved in %dms\n=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~\n\n", (endTime-startTime)/1000/1000);

            for (int i = 1; i < numOfRanks; i++) {
                int[] sendBuf = new int[1];
                MPI.COMM_WORLD.Isend(sendBuf, 0, 1, MPI.INT, i, TAGS.TERMINATE);
            }

            System.exit(0);
        }
        else {
            System.out.printf("Worker Thread (Rank %d) started\n", rank);

            PancakeFlipper pancakeFlipper = new PancakeFlipper();

            Object[] recvArr = new Object[3];
            MPI.COMM_WORLD.Recv(recvArr, 0, 3, MPI.OBJECT, MPI.ANY_SOURCE, TAGS.INITIAL_SEED);
            pancakeFlipper.stateStack = (Stack<State>)recvArr[0];
            pancakeFlipper.limit = (int)recvArr[1];
            pancakeFlipper.candidateLimit = (int)recvArr[2];

            System.out.printf("Received stack is currently %d in size\n", pancakeFlipper.stateStack.size());
            pancakeFlipper.printData();

            pancakeFlipper.solveParallel();
        }

        MPI.Finalize();
    }

    public Stack<State>[] splitStateStack(int numberOfReturnedStacks){
        Stack<State> tempStateStack = new Stack<>();
        Stack<State> localStateStack = new Stack<>();
        Stack<State> returnStateStack[] = new Stack[numberOfReturnedStacks];

        // Initialize return stacks
        for (int i = 0; i < numberOfReturnedStacks; i++){
            returnStateStack[i] = new Stack<State>();
        }

        // Flip current state stack over
        while(!this.stateStack.isEmpty()){
            tempStateStack.push(stateStack.pop());
        }

        // Duplicate base element to return state stack and local state stack
        // and move half of the elements to return state stack
        while(!tempStateStack.isEmpty()){
            State tempState = tempStateStack.pop();

            localStateStack.push(new State(tempState.pancakeOrder,tempState.depth,tempState.flipFromParent,tempState.heuristicNumber));
            for (int i = 0; i < numberOfReturnedStacks; i++){
                returnStateStack[i].push(new State(tempState.pancakeOrder,tempState.depth,tempState.flipFromParent,tempState.heuristicNumber));
            }

            // Split all available unexplored childs across all return stacks
            int i = 0;
            Stack<State> pushStack = localStateStack;
            while (!tempState.unexploredChilds.isEmpty()){
                pushStack.peek().unexploredChilds.push(tempState.unexploredChilds.pop());

                if (i < numberOfReturnedStacks){
                    pushStack = returnStateStack[i];
                    i++;
                } else {
                    pushStack = localStateStack;
                    i = 0;
                }
            }
        }

        this.stateStack = localStateStack;
        return returnStateStack;
    }

    public void searchSolution(int numberOfTries) {

        int stateLimit;

        // Search until heuristicNumber is 0, which means that a solution was found
        while (stateStack.peek().heuristicNumber != 0 && numberOfTries > 0) {
            numberOfTries--;

            // Check if limit is reached
            if ((stateStack.peek().heuristicNumber + stateStack.peek().depth) > limit) {
                // Solution with current limit not available in current path, try going back
                // Record new candidate limit
                stateLimit = stateStack.peek().depth + stateStack.peek().heuristicNumber;
                candidateLimit = stateLimit < candidateLimit ? stateLimit : candidateLimit;
                stateStack.pop();
            } else if (stateStack.peek().unexploredChilds.empty()) {
                if (stateStack.peek().depth == 0) {
                    System.out.printf("CRITICAL IN with current limit %d\n", this.limit);
                    // At root node, propose new search limit to control node and receive new work
                    int[] sendArr = new int[1];
                    sendArr[0] = candidateLimit;

                    System.out.println("Sending to rank0 . . .");
                    MPI.COMM_WORLD.Isend(sendArr,0,1,MPI.INT,0,TAGS.NEW_LIMIT);

                    Object[] recvArr = new Object[3];

                    Request initialSeedReceiveRequest = MPI.COMM_WORLD.Irecv(recvArr, 0, 3, MPI.OBJECT, MPI.ANY_SOURCE, TAGS.INITIAL_SEED);

                    int i = 0;
                    while(initialSeedReceiveRequest.Test() == null){
                        i++;
                        System.out.printf("Waiting for new seed... I'm Rank %d\n", MPI.COMM_WORLD.Rank());

                        if (i >= 2){
                            MPI.COMM_WORLD.Isend(sendArr,0,1,MPI.INT,0,TAGS.NEW_LIMIT);
                        }

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                    }

                    boolean[] sendBool = new boolean[1];
                    sendBool[0] = true;
                    MPI.COMM_WORLD.Isend(sendBool, 0, 1, MPI.BOOLEAN, 0, TAGS.UNBLOCKED);
                    this.stateStack = (Stack<State>)recvArr[0];
                    this.limit = (int)recvArr[1];
                    this.candidateLimit = (int)recvArr[2];
                    System.out.println("CRITICAL OUT");
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

    }

    public void solveParallel(){

        int[] splitTo = new int[1];
        Request splitRequest = MPI.COMM_WORLD.Irecv(splitTo, 0, 1, MPI.INT, MPI.ANY_SOURCE, TAGS.REQUEST_SPLIT);
        int[] recvBuf = new int[1];
        Request terminateRequest = MPI.COMM_WORLD.Irecv(recvBuf, 0, 1, MPI.INT, MPI.ANY_SOURCE, TAGS.TERMINATE);

        while (stateStack.peek().heuristicNumber != 0) {
            if (terminateRequest.Test() != null) {
                MPI.Finalize();
            }
            System.out.printf("Still looking for a solution, rank %d\n", MPI.COMM_WORLD.Rank());
            // Check if split to other worker should be done
            if (splitRequest.Test() != null) {
                Object[] sendArr = new Object[3];
                sendArr[1] = (Object) this.limit;
                sendArr[2] = (Object) this.candidateLimit;
                sendArr[0] = (Object) this.splitStateStack(1)[0];

                MPI.COMM_WORLD.Isend(sendArr, 0, 3, MPI.OBJECT, splitTo[0], TAGS.INITIAL_SEED);
                // Restart listening to split requests
                splitRequest = MPI.COMM_WORLD.Irecv(splitTo, 0, 1, MPI.INT, MPI.ANY_SOURCE, TAGS.REQUEST_SPLIT);
            }

            this.searchSolution(100000);
        }

        Object[] sendArr = new Object[1];
        sendArr[0] = (Object) stateStack;
        MPI.COMM_WORLD.Send(sendArr, 0, 1, MPI.OBJECT, 0, TAGS.SOLUTION_FOUND);
    }


}
