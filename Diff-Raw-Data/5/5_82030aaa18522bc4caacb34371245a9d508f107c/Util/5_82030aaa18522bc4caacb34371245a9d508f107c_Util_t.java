 public class Util {
     static void ensure(boolean condition) {
         if (! condition) {
            throw new RuntimeException();
         }
     }
 
     static void ensure(boolean condition, String message) {
         if (! condition) {
            throw new RuntimeException(message);
         }
     }
 }
