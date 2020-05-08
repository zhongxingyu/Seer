 package com.lambda;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 
 import static org.lwjgl.opengl.GL11.*;
 
 /**
  * The 'GameContainer' holds a 'Game' and displays it.
  * 
  * @author alex
  * 
  */
 public class GameContainer {
 
 	/**
 	 * The 'Game' the 'GameContainer' holds and displays.
 	 */
 	protected Game game;
 
 	/**
 	 * The title of the 'GameContainer'.
 	 */
 	protected String title;
 
 	/**
 	 * The width of the 'GameContainer'.
 	 */
 	protected int width;
 
 	/**
 	 * The height of the 'GameContainer'.
 	 */
 	protected int height;
 
 	/**
 	 * The vertical sync option of the 'GameContainer'.
 	 */
 	protected boolean vSync = false;
 
 	/**
 	 * The fullscreen option of the 'GameContainer'.
 	 */
 	protected boolean fullScreen = false;
 
 	/**
 	 * The framerate the 'GameContainer' will try to render at.
 	 */
 	protected int frameRate = 60;
 
 	/**
 	 * Whether the closing of the 'GameContainer' has been requested.
 	 */
 	protected boolean closeRequested = false;
 
 	/**
 	 * The last time the frame counter was updated at.
 	 */
 	private long lastCounterTime = 0;
 
 	/**
 	 * Creates a new 'GameContainer' that will hold the 'Game' game. The 'Game'
 	 * may not be 'null', otherwise an LambdaException will be thrown.
 	 * 
 	 * @param game
 	 *            The 'Game' the 'GameContainer' will hold.
 	 */
 	public GameContainer(Game game, int width, int height)
 			throws LambdaException {
 		if (width >= 0) {
 			this.width = width;
 		} else {
 			this.width = 0;
 		}
 
 		if (height >= 0) {
 			this.height = height;
 		} else {
 			this.height = 0;
 		}
 
 		if (game != null) {
 			this.game = game;
 		} else {
 			throw new LambdaException("Game not initialised.");
 		}
 	}
 
 	/**
 	 * Creates a new 'Display' with the specified size and options.
 	 */
 	protected void create() {
 		changeMode(width, height, fullScreen);
 		Display.setVSyncEnabled(vSync);
 		Display.setTitle(title);
 		if (!Display.isCreated()) {
 			try {
 				Display.create();
 			} catch (LWJGLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Changes the 'DisplayMode' of the 'GameContainer'.
 	 * 
 	 * @param width
 	 *            The width of the new 'DisplayMode'.
 	 * @param height
 	 *            The height of the new 'DisplayMode'.
 	 * @param fullscreen
 	 *            Whether the 'GameContainer' is in fullscreen mode.
 	 */
 	protected void changeMode(int width, int height, boolean fullscreen) {
 		this.width = width;
 		this.height = height;
 		this.fullScreen = fullscreen;
 
 		if ((width != Display.getWidth()) || (height != Display.getHeight())
 				|| (fullscreen != Display.isFullscreen())) {
 			try {
 				DisplayMode target = null;
 
 				if (fullscreen == true) {
 					DisplayMode[] modes = Display.getAvailableDisplayModes();
 					int freq = 0;
 
 					boolean found = false;
 					for (int i = 0; i < modes.length && !found; i++) {
 						DisplayMode current = modes[i];
 
 						if ((current.getWidth() == width)
 								&& (current.getHeight() == height)) {
 							if ((target == null)
 									|| (current.getFrequency() >= freq)) {
 								if ((target == null)
 										|| (current.getBitsPerPixel() > target
 												.getBitsPerPixel())) {
 									target = current;
 									freq = target.getFrequency();
 								}
 							}
 
 							if ((current.getBitsPerPixel() == Display
 									.getDesktopDisplayMode().getBitsPerPixel())
 									&& (current.getFrequency() == Display
 											.getDesktopDisplayMode()
 											.getFrequency())) {
 								target = current;
 								found = true;
 							}
 						}
 					}
 				} else {
 					target = new DisplayMode(width, height);
 				}
 
 				if (target != null) {
 					Display.setDisplayMode(target);
 					Display.setFullscreen(fullscreen);
 				}
 			} catch (LWJGLException e) {
 			}
 		}
 	}
 
 	/**
 	 * Initializes the OpenGL environment for the 'GameContainer'.
 	 */
 	protected void initOpenGL() {
 		glMatrixMode(GL_PROJECTION);
 
 		glLoadIdentity();
 
 		glOrtho(0, Display.getWidth(), 0, Display.getHeight(), -1, 1);
 
 		glViewport(0, 0, Display.getWidth(), Display.getHeight());
 		glMatrixMode(GL_MODELVIEW);
 
 		glEnable(GL_TEXTURE_2D);
 		glDisable(GL_DEPTH_TEST);
 
 		glEnable(GL_BLEND);
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 
 		glLoadIdentity();
 	}
 
 	/**
 	 * Starts the 'GameContainer' by setting it up and starting the loop.
 	 */
 	public void start() {
 		create();
 		initOpenGL();
 		
 		loop();
 	}
 
 	/**
 	 * Calls the 'init-()'-method of the 'Game'.
 	 */
 	protected void init() {
 		game.init(this);
 	}
 
 	/**
 	 * The loop the 'GameContainer' uses to update/render everything.
 	 */
 	protected void loop() {
 		init();
 
 		long lastTime = System.nanoTime();
 		int fps = 0;
 
 		while (!Display.isCloseRequested() || closeRequested) {
 			long now = System.nanoTime();
 			long diff = now - lastTime;
 			lastTime = now;
 
 			double delta = diff / ((double) frameRate);
 
 			lastCounterTime += diff;
 			fps++;
 
 			if (lastCounterTime >= 1000000000) {
 				System.out.println("[FPS] " + fps);
 				lastCounterTime = 0;
 				fps = 0;
 			}
 
 			game.update(delta);
 
 			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 			game.render();
 
 			Display.update();
			Display.sync(frameRate);
 		}
 
 		Display.destroy();
 	}
 
 	/**
 	 * Gives back the title of the 'GameContainer'.
 	 * 
 	 * @return The title of the 'GameContainer'.
 	 */
 	public String getTitle() {
 		return title;
 	}
 
 	/**
 	 * Gives back the width of the 'GameContainer'.
 	 * 
 	 * @return The width of the 'GameContainer'.
 	 */
 	public int getWidth() {
 		return width;
 	}
 
 	/**
 	 * Gives back the height of the 'GameContainer'.
 	 * 
 	 * @return The height of the 'GameContainer'.
 	 */
 	public int getHeight() {
 		return height;
 	}
 
 	/**
 	 * Whether the vSync option for the 'GameContainer' is enabled.
 	 * 
 	 * @return 'true' if the option is enabled, 'false' otherwise.
 	 */
 	public boolean isvSync() {
 		return vSync;
 	}
 
 	/**
 	 * Whether the 'GameContainer' displays in fullscreen mode.
 	 * 
 	 * @return 'true' if it's in fullscreen, 'false' otherwise.
 	 */
 	public boolean isFullScreen() {
 		return fullScreen;
 	}
 
 	/**
 	 * Gives back the frame rate the 'GameContainer' loops at.
 	 * 
 	 * @return The frame rate of the 'GameContainer'.
 	 */
 	public int getFrameRate() {
 		return frameRate;
 	}
 
 	/**
 	 * Whether the closing of the 'GameContainer' has been requested.
 	 * 
 	 * @return 'true' if the close was requested, 'false' otherwise.
 	 */
 	public boolean isCloseRequested() {
 		return closeRequested;
 	}
 
 	/**
 	 * Sets the title of the 'GameContainer' to 'title'. This will only work if
 	 * 'title' is not 'null'.
 	 * 
 	 * @param title
 	 *            The title to set.
 	 */
 	public void setTitle(String title) {
 		if (title != null) {
 			this.title = title;
 		}
 	}
 
 	/**
 	 * Sets the width of the 'GameContainer' to 'width' and its height to
 	 * 'height'. The width and the height may not be less than 0.
 	 * 
 	 * @param width
 	 *            The width of the 'GameContainer'.
 	 * @param height
 	 *            The height of the 'GameContainer'.
 	 */
 	public void setSize(int width, int height) {
 		if (width >= 0) {
 			this.width = width;
 		} else {
 			width = 0;
 		}
 
 		if (height >= 0) {
 			this.height = height;
 		} else {
 			height = 0;
 		}
 	}
 
 	/**
 	 * Sets the vSync option of the 'GameContainer' to 'vSync'.
 	 * 
 	 * @param vSync
 	 *            'true' to enable the option, 'false' to disable.
 	 */
 	public void setvSync(boolean vSync) {
 		this.vSync = vSync;
 	}
 
 	/**
 	 * Sets the fullscreen option of the 'GameContainer' to fullScreen.
 	 * 
 	 * @param fullScreen
 	 *            'true' to enable the option, 'false' to disable.
 	 */
 	public void setFullScreen(boolean fullScreen) {
 		this.fullScreen = fullScreen;
 	}
 
 	/**
 	 * Sets the frame rate of the 'GameContainer' to 'frameRate'. The frame rate
 	 * may not be less than 1.
 	 * 
 	 * @param frameRate
 	 *            The frame rate to set.
 	 */
 	public void setFrameRate(int frameRate) {
 		if (frameRate > 0) {
 			this.frameRate = frameRate;
 		}
 	}
 
 	/**
 	 * Requests a close from the 'GameContainer'.
 	 */
 	public void requestClose() {
 		this.closeRequested = true;
 	}
 }
