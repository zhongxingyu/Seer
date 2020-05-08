 public class Util {
    static void ensure(boolean condition) {
         if (! condition) {
             throw new RuntimeError();
         }
     }
 
    static void ensure(boolean condition, String message) {
         if (! condition) {
             throw new RuntimeError(message);
         }
     }
 }
