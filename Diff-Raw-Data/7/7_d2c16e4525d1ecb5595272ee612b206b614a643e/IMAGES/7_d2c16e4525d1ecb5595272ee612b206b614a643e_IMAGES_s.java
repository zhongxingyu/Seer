 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 
 public class IMAGES {
 	
 	public static final Image BACKGROUND = makeImage(GLOBAL.BACKGROUND, false);
 	public static final Image CLOUDS = makeImage(GLOBAL.CLOUDS, false);
 	public static final Image CRAP = makeImage(GLOBAL.CRAP, true);
	public static final Image EGG = makeImage(GLOBAL.EGG, true);
 	public static final Image SUN = makeImage(GLOBAL.SUN, true);
 	
 
 	public static final Image MENU_BACKGROUND = makeImage(GLOBAL.MENU_BACKGROUND, false);
 	public static final Image MENU_PLAY_BUTTON = makeImage(GLOBAL.MENU_PLAY_BUTTON, false);
 	public static final Image MENU_ABOUT_BUTTON = makeImage(GLOBAL.MENU_ABOUT_BUTTON, false);
 	public static final Image MENU_EXIT_BUTTON = makeImage(GLOBAL.MENU_EXIT_BUTTON, false);
 	public static final Image MENU_HIGHSCORE_BUTTON = makeImage(GLOBAL.MENU_HIGHSCORE_BUTTON, false);
 
 	public static final Image BIRD_WHITE_1 = makeImage(GLOBAL.BIRD_WHITE_1, true);
 	public static final Image BIRD_WHITE_2 = makeImage(GLOBAL.BIRD_WHITE_2, true);
 	public static final Image BIRD_WHITE_3 = makeImage(GLOBAL.BIRD_WHITE_3, true);
 	public static final Image BIRD_WHITE_4 = makeImage(GLOBAL.BIRD_WHITE_4, true);
 	public static final Image BIRD_WHITE_5 = makeImage(GLOBAL.BIRD_WHITE_5, true);
 	
 	public static final Image BIRD_BROWN_1 = makeImage(GLOBAL.BIRD_BROWN_1, true);
 	public static final Image BIRD_BROWN_2 = makeImage(GLOBAL.BIRD_BROWN_2, true);
 	public static final Image BIRD_BROWN_3 = makeImage(GLOBAL.BIRD_BROWN_3, true);
 	public static final Image BIRD_BROWN_4 = makeImage(GLOBAL.BIRD_BROWN_4, true);
 	public static final Image BIRD_BROWN_5 = makeImage(GLOBAL.BIRD_BROWN_5, true);
 	
 	public static final Image DUDE_WALK_1 = makeImage(GLOBAL.DUDE_WALK_1, true, GLOBAL.DUDE_SCALE);
 	public static final Image DUDE_WALK_2 = makeImage(GLOBAL.DUDE_WALK_2, true, GLOBAL.DUDE_SCALE);
 	public static final Image DUDE_WALK_3 = makeImage(GLOBAL.DUDE_WALK_3, true, GLOBAL.DUDE_SCALE);
 	public static final Image DUDE_WALK_4 = makeImage(GLOBAL.DUDE_WALK_4, true, GLOBAL.DUDE_SCALE);
 	public static final Image DUDE_WALK_5 = makeImage(GLOBAL.DUDE_WALK_5, true, GLOBAL.DUDE_SCALE);
 	public static final Image DUDE_WALK_6 = makeImage(GLOBAL.DUDE_WALK_6, true, GLOBAL.DUDE_SCALE);
 	public static final Image DUDE_WALK_7 = makeImage(GLOBAL.DUDE_WALK_7, true, GLOBAL.DUDE_SCALE);
 	public static final Image DUDE_WALK_8 = makeImage(GLOBAL.DUDE_WALK_8, true, GLOBAL.DUDE_SCALE);
 	public static final Image DUDE_WALK_9 = makeImage(GLOBAL.DUDE_WALK_9, true, GLOBAL.DUDE_SCALE);
 	public static final Image DUDE_DEAD = makeImage(GLOBAL.DUDE_DEAD, true, GLOBAL.DUDE_SCALE);
 	
 	public static final Image CAR_RED =  makeImage(GLOBAL.CAR_RED, true);
 	public static final Image CAR_RED_WRECK =  makeImage(GLOBAL.CAR_RED_WRECK, true);
 	public static final Image CAR_BLUE =  makeImage(GLOBAL.CAR_BLUE, true);
 	public static final Image CAR_BLUE_WRECK =  makeImage(GLOBAL.CAR_BLUE_WRECK, true);
 	public static final Image CAR_GREEN =  makeImage(GLOBAL.CAR_GREEN, true);
 	public static final Image CAR_GREEN_WRECK =  makeImage(GLOBAL.CAR_GREEN_WRECK, true);
 	public static final Image CAR_PURPLE =  makeImage(GLOBAL.CAR_PURPLE, true);
 	public static final Image CAR_PURPLE_WRECK =  makeImage(GLOBAL.CAR_PURPLE_WRECK, true);
 	
 	public static final Image TREE_1 =  makeImage(GLOBAL.TREE_1, true, GLOBAL.TREE_SCALE);
 	public static final Image TREE_2 =  makeImage(GLOBAL.TREE_2, true, GLOBAL.TREE_SCALE);
 	public static final Image POLE_1 =  makeImage(GLOBAL.POLE_1, true, GLOBAL.POLE_SCALE);
 	
 	public static final Image EGG_1 = makeImage(GLOBAL.EGG_1, true, GLOBAL.EGG_SCALE);
 	public static final Image EGG_2 = makeImage(GLOBAL.EGG_2, true, GLOBAL.EGG_SCALE);
 	public static final Image EGG_3 = makeImage(GLOBAL.EGG_3, true, GLOBAL.EGG_SCALE);
 	public static final Image EGG_4 = makeImage(GLOBAL.EGG_4, true, GLOBAL.EGG_SCALE);
 	public static final Image EGG_5 = makeImage(GLOBAL.EGG_5, true, GLOBAL.EGG_SCALE);
 	
 	///////////////////////////////////////////////////////////////////////////////
 	
 	/**
 	 * Get an array of unique Images for the evil bird
 	 * @return
 	 */
 	public static Image[] getNewEnemy(){
 		Image[] enemy = {
 				makeImage(GLOBAL.BIRD_BROWN_1, true),
 				makeImage(GLOBAL.BIRD_BROWN_2, true),
 				makeImage(GLOBAL.BIRD_BROWN_3, true),
 				makeImage(GLOBAL.BIRD_BROWN_4, true),
 				makeImage(GLOBAL.BIRD_BROWN_5, true)
 		};
 		return enemy;
 	}
 	/**
 	 * Get an array of unique Images for the birdy
 	 * @return
 	 */
 	public static Image[] getNewBird(){
 		Image[] bird = {
 				makeImage(GLOBAL.BIRD_WHITE_1, true),
 				makeImage(GLOBAL.BIRD_WHITE_2, true),
 				makeImage(GLOBAL.BIRD_WHITE_3, true),
 				makeImage(GLOBAL.BIRD_WHITE_4, true),
 				makeImage(GLOBAL.BIRD_WHITE_5, true)
 		};
 		return bird;
 	}
 	/**
 	 * Get an array of unique Images for the walking guy
 	 * @return
 	 */
 	public static Image[] getNewDude(){
 		Image[] dude = {
 				makeImage(GLOBAL.DUDE_WALK_9, true, GLOBAL.DUDE_SCALE),
 				makeImage(GLOBAL.DUDE_WALK_8, true, GLOBAL.DUDE_SCALE),
 				makeImage(GLOBAL.DUDE_WALK_7, true, GLOBAL.DUDE_SCALE),
 				makeImage(GLOBAL.DUDE_WALK_6, true, GLOBAL.DUDE_SCALE),
 				makeImage(GLOBAL.DUDE_WALK_5, true, GLOBAL.DUDE_SCALE),
 				makeImage(GLOBAL.DUDE_WALK_4, true, GLOBAL.DUDE_SCALE),
 				makeImage(GLOBAL.DUDE_WALK_3, true, GLOBAL.DUDE_SCALE),
 				makeImage(GLOBAL.DUDE_WALK_2, true, GLOBAL.DUDE_SCALE),
 				makeImage(GLOBAL.DUDE_WALK_1, true, GLOBAL.DUDE_SCALE)
 		};
 		return dude;
 	}
 	/**
 	 * Get an Image array of Egg images
 	 * @return
 	 */
 	public static Image[] getNewEggs(){
 		Image[] eggs = {
 				EGG_1, EGG_2, EGG_3, EGG_4, EGG_5};
 		return eggs;
 	}
 	
 	/**
 	 * Method to create an Image object and return it
 	 * @param fileLocation, hasTransparency
 	 * @return Image
 	 */
 	public static Image makeImage(String fileLocation, boolean hasTransparency){
 		Image img = null;
 		try {
 			if (hasTransparency){
 				img = new Image(fileLocation, GLOBAL.chromakey);
 			} else {
 				img = new Image(fileLocation);
 			}
 		} catch (SlickException e) {
 			System.out.println("Unable to create Image: " + fileLocation);
 		}
 		return img;
 	}
 	/**
 	 * Method to create an Image object and return it
 	 * @param fileLocation, hasTransparency
 	 * @return Image
 	 */
 	public static Image makeImage(String fileLocation, boolean hasTransparency, float scale){
 		Image img = null;
 		try {
 			if (hasTransparency){
 				img = new Image(fileLocation, GLOBAL.chromakey).getScaledCopy(scale);
 			} else {
 				img = new Image(fileLocation).getScaledCopy(scale);
 			}
 		} catch (SlickException e) {
 			System.out.println("Unable to create Image: " + fileLocation);
 		}
 		return img;
 	}
 }
 
