 package de.fzi.cjunit.jpf.assumptions;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 
 import java.util.List;
 
 import gov.nasa.jpf.Error;
 import gov.nasa.jpf.jvm.NoUncaughtExceptionsProperty;
 
 import org.junit.Test;
 
 import de.fzi.cjunit.testutils.JPFForTesting;
 
 /*
  * JPF provides its own implementation of some standard Java classes, but
  * those implementations are in some cases incomplete, i.e. there are methods
  * missing from the JPF implementation that are part of the public API or the
  * originals.  This class collects some of those methods.
  */
 public class NoSuchMethodExceptionInJPF extends JPFForTesting {
 
 	public static class GT {
 		public static void main(String[] args) {
 			assertThat(0, greaterThan(1));
 		}
 	}
 
 	public static class GTOE {
 		public static void main(String[] args) {
 			assertThat(0, greaterThanOrEqualTo(1));
 		}
 	}
 
 	public static class LT {
 		public static void main(String[] args) {
 			assertThat(1, lessThan(0));
 		}
 	}
 
 	public static class LTOE {
 		public static void main(String[] args) {
 			assertThat(1, lessThanOrEqualTo(0));
 		}
 	}
 
 	public void runTest(Class<?> app, Class<? extends Throwable> tc) {
 		createJPF(app);
 		NoUncaughtExceptionsProperty nuep
 				= new NoUncaughtExceptionsProperty(config);
 		jpf.addSearchProperty(nuep);
 		jpf.run();
 		List<Error> errors = jpf.getSearchErrors();
 		assertThat("number of errors", errors.size(), equalTo(1));
 		assertThat("exception type",
 				nuep.getUncaughtExceptionInfo().getExceptionClassname(),
 				equalTo(tc.getName()));
 	}
 
 	@Test
 	public void testGT() {
 		runTest(GT.class, AssertionError.class);
 	}
 
 	@Test
 	public void testGTOE() {
		runTest(GTOE.class, AssertionError.class);
 	}
 
 	@Test
 	public void testLT() {
		runTest(LT.class, AssertionError.class);
 	}
 
 	@Test
 	public void testLTOE() {
		runTest(LTOE.class, AssertionError.class);
 	}
 }
