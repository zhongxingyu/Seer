 package mvhsbandinventory;
 
 import java.util.Comparator;
 
 class InstrumentAttributeComparator implements Comparator
 {
 	private String compareBy;
 	private boolean flip;
 	
 	public InstrumentAttributeComparator (String attribute, boolean ascending) 
 		throws Exception
 	{
 		// Check to make sure that the attribute that was passed in to compare
 		// with is actually valid
 		if (!Instrument.attributeList.contains(attribute))
 		{
 			throw new Exception("Illegal attribute name.");
 		}
 		
 		// Save that attribute value for later use in the compare function
 		compareBy = attribute;
 		
 		// Determine whether we will need to flip the result of the compare
 		// int function
 		flip = !ascending;
 	}
 	
 	public int compare (Object obj1, Object obj2)
 	{
 		// Cast the objects appropriately so that we can read the data stored
 		// in the instruments
 		Instrument ins1 = (Instrument) obj1;
 		Instrument ins2 = (Instrument) obj2;
 		
 		// Get the values that we're going to compare from the two Instruments
 		Object value1 = ins1.get(compareBy);
 		Object value2 = ins2.get(compareBy);
 		
 		// Check to make sure that we can actually compare the values that we 
 		// just pulled out (e.g. the values are not ArrayList objects).
 		if (value1 instanceof List || value2 instanceof List) {
			throws new Exception("Cannot compare the attributes specified.");
 		}
 		
 		// Cast both of these values as strings so that we can actually compare
 		// the two values
 		String str1 = (String) value1;
 		String str2 = (String) value2;
 		
 		// Compare the strings and return a valid comparison value based on 
 		// whether we're doing an ascending or descending sort
 		int result = str1.compareTo(str2);
 		return (flip == true) ? result * -1 : result;
 	}
 }
