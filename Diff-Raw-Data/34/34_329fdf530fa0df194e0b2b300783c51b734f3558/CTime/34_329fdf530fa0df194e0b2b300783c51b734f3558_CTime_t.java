 package com.Capslock.Engine.Utility;
 
 /*
  * This class acts as a framerate timer.
  * It is packaged along with the screen and input classes in the engine instance object at runtime.
  * 
  *  This class also provides some functionality for keeping track of the time spent in game, such as keeping track of the time spent during pausing, so as not to falsely alter the framerate.
  *  
  *  There are two main framerates kept track of in the CTime class.
  *  
  *  The first, which is not exactly "tracked" is the desired framerate. The desired framerate is the framerate the engine should aim to achieve.
  *  This framerate will never exceed 1000fps, due to limitations within java whereby we track the time taken per frame in milliseconds.
  *  This framerate will also never equal 0. if it does, it will be set to the default framerate as determined by DEFAULT_FRAMERATE.
  *  
  *  The second framerate which is actually tracked is the "actual framerate". This framerate is the framerate actually achieved by the engine. it is calculated based on how long the last frame took to execute.
  *  
  *  The CTime class provides the functionality to pause and resume the framerate timer so as not to influence the percieved framerate when we are not in the game loop.
  */
 
 public class CTime {
 	
 	public static final int DEFAULT_FRAMERATE = 100;
 	
 	/*
 	 * The desired framerate is a variable set by the user of the framerate that describes the target framerate for the engine.
 	 * it is limited to between 1000 and 1.
 	 */
 	protected int DesiredFramerate;
 	
 	/*
 	 * The actual framerate is the one passed to physical entities which gives them an indication of how far to move, and how fast to accelerate.
 	 * The actual framerate is calculated from the time taken to perform the last frame.
 	 * 
 	 * the real framerate reported is 1 frame more than the true framerate. this is so that the instance in which the framerate is 0 can never occur.
 	 */
	protected float RealFramerate = DEFAULT_FRAMERATE;
 	
 	/*
 	 * The desired frame interval is the ideal number of milliseconds we wish a frame to execute for. This is calculated when the desired framerate is changed purely so as not to have to calculate it
 	 * every time we want to check if it's a good time to start the next frame.
 	 */
 	protected int DesiredFrameInterval;
 	
 	/*
 	 * LastFrameInterval is the number of milliseconds that the last frame took to execute. this value is used to calculate the actual framerate.
 	 */
 	protected long LastFrameInterval = (int)((double)1000/(double)RealFramerate);
 	
 	/*
 	 * LastFrameTime is the variable that keeps track of when the last frame ENDED. If the "desiredFrameInterval" has passed since the "lastFrameTime" then we should be allowed to start another frame.
 	 */
 	protected long LastFrameTime = System.currentTimeMillis();
 	
 	/*
 	 * The paused boolean determines whether the frame timer is "paused". Pausing allows the user to do an action that could take a long time, 
 	 */
 	protected boolean Paused = false;
 	
 	/*
 	 * The Pause time difference is the timer difference between the current time and the last frame time. this is recorded when the user pauses. then, when the user unpauses, this is subtracted from the current time
 	 * so as to create the illusion that the LastFrameTime kept up with the system time.
 	 */
 	protected long PauseTimeDifference = 0;
 	
 	/*
 	 * The constructor requires a desired framerate, mostly because the user should never really instantiate a CTime object, and as a result we can make the desired framerate a requirement, even though it really is not.
 	 */
 	public CTime(int DesiredFramerate){
 		setDesiredFramerate(DesiredFramerate);
 		
 		/*
 		 * Here we are simply setting up some variablkes to be as the user would expect them if the frametimer were already running. this is done so as not to cause any glitchy behavior of entities on the first frame.
 		 */
 		RealFramerate = this.DesiredFramerate;
 		LastFrameInterval = DesiredFrameInterval;
 	}
 	
 	
 	/*
 	 * The checkframe method is the method which is called by the game class when it wants to check when to start executing the next frame.
 	 * This method will return true if a sufficient amount of time has passed and we should start calculating the next frame.
 	 * this method will return false if an insufficient amount of time has passed and we should wait longer.
 	 * this method will return false if the frame timer is paused.
 	 */
 	
 	public boolean checkFrame(){
 		if(Paused)return false;
 		
 		if(System.currentTimeMillis()-LastFrameTime >= DesiredFrameInterval){
 			
 			/*
 			 * If a sufficient amount of time has passed for us to start calculating the next frame we do the following:
 			 * 
 			 * Calculate how long the last frame took to execute.
 			 * Calculate the actual framerate based on the execution time of the last frame.
 			 * Set the lastframetime to the current system time. this will be used to determine whether when we should start drawing the next frame.
 			 * 
 			 * finally, we return true to indicate to the game class that it should draw the next frame.
 			 */
 			
 			LastFrameInterval = System.currentTimeMillis() - LastFrameTime;
			RealFramerate = (1000f/(float)LastFrameInterval)+1f;
 			
 			LastFrameTime = System.currentTimeMillis();
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/*
 	 * The Pause method allows the user to pause the frametimer while they do a task in which frames will not tick by at a normal rate.
 	 * 
 	 * To pause the frametimer the CTime class records the length of time that has passed since the end of the last frame.
 	 * Then, when the frametimer is unpaused, the LastFrameTime is set to the current system time minus the recorded time.
 	 * This gives the frametimer the illusion that only a short amount of timehas passed since the last frame.
 	 */
 	
 	public void pause(){
 		if(Paused)return;
 		
 		PauseTimeDifference = System.currentTimeMillis() - LastFrameTime;
 		
 		Paused = true;
 	}
 	
 	public void unpause(){
 		if(!Paused)return;
 		
 		LastFrameTime = System.currentTimeMillis() - PauseTimeDifference;
 		
 		Paused = false;
 	}
 	
 	
 	/*
 	 * All get and set methods.
 	 */
 	public boolean getPaused(){
 		return Paused;
 	}
 	
	public float getRealFramerate(){
 		return RealFramerate;
 	}
 	
 	public long getLastFrameInterval(){
 		return LastFrameInterval;
 	}
 	
 	public int getDesiredFramerate(){
 		return DesiredFramerate;
 	}
 	
 	/*
 	 * Setting the desired framerate requires us to do some bounds checking. we also set the desiredframeinterval in this method, as it is dependent on the framerate.
 	 */
 	public void setDesiredFramerate(int newDesiredFramerate){
 		if(newDesiredFramerate >= 1000){
 			DesiredFramerate = 999;
 		}
 		else if(newDesiredFramerate < 1){
 			DesiredFramerate = DEFAULT_FRAMERATE;
 		}
 		else{
 			DesiredFramerate = newDesiredFramerate;
 		}
 		
 		DesiredFrameInterval = 1000 / DesiredFramerate;
 	}
 	
 	
 	
 }
