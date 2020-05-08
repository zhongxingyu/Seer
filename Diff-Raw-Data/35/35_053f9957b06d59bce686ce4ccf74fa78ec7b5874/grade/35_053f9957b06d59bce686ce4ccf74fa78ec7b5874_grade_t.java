 import java.util.Scanner;
 public class grade {
     public static void main(String [] args) {
         Scanner reader = new Scanner(System.in);
         System.out.print("What numerical grade did you get? ");
         double grade = reader.nextDouble();
         
         
        if (grade >= 97.5) {
             System.out.println("A " + grade + " is an A+.");
         }
         
        else if (grade >= 94.5) {
             System.out.println("A " + grade + " is an A.");
         }
         
        else if (grade >= 91.5) {
             System.out.println("A " + grade + " is an A-.");
         }
         
         
        else if (grade >= 88.5) {
             System.out.println("A " + grade + " is a B+.");    
         }
         
        else if (grade >=85.5) {
             System.out.println("A " + grade + " is a B.");
         }
         
        else if (grade >= 82.5) {
             System.out.println("A " + grade + " is a B-.");
         }
         
        else if (grade >= 75.5) {
             System.out.println("A " + grade + " is a C+.");
         }
         
        else if (grade >= 71.5) {
             System.out.println("A " + grade + " is a C.");
         }
         
        else if (grade >= 69.5) {
             System.out.println("A " + grade + " is a C-.");
         }
         
        else if (grade >= 65.5) {
             System.out.println("A " + grade + " is a D+.");
         }
         
        else if (grade >= 61.5) {
             System.out.println("A " + grade + " is a D.");
         }
         
        else if (grade >= 59.5) {
             System.out.println("A " + grade + " is a D.");
     
         }
         
         else {
             System.out.println("A " + grade + " is an F.");
         }
     }
 }
