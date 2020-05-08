 package day11;
 
 /**
  *
  * @author tom
  */
 public class RestrictedSmartPhone extends SmartPhone {
 
     public RestrictedSmartPhone(String brand) {
         super(brand);
     }
     
     @Override
     // Not possible because it would mean giving the method a less restrictive 
     // access privilege (protected is more restrictive than private).
     private void playGame(String game) {
        // Making method protected has no effect because all the classes
        // are in the same package anyway.
         System.out.println("Playing! " + game);
     }
     
 }
