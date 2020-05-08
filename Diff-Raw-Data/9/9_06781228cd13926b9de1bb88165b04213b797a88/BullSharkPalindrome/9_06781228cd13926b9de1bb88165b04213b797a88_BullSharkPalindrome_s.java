 package ch05.pp5_4;
 
 /**
  *
  * @author Christopher Lemire <christopher.lemire@gmail.com>
  */
 public class BullSharkPalindrome {
   /**
    * This recursive method takes a word(s) and tests for palindrome.
    * @param words
    * The words to test for palindrome
    * @return
    * True if palindrome, false otherwise
    */
  public boolean palidrome(String words) {
     if(words.length() <= 1) {
       return true;
     }
 
     if(words.charAt(0) == words.charAt(words.length()-1)) {
       return palidrome(words.substring(1, words.length()-1));
     } else {
       return false;
     }
   }
 }
