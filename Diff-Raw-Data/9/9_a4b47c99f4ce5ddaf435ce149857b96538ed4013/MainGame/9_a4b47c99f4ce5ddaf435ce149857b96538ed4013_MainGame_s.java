 package main;
 import java.lang.reflect.Field;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.ScalableGame;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import states.LevelSelectState;
 import states.LevelState;
 import states.ResultsState;
 import states.TitleState;
 import config.Config;
 import config.Section;
 import entities.Player;
 
 /**
  * 
  */
 
 /**
  * @author Mullings
  *
  */
 public class MainGame extends StateBasedGame {
 	public static Player player;
 	
 	public MainGame(String title) {
 		super(title);
 	}
 	
 	public static void main(String[] arguments) {
 		
 		String os = System.getProperty("os.name").toLowerCase();
 		if (os.matches(".*linux.*")) {
 			System.setProperty( "java.library.path", "lib/linux/" );
 		} else if (os.matches(".*windows.*")) {
 			System.setProperty( "java.library.path", "lib\\windows\\" );
 		} else if (os.matches(".*mac.*")) {
 			System.setProperty( "java.library.path", "lib/macosx/" );
 		} else {
 			System.out.println("Could not identify operating system: " + os);
 			System.exit(1);
 		}
 		
 		try {
 			Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
 			fieldSysPath.setAccessible( true );
 			fieldSysPath.set( null, null );
 			ScalableGame game = new ScalableGame(new MainGame("Seasons"), Config.RESOLUTION_WIDTH, Config.RESOLUTION_HEIGHT);
 			AppGameContainer app = new AppGameContainer(game);
			setFullscreen(app, false);
 			app.setVSync(true);
 			app.setAlwaysRender(true);
 			app.setTargetFrameRate(Config.ACTIVE_FRAME_RATE);
 			app.start();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void initStatesList(GameContainer gc) throws SlickException {
 		Config.loadTimes();
 		Config.loadFonts();
 		Config.initMusic();
 		player = new Player(Config.PLAYER_WIDTH, Config.PLAYER_HEIGHT, Config.PLAYER_GROUND);
 		player.setDrawWidth(Config.PLAYER_DRAW_WIDTH);
 		addState(new TitleState());
 		addState(new LevelSelectState());
 		addState(new ResultsState());
 		for (Section s : Section.values()) {
 			addState(new LevelState(s));
 		}
 	}
 	
 	public static void setFullscreen(AppGameContainer app, boolean fullscreen) throws SlickException {
 		if (fullscreen) {
 			app.setDisplayMode(app.getScreenWidth(), app.getScreenHeight(), true);
 		} else {
 			app.setDisplayMode(Config.RESOLUTION_WIDTH, Config.RESOLUTION_HEIGHT, false);
 		}
 	}
 
 }
