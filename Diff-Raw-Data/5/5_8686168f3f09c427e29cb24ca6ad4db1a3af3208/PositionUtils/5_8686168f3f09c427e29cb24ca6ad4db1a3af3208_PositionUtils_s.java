 package com.christophdietze.jack.common.board;
 
 public class PositionUtils {
 
 	public static Position makeMoveVerified(Position position, Move move) throws IllegalMoveException {
		MoveLegality legality = MoveChecker.isPseudoLegalMove(null, null);
 		if (!legality.isLegal()) {
 			throw new IllegalMoveException(legality.getMessage());
 		}
 		Position trialPosition = makeMove(position, move);
		if (MoveChecker.canCaptureKing(/* trialPosition */null)) {
 			throw new IllegalMoveException();
 		}
 		return trialPosition;
 	}
 
 	public static Position makeMove(Position position, Move move) {
 		if (isPseudoLegalCastleMove(position, move)) {
 			// castle move
 			Position.Builder builder = new Position.Builder(position);
 			builder.clearEnPassantPawn().clearPhantomKings().switchPlayerToMove();
 			if (builder.isWhiteToMove()) {
 				builder.fullmoveNumber(builder.getFullmoveNumber() + 1);
 			}
 			makeCastleMove(builder, move);
 			return builder.build();
 		} else {
 			// normal move
 			Position.Builder builder = new Position.Builder(position);
 			builder.clearEnPassantPawn().clearPhantomKings();
 			Piece fromPiece = position.getPiece(move.getFrom());
 			Piece toPiece = position.getPiece(move.getTo());
 			builder.clearPhantomKings();
 			// adjust castling availability
 			if (fromPiece == Piece.WHITE_KING) {
 				builder.canWhiteCastleKingside(false).canWhiteCastleQueenside(false);
 			} else if (fromPiece == Piece.BLACK_KING) {
 				builder.canBlackCastleKingside(false).canBlackCastleQueenside(false);
 			}
 			if (move.getFrom() == 0) {
 				builder.canWhiteCastleQueenside(false);
 			} else if (move.getFrom() == 7) {
 				builder.canWhiteCastleKingside(false);
 			} else if (move.getFrom() == 56) {
 				builder.canBlackCastleQueenside(false);
 			} else if (move.getFrom() == 63) {
 				builder.canBlackCastleKingside(false);
 			}
 
 			// if we're capturing an en passant pawn, remove the real pawn, too
 			if (toPiece == Piece.WHITE_EN_PASSANT_PAWN) {
 				builder.piece(move.getTo() + 8, Piece.EMPTY);
 			} else if (toPiece == Piece.BLACK_EN_PASSANT_PAWN) {
 				builder.piece(move.getTo() - 8, Piece.EMPTY);
 			}
 
 			builder.clearEnPassantPawn();
 
 			// create en passant pawns if necessary
 			if (fromPiece == Piece.WHITE_PAWN && move.getFrom() / 8 == 1 && move.getTo() / 8 == 3) {
 				int enPassantPawnIndex = move.getFrom() + 8;
 				builder.enPassantPawnIndex(enPassantPawnIndex, true);
 			} else if (fromPiece == Piece.BLACK_PAWN && move.getFrom() / 8 == 6 && move.getTo() / 8 == 4) {
 				int enPassantPawnIndex = move.getFrom() - 8;
 				builder.enPassantPawnIndex(enPassantPawnIndex, false);
 			}
 
 			boolean isPromotionMove = isPseudoPromotionMove(position, move);
 
 			// actually move the selected piece
 			builder.piece(move.getTo(), fromPiece);
 			builder.piece(move.getFrom(), Piece.EMPTY);
 
 			// if necessary, perform a promotion
 			if (isPromotionMove) {
 				if (move.getPromotionPiece() == null) {
 					throw new RuntimeException("Made a promotion move, but no promotion piece selected");
 				}
 				Piece promoPiece = Piece.getFromColorAndPiece(builder.isWhiteToMove(), move.getPromotionPiece());
 				builder.piece(move.getTo(), promoPiece);
 			}
 			builder.switchPlayerToMove();
 			if (builder.isWhiteToMove()) {
 				builder.fullmoveNumber(builder.getFullmoveNumber() + 1);
 			}
 			return builder.build();
 		}
 	}
 
 	/**
 	 * TODO fix redundancy to MoveChecker.isPseudoLegalCastleMove
 	 */
 	private static boolean isPseudoLegalCastleMove(Position position, Move move) {
 		if (position.isWhiteToMove() && move.getFrom() == 4 && move.getTo() == 6) {
 			return (position.canWhiteCastleKingside() && position.getPiece(4) == Piece.WHITE_KING
 					&& position.getPiece(5) == Piece.EMPTY && position.getPiece(6) == Piece.EMPTY && position.getPiece(7) == Piece.WHITE_ROOK);
 		}
 		if (position.isWhiteToMove() && move.getFrom() == 4 && move.getTo() == 2) {
 			return (position.canWhiteCastleQueenside() && position.getPiece(0) == Piece.WHITE_ROOK
 					&& position.getPiece(1) == Piece.EMPTY && position.getPiece(2) == Piece.EMPTY
 					&& position.getPiece(3) == Piece.EMPTY && position.getPiece(4) == Piece.WHITE_KING);
 		}
 
 		if (!position.isWhiteToMove() && move.getFrom() == 60 && move.getTo() == 62) {
 			return (position.canBlackCastleKingside() && position.getPiece(60) == Piece.BLACK_KING
 					&& position.getPiece(61) == Piece.EMPTY && position.getPiece(62) == Piece.EMPTY && position.getPiece(63) == Piece.BLACK_ROOK);
 		}
 		if (!position.isWhiteToMove() && move.getFrom() == 60 && move.getTo() == 58) {
 			return (position.canBlackCastleQueenside() && position.getPiece(56) == Piece.BLACK_ROOK
 					&& position.getPiece(57) == Piece.EMPTY && position.getPiece(58) == Piece.EMPTY
 					&& position.getPiece(59) == Piece.EMPTY && position.getPiece(60) == Piece.BLACK_KING);
 		}
 		return false;
 	}
 
 	public static boolean isPseudoPromotionMove(Position position, Move move) {
 		if (position.getPiece(move.getFrom()) == Piece.WHITE_PAWN && move.getFrom() / 8 == 6) {
 			return true;
 		}
 		if (position.getPiece(move.getFrom()) == Piece.BLACK_PAWN && move.getFrom() / 8 == 1) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * The specified move must already be checked for pseudo legality, this function does not check.
 	 */
 	private static Position.Builder makeCastleMove(Position.Builder builder, Move move) {
 		// FIXME check that the piece to move is a king!
 		if (move.getFrom() == 4 && move.getTo() == 6) {
 			// white O-O
 			builder.piece(4, Piece.EMPTY);
 			builder.piece(5, Piece.WHITE_ROOK);
 			builder.piece(6, Piece.WHITE_KING);
 			builder.piece(7, Piece.EMPTY);
 			builder.setPhantomKings(4, 5);
 		} else if (move.getFrom() == 4 && move.getTo() == 2) {
 			// white O-O-O
 			builder.piece(0, Piece.EMPTY);
 			builder.piece(1, Piece.EMPTY);
 			builder.piece(2, Piece.WHITE_KING);
 			builder.piece(3, Piece.WHITE_ROOK);
 			builder.piece(4, Piece.EMPTY);
 			builder.setPhantomKings(3, 4);
 		} else if (move.getFrom() == 60 && move.getTo() == 62) {
 			// black O-O
 			builder.piece(60, Piece.EMPTY);
 			builder.piece(61, Piece.BLACK_ROOK);
 			builder.piece(62, Piece.BLACK_KING);
 			builder.piece(63, Piece.EMPTY);
 			builder.setPhantomKings(60, 61);
 		} else if (move.getFrom() == 60 && move.getTo() == 58) {
 			// black O-O-O
 			builder.piece(56, Piece.EMPTY);
 			builder.piece(57, Piece.EMPTY);
 			builder.piece(58, Piece.BLACK_KING);
 			builder.piece(59, Piece.BLACK_ROOK);
 			builder.piece(60, Piece.EMPTY);
 			builder.setPhantomKings(59, 60);
 		} else {
 			throw new AssertionError();
 		}
 		return builder;
 	}
 
 	public static String toDiagramString(Position pos) {
 		StringBuilder sb = new StringBuilder();
 		for (int y = 7; y >= 0; --y) {
 			if (y == 7) {
 				sb.append("\n");
 			} else {
 				sb.append("|\n");
 			}
 			sb.append("--------------------------------\n");
 			for (int x = 0; x < 8; ++x) {
 				int index = y * 8 + x;
 				sb.append("| ");
 				sb.append(pos.getPiece(index).getSymbol());
 				sb.append(" ");
 			}
 		}
 		sb.append("|\n--------------------------------\n");
 		return sb.toString();
 	}
 }
