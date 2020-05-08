 /** Prints sum of entered numbers on the console. */
 public class Sum2 {
     public static void main(String[] args) {
       int sum = 0;
       for (String i : args){
           String trimmed = i.trim();
          String[] splited = trimmed.split("\\s+");
          for (String j : splited){
              int parsed = Integer.parseInt(j);
              sum = sum + parsed;
           }
       }
       System.out.println(sum);
     }
 }
