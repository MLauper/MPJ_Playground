package github.mlauper;

import java.util.LinkedList;

public class Pancake {
    private LinkedList<Integer> pancakes;
    private LinkedList<LinkedList<Integer>> candidatePancakes = new LinkedList<>();
    private LinkedList<LinkedList<Integer>> failedPancakes = new LinkedList<>();

    public Pancake(LinkedList<Integer> pancakes){
        this.pancakes = pancakes;
    }

    public void printPancakes (){
        System.out.println(pancakes);
    }

    public void calculateFlips () {
        while(!this.isPancakeOrdered()){
            failedPancakes.add((LinkedList<Integer>) pancakes.clone());
            for (LinkedList<Integer> pancakeCandidate :
                    this.getChildStates()) {
                if(!candidatePancakes.contains(pancakeCandidate) && !failedPancakes.contains(pancakeCandidate)){
                    candidatePancakes.add(pancakeCandidate);
                }
            }

            this.pancakes = candidatePancakes.pop();
        }
    }

    public boolean isPancakeOrdered(){
        int size = pancakes.size();
        for (int i = 0; i < size-1; i++) {
            if (pancakes.get(i) + 1 != pancakes.get(i + 1)){
                return false;
            }
        }
        return true;
    }

    public void flip(int Position){
        pancakes = this.fliped(Position);
    }

    public LinkedList<Integer> fliped(int Position){
        LinkedList<Integer> flipedPancakes = (LinkedList<Integer>) pancakes.clone();
        LinkedList<Integer> temp = new LinkedList<>();
        int j = 0;
        for (int i = Position; i < flipedPancakes.size(); i++){
            temp.add(j,flipedPancakes.get(i));
            j++;
        }
        j = Position;
        for (int i = temp.size()-1; i >= 0; i--){
            flipedPancakes.set(j, temp.get(i));
            j++;
        }
        return flipedPancakes;
    }

    public LinkedList<Integer> getPancakes(){
        return pancakes;
    }

    public LinkedList<LinkedList<Integer>> getChildStates() {
        LinkedList<LinkedList<Integer>> childStates = new LinkedList<>();
        for (int i = 1; i < pancakes.size()-1; i++) {
            childStates.add(new LinkedList<>(this.fliped(i)));
        }
        return childStates;
    }
}
