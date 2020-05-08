 package views.formdata;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * A class containing the different telephone types.
  * 
  * @author AJ
  */
 public class TelephoneTypes {
 
  private static String [] phoneTypes = {"Mobile", "Home", "Work"};
   
   
   /**
    * Creates a Map of the different phone types that are available.
    * 
    * @return phones the map containing the phone types.
    */
   public static Map<String, Boolean> getTypes() {
     Map<String, Boolean> phones = new HashMap<>();
     for (int x = 0; x < phoneTypes.length; x++) {
       phones.put(phoneTypes[x], false);
     }
     return phones;
   }
   
   /**
    * Checks the map to see if the given phone type is in the list.
    * 
    * @param phoneType the phone type being passed
    * @return true if it contains the phoneType, false if otherwise
    */
   public static boolean isType(String phoneType) {
     return TelephoneTypes.getTypes().keySet().contains(phoneType);
   }
   
   /**
    * Returns the map with the given phoneType set to true.
    * 
    * @param phoneType the phone type.
    * @return phones the map with the given phone type's value set to true
    */
   public static Map<String, Boolean> getTypes(String phoneType) {
     Map<String, Boolean> phones = TelephoneTypes.getTypes();
     if (isType(phoneType)) {
       phones.put(phoneType, true);
     }
     return phones;
   }
 }
