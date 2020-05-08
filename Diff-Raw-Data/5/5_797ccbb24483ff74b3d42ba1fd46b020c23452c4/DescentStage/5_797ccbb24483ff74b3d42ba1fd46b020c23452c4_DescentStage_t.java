 package chess.search.search34.pipeline;
 
 import chess.search.MoveSet;
 import chess.search.search34.Hash;
 import chess.search.search34.Search34;
 import chess.search.search34.StackFrame;
 import chess.search.search34.TTEntry;
 import chess.search.search34.moveGen.MoveGen;
 import chess.search.search34.moveGen.MoveList;
 import chess.search.search34.moveGen.RankedMoveSet;
 import chess.state4.Masks;
 import chess.state4.MoveEncoder;
 import chess.state4.State4;
 
 /** final search stage, explore possible moves*/
 public final class DescentStage implements MidStage{
 
 	private final static class KillerMoveSet {
 		final long l1killer1;
 		final long l1killer2;
 		final long l2killer1;
 		final long l2killer2;
 
 		KillerMoveSet(long l1killer1, long l1killer2, long l2killer1, long l2killer2){
 			this.l1killer1 = l1killer1;
 			this.l1killer2 = l1killer2;
 			this.l2killer1 = l2killer1;
 			this.l2killer2 = l2killer2;
 		}
 
 		public boolean contains(long encoding){
 			final long rawEn = encoding & 0xFFFL; //raw encoding
 			return rawEn == l1killer1 || rawEn == l1killer2 ||
 					rawEn == l2killer1 || rawEn == l2killer2;
 		}
 	}
 
 	private final StackFrame[] stack;
 	private final Search34 searcher;
 	private final MoveGen moveGen;
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
 					lmrReduction[d][mc] = (int)(pvRed >= 1.0 ? Math.floor(pvRed*Search34.ONE_PLY) : 0);
 					//Reductions[0][hd][mc] = (int8_t) (nonPVRed >= 1.0 ? floor(nonPVRed * int(ONE_PLY)) : 0);
 				} else{
 					lmrReduction[d][mc] = 0;
 				}
 			}
 		}
 	}
 
 	public DescentStage(MoveGen moveGen, StackFrame[] stack, Hash m, long[] pvStore, Search34 searcher){
 		this.moveGen = moveGen;
 		this.stack = stack;
 		this.m = m;
 		this.pvStore = pvStore;
 		this.searcher = searcher;
 	}
 
 	@Override
 	public int eval(SearchContext c, NodeProps props, State4 s) {
 
 		final KillerMoveSet kms = buildKMS(c, s);
 
 		int alpha = c.alpha;
 		int nt = c.nt;
 
 		final StackFrame frame = stack[c.stackIndex];
 		final MoveList mlist = frame.mlist;
 
 		//move generation
 		if(props.hasTTMove){
 			frame.mlist.add(props.tteMoveEncoding, MoveGen.tteMoveRank);
 		}
 		moveGen.genMoves(c.player, s, props.alliedKingAttacked, mlist, false);
 		final int length = mlist.len;
 		if(length == 0){ //no moves, draw
 			fillEntry.fill(props.zkey, 0, 0, props.staticScore.toScoreEncoding(), c.depth, TTEntry.CUTOFF_TYPE_EXACT, searcher.getSeq());
 			m.put(props.zkey, fillEntry);
 			return 0;
 		}
 		mlist.isort();
 
 
 		final RankedMoveSet[] mset = frame.mlist.list;
 
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
 			if(i >= 5 && nt == SearchContext.NODE_TYPE_CUT){
 				nt = SearchContext.NODE_TYPE_ALL;
 			}
 
 			final RankedMoveSet set = mset[i];
 			final long pieceMask = set.piece;
 			final int promotionType = set.promotionType;
 			final long move = set.moves;
 			long encoding = s.executeMove(c.player, pieceMask, move, promotionType);
 			boolean isDrawable = s.isDrawable(); //player can take a draw
 
 			final boolean isTTEMove = props.hasTTMove && encoding == props.tteMoveEncoding;
 			if((isTTEMove && set.rank != MoveGen.tteMoveRank)){
 				s.undoMove();
 				continue;
 			}
 
 			if(State4.posIsAttacked(s.kings[c.player], 1 - c.player, s)){
 				//king in check after move
 				g = -88888+c.stackIndex+1;
 			} else{
 				hasMove = true;
 
 				final boolean pvMove = nt == SearchContext.NODE_TYPE_PV && i==0;
 				final boolean isCapture = MoveEncoder.getTakenType(encoding) != State4.PIECE_TYPE_EMPTY;
 				final boolean givesCheck = Search34.isChecked(1 - c.player, s);
 				final boolean isPawnPromotion = MoveEncoder.isPawnPromotion(encoding);
 				final boolean isPassedPawnPush = isPawnPromotion || (MoveEncoder.getMovePieceType(encoding) == State4.PIECE_TYPE_PAWN &&
 						(Masks.passedPawnMasks[c.player][MoveEncoder.getPos1(encoding)] & s.pawns[1-c.player]) == 0);
 				final boolean isKillerMove = kms.contains(encoding);
 
 				final boolean isDangerous = givesCheck ||
 						MoveEncoder.isCastle(encoding) != 0 ||
 						isPassedPawnPush;
 
 				final int ext = isDangerous && nt == SearchContext.NODE_TYPE_PV? Search34.ONE_PLY: 0;
 
 				final int nextDepth = c.depth - Search34.ONE_PLY + ext;
 
 				//LMR
 				final boolean fullSearch;
 				//final int reduction;
 				if(c.depth > Search34.ONE_PLY && !isCapture && !inCheck && !isPawnPromotion &&
 						nt == SearchContext.NODE_TYPE_ALL &&
 						!isDangerous &&
 						!isKillerMove &&
 						!isTTEMove){
 
 					moveCount++;
 					final int lmrReduction = lmrReduction(c.depth/Search34.ONE_PLY, moveCount) + (nt == SearchContext.NODE_TYPE_PV? 0: Search34.ONE_PLY);
 					final int reducedDepth = nextDepth - lmrReduction;
 
 					g = -searcher.recurse(new SearchContext(1 - c.player, -alpha - 1, -alpha, reducedDepth, SearchContext.nextNodeType(nt), c.stackIndex + 1), s);
 					fullSearch = g > alpha && lmrReduction != 0;
 				} else{
 					fullSearch = true;
 				}
 
 				if(fullSearch){
 					//descend negascout style
 					if(!pvMove){
 						g = -searcher.recurse(new SearchContext(1 - c.player, -alpha - 1, -alpha, nextDepth, SearchContext.nextNodeType(nt), c.stackIndex + 1), s);
 						if(alpha < g && g < c.beta && nt == SearchContext.NODE_TYPE_PV){
 							g = -searcher.recurse(new SearchContext(1 - c.player, -c.beta, -alpha, nextDepth, SearchContext.NODE_TYPE_PV, c.stackIndex + 1), s);
 						}
 					} else{
 						g = -searcher.recurse(new SearchContext(1 - c.player, -c.beta, -alpha, nextDepth, SearchContext.NODE_TYPE_PV, c.stackIndex + 1), s);
 					}
 				}
 			}
 			s.undoMove();
 			assert props.zkey == s.zkey(); //keys should be unchanged after undo
 			assert drawCount == s.drawCount;
 			assert pawnZkey == s.pawnZkey();
 
 			final int score;
 			if(isDrawable && 0 > g){ //can take a draw instead of making the move
 				score = 0;
 				//encoding = 0;
 			} else{
 				score = g;
 			}
 
 			if(score > bestScore){
 				bestScore = score;
 				bestMove = encoding;
 
 				if(score >= c.beta){
					cutoffFlag = TTEntry.CUTOFF_TYPE_LOWER;
 
 					//check to see if killer move can be stored
 					//if used on null moves, need to have a separate killer array
 					if(c.stackIndex-1 >= 0){
 						Search34.attemptKillerStore(bestMove, c.skipNullMove, stack[c.stackIndex - 1]);
 					}
 
 					moveGen.betaCutoff(c.player, MoveEncoder.getMovePieceType(encoding),
 							MoveEncoder.getPos1(encoding),
 							MoveEncoder.getPos2(encoding), s, c.depth/Search34.ONE_PLY);
 
 					break;
 				} else if(score > alpha){
 					alpha = score;
 					cutoffFlag = TTEntry.CUTOFF_TYPE_EXACT;
 					moveGen.alphaRaised(c.player, MoveEncoder.getMovePieceType(encoding),
 							MoveEncoder.getPos1(encoding),
 							MoveEncoder.getPos2(encoding), s, c.depth/Search34.ONE_PLY);
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
 			fillEntry.fill(props.zkey, bestMove, bestScore, props.staticScore.toScoreEncoding(),
 					c.depth, cutoffFlag, searcher.getSeq());
 			m.put(props.zkey, fillEntry);
 		}
 
 		if(nt == SearchContext.NODE_TYPE_PV){
 			pvStore[c.stackIndex] = bestMove;
 		}
 
 		return bestScore;
 	}
 
 	private static int lmrReduction(final int depth, final int moveCount){
 		return lmrReduction[depth > 31? 31: depth][moveCount > 63? 63: moveCount];
 	}
 
 	/**
 	 * builds killer move set
 	 * @param c search context to
 	 * @param s state to use in testing killer moves for legality
 	 * @return returns killer move set
 	 */
 	private KillerMoveSet buildKMS(SearchContext c, State4 s) {
 		final long l1killer1;
 		final long l1killer2;
 		final long l2killer1;
 		final long l2killer2;
 
 		final MoveList mlist = stack[c.stackIndex].mlist;
 
 		if(c.stackIndex-1 >= 0 && !c.skipNullMove){
 			final StackFrame prev = stack[c.stackIndex-1];
 			final long l1killer1Temp = prev.killer[0];
 			if(l1killer1Temp != 0 && isPseudoLegal(c.player, l1killer1Temp, s)){
 				l1killer1 = l1killer1Temp & 0xFFFL;
 				mlist.add(l1killer1, MoveGen.killerMoveRank);
 			} else{
 				l1killer1 = 0;
 			}
 
 			final long l1killer2Temp = prev.killer[1];
 			if(l1killer2Temp != 0 && isPseudoLegal(c.player, l1killer2Temp, s)){
 				l1killer2 = l1killer2Temp & 0xFFFL;
 				mlist.add(l1killer2, MoveGen.killerMoveRank);
 			} else{
 				l1killer2 = 0;
 			}
 
 			if(c.stackIndex-3 >= 0){
 				final StackFrame prev2 = stack[c.stackIndex-3];
 				final long l2killer1Temp = prev2.killer[0];
 				if(l2killer1Temp != 0 && isPseudoLegal(c.player, l2killer1Temp, s)){
 					l2killer1 = l2killer1Temp & 0xFFFL;
 					mlist.add(l2killer1, MoveGen.killerMoveRank);
 				} else{
 					l2killer1 = 0;
 				}
 
 				final long l2killer2Temp = prev2.killer[1];
 				if(l2killer2Temp != 0 && isPseudoLegal(c.player, l2killer2Temp, s)){
 					l2killer2 = l2killer2Temp & 0xFFFL;
 					mlist.add(l2killer2, MoveGen.killerMoveRank);
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
 
 		return new KillerMoveSet(l1killer1, l1killer2, l2killer1, l2killer2);
 	}
 
 	/**
 	 * checks too see if a move is legal, assumming we do not start in check,
 	 * moving does not yield self check, we are not castling, and if moving a pawn
 	 * we have chosen a non take move that could be legal if no piece is
 	 * blocking the target square
 	 *
 	 * <p> used to check that killer moves are legal
 	 * @param player
 	 * @param encoding
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
 }
