 package object_out;
 
 public class TestReplaceSimpleInitializedSetter {
 	private final int i;
 	
	TestReplaceSimpleInitializedSetter() {
 		this.i = 42;
 	}
 
	TestReplaceSimpleInitializedSetter(int i) {
 		this.i = i;
 	}
	
 	TestReplaceSimpleInitializedSetter setI(int i) {
 		return new TestReplaceSimpleInitializedSetter(i);
 	}
 }
