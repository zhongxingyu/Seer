 import org.newdawn.slick.Color;
 
 /*
  * Reddit BaconGameJam - Aug 2012
  * Team maeT
  * 04/08/2012
  * 
  * Class to hold static global variables
  */
 
 public class GLOBAL {
 	// screen size
 	public static final int SCREEN_WIDTH = 800;
 	public static final int SCREEN_HEIGHT = 600;
 
 	// timing, when to increase the game's difficulty and score multiplier (ms)
 	public static int DIFFICULTY_INCREASE_PERIOD = 80000; // 80 secs??
 	
 	// overall speed of the game.
 	public static float SCROLL_SPEED = 0.1f;
 	public static float CLOUD_SPEED = SCROLL_SPEED / 2;
 	
 	// Entity generation timing
 	public static final long ENTITY_DELAY = 2000;
 	public static final long ENTITY_INCREMENT = 200;
 
 	// Each frequency should be bigger than the previous and sum should equal 0
 	//		The difference between is the weighting it gets
 	public static final int HUMAN_FREQUENCY = 25;		// 25
 	public static final int CAT_FREQUENCY = 45;			// 20
 	public static final int BIRD_FREQUENCY = 70;		// 25
 	public static final int TREE_FREQUENCY = 100;		// 25
 	public static final int CAR_FREQUENCY = 90;			// 20
 	public static final int DO_NOTHING = 120;			// 30
 	public static final int RANDOM_MAX = 120;			// max for random num gen			
 	
 	// images:
 	public static final Color chromakey = new Color(255, 0, 255);
 	
 	public static final String BACKGROUND = "resources/background.png";
 	public static final String CLOUDS = "resources/clouds.png";
 	public static final String CRAP = "resources/poop.png";
	public static final String EGG = "resources/egg.png";
 	public static final String SUN = "resources/Smiley Sun.png";
 	public static final float SUN_SCALE = .5f;
 	
 	public static final String MENU_BACKGROUND = "resources/Menu/MenuBackground.png";
 	public static final String MENU_PLAY_BUTTON = "resources/Menu/Play.png";
 	public static final String MENU_ABOUT_BUTTON = "resources/Menu/About.png";
 	public static final String MENU_EXIT_BUTTON = "resources/Menu/Exit.png";
 	public static final String MENU_HIGHSCORE_BUTTON = "resources/Menu/Highscore.png";
 	public static final int MAIN_MENU = 0;
 	public static final int ABOUT_MENU = 1;
 	public static final int HIGHSCORE_MENU = 2;
 	public static final int GAMEOVER_MENU = 3;
 	
 	
 	public static final String BIRD_WHITE_1 = "resources/bird/white/01.png";
 	public static final String BIRD_WHITE_2 = "resources/bird/white/02.png";
 	public static final String BIRD_WHITE_3 = "resources/bird/white/03.png";
 	public static final String BIRD_WHITE_4 = "resources/bird/white/04.png";
 	public static final String BIRD_WHITE_5 = "resources/bird/white/05.png";
 	
 	public static final String BIRD_BROWN_1 = "resources/bird/brown/01.png";
 	public static final String BIRD_BROWN_2 = "resources/bird/brown/02.png";
 	public static final String BIRD_BROWN_3 = "resources/bird/brown/03.png";
 	public static final String BIRD_BROWN_4 = "resources/bird/brown/04.png";
 	public static final String BIRD_BROWN_5 = "resources/bird/brown/05.png";
 	
 	public static final float DUDE_SCALE = 1.7f;
 	public static final String DUDE_WALK_1 = "resources/dude/walk/01.png";
 	public static final String DUDE_WALK_2 = "resources/dude/walk/02.png";
 	public static final String DUDE_WALK_3 = "resources/dude/walk/03.png";
 	public static final String DUDE_WALK_4 = "resources/dude/walk/04.png";
 	public static final String DUDE_WALK_5 = "resources/dude/walk/05.png";
 	public static final String DUDE_WALK_6 = "resources/dude/walk/06.png";
 	public static final String DUDE_WALK_7 = "resources/dude/walk/07.png";
 	public static final String DUDE_WALK_8 = "resources/dude/walk/08.png";
 	public static final String DUDE_WALK_9 = "resources/dude/walk/09.png";
 	public static final String DUDE_DEAD = "resources/dude/walk/human-death.png";
 	
 	public static final float EGG_SCALE = 0.5f;
 	public static final String EGG_1 = "resources/eggs/egg_1.png";
 	public static final String EGG_2 = "resources/eggs/egg_2.png";
 	public static final String EGG_3 = "resources/eggs/egg_3.png";
 	public static final String EGG_4 = "resources/eggs/egg_4.png";
 	public static final String EGG_5 = "resources/eggs/egg_5.png";
 	
 	public static final float CAR_SCALE = 1.7f;
 	public static final String CAR_RED = "resources/cars/red.png";
 	public static final String CAR_RED_WRECK = "resources/cars/red-wrecked.png";
 	public static final String CAR_BLUE = "resources/cars/blue.png";
 	public static final String CAR_BLUE_WRECK = "resources/cars/blue-wrecked.png";
 	public static final String CAR_GREEN = "resources/cars/green.png";
 	public static final String CAR_GREEN_WRECK = "resources/cars/green-wrecked.png";
 	public static final String CAR_PURPLE = "resources/cars/purple.png";
 	public static final String CAR_PURPLE_WRECK = "resources/cars/purple-wrecked.png";
 	
 	public static final float TREE_SCALE = .9f;
 	public static final int TREE_Y_VALUE = 463 - (int)(276 * TREE_SCALE);  // top of sidewalk - (img height * scale)
 	public static final String TREE_1 = "resources/static/tree1.png";
 	public static final String TREE_2 = "resources/static/tree2.png";
 	public static final float POLE_SCALE = .9f;
 	public static final String POLE_1 = "resources/static/pole.png";
 	
 	// scores:
 	public static final double BASE_SCORE = 0.2;
 	public static double MULTIPLIER_MULTIPLIER = 0.02;
 }
