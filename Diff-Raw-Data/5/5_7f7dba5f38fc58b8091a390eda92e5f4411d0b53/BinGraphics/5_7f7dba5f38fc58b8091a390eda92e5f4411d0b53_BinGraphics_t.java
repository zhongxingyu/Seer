 package DeviceGraphics;
 
 import Networking.Request;
 import Utils.Location;
 import agent.data.*;
 
 public class BinGraphics implements DeviceGraphics  {
 	
 	private PartGraphics part; // Type of part found in bin
 	//private int partNumber; // Number of parts in bin
 	private Location binLocation;
 	private Location initialLocation;
 	private boolean isFull;
 	private Bin bin;
 	private int binNum;
 	
 	// Constructor
 	/*public BinGraphics (PartGraphics parts, int partNum, Bin b) {
 		part = parts;
 		partNumber = partNum;
 		isFull = true;
 		bin = b;
 	}*/
 	
 	public BinGraphics (Bin b, int i, PartGraphics part) {
 		isFull = true;
 		bin = b;
 		binNum = i;
 		this.part=part;
		if (binNum < 8)
			initialLocation = new Location(1005, 30 + binNum*75);
		else
			initialLocation = new Location(1045, 30 + (binNum - 8)*75);
 		binLocation= new Location (initialLocation);
 	}
 	
 	/**
 	 * Used in order to receive parts from a feeder's purge
 	 * 
 	 * @param parts - Part type
 	 * @param partNum - Number of parts
 	 */
 	public void receiveParts(PartGraphics parts, int partNum) {
 		part = parts;
 		//partNumber = partNum;
 	}
 	
 	
 	public PartGraphics getPart() {
 		return part;
 	}
 	
 	
 	/*public int getQuantity() {
 		return partNumber;
 	}*/
 	
 	/**
 	 * Empties out the bin during purge
 	 */
 	public void setEmpty() {
 		//partNumber = 0;
 		part = null;
 	}
 	
 	
 	/*public void setLocation(Location newLocation) {
 		binLocation = newLocation;
 	}*/
 	
 	
 	/*public Location getLocation() {
 		return binLocation;
 	}*/
 
 	public void setFull(boolean f) {
 		isFull = f;
 	}
 	
 	public boolean getFull() {
 		return isFull;
 	}
 	
 	public Bin getBin() {
 		return bin;
 	}
 	
 	public void setLocation(Location newLocation) {
 		binLocation = new Location (newLocation);
 	}
 
 	public Location getLocation() {
 		return binLocation;
 	}
 	
 	public Location getInitialLocation() {
 		return initialLocation;
 	}
 	
 	@Override
 	public void receiveData(Request req) {
 		// TODO Auto-generated method stub
 		
 	}
 }
