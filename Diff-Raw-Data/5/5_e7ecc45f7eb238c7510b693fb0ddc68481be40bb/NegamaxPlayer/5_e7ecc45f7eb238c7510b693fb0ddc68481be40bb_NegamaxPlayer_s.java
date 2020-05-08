 package iago.players;
 
 import iago.Board;
 import iago.Move;
 import iago.features.*;
 
 import java.util.ArrayList;
 import java.util.Set;
 
 
 public class NegamaxPlayer extends AbstractPlayer {
     
     public static final int DEFAULT_DEPTH = 10;
     public static final int DEFAULT_SORT_DEPTH = 6; 
     private static final int INF = 65535;
     
     private int searchDepth;
     private int sortDepth;
     private Move bestMove;
     private FeatureSet features = new FeatureSet("negamax");
     
     @Override
     public Move chooseMove(Board board) {
         this.bestMove = null;
         
         negamax(board, getColour(), 1, -INF, INF, getSearchDepth(), 0);
         
         if (this.bestMove == null) {
             return pickGreedyMove(board);
         }
         
         return this.bestMove;
     }
     
     private double negamax(Board board, PlayerType player,
             int colour, double alpha, double beta, int depth, int plies) {
         if ((depth <= 0) || board.isVictory()) {
             return colour * features.score(board, player);
         }
         
         PlayerType nextPlayer = player.getOpponent();
         Set<Move> successors;
         if (plies < sortDepth) {
             successors = board.validMovesSorted(player);
         } else {
             successors = board.validMoves(player);
         }
         Move lBestMove = null;
         
         if (successors.size() == 0) {
             return -negamax(board, nextPlayer, -colour, -beta, -alpha, depth-1, plies+1);
         }
         
         for (Move m : successors) {
             double v = -negamax(board.apply(m, player, false), nextPlayer,
                     -colour, -beta, -alpha, depth-1, plies+1);
             if (v >= beta) {
                if (player == getColour()) {
                     this.bestMove = m;
                 }
                 return v;
             }
             if (v > alpha) {
                 alpha = v;
                 if (player == getColour()) {
                     lBestMove = m;
                 }
             }
         }
         
        if (lBestMove != null) {
             this.bestMove = lBestMove;
         }
         return alpha;
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
     
     //TODO: add a constructor that uses a minimal Feature Set if no feature set is specified
     
     public NegamaxPlayer(PlayerType colour) {
         this(colour, DEFAULT_DEPTH, DEFAULT_SORT_DEPTH);
     }
     
     public NegamaxPlayer(PlayerType colour, int depth) {
         this(colour, depth, DEFAULT_SORT_DEPTH);
     }
     
     public NegamaxPlayer(PlayerType colour, int depth, int sortDepth) {
         super(colour);
         this.searchDepth = depth; 
         this.setSortDepth(sortDepth);
         //Choose the features here
         features.add(new StoneCount(1.0));
     }
     
     public void setSearchDepth(int searchDepth) {
         this.searchDepth = searchDepth;
     }
     
     public int getSearchDepth() {
         return searchDepth;
     }
     
     public void setSortDepth(int sortDepth) {
         this.sortDepth = sortDepth;
     }
     
     public int getSortDepth() {
         return sortDepth;
     }
 
     public void setFeatureSet(FeatureSet features) {
     	this.features = features;
     }
     
     public FeatureSet getFeatureSet() {
     	return features;
     }
     
 }
