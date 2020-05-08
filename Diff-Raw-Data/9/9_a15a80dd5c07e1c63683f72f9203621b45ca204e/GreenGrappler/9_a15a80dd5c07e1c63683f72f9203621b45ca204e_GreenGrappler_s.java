 package se.darkbits.greengrappler;
 
 import static playn.core.PlayN.graphics;
 import static playn.core.PlayN.log;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import playn.core.Game;
 import playn.core.ImageLayer;
 import playn.core.ImmediateLayer;
 import playn.core.ImmediateLayer.Renderer;
 import playn.core.PlayN;
 import playn.core.Surface;
 import playn.core.gl.GLContext;
 import pythagoras.f.Point;
 import se.darkbits.greengrappler.Constants.Buttons;
 import se.darkbits.greengrappler.Input.AbstractHitTranslator;
 import se.darkbits.greengrappler.media.Font;
 import se.darkbits.greengrappler.media.Music;
 import se.darkbits.greengrappler.screens.SplashScreen;
 import se.darkbits.greengrappler.screens.TitleScreen;
 
 
 public class GreenGrappler implements Game, Renderer, AbstractHitTranslator {
 
 	float fps = 0.0f;
 
 	int frame = 0;
 
 	Font myFont = null;
 
 	boolean myFullScreen = false;
 
 	boolean myReadyForUpdates = false;
 
 	private static ImageLayer controlLayer = null;
 	private ImmediateLayer bufferLayer;
 
 	long startTimeMillis = System.currentTimeMillis();
 
 	public GreenGrappler(boolean aFullScreen) {
 		myFullScreen = aFullScreen;
 	}
 
 	@Override
 	public void init() {
 		log().debug("Green Grappler init");
 
 		GameState.loadFromFile();
 
 		// create and add background image layer
 		if (graphics().ctx() != null) {
 			graphics().ctx().setTextureFilter(GLContext.Filter.NEAREST,
 					GLContext.Filter.NEAREST);
 		}
 
 		Input.setTouchTranslator(this);
 
 		Resource.preLoad("data/images/boss.bmp");
 		Resource.preLoad("data/images/breakinghooktile.bmp");
 		Resource.preLoad("data/images/button.bmp");
 		Resource.preLoad("data/images/checkpoint.bmp");
 		Resource.preLoad("data/images/coin.bmp");
 		Resource.preLoad("data/images/completed_level.bmp");
 		Resource.preLoad("data/images/core.bmp");
 		Resource.preLoad("data/images/darkbitslogo_blink.bmp");
 		Resource.preLoad("data/images/darken.bmp");
 		Resource.preLoad("data/images/debris.bmp");
 		Resource.preLoad("data/images/dialogue.bmp");
 		Resource.preLoad("data/images/doctor_green_portrait.bmp");
 		Resource.preLoad("data/images/door.bmp");
 		Resource.preLoad("data/images/entities.bmp");
 		Resource.preLoad("data/images/fade.bmp");
 		Resource.preLoad("data/images/font.bmp");
 		Resource.preLoad("data/images/hand.bmp");
 		Resource.preLoad("data/images/hero_fall.bmp");
 		Resource.preLoad("data/images/hero_hurt.bmp");
 		Resource.preLoad("data/images/hero_jump.bmp");
 		Resource.preLoad("data/images/hero_run.bmp");
 		Resource.preLoad("data/images/hook.bmp");
 		Resource.preLoad("data/images/icons.bmp");
 		Resource.preLoad("data/images/lavafill.bmp");
 		Resource.preLoad("data/images/lavatop.bmp");
 		Resource.preLoad("data/images/level_exit_background.bmp");
 		Resource.preLoad("data/images/level_select.bmp");
 		Resource.preLoad("data/images/logo.bmp");
 		Resource.preLoad("data/images/movinghooktile.bmp");
 		Resource.preLoad("data/images/particles.bmp");
 		Resource.preLoad("data/images/reactor_shell.bmp");
 		Resource.preLoad("data/images/robot.bmp");
 		Resource.preLoad("data/images/rope.bmp");
 		Resource.preLoad("data/images/saw.bmp");
 		Resource.preLoad("data/images/selected_level_background.bmp");
 		Resource.preLoad("data/images/software.bmp");
 		Resource.preLoad("data/images/ted_portrait.bmp");
 		Resource.preLoad("data/images/tileset1.bmp");
 		Resource.preLoad("data/images/title.bmp");
 		Resource.preLoad("data/images/unselected_level_background.bmp");
 		Resource.preLoad("data/images/wall.bmp");
 		Resource.preLoad("data/images/z_coin.bmp");
 		Resource.preLoad("data/images/controls.png");
 
 		Resource.preLoadText("data/rooms/breaktilelevel.txt");
 		Resource.preLoadText("data/rooms/level1.txt");
 		Resource.preLoadText("data/rooms/levellava.txt");
 		Resource.preLoadText("data/rooms/olof.txt");
 		Resource.preLoadText("data/rooms/olof2.txt");
 		Resource.preLoadText("data/rooms/per1.txt");
 		Resource.preLoadText("data/rooms/per2.txt");
 		Resource.preLoadText("data/rooms/per3.txt");
 		Resource.preLoadText("data/rooms/per4.txt");
 		Resource.preLoadText("data/rooms/smalltestroom.txt");
 		Resource.preLoadText("data/rooms/test.txt");
 		Resource.preLoadText("data/rooms/tutorial.txt");
 		Resource.preLoadText("data/dialogues/1-tutorial1.txt");
 		Resource.preLoadText("data/dialogues/2-tutorial2.txt");
 		Resource.preLoadText("data/dialogues/boss_unlocked.txt");
 		Resource.preLoadText("data/dialogues/level_select.txt");
 
 		Resource.preLoadSound("data/sounds/alarm");
 		Resource.preLoadSound("data/sounds/beep");
 		Resource.preLoadSound("data/sounds/boot");
 		Resource.preLoadSound("data/sounds/boss_saw");
 		Resource.preLoadSound("data/sounds/coin");
 		Resource.preLoadSound("data/sounds/damage");
 		Resource.preLoadSound("data/sounds/green_peace");
 		Resource.preLoadSound("data/sounds/hook");
 		Resource.preLoadSound("data/sounds/hurt");
 		Resource.preLoadSound("data/sounds/jump");
		//TODO: buggy sound Resource.preLoadSound("data/sounds/land");
 		Resource.preLoadSound("data/sounds/no_hook");
 		Resource.preLoadSound("data/sounds/reactor_explosion");
 		Resource.preLoadSound("data/sounds/rope");
 		Resource.preLoadSound("data/sounds/select");
 		Resource.preLoadSound("data/sounds/start");
 		Resource.preLoadSound("data/sounds/time");
 		Resource.preLoadSound("data/sounds/timeout");
 
 		Input.init();
 	}
 
 	@Override
 	public void paint(float alpha) {
 
 		if (myFullScreen && 
 				(myScreenSizeX != PlayN.graphics().screenWidth() ||
 				myScreenSizeY != PlayN.graphics().screenHeight()))
 		{
 			myScreenSizeX = PlayN.graphics().screenWidth();
 			myScreenSizeY = PlayN.graphics().screenHeight();
 
 			graphics().setSize(
 					graphics().screenWidth(),
 					graphics().screenHeight());
 
 			screenSizeChanged();
 		}
 
 
 		if (myReadyForUpdates && myRebuildLayers) {
 			myRebuildLayers = false;
 
 			graphics().rootLayer().clear();
 
 			bufferLayer = graphics().createImmediateLayer(320, 240, this);
 
 			{
 				float w = PlayN.graphics().width();
 				float h = PlayN.graphics().height();
 
 				float aspect = w / h;
 				float targetAspect = 320 / 240;
 				float scale = 1.0f;
 
 				float translateX = 0;
 				float translateY = 0;
 
 				if (aspect < targetAspect) {
 					scale = w / 320;
 					translateY = h / 2 - 240 * scale / 2;
 				} else {
 					scale = h / 240;
 					translateX = w / 2 - 320 * scale / 2;
 				}
 
 				// bufferLayer.setTranslation(translateX, translateY);
 				bufferLayer.setScale(scale);
 				bufferLayer.setTranslation(translateX, translateY);
 			}
 
 			graphics().rootLayer().add(bufferLayer);
 
 			if (GlobalOptions.showTouchControls()) {
 				controlLayer = graphics().createImageLayer(
 						PlayN.assets().getImage("data/images/controls.png"));
 
 				{
 					float w = PlayN.graphics().width();
 					float h = PlayN.graphics().height();
 
 					float aspect = w / h;
 					float targetAspect = 960 / 720;
 					float scale = 1.0f;
 
 					float translateX = 0;
 					float translateY = 0;
 
 					if (aspect < targetAspect) {
 						scale = w / 960;
 						translateY = h / 2 - 720 * scale / 2;
 					} else {
 						scale = h / 720;
 						translateX = w / 2 - 960 * scale / 2;
 					}
 
 					controlLayer.setScale(scale);
 					controlLayer.setTranslation(translateX, translateY);
 					controlLayer.setAlpha(0.5f);
 				}
 
 				graphics().rootLayer().add(controlLayer);
 			}
 		}
 	}
 
 	void postPreloadInit() {
 		ScreenManager.add(new TitleScreen());
 		ScreenManager.add(new SplashScreen());
 		myFont = Resource.getFont("data/images/font.bmp");
 	}
 
 	void postPreloadUpdate() {
 		ScreenManager.onLogic();
 		Music.update();
 	}
 
 	int fpsCount = 0;
 	int lastFpsCount = 0;
 
 	Map<Integer, String> myFpsStringMap = new HashMap<Integer, String>();
 
 	final int myTouchFadeOutTime = Constants.TICKS_PER_SECOND * 3;
 	final int myTouchStayTime = Constants.TICKS_PER_SECOND * 2;
 	int myTouchedLast = myTouchFadeOutTime + myTouchStayTime;
 
 	private boolean myRebuildLayers = true;
 	private int myScreenSizeX = 0;
 	private int myScreenSizeY = 0;
 
 	@Override
 	public void render(Surface surface) {
 		if (!myReadyForUpdates)
 			return;
 		ScreenManager.draw(surface);
 		if (GlobalOptions.showFps())
 		{
 			long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
 			if (elapsedTimeMillis > 1000) {
 				startTimeMillis += 1000;
 				lastFpsCount = fpsCount;
 				fpsCount = 0;
 			} else {
 				fpsCount++;
 			}
 
 			if (!myFpsStringMap.containsKey(lastFpsCount)) {
 				myFpsStringMap.put(lastFpsCount, "fps: " + lastFpsCount);
 			}
 
 			myFont.draw(surface, myFpsStringMap.get(lastFpsCount), 10, 10);
 		}
 
 		myTouchedLast++;
 		if (Input.hasTouch())
 		{
 			myTouchedLast = 0;
 		}
 
 		if (myTouchedLast < myTouchFadeOutTime + myTouchStayTime)
 		{
 			if (myTouchedLast <= myTouchStayTime)
 			{
 				controlLayer.setAlpha(0.6f);
 			}
 			else
 			{
 				float alpha = 
 						(float)(myTouchFadeOutTime - (myTouchedLast - myTouchStayTime))/(float)myTouchFadeOutTime;
 				controlLayer.setAlpha(alpha * 0.6f);
 			}
 		}
 		else
 		{
 			controlLayer.setAlpha(0);
 		}
 	}
 
 	@Override
 	public void update(float delta) {
 		if (myReadyForUpdates) {
 			postPreloadUpdate();
 		} else if (Resource.isDonePreloading()) {
 			postPreloadInit();
 			myReadyForUpdates = true;
 		}
 
 		if (Input.isPressed(Buttons.FORCE_QUIT) || ScreenManager.isEmpty()) {
 			GlobalOptions.exit();
 		}
 
 		Input.update();
 	}
 
 	@Override
 	public int updateRate() {
 		return 1000 / Constants.TICKS_PER_SECOND;
 	}
 
 	@Override
 	public boolean translateHit(Point aHitpoint) {
 		if (controlLayer == null)
 			return false;
 
 		controlLayer.transform().inverseTransform(aHitpoint, aHitpoint);
 		return true;
 	}
 
 	public void screenSizeChanged() {
 		PlayN.log().debug("Screen size changed, will rebuild layers");
 		myRebuildLayers = true;
 	}
 }
