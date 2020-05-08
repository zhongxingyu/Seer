/**
 * Computer Science 210 - Introduction to Computer Programming - Java
 * Chang-Shyh Peng <peng@clunet.org> <http://public.clunet.org/~peng>
 * 
 * @author Erik Youngren <artanis.00@gmail.com>
 * 
 * Examination 01
 * ==============
 * 
 * 2009.09.28 18:00 - 2009.09.28 21:00
 * 
 * Game: Rock, Scissors, Paper
 * ---------------------------
 * Write a program that plays the popular scissor-rocl-paper game. The
 * program randomly generates a number 0, 1, or 2 representing scissor,
 * rock, and paper. The program prompts the user to enter a number 0, 1,
 * or 2 and displays a message indicating whether the user or the
 * computer wins or draws. Note that a scissor can cut a paper, a rock
 * can knock a scissor, and a paper can wrap a rock.
 * 
 */

 import java.util.Scanner;
 import java.util.Random;
 
 public class RoShamBo {
     public static void main (String[] args) {
         System.out.println("\nPlay Rock Paper Scissors with me!");
         
         int player = RoShamBo.player();
         int cpu = RoShamBo.cpu();
         
         if (player == -1) {
             System.out.println("\nYou don't want to play? Come back soon!");
             System.exit(0);
         } else {
             System.out.printf("You chose %s! CPU chose %s! ",
                 RoShamBo.name(player), RoShamBo.name(cpu));
             
             String result = "";
             
             if(player == cpu) result = "Its a draw!";
            else if(((player + 1 <= 2)? player+1: 0) == cpu) result = "CPU wins!";
             else result = "You win!";
             
             System.out.println(result+"\n\nThanks for playing! Come back soon!\n");
            System.out.printf("%d == %d", ((player + 1 < 2)? player+1: 0), cpu);
         }
     }
     
     public static int player() {
         Scanner stdin = new Scanner(System.in);
         
         int answer = -1;
         System.out.print(
         "Choose one of the following:\n" +
         "    1: Scissors (cuts paper)\n" +
         "    2: Rock     (knocks scissors)\n" +
         "    3: Paper    (wraps rock)\n");
         try {
             answer = stdin.nextInt()-1;
            System.out.println(answer);
            
        } catch (java.util.InputMismatchException e) {
            // don't need to do anything but trap the error.
            // function handles no input just fine.
        }
         
         return (answer >= 0 && answer <= 2)? answer: -1;
     }
     
     public static int cpu() {
         Random gen = new Random();
         return gen.nextInt(3);
     }
     
     public static String name(int answer) {
         String name = "";
         
         switch(answer) {
             case 0: name = "Scissors"; break;
             case 1: name = "Rock"; break;
             case 2: name = "Paper"; break;
         }
         
         return name;
     }
 }
