 package chessboard;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import utils.ChessboardUtils;
 import utils.Utils;
 import exceptions.ChessboardException;
 import exec.Constants;
 import exec.Move;
 
 public class SimpleChessboard {
 
     /**
      * used for optimization purposes. Sets number of plys to check for state
      * repetition for pat.
      */
     protected static final int          NUMBER_OF_PREVIOUS_MOVES_WE_CHECK_FOR_REPEATED_STATE_PAT = 20;
     /**
      * Holds piece number for board location.
      */
     protected int[]                     board;
     /**
      * Holds position of every piece.
      */
     protected int[]                     piecePosition;
     /**
      * Tells if it currently whites move.
      */
     protected boolean                   isWhitesTurn                                             = true;
     /** Number of moves that were made */
     protected int                       numberOfMovesMade                                        = 0;
     /**
      * This is used for keeping track how many times some chess board state has
      * occurred. Key is hash code of chess board and values is how may times
      * that has has occurred.
      */
     protected HashMap<Integer, Integer> numberOfTimesBoardStateHasOccured                        = new HashMap<Integer, Integer>(
                                                                                                          SimpleChessboard.NUMBER_OF_PREVIOUS_MOVES_WE_CHECK_FOR_REPEATED_STATE_PAT + 1);
     /**
      * Chess board name.
      */
     protected String                    name;
     /**
      * flag that tells us if chess board state has appeared three times.
      */
     protected boolean                   wasBoardStateRepeatedThreeTimes                          = false;
     /**
      * List of chess board hashes that have already occured.
      */
     protected ArrayList<Integer>        previousHashes                                           = new ArrayList<Integer>();
     /**
      * Logger
      */
     protected Logger                    log                                                      = Logger.getLogger("MCTS.Chessboard");
 
 
     /**
      * Gets fen notation for chess board.
      * 
      * @return fen representation of current board state
      */
     public String boardToFen() {
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
 
 
     /**
      * Converts chess board to string.
      * 
      * @return chess board string repesentation
      */
     @Override
     public String toString() {
         StringBuffer sb = new StringBuffer(50);
         for (int x = 0; x < 25; x++) {
             sb.append("*");
         }
         sb.append("\n");
 
         if (this.isWhitesTurn) {
            sb.append("Beli je na potezi na plosci " + this.name + "\r\n");
         }
         else {
            sb.append("crni je na potezi na plosci " + this.name + "\r\n");
         }
 
         for (int x = 0; x < 25; x++) {
             sb.append("*");
         }
 
         sb.append("*\r\n");
         for (int x = 7; x >= 0; x--) {
             sb.append(8 - x);
             int baza = x * 16;
             for (int y = 0; y < 8; y++) {
                 int t = this.board[baza + y];
                 if (t == -1) {
                     sb.append("*00");
                 }
                 else {
                     sb.append("*" + Utils.pieceNumberToString(t));
                 }
             }
            sb.append("*\r\n");
         }
         for (int x = 0; x < 25; x++) {
             sb.append("*");
         }
         sb.append("*\r\n");
 
         for (int x = 0; x < 8; x++) {
             char file = (char) ((int) 'A' + x);
             sb.append("**" + file);
         }
         sb.append("**\r\n");
 
         return sb.toString();
     }
 
 
     /**
      * Gets array that holds piece for specific position.
      * 
      * @return array that holds pieces for positions
      */
     public int[] cloneBoard() {
         return this.board.clone();
     }
 
 
     /**
      * Gets array that holds postion of specific piece.
      * 
      * @return clone of piecePosition
      */
     public int[] clonePiecePosition() {
         return this.piecePosition.clone();
     }
 
 
     /**
      * Gets clone of list that holds previous hashes.
      * 
      * @return list of hashes that have already occured
      */
     @SuppressWarnings("unchecked")
     public ArrayList<Integer> clonePreviousHashes() {
         return (ArrayList<Integer>) this.previousHashes.clone();
     }
 
 
     /**
      * Gets clone of map that map hashed to number of their occureances.
      * 
      * @return map which for every hash (key) tells how many times that hash has
      *         occured.
      */
     @SuppressWarnings("unchecked")
     public HashMap<Integer, Integer> cloneNumberOfTimesBoardStateHasOccured() {
         return (HashMap<Integer, Integer>) this.numberOfTimesBoardStateHasOccured
                 .clone();
     }
 
 
     /**
      * Tells if white is currently on the move.
      * 
      * @return <code>true</code> if this is whites turn, otherwise
      *         <code>false</code>.
      */
     public boolean getIsWhitesTurn() {
         return this.isWhitesTurn;
     }
 
 
     /**
      * Gets number of plies that were already made.
      * 
      * @return number of plies that were already made.
      */
     public int getNumberOfPliesMade() {
         return this.numberOfMovesMade;
     }
 
 
     /**
      * Builds move number from string. Used for getting move numbers for black
      * player from fruit.
      * 
      * @param move
      *            must have format of xnym (ie. a2b3), where x,y are files and
      *            2,3 are ranks.
      * @return move number
      */
     public int constructMoveNumberFromString(String move) {
         String fromS = move.substring(0, 2);
         String toS = move.substring(2);
         int from = Utils.positionFromString(fromS);
         int to = Utils.positionFromString(toS);
         return this.constructMoveNumber(from, to);
     }
 
 
     /**
      * This move is only used from Chessboard.makeAMove(int). It moves piece
      * from <code>from</code> position to <code>to</code> position. It also
      * fills numberOfTimesBoardStateHasOccured. And sets flag if this chess
      * board state has occurred at least three times.
      * 
      * @param from
      *            chess board position from which piece will be moved
      * @param to
      *            chess board position to which piece will be moved
      */
     public void makeAMove(int from, int to) throws ChessboardException {
         this.isWhitesTurn = !this.isWhitesTurn;
         int piece = this.board[from];
 
         int targetPiece = this.board[to];
         if (targetPiece != -1) {
             this.piecePosition[targetPiece] = -1;
         }
 
         this.board[from] = -1;
         this.board[to] = piece;
         this.piecePosition[piece] = to;
         this.numberOfMovesMade++;
 
         int hash = this.hashCode();
         this.previousHashes.add(hash);
 
         // for optimization purposes we limit number of states that we check
         if (this.previousHashes.size() > SimpleChessboard.NUMBER_OF_PREVIOUS_MOVES_WE_CHECK_FOR_REPEATED_STATE_PAT) {
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
     public void makeAMove(int moveNumber) throws ChessboardException {
         if (moveNumber == -1) { throw new ChessboardException(
                 "neveljavna poteza"); }
 
         int from = Utils.getStartingPositionFromMoveNumber(moveNumber);
         int to = Utils.getTargetPositionFromMoveNumber(moveNumber);
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
 
         this.makeAMove(from, to);
     }
 
 
     /**
      * generira vse mozne poteze za vse bele figure
      * 
      * @return ArrayList<Move> vseh moznih belih potez
      * @throws ChessboardException
      */
     public ArrayList<Move> getAllLegalWhitePlies() throws ChessboardException {
 
         ArrayList<Move> rez = new ArrayList<Move>();
         for (int x = 0; x < 16; x++) {
             int from = this.piecePosition[x];
 
             if (from != -1) {
                 if (!ChessboardUtils.isPositionLegal(from))
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
                 if (!ChessboardUtils.isPositionLegal(from))
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
                 if (!ChessboardUtils.isPositionLegal(from))
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
                 if (!ChessboardUtils.isPositionLegal(from))
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
                 if (!ChessboardUtils.isPositionLegal(from))
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
                 if (!ChessboardUtils.isPositionLegal(from))
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
                 if (!ChessboardUtils.isPositionLegal(from))
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
                 if (!ChessboardUtils.isPositionLegal(from))
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
                 if (!ChessboardUtils.isPositionLegal(from))
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
                 if (!ChessboardUtils.isPositionLegal(from))
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
             if (!ChessboardUtils.isPositionLegal(from))
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
             if (!ChessboardUtils.isPositionLegal(from))
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
             if (!ChessboardUtils.isPositionLegal(from))
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
             if (!ChessboardUtils.isPositionLegal(from))
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
      * Return chess board evaluation that is that white player is interested in.
      * 
      * @return evaluation of chess board state
      */
     public ChessboardEvalState evaluateChessboardFromWhitesPerpective()
             throws ChessboardException {
         if (this.isBlackKingMated()) { return ChessboardEvalState.BLACK_KING_MATED; }
         if (this.isBlackKingPatted()) { return ChessboardEvalState.PAT; }
         if (this.isAnyWhiteFigureUnderAttackFromBlack() && !this.isWhitesTurn) { return ChessboardEvalState.WHITE_PIECE_IN_DANGER; }
         if (this.numberOfMovesMade > Constants.MAX_DEPTH) { return ChessboardEvalState.TOO_MANY_MOVES_MADE; }
         if (this.wasBoardStateRepeatedThreeTimes) { return ChessboardEvalState.DRAW; }
 
         return ChessboardEvalState.NORMAl;
     }
 
 
     /**
      * Evaluates chess board state.
      * 
      * @return evaluation of current chess board state
      * @throws ChessboardException
      */
     public ChessboardEvalState evaluateChessboard() throws ChessboardException {
         if (this.isBlackKingMated()) { return ChessboardEvalState.BLACK_KING_MATED; }
 
         if (this.isBlackKingPatted()) { return ChessboardEvalState.PAT; }
         if (this.wasBoardStateRepeatedThreeTimes) { return ChessboardEvalState.DRAW; }
         if (this.numberOfMovesMade > Constants.MAX_DEPTH) { return ChessboardEvalState.TOO_MANY_MOVES_MADE; }
 
         return ChessboardEvalState.NORMAl;
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
 
 
     public boolean isWhitePawnMoveLegal(int from, int to)
             throws ChessboardException {
         if (from < 0 || from > 127)
             throw new ChessboardException("from = " + from);
 
         if (!ChessboardUtils.isPositionLegal(to))
             return false;
         if (from == to)
             return false;
 
         int diff = to - from;
         if (diff == 16 && this.board[to] == -1)
             return true;
         if (diff == 15 || diff == 17) {
             if (this.board[to] != -1
                     && !ChessboardUtils.isPieceWhite(this.board[to]))
                 return true;
         }
         return false;
     }
 
 
     public boolean isWhiteCannibalPawnMoveLegal(int from, int to)
             throws ChessboardException {
         if (from < 0 || from > 127)
             throw new ChessboardException("from = " + from);
 
         if (!ChessboardUtils.isPositionLegal(to))
             return false;
         if (from == to)
             return false;
 
         int diff = to - from;
         if (diff == 16 && this.board[to] == -1)
             return true;
         if (diff == 15 || diff == 17) {
             if (this.board[to] != -1)
                 return true;
         }
         return false;
     }
 
 
     public boolean isBlackPawnMoveLegal(int from, int to)
             throws ChessboardException {
         // ni stestirana, samo bi mogla delat
         if (from < 0 || from > 127)
             throw new ChessboardException("from = " + from);
 
         if (!ChessboardUtils.isPositionLegal(to))
             return false;
         if (from == to)
             return false;
 
         int diff = to - from;
         if (diff == -16 && this.board[to] == -1)
             return true;
         if (diff == -15 || diff == -17) {
             if (this.board[to] != -1
                     && ChessboardUtils.isPieceWhite(this.board[to]))
                 return true;
         }
         return false;
     }
 
 
     public boolean isBlackCannibalPawnMoveLegal(int from, int to)
             throws ChessboardException {
         // ni stestirana, samo bi mogla delat
         if (from < 0 || from > 127)
             throw new ChessboardException("from = " + from);
 
         if (!ChessboardUtils.isPositionLegal(to))
             return false;
         if (from == to)
             return false;
 
         int diff = to - from;
         if (diff == -16 && this.board[to] == -1)
             return true;
         if (diff == -15 || diff == -17) {
             if (this.board[to] != -1)
                 return true;
         }
         return false;
     }
 
 
     public boolean isWhiteRookMoveLegal(int from, int to)
             throws ChessboardException {
         if (from < 0 || from > 127)
             throw new ChessboardException("from = " + from);
 
         if (!ChessboardUtils.isPositionLegal(to))
             return false;
         if (from == to)
             return false;
 
         if (Utils.getRankFromPosition(from) == Utils.getRankFromPosition(to)) {
             int diff = to - from;
 
             if (diff < 0)
                 diff = -1;
             else
                 diff = 1;
 
             int temp = from + diff;
             while (temp != to) {
                 if (this.board[temp] != -1)
                     return false;
                 temp += diff;
             }
 
             if (this.board[to] == -1
                     || !ChessboardUtils.isPieceWhite(this.board[to]))
                 return true;
             if (ChessboardUtils.isPieceWhite(this.board[to]))
                 return false;
         }
         if (Utils.getFileFromPosition(from) == Utils.getFileFromPosition(to)) {
             int diff = to - from;
 
             if (diff < 0)
                 diff = -16;
             else
                 diff = 16;
 
             int temp = from + diff;
             while (temp != to) {
                 if (this.board[temp] != -1)
                     return false;
                 temp += diff;
             }
             if (this.board[to] == -1
                     || !ChessboardUtils.isPieceWhite(this.board[to]))
                 return true;
             if (ChessboardUtils.isPieceWhite(this.board[to]))
                 return false;
         }
 
         return false;
     }
 
 
     public boolean isWhiteCannibalRookMoveLegal(int from, int to)
             throws ChessboardException {
         if (from < 0 || from > 127)
             throw new ChessboardException("from = " + from);
 
         if (!ChessboardUtils.isPositionLegal(to))
             return false;
         if (from == to)
             return false;
 
         if (Utils.getRankFromPosition(from) == Utils.getRankFromPosition(to)) {
             int diff = to - from;
 
             if (diff < 0)
                 diff = -1;
             else
                 diff = 1;
 
             int temp = from + diff;
             while (temp != to) {
                 if (this.board[temp] != -1)
                     return false;
                 temp += diff;
             }
 
             return true;
         }
         if (Utils.getFileFromPosition(from) == Utils.getFileFromPosition(to)) {
             int diff = to - from;
 
             if (diff < 0)
                 diff = -16;
             else
                 diff = 16;
 
             int temp = from + diff;
             while (temp != to) {
                 if (this.board[temp] != -1)
                     return false;
                 temp += diff;
             }
             return true;
         }
 
         return false;
     }
 
 
     public boolean isBlackRookMoveLegal(int from, int to)
             throws ChessboardException {
         if (from < 0 || from > 127)
             throw new ChessboardException("from = " + from);
 
         if (!ChessboardUtils.isPositionLegal(to))
             return false;
         if (from == to)
             return false;
 
         if (Utils.getRankFromPosition(from) == Utils.getRankFromPosition(to)) {
             int diff = to - from;
 
             if (diff < 0)
                 diff = -1;
             else
                 diff = 1;
 
             int temp = from + diff;
             while (temp != to) {
                 if (this.board[temp] != -1)
                     return false;
                 temp += diff;
             }
 
             if (this.board[to] == -1
                     || ChessboardUtils.isPieceWhite(this.board[to]))
                 return true;
             if (!ChessboardUtils.isPieceWhite(this.board[to]))
                 return false;
         }
         if (Utils.getFileFromPosition(from) == Utils.getFileFromPosition(to)) {
             int diff = to - from;
 
             if (diff < 0)
                 diff = -16;
             else
                 diff = 16;
 
             int temp = from + diff;
             while (temp != to) {
                 if (this.board[temp] != -1)
                     return false;
                 temp += diff;
             }
             if (this.board[to] == -1
                     || ChessboardUtils.isPieceWhite(this.board[to]))
                 return true;
             if (!ChessboardUtils.isPieceWhite(this.board[to]))
                 return false;
         }
 
         return false;
     }
 
 
     public boolean isBlackCannibalRookMoveLegal(int from, int to)
             throws ChessboardException {
         if (from < 0 || from > 127)
             throw new ChessboardException("from = " + from);
 
         if (!ChessboardUtils.isPositionLegal(to))
             return false;
         if (from == to)
             return false;
 
         if (Utils.getRankFromPosition(from) == Utils.getRankFromPosition(to)) {
             int diff = to - from;
 
             if (diff < 0)
                 diff = -1;
             else
                 diff = 1;
 
             int temp = from + diff;
             while (temp != to) {
                 if (this.board[temp] != -1)
                     return false;
                 temp += diff;
             }
 
             return true;
         }
         if (Utils.getFileFromPosition(from) == Utils.getFileFromPosition(to)) {
             int diff = to - from;
 
             if (diff < 0)
                 diff = -16;
             else
                 diff = 16;
 
             int temp = from + diff;
             while (temp != to) {
                 if (this.board[temp] != -1)
                     return false;
                 temp += diff;
             }
             return true;
         }
 
         return false;
     }
 
 
     public boolean isWhiteBishopMoveLegal(int from, int to)
             throws ChessboardException {
         if (from < 0 || from > 127)
             throw new ChessboardException("from = " + from);
 
         if (!ChessboardUtils.isPositionLegal(to))
             return false;
         if (from == to)
             return false;
 
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
                 if (this.board[temp] != -1)
                     return false;
                 temp += diff;
             }
 
             if (this.board[to] == -1
                     || !ChessboardUtils.isPieceWhite(this.board[to]))
                 return true;
             else
                 return false;
         }
         return false;
     }
 
 
     public boolean isWhiteCannibalBishopMoveLegal(int from, int to)
             throws ChessboardException {
         if (from < 0 || from > 127)
             throw new ChessboardException("from = " + from);
 
         if (!ChessboardUtils.isPositionLegal(to))
             return false;
         if (from == to)
             return false;
 
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
                 if (this.board[temp] != -1)
                     return false;
                 temp += diff;
             }
 
             return true;
         }
         return false;
     }
 
 
     public boolean isBlackCannibalBishopMoveLegal(int from, int to)
             throws ChessboardException {
         if (from < 0 || from > 127)
             throw new ChessboardException("from = " + from);
 
         if (!ChessboardUtils.isPositionLegal(to))
             return false;
         if (from == to)
             return false;
 
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
                 if (this.board[temp] != -1)
                     return false;
                 temp += diff;
             }
 
             return true;
         }
         return false;
     }
 
 
     public boolean isBlackBishopMoveLegal(int from, int to)
             throws ChessboardException {
         if (from < 0 || from > 127)
             throw new ChessboardException("from = " + from);
 
         if (!ChessboardUtils.isPositionLegal(to))
             return false;
         if (from == to)
             return false;
 
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
                 if (this.board[temp] != -1)
                     return false;
                 temp += diff;
             }
 
             if (this.board[to] == -1
                     || ChessboardUtils.isPieceWhite(this.board[to]))
                 return true;
             else
                 return false;
         }
         return false;
     }
 
 
     public boolean isWhiteKnightMoveLegal(int from, int to)
             throws ChessboardException {
         if (from < 0 || from > 127)
             throw new ChessboardException("from = " + from);
 
         if (!ChessboardUtils.isPositionLegal(to))
             return false;
         if (from == to)
             return false;
 
         if (this.board[to] == -1
                 || !ChessboardUtils.isPieceWhite(this.board[to])) {
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
 
 
     public boolean isWhiteCannibalKnightMoveLegal(int from, int to)
             throws ChessboardException {
         if (from < 0 || from > 127)
             throw new ChessboardException("from = " + from);
 
         if (!ChessboardUtils.isPositionLegal(to))
             return false;
         if (from == to)
             return false;
 
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
 
 
     public boolean isBlackKnightMoveLegal(int from, int to)
             throws ChessboardException {
         if (from < 0 || from > 127)
             throw new ChessboardException("from = " + from);
 
         if (!ChessboardUtils.isPositionLegal(to))
             return false;
         if (from == to)
             return false;
 
         if (this.board[to] == -1
                 || ChessboardUtils.isPieceWhite(this.board[to])) {
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
 
 
     public boolean isBlackCannibalKnightMoveLegal(int from, int to)
             throws ChessboardException {
         if (from < 0 || from > 127)
             throw new ChessboardException("from = " + from);
 
         if (!ChessboardUtils.isPositionLegal(to))
             return false;
         if (from == to)
             return false;
 
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
 
 
     public boolean isWhiteQueenMoveLegal(int from, int to)
             throws ChessboardException {
         if (this.isWhiteBishopMoveLegal(from, to))
             return true;
         if (this.isWhiteRookMoveLegal(from, to))
             return true;
 
         return false;
     }
 
 
     public boolean isWhiteCannibalQueenMoveLegal(int from, int to)
             throws ChessboardException {
         if (this.isWhiteCannibalBishopMoveLegal(from, to))
             return true;
         if (this.isWhiteCannibalRookMoveLegal(from, to))
             return true;
 
         return false;
     }
 
 
     public boolean isBlackQueenMoveLegal(int from, int to)
             throws ChessboardException {
         if (this.isBlackBishopMoveLegal(from, to))
             return true;
         if (this.isBlackRookMoveLegal(from, to))
             return true;
 
         return false;
     }
 
 
     public boolean isBlackCannibalQueenMoveLegal(int from, int to)
             throws ChessboardException {
         if (this.isBlackCannibalBishopMoveLegal(from, to))
             return true;
         if (this.isBlackCannibalRookMoveLegal(from, to))
             return true;
 
         return false;
     }
 
 
     public boolean isWhiteKingMoveLegal(int from, int to)
             throws ChessboardException {
         if (from < 0 || from > 127)
             throw new ChessboardException("from = " + from);
 
         if (!ChessboardUtils.isPositionLegal(to))
             return false;
         if (from == to)
             return false;
 
         if (this.isPositionAdjacentToBlackKing(to)) { return false; }
 
         int targetPiece = this.board[to];
         if (targetPiece != -1 && ChessboardUtils.isPieceWhite(targetPiece)) { return false; }
 
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
 
 
     public boolean isWhiteCannibalKingMoveLegal(int from, int to)
             throws ChessboardException {
         if (from < 0 || from > 127)
             throw new ChessboardException("from = " + from);
 
         if (!ChessboardUtils.isPositionLegal(to))
             return false;
         if (from == to)
             return false;
 
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
 
 
     public boolean isBlackKingMoveLegal(int from, int to)
             throws ChessboardException {
         if (from < 0 || from > 127)
             throw new ChessboardException("from = " + from);
 
         if (!ChessboardUtils.isPositionLegal(to)) { return false; }
         if (from == to)
             return false;
 
         if (this.isPositionAdjacentToWhiteKing(to)) { return false; }
 
         int targetPiece = this.board[to];
 
         if (targetPiece != -1 && ChessboardUtils.isPieceBlack(targetPiece)) { return false; }
 
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
 
 
     public boolean isBlackCannibalKingMoveLegal(int from, int to)
             throws ChessboardException {
         if (from < 0 || from > 127)
             throw new ChessboardException("from = " + from);
 
         if (!ChessboardUtils.isPositionLegal(to)) { return false; }
         if (from == to)
             return false;
 
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
         if (piece == 3)
             return this.isWhiteQueenMoveLegal(from, to);
         if (piece == 4)
             return this.isWhiteKingMoveLegal(from, to);
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
         if (piece == 27)
             return this.isBlackQueenMoveLegal(from, to);
         if (piece == 28)
             return this.isBlackKingMoveLegal(from, to);
 
         throw new ChessboardException("na from je figura " + piece);
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
 
 
     public boolean isPositionUnderAttackByBlack(int position,
             boolean ignoreBlackKing) throws ChessboardException {
         if (!ChessboardUtils.isPositionLegal(position))
             return true;
 
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
                 if (diff == -17 || diff == -15)
                     return true;
             }
         }
 
         return false;
     }
 
 
     public boolean isPositionUnderAttackByWhite(int position,
             boolean ignoreWhiteKing) throws ChessboardException {
         if (position < 0 || position > 127)
             throw new ChessboardException("position = " + position);
 
         if (!ChessboardUtils.isPositionLegal(position))
             return true;
 
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
                 if (diff == 17 || diff == 15)
                     return true;
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
 
 
     /**
      * Checks if is position adjacent to black king
      * 
      * @param position
      *            position
      * @return <code>true</code> if position is adjacent to black king,
      *         <code>false</code> otherwise
      */
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
 
 
     /**
      * @return how many times has current chessboard state appeared. This mehtod
      *         should not be call immediately after chessboard initialization.
      */
     public int howManyTimeHasCurrentStateAppeared() {
         return this.numberOfTimesBoardStateHasOccured.get(this.hashCode());
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
 
 
     @SuppressWarnings("unchecked")
     public Object clone() {
         Chessboard result = new Chessboard();
         result.name = this.name + " clone";
         result.board = this.board.clone();
         result.isWhitesTurn = this.isWhitesTurn;
         result.numberOfMovesMade = this.numberOfMovesMade;
         result.numberOfTimesBoardStateHasOccured = new HashMap<Integer, Integer>(
                 this.numberOfMovesMade);
         result.piecePosition = this.piecePosition.clone();
         result.previousHashes = (ArrayList<Integer>) this.previousHashes
                 .clone();
         result.wasBoardStateRepeatedThreeTimes = this.wasBoardStateRepeatedThreeTimes;
 
         return result;
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
             if (ChessboardUtils.isPositionLegal(currPlusPosition)
                     && this.board[currPlusPosition] != -1) {
                 rez.add(this.board[currPlusPosition]);
             }
 
             if (ChessboardUtils.isPositionLegal(currMinusPosition)
                     && this.board[currMinusPosition] != -1) {
                 rez.add(this.board[currMinusPosition]);
             }
         }
         return rez;
     }
 
 
     /**
      * Construct correct piecePosition field from board filed.
      */
     protected void constructPiecePositionFromBoard() {
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
 
 
     /**
      * Builds move number.
      * 
      * @param from
      *            starting piece position
      * @param to
      *            target piece position
      * @return move number
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
 
 }
