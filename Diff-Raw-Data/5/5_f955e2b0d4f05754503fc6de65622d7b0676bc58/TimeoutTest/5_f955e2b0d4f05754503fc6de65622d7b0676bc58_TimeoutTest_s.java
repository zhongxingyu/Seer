 package se.mockachino;
 
 import org.junit.Test;
 import se.mockachino.verifier.VerifyRangeStart;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertTrue;
 
 public class TimeoutTest {
 
 	@Test
 	public void testTimeoutSuccessAtLeast1() {
 		final List mock = Mockachino.mock(ArrayList.class);
 		mock.size();
 		runTimeoutTest(Mockachino.verifyAtLeast(1), 0, 60, 200, 500, true, mock, new Runnable() {
 			@Override
 			public void run() {
 			}
 		});
 	}
 
 	@Test
 	public void testTimeoutSuccessAtLeast2() {
 		final List mock = Mockachino.mock(ArrayList.class);
 		runTimeoutTest(Mockachino.verifyAtLeast(1), 199, 230, 200, 500, true, mock, new Runnable() {
 			@Override
 			public void run() {
 				mock.size();
 			}
 		});
 	}
 
 	@Test
 	public void testTimeoutFailAtLeast() {
 		final List mock = Mockachino.mock(ArrayList.class);
		runTimeoutTest(Mockachino.verifyAtLeast(1), 190, 220, 200, 190, false, mock, new Runnable() {
 			@Override
 			public void run() {
 				mock.size();
 			}
 		});
 	}
 
 	@Test
 	public void testTimeoutSuccessAtMost() {
 		final List mock = Mockachino.mock(ArrayList.class);
 		runTimeoutTest(Mockachino.verifyAtMost(1), 500, 520, 200, 500, true, mock, new Runnable() {
 			@Override
 			public void run() {
 				mock.size();
 			}
 		});
 	}
 
 	@Test
 	public void testTimeoutFailAtMost() {
 		final List mock = Mockachino.mock(ArrayList.class);
 		runTimeoutTest(Mockachino.verifyAtMost(1), 200, 220, 200, 500, false, mock, new Runnable() {
 			@Override
 			public void run() {
 				mock.size();
 				mock.size();
 			}
 		});
 	}
 
 	@Test
 	public void testTimeoutFailExactly() {
 		final List mock = Mockachino.mock(ArrayList.class);
 		mock.size();
 		mock.size();
 		runTimeoutTest(Mockachino.verifyExactly(2), 200, 220, 200, 500, false, mock, new Runnable() {
 			@Override
 			public void run() {
 				mock.size();
 			}
 		});
 	}
 
 	@Test
 	public void testTimeoutSuccessExactly() {
 		final List mock = Mockachino.mock(ArrayList.class);
 		mock.size();
 		mock.size();
 		runTimeoutTest(Mockachino.verifyExactly(2), 500, 520, 200, 500, true, mock, new Runnable() {
 			@Override
 			public void run() {
 			}
 		});
 	}
 
 	private void runTimeoutTest(VerifyRangeStart type, int minExecutionTime, int maxExecutionTime, int waitTime, int timeout, boolean wantsOk, List mock, Runnable runnable) {
 
 		Executors.newSingleThreadScheduledExecutor().schedule(runnable, waitTime, TimeUnit.MILLISECONDS);
 		long t1 = System.currentTimeMillis();
 		boolean status;
 		try {
 			type.withTimeout(timeout).on(mock).size();
 			status = true;
 		} catch (Exception e) {
 			status = false;
 		}
 		long t2 = System.currentTimeMillis();
 		long time = t2 - t1;
 		assertTrue("Time: " + time + " expected at most " + maxExecutionTime, time <= maxExecutionTime);
 		assertTrue("Time: " + time + " expected at least " + minExecutionTime, time >= minExecutionTime);
 
		assertEquals(status, wantsOk);
 	}	
 }
