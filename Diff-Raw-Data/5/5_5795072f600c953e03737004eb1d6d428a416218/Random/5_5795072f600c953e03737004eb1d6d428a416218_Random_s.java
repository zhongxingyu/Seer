 import java.util.Scanner;
 
 /**
  * Created with IntelliJ IDEA.
  * User: abychko
  * Date: 04.09.13
  * Time: 19:35
  * To change this template use File | Settings | File Templates.
  */
 public class Random {
     public static  void main (String args[]){
         int myDigit = (int)( Math.random() * 100);
         int trycount = 0;
         String val;
        int input = -1;
         System.out.println("Enter your Integer");
         while (trycount < 8){
 
             try {
                 val = new Scanner(System.in).next();
                 input = Integer.valueOf(val);
            }catch (Throwable t){
                 System.out.println("Not a integer! " + t);
                 continue;
             }
             if (input == myDigit){
                 System.out.println("You win!");
                 System.exit(0);
             }
             if (input > myDigit){
                 System.out.println("It was less");
             }
             if(input < myDigit){
                 System.out.println("It was greater");
             }
 
             trycount++;
         }
         System.out.println("Game over, computer win! it was " + myDigit);
     }
 }
