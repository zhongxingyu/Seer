 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.ac.ebi.resource;
 
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.prefs.Preferences;
 
 
 /**
  * Enumeration organises user and system preferences
  *
  * @author johnmay
  */
 public enum Preference {
 
     /*
      * TEST
      */
     LIST_TEST(Type.TEST, Access.USER, new ArrayList<String>()),
     /*
      * IO
      */
     BUFFER_SIZE(Type.IO, Access.USER, 2048),
     RECENT_FILES(Type.IO, Access.USER, ""),
     /*
      * Resource
      */
     CHEMICAL_IDENTIFIER_FORMAT(Type.IDENTIFIER, Access.USER, "M/%05d"),
     PROTEIN_IDENTIFIER_FORMAT(Type.IDENTIFIER, Access.USER, "P/%05d"),
     GENE_IDENTIFIER_FORMAT(Type.IDENTIFIER, Access.USER, "G/%05d"),
     REACTION_IDENTIFIER_FORMAT(Type.IDENTIFIER, Access.USER, "R/%d"),
     /*
      * Tool
      */
     CPLEX_PATH(Type.TOOL, Access.USER, ""),
     BLASTP_PATH(Type.TOOL, Access.USER, ""),
     BLASTP_VERSION(Type.TOOL, Access.USER, "");
 
     public static final String LIST_SEPARATOR_SPLIT = "(?<!\\\\);";
 
     public static final String LIST_SEPARATOR = ";";
 
     public static final String LIST_SEPARATOR_ESCAPED = "\\\\;";
 
     private String key;
 
     private Type type;
 
     private Access access;
 
     private Object defaultValue;
 
 
     private Preference(Type type, Access access, Object defaultValue) {
         this.type = type;
         this.access = access;
         this.defaultValue = defaultValue;
         this.key = type + "." + name();
     }
 
 
     /**
      * Access the string value of the preference
      *
      * @return
      */
     public String get() {
         return access.getPreferences().get(key, defaultValue.toString());
     }
 
 
     /**
      * Access a list of string for the preference
      *
      * @return
      */
     public List<String> getList() {
         String rawValue = get();
         List<String> values = new ArrayList<String>();
         for (String value : rawValue.split(LIST_SEPARATOR_SPLIT)) {
             values.add(value.replaceAll(LIST_SEPARATOR_ESCAPED, LIST_SEPARATOR));
         }
         return values;
     }
 
 
     /**
      * Access a integer value from the preference
      *
      * @return
      */
     public int getInteger() {
         if (!(defaultValue instanceof Integer)) {
             throw new InvalidParameterException("Preference " + key + " default value must be an integer");
         }
         return access.getPreferences().getInt(key, (Integer) defaultValue);
     }
 
 
     /**
      * Access a double value from the preference
      *
      * @return
      */
     public double getDouble() {
         if (!(defaultValue instanceof Double)) {
             throw new InvalidParameterException("Preference " + key + " default value must be an double");
         }
         return access.getPreferences().getDouble(key, (Double) defaultValue);
     }
 
 
     public void put(String value) {
         access.getPreferences().put(key, value);
     }
 
 
     /**
      * Puts a list into preferences
      *
      * @param values
      */
     public void putList(List<String> values) {
         StringBuilder sb = new StringBuilder(values.size() * 14);
         for (String value : values) {
             sb.append(value.replaceAll(LIST_SEPARATOR, LIST_SEPARATOR_ESCAPED));
            if (value.equals(values.get(values.size() - 1))) {
                 sb.append(LIST_SEPARATOR);
             }
         }
         put(sb.toString());
     }
 
 
     public void putInteger(int value) {
         access.getPreferences().putInt(key, value);
     }
 
 
     public void putDouble(int value) {
         access.getPreferences().putDouble(key, value);
     }
 
 
     private enum Access {
 
         USER(Preferences.userNodeForPackage(Preference.class)),
         SYSTEM(Preferences.systemNodeForPackage(Preference.class));
 
         private final Preferences preferences;
 
 
         private Access(Preferences preferences) {
             this.preferences = preferences;
         }
 
 
         public Preferences getPreferences() {
             return preferences;
         }
     };
 
 
     private enum Type {
 
         IO, IDENTIFIER, TOOL, TEST;
     };
 }
