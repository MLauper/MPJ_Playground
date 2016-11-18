package github.mlauper;

import java.util.Stack;


public class PancakeFlipper {

    public Stack<State> stateStack;

    public class State {
        public int[] pancakeOrder;
        public int heuristicNumber;
        public int depth;
        public Stack<State> unexploredChilds;
        public int flipFromParent;

        private int pancakeSize;

        public State(int[] pancakeOrder, int depth){
            this.depth = depth;
            this.pancakeOrder = pancakeOrder;

            this.pancakeSize = pancakeOrder.length;
            calculateHeuristicNumber();
            exploreChildNodes();
        }

        public void exploreChildNodes() {
            for (int i = 0; i < pancakeSize; i++){
                unexploredChilds.push(new State(this.flippedPancakeOrder(i), this.depth+1));
            }
        }

        public int[] flippedPancakeOrder(int Position){
            int[] flippedPancakes = new int[this.pancakeSize];

            System.arraycopy(this.pancakeOrder, 0, flippedPancakes, 0, Position);

            for (int i = Position; i < pancakeSize; i++){
                flippedPancakes[i] = this.pancakeOrder[pancakeSize - i];
            }

            return flippedPancakes;
        }

        private void calculateHeuristicNumber(){
            this.heuristicNumber = 0;
            for (int i = 0; i < pancakeSize-1; i++){
                int x = pancakeOrder[i];
                int y = pancakeOrder[i+1];
                if (x + 1 != y && x -1 != y){
                    this.heuristicNumber += 1;
                }
            }
        }

    }

    public PancakeFlipper(int[] pancakeOrder) {
        int[] augmentedPancakeOrder = new int[pancakeOrder.length + 1];
        System.arraycopy(pancakeOrder, 0, augmentedPancakeOrder, 0, pancakeOrder.length);
        augmentedPancakeOrder[pancakeOrder.length] = Integer.MAX_VALUE;
        stateStack.push(new State(augmentedPancakeOrder, 0));
    }

    public void solveSequencial(){
        int limit = stateStack.peek().heuristicNumber;
        int optimisticalSearthDepth = stateStack.peek().heuristicNumber;

        while (stateStack.peek().heuristicNumber != 0) {

            // Check if limit is reached
            if (stateStack.peek().heuristicNumber + stateStack.peek().depth > limit){
                // Solution with current limit not available in current path, try going back
                if(stateStack.peek().depth == 0){
                    // Already at root node, increase limit
                    // TODO: Do not simply increase the limit
                    limit += 1;
                } else {
                    stateStack.pop();
                }
            }

            if (stateStack.peek().unexploredChilds.empty()) {
                // No more paths to go, go up
                stateStack.pop();
            } else {
                // Try next path
                this.stateStack.push(this.stateStack.peek().unexploredChilds.pop());
            }
        }

        // Found a solution (heuristic Number is zero)
    }

}
