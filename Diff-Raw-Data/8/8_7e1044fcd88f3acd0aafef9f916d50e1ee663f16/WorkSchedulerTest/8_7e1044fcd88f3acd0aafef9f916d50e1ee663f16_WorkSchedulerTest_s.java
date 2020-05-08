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
 import us.exultant.ahs.log.*;
 import us.exultant.ahs.test.*;
 import java.util.*;
 import java.util.concurrent.*;
 import java.util.concurrent.atomic.*;
 
 /**
  * <p>
  * Tests any kind of {@link WorkSchedulerTest} implementation for adherence to the basic
  * contracts. Subclasses need simply override the {@link #makeScheduler(int)} method in
  * order to make a valid test case for any specific implementation of
  * {@link WorkScheduler}.
  * </p>
  * 
  * <p>
  * It's essential to run this test repeatedly for confidence in its success being no mere
  * series of false positives; threading is a fickle thing. And we're not talking about a
  * two or three runs, or a dozen: you need to run this THOUSANDS of times if you want to
  * be confident. A useful statement is this: <tt>
  * i=0; while true; do i=$(math $i + 1); echo $i; date; java us.exultant.ahs.thread.WorkSchedulerFlexiblePriorityTest 2> lol; if [ "$?" -ne "0" ]; then break; fi; echo; done
  * </tt>
  * </p>
  * 
  * <p>
  * {@code DEPENDS: }
  * <ul>
  * <li>{@link DataPipeTest}
  * </ul>
  * </p>
  * 
  * @author Eric Myhre <tt>hash@exultant.us</tt>
  * 
  */
 public abstract class WorkSchedulerTest extends TestCase {
 	public WorkSchedulerTest() {
 		super(new Logger(Logger.LEVEL_TRACE), true);
 	}
 	
 	public WorkSchedulerTest(Logger $log, boolean $enableConfirmation) {
 		super($log, $enableConfirmation);
 	}
 	
 	public List<Unit> getUnits() {
 		List<Unit> $tests = new ArrayList<Unit>();
 		$tests.add(new TestRunOnce());
 		$tests.add(new TestCompletionPreSubscribe());
 		$tests.add(new TestCompletionPostSubscribe());
 		$tests.add(new TestWtAlwaysReady());
 		$tests.add(new TestNonblockingLeaderFollower());
 		$tests.add(new TestScheduleSingleDelayMany());
 		$tests.add(new TestFinishWhileRunning());
 		$tests.add(new TestCancelWhileRunning());
 		$tests.add(new TestScheduleFixedRate());
 		$tests.add(new TestNonblockingManyWorkSingleSource());
 		$tests.add(new TestNonblockingManyWorkSingleConcurrentSource());
 		$tests.add(new TestPrioritizedDuo());
 		return $tests;
 	}
 	
 	private static final TestData	TD	= TestData.getFreshTestData();
 	
 	/** If this is a positive integer, we want that many threads.  A zero means that you can use your default (presumably threads=cores). */
 	protected abstract WorkScheduler makeScheduler(int $threads);
 	
 	/** Number of milliseconds which we'll consider as "<b>a</b>cceptably <b>o</b>ver<b>d</b>ue". */
 	// I'd really like to be able to set this lower, and in many practical use cases, you can.  However, I've observed that my computer will get very, very lazy about timestamps when it's under heavy load, and will in fact start returning them at only 10ms granularity!  So, I'm stuck with an AOD of anything less than 11 being quite unreasonable.
 	private static final int	AOD	= 11;
 	
 	
 	
 	/** One runnable wrapped to be a one-shot WorkTarget. */
 	private class TestRunOnce extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler(0).start();
 		
 		public Object call() throws InterruptedException, ExecutionException {
 			Work $w = new Work();
 			Future<?> $f = $ws.schedule(new WorkTarget.RunnableWrapper($w), ScheduleParams.NOW);
 			
 			$f.get();
 			
 			assertEquals(999, $w.x);
 			
 			breakCaseIfFailed();
 			$ws.stop(false);
 			return null;
 		}
 		private class Work implements Runnable {
 			public volatile int x = 1000;
 			public void run() {
 				x--;
 			}
 		}
 	}
 	
 	
 	/** Tests for a single firing of a completion listener that's assigned to a one-shot WorkTarget before the scheduler is launched. */
 	private class TestCompletionPreSubscribe extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler(0);
 		
 		public Object call() throws InterruptedException, ExecutionException {
 			final AtomicInteger $completionCalls = new AtomicInteger(0);
 			final Work $wt = new Work();
 			final WorkFuture<Void> $wf = $ws.schedule(new WorkTarget.RunnableWrapper($wt), ScheduleParams.NOW);
 			
 			$wf.addCompletionListener(new Listener<WorkFuture<?>>() {
 				public void hear(WorkFuture<?> $lol) {
 					// demand an immediate response
 					$completionCalls.incrementAndGet();
 					try {
 						$wf.get(0, TimeUnit.NANOSECONDS);
 					}
 					catch (InterruptedException $e) { throw new AssertionFailed($e); }
 					catch (ExecutionException $e) { throw new AssertionFailed($e); }
 					catch (TimeoutException $e) { throw new AssertionFailed($e); }
 				}
 			});
 			$ws.start();
 			
 			$wf.get();
 			
 			X.chill(5);	// some delay can be required here, because the get() method must be able to return without blocking before listeners are called.
 			assertEquals(1, $completionCalls.intValue());
 			
 			breakCaseIfFailed();
 			$ws.stop(false);
 			return null;
 		}
 		private class Work implements Runnable { public void run() {} }
 	}
 	
 	
 	/** Tests for a single firing of a completion listener that's assigned to a one-shot WorkTarget after it's been completed. */
 	private class TestCompletionPostSubscribe extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler(0).start();
 		
 		public Object call() throws InterruptedException, ExecutionException {
 			final AtomicInteger $completionCalls = new AtomicInteger(0);
 			final Work $wt = new Work();
 			final WorkFuture<Void> $wf = $ws.schedule(new WorkTarget.RunnableWrapper($wt), ScheduleParams.NOW);
 			
 			$wf.get();
 			
 			$wf.addCompletionListener(new Listener<WorkFuture<?>>() {
 				public void hear(WorkFuture<?> $lol) {
 					// demand an immediate response
 					$completionCalls.incrementAndGet();
 					try {
 						$wf.get(0, TimeUnit.NANOSECONDS);
 					}
 					catch (InterruptedException $e) { throw new AssertionFailed($e); }
 					catch (ExecutionException $e) { throw new AssertionFailed($e); }
 					catch (TimeoutException $e) { throw new AssertionFailed($e); }
 				}
 			});
 			
 			assertEquals(1, $completionCalls.intValue());
 			
 			breakCaseIfFailed();
 			$ws.stop(false);
 			return null;
 		}
 		private class Work implements Runnable { public void run() {} }
 	}
 	
 	
 	
 	/** Eight work targets, all always ready until they're done.
 	 *  Also performs completion signaling test. */
 	private class TestWtAlwaysReady extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler(0).start();
 		
 		public Object call() throws InterruptedException, ExecutionException {
 			final AtomicInteger $completionCalls = new AtomicInteger(0);
 			Work[] $wt = new Work[8];
 			@SuppressWarnings("unchecked")	//srsly.
 			WorkFuture<Void>[] $f = new WorkFuture[8];
 			for (int $i = 0; $i < 8; $i++) $wt[$i] = new Work();
 			for (int $i = 0; $i < 8; $i++) $f[$i] = $ws.schedule($wt[$i], ScheduleParams.NOW);
 			for (int $i = 0; $i < 8; $i++) $f[$i].addCompletionListener(new Listener<WorkFuture<?>>() {
 				public void hear(WorkFuture<?> $lol) {
 					$completionCalls.incrementAndGet();
 				}
 			});
 			
 			for (int $i = 0; $i < 8; $i++) $f[$i].get();
 			
 			for (int $i = 0; $i < 8; $i++) assertEquals(0, $wt[$i].x);
 			
 			X.chill(5);	// some delay can be required here, because the get() method must be able to return without blocking before listeners are called.
 			assertEquals(8, $completionCalls.intValue());
 			
 			breakCaseIfFailed();
 			$ws.stop(false);
 			return null;
 		}
 		private class Work implements WorkTarget<Void> {
 			public volatile int x = 1000;
 			public Void call() {
 				x--;
 				return null;
 			}
 			public boolean isReady() {
 				return !isDone();
 			}
 			public boolean isDone() {
 				return (x <= 0);
 			}
 			public int getPriority() {
 				return 0;
 			}
 		}
 	}
 	
 	
 	
 	/** Test two work targets, once of which must always follow the other (in other words, one is always ready, but the other changes readiness based on the progress of the first). */
 	public class TestNonblockingLeaderFollower extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler(0).start();
 		final int HIGH = 10000;
 		final int LOW = 100;
 		
 		public Object call() throws InterruptedException, ExecutionException {
 			WorkLeader $w1 = new WorkLeader();
 			WorkFollower $w2 = new WorkFollower();
 			$w2.$leader = $w1;
 			WorkFuture<Void> $f2 = $ws.schedule($w2, ScheduleParams.NOW);
 			$w1.$followerFuture = $f2;
 			WorkFuture<Void> $f1 = $ws.schedule($w1, ScheduleParams.NOW);
 			
 			$f1.get();
 			$f2.get();
 			
 			assertTrue($w1.isDone());
 			assertFalse($w1.isReady());
 			assertTrue($w2.isDone());
 			assertFalse($w2.isReady());
 			assertEquals(LOW, $w2.x);
 			assertEquals(LOW, $w1.x);
 			
 			breakCaseIfFailed();
 			$ws.stop(false);
 			return null;
 		}
 		private class WorkLeader implements WorkTarget<Void> {
 			public volatile WorkFuture<?> $followerFuture;
 			public volatile int x = HIGH;
 			public Void call() {
 				x--;
 				$ws.update($followerFuture);
 				return null;
 			}
 			public boolean isReady() {
 				return !isDone();
 			}
 			public boolean isDone() {
 				return (x <= LOW);
 			}
 			public int getPriority() {
 				return 0;
 			}
 			public String toString() {
 				return "TestNonblockingLeaderFollower-WorkLeader";
 			}
 		}
 		public class WorkFollower implements WorkTarget<Void> {
 			public volatile WorkLeader $leader;
 			public volatile int x = HIGH;
 			public Void call() {
 				if (!isReady()) throw new IllegalStateException("called while unready with x="+x);	// not normal semantics for ready, obviously, but true for this test, since it should only be possible to flip to unready by running and one should never be scheduled for multiple runs without a check of readiness having already occurred between each run.
 				x--;
 				return null;
 			}
 			public boolean isReady() {
 				return (x > $leader.x);
 			}
 			public boolean isDone() {
 				return (x <= LOW);
 			}
 			public int getPriority() {
 				return 0;
 			}
 			public String toString() {
 				return "TestNonblockingLeaderFollower-WorkFollower";
 			}
 		}
 	}
 	
 	
 	
 	private class TestFinishWhileRunning extends TestCase.Unit {
 		private final WorkScheduler $ws = makeScheduler(0).start();
 		private final Pipe<String> $pipe = new DataPipe<String>();
 		
 		//XXX:AHS:THREAD: this really not a very smart test i think.  we should have one thread just constantly trying to finish a work target that's counting to 10.
 		public Object call() throws InterruptedException, ExecutionException {
 			$pipe.sink().write(TD.s1);
 			$pipe.sink().write(TD.s2);
 			$pipe.sink().close();
 			
 			WorkFuture<String> $wf1 = $ws.schedule(new Work(), ScheduleParams.NOW);
 			WorkFuture<String> $wf2 = $ws.schedule(new Work(), ScheduleParams.makeDelayed(3));
 			
 			$log.trace("waiting for first future...");
 			String $lol = $wf1.get();
 			assertNotNull($lol);
 			$log.trace("first future returned "+$lol+"; waiting for second...");
 			assertEquals($lol == TD.s1 ? TD.s2 : TD.s1, $wf2.get());	// which ever one the first to finish didn't get, the second must remember.
 			
 			breakCaseIfFailed();
 			$ws.stop(false);
 			return null;
 		}
 		private class Work implements WorkTarget<String> {
 			public String call() {
 				String $answer = $pipe.source().readNow();
 				$log.trace("read "+$answer);
 				X.chill(8);
 				$log.trace("returning "+$answer);
 				return $answer;
 			}
 			public boolean isReady() {
 				return !isDone();
 			}
 			public boolean isDone() {
 				return $pipe.source().isExhausted();
 			}
 			public int getPriority() {
 				return 0;
 			}
 		}
 	}
 	
 	
 	
 	private class TestCancelWhileRunning extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler(0).start();
 		private volatile boolean $clear = false;
 		public Object call() throws InterruptedException, ExecutionException, TimeoutException {
 			WorkFuture<Void> $wf = $ws.schedule(new WorkTarget.RunnableWrapper(new Work()), ScheduleParams.NOW);
			X.chill(2);
 			$wf.cancel(false);
 			assertEquals("work became cancelling state", WorkFuture.State.CANCELLING, $wf.getState());
 			try {
				$wf.get(55, TimeUnit.MILLISECONDS);
 				throw new AssertionFailed("should have gotten CancellationException");
 			} catch (CancellationException $e) { /* good! */ }
 			assertTrue("thread is clear of the work", $clear);
 			assertEquals("work reached cancelled state", WorkFuture.State.CANCELLED, $wf.getState());
 			$ws.stop(false);
 			return null;
 		}
		private class Work implements Runnable { public void run() { X.chill(50); $clear = true; } }
 	}
 	
 	
 	
 	/**  */
 	private class TestScheduleSingleDelayMany extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler(0);
 		public final int WTC = 8;
 		
 		public Object call() throws InterruptedException, ExecutionException {
 			final int space = 100;
 			WorkFuture<?>[] $wf = Arr.newInstance(WorkFuture.class, WTC);
 			$wf[3] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 03), ScheduleParams.makeDelayed(4*space));
 			$wf[4] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), -9), ScheduleParams.makeDelayed(5*space));
 			$wf[5] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 07), ScheduleParams.makeDelayed(6*space));
 			$wf[0] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 00), ScheduleParams.makeDelayed(1*space));
 			$wf[1] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 40), ScheduleParams.makeDelayed(2*space));
 			$wf[2] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 17), ScheduleParams.makeDelayed(3*space));
 			$wf[6] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 30), ScheduleParams.makeDelayed(7*space));
 			$wf[7] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), -6), ScheduleParams.makeDelayed(8*space));
 			$log.trace(this, "work scheduler starting...");
 			$ws.start();
 			$log.trace(this, "work scheduler started.");
 			
 			long $startTime = X.time();
 			for (int $i = 1; $i < WTC; $i++) {
 				$wf[$i-1].get();
 				long $timeTaken = X.time() - $startTime;
 				$log.trace(this, "task with "+$i*space+"ms delay finished");
 				assertTrue("task less than "+AOD+"ms overdue ($timeTaken="+$timeTaken+")", $timeTaken-AOD < $i*100);
 				assertFalse($wf[$i].isDone());
 			}
 			
 			breakCaseIfFailed();
 			$ws.stop(false);
 			return null;
 		}
 		private class Work implements Runnable {
 			public void run() {	// the run-once functionality is just provided by the RunnableWrapper class.
 				$log.trace(this, "task running");
 			}
 		}
 	}
 	
 	
 	
 	/** One task with a fixed delay is scheduled to run 10 times, and is checked by another thread. */
 	private class TestScheduleFixedRate extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler(0).start();
 		
 		public Object call() throws InterruptedException, ExecutionException {
 			Work $wt = new Work();
 			final int initialDelay = 100;
 			final int repeatDelay = 25;
 			WorkFuture<Integer> $wf = $ws.schedule($wt, ScheduleParams.makeFixedRate(initialDelay, repeatDelay));
 			
 			long $startTime = X.time();
 			long $targetTime = $startTime + initialDelay;
 			X.chillUntil($targetTime+AOD);
 			assertEquals("check amount of work completed after initial delay", 9, $wt.x);
 			for (int $i = 8; $i >= 0; $i--) {
 				X.chillUntil(($targetTime += repeatDelay)+AOD);
 				assertEquals("check amount of work completed after repeating delay", $i, $wt.x);
 			}
 			assertEquals(0, $wf.get().intValue());
 			
 			breakCaseIfFailed();
 			$ws.stop(false);
 			return null;
 		}
 		private class Work implements WorkTarget<Integer> {
 			public volatile int x = 10;
 			public Integer call() {
 				x--;
 				$log.trace(this, "reached count "+x);
 				return x;
 			}
 			public boolean isReady() {
 				return !isDone();
 			}
 			public boolean isDone() {
 				return (x <= 0);
 			}
 			public int getPriority() {
 				return 0;
 			}
 		}
 	}
 	
 	
 	
 	/** Similar to {@link TestNonblockingLeaderFollower}, but with many more WorkTargets than threads, and all drawing from the same input pipe (which will have been closed before the draining WorkTargets start).
 	 * 
 	 * This is a very major test, since it deals with WorkTarget who have to notice their doneness and finish concurrently instead of at the completion of a normal run (probably).
 	 * 
 	 * This also tests (if indirectly) consistent results from WorkTarget that receive concurrent finishes (but I'd recommend running it numerous times if you want to feel confident of that.  And by numerous times, I mean many thousands of times.).
 	 */
 	private class TestNonblockingManyWorkSingleSource extends TestCase.Unit {
 		protected WorkScheduler $ws = makeScheduler(0).start();
 		public final int HIGH = 1000;
 		public final int WTC = 32;
 		
 		protected final Pipe<Integer> $pipe = new DataPipe<Integer>();
 		protected final Work[] $wt = new Work[WTC];
 		@SuppressWarnings("unchecked")	// impossible to not suck in java.
 		protected final WorkFuture<Integer>[] $wf = Arr.newInstance(WorkFuture.class, WTC);
 		
 		public Object call() throws InterruptedException, ExecutionException {
 			feedPipe();
 
 			$log.trace(this, "creating work targets");
 			for (int $i = 0; $i < WTC; $i++)
 				$wt[$i] = new Work($i);
 			$log.trace(this, "scheduling work targets");
 			for (int $i = 0; $i < WTC; $i++)
 				$wf[$i] = $ws.schedule($wt[$i], ScheduleParams.NOW);
 			
 			configurePipe();	// this actually illustrates an important conundrum: one can't assign listeners until after we have the WorkFuture to make noise about... so if you want to be able to write (and possibly finish writing) concurrently with starting up new work to do reading, you actually require the ReadHead to send a "spurious" (that is, not triggered by any actual life-cycle event of write, close, or final read) event when you assign that Listener to it. 
 			
 			$log.trace(this, "waiting for work future completion");
 			boolean $wonOnce = false;
 			for (int $i = 0; $i < WTC; $i++) {
 				Integer $ans = $wf[$i].get();
 				$log.debug(this, "final result of work target "+$i+": "+$ans);
 				if ($ans != null && $ans == HIGH) {
 					assertFalse("No more than one WorkTarget finished with the high value.", $wonOnce);
 					$wonOnce = true;
 				}
 			}
 			assertTrue("Exactly one WorkTarget finished with the high value.", $wonOnce);
 			
 			breakCaseIfFailed();
 			$ws.stop(false);
 			return null;
 		}
 		private class Work implements WorkTarget<Integer> {
 			public Work(int $name) { this.$name = $name; }
 			private final int $name;
 			public Integer call() {
 				Integer $move = $pipe.source().readNow();
 				$log.trace(this, "WT"+$name+" pulled "+$move);
 				return $move;
 			}
 			public boolean isReady() {		// note that these actually CAN NOT be synchronized.  if they are, deadlock can occur in schedulers.
 				return $pipe.source().hasNext();
 			}
 			public boolean isDone() {		// note that these actually CAN NOT be synchronized.  if they are, deadlock can occur in schedulers.
 				return $pipe.source().isClosed() && !$pipe.source().hasNext();
 			}
 			public int getPriority() {
 				return 0;
 			}
 			public String toString() {
 				return Reflect.getShortClassName(this)+":[WT:"+$name+";];";
 			}
 		}
 		protected void feedPipe() {
 			$log.trace(this, "feed started");
 			for (int $i = 1; $i <= HIGH; $i++)
 				$pipe.sink().write($i);
 			$log.trace(this, "feed complete");
 			$pipe.sink().close();
 			$log.trace(this, "feed closed");
 		}
 		protected void configurePipe() {}
 	}
 	
 	
 	
 	/** Same as {@link TestNonblockingManyWorkSingleSource}, but the input pipe will be closed from the sink() thread when the source is already empty (resulting in a (probably) concurrent finish for the WorkTargets). */
 	private class TestNonblockingManyWorkSingleConcurrentSource extends TestNonblockingManyWorkSingleSource {
 		protected void feedPipe() {
 			final WorkSchedulerFlexiblePriority $bs = (WorkSchedulerFlexiblePriority) $ws;
 			$ws.schedule(new WorkTarget.RunnableWrapper(new Runnable() {
 				public void run() {
 					$log.trace("PIPE SIZE: "+$pipe.size()+"\nSCHEDULER STATUS:\n" + $bs.getStatus(true));
 				}
 			}, true, false, 100000), ScheduleParams.makeFixedDelay(100));
 			
 			$ws.schedule(new WorkTarget.RunnableWrapper(new Runnable() { public void run() { TestNonblockingManyWorkSingleConcurrentSource.super.feedPipe(); } }), ScheduleParams.NOW);	// that was an incredibly satisfying line to write
 		}
 		
 		protected void configurePipe() {
 			// the earlier test didn't actually need to set the pipe listener because all the writes were done before any reading started, and so all of the work was always ready as long as it wasn't done.  now we're in an entirely different situation.
 			$pipe.source().setListener(new Listener<ReadHead<Integer>>() {
 				public void hear(ReadHead<Integer> $rh) {
 					for (WorkFuture<Integer> $x : $wf)
 						$ws.update($x);
 				}
 			});
 		}
 	}
 	
 	
 	
 	/** Test that when two tasks of different priority are scheduled, the higher priority goes first.
 	 *  A scheduler with a thread pool size of one is used. */
 	private class TestPrioritizedDuo extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler(1);
 		
 		public Object call() throws InterruptedException, ExecutionException {
 			WorkFuture<Void> $wf_low = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 10), ScheduleParams.NOW);
 			WorkFuture<Void> $wf_high = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 90000), ScheduleParams.NOW);
 			$ws.start();
 			
 			$wf_high.get();
 			X.chill(50-AOD);
 			assertFalse($wf_low.isDone());
 			$wf_low.get();
 
 			$ws.stop(false);
 			return null;
 		}
 		
 		private class Work implements Runnable { public void run() { X.chill(50); } }
 	}
 	
 	
 	
 	/**  */
 	private class TestBasic extends TestCase.Unit {
 		protected WorkScheduler $ws = makeScheduler(0).start();
 		
 		public Object call() {
 			//TMPL
 			breakIfFailed();
 			$ws.stop(false);
 			return null;
 		}
 	}
 }
