 package iago;
 
 import java.util.Set;
 
 
 public class AlphaBetaPlayer extends AbstractPlayer {
     
     public static final int DEFAULT_DEPTH = 6;
     private static final int INF = 65535;
     private static final PlayerType MAX_PLAYER = PlayerType.WHITE;
     
     private int searchDepth;
     private Move bestMove;
     private boolean isMaxPlayer;
     
     @Override
     public Move chooseMove(Board board) {
         this.bestMove = null;
         
         minimax(board, getColour(), -INF, INF, getSearchDepth());
         
         if (this.bestMove == null) {
             return pickGreedyMove(board);
         }
         
         return this.bestMove;
     }
     
     private int minimax(Board board, PlayerType player,
             int alpha, int beta, int depth) {
         if ((depth <= 0) || (board.movesRemaining() == 0)) {
             return board.scoreBoardObjectively();
         }
         
         PlayerType nextPlayer = player.getOpponent();
         Set<Move> successors = board.validMoves(player);
         Move lBestMove = null;
         
         if (player == MAX_PLAYER) {
             for (Move m : successors) {
                 int v = minimax(board.apply(m, player), nextPlayer,
                                 alpha, beta, depth-1);
                 if (v > alpha) {
                     alpha = v;
                     if (isMaxPlayer) {
                         lBestMove = m;
                     }
                     if (alpha >= beta) {
                         break;
                     }
                 }
                 
             }
         } else {
             for (Move m : successors) {
                 int v = minimax(board.apply(m, player), nextPlayer,
                                 alpha, beta, depth-1);
                 if (v < beta) {
                     beta = v;
                     if (!isMaxPlayer) {
                         lBestMove = m;
                     }
                     if (beta <= alpha) {
                         break;
                     }
                 }
                 
             }
         }
         
         this.bestMove = lBestMove;
         if (player == MAX_PLAYER) {
             return alpha;
         } else {
             return beta;
         }
     }
     
     private Move pickGreedyMove(Board board) {
         Set<Move> legalMoves = board.validMoves(getColour());
         Move bestMove = Move.NO_MOVE;
         int maxScore = -INF;
         
         for (Move m : legalMoves) {
             int score = board.scoreMove(m, getColour());
             if (score > maxScore) {
                 maxScore = score;
                 bestMove = m;
             }
         }
         
         return bestMove;
     }
     public AlphaBetaPlayer(PlayerType colour) {
         this(colour, DEFAULT_DEPTH);
     }
     
     public AlphaBetaPlayer(PlayerType colour, int depth) {
         super(colour);
         this.searchDepth = depth;
         this.isMaxPlayer = (colour == MAX_PLAYER); 
     }
     
     public void setSearchDepth(int searchDepth) {
         this.searchDepth = searchDepth;
     }
     
     public int getSearchDepth() {
         return searchDepth;
     }
     
 }
