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
 import us.exultant.ahs.thread.WorkFuture.*;
 import java.util.*;
 import java.util.concurrent.*;
 
 /**
  * <p>
  * A WorkScheduler is a thread pooling and work organizing mechanism.
  * </p>
  * 
  * <p>
  * Every running JVM should aim to have exactly one WorkScheduler. A default singleton is
  * available from {@link WorkManager#getDefaultScheduler()}; use this whenever possible.
  * </p>
  * 
  * <div style="border:1px solid; margin:1em; padding:1em;">
  * <h3>Relationship between WorkScheduler, WorkTarget, and the Future</h3>
  * 
  * <p>
  * {@link WorkTarget} represents runnable logic that performs some work when given a
  * thread. A {@link WorkScheduler} provides threads to all of the WorkTargets that it
 * manages in the best order it knows how &mdash; corralling tasks with clock-based
  * schedules and tasks of various priorities, all of which may or may not be ready to
  * perform some work at any given time. A WorkScheduler returns a {@link WorkFuture} when
  * given a WorkTarget to manage; this behaves pretty much exactly like you'd expect any
  * {@link Future} implementation to behave. WorkTargets and WorkFutures thusly should
  * stand in a one-to-one and onto relationship.
  * </p>
  * 
  * <p>
  * It is only appropriate to schedule a single WorkTarget once and with a single
  * WorkScheduler at any one time. This condition is not checked in the code; it is up to
  * the developer to maintain sanity.
  * </p>
  * 
  * <p>
  * If a WorkTarget represents a task which can be performed by several threads at once (a
 * common example being some sort of server can respond to one client independently per
  * thread), then several instances of the same WorkTarget implementation should be created
 * and each of them scheduled &mdash; one per thread which should be able to respond. (If
  * you want as many threads as possible to be able to perform a type of work, consider
  * creating a {@link Factory} for the WorkTarget and using the
  * {@link WorkManager#scheduleOnePerCore(Factory,WorkScheduler)} helper method.)
  * </p>
  * </div>
  */
 public interface WorkScheduler {
 	/**
 	 * <p>
 	 * Starts the worker threads that this WorkScheduler will manage and use to
 	 * perform the work given to it, and returns immediately.
 	 * </p>
 	 * 
 	 * <p>
 	 * Calling this method repeatedly has no effect; the first invocation will start,
 	 * and all others will take no action.
 	 * </p>
 	 * 
 	 * @return the self-same object.
 	 * @throws IllegalStateException
 	 *                 if this method is called after after calling
 	 *                 {@link #stop(boolean)}
 	 */
 	@ChainableInvocation
 	@ThreadSafe
 	@Idempotent
 	public WorkScheduler start();
 	
 	/**
 	 * <p>
 	 * Arranges for a {@link WorkTarget} to be executed at the convenience of this
 	 * scheduler and in accordance with the specifications made by the
 	 * {@link ScheduleParams}. The {@link Future#get()} method of the returned
 	 * {@link Future} will return once the WorkTarget is considered complete by the
 	 * scheduler and will no longer be run, yielding the last result returned by the
 	 * {@link WorkTarget#call()} method.
 	 * </p>
 	 * 
 	 * <p>
 	 * The javadocs of the {@link ScheduleParams} class explains when a WorkScheduler
 	 * implementation should check for a WorkTarget to be shifted from waiting into
 	 * the scheduled-and-ready-to-be-run queue. As a general rule though, any task
 	 * that isn't triggered based on a wall-clock time will only be checked when the
 	 * {@link #update(WorkFuture)} method is called.
 	 * </p>
 	 * 
 	 * @param <$V>
 	 *                The type returned by a WorkTarget.
 	 * @param $work
 	 *                The WorkTarget to schedule.
 	 * @param $when
 	 *                The parameters defining whether or not the work should be
 	 *                scheduled in a clock-based fashion, and if so, how.
 	 * @return A {@link Future} representing the state of the progress of there
 	 *         WorkTarget.
 	 */
 	@ThreadSafe
 	@Idempotent
 	public <$V> WorkFuture<$V> schedule(WorkTarget<$V> $work, ScheduleParams $when);
 	
 	/**
 	 * Call this method to have the WorkScheduler check the WorkTarget associated with
 	 * the given Future for readiness to be scheduled. If the WorkTarget is done and
 	 * not currently being run, it may be immediately transitioned to a finished state
 	 * (completion notification hooks will be called from this thread before this
 	 * method returns).
 	 * 
 	 * @param <$V>
 	 * @param $fut
 	 *                The WorkFuture returned from an earlier invocation of the
 	 *                {@link #schedule(WorkTarget, ScheduleParams)} method on this
 	 *                same WorkScheduler.
 	 */
 	@ThreadSafe
 	@Idempotent
 	public <$V> void update(WorkFuture<$V> $fut);
 	
 	/**
 	 * Same as iteratively calling {@link #update(WorkFuture)}, but more efficient in
 	 * many implementations.
 	 * 
 	 * @param <$V>
 	 * @param $futs
 	 *                A collection of WorkFuture instances returned from an earlier
 	 *                invocation of the {@link #schedule(WorkTarget, ScheduleParams)}
 	 *                method on this same WorkScheduler.
 	 */
 	@ThreadSafe
 	@Idempotent
 	public <$V> void update(Collection<WorkFuture<$V>> $futs);
 	
 	/**
 	 * Produces a description of the current state of this scheduler. There is no
 	 * particular standard to this description, since WorkScheduler implementations
 	 * can vary widely in how they are implemented, but typically it might contain
 	 * some helpful information like how many tasks are currently running, how many
 	 * threads are available, and possibly some basic information about how tasks are
 	 * pooled or sorted (i.e. how big is the waiting set versus the ready heap, or how
 	 * big is the low priority pool compared to the high priority, etc).
 	 * 
 	 * @return a description, probably a String or object with a very reasonable
 	 *         toString method.
 	 */
 	public Object describe();
 	
 	/**
 	 * <p>
 	 * Trigger updating of all unready tasks managed by this scheduler. Much like the
 	 * {@link #update(WorkFuture)} method, {@link #flush()} may request updating which
 	 * the scheduler services at its leisure.
 	 * </p>
 	 * 
 	 * <p>
 	 * You may schedule a task to have a scheduler periodically flush itself.
 	 * {@link WorkManager#periodicallyFlush(WorkScheduler, long, TimeUnit)} is a
 	 * one-line way to set this up.
 	 * </p>
 	 * 
 	 * <p>
 	 * Whether or not this method should actually be used is an interesting
 	 * discussion.
 	 * </p>
 	 * 
 	 * <p>
 	 * It is entirely possible to construct a program where all task completions and
 	 * all work availability changes are updated in an event-based fashion, without
 	 * resorting to polling {@link #flush()}. Polling {@link #flush()} periodically
 	 * will provide less rapid reactions because there may be a delay proportional to
 	 * the poll frequency for any new work; while high frequency polling may make this
 	 * appear negligible to human standards, if events occur in a series the delay may
 	 * apply at every stop in that series and accumulate to something serious.
 	 * </p>
 	 * 
 	 * <p>
 	 * That being said, polling {@link #flush()} periodically is incredibly simple and
 	 * very hard to screw up in a way that hangs you. Furthermore, in some systems
 	 * that have very high throughputs of events and WorkTarget that are often ready,
 	 * it's actually possible that designing to use periodic flushing instead of
 	 * spending time doing dispatches of update requests for each event in a deluge
 	 * can actually cause a net performance throughput win.
 	 * </p>
 	 * 
 	 * <p>
 	 * Note that in many implementations the scheduler may be forced to acquire a
 	 * global lock on the entire scheduler in order to perform this function.
 	 * </p>
 	 * 
 	 */
 	public void flush();
 	
 	/**
 	 * <p>
 	 * A {@link ReadHead} that returns {@link WorkFuture} instances that were managed
 	 * by this scheduler and have now been completed (i.e. either
 	 * {@link State#FINISHED} or {@link State#CANCELLED}). Like the behavior of a
 	 * {@link FuturePipe}, WorkFutures are returned in the order they were completed.
 	 * </p>
 	 * 
 	 * <p>
 	 * The ReadHead's {@link ReadHead#isClosed()} method will return true only when
 	 * the scheduler is accepting no more tasks and all tasks already entered have
 	 * become complete, so waiting for this ReadHead to close is effectively waiting
 	 * for the scheduler to be stopped and have shut down gracefully.
 	 * </p>
 	 * 
 	 * @return a {@link ReadHead} that returns WorkFuture instances that were managed
 	 *         by this scheduler and have now been completed.
 	 */
 	public ReadHead<WorkFuture<Object>> completed();
 	
 	/**
 	 * <p>
 	 * Requests that all worker threads of this scheduler cease to perform work and
 	 * stop, and returns immediately.
 	 * </p>
 	 * 
 	 * @param $aggressively
 	 *                false if the threads should continue to perform work as long as
 	 *                some is immediately available; true if threads should not pick
 	 *                up any new work after they finish their current task.
 	 *                (Clock-based tasks that are not ready are not considered
 	 *                available for the purpose of keeping threads alive.)
 	 */
 	@ThreadSafe
 	@Idempotent
 	public void stop(boolean $aggressively);
 	
 	//TODO:AHS:THREAD: we'll want to be able to wait for completion, no?  make stop return a future.
 	//   that would get quite pear shaped though.  i mean, a normal Future?  fine.  take the thread that calls get() first.  a WorkFuture?  there's no one to send the event in anything like realtime.
 	//      well.  i guess it's really no different than a WorkFuture you got from a WorkScheduler that hasn't been started yet.
 	//          lolurdumb.  we just have to make a different kind of WorkFuture that we very carefully finish manually with the last thread of the stopping scheduler.  also i think we'd tell you to go fuck yourself if you tried to cancel.
 	//TODO:AHS:THREAD: it might be appropriate to cancel all tasks that were incomplete by the time we finish stopping.  (which is a bit of a pain; we should maybe even use all the threads for that, but that gives every single listener involved a chance to pretty much halt the whole damn stopping!)
 	//TODO:AHS:THREAD: um, also... waiting for a stop without calling for one, we want that, yes?  Oh lord, the whole scheduler should be a future, shouldn't it  O.o
 	//TODO:AHS:THREAD: there are actually several different intensity of stopping demands i can see potentially wanting:
 	//       - eventual finish - when all tasks ever entered are done.  we're not saying you have to stop accepting until then, though (so currently running tasks can generate more tasks, and we can essentially wait for them without actually having to know about them).
 	//       - close and finish all - don't accept new tasks, but finish everything, even the ones that require clocked waiting.
 	//       - close and finish current - don't accept new tasks, but finish everything unless it requires clocked waiting (cancel those).
 	//		one has to wonder a bit here as well... some clock tasks never expire.  loggers are typical of this.  should we have a wait-for-serious-tasks-but-ignore-the-stupid-ones mode somehow?  how on earth would you implement that or declare the stupidity of a task?
 	//       - close and finish aggressively - threads should not pick up any new work when they finish their current.  everything in queues should be cancelled.
 	//       - close and finish ragingly - interrupt all current tasks as well.
 }
