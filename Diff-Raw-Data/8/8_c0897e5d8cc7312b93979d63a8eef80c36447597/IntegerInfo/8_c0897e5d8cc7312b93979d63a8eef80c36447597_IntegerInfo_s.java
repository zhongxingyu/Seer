 package ch05.pp5_3;
 
 import java.util.Scanner;
 
 public class IntegerInfo
 {
   public static void main(String[] args)
   {
     Scanner scan = new Scanner(System.in);
     
    int x, remainder, numZero, numOdd, numEven;
     numZero = numOdd = numEven = 0;
 
     String prompt = "Enter an integer: ";
 
     System.out.print(prompt);
     while(scan.hasNextInt()) {
      x = scan.nextInt();
       while(x > 0) {
         remainder = x % 10;
         if(remainder == 0) {
           numZero++;
         }
         else if(remainder % 2 == 0) {
           numEven++;
         }
         else {
           numOdd++;
         }
         x /= 10;
       }
      System.out.println("== INFO FOR NUMBER " + x + " ==");
       System.out.println("Zero digits: " + numZero);
       System.out.println("Even digits: " + numEven);
       System.out.println(" Odd digits: " + numOdd );
       System.out.print(prompt);
 
       // Do cleanup
       numZero = numEven = numOdd = 0;
     }
   }
 }
