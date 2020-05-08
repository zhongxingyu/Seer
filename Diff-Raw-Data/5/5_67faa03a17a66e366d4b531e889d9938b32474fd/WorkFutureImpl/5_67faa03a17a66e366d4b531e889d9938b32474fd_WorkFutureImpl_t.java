 /*
  * Copyright 2010, 2011 Eric Myhre <http://exultant.us>
  * 
  * This file is part of AHSlib.
  *
  * AHSlib is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, version 3 of the License, or
  * (at the original copyright holder's option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package us.exultant.ahs.thread;
 
 import us.exultant.ahs.core.*;
 import us.exultant.ahs.util.*;
 import java.util.*;
 import java.util.concurrent.*;
 import java.util.concurrent.locks.*;
 
 /**
  * This is one of the most common implementations for use in managing tasks in a
  * WorkScheduler.
  */
 class WorkFutureImpl<$V> implements WorkFuture<$V> {
 	WorkFutureImpl(WorkScheduler $parent, WorkTarget<$V> $wt, ScheduleParams $schedp) {
 		this.$parent = $parent;
 		this.$work = $wt;
 		this.$schedp = $schedp;
 		this.$sync = new Sync();
 		this.$completionListeners = new ArrayList<Listener<WorkFuture<?>>>(1);
 	}
 	
 	
 	
 	/** My guts. */
 	final Sync					$sync;
 	
 	/**
 	 * This is largely just for being able to pass on {@link #update()} calls. We
 	 * could use it in a lot of defensive sanity checks as well, but we usually don't
 	 * because of reasons (tight loops, mainly).
 	 */
 	final WorkScheduler				$parent;
 	
 	/** The underlying callable */
 	final WorkTarget<$V>				$work;
 	
 	/** The parameters with which the work target was scheduled. */
 	private final ScheduleParams			$schedp;
 	
 	/**
 	 * The result to return from get(). Need not be volatile or synchronized because
 	 * it is never readable outside of this class until after a transition guarded by
 	 * the AQS.
 	 */
 	private $V					$result		= null;
 	/**
 	 * The (already wrapped) exception to throw from get(). Need not be volatile or
 	 * synchronized because it is never readable outside of this class until after a
 	 * transition guarded by the AQS.
 	 */
 	private ExecutionException			$exception	= null;
 	
 	/** Index into delay queue, to support faster updates. */
 	int						$heapIndex	= -1;
 	
 	/** When nulled after set/cancel, this indicates that the results are accessible. */
 	volatile Thread					$runner;
 	
 	/**
 	 * A list of {@link Listener} to be called as soon as possible after this task
 	 * becomes done. This is synchronized on before adding new elements, and before
 	 * transitioning to done (we're not worried about efficiency because this
 	 * operation should be quite rare (i.e. never ever ever ever in a loop) and
 	 * contention not really an issue).
 	 */
 	private final List<Listener<WorkFuture<?>>>	$completionListeners;
 	
 	
 	
 	WorkTarget<$V> getWorkTarget() {
 		return $work;
 	}
 	
 	public State getState() {
 		return $sync.getWFState();
 	}
 	
 	public ScheduleParams getScheduleParams() {
 		return $schedp;
 	}
 	
 	public void update() {
 		$parent.update(this);
 	}
 	
 	public boolean cancel(boolean $mayInterruptIfRunning) {
 		return $sync.cancel($mayInterruptIfRunning);
 	}
 	
 	public boolean isCancelled() {
 		return getState() == State.CANCELLED;
 	}
 	
 	public boolean isDone() {
 		switch (getState()) {
 			case FINISHED: return true;
 			case CANCELLED: return true;
 			default: return false;
 		}
 	}
 	
 	public $V get() throws InterruptedException, ExecutionException, CancellationException {
 		$sync.waitForDone();
 		return $sync.get(); 
 	}
 	
 	public $V get(long $timeout, TimeUnit $unit) throws InterruptedException, ExecutionException, TimeoutException, CancellationException {
 		$sync.waitForDone($unit.toNanos($timeout));
 		return $sync.get(); 
 	}
 	
 	public void addCompletionListener(Listener<WorkFuture<?>> $completionListener) {
 		synchronized ($completionListeners) {
 			if (isDone()) $completionListener.hear(this);
 			else $completionListeners.add($completionListener);
 		}
 	}
 	
 	public String toString() {
 		return Reflect.getObjectName(this)+"[State="+Strings.padRightToWidth(this.getState()+";",11)+"workstatus="+Strings.padRightToWidth($work.isDone() ? "done;" : $work.isReady() ? "ready;" : "unready;",9)+"work="+$work+"]";
 	}
 	
 	
 	
 	/**
 	 * Uses AQS sync state to represent run status.
 	 * 
 	 * Method names prefixed with "scheduler_" are those that I wish were in the damn
 	 * WorkScheduler, but aren't because the CAS'ing must come from inside here.
 	 */
 	final class Sync extends AbstractQueuedSynchronizer {
 		Sync() {}
 		
 		State getWFState() {
 			return State.values[getState()];
 		}
 		
 		/** Implements AQS base acquire to succeed if finished or cancelled */
 		protected int tryAcquireShared(int $ignore) {
 			/* denying acquires until that runner is null gives us a window
 			 * of time between doing the CAS to FINISHED and actually letting
 			 * the get(*) methods return where we can muck with the $result
 			 * field in a completely locked way, without actually ever building
 			 * a lock around it at any other time in our lifecycle.  neat, huh?
 			 */
 			return isDone() && $runner == null ? 1 : -1;
 		}
 		
 		/** Implements AQS base release to always signal after setting final done status by nulling runner thread. */
 		protected boolean tryReleaseShared(int $ignore) {
 			$runner = null;
 			// we don't call the completion listener here because this can be hit more than once; the thread nulling just works because it's essentially idempotent in this context 
 			return true;
 		}
 		
 		void waitForDone() throws InterruptedException {
 			acquireSharedInterruptibly(0);
 		}
 		
 		void waitForDone(long $nanosTimeout) throws InterruptedException, TimeoutException {
 			if (!tryAcquireSharedNanos(0, $nanosTimeout)) throw new TimeoutException();
 		}
 		
 		$V get() throws ExecutionException, CancellationException {	// this could also be implemented in the outer class, but it's ever so slightly more efficient to use the int state here instead of go through the array lookup.  probably.  i dunno, maybe that even gets optimized out by a good enough jvm.
 			if (getState() == State.CANCELLED.ordinal()) throw new CancellationException();
 			if ($exception != null) throw $exception;
 			return $result;
 		}
 		
 		/** Called exactly once.  Called AFTER the CAS to completion has already been completed. */
 		protected void hearDone() {
 			synchronized ($completionListeners) {
 				for (Listener<WorkFuture<?>> $x : $completionListeners)
 					$x.hear(WorkFutureImpl.this);
 				$completionListeners.clear();	// let that crap be gc'd even if this future is forced to hang around for a while
 			}
 		}
 		
 		
 		
 		boolean cancel(boolean $mayInterruptIfRunning) {
 			for (;;) {
 				int $s = getState();
 				if ($s == State.FINISHED.ordinal()) return false;
 				if ($s == State.CANCELLED.ordinal()) return false;
 				if (compareAndSetState($s, State.CANCELLED.ordinal())) break;
 			}
 			if ($mayInterruptIfRunning) {
 				Thread $r = $runner;
 				if ($r != null) $r.interrupt();
 			}
 			releaseShared(0);
			update();
 			hearDone();
 			return true;
 		}
 		
 		/**
 		 * <p>
 		 * Checks the state of this WorkFuture. Changes only occur if the
 		 * WorkFuture is currently WAITING and should become SCHEDULED. If the
 		 * task is clock-based, only delay is checked; otherwise, only
 		 * $work.isReady() is checked.
 		 * </p>
 		 * 
 		 * <p>
 		 * If this method returns false, it may merely because the task isn't
 		 * ready or is delayed, but it may also be because the task is FINISHED or
 		 * CANCELLED, which is something the caller is advised to check. False
 		 * will also be returned if the task is already SCHEDULED.
 		 * </p>
 		 * 
 		 * @return true if the scheduler must now remove the WF from the waiting
 		 *         pool (or delayed heap) and push it into the scheduled heap.
 		 */
 		boolean scheduler_shiftToScheduled() {
 			// the CAS will occur if:
 			//   - the task if delay-free (if clocked) or ready (if unclocked).
 			//        if this is not the case, obviously we won't be switching out of waiting state.
 			// the CAS will fail and return false if:
 			//   - we're FINISHED (if this happened at the end of the task's last run, this task shouldn't have stayed in the scheduler to get to this call again.  but it could have happened concurrently as a result of a thread outside the scheduler calling an update on a task that become done concurrently (close of a data source for example).)
 			//   - we've been CANCELLED concurrently
 			return shiftToScheduled(State.WAITING.ordinal());
 		}
 		private boolean shiftToScheduled(int $prevState) {
 			if ($schedp.isUnclocked() ? $work.isReady() : $schedp.getDelay() <= 0) return compareAndSetState($prevState, State.SCHEDULED.ordinal());
 			return false;
 		}
 		
 		/**
 		 * @return true, mostly.  False if we were concurrently cancelled.
 		 */
 		boolean scheduler_shiftToRunning() {
 			return compareAndSetState(State.SCHEDULED.ordinal(), State.RUNNING.ordinal());
 		}
 		
 		/**
 		 * Causes the current thread to take ownership of the task and power it.
 		 * 
 		 * This should be immediately preceded by a call to
 		 * {@link #scheduler_shiftToRunning()}. This should NOT be called while
 		 * holding the Scheduler's lock.
 		 * 
 		 * @return true if all went well and the task should be shifted out of
 		 *         running and back to either waiting or scheduled; false if we
 		 *         finished the task.
 		 */
 		boolean scheduler_power() {
 			$runner = Thread.currentThread();
 			if (getState() == State.RUNNING.ordinal()) { /* recheck after setting thread */
 				try {
 					$V $potentialResult = $work.call();
 					
 					if ($work.isDone()) {
 						tryFinish(true, $potentialResult, null);
 						return false;	/* even if tryFinish failed and returned false, since we're the running thread, that still means the task is finished (just that we weren't the ones to make it happen). */
 					} else {
 						if ($potentialResult != null) $result = $potentialResult;
 						$schedp.setNextRunTime(); /* it'd be cool if we could set this only if we cas'd to waiting successfully, but that actually introduces a mind-bending race where if you call update on a clock based tasks immediately after that cas, you'll get it to skip ahead into the scheduled heap because we haven't shifted its goal time yet. */
 						return true;
 					}
 				} catch (Throwable $t) {
 					$exception = new ExecutionException($t);
 					tryFinish(true, null, $exception);
 					return false;
 				}
 			} else {
 				/* there was a concurrent cancel or finish.  (note that this will result in null'ing $runner via tryReleaseShared(int).) */
 				releaseShared(0);
 				return false;
 			}
 		}
 		
 		/**
 		 * This method is called immediately after {@link #scheduler_power()}, as
 		 * soon as the scheduler has reacquired its internal lock. The state of
 		 * this work is still almost certainly {@link WorkFuture.State#RUNNING},
 		 * unless it was concurrently cancelled or finished.
 		 * 
 		 * @return the State we shifted into.
 		 */
 		State scheduler_shiftPostRun() {
 			if ($work.isDone()) {
 				tryFinish(true, null, null);
 				return getWFState();	/* finished or canceled */
 			} else if (shiftToScheduled(State.RUNNING.ordinal())) {
 				return State.SCHEDULED;	/* scheduled.  a cancel could come, but after this it's the post-cancel update that bears the burden of releasing pointers. */
 			} else if (compareAndSetState(State.RUNNING.ordinal(), State.WAITING.ordinal())) {
				if ($work.isDone()) tryFinish(false, null, null);	/* we have to do this AGAIN, yes.  we have to do it after the transition to waiting, or else another thread can do a final notify between the tryfinish at the top of this function and the one here. */
				return getWFState();
 			}
 			return getWFState();	/* cancelled or finished concurrently sometime during the run or our post-processing leading up to now. */
 		}
 		
 		/**
 		 * @return true if this invocation causes the finishing of this Future
 		 *         (and implicitly the final set and the completion notification).
 		 *         False if:
 		 *         <ul>
 		 *         <li>Finishing already occured (or occured concurrently in another thread).
 		 *         <li>Cancelling already occured (or occured concurrently in another thread).
 		 *         <li>The work is currently being run by another thread (in which case that thread is responsible for noticing finish).
 		 *         </ul>
 		 */
 		boolean tryFinish(final boolean $iAmTheRunner, $V $finish, ExecutionException $fail) {
 			// these come as a standard check whenever a shift returns false, or whenever an update call (from any thread!) noticed isDone was true.
 			// because this method is package-protected, we're going to assume that you've checked isDone before calling this, and that it was true.  (Sure, most people's isDone method is pretty cheap, but we're going to shoot for savings/contention-avoidance here anyway.)
 			// CONCURRENT FINISHES ARE BLOCKED IF WE'RE RUNNING.
 			//   because that's INSANE.  we have to set the answer of our current run.  and things often become "done" as data sources when they give us their last thing, but that clearly doesn't mean we're finished yet and shouldn't report the results of that last data piece.
 			//   (this is different than concurrently cancelled, mind; that's totally allowed.)
 			
 			for (;;) {
 				int $s = getState();
 				if ($s == State.FINISHED.ordinal())
 					return false;		// we're already done, obviously there's nothing to do here.  (we notice this in particular since we don't want to double-sent the done notification to listeners, nor invoke ridiculous releases).
 				if ($s == State.CANCELLED.ordinal()) {
 					releaseShared(0);	// aggressively release to set runner to null, in case we are racing with a cancel request that will try to interrupt runner
 					return false;		// we're letting the cancel go though (though trying to dodge any interrupts the cancellation might have requested).
 				}
 				if ($s == State.RUNNING.ordinal())
 					if ($iAmTheRunner);	// it's kay, we can continue to the finishing since this is our job.
 					else return false;	// the working thread will deal with noticing doneness again when it's completed its cycle.
 				// $s is either SCHEDULED or WAITING.  well, or RUNNING if we're the one running it.
 				if (compareAndSetState($s, State.FINISHED.ordinal())) {
 					// do the final set	// these are the couple of lines that kinda make a fool of that claim about get() returning "immediately" when state is FINISHED... it's not true if we happen to be in these three lines right here.
 					if ($fail != null) $exception = $fail;
 					else if ($finish == null);
 					else $result = $finish;
 					// do the final release and notify completion
 					releaseShared(0);
 					hearDone();
 					return true;
 				}
 			}
 		}
 	}
 	
 	
 	
 	public static class PriorityComparator implements Comparator<WorkFutureImpl<?>> {
 		public static final PriorityComparator INSTANCE = new PriorityComparator();
 		
 		/** @return positive if the first arg is higher priority than the second */
 		public int compare(WorkFutureImpl<?> $o1, WorkFutureImpl<?> $o2) {
 			return $o1.$work.getPriority() - $o2.$work.getPriority();
 		}
 	}
 	
 	
 	
 	/**
 	 * This is a duplicate of {@link WorkFuture.DelayComparator}, sadly &mdash; it's
 	 * just implemented for a more specific generic type.
 	 */
 	public static class DelayComparator implements Comparator<WorkFutureImpl<?>> {
 		public static final DelayComparator INSTANCE = new DelayComparator();
 
 		/** @return positive if the first arg should be run sooner than the second */
 		public int compare(WorkFutureImpl<?> $o1, WorkFutureImpl<?> $o2) {
 			final long $diff = $o1.getScheduleParams().getNextRunTime() - $o2.getScheduleParams().getNextRunTime();
 			if ($diff > 0) return -1;
 			if ($diff < 0) return 1;
 			return 0;
 		}
 	}
 }
