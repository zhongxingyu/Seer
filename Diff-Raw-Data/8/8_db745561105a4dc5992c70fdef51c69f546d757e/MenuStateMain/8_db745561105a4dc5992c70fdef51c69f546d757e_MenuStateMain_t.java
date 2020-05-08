 package se.chalmers.tda367.group15.game.states;
 
import java.util.prefs.Preferences;

 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import se.chalmers.tda367.group15.game.controllers.SoundEffectsController;
 import se.chalmers.tda367.group15.game.controllers.SoundEffectsController.GameMusic;
 import se.chalmers.tda367.group15.game.menu.Button;
 import se.chalmers.tda367.group15.game.settings.Constants;
 
 /**
  * A state representing a Main Menu in a graphical application. Class based on
  * tutorial for writing menus in Slick2D Originally posted on
  * http://slick.javaunlimited.net/ by user shiroto. Remade to suit our purpose.
  * 
  * @author Carl Jansson
  * @version 2.0
  */
 public class MenuStateMain extends AbstractMenuBasedState {
 
 	private SoundEffectsController soundEffectsController;
 	/**
 	 * Resume button must be possible to set visible at will.
 	 */
 	private Button resumeGameButton;
 
 	/**
 	 * the upper left corner of button group
 	 */
 	private final static int MENUX = Constants.MENU_UPPER_X;
 	private final static int MENUY = Constants.MENU_UPPER_Y;
 
 	/**
 	 * True if you have started a game.
 	 */
 	private boolean existsGameCurrently;
 
 	/**
 	 * creates a new MainMenuState.
 	 * 
 	 * @param id
 	 *            The int used to identify the state.
 	 */
 	public MenuStateMain(int id) {
 		super(id);
 		this.existsGameCurrently = false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void init() {
 		this.initMenuItems();
 		soundEffectsController = SoundEffectsController.instance();
 		try {
 			setBackground(new Image("res/menu/background.png"));
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void initMenuItems() {
 		try {
 			this.createButtons();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Creates the buttons. Separate method to avoid big try catch.
 	 * 
 	 * @throws SlickException
 	 *             If a image fail to load.
 	 */
 	private void createButtons() throws SlickException {
 		Image newGameImage = new Image("res/menu/newGame.png");
 		Image newGameImageMO = new Image("res/menu/newGameMO.png");
 		Image quitImage = new Image("res/menu/quit.png");
 		Image quitImageMO = new Image("res/menu/quitMO.png");
 		Image optionsImage = new Image("res/menu/options.png");
 		Image optionsImageMO = new Image("res/menu/optionsMO.png");
 		Image highScoreImage = new Image("res/menu/score.png");
 		Image highScoreImageMO = new Image("res/menu/scoreMO.png");
 		Image resumeImage = new Image("res/menu/resumeGame.png");
 		Image resumeImageMO = new Image("res/menu/resumeGameMO.png");
 
 		// Resume game button. This Button should only be visible when there is
 		// an active game.
 		resumeGameButton = new Button(container, resumeImage, resumeImageMO,
 				MENUX, MENUY) {
 			@Override
 			public void performAction() {
 				// Returns to currently active game.
 				game.enterState(Constants.GAME_STATE_PLAYING);
 			}
 		};
 		// Start a new game.
 		Button newGameButton = new Button(container, newGameImage,
 				newGameImageMO, MENUX, MENUY + 50) {
 			@Override
 			public void performAction() {
 
 				try { // Init PlayState to reset game.
 					game.getState(Constants.GAME_STATE_PLAYING).init(container,
 							game);
 					existsGameCurrently = true;
					
					Preferences prefSettings = Preferences.userNodeForPackage(MenuStateOptions.class);
 					soundEffectsController
 							.playGameMusic(GameMusic.NORMAL_MUSIC);
					container.setMusicOn(prefSettings.getBoolean("Music", true));
					container.setSoundOn(prefSettings.getBoolean("Sound", true));
 				} catch (SlickException e) {
 					e.printStackTrace();
 				}
 
 				game.enterState(Constants.GAME_STATE_PLAYING);
 				resumeGameButton.setButtonActive(true);
 			}
 		};
 		// open options
 		Button optionsButton = new Button(container, optionsImage,
 				optionsImageMO, MENUX, MENUY + 100) {
 			@Override
 			public void performAction() {
 				game.enterState(Constants.GAME_STATE_MENU_OPTIONS);
 			}
 		};
 		// Show high score
 		Button highScoreButton = new Button(container, highScoreImage,
 				highScoreImageMO, MENUX, MENUY + 150) {
 			@Override
 			public void performAction() {
 				game.enterState(Constants.GAME_STATE_MENU_HIGH_SCORE);
 			}
 		};
 		// Quit application
 		Button exitButton = new Button(container, quitImage, quitImageMO,
 				MENUX, MENUY + 200) {
 			@Override
 			public void performAction() {
 				if (game.closeRequested()) {
 					container.exit();
 				}
 			}
 		};
 
 		// Resume game button should not be visible before new game.
 		this.resumeGameButton.setButtonActive(false);
 
 		// add items to state.
 		this.addMenuItem(resumeGameButton);
 		this.addMenuItem(newGameButton);
 		this.addMenuItem(optionsButton);
 		this.addMenuItem(highScoreButton);
 		this.addMenuItem(exitButton);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void escpAction() {
 		// Escape only usable if game started.
 		if (existsGameCurrently) {
 			// Returns you to game.
 			game.enterState(Constants.GAME_STATE_PLAYING);
 		}
 	}
 
 	@Override
 	public void gameOver(boolean win) {
 		resumeGameButton.setButtonActive(false);
 		existsGameCurrently = false;
 	}
 }
