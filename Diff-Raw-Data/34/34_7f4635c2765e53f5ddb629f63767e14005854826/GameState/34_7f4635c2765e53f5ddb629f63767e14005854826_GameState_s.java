 package lt.ltech.numbers.game;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import lt.ltech.numbers.GameException;
 import lt.ltech.numbers.player.Player;
 
 public class GameState {
     private List<Round> rounds;
     private GameStep gameStep;
     private List<Player> players;
 
     public GameState() {
         this.players = new ArrayList<Player>();
         this.rounds = new ArrayList<Round>();
         this.gameStep = new GameStep(GameStep.Type.INITIAL);
     }
 
     public void addPlayer(Player player) throws GameException {
         if (this.gameStep.type() == GameStep.Type.GUESS
                 || this.gameStep.type() == GameStep.Type.GAME_OVER) {
             throw new GameException("Cannot add a new player "
                     + "to a game that has already started");
         }
         if (this.gameStep.type() == GameStep.Type.INITIAL) {
             this.gameStep = new GameStep(GameStep.Type.SET_NUMBER, player);
         }
         this.players.add(player);
     }
 
     public GameStep setNumber(Player player, Number number)
             throws GameException {
         GameStep gs = new GameStep(GameStep.Type.SET_NUMBER, player);
         this.checkStep(gs, this.gameStep);
 
         int playerIndex = this.players.indexOf(player);
         this.players.get(playerIndex).setNumber(number);
 
         GameStep.Type nextGameStepType = null;
         Player nextPlayer = null;
         if (playerIndex == this.players.size() - 1) {
             nextGameStepType = GameStep.Type.GUESS;
             nextPlayer = this.players.get(0);
         } else {
             nextGameStepType = GameStep.Type.SET_NUMBER;
             nextPlayer = this.players.get(this.players.indexOf(player) + 1);
         }
         this.gameStep = new GameStep(nextGameStepType, nextPlayer);
 
         return this.gameStep;
     }
 
     public GameStep guess(Player player, Number guess) throws GameException {
         GameStep gs = new GameStep(GameStep.Type.GUESS, player);
         this.checkStep(gs, this.gameStep);
 
         Round r = this.getLastRound();
         if (r != null) {
             Map<Player, Number> guesses = r.getGuesses();
             if (r.getGuesses().containsKey(player)) {
                 if (this.players.size() == guesses.size()) {
                     r = new Round();
                     this.rounds.add(r);
                 } else {
                     throw new GameException(String.format(
                             "%s has already made a guess this round", player));
                 }
             }
         } else {
             r = new Round();
             this.rounds.add(r);
         }
 
         int playerIndex = this.players.indexOf(player);
         int opponentIndex = playerIndex + 1;
         if (opponentIndex == this.players.size()) {
             opponentIndex = 0;
         }
         Player opponent = this.players.get(opponentIndex);
 
         r.addGuess(player, opponent, guess);
 
         if (this.isGameOver()) {
             this.gameStep = new GameStep(GameStep.Type.GAME_OVER);
         } else {
             Player nextPlayer = null;
             if (playerIndex == this.players.size() - 1) {
                 nextPlayer = this.players.get(0);
             } else {
                 nextPlayer = this.players.get(playerIndex + 1);
             }
             this.gameStep = new GameStep(GameStep.Type.GUESS, nextPlayer);
         }
 
         return this.gameStep;
     }
 
     public GameStep getGameStep() {
         return this.gameStep;
     }
 
     public Round getLastRound() {
         if (this.rounds.size() == 0) {
             return null;
         }
         return this.rounds.get(this.rounds.size() - 1);
     }
 
     private void checkStep(GameStep expected, GameStep actual) {
         if (!expected.equals(actual)) {
             throw new IllegalStateException(String.format(
                     "Illegal step - %s; expected %s", actual, expected));
         }
     }
 
     public boolean isGameOver() {
         return this.getWinner() != null;
     }
 
     public List<Round> getRounds() {
         return Collections.unmodifiableList(this.rounds);
     }
 
     public Player getWinner() {
         Player winner = null;
         Round lastRound = this.getLastRound();
         if (lastRound == null) {
             return winner;
         }
         byte correct = GameConfiguration.numberLength();
         for (Player player: lastRound.getAnswers().keySet()) {
             Answer answer = lastRound.getAnswers().get(player);
             if (answer.getCorrect() == correct) {
                 winner = player;
                 break;
             }
         }
         return winner;
     }
 }
