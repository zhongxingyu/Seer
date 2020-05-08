 package http.header;
 
 import http.attribute.Attribute;
 
 import java.util.Collection;
 
 /**
  * Represents an HTTP header which can have a name and a single or multiple values. The type of the value can be defined
  * on instantiation.
  *
  * @author Karl Bennett
  */
 public class Header<T> extends Attribute<T> {
 
     public static final String OPERATOR = ": ";
     public static final String DELIMITER = "\n";
 
 
     /**
      * Parse a {@link String} containing any number of "name: value" pairs into a collection of {@link Header}s. The
      * names and values must be delimited by an ": " string and each name/value pair must be delimited by an '\n'
      * character.
      * <p/>
      * Example:
      * <code>
      *  nameOne: valueOne
      *  nameTwo: valueTwo
      *  nameThree: valueThree
      * </code>
      * <p/>
      * Name value pairs that share the same name will be stored in the same {@code Header} instance.
      * <p/>
      * Example:
      * <code>
      *  List<Header> headers = new ArrayList(
      *      Header.parse("nameOne: valueOne\nnameOne: valueTwo\nnameThree: valueThree")
      *  );
      *  headers.size(); // 2
      *  headers.get(0).getValues(); // ["valueOne", "valueTwo"]
      *  headers.get(1).getValues(); // ["valueThree"]
      * </code>
      *
      * @param headers the string to parse.
      * @return the
      */
     public static Collection<Header<String>> parse(String headers) {
 
         return parse(new AttributeParser<Header<String>>(headers, OPERATOR, DELIMITER) {
 
             @Override
             protected Header<String> nextPair(String name, String value) {
 
                 return new Header<String>(name, value);
             }
         });
     }
 
     public static String toString(Collection<Header> headers) {
 
         return toString(headers, DELIMITER);
     }
 
 
     /**
     * Create a {@code Header} with a name and value.
      *
      * @param name  the name of the attribute.
      * @param value the single value for the attribute.
      */
     public Header(String name, T value) {
         super(name, value, OPERATOR);
     }
 }
