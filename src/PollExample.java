import java.util.Random;

import mpi.*;



public class PollExample {
	public static void main(String[] args) {
		MPI.Init(args);
		int size = MPI.COMM_WORLD.Size();
		int rank = MPI.COMM_WORLD.Rank();
		Random rand = new Random();
		long sTime = System.currentTimeMillis(); // start
		int waitTime = 100;
		long duration = 100000; // msec  
		long limit = 2000; // msec 
		double chance = 0.05; // chance that a msg is send (while polling)
		int [] outBuf = new int[1];
		int [] inBuf = new int[size];
		int inCnt = 0;
		int outCnt =0;
		Request [] reqs = new Request[size];
		// set up listening to all
		for (int i=0;i<size;i++){
			if (i!=rank){
				reqs[i] = MPI.COMM_WORLD.Irecv(inBuf,0,1, MPI.INT, i , 0);
			}
		}
		// polling loop
		while (System.currentTimeMillis()-sTime < 10000){
			try {
				Thread.currentThread().sleep(waitTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (rand.nextInt(waitTime) < chance*waitTime && (System.currentTimeMillis()-sTime < duration-limit)) {
				int target = (rank+rand.nextInt(size-1)+1) % size;
				MPI.COMM_WORLD.Send(inBuf,0,1, MPI.INT,target, 0);
				outCnt++;
				System.out.println("rank "+rank+" sent MSG to "+target) ;
			}
			// listen
			for (int i=0;i<size;i++){
				if (i!=rank){
					if (reqs[i].Test() != null){
						System.out.println("rank "+rank+" got MSG from "+i) ;
						inCnt++;
						reqs[i] = MPI.COMM_WORLD.Irecv(inBuf,0,1, MPI.INT, i , 0);						
					}
				}
			}			
		}	
		System.out.println("Rank "+rank+ " sendt "+outCnt+" messages and got "+inCnt+" messages");
		MPI.Finalize();
	}

}
