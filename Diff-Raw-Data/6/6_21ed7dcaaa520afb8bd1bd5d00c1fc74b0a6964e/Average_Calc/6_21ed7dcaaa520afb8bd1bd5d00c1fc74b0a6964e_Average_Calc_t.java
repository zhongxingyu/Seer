 /*  Author: Kyle
 	Disc: Average_Calc :: finds the sum on num1, ... num5 and then displayes it to the user via. PrintLN.  
     Creation: Sep. 10, 2013
     Laste edit: 4:25 ET
 */
 import java.io.*;
 import java.util.*;
 public class Average_Calc
 {
 	public static void main (String args[])
 	{
 	//add floats, num1, ... num5, ADV, sum
 	float num1 = 0, num2 = 0,	num3 = 0, num4 = 0, num5 = 0, ADV = 0, sum = 0;
 	//add Scaner
 	Scanner keyboard = new Scanner (System.in);
 //
 	System.out.println("Welcome!\n"); 
 	System.out.println("Number 1: ");
    num1 = keyboard.nextFloat(); //get num1
     System.out.println("Number 2: ");
     num2 = keyboard.nextFloat(); //get num2
     System.out.println("Number 3: ");
     num3 = keyboard.nextFloat(); //get num 3
     System.out.println("Number 4: ");
     num4 = keyboard.nextFloat(); //get num 4
     System.out.println("Number 5: "); 
     num5 = keyboard.nextFloat(); //get num 5
     sum = num1 + num2 + num3 + num4 + num5; //set sum. add num1, to num5 together.
     ADV = sum / 5; //get ADV. Divide sum by 5 for the Adv.
     System.out.println("Sum was: "+sum +". Average was "+ADV +".");
 	} //end main
} //end class
