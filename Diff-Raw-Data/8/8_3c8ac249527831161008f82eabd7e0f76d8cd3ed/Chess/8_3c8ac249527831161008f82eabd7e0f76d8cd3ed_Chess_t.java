 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import javax.swing.*;
 
 /*
  * BUGS:
  * - (FIXED) Bishops have trouble moving to their relative diagonal right,
  *   even when not blocked by anything.
  * - (FIXED) Set whiteTurn to false initially for white to start...
  *   The boolean is getting flipped somewhere.
  * - (FIXED) Pawns can capture sideways
 * - (FIXED) THE KING CAN'T DEFEND HIMSELF!!! NOOOOO
  */
 
 /*
  * TODO:
  * - Game can "see" a check-mate and end the game.
  * - Game is stateful: has a MENU, GAMEMODE, and RESTARTMODE
  * - Basic random AI
  * - Smart AI
  */
 
 public class Chess {
 	
 	public static final int PIECE_XY = 44;
 	public static final int WIDTH = PIECE_XY*8, HEIGHT = PIECE_XY*8 + 22; 
 	public static Piece[][] board;
 	public static BoardComponent bc;
 	
 	private static boolean whiteTurn;
 	private static boolean testing;
 	
 	public static void main(String[] args) {
 		// Construct chess board
 		board = new Piece[8][8];
 		buildBoard(board);
 		whiteTurn = false;
 		
 		// Initialize the frame
 		JFrame window = new JFrame();
 		window.setSize(WIDTH, HEIGHT);
 		window.setTitle("Chess");
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		// Initialize the component, add mouse listener
 		bc = new BoardComponent();
 		bc.addMouseListener(genBoardMouseListener());
 		bc.setBounds(0, 0, WIDTH, HEIGHT);
 		window.add(bc);
 		
 		// Tests grow strong bones
 		tests();
 		
 		// LAST
 		window.setVisible(true);
 	}
 	
 	public static void rawMove(int xfrom, int yfrom, int xto, int yto) {
 		// Differs from move in that it accepts raw x y coordinates, and
 		// attempts to call move.
 
 		int xf = (int) Math.floor(xfrom / PIECE_XY);
 		int yf = (int) Math.floor(yfrom / PIECE_XY);
 		int xt = (int) Math.floor(xto / PIECE_XY);
 		int yt = (int) Math.floor(yto / PIECE_XY);
 		
 		move(board, xf, yf, xt, yt);
 	}
 	
 	// Accepts a board and board position coordinates	
 	private static boolean move(Piece[][] board, int xfrom, int yfrom, int xto, int yto) {
 
 		Piece from = board[yfrom][xfrom];
 		Piece to = board[yto][xto];
 		boolean capture = (to != null);
 		
 		if (castleMove(board, from, to, capture, xfrom, yfrom, xto, yto)) {
 			int kDest, rDest;
 			boolean fromKing;
 			if (from instanceof King) {
 				fromKing = true;
 				kDest = (xto == 7) ? 6 : 2;
 				rDest = (xto == 7) ? 5 : 3;
 			} else {
 				fromKing = false;
 				kDest = (xfrom == 7) ? 6 : 2;
 				rDest = (xfrom == 7) ? 5 : 3;
 			}
 			
 			board[yfrom][xfrom] = null;
 			board[yto][xto] = null;
 			
 			board[yfrom][kDest] = (fromKing) ? from : to;
 			board[yto][rDest] = (fromKing) ? to : from;
 			
 			bc.repaint();
 			
 			whiteTurn = !whiteTurn;
 			from.firstMove = false;
 			to.firstMove = false;
 			return true;
 		} else if (standardMove(board, from, to, capture, xfrom, yfrom, xto, yto)) {
 			if (from instanceof Pawn && yto == ((from.white) ? 7 : 0)) {
 				board[yfrom][xfrom] = null;
 				board[yto][xto] = new Queen(from.white);
 			} else {
 				board[yfrom][xfrom] = null;
 				board[yto][xto] = from;
 			}
 			
 			bc.repaint();
 			
 			// Switch turns and set moved piece's firstMove bool to false 
 			whiteTurn = !whiteTurn;
 			from.firstMove = false;
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private static boolean standardMove(Piece[][] board, Piece from, Piece to, boolean capture,
 										int xfrom, int yfrom, int xto, int yto) {
 		// Validates a standard move given a slew of input.
 		// A standard move follows the general rules of chess, as opposed to fleeing check
 		// or castling a King and Rook, which require alternate validations.
 		
 		return (from != null &&											// from isn't null
 				correctTurn(from.white) &&								// correct player's turn
 				from.validMove(xfrom, yfrom, xto, yto, capture) && 		// valid move for that piece
 				(!capture || from.white != to.white) && 				// moving to valid position
 				notBlocked(board, xfrom, yfrom, xto, yto) &&			// the piece is not blocked
 				kingAvoidsCheck(board, from, xfrom, yfrom, xto, yto));
 	}
 	
 	private static boolean castleMove(Piece[][] board, Piece from, Piece to, boolean capture,
 									  int xfrom, int yfrom, int xto, int yto) {
 		
 		return (from != null && to != null &&		
 				correctTurn(from.white) &&								// A valid castle move:
 				from.white == to.white &&								// From and to are same color,
 				(from instanceof King && to instanceof Rook || 			// a king and rook are switching,
 						from instanceof Rook && to instanceof King) &&
 				from.firstMove && to.firstMove &&						// it is both pieces' first move,
 				notBlocked(board, xfrom, yfrom, xto, yto));				// and there is nothing blocking them.
 	}
 	
 	private static boolean correctTurn(boolean white) {
 		// Testing flag turns off correctTurn validations
 		return (testing) ? true : white == whiteTurn;
 	}
 	
 	private static boolean kingAvoidsCheck(Piece[][] board, Piece piece, int xfrom, int yfrom, int xto, int yto) {
 		// Returns true if the given piece could move to board[yto][xto] without
 		// encountering check.
 		Piece[][] team = getTeam(board, piece.white);
 		int[] king = getKing(team);
 		
 		boolean ret = true;
 		
 		Piece old = board[yto][xto];
 		board[yto][xto] = piece;
 		board[yfrom][xfrom] = null;
 		
 		if (piece instanceof King) {
 			king[0] = yto;
 			king[1] = xto;
 		}
 		
		// Declare opponents after the board is temporarily modified
		Piece[][] opps = getTeam(board, !piece.white);
		
 		for (int i = 0; i < opps.length; i++) {
 			for (int j = 0; j < opps.length; j++) {
 				Piece opp = opps[i][j];
 				
 				if (opp != null && 
 					opp.validMove(j, i, king[1], king[0], true) &&
 					notBlocked(board, j, i, king[1], king[0])) {
 					ret = false;
 				}
 			}
 		}
 		
 		board[yfrom][xfrom] = piece;
 		board[yto][xto] = old;
 		
 		return ret;
 	}
 	
 	private static int[] getKing(Piece[][] team) {
 		// Returns the King on the given team.
 		// Expects a single team, not the entire board.
 		// Returns null if a King can't be found.
 		
 		for (int i = 0; i < team.length; i++) {
 			for (int j = 0; j < team.length; j++) {
 				Piece piece = team[i][j];
 				
 				if (piece != null && piece instanceof King) {
 					return new int[] {i, j};
 				}
 			}
 		}
 		
 		for (Piece[] row : team) {
 			for (Piece piece : row) {
 				if (piece != null && piece instanceof King) {
 				}
 			}
 		}
 		return null;
 	}
 	
 	private static Piece[][] getTeam(Piece[][] board, boolean white) {
 		// Returns the opponents of the given color in their current positions
 		Piece[][] team = new Piece[8][8];
 		
 		for (int i = 0; i < board.length; i++) {
 			for (int j = 0; j < board.length; j++) {
 				Piece piece = board[i][j];
 				team[i][j] = (piece != null && piece.white == white) ? piece : null;
 			}
 		}
 		
 		return team;
 	}
 	
 	private static boolean notBlocked(Piece[][] board, int xfrom, int yfrom, int xto, int yto) {
 		// Returns true if the entire path from the origin to the destination is clear.
 		// (This excepts knights, which can move over teammates and enemies.)
 		
 		Piece from = board[yfrom][xfrom];
 		Piece to = board[yto][xto];
 		
 		// Determine the direction (if any) of x and y movement
 		int dx = (xfrom < xto) ? 1 : ((xfrom == xto) ? 0 : -1);
 		int dy = (yfrom < yto) ? 1 : ((yfrom == yto) ? 0 : -1);
 		
 		// Determine the number of times we must iterate
 		int steps = Math.max(Math.abs(xfrom - xto), Math.abs(yfrom - yto));
 		
 		if (xfrom == xto || yfrom == yto || Math.abs(xfrom - xto) == Math.abs(yfrom - yto)) {
 			for (int i = 1; i < steps; i++) {
 				int x = xfrom + i * dx;
 				int y = yfrom + i * dy;
 				if (isBlocked(board, from, to, x, y)) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 	
 	private static boolean isBlocked(Piece[][] board, Piece from, Piece to, int x, int y) {
 		return (board[y][x] != null && board[y][x] != to && board[y][x] != from);
 	}
 	
 	private static MouseListener genBoardMouseListener() {
 		return new MouseListener() {
 			int xfrom;
 			int yfrom;
 			
 			@Override
 			public void mousePressed(MouseEvent e) {
 				// Overwrite previous x and y from values
 				this.xfrom = e.getX();
 				this.yfrom = e.getY();
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				// Pass mouse from and to coordinates to Chess to handle
 				// validation and moves
 				int xto = e.getX();
 				int yto = e.getY();
 				int xfrom = this.xfrom;
 				int yfrom = this.yfrom;
 				
 				Chess.rawMove(xfrom, yfrom, xto, yto);
 			}
 			
 			@Override
 			public void mouseClicked(MouseEvent e) {
 			}
 			@Override
 			public void mouseEntered(MouseEvent e) {
 			}
 			@Override
 			public void mouseExited(MouseEvent e) {
 			}
 		};
 	}
 
 	private static void buildBoard(Piece[][] board) {
 		// Rows 0 and 1 will contain white pieces,
 		// and rows 6 and 7 will contain black pieces.
 		
 		// Place pawns in rows 1 and 6
 		for (int i = 0; i < board.length; i++) {
 			board[1][i] = new Pawn(true);
 			board[6][i] = new Pawn(false);
 		}
 		
 		// Place white and black row pieces.
 		placeBackRow(board, true, 0);
 		placeBackRow(board, false, 7);
 	}
 	
 	private static void placeBackRow(Piece[][] board, boolean white, int row) {
 		// Place rooks on the outside
 		board[row][0] = new Rook(white);
 		board[row][7] = new Rook(white);
 		
 		// Place knights inside of rooks
 		board[row][1] = new Knight(white);
 		board[row][6] = new Knight(white);
 		
 		// Place bishops inside of knights
 		board[row][2] = new Bishop(white);
 		board[row][5] = new Bishop(white);
 		
 		// Place king and queen
 		board[row][3] = new Queen(white);
 		board[row][4] = new King(white);
 	}
 	
 	private static void printBoard(Piece[][] board) {
 		// Prints a representation of the current state of the chess board
 		
 		for (Piece[] row: board) {
 			for (Piece p: row) {
 				System.out.print( ((p == null) ? "_" : p.str) + " " );
 			}
 			System.out.println();
 		}
 	}
 	
 	private static void tests() {
 		testing = true;
 		
 		//
 		// Tests for Piece-level validations
 		//
 		Pawn pawn = new Pawn(true);
 		System.out.println("PAWN TESTS RUNNING...");
 		testFor(true, pawn.validMove(0, 0, 1, 1, true), "Pawn can't diagonally capture");
 		testFor(true, pawn.validMove(0, 0, 0, 1, false), "Pawn can't move forward 1 space");
 		testFor(false, pawn.validMove(0, 0, 0, 1, true), "Pawn can capture forward");
 		testFor(false, pawn.validMove(4, 4, 1, 1, true), "Pawn can move backwards");
 		
 		King king = new King(false);
 		System.out.println("KING TESTS RUNNING...");
 		testFor(true, king.validMove(7, 7, 6, 6, true), "King can't capture diagonally 1 space");
 		testFor(true, king.validMove(7, 7, 6, 7, false), "King can't move horizontally 1 space");
 		testFor(false, king.validMove(7, 7, 5, 6, false), "King can move more than 1 horizontal space");
 		testFor(true, king.validMove(5, 5, 6, 6, false), "King can't move backwards");
 		
 		Queen queen = new Queen(true);
 		System.out.println("QUEEN TESTS RUNNING...");
 		testFor(true, queen.validMove(0, 0, 6, 6, true), "Queen can't move diagonally");
 		testFor(false, queen.validMove(1, 0, 6, 6, true), "Queen can move diagonally erratically");
 		testFor(true, queen.validMove(1, 0, 1, 7, false), "Queen can't move vertically");
 		testFor(true, queen.validMove(0, 0, 5, 0, false), "Queen can't move horizontally");
 		
 		Bishop bishop = new Bishop(false);
 		System.out.println("BISHOP TESTS RUNNING...");
 		testFor(true, bishop.validMove(0, 0, 6, 6, true), "Bishop can't move diagonally");
 		testFor(true, bishop.validMove(5, 6, 2, 3, false), "Bishop can't move diagonally");
 		testFor(false, bishop.validMove(6, 6, 2, 3, true), "Bishop can move diagonally erratically");
 		testFor(false, bishop.validMove(0, 1, 0, 5, true), "Bishop can move vertically");
 		
 		Knight knight = new Knight(true);
 		System.out.println("KNIGHT TESTS RUNNING...");
 		testFor(true, knight.validMove(0, 0, 1, 2, true), "Knight can't make 1x 2x move");
 		testFor(true, knight.validMove(5, 4, 3, 3, false), "Knight can't make 2x 1x move");
 		testFor(false, knight.validMove(0, 0, 2, 2, true), "Knight can move 2x and 2y");
 		testFor(false, knight.validMove(0, 0, 1, 0, false), "Knight can move in just one direction");
 		
 		Rook rook = new Rook(false);
 		System.out.println("ROOK TESTS RUNNING...");
 		testFor(true, rook.validMove(0, 1, 5, 1, true), "Rook can't move horizontally.");
 		testFor(true, rook.validMove(0, 0, 0, 5, false), "Rook can't move vertically backwards.");
 		testFor(true, rook.validMove(6, 6, 6, 2, true), "Rook can't move vertically forwards");
 		testFor(false, rook.validMove(0, 0, 3, 3, false), "Rook can move diagonally");
 		
 		//
 		// Tests for board-level validations
 		//
 		Piece[][] testBoard = new Piece[8][8];
 		buildBoard(testBoard);
 		
 		System.out.println("GAME MECHANICS TESTS RUNNING...");
 		
 		// Move a pawn, test that you can't move ontop of team
 		testFor(true, move(testBoard, 0, 6, 0, 5), "Pawn can't move forward one space");
 		testFor(false, move(testBoard, 0, 0, 1, 0), "Rook can move on top of neighboring knight");
 		testFor(false, move(testBoard, 4, 0, 4, 1), "King can move on top of pawn");
 		
 		// Move pawn blocking king
 		testFor(true, move(testBoard, 4, 1, 4, 2), "Pawn can't move forward one space");
 		testFor(true, move(testBoard, 4, 0, 4, 1), "King doesn't know pawn moved");
 		
 		// Test blocking
 		testFor(false, move(testBoard, 2, 0, 0, 2), "Bishop isn't diagonally blocked");
 		testFor(false, move(testBoard, 3, 0, 3, 3), "Queen isn't vertically blocked");
 		
 		// Move pawns blocking 2 0 bishop, then move bishop
 		testFor(true, move(testBoard, 3, 1, 3, 2), "Pawn can't move forward one space");
 		testFor(true, move(testBoard, 4, 2, 4, 3), "Pawn can't move forward one space");
 		testFor(true, move(testBoard, 2, 0, 6, 4), "Bishop can't move diagonally unblocked");
 		
 		// Test knight movement and blocking characteristics
 		testFor(true, move(testBoard, 1, 7, 2, 5), "Knight is blocked by team");
 		testFor(false, move(testBoard, 2, 5, 4, 6), "Knight can capture teammate");
 		
 		// Test captures
 		testFor(true, move(testBoard, 2, 5, 1, 3), "Knight can't make valid move");
 		testFor(true, move(testBoard, 1, 3, 3, 2), "Knight can't capture pawn");
 		testFor(true, move(testBoard, 6, 4, 4, 6), "Bishop can't capture pawn");
 		testFor(true, move(testBoard, 6, 7, 7, 5), "Knight can't make valid move");
 		testFor(true, move(testBoard, 7, 5, 5, 4), "Knight can't make valid move");
 		testFor(true, move(testBoard, 4, 3, 5, 4), "Pawn can't capture knight");
 		testFor(true, move(testBoard, 5, 4, 5, 5), "Pawn can't move forward one space");
 		testFor(false, move(testBoard, 5, 5, 5, 6), "Pawn can capture forward");
 		
 		// Test bishop relative right diagonal bug
 		testFor(true, move(testBoard, 4, 1, 4, 2), "King can't move forward one space");
 		testFor(true, move(testBoard, 3, 2, 5, 3), "Knight can't make valid move");
 		testFor(true, move(testBoard, 5, 0, 1, 4), "Bishop can't move relative right diagonal");
 		
 		// Test castling
 		testFor(true, move(testBoard, 6, 6, 6, 5), "Pawn can't move forward one space");
 		testFor(true, move(testBoard, 5, 7, 6, 6), "Bishop can't move diagnonally");
 		testFor(true, move(testBoard, 4, 7, 7, 7), "King can't castle rook");
 		
 		//
 		// Reset globals
 		//
 		whiteTurn = true;
 		testing = false;
 	}
 	
 	private static void testFor(boolean expects, boolean test, String errorMsg) {
 		// Small helper function for nice-looking tests
 		if (!(expects == test)) {
 			System.err.println("Failure: "+errorMsg);
 		}
 	}
 	
 }
