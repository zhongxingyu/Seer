 package com.evanreidland.e.client;
 
 import java.applet.Applet;
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.awt.image.RenderedImage;
 import java.io.File;
 import java.net.URL;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.DoubleBuffer;
 import java.nio.FloatBuffer;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.PixelFormat;
 
 import com.evanreidland.e.Game;
 import com.evanreidland.e.Vector3;
 import com.evanreidland.e.engine;
 import com.evanreidland.e.client.control.EventKey;
 import com.evanreidland.e.client.control.input;
 import com.evanreidland.e.client.control.key;
 import com.evanreidland.e.client.graphics.GLGraphicsManager;
 import com.evanreidland.e.client.graphics.vbo;
 import com.evanreidland.e.graphics.Camera;
 import com.evanreidland.e.graphics.graphics;
 
 import java.util.Vector;
 
 import javax.imageio.ImageIO;
 
 public class EApplet extends Applet
 implements Runnable {
 	/**
 	 * System.setProperty("org.lwjgl.librarypath", path + "natives");
 
 		// Make sure jinput knows about the new path too
 		System.setProperty("net.java.games.input.librarypath", path + "natives");
 	 */
 	private static final long serialVersionUID = -5168678542636824795L;
 	
 	public static URL getGlobalCodeBase() {
 		return EApplet.active.getCodeBase();
 	}
 	
 	public static EApplet active = null;
 	public double cursorX, cursorY;
 	
 	Canvas screen;
 	Thread gameThread;
 	boolean running = false;
 	
 	protected double xmouse, ymouse, lxmouse, lymouse;
 	Vector<EventKey> events;
 	StringBuilder typed;
 	char[] keys;
 	
 	public boolean lockedMouse, yLocked, freeLook = false;
 	
 	protected long curTime;
 
 	private long frameDelay;
 	
 	public double scale;
 	
 	public float clearR = 0.4f, clearG = 0.4f, clearB = 1;
 	
 	public Camera cam;
 	
 	public void setClearColor(double r, double g, double b) {
 		clearR = (float)r;
 		clearG = (float)g;
 		clearB = (float)b;
 	}
 	
 	public void setFrameDelay(long newDelay) {
 		frameDelay = newDelay;
 	}
 	
 	public long getTime() {
 		return curTime;
 	}
 	
 	public Vector3 getMouseRatio() {
 		return new Vector3(xmouse/(double)getWidth(), ymouse/(double)getHeight(), 1);
 	}
 	
 	public String getTyped() {
 		return typed.toString();
 	}
 	
 	public double getXMouse() { return xmouse; }
 	public double getYMouse() { return ymouse; }
 	
 	public boolean isKeyDown(int key) {
 		if ( key < 0 || key > 255 )
 			return false;
 		return keys[key] == EventKey.KEY_DOWN;
 	}
 	public boolean isKeyUp(int key) {
 		if ( key < 0 || key > 255 )
 			return false;
 		return keys[key] == EventKey.KEY_UP;
 	}
 	public boolean getKeyState(int key) {
 		if ( key < 0 || key > 255 )
 			return false;
 		return keys[key] == EventKey.KEY_PRESSED || keys[key] == EventKey.KEY_DOWN;
 	}
 	
 	public void addEventKey(EventKey event) {
 		if ( event.key < 0 || event.key >= 255 ) {
 			return;
 		}
 		events.add(event);
 		if ( (event.state == EventKey.KEY_DOWN && keys[event.key] != EventKey.KEY_PRESSED ) || event.state != EventKey.KEY_DOWN ) {
 			keys[event.key] = (char)event.state;
 		}
 	}
 	
 	private void keyFrame() {
 		if ( !events.isEmpty() ) {
 			int i = 0;
 			while ( i < events.size() ) {
 				EventKey cur = events.elementAt(i);
 				if ( cur.state == EventKey.KEY_DOWN ) {
 					keys[cur.key] = EventKey.KEY_PRESSED;
 					events.remove(i);
 					i--;
 				} else if ( cur.state == EventKey.KEY_UP ) {
 					keys[cur.key] = EventKey.KEY_NONE;
 					events.remove(i);
 					i--;
 				}
 				i++;
 			}
 		}
 		input.wheel = 0;
 		typed = new StringBuilder();
 		events = new Vector<EventKey>();
 	}
 	
 	public void startLWJGL() {
 		input.app = this;
 		gameThread = new Thread() {
 			public void run() {
 				running = true;
 				try {
 					Display.setParent(screen);
 					Display.setVSyncEnabled(false);
 					Display.create(new PixelFormat(8, 4, 0, 0));
 					initGL();
 					onInit();
 					curTime = System.currentTimeMillis();
 				} catch (LWJGLException e) {
 					e.printStackTrace();
 				}
 				mainLoop();
 			}
 		};
 		gameThread.start();
 	}
 	private void stopLWJGL() {
 		running = false;
 		try {
 			gameThread.join();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public String nextScreenshotName(String startName, String ext) {
 		String nextName = startName + 1 + "." + ext;
 		
 		int times = 1;
 		
 		while(new File(startName + times + "." + ext).exists()) {
 			times++;
 			nextName = startName + times + "." + ext;
 		}
 		
 		return nextName;
 	}
 	
 	public void screenShot(String name) {
 		try {
 			System.out.println("Taking screenshot at @" + System.currentTimeMillis());
 			String nextName = nextScreenshotName(name, "png");
 			File file = new File("./" + nextName);
 			
 			BufferedImage renderedImage = new BufferedImage(screen.getWidth(), screen.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
 			
 			Graphics g = renderedImage.createGraphics();
 			
 			int size = screen.getWidth()*screen.getHeight()*12;
 			
 			ByteBuffer b = (ByteBuffer) ByteBuffer.allocateDirect(size);
 			b.order(ByteOrder.nativeOrder());
 			
 			GL11.glReadPixels(0, 0, screen.getWidth(), screen.getHeight(), GL11.GL_RGB, GL11.GL_FLOAT, b);
 			
			DoubleBuffer f = (DoubleBuffer) b.asDoubleBuffer();
 			
 			for ( int x = 0; x < screen.getWidth(); x++ ) {
 				for ( int y = 0; y < screen.getHeight(); y++ ) {
 					int ppoint = (y*screen.getWidth() + x)*3;
 					//System.out.println("RGB: " + f.get(ppoint + 0) + ", " + f.get(ppoint + 1) + ", " + f.get(ppoint + 2));
 					renderedImage.setRGB(x, screen.getHeight() - 1 - y,
 							new Color(
 									(float)f.get(ppoint + 0),
 									(float)f.get(ppoint + 1),
 									(float)f.get(ppoint + 2))
 							.getRGB());
 				}
 			}
 			
 			ImageIO.write((RenderedImage)renderedImage, "png", file);
 			
 			g.dispose();
 			
 			curTime = System.currentTimeMillis();
 			engine.updateTime();
 			
 			System.out.println("@" + System.currentTimeMillis() + ": Screenshot saved as '" + nextName + "'");
 		} catch ( Exception e ) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void pollKeys() {
 		Keyboard.poll();
 		Mouse.poll();
 		
 		char rkey = 0, ch = 0;
 		boolean state = true;
 		
 		while ( Keyboard.next() ) {
 			rkey = (char)Keyboard.getEventKey();
 			ch = (char)Keyboard.getEventCharacter();
 			state = Keyboard.getEventKeyState();
 			if ( rkey == Keyboard.KEY_ESCAPE ) {
 				rkey = key.KEY_ESCAPE;
 			} else if ( input.isValidTyped(Character.toLowerCase(ch)) ) {
 				typed.append(ch);
 			}
 			addEventKey(new EventKey((state ? EventKey.KEY_DOWN : EventKey.KEY_UP), rkey));
 		}
 		while ( Mouse.next() ) {
 			rkey = (char)Mouse.getEventButton();
 			state = Mouse.getEventButtonState();
 			if ( rkey >= 0 && rkey <= 2 ) {
 				addEventKey(new EventKey((state ? EventKey.KEY_DOWN : EventKey.KEY_UP ), rkey));
 			}
 		}
 		
 		input.wheel = Mouse.getDWheel()/120;
 	}
 	
 	public void destroy() {
 		if ( screen != null ) {
 			remove(screen);
 		}
 		super.destroy();
 		System.out.println("Fin.");
 	}
 	
 	protected void setGraphicSize(int newWidth, int newHeight) {
 		screen.setSize(newWidth, newHeight);
 	}
 	
 	public void init() {
 		GLGraphicsManager.setLWJGLPath();
 		EApplet.active = this;
 		frameDelay = 0;
 		lockedMouse = false;
 		yLocked = false;
 		curTime = 0;
 		xmouse = ymouse = lxmouse = lymouse = 0;
 		events = new Vector<EventKey>();
 		keys = new char[256];
 		typed = new StringBuilder();
 		input.app = this;
 		setLayout(new BorderLayout());
 		try {
 			screen = new Canvas() {
 				private static final long serialVersionUID = 7141569955043137552L;
 				public final void addNotify() {
 					super.addNotify();
 					startLWJGL();
 				}
 				public final void removeNotify() {
 					stopLWJGL();
 					super.removeNotify();
 				}
 			};
 			screen.setSize(getWidth(), getHeight());
 			
 			screen.setFocusable(true);
 			screen.requestFocus();
 			screen.setIgnoreRepaint(true);
 			
 			add(screen);
 			
 			//setResizable(true);
 			setVisible(true);
 		} catch (Exception e) {
 			System.err.println(e);
 			throw new RuntimeException("Unable to create display...!?");
 		}
 	}
 	public void mainLoop() {
 		curTime = System.currentTimeMillis();
 		Display.setTitle("Spellcraft " + engine.version);
 		while(running) {
 			if ( System.currentTimeMillis() > curTime + frameDelay ) {
 	    		curTime = System.currentTimeMillis();
 	    		
 	    		/*//Move?
 				if ( screen.getWidth() != getWidth() || screen.getHeight() != getHeight() ) {
 					setGraphicSize(getWidth(), getHeight());
 					onResize();
 				}*/
 	    		
 	    		pollKeys();
 	    		
 	    		if ( freeLook ) {
 	    			xmouse = Mouse.getX();
 	    			ymouse = Mouse.getY();
 	    		} else {
 	    			xmouse += Mouse.getX() - getWidth()/2;
 	    			ymouse += Mouse.getY() - getHeight()/2;
 	    			
 	    			if ( xmouse < 0 ) {
 	    				xmouse = 0;
 	    			}
 	    			if ( ymouse < 0 ) {
 	    				ymouse = 0;
 	    			}
 	    			if ( xmouse > getWidth() ) {
 	    				xmouse = getWidth();
 	    			}
 	    			if ( ymouse > getHeight() ) {
 	    				ymouse = getHeight();
 	    			}
 	    		}
 	    		
 	    		if ( input.isKeyDown(key.KEY_F2) ) {
 	    			try {
 		    			File f = new File("screenshots/");
 		    			if ( !f.exists() ) {
 		    				f.mkdir();
 		    			}
 	    			} catch ( Exception e ) {
 	    				e.printStackTrace();
 	    			}
 	    			screenShot("screenshots/screenshot");
 	    		}
 				
 				onUpdate();
 				drawLoop();
 				
 				keyFrame();
 				
 				Display.update();
 				
 				onPostRender();
 			} else {
 				if ( curTime + frameDelay - System.currentTimeMillis() > 3 ) {
 					try { 
 						Thread.sleep((curTime + frameDelay) - System.currentTimeMillis() - 3);
 					} catch ( Exception e ) {
 						
 					}
 				}
 			}
 		}
 		
 		Display.destroy();
 	}
 	
 	public void drawLoop() {
 		int rwidth = getWidth() - (getInsets().left + getInsets().right);
 		int rheight = getHeight() - (getInsets().top + getInsets().bottom);
 		if ( cam.width != rwidth || cam.height != rheight ) {
 			cam.width = rwidth;
 			cam.height = rheight;
 			GL11.glViewport(getInsets().left, getInsets().bottom, rwidth, rheight);
 			System.out.println("Resizing: " + cam.width + ", " + cam.height);
 		}
 		GL11.glClearColor(clearR, clearG, clearB, 1);
 		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 		GL11.glLoadIdentity();
 		
 		GL11.glDisable(GL11.GL_CULL_FACE);
 		
 		graphics.up = graphics.camera.getUp();
 		graphics.right = graphics.camera.getRight();
 		graphics.forward = graphics.camera.getForward();
 		
 		try {
 			
 			ByteBuffer fogCol = ByteBuffer.allocateDirect(32);
 			fogCol.order(ByteOrder.nativeOrder());
 			
 			
 			
 			GL11.glFog(GL11.GL_FOG_COLOR, (FloatBuffer)fogCol.asFloatBuffer().put(new float[] { clearR, clearG, clearB, 1}).flip());
 			GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP2);
 			GL11.glFogf(GL11.GL_FOG_START, 50);
 			GL11.glFogf(GL11.GL_FOG_END, 100);
 			GL11.glFogf(GL11.GL_FOG_DENSITY, 0.01f);
 			GL11.glFogi(GL11.GL_FOG_HINT, GL11.GL_NEAREST);
 		} catch ( Exception e ) {
 			e.printStackTrace();
 		}
 		
 		GL11.glPushMatrix();
 		onRender();
 		GL11.glPopMatrix();
 		boolean last = cam.is3D;
 		
 		
 		GL11.glPushMatrix();
 		GL11.glBlendFunc (GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 		GL11.glBlendFunc (GL11.GL_SRC_COLOR, GL11.GL_MULT);
 		cam.is3D = false;
 		graphics.setCamera(cam);
 		GL11.glDisable(GL11.GL_CULL_FACE);
 		onRenderHUD();
 		GL11.glPopMatrix();
 		
 		GL11.glFlush();
 		
 		cam.is3D = last;
 		
 		
 		repaint();
 	}
 
 	protected void initGL() {
 		try {
 			//GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, /*insert DoubleBuffer*/);
 			//GL11.glEnable(GL11.GL_CULL_FACE);
 			//GL11.glCullFace(GL11.GL_BACK);
 			/*GL11.glEnable(GL11.GL_LIGHTING);
 			try {
 				DoubleBuffer a = DoubleBuffer.allocate(4);
 				DoubleBuffer b = DoubleBuffer.allocate(4);
 				DoubleBuffer c = DoubleBuffer.allocate(4);
 				
 				DoubleBuffer v = DoubleBuffer.allocate(3);
 				
 				a.put(new double[] { 1.0f, 1.0f, 1.0f, 1.0f});
 				b.put(new double[] { 1.0f, 1.0f, 1.0f, 1.0f});
 				c.put(new double[] { 1.0f, 1.0f, 1.0f, 1.0f});
 				
 				v.put(new double[] {-0, 0, 0});
 				
 				GL11.glEnable(GL11.GL_LIGHT0);
 				GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, a);
 				GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, b);
 				GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, c);
 				GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, v);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}*/
 			
 			//GL11.glEnable(GL11.GL_LIGHT0);
 			
 			//GL11.glEnable(GL11.GL_LINE_SMOOTH);
 			//GL11.glHint (GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NEAREST);
 			
 			GL11.glEnable(GL11.GL_DEPTH_TEST);
 			GL11.glDepthFunc(GL11.GL_LEQUAL);
 			
 			GL11.glEnable(GL11.GL_TEXTURE_2D);
 			
 			GL11.glClearDepth(1.0f);
 			
 			GL11.glEnable(GL11.GL_CULL_FACE);
 			GL11.glCullFace(GL11.GL_BACK);
 			
 			GL11.glShadeModel(GL11.GL_NICEST);
 			GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
 			
 			//GL11.glEnable (GL11.GL_POLYGON_SMOOTH);
 			GL11.glDepthMask(true);
 			GL11.glBlendFunc (GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 			GL11.glBlendFunc (GL11.GL_SRC_COLOR, GL11.GL_MULT);
 			GL11.glEnable (GL11.GL_BLEND);
 			
 			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1f);
 			GL11.glEnable(GL11.GL_ALPHA_TEST);
 			
 			//GL11.glEnable(GL11.GL_COLOR_MATERIAL);
 			//GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE );
 			
 
 			//GL11.glEnable(GL11.GL_FOG);
 			
 			//GL11.glEnable(GL11.GL_LIGHTING);
 			
 			
 			//GL11.glBlendFunc(GL11.GL_SRC_ALPHA_SATURATE, GL11.GL_ONE_MINUS_SRC_ALPHA);
 			
 			//GL11.glEnable(GL11.GL_BLEND);
 			//GL11.glBlendFunc(GL11.GL_BLEND, GL11.GL_ONE_MINUS_SRC_ALPHA);
 			
 			/*GL11.glTexEnvf( GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND );
 			GL11.glEnable(GL11.GL_TEXTURE_2D);
 			GL11.glTexParameterf( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST );
 			GL11.glTexParameterf( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST );
 			GL11.glTexParameterf( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT );
 			GL11.glTexParameterf( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT );
 
 			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);*/
 			vbo.init();
 		} catch (Exception e) {
 			System.err.println(e);
 			running = false;
 		}
 	}
 	
 	protected void onInit() {
 		lockedMouse = false;
 		cam = new Camera();
 		cam.width = getWidth() - (getInsets().left + getInsets().right);
 		cam.height = getHeight() - (getInsets().top + getInsets().bottom);
 		cam.fov = 60;
 		cam.nearDist = 0.1f;
 		cam.farDist = 1000;
 		cam.is3D = true;
 		cam.perspective = cam.width/cam.height;
 		graphics.camera = cam;
 		
 		curTime = 0;
 		cam.angle.x = (double) (-Math.PI/6);
 		EApplet.active = this;
 		engine.Initialize();
 	}
 	
 	protected void applyCursor(double x, double y) {
 		if ( lockedMouse && freeLook ) {
 			cam.applyMouse(-x, -y, 0.005f);
 		}
 	}
 	
 	protected void onUpdate() {
 		if ( input.isKeyDown(key.MOUSE_LBUTTON)) {
 			lockedMouse = true;
 			Mouse.setGrabbed(true);
 		}
 		
 		if ( input.isKeyDown(key.KEY_ESCAPE)) {
 			lockedMouse = false;
 			Mouse.setGrabbed(false);
 		}
 		
 		if ( lockedMouse ) {
 			applyCursor(xmouse - lxmouse, ymouse - lymouse);
 			Mouse.setCursorPosition(getWidth()/2, getHeight()/2);
 			if ( freeLook ) {
 				xmouse = getWidth()/2;
 				ymouse = getHeight()/2;
 			}
 			lxmouse = xmouse;
 			lymouse = ymouse;
 			
 			Game.mousePos.x = xmouse - getWidth()/2;
 			Game.mousePos.y = ymouse - getHeight()/2;
 			
 			//System.out.println("Mouse: " + Game.mousePos.x + ", " + Game.mousePos.y);
 		}
 		
 		engine.Update();
 	}
 	
 	public void update(Graphics g) {
 		paint(g);
 	}
 	
 	public void paint(Graphics g2) {	
 		//screen.paint(g2);
 		//g.drawImage(screen, 0, 0, getWidth(), getHeight(), null);
 	}
 	
 	protected void onRender() {
 		engine.Render();
 	}
 	
 	protected void onRenderHUD() {
 		engine.RenderHUD();
 	}
 	
 	public void onPostRender() {
 	}
 	
 	protected void onResize() {
 	}
 	
 	public void resize(int width, int height) {
 		super.resize(width, height);
 	}
 
 	public void run() {
 		System.out.println("EApplet.run(): This shouldn't be called.");
 	}
 }
