 package jscl.text;
 
 import jscl.math.Generic;
 import org.apache.commons.lang.mutable.MutableInt;
 import org.jetbrains.annotations.NotNull;
 
 /**
  * User: serso
  * Date: 10/27/11
  * Time: 2:45 PM
  */
 class Exponent implements Parser<Generic> {
 
 	public static final Parser<Generic> parser = new Exponent();
 
 	private Exponent() {
 	}
 
 	public Generic parse(@NotNull String string, @NotNull MutableInt position) throws ParseException {
 		int pos0 = position.intValue();
 
 		boolean sign = MinusParser.parser.parse(string, position).isSign();
 
 		Generic result;
 
 		try {
 			result = (Generic) UnsignedExponent.parser.parse(string, position);
		} finally {
 			position.setValue(pos0);
 		}
 
 		return sign ? result.negate() : result;
 	}
 }
