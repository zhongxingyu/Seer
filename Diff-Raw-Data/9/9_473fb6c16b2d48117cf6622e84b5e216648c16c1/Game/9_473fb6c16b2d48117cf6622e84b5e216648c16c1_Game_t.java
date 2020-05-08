 package ee.ut.cs.sysmodel;
 
 import ee.ut.cs.sysmodel.gui.GFrame;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * User: Karl Kilgi
  * Date: 10/7/12
  * Time: 10:10 PM
  */
 
 public class Game {
 
     private int betSize = 1;
     private Player activePlayer = Player.NONE;
     private Point[] points = new Point[26];
     private List<Move> availableMoves = new LinkedList<Move>();
     private List<Integer> throwResults = new LinkedList<Integer>();
     private Dice dice = new Dice();
     private List<Integer> diceMovesLeft;
     private GFrame frame;
 
     Game() {
         initializeGame();
         frame = new GFrame(this);
         onDiceThrow();
     }
 
     private void initializeGame() {
 
         for (int i = 0; i <= 25; i++) {
             points[i] = new Point(i);
         }
         initializeCheckers();
     }
 
     private void initializeCheckers() {
         //For Player1
         points[24].setNumberOfCheckers(2);
         points[24].setPlayer(Player.PLAYER1);
 
         points[13].setNumberOfCheckers(5);
         points[13].setPlayer(Player.PLAYER1);
 
         points[8].setNumberOfCheckers(3);
         points[8].setPlayer(Player.PLAYER1);
 
         points[6].setNumberOfCheckers(5);
         points[6].setPlayer(Player.PLAYER1);
 
         //For Player2
         points[1].setNumberOfCheckers(2);
         points[1].setPlayer(Player.PLAYER2);
 
         points[12].setNumberOfCheckers(5);
         points[12].setPlayer(Player.PLAYER2);
 
         points[17].setNumberOfCheckers(3);
         points[17].setPlayer(Player.PLAYER2);
 
         points[19].setNumberOfCheckers(5);
         points[19].setPlayer(Player.PLAYER2);
     }
 
     public static void main(String[] args) {
         new Game();
     }
 
     public void increaseBet() {
         if (betSize < 64) {
             betSize = betSize * 2;
         }
     }
 
     private void sendCheckerToBar() {
         if (activePlayer == Player.PLAYER1) {
             Player.PLAYER2.getBar().addChecker();
         } else {
             Player.PLAYER1.getBar().addChecker();
         }
         frame.refresh();
     }
 
     public void onMove(int fromPoint, int toPoint) {
         System.out.println(getActivePlayer().getName() + " made move " + fromPoint + "->" + toPoint);
         boolean movesLeft;
         Move playerMove = new Move(fromPoint, toPoint);
         if (availableMoves.contains(playerMove)) {
             if (activePlayer.getBar().isEmpty()) {
                 points[playerMove.getFromPoint()].removeChecker();
             } else {
                 activePlayer.getBar().removeChecker();
             }
             boolean checkerToBar = points[playerMove.getToPoint()].addChecker(activePlayer);
             if (checkerToBar) {
                 sendCheckerToBar();
             }
             setDiceMovesLeft(playerMove.getFromPoint(), playerMove.getToPoint());
             movesLeft = diceMovesLeft.isEmpty();
             if (!movesLeft) {
                 setAvailableMoves(diceMovesLeft);
                 frame.refresh();
             } else {
                 frame.refresh();
                 changeActivePlayer();
             }
             if (availableMoves.isEmpty()) {
                 frame.refresh();
                 changeActivePlayer();
             }
         } else {
             frame.showIllegalMovePopUp();
         }
     }
 
     private void onDiceThrow() {
         throwResults = dice.throwDice();
         if (activePlayer == Player.NONE) {
             setStartingPlayer();
         }
         diceMovesLeft = throwResults;
         setAvailableMoves(throwResults);
         frame.refresh();
         if (availableMoves.isEmpty()) {
             changeActivePlayer();
         }
     }
 
     private void setStartingPlayer() {
         frame.showBeginningPopup();
         while (true) {
             if (throwResults.get(0) > throwResults.get(1)) {
                 activePlayer = Player.PLAYER1;
                 frame.showStartingPlayerPopup(throwResults);
                 break;
             }
             if (throwResults.get(0) < throwResults.get(1)) {
                 activePlayer = Player.PLAYER2;
                 frame.showStartingPlayerPopup(throwResults);
                 break;
             } else {
                 throwResults = dice.throwDice();
             }
         }
     }
 
     private void changeActivePlayer() {
         if (activePlayer == Player.PLAYER1) {
             setActivePlayer(Player.PLAYER2);
         } else {
             setActivePlayer(Player.PLAYER1);
         }
         frame.showChangePlayersPopup();
         onDiceThrow();
     }
 
     private void setAvailableMoves(List<Integer> throwResult) {
         availableMoves.clear();
         int toPoint;
         boolean homeGame;
         if (isPlayerWon()) {
             onWin();
             return;
         }
         if (throwResult.isEmpty()) {
             return;
         }
         if (!activePlayer.getBar().isEmpty()) {
             setAvailableMovesOutOfBar(throwResult);
         } else {
             List<Point> populatedPoints = getActivePlayerPopulatedPoints();
             homeGame = isHomeGame(populatedPoints);
             for (Point populatedPoint : populatedPoints) {
                 if (populatedPoint.getPosition() != activePlayer.getHomePoint()) {
                     for (int i = 0; i <= 1; i++) {
                         int fromPoint = populatedPoint.getPosition();
                         int explodedHomePoint = Integer.MAX_VALUE;
                         if (activePlayer == Player.PLAYER1) {
                             toPoint = fromPoint - throwResult.get(i);
                             if (toPoint <= activePlayer.getHomePoint()) {
                                 explodedHomePoint = toPoint;
                                 toPoint = activePlayer.getHomePoint();
                             }
                         } else {
                             toPoint = fromPoint + throwResult.get(i);
                             if (toPoint >= activePlayer.getHomePoint()) {
                                 explodedHomePoint = toPoint;
                                 toPoint = activePlayer.getHomePoint();
                             }
                         }
                         if (points[toPoint].canAdd(activePlayer)) {
                             boolean explodedHomeMove = isExplodedHomeMoveAllowed(homeGame, fromPoint, toPoint, explodedHomePoint, populatedPoints, populatedPoint);
                             if (explodedHomeMove) {
                                 availableMoves.add(new Move(fromPoint, toPoint));
                             }
                             if (toPoint != activePlayer.getHomePoint()) {
                                 availableMoves.add(new Move(fromPoint, toPoint));
                             }
                         }
                         //No need to populate list with same moves
                         if (i == 0) {
                             if (throwResult.size() == 2)
                                 if (throwResult.get(i).equals(throwResult.get(i + 1))) {
                                     break;
                                 }
                         }
                         if (throwResult.size() > 2 || throwResult.size() == 1) {
                             break;
                         }
                     }
                 }
             }
         }
         System.out.println("Available moves " + getAvailableMovesToString());
     }
 
     private String getAvailableMovesToString() {
         String moves = "";
         for (Move a : availableMoves) {
             moves += a.getFromPoint() + ">" + a.getToPoint() + " ";
         }
         return moves;
     }
 
     private void setAvailableMovesOutOfBar(List<Integer> throwResult) {
         int fromPoint;
         int toPoint;
         for (int i = 0; i <= 1; i++) {
             if (activePlayer == Player.PLAYER1) {
                 fromPoint = 25;
                 toPoint = fromPoint - throwResult.get(i);
                 if (points[toPoint].canAdd(activePlayer)) {
                     availableMoves.add(new Move(fromPoint, toPoint));
                 }
             } else {
                 fromPoint = 0;
                 toPoint = fromPoint + throwResult.get(i);
                 if (points[toPoint].canAdd(activePlayer)) {
                     availableMoves.add(new Move(fromPoint, toPoint));
                 }
             }
            if (i == 0) {
                if (throwResult.size() == 2)
                    if (throwResult.get(i).equals(throwResult.get(i + 1))) {
                        break;
                    }
            }
            if (throwResult.size() > 2 || throwResult.size() == 1) {
                break;
            }
         }
     }
 
     private List<Point> getActivePlayerPopulatedPoints() {
         List<Point> populatedPoints = new LinkedList<Point>();
         for (int i = 0; i <= 25; i++) {
             if (points[i].getPlayer() == activePlayer) {
                 populatedPoints.add(points[i]);
             }
         }
         return populatedPoints;
     }
 
     public Player getActivePlayer() {
         return activePlayer;
     }
 
     public Player getInActivePlayer() {
         if (activePlayer == Player.NONE) {
             return activePlayer; // LOL
         }
         return activePlayer == Player.PLAYER1 ? Player.PLAYER2 : Player.PLAYER1;
     }
 
     private void setActivePlayer(Player activePlayer) {
         this.activePlayer = activePlayer;
     }
 
     public int getBetSize() {
         return betSize;
     }
 
     public Point[] getPoints() {
         return points;
     }
 
     private void setDiceMovesLeft(int fromPoint, int toPoint) {
         Integer usedMove;
         int smallestPossibleMove = Integer.MAX_VALUE;
         if (activePlayer == Player.PLAYER1) {
             usedMove = fromPoint - toPoint;
         } else {
             usedMove = toPoint - fromPoint;
         }
         //for exploded moves
         if (activePlayer.getHomePoint() == toPoint) {
             for (Integer diceMove : diceMovesLeft) {
                 if (diceMove >= usedMove && diceMove < smallestPossibleMove) {
                     smallestPossibleMove = diceMove;
                 }
             }
             usedMove = smallestPossibleMove;
         }
 
         diceMovesLeft.remove(usedMove);
     }
 
     public List<Integer> getDiceMovesLeft() {
         return diceMovesLeft;
     }
 
     private boolean isHomeGame(List<Point> populatedPoints) {
         boolean homeGame = true;
         for (Point populatedPoint : populatedPoints) {
             if (activePlayer == Player.PLAYER1) {
                 if (populatedPoint.getPosition() > 6) {
                     homeGame = false;
                 }
             }
             if (activePlayer == Player.PLAYER2) {
                 if (populatedPoint.getPosition() < 19) {
                     homeGame = false;
                 }
             }
             if (!activePlayer.getBar().isEmpty()) {
                 homeGame = false;
                 break;
             }
         }
         return homeGame;
     }
 
     private boolean isExplodedHomeMoveAllowed(boolean homeGame, int fromPoint, int toPoint, int explodedHomePoint, List<Point> populatedPoints, Point populatedPointToMove) {
         boolean explodedHomeMove = false;
         if (explodedHomePoint != Integer.MAX_VALUE) {
             if (homeGame && toPoint == activePlayer.getHomePoint()) {
                 if (explodedHomePoint == activePlayer.getHomePoint()) {
                     availableMoves.add(new Move(fromPoint, toPoint));
                 } else {
                     Point furthestPointFromHome = populatedPointToMove;
                     for (Point populatedPointOnBoard : populatedPoints) {
                         if (activePlayer == Player.PLAYER1) {
                             if (populatedPointToMove.getPosition() < populatedPointOnBoard.getPosition() && explodedHomePoint < activePlayer.getHomePoint()) {
                                 furthestPointFromHome = populatedPointOnBoard;
                             }
                         } else {
                             if (populatedPointToMove.getPosition() > populatedPointOnBoard.getPosition() && explodedHomePoint > activePlayer.getHomePoint()) {
                                 furthestPointFromHome = populatedPointOnBoard;
                             }
                         }
                     }
                     if (activePlayer == Player.PLAYER1) {
                         if (populatedPointToMove.getPosition() >= furthestPointFromHome.getPosition()) {
                             explodedHomeMove = true;
                         }
                     } else {
                         if (populatedPointToMove.getPosition() <= furthestPointFromHome.getPosition()) {
                             explodedHomeMove = true;
                         }
                     }
                 }
 
             }
         }
         return explodedHomeMove;
     }
 
     private boolean isPlayerWon() {
         return points[activePlayer.getHomePoint()].getNumberOfCheckers() == 15;
     }
 
     public void onWin() {
         System.out.println("Game over, player: " + activePlayer + " won");
     }
 
     public void startNewGame() {
         cleanAll();
         initializeCheckers();
         onDiceThrow();
     }
 
     private void cleanAll() {
         for (int i = 0; i <= 25; i++) {
             points[13].setNumberOfCheckers(0);
             points[13].setPlayer(Player.NONE);
         }
         activePlayer = Player.NONE;
     }
 }
