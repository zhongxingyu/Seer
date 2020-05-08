 package anchovy;
 
 /**
  * Pair<T> represents any <String, Object> pair 
  * 
  * @author Andrei
  * @param <T> -- an object type
  */
 
 public class Pair<T> {
 	/**
 	 * An enum that prevents hardcoding the strings used as labels for Pair values <br> 
 	 * More information about Java enums: http://docs.oracle.com/javase/tutorial/java/javaOO/enum.html
 	 */
 	public static enum Label {
 		temp("Temperature"),
 		pres("Pressure"),
 		coRL("Controll rod level"),
 		wLvl("Water level"),
 		RPMs("Revolutions per minute"),
 		psit("Position"),
 		cNme("Name of component"),
 		falT("FailuerTime"),
 		OPFL("Output flow rate"),
 		oPto("Outputs to"),
 		rcIF("Recieves input from"),
 		elec("Electrisity Generated"),
 		cmpnum("Component number"),
 		etc("Feel free to add more stuff;" +
 				"it should have the enum term 4-char long to keep the convention");
 		
 		// if needed we can add more info about labels (i.e. new fields)
 		private final String name;
 		
 		Label(String name) {
 			this.name = name;
 		}
 		
 		@Override
 		public String toString() {
 			return this.name;
 		}
 	}
 	private Label label;
 	/**
 	 * The label: this is a string that identifies what kind of value this Pair holds 
 	 */
 	private String first;
 	/**
 	 * The value: the actual variable, either primitive or reference (e.g. ArrayList)
 	 */
 	private T second;
 	
 	
 	/**
 	 * Simply initializes the label and the value with the 2 arguments given
 	 * @param nameassertTrue(gameEngine.getAllComponentInfo().equals(infoList));	
 	 * @param val
 	 */
 	public Pair(Label typeVal, T val) {
 		this.label = typeVal;
 		this.first = typeVal.name;
 		this.second = val;
 	}
 	
 	/**
 	 * 
 	 * @return The label contained within the pair.
 	 */
 	public Label getLabel(){return label;}
 	/**
 	 * Returns the label, i.e. the first element of the pair
 	 * @return String first == a string from Pair.Label
 	 */
 	public String first() {
 		return this.first;
 	}
 	
 	/**
 	 * Returns the label, i.e. the second element of the pair
 	 * @return T second == a value, either primitive or reference (e.g. ArrayList)
 	 */
 	public T second() {
 		return second;
 	}
 	
 	/**
 	 * Simply updates the value of the Pair, i.e. of the second element
 	 * @param val this value is assigned to the Pair
 	 */
 	public void update(T val) {
 		this.second = val;
 	}
 	
 	/**
	 * {@inheritDoc}
 	 * Determines whether two pairs are essentially the same. That is that the two pairs have the same label and the (second) values are also equal.
 	 */
	@Override
 	public boolean equals(Object _pair) {
 		Pair<?> pair = (Pair<?>) _pair;
 		
 		if(this.first().equals(pair.first()) == false)
 			return false;
 		
 		if(this.second().equals(pair.second()) == false)
 			return false;
 		
 		return true;
 	}
 	
 	/**
	 * {@inheritDoc}
 	 * @return The condense of the pair as a string in the form FIRST <SPACE> SECOND
 	 */
	@Override
 	public String toString() {
 		return this.first + " " + this.second.toString();
 	}
 
 	/**
 	 * Sandbox to check functionality. Remember to remove this main
 	 */
 	public static void main(String args[]) {
 		Pair<Integer> a = new Pair<Integer>(Label.temp, 100);
 		
 		System.out.println(a);
 		
 		a.update(120);
 		
 		System.out.println(a);
 	}
 }
