 package com;
 
 import java.io.*;
 import java.util.*;
 
 public class Game 
 {
 	public String[][] gameBoard = new String[9][9];
 	public String[][] bombBoard = new String[9][9];
     
 	public int row = 0;
 	public int col = 0;
 	public int bombRow = 0;
 	public int bombCol = 0;
 	public int turn = 0;
 	public int maxTurns = 81;
 	public boolean endGame = false;
 	
 	{
 		clearBoard();
 	}
 	
 // -- METHODS START HERE -- \\	
 
 	//Game Win method
 	public void gameWin()
 	{
 		do	{
 				displayBoard();
 				userInput();
 			} 	while (endGame = false);
 	}
 	
 	public void resetVars()
 	{
		gameBoard = new String[9][9];
		bombBoard = new String[9][9];
 		row = 0;
 		col = 0;
 		bombRow = 0;
 		bombCol = 0;
 		turn = 0;
 		maxTurns = 81;
 		endGame = false;
 	}
 	
 	//Game Win Message Method
 	public void gameWinMessage()
 	{
 		System.out.println("Congratulations! You have beaten the hardest game on the planet!");
 		sleepMe();
 		getInput("Press Enter to Play Again!");
 		clearScreen();
 		clearBoard();
 	}
 	
 	//Game Lose method
 	public void gameLose()
 	{
 		//Prompts user to play again
 		getInput("Press Enter to Play Again!");
 		clearScreen();
 		clearBoard();
 	}
 	
 	//User Input Method
 	public String getInput(String prompt)
 	{
 		BufferedReader stdin = new BufferedReader
 			(
 				new InputStreamReader(System.in)
 			);
 		System.out.print(prompt);
 		System.out.flush();
 		try 
 		{
 			return stdin.readLine();
 		} 
 		catch (Exception e)
 		{
 			return "Error: " + e.getMessage();
 		}
 	}
 	
 	//Clear Screen Method
 	public void clearScreen()
 	{
 		Process exitCode = null;
 		try
 		{
 			if(System.getProperty("os.name").startsWith("Window")) 
 			{
 				exitCode = Runtime.getRuntime().exec("cls");
 			} 
 			else 
 			{
 			    exitCode = Runtime.getRuntime().exec("clear");
 			}
 		} 
 		catch (IOException e) 
 		{
 			for(int i = 0; i < 1000; i++) 
 			{
 				System.out.println();
 			}
 		}
 	}
 	
 	//Game Description Method
 	public void gameDescr()
 	{
 		System.out.println("The purpose of this game is to open all the cells of the board which do not contain a bomb.");
 		System.out.println();
 		
 		getInput("Press Enter to Start!");
 		
 		clearScreen();
 		
 		System.out.println("Enter a x position and a y position to reveal what's underneath.");
 		System.out.println();
 	}
 	
 	//Load Array Method (load the initial boards)
 	public void loadArray()
 	{
 		//Load the default board
 		for (int row=0; row<9; row++)
 		{
 			for (int col=0; col< 9; col++)
 			{
 				gameBoard[row][col] = "\u25A0";
 			}
 		}
 		//Load the bomb board
 		for (int bombRow=0; bombRow<9; bombRow++)
 		{
 			for (int bombCol=0; bombCol< 9; bombCol++)
 			{
 				bombBoard[bombRow][bombCol] = "\u25A0";
 			}
 		}
 	}
 	
 	//Display Board Method
 	public void displayBoard()
 	{
 		System.out.println(" ******  MINESWEEPER ******");
 		System.out.println(" ----------------------------");
 		System.out.print("  |");
 		
 		for (int i=0; i<9; i++)
 		{
 			System.out.print(" " + i + "|");
 		}
 		
 		System.out.println();
 		
 		for (row=0; row<9; row++)
 		{
 			System.out.print(" " + row + "|");
 			for (col=0; col<9; col++)
 			{
 				System.out.print( " " + gameBoard[row][col] + "|");
 			}
 			System.out.print("\n");
 		}
 	}
 	
 	//Load Bomb Method
 	public void loadBombs()
 	{
 		Random randomGen = new Random();
 		for (int u=1; u<=9; u++)
 		{
 			int randomRow = randomGen.nextInt(9);
 			//System.out.print(randomRow); \\UNCOMMENT THIS TO SHOW WHERE BOMBS ARE
 			int randomCol= randomGen.nextInt(9);
 			//System.out.println(randomCol); \\UNCOMMENT THIS TO SHOW WHERE BOMBS ARE
 			bombBoard[randomRow][randomCol] = "@";
 		}
 		
 		//Counting bombs in Array
 		for (int d = 0; d < bombBoard[bombRow].length; d++)
 		{
 			for (int e = 0; e < bombBoard[bombCol].length; e++)
 			{
 				if (bombBoard[d][e].equals("@"))
 				{
 					maxTurns--;
 				}
 			}
 		}
 		
 //		UNCOMMENT THIS TO SHOW BOMB BOARD
 //   	System.out.print("  |");
 //		for (int i=0; i<9; i++)
 //		{
 //			System.out.print(" " + i + "|");
 //		}
 //		System.out.println();
 //		for (bombRow=0; bombRow<9; bombRow++)
 //		{
 //			System.out.print(" " + bombRow + "|");
 //			for (bombCol=0; bombCol<9; bombCol++)
 //			{
 //				System.out.print( " " + bombBoard[bombRow][bombCol] + "|");
 //			}
 //				System.out.print("\n");
 //		}
 	}
 	
 	//User Input Method
 	public void userInput()
 	{
 		try
 		{
 			System.out.println();
 			do
 			{
 				turn++;
 				System.out.println("Turn: " + turn);
 			} 	while (endGame = false);
 	
 			Scanner user_input = new Scanner(System.in);
 			System.out.print("Enter the x position: ");
 			row = user_input.nextInt();
 			bombRow = row;
 			
 			System.out.print("Enter the y position: ");
 			col = user_input.nextInt();
 			bombCol = col;
 			System.out.println();
 	
 			if (row < 9 && col < 9 && bombRow < 9 && bombCol < 9)
 			{
 				if (bombBoard[bombRow][bombCol] == "@")
 				{
 					gameBoard[row][col] = "@";
 					clearScreen();
 					displayBoard();
 					System.out.println();
 					loadNumbers();
 					sleepMe();
 					endGame = true;
 					gameLose();
 				}
 				else
 				{
 					gameBoard[row][col] = "*";
 					if (turn == maxTurns)
 					{
 						endGame = true;
 						gameWinMessage();
 					}
 					else
 					{
 						endGame = false;
 					}
 					loadNumbers();
 					sleepMe();
 					clearScreen();
 					gameWin();
 				}
 			}
 			else
 			{
 				System.out.println("Incorrect Entry. Try Again");
 				turn-=1;
 				userInput();
 			}
 			clearScreen();
 		}
 		catch (Exception e)
 		{
 			System.out.println("Incorrect Entry. Try Again");
 			turn-=1;
 			userInput();
 		}
 	}
 	
 	public void clearBoard()
 	{
		resetVars();
 		gameDescr();
 		loadArray();
 		loadBombs();
 		gameWin();
 	}
 	
 	public void sleepMe()
 	{
 	   try
 		{
 			Thread.sleep(4000);
 		}
 		catch(InterruptedException e)
 		{
 			Thread.currentThread().interrupt();
 		}
 	}
 	
 	public void loadNumbers()
 	{
 		if (bombBoard[bombRow][bombCol] == "@") 
 		{
 			System.out.println("You have landed on a bomb!");
 			gameLose();
 		}
 		if (bombRow < 8)
 		{
 			if (bombBoard[bombRow+1][bombCol] == "@")
 			{
 				System.out.println("There is a bomb close by at " + (bombRow + 1) + ", " + bombCol + ".");
 			}
 		}
 		if (bombRow > 0)
 		{
 			if (bombBoard[bombRow-1][bombCol] == "@")
 			{
 				System.out.println("There is a bomb close by at " + (bombRow - 1) + ", " + bombCol + ".");
 			}
 		}
 		if (bombCol < 8)
 		{
 			if (bombBoard[bombRow][bombCol+1] == "@")
 			{
 				System.out.println("There is a bomb close by at " + bombRow + ", " + (bombCol + 1) + ".");
 			}
 		}
 		if (bombCol > 0)
 		{
 			if (bombBoard[bombRow][bombCol-1] == "@")
 			{
 				System.out.println("There is a bomb close by at " + bombRow + ", " + (bombCol - 1) + ".");
 			}
 		}
 		if ((bombRow < 8) && (bombCol > 0))
 		{
 			if (bombBoard[bombRow+1][bombCol-1] == "@")
 			{
 				System.out.println("There is a bomb close by at " + (bombRow + 1) + ", " + (bombCol - 1) + ".");
 			}
 		}
 		if ((bombRow > 0) && (bombCol > 0))
 		{
 			if (bombBoard[bombRow-1][bombCol-1] == "@")
 			{
 				System.out.println("There is a bomb close by at " + (bombRow + 1) + ", " + (bombCol - 1) + ".");
 			}
 		}
 		if ((bombRow < 8) && (bombCol < 8))
 		{
 			if (bombBoard[bombRow+1][bombCol+1] == "@")
 			{
 				System.out.println("There is a bomb close by at " + (bombRow + 1) + ", " + (bombCol - 1) + ".");
 			}
 		}
 		if ((bombRow > 0) && (bombCol < 8))
 		{
 			if (bombBoard[bombRow-1][bombCol+1] == "@")
 			{
 				System.out.println("There is a bomb close by at " + (bombRow + 1) + ", " + (bombCol - 1) + ".");
 			}
 		}
 	}
 }
