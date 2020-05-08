 package game;
 
 import gui.MenuGUI;
 import org.apache.log4j.Logger;
 
 /**
  * Class responsible for keeping the game going: spawning aliens, moving to the next level, or ending the game.
  * @author Michael
  */
 public class Progression implements Runnable {
 
     private GameState gameState;
     private boolean istwoPlayer = false;
     private boolean playerOneTurn = true;
     private boolean spawnAlien = true;
     private final static Logger log = Logger.getLogger(Progression.class.getName());
 
     /**
      * Creates Progression using given parameter.
      *
      * @param gameState current game state
      * @param twoPlayer true if two player mode, false otherwise
      */
     public Progression(GameState gameState, boolean twoPlayer) {
         this.gameState = gameState;
         istwoPlayer = twoPlayer;
         log.setLevel(Logic.LOG_LEVEL);
     }
 
     /**
      * Checks progress of game and spawns alien.
      */
     @Override
     public void run() {
         checkGameProgress();
         spawnAlien();
     }
 
     //creates an alien if conditions allow
     private void spawnAlien() {
         //check if alien does not already exist and difficulty says to spawn one
         //Note: alien heading != 0 looks really strange, so always set to 0.
         if (isAlienDestroyed() && !isPlayerDead() && spawnAlien && Difficulty.spawnAlien()) {
             gameState.addAlienShip(new AlienShip(new float[]{Difficulty.randomAlienVelocity(), Difficulty.randomAlienVelocity()}, 0,
                     new int[]{Difficulty.randomXPos(), Difficulty.randomYPos()}, gameState));
         }
     }
 
     private void checkGameProgress() {
         //single player
         if (!istwoPlayer) {
             if (isPlayerDead()) {
                 gameState.setPlayer1Score(gameState.getCurrentScore());
                 gameState.setPlayer1Level(gameState.getLevel());
                 Logic.stopTimer();
                 Logic.displayGameOver(true);
             } //if the player is not dead, check for level completion and move to next level
             else if (allAsteroidsDestroyed() && isAlienDestroyed()) {
                 setupLevel(gameState.getLevel() + 1);
             }
         } //two player
         else {
             //if a player dies, need to find out if first or second player.
             if (isPlayerDead()) {
                 if (playerOneTurn) {
                     //restart game for player 2, save player 1 score
                     int player1Score = gameState.getCurrentScore();
                     int player1Level = gameState.getLevel();
                     Logic.displayPlayerTwoTurn();
                     setupInitialLevel();
                     gameState.setPlayer1Score(player1Score);
                     gameState.setPlayer1Level(player1Level);
                     gameState.setPlayerTwoTurn(true);
                     playerOneTurn = false;
                 } else {
                     //game over: save player 2 score, put it in the game state, and stop updating
                     int player2Score = gameState.getCurrentScore();
                    int player2Level = gameState.getPlayer2Level();
                     gameState.setPlayer2Score(player2Score);
                    gameState.setPlayer2Level(player2Level);
                     //note: player 1 info already set beforehand
                     Logic.stopTimer();
                     Logic.displayGameOver(false);
                 }
             } //same as 1 player
             else if (allAsteroidsDestroyed() && isAlienDestroyed()) {
                 setupLevel(gameState.getLevel() + 1);
             }
         }
     }
     
     //various end of game or level conditions are checked here
     private boolean isAlienDestroyed() {
         return gameState.getAlienShip() == null;
     }
 
     private boolean allAsteroidsDestroyed() {
         return gameState.getAsteroids().isEmpty();
     }
 
     private boolean isPlayerDead() {
         return gameState.isPlayerDead();
     }
 
     /**
      * Sets up initial level (level 1).
      */
     public void setupInitialLevel() {
         //start at level 1 (note: player ship needed, as setupLevel has as precondition that player ship != null
         //play warping in sound
         GameAssets.warp.play();
         gameState.addPlayerShip(new PlayerShip(new float[]{0, 0}, 0, new int[]{MenuGUI.WIDTH / 2, MenuGUI.HEIGHT / 2}, gameState, 1, 1, 1));
         setupLevel(1);
         
         //in case of 2 player, setupLevel saves the old score, so erase it
         gameState.resetCurrentScore();
     }
 
     //player ship != null assumed.  if null, won't do anything (will try again next turn)
     private void setupLevel(int levelNumber) {
         log.info("Going to level " + levelNumber);
         
         PlayerShip player = gameState.getPlayerShip();
         if (player != null) {
             log.info("Player != null; actually increasing level");
             //save score, is player two's turn
             int oldScore = gameState.getCurrentScore();
             int oldPlayerLives = player.getLives();
             int oldPlayerBomb = player.getBomb();
             boolean playerTwo = gameState.isPlayerTwoTurn();
 
             //reset to default, and setup for next level
             gameState.resetToDefaults();
             gameState.addPlayerShip(new PlayerShip(new float[]{0, 0}, 0, new int[]{MenuGUI.WIDTH / 2, MenuGUI.HEIGHT / 2}, gameState, oldPlayerLives, oldPlayerBomb, 3));
             addAsteroids(levelNumber);
             gameState.setLevel(levelNumber++);
             gameState.addToCurrentScore(oldScore);
             gameState.setPlayerTwoTurn(playerTwo);
             spawnAlien = true;
         } else {
             //want to disable/enable alien spawning if the player is null
             spawnAlien = false;
         }
     }
     
     //spawn asteroids, with number and parameters (velocity, size, position) dictated by the difficulty level
     private void addAsteroids(int levelNumber)
     {
         int NumberOfAsteroids = Difficulty.spawnAsteroids(levelNumber);
         log.info("Spawining " + NumberOfAsteroids + " asteroids");
         
         for (int i = 0; i < NumberOfAsteroids; i++) {
             float xVel = Difficulty.randomAsteroidVelocity(levelNumber);
             float yVel = Difficulty.randomAsteroidVelocity(levelNumber);
             float heading = Difficulty.randomHeading();
             int xCoord = Difficulty.randomXPos();
             int yCoord = Difficulty.randomYPos();
             int size = Difficulty.randomAsteroidSize();
             gameState.addAsteroid(new Asteroid(new float[]{xVel, yVel}, heading, new int[]{xCoord, yCoord}, gameState, size));
         }
     }
 }
