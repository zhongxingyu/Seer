 package model.board;
 
 import java.util.ArrayList;
 
 import util.*;
 import model.pieces.*;
 
 /**
  * 
  * @author Paul Jones
  *
  */
 
 public class ChessBoard {
 	public static final int NUMBER_OF_RANKS = 8;
 	public static final int NUMBER_OF_FILES = 8;
 
 	private ChessBoardSquare[][] board;
 	public boolean isBlackTurn = true;
 	public int moveCount = 0;
 
 	/**
 	 * Initializes and empty board.
 	 */
 	public ChessBoard() {
 		board = new ChessBoardSquare[NUMBER_OF_RANKS][NUMBER_OF_FILES];
 		for (int i = 0; i < board.length; i++) {
 			for (int j = 0; j < board[i].length; j++) {
 				board[i][j] = new ChessBoardSquare(
 						new ChessCoordinatePair(i, j), null);
 			}
 		}
 
 		this.loadStandardBoard();
 	}
 
 	/**
 	 * Loads the standard chess board onto an already initialized board.
 	 */
 	private void loadStandardBoard() {
 		for (int i = 0; i < board.length; i++) {
 			for (int j = 0; j < board[i].length; j++) {
 				if (i == 0 || i == 7) {
 					if (j == 0 || j == 7) {
 						board[i][j].piece = new RookChessPiece(i == 0);
 					} else if (j == 1 || j == 6) {
 						board[i][j].piece = new KnightChessPiece(i == 0);
 					} else if (j == 2 || j == 5) {
 						board[i][j].piece = new BishopChessPiece(i == 0);
 					} else if (j == 3) {
 						board[i][j].piece = new QueenChessPiece(i == 0);
 					} else if (j == 4) {
 						board[i][j].piece = new KingChessPiece(i == 0);
 					}
 				} else if (i == 1 || i == 6) {
 					board[i][j].piece = new PawnChessPiece(i == 1);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Attempts to make the move dictated by the inputs.
 	 * 
 	 * @param startAddress
 	 *            a valid ChessCoordinatePair which may or may not have a piece
 	 *            on it.
 	 * @param endAddress
 	 *            a valid ChessCoordinatePair which may or may not have a piece
 	 *            on it.
 	 * @param ChessPieceConstant
 	 *            for promotion of pawns, null every other time.
 	 * @throws InvalidMoveException
 	 *             if there is no piece on the start square, or if it isn't the
 	 *             present player's piece, or if they're trying to take their
 	 *             own piece, or if it just generally an invalid movement.
 	 */
 	public void makeMove(ChessCoordinatePair startAddress,
 			ChessCoordinatePair endAddress, String ChessPieceConstant)
 			throws InvalidMoveException {
 		ChessBoardSquare startSquare = this.squareAt(startAddress);
 		ChessBoardSquare endSquare = this.squareAt(endAddress);
 		ChessPiece movingPiece = startSquare.piece;
 		ChessPiece takenPiece = endSquare.piece;
 
 		PieceSpecialCases special = new PieceSpecialCases();
 
 		special.isCapturing = takenPiece == null ? false : true;
 		special.isPromoting = ChessPieceConstant == null ? false : true;
 		special.pieceInPath = this.boardHasPieceInPathBetween(startAddress, endAddress);
 
 		if (startAddress.equals(endAddress)) {
 			throw new InvalidMoveException(
 					"You cannot move from and to the same square.");
 		} else if (movingPiece == null) {
 			throw new InvalidMoveException("There is no piece there.");
 		} else if (movingPiece.isBlack != this.getIsBlackTurn()) {
 			throw new InvalidMoveException("That is not your piece");
 		} else if (takenPiece != null
 				&& (takenPiece.isBlack == this.getIsBlackTurn())) {
 			throw new InvalidMoveException("You cannot take your own piece.");
 		} else if (movingPiece.isValidMove(startAddress, endAddress, special)) {
 			this.move(startAddress, endAddress);
 
 			if (special.isPromoting) {
 				board[endAddress.rank][endAddress.file].piece = this
 						.getChessPieceFrom(ChessPieceConstant);
 			}
 
 		} else {
 			throw new InvalidMoveException(
 					"That is not a valid move for this piece.");
 		}
 	}
 
 	/**
 	 * For promotion only! The boolean is "notted" because the move hasn't
 	 * happened yet.
 	 * 
 	 * @param CHESS_PIECE_CONSTANT
 	 *            a String that is a "R", "N", "B", or "Q" only.
 	 * @return the ChessPiece that character represents
 	 */
 	private ChessPiece getChessPieceFrom(String CHESS_PIECE_CONSTANT) {
 		if (CHESS_PIECE_CONSTANT.equals(ChessNamingConstants.ROOK)) {
 			return new RookChessPiece(!this.isBlackTurn);
 		} else if (CHESS_PIECE_CONSTANT.equals(ChessNamingConstants.KNIGHT)) {
 			return new KnightChessPiece(!this.isBlackTurn);
 		} else if (CHESS_PIECE_CONSTANT.equals(ChessNamingConstants.BISHOP)) {
 			return new BishopChessPiece(!this.isBlackTurn);
 		} else {
 			return new QueenChessPiece(!this.isBlackTurn);
 		}
 	}
 
 	/**
 	 * For valid moves only! This method will do exactly what its told. It will
 	 * also increment the movement count and update the player whose turn it is,
 	 * so be careful.
 	 */
 	private void move(ChessCoordinatePair startAddress,
 			ChessCoordinatePair endAddress) {
 		this.board[endAddress.rank][endAddress.file].piece = this.board[startAddress.rank][startAddress.file].piece;
 		this.board[startAddress.rank][startAddress.file].piece = null;
 		this.moveCount++;
 		this.isBlackTurn = (moveCount % 2 == 0);
 	}
 
 	/**
 	 * This checks the horizontal, vertical, and diagonal paths between the two
 	 * inputed points and returns true if there is a piece in between those two
 	 * points.
 	 * 
 	 * @param startAddress
 	 *            The address that the possible path would start
 	 * @param endAddress
 	 *            The address that the possible path would end
 	 * @return A boolean representing whether there is something between the two
 	 *         points
 	 */
 	private boolean boardHasPieceInPathBetween(
 			ChessCoordinatePair startAddress, ChessCoordinatePair endAddress) {
 
 		if (ChessNamingConstants.DEVELOPMENT) {
 			System.out.println("boardHasPieceInPathBetween(" + startAddress
 					+ ", " + endAddress + ");");
 			System.out.println("\tstartAddress.rank = " + startAddress.rank);
 			System.out.println("\tstartAddress.file = " + startAddress.file);
 			System.out.println("\tendAddress.rank = " + endAddress.rank);
 			System.out.println("\tendAddress.file = " + endAddress.file);
 		}
 
 		if (startAddress.hasSameFileAs(endAddress)) {
 			
 			if (startAddress.rank < endAddress.rank) {
 			for (int i = startAddress.rank + 1; i < endAddress.rank; i++) {
 				if (board[i][startAddress.file].isOccupied()) {
 					return true;
 				}
 			}
 			} else {
 				for (int i = startAddress.rank - 1; i > endAddress.rank; i--) {
 					if (board[i][startAddress.file].isOccupied()) {
 						return true;
 					}
 				}
 			}
 			
 		} else if (startAddress.hasSameRankAs(endAddress)) {
 			if (startAddress.file < endAddress.file) {
 			for (int i = startAddress.file + 1; i < endAddress.file; i++) {
 				if (board[startAddress.rank][i].isOccupied()) {
 					return true;
 				}
 			} 
 			} else {
 				for (int i = startAddress.file - 1; i > endAddress.file; i--) {
 					if (board[startAddress.rank][i].isOccupied()) {
 						return true;
 					}
 				} 
 			}
 		} else if (startAddress.isDiagonalTo(endAddress)) {
 
 			if (ChessNamingConstants.DEVELOPMENT) {
 				System.out.printf("\n\t\t\t%s isDiagonalTo %s\n",
 						startAddress.toString(), endAddress.toString());
 			}
 
 			if (startAddress.isAdjacentTo(endAddress)) {
 				if (!this.squareAt(endAddress).isOccupied())
 					return true;
 			} else {
 
 				int changeInFile;
 				int changeInRank;
 
 				int currentRank = startAddress.rank;
 				int currentFile = startAddress.file;
 
 				if (endAddress.file > startAddress.file) {
 					changeInFile = 1;
 				} else {
 					changeInFile = -1;
 				}
 
 				if (endAddress.rank > startAddress.rank) {
 					changeInRank = 1;
 				} else {
 					changeInRank = -1;
 				}
 
 				currentRank += changeInRank;
 				currentFile += changeInFile;
 
 				while (currentFile != endAddress.file
 						&& currentRank != endAddress.rank) {
 
 					if (ChessNamingConstants.DEVELOPMENT) {
 						System.out.println("\t\t\tChecking " + currentRank
 								+ " " + currentFile);
 					}
 
 					if (this.board[currentRank][currentFile].isOccupied()) {
 						if (ChessNamingConstants.DEVELOPMENT) {
 							System.out.println("\t\t\tOccupied!" + currentRank
 									+ " " + currentFile);
 						}
 						return true;
 					}
 
 					currentFile += changeInFile;
 					currentRank += changeInRank;
 				}
 			}
 		}
 
 		return false;
 	}
 
 	private boolean kingIsInCheck() {
 		for (int currentRank = 0; currentRank < this.board.length; currentRank++) {
 			for (int currentFile = 0; currentFile < this.board[currentRank].length; currentFile++) {
 				if (this.board[currentRank][currentFile].isOccupied()
 						&& this.board[currentRank][currentFile].piece.isBlack == this.isBlackTurn) {
 					return isPieceCollidingWithKingAt(currentRank, currentFile);
 				}
 			}
 		}
 
 		return false;
 	}
 
 	public boolean isPieceCollidingWithKingAt(int rank, int file) {
 		ChessPiece cp = this.board[rank][file].piece;
 		ChessCoordinatePair startAddress = new ChessCoordinatePair(rank, file);
 		ArrayList<ChessCoordinatePair> deepestMoves = cp
 				.deepestMovesFrom(startAddress);
 
 		for (int i = 0; i < deepestMoves.size(); i++) {
 			if (this.kingIsFirstCollisionInPathBetween(startAddress,
 					deepestMoves.get(i))) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	private boolean kingIsFirstCollisionInPathBetween(
 			ChessCoordinatePair startAddress, ChessCoordinatePair endAddress) {
 
 		if (startAddress.hasSameFileAs(endAddress)) {
 			for (int i = startAddress.rank + 1; i < endAddress.rank; i++) {
 				if (board[i][startAddress.file].isOccupied()) {
 					return board[i][startAddress.file].piece instanceof KingChessPiece;
 				}
 			}
 		} else if (startAddress.hasSameRankAs(endAddress)) {
 			for (int i = startAddress.file + 1; i < endAddress.file; i++) {
 				if (board[startAddress.rank][i].isOccupied()) {
 					return board[startAddress.rank][i].piece instanceof KingChessPiece;
 				}
 			}
 		} else if (startAddress.isDiagonalTo(endAddress)) {
 
 			int changeInFile;
 			int changeInRank;
 
 			int currentRank = startAddress.rank;
 			int currentFile = startAddress.file;
 
 			if (endAddress.file > startAddress.file) {
 				changeInFile = 1;
 			} else {
 				changeInFile = -1;
 			}
 
 			if (endAddress.rank > startAddress.rank) {
 				changeInRank = 1;
 			} else {
 				changeInRank = -1;
 			}
 
 			currentRank += changeInRank;
 			currentFile += changeInFile;
 
 			while (currentFile != endAddress.file
 					&& currentRank != endAddress.rank) {
 
 				if (ChessNamingConstants.DEVELOPMENT) {
 					System.out.println("\t\t\tChecking " + currentRank + " "
 							+ currentFile);
 				}
 
 				if (this.board[currentRank][currentFile].isOccupied()) {
 					if (ChessNamingConstants.DEVELOPMENT) {
 						System.out.println("\t\t\tOccupied!" + currentRank
 								+ " " + currentFile);
 					}
 					return this.board[currentRank][currentFile].piece instanceof KingChessPiece;
 				}
 
 				currentFile += changeInFile;
 				currentRank += changeInRank;
 
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Returns a printed and formatted board.
 	 */
 	public String toString() {
 		String str = "";
 
 		for (int i = 0; i < board.length; i++) {
 			for (int j = 0; j < board[i].length; j++) {
 				str += this.board[i][j].toString() + " ";
 			}
 
 			str += (i + 1) + "\n";
 		}
 
 		str += " a  b  c  d  e  f  g  h\n";
 
 		return str;
 	}
 
 	/**
 	 * This is dependant on you keeping the boolean up to date. So only use my
 	 * move method. I'm watching you.
 	 * 
 	 * @return Whether or not it is the black peices/player's turn
 	 */
 	public boolean getIsBlackTurn() {
		return !isBlackTurn;
 	}
 
 	private ChessBoardSquare squareAt(ChessCoordinatePair ccp) {
 		return board[ccp.rank][ccp.file];
 	}
 
 }
