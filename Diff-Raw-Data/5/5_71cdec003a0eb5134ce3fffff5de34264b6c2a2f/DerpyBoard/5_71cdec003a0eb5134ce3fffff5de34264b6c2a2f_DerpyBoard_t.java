 package DerpyAI;
 
 import java.awt.Point;
 
 import sharedfiles.Bishop;
 import sharedfiles.Blank;
 import sharedfiles.Board;
 import sharedfiles.King;
 import sharedfiles.Knight;
 import sharedfiles.Pawn;
 import sharedfiles.Piece;
 import sharedfiles.Queen;
 import sharedfiles.Rook;
 
 public class DerpyBoard {
 
 	protected DerpyPiece[][] arr;
 
 	public DerpyBoard() {
 		arr = new DerpyPiece[8][8];
 		for (int i = 0; i < 8; i++) {
 			for (int a = 2; a < 6; a++) {
 				arr[i][a] = new DerpyBlank(new Point(i, a));
 			}
 		}
 		for (int x = 0; x < 8; x++) {
 			arr[x][1] = new DerpyPawn(false, new Point(x, 1));
 			arr[x][6] = new DerpyPawn(true, new Point(x, 6));
 		}
 
 		// black setup
 		arr[0][0] = new DerpyRook(false, new Point(0, 0));
 		arr[7][0] = new DerpyRook(false, new Point(7, 0));
 
 		arr[1][0] = new DerpyKnight(false, new Point(1, 0));
 		arr[6][0] = new DerpyKnight(false, new Point(6, 0));
 
 		arr[2][0] = new DerpyBishop(false, new Point(2, 0));
 		arr[5][0] = new DerpyBishop(false, new Point(5, 0));
 
 		arr[3][0] = new DerpyQueen(false, new Point(3, 0));
 		arr[4][0] = new DerpyKing(false, new Point(4, 0));
 
 		// white setup
 		arr[0][7] = new DerpyRook(true, new Point(0, 7));
 		arr[7][7] = new DerpyRook(true, new Point(7, 7));
 
 		arr[1][7] = new DerpyKnight(true, new Point(1, 7));
 		arr[6][7] = new DerpyKnight(true, new Point(6, 7));
 
 		arr[2][7] = new DerpyBishop(true, new Point(2, 7));
 		arr[5][7] = new DerpyBishop(true, new Point(5, 7));
 
 		arr[3][7] = new DerpyQueen(true, new Point(3, 7));
 		arr[4][7] = new DerpyKing(true, new Point(4, 7));
 
 	}
 
 	public DerpyBoard(DerpyBoard b) {
 		this.arr = b.arr;
 	}
 
 	public Board boardEquiv() {
 
 		Board b = new Board();
 
 		for (int x = 0; x < 8; x++) {
 			for (int y = 0; y < 8; y++) {
 				DerpyPiece p = arr[x][y];
 				boolean pieceColor = p.getColor();
 				Piece db = null;
 				if (p instanceof DerpyBishop) {
 					db = new Bishop(pieceColor);
 				} else if (p instanceof DerpyBlank) {
 					db = new Blank(true);
 				} else if (p instanceof DerpyKing) {
 					db = new King(pieceColor);
 				} else if (p instanceof DerpyKnight) {
 					db = new Knight(pieceColor);
 				} else if (p instanceof DerpyPawn) {
 					db = new Pawn(pieceColor);
 				} else if (p instanceof DerpyQueen) {
 					db = new Queen(pieceColor);
 				} else if (p instanceof DerpyRook) {
 					db = new Rook(pieceColor);
 				} else {
 					System.out.println("boardEquiv did something dumb.");
 				}
 				b.getBoardArray()[x][y] = db;
 
 			}
 		}
 		return b;
 
 	}
 
 	public DerpyBoard(Board b) {
 
 		arr = new DerpyPiece[8][8];
 
 		Piece[][] pieces = b.getBoardArray();
 		for (int x = 0; x < 8; x++) {
 			for (int y = 0; y < 8; y++) {
 				Piece p = pieces[x][y];
 				boolean pieceColor = p.getColor();
 
 				DerpyPiece db = null;
 
 				if (p instanceof Bishop) {
 					db = new DerpyBishop(pieceColor, new Point(x, y));
 				} else if (p instanceof Blank) {
 					db = new DerpyBlank(new Point(x, y));
 				} else if (p instanceof King) {
 					db = new DerpyKing(pieceColor, new Point(x, y));
 				} else if (p instanceof Knight) {
 					db = new DerpyKnight(pieceColor, new Point(x, y));
 				} else if (p instanceof Pawn) {
 					db = new DerpyPawn(pieceColor, new Point(x, y));
 				} else if (p instanceof Queen) {
 					db = new DerpyQueen(pieceColor, new Point(x, y));
 				} else if (p instanceof Rook) {
 					db = new DerpyRook(pieceColor, new Point(x, y));
 				}
 				arr[x][y] = db;
 			}
 		}
 	}
 
 	public DerpyPiece[][] getBoardArray() {
 		return arr;
 	}

 	public void clearPieces() {

 		for (int x = 0; x < 8; x++) {
 			for (int y = 0; y < 8; y++) {
 				arr[x][y] = new DerpyBlank(new Point(x,y));
 			}
 		}
 	}
 
 	public void printBoard() {
 		for (int y = 0; y < 8; y++) {
 
 			for (int x = 0; x < 8; x++) {
 				System.out.print(arr[x][y].toString() + " | ");
 			}
 
 			System.out.println();
 		}
 	}
 
 	public void updateLocations() {
 
 		DerpyPiece newArr[][] = new DerpyPiece[8][8];
 
 		for (int i = 0; i < 8; i++) {
 			for (int a = 0; a < 8; a++) {
 				DerpyPiece piece = arr[i][a];
 				System.out.println("i: " + i + "...a: " + a);
 				Point location = piece.getLocation();
 				newArr[(int) location.getX()][(int) location.getY()] = piece;
 			}
 		}
 
 		arr = newArr;
 
 	}
 
 }
