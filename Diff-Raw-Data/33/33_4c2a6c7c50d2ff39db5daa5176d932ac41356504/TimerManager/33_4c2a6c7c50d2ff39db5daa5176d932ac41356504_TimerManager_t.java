 package ca.couchware.wezzle2d.manager;
 
 import ca.couchware.wezzle2d.util.CouchLogger;
 import ca.couchware.wezzle2d.Game;
 import ca.couchware.wezzle2d.event.GameEvent;
 import ca.couchware.wezzle2d.event.IGameListener;
 import ca.couchware.wezzle2d.event.ILevelListener;
 import ca.couchware.wezzle2d.event.IMoveListener;
 import ca.couchware.wezzle2d.event.LevelEvent;
 import ca.couchware.wezzle2d.event.MoveEvent;
 import ca.couchware.wezzle2d.event.TimerEvent;
 import ca.couchware.wezzle2d.manager.ListenerManager.GameType;
 
 /**
  * A class for managing a timer.
  * 
  * @author Cameron McKay
  * @author Kevin Grad
  *
  */
 public class TimerManager implements 
        IResettable, IGameListener, IMoveListener, ILevelListener
 {                 
     
     /** The listener manager used to fire timer events. */
     final private ListenerManager listenerMan;
     
     /**
      * The timer upper bound, in ms.
      */
     final private int timeUpper = 10000;
    
     /** 
      * The timer lower bound, im ms.
      */
     final private int timeLower = 1000;    	    
     
 	/** 
      * The start time for this timer. 
      */
 	private int startTime;
 	
 	/** 
      * The current time, in ms.
      */
 	private int currentTime;
     
     /** 
      * Is the timer paused? 
      */
     private boolean paused;
     
     /**
      * Is the timer stopped?
      * It may seem like there's no point in having a pause and a stop, and to
      * an extent, you are right.  However, there is a distinction.  A pause
      * is fleeting, while a stop is more permanent.
      * 
      * For example, the timer is paused between each turn (fleeting).  However,
      * if a tutorial is running, sometime we want the timer off regardless
      * of how many turns have taken place (more permanent).
      */
     private boolean stopped;    
 	
 	/**
 	 * The overloaded constructor created a timer manager with a passed in 
 	 * initial time.
 	 * 
 	 * @param initialTime The initial time on the timer.
 	 */
 	private TimerManager(ListenerManager listenerMan)
 	{			
         this.listenerMan = listenerMan;
 		this.startTime   = timeUpper;        
 		this.currentTime = startTime;
         this.paused      = false;
         this.stopped     = false;
 	}
         
     public static TimerManager newInstance(ListenerManager listenerMan)        
     {
         return new TimerManager(listenerMan);
     }
 
     /**
 	 * Get the timer time.
      *
 	 * @return The time.
 	 */
 	public int getCurrrentTime()
 	{
 		return this.currentTime;
 	}
 
     public int getCurrentTimeInSeconds()
     {
         return this.currentTime / 1000;
     }
 
     /**
 	 * Get the initial time.
 	 * @return The initial time.
 	 */
 	public int getStartTime()
 	{
 		return this.startTime;
 	}
 
     public void updateLogic(Game game)
     {
         // Tick the timer.
         tick();
     }
     
     /**
 	 * A method to increment the internal time. If a second has passed
 	 * the internal time goes to 0 and the current time is decremented.
 	 * 
 	 * @param offset The elapsed time.
 	 */
 	private void tick()
 	{                
         // If the timer is paused, don't do anything.
         if (paused == true || stopped == true)
             return;                        		
 		        
         // Tick.
 		this.currentTime -= Settings.getMillisecondsPerTick();		
         
         // Notify all listeners of the tick.
         this.listenerMan.notifyTickOccurred(new TimerEvent(this, this.startTime, this.currentTime));
 	}   	
 		
 	/**
 	 * Reset the timer to the start time.
 	 */
 	private void resetCurrentTime()
 	{		
         this.currentTime = this.startTime;
         CouchLogger.get().recordMessage(this.getClass(), "Current time reset to " + this.currentTime);
         
         // Notify all listeners of the tick.
         this.listenerMan.notifyCurrentTimeReset(new TimerEvent(this, this.startTime, this.currentTime));
 	}            	
 
 	/**
 	 * Set the initial time.  Everytime the start time is changed, the current time will also reset.
      * 
 	 * @param time The new time.
 	 */
 	private void setStartTime(int time)
 	{
 		this.startTime = time;
         CouchLogger.get().recordMessage(this.getClass(), "Initial time changed to " + time);
         
         // Notify all listeners of the tick.
         this.listenerMan.notifyStartTimeChanged(new TimerEvent(this, this.startTime, this.currentTime));
 
         // Reset the current time.
         this.resetCurrentTime();
 	}
     
     /**
      * Reset the initial time to it's maximum amount.
      */
     private void setStartTimeToUpper()
     {
         setStartTime(timeUpper);
     }
 		    
     /**
      * Pause the timer.
      * 
      * @param paused True to pause the timer, false to unpause it.
      */
     public void setPaused(final boolean paused)
     {
         this.paused = paused;
     }
     
     /**
      * Check if the timer is paused.
      * 
      * @return Whether or not the timer is paused.
      */
     public boolean isPaused()
     {
         return this.paused;
     }
 
     public boolean isStopped()
     {
         return stopped;
     }
 
     public void setStopped(boolean stopped)
     {
         this.stopped = stopped;
     }
 
    /**
     * Determine the time limit for the given level.
     * @param level
     * @return
     */
    public int determineTimeForLevel(int level)
    {
        return Math.max(timeUpper - (level - 1) * 1000, timeLower);
    }


     public void levelChanged(LevelEvent event)
     {
        int time = determineTimeForLevel(event.getNewLevel());
         this.setStartTime(time);        
         
         CouchLogger.get().recordMessage(this.getClass(), "Level changed.");
     }   
     
     public void resetState()
     {
         this.setStartTimeToUpper();
         this.resetCurrentTime();
     }
 
     public void moveCommitted(MoveEvent event, GameType gameType)
     {
         // Nothing right now.
     }
 
     public void moveCompleted(MoveEvent event)
     {
         // Reset the counter.
         this.resetCurrentTime();
     }
 
     public void gameStarted(GameEvent event)
     {
         // Reset the counter.
         this.resetCurrentTime();
     }
 
     public void gameReset(GameEvent event)
     {
        // Set the max time to the value for this level.
        int time = determineTimeForLevel(event.getLevel());
        this.setStartTime(time);

         // Reset the counter.
         this.resetCurrentTime();
     }
 
     public void gameOver(GameEvent event)
     {
         // Nothing right now.
     }
 		
 }
