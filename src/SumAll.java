import mpi.MPI;

import java.util.ArrayList;

public class Ring {
    public static void main(String[] args) {

        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int numOfRanks = MPI.COMM_WORLD.Size();
        int bufferSize = 1;
        int outBuffer[] = new int[bufferSize];
        int inBuffer[] = new int[bufferSize];
        long startTime, endTime;

        startTime = System.nanoTime();

        mpi.Request request;
        if (rank == 0) {
            // first rank, receive from last rank
            System.out.format("Rank %d: Receive from %d, Send to %d\n", rank, numOfRanks-1, rank+1);
            request = MPI.COMM_WORLD.Irecv(inBuffer, 0, bufferSize, MPI.INT, numOfRanks-1, 1);
            MPI.COMM_WORLD.Send(outBuffer, 0, bufferSize, MPI.INT, rank+1, 1);
            try {
                request.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (rank == (numOfRanks-1)) {
            // Last rank, send to rank 0
            System.out.format("Rank %d: Receive from %d, Send to %d\n", rank, rank-1, 0);
            request = MPI.COMM_WORLD.Irecv(inBuffer, 0, bufferSize, MPI.INT, rank-1, 1);
            MPI.COMM_WORLD.Send(outBuffer, 0, bufferSize, MPI.INT, 0, 1);
            try {
                request.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            // other ranks
            System.out.printf("Rank %d: Receive from %d, Send to %d\n", rank, rank-1, rank+1);
            request = MPI.COMM_WORLD.Irecv(inBuffer, 0, bufferSize, MPI.INT, rank-1, 1);
            MPI.COMM_WORLD.Send(outBuffer, 0, bufferSize, MPI.INT, rank+1, 1);
            try {
                request.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        endTime = System.nanoTime();
        System.out.format("Finished, Rank: %d, Measured Time: %d ms\n", rank, (endTime-startTime)/1000/1000);


        MPI.Finalize();
    }
}
