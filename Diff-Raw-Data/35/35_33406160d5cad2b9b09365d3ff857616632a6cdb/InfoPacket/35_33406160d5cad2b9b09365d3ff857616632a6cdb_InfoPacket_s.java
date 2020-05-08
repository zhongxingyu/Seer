 package anchovy;
 
 import java.util.ArrayList;
 
 public class InfoPacket {
 	/**
 	 * ArrayList containing the data == (string, value) pairs
 	 */
 	public ArrayList<Pair<?>> namedValues = new ArrayList<Pair<?>>();
 	
	@Override
 	/**
	 * @inheritDoc
 	 * Determine whether two infoPackets are essentially the same. That is they have the same info pairs
 	 * however not necessarily in the same order. 
 	 */
 	public boolean equals(Object _infoPacket) {
 		InfoPacket infoPacket = (InfoPacket) _infoPacket;
 		
 		if(this.namedValues.size() != infoPacket.namedValues.size())
 			return false;
 		
 		for(Pair<?> p : namedValues) {
 			if(infoPacket.hasPair(p) == false)
 				return false;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Determines whether the infoPacket contains a particular pair. 
 	 * @param p The pair that you are checking the existence of.
 	 * @return Whether the pair p was in the infoPacket.
 	 */
 	public Boolean hasPair(Pair<?> p) {
 		for(Pair<?> pair : namedValues) {
 			if(p.equals(pair))
 				return true;
 		}		
 		return false;
 	}
 	
 	
 	/**
 	 * Sand box:
 	 * <li>			feel free to use Pair constructs here
 	 * <li>			run in Debug (F11)
 	 * <li> 		also give Pair.Label, the enum, a try
 	 */
 	public static void main(String args[]) {
 		// adding an integer to the Info object
 		InfoPacket i = new InfoPacket();
 		i.namedValues.add(new Pair<Integer>(Pair.Label.RPMs, 1));
 		
 		// adding a double value
 		i.namedValues.add(new Pair<Double>(Pair.Label.coRL, 2.0));
 		
 		// adding a String 
 		i.namedValues.add(new Pair<String>(Pair.Label.pres, "potato"));
 		
 		// adding an ArrayList
 		i.namedValues.add(new Pair<ArrayList<Integer>>(Pair.Label.etc, new ArrayList<Integer>(3)));
 		
 		// defining an ArrayList with words and adding it to this Info object
 		ArrayList<String> words = new ArrayList<String>();
 		words.add("meow");
 		words.add("cow");
 		words.add("pixels");
 		Pair<ArrayList<String>> x = new Pair<ArrayList<String>>(Pair.Label.etc, words);
 		
 		i.namedValues.add(x);
 		
 		// Printing the namedValues ArrayList
 		System.out.println(i.namedValues);
 	}
 }
