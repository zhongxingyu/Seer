 package snozama.amazons.mechanics.algo;
 
 import java.util.Arrays;
 
 import snozama.amazons.global.GlobalFunctions;
 import snozama.amazons.mechanics.Board;
 import snozama.amazons.mechanics.MoveManager;
 import snozama.amazons.mechanics.SnozamaHeuristic;
 import snozama.amazons.mechanics.transtable.ZobristTTable;
 
 /**
  * Class containing NegaScout search as described in Qian Liang's paper.
  * 
  * @author Cody Clerke
  *
  */
 
 public class TranspositionNegaScout {
 	
 	public static int POS_INFINITY = Integer.MAX_VALUE-2;
 	public static int NEG_INFINITY = Integer.MIN_VALUE+2;
 	
 	public static int firstN = 500; //for possible use with N-best selection search
 	
 	// Statistical fields.
 	public int nodes = 0;
 	public int depthCompleted;
 	
 	int[] bestMoves = new int[20]; //FIXME hard-coded as 20 for testing
 	int[] scores = new int[2176];
 	
 	ZobristTTable table;
 	int zkey;
 	
 	long endTime;
 	
 	int currentRoot;
 	
 	boolean gotoEnd;
 	
 	public TranspositionNegaScout(long end, int tableSize, Board startBoard)
 	{
 		table = new ZobristTTable(tableSize);
 		zkey = table.computeBoardHash(startBoard);
 		
 		Arrays.fill(scores, NEG_INFINITY);
 		depthCompleted = 0;
 		endTime = end;
 		
 		gotoEnd = false;
 	}
 	
 	/**
 	 * Chooses the best move for Snozama based on search algorithm.
 	 * @param board		The current board position.
 	 * @param colour	The player for whom to find a move for.
 	 * @param turn		The current ply of the game.
 	 * @return			Returns the best move found by the search algorihm.
 	 */
 	public int chooseMove(Board board, int colour, int turn)
 	{
 		int maxDepth = 2;
 		//probably don't need to keep the score?
 		//NegaScoutSearch(board, 0, maxDepth, NEG_INFINITY, POS_INFINITY, colour, turn);
 		//return bestMoves[0];
 		
 		//Iterative deepening NegaScout (plays better than regular fixed depth search)
 		return IDNegaScoutSearch(board, colour, turn);
 	}
 	
 	/**
 	 * NegaScout search algorithm.
 	 * @param board			The current board position.
 	 * @param depth			The starting depth of the search. Should always start at <code>depth = 0</code>.
 	 * @param maxDepth		The maximum depth to be searched.
 	 * @param alpha			The lower bound of the search window.
 	 * @param beta			The upper bound of the search window.
 	 * @param colour		The active player's colour.
 	 * @param turn			The current ply of the game.
 	 * @return				Returns the score of the best move.
 	 */
 	public int NegaScoutSearch(Board board, int depth, int maxDepth, int alpha, int beta, int colour, int turn)
 	{
 		int origAlpha = alpha;
 		int zrecord[];
 		
 		
 		// Check transposition table for previous board position.
 		// Ensure data is correct.
 		/// Transposition table code ///////////////////////////////////////////
 		// TODO: Is depth calculation right here?
 		// TODO: Only check if the position is actually this position.
 		if ((zrecord = table.get(zkey))[ZobristTTable.DEPTH] >= maxDepth - depth)
 		{
 			switch (zrecord[ZobristTTable.FLAG])
 			{
 				case ZobristTTable.UPPER_BOUND:
 					alpha = GlobalFunctions.max(alpha, zrecord[ZobristTTable.SCORE]);
 					break;
 				case ZobristTTable.LOWER_BOUND:
 					beta  = GlobalFunctions.min(beta, zrecord[ZobristTTable.SCORE]);
 					break;
 				case ZobristTTable.EXACT_SCORE:
 					return zrecord[ZobristTTable.SCORE];
 			}
 			if (alpha >= beta)
 			{
 				return alpha;
 			}
 		}
 		////////////////////////////////////////////////////////////////////////
 		
 		int next = 0;
 		
 		if (depth == maxDepth || board.isTerminal())
 		{
 			int value = SnozamaHeuristic.evaluateBoard(board, colour, turn);
 			
 			if (value > scores[currentRoot])
 			{
 				scores[currentRoot] = value;
 			}
 			return value;
 		}
 		
		int score = NEG_INFINITY;
 		int row_s;
 		int col_s;
 		
 		/// Transposition table code - attempt found value FIRST ///////////////
		if (zrecord[ZobristTTable.DEPTH] > -1)	// TODO: Only check if the position is actually this position.
 		{
 			 score = Integer.MIN_VALUE;
 			int aindex = MoveManager.getAmazonIndexFromUnmanagedMove(zrecord[ZobristTTable.MOVE], board);
 			row_s = Board.decodeAmazonRow(board.amazons[colour][aindex]);
 			col_s = Board.decodeAmazonColumn(board.amazons[colour][aindex]);
 			
 			MoveManager.applyUnmanagedMove(board, zrecord[ZobristTTable.MOVE]);
 			
 			int current = -NegaScoutSearch(board, depth+1, maxDepth, -beta, -alpha, GlobalFunctions.flip(colour), turn+1);
 			if (current > score)
 			{
 				score = current;
 			}
 			if (score > alpha)
 			{
 				alpha = score;
 				bestMoves[depth] = zrecord[ZobristTTable.MOVE];
 			}
 			
 			if (alpha >= beta)
 			{
 				//gotoEnd = true;
 			}
 			else
 			{
 				scores[currentRoot] = score;
 			}
 			
 			MoveManager.undoUnmanagedMove(board, zrecord[ZobristTTable.MOVE], row_s, col_s);
 		}
 		////////////////////////////////////////////////////////////////////////
 		
 		int b = beta;
 		MoveManager successors = board.getSuccessors(colour); //generate successors
 		
 		// Move ordering for iterative deepening.
 		if (next == 0 && depth == 0 && maxDepth > 1)
 		{
 			successors.sort(scores);
 		}
 
 		while (!gotoEnd && successors.hasIterations() && System.currentTimeMillis() < endTime) //for each move or until turn time runs out
 		{
 			next = successors.nextIterableIndex();
 			
 			if (depth == 0)
 			{
 				currentRoot = next;
 				scores[currentRoot] = alpha;
 			}
 			
 			row_s = Board.decodeAmazonRow(board.amazons[colour][successors.getAmazonIndex(next)]);
 			col_s = Board.decodeAmazonColumn(board.amazons[colour][successors.getAmazonIndex(next)]);
 			successors.applyMove(board, next); //execute current move
 			nodes++;
 			
 			int current = -NegaScoutSearch(board, depth+1, maxDepth, -b, -alpha, GlobalFunctions.flip(colour), turn+1);
 			
 			if (current > score)
 			{
 				if (b == beta || maxDepth - depth <= 2)
 					score = current;
 				else
 					score = -NegaScoutSearch(board, depth+1, maxDepth, -beta, -current, GlobalFunctions.flip(colour), turn+1); //re-search
 			}
 			
 			if (score > alpha)
 			{
 				alpha = score; //adjust the search window
 				
 				bestMoves[depth] = successors.getMove(next);
 			}			
 			
 			successors.undoMove(board, next, row_s, col_s); //retract current move
 			
 			if (alpha >= beta)
 			{
 				gotoEnd = true; //cut off
 			}
 			
 			// Update scores array.
 			if (!gotoEnd)
 			{
 				scores[currentRoot] = score;
 			}
 			
 			b = alpha + 1;
 		}
 		
 		/// Transposition table code ///////////////////////////////////////////
 		if (score <= alpha)
 		{
 			zrecord[ZobristTTable.FLAG] = ZobristTTable.LOWER_BOUND;
 		} else if (score >= beta)
 		{
 			zrecord[ZobristTTable.FLAG] = ZobristTTable.UPPER_BOUND;
 		}
 		else
 		{
 			zrecord[ZobristTTable.FLAG] = ZobristTTable.EXACT_SCORE;
 		}
 		////////////////////////////////////////////////////////////////////////
 		
 		return score;
 	}
 	
 	/**
 	 * Iterative deepening NegaScout search.
 	 * @param board		The current board position.
 	 * @param colour	The active player's colour.
 	 * @param turn		The current ply of the game.
 	 * @return		Returns the best move found for the current turn from the deepest fully searched depth.
 	 */
 	public int IDNegaScoutSearch(Board board, int colour, int turn)
 	{
 		int depth = 1;
 		int[] bestScore = new int[20];	// Really an array of best moves at a given depth.  TODO: Rename.
 		while (depth <= 20 && System.currentTimeMillis() < endTime)
 		{
 			NegaScoutSearch(board, 0, depth, NEG_INFINITY, POS_INFINITY, colour, turn);
 			bestScore[depth-1] = bestMoves[0]; //store best move for each depth
 			depthCompleted = depth;
 			depth++;
 		}
 		boolean found = false;
 		for (int i = bestScore.length-1; i >= 0; i--)
 		{
 			if (found)
 				return bestScore[i]; //return best move for deepest complete search
 			else if (bestScore[i] != 0) //first non-zero result will be best move for deepest partially complete search
 				found = true;
 		}
 		return -1; //no move found, ERROR!
 	}
 }
