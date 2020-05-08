 package com.github.joakimpersson.tda367.controller;
 
 import java.beans.PropertyChangeSupport;
 import java.util.List;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.github.joakimpersson.tda367.audio.AudioEventListener;
 import com.github.joakimpersson.tda367.controller.input.InputData;
 import com.github.joakimpersson.tda367.controller.input.InputManager;
 import com.github.joakimpersson.tda367.controller.utils.ControllerUtils;
 import com.github.joakimpersson.tda367.gui.HighscoreView;
 import com.github.joakimpersson.tda367.model.PyromaniacModel;
 import com.github.joakimpersson.tda367.model.IPyromaniacModel;
 import com.github.joakimpersson.tda367.model.constants.EventType;
 import com.github.joakimpersson.tda367.model.constants.PlayerAction;
 
 /**
  * A game state for the highscore
  * 
  * @author joakimpersson
  * 
  */
 public class HighscoreState extends BasicGameState {
 
 	/**
 	 * A simple enum containing the different states in the highscorestate
 	 * 
 	 */
 	private enum State {
 		ACTIVE, NOT_ACTIVE, EMPTY_LIST;
 	}
 
 	private int stateID = -1;
 	private HighscoreView view = null;
 	private InputManager inputManager = null;
 	private PropertyChangeSupport pcs = null;
 	private IPyromaniacModel model = null;
 	private State currentState = null;
 	private int currentIndex = 0;
 
 	/**
 	 * Create a new slick BasicGameState controller for the Highscorestate
 	 * 
 	 * @param stateID
 	 *            The states id number
 	 */
 	public HighscoreState(int stateID) {
 		this.stateID = stateID;
 	}
 
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 
 		this.currentState = State.NOT_ACTIVE;
 		this.model = PyromaniacModel.getInstance();
 		this.view = new HighscoreView();
 		this.inputManager = InputManager.getInstance();
 		this.pcs = new PropertyChangeSupport(this);
 		this.pcs.addPropertyChangeListener(AudioEventListener.getInstance());
 	}
 
 	@Override
 	public void enter(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		super.enter(container, game);
 
 		ControllerUtils.clearInputQueue(container.getInput());
 
 		if (model.getHighscoreList().size() > 0) {
 			currentState = State.ACTIVE;
 		} else {
 			currentState = State.EMPTY_LIST;
 		}
 
 		view.enter();
 		pcs.firePropertyChange("play", null, EventType.TITLE_SCREEN);
 
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 
 		if (currentState != State.NOT_ACTIVE) {
 			view.render(container, g, currentIndex);
 		}
 
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 
 		switch (currentState) {
 		case ACTIVE:
 			updateGame(container.getInput());
 			break;
 		case EMPTY_LIST:
 			// we do no navigation update
 		case NOT_ACTIVE:
 			// we do nothing
 			break;
 		default:
 			break;
 		}
 
 	}
 
 	/**
 	 * Manages all the states input by the player and maps it into an certain
 	 * action that the player has requested to perform
 	 * 
 	 * @param input
 	 *            The input method used by the slick framework that contains the
 	 *            latest action
 	 */
 	private void updateGame(Input input) {
 		List<InputData> inputData = inputManager.getMenuInputData(input);
 
 		for (InputData data : inputData) {
 			PlayerAction action = data.getAction();
 			switch (action) {
 			case MOVE_NORTH:
 				moveIndex(-1);
 				break;
 			case MOVE_SOUTH:
 				moveIndex(1);
 				break;
 			default:
 				break;
 			}
 		}
 
 	}
 
 	/**
 	 * Moves the currentIndex for the players navigation
 	 * 
 	 * @param delta
 	 *            The number of steps to be moved
 	 */
 	private void moveIndex(int delta) {
 		int listSize = model.getHighscoreList().size();
 		int newIndex = (currentIndex + delta);
 
 		int r = newIndex % listSize;
 		if (r < 0) {
 			r += listSize;
 
 		}
 		currentIndex = r;
 	}
 
 	@Override
 	public int getID() {
 		return stateID;
 	}
 
 }
