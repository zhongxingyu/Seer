 package net.sf.laja.parser.cdd.creator;
 
 public class Aparameter implements CreatorParser.IAparameter {
     public String name;
     public String next = "";
     public String value = "";
     public String method = "";
     public String signature = "";
 
     public void addParameterAttr(CreatorParser.IAparameterAttr iaparameterAttr) {
         AparameterAttr attr = (AparameterAttr)iaparameterAttr;
         String value = convertValue(attr.value);
 
         String variable = attr.variable;
         if (variable.equals("name")) {
             name = value;
             if (method.isEmpty()) {
                 method = name;
             }
         } else if (variable.equals("next")) {
             next = value;
         } else if (variable.equals("value")) {
             this.value = value;
         } else if (variable.equals("method")) {
             method = value;
         } else if (variable.equals("signature")) {
             signature = value;
         }
     }
 
     String convertValue(String value) {
         if (value.endsWith(";")) {
             value = value.substring(0, value.length() - 1).trim();
         }
         if (value.startsWith("\"")) {
             value = value.substring(1, value.length()-1);
         } else if (value.endsWith("_")) {
             value = value.substring(0, value.length() - 1);
         }
         value = value.replaceAll("\\\\", "");
 
         return value;
     }
 
    public boolean isParameterMethod() {
        return name.equals(method);
    }

     public boolean isLastAttribute() {
         return next.equals("*");
     }
 
     public boolean hasNext() {
         return !next.isEmpty() && !isLastAttribute();
     }
 
     public boolean useNext() {
         return next.isEmpty();
     }
 
     public String signatureArguments() {
         String[] values = signature.split("\\s");
 
         String result = "";
         String separator = "";
         boolean isValue = false;
         for (String value : values) {
             if (value.equals(",")) {
                 continue;
             }
             if (isValue) {
                 if (value.endsWith(",")) {
                     value = value.substring(0, value.length()-1);
                 }
                 result += separator + value;
                 separator = ", ";
             }
             isValue = !isValue;
         }
 
         return result;
     }
 
     @Override
     public String toString() {
         return "Aparameter{" +
                 "name='" + name + '\'' +
                 ", next='" + next + '\'' +
                 ", value='" + value + '\'' +
                 ", method='" + method + '\'' +
                 ", signature='" + signature + '\'' +
                 '}';
     }
 }
