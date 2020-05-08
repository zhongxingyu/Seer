 package moveChoosers;
 
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import utils.MCTUtils;
 import exec.MCTNode;
 
 /**
  * Class that handles choosing moves for white from MC Tree.
  * 
  * @author Andraz
  */
 public class WhiteMoveChooser {
 
     /** Random used by this class */
     private static Random random = new Random();
     /** Logger */
     private static Logger log    = Logger.getLogger("MCTS.WhiteMoveChooser");
 
 
     public WhiteMoveChooser() {}
 
 
     /**
      * Chooses black move from {@link MCTNode}
      * 
      * @param node
      *            root node
      * @param strategy
      *            strategy on which we choose ply move
      * @return chosen index of nodes child
      */
     public static int chooseAMove(MCTNode node, WhiteChooserStrategy strategy) {
         int rez = -1;
         switch (strategy) {
             case RANDOM:
                 rez = WhiteMoveChooser.chooseRandomMove(node);
                 break;
             case VISIT_COUNT:
                 rez = WhiteMoveChooser.chooseMaxVisitCountMove(node);
                 break;
             case RATING:
                 rez = WhiteMoveChooser.chooseMaxRatingMove(node);
                 break;
         }
         String logString = "V polpotezi " + (node.moveDepth + 1)
                 + " je beli izbiral med potezami :\r\n"
                 + node.descendantsToString() + "Izbral si pa je potezo "
                 + (rez + 1);
 
        WhiteMoveChooser.log.fine(logString);
 
         return rez;
     }
 
 
     /**
      * Gets son nodes with higheset visit count from <code>node</code>.
      * 
      * @param node
      *            parent of node from which we choose
      * @return indexes of son nodes with highest visit count
      */
     private static int chooseMaxVisitCountMove(MCTNode node) {
         ArrayList<Integer> rezCand = new ArrayList<Integer>();
 
         int maxVC = 0;
         for (int x = 0; x < node.nextMoves.size(); x++) {
             if (node.nextMoves.get(x).visitCount > maxVC) {
                 maxVC = node.nextMoves.get(x).visitCount;
                 rezCand = new ArrayList<Integer>();
             }
 
             if (node.nextMoves.get(x).visitCount == maxVC) {
                 rezCand.add(x);
             }
         }
 
         int chosenMove = WhiteMoveChooser.random.nextInt(rezCand.size());
         chosenMove = rezCand.get(chosenMove);
 
         return chosenMove;
     }
 
 
     /**
      * Return index of one of nodes with highest rating
      * 
      * @param node
      *            parent of nodes from which we choose
      * @return ply number
      */
     private static int chooseMaxRatingMove(MCTNode node) {
         ArrayList<Integer> rezCand = new ArrayList<Integer>();
         double maxRating = -Double.MAX_VALUE;
 
         for (int x = 0; x < node.nextMoves.size(); x++) {
             double currRating = MCTUtils.computeNodeRating(node.nextMoves
                     .get(x));
             if (currRating > maxRating) {
                 rezCand = new ArrayList<Integer>();
                 maxRating = currRating;
             }
 
             if (currRating == maxRating) {
                 rezCand.add(x);
             }
         }
 
         int chosenMove = WhiteMoveChooser.random.nextInt(rezCand.size());
         chosenMove = rezCand.get(chosenMove);
 
         return chosenMove;
     }
 
 
     /**
      * Return index of random son node
      * 
      * @param node
      *            parent of nodes from which we choose
      * @return ply number that belongs to one of nodes childer, chosen of random
      */
     private static int chooseRandomMove(MCTNode node) {
         int rez = WhiteMoveChooser.random.nextInt(node.nextMoves.size());
         return rez;
     }
 
 }
