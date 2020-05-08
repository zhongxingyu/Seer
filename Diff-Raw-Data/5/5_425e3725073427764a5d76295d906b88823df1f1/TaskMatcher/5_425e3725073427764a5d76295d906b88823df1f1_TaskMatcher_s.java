 package org.kalibro.core.concurrent;
 
 import static org.junit.Assert.*;
 import static org.kalibro.tests.SpecialAssertions.assertClassEquals;
 
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.kalibro.KalibroError;
 import org.kalibro.KalibroException;
 import org.kalibro.tests.ThrowableMatcher;
 
 public class TaskMatcher {
 
 	private Task<?> task;
 
 	public TaskMatcher(Task<?> task) {
 		this.task = task;
 	}
 
 	public void timesOutWith(long timeout, TimeUnit timeUnit) {
 		try {
 			task.execute(timeout, timeUnit);
 		} catch (Throwable exception) {
 			assertClassEquals(KalibroException.class, exception);
 			assertNotNull(exception.getCause());
 			assertClassEquals(TimeoutException.class, exception.getCause());
 			String unit = timeUnit.name().toLowerCase();
 			assertEquals("Timed out after " + timeout + " " + unit + " while " + task, exception.getMessage());
 		}
 	}
 
 	public ThrowableMatcher throwsError() {
 		return doThrow(KalibroError.class);
 	}
 
 	public ThrowableMatcher throwsException() {
 		return doThrow(KalibroException.class);
 	}
 
 	public ThrowableMatcher doThrow(Class<? extends Throwable> throwableClass) {
 		Throwable throwed = doCatch(throwableClass);
 		assertClassEquals("" + throwed.getStackTrace()[0], throwableClass, throwed);
 		return new ThrowableMatcher(throwed);
 	}
 
 	public void doThrow(Throwable throwable) {
 		assertSame(throwable, doCatch(throwable));
 	}
 
 	private Throwable doCatch(Object expected) {
 		try {
 			task.compute();
			fail("Expected but not throwed:\n" + expected);
			return null;
 		} catch (Throwable throwed) {
 			return throwed;
 		}
 	}
 }
