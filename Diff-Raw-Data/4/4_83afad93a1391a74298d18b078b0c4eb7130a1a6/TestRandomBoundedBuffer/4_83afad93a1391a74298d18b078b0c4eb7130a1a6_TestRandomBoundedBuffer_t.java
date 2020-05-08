 
 /**
  * <p>Title: Monitor package and examples</p>
  * <p>Description: </p>
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Company: Memorial University of Newfoundland</p>
  * @author Theodore S. Norvell
  * @version 1.0
  */
 
 /** Test a bounded buffer with multiple producers and consumers.
 */
 
 package examples.BoundedBuffer;
 
 import examples.util.DoneCounter;
 
 public class TestRandomBoundedBuffer {
 
     public static void main(String[] args) {
         int CONSUMERS = 10;
         int PRODUCERS = 1;
         int bufSize = 10;
         int totalNumActions = 10; 
         int delay = 0;
         ObjectBoundedBuffer rw_controller = null;
 
         try {
             bufSize = Integer.parseInt(args[0]);
             switch(args[1].charAt(0)) {
                 case 'e':
                     rw_controller = new ExplicitBoundedBuffer(bufSize);
                     break;
                 default:
                     rw_controller = 
                         new iMonitorBoundedBuffer(bufSize, args[1].charAt(0));
                     break;
             }
             CONSUMERS = Integer.parseInt(args[2]);
             totalNumActions = Integer.parseInt(args[3]);
             delay = Integer.parseInt(args[4]) * 1000;
         } catch (Exception e) { /* use defaults */ 
             e.printStackTrace();
             if(rw_controller == null) {
                 rw_controller = new ExplicitBoundedBuffer(bufSize);
             }
         }
 
         DoneCounter doneCounter = new DoneCounter() ;
 
 
 
 
         doneCounter.set( CONSUMERS + PRODUCERS ) ;
         TestThread[] threads = new TestThread[CONSUMERS + PRODUCERS];
         long startTime = System.currentTimeMillis();
         //System.out.println("Please wait. This takes a while");
         for( int k=0 ; k < CONSUMERS ; ++k ) {
             TestThread w = new RandomObjectConsumer( rw_controller, 
                     doneCounter, totalNumActions/CONSUMERS, delay ) ;
             threads[k] = w;
             w.start(); }
             for( int k=0 ; k < PRODUCERS ; ++k ) {
                 TestThread r = new RandomObjectProducer( rw_controller, 
                         doneCounter, totalNumActions/PRODUCERS, delay) ;
                 threads[k + CONSUMERS] = r;
                 r.start(); }
                 doneCounter.waitForDone() ;
                 long execTime = System.currentTimeMillis() - startTime;
 
                 System.out.println( execTime );
                 System.out.println(rw_controller.getNumContextSwitch());
     }
 }
 
 class RandomObjectProducer extends TestThread {
 
     private ObjectBoundedBuffer boundedBuffer ;
     private DoneCounter doneCounter ;
     private int numActions = 10;
     private int delayT = 0;
 
     RandomObjectProducer( ObjectBoundedBuffer bb, DoneCounter d, int n, int dt) {
         boundedBuffer = bb ; doneCounter = d ; numActions = n; delayT = dt;}
 
     public void run() {
         //for(int i=0 ; i < numActions ; ++i ) {
         while (numActions > 0) {
 
            if (delayT != 0) {
                delay(delayT);
            }
 
             try {
                 int n = boundedBuffer.getNumFreeSlot();
                 n *= Math.random();
                 n += 1;
                boundedBuffer.put(n) ; 
                numActions -= n;
            }
             catch(InterruptedException e ) { }
         }
         doneCounter.increment() ;
     }
 }
 
 class RandomObjectConsumer extends TestThread {
 
     private ObjectBoundedBuffer boundedBuffer ;
     private DoneCounter doneCounter ;
     private int numActions = 10;
     private int delayT;
 
     RandomObjectConsumer( ObjectBoundedBuffer bb, DoneCounter d, int n, int dt) {
         boundedBuffer = bb ; doneCounter = d ; numActions = n; delayT = dt;}
 
     public void run() {
 
         while (numActions > 0) {
             if (delayT != 0) {
                 delay(delayT);
             }
             //if (boundedBuffer.size() > numActions) {
             //    n += numActions/2;
             //    n += Math.random() * (numActions / 2); 
             //} else {
             //    n += boundedBuffer.size()/2;
             //    n += Math.random() * (boundedBuffer.size() / 2); 
             //}
 
             int n = 1;
             if (boundedBuffer.size() > numActions) {
                 n += numActions * Math.random();    
             } else {
                 n += boundedBuffer.size() * Math.random();
             }
 
             numActions -= n;
             try {
                 boundedBuffer.take(n) ; }
             catch(InterruptedException e ) { }
 
         }
         doneCounter.increment() ;
     }
 }
