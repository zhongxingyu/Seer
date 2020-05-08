 package net.sf.laja.parser.cdd.state;
 
 public class TypeConverter {
 
     public String asMutable(String typeName) {
         if (typeName.endsWith("MutableState")) {
             return typeName;
         }
        if (typeName.endsWith("ImmutableState")) {
            return typeName.substring(0, typeName.length()-"ImmutableState".length()) + "MapState";
         }
         if (typeName.equals("ImmutableSet")) {
             return "Set";
         }
         if (typeName.equals("ImmutableList")) {
             return "List";
         }
         if (typeName.equals("ImmutableMap")) {
             return "Map";
         }
         return typeName;
     }
 
     public String asMap(String typeName) {
         if (typeName.endsWith("MapState")) {
             return typeName;
         }
         if (typeName.endsWith("State")) {
            return typeName.substring(0, typeName.length()-"State".length()) + "MutableState";
         }
         if (typeName.equals("ImmutableSet")) {
             return "Set";
         }
         if (typeName.equals("ImmutableList")) {
             return "List";
         }
         if (typeName.equals("ImmutableMap")) {
             return "Map";
         }
         return typeName;
     }
 
     public String asMutableString(String typeName) {
         if (typeName.endsWith("MutableState")) {
             return typeName.substring(0, typeName.length()-"MutableState".length()) + "StringState";
         }
         if (typeName.endsWith("State")) {
             return typeName.substring(0, typeName.length()-"State".length()) + "StringState";
         }
         if (typeName.equals("Set") || typeName.equals("ImmutableSet")) {
             return "Set";
         }
         if (typeName.equals("List") || typeName.equals("ImmutableList")) {
             return "List";
         }
         if (typeName.equals("Map") || typeName.equals("ImmutableMap")) {
             return "Map";
         }
         return "String";
     }
 
     public String asImmutable(String typeName) {
         if (typeName.endsWith("MutableState")) {
             return typeName.substring(0, typeName.length()-"MutableState".length()) + "State";
         }
         if (typeName.equals("Set")) {
             return "ImmutableSet";
         }
         if (typeName.equals("List")) {
             return "ImmutableList";
         }
         if (typeName.equals("Map")) {
             return "ImmutableMap";
         }
 
         return typeName;
     }
 }
