 package utils;
 
 import java.util.ArrayList;
 
 import chessboard.Chessboard;
 
 import moveFinders.BlackMoveFinder;
 import moveFinders.WhiteMoveFinder;
 
 import exceptions.BlackMoveFinderException;
 import exceptions.ChessboardException;
 import exceptions.MCTUtilsException;
 import exceptions.WhiteMoveFinderException;
 import exec.Constants;
 import exec.MCTNode;
 
 public class MCTUtils {
 
     /**
      * computes MCT rating for current node
      * 
      * @param node
      *            node of which we want to compute rating of
      * @param methodOfComputing
      *            which UCT we'll use
      * @return node rating
      * @throws MCTUtilsException
      */
     public static double computeNodeRating(MCTNode node, int methodOfComputing)
             throws MCTUtilsException {
         if (node.visitCount == 0 || node.parent.visitCount == 0) { throw new MCTUtilsException(); }
         if (methodOfComputing == 1) {
             // poteze belega
             double value = (double) node.numberOfMatsInNode
                     / (double) node.visitCount;
             double temp = Math.log(node.parent.visitCount) / node.visitCount;
             return value + node.c * Math.sqrt(temp);
         }
         else if (methodOfComputing == 2) {
             // poteze crnega
             double value = (double) node.numberOfMatsInNode
                     / (double) node.visitCount;
             double temp = Math.log(node.parent.visitCount) / node.visitCount;
             return (1 - value) + node.c * Math.sqrt(temp);
         }
 
         throw new MCTUtilsException("neveljavna methoda of computing: "
                 + methodOfComputing);
     }
 
 
     /**
      * Searches children of current node and returns those with highest rating.
      * 
      * @param node
      *            parent of children we computing rating.
      * @param whiteRankingMethod
      *            rank computing method for white player.
      * @param blackRankingMethod
      *            rank computing method for black player.
      * @return list of indexes, which tell us which children have highest
      *         rating.
      * @throws MCTUtilsException
      */
     public static ArrayList<Integer> getInedexesWithMaxRating(MCTNode node,
             int whiteRankingMethod, int blackRankingMethod)
             throws MCTUtilsException {
         ArrayList<Integer> rez = new ArrayList<Integer>();
 
         if (node.nextMoves == null) { throw new MCTUtilsException(
                 "node nima razvitih naslednikov"); }
         if (node.nextMoves.size() == 0) { return rez; }
 
        // double maxRating = computeNodeRating(node.nextMoves.get(0),
        // whiteRankingMethod);
        double maxRating = Double.MIN_VALUE;
        double currRating = Double.MIN_VALUE;
 
         for (int x = 0; x < node.nextMoves.size(); x++) {
             if (node.isWhitesMove) {
                 currRating = MCTUtils.computeNodeRating(node.nextMoves.get(x),
                         whiteRankingMethod);
             }
             else {
                 currRating = MCTUtils.computeNodeRating(node.nextMoves.get(x),
                         blackRankingMethod);
             }
 
             if (currRating > maxRating) {
                 maxRating = currRating;
                 rez = new ArrayList<Integer>();
             }
 
             if (currRating == maxRating) {
                 rez.add(x);
             }
         }
 
         // if flag is on then we only use those with highest visit count
         if (Constants.SELECTION_ALSO_USES_VISIT_COUNT_FOR_NODE_CHOOSING) {
             int maxVisitCount = Integer.MIN_VALUE;
             ArrayList<Integer> filteredRez = new ArrayList<Integer>();
             for (Integer x : rez) {
                 MCTNode currNode = node.nextMoves.get(x);
                 if (currNode.visitCount > maxVisitCount) {
                     maxVisitCount = currNode.visitCount;
                     filteredRez = new ArrayList<Integer>();
                 }
                 if (currNode.visitCount == maxVisitCount) {
                     filteredRez.add(x);
                 }
             }
             return filteredRez;
         }
         return rez;
     }
 
 
     public static int findNextMove(MCTNode node, int whiteSimuationStrategy,
             int blackSimulationStrategy) throws ChessboardException,
             WhiteMoveFinderException, BlackMoveFinderException {
         if (node.isWhitesMove) {
             Chessboard temp = new Chessboard("temp", node);
             return WhiteMoveFinder.findWhiteMove(temp, whiteSimuationStrategy);
         }
         else {
             Chessboard temp = new Chessboard("temp", node);
             return BlackMoveFinder.findBlackKingMove(temp,
                     blackSimulationStrategy);
         }
     }
 
 }
