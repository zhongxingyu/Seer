 package edu.neumont.learningChess.model;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Stack;
 
 import edu.neumont.learningChess.api.ChessGameState;
 import edu.neumont.learningChess.api.Location;
 import edu.neumont.learningChess.api.PieceType;
 
 
 public class ChessBoard {
 
 	public static final int NUMBER_OF_ROWS = 8;
 	public static final int NUMBER_OF_COLUMNS = 8;
 	
 	private static final int WHITE_PROMOTION_ROW = 7;
 	private static final int BLACK_PROMOTION_ROW = 0;
 	
 	
 	
 	
 	
 	
 	public static boolean isInBounds(Location location) {
 		return (location.getRow() >= 0) && (location.getRow() < NUMBER_OF_ROWS) && (location.getColumn() >= 0) && (location.getColumn() < NUMBER_OF_COLUMNS);
 	}
 	
 	private BoardSquare grid[][] = new BoardSquare[NUMBER_OF_ROWS][NUMBER_OF_COLUMNS];
 	private Stack<MoveDescription> tryingMoves = new Stack<MoveDescription>();
 	private ArrayList<IListener> listeners = new ArrayList<IListener>();
 	
 	public ChessBoard() {
 		for (int i = 0; i < NUMBER_OF_ROWS; i++) {
 			for (int j = 0; j < NUMBER_OF_COLUMNS; j++) {
 				grid[i][j] = new BoardSquare();
 			}
 		}
 	}
 	
 	public void AddListener(IListener listener) {
 		listeners.add(listener);
 	}
 	
 	public void RemoveListener(IListener listener) {
 		listeners.remove(listener);
 	}
 	
 	private void notifyListenersOfMove(Move move, boolean capturePiece) {
 		for (Iterator<IListener> i = listeners.iterator(); i.hasNext(); ) {
 			IListener listener = i.next();
 			listener.movePiece(move, capturePiece);
 		}
 	}
 	
 	private void notifyListenersOfPlacement(ChessPiece piece, Location location) {
 		for (Iterator<IListener> i = listeners.iterator(); i.hasNext(); ) {
 			IListener listener = i.next();
 			listener.placePiece(piece, location);
 		}
 	}
 	
 	private void notifyListenersOfRemoval(Location location) {
 		for (Iterator<IListener> i = listeners.iterator(); i.hasNext(); ) {
 			IListener listener = i.next();
 			listener.removePiece(location);
 		}
 	}
 	
 	private void putPiece(ChessPiece piece, Location location) {
 		if (!grid[location.getRow()][location.getColumn()].isEmpty()) {
 			throw new RuntimeException("Cannot place piece at a non-empty space");
 		}
 		grid[location.getRow()][location.getColumn()].putPiece(piece);
 		piece.setLocation(location);
 	}
 	
 	public void placePiece(ChessPiece piece, Location location) {
 		putPiece(piece, location);
 		notifyListenersOfPlacement(piece, location);
 	}
 	
 	public ChessPiece getPiece(Location location) {
 		return grid[location.getRow()][location.getColumn()].getPiece();
 	}
 	
 	public boolean hasPiece(Location location) {
 		return getPiece(location) != null;
 	}
 	
 	public boolean hasPiece(Location location, boolean isWhite) {
 		ChessPiece piece = getPiece(location);
 		return (piece != null) && (piece.getTeam().isWhite() == isWhite);
 	}
 	
 	private ChessPiece removePiece(Location location) {
 		
 		ChessPiece piece = grid[location.getRow()][location.getColumn()].removePiece();
 		piece.setLocation(null);
 		return piece;
 	}
 	
 	private MoveDescription makeBasicMove(Move move) {
 		ChessPiece capturedPiece = null;
 		Team capturedTeam = null;
 		if (hasPiece(move.getTo())) {
 			capturedPiece = removePiece(move.getTo());
 			capturedTeam = capturedPiece.getTeam();
 			capturedTeam.remove(capturedPiece);
 		}
 		ChessPiece movingPiece = removePiece(move.getFrom());
 		putPiece(movingPiece, move.getTo());
 		movingPiece.incrementMoveCount();
 		MoveDescription moveDescription = new MoveDescription(move, movingPiece, capturedTeam, capturedPiece);
 		return moveDescription;
 	}
 
 	public void makeMove(Move move) {
 		if (isCastlingMove(move)) {
 			Move castlingMove = getCastlingRookMove(move);
 			makeBasicMove(castlingMove);
 			notifyListenersOfMove(castlingMove, false);
 		}
 		
 		MoveDescription description = null;
 		if (isEnPassantMove(move)) {
 			description = makeBasicMove(move);
 			tryingMoves.push(description);
 			notifyListenersOfMove(move, false);
 
 			Location target = getEnPassantTarget(move);
 			ChessPiece capturedPiece = removePiece(target);
 			Team capturedTeam = capturedPiece.getTeam();
 			description.setTakenPiece(capturedPiece);
 			capturedTeam.remove(capturedPiece);
 			notifyListenersOfRemoval(target);
 		} else {
 			description = makeBasicMove(move);
 			tryingMoves.push(description);
 			notifyListenersOfMove(move, description.getTakenPiece() != null);
 		}
 		if (requiresPromotion(move)) {
 			Pawn pawn = (Pawn) removePiece(move.getTo());
 			Team pawnsTeam = pawn.getTeam();
 			pawnsTeam.remove(pawn);
 			notifyListenersOfRemoval(move.getTo());
 			ChessPiece replacement = pawn.getPromotionPiece(move.getTo());
 			pawnsTeam.add(replacement);
 			placePiece(replacement, move.getTo());
 		}
 	}
 	
 	private boolean requiresPromotion(Move move) {
 		ChessPiece movingPiece = getPiece(move.getTo());
 		return (movingPiece instanceof Pawn) && 
 			((movingPiece.getTeam().isWhite() && (move.getTo().getRow() == WHITE_PROMOTION_ROW)) || 
 			(!movingPiece.getTeam().isWhite() && (move.getTo().getRow() == BLACK_PROMOTION_ROW)));
 	}
 	
 	public MoveDescription getMostRecentMoveDescription() {
 		return tryingMoves.size() > 0? tryingMoves.peek(): null;
 	}
 	
 	private boolean isEnPassantMove(Move move) {
 		boolean isEnPassant = false;
 		ChessPiece piece = getPiece(move.getFrom());
 		if (piece instanceof Pawn) {
 			MoveDescription lastMove = getMostRecentMoveDescription();
 			if (lastMove != null) {
 				int lastMoveDistance = Math.abs(lastMove.getMove().getFrom().getRow() - lastMove.getMove().getTo().getRow());
 				isEnPassant = ((lastMove.getMovingPiece() instanceof Pawn) && (lastMoveDistance == 2) &&
 						(Math.abs(move.getFrom().getColumn() - move.getTo().getColumn()) == 1) &&
 						(Math.abs(move.getFrom().getRow() - move.getTo().getRow()) == 1) &&
 						(getEnPassantTarget(move).equals(lastMove.getMove().getTo())));
 			}
 		}
 		return isEnPassant;
 	}
 	
 	private Location getEnPassantTarget(Move move) {
 		return new Location(move.getFrom().getRow(), move.getTo().getColumn());
 	}
 	
 	private boolean isCastlingMove(Move move) {
 		boolean castlingMove = false;
 		ChessPiece piece = getPiece(move.getFrom());
 		if (piece instanceof King) {
 			King king = (King) piece;
 			castlingMove = king.attemptingCastling(move);
 		}
 		return castlingMove;
 	}
 	
 	private Move getCastlingRookMove(Move move) {
 		ChessPiece piece = getPiece(move.getFrom());
 		Location rookDestination = new Location(move.getFrom().getRow(), (move.getFrom().getColumn() + move.getTo().getColumn())/2);
 		King king = (King) piece;
 		Location rookOrigin = king.getCastlingRookLocation(move);
 		Move rookMove = new Move(rookOrigin, rookDestination);
 		return rookMove;
 	}
 
 	public void tryMove(Move move) {
 		if (isCastlingMove(move)) {
 			Move castlingMove = getCastlingRookMove(move);
 			tryingMoves.push(makeBasicMove(castlingMove));
 		}
 		MoveDescription description = null;
 		if (isEnPassantMove(move)) {
 			Location target = getEnPassantTarget(move);
 			description = makeBasicMove(move);
 			ChessPiece capturedPiece = removePiece(target);
 			Team capturedTeam = capturedPiece.getTeam();
 			description.setTakenPiece(capturedPiece);
 			capturedTeam.remove(capturedPiece);
 			tryingMoves.push(description);
 		} else {
 			description = makeBasicMove(move);
 			tryingMoves.push(description);
 		}
 		if (requiresPromotion(move)) {
 			Pawn pawn = (Pawn) removePiece(move.getTo());
 			Team pawnsTeam = pawn.getTeam();
 			pawnsTeam.remove(pawn);
 			ChessPiece replacement = pawn.getPromotionPiece(move.getTo());
 			pawnsTeam.add(replacement);
 			putPiece(replacement, move.getTo());
 			description.setPromotionPiece(replacement);
 		}
 	}
 
 	public void undoTriedMove() {
 		MoveDescription moveDescription = tryingMoves.pop();
 		Move move = moveDescription.getMove();
 		if (moveDescription.getPromotionPiece() != null) {
 			ChessPiece replacement = removePiece(move.getTo());
 			Team pawnsTeam = replacement.getTeam();
 			pawnsTeam.remove(replacement);
 			ChessPiece pawn = moveDescription.getMovingPiece();
 			pawnsTeam.add(pawn);
 			putPiece(pawn, move.getTo());
 		}
 		ChessPiece movingPiece = removePiece(move.getTo());
 		putPiece(movingPiece, move.getFrom());
 		movingPiece.decrementMoveCount();
 		Location enPassantTarget = isEnPassantMove(move)? getEnPassantTarget(move): null;
 		// Undo the capture
 		if (moveDescription.getTakenPiece() != null) {
 			putPiece(moveDescription.getTakenPiece(), move.getTo());
 			moveDescription.getTakenTeam().add(moveDescription.getTakenPiece());
 		}
 		if (enPassantTarget != null) {
 			// This piece was replaced as a capture piece, so move it to the enpassant target
 			putPiece(removePiece(move.getTo()), enPassantTarget);
 		}
 		// Undo a castle move
 		if (isCastlingMove(move)) {
 			MoveDescription castlingDescription = tryingMoves.pop();
 			Move rookMove = castlingDescription.getMove();
 			ChessPiece rook = removePiece(rookMove.getTo());
 			putPiece(rook, rookMove.getFrom());
 			rook.decrementMoveCount();
 		}
 	}
 	
 	public static boolean isDarkSquare(Location location) {
 		return (location.getRow() + location.getColumn()) % 2 == 0;
 	}
 
	public boolean hasFiftyMovesWithCapturesOrPawnMoves() {
 		boolean captureOrMove = false;
 		int count = 0;
 		
 		for (Iterator<MoveDescription> moves = tryingMoves.iterator(); moves.hasNext() && count < 50 && !captureOrMove;count++) {
 			MoveDescription move = moves.next();
 			captureOrMove = (move.getTakenPiece() != null) || (move.getMovingPiece().getClass() == Pawn.class);
 		}
 		return captureOrMove;
 	}
 	
 	public Iterator<MoveDescription> getTryingMovesIterator() {
 		return tryingMoves.iterator();
 	}
 	
 }
