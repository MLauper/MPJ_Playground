import mpi.MPI;

import java.util.ArrayList;

public class PingPong {
    public static void main(String[] args) {
        int rank;
        MPI.Init(args);
        rank = MPI.COMM_WORLD.Rank();
        int bufferSize = 1;
        int outBuffer[] = new int[bufferSize];
        int inBuffer[] = new int[bufferSize];
        int iterations = 1000;
        int j;
        long startTime, endTime;

        ArrayList<Integer> meassureSizes = new ArrayList<Integer>() {{
            add(8);
            add(8*64);
            add((int) (8*Math.pow(64,2)));
            add((int) (8*Math.pow(64,3)));
        }};

        for(Integer size : meassureSizes){
            System.out.format("Start measuring with size: %d\n", size);
            bufferSize = size;

            outBuffer = new int[bufferSize];
            inBuffer = new int[bufferSize];

            j = 0;
            startTime = System.nanoTime();
            if (rank%2 == 0) {
                while (j < iterations){
                    MPI.COMM_WORLD.Send(outBuffer, 0, bufferSize, MPI.INT, 1, 1);
                    MPI.COMM_WORLD.Recv(inBuffer, 0, bufferSize, MPI.INT, 1, 1);
                    j++;
                }
            } else {
                while (j < iterations){
                    MPI.COMM_WORLD.Recv(inBuffer, 0, bufferSize, MPI.INT, 0, 1);
                    MPI.COMM_WORLD.Send(outBuffer, 0, bufferSize, MPI.INT, 0, 1);
                    j++;
                }
            }
            endTime = System.nanoTime();
            System.out.format("Finished, Rank: %d, Iterations: %d, Measured Time: %d ms\n", rank, j, (endTime-startTime)/1000/1000);

        }

        MPI.Finalize();
    }

}
