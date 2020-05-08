 /**
  * @author Adam Chappell, Joel Lyles, Taylor Phebus, Forrest Stallings
  */
 
 import java.util.*;
 
 public class Quarto
 {
 	public static void main(String args[])
 	{
 
 	}
 }
 
 class Game
 {
 	boolean player; //, set; set was a variable for the twist.
 	/**
 	 * These are all the sets we are keeping for whether we have already solved a given state. 
 	 * We are mostly keeping these so we can write it out to the disk
 	 * We are only keeping the states with 4,8,or 12 pieces on the board
 	 * as a balance between the work and memory of keeping these sets, and 
 	 * the desire to have data to analyze and be able to exit a branch quickly. 	
 	 * We can actually keep the result in the same set as the game state itself
 	 * by utilizing the fact that the first piece in the upper left corner is 
 	 * always 0000. So, to store that player 1 won the game, we start the int
 	 * with 10. To store that player 2 won, we store 01. To store a tie, 11.
 	 * We are doing this because I'm incredible scared of the amount of memory 
 	 * we need to store these things. 
 	*/
 	
 	/*Optomization, add later.
 	public static Set<Long> FourPiece=new HashSet<Long>();
 	public static Set<Long> EightPiece=new HashSet<Long>();
 	public static Set<Long> TwelvePiece=new HashSet<Long>();*/
 	public Set<Byte> pieces;
 	public byte[][] board;
 
 	//public byte chosen; chosen was a variable for the twist
<<<<<<< HEAD
	
 	Game(byte piece, int i, int j, Game previous){
=======

	Game(byte piece, byte placement, Game previous)
	{
>>>>>>> 183e97078f22dc3c58b0d1a8b30630c121038c0e
 		//Generates a new game state based on the placement.
 	}
 
 	/**
 	 * Instantiation of a new Game.
 	 * Creates the pieces and board.
 	 */
 	Game()
 	{
 		pieces = new LinkedHashSet<Byte>();
 		board = new byte[4][4];
 	}
 
 	/**
 	 * Decides the winner.
 	 * @return The ID of the winner.
 	 */
 	int winner()
 	{
 		if (done())
 		{
 			return result();
 		}
 		int winner = -1;
 		byte nextPiece;
 		Iterator<Byte> i = pieces.iterator();
 		while (i.hasNext())
 		{
 			nextPiece = i.next();
 			//For every piece
 			//For every placement
 			for(int j=0; j<4; j++){
 				for(int k=0; k<4; k++){
 					if(j!=0&&k!=0&&board[j][k]==0){//Dont overwrite the top left.
 						int winTemp=new Game(nextPiece,j,k,this).winner();
 						//winner=best of(winner, new Game(piece,placement,this).winner())
 					}
 				}
 			}
 		}
 		return 0;
 	}
 
 	/**
 	 * Figures out if the algorithm is done.
 	 * @return TRUE if done, FALSE otherwise.
 	 */
 	boolean done()
 	{
 		return false;
 		//If out of pieces
 
 		//Return true
 		//If there is a winner
 		//Return true
 		//Return false
 
 	}
 
 	/**
 	 * The result of the current gamestate?
 	 * @return 0 for tie, 1 for player 1 wins, 2 for player 2 wins
 	 * -1 for inconsistent gamestate
 	 */
 	byte result()
 	{
 		return -1;
 		//0 means tie.
 		//-1 means inconsistent gamestate (Could be useful later)
 		//1 means player 1 wins.
 		//2 means player 2 wins.
 	}
 }
