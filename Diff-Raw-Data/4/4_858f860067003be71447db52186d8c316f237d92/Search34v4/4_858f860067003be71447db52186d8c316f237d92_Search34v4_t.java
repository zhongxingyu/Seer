 package search.search34;
 
 import search.MoveSet;
 import search.Search4;
 import search.SearchListener2;
 import search.SearchStat;
 import state4.BitUtil;
 import state4.Masks;
 import state4.MoveEncoder;
 import state4.SEE;
 import state4.State4;
 import eval.Evaluator3;
 import eval.ScoreEncoder;
 
 /** heavy search reductions for non-pv lines after depth 7*/
 public final class Search34v4 implements Search4{
 	/** pvs framework node types*/
 	private static enum NodeType{
 		pv(){
 			NodeType next(){
 				return cut;
 			}
 		},
 		cut(){
 			NodeType next(){
 				return all;
 			}
 		},
 		all(){
 			NodeType next(){
 				return cut;
 			}
 		};
 		abstract NodeType next();
 	};
 	
 	public final static class SearchStat32k extends SearchStat{
 		/** scores returned from quiet search without bottoming out*/
 		public long forcedQuietCutoffs;
 		public long nullMoveVerifications;
 		public long nullMoveCutoffs;
 		public long nullMoveFailLow;
 		/** scores seen pes ply*/
 		public int[] scores;
 		public int maxPlySearched;
 		
 		public String toString(){
 			return "n="+nodesSearched+", t="+searchTime+", hh="+hashHits+", qc="+forcedQuietCutoffs;
 		}
 	}
 	
 	private final static class MoveList{
 		private final static int defSize = 128;
 		private final MoveSet[] mset = new MoveSet[defSize];
 		public boolean skipNullMove = false;
 		/** holds killer moves as first 12 bits (ie, masked 0xFFF) of move encoding*/
 		public final long[] killer = new long[2];
 		/** controls whether we should futility prune*/
 		public boolean futilityPrune;
 		
 		{
 			for(int a = 0; a < defSize; a++) mset[a] = new MoveSet();
 		}
 	}
 	
 	private final static int ONE_PLY = 8;
 
 	private final MoveList[] stack;
 	private final SearchStat32k stats = new SearchStat32k();
 	private final Evaluator3 e;
 	private final int qply = 12;
 	private final Hash m;
 	private SearchListener2 l;
 	private final static int stackSize = 256;
 	/** sequence number for hash entries*/
 	private int seq;
 	private final MoveGen2 moveGen = new MoveGen2();
 	private final TTEntry fillEntry = new TTEntry();
 	private volatile boolean cutoffSearch = false;
 	private final static int[][] lmrReduction = new int[32][64];
 	private final boolean printPV;
 	/** stores pv line as its encounted in PVS search*/
 	private final long[] pvStore = new long[64];
 
 	static{
 		for(int d = 0; d < lmrReduction.length; d++){
 			for(int mc = 0; mc < lmrReduction[d].length; mc++){
 				if(d != 0 && mc != 0){
 					final double pvRed = Math.log(d) * Math.log(mc) / 3.0;
 					//double nonPVRed = 0.33 + log(double(hd)) * log(double(mc)) / 2.25;
 					lmrReduction[d][mc] = (int)(pvRed >= 1.0 ? Math.floor(pvRed*ONE_PLY*3/4) : 0);
 					//Reductions[0][hd][mc] = (int8_t) (nonPVRed >= 1.0 ? floor(nonPVRed * int(ONE_PLY)) : 0);
 				} else{
 					lmrReduction[d][mc] = 0;
 				}
 			}
 		}
 	}
 	
 	private static int lmrReduction(final int depth, final int moveCount){
 		return lmrReduction[depth > 31? 31: depth][moveCount > 63? 63: moveCount];
 	}
 	
 	public Search34v4(Evaluator3 e, int hashSize){
 		this(e, hashSize, true);
 	}
 	
 	public Search34v4(Evaluator3 e, int hashSize, boolean printPV){
 		this.e = e;
 		
 		//m = new ZMap3(hashSize);
 		m = new ZMap4(hashSize);
 		
 		stack = new MoveList[stackSize];
 		for(int i = 0; i < stack.length; i++){
 			stack[i] = new MoveList();
 		}
 		stats.scores = new int[stackSize];
 		
 		this.printPV = printPV;
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
 		
 		//search initialization
 		seq++;
 		e.initialize(s);
 		moveGen.dampen();
 		cutoffSearch = false;
 		
 		long bestMove = 0;
 		int score = 0;
 		
 		final int max = 90000;
 		final int min = -90000;
 		
 		int alpha = min;
 		int beta = max;
 		
 		long nodesSearched = 0;
 		boolean skipAdjust = false;
 		int minRestartDepth = 7;
 		for(int i = 1; (maxPly == -1 || i <= maxPly) && !cutoffSearch && i <= stackSize; i++){
 			s.resetHistory();
 			
 			if(i <= 3){
 				alpha = min;
 				beta = max;
 			} else if(i > 3 && !skipAdjust){
 				final int index = i-1-1; //index of most recent score observation
 				int est = stats.scores[index];
 				est += stats.scores[index-1];
 				est /= 2;
 				final double dir = stats.scores[index]-stats.scores[index-1];
 				est += dir/2;
 				alpha = est-25;
 				beta = est+25;
 			}
 			skipAdjust = false;
 			
 			score = recurse(player, alpha, beta, i*ONE_PLY, NodeType.pv, 0, s);
 			
 			if((score <= alpha || score >= beta) && !cutoffSearch){
 				if(score <= alpha){
 					alpha = score-50;
 					beta = score+15;
 				} else if(score >= beta){
 					beta = score+50;
 					alpha = score-15;
 				}
 				
 				if(i < minRestartDepth){
 					score = recurse(player, alpha, beta, i*ONE_PLY, NodeType.pv, 0, s);
 					if((score <= alpha || score >= beta) && !cutoffSearch){
 						i--;
 						if(score <= alpha) alpha = score-150;
 						else if(score >= beta) beta = score+150;
 						skipAdjust = true;
 						continue;
 					}
 				} else{
 					minRestartDepth = i+1;
 					
 					i -= i/4+.5;
 					skipAdjust = true;
 					continue;
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
 	
 	/**
 	 * Traverses TT entries until the leaf state from which the evaluation was performed
 	 * is found. This can potentially fail if the TT path has been broken
 	 * <p> note, this should be called directly after a search has been performed
 	 * @param player
 	 * @param s
 	 * @param depth
 	 * @param maxDepth
 	 * @param result store for the result
 	 */
 	public void getPredictedLeafState(int player, State4 s, int depth, int maxDepth, State4 result){
 		final TTEntry e = m.get(s.zkey());
 		if(depth < maxDepth && e != null && e.move != 0){
 			final long pmask = 1L<<MoveEncoder.getPos1(e.move);
 			final long mmask = 1L<<MoveEncoder.getPos2(e.move);
 			s.executeMove(player, pmask, mmask);
 			getPredictedLeafState(1-player, s, depth+1, maxDepth, result);
 			s.undoMove();
 			return;
 		}
 		State4.copy(s, result);
 	}
 	
 	public void cutoffSearch(){
 		cutoffSearch = true;
 	}
 	
 	/** tests to see if the passed player is in check*/
 	private static boolean isChecked(final int player, final State4 s){
 		return State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s);
 	}
 	
 	private int recurse(final int player, int alpha, final int beta, final int depth,
 			NodeType nt, final int stackIndex, final State4 s){
 		stats.nodesSearched++;
 		assert alpha < beta;
 		
 		if(s.isForcedDraw()){
 			return 0;
 		} else if(depth <= 0){
 			final int q = qsearch(player, alpha, beta, 0, stackIndex, nt, s);
 			if(q > 70000 && nt == NodeType.pv){
 				return recurse(player, alpha, beta, ONE_PLY, nt, stackIndex, s);
 			} else{
 				return q;
 			}
 		} else if(cutoffSearch){
 			return 0;
 		}
 
 
 		final MoveList ml = stack[stackIndex];
 		final MoveSet[] mset = ml.mset;
 		ml.killer[0] = 0;
 		ml.killer[1] = 0;
 		
 		int w = 0;
 
 		final long zkey = s.zkey();
 		final TTEntry e = m.get(zkey);
 		boolean tteMove = false;
 		long tteMoveEncoding = 0;
 		
 		final int scoreEncoding;
 		if(e != null){
 			stats.hashHits++;
 			
 			if(e.depth >= depth){
 				final int cutoffType = e.cutoffType;
 				if(nt == NodeType.pv? cutoffType == TTEntry.CUTOFF_TYPE_EXACT: (e.score >= beta?
 						cutoffType == TTEntry.CUTOFF_TYPE_LOWER: cutoffType == TTEntry.CUTOFF_TYPE_UPPER)){
 					
 					if(stackIndex-1 >= 0 && e.score >= beta){
 						attemptKillerStore(e.move, ml.skipNullMove, stack[stackIndex-1]);
 					}
 					
					if(nt != NodeType.pv){
						return e.score;
					}
 				}
 			}
 			
 			if(e.move != 0){
 				tteMoveEncoding = e.move;
 				
 				final MoveSet temp = mset[w++];
 				temp.piece = 1L << MoveEncoder.getPos1(tteMoveEncoding);
 				temp.moves = 1L << MoveEncoder.getPos2(tteMoveEncoding);
 				temp.rank = MoveGen2.tteMoveRank;
 				tteMove = true;
 			}
 			
 			scoreEncoding = this.e.refine(player, s, alpha, beta, e.staticEval);
 		} else{
 			scoreEncoding = this.e.eval(player, s, alpha, beta);
 		}
 		
 		//final int scoreEncoding = this.e.eval(player, s);
 		final int staticEval = ScoreEncoder.getScore(scoreEncoding) + ScoreEncoder.getMargin(scoreEncoding);
 		final int eval;
 		if(e != null && ((e.cutoffType == TTEntry.CUTOFF_TYPE_LOWER && e.score > staticEval) ||
 				(e.cutoffType == TTEntry.CUTOFF_TYPE_UPPER && e.score < staticEval))){
 			eval = e.score;
 		} else{
 			eval = staticEval;
 		}
 		
 		
 		final boolean alliedKingAttacked = isChecked(player, s);
 		final boolean pawnPrePromotion = (s.pawns[player] & Masks.pawnPrePromote[player]) != 0;
 		final boolean hasNonPawnMaterial = s.pieceCounts[player][0]-s.pieceCounts[player][State4.PIECE_TYPE_PAWN] > 1;
 		final boolean nonMateScore = Math.abs(beta) < 70000 && Math.abs(alpha) < 70000;
 		
 		//futility pruning
 		//refute previous passive move when we are really far ahead
 		if(nt != NodeType.pv && depth <= 3*ONE_PLY &&
 				!pawnPrePromotion &&
 				!alliedKingAttacked &&
 				hasNonPawnMaterial &&
 				nonMateScore &&
 				ml.futilityPrune){
 			
 			final int futilityMargin;
 			if(depth <= 1*ONE_PLY){
 				futilityMargin = 250;
 			} else if(depth <= 2*ONE_PLY){
 				futilityMargin = 300;
 			} else { //depth <= 3*ONE_PLY
 				futilityMargin = 425;
 			}
 			
 			final int futilityScore = eval - futilityMargin;
 			
 			if(futilityScore >= beta){
 				return futilityScore;
 			}
 		}
 		
 		//razoring
 		//quick check for refutation of previous refutation (cut) move
 		int razorReduction = 0;
 		if(nt == NodeType.all &&
 				nonMateScore &&
 				depth <= 1*ONE_PLY &&
 				!alliedKingAttacked &&
 				!tteMove &&
 				!pawnPrePromotion){
 			
 			final int razorMargin = 270;
 			if(eval + razorMargin < alpha){
 				final int r = alpha-razorMargin;
 				final int v = qsearch(player, r-1, r, 0, stackIndex+1, nt, s);
 				if(v <= r-1){
 					//fail low, probably cant recover from their refutation move
 					return v+razorMargin;
 				}
 			}
 		}
 		
 		//null move pruning
 		final boolean threatMove; //true if opponent can make a move that causes null-move fail low
 		if(nt != NodeType.pv && !ml.skipNullMove && depth > 3*ONE_PLY && !alliedKingAttacked &&
 				hasNonPawnMaterial && nonMateScore){
 			
 			final int r = 3*ONE_PLY + depth/4;
 			
 			//note, non-pv nodes are null window searched - no need to do it here explicitly
 			stack[stackIndex+1].futilityPrune = true;
 			stack[stackIndex+1].skipNullMove = true;
 			s.nullMove();
 			final long nullzkey = s.zkey();
 			int n = -recurse(1-player, -beta, -alpha, depth-r, nt, stackIndex+1, s);
 			s.undoNullMove();
 			stack[stackIndex+1].skipNullMove = false;
 			
 			threatMove = n < alpha;
 			
 			if(n >= beta){
 				if(n >= 70000){
 					n = beta;
 				}
 				if(depth < 12*ONE_PLY){ //stockfish prunes at depth<12
 					stats.nullMoveCutoffs++;
 					return n;
 				}
 				
 				stats.nullMoveVerifications++;
 				//verification search
 				stack[stackIndex+1].futilityPrune = false;
 				stack[stackIndex+1].skipNullMove = true;
 				double v = recurse(player, alpha, beta, depth-r, nt, stackIndex+1, s);
 				stack[stackIndex+1].skipNullMove = false;
 				if(v >= beta){
 					stats.nullMoveCutoffs++;
 					return n;
 				}
 			} else if(n < alpha){
 				stats.nullMoveFailLow++;
 				final TTEntry nullTTEntry = m.get(nullzkey);
 				if(nullTTEntry != null && nullTTEntry.move != 0){
 					ml.killer[0] = nullTTEntry.move & 0xFFFL; //doesnt matter which we store to, killer is at node start
 				}
 			} else{ //case alpha < n < beta
 				alpha = n;
 			}
 		} else{
 			threatMove = false;
 		}
 
 		//internal iterative deepening
 		if(!tteMove && depth >= (nt == NodeType.pv? 5: 8)*ONE_PLY && nonMateScore &&
 				(nt == NodeType.pv || (!alliedKingAttacked && eval+256 >= beta))){
 			final int d = nt == NodeType.pv? depth-2*ONE_PLY: depth/2;
 			stack[stackIndex+1].futilityPrune = false; //would never have arrived here if futility pruned above, set false
 			stack[stackIndex+1].skipNullMove = true;
 			recurse(player, alpha, beta, d, nt, stackIndex+1, s);
 			stack[stackIndex+1].skipNullMove = false;
 			final TTEntry temp;
 			if((temp = m.get(zkey)) != null && temp.move != 0){
 				tteMove = true;
 				tteMoveEncoding = temp.move;
 				final MoveSet tempMset = mset[w++];
 				tempMset.piece = 1L<<MoveEncoder.getPos1(tteMoveEncoding);
 				tempMset.moves = 1L<<MoveEncoder.getPos2(tteMoveEncoding);
 				tempMset.rank = MoveGen2.tteMoveRank;
 			}
 		}
 		
 		//load killer moves
 		final long l1killer1;
 		final long l1killer2;
 		final long l2killer1;
 		final long l2killer2;
 		if(stackIndex-1 >= 0 && !ml.skipNullMove){
 			final MoveList prev = stack[stackIndex-1];
 			final long l1killer1Temp = prev.killer[0];
 			if(l1killer1Temp != 0 && isPseudoLegal(player, l1killer1Temp, s)){
 				assert MoveEncoder.getPos1(l1killer1Temp) != MoveEncoder.getPos2(l1killer1Temp);
 				final MoveSet temp = mset[w++];
 				temp.piece = 1L << MoveEncoder.getPos1(l1killer1Temp);
 				temp.moves = 1L << MoveEncoder.getPos2(l1killer1Temp);
 				temp.rank = MoveGen2.killerMoveRank;
 				l1killer1 = l1killer1Temp & 0xFFFL;
 			} else{
 				l1killer1 = 0;
 			}
 			final long l1killer2Temp = prev.killer[1];
 			if(l1killer2Temp != 0 && isPseudoLegal(player, l1killer2Temp, s)){
 				assert MoveEncoder.getPos1(l1killer2Temp) != MoveEncoder.getPos2(l1killer2Temp);
 				final MoveSet temp = mset[w++];
 				temp.piece = 1L << MoveEncoder.getPos1(l1killer2Temp);
 				temp.moves = 1L << MoveEncoder.getPos2(l1killer2Temp);
 				temp.rank = MoveGen2.killerMoveRank;
 				l1killer2 = l1killer2Temp & 0xFFFL;
 			} else{
 				l1killer2 = 0;
 			}
 			
 			if(stackIndex-3 >= 0){
 				final MoveList prev2 = stack[stackIndex-3];
 				final long l2killer1Temp = prev2.killer[0];
 				if(l2killer1Temp != 0 && isPseudoLegal(player, l2killer1Temp, s)){
 					assert MoveEncoder.getPos1(l2killer1Temp) != MoveEncoder.getPos2(l2killer1Temp);
 					final MoveSet temp = mset[w++];
 					temp.piece = 1L << MoveEncoder.getPos1(l2killer1Temp);
 					temp.moves = 1L << MoveEncoder.getPos2(l2killer1Temp);
 					temp.rank = MoveGen2.killerMoveRank;
 					l2killer1 = l2killer1Temp & 0xFFFL;
 				} else{
 					l2killer1 = 0;
 				}
 				final long l2killer2Temp = prev2.killer[1];
 				if(l2killer2Temp != 0 && isPseudoLegal(player, l2killer2Temp, s)){
 					assert MoveEncoder.getPos1(l2killer2Temp) != MoveEncoder.getPos2(l2killer2Temp);
 					final MoveSet temp = mset[w++];
 					temp.piece = 1L << MoveEncoder.getPos1(l2killer2Temp);
 					temp.moves = 1L << MoveEncoder.getPos2(l2killer2Temp);
 					temp.rank = MoveGen2.killerMoveRank;
 					l2killer2 = l2killer2Temp & 0xFFFL;
 				} else{
 					l2killer2 = 0;
 				}
 			} else{
 				l2killer1 = 0;
 				l2killer2 = 0;
 			}
 		} else{
 			l1killer1 = 0;
 			l1killer2 = 0;
 			l2killer1 = 0;
 			l2killer2 = 0;
 		}
 
 		//move generation
 		final int length = moveGen.genMoves(player, s, alliedKingAttacked, mset, w, false, stackIndex);
 		if(length == 0){ //no moves, draw
 			fillEntry.fill(zkey, 0, 0, scoreEncoding, (int)depth, TTEntry.CUTOFF_TYPE_EXACT, seq);
 			m.put(zkey, fillEntry);
 			return 0;
 		}
 		isort(mset, length);
 		
 		
 		int g = alpha;
 		long bestMove = 0;
 		final int initialBestScore = -99999;
 		int bestScore = initialBestScore;
 		int cutoffFlag = TTEntry.CUTOFF_TYPE_UPPER;
 		int moveCount = 0;
 		
 		final int drawCount = s.drawCount; //stored for error checking
 		final long pawnZkey = s.pawnZkey(); //stored for error checking
 		
 		boolean hasMove = alliedKingAttacked;
 		final boolean inCheck = alliedKingAttacked;
 		for(int i = 0; i < length; i++){
 			if(i >=5 && nt == NodeType.cut){
 				nt = NodeType.all;
 			}
 			
 			final MoveSet set = mset[i];
 			final long pieceMask = set.piece;
 			final int promotionType = set.promotionType;
 			final long move = set.moves;
 			long encoding = s.executeMove(player, pieceMask, move, promotionType);
 			this.e.makeMove(encoding);
 			boolean isDrawable = s.isDrawable(); //player can take a draw
 
 			if(State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s)){
 				//king in check after move
 				g = -88888+stackIndex+1;
 			} else{
 				hasMove = true;
 
 				final boolean pvMove = nt == NodeType.pv && i==0;
 				final boolean isCapture = MoveEncoder.getTakenType(encoding) != State4.PIECE_TYPE_EMPTY;
 				final boolean givesCheck = isChecked(1-player, s);
 				final boolean isPawnPromotion = MoveEncoder.isPawnPromotion(encoding);
 				final boolean isPassedPawnPush = isPawnPromotion || (MoveEncoder.getMovePieceType(encoding) == State4.PIECE_TYPE_PAWN &&
 						(Masks.passedPawnMasks[player][MoveEncoder.getPos1(encoding)] & s.pawns[1-player]) == 0);
 				final boolean isTTEMove = tteMove && encoding == tteMoveEncoding;
 
 				final long rawEn = encoding & 0xFFFL; //raw encoding
 				final boolean isKillerMove = rawEn == l1killer1 || rawEn == l1killer2 ||
 						rawEn == l2killer1 || rawEn == l2killer2;
 
 				final boolean isDangerous = givesCheck ||
 						MoveEncoder.isCastle(encoding) != 0 ||
 						isPassedPawnPush;
 
 				stack[stackIndex+1].futilityPrune = !isDangerous && !isCapture && !isPawnPromotion;
 
 				final int ext = (isDangerous && nt == NodeType.pv? ONE_PLY: 0) +
 						(threatMove && nt == NodeType.pv? 0: 0) + razorReduction;
 						//(!pv && depth > 7? -depth/10: 0);
 						//(!pv && depth > 7 && !isDangerous && !isCapture? -depth/10: 0);
 
 				final int nextDepth = depth - ONE_PLY + ext;
 				
 				//LMR
 				final boolean fullSearch;
 				//final int reduction;
 				if(depth > ONE_PLY && !pvMove && !isCapture && !inCheck && !isPawnPromotion &&
 						nt != NodeType.cut &&
 						!isDangerous && 
 						!isKillerMove &&
 						!isTTEMove){
 					
 					moveCount++;
 					final int lmrReduction = lmrReduction(depth/ONE_PLY, moveCount) + (nt == NodeType.pv? 0: ONE_PLY);
 					final int reducedDepth = nextDepth - lmrReduction;
 					
 					g = -recurse(1-player, -alpha-1, -alpha, reducedDepth, nt.next(), stackIndex+1, s);
 					fullSearch = g > alpha && lmrReduction != 0;
 				} else{
 					fullSearch = true;
 				}
 
 				if(fullSearch){
 					//descend negascout style
 					if(!pvMove){
 						g = -recurse(1-player, -alpha-1, -alpha, nextDepth, nt.next(), stackIndex+1, s);
 						if(alpha < g && g < beta && nt == NodeType.pv){
 							g = -recurse(1-player, -beta, -alpha, nextDepth, NodeType.pv, stackIndex+1, s);
 						}
 					} else{
 						g = -recurse(1-player, -beta, -alpha, nextDepth, NodeType.pv, stackIndex+1, s);
 					}
 				}
 			}
 			s.undoMove();
 			this.e.undoMove(encoding);
 			assert zkey == s.zkey(); //keys should be unchanged after undo
 			assert drawCount == s.drawCount;
 			assert pawnZkey == s.pawnZkey();
 
 			if(isDrawable && 0 > g){ //can take a draw instead of making the move
 				g = 0;
 				//encoding = 0;
 			} 
 
 			if(g > bestScore){
 				bestScore = g;
 				bestMove = encoding;
 				
 				if(g > alpha){
 					alpha = g;
 					cutoffFlag = TTEntry.CUTOFF_TYPE_EXACT;
 					if(alpha >= beta){
 						if(!cutoffSearch){
 							fillEntry.fill(zkey, encoding, alpha, scoreEncoding, depth, TTEntry.CUTOFF_TYPE_LOWER, seq);
 							m.put(zkey, fillEntry);
 						}
 
 						//check to see if killer move can be stored
 						//if used on null moves, need to have a separate killer array
 						if(stackIndex-1 >= 0){
 							attemptKillerStore(bestMove, ml.skipNullMove, stack[stackIndex-1]);
 						}
 						
 						moveGen.betaCutoff(player, MoveEncoder.getMovePieceType(encoding),
 								MoveEncoder.getPos1(encoding),
 								MoveEncoder.getPos2(encoding), s, depth/ONE_PLY);
 
 						return g;
 					} else{
 						moveGen.alphaRaised(player, MoveEncoder.getMovePieceType(encoding),
 								MoveEncoder.getPos1(encoding),
 								MoveEncoder.getPos2(encoding), s, depth/ONE_PLY);
 					}
 				}
 			}
 		}
 
 		if(!hasMove){
 			//no moves except king into death - draw
 			bestMove = 0;
 			bestScore = 0;
 			cutoffFlag = TTEntry.CUTOFF_TYPE_EXACT;
 		}
 
 		if(!cutoffSearch){
 			fillEntry.fill(zkey, bestMove, bestScore, scoreEncoding, (int)depth, nt == NodeType.pv? cutoffFlag: TTEntry.CUTOFF_TYPE_UPPER, seq);
 			m.put(zkey, fillEntry);
 		}
 		
 		if(nt == NodeType.pv){
 			pvStore[stackIndex] = bestMove;
 		}
 		
 		return bestScore;
 	}
 	
 	private int qsearch(final int player, int alpha, int beta, final int depth,
 			final int stackIndex, final NodeType nt, final State4 s){
 		stats.nodesSearched++;
 		
 		if(depth < -qply){
 			stats.forcedQuietCutoffs++;
 			return beta; //qply bottomed out, return bad value
 		} else if(cutoffSearch){
 			return 0;
 		}
 
 		
 		int w = 0;
 		final MoveSet[] mset = stack[stackIndex].mset;
 
 		final long zkey = s.zkey();
 		final TTEntry e = m.get(zkey);
 		final boolean hasTTMove;
 		final long ttMove;
 		final int scoreEncoding;
 		if(e != null){
 			stats.hashHits++;
 			if(e.depth >= depth){
 				final int cutoffType = e.cutoffType;
 				if(nt == NodeType.pv? cutoffType == TTEntry.CUTOFF_TYPE_EXACT: (e.score >= beta?
 						cutoffType == TTEntry.CUTOFF_TYPE_LOWER: cutoffType == TTEntry.CUTOFF_TYPE_UPPER)){
 					return e.score;
 				}
 			}
 			if(e.move != 0){
 				final long encoding = e.move;
 				final MoveSet temp = mset[w++];
 				temp.piece = 1L<<MoveEncoder.getPos1(encoding);
 				temp.moves = 1L<<MoveEncoder.getPos2(encoding);
 				temp.rank = MoveGen2.tteMoveRank;
 				hasTTMove = true;
 				ttMove = encoding;
 			} else{
 				hasTTMove = false;
 				ttMove = 0;
 			}
 			
 			scoreEncoding = nt == NodeType.pv? this.e.refine(player, s, -90000, 90000, e.staticEval):
 				this.e.refine(player, s, alpha, beta, e.staticEval);
 		} else{
 			hasTTMove = false;
 			ttMove = 0;
 			scoreEncoding = nt == NodeType.pv? this.e.eval(player, s): this.e.eval(player, s, alpha, beta);
 		}
 		
 		int bestScore;
 		final boolean alliedKingAttacked = State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s);
 		if(alliedKingAttacked){
 			bestScore = -77777;
 		} else{
 			bestScore = ScoreEncoder.getScore(scoreEncoding) + ScoreEncoder.getMargin(scoreEncoding);
 			if(bestScore >= beta){ //standing pat
 				return bestScore;
 			} else if(bestScore > alpha && nt == NodeType.pv){
 				alpha = bestScore;
 			}
 		}
 		
 		final int length = moveGen.genMoves(player, s, alliedKingAttacked, mset, w, true, stackIndex);
 		isort(mset, length);
 
 		
 		int g = alpha;
 		int cutoffFlag = TTEntry.CUTOFF_TYPE_UPPER;
 		long bestMove = 0;
 		final int drawCount = s.drawCount; //stored for error checking purposes
 		for(int i = 0; i < length; i++){
 			final MoveSet set = mset[i];
 			final long pieceMask = set.piece;
 			final int promotionType = set.promotionType;
 			final long move = set.moves;
 			long encoding = s.executeMove(player, pieceMask, move, promotionType);
 			this.e.makeMove(encoding);
 			final boolean isDrawable = s.isDrawable();
 			
 			if(State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s)){
 				//king in check after move
 				g = -77777;
 			} else{
 				if(nt != NodeType.pv && !alliedKingAttacked && !MoveEncoder.isPawnPromotion(encoding) &&
 						(!hasTTMove || encoding != ttMove)){
 					s.undoMove();
 					if(SEE.seeSign(player, pieceMask, move, s) < 0){
 						this.e.undoMove(encoding);
 						continue;
 					} else{
 						s.executeMove(player, pieceMask, move, promotionType);
 					}
 				}
 				
 				g = -qsearch(1-player, -beta, -alpha, depth-1, stackIndex+1, nt, s);
 			}
 			s.undoMove();
 			this.e.undoMove(encoding);
 
 			if(isDrawable && 0 > g){// && -10*depth > g){ //can draw instead of making the move
 				g = 0;
 				encoding = 0;
 			}
 
 			assert zkey == s.zkey();
 			assert drawCount == s.drawCount;
 
 			if(g > bestScore){
 				bestScore = g;
 				bestMove = encoding;
 				if(g > alpha){
 					alpha = g;
 					cutoffFlag = TTEntry.CUTOFF_TYPE_EXACT;
 					if(g >= beta){
 						if(!cutoffSearch){
 							fillEntry.fill(zkey, encoding, g, scoreEncoding, depth, TTEntry.CUTOFF_TYPE_LOWER, seq);
 							m.put(zkey, fillEntry);
 						}
 						return g;
 					}
 				}
 			}
 		}
 
 		if(!cutoffSearch){
 			fillEntry.fill(zkey, bestMove, bestScore, scoreEncoding, depth, nt == NodeType.pv? cutoffFlag: TTEntry.CUTOFF_TYPE_UPPER, seq);
 			m.put(zkey, fillEntry);
 		}
 		return bestScore;
 	}
 	
 
 	/**
 	 * checks too see if a move is legal, assumming we do not start in check,
 	 * moving does not yield self check, we are not castling, and if moving a pawn
 	 * we have chosen a non take move that could be legal if no piece is
 	 * blocking the target square
 	 * 
 	 * <p> used to check that killer moves are legal
 	 * @param player
 	 * @param piece
 	 * @param move
 	 * @param s
 	 * @return
 	 */
 	private static boolean isPseudoLegal(final int player, final long encoding, final State4 s){
 		final int pos1 = MoveEncoder.getPos1(encoding);
 		final int pos2 = MoveEncoder.getPos2(encoding);
 		final int takenType = MoveEncoder.getTakenType(encoding);
 		final long p = 1L << pos1;
 		final long m = 1L << pos2;
 		final long[] pieces = s.pieces;
 		final long agg = pieces[0] | pieces[1];
 		final long allied = pieces[player];
 		final long open = ~allied;
 		
 		if((allied & p) != 0 && takenType == s.mailbox[pos2]){
 			final int type = s.mailbox[pos1];
 			switch(type){
 			case State4.PIECE_TYPE_BISHOP:
 				final long tempBishopMoves = Masks.getRawBishopMoves(agg, p) & open;
 				return (m & tempBishopMoves) != 0;
 			case State4.PIECE_TYPE_KNIGHT:
 				final long tempKnightMoves = Masks.getRawKnightMoves(p) & open;
 				return (m & tempKnightMoves) != 0;
 			case State4.PIECE_TYPE_QUEEN:
 				final long tempQueenMoves = Masks.getRawQueenMoves(agg, p) & open;
 				return (m & tempQueenMoves) != 0;
 			case State4.PIECE_TYPE_ROOK:
 				final long tempRookMoves = Masks.getRawRookMoves(agg, p) & open;
 				return (m & tempRookMoves) != 0;
 			case State4.PIECE_TYPE_KING:
 				final long tempKingMoves = (Masks.getRawKingMoves(p) & open) | State4.getCastleMoves(player, s);
 				return (m & tempKingMoves) != 0;
 			case State4.PIECE_TYPE_PAWN:
 				final long tempPawnMoves = Masks.getRawAggPawnMoves(player, agg, s.pawns[player]);
 				return (m & tempPawnMoves) != 0;
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * attempts to store a move as a killer move
 	 * @param move move to be stored
 	 * @param skipNullMove current skip null move status
 	 * @param prev previous move list
 	 */
 	private static void attemptKillerStore(final long move, final boolean skipNullMove, final MoveList prev){
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
 	
 	/**
 	 * insertion sort (lowest rank first)
 	 * @param mset
 	 * @param length
 	 */
 	private static void isort(final MoveSet[] mset, final int length){
 		for(int i = 1; i < length; i++){
 			for(int a = i; a > 0 && mset[a-1].rank > mset[a].rank; a--){
 				final MoveSet temp = mset[a];
 				mset[a] = mset[a-1];
 				mset[a-1] = temp;
 			}
 		}
 	}
 
 	@Override
 	public void setListener(SearchListener2 l) {
 		this.l = l;
 	}
 }
