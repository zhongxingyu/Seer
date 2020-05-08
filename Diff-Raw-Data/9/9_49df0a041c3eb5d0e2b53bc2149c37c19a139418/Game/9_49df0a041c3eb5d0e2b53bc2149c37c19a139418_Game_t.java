 package name.dlazerka.go.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import roboguice.util.Ln;
 
 public class Game {
   final int tableSize;
 
 //  GameState state;
   boolean lastPassed ;
 
   final List<GameListener> listeners = new ArrayList<GameListener>();
 
   /**
    * Komi multiplied by two (to store to int)
    * Komi is score that added to White's to compensate for Black first-move advantage.
    * In handicap, komi is 0.5, otherwise:
    * American rules: 7.5
    * Japanese rules: 6.5 (since 2002).
    * Chineese rules: 5.5.
    */
   int twoKomi = 13;
 
   /**
    * Ordered collection of game states. One state per turn. The very first state
    * should contain one (or more for handicap) black stones, no white.
    */
   List<GameState> history;
 
   /** Table of viewed points for graph search. */
   final boolean[][] seenIndex;
 
   public Game(int tableSize) {
     this.tableSize = tableSize;
 
     seenIndex = new boolean[tableSize][tableSize];
     history = new ArrayList<GameState>(tableSize * tableSize / 2);
    resetGame();
   }
 
   public int getTableSize() {
     return tableSize;
   }
 
   public GameState getLastState() {
     return history.get(history.size() - 1);
   }
 
   public GameState getStateAt(int turnNo) {
     return history.get(turnNo);
   }
 
   /**
    * Chinese rules score: number of stones + number of captured dead stones.
    * Moonshine-life is not counted.
    * @return positive value: black wins, negative: white wins, zero: black wins (+1/2).
    */
   public int getCurrentScore() {
     return 0;
   }
 
   public void passTurn() {
     GameState newState = getLastState().nextTurnBuilder().build();
     history.add(newState);
     if (lastPassed) {
       Ln.i("Two consecutive passes: game ended");
       resetGame();
     }
     lastPassed = true;
     notifyListener();
   }
 
   void resetGame() {
     history.clear();
    history.add(new GameState());
     lastPassed = false;
   }
 
   public void makeTurnAt(int row, int col) throws SpaceTakenException, NoLibertiesException,
       KoRuleException {
     GameState.Builder stateBuilder = getLastState().nextTurnBuilder();
     if (stateBuilder.getStoneAt(row, col) != null)
       throw new SpaceTakenException();
     Stone stone = new Stone(row, col, getLastState().getWhoseTurn());
 
     // Adding, but may remove later, in case move is invalid (no liberty).
     stateBuilder.add(stone);
 
     boolean libertyFound = false;
     // We need to check for liberties (except New Zealand rules).
     // First, we check if we're capturing enemy stone so that liberty appears.
     Stone[] stonesAround = getStonesAround(stateBuilder, row, col);
     for (int i = 0; i < 4; i++) {
       Stone stoneAround = stonesAround[i];
       if (stoneAround != null && stoneAround.color != stone.color) {
         if (!hasLiberty(stateBuilder, stoneAround)) {
           capture(stateBuilder, stoneAround);
           libertyFound = true;
         }
       }
     }
     // Second, we check liberties.
     if (!libertyFound && !hasLiberty(stateBuilder, stone)) {
       stateBuilder.remove(stone);
       throw new NoLibertiesException();
     }
 
     // Check positional super ko (PSK) rule.
     GameState newState = stateBuilder.build();
     int turnNo = history.lastIndexOf(newState);
     if (turnNo > -1) {
       throw new KoRuleException(history.size() - turnNo);
     }
 
     history.add(newState);
 
     lastPassed = false;
     notifyListener();
   }
 
   Stone[] getStonesAround(GameState.Builder stateBuilder, int row, int col) {
     Stone[] stonesAround = new Stone[] {
         stateBuilder.getStoneAt(row - 1, col),
         stateBuilder.getStoneAt(row + 1, col),
         stateBuilder.getStoneAt(row, col - 1),
         stateBuilder.getStoneAt(row, col + 1)
     };
     return stonesAround;
   }
 
   /**
    * Given stone or its group has at least one liberty. Depth-first search.
    */
   boolean hasLiberty(GameState.Builder stateBuilder, Stone stone) {
     int row = stone.row;
     int col = stone.col;
 
     if (stateBuilder.getStoneAt(row - 1, col) == null ||
         stateBuilder.getStoneAt(row, col - 1) == null ||
         stateBuilder.getStoneAt(row + 1, col) == null ||
         stateBuilder.getStoneAt(row, col + 1) == null) {
       // Reset seenIndex.
       for (int i = 0; i < tableSize; i++) {
         for (int j = 0; j < tableSize; j++) {
           seenIndex[i][j] = false;
         }
       }
       return true;
     }
 
     seenIndex[row][col] = true;
     Stone[] stonesAround = getStonesAround(stateBuilder, row, col);
     for (int i = 0; i < 4; i++) {
       Stone s = stonesAround[i];
       if (s != null && !seenIndex[s.row][s.col] && s.color == stone.color) {
         if (hasLiberty(stateBuilder, s))
           return true;
       }
     }
     return false;
   }
 
   /**
    * Depth-first search.
    * Seen index is not needed, because we remove seen vertices.
    */
   void capture(GameState.Builder stateBuilder, Stone stone) {
     int row = stone.row;
     int col = stone.col;
     stateBuilder.remove(stone);
     Stone[] stonesAround = getStonesAround(stateBuilder, row, col);
     for (int i = 0; i < 4; i++) {
       Stone s = stonesAround[i];
       if (s == null || stateBuilder.getStoneAt(s.row, s.col) == null) {
         // Stone may be already captured from another path.
         continue;
       }
       if (s.color == stone.color) {
         capture(stateBuilder, s);
       }
     }
   }
 
   public void addListener(GameListener listener) {
     listeners.add(listener);
   }
 
   /** Notifies listener for differences that occured between two last game states. */
   void notifyListener() {
     for (GameListener listener : listeners)
       listener.onStateAdvanced(getLastState());
   }
 
   public class SpaceTakenException extends Exception {
   }
 
   public class NoLibertiesException extends Exception {
   }
 
   public class KoRuleException extends Exception {
     final int turnsAgo;
 
     public KoRuleException(int turnsAgo) {
       this.turnsAgo = turnsAgo;
     }
 
     public int getTurnsAgo() {
       return turnsAgo;
     }
   }
 }
