 package org.jnect.emfstore.replay;
 
 import org.jnect.bodymodel.Body;
 import org.jnect.emfstore.ReplayBodyProvider;
 
 public class Replay {
 
 	private static Replay INSTANCE;
 
 	IReplayBodyProvider replayProvider;
 
 	private Replay() {
 		setupReplayProvider();
 	}
 
 	public static Replay getInstance() {
 		if (INSTANCE == null)
 			INSTANCE = new Replay();
 		return INSTANCE;
 	}
 
 	public void displaySlider() {
		new SliderDialog(replayProvider).open();
 	}
 
 	public Body getReplayBody() {
 		return replayProvider.getReplayBody();
 	}
 
 	/**
 	 * sets the replay body to the first state
 	 */
 	public void setupBody() {
 		replayProvider.setReplayToState(0);
 	}
 
 	private void setupReplayProvider() {
 		replayProvider = new ReplayBodyProvider();
 	}
 
 }
