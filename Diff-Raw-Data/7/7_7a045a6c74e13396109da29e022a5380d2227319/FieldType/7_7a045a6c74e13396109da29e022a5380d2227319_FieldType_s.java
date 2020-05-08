 package models.management;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * The different types of fields that are by default supported by the ManageableModel to be
  * generated in the default form view.
  * @author Ruben Taelman
  *
  */
 public enum FieldType {
     TEXT,
     CHECKBOX,
    LIST;
 
     private static final Map<Class<?>, FieldType> TYPES = new HashMap<Class<?>, FieldType>();
     static {
         TYPES.put(boolean.class, CHECKBOX);
         TYPES.put(Listable.class, LIST);
     }
 
     /**
      * Get the FieldType for a certain class
      * @param cls class to get the FieldType for
      * @return a FieldType for the given class
      */
     public static FieldType getType(Class<?> cls) {
         // Make sure we have the highest possible class type
         if(Listable.class.isAssignableFrom(cls))
             cls = Listable.class;
 
         // Determine the type
         FieldType type = TYPES.get(cls);
         if(type == null) type = TEXT;
 
         return type;
     }
 }
