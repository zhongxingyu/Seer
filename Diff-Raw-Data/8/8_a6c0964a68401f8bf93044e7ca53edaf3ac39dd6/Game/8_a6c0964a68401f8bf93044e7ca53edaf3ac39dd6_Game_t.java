 package ee.ut.cs.sysmodel;
 
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
     Point[] points = new Point[26];
     List<Move> availableMoves = new LinkedList<Move>();
     List<Integer> throwResults = new LinkedList<Integer>();
     Dice dice = new Dice();
     private List<Integer> diceMovesLeft;
     GFrame frame;

     Game() {
         initializeGame();
         frame = new GFrame(this);
     }
 
     private void initializeGame() {
 
         for (int i = 0; i <= 25; i++) {
             points[i] = new Point(i);
         }
         initializeCheckers();
        onDiceThrow();
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
 
     public void onBetIncrease() {
         betSize = betSize * 2;
     }
 
     public void sendCheckerToBar() {
         if (activePlayer == Player.PLAYER1) {
             Player.PLAYER2.getBar().addChecker();
         } else {
             Player.PLAYER1.getBar().addChecker();
         }
     }
 
     public void onMove(int fromPoint, int toPoint) {
         Move playerMove = new Move(fromPoint, toPoint);
         for (Move move : availableMoves) {
             if (move.equals(playerMove)) {
                 if (activePlayer.getBar().isEmpty()) {
                     points[playerMove.getFromPoint()].removeChecker();
                 } else {
                     activePlayer.getBar().removeChecker();
                 }
                 boolean checkerToBar = points[playerMove.getToPoint()].addChecker(activePlayer);
                 if (checkerToBar) {
                     sendCheckerToBar();
                 }
                 setAvailableMoves(getDiceMovesLeft(playerMove.getFromPoint(), playerMove.getToPoint()));
                 if (availableMoves.isEmpty()) {
                     changeActivePlayer();
                 }
             }
         }
         frame.refresh();
     }
 
     public List<Integer> onDiceThrow() {
         throwResults = dice.throwDice();
         if (activePlayer == Player.NONE) {
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
         if (availableMoves.isEmpty()) {
             changeActivePlayer();
             return null;
         } else {
             return throwResults;
         }
     }
 
     private void changeActivePlayer() {
         //TODO set text about no moves left, changing player
         if (activePlayer == Player.PLAYER1) {
             setActivePlayer(Player.PLAYER2);
         } else {
             setActivePlayer(Player.PLAYER1);
         }
     }
 
     public void setAvailableMoves(List<Integer> throwResult) {
         availableMoves.clear();
         int toPoint;
         boolean homeGame = true;
        if (throwResult.isEmpty()) {
            return;
        }
         if (!activePlayer.getBar().isEmpty()) {
             setAvailableMovesOutOfBar(throwResult);
         } else {
             List<Point> populatedPoints = getActivePlayerPopulatedPoints();
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
             }
             for (Point populatedPoint : populatedPoints) {
                 for (int i = 0; i <= 1; i++) {
                     int fromPoint = populatedPoint.getPosition();
                     //Detecting if player can put the checker to home
                     if (activePlayer == Player.PLAYER1) {
                         toPoint = fromPoint - throwResult.get(i);
                         if (toPoint < activePlayer.getHomePoint()) {
                             toPoint = activePlayer.getHomePoint();
                         }
                     } else {
                         toPoint = fromPoint + throwResult.get(i);
                         if (toPoint > activePlayer.getHomePoint()) {
                             toPoint = activePlayer.getHomePoint();
                         }
                     }
                     if (points[toPoint].canAdd(activePlayer)) {
                         /*
                         TODO check that player cant put checker from position
                         1 to 0 if throw was 6 and in position 6 checker exists.
                         */
                         if (homeGame && toPoint == activePlayer.getHomePoint()) {
                             availableMoves.add(new Move(fromPoint, toPoint));
                         }
                         if (toPoint != activePlayer.getHomePoint()) {
                             availableMoves.add(new Move(fromPoint, toPoint));
                         }
                     }
                     //No need to populate list with same moves
                     if (throwResult.size() > 2) {
                         break;
                     }
                 }
             }
         }
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
 
     public void setActivePlayer(Player activePlayer) {
         this.activePlayer = activePlayer;
     }
 
     public int getBetSize() {
         return betSize;
     }
 
     public Point[] getPoints() {
         return points;
     }
 
     private void setPoints(Point[] points) {
         this.points = points;
     }
 
     public List<Integer> getDiceMovesLeft(int fromPoint, int toPoint) {
         int usedMove;
         if (activePlayer == Player.PLAYER1) {
             usedMove = fromPoint - toPoint;
         } else {
             usedMove = toPoint - fromPoint;
         }
         diceMovesLeft.remove(usedMove);
         return diceMovesLeft;
     }
 }
