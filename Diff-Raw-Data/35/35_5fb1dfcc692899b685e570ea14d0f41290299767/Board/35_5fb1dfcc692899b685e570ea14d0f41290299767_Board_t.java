 package chessBackend;
 
 import java.util.Vector;
 
 import chessGUI.BoardGUI;
 import chessPieces.Bishop;
 import chessPieces.CastleDetails;
 import chessPieces.Piece;
 import chessPieces.PiecePosition;
 import chessPieces.PositionBonus;
 import chessPieces.Values;
 import chessPieces.PositionStatus;
 import chessPieces.King;
 import chessPieces.Knight;
 import chessPieces.Pawn;
 import chessPieces.Queen;
 import chessPieces.Rook;
 
 public class Board {
 	private Piece[][] board;
 	private boolean inCheck;
 	private boolean noCheckNear;
 	private boolean noCheckFar;
 	private Vector<Piece> aiPieces;
 	private Vector<Piece> userPieces;
 	private Piece lastMovedPiece;
 	private int knightValue;
 
 	public Board(Piece[][] board, Vector<Piece> aiPieces, Vector<Piece> playerPieces) {
 		this.board = board;
 		this.aiPieces = aiPieces;
 		this.userPieces = playerPieces;
 	}
 
 	public Board() {
 		buildBoard();
 		adjustKnightValue();
 	}
 
 	public void buildBoard() {
 		int rowOne;
 		int rowTwo;
 		Player player;
 
 		// Build AI side of the board
 		player = Player.AI;
 		rowOne = 0;
 		rowTwo = 1;
 
 		aiPieces = new Vector<Piece>();
 		aiPieces.add(new Rook(player, rowOne, 0,false));
 		aiPieces.add(new Knight(player, rowOne, 1));
 		aiPieces.add(new Bishop(player, rowOne, 2));
		aiPieces.add(new Queen(player, rowOne, 6));
 		aiPieces.add(new King(player, rowOne, 4));
 		aiPieces.add(new Bishop(player, rowOne, 5));
 		aiPieces.add(new Knight(player, rowOne, 6));
 		aiPieces.add(new Rook(player, rowOne, 7,true));
 
 		for (int pawn = 0; pawn < 8; pawn++) {
 			aiPieces.add(new Pawn(player, rowTwo, pawn));
 		}
 
 		// Build User side of the board
 		player = Player.USER;
 		rowOne = 7;
 		rowTwo = 6;
 
 		userPieces = new Vector<Piece>();
 		userPieces.add(new Rook(player, rowOne, 0,false));
 		userPieces.add(new Knight(player, rowOne, 1));
 		userPieces.add(new Bishop(player, rowOne, 2));
 		userPieces.add(new Queen(player, rowOne, 3));
 		userPieces.add(new King(player, rowOne, 4));
 		userPieces.add(new Bishop(player, rowOne, 5));
 		userPieces.add(new Knight(player, rowOne, 6));
 		userPieces.add(new Rook(player, rowOne, 7,true));
 
 		for (int pawn = 0; pawn < 8; pawn++) {
 			userPieces.add(new Pawn(player, rowTwo, pawn));
 		}
 
 		Piece aiPiece;
 		Piece userPiece;
 		board = new Piece[8][8];
 
 		for (int i = 0; i < userPieces.size(); i++) {
 			userPiece = userPieces.elementAt(i);
 			board[userPiece.getRow()][userPiece.getCol()] = userPiece;
 			userPiece.setBoard(this);
 		}
 		
 		for (int i = 0; i < aiPieces.size(); i++) {
 			aiPiece = aiPieces.elementAt(i);
 			board[aiPiece.getRow()][aiPiece.getCol()] = aiPiece;
 			aiPiece.setBoard(this);
 		}
 
 	}
 
 	public void moveChessPiece(Move move, Player player) {
 
 		if (hasPiece(move.getToRow(), move.getToCol())) {
 			if (getPiece(move.getToRow(), move.getToCol()).getPlayer() == Player.USER) {
 				userPieces.remove(getPiece(move.getToRow(), move.getToCol()));
 			} else {
 				aiPieces.remove(getPiece(move.getToRow(), move.getToCol()));
 			}
 			
 			adjustKnightValue();
 		}
 
 		board[move.getFromRow()][move.getFromCol()].move(move);
 		board[move.getToRow()][move.getToCol()] = board[move.getFromRow()][move.getFromCol()];
 		board[move.getFromRow()][move.getFromCol()] = null;
 
 		if (move.getNote() == MoveNote.NEW_QUEEN) {
 			board[move.getToRow()][move.getToCol()] = new Queen(player, move.getToRow(), move.getToCol());
 		}
 
 		if (move.getNote() == MoveNote.CASTLE_NEAR) {
 			if (player == Player.AI) {
 				moveChessPiece(new Move(0, 7, 0, 5), player);
 			} else {
 				moveChessPiece(new Move(7, 7, 7, 5), player);
 			}
 		}
 
 		if (move.getNote() == MoveNote.CASTLE_FAR) {
 			if (player == Player.AI) {
 				moveChessPiece(new Move(0, 0, 0, 3), player);
 			} else {
 				moveChessPiece(new Move(7, 0, 7, 3), player);
 			}
 		}
 
 		lastMovedPiece = board[move.getToRow()][move.getToCol()];
 		
 		board[move.getToRow()][move.getToCol()].updateValue();
 		
 	}
 
 	public PositionStatus checkPiece(int row, int col, Player player) {
 
 		if (row > 7 || row < 0 || col > 7 || col < 0)
 			return PositionStatus.OFF_BOARD;
 
 		if (board[row][col] != null) {
 			if (board[row][col].getPlayer() == player)
 				return PositionStatus.FRIEND;
 			else
 				return PositionStatus.ENEMY;
 		} else {
 			return PositionStatus.NO_PIECE;
 		}
 
 	}
 
 	public int getPieceValue(int row, int col) {
 		return board[row][col].getPieceValue();
 	}
 
 	public Piece getPiece(int row, int col) {
 		return board[row][col];
 	}
 
 	public boolean hasPiece(int row, int col) {
 		return (board[row][col] != null);
 	}
 
 	public boolean hasMoved(int row, int col) {
 		return board[row][col].hasMoved();
 	}
 
 	public Piece getLastMovedPiece() {
 		return lastMovedPiece;
 	}
 
 	public Vector<Piece> getPlayerPieces(Player player) {
 		if (player == Player.USER) {
 			return userPieces;
 		} else {
 			return aiPieces;
 		}
 	}
 
 	public boolean isInCheck() {
 		return inCheck;
 	}
 	
 	public void setInCheck(boolean inCheck){
 		this.inCheck = inCheck;
 	}
 
 	public void setCastleDetails(CastleDetails castleDetails){
		noCheckNear = false;
		noCheckFar = false;
		
 		if(castleDetails == CastleDetails.CASTLE_BOTH){
 			noCheckNear = true;
 			noCheckFar = true;
 		}else{
 			if(castleDetails == CastleDetails.CASTLE_FAR){
 				noCheckFar = true;
 			}
 			
 			if(castleDetails == CastleDetails.CASTLE_NEAR){
 				noCheckNear = true;
 			}
 		}
 	}
 
 	public boolean canCastleFar(Player player) {
 
 		if (player == Player.AI) {
 
 			if (hasPiece(0, 4)) {
 				if (!getPiece(0, 4).hasMoved()) {
 					if (!hasPiece(0, 3) && !hasPiece(0, 2) && !hasPiece(0, 1)) {
 						if (hasPiece(0, 0)) {
 							if (!hasMoved(0, 0) && noCheckFar) {
 								return true;
 							}
 						}
 					}
 
 				}
 			}
 
 		} else {
 			if (hasPiece(7, 4)) {
 				if (!getPiece(7, 4).hasMoved()) {
 					if (!hasPiece(7, 3) && !hasPiece(7, 2) && !hasPiece(7, 1)) {
 						if (hasPiece(7, 0)) {
 							if (!hasMoved(7, 0) && noCheckFar) {
 								return true;
 							}
 						}
 					}
 
 				}
 			}
 		}
 		
 		return false;
 
 	}
 
 	public boolean canCastleNear(Player player) {
 		
 		if (player == Player.AI) {
 
 			if (hasPiece(0, 4)) {
 				if (!getPiece(0, 4).hasMoved()) {
 					if (!hasPiece(0, 5) && !hasPiece(0, 6)) {
 						if (hasPiece(0, 7)) {
 							if (!hasMoved(0, 7) && noCheckNear) {
 								return true;
 							}
 						}
 					}
 				}
 			}
 
 		} else {
 			if (hasPiece(7, 4)) {
 				if (!getPiece(7, 4).hasMoved()) {
 					if (!hasPiece(7, 5) && !hasPiece(7, 6)) {
 						if (hasPiece(7, 7)) {
 							if (!hasMoved(7, 7) && noCheckNear) {
 								return true;
 							}
 						}
 					}
 				}
 			}
 		}
 
 		return false;
 	}
 	
 	public void adjustKnightValue(){
 		int piecesMissing = 32-(aiPieces.size()+userPieces.size());
 		knightValue = Values.KNIGHT_VALUE - piecesMissing*Values.KNIGHT_ENDGAME_INC;
 	}
 	
 	public int getKnightValue(){
 		return knightValue;
 	}
 
 	public Board getCopy() {
 		Piece[][] copyBoard = new Piece[8][8];
		Vector<Piece> copyAiPieces = new Vector<Piece>(16);
		Vector<Piece> copyPlayerPieces = new Vector<Piece>(16);
 		Board newBoard = new Board(copyBoard, copyAiPieces, copyPlayerPieces);
 
 		for (int row = 0; row < 8; row++) {
 			for (int col = 0; col < 8; col++) {
 
 				if (board[row][col] != null) {
 
 					copyBoard[row][col] = board[row][col].getCopy();
 					copyBoard[row][col].setBoard(newBoard);
 
 					if (copyBoard[row][col].getPlayer() == Player.AI) {
 						copyAiPieces.add(copyBoard[row][col]);
 					} else {
 						copyPlayerPieces.add(copyBoard[row][col]);
 					}
 				}
 			}
 		}
 
 		return newBoard;
 	}
 
 }
