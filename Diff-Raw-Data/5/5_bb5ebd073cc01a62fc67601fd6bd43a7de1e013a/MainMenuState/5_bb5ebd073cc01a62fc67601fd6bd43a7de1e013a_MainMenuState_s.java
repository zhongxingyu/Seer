 package com.github.joakimpersson.tda367.controller;
 
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.FadeInTransition;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 import org.newdawn.slick.state.transition.Transition;
 
 import com.github.joakimpersson.tda367.audio.AudioEventListener;
 import com.github.joakimpersson.tda367.controller.input.InputData;
 import com.github.joakimpersson.tda367.controller.input.InputManager;
 import com.github.joakimpersson.tda367.gui.IView;
 import com.github.joakimpersson.tda367.gui.MainMenuView;
 import com.github.joakimpersson.tda367.model.constants.EventType;
 import com.github.joakimpersson.tda367.model.constants.PlayerAction;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public class MainMenuState extends BasicGameState {
 
 	private MainMenuView view = null;
 	private int stateID = -1;
 	private PropertyChangeSupport pcs;
 	private InputManager inputManager = null;
 	private int currentIndex;
 
 	public MainMenuState(int stateID) {
 		this.stateID = stateID;
 	}
 
 	@Override
 	public void enter(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		super.enter(container, game);
 		clearInputQueue(container.getInput());
 		pcs.firePropertyChange("play", null, EventType.TITLE_SCREEN);
 
 	}
 
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		this.pcs = new PropertyChangeSupport(this);
 		this.pcs.addPropertyChangeListener(AudioEventListener.getInstance());
 		view = new MainMenuView();
 		inputManager = InputManager.getInstance();
 		this.currentIndex = 1;
 
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		view.render(container, g, currentIndex);
 	}
 
 	private List<PlayerAction> defaultInput(Input input) {
 		List<InputData> dataList = inputManager.getData(input);
 		List<PlayerAction> actions = new ArrayList<PlayerAction>();
 		for (InputData data : dataList) {
 			actions.add(data.getAction());
 		}
 		return actions;
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		Input input = container.getInput();
 		List<PlayerAction> actions = defaultInput(input);
 
 		int newState = -1;
 		// TODO jocke only used during development
 		if (input.isKeyPressed(Input.KEY_ESCAPE)
 				|| input.isKeyPressed(Input.KEY_Q)) {
 			container.exit();
 		}
 
		if (actions.contains(PlayerAction.MoveUp)) {
 			moveIndex(-1);
 			pcs.firePropertyChange("play", null, EventType.MENU_NAVIGATE);
 		}
 
		if (actions.contains(PlayerAction.MoveDown)) {
 			moveIndex(1);
 			pcs.firePropertyChange("play", null, EventType.MENU_NAVIGATE);
 		}
 
 		if (inputManager.pressedProceed(input)) {
 			if (currentIndex == 1) {
 				newState = BombermanGame.SETUP_GAME_STATE;
 			} else if (currentIndex == 2) {
 				newState = BombermanGame.HIGHSCORE_STATE;
 			} else if (currentIndex == 3) {
 				container.exit();
 			}
 			pcs.firePropertyChange("play", null, EventType.MENU_ACTION);
 		}
 
 		if (newState != -1) {
 			changeState(game, newState);
 		}
 	}
 
 	private void moveIndex(int delta) {
 		int newIndex = (currentIndex + delta);
 
 		if (newIndex < 1) {
 			newIndex = 1;
 		} else if (newIndex > 3) {
 			newIndex = 3;
 		}
 
 		currentIndex = newIndex;
 
 	}
 
 	/**
 	 * Responsible for changing into an new state
 	 * 
 	 * @param game
 	 *            The Bomberman game container
 	 * @param newState
 	 */
 	private void changeState(StateBasedGame game, int newState) {
 		Transition fadeIn = new FadeInTransition(Color.cyan, 500);
 		Transition fadeOut = new FadeOutTransition(Color.cyan, 500);
 		game.enterState(newState, fadeOut, fadeIn);
 	}
 
 	/**
 	 * Clear everything in the input queue from previous states
 	 * 
 	 * @param input
 	 *            The input method used by the slick framework that contains the
 	 *            latest action
 	 */
 	private void clearInputQueue(Input input) {
 		input.clearControlPressedRecord();
 		input.clearKeyPressedRecord();
 		input.clearMousePressedRecord();
 	}
 
 	@Override
 	public int getID() {
 		return stateID;
 	}
 
 }
