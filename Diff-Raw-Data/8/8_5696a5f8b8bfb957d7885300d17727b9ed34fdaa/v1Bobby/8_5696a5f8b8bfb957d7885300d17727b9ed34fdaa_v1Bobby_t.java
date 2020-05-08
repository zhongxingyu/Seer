 package generalChess;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Random;
 
 import sharedfiles.Bishop;
 import sharedfiles.Blank;
 import sharedfiles.Board;
 import sharedfiles.King;
 import sharedfiles.Knight;
 import sharedfiles.Pawn;
 import sharedfiles.Piece;
 import sharedfiles.Queen;
 import sharedfiles.Rook;
 
 public class v1Bobby {
 	public boolean color;
 	public Piece[][] b;
 	public int move;
 
 	public v1Bobby(Board b, boolean c) {
 		this.color = c;
 		this.b = new Piece[8][8];
 		getBoard(b);
 		move = 0;
 	}
 
 	public v1Bobby(Piece[][] b, boolean c) {
 		this.color = c;
 		this.b = new Piece[8][8];
 		getBoard(b);
 		move = 0;
 	}
 
 	public boolean getColor() {
 		return color;
 	}
 
 	public Piece[][] getPieceArray() {
 		return b;
 	}
 
 	public void getBoard(Board b) {
 		for (int y = 0; y < 8; y++) {
 			for (int x = 0; x < 8; x++) {
 				char t = b.getBoardArray()[x][y].toString().charAt(1);
 				boolean tt = b.getBoardArray()[x][y].getColor();
 				switch (t) {
 					case 'P':
 						this.b[x][y] = new Pawn(tt);
 						break;
 					case 'R':
 						this.b[x][y] = new Rook(tt);
 						break;
 
 					case 'N':
 						this.b[x][y] = new Knight(tt);
 						break;
 
 					case 'B':
 						this.b[x][y] = new Bishop(tt);
 						break;
 
 					case 'K':
 						this.b[x][y] = new King(tt);
 						break;
 
 					case 'Q':
 						this.b[x][y] = new Queen(tt);
 						break;
 
 					case 'X':
 						this.b[x][y] = new Blank(true);
 						break;
 
 				}
 			}
 		}
 	}
 
 	public void getBoard(Piece[][] u) {
 		for (int y = 0; y < 8; y++) {
 			for (int x = 0; x < 8; x++) {
 				char t = u[x][y].toString().charAt(1);
 				boolean tt = u[x][y].getColor();
 				switch (t) {
 					case 'P':
 						this.b[x][y] = new Pawn(tt);
 						break;
 					case 'R':
 						this.b[x][y] = new Rook(tt);
 						break;
 
 					case 'N':
 						this.b[x][y] = new Knight(tt);
 						break;
 
 					case 'B':
 						this.b[x][y] = new Bishop(tt);
 						break;
 
 					case 'K':
 						this.b[x][y] = new King(tt);
 						break;
 
 					case 'Q':
 						this.b[x][y] = new Queen(tt);
 						break;
 
 					case 'X':
 						this.b[x][y] = new Blank(true);
 						break;
 
 				}
 			}
 		}
 	}
 
 	public void move(int ax, int ay, int bx, int by) {
 		char t = this.b[ax][ay].toString().charAt(1);
 		boolean c = this.b[ax][ay].getColor();
 		switch (t) {
 			case 'P':
 				this.b[bx][by] = new Pawn(c);
 				break;
 			case 'R':
 				this.b[bx][by] = new Rook(c);
 				break;
 			case 'N':
 				this.b[bx][by] = new Knight(c);
 				break;
 			case 'B':
 				this.b[bx][by] = new Bishop(c);
 				break;
 			case 'K':
 				this.b[bx][by] = new King(c);
 				break;
 			case 'Q':
 				this.b[bx][by] = new Queen(c);
 				break;
 		}
 
 		this.b[ax][ay] = new Blank(true);
 	}
 
 	public void printBoard() {
 		for (int x = 0; x < 8; x++) {
 			for (int y = 0; y < 8; y++) {
 				System.out.print(b[y][x] + " | ");
 			}
 			System.out.println();
 		}
 
 		System.out.println();
 		System.out.println("---------------------------------------");
 		System.out.println();
 
 	}
 
 	// returns possible king moves
 	public ArrayList<ArrayList> kMoves() {
 		ArrayList<ArrayList> d = new ArrayList();
 		ArrayList g = new ArrayList();
 		int x = -1;
 		int y = -1;
 		for (int j = 0; j < 8; j++) {
 			for (int k = 0; k < 8; k++) {
 				if (b[j][k].getColor() == color && b[j][k].toString().charAt(1) == 'K') {
 					x = j;
 					y = k;
 					break;
 				}
 			}
 		}
 		g.add(b[x][y]);
 		g.add(new Point(x, y));
 		if (x == -1 || y == -1) {
 			return null;
 		} else {
 			for (int a = x - 1; a < x + 2; a++) {
 				for (int b = y - 1; b < y + 2; b++) {
 					if (a > -1 && a < 8 && b > -1 && b < 8) {
 						if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color) {
 							g.add(new Point(a, b));
 						}
 					}
 				}
 			}
 		}
 		d.add(g);
 		return d;
 	}
 
 	// possible queen moves
 	public ArrayList<ArrayList> qMoves() {
 		ArrayList<ArrayList> d = new ArrayList<ArrayList>();
 		ArrayList g = new ArrayList();
 		int x = -1;
 		int y = -1;
 		for (int j = 0; j < 8; j++) {
 			for (int k = 0; k < 8; k++) {
 				if (b[j][k].getColor() == color && b[j][k].toString().charAt(1) == 'Q') {
 					x = j;
 					y = k;
 					break;
 				}
 			}
 		}
 		g.add(b[x][y]);
 		g.add(new Point(x, y));
 		if (x == -1 || y == -1) {
 			return null;
 		} else {
 			// checks queen for diagonal down to the right. quits if adds an
 			// opposite color piece or reaches end of board or our piece
 			int a = x;
 			if (a == 7)
 				a = a - 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			int b = y;
 			if (b == 7)
 				b = b - 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			int quit = 0;
 			do {
 				a = a + 1;
 				b = b + 1;
 				if (a == 7)
 					quit = -999;
 				if (b == 7)
 					quit = -999;
 				if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 					g.add(new Point(a, b));
 				if (this.b[a][b].getColor() != color)
 					quit = -999;
 				if (this.b[a][b].getColor() == color)
 					quit = -999;
 			} while (quit != -999);
 
 			// checks queen for diagonal down to the left. quits if adds an
 			// opposite color piece or reaches end of board or our piece
 			a = x;
 			if (a == 0)
 				a = a + 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			b = y;
 			if (b == 7)
 				b = b - 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			quit = 0;
 			do {
 				a = a - 1;
 				b = b + 1;
 				if (a == 0)
 					quit = -999;
 				if (b == 7)
 					quit = -999;
 				if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 					g.add(new Point(a, b));
 				if (this.b[a][b].getColor() != color)
 					quit = -999;
 				if (this.b[a][b].getColor() == color)
 					quit = -999;
 			} while (quit != -999);
 
 			// checks queen for diagonal up to the left. quits if adds an
 			// opposite color piece or reaches end of board or our piece
 			a = x;
 			if (a == 0)
 				a = a + 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			b = y;
 			if (b == 0)
 				b = b + 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			quit = 0;
 			do {
 				a = a - 1;
 				b = b - 1;
 				if (a == 0)
 					quit = -999;
 				if (b == 0)
 					quit = -999;
 				if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 					g.add(new Point(a, b));
 				if (this.b[a][b].getColor() != color)
 					quit = -999;
 				if (this.b[a][b].getColor() == color)
 					quit = -999;
 			} while (quit != -999);
 
 			// checks queen for diagonal up to the right. quits if adds an
 			// opposite color piece or reaches end of board or our piece
 			a = x;
 			if (a == 7)
 				a = a - 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			b = y;
 			if (b == 0)
 				b = b + 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			quit = 0;
 			do {
 				a = a + 1;
 				b = b - 1;
 				if (a == 7)
 					quit = -999;
 				if (b == 0)
 					quit = -999;
 				if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 					g.add(new Point(a, b));
 				if (this.b[a][b].getColor() != color)
 					quit = -999;
 				if (this.b[a][b].getColor() == color)
 					quit = -999;
 			} while (quit != -999);
 
 			// checks queen north. quits if adds an opposite color piece or
 			// reaches end of board or hits our piece
 			a = x;
 			b = y;
 			if (b == 0)
 				b = b + 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			quit = 0;
 			do {
 				b = b - 1;
 				if (b == 0)
 					quit = -999;
 				if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 					g.add(new Point(a, b));
 				if (this.b[a][b].getColor() != color)
 					quit = -999;
 				if (this.b[a][b].getColor() == color)
 					quit = -999;
 			} while (quit != -999);
 
 			// checks queen south. quits if adds an opposite color piece or
 			// reaches end of board or hits our piece
 			a = x;
 			b = y;
 			if (b == 7)
 				b = b - 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			quit = 0;
 			do {
 				b = b + 1;
 				if (b == 7)
 					quit = -999;
 				if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 					g.add(new Point(a, b));
 				if (this.b[a][b].getColor() != color)
 					quit = -999;
 				if (this.b[a][b].getColor() == color)
 					quit = -999;
 			} while (quit != -999);
 
 			// checks queen to the west. quits if adds an opposite color piece
 			// or reaches end of board or hits our piece
 			a = x;
 			b = y;
 			if (a == 7)
 				a = a - 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			quit = 0;
 			do {
 				a = a + 1;
 				if (a == 7)
 					quit = -999;
 				if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 					g.add(new Point(a, b));
 				if (this.b[a][b].getColor() != color)
 					quit = -999;
 				if (this.b[a][b].getColor() == color)
 					quit = -999;
 			} while (quit != -999);
 
 			// checks queen to the east. quits if adds an opposite color piece
 			// or reaches end of board or hits our piece
 			a = x;
 			b = y;
 			if (a == 0)
 				a = a + 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			quit = 0;
 			do {
 				a = a - 1;
 				if (a == 0)
 					quit = -999;
 				if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 					g.add(new Point(a, b));
 				if (this.b[a][b].getColor() != color)
 					quit = -999;
 				if (this.b[a][b].getColor() == color)
 					quit = -999;
 			} while (quit != -999);
 
 		}
 		d.add(g);
 		return d;
 	}
 
 	public ArrayList<ArrayList> rMoves() {
 		ArrayList<ArrayList> d = new ArrayList<ArrayList>();
 		ArrayList g = new ArrayList();
 		int x = -1;
 		int y = -1;
 		for (int j = 0; j < 8; j++) {
 			for (int k = 0; k < 8; k++) {
 				if (b[j][k].getColor() == color && b[j][k].toString().charAt(1) == 'Q') {
 					x = j;
 					y = k;
 					break;
 				}
 			}
 		}
 		g.add(b[x][y]);
 		g.add(new Point(x, y));
 		if (x == -1 || y == -1) {
 			return null;
 		} else {
 			// checks queen rook. quits if adds an opposite color piece or
 			// reaches end of board or hits our piece
 			int a = x;
 			int b = y;
 			if (b == 0)
 				b = b + 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			int quit = 0;
 			do {
 				b = b - 1;
 				if (b == 0)
 					quit = -999;
 				if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 					g.add(new Point(a, b));
 				if (this.b[a][b].getColor() != color)
 					quit = -999;
 				if (this.b[a][b].getColor() == color)
 					quit = -999;
 			} while (quit != -999);
 
 			// checks rook south. quits if adds an opposite color piece or
 			// reaches end of board or hits our piece
 			a = x;
 			b = y;
 			if (b == 7)
 				b = b - 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			quit = 0;
 			do {
 				b = b + 1;
 				if (b == 7)
 					quit = -999;
 				if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 					g.add(new Point(a, b));
 				if (this.b[a][b].getColor() != color)
 					quit = -999;
 				if (this.b[a][b].getColor() == color)
 					quit = -999;
 			} while (quit != -999);
 
 			// checks rook to the west. quits if adds an opposite color piece or
 			// reaches end of board or hits our piece
 			a = x;
 			b = y;
 			if (a == 7)
 				a = a - 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			quit = 0;
 			do {
 				a = a + 1;
 				if (a == 7)
 					quit = -999;
 				if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 					g.add(new Point(a, b));
 				if (this.b[a][b].getColor() != color)
 					quit = -999;
 				if (this.b[a][b].getColor() == color)
 					quit = -999;
 			} while (quit != -999);
 
 			// checks rook to the east. quits if adds an opposite color piece or
 			// reaches end of board or hits our piece
 			a = x;
 			b = y;
 			if (a == 0)
 				a = a + 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			quit = 0;
 			do {
 				a = a - 1;
 				if (a == 0)
 					quit = -999;
 				if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 					g.add(new Point(a, b));
 				if (this.b[a][b].getColor() != color)
 					quit = -999;
 				if (this.b[a][b].getColor() == color)
 					quit = -999;
 			} while (quit != -999);
 		}
 		d.add(g);
 		return d;
 	}
 
 	public ArrayList<ArrayList> nMoves() {
 		ArrayList<ArrayList> v = new ArrayList<ArrayList>();
 		for (int y = 0; y < 8; y++) {
 			for (int x = 0; x < 8; x++) {
 				if (b[x][y].getColor() == color && b[x][y].toString().charAt(1) == 'N') {
 					ArrayList d = new ArrayList();
 					d.add(b[x][y]);
 					d.add(new Point(x, y));
 					if (x - 2 > -1 && y - 1 > -1
 							&& (b[x - 2][y - 1].toString().charAt(1) == 'X' || b[x - 2][y - 1].getColor() == !color))
 						d.add(new Point(x - 2, y - 1));
 					if (x - 1 > -1 && y - 2 > -1
 							&& (b[x - 1][y - 2].toString().charAt(1) == 'X' || b[x - 1][y - 2].getColor() == !color))
 						d.add(new Point(x - 1, y - 2));
 
 					if (x - 2 > -1 && y + 1 < 8
 							&& (b[x - 2][y + 1].toString().charAt(1) == 'X' || b[x - 2][y + 1].getColor() == !color))
 						d.add(new Point(x - 2, y + 1));
 					if (x - 1 > -1 && y + 2 < 8
 							&& (b[x - 1][y + 2].toString().charAt(1) == 'X' || b[x - 1][y + 2].getColor() == !color))
 						d.add(new Point(x - 1, y + 2));
 
 					if (x + 2 < 8 && y + 1 < 8
 							&& (b[x + 2][y + 1].toString().charAt(1) == 'X' || b[x + 2][y + 1].getColor() == !color))
 						d.add(new Point(x + 2, y + 1));
 					if (x + 1 < 8 && y + 2 < 8
 							&& (b[x + 1][y + 2].toString().charAt(1) == 'X' || b[x + 1][y + 2].getColor() == !color))
 						d.add(new Point(x + 1, y + 2));
 
 					if (x + 1 < 8 && y - 2 > -1
 							&& (b[x + 1][y - 2].toString().charAt(1) == 'X' || b[x + 1][y - 2].getColor() == !color))
 						d.add(new Point(x + 1, y - 2));
 					if (x + 2 < 8 && y - 1 > -1
 							&& (b[x + 2][y - 1].toString().charAt(1) == 'X' || b[x + 2][y - 1].getColor() == !color))
 						d.add(new Point(x + 2, y - 1));
 					v.add(d);
 				}
 			}
 		}
 		return v;
 	}
 
 	public ArrayList<ArrayList> bMoves() {
 		ArrayList<ArrayList> m = new ArrayList<ArrayList>();
 		ArrayList g = new ArrayList();
 		int x = -1;
 		int y = -1;
 		for (int j = 0; j < 8; j++) {
 			for (int k = 0; k < 8; k++) {
 				if (b[j][k].getColor() == color && b[j][k].toString().charAt(1) == 'Q') {
 					x = j;
 					y = k;
 					break;
 				}
 			}
 		}
 		g.add(b[x][y]);
 		g.add(new Point(x, y));
 		if (x == -1 || y == -1) {
 			return null;
 		} else {
 			// checks bishop for diagonal down to the right. quits if adds an
 			// opposite color piece or reaches end of board or our piece
 			int a = x;
 			if (a == 7)
 				a = a - 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			int b = y;
 			if (b == 7)
 				b = b - 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			int quit = 0;
 			do {
 				a = a + 1;
 				b = b + 1;
 				if (a == 7)
 					quit = -999;
 				if (b == 7)
 					quit = -999;
 				if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 					g.add(new Point(a, b));
 				if (this.b[a][b].getColor() != color)
 					quit = -999;
 				if (this.b[a][b].getColor() == color)
 					quit = -999;
 			} while (quit != -999);
 
 			// checks queen for diagonal down to the left. quits if adds an
 			// opposite color piece or reaches end of board or our piece
 			a = x;
 			if (a == 0)
 				a = a + 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			b = y;
 			if (b == 7)
 				b = b - 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			quit = 0;
 			do {
 				a = a - 1;
 				b = b + 1;
 				if (a == 0)
 					quit = -999;
 				if (b == 7)
 					quit = -999;
 				if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 					g.add(new Point(a, b));
 				if (this.b[a][b].getColor() != color)
 					quit = -999;
 				if (this.b[a][b].getColor() == color)
 					quit = -999;
 			} while (quit != -999);
 
 			// checks bishop for diagonal up to the left. quits if adds an
 			// opposite color piece or reaches end of board or our piece
 			a = x;
 			if (a == 0)
 				a = a + 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			b = y;
 			if (b == 0)
 				b = b + 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			quit = 0;
 			do {
 				a = a - 1;
 				b = b - 1;
 				if (a == 0)
 					quit = -999;
 				if (b == 0)
 					quit = -999;
 				if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 					g.add(new Point(a, b));
 				if (this.b[a][b].getColor() != color)
 					quit = -999;
 				if (this.b[a][b].getColor() == color)
 					quit = -999;
 			} while (quit != -999);
 
 			// checks bishop for diagonal up to the right. quits if adds an
 			// opposite color piece or reaches end of board or our piece
 			a = x;
 			if (a == 7)
 				a = a - 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			b = y;
 			if (b == 0)
 				b = b + 1; // if piece is already on edge of board, need to
 							// adjust for the do while loop to work
 			quit = 0;
 			do {
 				a = a + 1;
 				b = b - 1;
 				if (a == 7)
 					quit = -999;
 				if (b == 0)
 					quit = -999;
 				if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 					g.add(new Point(a, b));
 				if (this.b[a][b].getColor() != color)
 					quit = -999;
 				if (this.b[a][b].getColor() == color)
 					quit = -999;
 			} while (quit != -999);
 		}
 		m.add(g);
 		return m;
 	}
 
 	public ArrayList<ArrayList> pMoves() {
 		ArrayList<ArrayList> v = new ArrayList<ArrayList>();
 		for (int y = 0; y < 8; y++) {
 			for (int x = 0; x < 8; x++) {
 				if (b[x][y].getColor() == color && b[x][y].toString().charAt(1) == 'P') {
 					ArrayList d = new ArrayList();
 					d.add(b[x][y]);
 					d.add(new Point(x, y));
 					if (color == false) {
 						if (y == 1 && b[x][y + 2].toString().charAt(1) == 'X')
 							d.add(new Point(x, y + 2));
 						if (y + 1 < 8 && b[x][y + 1].toString().charAt(1) == 'X')
 							d.add(new Point(x, y + 1));
 						if (x + 1 < 8 && y + 1 < 8
 								&& (b[x + 1][y + 1].toString().charAt(1) != 'X' && b[x + 1][y + 1].getColor() != color))
 							d.add(new Point(x + 1, y + 1));
 						if (x - 1 > -1 && y + 1 < 8
 								&& (b[x - 1][y + 1].toString().charAt(1) != 'X' && b[x - 1][y + 1].getColor() != color))
 							d.add(new Point(x - 1, y + 1));
 					}
 
 					if (color == true) {
 						if (y == 6 && b[x][y - 2].toString().charAt(1) == 'X')
 							d.add(new Point(x, y - 2));
 						if (y - 1 > -1 && b[x][y - 1].toString().charAt(1) == 'X')
 							d.add(new Point(x, y - 1));
 						if (x + 1 < 8 && y - 1 > -1
 								&& (b[x + 1][y - 1].toString().charAt(1) != 'X' && b[x + 1][y - 1].getColor() != color))
 							d.add(new Point(x + 1, y - 1));
 						if (x - 1 > -1 && y - 1 > -1
 								&& (b[x - 1][y - 1].toString().charAt(1) != 'X' && b[x - 1][y - 1].getColor() != color))
 							d.add(new Point(x - 1, y - 1));
 					}
 					v.add(d);
 				}
 			}
 		}
 		return v;
 	}
 
 	// makes a random move
 	public void randomMove() {
 		if (pMoves().size() != 0) {
 			Random r = new Random();
 			int m = r.nextInt(pMoves().size());
 			while (pMoves().get(m).size() == 2) {
 				m = r.nextInt(pMoves().size());
 			}
 			Point st = new Point((Point) pMoves().get(m).get(1));
 			Point fn = new Point((Point) pMoves().get(m).get(2));
 			move((int) st.getX(), (int) st.getY(), (int) fn.getX(), (int) fn.getY());
 		} else if (nMoves().size() != 0) {
 			Random r = new Random();
 			int m = r.nextInt(nMoves().size());
 			while (nMoves().get(m).size() == 2) {
 				m = r.nextInt(nMoves().size());
 			}
 			Point st = new Point((Point) nMoves().get(m).get(1));
 			Point fn = new Point((Point) nMoves().get(m).get(2));
 			move((int) st.getX(), (int) st.getY(), (int) fn.getX(), (int) fn.getY());
 		} else if (bMoves().size() != 0) {
 			Random r = new Random();
 			int m = r.nextInt(bMoves().size());
 			while (bMoves().get(m).size() == 2) {
 				m = r.nextInt(bMoves().size());
 			}
 			Point st = new Point((Point) bMoves().get(m).get(1));
 			Point fn = new Point((Point) bMoves().get(m).get(2));
 			move((int) st.getX(), (int) st.getY(), (int) fn.getX(), (int) fn.getY());
 		} else if (rMoves().size() != 0) {
 			Random r = new Random();
 			int m = r.nextInt(rMoves().size());
 			while (rMoves().get(m).size() == 2) {
 				m = r.nextInt(rMoves().size());
 			}
 			Point st = new Point((Point) rMoves().get(m).get(1));
 			Point fn = new Point((Point) rMoves().get(m).get(2));
 			move((int) st.getX(), (int) st.getY(), (int) fn.getX(), (int) fn.getY());
 		} else if (qMoves().size() != 0) {
 			Random r = new Random();
 			int m = r.nextInt(qMoves().size());
 			while (qMoves().get(m).size() == 2) {
 				m = r.nextInt(qMoves().size());
 			}
 			Point st = new Point((Point) qMoves().get(m).get(1));
 			Point fn = new Point((Point) qMoves().get(m).get(2));
 			move((int) st.getX(), (int) st.getY(), (int) fn.getX(), (int) fn.getY());
 		} else if (kMoves().size() != 0) {
 			Random r = new Random();
 			int m = r.nextInt(kMoves().size());
 			while (kMoves().get(m).size() == 2) {
 				m = r.nextInt(kMoves().size());
 			}
 			Point st = new Point((Point) kMoves().get(m).get(1));
 			Point fn = new Point((Point) kMoves().get(m).get(2));
 			move((int) st.getX(), (int) st.getY(), (int) fn.getX(), (int) fn.getY());
 		}
 	}
 
 	public Piece[][] turn(Board c) {
 		getBoard(c);
 		Piece[][] arr = new Piece[8][8];
 		arr = getPieceArray();
 		move++;
 		int w, x, y, z;
		w=0;
		x=0;
		y=0;
		z=0;
 		if (color == true) {
 
 			switch (move) {
 				case 1:
 					w=4;x=6;y=4;z=4;
 				case 2:
 					w=3;x=7;y=5;z=5;
 
 				case 3:
 
 					if (arr[2][4].toString().charAt(1) == 'X' || arr[2][4].getColor() != color)
 					w=5;x=7;y=2;z=4;
 
 				case 4:
 					if (arr[5][3].toString().charAt(1) == 'X' && arr[5][4].toString().charAt(1) == 'X'
 							&& arr[5][5].toString().charAt(1) == 'X'
 							&& (arr[5][6].toString().charAt(1) == 'X' || arr[5][6].getColor() != color))
 					w=5;x=2;y=5;z=6;
 
 					return b;
 				default:
 					randomMove();
 			}
 		} else {
 
 			switch (move) {
 				case 1:
 					w=4;x=1;y=4;z=3;
 
 				case 2:
 					w=3;x=0;y=5;z=2;
 
 				case 3:
 					if (arr[2][3].toString().charAt(1) == 'X' || arr[2][3].getColor() != color)
 					w=5;x=0;y=2;z=3;
 
 				case 4:
 					if (arr[5][3].toString().charAt(1) == 'X' && arr[5][4].toString().charAt(1) == 'X'
 							&& arr[5][5].toString().charAt(1) == 'X'
 							&& (arr[5][6].toString().charAt(1) == 'X' || arr[5][6].getColor() != color))
 					w=5;x=2;y=5;z=6;
 
 				default:
 					randomMove();
 			}
 		}
 		move(w, x, y, z);
 		
 		System.out.println("Origin: ("+w+", "+x+") Destination:"+y+", "+z+")"+"Piece: "+b[w][x].toString());
 		return b;
 
 
 	}
 
 	public Piece[][] turn(Piece[][] c) {
 		getBoard(c);
 		Piece[][] arr = new Piece[8][8];
 		arr = getPieceArray();
 		move++;
 		if (color == true) {
 			switch (move) {
 				case 1:
 					move(4, 6, 4, 4);
 					return b;
 				case 2:
 					move(3, 7, 5, 5);
 					return b;
 				case 3:
 
 					if (arr[2][4].toString().charAt(1) == 'X' || arr[2][4].getColor() != color)
 						move(5, 7, 2, 4);
 					return b;
 				case 4:
 					if (arr[5][3].toString().charAt(1) == 'X' && arr[5][4].toString().charAt(1) == 'X'
 							&& arr[5][5].toString().charAt(1) == 'X'
 							&& (arr[5][6].toString().charAt(1) == 'X' || arr[5][6].getColor() != color))
 						move(5, 2, 5, 6);
 					return b;
 				default:
 					randomMove();
 					return b;
 
 			}
 		} else {
 
 			switch (move) {
 				case 1:
 					move(4, 1, 4, 3);
 					return b;
 				case 2:
 					move(3, 0, 5, 2);
 					return b;
 				case 3:
 					if (arr[2][3].toString().charAt(1) == 'X' || arr[2][3].getColor() != color)
 						move(5, 0, 2, 3);
 					return b;
 				case 4:
 					if (arr[5][3].toString().charAt(1) == 'X' && arr[5][4].toString().charAt(1) == 'X'
 							&& arr[5][5].toString().charAt(1) == 'X'
 							&& (arr[5][6].toString().charAt(1) == 'X' || arr[5][6].getColor() != color))
 						move(5, 2, 5, 6);
 					return b;
 				default:
 					randomMove();
 					return b;
 			}
 
 		}
 
 	}
 
 
 public static void main(String[] args)
 {
 	v1Bobby a=new v1Bobby(new Board(), true);
 	a.turn(new Board());
 }
 }
