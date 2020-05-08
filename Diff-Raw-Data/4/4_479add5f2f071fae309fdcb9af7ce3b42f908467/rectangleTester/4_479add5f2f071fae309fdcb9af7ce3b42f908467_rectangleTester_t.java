 import java.util.Scanner;
 public class rectangleTester
 {
    public static void main(String [] args) {
     Scanner reader = new Scanner(System.in);
     
     //User-inputted length
     System.out.print("Enter the length: ");
     int length = reader.nextInt();
     
     //User-inputted width
     System.out.print("Enter the width: ");
     int width = reader.nextInt();
     
     rectangle x = new rectangle(length, width);
     
     //Perimeter
     System.out.println();
     System.out.print("Perimeter: ");
     System.out.println("2(" + x.getLength() + " + " + x.getWidth() +  ") = " + x.computePerimeter());
     
    //Area
    System.out.print("Area: ");
     System.out.println(x.getLength() + " x " + x.getWidth() + " = " +  x.computeArea());
     System.out.println();
     
     //Square Boolean
     if (x.squareTest()) {
         System.out.println("The rectangle is a square.");
     }
     else {
         System.out.println("The rectangle is not a square.");
     }
 
     System.out.println();
     
     //Draws the quadrilateral of *'s
     x.drawRectangle();
     System.out.println();
   }
 }
