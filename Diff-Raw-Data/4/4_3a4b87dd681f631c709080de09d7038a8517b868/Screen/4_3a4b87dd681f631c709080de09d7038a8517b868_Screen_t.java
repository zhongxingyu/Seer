 package com.gamepatriot.androidframework.implementation;
 
 import java.util.ArrayList;
 
 import com.gamepatriot.androidframework.framework.AndroidAtlas;
 import com.gamepatriot.androidframework.framework.AndroidImage;
 import com.gamepatriot.androidframework.framework.AndroidMain;
 import com.gamepatriot.androidframework.framework.AndroidMusicHandler;
 import com.gamepatriot.androidframework.framework.AndroidScreen;
 import com.gamepatriot.androidframework.framework.AndroidShape;
 import com.gamepatriot.androidframework.framework.AndroidSoundHandler;
 import com.gamepatriot.androidframework.screens.Example;
 
 /**
  * The Screen class organizes display lists for images and shapes so they can be drawn through a {@link Renderer}. In addition, the screen class receives broadcasted inputs when parented 
  * by a {@link Main} class ({@link #touchDown(int, int)} and {@link #touchUp(int, int)}) and also has its time-step function that is invoked ({@link #update()}) every frame update.
  * 
  * @see AndroidScreen
  * @author Pete Schmitz, May 9, 2013
  *
  */
 public class Screen implements AndroidScreen {
 	
 	/**
 	 * The ScreenID class initiates a relationship between name flags for different types of {@link Screen}s and their constructors. The ScreenID can be used to conveniently instantiate
 	 * a Screen-type. For example, by using {@link Main}'s function {@link Main#addScreen(ScreenID)}.
 	 * 
 	 * @author Pete Schmitz, May 9, 2013
 	 *
 	 */
 	public static enum ScreenID{
 		EXAMPLE(){
 			@Override
 			public Screen createScreen(Main $main){
 				return new Example($main);
 			}
 		};
 		
 		/** Abstract function for returning an instantiated Screen **/
 		public abstract Screen createScreen(Main $main);
 		
 	}
 	
 	/** The {@link #activations} index flag for the renderer. **/
 	public static final int INDEX_RENDER = 0;
 	
 	/** The {@link #activations} index flag for the updater. **/
 	public static final int INDEX_UPDATER = 1;
 	
 	/** The {@link #activations} index flag for the inputter. **/
 	public static final int INDEX_INPUTTER = 2;
 	
 	/** The flag for a deactivated Screen feature in {@link #activations}. **/
 	public static final int FLAG_DEACTIVE = 0;
 	
 	/** The flag for an activated Screen feature in {@link #activations}. **/
 	public static final int FLAG_ACTIVE = 1;
 	
 	
 	//References
 	
 	/** Reference to this application's {@link Main} object. **/
 	protected Main main;
 	
 	/** Reference to this application's {@link Atlas} object. **/
 	protected Atlas atlas;
 	
 	/** Reference to this application's {@link SoundHandler} object. **/
 	protected SoundHandler sound;
 	
 	/** Reference to this application's {@link MusicHandler} object. **/
 	protected MusicHandler music;
 	
 	
 	//Flags
 	
 	/** (Read-only) Whether or not this screen should have its display rendered. Call {@link #activateRender()} and {@link #deactivateRender()} to toggle. **/
 	public boolean activeRender = false;
 	
 	/** (Read-only) Whether or not this screen should have its time-step function invoked. Call {@link #activateUpdater()} and {@link #deactivateUpdater()} to toggle. **/
 	public boolean activeUpdater = false;
 	
 	/** (Read-only) Whether or not this screen should receive broadcasted input. Call {@link #activateInputter()} and {@link #deactivateInputter()} to toggle. **/
 	public boolean activeInputter = false;
 	
 	/** Flags to memorize which features were enabled and disabled so features can be resumed from a pause call. **/
 	private int[] activations = {0, 0, 0};
 	
 	
 	//Containers
 	
 	/** Display list of images this Screen is parenting. **/
 	private ArrayList<AndroidImage> images;
 	
 	/** Display list of shapes this Screen is parenting. **/
 	private ArrayList<AndroidShape> shapes;
 	
 	
 	/**
 	 * Constructor
 	 * @param $main		Reference to the application's {@link Main} object.
 	 */
 	public Screen(Main $main){
 		
 		//References
 		main = $main;
 		atlas = (Atlas) $main.getAtlas();
 		sound = (SoundHandler) $main.getSound();
 		music = (MusicHandler) $main.getMusic();
 		
 		//Containers
 		images = new ArrayList<AndroidImage>();
 		shapes = new ArrayList<AndroidShape>();
 		
 		//Set default activation
 		activate();
 	}
 	
 	@Override
 	public void activate() {
 		activateRender();
 		activateUpdater();
 		activateInputter();
 	}
 	
 	@Override
 	public void activateInputter() {
 		activeInputter = true;
 		activations[INDEX_INPUTTER] = FLAG_ACTIVE;
 	}
 	
 	@Override
 	public void activateRender() {
 		activeRender = true;
 		activations[INDEX_RENDER] = FLAG_ACTIVE;
 	}
 	
 	@Override
 	public void activateUpdater() {
 		activeUpdater = true;
 		activations[INDEX_UPDATER] = FLAG_ACTIVE;
 	}
 
 	@Override
 	public void deactivate() {
 		deactivateRender();
 		deactivateUpdater();
 		deactivateInputter();
 	}
 
 	@Override
 	public void deactivateRender() {
 		activeRender = false;
 		activations[INDEX_RENDER] = FLAG_DEACTIVE;
 	}
 
 	@Override
 	public void deactivateUpdater() {
 		activeUpdater = false;
 		activations[INDEX_UPDATER] = FLAG_DEACTIVE;
 	}
 
 	@Override
 	public void deactivateInputter() {
 		activeInputter = false;
 		activations[INDEX_INPUTTER] = FLAG_DEACTIVE;
 	}
 
 	@Override
 	public void pause() {
 		activeRender = false;
 		activeUpdater = false;
 		activeInputter = false;
 	}
 
 	@Override
 	public void resume() {
 		if (activations[INDEX_RENDER] == FLAG_ACTIVE) activeRender = true;
 		if (activations[INDEX_UPDATER] == FLAG_ACTIVE) activeUpdater = true;
 		if (activations[INDEX_INPUTTER] == FLAG_ACTIVE) activeInputter = true;
 	}
 
 	@Override
 	public int numImages() {
 		return images.size();
 	}
 
 	@Override
 	public AndroidImage getImageAt(int $index) {
 		if ($index < 0 || $index >= images.size()) return null;
 		
 		return images.get($index);
 	}
 
 	@Override
 	public int getImageIndex(AndroidImage $image) {
 		return images.indexOf($image);
 	}
 
 	@Override
 	public void addImage(AndroidImage $image) {
 		$image.setParent(this);
 		images.add((Image) $image);
 	}
 
 	@Override
 	public void addImageAt(int $index, AndroidImage $image) {
 		$image.setParent(this);
 		
 		if ($index < 0){
 			images.add(0, (Image) $image);
 			return;
 		}
 		
 		if ($index >= images.size()){
 			images.add((Image) $image);
 			return;
 		}
 		
 		images.add($index, (Image) $image);
 	}
 
 	@Override
 	public boolean removeImage(AndroidImage $image) {
 		if (!$image.hasParent() || $image.getParent() != this) return false;
 		
 		$image.setParent(null);
 		return images.remove($image);
 	}
 
 	@Override
 	public AndroidImage removeImageAt(int $index) {
 		if ($index < 0 || $index >= images.size()) return null;
 		
 		images.get($index).setParent(null);
 		return images.remove($index);
 	}
 
 	@Override
 	public ArrayList<AndroidImage> getImages() {
 		return (ArrayList<AndroidImage>) images;
 	}
 
 	@Override
 	public void removeImages() {
 		int $i;
 		for ($i = images.size()-1; $i >= 0; $i--){
 			removeImageAt($i);
 		}
 	}
 
 	@Override
 	public int numShapes() {
 		return shapes.size();
 	}
 
 	@Override
 	public AndroidShape getShapeAt(int $index) {
 		if ($index < 0 || $index >= shapes.size()) return null;
 		
 		return shapes.get($index);
 	}
 
 	@Override
 	public int getShapeIndex(AndroidShape $shape) {
 		return shapes.indexOf($shape);
 	}
 
 	@Override
 	public void addShape(AndroidShape $shape) {
 		$shape.setParent(this);
 		shapes.add($shape);
 	}
 
 	@Override
 	public void addShapeAt(int $index, AndroidShape $shape) {
 		$shape.setParent(this);
 		if ($index < 0){
 			shapes.add(0, $shape);
 			return;
 		}
 		
 		if ($index >= images.size()){
 			shapes.add($shape);
 			return;
 		}
 		
 		shapes.add($index, $shape);
 	}
 
 	@Override
 	public boolean removeShape(AndroidShape $shape) {
 		if (!$shape.hasParent() || $shape.getParent() != this) return false;
 		
 		$shape.setParent(null);
 		return shapes.remove($shape);
 	}
 
 	@Override
 	public AndroidShape removeShapeAt(int $index) {
 		if ($index < 0 || $index >= shapes.size()) return null;
 		
 		shapes.get($index).setParent(null);
 		return shapes.remove($index);
 	}
 
 	@Override
 	public ArrayList<AndroidShape> getShapes() {
 		return shapes;
 	}
 
 	@Override
 	public void removeShapes() {
 		int $i;
 		for ($i = shapes.size()-1; $i >= 0; $i--){
 			removeShapeAt($i);
 		}
 	}
 
 	@Override
 	public AndroidMain getMain() {
 		return main;
 	}
 
 	@Override
 	public AndroidAtlas getAtlas() {
 		return atlas;
 	}
 	
 	@Override
 	public AndroidMusicHandler getMusic() {
 		return music;
 	}
 	
 	@Override
 	public AndroidSoundHandler getSound() {
 		return sound;
 	}
 
 	@Override
 	public void update() {
 	}
 
 	@Override
 	public void touchUp(int $x, int $y) {
 	}
 
 	@Override
 	public void touchDown(int $x, int $y) {
 	}
 	
	@Override
	public void touchMove(int $x, int $y) {
	}
 	
 	
 }
