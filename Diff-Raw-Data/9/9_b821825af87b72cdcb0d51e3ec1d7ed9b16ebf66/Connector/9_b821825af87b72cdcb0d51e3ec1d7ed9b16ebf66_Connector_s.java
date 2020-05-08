 public class Connector {
 
 	// Implement an immutable connector that connects two points on the game board.
 	// Invariant: 1 <= myPoint1 < myPoint2 <= 6.
 
 	private int myPoint1, myPoint2;
 
 	public Connector (int p1, int p2) {
 		if (p1 < p2) {
 			myPoint1 = p1;
 			myPoint2 = p2;
 		} else {
 			myPoint1 = p2;
 			myPoint2 = p1;
 		}
 	}
 
 	public int endPt1 ( ) {
 		return myPoint1;
 	}
 
 	public int endPt2 ( ) {
 		return myPoint2;
 	}
 
 	public boolean equals (Object obj) {
 		Connector e = (Connector) obj;
 		return (e.myPoint1 == myPoint1 && e.myPoint2 == myPoint2);
 	}
 
 	public String toString ( ) {
 		return "" + myPoint1 + myPoint2;
 	}
 
 	// Format of a connector is endPoint1 + endPoint2 (+ means string concatenation),
 	// possibly surrounded by white space. Each endpoint is a digit between
 	// 1 and 6, inclusive; moreover, the endpoints aren't identical.
 	// If the contents of the given string is correctly formatted,
 	// return the corresponding connector.  Otherwise, throw IllegalFormatException.
<<<<<<< HEAD
	public static Connector toConnector (String s) throws IllegalFormatException {
		if(s == null) 
			throw new IllegalFormatException("Null string");
		else if(s.length() <= 1) 
=======
 	public static Connector toConnector (String s) throws IllegalFormatException
 	{
 		if (s == null) 
 			throw new IllegalFormatException("Null string");
 		else if (s.length() <= 1) 
>>>>>>> a654bb77938ef03f5aa48e9106fa1ca00965bbc7
 			throw new IllegalFormatException("String with length <= 1");
 
 		int p1, p2;
 		p1 = p2 = 0;
 
 		boolean found_number = false;
 		int i, numcount;
 		i = numcount = 0;
 
 		while (i < s.length())
 		{
 			if(Character.isWhitespace(s.charAt(i)))
 				if(found_number) 
 					throw new IllegalFormatException("Whitespace between endpoints");
 			else
 			{
 				if(s.charAt(i) < '1' || s.charAt(i) > '6') 
 					throw new IllegalFormatException("Not an endpoint");
 				else
 				{
 					++numcount;
 					if(numcount == 1)
 					{ 
 						p1 = s.charAt(i) - '0'; 
 						found_number = true;
 					}
 					else if(numcount == 2) 
 					{
 						p2 = s.charAt(i) - '0';
 						found_number = false;
 					}
 					else 
 						throw new IllegalFormatException("More than 2 endpoints found"); //more than 2 endpoints
 				}
 			}
 			++i;
 		}
 
 
 		if(p1 != p2) 
 			return new Connector(p1,p2);
 		else 
 			throw new IllegalFormatException("Endpoints are the same");
 	}
 }
