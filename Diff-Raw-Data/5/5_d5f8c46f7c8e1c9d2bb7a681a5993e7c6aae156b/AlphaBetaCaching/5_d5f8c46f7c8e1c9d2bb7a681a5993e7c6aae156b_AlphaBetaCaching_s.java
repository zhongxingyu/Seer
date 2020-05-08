 package search.alphabeta.caching;
 
 import base.board.Board;
 import search.TreeSearch;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: Sep 23, 2009
  * Time: 8:01:05 PM
  * This aims to do the same thing as AlphaBeta, except it caches positions already seen
  */
 public class AlphaBetaCaching {
     private int nodeCount;
     private final int numBuckets;
     private final PositionCache[] cache;
 
     public AlphaBetaCaching(int numBuckets) {
         this.numBuckets = numBuckets;
         this.cache = new PositionCache[numBuckets];
         for (int i=0 ; i<numBuckets ; i++) {
             this.cache[i] = new PositionCache();
         }
         nodeCount=0;
     }
 
     public int search(int color, Board[] boards, int empties, boolean alreadyPassed, int alpha, int beta) {
         nodeCount++;
 
         final int hashCode = boards[empties].hashCodeWithColor(color);
         final PositionCache positionCache = cache[hashCode % numBuckets];
         if (hashCode == positionCache.hashCode) { //we have seen this position before, maybe we can do something useful with this
             if (positionCache.lowerBound == positionCache.upperBound) { //if they equal to each other, we already know the score of the position
                 return positionCache.lowerBound; //doesn't matter which one we return
             }
             if (positionCache.lowerBound >= beta) { //if the lowest possible value is already above our window, we have a cutoff
                 return positionCache.lowerBound;
             }
             if (positionCache.upperBound <= alpha) { //likewise, if the position is going to be lower than our window
                 return positionCache.upperBound;
             }
 
         } else {
             positionCache.setNewHashCode(hashCode);
         }
 
 
         int curScore = TreeSearch.negInf;
         for (int curLocation : Board.allMoves) {
             if (boards[empties].isMoveValid(color,curLocation)) {
                 boards[empties-1].copyBoard(boards[empties]);
                 boards[empties-1].makeMove(color,curLocation);
                curScore = Math.max(curScore,-search(-color,boards,empties-1,false,-beta,-alpha));
                alpha = Math.max(alpha, curScore);
                if (beta <= alpha) {//beta cutoff
                     break;
                 }
             }
         }
 
         if (curScore != TreeSearch.negInf) {
             //update cache before returning
             if (curScore <= alpha) { //if curScore is lower than alpha, we don't know the exact score, but we know it can't be better than curScore
                 positionCache.upperBound = curScore;
             } else if (curScore >= beta) {
                 positionCache.lowerBound = curScore;
             } else { //if between alpha and beta, we know *exactly* what the score is
                 positionCache.lowerBound = positionCache.upperBound = curScore;
             }
             return curScore;
         } else { //this happens in case of a pass
             if (alreadyPassed) {
                 return boards[empties].getBlackMinusWhite() * color;
             } else {
                 return -search(-color,boards,empties,true,-beta,-alpha);
             }
         }
     }
 
     public int getNodeCount() {
         return nodeCount;
     }
 }
