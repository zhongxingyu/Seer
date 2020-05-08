 package playground;
 
 import java.io.IOException;
 
 import org.lwjgl.Sys;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.util.FastTrig;
 import org.newdawn.slick.util.Log;
 
 import playground.games.GameList;
 import playground.games.GameRecord;
 import playground.games.GameStore;
 import playground.games.GameStoreFactory;
 import playground.jnlp.LaunchConfig;
 import playground.jnlp.Launcher;
 
 /**
  * The main playground application for launching games.
  *
  * @author kevin
  */
 public class Playground extends BasicGame implements PodListener {
 	/** The list of pods for the alt menu at the bottom of the screen*/
 	private PodGroup alt = new PodGroup();
 	
 	/** The logo to display in the bottom right corer */
 	private Image logo;
 	/** The container holding this application */
 	private GameContainer container;
 	/** The location at which we'll cache launcher data */
 	private String launchCacheLocation;
 	/** The location at which we'll cache image data */
 	private String dataCacheLocation;
 	/** The location at which we'll cache games store data */
 	private String storeCacheLocation;
 	
 	/** The middle alt-menu pod */
 	private Pod quitPod;
 	/** The right alt-menu pod */
 	private Pod nextPod;
 	/** The left alt-menu pod */
 	private Pod prevPod;
 	
 	/** The version of java available */
 	private String version;
 	/** The record for the game currently being displayed (if any) */
 	private GameRecord current;
 	/** True if we're reinitialising after running a game */
 	private boolean reinit;
 	/** True if we're waiting for a download */
 	private boolean waitingForDownload;
 	/** The configuration that should be executed to launch the game */
 	private LaunchConfig config;
 	/** The bouce angle for the message display */
 	private double ang;
 	
 	/** The store that lists the games available */
 	private GameStore store;
 	/** The current state being display, main menu, games list, categories etc */
 	private State currentState;
 	/** The splash screen state */
 	private SplashState splash;
 	/** The list of states */
 	private State[] states = new State[10];
 	/** The games visual data cached */
 	private GamesData gData;
 	/** The state showing a list of games */
 	private GamesListState gamesListState;
 	/** The last state ID */
 	private int lastID;
 	/** The state display the information about a single game */
 	private InfoState infoState;
 	
 	/** The container holding this app */
 	private AppGameContainer app;
 	/** The theme colour */
 	private Color theme = new Color(1,0,0,1);
 	/** True if we've completed the initial cache */
 	private boolean doneInitialCache;
 	
 	/** 
 	 * Create a new playground
 	 */
 	public Playground() {
 		super("Playground");
 
 		storeCacheLocation = System.getProperty("user.home")+"/"+".playground";
 		dataCacheLocation = System.getProperty("user.home")+"/"+".playground/imagecache";
 		gData = new GamesData(dataCacheLocation);
 		
 		launchCacheLocation = System.getProperty("user.home")+"/"+".playground/cache";
 		version = "JRE "+System.getProperty("java.version").substring(0,3);
 	}
 	
 	/**
 	 * Set the list of games to be displayed in the games list state
 	 * 
 	 * @param list The list of games to be displayed
 	 */
 	public void setGamesList(GameList list) {
 		gamesListState.setList(list);
 	}
 	
 	/**
 	 * Get the visual data cached about the games (logos and thumbs)
 	 * 
 	 * @return The visual data cached about the games
 	 */
 	public GamesData getGamesData() {
 		return gData;
 	}
 	
 	/**
 	 * Enter a state specified by its ID
 	 * 
 	 * @param id The ID of the state to enter
 	 */
 	public void enterState(int id) {
 		if (id < 0) {
 			return;
 		}
 		
 		int oldID = lastID;
 		if (lastID == id) {
 			return;
 		}
 		lastID = id;
 		
 		if (currentState != null) {
 			currentState.leave(this);
 		}
 		
 		State state = states[id];
 		currentState = state;
 		
 		updateLabels(currentState);
 		currentState.enter(oldID, this);
 	}
 	
 	/**
 	 * Update the alt-menu labels
 	 * 
 	 * @param state The state we've entered
 	 */
 	private void updateLabels(State state) {
 		quitPod.setEnabled(currentState.getBackLabel() != null);
 		prevPod.setEnabled(currentState.getPrevLabel() != null);
 		nextPod.setEnabled(currentState.getNextLabel() != null);
 		if (state.getBackLabel() != null) {
 			quitPod.setLabel(state.getBackLabel());
 		}
 		if (state.getPrevLabel() != null) {
 			prevPod.setLabel(state.getPrevLabel());
 		}
 		if (state.getNextLabel() != null) {
 			nextPod.setLabel(state.getNextLabel());
 		}
 	}
 	
 	/**
 	 * @see org.newdawn.slick.BasicGame#init(org.newdawn.slick.GameContainer)
 	 */
 	public void init(GameContainer container) throws SlickException {
 		if (container instanceof AppGameContainer) {
 			app = (AppGameContainer) container;
 		}
 		
 		Thread t = new Thread() {
 			public void run() {
 				try { Thread.sleep(1000); } catch (Exception e) {};
 				try {
 					store = GameStoreFactory.getGameStore(storeCacheLocation);
 
 					GameList list = store.getGames();
 					for (int i=0;i<list.size();i++) {
 						gData.cache(list.getGame(i));
 					}
				} catch (Throwable e) {
 					Log.error(e);
					System.exit(0);
 				}
 				doneInitialCache = true;
 			}
 		};
 		t.start();
 		
 		container.setIcons(new String[] {"res/icon.png","res/icon32.png", "res/icon24.png"});
 		
 		alt = new PodGroup();
 		this.container = container;
 		
 		Resources.init();
 		
 		logo = new Image("res/logo.png");
 		container.setShowFPS(false);
 		container.setVSync(true);
 		
 		states[SplashState.ID] = splash = new SplashState();
 		
 		quitPod = new Pod(this, null, Resources.font, 365,570,60,20,"Quit");
 		prevPod = new Pod(this, null, Resources.font, 265,570,60,20,"Prev");
 		nextPod = new Pod(this, null, Resources.font, 465,570,60,20,"Next");
 		
 		prevPod.setEnabled(false);
 		nextPod.setEnabled(false);
 		alt.add(quitPod);
 		alt.add(prevPod);
 		alt.add(nextPod);
 		
 		if (reinit) {
 			updateLabels(currentState);
 		} else {
 			enterState(SplashState.ID);
 		}
 	}
 
 	/**
 	 * Exit the application
 	 */
 	public void exit() {
 		container.exit();
 	}
 	
 	/**
 	 * Init the world after the store has been resolved
 	 */
 	private void initialize() {
 		gData.loadImages();
 
 		if (!reinit) {
 			states[MainMenuState.ID] = new MainMenuState(this, store, reinit);
 			states[GamesListState.ID] = gamesListState = new GamesListState(this, store);
 			states[CategoriesState.ID] = new CategoriesState(this, store);
 			states[InfoState.ID] = infoState = new InfoState(this);
 			
 			gamesListState.setList(store.getGames());
 		} else {
 			for (int i=0;i<states.length;i++) {
 				if (states[i] != null) {
 					states[i].reinit();
 				}
 			}
 		}
 		
 		reinit = true;
 	}
 	
 	/**
 	 * @see org.newdawn.slick.BasicGame#update(org.newdawn.slick.GameContainer, int)
 	 */
 	public void update(GameContainer container, int delta) throws SlickException {
 		if (doneInitialCache) {
			doneInitialCache = false;
 			initialize();
 			enterState(MainMenuState.ID);
 		}
 		
 		if (config != null) {
 			try { Thread.sleep(100); } catch (Exception e) {}
 			return;
 		}
 		ang += delta * 0.01f;
 		
 		if (waitingForDownload) {
 			return;
 		}
 		
 		currentState.update(container, delta);
 		alt.update(container, delta);
 	}
 
 	/**
 	 * @see org.newdawn.slick.BasicGame#render(org.newdawn.slick.GameContainer, org.newdawn.slick.Graphics)
 	 */
 	public void render(GameContainer container, Graphics g) throws SlickException {
 		Texture.bindNone();
 		GL11.glBegin(GL11.GL_QUADS);
 			GL11.glColor3f(0.4f, 0.4f, 0.7f);
 			GL11.glVertex3f(0, 0, 0);
 			GL11.glColor3f(0.3f, 0.3f, 0.6f);
 			GL11.glVertex3f(800, 0, 0);
 			GL11.glColor3f(0.1f, 0.1f, 0.4f);
 			GL11.glVertex3f(800, 600, 0);
 			GL11.glColor3f(0.2f, 0.2f, 0.5f);
 			GL11.glVertex3f(0, 600, 0);
 		GL11.glEnd();
 			
 		logo.draw(600,400,200,200,new Color(0,0,0,0.1f));
 		
 		currentState.render(container, g);
 		
 		if (currentState == splash) {
 			return;
 		}
 		
 		alt.draw(container, g);
 
 		Texture.bindNone();
 		GL11.glBegin(GL11.GL_QUADS);
 			GL11.glColor3f(0.2f, 0.2f, 0.4f);
 			GL11.glVertex3f(0, 0, 0);
 			GL11.glVertex3f(800, 0, 0);
 			GL11.glColor3f(0.0f, 0.0f, 0.2f);
 			GL11.glVertex3f(800, 31, 0);
 			GL11.glVertex3f(0, 31, 0);
 			
 			GL11.glColor3f(0.4f, 0.4f, 0.7f);
 			GL11.glVertex3f(0, 31, 0);
 			GL11.glVertex3f(800, 31, 0);
 			GL11.glColor3f(0.2f, 0.2f, 0.5f);
 			GL11.glVertex3f(800, 33, 0);
 			GL11.glVertex3f(0, 33, 0);
 		GL11.glEnd();
 		
 		Resources.font2.drawString(6, 2, "Playground",Color.white);
 		Resources.font2.drawString(container.getWidth()-10-Resources.font2.getWidth(version), 2, version,Color.white);
 
 		if (waitingForDownload) {
 			if (config != null) {
 				waitingForDownload = false;
 				LaunchConfig local = config;
 				config = null;
 				
 				DisplayMode mode = Display.getDisplayMode();
 				try {
 					Display.update();
 					Mouse.destroy();
 					Keyboard.destroy();
 					Display.destroy();
 					
 					local.run();
 				} catch (Exception e) {
 					Log.error(e);
 				}
 				
 				try {
 					Display.setDisplayMode(mode);
 					Display.create();
 					container.reinit();
 				} catch (Exception e) {
 					Log.error(e);
 					System.exit(0);
 				}
 			} else {
 				Texture.bindNone();
 				GL11.glBegin(GL11.GL_QUADS);
 					GL11.glColor4f(0,0,0,0.9f);
 					GL11.glVertex3f(0, 0, 0);
 					GL11.glVertex3f(800, 0, 0);
 					GL11.glVertex3f(800, 600, 0);
 					GL11.glVertex3f(0, 600, 0);
 				GL11.glEnd();
 				
 				Resources.font.drawString(180,(int) (300+(FastTrig.cos(ang)*10)),"Getting game.. (this needs work)");
 				try { Thread.sleep(50); } catch (Exception e) {};
 			}
 		}
 	}
 	
 	/**
 	 * @see playground.PodListener#podMoveCompleted(playground.Pod)
 	 */
 	public void podMoveCompleted(Pod pod) {
 	}
 
 	/**
 	 * Set the information on the game to be displayed
 	 * 
 	 * @param info The information on the game to be displayed
 	 */
 	public void setInfo(GameRecord info) {
 		 infoState.setInfo(dataCacheLocation, launchCacheLocation, info);
 	}
 	
 	/**
 	 * @see playground.PodListener#podSelected(playground.Pod, java.lang.String)
 	 */
 	public void podSelected(Pod pod, String name) {
 		if (pod == prevPod) {
 			currentState.prevSelected();
 		}
 		if (pod == nextPod) {
 			currentState.nextSelected();
 		}
 		if (pod == quitPod) {
 			currentState.backSelected();
 		}
 	}
 
 	/**
 	 * Perform the download and potential launch of a game
 	 * 
 	 * @param current The info describing the game to be launched
 	 * @param update True if we should attempt to update the cache before running
 	 * @param run True if we should attempt to run the game
 	 */
 	public void doDownload(final GameRecord current, final boolean update, final boolean run) {
 		Thread t = new Thread() {
 			public void run() {
 				waitingForDownload = true;
 				config = null;
 				try {
 					Launcher launcher = new Launcher(launchCacheLocation);
 					LaunchConfig localConfig = launcher.getLaunch(current.getID(), current.getJNLP(), update);
 					if (run) {
 						config = localConfig;
 					} else {
 						waitingForDownload = false;
 					}
 				} catch (IOException e) {
 					waitingForDownload = false;
 				}
 			}
 		};
 		t.setDaemon(false);
 		t.start();
 	}
 
 	/**
 	 * @see org.newdawn.slick.BasicGame#keyPressed(int, char)
 	 */
 	public void keyPressed(int key, char c) {
 		if (key == Input.KEY_F1) {
 			if (app != null) {
 				try {
 					app.setFullscreen(!app.isFullscreen());
 				} catch (SlickException e) {
 					Log.error(e);
 				}
 			}
 		}
 		if (key == Input.KEY_ESCAPE) {
 			container.exit();
 		}
 	}
 	
 	/**
 	 * Entry point into the application
 	 * 
 	 * @param argv The arguments passed to the application
 	 */
 	public static void main(String[] argv) {
 		try {
 			AppGameContainer container = new AppGameContainer(new Playground(), 800, 600, false);
 			container.start();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
