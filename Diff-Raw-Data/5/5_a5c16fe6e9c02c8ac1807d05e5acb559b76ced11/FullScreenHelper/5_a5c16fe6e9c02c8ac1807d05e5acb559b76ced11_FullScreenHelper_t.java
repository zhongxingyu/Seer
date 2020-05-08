 package net.petterroea.starterkit;
 
 import java.awt.DisplayMode;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 
 import javax.swing.JFrame;
 /**
  * Designed to help you make a fullscreen game. Only usable with windows, as i am lazy.
  * @author petterroea
  *
  */
 public class FullScreenHelper {
 	/**
 	 * The graphics device used for fullscreen
 	 */
 	private static GraphicsDevice dev;
 	/**
 	 * Sets fullscreen
 	 * @param frame The window
 	 * @param width Width of fullscreen resolution
 	 * @param height Height of fullscreen resolution
 	 */
 	public static void setFullscreen(JFrame frame, int width, int height)
 	{
		frame.setResizable(false);
		frame.setUndecorated(true);
 		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		dev = env.getDefaultScreenDevice();
 		dev.setFullScreenWindow(frame);
 		dev.setDisplayMode(new DisplayMode(width, height, 16, 75));
 	}
 	/**
 	 * Sets fullscreen
 	 * @param frame The window to be used
 	 * @param mode The display mode
 	 */
 	public static void setFullscreen(JFrame frame, DisplayMode mode)
 	{
		frame.setResizable(false);
		frame.setUndecorated(true);
 		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		dev = env.getDefaultScreenDevice();
 		dev.setFullScreenWindow(frame);
 		dev.setDisplayMode(mode);
 	}
 	/**
 	 * Rolls back fullscreen
 	 */
 	public static void rollbackFullscreen()
 	{
 		if(!(dev == null))
 		{
 			dev.setFullScreenWindow(null);
 		}
 	}
 }
