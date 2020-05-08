 package http.parameter;
 
 import http.attribute.Attribute;
 
 import java.util.Collection;
 
 /**
  * Represents an HTTP parameter which can have a name and a single or multiple values. The type of the value can be
  * defined on instantiation.
  *
  * @author Karl Bennett
  */
 public class Parameter<T> extends Attribute<T> {
 
     public static final String OPERATOR = "=";
     public static final String DELIMITER = "&";
 
     /**
      * Parse a {@link String} containing any number of "name=value" pairs into a collection of {@link Parameter}s. The
      * names and values must be delimited by an '=' character and each name/value pair must be delimited by an '&'
      * character.
      * <p/>
      * Example:
      * <code>
      *  nameOne=valueOne&nameTwo=valueTwo&nameThree=valueThree
      * </code>
      * <p/>
      * Name value pairs that share the same name will be stored in the same {@code Parameter} instance.
      * <p/>
      * Example:
      * <code>
      *  List<Parameter> parameters = new ArrayList(
      *      Parameter.parse("nameOne=valueOne&nameOne=valueTwo&nameThree=valueThree")
      *  );
      *  parameters.size(); // 2
      *  parameters.get(0).getValues(); // ["valueOne", "valueTwo"]
      *  parameters.get(1).getValues(); // ["valueThree"]
      * </code>
      *
      * @param parameters the string to parse.
      * @return the
      */
     public static Collection<Parameter<String>> parse(String parameters) {
 
         return parse(new AttributeParser<Parameter<String>>(parameters, OPERATOR, DELIMITER) {
 
             @Override
             protected Parameter<String> nextPair(String name, String value) {
 
                 return new Parameter<String>(name, value);
             }
         });
     }
 
     public static <T> String toString(Collection<Parameter<T>> parameters) {
 
         return toString(parameters, DELIMITER);
     }
 
 
     /**
     * Create an {@code Parameter} with a name and a single value.
      *
      * @param name  the name of the attribute.
      * @param value the single value for the attribute.
      */
     public Parameter(String name, T value) {
         super(name, value, OPERATOR);
     }
 }
