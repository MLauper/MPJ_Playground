import mpi.Datatype;
import mpi.MPI;
import mpi.Op;
import mpi.User_function;

public class CustomGroupOperation {
    public static void main(String[] args) {

        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int numOfRanks = MPI.COMM_WORLD.Size();
        int bufferSize = 1;
        int outBuffer[] = new int[bufferSize];
        int inBuffer[] = new int[bufferSize];
        long startTime, endTime;

        startTime = System.nanoTime();

        outBuffer[0] = rank;
        MPI.COMM_WORLD.Scan(outBuffer, 0, inBuffer, 0, bufferSize, MPI.INT, new Op(new MyOp(), false));

        endTime = System.nanoTime();
        System.out.format("Finished, Rank: %d, Measured Time: %d ms, Calculated Sum: %d\n", rank, (endTime-startTime)/1000/1000, inBuffer[0]);


        MPI.Finalize();
    }

    static class MyOp extends User_function {
        @Override
        public void Call(Object inObj, int inOffset, Object outObj, int outOffset, int cnt, Datatype arg5){

            ((int[])outObj)[0] = 42;
        }
    }
}
