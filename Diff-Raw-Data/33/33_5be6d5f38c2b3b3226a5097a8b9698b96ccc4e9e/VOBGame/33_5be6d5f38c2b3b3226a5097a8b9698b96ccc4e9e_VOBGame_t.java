 package com.ahsgaming.valleyofbones;
 
 import com.ahsgaming.valleyofbones.network.*;
 import com.ahsgaming.valleyofbones.screens.*;
 import com.ahsgaming.valleyofbones.screens.GameSetupScreen.GameSetupConfig;
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.FPSLogger;
 import com.badlogic.gdx.utils.Array;
 
 public class VOBGame extends Game {
 	public static final boolean DEBUG = true;
     public static final boolean DEBUG_ATTACK = false;
     public static final boolean DEBUG_LOCK_SCREEN = true;
     public static final boolean DEBUG_GLOBAL_SERVER = false;
     public static final boolean DEBUG_AI = false;
 	public static final String LOG = "VOBGame";
 
     public static final String VERSION = "0.0.14";
 
     FPSLogger fpsLogger = new FPSLogger();
 	
 	protected GameController gController = null;
 	
 	private float keyScrollSpeed = 500;
 	private float mouseScrollSpeed = 500;
 	private float mouseScrollSize = 15;
 
     public static VOBGame instance;
     protected TextureManager textureManager;
 	
 
 	NetController netController;
 	Player player;
 	
 	boolean started = false;
 	
 //	boolean isServer = false;
 
 	boolean loadGame = false;
 	
 	GameResult gameResult = null; // client sets this when a game ends
 
     public String playerName = "Player";
 	
 	/*
 	 * Constructors
 	 */
 	
 	public VOBGame() {
         VOBGame.instance = this;
 	}
 	
 	/*
 	 * Methods
 	 */
 
     public boolean loadProfile() {
         if (Gdx.files.local("profile").exists()) {
             playerName = Gdx.files.local("profile").readString();
             return true;
         }
         return false;
     }
 
     public void saveProfile() {
         Gdx.files.local("profile").writeString(playerName, false);
     }
 	
 	public void createGame(GameSetupConfig cfg) {
         if (cfg.isMulti) {
             netController = new MPGameClient(this, cfg);
         } else {
             // TODO load settings from somewhere?
             netController = new SPGameClient(this, cfg);
         }
 	}
 	
 	public void closeGame() {
         if (netController != null)
             netController.stop();
 	}
 	
 	public void startGame() {
         started = true;
 		if (netController != null) {
             netController.startGame();
             gController = netController.getGameController();
             setScreen(getLevelScreen());
         }
 	}
 	
 	public void sendStartGame() {
 		if (netController != null) netController.sendStartGame();
 	}
 
 	public void quitGame() {
 		Gdx.app.exit();
 	}
 	
 	public void sendCommand(Command cmd) {
 		if (netController != null) netController.sendCommand(cmd);
 	}
 	
 	public void addAIPlayer(int team) {
 		netController.addAIPlayer(team);
 	}
 	
 	public void removePlayer(int playerId) {
 		netController.removePlayer(playerId);
 	}
 	
 	/**
 	 * Implemented methods
 	 */
 	
 	@Override
 	public void create() {
         textureManager = new TextureManager();
 
        Gdx.app.log(LOG, String.format("Valley of Bones Client Version %s", VERSION));
         if (!loadProfile()) {
             Gdx.files.local("profile").writeString(playerName, false);
             setScreen(getOptionsScreen());
         } else {
             if (DEBUG_AI) {
                 GameSetupConfig cfg = new GameSetupConfig();
                 cfg.isHost = true;
                 cfg.isMulti = false;
                 cfg.playerName = playerName;
                 createGame(cfg);
                 addAIPlayer(1);
                 sendStartGame();
 //                setScreen(getGameSetupScreen());
 //                addAIPlayer(1);
                 return;
             }
             setScreen((DEBUG ? getMainMenuScreen() : getSplashScreen()));
         }
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 
         Gdx.app.log(LOG, "dispose");
 //        if (AbstractScreen.skin != null) AbstractScreen.skin.dispose();
         textureManager.dispose();
 
 		closeGame();
 	}
 
 	@Override
 	public void render() {	
 		super.render();
 		if (DEBUG && !DEBUG_AI) fpsLogger.log();
 
 		if (loadGame) {
 			startGame();
 			loadGame = false;
 		}
 		
 		if (netController != null) {
 
             if (netController.isConnected() && loadGame && !started)
                 startGame();
 
 
             if (gameResult != null) {
 
                 this.setScreen(this.getGameOverScreen(gameResult));
                 gameResult = null;
                 netController = null;
                 gController = null;
             }
 
 
             if (netController != null) netController.update(Gdx.graphics.getDeltaTime());
         }
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		super.resize(width, height);
         Gdx.app.log(LOG, "resize");
 	}
 
 	@Override
 	public void pause() {
 		super.pause();
         Gdx.app.log(LOG, "pause");
 	}
 
 	@Override
 	public void resume() {
 		super.resume();
         Gdx.app.log(LOG, "resume");
 
 //        AbstractScreen.skin = null;
 //        TextureManager.clear();
 	}
 	
 	/**
 	 * getters and setters
 	 */
 	
 	public SplashScreen getSplashScreen() {
 		return new SplashScreen(this);
 	}
 	
 	public MainMenuScreen getMainMenuScreen() {
 		return new MainMenuScreen(this);
 	}
 	
 	public OptionsScreen getOptionsScreen() {
 		return new OptionsScreen(this);
 	}
 	
 	public GameSetupScreen getGameSetupScreen() {
 		
 		GameSetupConfig cfg = new GameSetupConfig();
 		cfg.isHost = true;
 		cfg.isMulti = false;
 		cfg.playerName = playerName;
 		return new GameSetupScreen(this, cfg);
 	}
 	
 	public GameSetupScreen getGameSetupScreenMP(boolean isHost) {
 		GameSetupConfig cfg = new GameSetupConfig();
 		cfg.isMulti = true;
 		cfg.isHost = isHost;
 		return new GameSetupScreen(this, cfg);
 	}
 	
 	public GameSetupScreen getGameSetupScreenMP(GameSetupConfig cfg) {
 		return new GameSetupScreen(this, cfg);
 	}
 	
 	public GameJoinScreen getGameJoinScreen() {
 		return new GameJoinScreen(this);
 	}
 	
 	public GameLoadingScreen getGameLoadingScreen() {
 		return new GameLoadingScreen(this);
 	}
 	
 	public LevelScreen getLevelScreen() {
 		return new LevelScreen(this, netController.getGameController());
 	}
 	
 	public GameOverScreen getGameOverScreen(GameResult result) {
 		return new GameOverScreen(this, result, getPlayers());
 	}
 	
 	public Player getPlayer() {
 		if (netController != null) {
 			player = netController.getPlayer();
 		}
 		return player;
 	}
 	
 	public Array<Player> getPlayers() {
 		if (netController != null) {
 			Array<Player> ret = new Array<Player>();
 			ret.addAll(netController.getPlayers());
 			return ret;
 		}
 		return new Array<Player>();
 	}
 	
 	public void setLoadGame() {
 		// TODO Auto-generated method stub
 		loadGame = true;
 	}
 	
 	public boolean isConnected() {
 		if (netController == null) return false;
 		
 		return netController.isConnected();
 	}
 	
 	public boolean isConnecting() {
 		if (netController == null) return false;
 		
 		return netController.isConnecting();
 	}
 	
 	public NetController getNetController() {
 		return netController;
 	}
 
 	/**
 	 * @return the keyScrollSpeed
 	 */
 	public float getKeyScrollSpeed() {
 		return keyScrollSpeed;
 	}
 
 	/**
 	 * @param keyScrollSpeed the keyScrollSpeed to set
 	 */
 	public void setKeyScrollSpeed(float keyScrollSpeed) {
 		this.keyScrollSpeed = keyScrollSpeed;
 	}
 
 	/**
 	 * @return the mouseScrollSpeed
 	 */
 	public float getMouseScrollSpeed() {
 		return mouseScrollSpeed;
 	}
 
 	/**
 	 * @param mouseScrollSpeed the mouseScrollSpeed to set
 	 */
 	public void setMouseScrollSpeed(float mouseScrollSpeed) {
 		this.mouseScrollSpeed = mouseScrollSpeed;
 	}
 
 	/**
 	 * @return the mouseScrollSize
 	 */
 	public float getMouseScrollSize() {
 		return mouseScrollSize;
 	}
 
 	/**
 	 * @param mouseScrollSize the mouseScrollSize to set
 	 */
 	public void setMouseScrollSize(float mouseScrollSize) {
 		this.mouseScrollSize = mouseScrollSize;
 	}
 
 	public void setGameResult(GameResult gameResult) {
 		this.gameResult = gameResult;
 	}
 
     public TextureManager getTextureManager() {
         return textureManager;
     }
 
 }
