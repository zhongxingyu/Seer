 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.drchaos;
 
 import java.util.Random;
 import java.util.Scanner;
 
 /**
  *
  * @author drchaos
  */
 public class SimpleApp {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         Random generator = new Random();
         Scanner scan = new Scanner(System.in);
         
         final int GUESSES = 3;
 	// (0 - 9) + 1 is 1-10
         int secret = generator.nextInt(9)+1;
         int myGuess = -1;
         
         System.out.println("I am thinking of a number between 1 and 10.\n"
 		+ "Try to guess it!");
         for(int x=0; x<GUESSES; x++) {
             System.out.print("Guess: ");
             myGuess = scan.nextInt();
 	    if(myGuess == secret) {
 		    System.out.println("You guessed correct!");
 		    System.exit(0); // leave main(), exit program
 	    } else {
 		    System.out.println("You guessed wrong; Try again.");
 	    }
        }
	System.out.println("The correct answer was: " + secret + ". Better luck " +
		"next time!");
 	System.exit(-1); // Exit status, -1 Lost game
     }
 }
