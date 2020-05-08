 package edu.neumont.learningChess.controller;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Vector;
 
 import edu.neumont.learningChess.api.ChessGameState;
 import edu.neumont.learningChess.api.ExtendedMove;
 import edu.neumont.learningChess.api.Location;
 import edu.neumont.learningChess.api.PieceDescription;
 import edu.neumont.learningChess.api.PieceType;
 import edu.neumont.learningChess.api.TeamColor;
 import edu.neumont.learningChess.model.AIPlayer;
 import edu.neumont.learningChess.model.Bishop;
 import edu.neumont.learningChess.model.ChessBoard;
 import edu.neumont.learningChess.model.ChessPiece;
 import edu.neumont.learningChess.model.HumanPlayer;
 import edu.neumont.learningChess.model.ICheckChecker;
 import edu.neumont.learningChess.model.IListener;
 import edu.neumont.learningChess.model.King;
 import edu.neumont.learningChess.model.Knight;
 import edu.neumont.learningChess.model.LocationIterator;
 import edu.neumont.learningChess.model.Move;
 import edu.neumont.learningChess.model.MoveDescription;
 import edu.neumont.learningChess.model.Pawn;
 import edu.neumont.learningChess.model.Pawn.IPromotionListener;
 import edu.neumont.learningChess.model.Player;
 import edu.neumont.learningChess.model.Queen;
 import edu.neumont.learningChess.model.RemotePlayer;
 import edu.neumont.learningChess.model.Rook;
 import edu.neumont.learningChess.model.Team;
 import edu.neumont.learningChess.view.BoardDisplay;
 import edu.neumont.learningChess.view.BoardDisplayPiece;
 import edu.neumont.learningChess.view.IDisplay;
 import edu.neumont.learningChess.view.NullDisplay;
 
 
 
 public class GameController implements IListener, ICheckChecker {
 
 	public enum PlayerType {
 		Human, AI, Remote
 	}
 
 	private IDisplay boardDisplay;
 
 	private Team whiteTeam;
 	private Team blackTeam;
 
 	private ChessBoard board;
 
 	private Player whitePlayer;
 	private Player blackPlayer;
 	private Player currentPlayer;
 	
 	private boolean showDisplay;
 	
 	private ArrayList<ChessGameState> history = new ArrayList<ChessGameState>(); 
 
 	public GameController(PlayerType whiteType, PlayerType blackType) {
 
 		showDisplay = (whiteType == PlayerType.Human)
 				|| (blackType == PlayerType.Human);
 		
 		board = new ChessBoard();
 		board.AddListener(this);
 		if (showDisplay) {
 			boardDisplay = new BoardDisplay();
 		} else {
 			boardDisplay = new NullDisplay();
 		}
 		// boardDisplay = (showDisplay)? new BoardDisplay(): new NullDisplay();
 
 		whiteTeam = buildTeam(Team.Color.LIGHT);
 		blackTeam = buildTeam(Team.Color.DARK);
 
 		// Determine the player types here
 		whitePlayer = createPlayer(whiteType, whiteTeam);
 		blackPlayer = createPlayer(blackType, blackTeam);
 
 		buildTeamPawns(whiteTeam, whitePlayer.getPromotionListener());
 		buildTeamPawns(blackTeam, blackPlayer.getPromotionListener());
 		
 		history.add(getCurrentGameState());
 		
 		boardDisplay.setVisible(true);
 	}
 	
 	private PieceType getPieceTypeFromChessPiece(ChessPiece chessPiece){
 		PieceType pieceType = null;
 		
 		if(chessPiece instanceof King){
 			pieceType = PieceType.KING;
 		}
 		else if(chessPiece instanceof Queen){
 			pieceType = PieceType.QUEEN;
 		}
 		else if(chessPiece instanceof Bishop){
 			pieceType = PieceType.BISHOP;
 		}
 		else if(chessPiece instanceof Knight){
 			pieceType = PieceType.KNIGHT;
 		}
 		else if(chessPiece instanceof Rook){
 			pieceType = PieceType.ROOK;
 		}
 		else if(chessPiece instanceof Pawn){
 			pieceType = PieceType.PAWN;
 		}
 		
 		return pieceType;
 	}
 	
 	private ChessPiece getChessPieceFromPieceType(PieceType pieceType, IPromotionListener promotionListener){
 		ChessPiece chessPiece = null;
 		
 		switch(pieceType){
 		case KING:
 			chessPiece = new King(this);
 			break;
 		case QUEEN:
 			chessPiece = new Queen();
 			break;
 		case BISHOP:
 			chessPiece = new Bishop();
 			break;
 		case KNIGHT:
 			chessPiece = new Knight();
 			break;
 		case ROOK:
 			chessPiece = new Rook();
 			break;
 		case PAWN:
 			chessPiece = new Pawn(promotionListener);
 			break;
 		}
 		
 		return chessPiece;
 	}
 	
 	public ChessGameState getCurrentGameState(){
 		ChessGameState chessGameState = new ChessGameState();
 		LocationIterator locations = new LocationIterator();
 		
 		while(locations.hasNext()){
 			Location location = locations.next();
 			ChessPiece chessPiece = getPiece(location);
 			
			TeamColor teamColor = chessPiece.getTeam().isWhite() ? TeamColor.LIGHT : TeamColor.DARK;
			PieceType pieceType = getPieceTypeFromChessPiece(chessPiece);
			PieceDescription pieceDescription = new PieceDescription(teamColor, chessPiece.hasMoved(), pieceType);
			
			chessGameState.setPieceDescription(location, pieceDescription);
			
			MoveDescription moveDescription = getMostRecentMoveDescription();
 			ChessPiece movingPiece = moveDescription.getMovingPiece();
 			if(movingPiece != null && movingPiece instanceof Pawn){
 				Move move = moveDescription.getMove();
 				Location fromLocation = move.getFrom();
 				Location toLocation = move.getTo();
 				
 				int pawnMoveDistance = Math.abs(fromLocation.getRow() - toLocation.getRow());
 				if(pawnMoveDistance == 2){
 					chessGameState.setPawnMovedTwoLocation(toLocation);
 				}
 			}
			
 		}
 		
 		return chessGameState;
 	}
 
 	private Player createPlayer(PlayerType playerType, Team team) {
 		Player player = null;
 		switch (playerType) {
 		case Human:
 			player = new HumanPlayer(team, board, this, boardDisplay);
 			boardDisplay.addMoveHandler((HumanPlayer) player);
 			break;
 		case Remote:
 			player = new RemotePlayer(team, board, this);
 			break;
 		case AI:
 			player = new AIPlayer(board, team, (team == whiteTeam) ? blackTeam
 					: whiteTeam);
 			break;
 		}
 		return player;
 	}
 
 	public void play() {
 		currentPlayer = whitePlayer;
 		boolean isCheckmate = false;
 		boolean isStalemate = false;
 		while (!(isCheckmate || isStalemate)) {
 			boardDisplay.promptForMove((currentPlayer == whitePlayer));
 			Move move = currentPlayer.getMove();
 			board.makeMove(move);
 			togglePlayers();
 			history.add(getCurrentGameState());
 			isCheckmate = isCheckmate();
 			isStalemate = isStalemate();
 			if (!isCheckmate && isInCheck(currentPlayer.getTeam())) {
 				boardDisplay.notifyCheck(currentPlayer == whitePlayer);
 			}
 		}
 		if (isCheckmate) {
 			boardDisplay.notifyCheckmate(currentPlayer == blackPlayer);
 		} else if (isStalemate) {
 			boardDisplay.notifyStalemate();
 		}
 	}
 	
 	public void tryMove(MoveDescription moveDescription) {
 		board.tryMove(moveDescription.getMove());
 		togglePlayers();
 	}
 
 	private void togglePlayers() {
 		currentPlayer = (currentPlayer == whitePlayer) ? blackPlayer
 				: whitePlayer;
 	}
 	
 	public ChessPiece getPiece(Location location) {
 		return board.getPiece(location);
 	}
 
 	public void close() {
 		boardDisplay.dispose();
 	}
 
 	private Team buildTeam(Team.Color color) {
 
 		char mainRow = (color == Team.Color.LIGHT)? '1': '8';
 		Team team = new Team(color);
 
 		King king = new King(this);
 		setupPiece(king, new Location(mainRow, 'e'), team);
 		setupPiece(new Queen(), new Location(mainRow, 'd'), team);
 		setupPiece(new Bishop(), new Location(mainRow, 'c'), team);
 		setupPiece(new Bishop(), new Location(mainRow, 'f'), team);
 		setupPiece(new Knight(), new Location(mainRow, 'b'), team);
 		setupPiece(new Knight(), new Location(mainRow, 'g'), team);
 		setupPiece(new Rook(), new Location(mainRow, 'a'), team);
 		setupPiece(new Rook(), new Location(mainRow, 'h'), team);
 		return team;
 	}
 	
 	private void buildTeamPawns(Team team, Pawn.IPromotionListener promotionListener) {
 
 		char pawnRow = team.isWhite()? '2': '7';
 		for (int i = 0; i < BoardDisplay.N_COLS; i++) {
 			setupPiece(new Pawn(promotionListener), new Location(pawnRow, (char) ('a' + i)),
 					team);
 		}
 	}
 
 	private void setupPiece(ChessPiece piece, Location location, Team team) {
 		team.add(piece);
 		board.placePiece(piece, location);
 	}
 
 	private URL getImageURL(ChessPiece piece) {
 		Team team = piece.getTeam();
 		String imageLetter = team.isWhite() ? "w" : "b";
 		String imagePath = "/Images/" + piece.getName() + imageLetter + ".gif";
 		URL imageUrl = getClass().getResource(imagePath);
 		return imageUrl;
 	}
 
 	public boolean isInCheck(Team team) {
 		Location kingsLocation = team.getKing().getLocation();
 		Team attackingTeam = (team == whiteTeam) ? blackTeam : whiteTeam;
 		return attackingTeam.canAttack(board, kingsLocation);
 	}
 
 	public boolean canMove(Team team) {
 		boolean canMove = false;
 		// For each piece of the current team and cannot yet move applies...
 		for (Iterator<ChessPiece> i = team.getPieces(); !canMove && i.hasNext();) {
 			ChessPiece piece = i.next();
 			// For each valid move of that piece and checkmate applies...
 			for (Iterator<Location> e = piece.getLegalMoves(board); !canMove
 					&& e.hasNext();) {
 				Location to = e.next();
 				// Apply the move
 				board.tryMove(new Move(piece.getLocation(), to));
 				// checkmate applies If the current team is not in check
 				canMove = !isInCheck(team);
 				// Unapply the move
 				board.undoTriedMove();
 			}
 		}
 		// return true iff checkmate applies
 		return canMove;
 	}
 
 	public boolean isCheckmate() {
 		Team currentTeam = currentPlayer.getTeam();
 		return isInCheck(currentTeam) && !canMove(currentTeam);
 	}
 
 	public boolean isStalemate() {
 		Team currentTeam = currentPlayer.getTeam();
 		return (!isInCheck(currentTeam) && !canMove(currentTeam))
 				|| ((isThreeFoldRepetition() 
						|| hasFiftyMovesWithNoCapturesOrPawnMoves() 
 						|| isStalematePieceCombination()));
 	}
 
 	@SuppressWarnings("unchecked")
 	private boolean isStalematePieceCombination() {
 		return ((whiteTeam.onlyHasPieces(King.class) && blackTeam.onlyHasPieces(King.class))
 				|| (whiteTeam.onlyHasPieces(King.class) && blackTeam.onlyHasPieces(King.class, Knight.class))
 				|| (whiteTeam.onlyHasPieces(King.class, Knight.class) && blackTeam.onlyHasPieces(King.class))
 				|| (whiteTeam.onlyHasPieces(King.class) && blackTeam.onlyHasPieces(King.class, Bishop.class))
 				|| (whiteTeam.onlyHasPieces(King.class, Bishop.class) && blackTeam.onlyHasPieces(King.class)) || isStalemateBishopPieceCombination());
 	}
 	
 	@SuppressWarnings("unchecked")
 	private boolean isStalemateBishopPieceCombination() {
 		boolean isStalematePossible = whiteTeam.onlyHasPieces(King.class, Bishop.class) && blackTeam.onlyHasPieces(King.class, Bishop.class);
 		boolean firstBishopFound = false;
 		boolean secondBishopFound = false;
 		boolean isFirstBishopOnDark = false;
 		for (Iterator<Location> locations = new LocationIterator(); !secondBishopFound && isStalematePossible && locations.hasNext();) {
 			Location location = locations.next();
 			if (getPieceTypeAt(location, Bishop.class) != null) {
 				if (!firstBishopFound) {
 					isFirstBishopOnDark = ChessBoard.isDarkSquare(location);
 					firstBishopFound = true;
 				} else {
 					isStalematePossible = isFirstBishopOnDark == ChessBoard.isDarkSquare(location);
 				}
 			}
 		}
 		return isStalematePossible;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private <T extends ChessPiece> T getPieceTypeAt(Location location, Class<T> cls) {
 		T returnedPiece = null;
 		ChessPiece piece = board.getPiece(location);
 		if ((piece != null) && (piece.getClass().equals(cls))) {
 			returnedPiece = (T) piece;
 		}
 		return returnedPiece;
 	}
 
	private boolean hasFiftyMovesWithNoCapturesOrPawnMoves() {
		return board.hasFiftyMovesWithNoCapturesOrPawnMoves();
 		
 	}
 
 	private boolean isThreeFoldRepetition() {
 		int seen = 1;
 
 		ChessGameState currentState = getCurrentGameState();
 		for (Iterator<ChessGameState> states = history.iterator(); (seen < 3) && states.hasNext();) {
 			ChessGameState state = states.next();
 			if (currentState.equals(state))
 				seen++;
 		}
 
 		return seen >= 3;
 	}
 
 	@Override
 	public void movePiece(Move move, boolean capturePiece) {
 		if (capturePiece) {
 			IDisplay.Piece displayPiece = boardDisplay
 					.removePiece(move.getTo());
 			displayPiece.setPieceLocation(null);
 		}
 		IDisplay.Piece displayPiece = boardDisplay.removePiece(move.getFrom());
 		boardDisplay.placePiece(displayPiece, move.getTo());
 		displayPiece.setPieceLocation(move.getTo());
 	}
 
 	@Override
 	public void placePiece(ChessPiece piece, Location location) {
 		if (showDisplay) {
 			IDisplay.Piece displayPiece = new BoardDisplayPiece(
 					getImageURL(piece));
 			boardDisplay.placePiece(displayPiece, location);
 			displayPiece.setPieceLocation(location);
 		}
 	}
 	
 	@Override
 	public void removePiece(Location location) {
 		IDisplay.Piece displayPiece = boardDisplay.removePiece(location);
 		displayPiece.setPieceLocation(null);
 	}
 
 	public MoveDescription getMostRecentMoveDescription() {
 		return board.getMostRecentMoveDescription();
 	}
 
 	public void untryMove() {
 		togglePlayers();
 		board.undoTriedMove();
 	}
 
 	public Team getCurrentTeam() {
 		return currentPlayer.getTeam();
 	}
 
 	public Iterator<Move> getPossibleMovesForCurrentTeam() {
 		return currentPlayer.getTeam().getMoves(board);
 	}
 
 	public Iterator<Move> getPossibleMoves(Location location) {
 		Iterator<Location> legalMoves = getPiece(location).getLegalMoves(board);
 		Vector<Move> moves = new Vector<Move>();
 		for(;legalMoves.hasNext();) {
 			Location legalMoveDestination = legalMoves.next();
 			moves.add(new Move(location, legalMoveDestination ));
 		}
 		return moves.iterator();
 		
 	}
 
 	public Iterator<ExtendedMove> getGameHistory() {
 		Vector<ExtendedMove> extendedMoves = new Vector<ExtendedMove>();
 		for(Iterator<MoveDescription> tryingMovesIterator = board.getTryingMovesIterator();tryingMovesIterator.hasNext();) {
 			MoveDescription moveDescription = tryingMovesIterator.next();
 			extendedMoves.add(new ExtendedMove(moveDescription.getMove(),getPieceTypeFromChessPiece(moveDescription.getPromotionPiece())));
 		}
 		return extendedMoves.iterator();
 	}
 }
