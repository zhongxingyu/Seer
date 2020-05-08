 package kata;
 
 public class Calculator {
     public <T extends Number> float add(T a, T b) {
         return ((Number) a).floatValue() + ((Number) b).floatValue();
     }
 }
