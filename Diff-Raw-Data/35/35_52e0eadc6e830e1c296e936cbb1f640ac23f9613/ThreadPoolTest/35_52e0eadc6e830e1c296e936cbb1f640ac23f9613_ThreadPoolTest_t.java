 package dfh.thread.pool;
 
import java.util.concurrent.atomic.AtomicInteger;

 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class ThreadPoolTest {
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		// leaving this stub here as a reminder in case
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void basicTest() {
 		try {
 			for (int i = 0; i < 100; i++) {
 				final int j = i;
 				Runnable r = new Runnable() {
 					@Override
 					public void run() {
 						System.out.printf("%.2f%n", Math.sqrt(j));
 					}
 				};
 				ThreadPool.enqueue(r);
 			}
 			ThreadPool.flush();
 			org.junit.Assert.assertTrue("finished simple task", true);
 		} catch (Exception e) {
 			org.junit.Assert.fail("error thrown: " + e.getMessage());
 		}
 	}

	@Test
	public void exceptions() {
		final AtomicInteger ai = new AtomicInteger();
		try {
			for (int i = 0; i < 100; i++) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						ai.incrementAndGet();
						throw new RuntimeException("oopsie!");
					}
				};
				ThreadPool.enqueue(r);
			}
			ThreadPool.flush();
			org.junit.Assert.assertEquals("incremented every time", 100L, ai.longValue());
		} catch (Exception e) {
			org.junit.Assert.fail("error thrown: " + e.getMessage());
		}
	}
 }
