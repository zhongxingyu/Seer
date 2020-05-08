 /**
  * Evaluates the board based on the locations of
  * possible future wins. More specifically, looks at all
  * the empty squares on the board and determines if they 
  * are one piece away from a win.
  * 
  * @author Steve Pennington and Julie Sparrow
  *
  */
 public class FutureWinEvaluator implements Evaluator {
 	
 	private static byte[] scoreMap = new byte[256];
 	private static byte[] maxMap = new byte[1<<10 + 1];
 	private static int[] maxMask = new int[2];
 	
 	private static FutureWinEvaluator instance = new FutureWinEvaluator();
 
 	
 	private FutureWinEvaluator() {
 		// Map for all possible scoring combinations
 		//RRGS
 		scoreMap[0x5C] = 5;
 		//RRSG
 		scoreMap[0x53] = 5;
 		//GSRR
 		scoreMap[0xC5] = 5;
 		//SGRR
 		scoreMap[0x35] = 5;
 		//RGSR
 		scoreMap[0x71] = 4;
 		//RSGR
 		scoreMap[0x4D] = 4;
 		//GRRS
 		scoreMap[0xD4] = 4;
 		//SRRG
 		scoreMap[0x1D] = 4;
 		//RGRS
 		scoreMap[0x74] = 3;
 		//RSRG
 		scoreMap[0x47] = 3;
 		//GRSR
 		scoreMap[0xD1] = 3;
 		//SRGR
 		scoreMap[0x1D] = 3;
 		//GGRS
 		scoreMap[0xF4] = 5;
 		//GGSR
 		scoreMap[0xF1] = 5;
 		//RSGG
 		scoreMap[0x4F] = 5;
 		//SRGG
 		scoreMap[0x1F] = 5;
 		//GRSG
 		scoreMap[0xD3] = 4;
 		//GSRG
 		scoreMap[0xC7] = 4;
 		//RGGS
 		scoreMap[0x7C] = 4;
 		//SGGR
 		scoreMap[0x3D] = 4;
 		//GRGS
 		scoreMap[0xDC] = 3;
 		//GSGR
 		scoreMap[0xCD] = 3;
 		//RGSG
 		scoreMap[0x73] = 3;
 		//SGRG
 		scoreMap[0x37] = 3;
 		
 		//BBGS
 		scoreMap[0xAC] = -5;
 		//BBSG
 		scoreMap[0xA3] = -5;
 		//GSBB
 		scoreMap[0xCA] = -5;
 		//SGBB
 		scoreMap[0x3A] = -5;
 		//BGSB
 		scoreMap[0xB2] = -4;
 		//BSGB
 		scoreMap[0x8E] = -4;
 		//GBBS
 		scoreMap[0xE8] = -4;
 		//SBBG
 		scoreMap[0x2B] = -4;
 		//BGBS
 		scoreMap[0xB8] = -3;
 		//BSBG
 		scoreMap[0x8B] = -3;
 		//GBSB
 		scoreMap[0xE2] = -3;
 		//SBGB
 		scoreMap[0x2E] = -3;
 		//GGBS
 		scoreMap[0xF8] = -5;
 		//GGSB
 		scoreMap[0xF2] = -5;
 		//BSGG
 		scoreMap[0x8F] = -5;
 		//SBGG
 		scoreMap[0x2F] = -5;
 		//GBSG
 		scoreMap[0xE3] = -4;
 		//GSBG
 		scoreMap[0xCB] = -4;
 		//BGGS
 		scoreMap[0xBC] = -4;
 		//SGGB
 		scoreMap[0x3E] = -4;
 		//GBGS
 		scoreMap[0xEC] = -3;
 		//GSGB
 		scoreMap[0xCE] = -3;
 		//BGSG
 		scoreMap[0xB3] = -3;
 		//SGBG
 		scoreMap[0x3B] = -3;
 		
 		//when determine winners bits are marked
 		//if a certain "type" of win has occurred.
 		//These bits are
 		//10: 5 point win (red)
 		// 9: 4 point win (red)
 		// 8: 3 point win (red)
 		// 5: no winner
 		// 2: -3 point win (blue)
 		// 1: -4 point win (blue)
 		// 0: -5 point win (blue)
 		//
 		//Consider only bits 10, 9, and 8. Used when
 		//it is determined that red is the winner.
 		maxMask[0] = 0x7 << 8;
 		//Consider only bits 0, 1, and 2. Used when it
 		//is determined that blue is the winner.
 		maxMask[1] = 0x7;
 		
 		
 		//Map winning combinations to their
 		//correct values. This map is used to determine
 		//a players score given their "collection" of wins.
 		//Essentially this is a map that provides an efficient
 		//way of computing the maximum win
 		//
 		//If the player has a 5 point win they get 5 points
 		maxMap[1<<10] = 5;
 		maxMap[1<<10 | 1<<9] = 5;
 		maxMap[1<<10 | 1<<8] = 5;
 		maxMap[1<<10 | 1<<9 | 1<<8] = 5;
 		//4 point wins take precedence of 3 point wins
 		maxMap[1<<9] = 4;
 		maxMap[1<<9 | 1<<8] = 4;
 		//3 point wins occur only if no 4 point or 5 point
 		//wins were also encountered
 		maxMap[1<<8] = 3;
 		
 		//same rational as the 5, 4, and 3 point wins but now
 		//considering the case where blue has won
 		maxMap[1] = -5;
 		maxMap[1 | 1<<1] = -5;
 		maxMap[1 | 1<<2] = -5;
 		maxMap[1 | 1<<1 | 1<<2] = -5;
 		
 		maxMap[1<<1] = -4;
 		maxMap[1<<1 | 1<<2] = -4;
 		
 		maxMap[1<<2] = -3;
 	}
 	
 	public static FutureWinEvaluator getInstance() {
 		return instance;
 	}
 	
 	private int isWin(Board board, int col, int row) {
 		//used to keep track of the cumulative score
 		//for both players
 		byte total = 0;
 		
 		//stores the "type" of wins by each opponent
 		//bit mappings are explained in the maxMask comment
 		int winValues = 0;
 		
 		byte score;
 		
 		//Each possible winning combination is added to the cumulative
 		//score and the value is recorded in winValue
 		// horizontal
 		score = score(board.get(col - 3, row),
 				board.get(col - 2, row),
 				board.get(col - 1, row),
 				board.get(col, row));
 		total += score;
 		winValues |= 1 << (score + 5);
 		score = score(board.get(col - 2, row),
 				board.get(col - 1, row),
 				board.get(col, row),
 				board.get(col + 1, row));
 		total += score;
 		winValues |= 1 << (score + 5);
 		score = score(board.get(col - 1, row),
 				board.get(col, row),
 				board.get(col + 1, row),
 				board.get(col + 2, row));
 		total += score;
 		winValues |= 1 << (score + 5);
 		score = score(board.get(col, row),
 				board.get(col + 1, row),
 				board.get(col + 2, row),
 				board.get(col + 3, row));
 		total += score;
 		winValues |= 1 << (score + 5);
 
 		// vertical
 		score = score(board.get(col, row),
 				board.get(col, row - 1),
 				board.get(col, row - 2),
 				board.get(col, row - 3));
 		total += score;
 		winValues |= 1 << (score + 5);
 
 		// bottom left to top right
 		score = score(board.get(col - 3, row - 3),
 				board.get(col - 2, row - 2),
 				board.get(col - 1, row - 1),
 				board.get(col, row));
 		total += score;
 		winValues |= 1 << (score + 5);
 
 		score = score(board.get(col - 2, row - 2),
 				board.get(col - 1, row - 1),
 				board.get(col, row),
 				board.get(col + 1, row + 1));
 		total += score;
 		winValues |= 1 << (score + 5);
 		score = score(board.get(col - 1, row - 1),
 				board.get(col, row),
 				board.get(col + 1, row + 1),
 				board.get(col + 2, row + 2));
 		total += score;
 		winValues |= 1 << (score + 5);
 		score = score(board.get(col, row),
 				board.get(col + 1, row + 1),
 				board.get(col + 2, row + 2),
 				board.get(col + 3, row + 3));
 		total += score;
 		winValues |= 1 << (score + 5);
 
 		// top left to bottom right
 		score = score(board.get(col - 3, row + 3),
 				board.get(col - 2, row + 2),
 				board.get(col - 1, row + 1),
 				board.get(col, row));
 		total += score;
 		winValues |= 1 << (score + 5);
 		score = score(board.get(col - 2, row + 2),
 				board.get(col - 1, row + 1),
 				board.get(col, row),
 				board.get(col + 1, row - 1));
 		total += score;
 		winValues |= 1 << (score + 5);
 		score = score(board.get(col - 1, row + 1),
 				board.get(col, row),
 				board.get(col + 1, row - 1),
 				board.get(col + 2, row - 2));
 		total += score;
 		winValues |= 1 << (score + 5);
 		score = score(board.get(col, row),
 				board.get(col + 1, row - 1),
 				board.get(col + 2, row - 2),
 				board.get(col + 3, row - 3));
 		total += score;
 		winValues |= 1 << (score + 5);
 		
 		//if total is 0 we either have a tie or no wins.
 		//if winValues contains a win we have a tie.
 		//if winValues contains no wins the maxMap will
 		//give a score of 0 indicating no winner
		if(total == 0 && (winValues != 1 << 5)) {
			//Return 0 because ties are of no value for a future win
 			return 0;
 		} else {
 			//Mask winValues so we only consider relevant bits, then look
 			//these up to determine the win value.
 			return maxMap[winValues & maxMask[(total & (1 << 7)) >> 7]];
 		}
 	}
 
 	private byte score(byte a, byte b, byte c, byte d) {
 		return scoreMap[(a << 6) | (b << 4) | (c << 2) | d];
 	}
 
 	/* (non-Javadoc)
 	 * @see Evaluator#evaluate(Board, int)
 	 */
 	@Override
 	public int evaluate(Board board, int lastCol) {
 		int columns = board.getWidth();
 		int rows = board.getHeight();
 		
 		int[][] scores = new int[columns][rows];
 		int score = 0;
 		
 		for(int i=0; i<columns; i++) {
 			for(int j=board.getTop(i); j<rows; j++) {
 				scores[i][j] = isWin(board, i, j);
 			}
 		}
 		
 		int height = board.getHeight();
 		
 		//TODO: need to weight the height or something. Even if there is a win at the bottom 
 		//of a column we should still consider the top
 		for(int i=0; i<columns; i++) {
 			for(int j=board.getTop(i); j<rows-1 && j - board.getTop(i)<height; j++) {
 				if(scores[i][j] != 0) {
 					if((scores[i][j] > 0 && scores[i][j+1] > 0) || (scores[i][j] < 0 && scores[i][j+1] < 0)) {
 						score += Math.min(scores[i][j], scores[i][j+1]) * 100;
 						height = j - board.getTop(i);
 						break;
 					} else {
 						score += scores[i][j];
 						break;
 					}
 				}
 			}
 		}
 		
 		return score;
 	}
 }
