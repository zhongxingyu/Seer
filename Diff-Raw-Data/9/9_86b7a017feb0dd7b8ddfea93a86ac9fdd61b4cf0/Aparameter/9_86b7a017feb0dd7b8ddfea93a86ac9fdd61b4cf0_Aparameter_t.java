 package net.sf.laja.parser.cdd.creator;
 
 public class Aparameter implements CreatorParser.IAparameter {
     public String attribute;
     public String nextAttribute = "";
     public String value = "";
     public String method = "";
     public String methodSignature = "";
 
     public void addParameterAttr(CreatorParser.IAparameterAttr iaparameterAttr) {
         AparameterAttr attr = (AparameterAttr)iaparameterAttr;
         String value = convertValue(attr.value);
 
         String variable = attr.variable;
         if (variable.equals("attribute")) {
             attribute = value;
             if (method.isEmpty()) {
                 method = attribute;
             }
         } else if (variable.equals("nextAttribute")) {
             nextAttribute = value;
         } else if (variable.equals("value")) {
             this.value = value;
         } else if (variable.equals("method")) {
             method = value;
         } else if (variable.equals("methodSignature")) {
             methodSignature = value;
         }
     }
 
     String convertValue(String value) {
         if (value.endsWith(";")) {
             value = value.substring(0, value.length() - 1).trim();
         }
         if (value.startsWith("\"")) {
             value = value.substring(1, value.length()-1);
         }
         value = value.replaceAll("\\\\", "");
 
         return value;
     }
 
     public boolean isParameterMethod() {
         return attribute.equals(method);
     }
 
     public boolean isLastAttribute() {
        return nextAttribute.equals("*");
     }
 
     public boolean hasNext() {
         return !nextAttribute.isEmpty() && !isLastAttribute();
     }
 
     public boolean useNext() {
         return nextAttribute.isEmpty();
     }
 
     public String signatureArguments() {
         String[] values = methodSignature.split("\\s");
 
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
                 "attribute='" + attribute + '\'' +
                 ", nextAttribute='" + nextAttribute + '\'' +
                 ", value='" + value + '\'' +
                 ", method='" + method + '\'' +
                 ", methodSignature='" + methodSignature + '\'' +
                 '}';
     }
 }
