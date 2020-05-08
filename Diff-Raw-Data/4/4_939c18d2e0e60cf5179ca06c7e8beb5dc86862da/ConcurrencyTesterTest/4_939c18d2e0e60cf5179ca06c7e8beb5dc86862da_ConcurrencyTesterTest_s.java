 /* Copyright 2013 Jonatan Jönsson
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package se.softhouse.common.testlib;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.fest.assertions.Fail.fail;
 
 import java.util.Collections;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 import java.util.concurrent.BrokenBarrierException;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.CyclicBarrier;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicReference;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.junit.Test;
 
 import se.softhouse.common.testlib.ConcurrencyTester.RunnableFactory;
 
 import com.google.common.base.Throwables;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.google.common.testing.NullPointerTester;
 import com.google.common.testing.NullPointerTester.Visibility;
 import com.google.common.util.concurrent.Atomics;
 
 /**
  * Tests for {@link ConcurrencyTester}
  */
 public class ConcurrencyTesterTest
 {
 	@Test
 	public void testThatRunsAreInterleavedBetweenDifferentThreadsForEachIteration() throws Throwable
 	{
 		final Vector<Integer> invocationIdentifiers = new Vector<Integer>();
 		final int iterationCount = 100;
 		ConcurrencyTester.verify(new RunnableFactory(){
 			@Override
 			public int iterationCount()
 			{
 				return iterationCount;
 			}
 
 			@Override
 			public Runnable create(final int identifierForThread)
 			{
 				return new Runnable(){
 					@Override
 					public void run()
 					{
 						invocationIdentifiers.add(identifierForThread);
 					}
 				};
 			}
 		}, Constants.EXPECTED_TEST_TIME_FOR_THIS_SUITE, TimeUnit.MILLISECONDS);
 
 		int minInARow = Integer.MAX_VALUE;
 		int currentStreak = 0;
 		Integer lastIdentifier = null;
 		for(int invocationIdentifier : invocationIdentifiers)
 		{
			if(lastIdentifier != null && invocationIdentifier == lastIdentifier)
 			{
 				if(currentStreak < minInARow)
 				{
 					minInARow = currentStreak;
 				}
 				currentStreak = 0;
 			}
 			lastIdentifier = invocationIdentifier;
 			currentStreak++;
 		}
 		assertThat(minInARow) //
 				.as("At least two of the threads should be interleaved with each other "
 							+ "to boost confidence of concurrency harness to an acceptable level") //
 				.isLessThan(iterationCount);
 	}
 
 	@Test
 	public void testThatExceptionsFromRunnableIsPropagatedAsIs() throws Throwable
 	{
 		try
 		{
 			final SimulatedException error = new SimulatedException();
 			ConcurrencyTester.verify(new RunnableFactory(){
 				@Override
 				public int iterationCount()
 				{
 					return 1;
 				}
 
 				@Override
 				public Runnable create(int uniqueNumber)
 				{
 					return new Runnable(){
 						@Override
 						public void run()
 						{
 							throw error;
 						}
 					};
 				}
 			}, Constants.EXPECTED_TEST_TIME_FOR_THIS_SUITE, TimeUnit.MILLISECONDS);
 			fail(error.getClass() + " was not propagated correctly, this could potentially hide AssertionErrors thrown when bugs are introduced");
 		}
 		catch(SimulatedException expected)
 		{
 		}
 	}
 
 	@Test
 	public void testThatEachRunnableIsRunIterationCountTimes() throws Throwable
 	{
 		final Map<Integer, AtomicInteger> counters = Maps.newHashMap();
 		final int iterationCount = 100;
 		ConcurrencyTester.verify(new RunnableFactory(){
 			@Override
 			public int iterationCount()
 			{
 				return iterationCount;
 			}
 
 			@Override
 			public Runnable create(int uniqueNumber)
 			{
 				final AtomicInteger counter = new AtomicInteger();
 				counters.put(uniqueNumber, counter);
 				return new Runnable(){
 					@Override
 					public void run()
 					{
 						counter.incrementAndGet();
 					}
 				};
 			}
 		}, Constants.EXPECTED_TEST_TIME_FOR_THIS_SUITE, TimeUnit.MILLISECONDS);
 
 		for(AtomicInteger counter : counters.values())
 		{
 			assertThat(counter.get()).isEqualTo(iterationCount);
 		}
 		assertThat(counters).hasSize(ConcurrencyTester.NR_OF_CONCURRENT_RUNNERS);
 	}
 
 	@Test
 	public void testThatNoUniqueNumberIsUsedTwice() throws Throwable
 	{
 		final Set<Integer> assignedNumbers = Collections.synchronizedSet(Sets.<Integer>newHashSet());
 		ConcurrencyTester.verify(new RunnableFactory(){
 			@Override
 			public int iterationCount()
 			{
 				return 2;
 			}
 
 			@Override
 			public Runnable create(final int uniqueNumber)
 			{
 				assignedNumbers.add(uniqueNumber);
 				return new Runnable(){
 					@Override
 					public void run()
 					{
 					}
 				};
 			}
 		}, Constants.EXPECTED_TEST_TIME_FOR_THIS_SUITE, TimeUnit.MILLISECONDS);
 		assertThat(assignedNumbers).hasSize(ConcurrencyTester.NR_OF_CONCURRENT_RUNNERS);
 	}
 
 	@Test
 	public void testThatInterruptedStatusIsClearedWhenInterruptedAndThatRemainingThreadsGetInterrupted() throws Throwable
 	{
 		final Thread originThread = Thread.currentThread();
 		final AtomicReference<Throwable> failure = Atomics.newReference();
 		final Lock infinitelyLocked = new ReentrantLock();
 		infinitelyLocked.lock();
 		// +1 so that the starter thread also can await the start
 		final CyclicBarrier startSignal = new CyclicBarrier(ConcurrencyTester.NR_OF_CONCURRENT_RUNNERS + 1);
 		final CountDownLatch activeWorkers = new CountDownLatch(ConcurrencyTester.NR_OF_CONCURRENT_RUNNERS);
 		Thread interruptableThread = new Thread(){
 			@Override
 			public void run()
 			{
 				try
 				{
 					ConcurrencyTester.verify(new RunnableFactory(){
 						@Override
 						public int iterationCount()
 						{
 							return 1;
 						}
 
 						@Override
 						public Runnable create(final int uniqueNumber)
 						{
 							return new Runnable(){
 								@Override
 								public void run()
 								{
 									try
 									{
 										startSignal.await();
 										// Wait for the interrupt signal
 										infinitelyLocked.lockInterruptibly();
 										fail("Executor did not interrupt remaining threads during shutdown operation");
 									}
 									catch(InterruptedException expected)
 									{
 										Thread.interrupted();
 									}
 									catch(BrokenBarrierException e)
 									{
 										Throwables.propagate(e);
 									}
 									activeWorkers.countDown();
 								}
 							};
 						}
 					}, Constants.EXPECTED_TEST_TIME_FOR_THIS_SUITE, TimeUnit.MILLISECONDS);
 				}
 				catch(InterruptedException expected)
 				{
 					originThread.interrupt();
 				}
 				catch(Throwable error)
 				{
 					failure.compareAndSet(null, error);
 					originThread.interrupt();
 				}
 			}
 		};
 		try
 		{
 			interruptableThread.start();
 			startSignal.await();
 			interruptableThread.interrupt();
 
 			if(!activeWorkers.await(Constants.EXPECTED_TEST_TIME_FOR_THIS_SUITE, TimeUnit.MILLISECONDS))
 			{
 				fail("Timeout while waiting for shutdown of remaining threads");
 			}
 		}
 		catch(InterruptedException e)
 		{
 		}
 		if(failure.get() != null)
 			throw failure.get();
 	}
 
 	@Test
 	public void testThatNullContractsAreFollowed() throws Exception
 	{
 		new NullPointerTester().testStaticMethods(ConcurrencyTester.class, Visibility.PACKAGE);
 	}
 
 	@Test
 	public void testThatUtilityClassDesignIsCorrect()
 	{
 		UtilityClassTester.testUtilityClassDesign(ConcurrencyTester.class);
 	}
 }
