 import java.io.Serializable;
<<<<<<< HEAD
 import java.util.ArrayList;
=======
>>>>>>> 6e8cb48993b8ad321ae408bf58c2db5e8df874b4
 import java.util.TreeMap;
 
 
 @SuppressWarnings("serial")
 public class Kit implements Serializable
 {
 	public static final int MIN_PARTS = 4;
 	public static final int MAX_PARTS = 8;
 
 	// Kit statuses
 	public static final int INCOMPLETE = 0; // all kits initialized to incomplete
 	public static final int INCORRECT = 1; // used when kit contains incorrect part in any location
 	public static final int COMPLETE = 2; // signifies completed kit with correct parts
 	
 	private int number;
 	public GUIKit link; //links to GUIKit counterpart
 	private String name, description;
	private TreeMap<Integer, Part> parts;
 	private int kitStatus; // Use kit statuses above
	public TreeMap<Integer, Part> parts = new TreeMap<Integer, Part>(); //ArrayList of parts contained in kit
 
 	/**
 	 * Constructor of empty kit for use on Kit Delivery Station
 	 * when conveyer brings in empty kits.
 	 * 
 	 */
 	public Kit()
 	{
 		name = "";
 		description = "";
 		number = 0;
 		kitStatus = INCOMPLETE;
 	}
 		
 	public Kit(String name, String description, int kitNumber)
 	{
 		this.name = name;
 		this.description = description;
 		this.number = kitNumber;
 		kitStatus = INCOMPLETE;
 	}
 
	public void addPart( int index, Part part ) {
		parts.put( index, part );
	}
	
	public Part removePart( int index ) {
		return parts.remove( index );
	}

 	public Part getPart(int index) {
 		return parts.get(index);
 	}
 	
 	public String getName()
 	{
 		return name;
 	}
 	
 	public String getDescription()
 	{
 		return description;
 	}
 	
 	public int getNumber()
 	{
 		return number;
 	}
<<<<<<< HEAD
 	
 	public int getStatus()
 	{
 		return kitStatus;
 	}
 	
 	public boolean addPart(int index, Part p)
 	{
 		if(parts.size() <= 8)
 		{
 			parts.put(index, p);
 			return true;
 		}
 		else
 		{
 			return false;
 		}	
 	}
 	public boolean removePart(int index)
 	{
 		if(parts.containsKey(index))
 		{
 			parts.remove(index);
 			return true;
 		}
 		return false;
 	}
 	
 	public void removeAllParts()
 	{
 		parts.clear();
 	}
 	
 	public void linkWithKit(GUIKit gui)
 	{
 		link = gui;
 	}
 	
 	public TreeMap<Integer, Part> getParts()
 	{
 		return parts;
 	}
=======
>>>>>>> 6e8cb48993b8ad321ae408bf58c2db5e8df874b4
 }
