 package edu.rit.se.sse.rapdevx.gui;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 public class Background {
 
 	private static int GRID_SIZE = 64;
 	public static final Color GRID_COLOR_1 = new Color(60, 60, 60);
 	public static final Color GRID_COLOR_2 = new Color(40, 40, 40);
 	
 	//public static final Color GRID_COLOR_2 = new Color(255, 0, 0);
 	//public static final Color GRID_COLOR_1 = new Color(0, 255, 0);
 
 	private int x, y, width, height;
 	private BufferedImage background;
 
 	public Background(int x, int y, int width, int height) {
 		this.x = x;
 		this.y = y;
 		this.width = width;
 		this.height = height;
 
 		try {
 			background = ImageIO.read(new File("assets/background.png"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void update() {
 	}
 
 	public void draw(Graphics2D gPen, Rectangle2D bounds) {
 		drawImage(gPen, bounds);
 		drawGrid(gPen, bounds);
 	}
 
 	public void drawImage(Graphics2D gPen, Rectangle2D bounds) {
 		
 		int cameraX = (int)bounds.getX();
 		int cameraY = (int)bounds.getY();
 		
 		int cameraWidth = (int)bounds.getWidth();
 		int cameraHeight = (int)bounds.getHeight();
 		
 		int backgroundWidth = background.getWidth();
 		int backgroundHeight = background.getHeight();
 		
 		// backgrounds can only be drawn at integer multiples
 		// of the with and height.
 		int drawAtX = (int)( cameraX / backgroundWidth ) * backgroundWidth;
 		int drawAtY = (int)( cameraY / backgroundHeight ) * backgroundHeight;
 		
 		// if the camera is in the negative coordinate space
 		// on the map, adjust the draw positions.
 		if ( cameraX < 0 ) {
			drawAtX = -drawAtX - backgroundWidth;
 		}
 		if ( cameraY < 0 ) {
			drawAtY = -drawAtY - backgroundHeight;
 		}
 		
 		int startDrawAtY = drawAtY;
 		
 		int xPosThatCantBe = cameraX + cameraWidth;
 		int yPosThatCantBe = cameraY + cameraHeight;
 		
 		// draw backgrounds until they would be
 		// drawn totally off screen.
 		while ( drawAtX < xPosThatCantBe ) {
 			while ( drawAtY < yPosThatCantBe ) {
 				gPen.drawImage(background, drawAtX, drawAtY,
 						backgroundWidth,
 						backgroundHeight, null);
 				drawAtY += backgroundHeight;
 			}
 			drawAtX += backgroundWidth;
 			drawAtY = startDrawAtY;
 		}
 	}
 
 	/**
 	 * Draws the grid
 	 * 
 	 * @param gPen
 	 *              the graphics2D Pen
 	 */
 	public void drawGrid(Graphics2D gPen, Rectangle2D bounds) {
 		int x = (int)bounds.getX();
 		int y = (int)bounds.getY();
 		int width = (int)bounds.getWidth();
 		int height = (int)bounds.getHeight();
 
 		for (int x1 = x; x1 < x + width; x1++) {
 			if (x1 % GRID_SIZE == 0) {
 				gPen.setColor(getColor(x1));
 				gPen.fill(new Rectangle(x1, y, 4, height));
 			}
 		}
 
 		for (int y1 = y; y1 < y + height; y1++) {
 			if (y1 % GRID_SIZE == 0) {
 				gPen.setColor(getColor(y1));
 				gPen.fill(new Rectangle(x, y1, width, 4));
 			}
 		}
 	}
 
 	/**
 	 * Swaps the color from gray to other gray
 	 * 
 	 * @param gPen
 	 *              the graphics2D Pen
 	 */
 	public Color getColor(int coordinate) {
 		if ((coordinate / GRID_SIZE) % 3 == 0) {
 			return GRID_COLOR_1;
 		} else {
 			return GRID_COLOR_2;
 		}
 	}
 }
