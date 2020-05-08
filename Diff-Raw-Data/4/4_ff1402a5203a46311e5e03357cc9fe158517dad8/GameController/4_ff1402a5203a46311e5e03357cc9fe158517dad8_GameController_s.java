 package controller;
 
 
 import model.Game;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import utils.SaveUtils;
 import view.GameView;
 
 
 public class GameController extends StateBasedGame {
 	private Game game;
 	private GameView gameView;
 	private StartMenuController startMenuController;
 	private InGameController inGameController;
 	private PauseMenuController pauseMenuController;
 	private EndOfLevelController endOfLevelController;
 	private HighScoreStateController highScoreStateController;
 	private NewHighscoreController newHighscoreController;
 	private ControlsController controlsController;
 	private Music inGameMusic;
 	
 	public GameController(String name) throws SlickException {
 		super(name);
 		this.inGameMusic = new Music("music/game_music_regular.wav");
 		this.inGameController = new InGameController(this);
 		this.game = new Game(inGameController.getInGame());
 		SaveUtils.init();
 		this.startMenuController = new StartMenuController(this);
 		this.gameView = new GameView(this.game);
 		this.highScoreStateController = new HighScoreStateController(this);
 		this.newHighscoreController = new NewHighscoreController(this);
 		this.gameView = new GameView(this.game);
 		this.pauseMenuController = new PauseMenuController(this);
 		this.endOfLevelController = new EndOfLevelController(this);
 		this.controlsController = new ControlsController(this);
 		this.addState(inGameController);
 		this.addState(highScoreStateController);
 		this.addState(pauseMenuController);
 		this.addState(newHighscoreController);
 		this.addState(pauseMenuController);
 		this.addState(endOfLevelController);
		this.addState(startMenuController);
		this.addState(controlsController);
 	}
 	
 	@Override
 	public void initStatesList(GameContainer container) throws SlickException {
 		this.enterState(startMenuController.getID());
 	}
 	
 	public StartMenuController getStartMenuController(){
 		return startMenuController;
 	}
 	
 	public InGameController getInGameController(){
 		return inGameController;
 	}
 	
 	public PauseMenuController getPauseController(){
 		return pauseMenuController;
 	}
 
 	public HighScoreStateController getHighScoreStateController() {
 		return highScoreStateController;
 	}
 
 	public Game getGame() {
 		return this.game;
 	}
 	
 	public GameView getGameView() {
 		return this.gameView;
 	}
 
 	public Music getInGameMusic() {
 		return inGameMusic;
 	}
 	
 	public void changeFullscreen (GameContainer gc) {
 		AppGameContainer agc = (AppGameContainer) gc;
 		try {
         	if (!gc.isFullscreen()) {
         		agc.setDisplayMode(Game.WINDOW_WIDTH, Game.WINDOW_HEIGHT, true);
     			gc.setMouseGrabbed(true);
 			} else 	{
 				agc.setDisplayMode(Game.WINDOW_WIDTH, Game.WINDOW_HEIGHT, false);
 				gc.setMouseGrabbed(false);
 			}
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 
 
 
 }
