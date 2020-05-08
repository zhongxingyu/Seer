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
 import us.exultant.ahs.anno.*;
 import java.util.*;
 import java.util.concurrent.*;
 
 /**
  * <p>
  * A WorkTarget is similar to {@link Runnable} and/or {@link Callable}, but combines the
  * concept of runnability with properties essential for intelligent scheduling of work.
  * Implementations of the WorkTarget interface which define their readiness for scheduling
  * based on availability of messages from a {@link us.exultant.ahs.core.ReadHead} form the
  * essence of the Actor model of concurrent programming.
  * </p>
  * 
  * <p>
  * One WorkTarget instance must be created for every thread that you wish to be able to
  * perform that type of task concurrently, and it never makes sense to submit the same
  * WorkTarget into more than one WorkScheduler.
  * </p>
  * 
  * @author Eric Myhre <tt>hash@exultant.us</tt>
  * @param <$V>
  *                The type returned by {@link #call()}. (This can often be {@link Void} in
  *                the case of WorkTarget that are designed to be called repeatedly, since
  *                they tend to ferry data in and out via {@link ReadHead} and
  *                {@link WriteHead} instead of simply returning one piece of data.)
  */
 public interface WorkTarget<$V> extends Callable<$V> {
 	/**
 	 * <p>
 	 * Defines whether or not this WorkTarget currently has enough data available to
 	 * complete some atom of work immediately if powered.
 	 * </p>
 	 * 
 	 * <p>
 	 * The return value of this method is used by {@link WorkScheduler} to determine
 	 * whether or not it is presently appropriate to consider scheduling this
 	 * WorkTarget to be powered by a thread. {@link WorkScheduler} checks this method
 	 * under two conditions: whenever it finishes a run of the work, and sometime
 	 * after an update is requested via the {@link WorkScheduler#update(WorkFuture)}
 	 * method. (Typically, updates are requested when triggered (i.e. by a callback on
 	 * a {@link Pipe} that feeds work data for this target; this is the preferred
 	 * mechanism); otherwise it may be done in polling fashion (this mechanism can be
 	 * useful if work data is produced at such a high volume that the reduced
 	 * synchronization is a performance improvement, but the event-based mechanism is
 	 * generally preferred and will have better performance when the system is at
 	 * rest. In either case it is necessary to make these arrangements after
 	 * scheduling a WorkTarget, since the WorkFuture returned from the scheduling
 	 * process is necessary.)
 	 * </p>
 	 * 
 	 * <p>
 	 * The answer to this question is based on a best-effort system and may not be
 	 * exactly true under all circumstances due to the concurrent nature of the
 	 * question. For example, this WorkTarget may claim to be ready when asked by the
 	 * WorkScheduler, but then may turn out to not find work when it's actually
 	 * powered because some other WorkTarget has drained from the same Pipe that
 	 * provides the flow of work input data to both WorkTarget.
 	 * </p>
 	 * 
 	 * @return true if this WorkTarget has at least one atom of work available; false
 	 *         otherwise.
 	 */
 	public boolean isReady();
 	
 	/**
 	 * <p>
 	 * Causes the current thread to be consumed in the running of the
 	 * <code>WorkTarget</code>, similarly to the {@link Runnable#run()} method.
 	 * </p>
 	 * 
 	 * <p>
 	 * This method can be called at any time, and any number of times, and need not be
 	 * reentrant &mdash it is the responsibility of the caller to make sure that calls
 	 * to this method are properly synchronized. (If a WorkTarget is scheduled with a
 	 * {@link WorkScheduler}, this is handled automatically.)
 	 * </p>
 	 * 
 	 * <p>
 	 * Each invocation of this method may return a value or throw an exception after
 	 * performing its work, and this result may be different with every invocation.
 	 * Typically this result is most convenient to use for tasks that are only run
 	 * once; for tasks that may be run repeatedly, it is often more convenient to use
 	 * a {@link WriteHead} to gather output into a {@link DataPipe} and leave the
 	 * generic return type {@link Void}.
 	 * </p>
 	 * 
 	 * <p>
 	 * Calling this method after {@link #isDone()} returns true is allowed to have
 	 * undefined results (i.e., may return any value, or null, or throw an exception),
 	 * but it MUST return immediately. (When submitted to a {@link WorkScheduler}, the
 	 * {@link WorkFuture#get()} method of the {@link WorkFuture} returned from
 	 * {@link WorkScheduler#schedule(WorkTarget, ScheduleParams)} can be used to
 	 * consistently access the final result of the work.)
 	 * </p>
 	 * 
 	 * <p>
 	 * It is typically expected that this method will be called by a thread from a
 	 * pool kept within a {@link WorkScheduler}. As such, all actions taken by this
 	 * method should not resort to waiting nor use blocking operations in order for a
 	 * complete system of scheduled WorkTargets to work smoothly and with high
 	 * efficiency. (In a practical sense, this interface cannot impose a strict limit
 	 * on the actual time that will be consumed by this call; if for example the
 	 * <code>WorkTarget</code> is assigned to some sort of blocking I/O channel, the
 	 * call may wait indefinitely in the same fashion as the underlying channel.
 	 * However, this sort of behavior should be avoided at all costs since
 	 * {@link WorkScheduler} assigns threads based on the assumption that this sort of
 	 * idiocy will not be performed.)
 	 * </p>
 	 * 
 	 * <h3>Additional return constraints when using a {@link WorkScheduler}</h3>
 	 * <p>
 	 * WorkScheduler implementations and the WorkFuture they link to a WorkTarget make
 	 * it possible to consistently access the "final" result of a WorkTarget that has
 	 * become done. However, they also add a few special rules:
 	 * <ul>
 	 * <li>Exceptions thrown from this {@link #call()} method cause the WorkFuture to
 	 * become {@link WorkFuture.State#FINISHED} and the WorkScheduler to stop handling
 	 * this WorkTarget. {@link WorkFuture#get()} will always throw that exception
 	 * (wrapped in an {@link ExecutionException}).
 	 * <li>Whenever this {@link #call()} method returns <tt>null</tt>, that return is
 	 * ignored for purposes of choosing the final return result from
 	 * {@link WorkFuture#get()} &mdash; if the WorkTarget becomes finished before any
 	 * other invocations of <tt>call()</tt>, then the {@link WorkFuture#get()} method
 	 * will return the most recent non-null return from the <tt>call()</tt> method.
 	 * (It is still possible for {@link WorkFuture#get()} to return <tt>null</tt> if
 	 * <tt>call()</tt> only ever returns null.)
 	 * <li>{@link WorkFuture#get()} will throw a {@link CancellationException} if
 	 * {@link WorkFuture#cancel(boolean)} was called before this WorkTarget became
 	 * done (regardless of anything this <tt>call</tt> method has already returned).
 	 * </ul>
 	 * </p>
 	 */
 	public $V call() throws Exception;
 	
 	/**
 	 * <p>
 	 * Signals whether or not the WorkTarget may wish to be run again at some time in
 	 * the future, or if it considers itself done and not in need of further
 	 * scheduling.
 	 * </p>
 	 * 
 	 * <p>
 	 * Once this method returns true, it should never again return false. (In other
 	 * words, the property of doneness must be idempotent.)
 	 * </p>
 	 * 
 	 * <p>
 	 * Note that due to the inherently concurrent nature of this method and scheduling
 	 * in general, a WorkTarget is not generally allowed to assume that its
 	 * {@link #call()} will never be invoked again after this {@code isDone()} method
 	 * returns true. A WorkScheduler will make its best effort to honor that (and if
 	 * the change to doneness happened during a run of the task, most implementations
 	 * will certainly succeed), but this should not be relied upon.
 	 * </p>
 	 * 
 	 * @return false if the WorkScheduler should continue to call continute to manage
 	 *         this task instance; true if the scheduler should drop the task and
 	 *         never again invoke the {@link #call()} method.
 	 */
 	public boolean isDone();
 	
 	/**
 	 * <p>
 	 * Estimates the priority with which this WorkTarget should be scheduled relative
 	 * to other WorkTarget at the same WorkScheduler; a higher priority indicates that
 	 * this WorkTarget should be scheduled preferentially.
 	 * </p>
 	 * 
 	 * <p>
 	 * WorkScheduler may use this priority to the best of its ability in scheduling
 	 * work for powering by the scheduler's threads, but the priority at any point in
 	 * time is at most a best-effort basis and not a guarantee of order due to the
 	 * concurrent nature of the scheduling and furthermore because a WorkScheduler
 	 * implementation may balance concerns other than priority (such as tasks
 	 * scheduled based on wall-clock time).
 	 * </p>
 	 * 
 	 * <p>
 	 * This priority may change over time (this could be useful for example in a
 	 * program which has several work buffers and always wishes to service the fullest
 	 * one, or similarly the least recently served one); WorkScheduler who obey the
 	 * priority hint will do their best to respond to this in a timely manner every
 	 * time they are told to update their relationship with this WorkTarget via an
 	 * invocation of the {@link WorkScheduler#update(WorkFuture)} method.
 	 * </p>
 	 * 
 	 * <p>
 	 * Another example of effective use of the priority system is a server application
 	 * which is composed of three types of task: nonblocking reads, some application
 	 * logic, and nonblocking writes of the result. By simply giving writes the
 	 * highest priority, application logic a medium priority, and reads the lowest
 	 * priority, the entire application becomes optimized to keep all of its pipes
 	 * between these task as small as possible, and furthermore if the reads happen to
 	 * be based on some system including a sliding window (namely, TCP), the TCP
 	 * buffer naturally fills in the OS kernel layer and causes the TCP window to
 	 * automatically adapt when the application level logic is over burdened &mdash;
 	 * all with no special effort from the application designer except for setting
 	 * those three priorities.
 	 * </p>
 	 * 
 	 * @return an integer representing the priority with which this WorkTarget should
 	 *         currently be considered if it has work ready.
 	 */
 	public int getPriority();
 	
 	
 	
 	/**
 	 * <p>
 	 * Compares two WorkTarget based on their priority alone. This is useful for
 	 * priority queues.
 	 * </p>
 	 * 
 	 * <p>
 	 * Note: this comparator imposes orderings that are inconsistent with equals.
 	 * </p>
 	 * 
 	 * <p>
 	 * Implementation note: this comparator is implemented as simple integer math
 	 * without overflow checking, so if it is applied to datasets which contain
 	 * priority values that can have a difference larger than
 	 * {@link Integer#MAX_VALUE} results are unpleasant. Basically, keep your priority
 	 * values between a billion and negative one billion and you'll be fine.
 	 * </p>
 	 * 
 	 * @author Eric Myhre <tt>hash@exultant.us</tt>
 	 * 
 	 */
 	public static class PriorityComparator implements Comparator<WorkTarget<?>> {
 		public int compare(WorkTarget<?> $o1, WorkTarget<?> $o2) {
 			// we could of course just compare Math.max(prio,2^31)... but i'm intending to use this in the tight bit of a synchronization block that the whole vm pivots around.  so, we're going with the 99% solution here because it runs faster.
 			return $o1.getPriority() - $o2.getPriority();
 		}
 	}
 	
 	
 	
 	/**
 	 * <p>
 	 * Implements most of the guts for the readiness and doneness functions for common
 	 * tasks. The task may start as unready, and become ready when a triggering
 	 * function is called, at which point it is ready until it is done. The task may
 	 * optionally be defined as run-once. The priority is fixed at construction time.
 	 * Tasks that would otherwise be expressed as {@link Callable} or {@link Runnable}
 	 * are likely to be easily expressed using this adapater; tasks that deal with
 	 * streams of events are probably better expressed using a
 	 * {@link WorkTarget.FlowingAdapter}.
 	 * </p>
 	 * 
 	 * @author Eric Myhre <tt>hash@exultant.us</tt>
 	 * 
 	 */
 	public static abstract class TriggerableAdapter<$V> implements WorkTarget<$V> {
 		public TriggerableAdapter(boolean $startReady, boolean $runOnce, int $priority) {
 			$once = $runOnce;
 			$ready = $startReady;
 			$prio = $priority;
 			$done = false;
 		}
 		
 		private final boolean		$once;
 		private final int		$prio;
 		private volatile boolean	$ready;
 		private volatile boolean	$done;
 		
 		@Idempotent
 		@ThreadSafe
 		@ChainableInvocation
 		public TriggerableAdapter<$V> trigger() {
 			$ready = true;
 			return this;
 		}
 		
 		/**
 		 * Call this to cause the work target to become done &mdash; after calling
 		 * this, the {@link WorkScheduler} will attempt to transition the state of
 		 * the associated {@link WorkFuture} to {@link WorkFuture.State#FINISHED}.
 		 * Resultingly, the {@link #run()} method will never be called again.
 		 */
 		@Idempotent
 		protected final void done() { $done = true; }
 		
 		/**
 		 * This method does the readiness and run-once checks, then passes control
 		 * to the {@link #run()} method which you must define.
 		 */
 		public final $V call() throws Exception {
 			if ($done) throw new IllegalStateException("This task is already done!");
 			if (!$ready) return null;
 			$V $v = run();
 			if ($once) done();
 			return $v;
 		}
 		protected abstract $V run() throws Exception;
 		
 		/** returns true any time {@link #trigger()} has been called and {@link #isDone()} is still false. */
 		public final boolean isReady() { return !isDone() && $ready; }
 		/** returns true when either {@link #done()} has been called or the task was run-once and has been run. */
 		public final boolean isDone() { return $done; }
 		/** @inheritDocs */
 		public final int getPriority() { return $prio; }
 	}
 	
 	
 	
 	/**
 	 * <p>
 	 * Implements most of the guts for the readiness and doneness functions for tasks
 	 * that deal with streams of data via {@link ReadHead} and {@link WriteHead} .
 	 * Tasks that are more of a one-shot thing or feel like a callback are probably
 	 * better expressed using a {@link WorkTarget.TriggerableAdapter}.
 	 * </p>
 	 * 
 	 * @author Eric Myhre <tt>hash@exultant.us</tt>
 	 * 
 	 */
 	public static abstract class FlowingAdapter<$IN, $OUT> implements WorkTarget<Void> {
 		/**
 		 * @param $workSource
 		 *                a ReadHead to get data for working on. The availablity
 		 *                and exhaustion of this object define the readiness and
 		 *                the doneness of this WorkTarget.
 		 * @param $workSink
 		 *                a WriteHead to push the results of work into. May be
 		 *                null, which behaves exactly like
 		 *                {@link us.exultant.ahs.core.WriteHead.NoopAdapter}.
 		 * @param $priority
 		 *                a fixed priority for {@link #getPriority()} to report.
 		 */
 		public FlowingAdapter(ReadHead<$IN> $workSource, WriteHead<$OUT> $workSink, int $priority) {
 			if ($workSource == null) throw new NullPointerException();
 			if ($workSink == null) $workSink = new WriteHead.NoopAdapter<$OUT>();
 			$src = $workSource;
 			$sink = $workSink;
 			$prio = $priority;
 		}
 		
 		/** Direct access to this field is not typically necessary or recommended, but is allowed in case for example a subclass should wish to close the stream. */ 
 		protected final ReadHead<$IN>	$src;
 		/** Direct access to this field is not typically necessary or recommended, but is allowed in case for example a subclass should wish to close the stream. */
 		protected final WriteHead<$OUT>	$sink;
 		private final int		$prio;
 		
 		/**
 		 * This method attempts to read some data for working on, then if it is
 		 * available, passes control to the {@link #run(Object)} method which you
 		 * must define.
 		 */
 		public final Void call() throws Exception {
			if (isDone()) return null;
 			$IN $a = $src.readNow();
 			if ($a == null) return null;
 			$OUT $b = run($a);
 			if ($b == null) return null;
 			$sink.write($b);
 			return null;
 		}
 		protected abstract $OUT run($IN $chunk) throws Exception;
 		
 		/** @inheritDocs */
		public final boolean isReady() { return !$src.hasNext(); }
 		/** @inheritDocs */
 		public final boolean isDone() { return $src.isExhausted(); }
 		/** @inheritDocs */
 		public final int getPriority() { return $prio; }
 	}
 	
 	
 	
 	/**
 	 * <p>
 	 * Bridges the gap between {@link Runnable} and WorkTarget. The work is ready any
 	 * time that it's not done.
 	 * </p>
 	 * 
 	 * <p>
 	 * If constructed in run-once mode, the WorkTarget will run exactly once when
 	 * scheduled with a {@link WorkScheduler}; otherwise if run-once is false the work
 	 * will always be ready and will never become done (to stop it, the
 	 * {@link WorkFuture} must be cancelled or it must throw an exception).
 	 * </p>
 	 * 
 	 * @author Eric Myhre <tt>hash@exultant.us</tt>
 	 */
 	public static class RunnableWrapper extends TriggerableAdapter<Void> {
 		public RunnableWrapper(Runnable $wrap) { this($wrap,true,true,0); }
 		public RunnableWrapper(Runnable $wrap, boolean $startReady, boolean $runOnce) { this($wrap,$startReady,$runOnce,0); }
 		public RunnableWrapper(Runnable $wrap, int $priority) { this($wrap,true,true,$priority); }
 		public RunnableWrapper(Runnable $wrap, boolean $startReady, boolean $runOnce, int $priority) {
 			super($startReady, $runOnce, $priority);
 			if ($wrap == null) throw new NullPointerException();
 			this.$wrap = $wrap;
 		}
 		
 		private final Runnable	$wrap;
 		
 		protected Void run() {
 			$wrap.run();
 			return null;
 		}
 	}
 	
 	
 	
 	/**
 	 * <p>
 	 * Bridges the gap between {@link Callable} and WorkTarget. The work is ready any
 	 * time that it's not done.
 	 * </p>
 	 * 
 	 * <p>
 	 * If constructed in run-once mode, the WorkTarget will run exactly once when
 	 * scheduled with a {@link WorkScheduler}; otherwise if run-once is false the work
 	 * will always be ready and will never become done (to stop it, the
 	 * {@link WorkFuture} must be cancelled or it must throw an exception).
 	 * </p>
 	 * 
 	 * @author Eric Myhre <tt>hash@exultant.us</tt>
 	 */
 	public static class CallableWrapper<$V> extends TriggerableAdapter<$V> {
 		public CallableWrapper(Callable<$V> $wrap) { this($wrap,true,true,0); }
 		public CallableWrapper(Callable<$V> $wrap, boolean $startReady, boolean $runOnce) { this($wrap,$startReady,$runOnce,0); }
 		public CallableWrapper(Callable<$V> $wrap, int $priority) { this($wrap,true,true,$priority); }
 		public CallableWrapper(Callable<$V> $wrap, boolean $startReady, boolean $runOnce, int $priority) {
 			super($startReady, $runOnce, $priority);
 			if ($wrap == null) throw new NullPointerException();
 			this.$wrap = $wrap;
 		}
 		
 		private final Callable<$V>	$wrap;
 		
 		protected $V run() throws Exception {
 			$wrap.call();
 			return null;
 		}
 	}
 }
