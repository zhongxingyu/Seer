 package mitzi;
 
 import static mitzi.MateScores.NEG_INF;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 /**
  * 
  * This class computes the value of a board in a proper way, see
  * http://philemon.cycovery.com/site/part2.html for more details.
  * 
  */
 public class BoardAnalyzer implements IPositionAnalyzer {
 
 	// the square to array index from Position.java
 	protected static int[] square_to_array_index = { 64, 64, 64, 64, 64, 64,
 			64, 64, 64, 64, 64, 56, 48, 40, 32, 24, 16, 8, 0, 64, 64, 57, 49,
 			41, 33, 25, 17, 9, 1, 64, 64, 58, 50, 42, 34, 26, 18, 10, 2, 64,
 			64, 59, 51, 43, 35, 27, 19, 11, 3, 64, 64, 60, 52, 44, 36, 28, 20,
 			12, 4, 64, 64, 61, 53, 45, 37, 29, 21, 13, 5, 64, 64, 62, 54, 46,
 			38, 30, 22, 14, 6, 64, 64, 63, 55, 47, 39, 31, 23, 15, 7, 64, 64,
 			64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
 			64, 64, 64 };
 
 	// The following arrays contains the value of a piece on a specific square,
 	// always in favor of white. Since the arrays are symmetric w.r.t. the
 	// columns, BLACK uses 63-i entry with opposite sign.
 	static private int[] piece_activity_b_k = { -16, -16, -8, -8, -8, -8, -16,
 			-16, -16, -16, -4, -4, -4, -4, -16, -16, -8, 2, 6, 6, 6, 6, 2, -8,
 			-8, 2, 6, 6, 6, 6, 2, -8, -8, 2, 4, 4, 4, 4, 2, -8, -8, 2, 2, 2, 2,
 			2, 2, -8, -8, -8, 0, 0, 0, 0, -8, -8, -16, -8, -8, -8, -8, -8, -8,
 			-16 };
 
 	static private int[] piece_activity_r = { 0, 0, 4, 6, 6, 4, 0, 0, 0, 0, 4,
 			6, 6, 4, 0, 0, 0, 0, 4, 6, 6, 4, 0, 0, 0, 0, 4, 6, 6, 4, 0, 0, 0,
 			0, 4, 6, 6, 4, 0, 0, 0, 0, 4, 6, 6, 4, 0, 0, 0, 0, 4, 6, 6, 4, 0,
 			0, 0, 0, 4, 6, 6, 4, 0, 0, };
 
 	static private int[] piece_activity_q = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4,
 			5, 5, 4, 0, 0, 0, 2, 4, 10, 10, 4, 2, 0, 0, 2, 10, 12, 12, 10, 2,
 			0, -10, 2, 10, 12, 12, 10, 2, -10, -10, -10, 4, 10, 10, 4, -10,
 			-10, -10, 2, 8, 8, 8, 8, 2, -10, -10, -8, 0, 0, 0, 0, -8, -10, };
 
 	static private int[] weak_positions = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 			0, 0, 0, 0, 0, 0, 8, 12, 12, 8, 0, 0, 0, 2, 12, 16, 16, 12, 2, 0,
 			0, 2, 12, 20, 20, 12, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, };
 
 	static private int[] piece_values = { 100, 500, 325, 325, 975, 000 };
 
 	// if not all Bishop and Knight has moved, moving the queen results in
 	// negative score
 	static private int PREMATURE_QUEEN = -17; // not yet implemented
 
 	// bonus, if the rook is on an open line (no other pawns)
 	static private int ROOK_OPEN_LINE = 20;
 
 	// bonus if the rook is on an halfopen line (only opponents pawns)
 	static private int ROOK_HALFOPEN_LINE = 5;
 
 	// bonus if the rook is in the 7th row and either opponents king is in the
 	// 8th or pawn in the 7th
 	static private int ROOK_7TH_2ND = 25;
 
 	// bonus if the previous bonus holds and the 7th row is empty. (hopefully)
 	static private int ROOK_7TH_2ND_ABSOLUTE = 15;
 
 	// bonus if a rook covers the other rook, this replaces the
 	// ROOK_7TH_2ND and counts for each rook (both on the 7th row)
 	static private int REINFORCED_ROOK_7TH_2ND = 40;
 
 	static private int PASSED_ROOK_SUPPORT = 10; // not yet implemented
 	static private int ENDGAME_BISHOP_BONUS = 10; // not yet implemented
 	static private int BISHOP_BASELINE_CAGED = -12; // not yet implemented
 
 	// bonus if a queen is covered on the 7th row by a rook
 	static private int REINFORCING_QUEEN_7TH_2ND = 20;
 
 	// The player receives a bonus if the 2 bishops are alive.
 	static private int bishop_pair_value = 25;
 
 	static public long eval_counter_seldepth = 0;
 
 	@Override
 	public AnalysisResult eval0(IPosition board) {
 		int score = 0;
 		board.cacheOccupiedSquares();
 
 		// Evaluate Diagonals and lines
 		score += evalLinesAndDiagonals(board);
 
 		// Evaluate position - activity
 		score += evalPieceActivity(board);
 
 		// Evaluate weak/strong position
 		score += evalWeakPosition(board);
 
 		// Evaluate the pieces
 		score += evalPieces(board);
 
 		AnalysisResult result = new AnalysisResult(score, false, false, 0, 0,
 				Flag.EXACT);
 		return result;
 	}
 
 	@Override
 	public AnalysisResult evalBoard(IPosition position, int alpha, int beta) {
 		AnalysisResult result = quiesce(position, alpha, beta);
		//The analysis result should always contain the pure value (not perturbed via side_sign)
		//result.score *= Side.getSideSign(position.getActiveColor());
 
 		return result;
 	}
 
 	/**
 	 * Implements Quiescence search to avoid the horizon effect. The function
 	 * increase the search depth until no capture is possible, where only
 	 * captures are analyzed. The optimal value is found using the negamax
 	 * algorithm.
 	 * 
 	 * @see <a
 	 *      href="http://chessprogramming.wikispaces.com/Quiescence+Search">http://chessprogramming.wikispaces.com/Quiescence+Search</a>
 	 * 
 	 * @param position
 	 *            the position to be analyzed
 	 * @param alpha
 	 *            the alpha value of alpha-beta search
 	 * @param beta
 	 *            the beta value of alpha-beta search
 	 * @return the value of the board
 	 */
 	private AnalysisResult quiesce(IPosition position, int alpha, int beta) {
 
 		int side_sign = Side.getSideSign(position.getActiveColor());
 
 		// generate moves
 		List<IMove> moves = position.getPossibleMoves();
 
 		// check for mate and stalemate
 		if (moves.isEmpty()) {
 			eval_counter_seldepth++;
 			if (position.isCheckPosition()) {
 				return new AnalysisResult(NEG_INF * side_sign, false, false, 0,
 						0, Flag.EXACT);
 			} else {
 				return new AnalysisResult(0, true, false, 0, 0, Flag.EXACT);
 			}
 		}
 
 		AnalysisResult standing_pat = eval0(position);
 		eval_counter_seldepth++;
 
 		int negaval = standing_pat.score * side_sign;
 
 		// alpha beta cutoff
 		if (negaval >= beta)
 			return standing_pat;
 		alpha = Math.max(alpha, negaval);
 
 		List<IMove> caputures = position.generateCaptures();
 
 		BasicMoveComparator move_comparator = new BasicMoveComparator(position);
 
 		// no previous computation given, use basic heuristic
 		ArrayList<IMove> ordered_captures = new ArrayList<IMove>(caputures);
 		Collections.sort(ordered_captures,
 				Collections.reverseOrder(move_comparator));
 
 		for (IMove move : ordered_captures) {
 			IPosition pos = position.doMove(move).new_position;
 			AnalysisResult result = quiesce(pos, -beta, -alpha);
 
 			negaval = result.score * side_sign;
 
 			if (negaval >= beta) {
 				result.plys_to_seldepth++;
 				return result;
 			}
 			alpha = Math.max(alpha, negaval);
 
 		}
 
		//The analysis result should always contain the pure value (not perturbed via side_sign)
		return new AnalysisResult(negaval*side_sign, false, false, 0, 1, Flag.EXACT);
 
 	}
 
 	/**
 	 * Evaluates only the material value of the board.
 	 * 
 	 * @param board
 	 *            the actual board
 	 * @return the material value
 	 */
 	private int evalPieces(IPosition board) {
 		int score = 0;
 
 		// basic evaluation
 		for (Side side : Side.values()) {
 			int side_sign = Side.getSideSign(side);
 
 			// piece values
 			for (Piece piece : Piece.values()) {
 				score += board.getNumberOfPiecesByColorAndType(side, piece)
 						* piece_values[piece.ordinal()] * side_sign;
 			}
 
 			// bishop pair gives bonus
 			if (board.getNumberOfPiecesByColorAndType(side, Piece.BISHOP) == 2) {
 				score += bishop_pair_value * side_sign;
 			}
 		}
 
 		return score;
 	}
 
 	/**
 	 * Computes the value of the possible activity of the pieces, e.g.
 	 * centralization,...
 	 * 
 	 * @param board
 	 *            the board to be analyzed
 	 * @return the score for the activity of Rook, Bishop, Knight, Queen
 	 */
 	private int evalPieceActivity(IPosition board) {
 		int score = 0;
 		Set<Integer> squares;
 		boolean queen_moved_last, queen_startpos;
 
 		for (Side side : Side.values()) {
 			int side_sign = Side.getSideSign(side);
 			queen_moved_last = true;
 			queen_startpos = false;
 
 			// Queen
 			squares = board.getOccupiedSquaresByColorAndType(side, Piece.QUEEN);
 			if (side == Side.WHITE)
 				for (int squ : squares)
 					score += piece_activity_q[square_to_array_index[squ]];
 			else
 				for (int squ : squares)
 					score -= piece_activity_q[63 - square_to_array_index[squ]];
 
 			if ((squares.contains(SquareHelper.getSquare(
 					SquareHelper.getRowForSide(side, 1), 4))))
 				queen_startpos = true;
 
 			// Bishop
 			squares = board
 					.getOccupiedSquaresByColorAndType(side, Piece.BISHOP);
 			if (side == Side.WHITE)
 				for (int squ : squares)
 					score += piece_activity_b_k[square_to_array_index[squ]];
 			else
 				for (int squ : squares)
 					score -= piece_activity_b_k[63 - square_to_array_index[squ]];
 
 			if (!queen_startpos
 					&& (squares.contains(SquareHelper.getSquare(
 							SquareHelper.getRowForSide(side, 1), 3)) || squares
 							.contains(SquareHelper.getSquare(
 									SquareHelper.getRowForSide(side, 1), 3))))
 				queen_moved_last = false;
 
 			// Knight
 			squares = board
 					.getOccupiedSquaresByColorAndType(side, Piece.KNIGHT);
 			if (side == Side.WHITE)
 				for (int squ : squares)
 					score += piece_activity_b_k[square_to_array_index[squ]];
 			else
 				for (int squ : squares)
 					score -= piece_activity_b_k[63 - square_to_array_index[squ]];
 
 			if (!queen_startpos
 					&& (squares.contains(SquareHelper.getSquare(
 							SquareHelper.getRowForSide(side, 1), 2)) || squares
 							.contains(SquareHelper.getSquare(
 									SquareHelper.getRowForSide(side, 1), 2))))
 				queen_moved_last = false;
 
 			// Rook
 			squares = board.getOccupiedSquaresByColorAndType(side, Piece.ROOK);
 			if (side == Side.WHITE)
 				for (int squ : squares)
 					score += piece_activity_r[square_to_array_index[squ]];
 			else
 				for (int squ : squares)
 					score -= piece_activity_r[63 - square_to_array_index[squ]];
 
 			if (!queen_startpos && !queen_moved_last)
 				score += side_sign * PREMATURE_QUEEN;
 
 		}
 		return score;
 	}
 
 	/**
 	 * this function evaluates the weak position of an outpost (?), however only
 	 * for bishop and knight. If a knight is covered by pawn, the value
 	 * increases.
 	 * 
 	 * @param board
 	 *            the board to be analyzed
 	 * @return the score w.r.t. weak/ strong positions
 	 */
 	private int evalWeakPosition(IPosition board) {
 
 		int score = 0;
 		Set<Integer> squares;
 
 		for (Side side : Side.values()) {
 
 			// Bishop
 			squares = board
 					.getOccupiedSquaresByColorAndType(side, Piece.BISHOP);
 			if (side == Side.WHITE)
 				for (int squ : squares)
 					score += weak_positions[square_to_array_index[squ]];
 			else
 				for (int squ : squares)
 					score -= weak_positions[63 - square_to_array_index[squ]];
 
 			// Knight (value get multiplied times the number of pawn covering
 			// the knight)
 			squares = board
 					.getOccupiedSquaresByColorAndType(side, Piece.BISHOP);
 			int count = 0;
 			if (side == Side.WHITE) {
 				for (int squ : squares) {
 					for (Direction dir : Direction
 							.pawnCapturingDirections(Side.BLACK))
 						if (board.getPieceFromBoard(squ + dir.offset) == Piece.PAWN)
 							count++;
 					score += count * weak_positions[square_to_array_index[squ]];
 				}
 			} else {
 				for (int squ : squares) {
 					for (Direction dir : Direction
 							.pawnCapturingDirections(Side.WHITE))
 						if (board.getPieceFromBoard(squ + dir.offset) == Piece.PAWN)
 							count++;
 					score -= count
 							* weak_positions[63 - square_to_array_index[squ]];
 				}
 			}
 
 		}
 
 		return score;
 	}
 
 	private int evalLinesAndDiagonals(IPosition board) {
 		int score = 0;
 
 		Set<Integer> squares_rook;
 		Piece p;
 		Side s;
 
 		for (Side side : Side.values()) {
 			int side_sign = Side.getSideSign(side);
 			Side opp_side = Side.getOppositeSide(side);
 
 			squares_rook = board.getOccupiedSquaresByColorAndType(side,
 					Piece.ROOK);
 
 			// Open line and halfopen line bonus
 			for (int square : squares_rook) {
 
 				boolean half_open = true;
 				boolean open = true;
 				List<Integer> squares = new ArrayList<Integer>(
 						SquareHelper.getAllSquaresInDirection(square,
 								Direction.NORTH));
 				squares.addAll(SquareHelper.getAllSquaresInDirection(square,
 						Direction.SOUTH));
 
 				for (int squ : squares) {
 					if (board.getPieceFromBoard(squ) == Piece.PAWN) {
 						if (board.getSideFromBoard(squ) == board
 								.getActiveColor()) {
 							half_open = false;
 							open = false;
 							break;
 						} else
 							open = false;
 					}
 				}
 				if (half_open && open)
 					score += side_sign * ROOK_OPEN_LINE;
 				else if (half_open)
 					score += side_sign * ROOK_HALFOPEN_LINE;
 
 				// 7th || 2nd line bonus
 				if (SquareHelper.getRow(square) == SquareHelper.getRowForSide(
 						side, 7)) {
 
 					boolean rook_7 = true;
 					boolean rook_7_abs = true;
 					boolean rook_7_cover_r = false;
 					boolean rook_7_cover_q = false;
 					boolean emty_direction = true;
 
 					// Check all squares at the west side of the rook
 					for (int squ : SquareHelper.getAllSquaresInDirection(
 							square, Direction.WEST)) {
 						p = board.getPieceFromBoard(squ);
 						s = board.getSideFromBoard(squ);
 						if (p == Piece.PAWN && s == opp_side) {
 							rook_7 = false;
 							rook_7_abs = false;
 							break;
 						} else if (emty_direction && p != null) {
 							emty_direction = false;
 							rook_7_abs = false;
 							if (p == Piece.ROOK && s == side)
 								rook_7_cover_r = true;
 							else if (p == Piece.QUEEN && s == side)
 								rook_7_cover_q = true;
 						}
 
 					}
 
 					// Check all squares at the west side of the rook
 					emty_direction = true;
 					for (int squ : SquareHelper.getAllSquaresInDirection(
 							square, Direction.EAST)) {
 						p = board.getPieceFromBoard(squ);
 						s = board.getSideFromBoard(squ);
 						if (rook_7 && p == Piece.PAWN && s == opp_side) {
 							rook_7 = false;
 							rook_7_abs = false;
 							break;
 						} else if (emty_direction && p != null) {
 							rook_7_abs = false;
 							emty_direction = false;
 							if (p == Piece.ROOK && s == side)
 								rook_7_cover_r = true;
 							else if (p == Piece.QUEEN && s == side)
 								rook_7_cover_q = true;
 						}
 
 					}
 
 					int king_pos = board.getKingPos(opp_side);
 					if (SquareHelper.getRow(king_pos) == SquareHelper
 							.getRowForSide(side, 8))
 						rook_7 = true;
 
 					if (rook_7)
 						score += side_sign * ROOK_7TH_2ND;
 					if (rook_7_abs)
 						score += side_sign * ROOK_7TH_2ND_ABSOLUTE;
 					if (rook_7_cover_r)
 						score += side_sign * REINFORCED_ROOK_7TH_2ND;
 					if (rook_7_cover_q)
 						score += side_sign * REINFORCING_QUEEN_7TH_2ND;
 				}
 
 			}
 		}
 		return score;
 	}
 }
