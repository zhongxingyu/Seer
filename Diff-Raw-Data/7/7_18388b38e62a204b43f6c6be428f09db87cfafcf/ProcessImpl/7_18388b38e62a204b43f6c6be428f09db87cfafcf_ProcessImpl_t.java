 package starter;
 
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.TimeUnit;
 
 import ggTCalculator.Coordinator;
 import ggTCalculator.Log;
 import ggTCalculator.Process;
 import ggTCalculator.ProcessPOA;
 
 public class ProcessImpl extends ProcessPOA implements Runnable {
 
     String name;
     int id;
     Coordinator coordinator;
     Process left;
     Process right;
     int number;
     double delay;
     int timeout;
     Semaphore ready = new Semaphore(0);
     boolean terminate = false;
     boolean running = true;
     Log log;
     LinkedBlockingQueue<Integer> numbers = new LinkedBlockingQueue<Integer>();
     Thread this_thread = new Thread(this);
     int timeoutstartup = 5;
     long last_received;
     
     public ProcessImpl(String name, int nextID, Coordinator coordinator) {
         this.name = name;
         this.id = nextID;
         this.coordinator = coordinator;
         // detach this object
         this_thread.start();
     }
 
     private void termination_start(){
         // ask to the right for permittance to terminate
         // if successful, inform coordinator
         if(right.terminate()){
             coordinator.finished(number);
             // if not successful, give Token back and reset termination-state
         }else{
             coordinator.terminationStop();
             terminate = false;
         }
     }
     
     @Override
     public boolean terminate() {
         // timestamp when to timeout: last_message + (timeout*500)
         long max_time = (last_received+(timeout*500));
         // check if we are the Terminator
         if(terminate == true){
             return true;
             // break termination if we recently received data
         }else if(max_time > System.currentTimeMillis()){
             return false;
             // otherwise ask right neighbour
         }else
             return right.terminate();
     }
 
     @Override
    public void message(int update) {
        numbers.add(update);
         //update timestamp
         last_received = System.currentTimeMillis();
     }
 
     @Override
     public void stop() {
         running = false;
 
     }
 
     @Override
     public void run() {
             //wait for params
             try {
                 ready.acquire();
             } catch (InterruptedException e) {
                 log.log(name+Integer.toString(id), e.toString());
                 e.printStackTrace();
             }
             // calculate
             while(running){
                 Integer next = null;
                 // wait for next number in queue
                 // add warmup time on first calculation
                 try {
                     next = numbers.poll(timeout + timeoutstartup, TimeUnit.SECONDS);
                 } catch (InterruptedException e) {
                     log.log(name+Integer.toString(id), e.toString());
                     e.printStackTrace();
                 }
                 // start termination if we got nothing after timeout
                 if(next == null && coordinator.terminationStart()){
                     log.log(name+Integer.toString(id), "timed out");
                     terminate = true;
                     termination_start();
                 // otherwise calculate
                 }else if(next != null && next < number) {
                     // remove warmup timeout
                     timeoutstartup = 0;
                     // set timestamp
                     // sleep for given delay
                     try {
                         Thread.sleep((long) delay);
                     } catch (InterruptedException e) {
                         log.log(name+Integer.toString(id), e.toString());
                         e.printStackTrace();
                     }
                     // calculate
                     number = ((number - 1)% next) + 1;
                     // notify neighbours
                     left.message(number);
                     right.message(number);
                     log.log(name+Integer.toString(id), "new number "+Integer.toString(number));
                 }
             }
     }
 
     @Override
     public void set_params(Process left, Process right, int number, Log log,
             double delay, int timeout) {
         this.left = left;
         this.right = right;
         this.number = number;
         this.log = log;
         this.delay = delay;
         this.timeout = timeout;
         log.log(name+Integer.toString(id), "got params!");
         // release thread
         ready.release();
     }
 
 }
