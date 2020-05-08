 package jscl.text;
 
 import jscl.math.Generic;
 import jscl.math.GenericVariable;
 import jscl.math.JSCLInteger;
 import jscl.math.NotIntegerException;
 import jscl.math.function.Pow;
 import org.apache.commons.lang.mutable.MutableInt;
 import org.jetbrains.annotations.NotNull;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ListIterator;
 
 /**
  * User: serso
  * Date: 10/27/11
  * Time: 2:45 PM
  */
 class UnsignedFactor implements Parser {
 	public static final Parser parser = new UnsignedFactor();
 
 	private UnsignedFactor() {
 	}
 
 	public Object parse(@NotNull String string, @NotNull MutableInt position) throws ParseException {
 		final List list = new ArrayList();
 
 		Generic generic = (Generic) UnsignedExponent.parser.parse(string, position);
 
 		list.add(generic);
 
 		while (true) {
 			try {
 				generic = (Generic) PowerExponent.parser.parse(string, position);
 				list.add(generic);
 			} catch (ParseException e) {
 				break;
 			}
 		}
 		ListIterator it = list.listIterator(list.size());
 		generic = (Generic) it.previous();
 		while (it.hasPrevious()) {
 			Generic b = (Generic) it.previous();
 			try {
 				int c = generic.integerValue().intValue();
				if (c < 0) generic = new Pow(GenericVariable.content(b, true), JSCLInteger.valueOf(c)).expressionValue();
				else generic = b.pow(c);
 			} catch (NotIntegerException e) {
 				generic = new Pow(GenericVariable.content(b, true), GenericVariable.content(generic, true)).expressionValue();
 			}
 		}
 
 		return generic;
 	}
 }
