 package com.youzwak.connectfour;
 
 public class ConnectFourBoard {
 
 	public enum Piece { EMPTY, RED, BLACK };
 	
 	// Constants
 	public static final int COLUMNS = 7;
	public static final int ROWS    = 10;
 	
 	// Board
 	private Piece board[][];
 	
 	public ConnectFourBoard() {
 		this.board = new Piece[COLUMNS][ROWS];
 		this.reset();
 	}
 	
 	public ConnectFourBoard(ConnectFourBoard other) {
 		this();
 		for (int i = 0; i < COLUMNS; i++) {
 			for (int j = 0; j < ROWS; j++) {
 				this.board[i][j] = other.board[i][j];
 			}
 		}		
 	}
 	
 	public void reset() {
 		for (int i = 0; i < COLUMNS; i++) {
 			for (int j = 0; j < ROWS; j++) {
 				this.board[i][j] = Piece.EMPTY;
 			}
 		}
 	}
 	
 	public boolean isFull(int col) {
 		boolean full = true;
 		
 		if (getTopOfColumn(col) < ROWS) {
 			full = false;
 		}
 		
 		return full;
 	}
 	
 	public boolean isFull() {
 		boolean full = true;
 		
 		for (int col = 0; col < COLUMNS; col++) {
 			if (!isFull(col)) {
 				full = false;
 				break;
 			}
 		}
 		
 		return full;
 	}
 	
 	public void addToColumn(int col, Piece p) throws OutOfRangeException, ColumnFullException {
 		if (col < 0 || col >= COLUMNS)
 			throw new OutOfRangeException();
 		
 		Piece column[] = this.board[col];
 		int   top = getTopOfColumn(col);
 		
 		if (top >= ROWS)
 			throw new ColumnFullException();
 		column[top++] = p;		
 	}
 	
 	public int getTopOfColumn(int col) {
 		int top = 0;
 
 		while(top < ROWS && !board[col][top].equals(Piece.EMPTY)) {
 			top++;
 		}
 		
 		return top;
 	}
 	
 	public void printBoard() {
 		for (int j = ROWS - 1; j >= 0; j--) {
 			for (int i = 0; i < COLUMNS; i++) {
 				if (board[i][j] == Piece.EMPTY) {
 					System.out.print(". ");
 				} else if (board[i][j] == Piece.RED) {
 					System.out.print("O ");
 				} else if (board[i][j] == Piece.BLACK) {
 					System.out.print("X ");
 				}
 			}
 			System.out.println();
 		}
 		for (int i = 0; i < COLUMNS; i++) {
 			System.out.print(i + " ");			
 		}
 		System.out.println();
 	}
 	
 	public boolean isWinner(Piece p) {
 		boolean won = false;
 		
 		if (hasHorizontalWin(p)) {
 			won = true;
 		} else if (hasVerticalWin(p)) {
 			won = true;
 		} else if (hasDiagonalWin(p)) {
 			won = true;
 		}
 		return won;
 	}
 	
 	public boolean hasHorizontalWin(Piece p) {
 		boolean won = false;
 
 		for (int i = 0; i <= COLUMNS - 4; i++) {
 			for (int j = 0; j < ROWS; j++) {
 				if (board[i][j]   == p &&
 					board[i+1][j] == p &&
 					board[i+2][j] == p &&
 					board[i+3][j] == p) {
 					won = true;
 					break;
 				}
 			}
 		}
 		
 		return won;
 	}
 	
 	public int getThreats(Piece p) {
 		int threat = 0;
 		
 		threat = threat + getHorizontalThreats(p);
 		threat = threat + getVerticalThreats(p);
 		threat = threat + getDiagonalThreats(p);
 
 		return threat;
 	}
 	
 	public int getHorizontalThreats(Piece p) {
 		int numEmpty = 0;
 		int numPiece = 0;
 		int threats  = 0;
 		
 		for (int i = 0; i <= COLUMNS - 4; i++) {
 			for (int j = 0; j < ROWS; j++) {
 
 				// Count the number of empty and player pieces
 				numEmpty = 0;
 				numPiece = 0;
 
 				for (int k = 0; k < 4; k++) {
 					if (board[i+k][j]   == p) {
 						numPiece++;
 					} else if (board[i+k][j] == Piece.EMPTY) {
 						numEmpty++;
 					}					
 				}
 
 				// Check for three pieces together with an empty space
 				if (numPiece == 3 && numEmpty == 1) {
 					threats++;
 				}
 			}
 		}
 		
 		return threats;
 	}
 	
 	public boolean hasVerticalWin(Piece p) {
 		boolean won = false;
 
 		for (int i = 0; i < COLUMNS; i++) {
 			for (int j = 0; j <= ROWS - 4; j++) {
 				if (board[i][j]   == p &&
 					board[i][j+1] == p &&
 					board[i][j+2] == p &&
 					board[i][j+3] == p) {
 					won = true;
 					break;
 				}
 			}
 		}
 		
 		return won;
 	}
 	
 	public int getVerticalThreats(Piece p) {
 		int numEmpty = 0;
 		int numPiece = 0;
 		int threats  = 0;
 		
 		for (int i = 0; i < COLUMNS; i++) {
 			for (int j = 0; j <= ROWS - 4; j++) {
 
 				// Count the number of empty and player pieces
 				numEmpty = 0;
 				numPiece = 0;
 
 				for (int k = 0; k < 4; k++) {
 					if (board[i][j+k]   == p) {
 						numPiece++;
 					} else if (board[i][j+k] == Piece.EMPTY) {
 						numEmpty++;
 					}					
 				}
 
 				// Check for three pieces together with an empty space
 				if (numPiece == 3 && numEmpty == 1) {
 					threats++;
 				}
 			}
 		}
 		
 		return threats;
 	}
 
 	
 	public boolean hasDiagonalWin(Piece p) {
 		boolean won = false;
 		
 		// Check the first diagonal
 		for (int i = 0; i <= COLUMNS - 4; i++) {
 			for (int j = 0; j <= ROWS - 4; j++) {
 				if (board[i][j]   == p &&
 					board[i+1][j+1] == p &&
 					board[i+2][j+2] == p &&
 					board[i+3][j+3] == p) {
 					won = true;
 					break;
 				}
 			}
 		}
 		
 		// Check the second diagonal
 		for (int i = 4; i < COLUMNS ; i++) {
 			for (int j = 0; j <= ROWS - 4; j++) {
 				if (board[i][j]   == p &&
 					board[i-1][j+1] == p &&
 					board[i-2][j+2] == p &&
 					board[i-3][j+3] == p) {
 					won = true;
 					break;
 				}
 			}
 		}
 		return won;
 	}
 	
 	public int getDiagonalThreats(Piece p) {
 		int numEmpty = 0;
 		int numPiece = 0;
 		int threats  = 0;
 		
 		// Check the first diagonal
 		for (int i = 0; i <= COLUMNS - 4; i++) {
 			for (int j = 0; j <= ROWS - 4; j++) {
 
 				// Count the number of empty and player pieces
 				numEmpty = 0;
 				numPiece = 0;
 
 				for (int k = 0; k < 4; k++) {
 					if (board[i+k][j+k]   == p) {
 						numPiece++;
 					} else if (board[i+k][j+k] == Piece.EMPTY) {
 						numEmpty++;
 					}					
 				}
 
 				// Check for three pieces together with an empty space
 				if (numPiece == 3 && numEmpty == 1) {
 					threats++;
 				}
 			}
 		}
 		
 		// Check the second diagonal
 		for (int i = 4; i < COLUMNS ; i++) {
 			for (int j = 0; j <= ROWS - 4; j++) {
 
 				// Count the number of empty and player pieces
 				numEmpty = 0;
 				numPiece = 0;
 
 				for (int k = 0; k < 4; k++) {
 					if (board[i-k][j+k]   == p) {
 						numPiece++;
 					} else if (board[i-k][j+k] == Piece.EMPTY) {
 						numEmpty++;
 					}					
 				}
 
 				// Check for three pieces together with an empty space
 				if (numPiece == 3 && numEmpty == 1) {
 					threats++;
 				}
 			}
 		}
 		
 		return threats;
 	}
 	
 	public class OutOfRangeException extends Exception {
 		
 	}
 	
 	public class ColumnFullException extends Exception {
 		
 	}
 
 }
