 package world;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Locale;
 import java.util.Observable;
 import java.util.Random;
 
 
 
 public class WorldModel extends Observable {
   public enum WorldEvents {
    CONFIG_CHANGED, STEP_FORWARD;
   }
   public enum GameState {
     STOP, PLAY, PAUSE, GAME_OVER;
   }
   public enum Direction {
 	UP, DOWN, LEFT, RIGHT;
   }
 
   private int  	score=0;
   private int	cntEaten=0;
   private int 	speed=10;
 
   public static final int GRID_WIDTH=50;
   public static final int GRID_HEIGHT=50;
   
   public final int SPEED_MIN=1;
   public final int SPEED_MAX=20;
   public final int STEPDELAY_MIN=100;
   public final int INSECTS = 4;
   public final int [] INSECT_SCORES = {1, 2, 3};
 
   private GameState   state;
   private Locale locale;
   private LinkedList<GridPoint> snake=new LinkedList<GridPoint>();
   private Direction nextDirection = Direction.LEFT;
   private ArrayList<Insect> insects = new ArrayList<Insect>();
   private boolean acceptDirectionChanges=true;
   private Random r = new Random();
   /**
    * constructor method
    */
   public WorldModel(){
     setLocale(Locale.getDefault());
     state = GameState.STOP;
   }
   /**
    * @param speed the speed to set
    */
   public void setSpeed(int speed) {
     this.speed = speed;
     setChanged();
     notifyObservers(WorldEvents.CONFIG_CHANGED);
   }
   /**
    * @return the speed
    */
   public int getSpeed() {
     return speed;
   }
   /**
    * @return the delay between a steps
    */
   public int getStepDelay() {
     return (int)((double)SPEED_MAX/speed*STEPDELAY_MIN);
   }
   /**
    * @param score the score to set
    */
   public void setScore(int score) {
     this.score = score;
     setChanged();
     notifyObservers(WorldEvents.CONFIG_CHANGED);
   }
   /**
    * @return the score
    */
   public int getScore() {
     return score;
   }
   /**
    * sets the number of eaten insects
    * @param cntEaten number of eaten insects
    */
   public void setCntEaten(int cntEaten) {
 	  setChanged();
 	  notifyObservers(WorldEvents.CONFIG_CHANGED);
 	  this.cntEaten = cntEaten;	  
   }
   /**
    * gets the number of eaten insects
    */
   public int getCntEaten() {
 	  return cntEaten;	  
   }
   /**
    * @param locale the locale to set
    */
   public void setLocale(Locale locale) {
     this.locale = locale;
     setChanged();
     notifyObservers(WorldEvents.CONFIG_CHANGED);
   }
   /**
    * @return the locale
    */
   public Locale getLocale() {
     return locale;
   }
   /**
    * @param state the state to set
    */
   public void setState(GameState state) {
 	this.state = state;
     setChanged();
     notifyObservers(WorldEvents.CONFIG_CHANGED);
   }
   /**
    * @return the state
    */
   public GameState getState() {
     return state;
   }
   /**
    * sets the next direction to d (if it is a direction allowed) and ignore next calls
    * to this method until getNextDirectionAndAcceptDirectionChanges has been called
    * @param d next direction
    */
   public void setNextDirection(Direction d) {
 	  if(!acceptDirectionChanges) return;
 	  // check if next direction is opposite to the current one
 	  if ((nextDirection==Direction.DOWN && d==Direction.UP) ||
 			  (nextDirection==Direction.UP && d==Direction.DOWN))
 		  return;
 	  if ((nextDirection==Direction.LEFT && d==Direction.RIGHT) ||
 			  (nextDirection==Direction.RIGHT && d==Direction.LEFT))
 		  return;
 	  // change next direction
 	  acceptDirectionChanges=false;
 	  nextDirection = d;
   }
   /**
    * Returns the next directions and accept new direction changes.
    */
   public Direction getNextDirectionAndAcceptDirectionChanges() {
 	  acceptDirectionChanges=true;
 	  return nextDirection;
   }
   /**
    * @return the insects
    */
   public ArrayList<Insect> getInsects(){
 	  return insects;
   }
   /**
    * @param insects list of insects
    */
   public void setInsects(ArrayList<Insect> insects) {
 	  this.insects = insects;
 	  setChanged();
	  notifyObservers(WorldEvents.STEP_FORWARD);
   }
   /**
    * @param snake list representing the snake's points
    */
   public void setSnake(LinkedList<GridPoint> snake) {
 	  this.snake = snake;
 	  setChanged();
 	  notifyObservers(WorldEvents.STEP_FORWARD);
   }
   /**
    * @return the Queue representing the snake
    */
   public LinkedList<GridPoint> getSnake(){
 	  return snake;
   }
 }
