 public class Util {
    void ensure(boolean condition) {
         if (! condition) {
             throw new RuntimeError();
         }
     }
 
    void ensure(boolean condition, String message) {
         if (! condition) {
             throw new RuntimeError(message);
         }
     }
 }
