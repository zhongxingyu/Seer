
 import java.util.Collections;
 import java.util.List;
 
 public class StringVariant extends AbstractVariant
 {
 	private String value;
 	
 	public StringVariant(String value)
 	{
 		super();
 		this.value = value;
 		this.type = Type.STRING;
 	}
 	
 	public static StringVariant create(String value)
 	{
 		return new StringVariant(value);
 	}
 	
 	public String stringVal()
 	{
 		return value;
 	}
 
 	/**
 	 * false when "", or "0", or "0.0"
 	 */
 	public boolean boolVal()
 	{
		return value.length() > 0
 			|| typeCast(Type.DOUBLE).longVal() != 0;
 	}
 
 	public List<Variant> arrayVal() 
 	{
 		return Collections.<Variant>singletonList(this);
 	}
 
 	public double doubleVal()
 	{
 		try {
 			return Double.parseDouble(value);
 		} catch (NumberFormatException e) {}
 		return 0.0D;
 	}
 
 	public long longVal() 
 	{
 		try {
 			return Long.parseLong(value);
 		} catch (NumberFormatException e) {}
 		return 0L;
 	}
 	
 	/**
 	 * if the string is all digits, this will convert to a LongVariant
 	 * if the string represent a valid double, it convert to a DoubleVariant
 	 * otherwise, it will return a LongVariant with the value 0
 	 * 
 	 */
 	public Variant toNumeric()
 	{
 		try {
 			return new LongVariant(Long.parseLong(value));
 		} catch (NumberFormatException e) {}
 		
 		try {
 			return new DoubleVariant(Double.parseDouble(value));
 		} catch (NumberFormatException e) {}
 		
 		return new LongVariant(0L);
 	}
 
 }
