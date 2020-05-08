 package net.sourceforge.htmlunit;
 
 import org.junit.Test;
 
 /**
 * Unit tests for <a href="https://bugzilla.mozilla.org/show_bug.cgi?id=374918">Bug 374918 - 
 * String primitive prototype wrongly resolved when used with many top scopes</a>  
  * @author Marc Guillemot
  */
 public class ArrayConcatTest
 {
 	/**
	 * Test for properties order as exposed in issues
	 * https://bugzilla.mozilla.org/show_bug.cgi?id=419090
	 * and
	 * https://bugzilla.mozilla.org/show_bug.cgi?id=383592
 	 * @throws Exception if the test fails
 	 */
 	@Test
 	public void concat() throws Exception {
 		final String script = "var a = [1, 2, 3];\n"
 			+ "for (var i=10; i<20; ++i) a[i] = 't' + i;\n"
 			+ "var b = [1, 2, 3];\n"
 			+ "b.concat(a)";
 		
 		Utilities.executeScript(script);
 	}
 }
