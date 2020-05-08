 package snozama.amazons.mechanics;
 
 /**
  * Heuristic functions to be used to play the Game of the Amazons.
  * @author Cody Clerke
  *
  */
 
 public class SnozamaHeuristic {
 
 	
 	/*
 	 * Heuristic strategy:
 	 * 	Use linear combination of minimum stone ply (MSP) and min-mobility (and regions, if implemented)
 	 * 	This is best in early stages of game (up to turn 30) when open space is more important
 	 * 		0.2*Regions/Territory + 0.5*MSP + 0.3*Mobility (suggested weighting)
 	 * 		-OR-
 	 * 		0.6*MSP + 0.4*Mobility
 	 * 	Use MSP during endgame (after turn 30)
 	 * 	This is suggested later when owning squares becomes more important
 	 */
 	
 	/**
 	 * Evaluates the board based on the heuristics MSP and min-mobility.
 	 * @param board			The current board state.
 	 * @param activePlayer	The player whose turn it is.
 	 * @param turn			The current turn number.
 	 * @return	The score for the active player of the given board position.
 	 */
 	public static int evaluateBoard(Board board, int activePlayer, int turn)
 	{
 		if (turn <= 30)
 		{
 			return 3*MSP(board, activePlayer) + 2*minMobility(board, activePlayer);
 		}
 		else
 		{
 			return MSP(board, activePlayer);
 		}
 	}
 	
 	/**
 	* Calculates closest player to each open square on the board.
 	* The closest player to a square owns that square.
 	* @param board			The current board state.
 	* @param activePlayer	The player (white or black) whose turn it is.
 	* @return	The difference between the number of squares the active player owns and the number of squares the inactive player owns.
 	*/
 	public static int MSP(Board board, int activePlayer)
 	{
 		int whiteAdv = minPliesToSquare(board);
 		if (activePlayer == Board.WHITE)
 			return whiteAdv; //returns white's advantage
 		else // activePlayer is black
 			return -1*whiteAdv; //returns black's advantage
 	}
 	
 	
 	/**
 	* Calculates the number of moves available to the amazon of each colour with the minimum mobility.
 	* @param board	The current board state.
 	* @param activePlayer	The player (white or black) whose turn it is.
 	* @return	The difference between the minimum moves across all amazons of the active player and
 	*  the minimum moves across all amazons of the inactive player.
 	*/
 	public static int minMobility(Board board, int activePlayer)
 	{
 		int whiteMoves = Integer.MAX_VALUE;
 		int blackMoves = Integer.MAX_VALUE;
 		
 		int totalWhiteMoves = 0;
 		int totalBlackMoves = 0;
 		
 		//for each white amazon
 		for (int i = 0; i < 4; i++)
 		{
 			int amazonMoves = getNumberAvailableMoves(board, board.amazons[Board.WHITE][i]); //calculates number of moves available to amazon
 			if (amazonMoves < whiteMoves)
 				whiteMoves = amazonMoves;
 			totalWhiteMoves += amazonMoves;
 		}
 		//for each black amazon
 		for (int i = 0; i < 4; i++)
 		{
 			int amazonMoves = getNumberAvailableMoves(board, board.amazons[Board.BLACK][i]);
 			if (amazonMoves < blackMoves)
 				blackMoves = amazonMoves;
 			totalBlackMoves += amazonMoves;
 		}
 		
 		if (activePlayer == Board.WHITE)
 			return whiteMoves-blackMoves;
 		else //activePLayer is black
 			return blackMoves-whiteMoves;
 	}
 	
 	/**
 	 * Heuristic designed to distribute amazons evenly across the board.
 	 * The heuristic awards points for amazons all being in separate quadrants and evenly distributed in board halves.
 	 * The heuristic deducts points for too many amazons being in the same quadrant or same half of the board.
 	 * @param board			The current state of the board.
 	 * @param activePlayer	The colour of the active player.
 	 * @return		A score based on how evenly distributed the amazons of the given colour are.
 	 */
 	public static int quadrants(Board board, int activePlayer)
 	{
 		int score = 0;
 		int adj = 2;
 		int[] quadrant = new int [4]; // 0 = NW, 1 = NE, 2 = SW, 3 = SE
 		int[] topBounds = {0, 0, Board.SIZE/2, Board.SIZE/2};
 		int[] bottomBounds = {Board.SIZE/2, Board.SIZE/2, Board.SIZE, Board.SIZE};
 		int[] leftBounds = {0, Board.SIZE/2, 0, Board.SIZE/2};
 		int[] rightBounds = {Board.SIZE/2, Board.SIZE, Board.SIZE/2, Board.SIZE};
 		
 		for (int i = 0; i < quadrant.length; i++)
 		{
 			quadrant[i] = findInRegion(board, topBounds[i], bottomBounds[i], 
 					leftBounds[i], rightBounds[i], activePlayer);
 			
 			if (quadrant[i] == 1)
 				score += adj;
 			else if (quadrant[i] > 2)
 				score -= 2*adj;
 		}
 		
 		//Top half of board
 		int north = quadrant[0] + quadrant[1];
 		if (north == 2)
 			score += adj;
 		else if (north == 0 || north == 4)
 			score -= adj;
 		
 		//Left half of board
 		int west = quadrant[0] + quadrant[2];
 		if (west == 2)
 			score += adj;
 		else if (west == 0 || west == 4)
 			score -= adj;
 		
 		return score;
 	}
 
 	/**
 	* Calculates the difference between squares white owns and squares black owns.
 	* @param board	The current board state.
 	* @return	The white player's MSP advantage.
 	*/
 	public static int minPliesToSquare(Board board)
 	{
 		int whiteAdv = 0;
 		byte[][] markedBoard = board.copy();
 		/*
 		 * Given a square on the board, find the closest player of the given player
 		 * Strategy:
 		 * 	Use a bi-directional approach
 		 * 	Start with position of amazons and expand, marking each square they can reach in one move
 		 * 	From unmarked squares expand vertically, horizontally, diagonally to find marked squares
 		 * 		-these squares will be two moves away from an amazon
 		 * 	Continue doing this with unmarked squares until there are no more unmarked squares
 		 * 	In the case of an enclosed region, we may need a maximum number of iterations before declaring square neutral
 		 */
 		
 		/*
 		 * Step 1: Start with position of amazons and expand, marking each square they can reach in one move
 		 */
 		for (int i = 0; i < board.amazons.length; i++) //for each colour of amazon (2)
 		{
 			for (int j = 0; j < board.amazons[i].length; j++) //for each amazon of a colour (4)
 			{
 				int arow = Board.decodeAmazonRow(board.amazons[i][j]);		//amazon's starting row position
 				int acol = Board.decodeAmazonColumn(board.amazons[i][j]);	//amazon's starting column position
 				
 				// The following comments assume [0][0] is considered top left
 				// Find moves to the right
 				for (int c = acol+1; c < Board.SIZE; c++)
 				{
 					if (board.isOccupied(arow, c))
 					{
 						break;
 					}
 					else // this is a legal move
 					{
 						whiteAdv += markSquare(markedBoard, arow, c, i);
 					}
 				}
 				// Find moves to the left
 				for (int c = acol-1; c > -1; c--)
 				{
 					if (board.isOccupied(arow, c))
 					{
 						break;
 					}
 					else // this is a legal move
 					{
 						whiteAdv += markSquare(markedBoard, arow, c, i);
 					}
 				}
 				// Find moves below
 				for (int r = arow+1; r < Board.SIZE; r++)
 				{
 					if (board.isOccupied(r, acol))
 					{
 						break;
 					}
 					else // this is a legal move
 					{
 						whiteAdv += markSquare(markedBoard, r, acol, i);;
 					}
 				}
 				// Find moves above
 				for (int r = arow-1; r > -1; r--)
 				{
 					if (board.isOccupied(r, acol))
 					{
 						break;
 					}
 					else // this is a legal move
 					{
 						whiteAdv += markSquare(markedBoard, r, acol, i);
 					}
 				}
 				// Find moves diagonally (\) to the right
 				for (int r = arow+1, c = acol+1; r < Board.SIZE && c < Board.SIZE; r++, c++)
 				{
 					if (board.isOccupied(r, c))
 					{
 						break;
 					}
 					else // this is a legal move
 					{
 						whiteAdv += markSquare(markedBoard, r, c, i);
 					}
 				}
 				// Find moves diagonally (\) to the left
 				for (int r = arow-1, c = acol-1; r > -1 && c > -1; r--, c--)
 				{
 					if (board.isOccupied(r, c))
 					{
 						break;
 					}
 					else // this is a legal move
 					{
 						whiteAdv += markSquare(markedBoard, r, c, i);
 					}
 				}
 				// Find moves anti-diagonally (/) to the right
 				for (int r = arow-1, c = acol+1; r > -1 && c < Board.SIZE; r--, c++)
 				{
 					if (board.isOccupied(r, c))
 					{
 						break;
 					}
 					else // this is a legal move
 					{
 						whiteAdv += markSquare(markedBoard, r, c, i);
 					}
 				}
 				// Find moves anti-diagonally (/) to the left
 				for (int r = arow+1, c = acol-1; r < Board.SIZE && c > -1; r++, c--)
 				{
 					if (board.isOccupied(r, c))
 					{
 						break;
 					}
 					else // this is a legal move
 					{
 						whiteAdv += markSquare(markedBoard, r, c, i);
 					}
 				}
 			}
 		}// end of Step 1
 
 		/*
 		 * Step 2: For each unmarked square find a path to a marked square
 		 * 	Repeat until all squares are marked or we run a set number of iterations
 		 */
 		int[] unmarked = new int[92];
 		int index = 0;
 		for (int row = 0; row < Board.SIZE; row++)
 		{
 			for (int col = 0; col < Board.SIZE; col++)
 			{
 				if (markedBoard[row][col] == 0)
 				{
 					whiteAdv += findMarkedSquares(board, markedBoard, row, col, 2);
 					if (markedBoard[row][col] == 0) //square still cannot be reached in this iteration
 					{
 						unmarked[index++] = row*10 + col; //put unmarked square in list to check later
 					}
 				}
 			}
 		}
 		int maxIterations = 8;
 		for (int itr = 3; itr < maxIterations; itr++)
 		{
 			for (int i = 0; i < index; i++) //for each unmarked square
 			{
 				int row = unmarked[i]/10;
 				int col = unmarked[i]%10;
				if (markedBoard[row][col] == 0)
				{
					whiteAdv += findMarkedSquares(board, markedBoard, row, col, itr);
				}
 			}
 		}
 		return whiteAdv;
 	} //end of Step 2
 	
 	/**
 	 * Used for the first iteration of minPliesToSquare heuristic.
 	 * Marks square with a colour (white=10, black=20) plus the number of moves for the closest player to reach that square.
 	 * A square reached by a white player in one move will be marked 11, for example.
 	 * Squares that can be reached in equal number of turns by both colours with be marked 'N'.
 	 * @param markedBoard	The board maintaining the owners of each square.
 	 * @param row	The row of the square being marked.
 	 * @param col	The column of the square being marked.
 	 * @param colour	The colour of the amazon able to reach this square.
 	 * @return		The owner of the square. Will return 1 for white, -1 for black and 0 for neutral.
 	 */
 	private static int markSquare(byte[][] markedBoard, int row, int col, int colour)
 	{
 		int whiteAdv = 0;
 		byte mark = (byte)(colour*10+11); // white->11, black->21
 		if (markedBoard[row][col] == 0) // if not already marked
 		{
 			markedBoard[row][col] = mark;
 			if (colour == Board.WHITE)
 				whiteAdv++; //white may own square
 			else
 				whiteAdv--; //black owns square
 		}
 		else if (mark > markedBoard[row][col])
 		{
 			markedBoard[row][col] = 'N'; //square is neutral
 			whiteAdv--; 	//negates white's point given earlier
 		}
 		return whiteAdv;
 	}
 	
 	/**
 	 * Used for the second part of minPliesToSquare heuristic.
 	 * From each unmarked square attempts to find a path to the nearest amazon(s).
 	 * @param board		The current state of the entire board.
 	 * @param markedBoard	The board maintaining the owners of each square.
 	 * @param row		The row of the square being marked.
 	 * @param col		The column of the square being marked.
 	 * @param iteration	The minimum number of turns to reach an amazon from this square.
 	 * @return		The owner of the square. Will return 1 for white, -1 for black, 0 for neutral.
 	 */
 	private static int findMarkedSquares(Board board, byte[][] markedBoard, int row, int col, int iteration)
 	{
 		int whiteAdv = 0;
 		// The following comments assume [0][0] is considered top left
 		// Find moves to the right
 		for (int c = col+1; c < Board.SIZE; c++)
 		{
 			if (board.isOccupied(row, c))
 			{
 				break;
 			}
 			else if (markedBoard[row][c] != 0)
 			{
 				whiteAdv = markSquare(markedBoard, row, c, row, col, iteration);
 				if (markedBoard[row][col] == 'N')
 				{
 					return 0;
 				}
 			}
 		}
 		// Find moves to the left
 		for (int c = col-1; c > -1; c--)
 		{
 			if (board.isOccupied(row, c))
 			{
 				break;
 			}
 			else if (markedBoard[row][c] != 0)
 			{
 				whiteAdv = markSquare(markedBoard, row, c, row, col, iteration);
 				if (markedBoard[row][col] == 'N')
 				{
 					return 0;
 				}
 			}
 		}
 		// Find moves below
 		for (int r = row+1; r < Board.SIZE; r++)
 		{
 			if (board.isOccupied(r, col))
 			{
 				break;
 			}
 			else if (markedBoard[r][col] != 0)
 			{
 				whiteAdv = markSquare(markedBoard, r, col, row, col, iteration);
 				if (markedBoard[row][col] == 'N')
 				{
 					return 0;
 				}
 			}
 		}
 		// Find moves above
 		for (int r = row-1; r > -1; r--)
 		{
 			if (board.isOccupied(r, col))
 			{
 				break;
 			}
 			else if (markedBoard[r][col] != 0)
 			{
 				whiteAdv = markSquare(markedBoard, r, col, row, col, iteration);
 				if (markedBoard[row][col] == 'N')
 				{
 					return 0;
 				}
 			}
 		}
 		// Find moves diagonally (\) to the right
 		for (int r = row+1, c = col+1; r < Board.SIZE && c < Board.SIZE; r++, c++)
 		{
 			if (board.isOccupied(r, c))
 			{
 				break;
 			}
 			else if (markedBoard[r][c] != 0)
 			{
 				whiteAdv = markSquare(markedBoard, r, c, row, col, iteration);
 				if (markedBoard[row][col] == 'N')
 				{
 					return 0;
 				}
 			}
 		}
 		// Find moves diagonally (\) to the left
 		for (int r = row-1, c = col-1; r > -1 && c > -1; r--, c--)
 		{
 			if (board.isOccupied(r, c))
 			{
 				break;
 			}
 			else if (markedBoard[r][c] != 0)
 			{
 				whiteAdv = markSquare(markedBoard, r, c, row, col, iteration);
 				if (markedBoard[row][col] == 'N')
 				{
 					return 0;
 				}
 			}
 		}
 		// Find moves anti-diagonally (/) to the right
 		for (int r = row-1, c = col+1; r > -1 && c < Board.SIZE; r--, c++)
 		{
 			if (board.isOccupied(r, c))
 			{
 				break;
 			}
 			else if (markedBoard[r][c] != 0)
 			{
 				whiteAdv = markSquare(markedBoard, r, c, row, col, iteration);
 				if (markedBoard[row][col] == 'N')
 				{
 					return 0;
 				}
 			}
 		}
 		// Find moves anti-diagonally (/) to the left
 		for (int r = row+1, c = col-1; r < Board.SIZE && c > -1; r++, c--)
 		{
 			if (board.isOccupied(r, c))
 			{
 				break;
 			}
 			else if (markedBoard[r][c] != 0)
 			{
 				whiteAdv = markSquare(markedBoard, r, c, row, col, iteration);
 				if (markedBoard[row][col] == 'N')
 				{
 					return 0;
 				}
 			}
 		}
 		return whiteAdv;
 	}
 	
 	/**
 	 * Used for the second part of the minPlies heuristic.
 	 * Marks an unmarked square with the colour of the player who can reach the square fastest.
 	 * If both players can reach the square equally fast, the square will be marked neutral.
 	 * @param markedBoard	The board maintaining the owners of each square.
 	 * @param row		The row of a previously marked square indicating a path to an amazon in this iteration.
 	 * @param col		The column of a previously marked square indicating a path to an amazon in this iteration.
 	 * @param row_s		The row of the square being marked.
 	 * @param col_s		The column of the square being marked.
 	 * @param itr		The minimum number of turns to reach an amazon from the square being marked.
 	 * @return		The owner of the square. Will return 1 for white, -1 for black, 0 for neutral.
 	 */
 	private static int markSquare(byte[][] markedBoard, int row, int col, int row_s, int col_s, int itr)
 	{
 		int whiteAdv = 0;
 		
 		if (markedBoard[row][col] == 'N') //found a neutral square
 		{
 			markedBoard[row_s][col_s] = 'N'; //start square is neutral
 			return 0;
 		}
 		else if (markedBoard[row][col] == 10+itr-1) //found square marked by white in previous iteration
 		{
 			if (markedBoard[row_s][col_s] == 0) //if starting square is unmarked
 			{
 				markedBoard[row_s][col_s] = (byte)(10+itr); //white can reach start square in i moves
 				whiteAdv = 1;
 			}
 			else if (markedBoard[row_s][col_s] == 20+itr) //black can reach start square in same iteration
 			{
 				markedBoard[row_s][col_s] = 'N'; //both players can reach square in i moves, square is neutral
 				return 0;
 			}
 		}
 		else if (markedBoard[row][col] == 20+itr-1) //found sqaure marked by black in previous iteration
 		{
 			if (markedBoard[row_s][col_s] == 0) //if starting square is unmarked
 			{
 				markedBoard[row_s][col_s] = (byte)(20+itr); //black can reach start square in i moves
 				whiteAdv = -1;
 			}
 			else if (markedBoard[row_s][col_s] == 10+itr) //white can reach start square in same iteration
 			{
 				markedBoard[row_s][col_s] = 'N'; //both players can reach start square in i moves, square is neutral
 				return 0;
 			}
 		}
 		
 		return whiteAdv;
 	}
 	
 	/**
 	* Calculates the number of moves available to the amazon.
 	* @param board	The current board state.
 	* @param amazon	An individual amazon to find possible moves for.
 	* @return	The number of moves available to the amazon.
 	*/
 	private static int getNumberAvailableMoves(Board board, byte amazon)
 	{
 		int moves = 0;
 		/*
 		 * Given an amazon, calculate the number of moves available to it
 		 * Strategy: (can be improved I'm sure)
 		 * 	Create list of all possible moves for this amazon (will likely have a similar function somewhere else)
 		 * 	Return number of elements in the list
 		 */
 		int arow = Board.decodeAmazonRow(amazon);		//amazon's starting row position
 		int acol = Board.decodeAmazonColumn(amazon);	//amazon's starting column position
 		
 		// The following comments assume [0][0] is considered top left
 		// Find moves to the right
 		for (int c = acol+1; c < Board.SIZE; c++)
 		{
 			if (board.isOccupied(arow, c))
 			{
 				break;
 			}
 			else // this is a legal move
 			{
 				moves += findAvailableArrowPlacements(board, arow, c, arow, acol);
 			}
 		}
 		// Find moves to the left
 		for (int c = acol-1; c > -1; c--)
 		{
 			if (board.isOccupied(arow, c))
 			{
 				break;
 			}
 			else // this is a legal move
 			{
 				moves += findAvailableArrowPlacements(board, arow, c, arow, acol);
 			}
 		}
 		// Find moves below
 		for (int r = arow+1; r < Board.SIZE; r++)
 		{
 			if (board.isOccupied(r, acol))
 			{
 				break;
 			}
 			else // this is a legal move
 			{
 				moves += findAvailableArrowPlacements(board, r, acol, arow, acol);
 			}
 		}
 		// Find moves above
 		for (int r = arow-1; r > -1; r--)
 		{
 			if (board.isOccupied(r, acol))
 			{
 				break;
 			}
 			else // this is a legal move
 			{
 				moves += findAvailableArrowPlacements(board, r, acol, arow, acol);
 			}
 		}
 		// Find moves diagonally (\) to the right
 		for (int r = arow+1, c = acol+1; r < Board.SIZE && c < Board.SIZE; r++, c++)
 		{
 			if (board.isOccupied(r, c))
 			{
 				break;
 			}
 			else // this is a legal move
 			{
 				moves += findAvailableArrowPlacements(board, r, c, arow, acol);
 			}
 		}
 		// Find moves diagonally (\) to the left
 		for (int r = arow-1, c = acol-1; r > -1 && c > -1; r--, c--)
 		{
 			if (board.isOccupied(r, c))
 			{
 				break;
 			}
 			else // this is a legal move
 			{
 				moves += findAvailableArrowPlacements(board, r, c, arow, acol);
 			}
 		}
 		// Find moves anti-diagonally (/) to the right
 		for (int r = arow-1, c = acol+1; r > -1 && c < Board.SIZE; r--, c++)
 		{
 			if (board.isOccupied(r, c))
 			{
 				break;
 			}
 			else // this is a legal move
 			{
 				moves += findAvailableArrowPlacements(board, r, c, arow, acol);
 			}
 		}
 		// Find moves anti-diagonally (/) to the left
 		for (int r = arow+1, c = acol-1; r < Board.SIZE && c > -1; r++, c--)
 		{
 			if (board.isOccupied(r, c))
 			{
 				break;
 			}
 			else // this is a legal move
 			{
 				moves += findAvailableArrowPlacements(board, r, c, arow, acol);
 			}
 		}
 
 		return moves;
 	}
 
 	/**
 	 * Finds and returns the number of places an arrow can be placed from a specified square.
 	 * @param board		The current state of the board.
 	 * @param arow		The row the amazon is in.
 	 * @param acol		The column the amazon is in.
 	 * @param row_s		The row the amazon began the move in.
 	 * @param col_s		The column the amazon began the move in.
 	 * @return		The number of places an arrow can be placed from specified square.
 	 */
 	private static int findAvailableArrowPlacements(Board board, int arow, int acol, int row_s, int col_s)
 	{
 		int arrows = 0;
 		// The following comments assume [0][0] is considered top left
 		// Find moves to the right
 		for (int c = acol+1; c < Board.SIZE; c++)
 		{
 			if (board.isOccupied(arow, c) && !(arow == row_s && c == col_s))
 			{
 				break;
 			}
 			else // this is a legal move
 			{
 				arrows++;
 			}
 		}
 		// Find moves to the left
 		for (int c = acol-1; c > -1; c--)
 		{
 			if (board.isOccupied(arow, c) && !(arow == row_s && c == col_s))
 			{
 				break;
 			}
 			else // this is a legal move
 			{
 				arrows++;
 			}
 		}
 		// Find moves below
 		for (int r = arow+1; r < Board.SIZE; r++)
 		{
 			if (board.isOccupied(r, acol) && !(r == row_s && acol == col_s))
 			{
 				break;
 			}
 			else // this is a legal move
 			{
 				arrows++;
 			}
 		}
 		// Find moves above
 		for (int r = arow-1; r > -1; r--)
 		{
 			if (board.isOccupied(r, acol) && !(r == row_s && acol == col_s))
 			{
 				break;
 			}
 			else // this is a legal move
 			{
 				arrows++;
 			}
 		}
 		// Find moves diagonally (\) to the right
 		for (int r = arow+1, c = acol+1; r < Board.SIZE && c < Board.SIZE; r++, c++)
 		{
 			if (board.isOccupied(r, c) && !(r == row_s && c == col_s))
 			{
 				break;
 			}
 			else // this is a legal move
 			{
 				arrows++;
 			}
 		}
 		// Find moves diagonally (\) to the left
 		for (int r = arow-1, c = acol-1; r > -1 && c > -1; r--, c--)
 		{
 			if (board.isOccupied(r, c) && !(r == row_s && c == col_s))
 			{
 				break;
 			}
 			else // this is a legal move
 			{
 				arrows++;
 			}
 		}
 		// Find moves anti-diagonally (/) to the right
 		for (int r = arow-1, c = acol+1; r > -1 && c < Board.SIZE; r--, c++)
 		{
 			if (board.isOccupied(r, c) && !(r == row_s && c == col_s))
 			{
 				break;
 			}
 			else // this is a legal move
 			{
 				arrows++;
 			}
 		}
 		// Find moves anti-diagonally (/) to the left
 		for (int r = arow+1, c = acol-1; r < Board.SIZE && c > -1; r++, c--)
 		{
 			if (board.isOccupied(r, c) && !(r == row_s && c == col_s))
 			{
 				break;
 			}
 			else // this is a legal move
 			{
 				arrows++;
 			}
 		}
 		return arrows;
 	}
 	
 	/**
 	 * Counts the number of amazons of given colour that are within given region.
 	 * @param board		The current board position.
 	 * @param top		The first row to search in.
 	 * @param bottom	The bottom bounds of the search. This row is not searched.
 	 * @param left		The first column to search in.
 	 * @param right		The right bounds of the search. This column is not searched.
 	 * @param colour	The colour of the amazons to count.
 	 * @return		The number of amazons of the given colour found in the region.
 	 */
 	private static int findInRegion(Board board, int top, int bottom, int left, int right, int colour)
 	{
 		int count = 0;
 
 		for (int row = top; row < bottom; row++)
 		{
 			for (int col = left; col < right; col++)
 			{
 				if (colour == Board.WHITE)
 				{
 					if (board.isWhite(row, col))
 						count++;
 				}
 				else
 				{
 					if (board.isBlack(row, col))
 						count++;
 				}
 			}
 		}
 		
 		return count;
 	}
 }
