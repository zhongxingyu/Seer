 package mvhsbandinventory;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Defines the data related to an instrument stored in the flat-file database.
  *
  * @author nicholson
  * @author jonathan
  */
 public class Instrument
 {
     private Map properties = new HashMap();
 
     public static String[] attributes = 
 		{ 
 			"Name", "Brand", "Serial", "Rank", "Value", "Status", "Notes", 
 			"Ligature", "Mouthpiece", "MouthpieceCap", "Bow", "History", 
 			"NeckStrap"
         };
     
     public static List attributeList = Arrays.asList(attributes);
     public static int attributesLength = attributes.length;
 
     /**
      * Constructor for an instrument without any properties.
      */
     public Instrument () {}
 
     /**
      * Constructor for an instrument contructed from the data in an array.  The
      * values in the array must be arranged in an order corresponding to the
      * static Instruments.attributes array.
      *
      * @param properties
      * @throws Exception
      */
     public Instrument (Object[] properties) throws Exception {
         for (int i = 0; i < attributesLength; i++) {
             set(attributes[i], properties[i]);
         }
     }
 
     /**
      * An accessor that sets the property named by the attribute argument to
      * the value passed in via the value argument.  The attribute must be
      * contained in the Instruments.attributes array.  If it is not, an
      * exception will be thrown.
      *
      * @param attribute
      * @param value
      * @throws Exception
      */
     public void set (String attribute, Object value) throws Exception
     {
         // Prevent people from storing arbitrary values in the instrument
        if (!attributeList.contains(attribute))
         {
             throw new Exception("Illegal attribute name.");
         }
         
         properties.put(attribute, value);
     }
 
     /**
      * An accessor that retrieves the value of the property named by the
      * attribute argument.
      *
      * @param attribute
      * @return value
      */
     public Object get (String attribute)
     {
         return properties.get(attribute);
     }
 }
