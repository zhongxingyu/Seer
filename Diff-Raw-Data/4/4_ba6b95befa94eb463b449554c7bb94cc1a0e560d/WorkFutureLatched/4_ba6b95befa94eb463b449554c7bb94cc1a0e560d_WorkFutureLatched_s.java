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
 import us.exultant.ahs.anno.*;
 import java.util.*;
 import java.util.concurrent.*;
 
 /**
  * <p>
  * A latched work future is a very simple implementation of {@link WorkFuture} that just
  * exposes a {@link #set(Object)} method and becomes finished as soon as that's called;
  * it's comparable to {@link FutureTask}. It's mostly meant for use in constructing more
  * complex systems. Works great anywhere you might have wanted to use a
  * {@link CountDownLatch} but you also needed to support nonblocking callbacks as well as
  * blocking waiting; if you need to count down several times instead of just once, slip a
  * bunch of these {@link WorkFutureLatched} instances into a {@link WorkFutureAggregate}
  * and you win.
  * </p>
  * 
  * @author Eric Myhre <tt>hash@exultant.us</tt>
  * 
  */
 public class WorkFutureLatched<$V> extends WorkFutureAdapter<$V> {
 	public WorkFutureLatched() {
 		this.$state = WorkFuture.State.WAITING;
 		this.$completionListeners = new ArrayList<Listener<WorkFuture<?>>>(1);
 		this.$latch = new CountDownLatch(1);
 	}
 	
 	private final CountDownLatch $latch;
 	/**
 	 * The result to return from get(). Need not be volatile or synchronized because
 	 * it is guarded by {@link #$latch}
 	 */
 	private $V					$result		= null;
 	/**
 	 * The (already wrapped) exception to throw from get(). Need not be volatile or
 	 * synchronized because it is guarded by {@link #$latch}.
 	 */
 	private ExecutionException			$exception	= null;
 	/**
 	 * Generally speaking, if you're going to change this, you should synchronize on
 	 * {@link #$latch} first. However, if you're only reading, and you're checking for
 	 * {@link WorkFuture.State#CANCELLED} or {@link WorkFuture.State#FINISHED}, you
 	 * can do that without any synchronization since this field is volatile and those
 	 * transitions are permanent.
 	 */
 	private volatile WorkFuture.State		$state;
 	/**
 	 * A list of {@link Listener} to be called as soon as possible after this task
 	 * becomes done. This is synchronized on before adding new elements, and before
 	 * transitioning to done (we're not worried about efficiency because this
 	 * operation should be quite rare (i.e. never ever ever ever in a loop) and
 	 * contention not really an issue).
 	 */
 	private final List<Listener<WorkFuture<?>>>	$completionListeners;
 	
 	public WorkFuture.State getState() {
 		return $state;
 	}
 	
 	public ScheduleParams getScheduleParams() {
 		return ScheduleParams.NOW;
 	}
 	
 	/**
 	 * Sets the result of this WorkFuture to the given value unless this future has
 	 * already been set or has been cancelled.
 	 * 
 	 * @return true if this set call caused the WorkFuture to become finished with
 	 *         this value; false if there was a concurrent set or cancel.
 	 */
 	@ThreadSafe
 	@Idempotent
 	public boolean set($V $result) {
 		if (!shiftToFinished()) return false;
 		this.$result = $result;
 		$latch.countDown();
 		hearDone();
 		return true;
 	}
 	
 	/**
 	 * Causes this future to report an ExecutionException with the given throwable as
 	 * its cause, unless this future has already been set or has been cancelled.
 	 * 
 	 * @return true if this set call caused the WorkFuture to become finished with
 	 *         this value; false if there was a concurrent set or cancel.
 	 */
 	@ThreadSafe
 	@Idempotent
 	public boolean setException(Throwable $result) {
 		if (!shiftToFinished()) return false;
 		this.$exception = new ExecutionException($result);
 		$latch.countDown();
 		hearDone();
 		return true;
 	}
 	
 	@ThreadSafe
 	@Idempotent
 	private boolean shiftToFinished() {
 		synchronized ($latch) {
 			switch ($state) {
 				case WAITING: $state = State.FINISHED; return true;
 				case CANCELLING: case FINISHED: case CANCELLED: return false;
 				case RUNNING: case SCHEDULED: default: throw new MajorBug();
 			}
 		}
 	}
 	
 	@ThreadSafe
 	@Idempotent
 	public $V get() throws InterruptedException, CancellationException, ExecutionException {
 		$latch.await();
 		if (isCancelled()) throw new CancellationException();
 		if ($exception != null) throw $exception;
 		return $result;
 	}
 	
 	@ThreadSafe
 	@Idempotent
 	public $V get(long $timeout, TimeUnit $unit) throws InterruptedException, TimeoutException, CancellationException, ExecutionException {
 		$latch.await($timeout, $unit);
 		if (isCancelled()) throw new CancellationException();
 		if ($exception != null) throw $exception;
 		return $result;
 	}
 	
 	/**
 	 * <p>
 	 * Immediately attempts to transition this WorkFuture to the
 	 * {@link WorkFuture.State#CANCELLED} state.
 	 * </p>
 	 * 
 	 * @return true if transitioning to {@link WorkFuture.State#CANCELLED} successful
 	 *         and is performed by this thread; otherwise, if this WorkFuture was
 	 *         already {@link WorkFuture.State#FINISHED} or
 	 *         {@link WorkFuture.State#CANCELLED}, returns false.
 	 */
 	@ThreadSafe
 	@Idempotent
 	public boolean cancel(boolean $notApplicable) {
 		synchronized ($latch) {
 			switch ($state) {
 				case WAITING:
 					$state = State.CANCELLED;
 					break;
 				case FINISHED:
 				case CANCELLED:
 					return false;
 				default:
 					throw new MajorBug();
 			}
 		}
 		$latch.countDown();
 		hearDone();
 		return true;
 	}
 	
 	/**
 	 * <p>
 	 * Updating is not a meaningful operation for a {@link WorkFutureLatched}; we're
 	 * done exactly when the latch is released, and there's no rushing or fussing
 	 * possible.
 	 * </p>
 	 */
 	public void update() {
 		/* no op. */
 	}
 	
 	public void addCompletionListener(Listener<WorkFuture<?>> $completionListener) {
 		synchronized ($completionListeners) {
 			if (isDone()) $completionListener.hear(this);
 			else $completionListeners.add($completionListener);
 		}
 	}
 	
 	/** Called exactly once.  Called AFTER the transition to completion has already been completed. */
 	private void hearDone() {
 		synchronized ($completionListeners) {
 			for (Listener<WorkFuture<?>> $x : $completionListeners)
 				$x.hear(WorkFutureLatched.this);
 			$completionListeners.clear();	// let that crap be gc'd even if this future is forced to hang around for a while
 		}
 	}
 }
