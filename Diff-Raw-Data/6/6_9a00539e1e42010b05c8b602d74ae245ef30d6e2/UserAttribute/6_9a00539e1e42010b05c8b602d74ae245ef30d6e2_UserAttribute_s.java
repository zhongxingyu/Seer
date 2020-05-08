 package no.feide.moria.directory;
 
 import java.util.Vector;
 
 /**
  * ' Represents a user attribute and its list of values (if any).
  */
 public class UserAttribute {
 
     /** Internal representation of the attribute's name. */
     private String myName;
 
     /** Internal representation of the attribute's values. */
     private Vector myValues;
 
 
     /**
      * Constructor.
      * @param name
      *            The attribute name.
      * @param values
      *            A list of attribute values. A <code>null</code> value is
      *            accepted, as is an empty array.
      * @throws IllegalAttributeException
      *             If the attribute name is <code>null<code> or an empty string.
      */
     public UserAttribute(String name, String[] values)
     throws IllegalAttributeException {
 
         if ((name == null) || (name.length() == 0)) {
         // TODO: Add logging.
         throw new IllegalAttributeException("Attribute name cannot be NULL"); }

         myValues = new Vector();
         if (values != null)
             for (int i = 0; i < values.length; i++)
                 myValues.add(values[i]);
 
     }
 
 
     /**
      * Get the attribute's name.
      * @return A new string containing the attribute's name.
      */
     public String getName() {
 
         return new String(myName);
 
     }
 
 
     /**
      * Get the attribute's values.
      * @return The attribute values, if any. May be an empty array.
      */
     public String[] getValues() {
 
         return (String[]) myValues.toArray(new String[] {});
 
     }
 
 }
