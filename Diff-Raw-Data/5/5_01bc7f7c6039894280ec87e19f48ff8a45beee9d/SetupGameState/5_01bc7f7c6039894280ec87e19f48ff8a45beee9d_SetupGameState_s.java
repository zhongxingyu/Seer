 package com.github.joakimpersson.tda367.controller;
 
 import java.awt.Dimension;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.github.joakimpersson.tda367.audio.AudioEventListener;
 import com.github.joakimpersson.tda367.controller.input.InputData;
 import com.github.joakimpersson.tda367.controller.input.InputHandler;
 import com.github.joakimpersson.tda367.controller.input.InputManager;
 import com.github.joakimpersson.tda367.controller.input.KeyBoardInputHandler;
 import com.github.joakimpersson.tda367.controller.input.X360InputHandler;
 import com.github.joakimpersson.tda367.controller.utils.ControllerUtils;
 import com.github.joakimpersson.tda367.gui.SetupGameView;
 import com.github.joakimpersson.tda367.model.BombermanModel;
 import com.github.joakimpersson.tda367.model.IBombermanModel;
 import com.github.joakimpersson.tda367.model.constants.EventType;
 import com.github.joakimpersson.tda367.model.constants.Parameters;
 import com.github.joakimpersson.tda367.model.constants.PlayerAction;
 import com.github.joakimpersson.tda367.model.player.Player;
 import com.github.joakimpersson.tda367.model.positions.Position;
 
 /**
  * Sets up the players
  * 
  * @author rekoil
  * @modified joakimpersson
  */
 public class SetupGameState extends BasicGameState {
 
 	private SetupGameView view = null;
 	private IBombermanModel model = null;
 	private int stateID = -1;
 	private int selection = 0;
 	private int stage = 0;
 	private int controllerCount;
 	private int possiblePlayers;
 	private int players;
 	private List<Player> playerList;
 	private List<String> controllersBound;
 	private InputManager inputManager = null;
 	private PropertyChangeSupport pcs;
 
 	public SetupGameState(int stateID) {
 		this.stateID = stateID;
 	}
 
 	@Override
 	public void enter(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		super.enter(container, game);
 
 		pcs.firePropertyChange("play", null, EventType.TITLE_SCREEN);
 		playerList = new ArrayList<Player>();
 		controllersBound = new ArrayList<String>();
 
 		stage = 0;
 		controllerCount = container.getInput().getControllerCount();
 		possiblePlayers = 2 + controllerCount;
 
 		if (possiblePlayers > 4) {
 			possiblePlayers = 4;
 		}
 
 		selection = 2;
 		view.setPossiblePlayers(possiblePlayers);
 		view.enter();
 	}
 
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 
 		ControllerUtils.clearInputQueue(container.getInput());
 
 		this.pcs = new PropertyChangeSupport(this);
 		this.pcs.addPropertyChangeListener(AudioEventListener.getInstance());
 
 		model = BombermanModel.getInstance();
 		inputManager = InputManager.getInstance();
 		view = new SetupGameView(container);
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		view.render(container, g, selection);
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
 
 		if (input.isKeyPressed(Input.KEY_ESCAPE)) {
 			game.enterState(BombermanGame.MAIN_MENU_STATE);
 		}
 
 		for (PlayerAction action : actions) {
 			switch (action) {
 			case MOVE_NORTH:
 				moveIndex(-1);
 				if (stage != 1) {
 					pcs.firePropertyChange("play", null, EventType.MENU_NAVIGATE);
 				}
 				break;
 			case MOVE_SOUTH:
 				moveIndex(1);
 				if (stage != 1) {
 					pcs.firePropertyChange("play", null, EventType.MENU_NAVIGATE);
 				}
 				break;
 			}
 		}
 
 		boolean controllerProceed = validProceed(input);
 		if (stage < 2 && inputManager.pressedProceed(input)) {
 			if (stage == 0) {
 				players = selection;
 				view.startPlayerCreation(players);
 				stage++;
 			} else if (stage == 1) {
 				if (view.verifyNameValidity()) {
 					createPlayer(view.getName(), view.getIndex());
 					view.playerCreated();
 					if (allPlayersCreated()) {
 						view.assignControllers();
 						stage++;
 					}
 					pcs.firePropertyChange("play", null, EventType.MENU_ACTION);
 				} else {
 					pcs.firePropertyChange("play", null, EventType.ERROR);
 				}
 			}
 		} else if (stage == 2 && controllerProceed) {
 			assignPlayer(controllerUsed(input), view.getIndex());
 			if (allPlayersAssigned()) {
 				int newState = BombermanGame.GAMEPLAY_STATE;
 				ControllerUtils.changeState(game, newState);
 			}
 			view.incIndex();
 		}
 	}
 
 	private boolean allPlayersAssigned() {
 		return controllersBound.size() == players;
 	}
 
 	private void assignPlayer(String controllerUsed, int i) {
 		inputManager.addInputObject(controllerFactory(playerList.get(i - 1),
 				controllerUsed));
 	}
 
 	private boolean validProceed(Input input) {
		if ((input.isKeyPressed(Input.KEY_SPACE) && !controllersBound
 				.contains("k0"))
				|| (input.isKeyPressed(Input.KEY_F) && !controllersBound
 						.contains("k1"))) {
 			return true;
 		}
 		for (int i = 0; i < controllerCount; i++) {
 			if (input.isButtonPressed(X360InputHandler.PROCEED_BUTTON, i)
 					&& !controllersBound.contains("x" + i))
 				return true;
 		}
 		return false;
 	}
 
 	private String controllerUsed(Input input) {
 		String controller = null; // this should never stay null
 		for (int i = 0; i < controllerCount; i++) {
 			if (input.isButtonPressed(X360InputHandler.PROCEED_BUTTON, i)) {
 				controller = "x" + i;
 			}
 		}
 		if (input.isKeyDown(Input.KEY_SPACE)) {
 			controller = "k" + 0;
 		} else if (input.isKeyDown(Input.KEY_F))
 			controller = "k" + 1;
 		controllersBound.add(controller);
 		return controller;
 	}
 
 	private void createPlayer(String name, int playerIndex) {
 		Player player = new Player(playerIndex, name,
 				getInitialPosition(playerIndex));
 		playerList.add(player);
 	}
 
 	private Position getInitialPosition(int playerIndex) {
 		Dimension mapD = Parameters.INSTANCE.getMapSize();
 
 		int left = 1;
 		int right = mapD.width - 2;
 		int top = 1;
 		int bottom = mapD.height - 2;
 
 		switch (playerIndex) {
 		default:
 			return new Position(left, top);
 		case 2:
 			if (players < 3) {
 				return new Position(right, bottom);
 			} else {
 				return new Position(right, top);
 			}
 		case 3:
 			return new Position(left, bottom);
 		case 4:
 			return new Position(right, bottom);
 		}
 	}
 
 	private InputHandler controllerFactory(Player player, String controllerUsed) {
 		char controllerType = controllerUsed.charAt(0);
 		int controllerIndex = Integer.decode(Character.toString(controllerUsed
 				.charAt(1)));
 		if (controllerType == 'k') {
 			return new KeyBoardInputHandler(player, controllerIndex);
 		} else {
 			return new X360InputHandler(player, controllerIndex);
 		}
 	}
 
 	private boolean allPlayersCreated() {
 		return playerList.size() == players;
 	}
 
 	private void moveIndex(int delta) {
 		int currentIndex = selection;
 		int newIndex = (currentIndex + delta);
 
 		if (newIndex < 2) {
 			newIndex = 2;
 		} else if (newIndex > possiblePlayers) {
 			newIndex = possiblePlayers;
 		}
 
 		selection = newIndex;
 	}
 
 	@Override
 	public void leave(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		super.leave(container, game);
 		model.startGame(playerList);
 	}
 
 	@Override
 	public int getID() {
 		return stateID;
 	}
 }
