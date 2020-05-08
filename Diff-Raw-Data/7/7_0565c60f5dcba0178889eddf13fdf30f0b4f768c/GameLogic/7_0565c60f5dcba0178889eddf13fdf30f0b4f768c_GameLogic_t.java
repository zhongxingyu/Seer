 package edu.mines.csci598.recycler.frontend;
 
 import edu.mines.csci598.recycler.backend.GameManager;
 import edu.mines.csci598.recycler.backend.GameState;
 import edu.mines.csci598.recycler.backend.ModalMouseMotionInputDriver;
 import edu.mines.csci598.recycler.frontend.graphics.*;
 import edu.mines.csci598.recycler.frontend.utils.GameConstants;
 import edu.mines.csci598.recycler.frontend.utils.Log;
 
 import java.awt.*;
 import java.util.ConcurrentModificationException;
 import java.util.LinkedList;
 import java.util.Random;
 
 
 /**
  * The GameLogic is where the game play logic is. The main game update loop will go here.
  * <p/>
  * Created with IntelliJ IDEA.
  * User: jzeimen
  * Date: 10/20/12
  * Time: 9:35 PM
  * To change this template use File | Settings | File Templates.
  */
 public class GameLogic extends GameState {
     //Needs to be updated
     /*private final Logger logger = LogManager.getLogger(GameLogic.class);
     private final boolean INFO = logger.isInfoEnabled();
     private final boolean DEBUG = logger.isDebugEnabled();
     private final boolean TRACE = logger.isTraceEnabled();     */
 
     private static GameLogic INSTANCE;
     private Player player1, player2;
     private GameScreen gameScreen;
     private GameManager gameManager;
     private LinkedList<RecycleBin> recycleBins = new LinkedList<RecycleBin>();
     private ConveyorBelt conveyor;
     private LinkedList<Recyclable> recyclablesToRemove = new LinkedList<Recyclable>();
     private Random random;
     private double lastGenerationTime;
     private double currentTimeSec;
     private long startTime;
     private double minTimeBetweenGenerations;
     private double itemGenerationDelay;
     private boolean debugCollision;
     private double itemGenerationProb;
     private int numItemTypesInUse;
     private int score;
     private int strikes;
     private int itemType2ActivationTime;
     private int itemType3ActivationTime;
     private int itemType4ActivationTime;
     private int timeToMaxDifficulty;
     private int gameOverStrikes;
 
     private GameLogic() {
         gameManager = new GameManager("Recycler", false);
         gameScreen = GameScreen.getInstance();
 
         random = new Random(System.currentTimeMillis());
         numItemTypesInUse = GameConstants.INITIAL_NUMBER_OF_ITEM_TYPES;
         itemType2ActivationTime = GameConstants.ITEM_TYPE_2_ACTIVATION_TIME;
         itemType3ActivationTime = GameConstants.ITEM_TYPE_3_ACTIVATION_TIME;
         itemType4ActivationTime = GameConstants.ITEM_TYPE_4_ACTIVATION_TIME;
         timeToMaxDifficulty = GameConstants.TIME_TO_MAX_DIFFICULTY;
         minTimeBetweenGenerations = GameConstants.INITIAL_ITEM_GENERATION_DELAY_SECONDS;
         lastGenerationTime = 0;
         currentTimeSec = 0;
         itemGenerationDelay = 0;
         debugCollision = false;
         itemGenerationProb = GameConstants.START_ITEM_GENERATION_PROB;
         gameOverStrikes = 3;
 
         conveyor = new ConveyorBelt();
         startTime = System.currentTimeMillis();
         if (debugCollision) {
             addRecyclable(new Recyclable(currentTimeSec, RecyclableType.getRandom(numItemTypesInUse)));
         }
         setUpBins();
 
         // sets up the first player and adds its primary hand to the gameScreen
         // so that it can be displayed
         player1 = new Player(gameManager);
         gameScreen.addHandSprite(player1.primary.getSprite());
     }
 
     // sets up the location of each of the bins with trash last since it is the last accessed bin
     private void setUpBins() {
         RecycleBin bin1 = new RecycleBin(
                 GameConstants.BIN_1_SIDE, GameConstants.BIN_1_MIN_Y,
                 GameConstants.BIN_1_MAX_Y, GameConstants.BIN_1_TYPE);
         RecycleBin bin2 = new RecycleBin(
                 GameConstants.BIN_2_SIDE, GameConstants.BIN_2_MIN_Y,
                 GameConstants.BIN_2_MAX_Y, GameConstants.BIN_2_TYPE);
         RecycleBin bin3 = new RecycleBin(
                 GameConstants.BIN_3_SIDE, GameConstants.BIN_3_MIN_Y,
                 GameConstants.BIN_3_MAX_Y, GameConstants.BIN_3_TYPE);
         RecycleBin bin4 = new RecycleBin(
                 GameConstants.BIN_4_SIDE, GameConstants.BIN_4_MIN_Y,
                 GameConstants.BIN_4_MAX_Y, GameConstants.BIN_4_TYPE);
         RecycleBin bin5 = new RecycleBin(
                 GameConstants.BIN_5_SIDE, GameConstants.BIN_5_MIN_Y,
                 GameConstants.BIN_5_MAX_Y, GameConstants.BIN_5_TYPE);
         RecycleBin trash = new RecycleBin(RecyclableType.TRASH);
 
         recycleBins.add(bin1);
         recycleBins.add(bin2);
         recycleBins.add(bin3);
         recycleBins.add(bin4);
         recycleBins.add(bin5);
         recycleBins.add(trash);
     }
 
     public static final GameLogic getInstance() {
         if (INSTANCE == null) {
             INSTANCE = new GameLogic();
         }
         return INSTANCE;
     }
 
     /**
      * Determines if item should be generated
      *
      * @return true if item should be generated, false otherwise
      */
     private boolean needsItemGeneration() {
         boolean generate = false;
         boolean timeToGenerate = false;
 
         if (Math.random() < itemGenerationProb) {
             generate = true;
         }
         //random.nextGaussian();
        //TODO: itemGenerationDelay
        if ((currentTimeSec - lastGenerationTime) > minTimeBetweenGenerations) {
             timeToGenerate = true;
            Log.logInfo("TimetoGenerate");
         }
         return (generate && timeToGenerate);
     }
 
     /**
     *   Generates new item if necessary
     */
     private void generateItems() {
         try {
             if (needsItemGeneration()) {
                 addRecyclable(new Recyclable(currentTimeSec, RecyclableType.getRandom(numItemTypesInUse)));
                 lastGenerationTime = currentTimeSec;
                 itemGenerationDelay = 0;
             } else {
                 itemGenerationDelay += GameConstants.ITEM_GENERATION_DELAY;
             }
         } catch (ConcurrentModificationException e) {
             Log.logError("ERROR: ConcurrentModificationException generating a new recyclable!");
         }
     }
 
     private synchronized void updateRecyclables() {
         try {
             for (Recyclable recyclable : conveyor.getRecyclables()) {
                 Sprite sprite = recyclable.getSprite();
                 try {
                     sprite.updateLocation(currentTimeSec);
                     if (sprite.getX() >= GameConstants.TOP_PATH_END_X) {
                         removeRecyclable(recyclable);
                         handleScore(recyclable, recycleBins.getLast());
                     }
                     if ((recyclable.getCurrentMotion() == Recyclable.MotionState.FALL_RIGHT &&
                             sprite.getX() >= GameConstants.VERTICAL_PATH_START_X + GameConstants.IN_BIN_OFFSET) ||
                             (recyclable.getCurrentMotion() == Recyclable.MotionState.FALL_LEFT &&
                                     sprite.getX() <= GameConstants.VERTICAL_PATH_START_X - GameConstants.IN_BIN_OFFSET)) {
                         recyclablesToRemove.addLast(recyclable);
                         gameScreen.removeSprite(recyclable.getSprite());
                     }
                     // make sure the item is still on the conveyor before changing it's touch status
                     if (recyclable.getCurrentMotion() != Recyclable.MotionState.FALL_RIGHT &&
                             recyclable.getCurrentMotion() != Recyclable.MotionState.FALL_LEFT) {
                         if (sprite.getY() <= GameConstants.SPRITE_BECOMES_UNTOUCHABLE) {
                             sprite.setState(Sprite.TouchState.UNTOUCHABLE);
                         } else if (sprite.getY() <= GameConstants.SPRITE_BECOMES_TOUCHABLE) {
                             sprite.setState(Sprite.TouchState.TOUCHABLE);
                         }
                     }
                     checkCollision(recyclable);
 
                 } catch (ConcurrentModificationException e) {
                     Log.logError("ERROR: ConcurrentModificationException updating recyclable " + recyclable + " with time " + currentTimeSec);
                 }
             }
         } catch (ExceptionInInitializerError e) {
             Log.logError("ERROR: ExceptionInInitializerError updating sprites with time " + currentTimeSec);
         }
         for (Recyclable recyclable : recyclablesToRemove) {
             conveyor.removeRecyclable(recyclable);
         }
         //drawThis();
     }
 
     public synchronized void addRecyclable(Recyclable r) {
         try {
             conveyor.addRecyclable(r);
             gameScreen.addSprite(r.getSprite());
         } catch (ExceptionInInitializerError e) {
             Log.logError("ERROR: ExceptionInInitializerError adding Recyclable with time " + currentTimeSec);
         }
     }
 
     public synchronized void removeRecyclable(Recyclable r) {
         recyclablesToRemove.addLast(r);
         gameScreen.removeSprite(r.getSprite());
     }
 
     private synchronized void checkCollision(Recyclable r) {
         if (r.hasCollisionWithHand(player1.primary, currentTimeSec)) {
             r.getSprite().setState(Sprite.TouchState.UNTOUCHABLE);
             RecycleBin bin = findBinForFallingRecyclable(r);
             handleScore(r, bin);
         }
     }
 
     /**
      * Returns the bin that the recyclable is currently falling into. If it is not falling into any then it is going
      * into the trash.
      *
      * @param r
      * @return
      */
     private RecycleBin findBinForFallingRecyclable(Recyclable r) {
         int yCord = r.getSprite().getScaledY();
 
         // finds the bin that the trash has gone into using the y coordinates since it can only fall to the right or
         // left of the conveyor we only need to check which way it's going and the y coordinates
         for (RecycleBin bin : recycleBins) {
             if ((r.getCurrentMotion() == Recyclable.MotionState.FALL_LEFT && bin.getSide() == RecycleBin.ConveyorSide.LEFT) ||
                     (r.getCurrentMotion() == Recyclable.MotionState.FALL_RIGHT && bin.getSide() == RecycleBin.ConveyorSide.RIGHT)) {
 
                 if (yCord >= bin.getMinY() && yCord <= bin.getMaxY()) {
                     return bin;
                 }
             }
         }
         return recycleBins.getLast();
     }
 
     /**
      *  Given the recyclable and the bin it went into this function either increments the score or adds a strike
      *
      * @param r
      * @param bin
      */
     private void handleScore(Recyclable r, RecycleBin bin) {
         if (bin.isCorrectRecyclableType(r)) {
             score++;
         } else {
             strikes++;
         }
 
         if (strikes >= gameOverStrikes) {
             gameOver();
         }
     }
 
     public String getScoreString() {
         return ("" + score);
     }
 
     public String getStrikesString() {
         return ("" + strikes);
     }
 
 
     private void gameOver() {
         System.out.println("GAME OVER");
         Sprite sprite = new Sprite("src/main/resources/SpriteImages/GameOverText.png", (GraphicsConstants.GAME_SCREEN_WIDTH / 2) - 220, (GraphicsConstants.GAME_SCREEN_HEIGHT / 2) - 200);
         gameScreen.addSprite(sprite);
         //If We want it to exit
         //gameManager.destroy();
 
     }
 
 
     private void increaseDifficulty() {
         // Possibly add more items
         if (numItemTypesInUse < 2 && Math.round(currentTimeSec) > itemType2ActivationTime) {
             Log.logInfo("INFO: Increasing item types to 2!");
             numItemTypesInUse++;
         }
         if (numItemTypesInUse < 3 && Math.round(currentTimeSec) > itemType3ActivationTime) {
             Log.logInfo("INFO: Increasing item types to 3!");
             numItemTypesInUse++;
         }
         if (numItemTypesInUse < 4 && Math.round(currentTimeSec) > itemType4ActivationTime) {
             Log.logInfo("INFO: Increasing item types to 4!");
             numItemTypesInUse++;
         }
 
         double pctToMaxDifficulty = Math.min(1, currentTimeSec / timeToMaxDifficulty);
         conveyor.setSpeed(pctToMaxDifficulty);
 
         // increase probability of item generation
         double startProbability = GameConstants.START_ITEM_GENERATION_PROB;
         itemGenerationProb = startProbability + (1 - startProbability) * pctToMaxDifficulty;
     }
 
     public GameManager getGameManager() {
         return this.gameManager;
     }
 
     protected GameState updateThis(float elapsedTime) {
 
         //in seconds
         currentTimeSec = (System.currentTimeMillis() - startTime) / 1000.0;
 
         increaseDifficulty();
 
        if (!debugCollision) {
             generateItems();
        }
 
         // display the hand
         player1.primary.updateLocation();
 
         //see if hand is going through item and handle it
         // check coordinates here
         //gameScreen.checkCollisions();
 
         //see if hand hits powerup and handle it
 
         //Recyclable r = new Recyclable(currentTime, RecyclableType.SKULL);
 
         updateRecyclables();
 
         //check for winning condition.
         return this;
     }
 
     protected void drawThis(Graphics2D g2d) {
         gameScreen.paint(g2d, gameManager.getCanvas());
     }
 
     public static void main(String[] args) {
         GameLogic gm = GameLogic.getInstance();
         ModalMouseMotionInputDriver mouse = new ModalMouseMotionInputDriver();
         gm.getGameManager().installInputDriver(mouse);
         gm.getGameManager().setState(gm);
         gm.getGameManager().run();
         gm.getGameManager().destroy();
     }
 
 }
