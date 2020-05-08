 import java.util.concurrent.Semaphore;
 
 public class Barrier implements Runnable {
   Semaphore stop;
   Semaphore[] gates;
   Semaphore finished;
   int threads;
 
   Operation[] operations;
   Image helper;
   Image im;
 
   public Barrier(Semaphore finished, Semaphore stop, Semaphore[] gates, int threads, Operation[] operations, Image im, Image helper) {
     this.stop = stop;
     this.gates = gates;
     this.threads = threads;
     this.operations = operations;
     this.im = im;
     this.helper = helper;
     this.finished = finished;
   }
 
   public void run() {
     for (Operation op : this.operations) {
       System.out.println(op);
       // wait for all of the threads to stop
       for (int i = 0; i < threads; i++)
         try { stop.acquire(); } catch (Exception e) {}
        // release all worker threads
        for (int i = 0; i < threads; i++)
          gates[i].release();
     }
     finished.release();
   }
 }
