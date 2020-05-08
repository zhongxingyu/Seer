 package com.chess.genesis;
 
 class RegPosition extends RegMoveLookup
 {
 	private static final int[] TYPE = {
 		Piece.EMPTY,		Piece.EMPTY,		Piece.BLACK_KING,	Piece.WHITE_BISHOP,
 		Piece.EMPTY,		Piece.BLACK_KNIGHT,	Piece.EMPTY,		Piece.BLACK_PAWN,
 		Piece.BLACK_QUEEN,	Piece.BLACK_ROOK,	Piece.EMPTY,		Piece.EMPTY,
 		Piece.WHITE_KING,	Piece.EMPTY,		Piece.BLACK_BISHOP,	Piece.WHITE_KNIGHT,
 		Piece.EMPTY,		Piece.WHITE_PAWN,	Piece.WHITE_QUEEN,	Piece.WHITE_ROOK};
 
 	private static final RegPiece[] InitRegParse = {
 		new RegPiece(Piece.DEAD, Piece.BLACK_PAWN), new RegPiece(Piece.DEAD, Piece.BLACK_PAWN), new RegPiece(Piece.DEAD, Piece.BLACK_PAWN), new RegPiece(Piece.DEAD, Piece.BLACK_PAWN),
 		new RegPiece(Piece.DEAD, Piece.BLACK_PAWN), new RegPiece(Piece.DEAD, Piece.BLACK_PAWN), new RegPiece(Piece.DEAD, Piece.BLACK_PAWN), new RegPiece(Piece.DEAD, Piece.BLACK_PAWN),
 		new RegPiece(Piece.DEAD, Piece.BLACK_KNIGHT), new RegPiece(Piece.DEAD, Piece.BLACK_KNIGHT), new RegPiece(Piece.DEAD, Piece.BLACK_BISHOP), new RegPiece(Piece.DEAD, Piece.BLACK_BISHOP),
 		new RegPiece(Piece.DEAD, Piece.BLACK_ROOK), new RegPiece(Piece.DEAD, Piece.BLACK_ROOK), new RegPiece(Piece.DEAD, Piece.BLACK_QUEEN), new RegPiece(Piece.DEAD, Piece.BLACK_KING),
 		new RegPiece(Piece.DEAD, Piece.WHITE_PAWN), new RegPiece(Piece.DEAD, Piece.WHITE_PAWN), new RegPiece(Piece.DEAD, Piece.WHITE_PAWN), new RegPiece(Piece.DEAD, Piece.WHITE_PAWN),
 		new RegPiece(Piece.DEAD, Piece.WHITE_PAWN), new RegPiece(Piece.DEAD, Piece.WHITE_PAWN), new RegPiece(Piece.DEAD, Piece.WHITE_PAWN), new RegPiece(Piece.DEAD, Piece.WHITE_PAWN),
 		new RegPiece(Piece.DEAD, Piece.WHITE_KNIGHT), new RegPiece(Piece.DEAD, Piece.WHITE_KNIGHT), new RegPiece(Piece.DEAD, Piece.WHITE_BISHOP), new RegPiece(Piece.DEAD, Piece.WHITE_BISHOP),
 		new RegPiece(Piece.DEAD, Piece.WHITE_ROOK), new RegPiece(Piece.DEAD, Piece.WHITE_ROOK), new RegPiece(Piece.DEAD, Piece.WHITE_QUEEN), new RegPiece(Piece.DEAD, Piece.WHITE_KING) };
 
 	public RegPiece[] piece;
 	public MoveFlags flags;
 
 	public int ply;
 	public int stm;
 	
 	public RegPosition()
 	{
 		square = new int[64];
 		piece = new RegPiece[32];
 		flags = new MoveFlags();
 	}
 
 	private void parseReset()
 	{
 		for (int i = 0; i < 64; i++) 
 			square[i] = Piece.EMPTY;
 		piece = RegPiece.arrayCopy(InitRegParse);
 		flags.reset();
 	}
 
 	private boolean setPiece(final int loc, final int type)
 	{
 		final int[] offset = {-1, 0, 8, 10, 12, 14, 15, 16};
 		final int start = ((type < 0)? 0 : 16) + offset[Math.abs(type)],
 			end = ((type < 0)? 0 : 16) + offset[Math.abs(type) + 1];
 
 		// first try for setting non promoted pieces
 		for (int i = start; i < end; i++) {
 			if (piece[i].loc == Piece.DEAD) {
 				piece[i].loc = loc;
 				square[loc] = type;
 				return true;
 			}
 		}
 
 		// piece might be a promote
 		if (Math.abs(type) == Piece.PAWN || Math.abs(type) == Piece.KING)
 			return false;
 
 		final int pstart = (type > 0)? 16:0, pend = (type > 0)? 24:8;
 		for (int i = pstart; i < pend; i++) {
 			if (piece[i].loc == Piece.DEAD) {
 				piece[i].loc = loc;
 				piece[i].type = type;
 				square[loc] = type;
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean incheck(final int color)
 	{
 		final int king = (color == Piece.WHITE)? 31:15;
 
 		return isAttacked(piece[king].loc, color);
 	}
 
 	public boolean parseZfen(final String pos)
 	{
 		parseReset();
 		final char[] st = pos.toCharArray();
 
 		// index counter for st
 		int n = 0;
 
 		// parse board
 		StringBuffer num = new StringBuffer();
 		for (int loc = 0, act = 0; true; n++) {
 			if (Character.isDigit(st[n])) {
 				num.append(st[n]);
 				act = 1;
 			} else if (Character.isLetter(st[n])) {
 				if (act != 0) {
 					loc += Integer.parseInt(num.toString());
 					num = new StringBuffer();
 					act = 0;
 				}
 				if (!setPiece(loc, TYPE[st[n] % 21]))
 					return false;
 				loc++;
 			} else if (st[n] == ':') {
 				n++;
 				break;
 			} else {
 				return false;
 			}
 		}
 
 		// parse castle rights
 		int castle = 0;
 		for (; st[n] != ':'; n++) {
 			switch (st[n]) {
 			case 'K':
 				castle |= 0x10;
 				break;
 			case 'Q':
 				castle |= 0x20;
 				break;
 			case 'k':
 				castle |= 0x40;
 				break;
 			case 'q':
 				castle |= 0x80;
 				break;
 			}
 		}
 		flags.setCastle(castle);
 
 		// parse en passant
 		n++;
 		if (st[n] != ':') {
 			int eps = st[n++] - 'a';
 			eps += 8 * (8 - (st[n++] - '0'));
 			flags.setEnPassant(eps & 0x7);
 		}
 		n++;
 
 		// parse half-ply
 		num = new StringBuffer();
 		while (Character.isDigit(st[n])) {
 			num.append(st[n]);
 			n++;
 		}
 		final int tply = Integer.valueOf(num.toString());
 		ply = (tply >= 0)? tply:0;
 
 		// check if color not on move is in check
 		stm = (ply % 2 != 0)? Piece.BLACK : Piece.WHITE;
 		if (incheck(stm ^ -2))
 			return false;
 		return true;
 	}
 
 	public String printZfen()
 	{
 		final StringBuffer fen = new StringBuffer();
 
 		for (int i = 0, empty = 0; i < 64; i++) {
 			if (square[i] == Piece.EMPTY) {
 				empty++;
 				continue;
 			}
 			if (empty != 0)
 				fen.append(empty);
 			if (square[i] > Piece.EMPTY)
 				fen.append(Move.pieceSymbol[square[i]]);
 			else
 				fen.append(String.valueOf(Move.pieceSymbol[-square[i]]).toLowerCase());
 			empty = 0;
 		}
 		fen.append(':');
 
 		// print castle rights
 		if ((flags.bits & 0xf0) != 0) {
 			if (flags.canKingCastle(Piece.WHITE) != 0)
 				fen.append('K');
 			if (flags.canQueenCastle(Piece.WHITE) != 0)
 				fen.append('Q');
 			if (flags.canKingCastle(Piece.BLACK) != 0)
 				fen.append('k');
 			if (flags.canQueenCastle(Piece.BLACK) != 0)
 				fen.append('q');
 		}
 		fen.append(':');
 
 		if (flags.canEnPassant() != 0) {
			fen.append((char) ('a' + flags.enPassantFile()));
 			fen.append((ply % 2 != 0)? '3':'6');
 		}
 		fen.append(':');
		fen.append(ply);
 
 		return fen.toString();
 	}
 }
