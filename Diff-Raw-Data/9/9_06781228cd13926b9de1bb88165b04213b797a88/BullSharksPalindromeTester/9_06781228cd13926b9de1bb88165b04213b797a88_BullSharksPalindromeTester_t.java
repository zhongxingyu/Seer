 /*
  * BullShark's version of Palindrome Tester
  */
 package ch05.pp5_4;
 
 import java.util.Scanner;
 
 /**
  *
  * @author Christopher Lemire <christopher.lemire@gmail.com>
  */
 public class BullSharksPalindromeTester {
 
   /*
    * No error checking implemented
    * If you want error checking, implement it
    */
   public static void main(String[] args) {
     Scanner scan = new Scanner(System.in);
     System.out.println("Testing for Palindrome. Please enter your word or phrase.");
     System.out.print("Word or phrase: ");
     String words = scan.nextLine();
 
     // Initiate recursion
     BullSharkPalindrome bsp = new BullSharkPalindrome();
    boolean isPalidrome = bsp.palindrome(words.toLowerCase().replaceAll("[^A-Za-z0-9]", ""));
     if(isPalidrome) {
       System.out.println("\"" + words + "\"" + " IS a palindrome!");
     } else {
       System.out.println("\"" + words + "\"" + " IS NOT a palindrome!");
     }
   }
 }
