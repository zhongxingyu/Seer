 package mitzi;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import mitzi.UCIReporter.InfoType;
 
 public class MitziBrain implements IBrain {
 
 	private static int POS_INF = +1000000000;
 	private static int NEG_INF = -1000000000;
 
 	private IBoard board;
 
 	private Variation principal_variation;
 
 	private long eval_counter;
 
 	@Override
 	public void set(IBoard board) {
 		this.board = board;
 		this.eval_counter = 0;
 		this.principal_variation = null;
 	}
 
 	/**
 	 * Sends updates about evaluation status to UCI GUI.
 	 * 
 	 */
 	class UCIUpdater extends TimerTask {
 
 		private long old_mtime;
 		private long old_eval_counter;
 
 		@Override
 		public void run() {
 			long mtime = System.currentTimeMillis();
 			long eval_span = eval_counter - old_eval_counter;
 
 			if (old_mtime != 0) {
 				long time_span = mtime - old_mtime;
 				UCIReporter.sendInfoNum(InfoType.NPS, eval_span * 1000
 						/ time_span);
 			}
 
 			old_mtime = mtime;
 			old_eval_counter += eval_span;
 
 		}
 	}
 
 	/**
 	 * 
 	 * This Comparator class implements the comparing of two moves. The moves
 	 * are saved intern as an ArrayList for a simpler search.
 	 * 
 	 */
 	class MoveComperator implements Comparator<IMove> {
 
 		private double[] board_values;
 		private ArrayList<IMove> moves;
 
 		public void compute_values(Set<IMove> moves, IBoard board, int side_sign) {
 			this.moves = new ArrayList<IMove>(moves);
 			board_values = new double[moves.size()];
 			int i = 0;
 			for (IMove move : this.moves) {
 				board_values[i] = side_sign * evalBoard0(board.doMove(move));
 				i++;
 			}
 		}
 
 		public int compare(IMove m1, IMove m2) {
 
 			int i1 = moves.indexOf(m1);
 			int i2 = moves.indexOf(m2);
 
 			// "-" to receive an increasing ordering
 			return -Double.compare(board_values[i1], board_values[i2]);
 
 		}
 	}
 
 	/**
 	 * Sorts the moves w.r.t. the value of the next board (base case). If the
 	 * depth is 1 (so after one move the base case is reached) no sorting is
 	 * done. (preventing double evaluation of board)
 	 * 
 	 * @param moves
 	 *            the set of moves to be sorted
 	 * @param board
 	 *            the actual board
 	 * @param side_sign
 	 *            the actual side
 	 * @param depth
 	 *            the current search depth
 	 * @return the sorted set of moves in a TreeSet
 	 */
 	private ArrayList<IMove> sortMoves(Set<IMove> moves, IBoard board,
 			int side_sign, int depth) {
 
 		ArrayList<IMove> ordered_moves;
 		ordered_moves = new ArrayList<IMove>(moves);
 		if (depth != 1) {
 			MoveComperator my_comp = new MoveComperator();
 			my_comp.compute_values(moves, board, side_sign);
 
 			Collections.sort(ordered_moves, my_comp);
 		}
 
 		return ordered_moves;
 	}
 
 	/**
 	 * NegaMax with Alpha Beta Pruning
 	 * 
 	 * @see <a
 	 *      href="https://en.wikipedia.org/wiki/Negamax#NegaMax_with_Alpha_Beta_Pruning">NegaMax
 	 *      with Alpha Beta Pruning</a>
 	 * @param board
 	 *            the current board
 	 * @param total_depth
 	 *            the total depth to search
 	 * @param depth
 	 *            the remaining depth to search
 	 * @param alpha
 	 * @param beta
 	 * @return returns a Variation tree
 	 */
 	private Variation evalBoard(IBoard board, int total_depth, int depth,
 			int alpha, int beta, Variation old_tree) {
 
 		// whose move is it?
 		Side side = board.getActiveColor();
 		int side_sign = Side.getSideSign(side);
 
 		// generate moves
 		Set<IMove> moves = board.getPossibleMoves();
 
 		// check for mate and stalemate (the side should alternate)
 		if (moves.isEmpty()) {
 			Variation base_variation;
 			if (board.isCheckPosition()) {
 				base_variation = new Variation(null, NEG_INF * side_sign,
 						Side.getOppositeSide(side));
 			} else {
 				base_variation = new Variation(null, 0,
 						Side.getOppositeSide(side));
 			}
 			return base_variation;
 		}
 
 		// base case (the side should alternate)
 		if (depth == 0) {
 			Variation base_variation = new Variation(null, evalBoard0(board),
 					Side.getOppositeSide(side));
 			return base_variation;
 		}
 
 		int best_value = NEG_INF; // this starts always at negative!
 
 		// Sort the moves:
 		ArrayList<IMove> ordered_moves;
 		ArrayList<Variation> ordered_variations = null;
 		if (old_tree == null || old_tree.getSubVariations().isEmpty()) {
 			// no previous computation given
 			ordered_moves = sortMoves(moves, board, side_sign, depth);
 		} else {
 			// use old Variation tree for ordering
 			Set<Variation> children = old_tree.getSubVariations();
 			ordered_variations = new ArrayList<Variation>(children);
 			Collections.sort(ordered_variations);
 			if (side == Side.WHITE)
 				Collections.reverse(ordered_variations);
 			ordered_moves = new ArrayList<IMove>();
 			for (Variation var : ordered_variations) {
 				ordered_moves.add(var.getMove());
 			}
 		}
 
 		// create new parent Variation
 		Variation parent = new Variation(null, NEG_INF,
 				Side.getOppositeSide(side));
 
 		int i = 0;
 		// alpha beta search
 		for (IMove move : ordered_moves) {
 
 			Variation variation;
			if (ordered_variations != null) {
 				variation = evalBoard(board.doMove(move), total_depth,
 						depth - 1, -beta, -alpha, ordered_variations.get(i));
 			} else {
 				variation = evalBoard(board.doMove(move), total_depth,
 						depth - 1, -beta, -alpha);
 			}
 			int negaval = variation.getValue() * side_sign;
 
 			// better variation found
 			if (negaval >= best_value) {
 				boolean truly_better = negaval > best_value;
 				best_value = negaval;
 
 				// update variation tree
 				parent.update(null, variation.getValue());
 
 				// update the missing move for the child
 				variation.update(move, variation.getValue());
 
 				parent.addSubVariation(variation);
 
 				// output to UCI
 				if (depth == total_depth && truly_better) {
 
 					principal_variation = parent.getPrincipalVariation();
 					UCIReporter.sendInfoPV(principal_variation, total_depth,
 							variation.getValue());
 
 				}
 			}
 
 			// alpha beta cutoff
 			alpha = Math.max(alpha, negaval);
 			if (alpha >= beta)
 				break;
 
 			i++; // keep ordered_moves and ordered_variations in sync
 		}
 
 		return parent;
 
 	}
 
 	private Variation evalBoard(IBoard board, int total_depth, int depth,
 			int alpha, int beta) {
 		return evalBoard(board, total_depth, depth, alpha, beta, null);
 	}
 
 	/**
 	 * returns the value of a board. Does NOT recognize mate and stalemate!
 	 * 
 	 * @param board
 	 *            the board to be analyzed
 	 * @return the value of a board in centipawns
 	 */
 	private int evalBoard0(IBoard board) {
 
 		eval_counter++;
 
 		// A very very simple implementation
 		int value = 0;
 
 		// One way to prevent copy and paste
 		int[] fig_value = { 100, 500, 330, 330, 900, 000 };
 
 		// Maybe not the most efficient way (several runs over the board)
 		for (Side c : Side.values()) {
 			int side_sign = Side.getSideSign(c);
 			for (Piece fig : Piece.values()) {
 				value += board.getNumberOfPiecesByColorAndType(c, fig)
 						* fig_value[fig.ordinal()] * side_sign;
 			}
 
 		}
 
 		return value;
 	}
 
 	@Override
 	public IMove search(int movetime, int maxMoveTime, int searchDepth,
 			boolean infinite, Set<IMove> searchMoves) {
 
 		// first of all, ignoring the timings and restriction to certain
 		// moves...
 
 		Timer timer = new Timer();
 		timer.scheduleAtFixedRate(new UCIUpdater(), 1000, 5000);
 
 		// iterative deepening
 		Variation var_tree = null; // TODO: use previous searches as starting
 									// point
 		for (int current_depth = 1; current_depth < searchDepth - 1; current_depth *= 2) {
 			this.principal_variation = null;
 			var_tree = evalBoard(board, current_depth, current_depth, NEG_INF,
 					POS_INF, var_tree);
 			// mate found
 			if (principal_variation.getValue() == POS_INF
 					&& board.getActiveColor() == Side.WHITE
 					|| principal_variation.getValue() == NEG_INF
 					&& board.getActiveColor() == Side.BLACK) {
 				timer.cancel();
 
 				return principal_variation.getMove();
 			}
 		}
 		this.principal_variation = null;
 		var_tree = evalBoard(board, searchDepth, searchDepth, NEG_INF, POS_INF,
 				var_tree);
 
 		timer.cancel();
 
 		return principal_variation.getMove();
 	}
 
 	@Override
 	public IMove stop() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
