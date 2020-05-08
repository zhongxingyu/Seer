 package edu.umd.cs.guitar.ripper.test;
 
 import org.junit.Test;
 
 import edu.umd.cs.guitar.ripper.SWTRipperMain;
 
 public class SWTRipperMainTest {
 
 	@Test
 	public void testMainNoArgs() {
 //		SWTRipperMain.main(new String[0]);
 		// TODO test output by redirecting stdout?
 	}
 
	@Test
 	public void testMain() {
		SWTRipperMain.main(new String[] { "-c", "NOSUCHCLASS" });
		// TODO how do we test this if it handles the exception itself?
 	}
 }
