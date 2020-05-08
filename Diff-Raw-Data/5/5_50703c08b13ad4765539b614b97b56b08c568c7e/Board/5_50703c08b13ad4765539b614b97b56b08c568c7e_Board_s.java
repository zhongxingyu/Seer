 package GameObjects;
 
 /**
  * @author RHsu
  */
 //Creates a board where the game can be played
 public class Board
 {
 	//<editor-fold defaultstate="collapsed" desc="PRIVATE">
 	protected Piece[][] board;
 	//</editor-fold>
 	
 	/**
 	 * Creates a random piece if the piece is at the bottom, otherwise create an empty piece.
 	 */
 	public Board()
 	{
 		board = new Piece[7][7];
 
 		for (int i = 0; i < 7; i++)
 		{
 			for (int j = 0; j < 7; j++) 
 			{
 				board[i][j] = (i < 6) ? new Piece(Piece.Type.EMPTY) : new Piece(Piece.Type.NEW);
 			}
 		}
 	}
 		
 	/**
 	 * Given a position, this function will create a new piece and update the piece
 	 * @param position the column that the piece should be inserted
 	 * @return true if the insert was successful, else false
 	 */
 	public boolean Insert(int position, int value)
 	{
		if((position <= 0) || (value <= 0))
 		{
 			throw new IllegalArgumentException();
 		}
 		
 		//subtracting one to calibrate the position to work with array indexes
 		position--;
 		
 		for (int index = 6; index >= 0; index--)
 		{
 			Piece current = board[index][position];
 			if(current.getType() == Piece.Type.EMPTY)
 			{
 				current.setType(Piece.Type.NEW);
 				current.setValue(value);
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 	@Override
 	public String toString()
 	{
 		StringBuilder builder = new StringBuilder();
 		for (int i = 0; i < 7; i++)
 		{
 			for (int j = 0 ; j < 7; j++)
 			{
 				builder.append(board[i][j].toString()).append(" ");
 			}
 			builder.append("\n");
 		}
 		return builder.toString();
 	}
 	
 	public static void main(String[] args)
 	{		
 		Board b = new Board();
		b.Insert(0,0);
 		System.out.print(b)
 ;	}
 }
 
