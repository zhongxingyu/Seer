 package cha.domain;
 
 import cha.event.Event;
 import cha.event.EventBus;
 
 public class Challenge extends TurnType {
 	private Piece opponent;
 	private int chaScore;
 	private int oppScore;
 	private final int NUMBER_OF_CARDS = 7;
 
 	// I ButtonPanel frgas efter motstndare. Denna klass initieras nr det
 	// valet har gjorts.
 	public Challenge(Piece opponent) {
 		chaScore = -1;
 		oppScore = -1;
 		this.opponent = opponent;
 	}
 
 	/**
 	 * This method starts the Challenge, only by creating the first mission and
 	 * making it start. It is called from the Challenge constructor when a
 	 * Challenge has been initiated.
 	 */
 	@Override
 	public void startMission(Category category) {
 		if (chaScore < 0) {
 			mission = new Mission(category, NUMBER_OF_CARDS);
 			EventBus.getInstance().publish(Event.StartMission, mission, null);
 		} else {
 			if (oppScore < 0) {
 				mission = new Mission(category, NUMBER_OF_CARDS);
 				EventBus.getInstance().publish(Event.StartMission, mission,
 						null);
 			}
 		}
 	}
 
 	/**
 	 * This method sets the opponent and the challengers scores. When creating a
 	 * Challenge, both values are set to -1.
 	 */
 	@Override
 	public void setScore(int i) {
 		if (chaScore < 0) {
 			chaScore = i;
 		} else if (oppScore < 0) {
 			oppScore = i;
 			endChallenge();
 		}
 	}
 
 	/**
 	 * Checks who has won the challenge and calls method to move pieces.
 	 * Opponent wins at draw.
 	 */
 	private void getResult() {
 		if (chaScore > oppScore) {
 			Board.getInstance().missionStatus(true);
 			opponent.movePieceBackward();
 		} else {
 			opponent.movePieceForward(oppScore);
 			Board.getInstance().missionStatus(false);
 		}
 	}
 
 	private void endChallenge() {
 		mission = null;
 		getResult();
 	}
 
 	@Override
 	public void missionDone() {
 		mission.stopMission();
 		if (chaScore == -1 || oppScore == -1) {
 			EventBus.getInstance().publish(Event.GetChallengeScore, null, null);
 		}
 	}
 
 	public int getChaScore() {
 		return chaScore;
 	}
 	public int getOppScore() {
 		return oppScore;
 	}
 }
