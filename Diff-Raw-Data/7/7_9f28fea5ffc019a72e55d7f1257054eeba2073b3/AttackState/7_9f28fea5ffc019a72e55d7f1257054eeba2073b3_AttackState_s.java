 /**
  * 
  */
 package edu.rit.se.sse.rapdevx.clientstate;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import edu.rit.se.sse.rapdevx.api.GameApi;
 
 /**
  * @author Cody Krieger
  * 
  */
 public class AttackState extends StateBase {
 	private Timer	timer	= new Timer();
 	private int		phaseNum;
 
 	public AttackState() {
 		this.nextState = MoveState.class;
 
 		// TODO when one team is out of ships, we actually need to set our
 		// nextState to DoneState.class
 	}
 
 	/**
 	 * Make an attack and send it to the server.
 	 */
 	public void makeAttack(/* TODO take in an attack from the GUI */) {
 		// TODO GameApi.submitAbilityUseOrder(userSession, attack)
 	}
 
 	/**
 	 * Call this when the player is done with their attack stack.
 	 */
 	public void ready() {
		phaseNum = GameApi.getStatus(GameSession.get().getSession()).getPhase();
 
 		GameApi.setReady(GameSession.get().getSession());
 
 		timer.scheduleAtFixedRate(new TimerTask() {
 
 			@Override
 			public void run() {
				if (GameApi.getStatus(GameSession.get().getSession())
						.getPhase() != phaseNum) {
 					this.cancel();
 					readyReady();
 				}
 			}
 
 		}, 0, 1000);
 	}
 
 	private void readyReady() {
 		GameSession.get().advanceState();
 	}
 }
