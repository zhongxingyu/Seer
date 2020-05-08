 package com.hawksman.unnamedgame;
 
 import java.util.Scanner;
 
 public class MainGame {
 	
 	static Scanner sc = new Scanner(System.in);
 	public static String playAgain;
 	
	static boolean play;
 	
 	public static void main(String[] args) {
           start();
 	}
 	
 	public static void over() {
 		
 		System.out.println("Play again?");
 		System.out.println("(Y)es");
 		System.out.println("(N)o");
 		System.out.println("");
 		playAgain = sc.next();
 		
 		if(playAgain.equalsIgnoreCase("y")) {
 			play = true;
		}
		
		if(playAgain.equalsIgnoreCase("n")) {
 			play = false;
 		}
 		
 	}
 	
 	public static void start() {
 		
		do {
 			String menuChoice;
 		
 			System.out.println("Welcome to the Unnamed Game!");
 			System.out.println("");
 			System.out.println("What do you want to do?:");
 			System.out.println("_______________");
 			System.out.println("(P)lay");
 			System.out.println("(H)elp");
 			System.out.println("(E)xit");
 			System.out.println("_______________");
 			System.out.println("");
 			menuChoice = sc.next();
 		
 			if(menuChoice.equalsIgnoreCase("p")) {
 				Game.play();
 			} else if(menuChoice.equalsIgnoreCase("h")) {
 				Game.showHelp();
 			} else if (menuChoice.equalsIgnoreCase("e")) {
 				play = false;
 			} else {
 				System.out.println("Did not recognize input.");
 				System.out.println("");
 				
 				start();
 			}
 			
 			
		} while(play);
 		
 		System.out.println("Bye! Thanks for playing!");
 		System.out.println("");
 	}
 
 }
