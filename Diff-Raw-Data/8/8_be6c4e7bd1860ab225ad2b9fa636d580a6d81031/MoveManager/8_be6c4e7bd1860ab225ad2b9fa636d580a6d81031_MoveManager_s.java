 package snozama.amazons.mechanics;
 
 import java.util.Random;
 
 import snozama.amazons.global.GlobalFunctions;
 
 /**
  * A high performance management library for board moves.
  * @author Graeme Douglas
  * 
  * Moves are encoded as 32-bit integers (shorts).  We divide the bits as follows:
  * 	Portion 0 - bits 0-3:	row of old amazon position.
  * 	Portion 1 - bits 4-7: 	column of old amazon position.
  * 	Portion 2 - bits 8-11:	row of new amazon position.
  *  Portion 3 - bits 12-15:	column of new amazon position.
  *  Portion 4 - bits 16-19:	row where new arrow placed.
  *  Portion 5 - bits 20-24:	column where new arrow placed.
  * 	Portion 6 - bits 25-28:	???
  * 	Portion 7 - bits 29-32:	???
  */
 public class MoveManager
 {
 	/*** Portion Constants ****************************************************/
 	/**
 	 * Portion of move that is for the player's colour.
 	 */
 	private static final int PLAYER_COLOUR = 0;
 	
 	/**
 	 * Portion of move that is for the index within the board's amazon array.
 	 */
 	private static final int AMAZON_ARRAY_INDEX = 1;
 	
 	/**
 	 * Portion of move that is for row of the final position of amazon.
 	 */
 	private static final int AMAZON_ROW_FINISH = 2;
 	
 	/**
 	 * Portion of move that is for column of final position of amazon.
 	 */
 	private static final int AMAZON_COLUMN_FINISH = 3;
 	
 	/**
 	 * Portion of move that is for row of the arrow that will be placed.
 	 */
 	private static final int ARROW_ROW = 4;
 	
 	/**
 	 * Portion of move that is for column of the arrow that will be placed.
 	 */
 	private static final int ARROW_COLUMN = 5;
 	/**************************************************************************/
 	
 	/**
 	 * Array storing move data.
 	 */
 	private int[]  moves;
 	
 	/**
 	 * Position in moves where to add next move to.
 	 */
 	private int nextPos;
 	
 	/**
 	 * State variable for the iterator.
 	 */
 	private int iteratorPosition;
 	
 	/**
 	 * Constructor. Create a new MoveManager.
 	 * 
 	 * @param size		The number of elements we wish to make the array of
 	 * 					moves initially.
 	 */
 	public MoveManager(int size)
 	{
 		moves = new int[size];
 		nextPos = 0;
 		iteratorPosition = 0;
 	}
 	
 	/**
 	 * Default constructor.
 	 */
 	public MoveManager()
 	{
 		moves = new int[2176];
 		nextPos = 0;
 		iteratorPosition = 0;
 	}
 	
 	/**
 	 * Get the current number of moves being managed.
 	 * 
 	 * @return	The current number of moves being managed.
 	 */
 	public int size()
 	{
 		return nextPos;
 	}
 	
 	/**
 	 * Get the maximum possible size of the move manager.
 	 * 
 	 * @return	The maximum number of moves this instance can managed.
 	 */
 	public int maxSize()
 	{
 		return moves.length;
 	}
 	
 	/**
 	 * Apply a move to a board.
 	 * 
 	 * @param board		The board to apply the move to.
 	 * @param index		The index of the move to apply.
 	 * @return	{@value true} if the move was applied successfully, 
 	 * 			{@value false} otherwise.
 	 */
 	public boolean applyMove(Board board, int index)
 	{
 		return board.move(getAmazonIndex(index), getFinishRow(index),
 				getFinishColumn(index), getArrowRow(index),
 				getArrowColumn(index), (byte)getColour(index));
 	}
 	
 	/**
 	 * Undo a move from a board.
 	 * 
 	 * @param board		The board to undo the move for.
 	 * @param index		The index of the move to undo.
 	 * @param row_s		Row where amazon previously was.
 	 * @param col_s		Column where amazon previously was.
 	 * @return	{@value true} if the move was undone successfully,
 	 * 			{@value false} otherwise.
 	 */
 	public boolean undoMove(Board board, int index, int row_s, int col_s)
 	{
		board.board[row_s][col_s] = Board.OCCUPIED;
 		
 		board.board[getFinishRow(index)][getFinishColumn(index)] = Board.EMPTY;
 		
 		board.amazons[getColour(index)][getAmazonIndex(index)]=
 				Board.encodeAmazonPosition(row_s, col_s);
 		
		board.board[getArrowRow(index)][getArrowColumn(index)] = Board.EMPTY;
		
 		return true;
 	}
 	
 	/**
 	 * Decode a portion of a move.
 	 * @param index		The index of the move to be decoded.
 	 * @param portion	Which set of 4 bits to be decoded.		
 	 * @return	The decoded portion of the move at index.
 	 */
 	private int decodePortion(int index, int portion)
 	{
 		return ((moves[index] & (0xf << (4*portion))) >> (4*portion));
 	}
 	
 	/**
 	 * Encode information to a portion of a move at an index.
 	 * @param index		The index of the move to encode data into.
 	 * @param portion	The portion of the m0e to encode the data to.
 	 * @param toEncode	The data that is to be encoded. Necessary data should
 	 * 					be in the lowest 4 bits.
 	 * @return	{@value true} if the data was encoded, {@value false} otherwise.
 	 */
 	private boolean encodePortion(int index, int portion, int toEncode)
 	{
 		// First clear the necessary bits.
 		moves[index] &= ~(0xf << (4*portion));
 		
 		// Now set them.
 		moves[index] |= (toEncode << (4*portion));
 		return true;
 	}
 	
 	/**
 	 * Add an encoded move to the set of moves.
 	 * 
 	 * @param move		The encoded move.
 	 * @return	{@value false} if the move was not added, {@value true} otherwise.
 	 */
 	public boolean add(int move)
 	{
 		if (nextPos >= moves.length)
 			return false;
 		
 		moves[nextPos++] = move;
 		return true;
 	}
 	
 	/**
 	 * Add a move to the set of possible moves.
 	 * 
 	 * @param colour	The player's/amazon's colour.
 	 * @param arr_i		The index of the amazon in the board's amazon array.
 	 * @param arow_f	The row the amazon ends in.
 	 * @param acol_f	The column the amazon ends in.
 	 * @param arrowrow	The row the arrow is placed.
 	 * @param arrowcol	The row the arrow is placed.
 	 * @return	{@value true} if the move was added, {@value false otherwise}.
 	 */
 	public boolean add(int colour, int arr_i, int arow_f, int acol_f, int arrowrow, int arrowcol)
 	{
 		if (nextPos >= moves.length)
 			return false;
 		
 		// Encode all the data.
 		encodePortion(nextPos, PLAYER_COLOUR, colour);
 		encodePortion(nextPos, AMAZON_ARRAY_INDEX, arr_i);
 		encodePortion(nextPos, AMAZON_ROW_FINISH, arow_f);
 		encodePortion(nextPos, AMAZON_COLUMN_FINISH, acol_f);
 		encodePortion(nextPos, ARROW_ROW, arrowrow);
 		encodePortion(nextPos, ARROW_COLUMN, arrowcol);
 		
 		nextPos++;
 		return true;
 	}
 	
 	/**
 	 * Get the colour of the amazon that is moved.
 	 * 
 	 * @param index		The index of the move to be decoded.
 	 * @return	The colour of the amazon that will be moved.
 	 */
 	public int getColour(int index)
 	{
 		return decodePortion(index, PLAYER_COLOUR);
 	}
 	
 	/**
 	 * Get the index of the amazon.
 	 * 
 	 * @param index		The index of the move to be decoded.
 	 * @return	The index within the board's amazons array.
 	 */
 	public int getAmazonIndex(int index)
 	{
 		return decodePortion(index, AMAZON_ARRAY_INDEX);
 	}
 	
 	/**
 	 * Get the row the amazon finishes in.
 	 * 
 	 * @param index		The index of the move to be decoded.
 	 * @return	The row the amazon finish at.
 	 */
 	public int getFinishRow(int index)
 	{
 		return decodePortion(index, AMAZON_ROW_FINISH);
 	}
 	
 	/**
 	 * Get the column the amazon finishes in.
 	 * 
 	 * @param index		The index of the move to be decoded.
 	 * @return	The column the amazon finishes at.
 	 */
 	public int getFinishColumn(int index)
 	{
 		return decodePortion(index, AMAZON_COLUMN_FINISH);
 	}
 	
 	/**
 	 * Get the row the arrow is placed in.
 	 * 
 	 * @param index		The index of the move to be decoded.
 	 * @return	The row the arrow is placed.
 	 */
 	public int getArrowRow(int index)
 	{
 		return decodePortion(index, ARROW_ROW);
 	}
 	
 	/**
 	 * Get the column the arrow is placed at.
 	 * 
 	 * @param index		The index of the move to be decoded.
 	 * @return	The column the arrow is placed.
 	 */
 	public int getArrowColumn(int index)
 	{
 		return decodePortion(index, ARROW_COLUMN);
 	}
 	
 	/**
 	 * Clears the state of internal iterator. After calling function,
 	 * the next call to nextMove() will return first move added.
 	 */
 	public void clearIteratorState()
 	{
 		iteratorPosition = 0;
 	}
 	
 	/**
 	 * Check if there are any more moves to iterate over.
 	 * 
 	 * @return	{@value true} if there are more moves to iterate over,
 	 * 			{@value false} otherwise.
 	 */
 	public boolean hasIterations()
 	{
 		return iteratorPosition < nextPos;
 	}
 	
 	/**
 	 * Return the next position to be iterated over.
 	 * 
 	 * @return	The index of the next move to be checked.
 	 */
 	public int nextIterableIndex()
 	{
 		return iteratorPosition++;
 	}
 	
 	/**
 	 * Create a new array to hold moves that is exactly the number of elements
 	 * in the current move array.
 	 * 
 	 * @return	{@value true} if procedure completed, {@value false} otherwise.
 	 */
 	public boolean condense()
 	{
 		// TODO: Make it use statistics instead.?
 		if (nextPos == moves.length)
 			return true;
 		
 		int[] temp = new int[nextPos];
 		for (int i = 0; i < nextPos; i++)
 		{
 			temp[i] = moves[i];
 		}
 		moves = temp;
 		
 		return true;
 	}
 	
 	/**
 	 * Sort the moves according to some other array, in descending order.
 	 * 
 	 * @param sortBy	The array to the moves according to.  Likely going to be
 	 * 					heuristic scores.
 	 */
 	public void sort(int[] sortBy)
 	{
 		GlobalFunctions.dualQuickSort(this.moves, sortBy, 0, GlobalFunctions.min(size(), sortBy.length - 1), (byte)(-1));
 	}
 	
 	/**
 	 * Shuffle the set of moves randomly.
 	 */
 	public void shuffle()
 	{
 		Random random = new Random();
 		random.nextInt();
 		for (int i = 0; i < nextPos; i++)
 		{
 			int j = i + random.nextInt(nextPos - i);
 			GlobalFunctions.swap(moves, i, j);
 		}
 	}
 	
 	public int getMove(int index)
 	{
 		return moves[index];
 	}
 }
