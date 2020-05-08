 package team01;
 
 import java.util.*;
 
 public class Fanorona {
 
 	/**
 	 * @param args
 	 */
 	static int moves = 0; //Keep track of number of game moves
 	static Board board = new Board(9, 5);
 	
 	public static void main(String[] args) 
 	{
 		//Prints board
		boolean quit = false, move_done=true;
 		int curr_pos_x, curr_pos_y, new_pos_x, new_pos_y, in;
 		Scanner input=new Scanner(System.in);
		while(!quit)
 		{
 			while(move_done)
 			{
 				System.out.println("White's turn\n------------\n");
 				System.out.print(board);
 				System.out.print("\nEnter X position of piece to move: ");
 				curr_pos_x = input.nextInt();
 				System.out.print("Enter Y position of piece to move: ");
 				curr_pos_y = input.nextInt();
 				System.out.print("Enter new X pos: ");
 				new_pos_x = input.nextInt();
 				System.out.print("Enter new Y pos: ");
 				new_pos_y = input.nextInt();
 				
 				move_done = move_function(curr_pos_x, curr_pos_y, new_pos_x, new_pos_y);
 				quit = max_moves();
 			}
 			move_done = true;
 			while(move_done)
 			{
 				System.out.println("Black's turn\n------------");
 				System.out.print(board);
 				System.out.print("\nEnter X position of piece to move: ");
 				curr_pos_x = input.nextInt();
 				System.out.print("Enter Y position of piece to move: ");
 				curr_pos_y = input.nextInt();
 				System.out.print("Enter new X pos: ");
 				new_pos_x = input.nextInt();
 				System.out.print("Enter new Y pos: ");
 				new_pos_y = input.nextInt();
 				
 				move_done = move_function(curr_pos_x, curr_pos_y, new_pos_x, new_pos_y);
 				quit = max_moves();
 			}
 			move_done = true;
 		}
 		
 		input.close();
 	}
 	//Returns FALSE if maximum number of moves has been exceeded
 	public static boolean max_moves()
 	{
 		if(moves >= 50)
 		{
 			System.out.println("Maximum moves exceeded");
 			return false;
 		}
 		else
 			return true;
 	}
 	public static boolean move_function(int x1, int y1, int x2, int y2)
 	{
 		boolean done = true;
 		Move m = new Move(board);
 		Point from = new Point(x1, y1);
 		Point to = new Point(x2, y2);
 		if(m.isValidMove(from, to))
 		{
 			done = m.capture(from, to, true);
 			//if move_done == false, no more captures
 			System.out.println("Valid move");
 			moves++;
 		}
 		else
 		{
 			System.out.println("Not a valid move");
 		}
 		return done;
 	}
 	//Function to move white pieces, returns -1 if invalid move etc.
 	public static int move_white(int curr_pos_x, int curr_pos_y, int new_pos_x, int new_pos_y) {
 //		if(board.isWhite(curr_pos_x, curr_pos_y) && board.isEmpty(new_pos_x, new_pos_y)) {
 //			board.setPosition(curr_pos_x, curr_pos_y, Board.EMPTY);
 //			board.setPosition(new_pos_x, new_pos_y, Board.WHITE);
 //			return 0; 
 //		} else {
 //			return -1;
 //		}
 		
 		return 0;
 	}
 	
 	//Function to move black pieces, returns -1 if invalid move etc.
 	public static int move_black(int curr_pos_x, int curr_pos_y, int new_pos_x, int new_pos_y) {
 //		if(board.isBlack(curr_pos_x, curr_pos_y) && board.isEmpty(new_pos_x, new_pos_y)) {
 //			board.setPosition(curr_pos_x, curr_pos_y, Board.EMPTY);
 //			board.setPosition(new_pos_x, new_pos_y, Board.BLACK);
 //			return 0; 
 //		} else {
 //			return -1;
 //		}
 		
 		return 0;
 	}
 }
