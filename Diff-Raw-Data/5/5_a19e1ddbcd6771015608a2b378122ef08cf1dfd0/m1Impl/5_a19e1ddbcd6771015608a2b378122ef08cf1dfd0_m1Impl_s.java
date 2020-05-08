 package setImpls_java;
 
 // Task: m1Impl
 
 public class m1Impl implements Runnable {
 
 	// Instance variables and constants
 	protected SetImpl<Integer> mySet = new SetImpl<Integer>();
 	protected SetImpl<Integer> otherSet = new SetImpl<Integer>();
	protected Integer element = 0;
 	protected SetImpl<AltType> altTypeSet = new SetImpl<AltType>();
 	protected AltType alt1 = new AltType();
 	protected AltType alt2 = new AltType();
 	protected HashMapImpl<AltType, AltType> func = new HashMapImpl<AltType, AltType>();
 	protected int priority = 5;
 
 	public m1Impl() {
 	}
 
 	public void run() {
 		mySet = mySet.intersect(otherSet);
 		mySet = mySet.union(otherSet);
 		mySet = mySet.subtract(otherSet);
 		element = mySet.getFirst();
 		mySet = (mySet.setUnion(element));
 		altTypeSet = (altTypeSet.setUnion(alt1));
 		func = func.put2(alt1, alt2);
 		alt1 = func.get(alt1);
 	}
 
 	// Subroutines
 	public int getPriority() {
 		return priority;
 	}
 
 }
