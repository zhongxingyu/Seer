 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package chess;
 
 import chess.Gamestate.GameMoves;
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 /**
  *
  * @author Tyler
  */
 public class Minimax implements Cloneable
 {
     public static final int UNLIMITED_SEARCH_DEPTH = -1;
     public static final int MINI_HAS_WON           = Integer.MAX_VALUE;
     public static final int STALE_MATE             = 0;
     public static final int MAX_HAS_WON            = Integer.MIN_VALUE;
     public static final int MAX_TURN               = 1;
     public static final int MIN_TURN               = -1;
 
     private int player = Minimax.MIN_TURN; // Must always be 1 or -1
     public Gamestate myState;
     
     
     public Minimax()
     {
         myState = new Gamestate();
     }
     
     public Minimax(Gamestate state)
     {
         myState = state;
     }
 
     public int getPlayer()
     {
         return this.player;
     }
 
     public int[] makePerfectMove(int maxSearchDepth)
     {
         boolean kingSeized;
         if(maxSearchDepth == 0)
         {
             return null;
         }
 
         LinkedList<int[]> moves = this.listAllLegalMoves();
         if(moves.isEmpty())
         {
             this.staleMate();
             return null;
         }
         else if(moves.size() == 1)
         {
             kingSeized = doMove(moves.get(0));
             if(kingSeized)
                 return new int[]{moves.get(0)[0], moves.get(0)[1], 1} ;
             else
                 return moves.get(0);
         }
 
         int     bestScore = this.player == Minimax.MAX_TURN ? Minimax.MINI_HAS_WON : Minimax.MAX_HAS_WON;
         int[]  bestMove  = null;
 
         byte[] originalState = this.myState.state;
         for(int[] move : moves)
         {
             Minimax tempBoard = new Minimax();
             tempBoard.myState.changeState(originalState);
             tempBoard.doMove(move);
             int score = tempBoard.evaluate(maxSearchDepth == Minimax.UNLIMITED_SEARCH_DEPTH ? Minimax.UNLIMITED_SEARCH_DEPTH : maxSearchDepth - 1, new AlphaBeta());
             if(score * player < bestScore || bestMove == null)
             {
                 bestScore = score * player;
                 bestMove  = move;
             }
             this.myState.changeState(originalState);
         }
         kingSeized = doMove(bestMove);
         if(kingSeized)
             return(new int[]{bestMove[0],bestMove[1], 1});
         else
             return(bestMove);
     }
 
     public int evaluate(int maxSearchDepth, AlphaBeta alphaBeta)
     {
         int currentScore = this.getCurrentScore();
         if(currentScore == Minimax.MINI_HAS_WON || currentScore == Minimax.MAX_HAS_WON)
         {
             return currentScore;
         }
         LinkedList<int[]> moves = this.listAllLegalMoves();
         if(moves.isEmpty())
         {
             return Minimax.STALE_MATE;
         }
         int bestScore = 0;
         byte[] originalState = this.myState.state;
         for(int[] move : moves)
         {
             Minimax tempBoard = new Minimax();
             tempBoard.myState.changeState(originalState);
             tempBoard.doMove(move);
             int score;
             if(maxSearchDepth == 0)
             {
                 score = tempBoard.getCurrentScore();
             }
             else
             {
                 score = tempBoard.evaluate(maxSearchDepth == Minimax.UNLIMITED_SEARCH_DEPTH ? Minimax.UNLIMITED_SEARCH_DEPTH : maxSearchDepth - 1, alphaBeta);
 
                 // Alpha-beta pruning
                 if(this.player != Minimax.MIN_TURN)
                 {
                     if(score < alphaBeta.alpha)
                     {
                         return score;
                     }
                     else if(score < alphaBeta.beta)
                     {
                         alphaBeta.beta = score;
                     }
                 }
                 else
                 {
                     if(score > alphaBeta.beta)
                     {
                         return score;
                     }
                     else if(score > alphaBeta.alpha)
                     {
                         alphaBeta.alpha = score;
                     }
                 }
             }
             if(score == Minimax.MINI_HAS_WON && player == -1)
             {
                 return Minimax.MINI_HAS_WON;
             }
             else if(score == Minimax.MAX_HAS_WON && player == 1)
             {
                 return Minimax.MAX_HAS_WON;
             }
             if(score * player > bestScore)
             {
                 bestScore = score * player;
             }
         }
         return bestScore;
     }
 
     @Override
     public Object clone()
     {
         try
         {
             return super.clone();
         }
         catch(Exception e)
         {
             return null;
         }
     }
 
     
     public int getCurrentScore()
     {
         int whitePawns;
         int whiteKnights = 0;
         int whiteBishops = 0;
         int whiteRooks = 0;
         int whiteQueens = 0;
         int whiteKings = 0;
         
         int blackPawns;
         int blackKnights = 0;
         int blackBishops = 0;
         int blackRooks = 0;
         int blackQueens = 0;
         int blackKings = 0;
 
         int doubledWhitePawns = 0;
         int doubledBlackPawns = 0;
         int backwardWhitePawns = 0;
         int backwardBlackPawns = 0;
         int isolatedWhitePawns = 0;
         int isolatedBlackPawns = 0;
 
         int fileAWhitePawns = 0;
         int fileBWhitePawns = 0;
         int fileCWhitePawns = 0;
         int fileDWhitePawns = 0;
         int fileEWhitePawns = 0;
         int fileFWhitePawns = 0;
         int fileGWhitePawns = 0;
         int fileHWhitePawns = 0;
         int fileABlackPawns = 0;
         int fileBBlackPawns = 0;
         int fileCBlackPawns = 0;
         int fileDBlackPawns = 0;
         int fileEBlackPawns = 0;
         int fileFBlackPawns = 0;
         int fileGBlackPawns = 0;
         int fileHBlackPawns = 0;
 
         for (int i = 0; i < 8; i++) { // Doubled Pawns and Backward Pawns Calculator
             int pos = i;
             switch (i) {
                 case 0:
                    while (pos < 56) {
                         if (myState.state[pos] == 1) {
                             fileAWhitePawns++;
                             // backward pawns calculator
                             if (pos <= 40) {
                                 if (myState.state[pos + 9] == 1 &&
                                         myState.state[pos + 17] == 7) {
                                     backwardWhitePawns++; // friendly pawn diagonal that's blocking enemy pawn?
                                 }
                             }
                             
                         }
                         else if (myState.state[pos] == 7) {
                             fileABlackPawns++;
                             if (pos >= 16) {      // backward pawns calculator
                                 if (myState.state[pos - 7] == 7 &&
                                         myState.state[pos - 15] == 1) {
                                     backwardBlackPawns++;
                                 }
                             }
                         }
                         pos += 8;
                     }
                     if (fileAWhitePawns > 1) { // if more than one like-colored pawn in file, doubled pawns
                         doubledWhitePawns += fileAWhitePawns;
                     }
                     if (fileABlackPawns > 1) {
                         doubledBlackPawns += fileABlackPawns;
                     }
                     break;
                 case 1:
                    while (pos < 57) {
                         if (myState.state[pos] == 1) {
                             fileBWhitePawns++;
                             if (pos <= 41) {
                                 if ((myState.state[pos + 7] == 1 &&
                                         myState.state[pos + 15] == 7) ||
                                         (myState.state[pos + 9] == 1 &&
                                         myState.state[pos + 17] == 7)) {
                                     backwardWhitePawns++;
                                 }
                             }
                         }
                         else if (myState.state[pos] == 7) {
                             fileBBlackPawns++;
                             if (pos >= 17) {
                                 if ((myState.state[pos - 9] == 7 &&
                                         myState.state[pos - 17] == 1) ||
                                         (myState.state[pos - 7] == 7 &&
                                         myState.state[pos - 15] == 1)) {
                                     backwardBlackPawns++;
                                 }
                             }
                         }
                         pos += 8;
                     }
                     if (fileBWhitePawns > 1) {
                         doubledWhitePawns += fileBWhitePawns;
                     }
                     if (fileBBlackPawns > 1) {
                         doubledBlackPawns += fileBBlackPawns;
                     }
                     break;
                 case 2:
                    while (pos < 58) {
                         if (myState.state[pos] == 1) {
                             fileCWhitePawns++;
                             if (pos <= 42) {
                                 if ((myState.state[pos + 7] == 1 &&
                                         myState.state[pos + 15] == 7) ||
                                     (   myState.state[pos + 9] == 1 &&
                                         myState.state[pos + 17] == 7)) {
                                     backwardWhitePawns++;
                                 }
                             }
                         }
                         else if (myState.state[pos] == 7) {
                             fileCBlackPawns++;
                             if (pos >= 18) {
                                 if ((myState.state[pos - 9] == 7 &&
                                         myState.state[pos - 17] == 1) ||
                                         (myState.state[pos - 7] == 7 &&
                                         myState.state[pos - 15] == 1)) {
                                     backwardBlackPawns++;
                                 }
                             }
                         }
                         pos += 8;
                     }
                     if (fileCWhitePawns > 1) {
                         doubledWhitePawns += fileCWhitePawns;
                     }
                     if (fileCBlackPawns > 1) {
                         doubledBlackPawns += fileCBlackPawns;
                     }
                     break;
                 case 3:
                    while (pos < 59) {
                         if (myState.state[pos] == 1) {
                             fileDWhitePawns++;
                             if (pos <= 43) {
                                 if ((myState.state[pos + 7] == 1 &&
                                         myState.state[pos + 15] == 7) ||
                                         (myState.state[pos + 9] == 1 &&
                                         myState.state[pos + 17] == 7)) {
                                     backwardWhitePawns++;
                                 }
                             }
                         }
                         else if (myState.state[pos] == 7) {
                             fileDBlackPawns++;
                             if (pos >= 19) {
                                 if ((myState.state[pos - 9] == 7 &&
                                         myState.state[pos - 17] == 1) ||
                                         (myState.state[pos - 7] == 7 &&
                                         myState.state[pos - 15] == 1)) {
                                     backwardBlackPawns++;
                                 }
                             }
                         }
                         pos += 8;
                     }
                     if (fileDWhitePawns > 1) {
                         doubledWhitePawns += fileDWhitePawns;
                     }
                     if (fileDBlackPawns > 1) {
                         doubledBlackPawns += fileDBlackPawns;
                     }
                     break;
                 case 4:
                    while (pos < 60) {
                         if (myState.state[pos] == 1) {
                             fileEWhitePawns++;
                             if (pos <= 44) {
                                 if ((myState.state[pos + 7] == 1 &&
                                         myState.state[pos + 15] == 7) ||
                                         (myState.state[pos + 9] == 1 &&
                                         myState.state[pos + 17] == 7)) {
                                     backwardWhitePawns++;
                                 }
                             }
                         }
                         else if (myState.state[pos] == 7) {
                             fileEBlackPawns++;
                             if (pos >= 20) {
                                 if ((myState.state[pos - 9] == 7 &&
                                         myState.state[pos - 17] == 1) ||
                                         (myState.state[pos - 7] == 7 &&
                                         myState.state[pos - 15] == 1)) {
                                     backwardBlackPawns++;
                                 }
                             }
                         }
                         pos += 8;
                     }
                     if (fileEWhitePawns > 1) {
                         doubledWhitePawns += fileEWhitePawns;
                     }
                     if (fileEBlackPawns > 1) {
                         doubledBlackPawns += fileEBlackPawns;
                     }
                     break;
                 case 5:
                    while (pos < 61) {
                         if (myState.state[pos] == 1) {
                             fileFWhitePawns++;
                             if (pos <= 45) {
                                 if ((myState.state[pos + 7] == 1 &&
                                         myState.state[pos + 15] == 7) ||
                                         (myState.state[pos + 9] == 1 &&
                                         myState.state[pos + 17] == 7)) {
                                     backwardWhitePawns++;
                                 }
                             }
                         }
                         else if (myState.state[pos] == 7) {
                             fileFBlackPawns++;
                             if (pos >= 21) {
                                 if ((myState.state[pos - 9] == 7 &&
                                         myState.state[pos - 17] == 1) ||
                                         (myState.state[pos - 7] == 7 &&
                                         myState.state[pos - 15] == 1)) {
                                     backwardBlackPawns++;
                                 }
                             }
                         }
                         pos += 8;
                     }
                     if (fileFWhitePawns > 1) {
                         doubledWhitePawns += fileFWhitePawns;
                     }
                     if (fileFBlackPawns > 1) {
                         doubledBlackPawns += fileFBlackPawns;
                     }
                     break;
                 case 6:
                    while (pos < 62) {
                         if (myState.state[pos] == 1) {
                             fileGWhitePawns++;
                             if (pos <= 46) {
                                 if ((myState.state[pos + 7] == 1 &&
                                         myState.state[pos + 15] == 7) ||
                                         (myState.state[pos + 9] == 1 &&
                                         myState.state[pos + 17] == 7)) {
                                     backwardWhitePawns++;
                                 }
                             }
                         }
                         else if (myState.state[pos] == 7) {
                             fileGBlackPawns++;
                             if (pos >= 22) {
                                 if ((myState.state[pos - 9] == 7 &&
                                         myState.state[pos - 17] == 1) ||
                                         (myState.state[pos - 7] == 7 &&
                                         myState.state[pos - 15] == 1)) {
                                     backwardBlackPawns++;
                                 }
                             }
                         }
                         pos += 8;
                     }
                     if (fileGWhitePawns > 1) {
                         doubledWhitePawns += fileGWhitePawns;
                     }
                     if (fileGBlackPawns > 1) {
                         doubledBlackPawns += fileGBlackPawns;
                     }
                     break;
                 case 7:
                    while (pos < 63) {
                         if (myState.state[pos] == 1) {
                             fileHWhitePawns++;
                             if (pos <= 47) {
                                 if (myState.state[pos + 7] == 1 &&
                                         myState.state[pos + 15] == 7) {
                                     backwardWhitePawns++;
                                 }
                             }
                         }
                         else if (myState.state[pos] == 7) {
                             fileHBlackPawns++;
                             if (pos >= 23) {
                                 if (myState.state[pos - 9] == 7 &&
                                         myState.state[pos - 17] == 1) {
                                     backwardBlackPawns++;
                                 }
                             }
                         }
                         pos += 8;
                     }
                     if (fileHWhitePawns > 1) {
                         doubledWhitePawns += fileHWhitePawns;
                     }
                     if (fileHBlackPawns > 1) {
                         doubledBlackPawns += fileHBlackPawns;
                     }
                     break;
             }
         } // End doubled pawns and backward pawns calculator
          
         for (int i = 0; i < 8; i++) {   // Isolated Pawns Calculator
             switch (i) {                // i represents file. 0 = A, 7 = H
                 case 0:
                     if (fileBWhitePawns == 0) {
                         isolatedWhitePawns += fileAWhitePawns;
                     }
                     if (fileBBlackPawns == 0) {
                         isolatedBlackPawns += fileABlackPawns;
                     }
                     break;
                 case 1:
                     if (fileAWhitePawns == 0 && fileCWhitePawns == 0) {
                         isolatedWhitePawns += fileBWhitePawns;
                     }
                     if (fileABlackPawns == 0 && fileCBlackPawns == 0) {
                         isolatedBlackPawns += fileBBlackPawns;
                     }
                     break;
                 case 2:
                     if (fileBWhitePawns == 0 && fileDWhitePawns == 0) {
                         isolatedWhitePawns += fileCWhitePawns;
                     }
                     if (fileBBlackPawns == 0 && fileDBlackPawns == 0) {
                         isolatedBlackPawns += fileCBlackPawns;
                     }
                     break;
                 case 3:
                     if (fileCWhitePawns == 0 && fileEWhitePawns == 0) {
                         isolatedWhitePawns += fileDWhitePawns;
                     }
                     if (fileCBlackPawns == 0 && fileEBlackPawns == 0) {
                         isolatedBlackPawns += fileDBlackPawns;
                     }
                     break;
                 case 4:
                     if (fileDWhitePawns == 0 && fileFWhitePawns == 0) {
                         isolatedWhitePawns += fileEWhitePawns;
                     }
                     if (fileDBlackPawns == 0 && fileFBlackPawns == 0) {
                         isolatedBlackPawns += fileEBlackPawns;
                     }
                     break;
                 case 5:
                     if (fileEWhitePawns == 0 && fileGWhitePawns == 0) {
                         isolatedWhitePawns += fileFWhitePawns;
                     }
                     if (fileEBlackPawns == 0 && fileGBlackPawns == 0) {
                         isolatedBlackPawns += fileFBlackPawns;
                     }
                     break;
                 case 6:
                     if (fileFWhitePawns == 0 && fileHWhitePawns == 0) {
                         isolatedWhitePawns += fileGWhitePawns;
                     }
                     if (fileFBlackPawns == 0 && fileHBlackPawns == 0) {
                         isolatedBlackPawns += fileGBlackPawns;
                     }
                     break;
                 case 7:
                     if (fileGWhitePawns == 0) {
                         isolatedWhitePawns += fileHWhitePawns;
                     }
                     if (fileGBlackPawns == 0) {
                         isolatedBlackPawns += fileHBlackPawns;
                     }
                     break;
             }
         } // End isolated pawns calculator
 
         for (int i = 0; i < myState.state.length; i++) { // Determines the type of piece and
             int piece = myState.state[i];                // stores the sum of the occurrences
             int pos = i;                                 // of that piece for evaluation.
             switch (piece) {                             // Don't need cases for 1 or 7 because the
                 case 2:                                  // amount of pawns in each file are stored
                     whiteKnights++;
                     break;
                 case 3:
                     whiteBishops++;
                     break;
                 case 4: 
                     whiteRooks++;
                     break;
                 case 5: 
                     whiteQueens++;
                     break;
                 case 6: 
                     whiteKings++;
                     break;
                 case 8: 
                     blackKnights++;
                     break;
                 case 9: 
                     blackBishops++;
                     break;
                 case 10: 
                     blackRooks++;
                     break;
                case 12: 
                     blackQueens++;
                     break;
                case 13: 
                     blackKings++;
                     break;
             }
         }
 
         whitePawns = (fileAWhitePawns + fileBWhitePawns + fileCWhitePawns + 
                 fileDWhitePawns + fileEWhitePawns + fileFWhitePawns + 
                 fileGWhitePawns + fileHWhitePawns);
         blackPawns = (fileABlackPawns + fileBBlackPawns + fileCBlackPawns + 
                 fileDBlackPawns + fileEBlackPawns + fileFBlackPawns + 
                 fileGBlackPawns +fileHWhitePawns);
         
         if (this.getPlayer() == -1) {// White player 
             return (int)((200 * (whiteKings - blackKings)) +
                     (9 * (whiteQueens - blackQueens)) +
                     (5 * (whiteRooks - blackRooks)) +
                     (3 * (whiteBishops - blackBishops + 
                         whiteKnights - blackKnights)) +
                     (1 * (whitePawns - blackPawns)) -
                     (0.5 * (doubledWhitePawns - doubledBlackPawns +
                         backwardWhitePawns - backwardBlackPawns +
                         isolatedWhitePawns - isolatedBlackPawns)) +
                     (0.1 * (this.listAllLegalMoves().size())));
         }
         else {
             return (int)((200 * (blackKings - whiteKings)) +
                     (9 * (blackQueens - whiteQueens)) +
                     (5 * (blackRooks - whiteRooks)) +
                     (3 * (blackBishops - whiteBishops + 
                         whiteKnights - whiteKnights)) +
                     (1 * (blackPawns - whitePawns)) -
                     (0.5 * (doubledBlackPawns - doubledWhitePawns +
                         backwardBlackPawns - backwardWhitePawns +
                         isolatedBlackPawns - isolatedWhitePawns)) +
                     (0.1 * (this.listAllLegalMoves().size())));
         }
     }
     
     public LinkedList<int[]> listAllLegalMoves()
     {
         Gamestate.GameMoves gm = myState.new GameMoves();
         LinkedList<int[]> moves = new LinkedList<int[]>();
         for(int i = 0; i < myState.state.length; i++)
         {
             
             ArrayList<Integer> tempList = new ArrayList<Integer>();
             byte temp = myState.state[i];
             if(temp == 1 && this.getPlayer() == -1)
             {
                 tempList = gm.movePawn(i, -1);
                 for(int j = 0; j < tempList.size(); j++)
                 {
                     moves.add(new int[]{i, tempList.get(j)});                    
                 }
                 //gm.validatePawnMove(temp, tempList);
                 
             }
             if(temp == 7 && this.getPlayer() == 1)
             {
                 tempList = gm.movePawn(i, 1);
                 for(int j = 0; j < tempList.size(); j++)
                 {
                     moves.add(new int[]{i, tempList.get(j)});                    
                 }
                // myState.validatePawn(tempList);                
             }
             if(temp == 2 && this.getPlayer() == -1)
             {
                 tempList = gm.moveKnight(i, -1);
                 for(int j = 0; j < tempList.size(); j++)
                 {
                     moves.add(new int[]{i,tempList.get(j)});
                 }
             }
             if(temp == 8 && this.getPlayer() == 1)
             {
                 tempList = gm.moveKnight(i, 1);
                 for(int j = 0; j < tempList.size(); j++)
                 {
                     moves.add(new int[]{i,tempList.get(j)});
                 }
             }
             if(temp == 3 && this.getPlayer() == -1)
             {
                 tempList = gm.moveBishop(i, -1);
                 for(int j = 0; j < tempList.size(); j++)
                 {
                     moves.add(new int[]{i,tempList.get(j)});
                 }
             }
             if(temp == 9 && this.getPlayer() == 1)
             {
                 tempList = gm.moveBishop(i, 1);
                 for(int j = 0; j < tempList.size(); j++)
                 {
                     moves.add(new int[]{i,tempList.get(j)});
                 }
             }
             if(temp == 4 && this.getPlayer() == -1)
             {
                 tempList = gm.moveRook(i, -1);
                 for(int j = 0; j < tempList.size(); j++)
                 {
                     moves.add(new int[]{i,tempList.get(j)});
                 }
             }
             if(temp == 10 && this.getPlayer() == 1)
             {
                 tempList = gm.moveRook(i, 1);
                 for(int j = 0; j < tempList.size(); j++)
                 {
                     moves.add(new int[]{i,tempList.get(j)});
                 }
             }
             if(temp == 5 && this.getPlayer() == -1)
             {
                 tempList = gm.moveQueen(i, -1);
                 for(int j = 0; j < tempList.size(); j++)
                 {
                     moves.add(new int[]{i,tempList.get(j)});
                 }
             }
             if(temp == 11 && this.getPlayer() == 1)
             {
                 tempList = gm.moveQueen(i, 1);
                 for(int j = 0; j < tempList.size(); j++)
                 {
                     moves.add(new int[]{i,tempList.get(j)});
                 }
             }
             if(temp == 6 && this.getPlayer() == -1)
             {
                 tempList = gm.moveKing(i, -1);
                 for(int j = 0; j < tempList.size(); j++)
                 {
                     moves.add(new int[]{i,tempList.get(j)});
                 }
             }
             if(temp == 7 && this.getPlayer() == 1)
             {
                 tempList = gm.moveKing(i, 1);
                 for(int j = 0; j < tempList.size(); j++)
                 {
                     moves.add(new int[]{i,tempList.get(j)});
                 }
             }
             
         }
         
         return moves;
     }
     
     public boolean moveAction(int[] move)
     {
         //work with myState.state as your byte array.perform move then handle
         //conditions like remove oppenent piece or get piece back (which we need to implement eventually)
         // can use this.player to get who holds current move(-1=W , 1=B)
         //or can just simply tell by what number is in the initial position myState.state[move[0]] 
         //move[0] holds inital positon, move[1] holds resulting position
         //myState.state[position] holds the number of that position.
         
         if(myState.state[move[0]] < 7 && myState.state[move[1]] == 12 )
             return true;
         else if(myState.state[move[0]] >= 7 && myState.state[move[1]] == 6)
             return true;
         else if (myState.state[move[0]] == 1 && move[1] >= 56) { //Pawn reaching end?
             myState.state[move[1]] = (byte) 5;                     //Queen him
         }
         else if (myState.state[move[0]] == 7 && move[1] <= 7) {
             myState.state[move[1]] = (byte) 11;
         }
         else {
             myState.state[move[1]] = myState.state[move[0]];
         }
         myState.state[move[0]] = (byte) 0;  // Replace the starting position with 0
         return false;
     }
     public boolean doMove(int[] move)
     {
         boolean answer = this.moveAction(move);
         this.player *= -1;
         return answer;
     }
     public void staleMate()
     {
         
     }
}
