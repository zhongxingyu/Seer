 package chess.search.search34;
 
 import chess.eval.Evaluator;
 import chess.eval.e9.pipeline.EvalResult;
 import chess.search.MoveSet;
 import chess.search.Search;
 import chess.search.SearchListener2;
 import chess.search.SearchStat;
 import chess.search.search34.moveGen.MoveGen;
 import chess.search.search34.moveGen.MoveList;
 import chess.search.search34.moveGen.RankedMoveSet;
 import chess.search.search34.pipeline.*;
 import chess.state4.BitUtil;
 import chess.state4.MoveEncoder;
 import chess.state4.SEE;
 import chess.state4.State4;
 
 /** heavy chess.search reductions for non-pv lines after depth 7*/
 public final class Search34 implements Search{
 	
 	public final static class SearchStat32k extends SearchStat{
 		/** scores returned from quiet chess.search without bottoming out*/
 		public long forcedQuietCutoffs;
 		public long nullMoveVerifications;
 		public long nullMoveCutoffs;
 		/** scores seen pes ply*/
 		public int[] scores;
 		public int maxPlySearched;
 		
 		public String toString(){
 			return "n="+nodesSearched+", t="+searchTime+", hh="+hashHits+", qc="+forcedQuietCutoffs;
 		}
 	}
 	
 	public final static int ONE_PLY = 8;
 	private final static int maxScore = 90000;
 	private final static int minScore = -90000;
 
 	private final StackFrame[] stack;
 	private final SearchStat32k stats = new SearchStat32k();
 	private final Evaluator e;
 	private final int qply = 12;
 	private final Hash m;
 	private SearchListener2 l;
 	private final static int stackSize = 256;
 	/** sequence number for hash entries*/
 	private int seq;
 	private final MoveGen moveGen = new MoveGen();
 	private final TTEntry fillEntry = new TTEntry();
 	private volatile boolean cutoffSearch = false;
 	private final boolean printPV;
 	/** stores pv line as its encounted in PVS chess.search*/
 	private final long[] pvStore = new long[64];
 	private final EntryStage searchPipeline;
 	
 	public Search34(Evaluator e, int hashSize, boolean printPV){
 		this.e = e;
 		
 		m = new ZMap4(hashSize);
 		
 		stack = new StackFrame[stackSize];
 		for(int i = 0; i < stack.length; i++){
 			stack[i] = new StackFrame();
 		}
 		stats.scores = new int[stackSize];
 		
 		this.printPV = printPV;
 
 		//construct search pipeline
 		MidStage descent = new DescentStage(moveGen, stack, m, pvStore, this);
 		MidStage internalIterativeDeepening = new InternalIterativeDeepeningStage(m, this, descent);
 		MidStage nullMovePruning = new NullMoveStage(stack, m, this, internalIterativeDeepening);
 		MidStage razoring = new RazoringStage(this, nullMovePruning);
 		MidStage futilityPruning = new FutilityPruningStage(razoring);
 		EntryStage entry = new HashLookupStage(stack, m, e, futilityPruning);
 		this.searchPipeline = entry;
 
 	}
 	
 	public SearchStat32k getStats(){
 		return stats;
 	}
 	
 	public void search(final int player, final State4 s, final MoveSet moveStore){
 		search(player, s, moveStore, -1);
 	}
 	
 	@Override
 	public void resetSearch(){
 		m.clear();
 		seq = 0;
 		e.reset();
 		moveGen.reset();
 	}
 	
 	public void search(final int player, final State4 s, final MoveSet moveStore, final int maxPly){
 		stats.nodesSearched = 0;
 		stats.hashHits = 0;
 		stats.forcedQuietCutoffs = 0;
 		stats.nullMoveVerifications = 0;
 		stats.nullMoveCutoffs = 0;
 		stats.maxPlySearched = 0;
 		stats.searchTime = System.currentTimeMillis();
 		
 		//chess.search initialization
 		seq++;
 		moveGen.dampen();
 		cutoffSearch = false;
 		
 		long bestMove = 0;
 		int score = 0;
 		
 		int alpha, beta;
 		
 		long nodesSearched = 0;
 		int minRestartDepth = 7;
 		for(int i = 1; (maxPly == -1 || i <= maxPly) && !cutoffSearch && i <= stackSize; i++){
 			s.resetHistory();
 			
 			if(i <= 3){
 				alpha = minScore;
 				beta = maxScore;
 			} else {//if(i > 3){
 				final int index = i-1-1; //index of most recent score observation
 				int est = stats.scores[index];
 				est += stats.scores[index-1];
 				est /= 2;
 				final double dir = stats.scores[index]-stats.scores[index-1];
 				est += dir/2;
 				alpha = est-25;
 				beta = est+25;
 			}
 
 			boolean research = true;
 			int k = 0;
 			while(research && !cutoffSearch){
 				research = false;
 				score = recurse(new SearchContext(player, alpha, beta, i*ONE_PLY, SearchContext.NODE_TYPE_PV, 0), s);
 
 				if(score <= alpha || score >= beta){
 					if(i >= minRestartDepth){
 						minRestartDepth = i+1;
 						i -= i/4+.5;
 						continue;
 					} else{
 						research = true;
 						alpha = Math.min(score - 35 - 10*k, alpha);
 						beta = Math.max(score + 35 + 10*k, beta);
 						k++;
 					}
 				}
 			}
 			
 			if(!cutoffSearch){
 				nodesSearched = stats.nodesSearched;
 				stats.maxPlySearched = i;
 				
 				bestMove = pvStore[0];
 				
 				if(printPV){
 					String pvString = "";
 					for(int a = 0; a < i; a++){
 						long move = pvStore[a];
 						int pos1 = MoveEncoder.getPos1(move);
 						int pos2 = MoveEncoder.getPos2(move);
 						pvString += moveString(pos1)+moveString(pos2)+" ";
 					}
 					
 					long time = System.currentTimeMillis()-stats.searchTime;
 					int nps = (int)(stats.nodesSearched*1000./time);
 					
 					String infoString = "info depth "+i+" score cp "+score +
 							" time "+time +
 							" nodes "+stats.nodesSearched +
 							" nps "+nps +
 							" pv "+pvString;
 					
 					System.out.println(infoString);
 				}
 				
 				stats.predictedScore = score;
 				if(l != null){
 					l.plySearched(bestMove, i, score);
 				}
 				
 				if(i-1 < stats.scores.length){
 					stats.scores[i-1] = score;
 				}
 			}
 		}
 		
 		stats.empBranchingFactor = Math.pow(nodesSearched, 1./stats.maxPlySearched);
 		
 		if(moveStore != null){
 			final int pos1 = MoveEncoder.getPos1(bestMove);
 			final int pos2 = MoveEncoder.getPos2(bestMove);
 			moveStore.piece = 1L << pos1;
 			moveStore.moves = 1L << pos2;
 			moveStore.promotionType = MoveEncoder.getPawnPromotionType(bestMove);
 		}
 		
 		stats.searchTime = System.currentTimeMillis()-stats.searchTime;
 	}
 	
 	private static String moveString(int pos){
 		return ""+(char)('a'+(pos%8))+(pos/8+1);
 	}
 	
 	public void cutoffSearch(){
 		cutoffSearch = true;
 	}
 
 	public boolean isCutoffSearch(){
 		return cutoffSearch;
 	}
 
 	public int getSeq(){
 		return seq;
 	}
 	
 	/** tests to see if the passed player is in check*/
 	public static boolean isChecked(final int player, final State4 s){
 		return State4.posIsAttacked(BitUtil.lsbIndex(s.kings[player]), 1 - player, s);
 	}
 	
 	public int recurse(SearchContext c, final State4 s){
 		stats.nodesSearched++;
 
 		assert c.alpha < c.beta;
 		
 		if(s.isForcedDraw()){
 			return 0;
 		} else if(c.depth <= 0){
 			final int q = qsearch(c.player, c.alpha, c.beta, 0, c.stackIndex, c.nt, s);
 			if(q > 70000 && c.nt == SearchContext.NODE_TYPE_PV){ //false mate for enemy king
 				return recurse(new SearchContext(c.player, q, maxScore, ONE_PLY, c.nt, c.stackIndex), s);
 			} else if(q < -70000 && c.nt == SearchContext.NODE_TYPE_PV){ //false mate for allied king
 				return recurse(new SearchContext(c.player, minScore, q, ONE_PLY, c.nt, c.stackIndex), s);
 			} else{
 				return q;
 			}
 		} else if(cutoffSearch){
 			return 0;
 		}
 
 		return searchPipeline.eval(c, s);
 	}
 	
 	public int qsearch(final int player, int alpha, int beta, final int depth,
 			final int stackIndex, final int nt, final State4 s){
 		stats.nodesSearched++;
 		
 		if(depth < -qply){
 			stats.forcedQuietCutoffs++;
 			return beta; //qply bottomed out, return bad value
 		} else if(cutoffSearch){
 			return 0;
 		}
 
 		
 		StackFrame frame = stack[stackIndex];
 		final MoveList mlist = frame.mlist;
 		mlist.clear();
 
 		final long zkey = s.zkey();
 		final TTEntry e = m.get(zkey);
 		final boolean hasTTMove;
 		final long ttMove;
 		final EvalResult staticEval;
 		if(e != null){
 			stats.hashHits++;
 			if(e.depth >= depth){
 				final int cutoffType = e.cutoffType;
 				if(nt == SearchContext.NODE_TYPE_PV? cutoffType == TTEntry.CUTOFF_TYPE_EXACT: (e.score >= beta?
 						cutoffType == TTEntry.CUTOFF_TYPE_LOWER: cutoffType == TTEntry.CUTOFF_TYPE_UPPER)){
 					return e.score;
 				}
 			}
 			if(e.move != 0){
 				final long encoding = e.move;
 				mlist.add(encoding, MoveGen.tteMoveRank);
 
 				hasTTMove = true;
 				ttMove = encoding;
 			} else{
 				hasTTMove = false;
 				ttMove = 0;
 			}
 			
 			staticEval = nt == SearchContext.NODE_TYPE_PV? this.e.refine(player, s, minScore, maxScore, e.staticEval):
 				this.e.refine(player, s, alpha, beta, e.staticEval);
 		} else{
 			hasTTMove = false;
 			ttMove = 0;
 			staticEval = nt == SearchContext.NODE_TYPE_PV? this.e.eval(player, s): this.e.eval(player, s, alpha, beta);
 		}
 		
 		int bestScore;
 		final boolean alliedKingAttacked = State4.posIsAttacked(BitUtil.lsbIndex(s.kings[player]), 1 - player, s);
 		if(alliedKingAttacked){
 			bestScore = -77777;
 		} else{
 			bestScore = staticEval.score;
 			if(bestScore >= beta){ //standing pat
 				return bestScore;
 			} else if(bestScore > alpha){ //redundent to check 'nt == NodeType.pv' because non-pv has null window
 				alpha = bestScore;
 			}
 		}
 		
 		moveGen.genMoves(player, s, alliedKingAttacked, mlist, true);
 		mlist.isort();
 		final int length = mlist.len;
 		final RankedMoveSet[] mset = mlist.list;
 
 		int cutoffFlag = TTEntry.CUTOFF_TYPE_UPPER;
 		long bestMove = 0;
 		final int drawCount = s.drawCount; //stored for error checking purposes
 		for(int i = 0; i < length; i++){
 			final MoveSet set = mset[i];
 			final long pieceMask = set.piece;
 			final int promotionType = set.promotionType;
 			final long move = set.moves;
 			long encoding = s.executeMove(player, pieceMask, move, promotionType);
 			final boolean isDrawable = s.isDrawable();
 
 			final int moveScore;
 			if(State4.posIsAttacked(s.kings[player], 1 - player, s)){
 				//king in check after move
 				moveScore = -77777;
 			} else{
 				if(nt != SearchContext.NODE_TYPE_PV && !alliedKingAttacked && !MoveEncoder.isPawnPromotion(encoding) &&
 						(!hasTTMove || encoding != ttMove)){
 					s.undoMove();
 					if(SEE.seeSign(player, pieceMask, move, s) < 0){
 						continue;
 					} else{
 						s.executeMove(player, pieceMask, move, promotionType);
 					}
 				}
 
 				moveScore = -qsearch(1-player, -beta, -alpha, depth-1, stackIndex+1, nt, s);
 			}
 			s.undoMove();
 
 			final int score;
 			if(isDrawable && 0 > moveScore){ //can draw instead of making the move
 				score = 0;
 				encoding = 0;
 			} else{
 				score = moveScore;
 			}
 
 			assert zkey == s.zkey();
 			assert drawCount == s.drawCount;
 
 			if(score > bestScore){
 				bestScore = score;
 				bestMove = encoding;
 
 				if(bestScore >= beta){
 					if(!cutoffSearch){
 						fillEntry.fill(zkey, encoding, bestScore, staticEval.toScoreEncoding(), depth, TTEntry.CUTOFF_TYPE_LOWER, seq);
 						m.put(zkey, fillEntry);
 					}
 					return bestScore;
 				} else if(bestScore > alpha){
 					alpha = bestScore;
 					cutoffFlag = TTEntry.CUTOFF_TYPE_EXACT;
 				}
 			}
 		}
 
 		if(!cutoffSearch){
 			fillEntry.fill(zkey, bestMove, bestScore, staticEval.toScoreEncoding(),
 					depth, nt == SearchContext.NODE_TYPE_PV? cutoffFlag: TTEntry.CUTOFF_TYPE_UPPER, seq);
 			m.put(zkey, fillEntry);
 		}
 		return bestScore;
 	}
 	
 	/**
 	 * attempts to store a move as a killer move
 	 * @param move move to be stored
 	 * @param skipNullMove current skip null move status
 	 * @param prev previous move list
 	 */
 	public static void attemptKillerStore(final long move, final boolean skipNullMove, final StackFrame prev){
 		assert prev != null;
 		if(move != 0 && !skipNullMove &&
 				(move&0xFFF) != prev.killer[0] &&
 				(move&0xFFF) != prev.killer[1] &&
 				MoveEncoder.getTakenType(move) == State4.PIECE_TYPE_EMPTY &&
 				MoveEncoder.isEnPassanteTake(move) == 0 &&
 				!MoveEncoder.isPawnPromotion(move)){
 			if(prev.killer[0] != move){
 				prev.killer[1] = prev.killer[0];
 				prev.killer[0] = move & 0xFFF;
 			} else{
 				prev.killer[0] = prev.killer[1];
 				prev.killer[1] = move & 0xFFF;
 			}
 		}
 	}
 
 	@Override
 	public void setListener(SearchListener2 l) {
 		this.l = l;
 	}
 }
