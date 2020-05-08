 package nachos.threads;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import nachos.machine.Lib;
 import nachos.machine.Machine;
 
 /**
  * A scheduler that chooses threads based on their priorities.
  * 
  * <p>
  * A priority scheduler associates a priority with each thread. The next thread
  * to be dequeued is always a thread with priority no less than any other
  * waiting thread's priority. Like a round-robin scheduler, the thread that is
  * dequeued is, among all the threads of the same (highest) priority, the thread
  * that has been waiting longest.
  * 
  * <p>
  * Essentially, a priority scheduler gives access in a round-robin fassion to
  * all the highest-priority threads, and ignores all other threads. This has the
  * potential to starve a thread if there's always a thread waiting with higher
  * priority.
  * 
  * <p>
  * A priority scheduler must partially solve the priority inversion problem; in
  * particular, priority must be donated through locks, and through joins.
  */
 public class PriorityScheduler extends Scheduler {
 	/**
 	 * Allocate a new priority scheduler.
 	 */
 	public PriorityScheduler() {
 	}
 
 	/**
 	 * Allocate a new priority thread queue.
 	 * 
 	 * @param transferPriority
 	 *            <tt>true</tt> if this queue should transfer priority from
 	 *            waiting threads to the owning thread.
 	 * @return a new priority thread queue.
 	 */
 	public ThreadQueue newThreadQueue(boolean transferPriority) {
 		return new PriorityQueue(transferPriority);
 	}
 
 	public int getPriority(KThread thread) {
 		Lib.assertTrue(Machine.interrupt().disabled());
 
 		return getThreadState(thread).getPriority();
 	}
 
 	public int getEffectivePriority(KThread thread) {
 		Lib.assertTrue(Machine.interrupt().disabled());
 
 		return getThreadState(thread).getEffectivePriority();
 	}
 
 	public void setPriority(KThread thread, int priority) {
 		Lib.assertTrue(Machine.interrupt().disabled());
 
 		Lib.assertTrue(priority >= priorityMinimum
 				&& priority <= priorityMaximum);
 
 		getThreadState(thread).setPriority(priority);
 	}
 
 	public boolean increasePriority() {
 		boolean intStatus = Machine.interrupt().disable();
 
 		KThread thread = KThread.currentThread();
 
 		int priority = getPriority(thread);
 		if (priority == priorityMaximum)
 			return false;
 
 		setPriority(thread, priority + 1);
 
 		Machine.interrupt().restore(intStatus);
 		return true;
 	}
 
 	public boolean decreasePriority() {
 		boolean intStatus = Machine.interrupt().disable();
 
 		KThread thread = KThread.currentThread();
 
 		int priority = getPriority(thread);
 		if (priority == priorityMinimum)
 			return false;
 
 		setPriority(thread, priority - 1);
 
 		Machine.interrupt().restore(intStatus);
 		return true;
 	}
 
 	/**
 	 * The default priority for a new thread. Do not change this value.
 	 */
 	public static final int priorityDefault = 1;
 	/**
 	 * The minimum priority that a thread can have. Do not change this value.
 	 */
 	public static final int priorityMinimum = 0;
 	/**
 	 * The maximum priority that a thread can have. Do not change this value.
 	 */
 	public static final int priorityMaximum = 7;
 
 	/**
 	 * Return the scheduling state of the specified thread.
 	 * 
 	 * @param thread
 	 *            the thread whose scheduling state to return.
 	 * @return the scheduling state of the specified thread.
 	 */
 	protected ThreadState getThreadState(KThread thread) {
 		if (thread.schedulingState == null)
 			thread.schedulingState = new ThreadState(thread);
 
 		return (ThreadState) thread.schedulingState;
 	}
 
 	/**
 	 * A <tt>ThreadQueue</tt> that sorts threads by priority.
 	 */
 	protected class PriorityQueue extends ThreadQueue {
 		PriorityQueue(boolean transferPriority) {
 			this.transferPriority = transferPriority;
 		}
 
 		public void waitForAccess(KThread thread) {
 			Lib.assertTrue(Machine.interrupt().disabled());
 			getThreadState(thread).waitForAccess(this);
 		}
 
 		public void acquire(KThread thread) {
 			Lib.assertTrue(Machine.interrupt().disabled());
 			getThreadState(thread).acquire(this);
 		}
 
 		/**
 		 * Remove highest priority thread from the queue. Once it is removed
 		 * calculate its effective priority.
 		 * 
 		 * @return HighestPriority KThread
 		 */
 		public KThread nextThread() {
 			Lib.assertTrue(Machine.interrupt().disabled());
 			ThreadState threadState = this.pickNextThread();
 			priorityQueue.remove(threadState);
 			if (transferPriority && threadState != null) {
 				this.dequeuedThread.removeQueue(this);
 				threadState.waiting = null;
 				threadState.addQueue(this);
 			}
 			this.dequeuedThread = threadState;
 			if (threadState == null) {
 				this.priorityQueue = new LinkedList<ThreadState>();
 				return null;
 			}
 			return threadState.thread;
 		}
 
 		/**
 		 * Return the next thread that <tt>nextThread()</tt> would return,
 		 * without modifying the state of this queue.
 		 * 
 		 * @return the next thread that <tt>nextThread()</tt> would return.
 		 */
 		protected ThreadState pickNextThread() {
 			boolean intStatus = Machine.interrupt().disable();
 			// ensure the priorityQueue is ordered
 			this.priorityQueue = new LinkedList<ThreadState>(priorityQueue);
 			Machine.interrupt().restore(intStatus);
 			return this.priorityQueue.peek();
 		}
 
 		public void print() {
 			Lib.assertTrue(Machine.interrupt().disabled());
 			// ----triplecq----
 			for (Iterator i = priorityQueue.iterator(); i.hasNext();)
 				System.out.println((KThread) i.next() + " ");
 		}
 
 		/**
 		 * <tt>true</tt> if this queue should transfer priority from waiting
 		 * threads to the owning thread.
 		 */
 		public boolean transferPriority;
 
 		// base priority queue object
 		protected LinkedList<ThreadState> priorityQueue = new LinkedList<ThreadState>();
 
 		// most recently dequeued ThreadState
 		protected ThreadState dequeuedThread = null;
 	}
 
 	/**
 	 * The scheduling state of a thread. This should include the thread's
 	 * priority, its effective priority, any objects it owns, and the queue it's
 	 * waiting for, if any.
 	 * 
 	 * @see nachos.threads.KThread#schedulingState
 	 */
 	protected class ThreadState implements Comparable<ThreadState> {
 		/**
 		 * Allocate a new <tt>ThreadState</tt> object and associate it with the
 		 * specified thread.
 		 * 
 		 * @param thread
 		 *            the thread this state belongs to.
 		 */
 		public ThreadState(KThread thread) {
 			this.thread = thread;
 
 			// initialize the onQueue
 			this.onQueues = new LinkedList<PriorityQueue>();
 			this.age = Machine.timer().getTime();
 			this.effectivePriority = priorityDefault;
 			this.waiting = null;
 		}
 
 		/**
 		 * Return the priority of the associated thread.
 		 * 
 		 * @return the priority of the associated thread.
 		 */
 		public int getPriority() {
 			return priority;
 		}
 
 		/**
 		 * Calculate the effective priority of the thread and the thread which
 		 * current holds the resources it's waiting on
 		 */
 		public void calEffectivePriority() {
 			int initPriority = this.getPriority();
 			int maxEP = -1;
 			if (onQueues.size() != 0) {
 				int size = onQueues.size();
 				for (int i = 0; i < size; i++) {
 					PriorityQueue current = onQueues.get(i);
 					ThreadState donator = current.pickNextThread();
 					if (donator != null) {
 						if (donator.getEffectivePriority() > maxEP
 								&& current.transferPriority)
 							maxEP = donator.getEffectivePriority();
 					}
 				}
 			}
 			if (initPriority > maxEP)
 				maxEP = initPriority;
 			this.effectivePriority = maxEP;
 
 			// recalculate the thread which this one is waiting on
 			if (this.waiting != null && this.waiting.dequeuedThread != null)
 				if (this.effectivePriority != this.waiting.dequeuedThread.effectivePriority)
 					this.waiting.dequeuedThread.calEffectivePriority();
 		}
 
 		/**
 		 * Return the effective priority of the associated thread.
 		 * 
 		 * @return the effective priority of the associated thread.
 		 */
 		public int getEffectivePriority() {
 			return this.effectivePriority;
 		}
 
 		/**
 		 * Set the priority of the associated thread to the specified value.
 		 * 
 		 * @param priority
 		 *            the new priority.
 		 */
 		public void setPriority(int priority) {
 			if (this.priority == priority)
 				return;
 
 			this.priority = priority;
 			this.calEffectivePriority();
 			if (this.waiting != null && this.waiting.dequeuedThread != null)
 				this.waiting.dequeuedThread.calEffectivePriority();
 		}
 
 		/**
 		 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
 		 * the associated thread) is invoked on the specified priority queue.
 		 * The associated thread is therefore waiting for access to the resource
 		 * guarded by <tt>waitQueue</tt>. This method is only called if the
 		 * associated thread cannot immediately obtain access.
 		 * 
 		 * @param waitQueue
 		 *            the queue that the associated thread is now waiting on.
 		 * 
 		 * @see nachos.threads.ThreadQueue#waitForAccess
 		 */
 		public void waitForAccess(PriorityQueue waitQueue) {
 			Lib.assertTrue(Machine.interrupt().disabled());
 			long time = Machine.timer().getTime();
 			this.age = time;
 			waitQueue.priorityQueue.add(this);
 			this.calEffectivePriority();
 		}
 
 		/**
 		 * Called when the associated thread has acquired access to whatever is
 		 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
 		 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
 		 * <tt>thread</tt> is the associated thread), or as a result of
 		 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
 		 * 
 		 * @see nachos.threads.ThreadQueue#acquire
 		 * @see nachos.threads.ThreadQueue#nextThread
 		 */
 		public void acquire(PriorityQueue waitQueue) {
			//Lib.assertTrue(Machine.interrupt().disable());
 			Lib.assertTrue(waitQueue.priorityQueue.isEmpty());
 			waitQueue.dequeuedThread = this;
 			this.addQueue(waitQueue);
 			this.calEffectivePriority();
 		}
 
 		public void addQueue(PriorityQueue waitQueue) {
 			onQueues.add(waitQueue);
 			this.calEffectivePriority();
 		}
 
 		public void removeQueue(PriorityQueue waitQueue) {
 			onQueues.remove();
 			this.calEffectivePriority();
 		}
 
 		public int compareTo(ThreadState threadState) {
 			if (threadState == null)
 				return -1;
 			if (this.getEffectivePriority() < threadState
 					.getEffectivePriority() || this.age >= threadState.age)
 				return 1;
 			else
 				return -1;
 		}
 
 		public String toString() {
 			return "ThreadState thread = " + thread + ", priority = "
 					+ getPriority() + ", effective priority = "
 					+ getEffectivePriority();
 		}
 
 		/** The thread with which this object is associated. */
 		protected KThread thread;
 		/** The priority of the associated thread. */
 		protected int priority;
 		// the age of the thread state relative to Nachos time
 		protected long age = Machine.timer().getTime();
 		protected int effectivePriority;
 		protected LinkedList<PriorityQueue> onQueues;
 		protected PriorityQueue waiting;
 	}
 }
