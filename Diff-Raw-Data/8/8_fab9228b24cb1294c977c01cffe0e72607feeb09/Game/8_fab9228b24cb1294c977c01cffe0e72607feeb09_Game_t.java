 /*
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>
  */
 package com.googlecode.prmf.corleone.game;
 
 import com.googlecode.prmf.corleone.connection.IOThread;
 import com.googlecode.prmf.corleone.game.state.MafiaGameState;
 import com.googlecode.prmf.corleone.game.state.Postgame;
 import com.googlecode.prmf.corleone.game.state.Pregame;
 import com.googlecode.prmf.corleone.game.util.TimerThread;
 
 public class Game {
 	private Player[] players;
 	private TimerThread timerThread;
 	private String gameStarter; //TODO: change gameStarter to a Player, so it follows the Player around after he /NICKs
 	private IOThread inputOutputThread;
 	private MafiaGameState state;
 
 	private Pregame pregame;
 	private Postgame postgame;
 	private boolean inProgress;
 
 	public Game(String gameStarter, IOThread inputOutputThread) {
 		this.gameStarter = gameStarter;
 		this.inputOutputThread = inputOutputThread;
 		//TODO: the next two lines should be combined
 		pregame = new Pregame(gameStarter, inputOutputThread);
 		state = getPregame();
 		timerThread = new TimerThread(inputOutputThread);
 		setProgress(false);
 	}
 
 	public void setProgress(boolean set) {
 		this.inProgress = set;
 	}
 
 	public boolean isInProgress() {
 		return inProgress;
 	}
 
 	public void receiveMessage(String line) {
 		//ok, basically a game of mafia is ALWAYS in a state
 		//a state being which phase of the game is currently in progress: day, night, pregame, postgame
 		//so game has State state, which is one of those four phases
 		//and the message that game receives is always passed to the state
 		//the game doesn't actually do anything, since it makes no sense for the game to do something
 		//without consulting the state that is currently in progress
 		//so just pass it to the state, and let it figure out what to do
 		state.receiveMessage(this, line);
 	}
 
 	//TODO: implement dayStart/nightStart
 
 	public boolean isOver() {
 		boolean result = false;
 
 		//if anyone has won, the game is over ldo
 		for (Player player : getPlayerList()) {
 			if (player.getRole().getTeam().hasWon(players)) {
 				result = true;
 				break;
 			}
 		}
 		return result;
 	}
 
 	public IOThread getIOThread() {
 		return inputOutputThread;
 	}
 
 	public String getGameStarter() {
 		return gameStarter;
 	}
 
 	public TimerThread getTimerThread() {
 		return timerThread;
 	}
 
 	public void stopTimer() {
 		getTimerThread().getTimer().interrupt();
 		state.endState(this);
 	}
 
 	public MafiaGameState getState() {
 		return state;
 	}
 
 	public void setState(MafiaGameState state) {
 		if (isOver()) {
 			state = new Postgame(inputOutputThread, getPlayerList());
			inProgress = false;
 			//game is over, moderation is over... this could happen in postgame maybe?
 			for (int i = 0; i < getPlayerList().length; ++i) { // TODO rewrite using sexier foreach syntax
 				inputOutputThread.sendMessage("MODE", inputOutputThread.getChannel(), "+v " + players[i].getName());
 			}
 			inputOutputThread.sendMessage("MODE", inputOutputThread.getChannel(), "-m");
 		}
 		this.state = state;
 		getTimerThread().getTimer().interrupt();
 		this.state.status();
 	}
 
 	public Player[] getPlayerList() {
 		if (isInProgress()) {
 			//if the game is in progress, the player list is finalized, so finalize it!
 			if (players == null) //this should only happen on 1st access after game starts
 				players = getPregame().getPlayerArray();
 			return players;
 		}
 		//if the game isn't in progress yet, the player list hasn't been finalized, so get the current one from pregame
 		return getPregame().getPlayerArray();
 	}
 
 	public void startTimer() {
 		// TODO the fact that you have to do it like this is just weird
 		// wat?
 		timerThread = new TimerThread(inputOutputThread);
 		getTimerThread().getTimer().start();
 	}
 
 	// TODO this is a bad method, it shouldn't exist imo
 	public Pregame getPregame() {
 		return pregame;
 	}
 
 	// TODO this is a bad method, it shouldn't exist imo
 	public Postgame getPostgame() {
 		return postgame;
 	}
 
 	public void invokeEndState() {
 		state.endState(this);
 	}
 }
