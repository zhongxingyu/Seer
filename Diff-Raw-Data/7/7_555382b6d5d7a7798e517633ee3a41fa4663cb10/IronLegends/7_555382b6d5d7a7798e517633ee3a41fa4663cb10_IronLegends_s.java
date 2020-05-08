 package jig.ironLegends;
 
 import java.awt.Color;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.net.SocketException;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import javax.imageio.spi.ServiceRegistry;
 
 import jig.engine.ImageResource;
 import jig.engine.Mouse;
 import jig.engine.PaintableCanvas;
 import jig.engine.RenderingContext;
 import jig.engine.ResourceFactory;
 import jig.engine.ViewableLayer;
 import jig.engine.PaintableCanvas.JIGSHAPE;
 import jig.engine.audio.AudioState;
 import jig.engine.audio.jsound.AudioStream;
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
 import jig.ironLegends.core.SpecialFx;
 import jig.ironLegends.core.StaticBodyLayer;
 import jig.ironLegends.core.GameScreens.ScreenTransition;
 import jig.ironLegends.mapEditor.MapCalc;
 import jig.ironLegends.oxide.client.ClientInfo;
 import jig.ironLegends.oxide.client.ILClientThread;
import jig.ironLegends.oxide.console.ILConsoleCommandHandler;
 import jig.ironLegends.oxide.packets.ILPacket;
 import jig.ironLegends.oxide.packets.ILPacketFactory;
 import jig.ironLegends.oxide.packets.ILStartGamePacket;
 import jig.ironLegends.oxide.server.ILServerThread;
 import jig.ironLegends.router.ClientContext;
 import jig.ironLegends.router.IMsgTransport;
 import jig.ironLegends.router.ServerContext;
 import jig.ironLegends.screens.GameInfoTextLayer;
 import jig.ironLegends.screens.GameOverTextLayer;
 import jig.ironLegends.screens.GameOver_GS;
 import jig.ironLegends.screens.GamePlayTextLayer;
 import jig.ironLegends.screens.GamePlay_GS;
 import jig.ironLegends.screens.HelpScreen;
 import jig.ironLegends.screens.LobbyScreen;
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
 	public static final String RESOURCE_AUDIO = "jig/ironLegends/resources/audio/";
 
 	public static final String SPRITE_SHEET = RESOURCE_ROOT
 			+ "ironLegends-spritesheet.png";
 	public static final String SCREEN_SPRITE_SHEET = RESOURCE_ROOT
 			+ "screens/menus-spritesheet.png";
 
 	public static final String MY_RESOURCES = "ironLegends-resources.xml";
 	public static final String SCREEN_RESOURCES = "menus-resources.xml";
 	public static final String AUDIO_RESOURCES = "audio-resources.xml";
 
 	public static final int SPLASH_SCREEN = 0;
 	public static final int HELP_SCREEN = 1;
 	public static final int SERVER_SCREEN = 2;
 	public static final int LOBBY_SCREEN = 3;
 	public static final int GAMEOVER_SCREEN = 4;
 	public static final int GAMEPLAY_SCREEN = 5;
 
 	public static final int START_LIVES = 2;
 	public static final int PORT = 2555;
 	public VanillaPhysicsEngine m_physicsEngine;
 	public PolygonFactory m_polygonFactory;
 	public ResourceIO m_rr;
 	public GameScreens m_screens = new GameScreens();
 	public KeyCommands m_keyCmds = new KeyCommands();
 	public Fonts m_fonts = new Fonts();
 	public SoundFx m_soundFx;
 	public SpecialFx m_sfx;
 	
 	public LevelProgress m_levelProgress;
 	public GameProgress m_gameProgress;
 	public HighScore m_highScore = new HighScore();
 	public HighScorePersistance m_highScorePersist;
 	public MapCalc m_mapCalc;
 
 	public RadarHUD m_radarHUD;
 	public PowerUpHUD m_powerUpHUD;
 	public LifeHUD m_lifeHUD;
 	
 	public String m_mapName;
 	public PlayerInfo m_playerInfo;
 	public ClientInfo playerClient;
 	
 	public Tank m_tank = null;
 	public ViewableLayer m_bgLayer;
 	public BodyLayer<Body> m_entityLayer; // All entities in the game
 	public BodyLayer<Body> m_tankLayer;
 	public BodyLayer<Body> m_bulletLayer;
 	public BodyLayer<Body> m_tankObstacleLayer; // trees
 	public BodyLayer<Body> m_tankBulletObstacleLayer; // walls, buildings, rocks
 	public BodyLayer<Body> m_powerUpLayer;
 	public StaticBodyLayer<Body> m_hudLayer;
 	public Vector<SpawnInfo> m_spawnInfo;
 	public int _lastSpawnIndex = 0;
 	public SortedMap<Integer, Obstacle> m_obstacles;
 
 	public int m_numAITanks = 20;
 	public int[] m_AITankProb = {65, 15, 20}; // BASIC, SPEEDY, ARMORED
 	public boolean m_bGameOver = false;
 	public boolean m_bFirstLevelUpdate = false;
 	public boolean m_paused = false;
 	public boolean m_godmode = false;
 	public String m_sError;
 	public String m_sInstallDir;
 	public Obstacle m_redBase = null;
 	public Obstacle m_blueBase = null;
 	
 	public ClientContext m_client = null;
 	public ServerContext m_server = null;
 	public IMsgTransport m_clientMsgTransport = null;
 	public IMsgTransport m_serverMsgTransport = null;
 	public Vector<String> m_availableMaps;
 	
 	public ILClientThread client;
 	public Thread cThread;
 	public ILServerThread server;
 	public Thread sThread;
 	public boolean createdServer = false;
 	//public boolean multiPlayerMode = false;
 	public int maxActiveTanks = 4;
 	public boolean play_music = true;
 	private AudioStream music;
 	
 	public IronLegends() {
 		super(SCREEN_WIDTH, SCREEN_HEIGHT, false);
 		gameframe.setTitle("Iron Legends");
 
 		m_mapCalc = new MapCalc(SCREEN_WIDTH, SCREEN_HEIGHT);
 		setWorldDim(WORLD_WIDTH, WORLD_HEIGHT);
 		
 		m_sInstallDir = InstallInfo.getInstallDir("/" + GAME_ROOT
 				+ "IronLegends.class", "IronLegends.jar");		
 		// Load resources
 		loadResources();
 		
 		m_levelProgress = new LevelProgress();
 		m_gameProgress = new GameProgress(m_levelProgress);
 		
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
 
 		m_obstacles = new TreeMap<Integer, Obstacle>();
 		
 		// Server/Client
 		try {
 			// TODO: Remove magic numbers
 			this.client = new ILClientThread(20);
 			this.cThread = new Thread(this.client);
 			this.cThread.start();
 			
 			this.server = new ILServerThread(33);
 			this.sThread = new Thread(this.server);
 			this.sThread.start();
 		} catch (SocketException e) {
 			Logger.getLogger("global").warning("Host port already in use -- unable to host: "+ e.toString());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
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
 		resourceFactory.loadResources(RESOURCE_AUDIO, AUDIO_RESOURCES);
 		
 		m_fonts.create(resourceFactory);
 		
 		m_availableMaps = MapLoader.listMaps(m_sInstallDir, "IronLegends.jar");
 		m_rr = new ResourceIO(m_sInstallDir);
 		m_soundFx = new SoundFx();
 		m_sfx = new SpecialFx(m_soundFx);
 		
 		// Load Sound Effects
 		m_soundFx.addSfx("bullet", "Light_ta-Diode111-8758.wav");
 		m_soundFx.addSfx("tankExplosion", "2512__funhouse__tro_bassPan.wav");
 		m_soundFx.addSfx("bulletWall", "81045__Rock_Savage__Pistol_or_Hand_Gun_Firing_at_wood_short1.wav");
 		m_soundFx.addSfx("bulletHitTank", "81045__Rock_Savage__Pistol_or_Hand_Gun_Firing_at_wood_hitTank_short.wav");
 		
 		// Load Special Effects
 		m_sfx.add("tankExplosion", "tankExplosion", IronLegends.SPRITE_SHEET + "#explosion", 1500, 1);
 		
 		// Background Music
 		music = new AudioStream(RESOURCE_AUDIO + "ry_z-Forked_Road.mp3");
 	}
 
 	public void loadMap(String sMapFile) {		
 		m_levelProgress.reset();
 		m_entityLayer.clear();
 		m_tankLayer.clear();
 		m_bulletLayer.clear();
 		m_tankObstacleLayer.clear();
 		m_tankBulletObstacleLayer.clear();
 		m_powerUpLayer.clear();
 		m_obstacles.clear();
 		
 		IronLegendsMapLoadSink sink = new IronLegendsMapLoadSink(this);
 		MapLoader.loadLayer(sink, sMapFile, m_rr);
 
 		//m_levelProgress.setIntro(2999);
 		//m_bFirstLevelUpdate = false;
 	}
 
 	public void loadLevel(String mapFile) {
 		System.out.println("Loading map: " + mapFile);		
 		loadMap(mapFile);
 
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
 
 		m_keyCmds.addCommand("music", KeyEvent.VK_M);
 		m_keyCmds.addCommand("fixturret", KeyEvent.VK_T);
 		m_keyCmds.addCommand("fire", KeyEvent.VK_CONTROL);
 		m_keyCmds.addCommand("space", KeyEvent.VK_SPACE);
 		m_keyCmds.addCommand("pause", KeyEvent.VK_P);
 		m_keyCmds.addCommand("enter", KeyEvent.VK_ENTER);
 		m_keyCmds.addCommand("esc", KeyEvent.VK_ESCAPE);
 		m_keyCmds.addCommand("backspace", KeyEvent.VK_BACK_SPACE);
 
 		// Cheat codes
 		m_keyCmds.addCommand("splat", KeyEvent.VK_8);
 		m_keyCmds.addCommand("godmode", KeyEvent.VK_0);
 		m_keyCmds.addCommand("shield", KeyEvent.VK_1);
 		m_keyCmds.addCommand("upgrade", KeyEvent.VK_2);
 		m_keyCmds.addCommand("doublecannon", KeyEvent.VK_3);
 		m_keyCmds.addCommand("die", KeyEvent.VK_9);
 		
 		m_keyCmds.addAlphabet();
 	}
 
 	public void setupScreens() {
 		// background layer
 		ImageResource bkg = ResourceFactory.getFactory().getFrames(
 				RESOURCE_ROOT + "background.png").get(0);
 		m_bgLayer = new ImageBackgroundLayer(bkg, WORLD_WIDTH, WORLD_HEIGHT,
 				ImageBackgroundLayer.TILE_IMAGE);
 
 		// GamePlay Layers
 		m_entityLayer = new AbstractBodyLayer.NoUpdate<Body>();
 		m_tankLayer = new AbstractBodyLayer.NoUpdate<Body>();
 		m_bulletLayer = new AbstractBodyLayer.IterativeUpdate<Body>();
 		m_tankObstacleLayer = new AbstractBodyLayer.NoUpdate<Body>();
 		m_tankBulletObstacleLayer = new AbstractBodyLayer.IterativeUpdate<Body>();
 		m_powerUpLayer = new AbstractBodyLayer.IterativeUpdate<Body>();
 		
 		{
 			//m_hudLayer = new AbstractBodyLayer.NoUpdate<Body>();
 			m_hudLayer = new StaticBodyLayer.NoUpdate<Body>();
 			
 			// creates image... not actually renderable, is just a sprite
 			PaintableCanvas.loadDefaultFrames("radarhud_opponentbase", 4,4,1, JIGSHAPE.RECTANGLE, new Color(255,0,0));
 			PaintableCanvas.loadDefaultFrames("radarhud_teambase", 4,4,1, JIGSHAPE.RECTANGLE, new Color(255,255,255));
 			PaintableCanvas.loadDefaultFrames("radarhud_self", 4,4,1, JIGSHAPE.CIRCLE, new Color(255,255,255));
 			PaintableCanvas.loadDefaultFrames("radarhud_teammate", 4,4,1, JIGSHAPE.CIRCLE, new Color(0,0,255));
 			PaintableCanvas.loadDefaultFrames("radarhud_opponent", 4,4,1, JIGSHAPE.CIRCLE, new Color(255,0,0));
 			PaintableCanvas.loadDefaultFrames("health_total", 4,4,1, JIGSHAPE.RECTANGLE, new Color(255,0,0));
 			PaintableCanvas.loadDefaultFrames("health_remaining", 4,4,1, JIGSHAPE.RECTANGLE, new Color(0,255,0));
 				
 			int radiusInScreenUnits = 64;
 			int radarRangeWorldUnits = 1000;
 			
 			m_radarHUD = new RadarHUD(0,0, radiusInScreenUnits, radarRangeWorldUnits, this);
 			m_radarHUD.setWorldDim(WORLD_WIDTH,WORLD_HEIGHT);
 			m_powerUpHUD = new PowerUpHUD(0, SCREEN_HEIGHT, this);
 			m_lifeHUD = new LifeHUD(SCREEN_WIDTH, SCREEN_HEIGHT, this);
 			
 			m_spawnInfo = new Vector<SpawnInfo>();
 		}
 		// SCREENS
 		m_screens.addScreen(new SplashScreen(SPLASH_SCREEN, m_fonts,
 				m_playerInfo, this));
 		
 		m_screens.addScreen(new HelpScreen(HELP_SCREEN, m_fonts));
 		m_screens.addScreen(new ServerSelectScreen(SERVER_SCREEN, m_fonts, this));
 		
 		m_screens.addScreen(new LobbyScreen(LOBBY_SCREEN, m_fonts, this));
 		
 		m_screens.addScreen(new GamePlay_GS(GAMEPLAY_SCREEN, this));
 
 		GameScreen gameplayScreen = m_screens.getScreen(GAMEPLAY_SCREEN);
 		gameplayScreen.addViewableLayer(new GameInfoTextLayer(m_fonts,
 				m_gameProgress, m_highScore, this));
 		gameplayScreen.addViewableLayer(new GamePlayTextLayer(m_fonts,
 				m_gameProgress, m_playerInfo, this));
 
 		// gameover screen has all the layers of gameplay except the text layer
 		// is different
 		Iterator<ViewableLayer> iter = gameplayScreen.getViewableLayers();
 		GameScreen gameOverScreen = new GameOver_GS(GAMEOVER_SCREEN, this);
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
 
 	public void screenTransition(int iFromScreen, int iToScreen)
 	{
 		if (iFromScreen != iToScreen)
 		{			
 			GameScreen curScreen = m_screens.getScreen(iFromScreen);
 			m_screens.setActiveScreen(iToScreen);
 			GameScreen newScreen = m_screens.getActiveScreen();
 			if (curScreen != null)
 				curScreen.deactivate();
 			newScreen.activate(iFromScreen);
 			
 			if (iToScreen != GAMEPLAY_SCREEN) {
 				backgroundMusic(false);
 			} else {
 				backgroundMusic(play_music);
 			}
 		}
 	}
 	
 	public void processCommands(long deltaMs) {
 		m_keyCmds.update(keyboard);
 		int activeScreen = m_screens.activeScreen();
 		GameScreen curScreen = m_screens.getScreen(activeScreen);
 
 		if (curScreen != null) {
 			if (this.createdServer) {
 				this.server.update(deltaMs);
 			}
 			if (this.client != null)
 			{
 				this.client.update(deltaMs);
 			}
 			
 			int iNewScreen = curScreen.processCommands(m_keyCmds, mouse,
 					deltaMs);
 			int iCurScreen = curScreen.name();
 			screenTransition(iCurScreen, iNewScreen);
 		}
 
 		// Screen Transitions
 		ScreenTransition t = m_screens.transition(m_keyCmds);
 		if (t != null) {
 			screenTransition(t.m_from, t.m_to);
 		}
 
 		if (m_keyCmds.wasPressed("pause")) {
 			m_paused = !m_paused;
 			if (m_paused) {
 				backgroundMusic(false);
 			} else {
 				backgroundMusic(play_music);
 			}
 		}
 
 		if (activeScreen == GAMEPLAY_SCREEN) {
 			// Controls
 			if (m_keyCmds.wasPressed("music")) {
 				play_music = !play_music;
 				backgroundMusic(play_music);
 			}
 			
 			// Cheat Codes
 			if (m_keyCmds.wasPressed("godmode")) {
 				m_godmode = !m_godmode;
 			}
 			
 			if (m_keyCmds.wasPressed("die")) {
 				m_tank.explode();
 				m_gameProgress.tankDestroyed(m_tank);
 			}
 			
 			if (m_keyCmds.wasPressed("shield")) {
 				m_tank.setShield(true);
 			}
 			
 			if (m_keyCmds.wasPressed("upgrade")) {
 				m_tank.upgrade();
 			}
 
 			if (m_keyCmds.wasPressed("doublecannon")) {
 				m_tank.setWeapon(Tank.Weapon.DOUBLECANNON);
 			}
 			
 			// Update Spawn locations
 			Iterator<SpawnInfo> iter = m_spawnInfo.iterator();
 			while (iter.hasNext()) {
 				SpawnInfo s = iter.next();
 				s.update();
 			}
 		}
 	}
 
 	public void resetGame()
 	{
 		m_bGameOver = false;
 
 		m_gameProgress.reset();
 	}
 	public void newGame(String mapFile) {
 		resetGame();
 		
 		//String mapFile = "maps/mapitems.txt";
 
 		m_gameProgress.reset();		
 	
 		if (!isMultiPlayerMode())
 		{
 			// pick random map
 			mapFile = m_availableMaps.get((int) (Math.random() * m_availableMaps.size()));
 			int bx = mapFile.indexOf("maps/");
 			if (bx > 0) {
 				mapFile = mapFile.substring(bx);
 			}
 		}
 	//		String mapFile = "maps/grunge.txt";
 	//		String mapFile = "maps/helljungle.txt";		
 		if (client != null)
 		{
 			ILStartGamePacket startGamePacket = client.getStartGamePacket();
 			if (startGamePacket != null)
 			{
 				int i = startGamePacket.map.lastIndexOf("/");
 				String s = startGamePacket.map.substring(i+1);
 				
 				mapFile = "maps/" + s;
 			}
 		}
 		
 		loadLevel(mapFile);
 		if (client != null)
 			client.loadedMap = true;
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
 		// out of band entityStates for now
 		Vector<EntityState> entityStates = new Vector<EntityState>();
 		
 		if (m_client != null)
 		{
 			// send msgs to server
 			m_clientMsgTransport.send(m_client.getTxQueue());
 		}
 		if (server != null && client != null && isMultiPlayerMode())
 		{
 			if (server.createdTanks == true)
 			{
 				// TODO send start game msg with tank info
 				ILStartGamePacket msg = ILPacketFactory.newStartGamePacket(server.packetID()
 						, server.hostAddress.getHostAddress() + "\0"
 						, server.hostAddress.getHostAddress() + "\0"
 						, server.getMapName() + "\0");
 				msg.m_bGo = true;
 				msg.m_bSinglePlayer = false;
 				
 				{
 					Iterator<Body> iter = m_tankLayer.iterator();
 					while (iter.hasNext())
 					{
 						Tank t = (Tank) iter.next();
 						EntityState es = new EntityState();
 						
 						t.serverPopulate(es);
 						entityStates.add(es);
 					}
 				}
 				
 				msg.setEntityStates(entityStates);
 
 				server.send(msg);				
 			}
 			else if (client.loadedMap)
 			{
 				// create tanks at spawn locations
 				if (client.lobbyState.clients != null) {
 					m_tankLayer.clear();
 
 					Iterator<ClientInfo> itr = client.lobbyState.clients.iterator();
 					while (itr.hasNext())
 					{
 						ClientInfo c = itr.next();
 						addTank(c);
 					}
 					server.createdTanks = true;
 				}
 			}
 			else
 			{
 				// map not loaded, keep sending startGame packet
 				/*
 				//TODO: during ironlegends update, keep track of how many players are sending client updates
 				// if all clients are sending client updates, send start game with go = true
 				// client update could be modified to acknowledge it received go and then
 				// when server receives client update with go set from all clients
 				// then the server can stop sending go.
 				ILStartGamePacket msg = ILPacketFactory.newStartGamePacket(server.packetID()
 						, server.hostAddress.getHostAddress() + "\0"
 						, server.hostAddress.getHostAddress() + "\0"
 						, server.getMapName() + "\0");
 				msg.m_bGo = false;
 				msg.m_bSinglePlayer = false;
 				server.send(msg);
 				*/
 			}
 		}
 		
 		if (m_server != null)
 		{
 			//this.client.send(event)
 			//this.client.stateUpdates
 			/*
 			 * client
 			 * startGame received
 			 * stateUpdates
 			 * 
 			 * lobby,ready
 			 * server rx commandState
 			 */
 			// process client msgs
 		
 			while (m_serverMsgTransport.hasRxMsg())
 			{
 				ILPacket msg = m_serverMsgTransport.nextRxMsg();
 				//SPStartGame startGame = (SPStartGame)msg;
 				
 				ILStartGamePacket startGame = (ILStartGamePacket)msg;
 				// send msg responses to client
 				m_serverMsgTransport.send(startGame);	
 			}
 			// send entity states (game state)
 			if (!isMultiPlayerMode())
 			{
 				Iterator<Body> iter = m_tankLayer.iterator();
 				while (iter.hasNext())
 				{
 					Tank t = (Tank) iter.next();
 					EntityState es = new EntityState();
 					
 					t.serverPopulate(es);
 					entityStates.add(es);
 				}
 			}
 		}
 
 		if (client != null)
 		{
 			// this could be problematic if client receives a start game packet and overrides the member variable!
 			if (client.receivedStartGame && client.getStartGamePacket() != null)
 			{
 				ILStartGamePacket startGame = client.getStartGamePacket();
 				if (!client.loadedMap)
 				{
 					// load map and wait for game to start
 					screenTransition(m_screens.activeScreen(), GAMEPLAY_SCREEN);				
 				}
 				else if (m_tank == null && startGame.m_entityStates.size() > 0)
 				{
 					// for each tank, add if not already exists
 					Iterator<EntityState> iter = startGame.m_entityStates.iterator();
 					while (iter.hasNext())
 					{
 						EntityState es = iter.next();
 						addTank(es);
 					}
 					
 				}
 				else if (m_tank != null)
 				{
 					// TODO: tell server ready..?
 					if (startGame.m_bGo)
 						client.receivedGo = true;
 				}
 			}
 		}
 		
 		if (m_client != null)
 		{
 			// update tanks from entityStates
 			if (!isMultiPlayerMode())
 			{
 				// for each entityState, find appropriate entity and update
 				Iterator<EntityState> iter = entityStates.iterator();
 				while (iter.hasNext())
 				{
 					EntityState es = iter.next();
 					Tank t = findEntity(es.m_entityNumber);
 					t.clientUpdate(es);
 				}
 			}
 			
 			// process server msgs
 			while (m_clientMsgTransport.hasRxMsg())
 			{
 				ILPacket msg = m_clientMsgTransport.nextRxMsg();
 				@SuppressWarnings("unused")
 				ILStartGamePacket startGame = (ILStartGamePacket)msg;
 				//SPStartGame startGame = (SPStartGame)msg;
 				
 				// TODO: supply startGame parameters to gameplay screen 
 				// set active screen
 				screenTransition(m_screens.activeScreen(), GAMEPLAY_SCREEN);				
 			}
 		}
 		
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
 
 		GameScreen activeGS = m_screens.getActiveScreen();
 		activeGS.update(deltaMs);		
 	}
 
 	private void addTank(ClientInfo c) {
 		Tank t = new Tank(this, c.team, c.team==0?Tank.Team.RED:Tank.Team.BLUE, c.id);
 		
 		// set position of tank to one of "bluespawn" locations/orientations
 		setSpawn(t, c.team==0?"redspawn":"bluespawn");
 		if (t.getEntityNumber() == playerClient.id)
 		{
 			m_tank = t;
 			m_gameProgress.setSelf(t);
 			
 		}
 
 		m_tankLayer.add(t);
 		m_entityLayer.add(t);		
 	}
 
 	private void addTank(EntityState es) {
 		// find tank if not exist, add
 		Tank t = findEntity(es.m_entityNumber);
 		if (t == null)
 		{
 			// add tank
 			ClientInfo c = getClientInfo(es.m_entityNumber);
 			
 			addTank(c);			
 		}
 		
 		t.clientUpdate(es);	
 	}
 
 	private ClientInfo getClientInfo(int entityNumber) {
 
 		Iterator<ClientInfo> itr = client.lobbyState.clients.iterator();
 		while (itr.hasNext())
 		{
 			ClientInfo c = itr.next();
 			if (c.id == entityNumber)
 				return c;
 		}
 		return null;
 
 	}
 
 	public Tank findEntity(int entityNumber) {
 		Iterator<Body> iter = m_tankLayer.iterator();
 		while (iter.hasNext())
 		{
 			Tank t = (Tank) iter.next();
 			if (t.getEntityNumber() == entityNumber)
 				return t;
 		}
 		return null;
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
 		if (m_radarHUD != null)
 			m_radarHUD.setWorldDim(width, height);
 	}
 
 	public void setMapName(String mapName) {
 		m_mapName = mapName;
 		m_gameProgress.setMapName(m_mapName);		
 	}
 
 	public String getMapName() {
 		return m_mapName;
 	}
 	
 	public Mouse getMouse() {
 		return mouse;
 	}
 	
 	public void setSpawn(Tank tank, String spawnColor) {
 		// find "free" spawn locations
 		Iterator<SpawnInfo> iter = m_spawnInfo.iterator();
 		while (iter.hasNext())
 		{
 			SpawnInfo s = iter.next();
 			if (!s.isOccupied() && s.name().equals(spawnColor))
 			{
 				tank.setSpawn(s);
 				s.setOccupied(tank);
 				break;
 			}
 		}
 	}
 	
 	public void addAITank(int entityNumber) {
 		int c = getRandomChoice(m_AITankProb);
 		Tank t = new Tank(this, 1, Tank.Team.RED, Tank.getType(c), entityNumber, true);
 		t.setTarget(m_tank);
 		
 		// try to do round robin spawn selection
 		boolean found = false;
 		Iterator<SpawnInfo> iter = m_spawnInfo.iterator();
 		while (iter.hasNext()) {
 			SpawnInfo s = iter.next();
 			if (s.name().equals("redspawn") && s.getSequence() == _lastSpawnIndex && !s.isOccupied()) {
 				t.setSpawn(s);
 				s.setOccupied(t);
 				_lastSpawnIndex = ((_lastSpawnIndex == 3) ? 0 : _lastSpawnIndex + 1);
 				found = true;
 				break;
 			}
 		}
 		
 		if (!found) {
 			setSpawn(t, "redspawn");
 		}
 		
 		m_tankLayer.add(t);
 		m_entityLayer.add(t);
 	}
 	
 	public void addPowerUp(Body b) {
 		PowerUp power = null;
 		int c = -1;
 		// UPGRADE, REPAIR, SHIELD, LIFE, DAMAGE
 		if (b instanceof Tank) { // Tank destroyed
 			int[] prob = {4, 10, 10, 3, 7}; // probabilities
 			c = getRandomChoice(prob);
 		} else { // crate destroyed
 			int[] prob = {7, 25, 30, 5, 15}; // probabilities
 			c = getRandomChoice(prob);			
 		}
 		if (c == -1) {
 			return;
 		}
 		
 		PowerUp.Type t = PowerUp.getType(c);
 		// find existing, inactive powerup object
 		for (Body p : m_powerUpLayer) {
 			PowerUp pow = (PowerUp) p;
 			if (!p.isActive() && pow.getType() == t) {
 				power = pow;
 				power.reset();
 			}
 		}
 		
 		if (power == null) {
 			power = new PowerUp(t);
 			m_powerUpLayer.add(power);
 		}
 		
 		power.setCenterPosition(b.getCenterPosition());		
 	}
 	
 	public int getRandomChoice(int[] probabilities) {
 		Integer[] choices = new Integer[100];
 		int total = 0;
 		for (int i=0; i < probabilities.length; i++) {
 			for (int j=0; j < probabilities[i] && total < choices.length; j++) {
 				choices[total++] = i;
 			}
 		}
 		for (int j=0; total < choices.length; j++) {
 			choices[total++] = -1;
 		}
 		
 		java.util.Collections.shuffle(Arrays.asList(choices));
 		return choices[(int) (Math.random() * 100)];
 	}
 
 	public void setNumAITanks(int m_numAITanks) {
 		this.m_numAITanks = m_numAITanks;		
 	}
 
 	public int getNumAITanks() {
 		return m_numAITanks;
 	}
 
 	/*
 	public void setMultiPlayerMode(boolean multiPlayerMode) {
 		this.multiPlayerMode = multiPlayerMode;
 	}
 	*/
 
 	public boolean isMultiPlayerMode() {
 		if (client != null && client.receivedStartGame && client.getStartGamePacket() != null
 				&& !client.getStartGamePacket().m_bSinglePlayer)
 			return true;
 		return false;
 		//return multiPlayerMode;
 	}
 	
 	public void backgroundMusic(boolean play) {
 		if (play) {
 			if (music.getState() == AudioState.PRE || music.getState() == AudioState.STOPPED) {
 				music.play(.5);
 			} else if (music.getState() == AudioState.PAUSED) {
 				music.resume();
 			}			
 		} else {
 			if (music.getState() == AudioState.PLAYING) {
 				music.pause();
 			}
 		}
 	}	
 }
