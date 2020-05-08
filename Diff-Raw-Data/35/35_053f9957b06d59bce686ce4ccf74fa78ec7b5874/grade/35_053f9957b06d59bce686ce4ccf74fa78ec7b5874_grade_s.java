 import java.util.Scanner;
 public class grade {
     public static void main(String [] args) {
         Scanner reader = new Scanner(System.in);
         System.out.print("What numerical grade did you get? ");
         double grade = reader.nextDouble();
         
         
        if (x >= 97.5) {
             System.out.println("A " + grade + " is an A+.");
         }
         
        else if (x >= 94.5) {
             System.out.println("A " + grade + " is an A.");
         }
         
        else if (x >= 91.5) {
             System.out.println("A " + grade + " is an A-.");
         }
         
         
        else if (x >= 88.5) {
             System.out.println("A " + grade + " is a B+.");    
         }
         
        else if (x >=85.5) {
             System.out.println("A " + grade + " is a B.");
         }
         
        else if (x >= 82.5) {
             System.out.println("A " + grade + " is a B-.");
         }
         
        else if (x >= 75.5) {
             System.out.println("A " + grade + " is a C+.");
         }
         
        else if (x >= 71.5) {
             System.out.println("A " + grade + " is a C.");
         }
         
        else if (x >= 69.5) {
             System.out.println("A " + grade + " is a C-.");
         }
         
        else if (x >= 65.5) {
             System.out.println("A " + grade + " is a D+.");
         }
         
        else if (x >= 61.5) {
             System.out.println("A " + grade + " is a D.");
         }
         
        else if (x >= 59.5) {
             System.out.println("A " + grade + " is a D.");
     
         }
         
         else {
             System.out.println("A " + grade + " is an F.");
         }
     }
 }
