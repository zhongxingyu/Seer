 package mitzi;
 
 import java.lang.ref.SoftReference;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import static mitzi.MateScores.*;
 import mitzi.UCIReporter.InfoType;
 
 public class MitziBrain implements IBrain {
 
 	private IBoard board;
 
 	private Variation principal_variation;
 
 	private long eval_counter;
 
 	private long table_counter = 0;
 
 	private IBoardAnalyzer board_analyzer = new BasicBoardAnalyzer();
 
 	// maybe reuse old tables
 	public Map<IBoard, SoftReference<Variation>> transposition_table = new HashMap<IBoard, SoftReference<Variation>>(
 			100000);
 
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
 
 		int alpha_old = alpha;
 
 		// Transposition Table Lookup; node is the lookup key for entry
 		// http://en.wikipedia.org/wiki/Negamax#NegaMax_with_Alpha_Beta_Pruning_and_Transposition_Tables
 		Variation entry = null;
 		SoftReference<Variation> sr_entry = transposition_table.get(board);
 		if (sr_entry != null)
 			entry = sr_entry.get();
 		if (entry != null && entry.getDepth() >= depth) {
 			table_counter++;
 			if (entry.getFlag() == Flag.EXACT) {
 				return entry;
 			} else if (entry.getFlag() == Flag.LOWERBOUND)
 				alpha = Math.max(alpha, entry.getValue());
 			else if (entry.getFlag() == Flag.UPPERBOUND)
 				beta = Math.min(beta, entry.getValue());
 
 			if (alpha >= beta) {
 				return entry;
 			}
 		}
 
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
 			eval_counter++;
 			return base_variation;
 		}
 
 		// base case (the side should alternate)
 		if (depth == 0) {
 			AnalysisResult result = board_analyzer.eval0(board);
 			result.setPlysToEval0(0);
 			result.setPlysToSelDepth(0);
 			Variation base_variation = new Variation(null, result.score,
 					Side.getOppositeSide(side));
 			eval_counter++;
 			return base_variation;
 		}
 
 		int best_value = NEG_INF; // this starts always at negative!
 
 		// Sort the moves:
 		BasicMoveComparator move_comparator = new BasicMoveComparator(board);
 		ArrayList<IMove> ordered_moves;
 		ArrayList<Variation> ordered_variations = null;
 		if (old_tree == null || old_tree.getSubVariations().isEmpty()) {
 			// no previous computation given, use basic heuristic
 			ordered_moves = new ArrayList<IMove>(moves);
 			Collections.sort(ordered_moves,
 					Collections.reverseOrder(move_comparator));
 		} else {
 			// use old Variation tree for ordering
 			Set<Variation> children = old_tree.getSubVariations();
 			ordered_variations = new ArrayList<Variation>(children);
 			if (side == Side.BLACK)
 				Collections.sort(ordered_variations);
 			else
 				Collections
 						.sort(ordered_variations, Collections.reverseOrder());
 			ordered_moves = new ArrayList<IMove>();
 			ArrayList<Variation> del = new ArrayList<Variation>();
 			for (Variation var : ordered_variations) {
 				// TODO: WORKAROUND FOR CRASH. This should not happen, but
 				// happens only in connection with Transpos. Tables!!!!
 				if (!moves.contains(var.getMove()))
 					del.add(var);
 				else
 					ordered_moves.add(var.getMove());
 			}
 			ordered_variations.removeAll(del);
 
 			// add remaining moves in basic heuristic order
 			ArrayList<IMove> remaining_moves = new ArrayList<IMove>();
 			for (IMove move : moves) {
 				if (!ordered_moves.contains(move))
 					remaining_moves.add(move);
 			}
 			Collections.sort(remaining_moves,
 					Collections.reverseOrder(move_comparator));
 			ordered_moves.addAll(remaining_moves);
 		}
 
 		// create new parent Variation
 		Variation parent = new Variation(null, NEG_INF,
 				Side.getOppositeSide(side));
 
 		int i = 0;
 		// alpha beta search
 		for (IMove move : ordered_moves) {
 
 			if (depth == total_depth && total_depth >= 6) {
 				// output currently searched move to UCI
 				UCIReporter.sendInfoCurrMove(move, i + 1);
 			}
 
 			Variation variation;
 			if (ordered_variations != null && i < ordered_variations.size()) {
 				variation = evalBoard(board.doMove(move), total_depth,
 						depth - 1, -beta, -alpha, ordered_variations.get(i));
 			} else {
 				variation = evalBoard(board.doMove(move), total_depth,
 						depth - 1, -beta, -alpha);
 			}
 			int negaval = variation.getValue() * side_sign;
 
 			// update the missing move for the child
 			variation.update(move, variation.getValue());
 
 			// build variation tree
 			if (negaval >= best_value - 100) // NOTE: fine tune this
 				parent.addSubVariation(variation);
 
 			// better variation found
 			if (negaval >= best_value) {
 				boolean truly_better = negaval > best_value;
 				best_value = negaval;
 
 				// update variation tree
 				parent.update(null, variation.getValue());
 
 				// output to UCI
 				if (depth == total_depth && truly_better) {
 
 					principal_variation = parent.getPrincipalVariation();
 					UCIReporter.sendInfoPV(principal_variation, total_depth,
 							variation.getValue(), board.getActiveColor());
 
 				}
 			}
 
 			// alpha beta cutoff
 			alpha = Math.max(alpha, negaval);
 			if (alpha >= beta)
 				break;
 
 			i++; // keep ordered_moves and ordered_variations in sync
 		}
 
 		// Transposition Table Store; board is the lookup key for parent
 
 		Variation t_entry = new Variation(null, parent.getValue(), side);
 		Set<Variation> childs = new HashSet<Variation>(
 				parent.getSubVariations());
 		for (Variation var : childs)
 			t_entry.addSubVariation(var);
 		if (t_entry.getValue() <= alpha_old)
 			t_entry.setFlag(Flag.UPPERBOUND);
 		else if (t_entry.getValue() >= beta)
 			t_entry.setFlag(Flag.LOWERBOUND);
 		else
 			t_entry.setFlag(Flag.EXACT);
 
 		t_entry.setDepth(depth); // the depth of subvariations is not changed.
 									// (because it not needed)
 		transposition_table.put(board, new SoftReference<Variation>(t_entry));
 
 		return parent;
 
 	}
 
 	private Variation evalBoard(IBoard board, int total_depth, int depth,
 			int alpha, int beta) {
 		return evalBoard(board, total_depth, depth, alpha, beta, null);
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
 		Variation var_tree_temp;
 
 		// Parameters for aspiration windows
 		int alpha = NEG_INF; // initial value
 		int beta = POS_INF; // initial value
 		int asp_window = 150; // often 50 or 25 is used
 		int factor = 2; // factor for increasing if out of bounds
 
 		for (int current_depth = 1; current_depth < searchDepth; current_depth++) {
 			this.principal_variation = null;
 			table_counter = 0;
 			// should or should not be cleared?
 			// transposition_table.clear();
 			var_tree_temp = evalBoard(board, current_depth, current_depth,
 					alpha, beta, var_tree);
 			// mate found
 			if (principal_variation != null
					&& (principal_variation.getValue() == POS_INF
							&& board.getActiveColor() == Side.WHITE || principal_variation
							.getValue() == NEG_INF
							&& board.getActiveColor() == Side.BLACK)) {
 				timer.cancel();
 
 				return principal_variation.getMove();
 			}
 			// If Value is out of bounds, redo search with larger bounds, but
 			// with the same variation tree
 			if (var_tree_temp.getValue() <= alpha) {
 				alpha -= factor * asp_window;
 				current_depth--;
 				continue;
 			} else if (var_tree_temp.getValue() >= beta) {
 				beta += factor * asp_window;
 				current_depth--;
 				continue;
 			}
 
 			alpha = var_tree_temp.getValue() - asp_window;
 			beta = var_tree_temp.getValue() + asp_window;
 
 			var_tree = var_tree_temp;
 			UCIReporter.sendInfoString("Boards found: " + table_counter);
 		}
 
 		// repeat until a value inside the alpha-beta bound is found.
 		while (true) {
 			// should or should not be cleared?
 			// transposition_table.clear();
 			table_counter = 0;
 			this.principal_variation = null;
 			var_tree_temp = evalBoard(board, searchDepth, searchDepth, alpha,
 					beta, var_tree);
 			if (var_tree_temp.getValue() <= alpha) {
 				alpha -= factor * asp_window;
 			} else if (var_tree_temp.getValue() >= beta) {
 				beta += factor * asp_window;
 			} else {
 				var_tree = var_tree_temp;
 				break;
 			}
 		}
 
 		timer.cancel();
 		UCIReporter.sendInfoString("Size transposition table: "
 				+ transposition_table.size());
 		UCIReporter.sendInfoString("Boards found: " + table_counter);
 		if (principal_variation != null) {
 			return principal_variation.getMove();
 		} else {
 			// mitzi cannot avoid mate :(
 			return var_tree.getBestMove();
 		}
 	}
 
 	@Override
 	public IMove stop() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
