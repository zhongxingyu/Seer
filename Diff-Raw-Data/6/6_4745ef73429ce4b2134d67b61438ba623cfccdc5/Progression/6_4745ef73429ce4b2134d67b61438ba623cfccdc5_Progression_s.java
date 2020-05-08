 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package game;
 
 import gui.MenuGUI;
 import game.Difficulty.*;
 
 /**
  *
  * @author Michael
  */
 public class Progression implements Runnable{
     
     private GameState gameState;
     private boolean istwoPlayer = false;
     public boolean playerOneTurn = true;
     
     private int player1Score = 0;
     private int player2Score = 0;
     
     public Progression(GameState gs, boolean twoPlayer){
         gameState = gs;
         istwoPlayer = twoPlayer;
     }
 
     @Override
     public void run() {
         checkGameProgress();
         spawnAlien();
     }
 
     private void spawnAlien() {
         //check if alien does not already exist and difficulty says to spawn one
         //Note: asteroid heading != 0 looks really strange, so always set to 0.
         if (isAlienDestroyed() && Difficulty.spawnAlien()) {
             gameState.addAlienShip(new AlienShip(new float[]{Difficulty.randomAlienVelocity(), Difficulty.randomAlienVelocity()}, 0, 
                     new int[]{Difficulty.randomXPos(), Difficulty.randomYPos()}, gameState));
         }
     }
     
     private void checkGameProgress() {
         //single player
         if (!istwoPlayer) {
             //game over
             if (isPlayerDead()) {
                 gameState.setPlayer1Score(gameState.getCurrentScore());
                 // Freezes when not stopped
                 //Logic.stopTimer();
                 Logic.displayGameOver();
             } //if the player is not dead, check for level completion and move to next level
             else if (allAsteroidsDestroyed() && isAlienDestroyed()) {
                 setupLevel(gameState.getLevel() + 1);
             }
         }
         //two player
         else
         {
            //if a player dies, need to find out if first or second player.
             if (isPlayerDead())
             {
                 if (playerOneTurn)
                 {
                     //restart game for player 2, save player 1 score
                     player1Score = gameState.getCurrentScore();
                     Logic.displayPlayerTwoTurn();
                     setupInitialLevel();
                     gameState.setPlayer1Score(player1Score);
                     gameState.setPlayerTwoTurn(true);
                     playerOneTurn = false;
                 }
                 else
                 {
                     //game over: save player 2 score, put it in the game state, and stop updating
                     player2Score = gameState.getCurrentScore();
                     gameState.setPlayer1Score(player1Score);
                     gameState.setPlayer2Score(player2Score);
                     //Logic.stopTimer();
                     Logic.displayWinner();
                 }
             }
             //same as 1 player
             else if (allAsteroidsDestroyed() && isAlienDestroyed()) {
                  setupLevel(gameState.getLevel() + 1);
             }
         }
     }
     
     
     private boolean isAlienDestroyed()
     {
         return gameState.getAlienShip() == null;
     }
     
     private boolean allAsteroidsDestroyed()
     {
         return gameState.getAsteroids().isEmpty();
     }
     
     private boolean isPlayerDead()
     {
         return gameState.isPlayerDead();
     }
     
     public void setupInitialLevel()
     {
         //for when the game begins, clear everything and start off at level 1.
         gameState.resetToDefaults();
         gameState.addPlayerShip(new PlayerShip(new float[]{0, 0}, 0, new int[]{MenuGUI.WIDTH/2, MenuGUI.HEIGHT/2}, gameState, 3, 0, 3));
         //addAsteroids(1);
         setupLevel(0);
         //gameState.addAsteroid(new Asteroid(new float[]{1.2f, 1.5f}, 0, new int[]{800, 10}, gameState, Asteroid.LARGE_ASTEROID_SIZE));
 
     }
     
     private void setupLevel(int levelNumber)
     {
        //save old level & score
         int oldLevel = gameState.getLevel();
         int oldScore = gameState.getCurrentScore();
         //player can't be null here
         int oldPlayerLives = gameState.getPlayerShip().getLives();
         
         
         gameState.resetToDefaults();
         gameState.addPlayerShip(new PlayerShip(new float[]{0, 0}, 0, new int[]{MenuGUI.WIDTH/2, MenuGUI.HEIGHT/2}, gameState, oldPlayerLives, 0, 3));
         addAsteroids(levelNumber);
         gameState.setLevel(oldLevel+1);
         gameState.addToCurrentScore(oldScore);
     }
     
     private void addAsteroids(int levelNumber)
     {
         int NumberOfAsteroids = Difficulty.spawnAsteroids(levelNumber);
 
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
