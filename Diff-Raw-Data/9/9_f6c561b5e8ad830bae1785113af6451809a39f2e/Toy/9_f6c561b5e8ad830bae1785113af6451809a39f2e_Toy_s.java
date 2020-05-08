 public class Toy {
 	static final boolean ASSERTIONS = false;
 	static { assert ASSERTIONS == true : "Assertions enabled, but ASSERTIONS is false"; }
 	public static void main( String args[] ) {
 		test();
 		if ( ASSERTIONS ) assert 1 == 0;
 	}
	public static void test() { 
 		boolean assertionsAreEnabled = false;
 		assert assertionsAreEnabled = true; // side-effect is intended!
		if (assertionsAreEnabled) System.out.println( "Assertions are enabled!" );
 		else System.out.println( "Assertions are disabled!" );
 	}
}
