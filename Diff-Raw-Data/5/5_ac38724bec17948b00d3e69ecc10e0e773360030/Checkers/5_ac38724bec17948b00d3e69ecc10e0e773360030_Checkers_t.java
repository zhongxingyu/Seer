 //Jeff Stewart
 // 6/17/11
 //Checkers will allow a checker piece to move diagonally on an 8x8 grid. 
 //If a proposed move is both within the grid and is touching diagonally the current location the move will be completed.
 //Moves can be proposed by the user with the keyboard
 import java.io.*;
 class Checkers {
 
 	public static void main(String[] arg) throws Exception{ // using the main method to test the game of checkers
 		int row = 0;	//setting the initial state for the row
 		int col = 0;	//setting the initial state for the col
 		String proposedMove = "";
 		BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));	//allows for keyboard entry of proposed moves
 		int movesLeft = 5; //will have the user make this many legal moves
 		
 		while (movesLeft > 0)	{
 			
 			System.out.print("Please enter a row to move to: ");	//prompt user for new row
 			proposedMove = keyboard.readLine(); 	//proposed row to be checked for in bounds and legality
 			int proposedRow = Integer.parseInt(proposedMove);
 			System.out.print("Please enter a col to move to: ");	//prompt user for new col
 			proposedMove = keyboard.readLine(); 	//proposed col to be checked for in bounds and legality
 			int proposedCol = Integer.parseInt(proposedMove); 	
 								//if both are legal the move will be completed
 			if (checkIfPossible(row, col, proposedRow, proposedCol)) {
 				row = proposedRow; //moves the player to the new location
 				col = proposedCol; //moves the player to the new location
 				System.out.println("The move has been completed. \nYou are now located at " + row + "," + col); //print the outcome of the proposed move for the user
 				movesLeft--;
 			}
 			else
 				System.out.println("The move has not been completed. \nYou are still located at " + row + "," + col);	//print the outcome of the proposed move for the user
 			
 			System.out.println("You have " + movesLeft + " moves left" );
 			
 		}
 		System.out.println("Thanks for playing, have a nice day!");
 	}	
 	
 	public static boolean checkIfPossible(int currentRow, int currentCol, int proposedRow, int proposedCol){	//checks for in bounds and legality of both row and col
 		if (CheckInBounds(proposedRow, proposedCol))
 			if(CheckLegal(currentRow, currentCol, proposedRow, proposedCol))
 				return true;
 			else
 				return false;
 		else
 			return false;
 	}
 	
	public static boolean CheckInBounds(int row, int col){		//checks for both row and col to be in bounds
 		if (row >= 0 && row <= 7 && col >= 0 && row <= 7) {		//8x8 grid
 			System.out.print(row + "," + col + " is in bounds, ");	//print the outcome of the test for debug purposes
 			return true;
 		}
 		else
 			System.out.println(row + "," + col + " is out of bounds");	//print the outcome of the test for debug purposes
 			return false;
 	}
 	
	public static boolean CheckLegal(int initalRow, int initalCol, int proposedRow, int proposedCol) {	//checks for a legal one-space diagonal move. Does not allow jumping
 		int rowChange = Math.abs(initalRow-proposedRow);
 		int colChange = Math.abs(initalCol-proposedCol);
 		if (rowChange==1 && colChange==1) { //must move one unit diagonally 
 			System.out.println("and is a legal move.");	//print the outcome of the test for debug purposes
 			return true;
 		}
 		else
 			System.out.println("however, it is not a legal move.");	//print the outcome of the test for debug purposes
 			return false;
 	}
 }
