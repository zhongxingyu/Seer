 package assignment3;
 
 import java.util.Scanner;
 
 /**
  *
  * @author Zane melcho
 * 3/8/2012
  */
 public class Game 
 {
     
     private static Match aMatch;
     private static int rounds = 0;
     
     public void DisplayHelp()
     {
         //System.out.println("What do you need help with?");
     }
     
     public static void MainMenu()
     {
         Scanner scan = new Scanner(System.in);
         
         //Display logo and prompt for number of throws
         System.out.println("************************************");
         System.out.println("*     Rock Paper Scissors 2012     *");
         System.out.println("*             Hypnocode            *");
         System.out.println("************************************");
         System.out.println("\n\n\n\n\n");
         System.out.println("Please enter the number of throws");
         rounds = scan.nextInt();
         
         System.out.println("\033");
     }
     public static void main(String[] args) 
     {
         MainMenu();
         
         aMatch = new Match(rounds);
         
         while(!aMatch.isMatchOver())
         {
             aMatch.makeThrows();
         }
         
         aMatch.declareWinner();
     }
     
 }
 
