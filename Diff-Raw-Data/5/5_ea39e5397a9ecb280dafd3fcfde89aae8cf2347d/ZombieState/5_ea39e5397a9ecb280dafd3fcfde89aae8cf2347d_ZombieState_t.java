 package jzi.controller.state;
 
 import jzi.model.entities.IPlayer;
 import jzi.model.entities.IZombie;
 import jzi.model.game.Game;
 import jzi.model.game.IGame;
 import jzi.model.map.ICoordinates;
 import jzi.model.map.IField;
 import jzi.view.GameMenu;
 import jzi.view.IWindow;
 
 /**
  * State for zombie movement.
  * 
  * @author Buddy
  */
 public class ZombieState implements IState {
 	/**
 	 * UID for serialization.
 	 */
 	private static final long serialVersionUID = -6374869293857060070L;
 	/**
 	 * State's game handle.
 	 */
 	private transient IGame game;
 	/**
 	 * State's window handle.
 	 */
 	private transient IWindow window;
 	/**
 	 * State's menu handle.
 	 */
 	private transient GameMenu menu;
 	/**
 	 * Defines whether zombies should be moved or placed.
 	 */
 	private ZombieMode mode;
 
 	/**
 	 * Constructor with window.
 	 * 
 	 * @param window
 	 *            window handle
 	 */
 	public ZombieState(final IWindow window) {
 		this.window = window;
 		menu = (GameMenu) window.getMenu();
 	}
 
 	/**
 	 * Sets the state's game handle.
 	 * 
 	 * @param game
 	 *            new game handle
 	 */
 	@Override
 	public final void setGame(final IGame game) {
 		this.game = game;
 	}
 
 	/**
 	 * Sets the state's window handle.
 	 * 
 	 * @param window
 	 *            new window handle
 	 */
 	@Override
 	public final void setWindow(final IWindow window) {
 		this.window = window;
 		menu = (GameMenu) window.getMenu();
 	}
 
 	/**
 	 * Sets the zombie mode. The zombie mode defines whether zombies are moved
 	 * or placed.
 	 * 
 	 * @param mode
 	 *            new zombie mode
 	 */
 	public final void setZombieMode(final ZombieMode mode) {
 		this.mode = mode;
 	}
 
 	/**
 	 * Gets the zombie mode.
 	 * 
 	 * @return current zombie mode
 	 */
 	public ZombieMode getZombieMode() {
 		return mode;
 	}
 
 	/**
 	 * Performs state set up; updates the GUI accordingly.
 	 */
 	@Override
 	public final void enter() {
 		menu.getContinueButton().setEnabled(true);
 		menu.getRollDieButton().setEnabled(true);
 
 		if (game.getZombies().isEmpty()) {
 			continueAction();
 		}
 
 		mode = ZombieMode.Move;
 	}
 
 	/**
 	 * Performs state tear down; updates the GUI accordingly.
 	 */
 	@Override
 	public final void exit() {
 		menu.getMoveZombieButton().setEnabled(false);
 		menu.getPlaceZombieButton().setEnabled(false);
 		menu.getContinueButton().setEnabled(false);
 		menu.getRollDieButton().setEnabled(false);
 
 		game.setCurrentZombie(null);
 		game.setDie(0);
 	}
 
 	/**
 	 * Occurs when the player rolls the die; sets the player's zombie movement
 	 * points to the die value.
 	 */
 	@Override
 	public final void rollAction() {
 		game.getCurrentPlayer().setZombies(game.getDie());
 		game.getCurrentPlayer().setRolledZombie(true);
 
 		updateMode();
 	}
 
 	/**
 	 * Occurs when the player presses the continue button, advances to the next
 	 * player and goes to the tile placement state.
 	 */
 	@Override
 	public final void continueAction() {
 		game.nextPlayer();
 		game.setState(new TileState(window));
 	}
 
 	/**
 	 * Occurs when the player clicks the map; tries to place or move a zombie
 	 * there.
 	 * 
 	 * @param coords
 	 *            clicked coordinates
 	 */
 	@Override
 	public final void mapAction(final ICoordinates coords) {
 		IField field = game.getMap().getField(coords);
 		IPlayer player = game.getCurrentPlayer();
 
 		// no field there or player hasn't rolled
 		if (field == null || !player.hasRolledZombie()) {
 			return;
 		}
 
 		if (mode.equals(ZombieMode.Move)) {
 			moveZombie(field);
 		} else if (mode.equals(ZombieMode.Place)) {
 			game.placeZombie(field);
 		}
 
 		game.setDie(player.getZombies());
 
		updateMode();

 		// disable radio button if player can't place zombies any more
 		if (player.getZombies() < Game.ZOMBIE_PLACE_COST) {
 			menu.getPlaceZombieButton().setEnabled(false);
 			menu.getMoveZombieButton().setSelected(true);
 			mode = ZombieMode.Move;
 		}
 
 		// If last zombie: continue
 		if (player.getZombies() == 0) {
 			continueAction();
 		}
 	}
 
 	/**
 	 * Tries to move a zombie to the given field.
 	 * 
 	 * @param field
 	 *            field to move zombie to
 	 */
 	private void moveZombie(final IField field) {
 		IZombie zombie = field.getZombie();
 
 		// If zombie is clicked again, deselect him
 		if (field.getZombie() != null
 				&& field.getZombie().equals(game.getCurrentZombie())) {
 			game.setCurrentZombie(null);
 			return;
 		}
 
 		// If field has a zombie and zombie has steps left, set current zombie
 		if (zombie != null && game.canZombieMove(zombie)) {
 			game.setCurrentZombie(zombie);
 			return;
 		}
 
 		// If there is no current zombie, return because there's nothing to do
 		if (game.getCurrentZombie() == null) {
 			return;
 		}
 
 		// otherwise move zombie and reset current zombie
 		if (game.moveZombie(field)) {
 			game.setCurrentZombie(null);
 		}
 	}
 
 	/**
 	 * Doesn't do anything as tiles can't be drawn in this state.
 	 */
 	@Override
 	public void drawAction() {
 		// no tile drawing in this state
 	}
 
 	private void updateMode() {
 		boolean canMove = false;
 
 		menu.getMoveZombieButton().setEnabled(true);
 		menu.getPlaceZombieButton().setEnabled(true);
 
 		if (game.getCurrentPlayer().getZombies() >= Game.ZOMBIE_PLACE_COST) {
 			menu.getPlaceZombieButton().setEnabled(true);
 		} else {
 			mode = ZombieMode.Move;
 			menu.getMoveZombieButton().setSelected(true);
 			menu.getPlaceZombieButton().setEnabled(false);
 		}
 
 		if (game.getMap().getEmptyBuildings().isEmpty()) {
 			mode = ZombieMode.Move;
 			menu.getMoveZombieButton().setSelected(true);
 			menu.getPlaceZombieButton().setEnabled(false);
 		}
 
 		for (IZombie zombie : game.getZombies()) {
 			if (game.canZombieMove(zombie)) {
 				canMove = true;
 				break;
 			}
 		}
 
 		if (!canMove) {
 			continueAction();
 		}
 	}
 }
