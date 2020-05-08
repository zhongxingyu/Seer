 package sandbox;
 
 public class Sleep {
     public static void main(String[] args) throws Exception {
        long sec = Long.MAX_VALUE;
         if (args.length != 0) {
             try {
                sec = Long.parseLong(args[0]);
             } catch (Exception ignore) { }
         }
        Thread.sleep(sec);
     }
 }
