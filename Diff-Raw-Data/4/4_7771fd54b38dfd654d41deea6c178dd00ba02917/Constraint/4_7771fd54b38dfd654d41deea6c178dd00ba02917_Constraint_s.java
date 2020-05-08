 import java.io.* ;
 import java.util.* ;
 import java.lang.* ;
 
 
 public class Constraint {
 	// first operand
 	private String first ;
 	// second operand
 	private String second ;
 	// relation between operands
 	private String op ;
 	// create a constraint from a string
 	// such as "a > b"
 	public Constraint ( String s )
 	{
 
 		if ( s.indexOf ( ">=" ) > 0 )
 			op = ">=" ;
 		if ( s.indexOf ( "<=" ) > 0 )
 			op = "<=" ;
		if ( s.indexOf ( "<=" ) > 0 )
			op = "<=" ;
 		if ( s.indexOf ( "=<" ) > 0 )
 			op = "=<" ;
 		if ( s.indexOf ( "!=" ) > 0 )
 			op = "!=" ;
 		if ( s.indexOf ( ">=" ) == -1 &&
 				 s.indexOf ( "<=" ) == -1 &&
 				 s.indexOf ( "<=" ) == -1 &&
 				 s.indexOf ( "=<" ) == -1 &&
 				 s.indexOf ( "!=" ) == -1 )
 		{
 			if ( s.indexOf ( "<" ) > 0 )
 				op = "<" ;
 			if ( s.indexOf ( "=" ) > 0 )
 				op = "=" ;
 			if ( s.indexOf ( ">" ) > 0 )
 				op = ">" ;
 		}
 		StringTokenizer tok = new StringTokenizer ( s, " <>=!" ) ;
 		first  = tok.nextToken () ;
 		second = tok.nextToken () ;
 
 	}
 
 	// return the first operand
 	public String getFirst ()
 	{
 		return first ;
 	}
 
 	// return the second operand
 	public String getSecond ()
 	{
 		return second ;
 	}
 
 	// return the relation
 	public String getOperation ()
 	{
 		return op ;
 	}
 
 	public String toString ()
 	{
 		return "Constraint: " + first + " " + op + " " + second ;
 	}
 }
