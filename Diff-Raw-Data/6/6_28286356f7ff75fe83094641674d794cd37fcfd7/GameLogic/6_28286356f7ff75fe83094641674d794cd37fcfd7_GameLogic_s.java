 package edu.mines.csci598.recycler.frontend;
 
 import edu.mines.csci598.recycler.backend.GameManager;
 import edu.mines.csci598.recycler.frontend.Recyclable.CollisionState;
 import edu.mines.csci598.recycler.frontend.graphics.*;
 import edu.mines.csci598.recycler.frontend.utils.GameConstants;
 import org.apache.log4j.Logger;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * The GameLogic is where the game play logic is. The main game update loop will go here.
  * <p/>
  * Created with IntelliJ IDEA.
  * User: jzeimen
  * Date: 10/20/12
  * Time: 9:35 PM
  * To change this template use File | Settings | File Templates.
  */
 public class GameLogic  {
     private static final Logger logger = Logger.getLogger(GameLogic.class);
 
     private List<Hand> hands;
     private ComputerPlayer computerPlayer;
     private GameScreen gameScreen;
     private RecycleBins recycleBins;
     private ConveyorBelt conveyorBelt;
     private List<Recyclable> fallingItems;
     private double currentTimeSec;
     private long startTime;
     private double nextItemTypeGenerationTime;
     private int numItemTypesInUse;
     private int score;
     private int strikes;
     private boolean gameOverNotified = false;
     private boolean playerIsAComputer;
     private boolean debuggingCollision;
     //TODO game manager should be removed from this class when the idea of players having hands is dissolved
     private GameManager gameManager;
 
 
 
     public GameLogic( RecycleBins recycleBins, Path conveyorPath, GameManager gameManager,
                       boolean playerIsAComputer,boolean debuggingCollision) {
         this.gameManager = gameManager;
         gameScreen = GameScreen.getInstance();
         this.recycleBins = recycleBins;
         fallingItems = new ArrayList<Recyclable>();
         numItemTypesInUse = GameConstants.INITIAL_NUMBER_OF_ITEM_TYPES;
         currentTimeSec = 0;
         nextItemTypeGenerationTime = GameConstants.TIME_TO_ADD_NEW_ITEM_TYPE;
 
         conveyorBelt = new ConveyorBelt(this,gameScreen,conveyorPath,debuggingCollision);
         startTime = System.currentTimeMillis();
 
         this.playerIsAComputer = playerIsAComputer;
         this.debuggingCollision = debuggingCollision;
 
         hands = new ArrayList<Hand>();
 
         // sets up the first player and adds its primary hand to the gameScreen
         // so that it can be displayed
         if (!this.playerIsAComputer) {
          //   player1 = new Player(gameManager);
             // creates the max number of hands that can be displayed which is 4
             for (int i = 0; i < 4; i++) {
                 hands.add(new Hand(gameManager, i));
                 gameScreen.addHandSprite(hands.get(hands.size() - 1).getSprite());
             }
         } else {
             computerPlayer = new ComputerPlayer(recycleBins);
             gameScreen.addHandSprite(computerPlayer.primary.getSprite());
         }
         if(this.debuggingCollision){
             Recyclable r = RecyclableFactory.generateRandom(ConveyorBelt.CONVEYOR_BELT_PATH_RIGHT, 1);
             conveyorBelt.addRecyclable(r);
         }
     }
 
     public void addRecyclable(Recyclable r) {
         try {
             gameScreen.addSprite(r.getSprite());
         } catch (ExceptionInInitializerError e) {
             logger.error("ExceptionInInitializerError adding Recyclable with time " + currentTimeSec);
         }
     }
 
     private void potentiallyHandleCollision(Recyclable r) {
         if (!playerIsAComputer) {
             // checks to see if there is a collision with any one of the hands
             for (Hand hand: hands) {
                 //logger.info("rms="+r.getMotionState()+",msc="+MotionState.CONVEYOR);
                 if(r.getMotionState()== MotionState.CONVEYOR) {
                     // Find out what kind of collision happened, if any
                     CollisionState collisionState = r.hasCollisionWithHand(hand, currentTimeSec);
                     if(collisionState == CollisionState.NONE){
                         //logger.info("Item is untouchable");
                         return;
                     }
                     else{
                         Point2D position = r.getPosition();
                         Path path = new Path();
                         Line collideLine;
                         if (collisionState == CollisionState.HIT_RIGHT) {
                             //logger.debug("Pushed Right");
                             collideLine = new Line(position.getX(), position.getY(),
                                     position.getX() + GameConstants.ITEM_PATH_END, position.getY());
                             r.setMotionState(MotionState.FALL_RIGHT);
                         }
                         else if (collisionState == CollisionState.HIT_LEFT) {
                             //logger.debug("Pushed Left");
                             collideLine = new Line(position.getX(), position.getY(),
                                     position.getX() - GameConstants.ITEM_PATH_END, position.getY());
                             r.setMotionState(MotionState.FALL_LEFT);
                         }
                         else{
                             throw new IllegalStateException("Collision handling can't handle collision states other than right and left");
                         }
                         path.addLine(collideLine);
                         r.setPath(path);
 
                         fallingItems.add(r);
 
                         //Retrieves bin
                         RecycleBin bin = recycleBins.findBinForFallingRecyclable(r);
                         handleScore(r, bin);
                     }
                 }
             }
         } else {
             //Computer collision detection
         }
     }
 
     /**
      * Given the recyclable and the bin it went into this function either increments the score or adds a strike
      *
      * @param r
      * @param bin
      */
     public void handleScore(Recyclable r, RecycleBin bin) {
         if (!playerIsAComputer) {
             if (bin.isCorrectRecyclableType(r)) {
                 score++;
             } else {
                 strikes++;
             }
         } else {
             handleAIScore();
         }
 //
 //        if (strikes >= gameOverStrikes) {
 //            gameOver();
 //        }
     }
 
     public void handleAIScore() {
         score = computerPlayer.getAIScore();
         strikes = computerPlayer.getAIStrikes();
     }
 
     public String getScoreString() {
         return Integer.toString(score);
     }
 
     public String getStrikesString() {
         return Integer.toString(strikes);
     }
 
 	public int getNumItemTypesInUse() {
 		return numItemTypesInUse;
 	}
 
     private void gameOver() {
         if (gameOverNotified) return;
         Sprite sprite = new Sprite("src/main/resources/SpriteImages/GameOverText.png", (GraphicsConstants.GAME_SCREEN_WIDTH / 2) - 220, (GraphicsConstants.GAME_SCREEN_HEIGHT / 2) - 200);
         gameScreen.addSprite(sprite);
         gameOverNotified = true;
         //If We want it to exit
         //gameManager.destroy();
     }
 
     private void increaseDifficulty() {
         possiblyIncreaseTypesInUse();
         increaseSpeed();
         increaseItemGenerationProbability();
     }
 
     private void possiblyIncreaseTypesInUse() {
         if (numItemTypesInUse < GameConstants.MAX_ITEM_COUNT && Math.round(currentTimeSec) > nextItemTypeGenerationTime) {
             numItemTypesInUse++;
             logger.info("Increasing item types to " + numItemTypesInUse + "!");
             nextItemTypeGenerationTime += GameConstants.TIME_TO_ADD_NEW_ITEM_TYPE;
         }
     }
 
     private void increaseSpeed() {
         double pctToMaxDifficulty = Math.min(1, currentTimeSec / GameConstants.TIME_TO_MAX_DIFFICULTY);
         conveyorBelt.setSpeed(pctToMaxDifficulty);
     }
 
     private void increaseItemGenerationProbability() {
         double startProbability = GameConstants.START_ITEM_GENERATION_PROB;
         //itemGenerationProb = startProbability + (1 - startProbability) * GameConstants.TIME_TO_MAX_DIFFICULTY;
     }
 
     public GameManager getGameManager() {
         return gameManager;
     }
 
 	public GameScreen getGameScreen() {
 		return gameScreen;
 	}
 
     protected void updateThis(float elapsedTime) {
        if(System.currentTimeMillis() % 10 != 0){
            return;
        }
 
         //in seconds
         currentTimeSec = (System.currentTimeMillis() - startTime) / 1000.0;
 
         if (!playerIsAComputer) {
             // display the hands
             for (Hand hand: hands) {
                 hand.updateLocation();
             }
         } else {
             //call update to computer hand on next recyclable that is touchable
             if (conveyorBelt.getNumRecyclablesOnConveyor() > 0) {
                 computerPlayer.updateAI(conveyorBelt.getNextRecyclableThatIsTouchable(), currentTimeSec);
             }
             handleAIScore();
         }
 
         // Move conveyor and generate items
         conveyorBelt.update(currentTimeSec);
         // Handle existing item collisions
         for (Recyclable r : conveyorBelt.getRecyclables()) {
             potentiallyHandleCollision(r);
         }
 
         
         for(Recyclable r : fallingItems){
 			Point2D newPosition = r.getPath().getLocation(r.getPosition(), GameConstants.HAND_COLLISION_PATH_SPEED_IN_PIXELS_PER_SECOND, elapsedTime); 
 			r.setPosition(newPosition);
         }
         
         increaseDifficulty();
     }
 
 
 }
