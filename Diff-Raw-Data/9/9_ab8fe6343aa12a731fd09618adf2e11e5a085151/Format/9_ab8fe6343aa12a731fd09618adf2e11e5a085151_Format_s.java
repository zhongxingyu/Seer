 package java.text;
 
 import java.io.Serializable;
 import java.text.AttributedCharacterIterator.Attribute;
 
 public abstract class Format implements Serializable, Cloneable {
 	public static class Field extends Attribute {
 	    protected Field(String name) {super(null);}
 
 	}
     public Format() {}
 
     public Object clone() {
         return null;
     }
 
     /**
      * @throws IllegalArgumentException
      */
     public final String format(Object obj) {
         return null;
     }
 
     /**
      * @throws NullPointerException 
      * @throws IllegalArgumentException
      */
     public abstract StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos);
 
     /**
      * @throws NullPointerException 
     * @thtows IllegalArgumentException
      */
     public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
         return null;
     }
 
     /**
      * @throws NullPointerException
      */
     public abstract Object parseObject(String source, ParsePosition pos);
 
     /**
      * @throws ParseException
      */
     public Object parseObject(String source) throws ParseException {
         return null;
     }
 
 }
