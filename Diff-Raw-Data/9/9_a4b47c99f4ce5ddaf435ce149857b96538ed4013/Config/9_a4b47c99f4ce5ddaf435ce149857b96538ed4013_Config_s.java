 package config;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.EnumMap;
 import java.util.List;
 
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.font.effects.Effect;
 
 import time.Timer;
 
 import com.thoughtworks.xstream.XStream;
 
 
 
 public class Config {
 	
 	public static final String TITLE = "Seasons";
 	
 	// Resolution
 	public static final int RESOLUTION_WIDTH = 1366;
 	public static final int RESOLUTION_HEIGHT = 768;
	public static final boolean FULLSCREEN = false;
 	
 	// Frame Rate
 	public static final int ACTIVE_FRAME_RATE = 60;
 	public static final int INACTIVE_FRAME_RATE = 10;
 	
 	// Physics
 	public static final float GRAVITY = 50;
 	public static final float DEFAULT_DENSITY = 1f;
 	public static final float DEFAULT_FRICTION = .03f;
 	public static final float DEFAULT_TRACTION = 20f; // Friction for feet
 	public static final float PIXELS_PER_METER = 32;
 	public static final float VEL_EPSILON = .5f;
 	public static final int VELOCITY_ITERATIONS = 6;
 	public static final int POSITION_ITERATIONS = 2;
 	
 	// Map parsing
 	public static final int[] WALL_IDS = {2};
 	public static final int TILE_WIDTH = 32;
 	public static final int TILE_HEIGHT = 32;
 	public static final String MAP_PATH = "assets/maps/";
 	public static final String BACKGROUND_PATH = "assets/backgrounds/";
 	
 	// Object Filter Categories
 	public static final int HOOKABLE = 2;
 	public static final int WATER = 4;
 	public static final int SALMON = 8;
 	public static final int MUSHROOM = 16;
 	public static final int STEAM = 32;
 	public static final int STATICENTITY = 24;
 	
 	// Player
 	public static final float PLAYER_DENSITY = 1f;
 	public static final int PLAYER_WIDTH = 36;
 	public static final int PLAYER_HEIGHT = 80;
 	public static final int PLAYER_DRAW_WIDTH = 80;
 	public static final int PLAYER_DRAW_HEIGHT = 80;
 	public static final int PLAYER_GROUND = 6;
 	public static final int PLAYER_MAX_HP = 1;
 	public static final float PLAYER_MOVE_SPEED = 35f;
 	public static final float PLAYER_ACCELERATION = 100;
 	public static final float PLAYER_MAX_AIR_SPEED = 19;
 	public static final float PLAYER_AIR_ACCELERATION = 1f;
 	public static final float PLAYER_JUMP_SPEED = 115;
 	public static final float PLAYER_WATER_MOVE_SPEED = 2f;
 
 	// Obstacles
 	public static final float FLY_VEL = 10;
 
 	// Hookshot
 	public static final float HOOKSHOT_SHOOT_VEL = 100;
 	public static final float HOOKSHOT_PULL_VEL = 40;
 	public static final float HOOKSHOT_MAX_RANGE = 500;
 	public static final float HOOKSHOT_TOLERANCE = 30;
 	
 	// Movement
 	public static final float WATER_GRAVITY_SCALE = -1f;
 	public static final float WATER_DRAG = 10;
 	public static final float STEAM_GRAVITY_SCALE = -.7f;
 	public static final float STEAM_DRAG = 0f;
 	
 	// Sound
 	public static boolean soundOn = true;
 	public static float gameVolume = 1;
 	
 	// UI
 	// how far away from the player the aiming cursor appears
 	public static final float CURSOR_DIST = 100;
 	public static final int CURSOR_SIZE = 15;
 	public static final int SALMON_TIME = -500;
 	
 	public static final float PAUSE_BLINK = 2000;
 	
 	public static EnumMap<Section, Timer> times;
 	public static final UnicodeFont MENU_FONT = new UnicodeFont(new Font("", Font.PLAIN, 30));
 	public static final UnicodeFont PLAIN_FONT = new UnicodeFont(new Font("", Font.PLAIN,16));
 	public static final UnicodeFont BOLD_FONT = new UnicodeFont(new Font("", Font.BOLD,16));
 	public static final UnicodeFont BIG_FONT = new UnicodeFont(new Font("", Font.PLAIN, 70));
 	
 	// music
 	public static Music musicLoop;
 	public static Music levelSelectMusic;
 	public static Music titleMusic;
 	
 	@SuppressWarnings("unchecked")
 	public static void loadTimes() {
 		XStream x = new XStream();
 		try {
 			File f = new File("times.xml");
 			if (f.exists()) {
 				times = (EnumMap<Section, Timer>) x.fromXML(new FileInputStream(f));
 			} else {
 				createAndSaveTimes();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			createAndSaveTimes();
 		}
 	}
 	
 	public static void saveTimes() {
 		XStream x = new XStream();
 		File f = new File("times.xml");
 		try {
 			if (f.exists()) f.delete();
 			f.createNewFile();
 			FileWriter w = new FileWriter(f);
 			x.toXML(times, w);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static void createAndSaveTimes() {
 		times = new EnumMap<Section, Timer>(Section.class);
 		for (Section s : Section.values()) {
 			times.put(s, new Timer());
 		}
 		saveTimes();
 	}
 
 	@SuppressWarnings("unchecked")
 	public static void loadFonts() {
 		MENU_FONT.addAsciiGlyphs();
 		((List<Effect>) MENU_FONT.getEffects()).add(new ColorEffect(new Color(1, 1, 1, 0.8f)));
 
 		PLAIN_FONT.addAsciiGlyphs();
         ((List<Effect>) PLAIN_FONT.getEffects()).add(new ColorEffect(java.awt.Color.WHITE));
         
         BOLD_FONT.addAsciiGlyphs();
         ((List<Effect>) BOLD_FONT.getEffects()).add(new ColorEffect(java.awt.Color.WHITE));
         
         BIG_FONT.addAsciiGlyphs();
         ((List<Effect>) BIG_FONT.getEffects()).add(new ColorEffect(java.awt.Color.WHITE));
         
 		try {
 			MENU_FONT.loadGlyphs();
 			PLAIN_FONT.loadGlyphs();
 			BOLD_FONT.loadGlyphs();
 			BIG_FONT.loadGlyphs();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 	public static void initMusic() {
 		try {
 			titleMusic = new Music("assets/sounds/Field07.wav");
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		musicLoop = titleMusic;
 		levelSelectMusic = titleMusic;
 	}
 	
 	public static void playMusic(Music music) {
 		musicLoop.stop();
 		musicLoop = music;
 		musicLoop.loop();
 		musicLoop.setVolume(0f);
 		if (soundOn) {
 			musicLoop.fade(2000, 1f, false);
 		} else {
 			musicLoop.setVolume(gameVolume);
 			musicLoop.pause();
 		}
 	}
 }
