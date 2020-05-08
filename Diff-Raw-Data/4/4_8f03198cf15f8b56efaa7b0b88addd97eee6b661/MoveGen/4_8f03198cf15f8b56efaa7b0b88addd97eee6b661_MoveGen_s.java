 package chess.search.search34.moveGen;
 
 import chess.eval.e9.pipeline.PieceWeights;
 import chess.state4.BitUtil;
 import chess.state4.Masks;
 import chess.state4.State4;
 
 public final class MoveGen {
 
 	public final static int tteMoveRank = 9999;
 	/** rank set to the first of the non takes */
 	public final static int killerMoveRank = 100;
 
 	private final static int maxWeight = 1 << 10;
 
 	private final FeatureSet[] f;
 
 	private final static int[] pieceValue = new int[7];
 
 	public MoveGen() {
 		f = new FeatureSet[2];
 		for (int a = 0; a < f.length; a++) f[a] = new FeatureSet();
 
 		pieceValue[State4.PIECE_TYPE_PAWN] = PieceWeights.pawn;
 		pieceValue[State4.PIECE_TYPE_BISHOP] = PieceWeights.bishop;
 		pieceValue[State4.PIECE_TYPE_KNIGHT] = PieceWeights.knight;
 		pieceValue[State4.PIECE_TYPE_ROOK] = PieceWeights.rook;
 		pieceValue[State4.PIECE_TYPE_QUEEN] = PieceWeights.queen;
 	}
 
 	private final static class FeatureSet {
 		int pawnPromotionWeight;
 		int passedPawnWeight;
 		/**
 		 * position weights, indexed [piece-type][start position][end position]
 		 */
 		final int[][][] posWeight = new int[7][64][64];
 	}
 
 	private final static long pawnLeftShiftMask = Masks.colMaskExc[7];
 	private final static long pawnRightShiftMask = Masks.colMaskExc[0];
 
 	/**
 	 *
 	 * @param player
 	 * @param pieceMovingType
 	 * @param pieceMask
 	 * @param moves
 	 * @param enemyDefendedPos positions defended by enemy pieces of lesser value
 	 * @param enemyPieces
 	 * @param mlist
 	 * @param promotionMask promotion mask for pawns
 	 * @param s
 	 * @param f
 	 */
 	private static void recordMoves(final int player, final int pieceMovingType,
 									final long pieceMask, final long moves,
 									final long enemyDefendedPos, final long enemyPieces,
 									final long promotionMask,
 									final MoveList mlist, final State4 s, final FeatureSet f) {
 
 		if (pieceMask != 0 && moves != 0) {
 
 			final int[] mailbox = s.mailbox;
 
 			final long piece = pieceMask & -pieceMask;
 
 			for (long m = moves; m != 0; m &= m - 1) {
 				final long move = m & -m;
 				final int moveIndex = BitUtil.lsbIndex(move);
 
 				final int gain;
 				if((move & enemyPieces) != 0){
 					gain = pieceValue[mailbox[moveIndex]];
 				} else{
 					gain = 0;
 				}
 
 				//penalty if position defended by lesser value piece
 				int defendedPenalty = (enemyDefendedPos & move) != 0? -10: 0;
 
 				int historyWeight = f != null? getMoveWeight(player, pieceMovingType, piece, move, f, s) * 20 / maxWeight: 0;
 
 				int baseRank = gain + defendedPenalty + historyWeight;
 
 				if(pieceMovingType == State4.PIECE_TYPE_PAWN){
 					if((move & promotionMask) != 0){
 						mlist.add(piece, move, baseRank + PieceWeights.queen, State4.PROMOTE_QUEEN);
 						mlist.add(piece, move, baseRank + PieceWeights.knight, State4.PIECE_TYPE_KNIGHT);
 					}
 				} else{
 					//non-pawn movement
 					mlist.add(piece, move, baseRank);
 				}
 			}
 		}
 	}
 
 	public void reset() {
 		for (int a = 0; a < f.length; a++) {
 			f[a].passedPawnWeight = 0;
 			f[a].pawnPromotionWeight = 0;
 			for (int q = 0; q < 7; q++) {
 				for (int w = 0; w < 64; w++) {
 					for (int s = 0; s < 64; s++) {
 						f[a].posWeight[q][w][s] = 0;
 					}
 				}
 			}
 		}
 	}
 
 	public void alphaRaised(final int player, final int pieceType, final int startPos,
 							final int movePos, final State4 s, final int depth) {
 		final FeatureSet f = this.f[player];
 
 		final int offset = depth;
 
 		final int index = movePos;
 		/*final long move = 1L << movePos;
 		if(pieceType == State4.PIECE_TYPE_PAWN){
 			final long ppMask = Masks.passedPawnMasks[player][index];
 			if((ppMask & s.pawns[1-player]) == 0){
 				//f.passedPawnWeight += offset;
 				final long pomotionMask = Masks.pawnPromotionMask[player];
 				if((move & pomotionMask) != 0){
 					//f.pawnPromotionWeight += offset;
 				}
 			}
 		}*/
 
 		f.posWeight[pieceType][startPos][index] += offset;
 		//f.pieceTypeWeight[pieceType] += offset;
 
 		if (f.posWeight[pieceType][startPos][index] >= maxWeight ||
 				f.passedPawnWeight >= maxWeight || f.pawnPromotionWeight >= maxWeight) {
 			dampen(f);
 		}
 	}
 
 	public void betaCutoff(final int player, final int pieceType, final int startPos,
 						   final int movePos, final State4 s, final int depth) {
 		final FeatureSet f = this.f[player];
 
 		final int offset = (depth * depth) >>> 1;
 
 		final int index = movePos;
 		/*final long move = 1L << movePos;
 		if(pieceType == State4.PIECE_TYPE_PAWN){
 			final long ppMask = Masks.passedPawnMasks[player][index];
 			if((ppMask & s.pawns[1-player]) == 0){
 				//f.passedPawnWeight += offset;
 				final long pomotionMask = Masks.pawnPromotionMask[player];
 				if((move & pomotionMask) != 0){
 					//f.pawnPromotionWeight += offset;
 				}
 			}
 		}*/
 
 		f.posWeight[pieceType][startPos][index] += offset;
 		//f.pieceTypeWeight[pieceType] += offset;
 
 		if (f.posWeight[pieceType][startPos][index] >= maxWeight ||
 				f.passedPawnWeight >= maxWeight || f.pawnPromotionWeight >= maxWeight) {
 			dampen(f);
 		}
 	}
 
 	public void dampen() {
 		dampen(f[0]);
 		dampen(f[1]);
 	}
 
 	/**
 	 * Lowers all weights
 	 * <p> Called when a weight gets too high. Helps the history heuristic
 	 * stay in line with the game tree being searched
 	 *
 	 * @param f
 	 */
 	private void dampen(final FeatureSet f) {
 		f.passedPawnWeight >>>= 1;
 		f.pawnPromotionWeight >>>= 1;
 		for (int q = 1; q < 7; q++) {
 			for (int a = 0; a < 64; a++) {
 				for (int z = 0; z < 64; z++) {
 					f.posWeight[q][a][z] >>>= 1;
 				}
 			}
 		}
 	}
 
 	private static int getMoveWeight(final int player, final int pieceType,
 									 final long piece, final long move, final FeatureSet f, final State4 s) {
 
 		int rank = 0;
 
 		final int startIndex = BitUtil.lsbIndex(piece);
 		final int index = BitUtil.lsbIndex(move);
 
 		if (pieceType == State4.PIECE_TYPE_PAWN) {
 			final long ppMask = Masks.passedPawnMasks[player][index];
 			if ((ppMask & s.pawns[1 - player]) == 0) {
 				rank += f.passedPawnWeight;
 
 				final long pomotionMask = Masks.pawnPromotionMask[player];
 				if ((move & pomotionMask) != 0) {
 					rank += f.pawnPromotionWeight;
 				}
 			}
 		}
 
 		rank += f.posWeight[pieceType][startIndex][index];
 
 		return rank;
 	}
 
 	/**
 	 * genereates moves
 	 *
 	 * @param player
 	 * @param s
 	 * @param alliedKingAttacked
 	 * @param mlist
 	 * @param quiesce
 	 */
 	public void genMoves(final int player, final State4 s, final boolean alliedKingAttacked,
 						 final MoveList mlist, final boolean quiesce) {
 
 		final FeatureSet f = quiesce ? null : this.f[player];
 
 		final long enemyPawnAttacks = Masks.getRawPawnAttacks(1 - player, s.pawns[1 - player]) & ~s.pieces[1 - player];
 
 		final long allied = s.pieces[player];
 		final long enemy = s.pieces[1 - player];
 		//final long enemyKing = s.kings[1-player];
 		final long agg = allied | enemy;
 
 		long quiesceMask = quiesce? enemy: ~0;
 		final long promotionMask = Masks.pawnPromotionMask[player];
 
 		if (alliedKingAttacked) {
 			long kingMoves = Masks.getRawKingMoves(s.kings[player]) & ~allied;
 			recordMoves(player, State4.PIECE_TYPE_KING, s.kings[player], kingMoves,
 					enemyPawnAttacks, enemy, promotionMask, mlist, s, f);
 		}
 
		final long queenUpTakes = s.queens[1 - player];
 		for (long queens = s.queens[player]; queens != 0; queens &= queens - 1) {
 			recordMoves(player, State4.PIECE_TYPE_QUEEN, queens,
 					Masks.getRawQueenMoves(agg, queens & -queens) & ~allied & quiesceMask,
 					enemyPawnAttacks, enemy, promotionMask, mlist, s, f);
 		}
 
 		for (long rooks = s.rooks[player]; rooks != 0; rooks &= rooks - 1) {
 			recordMoves(player, State4.PIECE_TYPE_ROOK, rooks,
 					Masks.getRawRookMoves(agg, rooks & -rooks) & ~allied & quiesceMask,
 					enemyPawnAttacks, enemy, promotionMask, mlist, s, f);
 		}
 
 		for (long knights = s.knights[player]; knights != 0; knights &= knights - 1) {
 			recordMoves(player, State4.PIECE_TYPE_KNIGHT, knights,
 					Masks.getRawKnightMoves(knights & -knights) & ~allied & quiesceMask,
 					enemyPawnAttacks, enemy, promotionMask, mlist, s, f);
 		}
 
 		for (long bishops = s.bishops[player]; bishops != 0; bishops &= bishops - 1) {
 			recordMoves(player, State4.PIECE_TYPE_BISHOP, bishops,
					Masks.getRawBishopMoves(agg, bishops & -bishops) & ~allied,
 					enemyPawnAttacks, enemy, promotionMask, mlist, s, f);
 		}
 
 		final long open = ~agg;
 		final long enPassant = s.enPassante;
 		for (long pawns = s.pawns[player]; pawns != 0; pawns &= pawns - 1) {
 			final long p = pawns & -pawns;
 			final long attacks;
 			final long l1move;
 			final long l2move;
 			if (player == 0) {
 				attacks = (((p << 7) & pawnLeftShiftMask) | ((p << 9) & pawnRightShiftMask)) & (enemy | enPassant);
 				l1move = (p << 8) & open;
 				l2move = ((((p & 0xFF00L) << 8) & open) << 8) & open;
 			} else {
 				attacks = (((p >>> 9) & pawnLeftShiftMask) | ((p >>> 7) & pawnRightShiftMask)) & (enemy | enPassant);
 				l1move = (p >>> 8) & open;
 				l2move = ((((p & 0xFF000000000000L) >>> 8) & open) >>> 8) & open;
 			}
 
 			long moves = attacks | l1move | l2move;
 			recordMoves(player, State4.PIECE_TYPE_PAWN, p, moves & (quiesceMask | promotionMask), 0, enemy, promotionMask, mlist, s, f);
 		}
 
 		if (!alliedKingAttacked) {
 			long kingMoves = ((Masks.getRawKingMoves(s.kings[player]) & ~allied) | State4.getCastleMoves(player, s)) & quiesceMask;
 			recordMoves(player, State4.PIECE_TYPE_KING, s.kings[player],
 					kingMoves,
 					enemyPawnAttacks, enemy, promotionMask, mlist, s, f);
 		}
 	}
 }
