 package dk.itu.ecdar.text.generator.framework;
 
 import dk.itu.ecdar.text.generator.environment.QuickLog;
 
 /**
  * Base class for timed I/O automata implementations.
  */
 public abstract class ITIOA {
 
 	/**
 	 * This thread monitors transitions over input edges.
 	 * It will go sleeping each time no edge is available
 	 * until there is at least one edge available.
 	 */
 	class TransitionThread extends Thread {
 		@Override
 		public void run () {
 			
 			while (true) {
				
 				transition();
 				
 				//	Put this thread to sleep while waiting for the first
 				// edge to become available (lazy waiting)
 				try {
 					super.sleep(getMinWaitingTime() * AutomatonTimer.getResolution());
 				} catch (InterruptedException e) {
 				}
 			}
 		}
 	}
 	
 	public AutomatonTimer timer;
 	protected ILocation current;
 	
 	boolean executing, executed;
 	private boolean running;
 	
 	private TransitionThread transitionThread;
 
 	public ITIOA() {
 
 		// TODO: Replace this with RTS Java clock instance
 		timer = new AutomatonTimer();
 		
 		running = false;
 		transitionThread = new TransitionThread();
 	}
 
 	/**
 	 * Starts the automaton. Will only have effect once.
 	 */
 	public void run() {
 		if (!running) {
 			resetTime();
 			
 			// Execute the initial location
 			current.execute();
 			
 			transitionThread.start();
 			running = true;
 		}
 	}
 	
 	/**
 	 * Resets the ITIOA flags to false.
 	 */
 	private synchronized void reset() {
 		executing = false;
 		executed = false;
 	}
 
 	/**
 	 * Notifies the TIOA about input.
 	 * 
 	 * Currently, the system will exit if input must be handled during
 	 * non-preemptive task execution.
 	 * 
 	 * Synchronized so that state does not change during notification.
 	 * 
 	 * @param input Some action
 	 */
 	public synchronized void notify(String input) {
 		IInputEdge that = null;
 
 		// Check for an edge that accepts this input and for which
 		// the guard holds.
 		for (IInputEdge edge : current.inputEdges) {
 			if (edge.acceptInput(input) && edge.checkGuard(getTime())) {
 				that = edge;
 				break;
 			}
 		}
 
 		// If there is no edge that handles this input, ignore it.
 		if (that == null)
 			return;
 
 		// If the current location is executing a task and
 		// not preemptive, we can safely kill it. Otherwise,
 		// we exit the program.
 		if (executing) {
 			if (current.isPreemptive()) {
 				current.kill();
 			} else {
 				QuickLog.log(toString(), getTime(),
 						"Received input during execution of non-preemptive task! Input will be ignored.");
 				return;
 			}
 		}
 
 		// Traverse and reset.
 		current = that.traverse();
 		reset();
 		execute();
 		
 		// Wake up the transition thread!
 		// TODO: Is this sensitive enough?
 		transitionThread.interrupt();
 	}
 
 	/**
 	 * Executes the task at the current location if it hasn't been executed
 	 * before.
 	 */
 	private void execute() {
 		if (!executing && !executed) {
 			current.execute();
 		}
 	}
 
 	/**
 	 * This is used to employ lazy waiting during transitions.
 	 * 
 	 * @return The minimum waiting time as long until the first edge becomes available.
 	 */
 	private long getMinWaitingTime() {
 		long time = 0;
 		
 		// Wait 1000ms 
 		if (current.outputEdges.length == 0)
 			return 1000;
 		
 		while (true) {
 			time++;
 			for (IEdge e : current.outputEdges) {
 				
 				// TODO: Time will continue. How should we handle this? Synchrony hypothesis?
 				if (e.checkGuard(getTime() + time))
 					return time;
 			}
 		}
 	}
 	
 	/**
 	 * Performs a transition on the automaton
 	 * 
 	 * The transition should ideally be highly optimized and take very few time.
 	 * However, this implementation grounds on the <b>synchrony hypothesis</b>.
 	 * 
 	 * Synchronized so the state stays the same during execution.
 	 */
	private void transition() {
 		
 		// An uncontrollable transition is only possible if the task
 		// at the current location has been performed.
 		if (!executing && executed) {
 			for (IEdge edge : current.outputEdges) {
 				if (edge.checkGuard(getTime())) {
 					current = edge.traverse();
 
 					// Reset only if sure that traversal has happened
 					reset();
 					execute();
 					return;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * This is a wrapper for getting the current time.
 	 * By using the wrapper instead of manipulating the
 	 * timer object directly, it is easier to alter
 	 * the implementation.
 	 * 
 	 * @return The current time on the automaton
 	 */
 	public final long getTime() {
 		return timer.getTime();
 	}
 	
 	/**
 	 * This is a wrapper for resetting the current time.
 	 * By using the wrapper instead of manipulating the
 	 * timer object directly, it is easier to alter
 	 * the implementation.
 	 */
 	public final void resetTime() {
 		timer.reset();
 	}
 	
 	@Override
 	public String toString() {
 		return getClass().getSimpleName();
 	}
 }
