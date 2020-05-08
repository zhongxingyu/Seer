 package chessboard;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.TreeMap;
 
 import javax.management.RuntimeErrorException;
 
 import exceptions.ChessboardException;
 import exceptions.UtilsException;
 import exec.Constants;
 import exec.MCTNode;
 import exec.Move;
 import exec.Print;
 import exec.Utils;
 
 public class Chessboard {
 
     private static final int	  NUMBER_OF_PREVIOUS_MOVES_WE_CHECK_FOR_REPEATED_STATE_PAT = 20;
 
     /*
      * board hrani stevilko figure, ki je na doloceni lokaciji
      */
     private int[]		     board;
     /*
      * piecePosition shrani lokacijo vsake figure na plosci
      */
     private int[]		     piecePosition;
 
     private boolean		   isWhitesTurn					     = true;
     private int		       numberOfMovesMade					= 0;
     private int		       maxNumberOfMoves					 = Constants.MAX_DEPTH;
    private ArrayList<Move>	   previousMoves					    = new ArrayList<Move>();
     /**
      * This is used for keeping track how many times some chess board state has
      * occurred. Key is hash code of chess board and values is how may times
      * that has has occurred.
      */
     private HashMap<Integer, Integer> numberOfTimesBoardStateHasOccured			= new HashMap<Integer, Integer>(
 												       Chessboard.NUMBER_OF_PREVIOUS_MOVES_WE_CHECK_FOR_REPEATED_STATE_PAT + 1);
 
     private String		    name;
     private boolean		   wasBoardStateRepeatedThreeTimes			  = false;
     private ArrayList<Integer>	previousHashes					   = new ArrayList<Integer>();
 
 
     public Chessboard(String name) {
 	this.name = name;
 	this.isWhitesTurn = true;
 	this.numberOfMovesMade = 0;
 	this.maxNumberOfMoves = 100;
 
 	this.board = new int[128];
 	for (int x = 0; x < 128; x++) {
 	    this.board[x] = -1;
 	}
 
 	// //////////////////////////////
 	// tukaj pride zacetna posatvitev
 	// /////////////////////////////
 
 	if (Constants.ENDING.equalsIgnoreCase("KRK")) {
 	    this.board[0] = 0;
 	    this.board[4] = 4;
 	    this.board[67] = 28;
 	}
 	else if (Constants.ENDING.equalsIgnoreCase("KQK")) {
 	    this.board[0] = 3;
 	    this.board[4] = 4;
 	    this.board[67] = 28;
 	}
 	else if (Constants.ENDING.equalsIgnoreCase("KRRK")) {
 	    this.board[0] = 0;
 	    this.board[4] = 4;
 	    this.board[7] = 7;
 	    this.board[67] = 28;
 	}
 	else if (Constants.ENDING.equalsIgnoreCase("KBBK")) {
 	    this.board[2] = 2;
 	    this.board[4] = 4;
 	    this.board[5] = 5;
 	    this.board[67] = 28;
 	}
 
 	// /////////////////////////////
 	// konec zacetne postavitve//
 	// ////////////////////////////
 	this.constructPiecePositionFromBoard();
 
     }
 
 
     /**
      * Constructor which transforms MCTNode into chessboard
      * 
      * @param name
      *            name which chess board will have
      * @param node
      *            node from which we get chess board state
      */
     public Chessboard(String name, MCTNode node) {
 	this.name = name;
 	this.numberOfMovesMade = node.moveDepth;
 	this.maxNumberOfMoves = 100;
 
 	this.board = new int[128];
 	for (int x = 0; x < 128; x++) {
 	    this.board[x] = -1;
 	}
 
 	this.board = node.boardState.clone();
 	this.isWhitesTurn = node.isWhitesMove;
 
 	this.constructPiecePositionFromBoard();
 
     }
 
 
     public Chessboard(Chessboard cb, String name) {
 	this.name = name;
 	this.isWhitesTurn = cb.getIsWhitesTurn();
 	this.board = cb.getBoard();
 	this.constructPiecePositionFromBoard();
 	this.numberOfMovesMade = cb.getNumberOfMovesMade();
 	this.maxNumberOfMoves = 100;
 
     }
 
 
     public Chessboard(String name, TreeMap<Integer, Integer> startingPosition) {
 	this.name = name;
 	this.board = new int[128];
 	// we empty entire board
 	for (int x = 0; x < 128; x++) {
 	    this.board[x] = -1;
 	}
 
 	// we set those board according to map
 	for (int position : startingPosition.keySet()) {
 	    this.board[position] = startingPosition.get(position);
 	}
 
 	// we update piecesPosition
 	this.constructPiecePositionFromBoard();
 
     }
 
 
     /* ************************************************************************
      * ***************************JAVNE FUNKCIJE*******************************
      */
 
     /**
      * @return fen representation of board
      * @throws UtilsException
      */
     public String boardToFen() throws UtilsException {
 	StringBuffer sb = new StringBuffer();
 	for (int x = 112; x >= 0; x = x - 16) {
 	    int counter = 0;
 	    for (int y = 0; y < 8; y++) {
 		int piece = this.board[x + y];
 
 		if (piece == -1) {
 		    counter++;
 		    if (y == 7) {
 			sb.append(counter);
 		    }
 		}
 		else {
 		    if (counter == 0) {
 			sb.append(Utils.pieceToCharFEN(piece));
 		    }
 		    else {
 			sb.append(counter + "" + Utils.pieceToCharFEN(piece));
 			counter = 0;
 		    }
 		}
 	    }
 	    if (x > 0) {
 		sb.append("/");
 		counter = 0;
 	    }
 	    else {
 		if (this.isWhitesTurn) {
 		    sb.append(" w - - 0 " + (this.numberOfMovesMade / 2));
 		}
 		else {
 		    sb.append(" b - - 0 " + (this.numberOfMovesMade / 2));
 		}
 	    }
 	}
 
 	return sb.toString();
     }
 
 
     public void printChessboard() throws UtilsException {
 	for (int x = 0; x < 24; x++) {
 	    System.out.print("*");
 	}
 	System.out.println();
 
 	Print.print("board: ");
 	Utils.printBoardArray(this.board);
 	Print.print("piecePosition: ");
 	Utils.printIntArray(this.piecePosition);
 
 	if (this.isWhitesTurn) {
 	    System.out.println("beli je na potezi na plo��i " + this.name);
 	}
 	else {
 	    System.out.println("�rni je na potezi na " + this.name);
 	}
 
 	for (int x = 0; x < 24; x++) {
 	    System.out.print("*");
 	}
 	System.out.println("*");
 	for (int x = 7; x >= 0; x--) {
 	    int baza = x * 16;
 	    for (int y = 0; y < 8; y++) {
 		int t = this.board[baza + y];
 		if (t == -1) {
 		    System.out.print("*00");
 		}
 		else {
 		    System.out.print("*" + Utils.pieceNumberToString(t));
 		}
 	    }
 	    System.out.println("*");
 	}
 	for (int x = 0; x < 24; x++) {
 	    System.out.print("*");
 	}
 	System.out.println("*");
     }
 
 
     @Override
     public String toString() {
 	StringBuffer sb = new StringBuffer(50);
 	for (int x = 0; x < 24; x++) {
 	    sb.append("*");
 	}
 	sb.append("\n");
 
 	if (this.isWhitesTurn) {
 	    sb.append("Beli je na potezi na plosci " + this.name + "\n");
 	}
 	else {
 	    sb.append("crni je na potezi na plosci " + this.name + "\n");
 	}
 
 	for (int x = 0; x < 24; x++) {
 	    sb.append("*");
 	}
 
 	sb.append("*\n");
 	for (int x = 7; x >= 0; x--) {
 	    int baza = x * 16;
 	    for (int y = 0; y < 8; y++) {
 		int t = this.board[baza + y];
 		if (t == -1) {
 		    sb.append("*00");
 		}
 		else {
 		    try {
 			sb.append("*" + Utils.pieceNumberToString(t));
 		    }
 		    catch (UtilsException e) {
 			throw new RuntimeErrorException(new Error(e));
 		    }
 		}
 	    }
 	    sb.append("*\n");
 	}
 	for (int x = 0; x < 24; x++) {
 	    sb.append("*");
 	}
 	sb.append("*\n");
 
 	return sb.toString();
     }
 
 
     /**
      * @return int[] ki predstavlja plosco v X88 obliki
      */
     public int[] getBoard() {
 	return this.board.clone();
     }
 
 
     /**
      * @return polje int[] v katerem je v x elementu shranjena pozicija za
      *         figuro x
      */
     public int[] getPiecesPosition() {
 	return this.piecePosition.clone();
     }
 
 
     public boolean getIsWhitesTurn() {
 	return this.isWhitesTurn;
     }
 
 
     public int getNumberOfMovesMade() {
 	return this.numberOfMovesMade;
     }
 
 
     /*
      * zgradi �tevilko poteze vhod je oblike xnxn (naprimer a2a5 - premik iz
      * a2 na a5)
      */
     public int constructMoveNumberFromString(String move) throws UtilsException {
 	String fromS = move.substring(0, 2);
 	String toS = move.substring(2);
 	int from = Utils.positionFromString(fromS);
 	int to = Utils.positionFromString(toS);
 	return this.constructMoveNumber(from, to);
     }
 
 
     /**
      * ce je poteza mozna naredi potezo
      * 
      * @param from
      *            pozicija iz katere premikamo
      * @param to
      *            pozicija na katero premikamo
      * @return true ce je bila poteza narejena, drugace vrne false
      */
     public boolean makeWhitePawnMove(int from, int to)
 	    throws ChessboardException {
 	int piece = this.board[from];
 
 	// if(DEBUG) println("Za�etek makeWhitePawnMove(int from, int to)");
 	// if(DEBUG) println("from: " + from + "\tto: " + to + "\tpiece: " +
 	// piece);
 
 	if (piece < 8 || piece > 15)
 	    throw new ChessboardException("na from je figura" + piece);
 	if (!this.isWhitePawnMoveLegal(from, to)) {
 	    // if(DEBUG) println("Poteza ni dovoljena, vra�am false");
 	    // if(DEBUG) println("Konec makeWhitePawnMove(int from, int to)");
 
 	    return false;
 	}
 
 	int targetPiece = this.board[to];
 	if (targetPiece != -1) {
 	    this.piecePosition[targetPiece] = -1;
 	}
 
 	this.board[from] = -1;
 	this.board[to] = piece;
 	this.piecePosition[piece] = to;
 	this.numberOfMovesMade++;
 
 	return true;
     }
 
 
     /**
      * ce je poteza mozna naredi potezo
      * 
      * @param from
      *            pozicija iz katere premikamo
      * @param to
      *            pozicija na katero premikamo
      * @return true ce je bila poteza narejena, drugace vrne false
      */
     public boolean makeBlackPawnMove(int from, int to)
 	    throws ChessboardException {
 	int piece = this.board[from];
 
 	// if(DEBUG) println("Za�etek makeBlackPawnMove(int from, int to)");
 	// if(DEBUG) println("from: " + from + "\tto: " + to + "\tpiece: " +
 	// piece);
 
 	if (piece < 16 || piece > 23)
 	    throw new ChessboardException("na from je figura" + piece);
 	if (!this.isBlackPawnMoveLegal(from, to)) {
 	    // if(DEBUG) println("Poteza ni dovoljena, vra�am false");
 	    // if(DEBUG) println("Konec makeBlackPawnMove(int from, int to)");
 
 	    return false;
 	}
 
 	int targetPiece = this.board[to];
 	if (targetPiece != -1) {
 	    this.piecePosition[targetPiece] = -1;
 	}
 
 	this.board[from] = -1;
 	this.board[to] = piece;
 	this.piecePosition[piece] = to;
 	this.numberOfMovesMade++;
 
 	// if(DEBUG) println("Konec makeBlackPawnMove(int from, int to)");
 
 	return true;
     }
 
 
     /**
      * ce je poteza mozna naredi potezo
      * 
      * @param from
      *            pozicija iz katere premikamo
      * @param to
      *            pozicija na katero premikamo
      * @return true ce je bila poteza narejena, drugace vrne false
      */
     public boolean makeWhiteRookMove(int from, int to)
 	    throws ChessboardException {
 	int piece = this.board[from];
 
 	// if(DEBUG) println("Za�etek makeWhiteRookMove(int from, int to)");
 	// if(DEBUG) println("from: " + from + "\tto: " + to + "\tpiece: " +
 	// piece);
 
 	if (piece != 0 && piece != 7)
 	    throw new ChessboardException("na from je figura " + piece);
 	if (!this.isWhiteRookMoveLegal(from, to)) {
 	    // if(DEBUG) println("Poteza ni dovoljena, vra�am false");
 	    // if(DEBUG) println("Konec makeWhiteRookMove(int from, int to)");
 
 	    return false;
 	}
 
 	int targetPiece = this.board[to];
 	if (targetPiece != -1) {
 	    this.piecePosition[targetPiece] = -1;
 	}
 
 	this.board[from] = -1;
 	this.board[to] = piece;
 	this.piecePosition[piece] = to;
 	this.numberOfMovesMade++;
 
 	// if(DEBUG) println("Konec makwWhiteRookMove(int from, int to)");
 
 	return true;
     }
 
 
     /**
      * ce je poteza mozna naredi potezo
      * 
      * @param from
      *            pozicija iz katere premikamo
      * @param to
      *            pozicija na katero premikamo
      * @return true ce je bila poteza narejena, drugace vrne false
      */
     public boolean makeBlackRookMove(int from, int to)
 	    throws ChessboardException {
 	int piece = this.board[from];
 
 	// if(DEBUG) println("Za�etek makeBlackRookMove(int from, int to)");
 	// if(DEBUG) println("from: " + from + "\tto: " + to + "\tpiece: " +
 	// piece);
 
 	if (piece != 24 && piece != 31)
 	    throw new ChessboardException("na from je figura " + piece);
 	if (!this.isBlackRookMoveLegal(from, to)) {
 	    // if(DEBUG) println("Poteza ni dovoljena, vra�am false");
 	    // if(DEBUG) println("Konec makeBlackRookMove(int from, int to)");
 
 	    return false;
 	}
 
 	int targetPiece = this.board[to];
 	if (targetPiece != -1) {
 	    this.piecePosition[targetPiece] = -1;
 	}
 
 	this.board[from] = -1;
 	this.board[to] = piece;
 	this.piecePosition[piece] = to;
 	this.numberOfMovesMade++;
 
 	// if(DEBUG) println("Konec makeBlackRookMove(int from, int to)");
 
 	return true;
     }
 
 
     /**
      * ce je poteza mozna naredi potezo
      * 
      * @param from
      *            pozicija iz katere premikamo
      * @param to
      *            pozicija na katero premikamo
      * @return true ce je bila poteza narejena, drugace vrne false
      */
     public boolean makeWhiteKnightMove(int from, int to)
 	    throws ChessboardException {
 	int piece = this.board[from];
 
 	// if(DEBUG) println("Za�etek makeWhiteKnightMove(int from, int to)");
 	// if(DEBUG) println("from: " + from + "\tto: " + to + "\tpiece: " +
 	// piece);
 
 	if (piece != 1 && piece != 6)
 	    throw new ChessboardException("na from je figura " + piece);
 	if (!this.isWhiteKnightMoveLegal(from, to)) {
 	    // if(DEBUG) println("Poteza ni dovoljena, vra�am false");
 	    // if(DEBUG) println("Konec makwWhiteKnightMove(int from, int to)");
 
 	    return false;
 	}
 
 	int targetPiece = this.board[to];
 	if (targetPiece != -1) {
 	    this.piecePosition[targetPiece] = -1;
 	}
 
 	this.board[from] = -1;
 	this.board[to] = piece;
 	this.piecePosition[piece] = to;
 	this.numberOfMovesMade++;
 
 	// if(DEBUG) println("Konec makeWhiteKnightMove(int from, int to)");
 
 	return true;
     }
 
 
     /**
      * ce je poteza mozna naredi potezo
      * 
      * @param from
      *            pozicija iz katere premikamo
      * @param to
      *            pozicija na katero premikamo
      * @return true ce je bila poteza narejena, drugace vrne false
      */
     public boolean makeBlackKnightMove(int from, int to)
 	    throws ChessboardException {
 	int piece = this.board[from];
 
 	// if(DEBUG) println("Za�etek makeBlackKnightMove(int from, int to)");
 	// if(DEBUG) println("from: " + from + "\tto: " + to + "\tpiece: " +
 	// piece);
 
 	if (piece != 25 && piece != 30)
 	    throw new ChessboardException("na from je figura " + piece);
 	if (!this.isBlackKnightMoveLegal(from, to)) {
 	    // if(DEBUG) println("Poteza ni dovoljena, vra�am false");
 	    // if(DEBUG) println("Konec makeBlackKnightMove(int from, int to)");
 	    return false;
 	}
 
 	int targetPiece = this.board[to];
 	if (targetPiece != -1) {
 	    this.piecePosition[targetPiece] = -1;
 	}
 
 	this.board[from] = -1;
 	this.board[to] = piece;
 	this.piecePosition[piece] = to;
 	this.numberOfMovesMade++;
 
 	// if(DEBUG) println("Konec makeBlackKnightMove(int from, int to");
 
 	return true;
     }
 
 
     /**
      * ce je poteza mozna naredi potezo
      * 
      * @param from
      *            pozicija iz katere premikamo
      * @param to
      *            pozicija na katero premikamo
      * @return true ce je bila poteza narejena, drugace vrne false
      */
     public boolean makeWhiteBishopMove(int from, int to)
 	    throws ChessboardException {
 	int piece = this.board[from];
 
 	// if(DEBUG) println("Za�etek makeWhiteBishopMove(int from, int to)");
 	// if(DEBUG) println("from: " + from + "\tto: " + to + "\tpiece: " +
 	// piece);
 
 	if (piece != 2 && piece != 5)
 	    throw new ChessboardException("na from je figura " + piece);
 	if (!this.isWhiteBishopMoveLegal(from, to)) {
 	    // if(DEBUG) println("Poteza ni dovoljena, vra�am false");
 	    // if(DEBUG) println("Konec makeWhiteBishopMove(int from, int to)");
 
 	    return false;
 	}
 
 	int targetPiece = this.board[to];
 	if (targetPiece != -1) {
 	    this.piecePosition[targetPiece] = -1;
 	}
 
 	this.board[from] = -1;
 	this.board[to] = piece;
 	this.piecePosition[piece] = to;
 	this.numberOfMovesMade++;
 
 	// if(DEBUG) println("Konec makeWhiteBishopMove(int from, int to)");
 
 	return true;
     }
 
 
     /**
      * ce je poteza mozna naredi potezo
      * 
      * @param from
      *            pozicija iz katere premikamo
      * @param to
      *            pozicija na katero premikamo
      * @return true ce je bila poteza narejena, drugace vrne false
      */
     public boolean makeBlackBishopMove(int from, int to)
 	    throws ChessboardException {
 
 	int piece = this.board[from];
 	int targetPiece = this.board[to];
 
 	// if(DEBUG) println("Za�etek makeBlackBishopMove(int from, int to)");
 	// if(DEBUG) println("from: " + from + "\tto: " + to + "\tpiece: " +
 	// piece);
 
 	if (piece != 26 && piece != 29)
 	    throw new ChessboardException("na from je figura " + piece);
 
 	if (!this.isBlackBishopMoveLegal(from, to)) {
 	    // if(DEBUG) println("Poteza ni dovoljena, vra�am false;");
 	    // if(DEBUG) println("Konec makeBlackBishopMove(int from, int to)");
 
 	    return false;
 	}
 
 	this.board[from] = -1;
 	this.board[to] = piece;
 	this.piecePosition[piece] = to;
 	this.numberOfMovesMade++;
 
 	if (targetPiece != -1) {
 	    this.piecePosition[targetPiece] = -1;
 	}
 
 	// if(DEBUG) println("Konec makeBlackBishopMove(int from, int to)");
 
 	return true;
     }
 
 
     /**
      * ce je poteza mozna naredi potezo
      * 
      * @param from
      *            pozicija iz katere premikamo
      * @param to
      *            pozicija na katero premikamo
      * @return true ce je bila poteza narejena, drugace vrne false
      */
     public boolean makeWhiteQueenMove(int from, int to)
 	    throws ChessboardException {
 	int piece = this.board[from];
 
 	// if(DEBUG) println("Za�etek makeWhiteQueenMove(int from, int to)");
 	// if(DEBUG) println("from: " + from + "\tto: " + to + "\tpiece: " +
 	// piece);
 
 	if (piece != 3)
 	    throw new ChessboardException("na from je figura " + piece);
 
 	if (!this.isWhiteQueenMoveLegal(from, to)) {
 	    // if(DEBUG) println("Poteza ni dovoljena, vra�am false");
 	    // if(DEBUG) println("Konec makeWhiteQueenMove(int from, int to)");
 
 	    return false;
 	}
 
 	int targetPiece = this.board[to];
 	if (targetPiece != -1) {
 	    this.piecePosition[targetPiece] = -1;
 	}
 
 	this.board[from] = -1;
 	this.board[to] = piece;
 	this.piecePosition[piece] = to;
 	this.numberOfMovesMade++;
 
 	// if(DEBUG) println("Konec makeWhiteQueenMove(int from, int to)");
 
 	return true;
     }
 
 
     /**
      * ce je poteza mozna naredi potezo
      * 
      * @param from
      *            pozicija iz katere premikamo
      * @param to
      *            pozicija na katero premikamo
      * @return true ce je bila poteza narejena, drugace vrne false
      */
     public boolean makeBlackQueenMove(int from, int to)
 	    throws ChessboardException {
 	int piece = this.board[from];
 
 	// if(DEBUG) println("Za�etek makeBlackQueenMove(int from, int to)");
 	// if(DEBUG) println("from: " + from + "\tto: " + to + "\tpiece: " +
 	// piece);
 
 	if (piece != 27)
 	    throw new ChessboardException("na from je figura " + piece);
 	if (!this.isBlackQueenMoveLegal(from, to)) {
 	    // if(DEBUG) println("Poteza ni dovoljena, vra�am false");
 	    // if(DEBUG) println("Konec makeBlackQueenMove(int from, int to)");
 
 	    return false;
 	}
 
 	int targetPiece = this.board[to];
 	if (targetPiece != -1) {
 	    this.piecePosition[targetPiece] = -1;
 	}
 
 	this.board[from] = -1;
 	this.board[to] = piece;
 	this.piecePosition[piece] = to;
 	this.numberOfMovesMade++;
 
 	// if(DEBUG) println("Konec makeBlackQueenMove(int from, int to)");
 
 	return true;
     }
 
 
     /**
      * ce je poteza mozna naredi potezo
      * 
      * @param from
      *            pozicija iz katere premikamo
      * @param to
      *            pozicija na katero premikamo
      * @return true ce je bila poteza narejena, drugace vrne false
      */
     public boolean makeWhiteKingMove(int from, int to)
 	    throws ChessboardException {
 	int piece = this.board[from];
 
 	// if(DEBUG) println("Za�etek makeWhiteKingMove(int from, int to)");
 	// if(DEBUG) println("from: " + from + "\tto: " + to + "\tpiece: " +
 	// piece);
 
 	if (piece != 4)
 	    throw new ChessboardException("na from je figura " + piece);
 	if (!this.isWhiteKingMoveLegal(from, to)) {
 	    // if(DEBUG) println("Poteza ni dovoljena, vra�am false");
 	    // if(DEBUG) println("Konec makeWhiteKingMove(int from, int to)");
 
 	    return false;
 	}
 
 	int targetPiece = this.board[to];
 	if (targetPiece != -1) {
 	    this.piecePosition[targetPiece] = -1;
 	}
 
 	this.board[from] = -1;
 	this.board[to] = piece;
 	this.piecePosition[piece] = to;
 	this.numberOfMovesMade++;
 
 	// if(DEBUG) println("Konec makeWhiteKingMove(int from, int to)");
 
 	return true;
     }
 
 
     /**
      * ce je poteza mozna naredi potezo
      * 
      * @param from
      *            pozicija iz katere premikamo
      * @param to
      *            pozicija na katero premikamo
      * @return true ce je bila poteza narejena, drugace vrne false
      */
     public boolean makeBlackKingMove(int from, int to)
 	    throws ChessboardException {
 	int piece = this.board[from];
 
 	// if(DEBUG) println("Za�etek makeBlackKingMove(int from, int to)");
 	// if(DEBUG) println("from: " + from + "\tto: " + to + "\tpiece: " +
 	// piece);
 
 	if (piece != 28)
 	    throw new ChessboardException("na from je figura " + piece);
 	if (!this.isBlackKingMoveLegal(from, to)) {
 	    // if(DEBUG) println("Poteza ni dovoljena, vra�am false");
 	    // if(DEBUG) println("Konec makeBlackKingMove(int from, int to)");
 
 	    return false;
 	}
 
 	int targetPiece = this.board[to];
 	if (targetPiece != -1) {
 	    this.piecePosition[targetPiece] = -1;
 	}
 
 	this.board[from] = -1;
 	this.board[to] = piece;
 	this.piecePosition[piece] = to;
 	this.numberOfMovesMade++;
 
 	return true;
     }
 
 
     /**
      * This move is only used from Chessboard.makeAMove(int). It moves poiece
      * from <code>from</code> position to <code>to</code> position. It also
      * fills numberOfTimesBoardStateHasOccured.
      * 
      * @param from
      *            pozicija iz katere premikamo
      * @param to
      *            pozicija na katero premikamo
      * @return true ce je bila poteza narejena, drugace vrne false
      */
     @Deprecated
     public boolean makeAMove(int from, int to) throws ChessboardException {
 	this.isWhitesTurn = !this.isWhitesTurn;
 	int piece = this.board[from];
 
 	int hash = this.hashCode();
 	this.previousHashes.add(hash);
 
 	// for optimization purpouses we litmit number od states that we check
 	if (this.previousHashes.size() > Chessboard.NUMBER_OF_PREVIOUS_MOVES_WE_CHECK_FOR_REPEATED_STATE_PAT) {
 	    this.numberOfTimesBoardStateHasOccured.remove(this.previousHashes
 		    .get(0));
 	    this.previousHashes.remove(0);
 	}
 
 	if (this.numberOfTimesBoardStateHasOccured.containsKey(hash)) {
 	    // we increase number of times that state has appeared.
 	    int stateAppeared = this.numberOfTimesBoardStateHasOccured
 		    .get(hash) + 1;
 	    this.numberOfTimesBoardStateHasOccured.put(hash, stateAppeared);
 
 	    if (stateAppeared > 2) {
 		this.wasBoardStateRepeatedThreeTimes = true;
 	    }
 	}
 	else {
 	    this.numberOfTimesBoardStateHasOccured.put(hash, 1);
 	}
 
 	if (piece == 0 || piece == 7) { return this.makeWhiteRookMove(from, to); }
 	if (piece == 1 || piece == 6) { return this.makeWhiteKnightMove(from,
 		to); }
 	if (piece == 2 || piece == 5) { return this.makeWhiteBishopMove(from,
 		to); }
 	if (piece == 3) { return this.makeWhiteQueenMove(from, to); }
 	if (piece == 4) { return this.makeWhiteKingMove(from, to); }
 	if (piece > 7 && piece < 16) { return this.makeWhitePawnMove(from, to); }
 	if (piece > 15 && piece < 24) { return this.makeBlackPawnMove(from, to); }
 	if (piece == 24 || piece == 31) { return this.makeBlackRookMove(from,
 		to); }
 	if (piece == 25 || piece == 30) { return this.makeBlackKnightMove(from,
 		to); }
 	if (piece == 26 || piece == 29) { return this.makeBlackBishopMove(from,
 		to); }
 	if (piece == 27) { return this.makeBlackQueenMove(from, to); }
 	if (piece == 28) { return this.makeBlackKingMove(from, to); }
 
 	throw new ChessboardException();
     }
 
 
     /**
      * ce je poteza mozna naredi potezo, preveri tudi �e je �tevilka poteze
      * pravilna, druga�e
      * 
      * @param moveNumber
      *            - stevilka poteze, ki jo hocemo narediti
      * @throws ChessboardException
      * @throws UtilsException
      */
     public boolean makeAMove(int moveNumber) throws ChessboardException {
 	if (moveNumber == -1) { throw new ChessboardException(
 		"neveljavna poteza"); }
 
	Move move = new Move(moveNumber);
	this.previousMoves.add(0, move);

 	int from = Utils.getFromFromMoveNumber(moveNumber);
 	int to = Utils.getToFromMoveNumber(moveNumber);
 	int movedPiece = Utils.getMovedPieceFromMoveNumber(moveNumber);
 	int targetPiece = Utils.getTargetPieceFromMoveNumber(moveNumber);
 
 	if (this.board[from] != movedPiece
 		|| this.piecePosition[movedPiece] != from) { throw new ChessboardException(
 		"na mestu " + from + "ni figura " + movedPiece); }
 	if (targetPiece != -1
 		&& (this.board[to] != targetPiece || this.piecePosition[targetPiece] != to)) {
 	    throw new ChessboardException("na mestu " + to + " ni figura "
 		    + targetPiece + ", ampak je " + this.board[to]);
 	}
 	else if (targetPiece == -1 && this.board[to] != -1) { throw new ChessboardException(
 		"Pozijia to: " + to + " ni prazna"); }
 
 	return this.makeAMove(from, to);
     }
 
 
     /**
      * generira vse mozne poteze za vse bele figure
      * 
      * @return ArrayList<Move> vseh moznih belih potez
      * @throws ChessboardException
      */
     public ArrayList<Move> getAllLegalWhiteMoves() throws ChessboardException {
 
 	ArrayList<Move> rez = new ArrayList<Move>();
 	for (int x = 0; x < 16; x++) {
 	    int from = this.piecePosition[x];
 
 	    if (from != -1) {
 		if (!Chessboard.isPositionLegal(from))
 		    throw new ChessboardException("figura " + x
 			    + " je na poziciji " + from);
 		for (int y = 0; y < 128; y++) {
 		    if (this.isWhiteMoveLegal(from, y)) {
 			int t = this.constructMoveNumber(from, y);
 			rez.add(new Move(t));
 		    }
 		}
 	    }
 	}
 
 	return rez;
     }
 
 
     /**
      * generira vse mozne poteze za vse crne figure
      * 
      * @return ArrayList<Move> vseh moznih crnih potez
      * @throws ChessboardException
      */
     public ArrayList<Move> getAllLegalBlackMoves() throws ChessboardException {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	for (int x = 16; x < 32; x++) {
 	    int from = this.piecePosition[x];
 	    if (from != -1) {
 		if (!Chessboard.isPositionLegal(from))
 		    throw new ChessboardException("figura " + x
 			    + " je na poziciji " + from);
 		for (int y = 0; y < 128; y++) {
 		    if (this.isBlackMoveLegal(from, y)) {
 			int t = this.constructMoveNumber(from, y);
 			rez.add(new Move(t));
 		    }
 		}
 	    }
 	}
 
 	return rez;
     }
 
 
     /**
      * generira vse mozne poteze
      * 
      * @return ArrayList<Move> vseh moznih potez belih kmetov
      * @throws ChessboardException
      */
     public ArrayList<Move> getAllLegalWhitePawnMoves()
 	    throws ChessboardException {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	for (int x = 8; x < 16; x++) {
 	    int from = this.piecePosition[x];
 	    if (from != -1) {
 		if (!Chessboard.isPositionLegal(from))
 		    throw new ChessboardException("figura " + x
 			    + " je na poziciji " + from);
 		for (int y = 0; y < 128; y++) {
 		    if (this.isWhitePawnMoveLegal(from, y)) {
 			int t = this.constructMoveNumber(from, y);
 			rez.add(new Move(t));
 		    }
 		}
 	    }
 	}
 	return rez;
     }
 
 
     /**
      * generira vse mozne poteze crnih kmetov
      * 
      * @return ArrayList<Move> vseh moznih potez crnih kmetov
      * @throws ChessboardException
      */
     public ArrayList<Move> getAllLegalBlackPawnMoves()
 	    throws ChessboardException {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	for (int x = 16; x < 24; x++) {
 	    int from = this.piecePosition[x];
 	    if (from != -1) {
 		if (!Chessboard.isPositionLegal(from))
 		    throw new ChessboardException("figura " + x
 			    + " je na poziciji " + from);
 		for (int y = 0; y < 128; y++) {
 		    if (this.isBlackPawnMoveLegal(from, y)) {
 			int t = this.constructMoveNumber(from, y);
 			rez.add(new Move(t));
 		    }
 		}
 	    }
 	}
 	return rez;
     }
 
 
     /**
      * generira vse mozne poteze belih trdnjav
      * 
      * @return ArrayList<Move> vseh moznih potez belih trdnjav
      * @throws ChessboardException
      */
     public ArrayList<Move> getAllLegalWhiteRookMoves()
 	    throws ChessboardException {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	int[] rooks = { 0, 7 };
 
 	for (int x : rooks) {
 	    int from = this.piecePosition[x];
 	    if (from != -1) {
 		if (!Chessboard.isPositionLegal(from))
 		    throw new ChessboardException("figura " + x
 			    + " je na poziciji " + from);
 		for (int y = 0; y < 128; y++) {
 		    if (this.isWhiteRookMoveLegal(from, y)) {
 			int t = this.constructMoveNumber(from, y);
 			rez.add(new Move(t));
 		    }
 		}
 	    }
 	}
 	return rez;
     }
 
 
     /**
      * generira vse mozne poteze crnih trdnjav
      * 
      * @return ArrayList<Move> vseh moznih potez crnih trdnjav
      * @throws ChessboardException
      */
     public ArrayList<Move> getAllLegalBlackRookMoves()
 	    throws ChessboardException {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	int[] rooks = { 24, 31 };
 
 	for (int x : rooks) {
 	    int from = this.piecePosition[x];
 	    if (from != -1) {
 		if (!Chessboard.isPositionLegal(from))
 		    throw new ChessboardException("figura " + x
 			    + " je na poziciji " + from);
 		for (int y = 0; y < 128; y++) {
 		    if (this.isBlackRookMoveLegal(from, y)) {
 			int t = this.constructMoveNumber(from, y);
 			rez.add(new Move(t));
 		    }
 		}
 	    }
 	}
 	return rez;
     }
 
 
     /**
      * generira vse mozne poteze belih konjev
      * 
      * @return ArrayList<Move> vseh moznih potez belih konjev
      * @throws ChessboardException
      */
     public ArrayList<Move> getAllLegalWhiteKnightMoves()
 	    throws ChessboardException {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	int[] knights = { 1, 6 };
 
 	for (int x : knights) {
 	    int from = this.piecePosition[x];
 	    if (from != -1) {
 		if (!Chessboard.isPositionLegal(from))
 		    throw new ChessboardException("figura " + x
 			    + " je na poziciji " + from);
 
 		for (int y = 0; y < 128; y++) {
 		    if (this.isWhiteKnightMoveLegal(from, y)) {
 			int t = this.constructMoveNumber(from, y);
 			rez.add(new Move(t));
 		    }
 		}
 	    }
 	}
 	return rez;
     }
 
 
     /**
      * generira vse mozne poteze crnih konjev
      * 
      * @return ArrayList<Move> vseh moznih potez crnih konjev
      * @throws ChessboardException
      */
     public ArrayList<Move> getAllLegalBlackKnightMoves()
 	    throws ChessboardException {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	int[] knights = { 25, 30 };
 
 	for (int x : knights) {
 	    int from = this.piecePosition[x];
 	    if (from != -1) {
 		if (!Chessboard.isPositionLegal(from))
 		    throw new ChessboardException("figura " + x
 			    + " je na poziciji " + from);
 
 		for (int y = 0; y < 128; y++) {
 		    if (this.isBlackKnightMoveLegal(from, y)) {
 			int t = this.constructMoveNumber(from, y);
 			rez.add(new Move(t));
 		    }
 		}
 	    }
 	}
 	return rez;
     }
 
 
     /**
      * generira vse mozne poteze belih tekacev
      * 
      * @return ArrayList<Move> vseh moznih potez belih tekacev
      * @throws ChessboardException
      */
     public ArrayList<Move> getAllLegalWhiteBishopMoves()
 	    throws ChessboardException {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	int[] bishops = { 2, 5 };
 
 	for (int x : bishops) {
 	    int from = this.piecePosition[x];
 	    if (from != -1) {
 		if (!Chessboard.isPositionLegal(from))
 		    throw new ChessboardException("figura " + x
 			    + " je na poziciji " + from);
 
 		for (int y = 0; y < 128; y++) {
 		    if (this.isWhiteBishopMoveLegal(from, y)) {
 			int t = this.constructMoveNumber(from, y);
 			rez.add(new Move(t));
 		    }
 		}
 	    }
 	}
 	return rez;
     }
 
 
     /**
      * generira vse mozne poteze crnih tekacev
      * 
      * @return ArrayList<Move> vseh moznih potez crnih tekacev
      * @throws ChessboardException
      */
     public ArrayList<Move> getAllLegalBlackBishopMoves()
 	    throws ChessboardException {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	int[] bishops = { 26, 29 };
 
 	for (int x : bishops) {
 	    int from = this.piecePosition[x];
 	    if (from != -1) {
 		if (!Chessboard.isPositionLegal(from))
 		    throw new ChessboardException("figura " + x
 			    + " je na poziciji " + from);
 
 		for (int y = 0; y < 128; y++) {
 		    if (this.isBlackBishopMoveLegal(from, y)) {
 			int t = this.constructMoveNumber(from, y);
 			rez.add(new Move(t));
 		    }
 		}
 	    }
 	}
 	return rez;
     }
 
 
     /**
      * generira vse mozne poteze bele kraljice
      * 
      * @return ArrayList<Move> vseh moznih potez bele kravljice
      * @throws ChessboardException
      */
     public ArrayList<Move> getAllLegalWhiteQueenMoves()
 	    throws ChessboardException {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	int from = this.piecePosition[3];
 
 	if (from != -1) {
 	    if (!Chessboard.isPositionLegal(from))
 		throw new ChessboardException("figura " + 3
 			+ " je na poziciji " + from);
 
 	    for (int y = 0; y < 128; y++) {
 		if (this.isWhiteQueenMoveLegal(from, y)) {
 		    int t = this.constructMoveNumber(from, y);
 		    rez.add(new Move(t));
 		}
 	    }
 	}
 	return rez;
     }
 
 
     /**
      * generira vse mozne poteze crne kraljice
      * 
      * @return ArrayList<Move> vseh moznih potez crne kravljice
      * @throws ChessboardException
      */
     public ArrayList<Move> getAllLegalBlackQueenMoves()
 	    throws ChessboardException {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	int from = this.piecePosition[27];
 
 	if (from != -1) {
 	    if (!Chessboard.isPositionLegal(from))
 		throw new ChessboardException("figura " + 27
 			+ " je na poziciji " + from);
 
 	    for (int y = 0; y < 128; y++) {
 		if (this.isBlackQueenMoveLegal(from, y)) {
 		    int t = this.constructMoveNumber(from, y);
 		    rez.add(new Move(t));
 		}
 	    }
 	}
 	return rez;
     }
 
 
     /**
      * generira vse mozne poteze belega kralja
      * 
      * @return ArrayList<Move> vseh moznih potez belega kralja
      * @throws ChessboardException
      */
     public ArrayList<Move> getAllLegalWhiteKingMoves()
 	    throws ChessboardException {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	int from = this.piecePosition[4];
 
 	if (from != -1) {
 	    if (!Chessboard.isPositionLegal(from))
 		throw new ChessboardException("figura " + 4
 			+ " je na poziciji " + from);
 
 	    for (int y = 0; y < 128; y++) {
 		if (this.isWhiteKingMoveLegal(from, y)) {
 		    int t = this.constructMoveNumber(from, y);
 		    rez.add(new Move(t));
 		}
 	    }
 	}
 	return rez;
     }
 
 
     /**
      * generira vse mozne poteze crnega kralja
      * 
      * @return ArrayList<Move> vseh moznih potez crnega kralja
      * @throws ChessboardException
      */
     public ArrayList<Move> getAllLegalBlackKingMoves()
 	    throws ChessboardException {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	int from = this.piecePosition[28];
 
 	if (from != -1) {
 	    if (!Chessboard.isPositionLegal(from))
 		throw new ChessboardException("figura " + 28
 			+ " je na poziciji " + from);
 
 	    for (int y = 0; y < 128; y++) {
 		if (this.isBlackKingMoveLegal(from, y)) {
 		    int t = this.constructMoveNumber(from, y);
 		    rez.add(new Move(t));
 		}
 	    }
 	}
 
 	return rez;
     }
 
 
     /**
      * pomozna funkcija za simulacijo,
      * 
      * @return ce je kak�na bela figura napadena ali ce je pat vrne -1, ce je
      *         crni kralj matiran vrne 1, drugace pa vrne 0.
      */
     public int evaluateChessboardFromWhitesPerpective()
 	    throws ChessboardException {
 	if (this.isBlackKingMated()) { return 1; }
 	if (this.isBlackKingPatted()) { return -1; }
 	if (this.isAnyWhiteFigureUnderAttackFromBlack() && !this.isWhitesTurn) { return -1; }
 	if (this.numberOfMovesMade > this.maxNumberOfMoves) { return -1; }
 	if (this.wasBoardStateRepeatedThreeTimes) { return -1; }
 
 	return 0;
     }
 
 
     /**
      * pomozna funkcija za Chessgame
      * 
      * @return vrednost trenutne pozicije
      * @throws ChessboardException
      */
     public int evaluateChessboard() throws ChessboardException {
 	if (this.isBlackKingMated()) { return 1; }
 
 	if (this.isBlackKingPatted()) { return -1; }
 	if (this.wasBoardStateRepeatedThreeTimes) { return -1; }
 	if (this.numberOfMovesMade > this.maxNumberOfMoves) { return -1; }
 
 	return 0;
     }
 
 
     public boolean isBlackKingChecked() throws ChessboardException {
 	// ni stestirana
 
 	int blackKingPos = this.piecePosition[28];
 
 	if (this.isPositionUnderAttackByWhite(blackKingPos, false)) {
 	    return true;
 	}
 	else {
 	    return false;
 	}
     }
 
 
     public boolean isBlackKingMated() throws ChessboardException {
 	int numberOfBlackKingPossibleMoves = this.getAllLegalBlackKingMoves()
 		.size();
 
 	if (numberOfBlackKingPossibleMoves == 0) {
 	    return this.isBlackKingChecked();
 	}
 	else {
 	    return false;
 	}
     }
 
 
     /**
      * Checks if black king is in pat position (either it has no more moves left
      * or same board state has occured three times in a row).
      * 
      * @return true if black king is in pat position, otherwise false
      * @throws ChessboardException
      */
     public boolean isBlackKingPatted() throws ChessboardException {
 	int numberOfPossibleBlackKingMoves = this.getAllLegalBlackKingMoves()
 		.size();
 
 	if (numberOfPossibleBlackKingMoves == 0) {
 	    return !this.isBlackKingChecked();
 	}
 	else {
 	    return false;
 	}
     }
 
 
     public boolean isAnyWhiteFigureUnderAttackFromBlack()
 	    throws ChessboardException {
 	// ni stestirana
 	for (int x = 0; x < 16; x++) {
 	    int pos = this.piecePosition[x];
 	    if (pos == -1) {
 		continue;
 	    }
 
 	    if (this.isPositionUnderAttackByBlack(pos, false)) { return true; }
 	}
 
 	return false;
     }
 
 
     /* ************************************************************************
      * *****************************PREMIKANJE FIGUR***************************
      */
     /*
      * ne preverja ce je na poziciji from beli kmet
      */
     public boolean isWhitePawnMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (from < 0 || from > 127)
 	    throw new ChessboardException("from = " + from);
 
 	if (!Chessboard.isPositionLegal(to)) return false;
 	if (from == to) return false;
 
 	int diff = to - from;
 	if (diff == 16 && this.board[to] == -1) return true;
 	if (diff == 15 || diff == 17) {
 	    if (this.board[to] != -1 && !this.isPieceWhite(this.board[to]))
 		return true;
 	}
 	return false;
     }
 
 
     /*
      * ne preverja ce je na poziciji from beli kmet
      */
     public boolean isWhiteCannibalPawnMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (from < 0 || from > 127)
 	    throw new ChessboardException("from = " + from);
 
 	if (!Chessboard.isPositionLegal(to)) return false;
 	if (from == to) return false;
 
 	int diff = to - from;
 	if (diff == 16 && this.board[to] == -1) return true;
 	if (diff == 15 || diff == 17) {
 	    if (this.board[to] != -1) return true;
 	}
 	return false;
     }
 
 
     /*
      * ne preverja kaj je s figuro na poziciji from
      */
     public boolean isBlackPawnMoveLegal(int from, int to)
 	    throws ChessboardException {
 	// ni stestirana, samo bi mogla delat
 	if (from < 0 || from > 127)
 	    throw new ChessboardException("from = " + from);
 
 	if (!Chessboard.isPositionLegal(to)) return false;
 	if (from == to) return false;
 
 	int diff = to - from;
 	if (diff == -16 && this.board[to] == -1) return true;
 	if (diff == -15 || diff == -17) {
 	    if (this.board[to] != -1 && this.isPieceWhite(this.board[to]))
 		return true;
 	}
 	return false;
     }
 
 
     /*
      * ne preverja kaj je s figuro na poziciji from
      */
     public boolean isBlackCannibalPawnMoveLegal(int from, int to)
 	    throws ChessboardException {
 	// ni stestirana, samo bi mogla delat
 	if (from < 0 || from > 127)
 	    throw new ChessboardException("from = " + from);
 
 	if (!Chessboard.isPositionLegal(to)) return false;
 	if (from == to) return false;
 
 	int diff = to - from;
 	if (diff == -16 && this.board[to] == -1) return true;
 	if (diff == -15 || diff == -17) {
 	    if (this.board[to] != -1) return true;
 	}
 	return false;
     }
 
 
     /*
      * ne preverja ce je na from bela trdnjava
      */
     public boolean isWhiteRookMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (from < 0 || from > 127)
 	    throw new ChessboardException("from = " + from);
 
 	if (!Chessboard.isPositionLegal(to)) return false;
 	if (from == to) return false;
 
 	if (Chessboard.getRankFromPosition(from) == Chessboard
 		.getRankFromPosition(to)) {
 	    int diff = to - from;
 
 	    if (diff < 0)
 		diff = -1;
 	    else
 		diff = 1;
 
 	    int temp = from + diff;
 	    while (temp != to) {
 		if (this.board[temp] != -1) return false;
 		temp += diff;
 	    }
 
 	    if (this.board[to] == -1 || !this.isPieceWhite(this.board[to]))
 		return true;
 	    if (this.isPieceWhite(this.board[to])) return false;
 	}
 	if (Chessboard.getFileFromPosition(from) == Chessboard
 		.getFileFromPosition(to)) {
 	    int diff = to - from;
 
 	    if (diff < 0)
 		diff = -16;
 	    else
 		diff = 16;
 
 	    int temp = from + diff;
 	    while (temp != to) {
 		if (this.board[temp] != -1) return false;
 		temp += diff;
 	    }
 	    if (this.board[to] == -1 || !this.isPieceWhite(this.board[to]))
 		return true;
 	    if (this.isPieceWhite(this.board[to])) return false;
 	}
 
 	return false;
     }
 
 
     /*
      * ne preverja ce je na from bela trdnjava
      */
     public boolean isWhiteCannibalRookMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (from < 0 || from > 127)
 	    throw new ChessboardException("from = " + from);
 
 	if (!Chessboard.isPositionLegal(to)) return false;
 	if (from == to) return false;
 
 	if (Chessboard.getRankFromPosition(from) == Chessboard
 		.getRankFromPosition(to)) {
 	    int diff = to - from;
 
 	    if (diff < 0)
 		diff = -1;
 	    else
 		diff = 1;
 
 	    int temp = from + diff;
 	    while (temp != to) {
 		if (this.board[temp] != -1) return false;
 		temp += diff;
 	    }
 
 	    return true;
 	}
 	if (Chessboard.getFileFromPosition(from) == Chessboard
 		.getFileFromPosition(to)) {
 	    int diff = to - from;
 
 	    if (diff < 0)
 		diff = -16;
 	    else
 		diff = 16;
 
 	    int temp = from + diff;
 	    while (temp != to) {
 		if (this.board[temp] != -1) return false;
 		temp += diff;
 	    }
 	    return true;
 	}
 
 	return false;
     }
 
 
     /*
      * ne preverja kaj je s figuro na from
      */
     public boolean isBlackRookMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (from < 0 || from > 127)
 	    throw new ChessboardException("from = " + from);
 
 	if (!Chessboard.isPositionLegal(to)) return false;
 	if (from == to) return false;
 
 	if (Chessboard.getRankFromPosition(from) == Chessboard
 		.getRankFromPosition(to)) {
 	    int diff = to - from;
 
 	    if (diff < 0)
 		diff = -1;
 	    else
 		diff = 1;
 
 	    int temp = from + diff;
 	    while (temp != to) {
 		if (this.board[temp] != -1) return false;
 		temp += diff;
 	    }
 
 	    if (this.board[to] == -1 || this.isPieceWhite(this.board[to]))
 		return true;
 	    if (!this.isPieceWhite(this.board[to])) return false;
 	}
 	if (Chessboard.getFileFromPosition(from) == Chessboard
 		.getFileFromPosition(to)) {
 	    int diff = to - from;
 
 	    if (diff < 0)
 		diff = -16;
 	    else
 		diff = 16;
 
 	    int temp = from + diff;
 	    while (temp != to) {
 		if (this.board[temp] != -1) return false;
 		temp += diff;
 	    }
 	    if (this.board[to] == -1 || this.isPieceWhite(this.board[to]))
 		return true;
 	    if (!this.isPieceWhite(this.board[to])) return false;
 	}
 
 	return false;
     }
 
 
     /*
      * ne preverja kaj je s figuro na from
      */
     public boolean isBlackCannibalRookMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (from < 0 || from > 127)
 	    throw new ChessboardException("from = " + from);
 
 	if (!Chessboard.isPositionLegal(to)) return false;
 	if (from == to) return false;
 
 	if (Chessboard.getRankFromPosition(from) == Chessboard
 		.getRankFromPosition(to)) {
 	    int diff = to - from;
 
 	    if (diff < 0)
 		diff = -1;
 	    else
 		diff = 1;
 
 	    int temp = from + diff;
 	    while (temp != to) {
 		if (this.board[temp] != -1) return false;
 		temp += diff;
 	    }
 
 	    return true;
 	}
 	if (Chessboard.getFileFromPosition(from) == Chessboard
 		.getFileFromPosition(to)) {
 	    int diff = to - from;
 
 	    if (diff < 0)
 		diff = -16;
 	    else
 		diff = 16;
 
 	    int temp = from + diff;
 	    while (temp != to) {
 		if (this.board[temp] != -1) return false;
 		temp += diff;
 	    }
 	    return true;
 	}
 
 	return false;
     }
 
 
     /*
      * ne preverja kaj je z s figuro na from
      */
     public boolean isWhiteBishopMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (from < 0 || from > 127)
 	    throw new ChessboardException("from = " + from);
 
 	if (!Chessboard.isPositionLegal(to)) return false;
 	if (from == to) return false;
 
 	int diff = to - from;
 	if (diff % 15 == 0) {
 	    if ((diff / 15) < 0)
 		diff = -15;
 	    else
 		diff = 15;
 	}
 	if (diff % 17 == 0) {
 	    if ((diff / 17) < 0)
 		diff = -17;
 	    else
 		diff = 17;
 	}
 
 	if (Math.abs(diff) == 17 || Math.abs(diff) == 15) {
 	    int temp = from + diff;
 	    while (temp != to) {
 		if (this.board[temp] != -1) return false;
 		temp += diff;
 	    }
 
 	    if (this.board[to] == -1 || !this.isPieceWhite(this.board[to]))
 		return true;
 	    else
 		return false;
 	}
 	return false;
     }
 
 
     /*
      * ne preverja kaj je z s figuro na from
      */
     public boolean isWhiteCannibalBishopMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (from < 0 || from > 127)
 	    throw new ChessboardException("from = " + from);
 
 	if (!Chessboard.isPositionLegal(to)) return false;
 	if (from == to) return false;
 
 	int diff = to - from;
 	if (diff % 15 == 0) {
 	    if ((diff / 15) < 0)
 		diff = -15;
 	    else
 		diff = 15;
 	}
 	if (diff % 17 == 0) {
 	    if ((diff / 17) < 0)
 		diff = -17;
 	    else
 		diff = 17;
 	}
 
 	if (Math.abs(diff) == 17 || Math.abs(diff) == 15) {
 	    int temp = from + diff;
 	    while (temp != to) {
 		if (this.board[temp] != -1) return false;
 		temp += diff;
 	    }
 
 	    return true;
 	}
 	return false;
     }
 
 
     /*
      * ne preverja kaj je s figuro na from
      */
     public boolean isBlackCannibalBishopMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (from < 0 || from > 127)
 	    throw new ChessboardException("from = " + from);
 
 	if (!Chessboard.isPositionLegal(to)) return false;
 	if (from == to) return false;
 
 	int diff = to - from;
 	if (diff % 15 == 0) {
 	    if ((diff / 15) < 0)
 		diff = -15;
 	    else
 		diff = 15;
 	}
 	if (diff % 17 == 0) {
 	    if ((diff / 17) < 0)
 		diff = -17;
 	    else
 		diff = 17;
 	}
 
 	if (Math.abs(diff) == 17 || Math.abs(diff) == 15) {
 	    int temp = from + diff;
 	    while (temp != to) {
 		if (this.board[temp] != -1) return false;
 		temp += diff;
 	    }
 
 	    return true;
 	}
 	return false;
     }
 
 
     /*
      * ne preverja kaj je s figuro na from
      */
     public boolean isBlackBishopMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (from < 0 || from > 127)
 	    throw new ChessboardException("from = " + from);
 
 	if (!Chessboard.isPositionLegal(to)) return false;
 	if (from == to) return false;
 
 	int diff = to - from;
 	if (diff % 15 == 0) {
 	    if ((diff / 15) < 0)
 		diff = -15;
 	    else
 		diff = 15;
 	}
 	if (diff % 17 == 0) {
 	    if ((diff / 17) < 0)
 		diff = -17;
 	    else
 		diff = 17;
 	}
 
 	if (Math.abs(diff) == 17 || Math.abs(diff) == 15) {
 	    int temp = from + diff;
 	    while (temp != to) {
 		if (this.board[temp] != -1) return false;
 		temp += diff;
 	    }
 
 	    if (this.board[to] == -1 || this.isPieceWhite(this.board[to]))
 		return true;
 	    else
 		return false;
 	}
 	return false;
     }
 
 
     /*
      * ne ugotavlja, kaj je s figuro na from poziciji
      */
     public boolean isWhiteKnightMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (from < 0 || from > 127)
 	    throw new ChessboardException("from = " + from);
 
 	if (!Chessboard.isPositionLegal(to)) return false;
 	if (from == to) return false;
 
 	if (this.board[to] == -1 || !this.isPieceWhite(this.board[to])) {
 	    int raz = Math.abs(from - to);
 	    switch (raz) {
 	    case 14:
 		return true;
 	    case 31:
 		return true;
 	    case 33:
 		return true;
 	    case 18:
 		return true;
 	    }
 	}
 	return false;
     }
 
 
     /*
      * ne ugotavlja, kaj je s figuro na from poziciji
      */
     public boolean isWhiteCannibalKnightMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (from < 0 || from > 127)
 	    throw new ChessboardException("from = " + from);
 
 	if (!Chessboard.isPositionLegal(to)) return false;
 	if (from == to) return false;
 
 	if (true) {
 	    int raz = Math.abs(from - to);
 	    switch (raz) {
 	    case 14:
 		return true;
 	    case 31:
 		return true;
 	    case 33:
 		return true;
 	    case 18:
 		return true;
 	    }
 	}
 	return false;
     }
 
 
     /*
      * ne ugotavlja, kaj je s figuro na from poziciji
      */
     public boolean isBlackKnightMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (from < 0 || from > 127)
 	    throw new ChessboardException("from = " + from);
 
 	if (!Chessboard.isPositionLegal(to)) return false;
 	if (from == to) return false;
 
 	if (this.board[to] == -1 || this.isPieceWhite(this.board[to])) {
 	    int raz = Math.abs(from - to);
 	    switch (raz) {
 	    case 14:
 		return true;
 	    case 31:
 		return true;
 	    case 33:
 		return true;
 	    case 18:
 		return true;
 	    }
 	}
 	return false;
     }
 
 
     /*
      * ne ugotavlja, kaj je s figuro na from poziciji
      */
     public boolean isBlackCannibalKnightMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (from < 0 || from > 127)
 	    throw new ChessboardException("from = " + from);
 
 	if (!Chessboard.isPositionLegal(to)) return false;
 	if (from == to) return false;
 
 	if (true) {
 	    int raz = Math.abs(from - to);
 	    switch (raz) {
 	    case 14:
 		return true;
 	    case 31:
 		return true;
 	    case 33:
 		return true;
 	    case 18:
 		return true;
 	    }
 	}
 	return false;
     }
 
 
     /*
      * ne preverja kaj je s figuro na from
      */
     public boolean isWhiteQueenMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (this.isWhiteBishopMoveLegal(from, to)) return true;
 	if (this.isWhiteRookMoveLegal(from, to)) return true;
 
 	return false;
     }
 
 
     /*
      * ne preverja kaj je s figuro na from
      */
     public boolean isWhiteCannibalQueenMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (this.isWhiteCannibalBishopMoveLegal(from, to)) return true;
 	if (this.isWhiteCannibalRookMoveLegal(from, to)) return true;
 
 	return false;
     }
 
 
     /*
      * ne preverja, kaj je s figuro na from
      */
     public boolean isBlackQueenMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (this.isBlackBishopMoveLegal(from, to)) return true;
 	if (this.isBlackRookMoveLegal(from, to)) return true;
 
 	return false;
     }
 
 
     /*
      * ne preverja, kaj je s figuro na from
      */
     public boolean isBlackCannibalQueenMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (this.isBlackCannibalBishopMoveLegal(from, to)) return true;
 	if (this.isBlackCannibalRookMoveLegal(from, to)) return true;
 
 	return false;
     }
 
 
     /*
      * ne ugotavlja kaj je s figuro na from poziciji
      */
     public boolean isWhiteKingMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (from < 0 || from > 127)
 	    throw new ChessboardException("from = " + from);
 
 	if (!Chessboard.isPositionLegal(to)) return false;
 	if (from == to) return false;
 
 	if (this.isPositionAdjacentToBlackKing(to)) { return false; }
 
 	int targetPiece = this.board[to];
 	if (targetPiece != -1 && this.isPieceWhite(targetPiece)) { return false; }
 
 	int whiteKingPos = this.piecePosition[4];
 	this.piecePosition[4] = -1;
 	this.board[whiteKingPos] = -1;
 
 	if (this.isPositionUnderAttackByBlack(to, true)) {
 	    this.piecePosition[4] = whiteKingPos;
 	    this.board[whiteKingPos] = 4;
 
 	    return false;
 	}
 
 	this.piecePosition[4] = whiteKingPos;
 	this.board[whiteKingPos] = 4;
 
 	if (this.isPositionAdjacentToWhiteKing(to)) {
 	    return true;
 	}
 	else {
 	    return false;
 	}
     }
 
 
     /*
      * ne ugotavlja kaj je s figuro na from poziciji
      */
     public boolean isWhiteCannibalKingMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (from < 0 || from > 127)
 	    throw new ChessboardException("from = " + from);
 
 	if (!Chessboard.isPositionLegal(to)) return false;
 	if (from == to) return false;
 
 	if (this.isPositionAdjacentToBlackKing(to)) { return false; }
 
 	int whiteKingPos = this.piecePosition[4];
 	this.piecePosition[4] = -1;
 	this.board[whiteKingPos] = -1;
 
 	if (this.isPositionUnderAttackByBlack(to, true)) {
 	    this.piecePosition[4] = whiteKingPos;
 	    this.board[whiteKingPos] = 4;
 
 	    return false;
 	}
 	this.piecePosition[4] = whiteKingPos;
 	this.board[whiteKingPos] = 4;
 
 	if (this.isPositionAdjacentToWhiteKing(to)) {
 	    return true;
 	}
 	else {
 	    return false;
 	}
 
     }
 
 
     /*
      * ne ugotavlja kaj je s figuro na from poziciji
      */
     public boolean isBlackKingMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (from < 0 || from > 127)
 	    throw new ChessboardException("from = " + from);
 
 	if (!Chessboard.isPositionLegal(to)) { return false; }
 	if (from == to) return false;
 
 	if (this.isPositionAdjacentToWhiteKing(to)) { return false; }
 
 	int targetPiece = this.board[to];
 
 	if (targetPiece != -1 && this.isPieceBlack(targetPiece)) { return false; }
 
 	int blackKingPos = this.piecePosition[28];
 	this.piecePosition[28] = -1;
 	this.board[blackKingPos] = -1;
 
 	if (this.isPositionUnderAttackByWhite(to, true)) {
 	    this.piecePosition[28] = blackKingPos;
 	    this.board[blackKingPos] = 28;
 
 	    return false;
 	}
 
 	this.piecePosition[28] = blackKingPos;
 	this.board[blackKingPos] = 28;
 
 	if (this.isPositionAdjacentToBlackKing(to)) {
 	    return true;
 	}
 	else {
 	    return false;
 	}
 
     }
 
 
     /*
      * ne ugotavlja kaj je s figuro na from poziciji
      */
     public boolean isBlackCannibalKingMoveLegal(int from, int to)
 	    throws ChessboardException {
 	if (from < 0 || from > 127)
 	    throw new ChessboardException("from = " + from);
 
 	if (!Chessboard.isPositionLegal(to)) { return false; }
 	if (from == to) return false;
 
 	if (this.isPositionAdjacentToWhiteKing(to)) { return false; }
 
 	int blackKingPos = this.piecePosition[28];
 	this.piecePosition[28] = -1;
 	this.board[blackKingPos] = -1;
 
 	if (this.isPositionUnderAttackByWhite(to, true)) {
 	    this.piecePosition[28] = blackKingPos;
 	    this.board[blackKingPos] = 28;
 
 	    return false;
 	}
 
 	this.piecePosition[28] = blackKingPos;
 	this.board[blackKingPos] = 28;
 
 	if (this.isPositionAdjacentToBlackKing(to)) {
 	    return true;
 	}
 	else {
 	    return false;
 	}
     }
 
 
     /*
      * preveri ce je poteza mozna
      */
     public boolean isWhiteMoveLegal(int from, int to)
 	    throws ChessboardException {
 	int piece = this.board[from];
 
 	// if(DEBUG) println("Za�etek isWhiteMoveLegal(int from, int to)");
 	// if(DEBUG) println("from: " + from + "\tto: " + to + "\tpiece: " +
 	// piece);
 	// if(DEBUG) println("Kli�em in vra�am isWhiteXXXMoveLegal");
 
 	if (piece == 0 || piece == 7)
 	    return this.isWhiteRookMoveLegal(from, to);
 	if (piece == 1 || piece == 6)
 	    return this.isWhiteKnightMoveLegal(from, to);
 	if (piece == 2 || piece == 5)
 	    return this.isWhiteBishopMoveLegal(from, to);
 	if (piece == 3) return this.isWhiteQueenMoveLegal(from, to);
 	if (piece == 4) return this.isWhiteKingMoveLegal(from, to);
 	if (piece > 7 && piece < 16)
 	    return this.isWhitePawnMoveLegal(from, to);
 
 	// if(DEBUG)
 	// {
 	// println("///////////////////////////////////");
 	// println("Napaka: Na from=" + from +" je figura " + piece);
 	// try
 	// {
 	// printChessboard();
 	// }
 	// catch(Exception e)
 	// {
 	// e.printStackTrace();
 	// }
 	// println("///////////////////////////////////");
 	// }
 
 	throw new ChessboardException("na from=" + from + " je figura " + piece);
     }
 
 
     /*
      * preveri ce je poteza mozna
      */
     public boolean isBlackMoveLegal(int from, int to)
 	    throws ChessboardException {
 	int piece = this.board[from];
 
 	if (piece > 15 && piece < 24)
 	    return this.isBlackPawnMoveLegal(from, to);
 	if (piece == 24 || piece == 31)
 	    return this.isBlackRookMoveLegal(from, to);
 	if (piece == 25 || piece == 30)
 	    return this.isBlackKnightMoveLegal(from, to);
 	if (piece == 26 || piece == 29)
 	    return this.isBlackBishopMoveLegal(from, to);
 	if (piece == 27) return this.isBlackQueenMoveLegal(from, to);
 	if (piece == 28) return this.isBlackKingMoveLegal(from, to);
 
 	throw new ChessboardException("na from je figura " + piece);
     }
 
 
     /* *************************************************************************
      * *******************POMOZNE FUNKCIJE**************************************
      */
 
     private int constructMoveNumber(int from, int to) {
 	/*
 	 * stevilka je sestavljena: from sestevlja prvih 8 bitov poteze (najbolj
 	 * levih) to je drugih 8 bitov tretjih 8 bitov je figura, ki jo
 	 * premaknemo zadnjih 8 bitov je pa figura, ki na mestu kamor se
 	 * premikamo
 	 */
 	int rez = this.board[to] & 0xFF;
 	rez |= (this.board[from] & 0xFF) << 8;
 	rez |= (to & 0xFF) << 16;
 	rez |= (from & 0xFF) << 24;
 	return rez;
     }
 
 
     private static boolean isPositionLegal(int position) {
 	if (position < 0 || position > 127) return false;
 	if ((position & 0x88) != 0)
 	    return false;
 	else
 	    return true;
     }
 
 
     private static int getRankFromPosition(int position) {
 	return (position / 16) + 1;
     }
 
 
     private static int getFileFromPosition(int position) {
 	return (position % 16) + 1;
     }
 
 
     private boolean isPieceWhite(int pieceNumber) throws ChessboardException {
 	// ce vrne true, je figure bela, drugace je crna
 	if (pieceNumber < 0 || pieceNumber > 31)
 	    throw new ChessboardException("piecenumber = " + pieceNumber);
 	if (pieceNumber >= 0 && pieceNumber < 16)
 	    return true;
 	else
 	    return false;
     }
 
 
     private boolean isPieceBlack(int pieceNumber) throws ChessboardException {
 	if (pieceNumber < 0 || pieceNumber > 31)
 	    throw new ChessboardException("piecenumber = " + pieceNumber);
 	if (pieceNumber > 15 && pieceNumber < 32)
 	    return true;
 	else
 	    return false;
     }
 
 
     private void constructPiecePositionFromBoard() {
 	this.piecePosition = new int[32];
 	for (int x = 0; x < this.piecePosition.length; x++) {
 	    this.piecePosition[x] = -1;
 	}
 	for (int x = 0; x < this.board.length; x++) {
 	    if (this.board[x] != -1) {
 		this.piecePosition[this.board[x]] = x;
 	    }
 	}
     }
 
 
     public int distanceBewteenKings() {
 	int positionA = this.piecePosition[4];
 	int positionB = this.piecePosition[28];
 	return Utils.distanceBetweenPositions(positionA, positionB);
     }
 
 
     /**
      * Checks if king are in opposition if black king moves to certain position
      * 
      * @param position
      *            position where if black king would be there we check if kings
      *            are in opposition
      * @return <code>true</code> if kings would be in oppostion if black king
      *         moves to postiion, otherwise <code>false</code>.
      */
     public boolean willBlackKingBeInOppositionIfItMovesTo(int position) {
 	int whiteKingPos = this.piecePosition[4];
 	return Utils.distanceBetweenPositions(whiteKingPos, position) == 2;
     }
 
 
     /* ***********************************************************************************
      * ***********************************************************************************
      * ***********************************************************************************
      */
 
     /*
      * ce je pozicija na imaginarnem delu plosce vrne true
      */
     public boolean isPositionUnderAttackByBlack(int position,
 	    boolean ignoreBlackKing) throws ChessboardException {
 	if (!Chessboard.isPositionLegal(position)) return true;
 
 	for (int x = 16; x < 32; x++) {
 	    // trdnjavi
 	    if ((x == 24 || x == 31) && this.piecePosition[x] != -1) {
 		if (this.isBlackCannibalRookMoveLegal(this.piecePosition[x],
 			position)) { return true; }
 	    }
 	    // konja
 	    if ((x == 25 || x == 30) && this.piecePosition[x] != -1) {
 		if (this.isBlackCannibalKnightMoveLegal(this.piecePosition[x],
 			position)) { return true; }
 	    }
 	    // tekaca
 	    if ((x == 26 || x == 29) && this.piecePosition[x] != -1) {
 		if (this.isBlackCannibalBishopMoveLegal(this.piecePosition[x],
 			position)) { return true; }
 	    }
 	    // kraljica
 	    if (x == 27 && this.piecePosition[x] != -1) {
 		if (this.isBlackCannibalQueenMoveLegal(this.piecePosition[x],
 			position)) { return true; }
 	    }
 	    // kralj
 	    if (x == 28 && !ignoreBlackKing) {
 		if (this.isBlackKingMoveLegal(this.piecePosition[x], position)) { return true; }
 	    }
 	}
 
 	for (int x = 16; x < 24; x++) {
 	    int from = this.piecePosition[x];
 	    if (from != -1) {
 		int diff = position - from;
 		if (diff == -17 || diff == -15) return true;
 	    }
 	}
 
 	return false;
     }
 
 
     public boolean isPositionUnderAttackByWhite(int position,
 	    boolean ignoreWhiteKing) throws ChessboardException {
 	if (position < 0 || position > 127)
 	    throw new ChessboardException("position = " + position);
 
 	if (!Chessboard.isPositionLegal(position)) return true;
 
 	for (int x = 0; x < 8; x++) {
 	    // trdnjavi
 	    if ((x == 0 || x == 7) && this.piecePosition[x] != -1) {
 		if (this.isWhiteCannibalRookMoveLegal(this.piecePosition[x],
 			position)) { return true; }
 	    }
 	    // konja
 	    if ((x == 1 || x == 6) && this.piecePosition[x] != -1) {
 		if (this.isWhiteCannibalKnightMoveLegal(this.piecePosition[x],
 			position)) { return true; }
 	    }
 	    // tekaca
 	    if ((x == 2 || x == 5) && this.piecePosition[x] != -1) {
 		if (this.isWhiteCannibalBishopMoveLegal(this.piecePosition[x],
 			position)) { return true; }
 	    }
 	    // kraljica
 	    if (x == 3 && this.piecePosition[x] != -1) {
 		if (this.isWhiteCannibalQueenMoveLegal(this.piecePosition[x],
 			position)) { return true; }
 	    }
 	    // kralj
 	    if (x == 4 && !ignoreWhiteKing) {
 		if (this.isWhiteKingMoveLegal(this.piecePosition[x], position)) { return true; }
 	    }
 	}
 
 	for (int x = 8; x < 16; x++) {
 	    int from = this.piecePosition[x];
 	    if (from != -1) {
 		int diff = position - from;
 		if (diff == 17 || diff == 15) return true;
 	    }
 	}
 
 	return false;
     }
 
 
     public boolean isPositionAdjacentToWhiteKing(int position) {
 	int kingPos = this.piecePosition[4];
 	int diff = Math.abs(kingPos - position);
 
 	if (diff == 1 || diff == 15 || diff == 16 || diff == 17) {
 	    return true;
 	}
 	else {
 	    return false;
 	}
     }
 
 
     public boolean isPositionAdjacentToBlackKing(int position) {
 	int kingPos = this.piecePosition[28];
 	int diff = Math.abs(kingPos - position);
 
 	if (diff == 1 || diff == 15 || diff == 16 || diff == 17) {
 	    return true;
 	}
 	else {
 	    return false;
 	}
     }
 
 
     public boolean isPositionProtectedByKingIfKingIsMovedTo(int position,
 	    int kingPos) {
 	if (Utils.distanceBetweenPositions(position, kingPos) == 1) { return true; }
 
 	int whiteKingPos = kingPos;
 	if (Utils.distanceBetweenPositions(whiteKingPos, position) == 2) {
 	    int whiteKingRank = Utils.getRankFromPosition(whiteKingPos);
 	    int whiteKingFile = Utils.getFileFromPosition(whiteKingPos);
 	    int posRank = Utils.getRankFromPosition(position);
 	    int posFile = Utils.getFileFromPosition(position);
 	    boolean differentRanks = whiteKingRank != posRank;
 	    boolean differentFiles = whiteKingFile != posFile;
 	    if (differentFiles && differentRanks) { return true; }
 	}
 
 	return false;
     }
 
 
     /* *********************************************************************
      * ***********************FUNKCIJE ZA POMOC ISKANJA POTEZ**************
      * *********************************************************************
      */
 
     public ArrayList<Move> movesWhereBlackKingEatsWhite()
 	    throws ChessboardException {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	for (int x = 0; x < 16; x++) {
 	    int piecePosition = this.piecePosition[x];
 	    if (piecePosition != -1
 		    && this.isPositionAdjacentToBlackKing(piecePosition)) {
 		int from = this.piecePosition[28];
 		int to = piecePosition;
 		if (this.isBlackKingMoveLegal(from, to)) {
 		    int movedPiece = 28;
 		    int targetPiece = x;
 		    Move add = new Move(Utils.constructMoveNumber(from, to,
 			    movedPiece, targetPiece));
 		    rez.add(add);
 		}
 	    }
 	}
 	return rez;
     }
 
 
     public ArrayList<Move> movesWhereBlackKingEvadesOposition(
 	    ArrayList<Move> blackKingPossibleMoves) throws ChessboardException {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	for (int x = 0; x < blackKingPossibleMoves.size(); x++) {
 	    int to = Utils
 		    .getToFromMoveNumber(blackKingPossibleMoves.get(x).moveNumber);
 	    if (!this.willBlackKingBeInOppositionIfItMovesTo(to)) {
 		Move copyMove = new Move(
 			blackKingPossibleMoves.get(x).moveNumber);
 		rez.add(copyMove);
 	    }
 	}
 	return rez;
     }
 
 
     public ArrayList<Move> movesWhereBlackKingTriesToEatRook(
 	    ArrayList<Move> possibleBlakKingMoves) {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	int distance = 17;
 	int rookPosition = -1;
 	if (this.piecePosition[0] != -1 && this.piecePosition[7] != -1) {
 	    int dis0 = Utils.distanceBetweenPositions(this.piecePosition[28],
 		    this.piecePosition[0]);
 	    int dis7 = Utils.distanceBetweenPositions(this.piecePosition[28],
 		    this.piecePosition[7]);
 	    if (dis0 < dis7) {
 		rookPosition = this.piecePosition[0];
 	    }
 	    else if (dis7 < dis0) {
 		rookPosition = this.piecePosition[7];
 	    }
 	    else {
 		Random rand = new Random();
 		int i = rand.nextInt(2);
 		if (i == 0) {
 		    rookPosition = this.piecePosition[0];
 		}
 		else {
 		    rookPosition = this.piecePosition[7];
 		}
 	    }
 	}
 	else if (this.piecePosition[0] != -1) {
 	    rookPosition = this.piecePosition[0];
 	}
 	else if (this.piecePosition[7] != -1) {
 	    rookPosition = this.piecePosition[7];
 	}
 	else {
 	    return rez;
 	}
 	for (int x = 0; x < possibleBlakKingMoves.size(); x++) {
 	    int from = Utils
 		    .getToFromMoveNumber(possibleBlakKingMoves.get(x).moveNumber);
 	    int currDistance = Utils.distanceBetweenPositions(from,
 		    rookPosition);
 	    if (currDistance < distance) {
 		rez = new ArrayList<Move>();
 		distance = currDistance;
 	    }
 	    if (currDistance == distance) {
 		rez.add(possibleBlakKingMoves.get(x));
 	    }
 	}
 	return rez;
     }
 
 
     public ArrayList<Move> movesWhereWhiteIsntNearBlackKing(
 	    ArrayList<Move> moves) throws ChessboardException {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	for (int x = 0; x < moves.size(); x++) {
 	    int to = Utils.getToFromMoveNumber(moves.get(x).moveNumber);
 	    if (!this.isPositionAdjacentToBlackKing(to)) {
 		rez.add(new Move(moves.get(x).moveNumber));
 	    }
 	}
 	return rez;
     }
 
 
     public ArrayList<Move> movesWhereWhiteRooksArentPlaceOnSameLine(
 	    ArrayList<Move> possibleRookMoves) throws ChessboardException {
 	int rook0Pos = this.piecePosition[0];
 	int rook7Pos = this.piecePosition[7];
 	if (rook0Pos == -1 || rook7Pos == -1) { return possibleRookMoves; }
 	ArrayList<Move> rez = new ArrayList<Move>();
 	for (int x = 0; x < possibleRookMoves.size(); x++) {
 	    int from = Utils
 		    .getFromFromMoveNumber(possibleRookMoves.get(x).moveNumber);
 	    int badRank = -1;
 	    int badFile = -1;
 	    if (from == rook0Pos) {
 		badRank = Utils.getRankFromPosition(rook7Pos);
 		badFile = Utils.getFileFromPosition(rook7Pos);
 	    }
 	    else if (from == rook7Pos) {
 		badRank = Utils.getRankFromPosition(rook0Pos);
 		badFile = Utils.getFileFromPosition(rook0Pos);
 	    }
 	    else {
 		throw new ChessboardException("poteza ne pripada trdnjavi");
 	    }
 	    int to = Utils
 		    .getToFromMoveNumber(possibleRookMoves.get(x).moveNumber);
 	    int currRank = Utils.getRankFromPosition(to);
 	    int currFile = Utils.getFileFromPosition(to);
 	    if (currFile != badFile && currRank != badRank) {
 		rez.add(possibleRookMoves.get(x));
 	    }
 	}
 	return rez;
     }
 
 
     public ArrayList<Move> movesWherePieceIsNotInCorner(ArrayList<Move> moves) {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	for (int x = 0; x < moves.size(); x++) {
 	    int to = Utils.getToFromMoveNumber(moves.get(x).moveNumber);
 	    if (to != 0 && to != 7 && to != 112 && to != 119) {
 		rez.add(moves.get(x));
 	    }
 	}
 	return rez;
     }
 
 
     public ArrayList<Move> movesWhereWhiteRookTryToStayOnNearestLines(
 	    ArrayList<Move> possibleRookMoves) throws ChessboardException {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	int rook0Pos = this.piecePosition[0];
 	int rook7Pos = this.piecePosition[7];
 	if (rook0Pos == -1 || rook7Pos == -1) { return possibleRookMoves; }
 	int minDistance = 20;
 	for (int x = 0; x < possibleRookMoves.size(); x++) {
 	    Move currMove = possibleRookMoves.get(x);
 	    int from = Utils.getFromFromMoveNumber(currMove.moveNumber);
 	    int to = Utils.getToFromMoveNumber(currMove.moveNumber);
 	    int targetPosition = -1;
 	    if (from == rook0Pos) {
 		targetPosition = rook7Pos;
 	    }
 	    else if (from == rook7Pos) {
 		targetPosition = rook0Pos;
 	    }
 	    else {
 		throw new ChessboardException("Poteza ni od trdnjave");
 	    }
 	    int currDistance = Utils.distanceBetweenPositions(to,
 		    targetPosition);
 	    if (currDistance < minDistance) {
 		minDistance = currDistance;
 		rez = new ArrayList<Move>();
 	    }
 	    if (currDistance == minDistance) {
 		rez.add(currMove);
 	    }
 	}
 	return rez;
     }
 
 
     public ArrayList<Move> movesWhereWhiteAvoidBeingEatenByBlackKing(
 	    ArrayList<Move> moves) {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	for (int x = 0; x < moves.size(); x++) {
 	    int from = Utils.getFromFromMoveNumber(moves.get(x).moveNumber);
 	    int to = Utils.getToFromMoveNumber(moves.get(x).moveNumber);
 	    if (this.isPositionAdjacentToBlackKing(from)
 		    && !this.isPositionAdjacentToBlackKing(to)) {
 		rez.add(moves.get(x));
 	    }
 	}
 	return rez;
     }
 
 
     public ArrayList<Move> movesWhereWhiteKingMovesCloserToBlackKind(
 	    ArrayList<Move> posKingMoves) {
 	int distance = this.distanceBewteenKings();
 	int blackKingPosition = this.piecePosition[28];
 	ArrayList<Move> rez = new ArrayList<Move>();
 	for (int x = 0; x < posKingMoves.size(); x++) {
 	    int to = Utils.getToFromMoveNumber(posKingMoves.get(x).moveNumber);
 	    int currDistance = Utils.distanceBetweenPositions(to,
 		    blackKingPosition);
 	    if (currDistance < distance) {
 		distance = currDistance;
 		rez = new ArrayList<Move>();
 	    }
 	    if (currDistance == distance) {
 		rez.add(posKingMoves.get(x));
 	    }
 	}
 	return rez;
     }
 
 
     public ArrayList<Move> movesWhereWhiteKingMovesCloserOrEqualToBlackKind(
 	    ArrayList<Move> posMoves) {
 	int distance = this.distanceBewteenKings();
 	int blackKingPosition = this.piecePosition[28];
 	ArrayList<Move> rez = new ArrayList<Move>();
 	for (int x = 0; x < posMoves.size(); x++) {
 	    int movedPiece = Utils
 		    .getMovedPieceFromMoveNumber(posMoves.get(x).moveNumber);
 
 	    if (movedPiece == 4) {
 		int to = Utils.getToFromMoveNumber(posMoves.get(x).moveNumber);
 		int currDistance = Utils.distanceBetweenPositions(to,
 			blackKingPosition);
 		if (currDistance < distance) {
 		    rez.add(posMoves.get(x));
 		}
 	    }
 	    else {
 		rez.add(posMoves.get(x));
 	    }
 	}
 	return rez;
     }
 
 
     public ArrayList<Move> movesWhereWhiteRookLikePieceBoundsBlackKing(
 	    ArrayList<Move> posQueenMoves) {
 	int blackKingPosition = this.piecePosition[28];
 	int blackKingRank = Utils.getRankFromPosition(blackKingPosition);
 	int blackKingFile = Utils.getFileFromPosition(blackKingPosition);
 	ArrayList<Move> rez = new ArrayList<Move>();
 	for (int x = 0; x < posQueenMoves.size(); x++) {
 	    int to = Utils.getToFromMoveNumber(posQueenMoves.get(x).moveNumber);
 	    int currRank = Utils.getRankFromPosition(to);
 	    int currFile = Utils.getFileFromPosition(to);
 	    int rankDiff = Math.abs(currRank - blackKingRank);
 	    int fileDiff = Math.abs(currFile - blackKingFile);
 	    if (rankDiff == 1 || fileDiff == 1) {
 		rez.add(posQueenMoves.get(x));
 	    }
 	}
 	return rez;
     }
 
 
     public ArrayList<Move> movesWhereWhiteKQKIsSafe(ArrayList<Move> moves)
 	    throws ChessboardException {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	int queenPosition = this.piecePosition[3];
 	if (this.isPositionAdjacentToBlackKing(queenPosition)) {
 	    for (int x = 0; x < moves.size(); x++) {
 		int movedPiece = Utils
 			.getMovedPieceFromMoveNumber(moves.get(x).moveNumber);
 		int to = Utils.getToFromMoveNumber(moves.get(x).moveNumber);
 		if (movedPiece == 3) {
 		    if (!this.isPositionAdjacentToBlackKing(to)) {
 			rez.add(moves.get(x));
 		    }
 		    else if (this.isPositionAdjacentToBlackKing(to)
 			    && this.isPositionAdjacentToWhiteKing(to)) {
 			rez.add(moves.get(x));
 		    }
 		}
 		else if (movedPiece == 4) {
 		    if (Utils.arePositionsAdjacent(queenPosition, to)) {
 			rez.add(moves.get(x));
 		    }
 		}
 		else {
 		    throw new ChessboardException(
 			    "poteza ne pripada beli kraljici ali belemu kralju");
 		}
 	    }
 	}
 	else {
 	    for (int x = 0; x < moves.size(); x++) {
 		int movedPiece = Utils
 			.getMovedPieceFromMoveNumber(moves.get(x).moveNumber);
 		if (movedPiece == 4) {
 		    rez.add(moves.get(x));
 		}
 		else if (movedPiece == 3) {
 		    int to = Utils.getToFromMoveNumber(moves.get(x).moveNumber);
 		    if (this.isPositionAdjacentToBlackKing(to)) {
 			if (this.isPositionAdjacentToWhiteKing(to)) {
 			    rez.add(moves.get(x));
 			}
 		    }
 		    else {
 			rez.add(moves.get(x));
 		    }
 		}
 		else {
 		    throw new ChessboardException(
 			    "poteza ne pripada beli kraljici ali belemu kralju");
 		}
 	    }
 	}
 	return rez;
     }
 
 
     /*
      * ne preverja �e je dejansko ena figura, gleda pa samo bli�ino kraljev,
      * tako da �e je ve� figur, lahko odre�e kak�no potezo preve�
      */
     public ArrayList<Move> movesWhereWhiteOnePieceEndingIsSafe(
 	    ArrayList<Move> moves) {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	for (int x = 0; x < moves.size(); x++) {
 	    Move temp = moves.get(x);
 	    int to = Utils.getToFromMoveNumber(temp.moveNumber);
 	    int movedPiece = Utils.getMovedPieceFromMoveNumber(temp.moveNumber);
 	    if (movedPiece != 4) {
 		if (this.isPositionAdjacentToBlackKing(to)
 			&& this.isPositionAdjacentToWhiteKing(to)) {
 		    rez.add(temp);
 		}
 		if (!this.isPositionAdjacentToBlackKing(to)) {
 		    rez.add(temp);
 		}
 	    }
 	    else {
 		// poteze belega kralja so varne
 		rez.add(temp);
 	    }
 	}
 	return rez;
     }
 
 
     /**
      * filter moves to those that need to be made (so that white doesn't loose a
      * piece), this method only checks protection between piece and white king
      * (it doesnt know that for instance two rooks can protect each other).
      * 
      * @param allWhiteMoves
      * @return list of moves that white must do to avoid loosing a piece
      */
     public ArrayList<Move> whiteUrgentMoves(ArrayList<Move> allWhiteMoves) {
 	ArrayList<Move> rez = new ArrayList<Move>();
 
 	// if black king isn't near any pieces, it can't eat them
 	if (this.piecesNearPosition(this.piecePosition[28]).size() == 0) { return rez; }
 
 	for (Move currMove : allWhiteMoves) {
 	    int from = Utils.getFromFromMoveNumber(currMove.moveNumber);
 	    int to = Utils.getToFromMoveNumber(currMove.moveNumber);
 	    int movedPiece = Utils
 		    .getMovedPieceFromMoveNumber(currMove.moveNumber);
 
 	    if (movedPiece == 4) {
 		for (int piecesAroundBlackKing : this
 			.piecesNearPosition(this.piecePosition[28])) {
 		    if (!ChessboardUtils.arePositionsAdjacent(from,
 			    this.piecePosition[piecesAroundBlackKing])
 			    && ChessboardUtils.arePositionsAdjacent(to,
 				    this.piecePosition[piecesAroundBlackKing])) {
 			rez.add(currMove);
 		    }
 		}
 	    }
 	    else if (!ChessboardUtils.arePositionsAdjacent(from,
 		    this.piecePosition[4])
 		    && ChessboardUtils.arePositionsAdjacent(from,
 			    this.piecePosition[28])
 		    && !ChessboardUtils.arePositionsAdjacent(to,
 			    this.piecePosition[28])) {
 		rez.add(currMove);
 	    }
 	}
 
 	return rez;
     }
 
 
     /**
      * heuristic that filters moves so that white wont give black king any
      * pieces (however this method only consideres piece protected by other
      * piece if other piece is white king), but it doesn't check if any moves
      * need to be made to avoid being eaten
      * 
      * @param allWhiteMoves
      * @return list of safe moves
      */
     public ArrayList<Move> whiteSafeMoves(ArrayList<Move> allWhiteMoves) {
 	ArrayList<Move> rez = new ArrayList<Move>();
 
 	for (Move currMove : allWhiteMoves) {
 	    int movedPiece = Utils
 		    .getMovedPieceFromMoveNumber(currMove.moveNumber);
 	    int to = Utils.getToFromMoveNumber(currMove.moveNumber);
 
 	    // since black only has black king, white kings move is always safe
 	    if (movedPiece == 4) {
 		// we get all pieces that could be eaten by black king
 		ArrayList<Integer> piecesThatCantLooseProtection = this
 			.piecesNearPosition(this.piecePosition[28]);
 		boolean addKingMove = true;
 		for (int piece : piecesThatCantLooseProtection) {
 		    int positionOfPiece = this.piecePosition[piece];
 
 		    // if some piece is protected by king, then king shouldn't
 		    // withdraw protection
 		    if (this.isPositionAdjacentToWhiteKing(positionOfPiece)
 			    && !ChessboardUtils.arePositionsAdjacent(
 				    positionOfPiece, to)) {
 			addKingMove = false;
 		    }
 		}
 		if (addKingMove) {
 		    rez.add(currMove);
 		}
 	    }
 	    // if piece doesn't move near black king or is near white king, than
 	    // it's also safe move
 	    else if (!this.isPositionAdjacentToBlackKing(to)
 		    || this.isPositionAdjacentToWhiteKing(to)) {
 		rez.add(currMove);
 	    }
 	}
 	return rez;
     }
 
 
     public ArrayList<Move> KRKWhiteMovesWhereRookChecksIfKingsAreInOpposition(
 	    ArrayList<Move> allWhiteMoves) {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	int blackKingPos = this.piecePosition[28];
 
 	// there are two rooks and only one should be on board
 	int rookPos = this.piecePosition[0];
 	if (rookPos == -1) {
 	    rookPos = this.piecePosition[7];
 	}
 
 	// if kings are on oppossition we find those in which white checks
 	// otherwise all moves are valid
 	if (this.willBlackKingBeInOppositionIfItMovesTo(blackKingPos)) {
 	    int blackKingRank = Utils.getRankFromPosition(blackKingPos);
 	    int blackKingFile = Utils.getFileFromPosition(blackKingPos);
 
 	    for (Move currMove : allWhiteMoves) {
 		int movedPiece = Utils
 			.getMovedPieceFromMoveNumber(currMove.moveNumber);
 		int to = Utils.getToFromMoveNumber(currMove.moveNumber);
 
 		if (movedPiece == 0 || movedPiece == 7) {
 		    // poteze trdnjave
 		    int rank = Utils.getRankFromPosition(to);
 		    int file = Utils.getFileFromPosition(to);
 
 		    boolean sameRank = rank == blackKingRank;
 		    boolean sameFile = file == blackKingFile;
 		    if ((sameFile || sameRank)
 			    && !ChessboardUtils
 				    .isPositionBetweenPositionsOnLine(
 					    this.piecePosition[4],
 					    blackKingPos, to)) {
 			rez.add(new Move(currMove.moveNumber));
 		    }
 		}
 		else {
 		    // poteze kralja
 		    int whiteKingPos = this.piecePosition[4];
 		    int whiteKingFile = Utils.getFileFromPosition(whiteKingPos);
 		    int whiteKingRank = Utils.getRankFromPosition(whiteKingPos);
 		    int rookRank = Utils.getRankFromPosition(rookPos);
 		    int rookFile = Utils.getFileFromPosition(rookPos);
 
 		    boolean areRanksSame = (whiteKingRank == blackKingRank)
 			    && (whiteKingRank == rookRank);
 		    boolean areFilesSame = (whiteKingFile == blackKingFile)
 			    && (whiteKingFile == rookFile);
 
 		    if (areRanksSame) {
 			int rank = Utils.getRankFromPosition(to);
 			if (rank != whiteKingRank) {
 			    rez.add(new Move(currMove.moveNumber));
 			}
 		    }
 		    if (areFilesSame) {
 			int file = Utils.getFileFromPosition(to);
 			if (file != whiteKingFile) {
 			    rez.add(new Move(currMove.moveNumber));
 			}
 		    }
 		}
 	    }
 	}
 	else {
 	    return allWhiteMoves;
 	}
 	return rez;
     }
 
 
     public ArrayList<Move> KBBKWhiteUrgentMoves(ArrayList<Move> allWhiteMoves) {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	int bishop2Position = this.piecePosition[2];
 	int bishop5Position = this.piecePosition[5];
 	boolean isBishop2NearBlackKing = false;
 	boolean isBishop5NearBlackKing = false;
 	if (bishop2Position != -1) {
 	    isBishop2NearBlackKing = this
 		    .isPositionAdjacentToBlackKing(bishop2Position);
 	}
 	if (bishop5Position != -1) {
 	    isBishop5NearBlackKing = this
 		    .isPositionAdjacentToBlackKing(bishop5Position);
 	}
 	if (!isBishop2NearBlackKing && !isBishop5NearBlackKing) { return rez; }
 
 	for (Move currMove : allWhiteMoves) {
 	    int to = Utils.getToFromMoveNumber(currMove.moveNumber);
 	    int movedPiece = Utils
 		    .getMovedPieceFromMoveNumber(currMove.moveNumber);
 	    if (movedPiece == 4) {
 		if (isBishop2NearBlackKing) {
 		    if (this.isPositionProtectedByKingIfKingIsMovedTo(
 			    bishop2Position, to)) {
 			rez.add(new Move(currMove.moveNumber));
 		    }
 		}
 		if (isBishop5NearBlackKing) {
 		    if (this.isPositionProtectedByKingIfKingIsMovedTo(
 			    bishop5Position, to)) {
 			rez.add(new Move(currMove.moveNumber));
 		    }
 		}
 	    }
 	    else {
 		if (isBishop2NearBlackKing && movedPiece == 2
 			&& !this.isPositionAdjacentToBlackKing(to)
 			&& !this.isPositionAdjacentToWhiteKing(bishop2Position)) {
 		    rez.add(new Move(currMove.moveNumber));
 		}
 		else if (isBishop5NearBlackKing && movedPiece == 5
 			&& !this.isPositionAdjacentToBlackKing(to)
 			&& !this.isPositionAdjacentToWhiteKing(to)) {
 		    rez.add(new Move(currMove.moveNumber));
 		}
 	    }
 	}
 
 	return rez;
     }
 
 
     public ArrayList<Move> KBBKWhiteSafeMoves(ArrayList<Move> allWhiteMoves) {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	for (Move currMove : allWhiteMoves) {
 	    int to = Utils.getToFromMoveNumber(currMove.moveNumber);
 	    int movedPiece = Utils
 		    .getMovedPieceFromMoveNumber(currMove.moveNumber);
 	    if (movedPiece == 4) {
 		rez.add(new Move(currMove.moveNumber));
 	    }
 	    else {
 		if (!this.isPositionAdjacentToBlackKing(to)) {
 		    rez.add(new Move(currMove.moveNumber));
 		}
 		if (this.isPositionAdjacentToBlackKing(to)
 			&& this.isPositionAdjacentToWhiteKing(to)) {
 		    rez.add(new Move(currMove.moveNumber));
 		}
 	    }
 	}
 	return rez;
     }
 
 
     public ArrayList<Move> KBBKWhiteMovesWhereBishopsAreOnAdjacentDiagonals(
 	    ArrayList<Move> allWhiteMoves) {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	for (Move currMove : allWhiteMoves) {
 	    int movedPiece = Utils
 		    .getMovedPieceFromMoveNumber(currMove.moveNumber);
 	    if (movedPiece == 4) {
 		rez.add(new Move(currMove.moveNumber));
 	    }
 	    else {
 		int otherBishop = movedPiece == 2 ? 5 : 2;
 		int otherBishopPosition = this.piecePosition[otherBishop];
 		int to = Utils.getToFromMoveNumber(currMove.moveNumber);
 		if (otherBishopPosition != -1
 			&& Utils.arePsotionsDiagonallyAdjacent(to,
 				otherBishopPosition)) {
 		    rez.add(new Move(currMove.moveNumber));
 		}
 	    }
 	}
 	return rez;
     }
 
 
     public ArrayList<Move> filterMovesToWhiteKingMoves(
 	    ArrayList<Move> allwhiteMoves) {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	for (Move currMove : allwhiteMoves) {
 	    int movedPiece = Utils
 		    .getMovedPieceFromMoveNumber(currMove.moveNumber);
 	    if (movedPiece == 4) {
 		rez.add(new Move(currMove.moveNumber));
 	    }
 	}
 	return rez;
     }
 
 
     public ArrayList<Move> filterMovesToWhiteRookMoves(
 	    ArrayList<Move> allWhiteMoves) {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	for (Move currMove : allWhiteMoves) {
 	    int movedPiece = Utils
 		    .getMovedPieceFromMoveNumber(currMove.moveNumber);
 	    if (movedPiece == 0 || movedPiece == 7) {
 		rez.add(new Move(currMove.moveNumber));
 	    }
 	}
 	return rez;
     }
 
 
     public ArrayList<Move> filterMovesToWhiteBishopMoves(
 	    ArrayList<Move> allWhiteMoves) {
 	ArrayList<Move> rez = new ArrayList<Move>();
 	for (Move currMove : allWhiteMoves) {
 	    int movedPiece = Utils
 		    .getMovedPieceFromMoveNumber(currMove.moveNumber);
 	    if (movedPiece == 2 || movedPiece == 5) {
 		rez.add(new Move(currMove.moveNumber));
 	    }
 	}
 	return rez;
     }
 
 
     /**
      * Get all pieces that are near selected position, meaning they are on
      * positions that have distance from selected position exactly 1.
      * 
      * @param position
      *            selected position
      * @return list of all pieces that are near position
      */
     protected ArrayList<Integer> piecesNearPosition(int position) {
 	ArrayList<Integer> rez = new ArrayList<Integer>();
 	int[] diff = { 1, 15, 16, 17 };
 	for (int off : diff) {
 	    int currPlusPosition = position + off;
 	    int currMinusPosition = position - off;
 	    if (Utils.isPositionLegal(currPlusPosition)
 		    && this.board[currPlusPosition] != -1) {
 		rez.add(this.board[currPlusPosition]);
 	    }
 
 	    if (Utils.isPositionLegal(currMinusPosition)
 		    && this.board[currMinusPosition] != -1) {
 		rez.add(this.board[currMinusPosition]);
 	    }
 	}
 	return rez;
     }
 
 
     /**
      * Calculates hashcode of chessboard. Each chessboard state has its own
      * hashcode. WARNING: it only works correctly as long as there are no more
      * than 4 pieces on the board. It also only distnguish between chessboards
      * with sam pieces on the board.
      * 
      * @return chessboard hashcode
      */
     @Override
     public int hashCode() {
 	int result = 0;
 	int counter = 0;
 	for (int pos : this.piecePosition) {
 	    if (pos != -1) {
 		int offset = counter * 8;
 		int shiftedPos = pos & 0xFF;
 		shiftedPos = shiftedPos << offset;
 		result = result | shiftedPos;
 		counter++;
 	    }
 	}
 
 	return result;
     }
 
 }
