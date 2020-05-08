 /**
  * A simple java calculator.
  * @author Dakota Strickner (computerxpds)
  * @version 1
 * @build 13
  * This very arcain and simple calculator will be able to do 2 variable operations. 
  */
 //import
 import java.util.*;
 //define class
 public class simplecalc{
 
     static Scanner console = new Scanner(System.in);
     //main string
     public static void main(String[] args){
         boolean go = true;
         {
            System.out.println("This is my java calculator!");
            System.out.println("Enter a, b, c, or d, a is addition b is subtraction c is multiplication and d is division");
             
             Scanner scan = new Scanner(System.in);
             Scanner scan1 = new Scanner(System.in);
             
             
             String action = scan.nextLine();
             
             if("a".equals(action))
             {
                 System.out.println("Now type in the first number you would like to add.");
                 int add1 = scan.nextInt();
                 System.out.println("Now type the second number.");
                 int add2 = scan.nextInt();
                 int add3 = add1 + add2;
                 System.out.println(add1 + " added to " + add2 + " equals " + add3 + "!");
             }
             if("b".equals(action))
             {
                 System.out.println("Now type in the first number you would like to subtract");
                 int sub1 = scan.nextInt();
                 System.out.println("Now type the second number.");
                 int sub2 = scan.nextInt();
                int sub3 = sub1 - sub2;
                 System.out.println(sub1 + " subtracted from " + sub2 + " equals " + sub3 + "!");
             }
            if("c".equals(action))
            {
                System.out.println("Now type in the first number you would like to multiply");
                int mult1 = scan.nextInt();
                System.out.println("Now type the second number");
                int mult2 = scan.nextInt();
                int mult3 = mult1 * mult2;
                System.out.println("equals " + mult3);
            }
            if("d".equals(action))
            {
                System.out.println("Now type the first number you would like to divide");
                int div1 = scan.nextInt();
                System.out.println("Now type the second number");
                int div2 = scan.nextInt();
                int div3 = div1 / div2;
                System.out.println("Equals " + div3);
            }
     }
 }
}
