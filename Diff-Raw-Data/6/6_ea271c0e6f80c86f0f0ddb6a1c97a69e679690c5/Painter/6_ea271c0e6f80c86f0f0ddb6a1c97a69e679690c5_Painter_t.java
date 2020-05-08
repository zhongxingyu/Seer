 package main;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.ImageObserver;
 
 public class Painter {
 	private static Painter instance;
 	private Graphics g;
 	private final ImageObserver ob;
 	
 	private Painter(ImageObserver ob){
 		this.ob = ob;
 	}
 	/**
 	 * should only be called in main loop at init()
 	 * @param ob
 	 */
 	protected static void init(ImageObserver ob){
 		if(instance == null)
 			instance = new Painter(ob);
 	}
 	
 	/**
 	 * should be called every time a buffer is being initialized
 	 * @param g
 	 */
 	protected static void setG(Graphics g){
 		instance.g = g;
 	}
 	
 	/**
 	 * paints an arbitrary image
 	 * @param image
 	 * @param x
 	 * @param y
 	 */
 	public static void paint(Image image, int x, int y){
 		instance.g.drawImage(image, x, y, instance.ob);
 	}
 	/** 
 	 * paints an arbitrary quadrilateral
 	 * @param x
 	 * @param y
 	 * @param width
 	 * @param height
 	 * @param c
 	 */
 	public static void paint(int x, int y, int width, int height, Color c){
 		System.out.println(c.toString());
 		instance.g.setColor(c);
		instance.g.fillRect(x, y, width, height);
 	}
 
 }
