 package chess.search.search34.pipeline;
 
 import chess.eval.Evaluator;
 import chess.search.MoveSet;
 import chess.search.search34.*;
 import chess.state4.BitUtil;
 import chess.state4.Masks;
 import chess.state4.MoveEncoder;
 import chess.state4.State4;
 
 /** final search stage, explore possible moves*/
 public class DescentStage implements FinalStage{
 
 	private final StackFrame[] stack;
 	private final Search34 searcher;
 	private final MoveGen moveGen;
 	private final Evaluator e;
 	private final Hash m;
 	private final long[] pvStore;
 
 	private final static int[][] lmrReduction = new int[32][64];
 	private final TTEntry fillEntry = new TTEntry();
 
 	static{
 		for(int d = 0; d < lmrReduction.length; d++){
 			for(int mc = 0; mc < lmrReduction[d].length; mc++){
 				if(d != 0 && mc != 0){
 					final double pvRed = Math.log(d) * Math.log(mc) / 3.0;
 					//double nonPVRed = 0.33 + log(double(hd)) * log(double(mc)) / 2.25;
 					lmrReduction[d][mc] = (int)(pvRed >= 1.0 ? Math.floor(pvRed*Search34.ONE_PLY*3/4) : 0);
 					//Reductions[0][hd][mc] = (int8_t) (nonPVRed >= 1.0 ? floor(nonPVRed * int(ONE_PLY)) : 0);
 				} else{
 					lmrReduction[d][mc] = 0;
 				}
 			}
 		}
 	}
 
 	public DescentStage(MoveGen moveGen, Evaluator e, StackFrame[] stack, Hash m, long[] pvStore, Search34 searcher){
 		this.moveGen = moveGen;
 		this.e = e;
 		this.stack = stack;
 		this.m = m;
 		this.pvStore = pvStore;
 		this.searcher = searcher;
 	}
 
 	@Override
 	public int eval(SearchContext c, NodeProps props, KillerMoveSet kms, State4 s) {
 
 		int alpha = c.alpha;
 		NodeType nt = c.nt;
 
 		//unimplemented misc from previous search
 		final int razorReduction = 0;
 		final boolean threatMove = false;
 
 		StackFrame frame = stack[c.stackIndex];
 		MoveSet[] mset = frame.mlist.list;
 
 		//move generation
 		if(props.hasTTMove){
 			frame.mlist.add(props.tteMoveEncoding, MoveGen.tteMoveRank);
 		}
		final int length = moveGen.genMoves(c.player, s, props.alliedKingAttacked, mset, frame.mlist.len, false, c.stackIndex);
 		if(length == 0){ //no moves, draw
 			fillEntry.fill(props.zkey, 0, 0, props.scoreEncoding, c.depth, TTEntry.CUTOFF_TYPE_EXACT, searcher.getSeq());
 			m.put(props.zkey, fillEntry);
 			return 0;
 		}
 		Search34.isort(mset, length);
 
 		int g = alpha;
 		long bestMove = 0;
 		final int initialBestScore = -99999;
 		int bestScore = initialBestScore;
 		int cutoffFlag = TTEntry.CUTOFF_TYPE_UPPER;
 		int moveCount = 0;
 
 		final int drawCount = s.drawCount; //stored for error checking
 		final long pawnZkey = s.pawnZkey(); //stored for error checking
 
 		boolean hasMove = props.alliedKingAttacked;
 		final boolean inCheck = props.alliedKingAttacked;
 		for(int i = 0; i < length; i++){
 			if(i >= 5 && nt == NodeType.cut){
 				nt = NodeType.all;
 			}
 
 			final MoveSet set = mset[i];
 			final long pieceMask = set.piece;
 			final int promotionType = set.promotionType;
 			final long move = set.moves;
 			long encoding = s.executeMove(c.player, pieceMask, move, promotionType);
 			this.e.makeMove(encoding);
 			boolean isDrawable = s.isDrawable(); //player can take a draw
 
 			if(State4.isAttacked2(BitUtil.lsbIndex(s.kings[c.player]), 1-c.player, s)){
 				//king in check after move
 				g = -88888+c.stackIndex+1;
 			} else{
 				hasMove = true;
 
 				final boolean pvMove = nt == NodeType.pv && i==0;
 				final boolean isCapture = MoveEncoder.getTakenType(encoding) != State4.PIECE_TYPE_EMPTY;
 				final boolean givesCheck = Search34.isChecked(1 - c.player, s);
 				final boolean isPawnPromotion = MoveEncoder.isPawnPromotion(encoding);
 				final boolean isPassedPawnPush = isPawnPromotion || (MoveEncoder.getMovePieceType(encoding) == State4.PIECE_TYPE_PAWN &&
 						(Masks.passedPawnMasks[c.player][MoveEncoder.getPos1(encoding)] & s.pawns[1-c.player]) == 0);
 				final boolean isTTEMove = props.hasTTMove && encoding == props.tteMoveEncoding;
 				final boolean isKillerMove = kms.contains(encoding);
 
 				final boolean isDangerous = givesCheck ||
 						MoveEncoder.isCastle(encoding) != 0 ||
 						isPassedPawnPush;
 
 				//stack[c.stackIndex+1].futilityPrune = !isDangerous && !isCapture && !isPawnPromotion;
 
 				final int ext = (isDangerous && nt == NodeType.pv? Search34.ONE_PLY: 0) +
 						(threatMove && nt == NodeType.pv? 0: 0) + razorReduction;
 				//(!pv && depth > 7? -depth/10: 0);
 				//(!pv && depth > 7 && !isDangerous && !isCapture? -depth/10: 0);
 
 				final int nextDepth = c.depth - Search34.ONE_PLY + ext;
 
 				//LMR
 				final boolean fullSearch;
 				//final int reduction;
 				if(c.depth > Search34.ONE_PLY && !pvMove && !isCapture && !inCheck && !isPawnPromotion &&
 						nt != NodeType.cut &&
 						!isDangerous &&
 						!isKillerMove &&
 						!isTTEMove){
 
 					moveCount++;
 					final int lmrReduction = lmrReduction(c.depth/Search34.ONE_PLY, moveCount) + (nt == NodeType.pv? 0: Search34.ONE_PLY);
 					final int reducedDepth = nextDepth - lmrReduction;
 
 					g = -searcher.recurse(new SearchContext(1 - c.player, -alpha - 1, -alpha, reducedDepth, nt.next(), c.stackIndex + 1), s);
 					fullSearch = g > alpha && lmrReduction != 0;
 				} else{
 					fullSearch = true;
 				}
 
 				if(fullSearch){
 					//descend negascout style
 					if(!pvMove){
 						g = -searcher.recurse(new SearchContext(1 - c.player, -alpha - 1, -alpha, nextDepth, nt.next(), c.stackIndex + 1), s);
 						if(alpha < g && g < c.beta && nt == NodeType.pv){
 							g = -searcher.recurse(new SearchContext(1 - c.player, -c.beta, -alpha, nextDepth, NodeType.pv, c.stackIndex + 1), s);
 						}
 					} else{
 						g = -searcher.recurse(new SearchContext(1 - c.player, -c.beta, -alpha, nextDepth, NodeType.pv, c.stackIndex + 1), s);
 					}
 				}
 			}
 			s.undoMove();
 			this.e.undoMove(encoding);
 			assert props.zkey == s.zkey(); //keys should be unchanged after undo
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
 					if(alpha >= c.beta){
 						if(!searcher.isCutoffSearch()){
 							fillEntry.fill(props.zkey, encoding, alpha, props.scoreEncoding, c.depth, TTEntry.CUTOFF_TYPE_LOWER, searcher.getSeq());
 							m.put(props.zkey, fillEntry);
 						}
 
 						//check to see if killer move can be stored
 						//if used on null moves, need to have a separate killer array
 						if(c.stackIndex-1 >= 0){
 							Search34.attemptKillerStore(bestMove, c.skipNullMove, stack[c.stackIndex - 1]);
 						}
 
 						moveGen.betaCutoff(c.player, MoveEncoder.getMovePieceType(encoding),
 								MoveEncoder.getPos1(encoding),
 								MoveEncoder.getPos2(encoding), s, c.depth/Search34.ONE_PLY);
 
 						return g;
 					} else{
 						moveGen.alphaRaised(c.player, MoveEncoder.getMovePieceType(encoding),
 								MoveEncoder.getPos1(encoding),
 								MoveEncoder.getPos2(encoding), s, c.depth/Search34.ONE_PLY);
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
 
 		if(!searcher.isCutoffSearch()){
 			fillEntry.fill(props.zkey, bestMove, bestScore, props.scoreEncoding,
 					c.depth, nt == NodeType.pv? cutoffFlag: TTEntry.CUTOFF_TYPE_UPPER, searcher.getSeq());
 			m.put(props.zkey, fillEntry);
 		}
 
 		if(nt == NodeType.pv){
 			pvStore[c.stackIndex] = bestMove;
 		}
 
 		return bestScore;
 	}
 
 	private static int lmrReduction(final int depth, final int moveCount){
 		return lmrReduction[depth > 31? 31: depth][moveCount > 63? 63: moveCount];
 	}
 }
