 package sky.engine.util;
 
 /**
  * A Nullean object helps represent 3 different possible outcomes: False, True and Neutral (null).
  * 
  * When a Nullean is created, the Value will be either True or False, but unlike a Boolean,
  * a Nullean is nullable, allowing for a theoretical third possible outcome. Therefore, we
  * can have either:
  * 
  * 		Nullean nullean = new Nullean(true);
  * 		Nullean nullean = new Nullean(false);
  * 		Nullean nullean = null;
  * 
  * @author Matthew Kelly (Badgerati).
  *
  */
 public class Nullean
 {
 	/**
 	 * Current boolean value of this Nullean object.
 	 */
 	public boolean Value;
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/**
 	 * Create an instance of a new Nullean object.
 	 * 
 	 * @param bool - Boolean value to create Nullean from.
 	 */
 	public Nullean(boolean bool)
 	{
 		Value = bool;
 	}
 	
 	
 	/**
 	 * Create an instance of a new Nullean object.
 	 * 
	 * @param value - Integer value to create Nullean from. False if 0, True if 1, or False otherwise.
 	 */
 	public Nullean(int value)
 	{
 		switch (value)
 		{
 			case 0: this.Value = false; break;
 			case 1: this.Value = true; break;
 			default: this.Value = false; break;
 		}
 	}
 	
 	
 	/**
 	 * Create an instance of a new Nullean object.
 	 * 
 	 * @param string - String to create Nullean from. True if null or equal to "true",
 	 * 				   false otherwise. Case is ignored.
 	 */
 	public Nullean(String string)
 	{		
 		if (string.equalsIgnoreCase("TRUE") || string == null)
 			Value = true;
 		else
 			Value = false;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/**
 	 * Clones this Nullean object.
 	 */
 	public Nullean clone()
 	{
 		return new Nullean(Value);
 	}
 	
 	
 	
 	
 	
 	
 	
 
 	
 	
 	
 	/**
 	 * Does this Nullean object equal the passed object?
 	 * 
 	 * @param o - Object to testing equality.
 	 */
 	@Override
 	public boolean equals(Object o)
 	{
 		try
 		{
 			return (Value == ((Nullean)o).Value);
 		}
 		catch (Exception e)
 		{
 			return super.equals(o);
 		}
 	}
 	
 	
 	/**
 	 * Does this Nullean object equal the passed boolean?
 	 * 
 	 * @param bool - Boolean to test equality against.
 	 */
 	public boolean equals(boolean bool)
 	{
 		return (Value == bool);
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/**
 	 * Returns the hash code of this Nullean.
 	 */
 	@Override
 	public int hashCode()
 	{
 		return Value ? 1 : 0;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/**
 	 * Returns the string representation of this Nullean object.
 	 * 
 	 * @return String representing this Nullean.
 	 */
 	public String toString()
 	{
 		return Value ? "TRUE" : "FALSE";
 	}
 	
 	
 	
 	
 	
 
 }
