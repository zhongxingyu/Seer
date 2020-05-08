 package select2.bench;
 
 /**
  * sync2 protocol implementation, building upon the Java-builtin synchronization primitive: volatile.
  */
 public class Sync2 implements Executor{
 
 	private final long[] threadIds;
 
 	private volatile boolean[] active; // marks whether thread i is selected
 	private volatile boolean[] wait; // marks whether thread i is to wait
 	private volatile int waker; // shows which thread is the waker
     private volatile boolean waker_change; // marks whether there is a waker change in progress
 	private volatile boolean[] selected; // marks whether `thread i` is selected (useful for debugging purposes)
 
 	/**
 	 * The current implementation does not deal with open systems: one must explicitely give the threads to synchronize.
 	 */
 	public Sync2(Thread[] threads){
 		threadIds = new long[2];
 		for (int i = 0; i<2;i++){
 			threadIds[i] = threads[i].getId();
 		}
 		active = new boolean[2];
 		wait = new boolean[2];
 		selected = new boolean[2];
 	}	
 
 
 	/**
 	 * sync2 application service that executes the give closure in a synchronized manner.
 	 * Returns true if the closure execution was successful, otherwise false. 
 	 */	
 	public boolean execute(Closure closure){
 		// get the internal thread number
 		int i =  getInternalThreadId();
 		System.out.println("activate(" + i + ")");
 	
 		// #######################################################################
 		// guard stage
 		// #######################################################################
 
 		// mark this thread as active
 		active[i] = true;
 
 		// check whether the other thread is active
 		if (active[(i + 1) % 2]){
 
 			// if the other thread is active then mark this to wait 
 			wait[i] = true;
 
 			// wait until the other thread is deactived or marks this thread to not wait
 			int spin = 0;
 			while ( active[(i + 1) % 2] && wait[i] ){
 
 				// if this is waker wake up the other thread
 				if (waker == i){ wait[(i + 1) % 2] = false; }
 				
 				// if waker change is in progress then acknowledge it
 				if (waker_change){ waker_change = false; }
 				
 				System.out.println("guard_wait(" + i + ")");
 				
 				Thread.yield();
 			}
 		}
 		else{
 			System.out.println("no_guard_wait(" + i + ")");
 		}
 		
 		// #######################################################################
 		// selection stage
 		// #######################################################################
 
 		// mark this selected 
 		selected[i] = true;
 
 		assert !selected[(i+1) % 2];
 		
 		// execute the business method
 		boolean result = closure.execute();
 		
 		// mark this unselected
 		selected[i] = false;
 
 		// change waker if this is not the one
 		if (waker != i){
 
 		   // change waker to point to this thread
 		   waker = i;
 
 		   // tell the other thread about the change
 		   if (active[(i + 1) % 2]){
 
 			   // yield the change
 			   waker_change = true;
 
 			   // wait until the other thread acknowledges the change
			   while (waker_change){ Thread.yield(); }
 			}
 			else{
 				    System.out.println("no_waker_wait(" + i + ")");
 			}
 		}
 		
 		System.out.println("deactivate(" + i + ")");
 		// mark this inactive
 		active[i] = false;
 		
 		// return the result of the business execution
 		return result; 
 	}
 	
 	/**
 	 * Get the internal thread id used in the protocol: 0 or 1.
 	 */
 	protected int getInternalThreadId(){
 		if (threadIds[0] == Thread.currentThread().getId()){ return 0; }
 		else{ return 1; }
 	}	
 }
