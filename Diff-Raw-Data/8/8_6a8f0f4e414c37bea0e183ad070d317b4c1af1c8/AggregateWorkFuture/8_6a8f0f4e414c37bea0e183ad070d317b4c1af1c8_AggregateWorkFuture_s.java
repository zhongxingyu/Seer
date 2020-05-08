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
 
 /**
  * <p>
  * A {@link WorkFuture} that represents a set of WorkFutures. This makes it possible to
  * wait for all of the collected WorkFutures to complete, or use the completion listener
  * to fire once when all the tasks of a set are done.
  * </p>
  * 
  * @author Eric Myhre <tt>hash@exultant.us</tt>
  * 
  */
 public class AggregateWorkFuture<$T> implements WorkFuture<Void> {
 	public AggregateWorkFuture(Collection<WorkFuture<$T>> $futures) {
 		this.$pip = new FuturePipe<$T>();
 		this.$state = WorkFuture.State.WAITING;	// i have to admit neither RUNNING nor WAITING (and certainly not SCHEDULED) makes any sense here.  I should just pick one of them as the correct resopnse for any future that isn't directly powered or scheduled.
 		this.$completionListeners = new ArrayList<Listener<WorkFuture<?>>>(1);
 		this.$pip.sink().writeAll($futures);
 		this.$pip.sink().close();
 		this.$pip.source().setListener(new Listener<ReadHead<WorkFuture<$T>>>() {
 			public void hear(ReadHead<WorkFuture<$T>> $rh) {
 				synchronized ($pip) {
 					$pip.source().readAllNow();
 					if (!$pip.source().isExhausted()) return;
 					if ($state != State.WAITING) return;
 					$state = State.FINISHED;
 					X.notifyAll($pip);
 				}
 				hearDone();
 			}
 		});
 	}
 	
 	private FuturePipe<$T> $pip;
 	/**
 	 * Generally speaking, if you're going to change this, you should synchronize on
 	 * {@link #$pip} first. However, if you're only reading, and you're checking for
 	 * {@link WorkFuture.State#CANCELLED} or {@link WorkFuture.State#FINISHED}, you
 	 * can do that without any synchronization since this field is volatile and those
 	 * transitions are permanent.
 	 */
 	private volatile WorkFuture.State $state;
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
 	
 	/**
 	 * Returns when all of the aggregated WorkFutures have either finished or been
 	 * cancelled (or if this wait itself is interrupted).
 	 * 
 	 * @throws InterruptedException
 	 *                 if this wait is interrupted (note, NOT if any of the aggregated
 	 *                 futures were interrupted)
 	 * @throws CancellationException
 	 *                 if this AggregateWorkFuture object was cancelled (note, NOT
 	 *                 merely if any of the aggregated futures were cancelled; also,
 	 *                 while cancellation of this AggregateWorkFuture means that the
 	 *                 cancellation event was passed on to the aggregated futures, it
 	 *                 may not have taken effect on all of them because they may have
 	 *                 already finished.)
 	 */
 	public Void get() throws InterruptedException, CancellationException {
 		synchronized ($pip) {
 			while (!isDone())
 				$pip.wait();
 		}
 		if (isCancelled()) throw new CancellationException();
 		return null;
 	}
 
 	/**
 	 * Returns when all of the aggregated WorkFutures have either finished or been
 	 * cancelled; or if this wait itself is interrupted or times out.
 	 * 
 	 * @throws InterruptedException
 	 *                 if this wait is interrupted (note, NOT if any of the aggregated
 	 *                 futures were interrupted)
 	 * @throws CancellationException
 	 *                 if this AggregateWorkFuture object was cancelled (note, NOT
 	 *                 merely if any of the aggregated futures were cancelled; also,
 	 *                 while cancellation of this AggregateWorkFuture means that the
 	 *                 cancellation event was passed on to the aggregated futures, it
 	 *                 may not have taken effect on all of them because they may have
 	 *                 already finished.)
 	 * @throws TimeoutException
 	 *                 if the wait timed out
 	 */
 	public Void get(long $timeout, TimeUnit $unit) throws InterruptedException, TimeoutException, CancellationException {
 		$timeout = $unit.toMillis($timeout);
 		final long $target = X.time() + $timeout;
 		synchronized ($pip) {
 			while (!isDone() && $timeout > 0) {
 				$pip.wait($timeout);
 				$timeout = $target-X.time();
 			}
 		}
 		if (isCancelled()) throw new CancellationException();
 		if (isDone()) return null;
 		throw new TimeoutException();
 	}
 	
 	/**
 	 * Attempts to cancel execution of all of the individual tasks aggregated by this
 	 * object that are not yet completed. This method then waits for the completion of
 	 * all the aggregated tasks, and when this is done, finally attempts to transition
 	 * this WorkTarget to cancelled. Only then does it return. This means that by the
 	 * time this method returns, all aggregated tasks are no longer runnable; however,
 	 * it's quite possible for concurrent finishing of the aggregated tasks to mean
 	 * that this AggregateWorkFuture becomes {@link WorkFuture.State#FINISHED} instead
 	 * of {@link WorkFuture.State#CANCELLED} even if this method call did cause the
 	 * cancellation of the majority of child tasks.
 	 */
 	public boolean cancel(boolean $mayInterruptIfRunning) {
 		Set<WorkFuture<$T>> $helds;
 		synchronized ($pip.$held) {
 			$helds = new HashSet<WorkFuture<$T>>($pip.$held);
 		}
 		for (WorkFuture<$T> $held : $helds)
 			$held.cancel($mayInterruptIfRunning);
 		for (WorkFuture<$T> $held : $helds)
 			try {
 				$held.get();
 			} catch (ExecutionException $e) { /* I don't care how you ended. */
 			} catch (InterruptedException $e) { /* Seriously I don't. */ }
 		synchronized ($pip) {	// this is really less than ideal.  like, it's as likely as not to end as FINISHED instead of CANCELLED.  not the intended effect.  maybe we should do the transition instantly but then do the waiting?  no, that doesn't seem right either.  hm.
			if ($state != State.WAITING) return false;
			$state = State.CANCELLED;
 		}
 		hearDone();
 		return true;
 	}
 	
 
 	/**
 	 * <p>
 	 * Invokes update for all of the individual tasks aggregated by this object that
 	 * are not yet completed.
 	 * </p>
 	 * 
 	 * <p>
 	 * Note that if you have kept a the full set of WorkFuture that were aggregated
 	 * around elsewhere and you know that they all came from the same
 	 * {@link WorkScheduler}, it may be slightly more efficient to use that
 	 * WorkScheduler's {@link WorkScheduler#update(Collection) mass update} method.
 	 * </p>
 	 */
 	public void update() {
 		Set<WorkFuture<$T>> $helds;
 		synchronized ($pip.$held) {
 			$helds = new HashSet<WorkFuture<$T>>($pip.$held);
 		}
 		//XXX:AHS:THREAD: i'm really not sure how i feel about this copy.  avoiding holding that sync?  great.  but wasting all that memory in a possibly tight loop is bad.  and really... a monitor in a tight loop?  that's shiver enough.  that monitor was originally intended to be grabbed only when something was finishing and thus not be a bottleneck.  perhaps we should just make a single copy of the whole collection and keep that readonly?
 		for (WorkFuture<$T> $held : $helds)
 			$held.update();
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
 				$x.hear(AggregateWorkFuture.this);
 			$completionListeners.clear();	// let that crap be gc'd even if this future is forced to hang around for a while
 		}
 	}
 }
