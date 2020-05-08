 import java.util.*;
 public class GuessNumber {
     public static void main(String [] args) {
         int number = (int)(Math.random() * 100) + 1;
         int guess = -1;
         int count = 1;
         System.out.print("A number between 1-100 has been randomly selected, you have 7 attempts to guess this number\n" + count + ": ");
         while(guess != number && count < 8) {
             guess = new Scanner(System.in).nextInt();
             count++;
            if(count < 8){
                 if(guess < number) {
                     System.out.print("Higher\n" + count + ": ");
                 } else if(guess > number) {
                     System.out.print("Lower\n" + count + ": ");
                 }
             }
         }
         if(guess == number) {
             System.out.println("Correct!");
         } else {
             System.out.println("You are out of attempts, the correct number was " + number);
         }
     }
}
