 package ee.ut.cs.sysmodel;
 
 import ee.ut.cs.sysmodel.gui.GFrame;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * User: Karl Kilgi
  * Date: 10/7/12
  * Time: 10:10 PM
  */
 // TODO - kõik meetodid, mida ainult klassi sees kasutatakse -----> privaatseks
 // TODO - getterid infopaneeli jaoks
 // TODO - lisada selline loogika, et saaks uut mängu alustada (nt pärast käesoleva katkestamist või läbisaamist)
 
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
         Game game = new Game();
     }
 
     public void increaseBet() {
         betSize = betSize * 2;
     }
 
     public void sendCheckerToBar() {
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
             getDiceMovesLeft(playerMove.getFromPoint(), playerMove.getToPoint());
             movesLeft = diceMovesLeft.isEmpty();
             if (!movesLeft) {
                 setAvailableMoves(diceMovesLeft);
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
 
     public List<Integer> onDiceThrow() {
         throwResults = dice.throwDice();
         if (activePlayer == Player.NONE) {
             frame.showBeginningPopup();
             boolean startingGame = true;
             while (startingGame) {
                 if (throwResults.get(0) > throwResults.get(1)) {
                     activePlayer = Player.PLAYER1;
                     startingGame = false;
                 }
                 if (throwResults.get(0) < throwResults.get(1)) {
                     activePlayer = Player.PLAYER2;
                     startingGame = false;
                 } else {
                     throwResults = dice.throwDice();
                 }
             }
         }
         diceMovesLeft = throwResults;
         setAvailableMoves(throwResults);
         frame.refresh();
         if (availableMoves.isEmpty()) {
             changeActivePlayer();
             return null;
         } else {
             frame.showDiceResultsPopup(throwResults);
             return throwResults;
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
 
     public void setAvailableMoves(List<Integer> throwResult) {
         availableMoves.clear();
         int toPoint;
         boolean homeGame;
         if (throwResult.isEmpty()) {
             return;
         }
         if (!activePlayer.getBar().isEmpty()) {
             setAvailableMovesOutOfBar(throwResult);
         } else {
             List<Point> populatedPoints = getActivePlayerPopulatedPoints();
             homeGame = isHomeGame(populatedPoints);
             for (Point populatedPoint : populatedPoints) {
                 for (int i = 0; i <= 1; i++) {
                     int fromPoint = populatedPoint.getPosition();
                     int explodedHomePoint = Integer.MAX_VALUE;
                     if (activePlayer == Player.PLAYER1) {
                         toPoint = fromPoint - throwResult.get(i);
                        if(toPoint < Player.PLAYER1.getHomePoint())            {
                            toPoint = Player.PLAYER1.getHomePoint();
                        }
                     } else {
                         toPoint = fromPoint + throwResult.get(i);
                        if(toPoint > Player.PLAYER2.getHomePoint())            {
                            toPoint = Player.PLAYER2.getHomePoint();
                        }
                     }
                     //Detecting if player can put the checker to home
                     if (homeGame) {
                         if (toPoint < activePlayer.getHomePoint()) {
                             explodedHomePoint = toPoint;
                             toPoint = activePlayer.getHomePoint();
                         }
                         if (toPoint > activePlayer.getHomePoint()) {
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
                     if (throwResult.size() > 2 || throwResult.size() == 1) {
                         break;
                     }
                 }
             }
         }
         System.out.println("Available moves " + doShit());
     }
 
     private String doShit() {
         String asd = "";
         for (Move a : availableMoves) {
             asd += a.getFromPoint() + ">" + a.getToPoint() + " ";
         }
         return asd;
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
         }
     }
 
     public List<Point> getActivePlayerPopulatedPoints() {
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
 
     public void setActivePlayer(Player activePlayer) {
         this.activePlayer = activePlayer;
     }
 
     public int getBetSize() {
         return betSize;
     }
 
     public Point[] getPoints() {
         return points;
     }
 
     private List<Integer> getDiceMovesLeft(int fromPoint, int toPoint) {
         Integer usedMove;
         if (activePlayer == Player.PLAYER1) {
             usedMove = fromPoint - toPoint;
         } else {
             usedMove = toPoint - fromPoint;
         }
         diceMovesLeft.remove(usedMove);
         return diceMovesLeft;
     }
 
     public boolean isHomeGame(List<Point> populatedPoints) {
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
 
     public boolean isExplodedHomeMoveAllowed(boolean homeGame, int fromPoint, int toPoint, int explodedHomePoint, List<Point> populatedPoints, Point populatedPointToMove) {
         boolean explodedHomeMove = false;
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
         return explodedHomeMove;
     }
 }
