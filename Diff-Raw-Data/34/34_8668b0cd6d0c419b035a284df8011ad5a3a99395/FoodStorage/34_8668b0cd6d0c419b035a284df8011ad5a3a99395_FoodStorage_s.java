 package hu.miracle.workers;
 
 public class FoodStorage extends Storage {
 
 	// Constructor
 	public FoodStorage() {
 	}
 
 	// Public interface
 	public void handleTick() {
 		System.out.println(getClass().getCanonicalName() + ".handleTick()");
 	}
 
 	// Ételfelvétel
 	public int getItems() {
 		System.out.println(getClass().getCanonicalName() + ".getItems()");
		return 0;
 	}
 
 	// Ételvisszarakás
 	public void putItems(int count) {
 		System.out.println(getClass().getCanonicalName() + ".putItems()");
 	}
 }
