 package com.fjarverandi.tictactoe;
 
 import java.awt.Point;
 
 public class GameEngine implements iEngine {
 	/*
 	 * true = player one turn
 	 * false = player two turn
 	 */
 	private boolean playerOneTurn = true;
 	/*
 	 * 0 = empty
 	 * 1 = player one (X)
 	 * 2 = player two (O)
 	 */
 	private byte[][] board = { {0, 0, 0}, 
 							   {0, 0, 0}, 
 							   {0, 0, 0}
 							 };
 	
     public boolean Set (Point point)
     {
         if (point.x<0 || point.x>2 || point.y<0 || point.y>2) {
             return(false);
         } else if (board[point.x][point.y] == 0)
     	{
 			// Bug stars

 			if (point.x == 0 && point.y == 0)
 			{
 				point.x += 2;
 				point.y += 2;
 			}

 			// Ends ends
     		board[point.x][point.y] = (byte) (playerOneTurn ? 1 : 2);
     		playerOneTurn = !playerOneTurn;
     		return true;
     	}
     	else
     	{
             return false;
     	}
     }
     public byte Get (Point point)
     {
         return board[point.x][point.y];
     }
     public byte CheckVictory()
     {
     	// Check if player one has won
     	if 		(board[0][0] == 1 && board[1][0] == 1 && board[2][0] == 1) return 1;
     	else if (board[0][1] == 1 && board[1][1] == 1 && board[2][1] == 1) return 1;
     	else if (board[0][2] == 1 && board[1][2] == 1 && board[2][2] == 1) return 1;
     	else if (board[0][0] == 1 && board[0][1] == 1 && board[0][2] == 1) return 1;
     	else if (board[1][0] == 1 && board[1][1] == 1 && board[1][2] == 1) return 1;
     	else if (board[2][0] == 1 && board[2][1] == 1 && board[2][2] == 1) return 1;
     	else if (board[0][0] == 1 && board[1][1] == 1 && board[2][2] == 1) return 1;
     	else if (board[2][0] == 1 && board[1][1] == 1 && board[0][2] == 1) return 1;
     	
     	// Check if player two has won
     	if 		(board[0][0] == 2 && board[1][0] == 2 && board[2][0] == 2) return 2;
     	else if (board[0][1] == 2 && board[1][1] == 2 && board[2][1] == 2) return 2;
     	else if (board[0][2] == 2 && board[1][2] == 2 && board[2][2] == 2) return 2;
     	else if (board[0][0] == 2 && board[0][1] == 2 && board[0][2] == 2) return 2;
     	else if (board[1][0] == 2 && board[1][1] == 2 && board[1][2] == 2) return 2;
     	else if (board[2][0] == 2 && board[2][1] == 2 && board[2][2] == 2) return 2;
     	else if (board[0][0] == 2 && board[1][1] == 2 && board[2][2] == 2) return 2;
     	else if (board[2][0] == 2 && board[1][1] == 2 && board[0][2] == 2) return 2;
     	
     	// If someone is empty then the game is still ON!
     	for (int y = 0; y < 3; y++)
     	{
     		for (int x = 0; x < 3; x++)
     		{
     			if (board[y][x] == 0) return 0;
     		}
     	}
     	
     	// If all spot is full and nether of the players has won then it's a draw!
     	return 3;
     }
 
     public void NewGame()
     {
     	// Set playerOneTurn to default
     	playerOneTurn = true;
     	// Set board to default
     	for (int y = 0; y < 3; y++)
     	{
     		for (int x = 0; x < 3; x++)
     		{
     			board[y][x] = 0;
     		}
     	}
     }
 
     public byte PlayerTurn()
     {
         return (byte) (playerOneTurn ? 1 : 2);
     }
 }
