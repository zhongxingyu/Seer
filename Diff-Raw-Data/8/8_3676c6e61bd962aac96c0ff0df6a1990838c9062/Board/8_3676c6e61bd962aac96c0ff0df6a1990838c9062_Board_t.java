 /* Board.java */
 
 package player;
 
 import list.*;
 
 class Board {
 
 	public static final int WHITE = 1;
 	public static final int BLACK = 0;
 	public static final int LOWESTVAL = -100;
 	public static final int HIGHESTVAL = 100;
 	private Chip[][] gameboard;
 	private int numPieces;
 
 	/**
 	 * makes a blank board
 	 */
 	public Board() {
 		gameboard = new Chip[8][8]; // ROMI changed 8 to 7.
 
 	}
 
 	/**
 	 * makes hypothetical variations to a certain Board
 	 */
 	public Board(Board b, int color, Move m) { // TODO is color useful?
 		this();
 		for (int x = 0; x < gameboard.length; x++) {
 			for (int y = 0; y < gameboard[0].length; y++) {
 				gameboard[x][y] = b.gameboard[x][y];
 			}
 		}
 		makeMove(color, m);
 
 	}
 
 	public int numPieces() {
 		return numPieces;
 	}
 
 	/**
 	 * if moveKind is ADD return addChip if moveKind is Step return moveChip
 	 */
 
 	public boolean makeMove(int color, Move m) {
 		if (m.moveKind == Move.ADD) {
 			return addChip(color, m);
 		} else if (m.moveKind == Move.STEP) {
 			return moveChip(color, m);
 		}
 		return false;
 	}
 
 	/**
 	 * checks if valid returns false if it isn't adds chip to board return true
 	 */
 	private boolean addChip(int color, Move m) {
 		// checks if valid
 		if (isValid(color, m)) {
 			Chip c = new Chip(m.x1, m.y1, color);
 			gameboard[m.x1][m.y1] = c;
 			Chip[] chips = lineOfSight(c);
 			// for each chip in the new chip's line of sight
 			// recaluate the seen chip's sight
 			for (int i = 0; i < chips.length; i++) {
 				chips[i].clear();
 				Chip[] tmp = lineOfSight(chips[i]);
 				for (int j = 0; j < tmp.length; j++) {
 					chips[i].addC(tmp[j]);
 				}
 			}
 			numPieces++;
 			return true;
 		}
 		// returns false if not valid
 		return false;
 	}
 
 	/**
 	 * returns all valid moves for given color and move
 	 */
 	public DList validMoves(int color) {
 
 		DList allmoves = new DList();
 		for (int i = 0; i < gameboard.length; i++) {
 			for (int j = 0; j < gameboard[0].length; j++) {
 
 				if (numPieces >= 20) { // try moving if more than 20 pieces on
 										// board
 					if (gameboard[i][j] != null) { // and contains and also
 													// contains piece
 
 						for (int a = 0; a < gameboard.length; a++) {
 							for (int b = 0; b < gameboard[0].length; b++) {
 								// to every other possible space
 								Move amove = new Move(a, b, i, j);
 								if (isValid(color, amove)) {
 									allmoves.insertBack(amove);
 								} // TODO number of indentations is scary. can
 									// this change?
 							}
 						}
 					}
 				} else {
 					Move trymove = new Move(i, j); // else try adding
 					if (isValid(color, trymove)) {
 						allmoves.insertBack(trymove);
 					}
 
 				}
 			}
 		}
 		return allmoves;
 	}
 
 	/**
 	 * store and remove old chip from board checks if valid (must check over
 	 * here so isValid doesn't count in the old chip) returns false if it isn't
 	 * valid and readd the chip add a new chip at location x,y return true
 	 */
 	// ROMI: changed for this method warrented by change to isValid.
 	private boolean moveChip(int color, Move m) {
 		// store and remove old chip from board
 		Chip c = new Chip(m.x2, m.y2, color);
 		// ROMI's change: gameboard[m.x2][m.y2].remove(); was here
 		// checks if valid
 		if (isValid(color, m)) {
 			removeChip(gameboard[m.x2][m.y2]);
 			
 			// add a new chip at location x,y
 			addChip(color, new Move(m.x1,m.y1));
 			// return true
 			return true;
 
 		}
 
 		// ROMI's change. adding m.x2,y.x2 chip was here
 		return false;
 	}
 
 	private void removeChip(Chip c) {
 		gameboard[c.getX()][c.getY()]=null;
 		numPieces--;
 		/*
 		 * for (int i = 0; i < chips.length; i++) { chips[i].clear(); Chip[] tmp
 		 * = lineOfSight(chips[i]); for (int j = 0; j < tmp.length; j++) {
 		 * chips[i].addC(tmp[j]); } }
 		 */
 	}
 
 	/**
 	 * returns a score from -100 to 100 100 is a win for self
 	 */
 	public int value(int color) {
 		return 0;
 	}
 
 	/**
 	 * returns a bool to tell you if match is finished. Needed for min max. Use
 	 * in value code
 	 */
 	public boolean isFinished() {
 		return false;
 	}
 
 	/**
 	 * returns true if Move for given color is valid
 	 * 
 	 * chip not in four corners black pieces are not in 0-1 to 0-6 or 7-1 to 7-6
 	 * (inclusive) white pieces are not in 1-0 to 6-0 or 1-7 to 6-7 (inclusive)
 	 * no chip may be placed in a occupied square a chip may not be a cluster(2+
 	 * adjacent chips)
 	 */
 	private boolean isValid(int color, Move m) {
 		System.out.print("\nChecking ");
 		if (color == 1)
 			System.out.print("white moves:");
 		else
 			System.out.print("black moves:");
 		System.out.print(m);
 		// if move is QUIT
 		// return false
 		if (m.moveKind == Move.QUIT) {
 			return false;
 			// if square is occuped
 			// return false
 		}
 		else if(m.moveKind==Move.ADD && numPieces()>=20)
 		{
 			return false;
 		}
 		else if(m.moveKind==Move.STEP && numPieces()<20)
 		{
 			return false;
 		}
 		else if (gameboard[m.x1][m.y1] != null) {
 			return false;
 			// if in 0-0, 0-7, 7-0, 7-7
 			// return false
 		} else if ((m.x1 == 0 || m.x1 == 7) && (m.y1 == 0 || m.y1 == 7)) {
 			return false;
 			// else if black and in 0-1 to 0-6 or in 7-1 to 7-6
 			// return false
 		} else if (color == BLACK && (m.x1 == 0 || m.x1 == 7)
 				&& (m.y1 >= 1 && m.y1 <= 6)) {
 			return false;
 			// else if white and in 1-0 to 6-0 or in 1-7 to 6-7
 			// return false
 		} else if (color == WHITE && (m.x1 >= 1 && m.x1 <= 6) && (m.y1 == 0 || m.y1 == 7)) {
 				return false;
 			// else if is a cluster(2+ adjacent chips)
 			// return false
 		} else if (isCluster(new Chip(m.x1, m.y1, color), 0)) {
 			return false;
 			// Romi's addition
 			// else if moving nonexistant chips return false (uses assumption
 			// that unused vars in
 			// move are initialized to zero
 		} else if (m.moveKind==Move.STEP && gameboard[m.x2][m.y2] == null) {
 			return false;
 			// else if moving a different color than self
 			// return false
 		} else if (m.moveKind==Move.STEP
 				&& gameboard[m.x2][m.y2].color() != color) {
 			return false;
 			// else
 			// return true
 		}
 		System.out.print("is valid");
 		return true;
 	}
 
 	/**
 	 * checks all neighboring spaces on the gameboard around chip c do not count
 	 * the space to be occupied by chip c returns true if the chip has more than
 	 * 1 neighbor check if a neighbor has more than 1 neighbor returns false
 	 * otherwise
 	 */
 	private boolean isCluster(Chip c, int n) {
 		for (int x = c.getX() - 1; x <= c.getX() + 1; x++) {
 			for (int y = c.getY() - 1; y <= c.getY() + 1; y++) {
 				if (x >= 0 && x <= 7 && y >= 0 && y <= 7
 						&& !(x == c.getX() && y == c.getY())) {
 					if (gameboard[x][y] != null
 							&& gameboard[x][y].color() == c.color()) {
 						n++;
 						if (n > 2) {
 							return true;
 						}
 						if (isCluster(new Chip(x, y, gameboard[x][y].color()), n)) {
                             return true;
                         }
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * finds and returns an array of chips that the chip c has direct line of
 	 * sight to
 	 */
 	private Chip[] lineOfSight(Chip c) {
 		int count = 0;
 		Chip[] inLine = new Chip[8];
 		Chip[] result;
 		// searches in 8 directions
 		for (int i = -1; i <= 1; i++) {
 			for (int j = -1; j <= 1; j++) {
 				if (i != 0 && j != 0) {
 					Chip tmp = search(i, j, c);
 					if (tmp != null) {
 						// adds the first chip to an array
 						inLine[count] = tmp;
 						count++;
 					}
 				}
 			}
 		}
 		// copies array into an array with length equal to the number of chips
 		// in line of sight
 		result = new Chip[count];
 		for (int i = 0; i < count; i++) {
 			result[i] = inLine[i];
 		}
 		return result;
 	}
 
 	/**
 	 * checks gameboard at coordinates x and y if its a chip, return chip else
 	 * return null
 	 */
 	private Chip search(int dx, int dy, Chip c) {
 		int x = c.getX() + dx;
 		int y = c.getY() + dy;
 		while (x >= 0 && x < gameboard.length && y >= 0
 				&& y < gameboard[0].length && gameboard[x][y] == null) {
 			if (gameboard[x][y] != null) { // ROMI: i think that this will never
 											// be reached because of the while
 											// statement.
 				return gameboard[x][y];
 			}
 			x += dx;
 			y += dy;
 		}
 		return null;
 	}
 
 	/**
 	 * tester method to test private methods
 	 */
 	public void tester() {
        System.out.println("Testing isCluster");
        System.out.println("Testing search");
        System.out.println("Testing lineOfSight");
        System.out.println("Testing isValid");
 	}
 
 	public static void main(String[] args) {
 		Board board = new Board();
         board.tester();
     }
 }
