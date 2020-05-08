 package org.jnect.emfstore.replay;
 
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
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
		Shell dlgShell = new Shell(Display.getDefault());
		dlgShell.setSize(250, 125);
		new SliderDialog(dlgShell, SWT.NONE, replayProvider).open();
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
