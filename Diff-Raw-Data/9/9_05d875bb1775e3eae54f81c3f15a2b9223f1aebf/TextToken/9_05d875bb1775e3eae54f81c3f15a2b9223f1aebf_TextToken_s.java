 
 @SuppressWarnings("unchecked")
public abstract class TextToken implements java.lang.Comparable {
	
	java.lang.String str;
 	java.lang.Integer freq;
 	static boolean isStrSorted;
 	
 	public int compareTo(Object o) throws java.lang.ClassCastException
 	{
 		if(!(o instanceof TextToken))
 		{
 			throw new java.lang.ClassCastException();
 		}
 		if(isStrSorted)
 		{
 			return str.compareTo(((TextToken) o).str);
 		}
 		return freq.compareTo(((TextToken) o).freq);
 		
 	}
 	
 	public static void setFreqSort()
 	{
 		isStrSorted = false;
 	}
 	
 	public static void setStrSort()
 	{
 		isStrSorted = true;
 	}
 }
