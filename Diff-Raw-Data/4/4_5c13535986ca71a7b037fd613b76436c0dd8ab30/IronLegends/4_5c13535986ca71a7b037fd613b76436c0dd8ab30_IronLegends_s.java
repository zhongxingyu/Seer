 package jig.ironLegends;
 
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.util.Iterator;
 
 import javax.imageio.spi.ServiceRegistry;
 
 import jig.engine.ImageResource;
 import jig.engine.Mouse;
 import jig.engine.RenderingContext;
 import jig.engine.ResourceFactory;
 import jig.engine.ViewableLayer;
 import jig.engine.hli.ImageBackgroundLayer;
 import jig.engine.hli.ScrollingScreenGame;
 import jig.engine.physics.AbstractBodyLayer;
 import jig.engine.physics.Body;
 import jig.engine.physics.BodyLayer;
 import jig.engine.physics.vpe.VanillaPhysicsEngine;
 import jig.engine.util.Vector2D;
 import jig.ironLegends.core.Fonts;
 import jig.ironLegends.core.GameScreen;
 import jig.ironLegends.core.GameScreens;
 import jig.ironLegends.core.HighScore;
 import jig.ironLegends.core.HighScorePersistance;
 import jig.ironLegends.core.InstallInfo;
 import jig.ironLegends.core.KeyCommands;
 import jig.ironLegends.core.ResourceIO;
 import jig.ironLegends.core.SoundFx;
 import jig.ironLegends.core.GameScreens.ScreenTransition;
 import jig.ironLegends.mapEditor.MapCalc;
 import jig.ironLegends.screens.GameInfoTextLayer;
 import jig.ironLegends.screens.GameOverTextLayer;
 import jig.ironLegends.screens.GamePlayTextLayer;
 import jig.ironLegends.screens.GamePlay_GS;
 import jig.ironLegends.screens.HelpScreen;
 import jig.ironLegends.screens.ServerSelectScreen;
 import jig.ironLegends.screens.SplashScreen;
 import jig.misc.sat.PolygonFactory;
 
 public class IronLegends extends ScrollingScreenGame {
 	public static final int TILE_WIDTH = 32;
 	public static final int TILE_HEIGHT = 32;
 	public static final int WORLD_WIDTH = 2000;
 	public static final int WORLD_HEIGHT = 2000;
 	public static final int SCREEN_WIDTH = 800;
 	public static final int SCREEN_HEIGHT = 600;
 	public static final Rectangle WORLD_BOUNDS = new Rectangle(0, 0,
 			WORLD_WIDTH, WORLD_HEIGHT);
 	public static final Rectangle VISIBLE_BOUNDS = new Rectangle(
 			SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, WORLD_WIDTH - 2
 					* (SCREEN_WIDTH / 2), WORLD_HEIGHT - 2
 					* (SCREEN_HEIGHT / 2));
 
 	public static final String GAME_ROOT = "jig/ironLegends/";
 	public static final String RESOURCE_ROOT = "jig/ironLegends/resources/";
 	public static final String RESOURCE_SCREEN = "jig/ironLegends/resources/screens/";
 
 	public static final String HR_SPRITE_SHEET = RESOURCE_ROOT
 			+ "hr-spritesheet.png";
 	public static final String SPRITE_SHEET = RESOURCE_ROOT
 			+ "ironLegends-spritesheet.png";
 	public static final String SCREEN_SPRITE_SHEET = RESOURCE_ROOT
 			+ "screens/menus-spritesheet.png";
 
 	public static final String HR_RESOURCES = "hr-resources.xml";
 	public static final String MY_RESOURCES = "ironLegends-resources.xml";
 	public static final String SCREEN_RESOURCES = "menus-resources.xml";
 
 	public static final int SPLASH_SCREEN = 0;
 	public static final int HELP_SCREEN = 1;
 	public static final int SERVER_SCREEN = 2;
 	public static final int LOBBY_SCREEN = 3;
 	public static final int GAMEOVER_SCREEN = 4;
 	public static final int GAMEPLAY_SCREEN = 5;
 	public static final int GAMEWON_SCREEN = 6;
 
 	public static final int START_LIVES = 2;
 	public VanillaPhysicsEngine m_physicsEngine;
 	public PolygonFactory m_polygonFactory;
 	public ResourceIO m_rr;
 	public GameScreens m_screens = new GameScreens();
 	public KeyCommands m_keyCmds = new KeyCommands();
 	public Fonts m_fonts = new Fonts();
 	public SoundFx m_sfx;
 	public LevelProgress m_levelProgress;
 	public GameProgress m_gameProgress;
 	public HighScore m_highScore = new HighScore();
 	public HighScorePersistance m_highScorePersist;
 	public MapCalc m_mapCalc;
 
 	public String m_mapName;
 	public PlayerInfo m_playerInfo;
 	public Tank m_tank;
 	public ViewableLayer m_bgLayer;
 	public BodyLayer<Body> m_tankLayer;
 	public BodyLayer<Body> m_bulletLayer;
 	public BodyLayer<Body> m_tankObstacleLayer; // trees
 	public BodyLayer<Body> m_tankBulletObstacleLayer; // walls, buildings, rocks
 	public BodyLayer<Body> m_powerUpLayer;
 
 	public boolean m_bGameOver = false;
 	public boolean m_bFirstLevelUpdate = false;
 	public boolean m_paused = false;
 	public String m_sError;
 	public String m_sInstallDir;
 
 	public IronLegends() {
 		super(SCREEN_WIDTH, SCREEN_HEIGHT, false);
 		gameframe.setTitle("Iron Legends");
 
		m_mapCalc = new MapCalc(WORLD_WIDTH, WORLD_HEIGHT);
 		setWorldDim(WORLD_WIDTH, WORLD_HEIGHT);
 
 		// Load resources
 		loadResources();
 
 		m_sInstallDir = InstallInfo.getInstallDir("/" + GAME_ROOT
 				+ "IronLegends.class", "IronLegends.jar");
 		m_levelProgress = new LevelProgress();
 		m_gameProgress = new GameProgress(m_levelProgress);
 		m_rr = new ResourceIO(m_sInstallDir);
 		m_sfx = new SoundFx();
 		m_playerInfo = new PlayerInfo("ace");
 		m_highScorePersist = new HighScorePersistance(m_sInstallDir);
 		m_highScorePersist.load(m_highScore);
 
 		// create persons polygon factory
 		m_polygonFactory = null;
 
 		// Load polygon factory
 		for (Iterator<PolygonFactory> f = ServiceRegistry
 				.lookupProviders(PolygonFactory.class); f.hasNext();) {
 			m_polygonFactory = f.next();
 			if (m_polygonFactory.getClass().getName().equals("PersonsFactory")) {
 				break;
 			}
 		}
 		if (m_polygonFactory == null) {
 			return;
 		}
 
 		// Physics Engine
 		m_physicsEngine = new VanillaPhysicsEngine();
 
 		// Commands
 		configureCommands();
 
 		// Screens
 		setupScreens();
 
 		// Load Layers
 		populateGameLayers();
 	}
 
 	public void loadResources() {
 		ResourceFactory resourceFactory = ResourceFactory.getFactory();
 		
 		resourceFactory.loadResources(RESOURCE_ROOT, MY_RESOURCES);
 		resourceFactory.loadResources(RESOURCE_SCREEN, SCREEN_RESOURCES);
 		m_fonts.create(resourceFactory);
 	}
 
 	public void loadMap(String sMapFile) {
 		m_tankObstacleLayer.clear();
 		m_tankBulletObstacleLayer.clear();
 		IronLegendsMapLoadSink sink = new IronLegendsMapLoadSink(this);
 		MapLoader.loadLayer(sink, sMapFile, m_rr);
 	}
 
 	public void loadLevel() {
 		m_powerUpLayer.clear();
 		m_levelProgress.reset();
 
 		loadMap("maps/mapitems.txt");
 
 		populateGameLayers();
 		m_levelProgress.setIntro(2999);
 		m_bFirstLevelUpdate = false;
 	}
 
 	/**
 	 * Configure Commands
 	 */
 	public void configureCommands() {
 		m_keyCmds.addCommand("left", KeyEvent.VK_LEFT);
 		m_keyCmds.addCommand("right", KeyEvent.VK_RIGHT);
 		m_keyCmds.addCommand("up", KeyEvent.VK_UP);
 		m_keyCmds.addCommand("down", KeyEvent.VK_DOWN);
 
 		m_keyCmds.addCommand("fixturret", KeyEvent.VK_T);
 		m_keyCmds.addCommand("fire", KeyEvent.VK_CONTROL);
 		m_keyCmds.addCommand("space", KeyEvent.VK_SPACE);
 		m_keyCmds.addCommand("pause", KeyEvent.VK_P);
 		m_keyCmds.addCommand("enter", KeyEvent.VK_ENTER);
 		m_keyCmds.addCommand("esc", KeyEvent.VK_ESCAPE);
 		m_keyCmds.addCommand("backspace", KeyEvent.VK_BACK_SPACE);
 
 		// Cheat codes
 		m_keyCmds.addCommand("splat", KeyEvent.VK_8);
 		m_keyCmds.addCommand("die", KeyEvent.VK_F);
 
 		m_keyCmds.addAlphabet();
 	}
 
 	public void setupScreens() {
 		// background layer
 		ImageResource bkg = ResourceFactory.getFactory().getFrames(
 				SPRITE_SHEET + "#background").get(0);
 		m_bgLayer = new ImageBackgroundLayer(bkg, WORLD_WIDTH, WORLD_HEIGHT,
 				ImageBackgroundLayer.TILE_IMAGE);
 
 		// GamePlay Layers
 		m_tankLayer = new AbstractBodyLayer.NoUpdate<Body>();
 		// Main player
 		m_tank = new Tank(this, Tank.Team.WHITE, new Vector2D(100, 100));
 		m_tankLayer.add(m_tank);
 
 		// Temporary: add random 10 AI tanks
 		while (m_tankLayer.size() < 3) {
 			addAITank();
 		}
 
 		m_bulletLayer = new AbstractBodyLayer.IterativeUpdate<Body>();
 		m_tankObstacleLayer = new AbstractBodyLayer.NoUpdate<Body>();
 		m_tankBulletObstacleLayer = new AbstractBodyLayer.IterativeUpdate<Body>();
 		m_powerUpLayer = new AbstractBodyLayer.NoUpdate<Body>();
 
 		m_screens.addScreen(new SplashScreen(SPLASH_SCREEN, m_fonts,
 				m_playerInfo));
 		
 		m_screens.addScreen(new HelpScreen(HELP_SCREEN, m_fonts));
 		m_screens.addScreen(new ServerSelectScreen(SERVER_SCREEN, m_fonts));
 		
 		m_screens.addScreen(new GamePlay_GS(GAMEPLAY_SCREEN, this));
 		m_screens.addScreen(new GameScreen(GAMEWON_SCREEN));
 
 		GameScreen gameplayScreen = m_screens.getScreen(GAMEPLAY_SCREEN);
 		gameplayScreen.addViewableLayer(new GameInfoTextLayer(m_fonts,
 				m_gameProgress, m_highScore));
 		gameplayScreen.addViewableLayer(new GamePlayTextLayer(m_fonts,
 				m_gameProgress, m_playerInfo));
 
 		// gameover screen has all the layers of gameplay except the text layer
 		// is different
 		Iterator<ViewableLayer> iter = gameplayScreen.getViewableLayers();
 		GameScreen gameOverScreen = new GameScreen(GAMEOVER_SCREEN);
 		while (iter.hasNext()) {
 			gameOverScreen.addViewableLayer(iter.next());
 		}
 		gameOverScreen.addViewableLayer(new GameOverTextLayer(m_fonts,
 				m_gameProgress));
 		m_screens.addScreen(gameOverScreen);
 		
 		// Start with Splash screen
 		m_screens.setActiveScreen(SPLASH_SCREEN);
 	}
 
 	public void populateGameLayers() {
 		gameObjectLayers.clear();
 		m_physicsEngine.clear();
 
 		GameScreen gs = m_screens.getActiveScreen();
 		gs.populateLayers(gameObjectLayers);
 	}
 
 	public void processCommands(long deltaMs) {
 		m_keyCmds.update(keyboard);
 		int activeScreen = m_screens.activeScreen();
 		GameScreen curScreen = m_screens.getScreen(activeScreen);
 
 		if (curScreen != null) {
 			int iNewScreen = curScreen.processCommands(m_keyCmds, mouse,
 					deltaMs);
 			int iCurScreen = curScreen.name();
 			if (iCurScreen != iNewScreen) {
 				// NOTE if had a SplashScreen GameScreen, the activate could
 				// populateGameLayers?
 				m_screens.setActiveScreen(iNewScreen);
 				GameScreen newScreen = m_screens.getActiveScreen();
 				curScreen.deactivate();
 				populateGameLayers();
 				newScreen.activate(iCurScreen);
 			}
 		}
 
 		if (m_levelProgress.isExitActivated()) {
 			if (m_keyCmds.wasPressed("enter")) {
 				m_levelProgress.setExitComplete(true);
 			}
 		}
 
 		// Screen Transitions
 		ScreenTransition t = m_screens.transition(m_keyCmds);
 		if (t != null) {
 			GameScreen newScreen = m_screens.getActiveScreen();
 			curScreen.deactivate();
 			newScreen.activate(t.m_from);
 			populateGameLayers();
 		}
 
 		if (m_keyCmds.wasPressed("pause")) {
 			if (m_paused) {
 			} else {
 			}
 			m_paused = !m_paused;
 		}
 
 		if (activeScreen == GAMEPLAY_SCREEN) {
 			if (m_keyCmds.wasPressed("die")) {
 				m_gameProgress.playerDied();
 				if (m_gameProgress.getLivesRemaining() < 0) {
 					m_screens.setActiveScreen(GAMEOVER_SCREEN);
 					// !m_levelProgress.isExitActivated()
 					m_gameProgress.getLevelProgress().setExit(true);
 				}
 			}
 		}
 	}
 
 	public void newGame() {
 		m_bGameOver = false;
 		m_gameProgress.reset();
 		loadLevel();
 	}
 
 	public Bullet getBullet() {
 		// search for inactive bullet
 		for (Body b : m_bulletLayer) {
 			if (!b.isActive()) {
 				return (Bullet) b;
 			}
 		}
 
 		Bullet bullet = new Bullet();
 		m_bulletLayer.add(bullet);
 		return bullet;
 	}
 
 	public void updateMapCenter(Vector2D centerPosition) {
 		centerOnPoint(centerPosition);
 		m_mapCalc.centerOnPoint(centerPosition);
 	}
 
 	@Override
 	public void update(long deltaMs) {
 		processCommands(deltaMs);
 
 		if (m_levelProgress.isExitActivated()) {
 			super.update(deltaMs);
 			processCommands(deltaMs);
 			if (m_levelProgress.isExitComplete()) {
 				// advanceLevel();
 			}
 			// m_levelProgress.update(deltaMs);
 			return;
 		}
 
 		if (!m_levelProgress.isIntro()) {
 			super.update(deltaMs);
 		} else if (!m_bFirstLevelUpdate) {
 			super.update(deltaMs);
 			m_bFirstLevelUpdate = true;
 		}
 
 		int activeScreen = m_screens.activeScreen();
 		switch (activeScreen) {
 		case SPLASH_SCREEN:
 			break;
 
 		case GAMEOVER_SCREEN:
 			// allow things to keep moving
 			m_physicsEngine.applyLawsOfPhysics(deltaMs);
 			break;
 
 		case GAMEPLAY_SCREEN:
 			if (m_levelProgress.isIntro()) {
 				m_levelProgress.update(deltaMs);
 			} else if (m_levelProgress.isExitActivated()) {
 				m_levelProgress.update(deltaMs);
 			} else {
 				m_physicsEngine.applyLawsOfPhysics(deltaMs);
 			}
 
 			// TODO: Temporary hack to show score
 			m_levelProgress.setScore(m_tank.getScore());
 			
 			if (m_gameProgress.getLivesRemaining() == 0) {
 				m_screens.setActiveScreen(GAMEOVER_SCREEN);
 
 				int totalScore = m_gameProgress.gameOver();
 				if (totalScore > m_highScore.getHighScore()) {
 					m_highScore.setHighScore(totalScore);
 					m_highScore.setPlayer(m_playerInfo.getName());
 					m_highScorePersist.save(m_highScore);
 				}
 			}
 		}
 
 		// center screen on tank
 		Vector2D center = m_tank.getShapeCenter();
 		updateMapCenter(center.clamp(VISIBLE_BOUNDS));
 	}
 
 	@Override
 	public void render(RenderingContext rc) {
 		super.render(rc);
 		GameScreen curScreen = m_screens.getActiveScreen();
 		if (curScreen != null) {
 			curScreen.render(rc);
 		}
 	}
 
 	public static void main(String[] args) {
 		IronLegends game = new IronLegends();
 		game.run();
 	}
 
 	public static Vector2D bodyPosToPolyPos(int w, int h, Vector2D pos) {
 		double r = Math.sqrt(w * w + h * h) / 2;
 		double deltaX = r - w / 2;
 		double deltaY = r - h / 2;
 
 		return new Vector2D(pos.getX() - deltaX, pos.getY() - deltaY);
 	}
 
 	public void setWorldDim(int width, int height) {
 		setWorldBounds(0, 0, width, height);
 		m_mapCalc.setWorldBounds(0, 0, width, height);
 	}
 
 	public void setMapName(String m_mapName) {
 		this.m_mapName = m_mapName;
 	}
 
 	public String getMapName() {
 		return m_mapName;
 	}
 	
 	public Mouse getMouse() {
 		return mouse;
 	}
 	
 	public void addAITank() {
 		Vector2D pos = Vector2D.getRandomXY(VISIBLE_BOUNDS.getMinX(),
 				VISIBLE_BOUNDS.getMaxX(), VISIBLE_BOUNDS.getMinY(),
 				VISIBLE_BOUNDS.getMaxY());
 		Tank t = new Tank(this, Tank.Team.RED, pos, true);
 		t.setTarget(m_tank);
 		m_tankLayer.add(t);		
 	}
 }
