package github.mlauper;

import java.util.Arrays;
import java.util.Stack;


public class PancakeFlipper {

    public Stack<State> stateStack = new Stack<>();

    public class State {
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
    }

    public void solveSequencial(){

        // Initially set heuristic number as limit
        int limit = stateStack.peek().heuristicNumber;
        System.out.printf("Initial Limit: %d\n",limit);

        // describes the new search limit, based on aborted earlier searches
        int candidateLimit = Integer.MAX_VALUE;

        // Search until heuristicNumber is 0, which means that a solution was found
        while (stateStack.peek().heuristicNumber != 0) {

            // Check if limit is reached
            if ((stateStack.peek().heuristicNumber + stateStack.peek().depth) > limit){
                // Solution with current limit not available in current path, try going back
                // Record new candidate limit
                int stateLimit = stateStack.peek().depth + stateStack.peek().heuristicNumber;
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

        // Found a solution (heuristic Number is zero), print result
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

}
