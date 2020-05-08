 package http.conversion;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * This is an abstract class that is implemented to test the generic conversion behaviour of the different
  * {@link http.conversion.Conversion}s when converting to a {@link String}.
  *
  * @author Karl Bennett
  */
 public abstract class AbstractConversionToStringTest<I> extends AbstractConversionTest<I, String> {
 
     private Conversion<I, String> conversion;
     private I input;
     private String output;
 
 
     /**
      * Create a new {@code AbstractConversionToStringTest} that will use the supplied conversion, expected output, and
      * input objects.
      *
      * @param conversion the conversion instance to use to carry out the conversion.
      * @param output     the expected output from the conversion.
      * @param input      the input to be converted.
      */
     protected AbstractConversionToStringTest(Conversion<I, String> conversion, String output, I input) {
         super(conversion, output, input);

        this.conversion = conversion;
        this.output = output;
        this.input = input;
     }
 
     @Test
     public void testConvertNull() throws Exception {
 
         assertEquals("a null value should be converted to an empty string.", "", conversion.convert(null));
     }
 }
