 package jscl.text;
 
 import jscl.math.Generic;
 import org.apache.commons.lang.mutable.MutableInt;
 import org.jetbrains.annotations.NotNull;
 
 /**
  * User: serso
  * Date: 10/27/11
  * Time: 2:45 PM
  */
 class PowerExponent implements Parser<Generic> {
 
 	public static final Parser<Generic> parser = new PowerExponent();
 
 	private PowerExponent() {
 	}
 
 	public Generic parse(@NotNull String string, @NotNull MutableInt position) throws ParseException {
 		int pos0 = position.intValue();
 
 		try {
 			PowerParser.parser.parse(string, position);
		} finally {
 			position.setValue(pos0);
 		}
 
 		Generic result;
 		try {
 			result = Exponent.parser.parse(string, position);
		} finally {
 			position.setValue(pos0);
 		}
 
 		return result;
 	}
 }
