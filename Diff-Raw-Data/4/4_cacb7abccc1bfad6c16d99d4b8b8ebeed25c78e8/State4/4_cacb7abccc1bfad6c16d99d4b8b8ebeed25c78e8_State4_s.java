 package state4;
 
 
 
 public final class State4 {
 	
 	public static final int WHITE = 0;
 	public static final int BLACK = 1;
 	
 	public static final int PIECE_TYPE_EMPTY = 0; //must stay 0, others can change
 	public static final int PIECE_TYPE_KING = 1;
 	public static final int PIECE_TYPE_QUEEN = 2;
 	public static final int PIECE_TYPE_ROOK = 3;
 	public static final int PIECE_TYPE_BISHOP = 4;
 	public static final int PIECE_TYPE_KNIGHT = 5;
 	public static final int PIECE_TYPE_PAWN = 6;
 	
 	private final long[][] pieceMasks = new long[7][2];
 	
 	/** stores piece counts for each player, total pieces
 	 * stored in {@link #PIECE_TYPE_EMPTY} index*/
 	public final int[][] pieceCounts = new int[2][7];
 	
 	/** aggregate piece mask*/
 	public final long[] pieces = pieceMasks[PIECE_TYPE_EMPTY];
 	/** stores the piece type on each board index*/
 	public final int[] mailbox = new int[64];
 	/** stores possible enPassante square*/
 	public long enPassante = 0;
 	
 	public final long[] pawns = pieceMasks[PIECE_TYPE_PAWN];
 	public final long[] knights = pieceMasks[PIECE_TYPE_KNIGHT];
 	public final long[] kings = pieceMasks[PIECE_TYPE_KING];
 	public final long[] queens = pieceMasks[PIECE_TYPE_QUEEN];
 	public final long[] rooks = pieceMasks[PIECE_TYPE_ROOK];
 	public final long[] bishops = pieceMasks[PIECE_TYPE_BISHOP];
 	
 	private final long[] history = new long[128];
 	/** index in {@link #history} of first unused record*/
 	private int hindex = 0;
 	
 	/** zobrist hash key*/
 	private long zkey = 0;
 	private final ZHash zhash;
 	private final HistoryMap2 hm = new HistoryMap2(10);
 	/** appearance hash, applied to zkey to denote how many times a positions hash appeared*/
 	private final long[] appHashs;
 	/** seed used to generate zkey hashes*/
 	private final long zkeySeed;
 	
 	/** records if king has moved to determine castling props, indexed [player]*/
 	public final boolean[] kingMoved = new boolean[2];
 	/** records if rook has moved to determine castline props, indexed [player][rook] where rook==left-rook? 0: 1*/
 	public final boolean[][] rookMoved = new boolean[2][2];
 	/** count since last pawn move or take (for 50-move draw)*/
 	public int drawCount = 0;
 	
 	public State4(final long zkeySeed){
 		zhash = new ZHash(zkeySeed);
 		appHashs = new long[]{0, 0, zhash.appeared2, zhash.appeared3};
 		this.zkeySeed = zkeySeed;
 	}
 	
 	public State4(){
 		this(47388L);
 	}
 	
 	/** returns the seed used to generate the zobrist hash keys*/
 	public long getZkeySeed(){
 		return zkeySeed;
 	}
 	
 	/**
 	 * gets rook moves for a specified rook
 	 * @param pieces aggregate piece masks
 	 * @param rook uses lsb as rook
 	 * @return returns move mask for viable moves
 	 */
 	public static long getRookMoves(final int player, final long[] pieces, final long rook){
 		/*long agg = pieces[0] | pieces[1];
 		int pos = BitUtil.lsbIndex(rook);
 		long attackMask = Masks.rookMoves[pos] & agg;
 		int hashIndex = (int)(attackMask*Magics.rookMagics[pos] >>> (64-Magics.rookBits));
 		long move = Magics.rookMoveLookup[pos][hashIndex];*/
 		final long move = Masks.getRawRookMoves(pieces[0]|pieces[1], rook);
 		return (move & pieces[player]) ^ move;
 	}
 	
 	/** {@link #getRookMoves(int, long[], long)} */
 	public static long getBishopMoves(final int player, final long[] pieces, final long bishop){
 		/*long agg = pieces[0] | pieces[1];
 		int pos = BitUtil.lsbIndex(bishop);
 		long attackMask = Masks.bishopMoves[pos] & agg;
 		int hashIndex = (int)(attackMask*Magics.bishopMagics[pos] >>> (64-Magics.bishopBits));
 		long move = Magics.bishopMoveLookup[pos][hashIndex];*/
 		final long move = Masks.getRawBishopMoves(pieces[0]|pieces[1], bishop);
 		return (move & pieces[player]) ^ move;
 	}
 	
 	public static long getQueenMoves(final int player, final long[] pieces, final long queens){
 		return getRookMoves(player, pieces, queens) | getBishopMoves(player, pieces, queens);
 	}
 
 	/** returns knight moves for king in lsb position*/
 	public static long getKingMoves(final int player, final long[] pieces, final long king){
 		int index = BitUtil.lsbIndex(king);
 		//return Masks.kingMoves[index] & (~(pieces[0]|pieces[1]) | pieces[1-player]);
 		return (Masks.kingMoves[index] & pieces[player]) ^ Masks.kingMoves[index];
 	}
 	
 	/** returns knight moves for knight in lsb position*/
 	public static long getKnightMoves(final int player, final long[] pieces, final long knights){
 		int index = BitUtil.lsbIndex(knights);
 		//return Masks.knightMoves[index] & (~(pieces[0]|pieces[1]) | pieces[1-player]);
 		return (Masks.knightMoves[index] & pieces[player]) ^ Masks.knightMoves[index];
 	}
 	
 	public static long getLeftPawnAttacks(final int player, final long[] pieces, final long enPassante, final long pawns){
 		long colMask = player == 0? Masks.colMaskExc[7]: Masks.colMaskExc[0];
 		long enemy = pieces[1-player]|enPassante;
 		return player == 0? (pawns << 7) & colMask & enemy:
 			(pawns >>> 7) & colMask & enemy;
 	}
 	
 	public static long getRightPawnAttacks(final int player, final long[] pieces, final long enPassante, final long pawns){
 		long colMask = player == 0? Masks.colMaskExc[0]: Masks.colMaskExc[7];
 		long enemy = pieces[1-player]|enPassante;
 		return player == 0? (pawns << 9) & colMask & enemy:
 			(pawns >>> 9) & colMask & enemy;
 	}
 	
 	/** gets pawn moves that move the pawns 1 square*/
 	public static long getPawnMoves(int player, long[] pieces, long pawns){
 		final int offset = player == 0? 8: -8;
 		final long open = ~(pieces[0] | pieces[1]);
 		return offset >= 0? (pawns << offset) & open: (pawns >>> -offset) & open;
 	}
 
 	/** gets pawn moves that move the pawns 2 squares*/
 	public static long getPawnMoves2(int player, long[] pieces, long pawns){
 		final int offset = player == 0? 8: -8;
 		final long open = ~(pieces[0] | pieces[1]);
 		pawns &= player == 0? 0xFF00L: 0xFF000000000000L;
 		return offset >= 0?
 				(((pawns << offset) & open) << offset & open):
 				(((pawns >>> -offset) & open) >>> -offset & open);
 	}
 	
 	public static long getCastleMoves(int player, State4 s){
 		if(!s.kingMoved[player] && (!s.rookMoved[player][0] || !s.rookMoved[player][1])){
 			long moves = 0;
 			final long agg = s.pieces[0]|s.pieces[1];
 			if(!s.rookMoved[player][0] && (Masks.castleBlockedMask[player][0] & agg) == 0 &&
 					!isAttacked(Masks.castleThroughCheck[player][0], 1-player, s)){
 				moves |= Masks.castleMask[player][0];
 			}
 			if(!s.rookMoved[player][1] && (Masks.castleBlockedMask[player][1] & agg) == 0 &&
 					!isAttacked(Masks.castleThroughCheck[player][1], 1-player, s)){
 				moves |= Masks.castleMask[player][1];
 			}
 			return moves;
 		}
 		return 0;
 	}
 	
 	/** clears stored history, should be called just before beginning a new search*/
 	public void resetHistory(){
 		hindex = 0;
 	}
 	
 	/** if true, player to move has the option to draw*/
 	public boolean isDrawable(){
 		return hm.get(zkey) >= 3;
 	}
 	
 	/** if true, game is a forced draw (via 50 move draw)*/
 	public boolean isForcedDraw(){
 		return drawCount >= 100;
 	}
 	
 	/**
 	 * quicker check to see if a single position index is attacked.
 	 * use {@link #isAttacked(long, int, State4)} to handle entire position masks
 	 * @param pos
 	 * @param player player doing the attacking attacking
 	 * @param s
 	 */
 	public static boolean isAttacked2(final int pos, final int player, final State4 s){
 		final long l = 1L<<pos;
 		
 		long colMask = player == 0? Masks.colMaskExc[7]: Masks.colMaskExc[0];
 		final long pawns = s.pawns[player];
 		long temp = player == 0? (pawns << 7) & colMask & l: (pawns >>> 7) & colMask & l;
 		colMask = player == 0? Masks.colMaskExc[0]: Masks.colMaskExc[7];
 		temp |= player == 0? (pawns << 9) & colMask & l: (pawns >>> 9) & colMask & l;
 
 		final long agg = s.pieces[0] | s.pieces[1] | l;
 		
 		/*System.out.println("pawn attacks = "+temp);
 		System.out.println("bishop attackes = "+(Masks.getRawBishopMoves(agg, l) & (s.queens[player]|s.bishops[player])));
 		System.out.println("rook attackes = "+(Masks.getRawRookMoves(agg, l) & (s.queens[player]|s.rooks[player])));
 		System.out.println("knight attacks = "+(Masks.getRawKnightMoves(l) & s.knights[player]));
 		System.out.println("king attacks = "+(Masks.getRawKingMoves(l) & s.kings[player]));
 		System.out.println(Masks.getString(Masks.getRawBishopMoves(agg|l, l)));
 		System.out.println(Masks.getString(getBishopMoves(1-player, s.pieces, l)));
 		System.out.println(Masks.getString(agg));*/
 		
 		return temp != 0 ||
 				((Masks.getRawBishopMoves(agg, l) & (s.queens[player]|s.bishops[player])) |
 				(Masks.getRawRookMoves(agg, l) & (s.queens[player]|s.rooks[player])) |
 				(Masks.getRawKnightMoves(l) & s.knights[player]) |
 				(Masks.getRawKingMoves(l) & s.kings[player])) != 0;
 	}
 	
 	/**
 	 * checks to see if the passed position mask is attacked by passed player
 	 * @param posMask
 	 * @param player
 	 * @param s
 	 * @return returns true if attacked, false otherwise
 	 */
 	public static boolean isAttacked(long posMask, final int player, final State4 s){
 		for(; posMask != 0; posMask &= posMask-1){
 			if(isAttacked2(BitUtil.lsbIndex(posMask), player, s)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/** conveneince method for executing a move stored in a move encoding*/
 	public long executeMove(int player, long encoding){
 		long pieceMask = 1L<<MoveEncoder.getPos1(encoding);
 		long moveMask = 1L<<MoveEncoder.getPos2(encoding);
 		return executeMove(player, pieceMask, moveMask);
 	}
 	
 	/**
 	 * executs passed move
 	 * @param player player moving
 	 * @param pieceMask mask for starting position of the piece to move
 	 * @param moveMask mask for final location of the piece
 	 * @return returns move encoding
 	 */
 	public long executeMove(int player, long pieceMask, long moveMask){
 		final int pos1 = BitUtil.lsbIndex(pieceMask);
 		final int pos2 = BitUtil.lsbIndex(moveMask);
 		
 		assert pieceMask != 0;
 		assert moveMask != 0;
 		assert player == 0 || player == 1;
 		assert (pieceMask & pieces[1-player]) == 0;
 		assert (moveMask & pieces[player]) == 0;
 		assert (pieces[player] & pieces[1-player]) == 0;
 		
 		zkey ^= zhash.turnChange;
 		final int movingType = mailbox[pos1];
 		final int takenType = mailbox[pos2];
 		
 		/*if(type == 0){
 			System.out.println(pos1+" -> "+pos2);
 			System.out.println(this);
 		}*/
 		assert movingType != 0;
 		
 		zkey ^= zhash.zhash[player][movingType][pos1] ^ zhash.zhash[player][movingType][pos2];
 		
 		long castleBit = 0;
 		if(mailbox[pos1] == PIECE_TYPE_KING && !kingMoved[player]){
 			//System.out.println("castling");
 			if(player == 0){
 				if(pos2 == 2){
 					//castle left
 					rooks[0] &= ~1L;
 					rooks[0] |= 0x8L;
 					mailbox[0] = PIECE_TYPE_EMPTY;
 					mailbox[3] = PIECE_TYPE_ROOK;
 					zkey ^= zhash.zhash[0][PIECE_TYPE_ROOK][0] ^
 							zhash.zhash[0][PIECE_TYPE_ROOK][3];
 					castleBit = 1;
 				} else if(pos2 == 6){
 					//castle right
 					rooks[0] &= ~0x80L;
 					rooks[0] |= 0x20L;
 					mailbox[7] = PIECE_TYPE_EMPTY;
 					mailbox[5] = PIECE_TYPE_ROOK;
 					zkey ^= zhash.zhash[0][PIECE_TYPE_ROOK][7] ^
 							zhash.zhash[0][PIECE_TYPE_ROOK][5];
 					castleBit = 1;
 				}
 			} else if(player == 1){
 				if(pos2 == 58){
 					//castle left
 					rooks[1] &= ~0x100000000000000L;
 					rooks[1] |= 0x800000000000000L;
 					mailbox[56] = PIECE_TYPE_EMPTY;
 					mailbox[59] = PIECE_TYPE_ROOK;
 					zkey ^= zhash.zhash[1][PIECE_TYPE_ROOK][56] ^
 							zhash.zhash[1][PIECE_TYPE_ROOK][59];
 					castleBit = 1;
 				} else if(pos2 == 62){
 					//castle right
 					rooks[1] &= ~0x8000000000000000L;
 					rooks[1] |= 0x2000000000000000L;
 					mailbox[63] = PIECE_TYPE_EMPTY;
 					mailbox[61] = PIECE_TYPE_ROOK;
 					zkey ^= zhash.zhash[1][PIECE_TYPE_ROOK][63] ^
 							zhash.zhash[1][PIECE_TYPE_ROOK][61];
 					castleBit = 1;
 				}
 			} 
 		}
 		
 		long encoding = MoveEncoder.encode(pos1, pos2, movingType, takenType, player, this);
 		encoding |= castleBit<<33;
 		//move the first piece
 		final long[] b1 = pieceMasks[movingType];
 		b1[player] = (b1[player] & ~pieceMask) | moveMask;
 		//remove the second piece if move was a take (non-branching)
 		final long isTake = BitUtil.isDef(mailbox[pos2]);
 		final long[] b2 = pieceMasks[takenType];
 		b2[1-player] = b2[1-player] & ~(moveMask*isTake);
 		pieceCounts[1-player][mailbox[pos2]] -= 1*isTake;
 		pieceCounts[1-player][PIECE_TYPE_EMPTY] -= 1*isTake;
 		zkey ^= zhash.zhash[1-player][mailbox[pos2]][pos2]*isTake;
 		//remove the second piece if move was a take (branching)
 		/*if(mailbox[pos2] != PIECE_TYPE_EMPTY){
 			//piece take
 			long[] b2 = pieceMasks[mailbox[pos2]];
 			b2[1-player] = b2[1-player] & ~moveMask;
 			pieceCounts[1-player][mailbox[pos2]]--;
 			pieceCounts[1-player][PIECE_TYPE_EMPTY]--;
 			
 			zkey ^= zhash.zhash[1-player][mailbox[pos2]][pos2];
 		}*/
 		
 		assert pos1 != pos2;
 		
 		mailbox[pos2] = mailbox[pos1];
 		mailbox[pos1] = PIECE_TYPE_EMPTY;
 
 		long prevEnPassante = enPassante; //make new copy to clear old
 		
 		//non-branching code below is bugged
 		/*long hasPrevEnPassant = BitUtil.isDef(enPassante);
 		zkey ^= zhash.enPassante[BitUtil.lsbIndex(enPassante)]*hasPrevEnPassant;
 		encoding = MoveEncoder.setPrevEnPassantePos(prevEnPassante, encoding);*/
 		if(enPassante != 0){
 			zkey ^= zhash.enPassante[BitUtil.lsbIndex(enPassante)];
 			encoding = MoveEncoder.setPrevEnPassantePos(prevEnPassante, encoding);
 		}
 		
 		enPassante = 0;
 		if(mailbox[pos2] == PIECE_TYPE_PAWN){
 			/*if((player == 0 && (moveMask & 0xFF00000000000000L) != 0) ||
 					(player == 1 && (moveMask & 0xFFL) != 0)){*/
 			if((moveMask & Masks.pawnPromotionMask[player]) != 0){
 				//pawn promotion
 				mailbox[pos2] = PIECE_TYPE_QUEEN;
 				pawns[player] &= ~moveMask;
 				queens[player] |= moveMask;
 				pieceCounts[player][PIECE_TYPE_PAWN]--;
 				pieceCounts[player][PIECE_TYPE_QUEEN]++;
 				encoding = MoveEncoder.setPawnPromotion(encoding);
 				
 				zkey ^= zhash.zhash[player][PIECE_TYPE_PAWN][pos2] ^
 						zhash.zhash[player][PIECE_TYPE_QUEEN][pos2];
 				//System.out.println("pawn promoted");
 			} else if((player == 0 && (pieceMask & 0xFF00L) != 0 && (moveMask & 0xFF000000L) != 0) ||
 					(player == 1 && (pieceMask & 0xFF000000000000L) != 0 && (moveMask & 0xFF00000000L) != 0)){
 				//pawn moved 2 squares, set en passante
 				enPassante = player == 0? moveMask >>> 8: moveMask << 8;
 				//System.out.println("possible en passante set, pos = "+BitUtil.lsbIndex(enPassante));
 				zkey ^= zhash.enPassante[BitUtil.lsbIndex(enPassante)];
 			} else if(moveMask == prevEnPassante){
 				//making an en passante take move
 				final long takePos = player == 0? moveMask >>> 8: moveMask << 8;
 				pawns[1-player] &= ~takePos;
 				int pos3 = BitUtil.lsbIndex(takePos);
 				mailbox[pos3] = PIECE_TYPE_EMPTY;
 				pieceCounts[1-player][PIECE_TYPE_PAWN]--;
 				encoding = MoveEncoder.setEnPassanteTake(encoding);
 				pieceCounts[1-player][PIECE_TYPE_EMPTY]--;
 				zkey ^= zhash.zhash[1-player][PIECE_TYPE_PAWN][pos3];
 			}
 		}
 		
 
 		encoding = MoveEncoder.setPrevDrawCount(encoding, drawCount);
 		drawCount++;
 		if(movingType == PIECE_TYPE_PAWN || takenType != PIECE_TYPE_EMPTY){
 			drawCount = 0;
 		}
 		
 		collect();
 		assert (pieces[player] & pieces[1-player]) == 0;
 		
 		history[hindex++] = encoding;
 		hm.put(zkey);
 		
 		return encoding;
 	}
 	
 	public void nullMove(){
 		zkey ^= zhash.turnChange;
 		
 		long encoding = 0;
 		/*long hasPrevEnPassant = BitUtil.isDef(enPassante);
 		zkey ^= zhash.enPassante[BitUtil.lsbIndex(enPassante)]*hasPrevEnPassant;
 		encoding = MoveEncoder.setPrevEnPassantePos(prevEnPassante, encoding);*/
 		if(enPassante != 0){
 			zkey ^= zhash.enPassante[BitUtil.lsbIndex(enPassante)];
 			encoding = MoveEncoder.setPrevEnPassantePos(enPassante, encoding);
 		}
 		enPassante = 0;
 		encoding = MoveEncoder.setPrevDrawCount(encoding, drawCount);
 		
 		history[hindex++] = encoding;
 	}
 	
 	public void undoNullMove(){
 		zkey ^= zhash.turnChange;
 		final long encoding = history[--hindex];
 		
 		final long prevEnPassantPos = MoveEncoder.getPrevEnPassantePos(encoding);
 		final long hasPrevEnPassant = BitUtil.isDef(prevEnPassantPos);
 		enPassante = (1L<<prevEnPassantPos)*hasPrevEnPassant;
 		zkey ^= zhash.enPassante[BitUtil.lsbIndex(enPassante)]*hasPrevEnPassant;
 		/*if(MoveEncoder.getPrevEnPassantePos(encoding) != 0){
 			enPassante = 1L<<MoveEncoder.getPrevEnPassantePos(encoding);
 			zkey ^= zhash.enPassante[BitUtil.lsbIndex(enPassante)];
 		}*/
 		drawCount = MoveEncoder.getPrevDrawCount(encoding);
 	}
 	
 	public void undoMove(){
 		final long encoding = history[--hindex];
 		
 		hm.remove(zkey);
 		
 		assert encoding != 0;
 		assert (pieces[0] & pieces[1]) == 0;
 		
 		
 		final long q = 1;
 		final int pos1 = MoveEncoder.getPos1(encoding);
 		final int pos2 = MoveEncoder.getPos2(encoding);
 		final int taken = MoveEncoder.getTakenType(encoding);
 		final int player = MoveEncoder.getPlayer(encoding);
 		
 		zkey ^= zhash.turnChange;
 		int type = mailbox[pos2];
 
 		assert type != 0;
 		
 		
 		zkey ^= zhash.zhash[player][type][pos2] ^ zhash.zhash[player][type][pos1];
 		
 		//final long[] b1 = getBoard(mailbox[pos2]);
 		final long[] b1 = pieceMasks[mailbox[pos2]];
 		
 		if(mailbox[pos2] == PIECE_TYPE_KING && MoveEncoder.isFirstKingMove(player, encoding)){
 			//System.out.println("undoing castle");
 			if(player == 0){
 				if(pos2 == 2){
 					//castle left
 					rooks[0] &= ~0x8L;
 					rooks[0] |= 1L;
 					mailbox[3] = PIECE_TYPE_EMPTY;
 					mailbox[0] = PIECE_TYPE_ROOK;
 					zkey ^= zhash.zhash[0][PIECE_TYPE_ROOK][0] ^
 							zhash.zhash[0][PIECE_TYPE_ROOK][3];
 				} else if(pos2 == 6){
 					//castle right
 					rooks[0] &= ~0x20L;
 					rooks[0] |= 0x80L;
 					mailbox[5] = PIECE_TYPE_EMPTY;
 					mailbox[7] = PIECE_TYPE_ROOK;
 					zkey ^= zhash.zhash[0][PIECE_TYPE_ROOK][5] ^
 							zhash.zhash[0][PIECE_TYPE_ROOK][7];
 				}
 			} else if(player == 1){
 				if(pos2 == 58){
 					//castle left
 					rooks[1] &= ~0x800000000000000L;
 					rooks[1] |= 0x100000000000000L;
 					mailbox[59] = PIECE_TYPE_EMPTY;
 					mailbox[56] = PIECE_TYPE_ROOK;
 					zkey ^= zhash.zhash[1][PIECE_TYPE_ROOK][59] ^
 							zhash.zhash[1][PIECE_TYPE_ROOK][56];
 				} else if(pos2 == 62){
 					//castle right
 					rooks[1] &= ~0x2000000000000000L;
 					rooks[1] |= 0x8000000000000000L;
 					mailbox[61] = PIECE_TYPE_EMPTY;
 					mailbox[63] = PIECE_TYPE_ROOK;
 					zkey ^= zhash.zhash[1][PIECE_TYPE_ROOK][61] ^
 							zhash.zhash[1][PIECE_TYPE_ROOK][63];
 				}
 			} 
 		}
 		MoveEncoder.undoCastleProps(encoding, this);
 		
 		//undo move of piece
 		b1[player] = (b1[player] & ~(q<<pos2)) | q<<pos1;
 		mailbox[pos1] = mailbox[pos2];
 		mailbox[pos2] = taken;
 		//add back taken piece (non-branching)
 		long isTake = BitUtil.isDef(taken);
 		final long[] b2 = pieceMasks[taken];
 		b2[1-player] |= (q<<pos2)*isTake;
 		pieceCounts[1-player][taken] += 1*isTake;
 		pieceCounts[1-player][PIECE_TYPE_EMPTY] += 1*isTake;
 		zkey ^= zhash.zhash[1-player][taken][pos2]*isTake;
 		//add back take piece (branching)
 		/*if(taken != PIECE_TYPE_EMPTY){
 			//final long[] b2 = getBoard(taken);
 			final long[] b2 = pieceMasks[taken];
 			b2[1-player] |= q<<pos2;
 			pieceCounts[1-player][taken]++;
 			pieceCounts[1-player][PIECE_TYPE_EMPTY]++;
 			zkey ^= zhash.zhash[1-player][taken][pos2];
 		}*/
 		
 		//pawn promotion
 		if(MoveEncoder.isPawnPromoted(encoding)){
 			long moveMask = 1L<<pos1;
 			mailbox[pos1] = PIECE_TYPE_PAWN;
 			queens[player] &= ~moveMask;
 			pawns[player] |= moveMask;
 			pieceCounts[player][PIECE_TYPE_PAWN]++;
 			pieceCounts[player][PIECE_TYPE_QUEEN]--;
 			//System.out.println("undoing pawn promotion");
 			/*System.out.println(this);*/
 			zkey ^= zhash.zhash[player][PIECE_TYPE_PAWN][pos1] ^
 					zhash.zhash[player][PIECE_TYPE_QUEEN][pos1];
 		}
 		
 		//clear any current en passant square (non-branching)
 		//zkey ^= BitUtil.isDef(enPassante)*zhash.enPassante[BitUtil.lsbIndex(enPassante)];
 		//clear any current en passant square (branching)
 		if(enPassante != 0){
 			zkey ^= zhash.enPassante[BitUtil.lsbIndex(enPassante)];
 		}
 		
 		enPassante = 0;
 		
 		//re-apply previous en passant square (non-branching)
 		final long prevEnPassantPos = MoveEncoder.getPrevEnPassantePos(encoding);
 		final long hasPrevEnPassant = BitUtil.isDef(prevEnPassantPos);
 		enPassante = (1L<<prevEnPassantPos)*hasPrevEnPassant;
 		zkey ^= zhash.enPassante[BitUtil.lsbIndex(enPassante)]*hasPrevEnPassant;
 		//re-apply previous en passant square (branching)
 		/*if(MoveEncoder.getPrevEnPassantePos(encoding) != 0){
 			//pawn moved 2 squares, set en passante
 			enPassante = 1L<<MoveEncoder.getPrevEnPassantePos(encoding);
 			zkey ^= zhash.enPassante[BitUtil.lsbIndex(enPassante)];
 		}*/
 		
 		//undo an en passant take (non-branching)
 		/*long isEnPassantTake = BitUtil.isDef(MoveEncoder.isEnPassanteTake(encoding));
 		final long takePos = isEnPassantTake*((1-player)*((1L<<pos2) >>> 8) + player*((1L<<pos2) << 8));
 		pawns[1-player] |= takePos;
 		int pos3 = BitUtil.lsbIndex(takePos);
 		long type3 = mailbox[pos3]*(1-isEnPassantTake) + PIECE_TYPE_PAWN*isEnPassantTake;
 		mailbox[pos3] = 0;
 		mailbox[pos3] += type3;
 		pieceCounts[1-player][PIECE_TYPE_PAWN] += 1*isEnPassantTake;
 		pieceCounts[1-player][PIECE_TYPE_EMPTY] += 1*isEnPassantTake;
 		zkey ^= zhash.zhash[1-player][PIECE_TYPE_PAWN][pos3] * isEnPassantTake;*/
 		//undo an en passante take (branching)
 		if(MoveEncoder.isEnPassanteTake(encoding) != 0){
 			final long takePos = player == 0? (1L<<pos2) >>> 8: (1L<<pos2) << 8;
 			pawns[1-player] |= takePos;
 			int pos3 = BitUtil.lsbIndex(takePos);
 			mailbox[pos3] = PIECE_TYPE_PAWN;
 			pieceCounts[1-player][PIECE_TYPE_PAWN]++;
 			//enPassante = takePos;
 			pieceCounts[1-player][PIECE_TYPE_EMPTY]++;
 			zkey ^= zhash.zhash[1-player][PIECE_TYPE_PAWN][pos3];
 		}
 
 		collect();
 		drawCount = MoveEncoder.getPrevDrawCount(encoding);
 		
 		assert (pieces[player] & pieces[1-player]) == 0;
 	}
 	
 	public void executeMove(int player, int x1, int y1, int x2, int y2) {
 		int pos1 = x1+8*y1;
 		int pos2 = x2+8*y2;
 		final long q = 1;
 		//System.out.println("moving: player = "+player+", pos1 = "+pos1+", pos2 = "+pos2);
 		executeMove(player, q<<pos1, q<<pos2);
 	}
 	
 	/** collects all pieces onto the player piece aggregators*/
 	public void collect(){
 		pieces[0] = pawns[0] | knights[0] | kings[0] | queens[0] | rooks[0] | bishops[0];
 		pieces[1] = pawns[1] | knights[1] | kings[1] | queens[1] | rooks[1] | bishops[1];
 	}
 	
 	public String toString(){
 		char[] c = new char[64];
 		for(int i = 0; i < 64; i++){
 			c[i] = '-';
 		}
 		
 		long[][] l = new long[][]{pawns,kings,queens,rooks,bishops,knights};
 		char[] rep = new char[]{'p','k','q','r','b','n'};
 		for(int i = 0; i < l.length; i++){
 			for(int a = 0; a <= 1; a++){
 				long p = l[i][a];
 				while(p != 0){
 					int index = BitUtil.lsbIndex(p);
 					c[index] = a == 0? rep[i]: Character.toUpperCase(rep[i]);
 					p = p&(p-1);
 				}
 			}
 		}
 		
 		String s = "";
 		String temp = "";
 		for(int i = 63; i >= 0; i--){
 			temp = c[i]+temp;
 			if(i % 8 == 0){
 				s += temp;
 				if(i != 0){
 					s += "\n";
 				}
 				temp = "";
 			}
 		}
 		return s;
 	}
 	
 	public void initialize(){
 		long p = 0xFFL;
 		long q = 0x8L;
 		long k = 0x10L;
 		long n = 0x42L;
 		long b = 0x24L;
 		long r = 0x81L;
 		pieceCounts[0][PIECE_TYPE_PAWN] = 8;
 		pieceCounts[0][PIECE_TYPE_KING] = 1;
 		pieceCounts[0][PIECE_TYPE_QUEEN] = 1;
 		pieceCounts[0][PIECE_TYPE_BISHOP] = 2;
 		pieceCounts[0][PIECE_TYPE_KNIGHT] = 2;
 		pieceCounts[0][PIECE_TYPE_ROOK] = 2;
 		pieceCounts[0][PIECE_TYPE_EMPTY] = 16;
 		System.arraycopy(pieceCounts[0], 0, pieceCounts[1], 0, 7);
 		
 		pawns[0] = p<<8;
 		pawns[1] = p<<8*6;
 		queens[0] = q;
 		queens[1] = q<<8*7;
 		kings[0] = k;
 		kings[1] = k<<8*7;
 		knights[0] = n;
 		knights[1] = n<<8*7;
 		bishops[0] = b;
 		bishops[1] = b<<8*7;
 		rooks[0] = r;
 		rooks[1] = r<<8*7;
 		
 		update();
 	}
 	
 	public long zkey(){
 		final int count = hm.get(zkey);
 		assert count >= 0;
 		
 		long castleKey = 0;
 		if(!kingMoved[0] && !rookMoved[0][0]) castleKey ^= zhash.canCastle[0][0];
 		if(!kingMoved[0] && !rookMoved[0][1]) castleKey ^= zhash.canCastle[0][1];
		if(!kingMoved[1] && !rookMoved[0][0]) castleKey ^= zhash.canCastle[1][0];
		if(!kingMoved[1] && !rookMoved[0][1]) castleKey ^= zhash.canCastle[1][1];
 		
 		return zkey ^ appHashs[count < 4? count: 3] ^ castleKey;
 	}
 	
 	/**
 	 * copies all relevant information from source into the destination
 	 * and clears the history information (as well as the hash holding
 	 * historical positions) from the destination
 	 * @param src
 	 * @param dest
 	 */
 	public static void copy(final State4 src, final State4 dest){
 		for(int a = 0; a < src.pieceMasks.length; a++){
 			System.arraycopy(src.pieceMasks[a], 0, dest.pieceMasks[a], 0, 2);
 		}
 		dest.enPassante = src.enPassante;
 		System.arraycopy(src.mailbox, 0, dest.mailbox, 0, 64);
 		for(int a = 0; a < src.pieceCounts.length; a++){
 			System.arraycopy(src.pieceCounts[a], 0, dest.pieceCounts[a], 0, 7);
 		}
 		System.arraycopy(src.kingMoved, 0, dest.kingMoved, 0, 2);
 		System.arraycopy(src.rookMoved[0], 0, dest.rookMoved[0], 0, 2);
 		System.arraycopy(src.rookMoved[1], 0, dest.rookMoved[1], 0, 2);
 		dest.zkey = src.zkey;
 		dest.resetHistory();
 		dest.hm.clear();
 	}
 	
 	/** 
 	 * updates mailbox, zkey, etc to the pieces set on the board
 	 * <p> this should only be called once after the pieces have been set up
 	 * on a new board. Afterwards, everything will be maintained incrementally
 	 */
 	public void update(){
 		long[][] l = new long[][]{pawns,kings,queens,rooks,bishops,knights};
 		int[] rep = new int[]{PIECE_TYPE_PAWN, PIECE_TYPE_KING, PIECE_TYPE_QUEEN,
 				PIECE_TYPE_ROOK, PIECE_TYPE_BISHOP, PIECE_TYPE_KNIGHT};
 		for(int i = 0; i < l.length; i++){
 			for(int a = 0; a <= 1; a++){
 				long piece = l[i][a];
 				while(piece != 0){
 					int index = BitUtil.lsbIndex(piece);
 					mailbox[index] = rep[i];
 					piece = piece&(piece-1);
 				}
 			}
 		}
 		
 		//build zkey
 		zkey = 0;
 		for(int f = 0; f < l.length; f++){
 			for(int i = 0; i < 2; i++){
 				long w = l[f][i];
 				while(w != 0){
 					int pos = BitUtil.lsbIndex(w&-w);
 					w &= w-1;
 					zkey ^= zhash.zhash[i][rep[f]][pos];
 				}
 			}
 		}
 		zkey ^= zhash.turn[0]; //NOTE: THIS METHOD SHOULD PROB BE CALLED WITH A PLAYER TO CORRECTLY SET THIS
 		if(enPassante != 0){
 			zkey ^= zhash.enPassante[BitUtil.lsbIndex(enPassante)];
 		}
 		
 		//add castling rights into zkey
 		/*for(int a = 0; a < 2; a++){
 			if(!kingMoved[a]){
 				for(int q = 0; q < 2; q++){
 					if(!rookMoved[a][q]){
 						zkey ^= zhash.canCastle[a][q];
 					}
 				}
 			}
 		}*/
 		
 
 		hm.clear();
 		hm.put(zkey);
 		collect();
 	}
 }
