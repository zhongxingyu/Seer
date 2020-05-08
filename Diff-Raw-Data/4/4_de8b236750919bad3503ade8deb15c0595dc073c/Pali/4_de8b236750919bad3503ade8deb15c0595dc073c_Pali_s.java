 package ro.inf.p2.uebung03;
 
 /**
  * Created with IntelliJ IDEA.
  * User: felix
  * Date: 4/10/13
  * Time: 2:14 PM
  * Palindrom
  */
 public class Pali {
     public static String filter(String s) {
         s = s.toLowerCase();
         s = s.replaceAll("[^a-z]","");
 
         return s;
     }
 
     public static boolean isPalindrome(String s) {
         s = filter(s);
 
        String s2 = new StringBuffer(s).reverse().toString();

        return s.equals(s2);
     }
 }
