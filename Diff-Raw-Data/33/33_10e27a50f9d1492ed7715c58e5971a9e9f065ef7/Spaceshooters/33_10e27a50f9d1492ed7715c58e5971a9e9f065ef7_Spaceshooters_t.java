 package spaceshooters.main;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import spaceshooters.api.plugins.PluginLoader;
 import spaceshooters.exceptions.OSNotSupportedException;
 import spaceshooters.gfx.ImageRegister;
 import spaceshooters.gui.font.Font;
 import spaceshooters.gui.font.Fonts;
 import spaceshooters.script.ScriptEngine;
 import spaceshooters.sfx.SoundEngine;
 import spaceshooters.states.ClientMultiplayerState;
 import spaceshooters.states.GameOverState;
 import spaceshooters.states.GameplayState;
 import spaceshooters.states.HostMultiplayerClass;
 import spaceshooters.states.MainMenuState;
 import spaceshooters.states.OptionsState;
 import spaceshooters.states.PluginsState;
 import spaceshooters.states.PreMultiplayerState;
 import spaceshooters.states.StoreState;
 import spaceshooters.states.TestLevelState;
 import spaceshooters.util.ErrorFrame;
 import spaceshooters.util.config.Configuration;
 import spaceshooters.util.config.IntOptions;
 import spaceshooters.util.config.Options;
 
 /**
  * The main game class. All states are initialized here. Game starting point is in {@link Launcher}, though.
  * 
  * @author Mat
  * 
  */
 public class Spaceshooters extends StateBasedGame {
 	
 	public static final String VERSION = "Alpha 1.1";
 	public static final int WIDTH = 800;
 	public static final int HEIGHT = 600;
	private Configuration config = Configuration.getConfiguration();
 	private static Font font;
 	
 	Spaceshooters() throws SlickException {
 		super("Spaceshooters 2 " + VERSION);
		// config
 	}
 	
 	@Override
 	public void initStatesList(GameContainer container) throws SlickException {
 		this.init();
 		this.addState(new MainMenuState());
 		this.addState(new GameplayState());
 		this.addState(new StoreState());
 		this.addState(new PreMultiplayerState());
 		this.addState(new HostMultiplayerClass());
 		this.addState(new ClientMultiplayerState());
 		this.addState(new PluginsState());
 		this.addState(new OptionsState());
 		this.addState(new TestLevelState());
 		this.addState(new GameOverState());
 	}
 	
 	public void init() throws SlickException {
 		if (config.getInt(IntOptions.FONT_INDEX) == Fonts.DEFAULT.getFontIndex()) {
 			font = new Font("gfx/fonts/default.ttf", 16);
 		} else if (config.getInt(IntOptions.FONT_INDEX) == Fonts.DEFAULT_2.getFontIndex()) {
 			font = new Font("gfx/fonts/default2.ttf", 16);
 		} else {
 			font = new Font("gfx/fonts/space.ttf", 16);
 		}
 		
 		ImageRegister.registerImages(true);
 		
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				PluginLoader.initPlugins();
 				SoundEngine.init();
 				ScriptEngine.validateScripts();
 			}
 		}).start();
 	}
 	
 	public void start() throws SlickException {
 		AppGameContainer game = new AppGameContainer(this, WIDTH, HEIGHT, config.getBoolean(Options.FULLSCREEN));
 		game.setVerbose(false);
 		game.setShowFPS(false);
 		if (config.getBoolean(Options.LIMIT_FPS)) {
 			game.setTargetFrameRate(75);
 		}
 		if (config.getBoolean(Options.USE_VSYNC)) {
 			game.setVSync(true);
 		}
 		game.setMinimumLogicUpdateInterval(20); // 50 ticks per second.
 		game.setMaximumLogicUpdateInterval(20);
 		game.setAlwaysRender(true);
 		game.setUpdateOnlyWhenVisible(false);
 		game.start();
 	}
 	
 	public static Font getFont() {
 		return font;
 	}
 	
 	public static String getPath() {
 		String os = System.getProperty("os.name").toLowerCase();
 		if (os.indexOf("win") >= 0) {
 			return System.getenv("APPDATA") + "/Spaceshooters 2/";
 		} else if (os.indexOf("mac") >= 0) {
 			return System.getProperty("user.home") + "/Library/Application Support/Spaceshooters 2/";
 		} else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0) {
 			return System.getProperty("user.home") + "/Spaceshooters 2/";
 		} else {
 			new ErrorFrame(new OSNotSupportedException(os));
 			return null;
 		}
 	}
 	
 	public static String getPluginsPath() {
 		return getPath() + "plugins/";
 	}
 	
 	public static String getGraphicsFolder() {
		return Configuration.getConfiguration().getBoolean(Options.OLD_GRAPHICS) ? "gfx/org/" : "gfx/new/";
 	}
 }
