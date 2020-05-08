 package org.pillarone.riskanalytics.core.components;
 
 /**
  * Contains utility methods for conversion between model and display names. Usage examples can be found in the
  * corresponding test class {@link ComponentUtilsTests}.
  *
  * @author stefan.kunz (at) intuitive-collaboration (dot) com
  */
 public class ComponentUtils {
 
     public static final String PATH_SEPARATOR = ":";
     public static final String SUB = "sub";
     public static final String PARM = "parm";
     public static final String OUT = "out";
 
     public static String getNormalizedPath(String path, String pathSeparator) {
         String[] pathElements = path.split(PATH_SEPARATOR);
         StringBuilder normalizedPath = new StringBuilder();
         for (String pathElement : pathElements) {
             if (pathElement == null) {
             }
             else {
                 normalizedPath.append(pathSeparator);
                 normalizedPath.append(removeNamingConventions(pathElement));
             }
         }
         return insertBlanks(normalizedPath.substring(pathSeparator.length()));
     }
 
     public static String getModelName(String displayName, String prefix) {
         StringBuffer componentName = new StringBuffer(prefix);
         String[] splittedComponentName = displayName.split(" ");
         for (String word : splittedComponentName) {
             componentName.append(word.substring(0, 1).toUpperCase());
             componentName.append(word.substring(1));
         }
         return componentName.toString();
     }
 
     public static String getNormalizedName(String componentName) {
         return insertBlanks(removeNamingConventions(componentName));
     }
 
     public static String removeNamingConventions(String componentName) {
         if (componentName == null) {
             return null;
         }
         else if (componentName.startsWith(SUB)) {
             return componentName.substring(3);
         }
         else if (componentName.startsWith(PARM)) {
             return componentName.substring(4);
         }
         else if (componentName.startsWith(OUT)) {
             return componentName.substring(3);
         }
         return componentName;
     }
 
     public static String getComponentNormalizedName(String path) {
         String[] subComponents = path.split(PATH_SEPARATOR.concat(SUB));
         String componentName = subComponents[subComponents.length - 1];
         return insertBlanks(componentName);
     }
 
     public static String insertBlanks(String componentName) {
         if (componentName == null) {
             return null;
         }
         StringBuffer displayNameBuffer = new StringBuffer();
 
         int index = 0;
         for (char c : componentName.toCharArray()) {
             if (Character.isUpperCase(c) && index == 0) {
                 displayNameBuffer.append(Character.toLowerCase(c));
             } else if (Character.isUpperCase(c)) {
                 displayNameBuffer.append(" ").append(Character.toLowerCase(c));
             } else {
                 displayNameBuffer.append(c);
             }
             index++;
         }
         return displayNameBuffer.toString();
     }
 }
