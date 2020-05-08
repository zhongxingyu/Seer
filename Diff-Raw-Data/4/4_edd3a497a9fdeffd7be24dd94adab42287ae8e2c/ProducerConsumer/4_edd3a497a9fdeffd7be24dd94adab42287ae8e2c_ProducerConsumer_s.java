 public class ProducerConsumer {
 
     // Shared buffer to hold the Charms
     public static final int BUF_SIZE = 3;
     public static Charm[] buffer = new Charm[BUF_SIZE];
 
     public static void main(String[] args)
     throws InterruptedException {
 
         // Create Semaphores
         final Semaphore mutex = new Semaphore(1);
         final Semaphore empty = new Semaphore(buffer.length);
         final Semaphore full  = new Semaphore(0);
 
         // Anonymous inner class defines
         // code for producer thread
         Runnable producer = new Runnable() {
             @Override
             public void run() {
                 int nextp = 0;
                 for(Charm c : Charm.values()) {
                     try {
 
                         empty.semWait();
                         mutex.semWait();
 
                         /******** Start Critical Section ********/
                         System.out.println("Producing: " + c);
                         buffer[nextp] = c;
                         nextp = (nextp + 1) % buffer.length;
                         /********* End Critical Section *********/
 
                         mutex.semSignal();
                         full.semSignal();
 
                     } catch(Exception e) {
                         e.printStackTrace();
                         break;
                     }
                 }
             }
         };
 
         // Anonymous inner class defines
         // code for consumer thread
         Runnable consumer = new Runnable() {
             @Override
             public void run() {
                 int nextc = 0;
                 while(!Thread.interrupted()) {
                     try{
 
                         full.semWait();
                         mutex.semWait();
 
                         /******** Start Critical Section ********/
                         Charm nextCharm = buffer[nextc];
                         System.out.println("Consuming: " + nextCharm);
                         nextc = (nextc + 1) % buffer.length;
                         /********* End Critical Section *********/
 
                         mutex.semSignal();
                         empty.semSignal();
 
                     } catch(InterruptedException e) {
                         break;
                     }
                 }
             }
         };
 
         // Create and start threads
         Thread producerThread = new Thread(producer);
         Thread consumerThread = new Thread(consumer);
         producerThread.start();
         consumerThread.start();
 
         // Wait for producer to finish
         producerThread.join();
 
         // Tell consumer to finish
         consumerThread.interrupt();
         consumerThread.join();
     }
 
     /*
      * Define the Charms that
      * we are producing and consuming.
      */
     public static enum Charm {
         PINK_HEART       (35, 1),
        ORANGE_STAR      (33, 1),
        YELLOW_MOON      (33, 0),
         GREEN_CLOVER     (32, 0),
         BLUE_DIAMOND     (34, 1),
         PURPLE_HORSESHOE (35, 0),
         RED_BALLOON      (31, 0);
 
         private int ansi_color, setting;
 
         private Charm(int ansi_color, int setting) {
             this.ansi_color = ansi_color;
             this.setting = setting;
         }
 
         @Override
         public String toString() {
             String fixed = this.name().replace('_',' ');
             return String.format("\033[%d;%dm%s\033[0m",
                 setting, ansi_color, fixed);
         }
     }
 
 }
