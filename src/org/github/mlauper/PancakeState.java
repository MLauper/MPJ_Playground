package github.mlauper;

import java.util.LinkedList;

public class PancakeState {
    Pancake pancake;
    int[][] childStates;

    public PancakeState(Pancake pancake){
        this.pancake = pancake;
        this.childStates = pancake.getChildStates();
    }


}
