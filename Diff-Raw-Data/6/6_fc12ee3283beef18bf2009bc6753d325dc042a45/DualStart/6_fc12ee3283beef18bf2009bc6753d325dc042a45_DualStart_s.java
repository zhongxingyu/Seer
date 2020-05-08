 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package es.ld23;
 
 import java.applet.Applet;
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.openal.AL;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 
 /**
  * This class will become the dual entry of the game. For the applet and for the application.
  * start() will be the applet start point
  * main() will be the program start point
  * both will iniciallice LWJGL, applet will use a canvas and application will let lwjgl to create.
  * @author Jorge Barcelo
  * @email shephiroth@hotmail.com
  */
 public class DualStart extends Applet {
 
	private static  Canvas father = null;
 	private static Thread hilo = null;
 	private static Game game = null;
 	private static boolean running = false;
 	private static int w = 800;
 	private static int h = 600;
 
 	/**
 	 * Method to create new thread and set lwjgl environment for applet.
 	 * Only called for applet entry point.
 	 */
 	private static void startLWJGL() {
 		hilo = new Thread() {
 
 			@Override
 			public void run() {
 				running = true;
 				try {
 					if (father != null) {
 						Display.setParent(father);
 					}
 					Display.create();
 					game.initGL();
 					game.loadResources();
 				} catch (LWJGLException e) {
 					Game.debug("DualStart::startLWJGL  ->  " + e.getMessage());
 					return;
 				}
 				gameLoop();
 			}
 		};
 		hilo.start();
 	}
 
 	/**
 	 * Method that will stop thread and will stop gameLoop.
 	 */
 	private static void stopLWJGL() {
 		running = false;
 		try {
 			hilo.join();
 		} catch (InterruptedException e) {
 			Game.debug("DualStart::stopLWJGL  ->  " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Method that control the flow of time. Is called from applet and program rutines. 
 	 * Applet handle close by Cavas.Notify, but application need to check closeRequested.
 	 */
 	private static void gameLoop() {
 		while (running) {
 			game.tick();
 			game.render();
 			Display.sync(60);
 			Display.update();
 			if (Display.isCloseRequested()) {
 				running = false;
 			}
 		}
 		Display.destroy();
 		AL.destroy();
 	}
 
 	/**
 	 * Initialization method that will be called after the applet is loaded
 	 * into the browser.
 	 */
 	@Override
 	public void start() {
 		w = this.getWidth();
 		h = this.getHeight();
 
 		setLayout(new BorderLayout());
 		try {
 			father = new Canvas() {
 
 				public final void addNotify() {
 					super.addNotify();
 					startLWJGL();
 				}
 
 				public final void removeNotify() {
 					stopLWJGL();
 					super.removeNotify();
 				}
 			};
 			father.setSize(w, h);
			game = new Game(w, h);
 			add(father);
 			father.setFocusable(true);
 			father.requestFocus();
 			father.setIgnoreRepaint(true);
 			setVisible(true);
 		} catch (Exception e) {
 			Game.debug("DualStart::start  ->  " + e.getMessage());
 			throw new RuntimeException("Unable to create display");
 		}
 	}
 
 	/**
 	 * @param args the command line arguments
 	 */
 	public static void main(String[] args) {
 		running = true;
 		try {
 			Display.setDisplayMode(new DisplayMode(800, 600));
 			Display.setTitle("LD23 Tiny World");
 			Display.create();
 			game = new Game(800, 600);
 			game.initGL();
 			game.loadResources();
 		} catch (LWJGLException e) {
 			Game.debug("DualStart::main  ->  " + e.getMessage());
 		}
 		gameLoop();
 	}
 }
