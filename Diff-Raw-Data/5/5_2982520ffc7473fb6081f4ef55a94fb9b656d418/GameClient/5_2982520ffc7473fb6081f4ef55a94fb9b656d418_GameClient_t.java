 /**
  * This file is part of Distro Wars (Client).
  * 
  *  Distro Wars (Client) is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  Distro Wars (Client) is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with Distro Wars (Client).  If not, see <http://www.gnu.org/licenses/>.
 */
 package net.k3rnel.arena.client;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ConcurrentModificationException;
 import java.util.HashMap;
 import java.util.List;
 
 import mdes.slick.sui.Container;
 import mdes.slick.sui.Display;
 import mdes.slick.sui.event.ActionEvent;
 import mdes.slick.sui.event.ActionListener;
 import net.k3rnel.arena.client.backend.Animator;
 import net.k3rnel.arena.client.backend.BattleManager;
 import net.k3rnel.arena.client.backend.ClientMap;
 import net.k3rnel.arena.client.backend.ClientMapMatrix;
 import net.k3rnel.arena.client.backend.SoundManager;
 import net.k3rnel.arena.client.backend.SpriteFactory;
 import net.k3rnel.arena.client.backend.entity.OurPlayer;
 import net.k3rnel.arena.client.backend.entity.Player;
 import net.k3rnel.arena.client.backend.entity.Player.Direction;
 import net.k3rnel.arena.client.backend.time.TimeService;
 import net.k3rnel.arena.client.backend.time.WeatherService;
 import net.k3rnel.arena.client.backend.time.WeatherService.Weather;
 import net.k3rnel.arena.client.network.PacketGenerator;
 import net.k3rnel.arena.client.network.TCPManager;
 import net.k3rnel.arena.client.ui.LoadingScreen;
 import net.k3rnel.arena.client.ui.LoginScreen;
 import net.k3rnel.arena.client.ui.Ui;
 import net.k3rnel.arena.client.ui.base.ConfirmationDialog;
 import net.k3rnel.arena.client.ui.base.MessageDialog;
 import net.k3rnel.arena.client.ui.frames.CharacterSelectDialog;
 import net.k3rnel.arena.client.ui.frames.PlayerPopupDialog;
 import net.k3rnel.arena.client.utils.FileListing;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.loading.DeferredResource;
 import org.newdawn.slick.loading.LoadingList;
 import org.newdawn.slick.muffin.FileMuffin;
 
 /**
  * The game client
  * @author shadowkanji
  * @author ZombieBear
  * @author Nushio
  *
  */
 @SuppressWarnings({ "unchecked", "deprecation" })
 public class GameClient extends BasicGame {
 	//Some variables needed
 	private static GameClient m_instance;
 	private static AppGameContainer gc;
 	private ClientMapMatrix m_mapMatrix;
 	private OurPlayer m_ourPlayer = null;
 	private boolean m_isNewMap = false;
 	private int m_mapX, m_mapY, m_playerId;
 	private PacketGenerator m_packetGen;
 	private Animator m_animator;
 	private static HashMap<String, String> options;
 	//Static variables
 	private static String m_filepath="";
 	private static Font m_fontLarge, m_fontSmall, m_trueTypeFont;
 	private static String HOST;
 	//UI
 	private LoadingScreen m_loading;
 	private LoginScreen m_login;
 	private CharacterSelectDialog m_chooseChar;
 	//The gui display layer
 	private Display m_display;
 
 	private WeatherService m_weather;// = new WeatherService();
 	private TimeService m_time;// = new TimeService();
 	private Ui m_ui;
 	private Color m_daylight;
 	private static String m_language = "english";
 	private static boolean m_languageChosen = false;
 	private ConfirmationDialog m_confirm;
 	private PlayerPopupDialog m_playerDialog;
 	private static SoundManager m_soundPlayer;
 	private static boolean m_disableMaps = false;
 	public static String UDPCODE = "";
 
 	public static DeferredResource m_nextResource;
 	private boolean m_started;
 
 	Color m_loadGreen = new Color(132,185,0);
 
 	private boolean m_close = false; //Used to tell the game to close or not.
 
 	private static int m_width = 1024;
 	private static int m_height = 576;
 
 	private Image[] m_spriteImageArray = new Image[10]; /* WARNING: Replace with actual number of sprites */
 	/**
 	 * Load options
 	 */
 	static {
 		try {
 			try{
 				m_filepath = System.getProperty("res.path");
 				System.out.println("Path: "+m_filepath);
 				if(m_filepath==null)
 					m_filepath="";
 			}catch(Exception e){
 				m_filepath="";
 			}
 			options = new FileMuffin().loadFile("options.dat");
 			if (options == null) {
 				options = new HashMap<String,String>();
 				options.put("soundMuted", String.valueOf(true));
 				options.put("disableMaps", String.valueOf(false));
 				options.put("disableWeather", String.valueOf(false));
 			}
 			m_instance = new GameClient("Distro Wars (v0.0.1)");
			/*m_soundPlayer = new SoundManager();
 			m_soundPlayer.mute(Boolean.parseBoolean(options.get("soundMuted")));
			m_soundPlayer.start();*/
 			m_disableMaps = Boolean.parseBoolean(options.get("disableMaps"));
 		} catch (Exception e) { 
 			e.printStackTrace();
 			m_instance = new GameClient("Distro Wars (v0.0.1)");
 			m_disableMaps = false;
 			m_soundPlayer = new SoundManager();
 			m_soundPlayer.mute(true);
 			m_soundPlayer.start();
 		}
 	}
 
 	/**
 	 * Default constructor
 	 * @param title
 	 */
 	public GameClient(String title) {
 		super(title);
 	}
 
 	/**
 	 * Called before the window is created
 	 */
 	@Override
 	public void init(GameContainer gc) throws SlickException {
 
 		LoadingList.setDeferredLoading(true);
 
 
 		m_instance = this;
 		gc.getGraphics().setWorldClip(-32, -32,  m_width + 32, m_height + 32);
 		gc.setShowFPS(false);
 		m_display = new Display(gc);
 
 		/*
 		 * Setup variables
 		 */
 	
 		//		Player.loadSpriteFactory();
 
 		loadSprites();
 
 		try {
 		   m_fontLarge = new TrueTypeFont(java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, new File(m_filepath+"res/fonts/VeraSe.ttf"))
 	                .deriveFont(java.awt.Font.PLAIN, 10), false);
 		   m_fontSmall = new TrueTypeFont(java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, new File(m_filepath+"res/fonts/VeraMono.ttf"))
 	                .deriveFont(java.awt.Font.PLAIN, 10), false);
 		   m_trueTypeFont = new TrueTypeFont(java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, new File(m_filepath+"res/fonts/VeraSe.ttf"))
 					.deriveFont(java.awt.Font.PLAIN, 10), false);
 		} catch (Exception e) {e.printStackTrace(); m_trueTypeFont = m_fontSmall;}
 		/*
 		 * Time/Weather Services
 		 */
 		m_time = new TimeService();
 		m_weather = new WeatherService();
 		if(options != null)
 			m_weather.setEnabled(!Boolean.parseBoolean(options.get("disableWeather")));
 
 
 		/*
 		 * Add the ui components
 		 */
 		m_loading = new LoadingScreen();
 		m_display.add(m_loading);
 
 		m_chooseChar = new CharacterSelectDialog();
 		m_display.add(m_chooseChar);
 
 		m_login = new LoginScreen();
 		m_login.showLanguageSelect();
 		//		m_login.showCharacterSelect();
 		m_display.add(m_login);
 
 
 
 		m_ui = new Ui(m_display);
 		m_ui.setAllVisible(false);
 
 		/*
 		 * The animator and map matrix
 		 */
 		m_mapMatrix = new ClientMapMatrix();
 		m_animator = new Animator(m_mapMatrix);
 
 		gc.getInput().enableKeyRepeat(50, 300);
 		//		LoadingList.setDeferredLoading(false);
 
 	}
 
 	private void loadSprites() {
 		try {
 			String location;
 			String respath = System.getProperty("res.path");
 			if(respath==null)
 				respath="";
 			/*
 			 * WARNING: Change 224 to the amount of sprites we have in client
 			 * the load bar only works when we don't make a new SpriteSheet
 			 * ie. ss = new SpriteSheet(temp, 41, 51); needs to be commented out
 			 * in order for the load bar to work.
 			 */
 			location = respath+"res/characters/";
 			List<File> files = FileListing.getFiles(location);
 			int i = 0;
 			for(File file : files ){
 				try {
 					m_spriteImageArray[i] = new Image(file.getAbsolutePath());
 
 				} catch (Exception e) {
 					m_spriteImageArray[i] = new Image(file.getAbsolutePath());
 				}
 				i++;
 			}
 		} catch (Exception e) { 
 			e.printStackTrace();
 		}
 
 	}
 
 	void setPlayerSpriteFactory() {
 		Player.setSpriteFactory(new SpriteFactory(m_spriteImageArray));
 	}
 
 	/**
 	 * Updates the game window
 	 */
 	@Override
 	public void update(GameContainer gc, int delta) throws SlickException {
 		if (m_nextResource != null) {
 			try { 
 				m_nextResource.load();
 
 			} catch (Exception e) {
 				//throw new SlickException("Failed to load: " + m_nextResource.getDescription(), e);
 				System.err.println("Failed to load: " + m_nextResource.getDescription() + "\n"
 						+ "... WARNING: the game may or may not work because of this");
 			}
 
 			m_nextResource = null;
 		}
 
 		if (LoadingList.get().getRemainingResources() > 0) {
 			m_nextResource = LoadingList.get().getNext();
 		} else {
 			if (!m_started) {
 				m_started = true;
 				//				music.loop();
 				//				sound.play();
 				if(m_ui == null){
 					LoadingList.setDeferredLoading(false);
 
 					setPlayerSpriteFactory();
 
 					m_weather = new WeatherService();
 					m_time = new TimeService();
 					if(options != null)
 						m_weather.setEnabled(!Boolean.parseBoolean(options.get("disableWeather")));
 
 					m_ui = new Ui(m_display); 
 					m_ui.setAllVisible(false);	
 				}
 
 			}
 		}
 
 		if(m_started){
 			// make sure we can't move while chaging maps
 			if(m_loading.isVisible()){
 				gc.getInput().disableKeyRepeat();
 			}else{
 				gc.getInput().enableKeyRepeat(50, 300);
 
 			}
 			/*
 			 * Update the gui layer
 			 */
 			try {
 				synchronized (m_display) {
 					try{
 						m_display.update(gc, delta);
 					} catch (Exception e) {}
 				}
 			} catch (Exception e) { e.printStackTrace(); }
 			/*
 			 * Check if language was chosen.
 			 */
 			if(m_language != null && !m_language.equalsIgnoreCase("") && m_languageChosen==true && ((HOST != null && HOST.equalsIgnoreCase("")) || m_packetGen == null)){
 				m_login.showServerSelect();
 			} else if(m_language == null || m_language.equalsIgnoreCase("")){
 				m_login.showLanguageSelect();
 			}
 			/*
 			 * Check if we need to connect to a selected server
 			 */
 			if(HOST != null && !HOST.equalsIgnoreCase("") && m_packetGen == null) {
 				this.connect();
 			}
 			/*
 			 * Check if we need to loads maps
 			 */
 			if(m_isNewMap && m_loading.isVisible()) {
 				m_mapMatrix.loadMaps(m_mapX, m_mapY, gc.getGraphics());
 				while(m_ourPlayer == null);
 				m_mapMatrix.getCurrentMap().setName(m_mapMatrix.getMapName(m_mapX, m_mapY));
 				m_mapMatrix.getCurrentMap().setXOffset((m_width / 2) - m_ourPlayer.getX(), false);
 				m_mapMatrix.getCurrentMap().setYOffset((m_height / 2) - m_ourPlayer.getY(), false);
 				m_mapMatrix.recalibrate();
 				m_ui.getMap().setPlayerLocation();
 				m_isNewMap = false;
 				m_loading.setVisible(false);
 			}
 			/*
 			 * Animate the player
 			 */
 			if(m_ourPlayer != null) {
 				m_animator.animate();
 			}
 			/*
 			 * Update weather and daylight
 			 */
 			if(!m_isNewMap) {
 				int a = 0;
 				//Daylight
 				m_time.updateDaylight();
 				a = m_time.getDaylight();
 				//Weather
 				if(m_weather.isEnabled() && m_weather.getWeather() != Weather.NORMAL) {
 					try {
 						m_weather.getParticleSystem().update(delta);
 					} catch (Exception e) {
 						m_weather.setEnabled(false);
 					}
 					a = a < 100 ? a + 60 : a;
 				}
 				m_daylight = new Color(0, 0, 0, a);
 			}
 		}
 
 	}
 
 	/**
 	 * Renders to the game window
 	 */
 	public void render(GameContainer gc, Graphics g) throws SlickException {
 		if (m_nextResource != null) {
 			g.setColor(Color.white);
 			g.drawString("Loading: "+m_nextResource.getDescription(), 10, 100);
 		}
 
 		int total = LoadingList.get().getTotalResources();
 		int maxWidth = gc.getWidth() - 20;
 		int loaded = LoadingList.get().getTotalResources() - LoadingList.get().getRemainingResources();
 		if(!m_started){
 			float bar = loaded / (float) total;
 
 			// non-imagy loading bar
 			g.setColor(m_loadGreen);
 			g.setAntiAlias(true);
 			g.fillRoundRect(15, 154, bar*(maxWidth - 10), 20, 10);
 			g.setColor(Color.gray);
 			g.drawRoundRect(10 ,150, maxWidth, 28, 15);	
 
 
 
 
 		}
 
 		if (m_started){
 
 			/* Clip the screen, no need to render what we're not seeing */
 			g.setWorldClip(-32, -32, m_width + 64, m_height + 64);
 			/*
 			 * If the player is playing, run this rendering algorithm for maps.
 			 * The uniqueness here is:
 			 *  For the current map it only renders line by line for the layer that the player's are on, 
 			 *  other layers are rendered directly to the screen.
 			 *  All other maps are simply rendered directly to the screen.
 			 */
 			if(!m_isNewMap && m_ourPlayer != null) {
 				ClientMap thisMap;
 				g.setFont(m_fontLarge);
 				g.scale(2, 2);
 				for (int x = 0; x <= 2; x++) {
 					for (int y = 0; y <= 2; y++) {
 						thisMap = m_mapMatrix.getMap(x, y);
 						if (thisMap != null && thisMap.isRendering()) {
 							thisMap.render(thisMap.getXOffset() / 2,
 									thisMap.getYOffset() / 2, 0, 0,
 									(gc.getScreenWidth() - thisMap.getXOffset()) / 32,
 									(gc.getScreenHeight() - thisMap.getYOffset()) / 32,
 									false);
 						}
 					}
 				}
 				g.resetTransform();
 				try {
 					m_mapMatrix.getCurrentMap().renderTop(g);
 				}catch (ConcurrentModificationException e){
 					m_mapMatrix.getCurrentMap().renderTop(g);
 				}
 
 				if(m_mapX > -30) {
 					//Render the current weather
 					if(m_weather.isEnabled() && m_weather.getParticleSystem() != null) {
 						try {
 							m_weather.getParticleSystem().render();
 						} catch(Exception e) {
 							m_weather.setEnabled(false);
 						}
 					}
 					//Render the current daylight
 					if(m_time.getDaylight() > 0 || 
 							(m_weather.getWeather() != Weather.NORMAL && 
 									m_weather.getWeather() != Weather.SANDSTORM)) {
 						g.setColor(m_daylight);
 						g.fillRect(0, 0, 800, 600);
 					}
 				}
 			}
 			/*
 			 * Render the UI layer
 			 */
 			try {
 				synchronized(m_display) {
 					try{
 						m_display.render(gc, g);
 					} catch (ConcurrentModificationException e){m_display.render(gc, g);}
 				}
 			} catch (Exception e) { e.printStackTrace(); }	
 		}
 
 	}
 
 	/**
 	 * Accepts the user input.
 	 * @param key The integer representing the key pressed.
 	 * @param c ???
 	 */
 	@Override
 	public void keyPressed(int key, char c) {
 		if(m_started){
 			if (m_login.isVisible()){
 				if (key == (Input.KEY_ENTER) || key == (Input.KEY_NUMPADENTER))
 					m_login.enterKeyDefault();
 				if (key == (Input.KEY_TAB))
 					m_login.tabKeyDefault();
 			}
 
 			if (key == (Input.KEY_ESCAPE)) {
 				if(m_confirm==null){
 					ActionListener yes = new ActionListener() {
 						public void actionPerformed(ActionEvent arg0) {
 							try {
 								System.exit(0);
 							} catch (Exception e) {
 								e.printStackTrace();
 							}
 
 						}
 					};
 					ActionListener no = new ActionListener() {
 						public void actionPerformed(ActionEvent arg0) {
 							m_confirm.setVisible(false);
 							getDisplay().remove(m_confirm);
 							m_confirm = null;
 						}
 					};
 					m_confirm = new ConfirmationDialog("Are you sure you want to exit?",yes,no);
 					getUi().getDisplay().add(m_confirm);
 				}else{
 					System.exit(0);
 				}
 			}
 			if(m_ui.getNPCSpeech() == null && !m_ui.getChat().isActive() && !m_login.isVisible()
 					&& !getDisplay().containsChild(m_playerDialog) && !BattleManager.isBattling()){
 				if(m_ourPlayer != null && !m_isNewMap
 						/*&& m_loading != null && !m_loading.isVisible()*/
 						&& m_ourPlayer.canMove()) {
 					if (key == (Input.KEY_DOWN) || key == (Input.KEY_S)) {
 						if(!m_mapMatrix.getCurrentMap().isColliding(m_ourPlayer, Direction.Down)) {
 							m_packetGen.move(Direction.Down);
 							m_ourPlayer.queueMovement(Direction.Down);
 						} else if(m_ourPlayer.getDirection() != Direction.Down) {
 							m_packetGen.move(Direction.Down);
 							m_ourPlayer.queueMovement(Direction.Down);
 						}
 					} else if (key == (Input.KEY_UP) || key == (Input.KEY_W)) {
 						if(!m_mapMatrix.getCurrentMap().isColliding(m_ourPlayer, Direction.Up)) {
 							m_packetGen.move(Direction.Up);
 							m_ourPlayer.queueMovement(Direction.Up);
 						} else if(m_ourPlayer.getDirection() != Direction.Up) {
 							m_packetGen.move(Direction.Up);
 							m_ourPlayer.queueMovement(Direction.Up);
 						}
 					} else if (key == (Input.KEY_LEFT) || key == (Input.KEY_A)) {
 						if(!m_mapMatrix.getCurrentMap().isColliding(m_ourPlayer, Direction.Left)) {
 							m_packetGen.move(Direction.Left);
 							m_ourPlayer.queueMovement(Direction.Left);
 						} else if(m_ourPlayer.getDirection() != Direction.Left) {
 							m_packetGen.move(Direction.Left);
 							m_ourPlayer.queueMovement(Direction.Left);
 						}
 					} else if (key == (Input.KEY_RIGHT) || key == (Input.KEY_D)) {
 						if(!m_mapMatrix.getCurrentMap().isColliding(m_ourPlayer, Direction.Right)) {
 							m_packetGen.move(Direction.Right);
 							m_ourPlayer.queueMovement(Direction.Right);
 						} else if(m_ourPlayer.getDirection() != Direction.Right) {
 							m_packetGen.move(Direction.Right);
 							m_ourPlayer.queueMovement(Direction.Right);
 						}
 					} else if (key == Input.KEY_C) {
 						m_ui.toggleChat();
 					}
 				}
 			}
 			if ((key == (Input.KEY_SPACE) || key == (Input.KEY_E)) && !m_login.isVisible() &&
 					!m_ui.getChat().isActive()) {
 				if(m_ui.getNPCSpeech() == null && !getDisplay().containsChild(BattleManager.getInstance()
 						.getBattleWindow()) ){
 					//TODO: NPC Speech
 					//					m_packetGen.writeTcpMessage("Ct");
 				}
 				if (BattleManager.isBattling() && 
 						getDisplay().containsChild(BattleManager.getInstance().getTimeLine().getBattleSpeech())) {
 					BattleManager.getInstance().getTimeLine().getBattleSpeech().advance();
 				} else{
 					try {
 						m_ui.getNPCSpeech().advance();
 					} catch (Exception e) { 
 						m_ui.nullSpeechFrame();
 						//					m_packetGen.write("F"); 
 					}
 				}
 			}	
 		}
 
 	}
 
 	@Override
 	public void controllerDownPressed(int controller){
 		if(m_ui.getNPCSpeech() == null && m_ui.getChat().isActive()==false && !m_login.isVisible()
 				&& !m_ui.getChat().isActive() && !getDisplay().containsChild(m_playerDialog)){
 			if(m_ourPlayer != null && !m_isNewMap
 					/*&& m_loading != null && !m_loading.isVisible()*/
 					&& m_ourPlayer.canMove()) {
 				if(!m_mapMatrix.getCurrentMap().isColliding(m_ourPlayer, Direction.Down)) {
 					m_packetGen.move(Direction.Down);
 				} else if(m_ourPlayer.getDirection() != Direction.Down) {
 					m_packetGen.move(Direction.Down);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void controllerUpPressed(int controller){
 		if(m_ui.getNPCSpeech() == null && m_ui.getChat().isActive()==false && !m_login.isVisible()
 				&& !m_ui.getChat().isActive() && !getDisplay().containsChild(m_playerDialog)){
 			if(m_ourPlayer != null && !m_isNewMap
 					/*&& m_loading != null && !m_loading.isVisible()*/
 					&& m_ourPlayer.canMove()) {
 				if(!m_mapMatrix.getCurrentMap().isColliding(m_ourPlayer, Direction.Up)) {
 					m_packetGen.move(Direction.Up);
 				} else if(m_ourPlayer.getDirection() != Direction.Up) {
 					m_packetGen.move(Direction.Up);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void controllerLeftPressed(int controller){
 		if(m_ui.getNPCSpeech() == null && m_ui.getChat().isActive()==false && !m_login.isVisible()
 				&& !m_ui.getChat().isActive() && !getDisplay().containsChild(m_playerDialog)){
 			if(m_ourPlayer != null && !m_isNewMap
 					/*&& m_loading != null && !m_loading.isVisible()*/
 					&& m_ourPlayer.canMove()) {
 				if(!m_mapMatrix.getCurrentMap().isColliding(m_ourPlayer, Direction.Left)) {
 					m_packetGen.move(Direction.Left);
 				} else if(m_ourPlayer.getDirection() != Direction.Left) {
 					m_packetGen.move(Direction.Left);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void controllerRightPressed(int controller){
 		if(m_ui.getNPCSpeech() == null && m_ui.getChat().isActive()==false && !m_login.isVisible()
 				&& !m_ui.getChat().isActive() && !getDisplay().containsChild(m_playerDialog)){
 			if(m_ourPlayer != null && !m_isNewMap
 					/*&& m_loading != null && !m_loading.isVisible()*/
 					&& m_ourPlayer.canMove()) {
 				if(!m_mapMatrix.getCurrentMap().isColliding(m_ourPlayer, Direction.Right)) {
 					m_packetGen.move(Direction.Right);
 				} else if(m_ourPlayer.getDirection() != Direction.Right) {
 					m_packetGen.move(Direction.Right);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Accepts the mouse input
 	 */
 	@Override
 	public void mousePressed(int button, int x, int y) {
 		// Right Click
 		if (button == 1) {
 			// loop through the players and look for one that's in the
 			// place where the user just right-clicked
 			for (Player p : m_mapMatrix.getPlayers()) {
 				if ((x >= p.getX() + m_mapMatrix.getCurrentMap().getXOffset() && x <= p.getX() + 32 + m_mapMatrix.getCurrentMap().getXOffset()) 
 						&& (y >= p.getY() + m_mapMatrix.getCurrentMap().getYOffset() && y <= p.getY() + 40 + m_mapMatrix.getCurrentMap().getYOffset())) {
 					// Brings up a popup menu with player options
 					if (!p.isOurPlayer()){
 						if (getDisplay().containsChild(m_playerDialog))
 							getDisplay().remove(m_playerDialog);
 						m_playerDialog = new PlayerPopupDialog(p.getUsername());
 						m_playerDialog.setLocation(x, y);
 						getDisplay().add(m_playerDialog);
 					}
 				}
 			}
 		}
 		//Left click
 		if (button == 0){
 			//Get rid of the popup if you click outside of it
 			if (getDisplay().containsChild(m_playerDialog)){
 				if (x > m_playerDialog.getAbsoluteX() || x < m_playerDialog.getAbsoluteX()
 						+ m_playerDialog.getWidth()){
 					m_playerDialog.destroy();
 				} else if (y > m_playerDialog.getAbsoluteY() || y < m_playerDialog.getAbsoluteY() 
 						+ m_playerDialog.getHeight()){
 					m_playerDialog.destroy();
 				}
 			} 
 			//repeats space bar items (space bar emulation for mouse. In case you done have a space bar!)
 			try
 			{
 				if(getDisplay().containsChild(m_ui.getChat())){
 					m_ui.getChat().dropFocus();
 				}
 				if(m_ui.getNPCSpeech() == null && !getDisplay().containsChild(BattleManager.getInstance()
 						.getBattleWindow()) ){
 					//TODO: This.
 					System.out.println("Should've sent Ct over Network.");
 //					m_packetGen.writeTcpMessage("Ct");
 				}
 				if (BattleManager.isBattling() && 
 						getDisplay().containsChild(BattleManager.getInstance().getTimeLine().getBattleSpeech())) {
 					BattleManager.getInstance().getTimeLine().getBattleSpeech().advance();
 				} else{
 					try {
 						m_ui.getNPCSpeech().advance();
 					} catch (Exception e) { 
 						m_ui.nullSpeechFrame();
 						//					m_packetGen.write("F"); 
 					}
 				}
 			} catch (Exception e) {}
 		}
 	}
 
 
 	/**
 	 * Connects to a selected server
 	 */
 	public void connect() {
 		m_packetGen = new PacketGenerator();
 		/*
 		 * Connect via TCP to game server
 		 */
 
 		try {
 			TCPManager tcpManager = new TCPManager(HOST,this);
 			if(tcpManager.getClient().isConnected())
 				m_packetGen.setTcpConnection(tcpManager.getClient());
 		} catch (IOException e) {
 			e.printStackTrace();
 			messageDialog("Connection timed out.\n"
 					+ "The server may be offline.\n"
 					+ "Contact an administrator for assistance.", getDisplay());
 			HOST = "";
 			m_packetGen = null;
 		}
 		/*
 		 * Show login screen
 		 */
 		if(!HOST.equals(""))
 			m_login.showLogin();
 	}
 
 	/**
 	 * Returns the map matrix
 	 * @return
 	 */
 	public ClientMapMatrix getMapMatrix() {
 		return m_mapMatrix;
 	}
 
 	/**
 	 * If you don't know what this does, you shouldn't be programming!
 	 * @param args
 	 */
 	public static void main(String [] args) {
 		boolean fullscreen = false;
 		try {
 			fullscreen = Boolean.parseBoolean(options.get("fullScreen"));
 		} catch (Exception e) {
 			fullscreen = false;
 		}
 
 		// get the width and height and set it
 		String temp;
 		temp = getOptions().get("width");
 		m_width = Integer.parseInt(temp == null ? m_width + "" : temp);
 		temp = getOptions().get("height");
 		m_height = Integer.parseInt(temp == null ? m_height + "" : temp);
 
 		try {
 			// need to pull in size from preferences
 			gc = new AppGameContainer(new GameClient("Distro Wars (v0.0.1a)"),
 					m_width, m_height, fullscreen);
 			gc.setTargetFrameRate(50);
 			gc.start();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 
 
 	/**
 	 * When the close button is pressed... 
 	 * @param args
 	 */
 	public boolean closeRequested(){
 		if (m_confirm == null){
 			ActionListener yes = new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					try {
 						System.exit(0);
 						m_close = true;
 					} catch (Exception e) {
 						e.printStackTrace();
 						m_close = true;
 					}
 				}
 			};
 			ActionListener no = new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					m_confirm.setVisible(false);
 					getDisplay().remove(m_confirm);
 					m_confirm = null;
 					m_close = false;
 				}
 			};
 			m_confirm = new ConfirmationDialog("Are you sure you want to exit?",yes,no);
 			getUi().getDisplay().add(m_confirm);
 		}		
 		return m_close;
 	}
 
 	/**
 	 * Returns the font in large
 	 * @return
 	 */
 	public static Font getFontLarge() {
 		return m_fontLarge;
 	}
 
 	/**
 	 * Returns the font in small
 	 * @return
 	 */
 	public static Font getFontSmall() {
 		return m_fontSmall;
 	}
 
 	public static Font getTrueTypeFont() {
 		return m_trueTypeFont;
 	}
 
 	/**
 	 * Sets the server host. The server will connect once m_host is not equal to ""
 	 * @param s
 	 */
 	public static void setHost(String s) {
 		HOST = s;
 	}
 
 	/**
 	 * Returns this instance of game client
 	 * @return
 	 */
 	public static GameClient getInstance() {
 		return m_instance;
 	}
 
 	/**
 	 * Returns the packet generator
 	 * @return
 	 */
 	public PacketGenerator getPacketGenerator() {
 		return m_packetGen;
 	}
 
 	/**
 	 * Returns the login screen
 	 * @return
 	 */
 	public LoginScreen getLoginScreen() {
 		return m_login;
 	}
 
 	/**
 	 * Returns the loading screen
 	 * @return
 	 */
 	public LoadingScreen getLoadingScreen() {
 		return m_loading;
 	}
 
 	/**
 	 * Returns the weather service
 	 * @return
 	 */
 	public WeatherService getWeatherService() {
 		return m_weather;
 	}
 
 	/**
 	 * Returns the time service
 	 * @return
 	 */
 	public TimeService getTimeService() {
 		return m_time;
 	}
 
 	/**
 	 * Stores the player's id
 	 * @param id
 	 */
 	public void setPlayerId(int id) {
 		m_playerId = id;
 	}
 
 	/**
 	 * Returns this player's id
 	 * @return
 	 */
 	public int getPlayerId() {
 		return m_playerId;
 	}
 
 
 	/**
 	 * Resets the client back to the z
 	 */
 	public void reset() {
 		m_packetGen = null;
 		HOST = "";
 		try {
 			if(BattleManager.getInstance() != null)
 				BattleManager.getInstance().endBattle();
 			if(this.getUi().getNPCSpeech() != null)
 				this.getUi().getNPCSpeech().setVisible(false);
 			if(this.getUi().getChat() != null)
 				this.getUi().getChat().setVisible(false);
 			this.getUi().setVisible(false);
 		} catch (Exception e) {}
 		m_login.setVisible(true);
 		m_login.showLanguageSelect();
 	}
 
 	/**
 	 * Sets the map and loads them on next update() call
 	 * @param x
 	 * @param y
 	 */
 	public void setMap(int x, int y) {
 		m_mapX = x;
 		m_mapY = y;
 		m_isNewMap = true;
 		m_loading.setVisible(true);
 		m_soundPlayer.setTrackByLocation(m_mapMatrix.getMapName(x, y));
 	}
 
 	/**
 	 * Returns our player
 	 * @return
 	 */
 	public OurPlayer getOurPlayer() {
 		return m_ourPlayer;
 	}
 
 	/**
 	 * Sets our player
 	 * @param pl
 	 */
 	public void setOurPlayer(OurPlayer pl) {
 		m_ourPlayer = pl;
 	}
 
 	/**
 	 * Returns the user interface
 	 */
 	public Ui getUi() {
 		return m_ui;
 	}
 
 	public CharacterSelectDialog getChooseCharDialog() {
 		return m_chooseChar;
 	}
 
 	/**
 	 * Returns the File Path, if any
 	 */
 	public static String getFilePath() {
 		return m_filepath;
 	}
 
 	/**
 	 * Returns the options
 	 */
 	public static HashMap<String, String> getOptions() {
 		return options;
 	}
 
 	/**
 	 * Reloads options
 	 */
 	public static void reloadOptions() {
 		try {
 			options = new FileMuffin().loadFile("options.dat");
 			if (options == null) options = new HashMap<String,String>();
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(32);
 		}
 	}
 
 	/**
 	 * Returns the sound player
 	 * @return
 	 */
 	public static SoundManager getSoundPlayer() {
 		return m_soundPlayer;
 	}
 
 
 	/**
 	 * Creates a message Box
 	 */
 	public static void messageDialog(String message, Container container) {
 		new MessageDialog(message.replace('~','\n'), container);
 	}
 
 	/**
 	 * Returns the display
 	 */
 	public Display getDisplay(){
 		return m_display;
 	}
 
 	/**
 	 * Returns the language selection
 	 * @return
 	 */
 	public static String getLanguage() {
 		return m_language;
 	}
 	/**
 	 * Sets the language selection
 	 * @return
 	 */
 	public static String setLanguage(String lang) {
 		m_language = lang;
 		m_languageChosen=true;
 		return m_language;
 	}
 
 	/**
 	 * Changes the playing track
 	 * @param fileKey
 	 */
 	public static void changeTrack(String fileKey){
 		m_soundPlayer.setTrack(fileKey);
 	}
 
 	/**
 	 * Returns false if the user has disabled surrounding map loading
 	 * @return
 	 */
 	public static boolean disableMaps() {
 		return m_disableMaps;
 	}
 
 	/**
 	 * Sets if the client should load surrounding maps
 	 * @param b
 	 */
 	public static void setDisableMaps(boolean b) {
 		m_disableMaps = b;
 	}
 
 	public static AppGameContainer getGameContainer() {
 		return gc;
 	}
 
 	public void setDisplay() {
 		m_display.setHeight(gc.getHeight());
 		m_display.setWidth(gc.getWidth());
 	}
 
 	/**
 	 * Sets the field called 'width' to the given value.
 	 * @param width The width to set.
 	 */
 	@SuppressWarnings("static-access")
 	public void setWidth(int width) {
 		this.m_width = width;
 	}
 
 	/**
 	 * Returns the value of the field called 'width'.
 	 * @return Returns the width.
 	 */
 	public int getWidth() {
 		return (int) getDisplay().getWidth();
 	}
 
 	/**
 	 * Sets the field called 'height' to the given value.
 	 * @param height The height to set.
 	 */
 	public void setHeight(int height) {
 		GameClient.m_height = height;
 	}
 
 	/**
 	 * Returns the value of the field called 'height'.
 	 * @return Returns the height.
 	 */
 	public int getHeight() {
 		return (int) getDisplay().getHeight();
 	}
 
 	/**
 	 * Slick Native library finder.
 	 */
 	/*static {
 		String s = File.separator;
       	// Modify this to point to the location of the native libraries.
       	String newLibPath = System.getProperty("user.dir") + s + "lib" + s + "native";
       	System.setProperty("java.library.path", newLibPath);
 
       	Field fieldSysPath = null;
       	try {
         	fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
       	} catch (SecurityException e) {
         	e.printStackTrace();
       	} catch (NoSuchFieldException e) {
         	e.printStackTrace();
       	}
 
       	if (fieldSysPath != null) {
         	try {
           		fieldSysPath.setAccessible(true);
           		fieldSysPath.set(System.class.getClassLoader(), null);
         	} catch (IllegalArgumentException e) {
           		e.printStackTrace();
         	} catch (IllegalAccessException e) {
           		e.printStackTrace();
         	}
       	}
     }*/
 }
