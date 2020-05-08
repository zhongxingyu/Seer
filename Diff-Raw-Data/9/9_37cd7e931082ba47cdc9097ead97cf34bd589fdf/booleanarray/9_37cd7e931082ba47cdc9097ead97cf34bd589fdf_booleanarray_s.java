 // EXT:ISC
 // EXT:ICG
 
 class MainClass {
 	public static void main(String[] args) {
		System.out.println( new A().doStuff(-100)[0] );
 		System.out.println( new A().doStuff( 100)[0] );
 	}
 }
 
 class booleanarray {
 	int[] array;
 
 	public boolean create(int len) {
 		array = new int[len];
 		return true;
 	}
 
 	public int getLength() {
 		return array.length;
 	}
 
 	public boolean set(int pos, boolean value) {
 		boolean success;
 		if( 0 < pos+1 && pos < this.getLength() ) {
 			if(value)
 				array[pos] = 1;	// 1 is true
 			else
 				array[pos] = 0;	// 0 is false
 
 			success = true;
 		} else
 			success = false;
 
 		return success;
 	}
 
 	public boolean get(int pos) {
 		boolean retval;
 		if(array[pos] < 1)	// Then it is zero
 			retval = false;
 		else
 			retval = true;
 
 		return retval;
 	}
 
 	public int[] getArray() {
 		return array;
 	}
 
 	public boolean setArray(int[] newarray) {
 	    array = newarray;
 		return true;
 	}
 }
 
 class A {
 	public int[] doStuff(int hundra)
 	{
 		// Variable declarations
 		int i;
 		moreBooleanArray ba;
 		boolean start;
 		boolean junk;
 
 		// Assigning stuff
 		i = 0;
 		ba = new moreBooleanArray();
 		start = true;
 
 		junk = ba.create(10);
 
 		while(i < ba.getLength() && ba.set(i, start))
 		{
 			start = !start;
 			i = i + 1;
 		}
 
 		// Should be true
 		junk = ba.get(2);
 		// Will become false
 		if( ba.set(2, false) )
 			junk = ba.get(2);						// Should be false
 		else
 			System.out.println(4711);				// Should not happen
 
 		if(hundra < 99) {
 			junk = ba.reset();
 		}
 		else {}
 
 		return ba.getArray();
 	}
 }
 
 class moreBooleanArray extends booleanarray {
     public boolean reset() {
 	int[] ba;
 	boolean retval;
 	int c;
 	retval = false;
 
 	ba = this.getArray();
 	c = 0;
 	while(c < ba.length) {
 	    ba[c] = 0;
 	    c = c + 1;
 	}
 	return this.setArray(ba);
     }
 }
