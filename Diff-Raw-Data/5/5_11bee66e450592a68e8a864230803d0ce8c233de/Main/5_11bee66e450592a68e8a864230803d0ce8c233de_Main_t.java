 package com.tictactoe.main;
 
 import java.util.Scanner;
 import com.tictactoe.game.*;
 public class Main
 {
 	/**
 	 * Main function
 	 */
 	public static void main(String[] args)
 	{
 		Scanner inputStream=new Scanner(System.in);
 		System.out.println("Enter your name:");
 		String userName=inputStream.nextLine();
 		System.out.println("Welcome to KenTacToe, "+userName+"!");
 		System.out.println("Please enter valid inputs, as the lazy author did" +
 				"not implement exception handling in me. Hope you keep that in mind!");
 		System.out.println("Enter you turn to play (1 for X, 2 for O):");
 		int turnNumber=inputStream.nextInt();
 		System.out.println("Enter grid size you want to play in (n means nxn grid):");
 		int gridSize=inputStream.nextInt();
		System.out.println("Enter search depth (zero or negative values take" +
				"default search depth as moves available):");
		int searchDepth=inputStream.nextInt();
		Game ticTacToe=new Game(gridSize, userName, turnNumber, searchDepth);
 		ticTacToe.init();
 		ticTacToe.play();
 		inputStream.close();
 	}
 
 }
