 package org.dazeend.harmonium;
 
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.dazeend.harmonium.screens.NowPlayingScreen;
 import org.dazeend.harmonium.screens.ScreenSaverScreen;
 
 import com.tivo.hme.bananas.BScreen;
 import com.tivo.hme.bananas.IBananas;
 
 public final class InactivityHandler {
 	
 	// If no notable key is pressed for this many milliseconds, we go into the inactive state.
 	private static int inactivityMilliseconds;
 	
 	private Harmonium app;
 	private Timer idleTimer = new Timer();
 	private Date lastActivityDate;
 	private boolean inactive = false;
 	
 	public InactivityHandler(Harmonium app) {
 		this.app = app;
 		updateScreenSaverDelay();
 		lastActivityDate = new Date();
 		idleTimer.schedule(new InactiveCheckTimerTask(this), 10000, 10000);
 	}
 	
 	/**
 	 * Call this any time a notable key (e.g. not volume) is pressed to reset the inactivity timer. 
 	 */
 	public void resetInactivityTimer(){
 		synchronized (this) {
 
 //			if (this.app.isInSimulator())
 //				System.out.println("Resetting inactivity timer.");
 			
 			lastActivityDate = new Date();
 			
 			if (inactive) {
 				if (this.app.isInSimulator()) {
 					System.out.println("Setting inactivity state: ACTIVE.");
 					System.out.flush();
 				}
 				inactive = false;
 			}
 		}
 	}
 
 	private void checkIfInactive() {
 		synchronized (this) {
 			
 			if (inactive)
 				return;
 			
 			Date rightNow = new Date();
			if (rightNow.getTime() - lastActivityDate.getTime() >= 20000) {
 				
 				// If we've been inactive, but there's music playing and we're not on the Now Playing screen,
 				// go to the Now Playing screen.  Next time we go inactive we'll enable the screen saver.
 				if (this.app.getDiscJockey().isPlaying() && (this.app.getCurrentScreen().getClass() != NowPlayingScreen.class) 
 						&& this.app.getDiscJockey().getNowPlayingScreen() != null) {
 					
 					resetInactivityTimer();
 					this.app.push(this.app.getDiscJockey().getNowPlayingScreen(), IBananas.TRANSITION_LEFT);
 					return;
 				}
 				
				if (inactivityMilliseconds != 0 && rightNow.getTime() - lastActivityDate.getTime() >= inactivityMilliseconds)
					setInactive();
 			}
 		}
 	}
 
 	/**
 	 * Called when the inactivity timer elapses and via Harmonium class for HME's idle event.
 	 */
 	public void setInactive()	{
 		synchronized (this) {
 			if (inactive)
 				return;
 			inactive = true;
 
 			if (this.app.isInSimulator()){
 				System.out.println("Setting inactivity state: INACTIVE.");
 				System.out.flush();
 			}
 			
 			// Start the screen saver.
 			BScreen currentScreen = app.getCurrentScreen();
 			ScreenSaverScreen sss = ScreenSaverScreen.getInstance(app);
 			if (currentScreen != sss) {
 				app.push(sss, IBananas.TRANSITION_FADE);
 				app.flush();
 			}
 		}
 	}
 	
 	private class InactiveCheckTimerTask extends TimerTask {
 
 		private InactivityHandler handler;
 		
 		public InactiveCheckTimerTask(InactivityHandler handler) {
 			this.handler = handler;
 		}
 		
 		@Override
 		public void run() {
 			handler.checkIfInactive();
 		}
 	}
 
 	public void updateScreenSaverDelay()
 	{
 		inactivityMilliseconds = app.getPreferences().screenSaverDelay();
 	}
 }
