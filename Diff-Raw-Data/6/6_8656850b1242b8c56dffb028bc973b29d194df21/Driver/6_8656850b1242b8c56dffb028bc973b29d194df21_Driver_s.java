 package edu.victone.scrabblah;
 
 import edu.victone.scrabblah.ui.IOAdapter;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vwilson
  * Date: 9/11/13
  * Time: 2:53 PM
  */
 
 public class Driver {
     public static void main(String... args) {
        System.out.println("Driver has begun to drive...");
         IOAdapter ioa = null;
 
         //take args from CLI
         if (args.length == 0) {
             ioa = new IOAdapter(System.in, System.out);
 
         }
         ioa.listen();
     }
 }
