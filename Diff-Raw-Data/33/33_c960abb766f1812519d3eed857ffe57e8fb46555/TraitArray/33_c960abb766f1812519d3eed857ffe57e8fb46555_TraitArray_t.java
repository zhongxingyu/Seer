 package cwcore.legacy;
 
 
 /* TraitArray encompasses the features of an autonomous agent that are
  * constanat within the lifetime of a single agent and represent what
  * to a biological organism would be physical and genetic constraints. */
 
 @Deprecated
 public class TraitArray implements InnateArray {
 
 	/* Const named version of variables. */
 	public Integer foodEnergy;
 
 	public Integer breedEnergy;
 
 	public Integer initEnergy;
 
 	public Integer stepEnergy;
 
 	public Integer stepRockEnergy;
 
 	public Integer turnLeftEnergy;
 
 	public Integer turnRightEnergy;
 
 	public Float mutationRate;
 
 	/* The same variables kept in an array. */
 	private final Object[] array;
 
 	/* Constructor: make an InnateArray. */
 	public TraitArray(Object[] array) {
 		this.array = array;
 
 		foodEnergy = (Integer) array[0];
 		breedEnergy = (Integer) array[1];
 		initEnergy = (Integer) array[2];
 		stepEnergy = (Integer) array[3];
 		stepRockEnergy = (Integer) array[4];
 		turnLeftEnergy = (Integer) array[5];
 		turnRightEnergy = (Integer) array[6];
 		mutationRate = (Float) array[7];
 	}
 
 	/* Return an exact copy of this object. */
 	@Override
 	public Object clone() {
 		try {
 			return super.clone();
 		} catch (CloneNotSupportedException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	/*
 	 * Return -1, 0, or 1, depending on whether this InnateArray is less than,
 	 * equal to, or greater than other, by some measure.
 	 */
 	public int compareTo(InnateArray other) {
 		if ( !(other instanceof TraitArray) ) {
 			throw new IllegalArgumentException("Can only compare to TraitArray");
 		}
 		int compare;
 		Integer o, t;
 		/* Compare the indices in order. */
 		for (int i = 0; i < this.size(); i++) {
 			o = (Integer) other.index(i);
 			t = (Integer) this.index(i);
 			if ((compare = o.compareTo(t)) != 0) {
 				return compare;
 			}
 		}
 
 		/*
 		 * There was no difference during the loop, so the arrays must be the
 		 * same.
 		 */
 		return 0;
 	}
 
 	/* Return a copy of this InnateArray. No mutation is performed. */
 	public InnateArray copy(float mRate) {
 		return (InnateArray) this.clone();
 		/*
 		 * InnateArray c = this.clone();
 		 *
 		 * for (int i = 0; i < this.size(); i++) { if (cobweb.globals.random.() <
 		 * mutationRate) { // Mutate index!!! int newval = array[i].intValue(); //
 		 * takes values: 1, 2 int modifier = (int)(cobweb.globals.random.() * 2) +
 		 * 1; // adds half the time and subtracts half the time newval +=
 		 * modifier * ((cobweb.globals.random.() > .5) ? 1 : -1); array[i] = new
 		 * Integer(newval); } }
 		 */
 	}
 
 	/* Return the Object at index "i". */
 	public Object index(int i) {
 		return array[i];
 	}
 
 	/* Set the Object at index "i" to "o". */
 	public void index(int i, Object o) {
 		if (!array[i].getClass().equals(o.getClass()))
 			throw new java.lang.ClassCastException("index(int i, Object o)"
 					+ ": o is of different type from array[i]");
 
 		array[i] = o;
 	}
 
 	/* Give random values to all of the variables in the array. */
 	public void init() {
 		/*
 		 * Initializes that array to all random Integers . This function is
 		 * relatively useless for now.
 		 */
 		for (int i = 0; i < this.size(); i++) {
 			array[i] = new Integer(cobweb.globals.random.nextInt());
 		}
 	}
 
 	/* Return the number of elements this InnateArray can hold. */
 	public int size() {
 		return array.length;
 	}
 
 	/*
 	 * "Genetically" splice two this InnateArray with other, in this case
 	 * returning one's TraitArray (with equal chance either will be picked.
 	 */
 	public InnateArray splice(InnateArray other) {
 		/* change of plans: gets all of one parents info */
 
 		InnateArray result;
 		if (cobweb.globals.random.nextFloat() < .5) {
 			result = (InnateArray) this.clone();
 		} else {
 			result = (InnateArray) other.clone();
 		}
 		return result;
 	}
 
 	/*
 	 * Return a unique String representation of this InnateArray. ie., two
 	 * InnateArray's with identical properties will return the same String from
 	 * their toString() methods.
 	 */
 	@Override
 	public String toString() {
 		// Is there a way to check if toString() has been overloaded in a
 		// certain object (ie. no longer returns a memory address?
 		String output = this.index(0).toString();
 
 		for (int i = 1; i < this.size(); i++) {
 			output += "-" + index(i).toString();
 		}
 
 		return output;
 	}
 }
