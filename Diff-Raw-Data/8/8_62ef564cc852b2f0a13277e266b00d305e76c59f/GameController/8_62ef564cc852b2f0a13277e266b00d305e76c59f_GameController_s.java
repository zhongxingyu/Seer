 package org.joedog.pinochle.control;
 
 import java.lang.Thread;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.joedog.pinochle.view.*;
 import org.joedog.pinochle.game.*;
 import org.joedog.pinochle.player.*;
 
 public class GameController extends AbstractController {
   private Thread  thread;
   private AtomicBoolean hiatus           = new AtomicBoolean(false);
   //private boolean running                = false;
   private boolean passable               = false;
   private boolean meldable               = false;
   private boolean playable               = false;
   public final static int DEAL           = 0;
   public final static int BID            = 1;
   public final static int PASS           = 2;
   public final static int MELD           = 3;
   public final static int PLAY           = 4;
   public final static int SCORE          = 5;
   public final static int OVER           = 6;
   public final static String RESET       = "NEWHAND";
   public final static String TRUMP       = "TRUMP";
   public final static String HIGH_BID    = "HIBID";
   public final static String OURS        = "OURS";
   public final static String THEIRS      = "THEIRS";
   public final static String MELD_SCORE  = "NSMELD";
   public final static String TAKE_SCORE  = "NSTAKE";
   public final static String HAND_SCORE  = "NSHAND";
   public final static String GAME_SCORE  = "NSGAME";
   public final static String WINNER      = "NONE";
 
   private boolean over = false;
 
   public GameController () {
     setModelProperty("GameStatus", DEAL);
   }
 
   public void resetHand() {
     runModelMethod("resetHand");
   }
  
   public void newHand() {
     this.over     = false;
     this.meldable = false;
     this.passable = false;
     setStatus("New hand!");
     setModelProperty("GameStatus", ""+DEAL);
   }
 
   public void resetGame() {
     this.over     = false;
     this.meldable = false;
     this.passable = false;
     setStatus("New game!");
     runModelMethod("resetGame");
     runViewMethod("resetScore");
     setModelProperty("GameStatus", ""+DEAL);
     this.thread.stop();
   }
 
   public void winner() {
     this.over = true;
     setModelProperty("GameStatus", ""+OVER);
   }
 
   public void setProperty(String key, String value) {
     setModelProperty(key, value);
   }
 
   public String getProperty(String property) {
     String str = (String)getModelProperty(property); 
     if (str == null) {
       return "";
     } else {
       return str;
     }
   }
 
   public int getIntProperty(String property) {
     String tmp = (String)getModelProperty(property); 
     try {
       if (tmp != null && tmp.length() > 0) {
         return Integer.parseInt(tmp);
       }
     } catch (final NumberFormatException nfe) {
       return -1;
     }
     return -1;
   }
 
   public boolean getBooleanProperty(String property) {
     String tmp = (String)getModelProperty(property); 
     if (tmp.equals("true")) {
       return true;
     } 
     return false;
   }
 
   public int getTrump() {
     return getIntProperty("GameTrump");
   }
 
   public void setPassable(boolean passable) {
     this.passable = passable;
   }
 
   public boolean isPassable() {
     return passable;
   }
 
   public void addPassButton() {
     runViewMethod("addPassButton");
   }
 
   public void addPlayButton() {
     runViewMethod("addPlayButton");
   }
   
   public void enablePassButton() {
     runViewMethod("enablePassButton");
   }
   
   public void disablePassButton() {
     runViewMethod("disablePassButton");
   }
 
   public void addMeldButton() {
     runViewMethod("addMeldButton");
   }
   
   public void setMeldable(boolean meldable) {
     this.meldable = meldable;
   }
 
   public boolean isMeldable() {
     return meldable;
   }
 
   public void setPlayable(boolean playable) {
     this.playable = playable;
   }
 
   public boolean isPlayable() {
     return playable;
   }
 
   public int save() {
     runViewMethod("save");
     runModelMethod("save");
     return 1;
   }
 
   // ExitAction
   public void exit() {
     setStatus("Shutting down...");
     this.save();
     try {
       TimeUnit.SECONDS.sleep(1);
     } catch (Exception e) {}
     System.exit(0);
   }
 
   public synchronized int gameStatus () {
     int status  = ((Integer)getModelProperty("GameStatus")).intValue();
     switch (status) {
       case DEAL:
       case BID:
       case PASS:
       case MELD:
       case PLAY:
       case SCORE:
         this.over = false;
         break;
       case OVER:
         /**
          * we should loop in this case  
          * until the user selects exit
          * or a new game....
          */
         this.over = true;
         try {
           TimeUnit.SECONDS.sleep(1);
         } catch (Exception e) {}
         break;
     }
     return status;
   }
 
   public String getName (int player) {
     switch (player) {
       case Pinochle.NORTH:
         return (String)getModelProperty("PlayerNorthName");
       case Pinochle.SOUTH:
         return (String)getModelProperty("PlayerSouthName");
       case Pinochle.EAST:  
         return (String)getModelProperty("PlayerEastName");
       case Pinochle.WEST:  
         return (String)getModelProperty("PlayerWestName");
       default:
         return "";
     }
   }
 
   public void store(String property, Object value) {
     this.setModelProperty(property, value);
   }
 
   public void display(String property, Object value) {
     this.setViewProperty(property, value);
   }
 
   public void addScore() {
    this.runViewMethod("addScore");
   }
 
   public void clear() {
    this.runViewMethod("clear");
   }
 
   public boolean over() {
     return this.over;
   }
 
   public void setStatus (String status) {
     setViewProperty("Status", status);
   }
 
   public String getGameString () {
     return (String)getModelProperty("GameString");
   }
 
   public void addThread(Thread thread) {
     this.thread = thread;
   }
 
   public void pause(boolean b) {
     hiatus.set(b);
   }
 
   public boolean isPaused() {
     return hiatus.get();
   }
 
   public boolean cheatMode() {
     String mode = (String)getModelProperty("CheatMode");
     return Boolean.parseBoolean(mode);
   }
 }
