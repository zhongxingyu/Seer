 package tutorial_syntax;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Aristide
  * Date: 6/8/13
  */
 public class Loops {
     public static void main(String[] args){
         // Test for each
         String[] list = {"what", "about", "now"};
         listEntries(list);
         System.out.println();
 
         // Test for
         listNumsUpTo(5);
         System.out.println();
 
         // Test while
         listNumbersLessThan(5);
         System.out.println();
 
         // Test Do
        listIntegersLessThan(10);
 
     }
 
     public static void listEntries(String[] entries){
         for(String entry : entries){
             System.out.println(entry);
         }
     }
 
     public static void listNumsUpTo(int max){
         for(int i = 0; i <= max; i++){
             System.out.println("Number: " + i);
         }
     }
 
     public static void listNumbersLessThan(int max){
         int i = 0;
         while (i < max){
             System.out.println("Number: " + i);
             i++;
         }
     }
 
     public static void listIntegersLessThan(int max){
         int i = 0;
         do{
            System.out.println("Number: " + i);
             i++;
         } while ( i < max);
     }
 }
