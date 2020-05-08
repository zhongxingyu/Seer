 package object_out;
 
 public class TestReplaceSimpleInitializedSetter {
 	private final int i;
 	
	public TestReplaceSimpleInitializedSetter() {
 		this.i = 42;
 	}
 
	public TestReplaceSimpleInitializedSetter(int i) {
 		this.i = i;
 	}

 	TestReplaceSimpleInitializedSetter setI(int i) {
 		return new TestReplaceSimpleInitializedSetter(i);
 	}
 }
