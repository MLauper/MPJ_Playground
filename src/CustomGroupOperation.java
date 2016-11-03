import mpi.MPI;

public class SumAll {
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
        MPI.COMM_WORLD.Scan(outBuffer, 0, inBuffer, 0, bufferSize, MPI.INT, MPI.SUM);

        endTime = System.nanoTime();
        System.out.format("Finished, Rank: %d, Measured Time: %d ms, Calculated Sum: %d\n", rank, (endTime-startTime)/1000/1000, inBuffer[0]);


        MPI.Finalize();
    }
}
