import mpi.MPI;


// Run with C:\code\src\github.com\MLauper\MPJ_Playground\out\production\MPJ_Playground>"C:\mpj\bin\mpjrun.bat" -np 12 HelloWorld

public class HelloWorld {

    public static void main(String[] args) {
        int rank, size, i;
        MPI.Init(args);
        rank = MPI.COMM_WORLD.Rank();
        if (rank == 0) System.out.println("Hello World");
        else System.out.println("I am slave nb. " + rank);
        MPI.Finalize();
    }

}