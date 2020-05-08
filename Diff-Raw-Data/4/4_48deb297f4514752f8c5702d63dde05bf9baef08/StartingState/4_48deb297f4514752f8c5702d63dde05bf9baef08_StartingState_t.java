 /**
  * 
  */
 package edu.rit.se.sse.rapdevx.clientstate;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import edu.rit.se.sse.rapdevx.api.GameApi;
 import edu.rit.se.sse.rapdevx.api.SessionApi;
 import edu.rit.se.sse.rapdevx.clientmodels.AssetLibrary;
 
 /**
  * @author Cody Krieger
  * 
  */
 public class StartingState extends StateBase {
 	private Timer	timer	= new Timer();
 
 	public StartingState() {
 		this.nextState = UnitPlacementState.class;
 
 		// TODO here we'll need to include some "game picking" logic -- passing
 		// in null will, in effect, request matchmaking
 		GameSession.get()
 				.setSession(SessionApi.createSession("nickname", null));
 
 		// TODO this null will go away once the API changes -- remove it when
 		// necessary
		AssetLibrary.setAssets(GameApi
				.getAssets(GameSession.get().getSession()));
 
 		// TODO set ready here
 
 		timer.scheduleAtFixedRate(new TimerTask() {
 
 			@Override
 			public void run() {
 				// TODO poll for phase # change
 
 				// once phase # has changed, we're ready to change states
 				this.cancel();
 				ready();
 			}
 
 		}, 0, 1);
 	}
 
 	private void ready() {
 		GameSession.get().advanceState();
 	}
 }
