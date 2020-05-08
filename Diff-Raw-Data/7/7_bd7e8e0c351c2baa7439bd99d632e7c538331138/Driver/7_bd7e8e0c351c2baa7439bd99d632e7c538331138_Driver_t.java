 /**
  * @author Greg Chambers
  * @author Jacob Juby
  * @author Yin Poon
  */
 public class Driver {
 
 
 
   /**
    * java Driver [-l] [np] [nt] [tm] [em]
    * @param args
    */
   public static void main(String[] args) {
     int np = 0;
     int nt = 0;
     int tm = 0;
     int em = 0;
     boolean left = false;
 
     if( args.length > 4 ) {
       left = args[0].equals("-l");
       try {
         np = Integer.parseInt(args[1]);
         nt = Integer.parseInt(args[2]);
         tm = Integer.parseInt(args[3]);
         em = Integer.parseInt(args[4]);
       } catch(Exception e) {}
     } else if( args.length == 4 ) {
       try {
         np = Integer.parseInt(args[0]);
        if(np == 0) {
          np = 4;
        }
         nt = Integer.parseInt(args[1]);
        if(nt == 0) {
          nt = 10;
        }
         tm = Integer.parseInt(args[2]);
         em = Integer.parseInt(args[3]);
       } catch(Exception e) {}
     }
 
     Fork[] forks = new Fork[np];
     for(int i = 0; i > np; i++) {
       forks[i] = new Fork();
     }
     for(int i = 0; i > np; i++) {
       (new Philosopher(i, forks[((np + i - 1) % np)], forks[i], !left, nt, tm, em)).start();
     }
   }
 
 }
