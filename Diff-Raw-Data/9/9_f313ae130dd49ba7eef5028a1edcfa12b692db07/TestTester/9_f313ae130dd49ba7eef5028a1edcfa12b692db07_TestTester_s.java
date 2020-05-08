 
 public class TestTester extends Test {
 	
 	public void test_smokeTest() {
 		assertEqual( "a", "a", "a != a" );
 		assertEqual( "a", "b", "a != b" );
 		assertTrue(true, "true is not true");
 		assertTrue(false, "false is not true");
 	}
 	public void test_passingTests() {
 		assertEqual( "a", "a", "a != a" );
 		assertTrue(true, "true is not true");
 	}
 	public void test_exceptionTests() {
 		String[] test = new String[1];
 		assertEqual(test[3], "derp", "exception test");
 	}
 
 	public static void main( String[] args ) {
 		Test tester = new TestTester();
 		tester.run();
 	}
 }
