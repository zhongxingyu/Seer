 package riskyspace.view;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 
 public class Sprite {
 	private double rotation = 0;
 	private float dx;
 	private float dy;
 	private float scale;
 	private Image img = null;
 	private Image resizedImage = null;
 	
 	/**
 	 * Creates a new Sprite object
 	 * @param image The texture to be drawn.
 	 * @param dx The offset from 0 to 1 of the left side 
 	 * inside the position
 	 * @param dy The offset from 0 to 1 of the top side 
 	 * inside the position
 	 * @param scale How much space of one square this image should take
 	 */
 	public Sprite(Image image, float dx, float dy, float scale) {
 		img = image;
		resizedImage = img;
 		this.dx = dx;
 		this.dy = dy;
 		this.scale = scale;
 	}
 	
 	/**
 	 * Set the rotation of this Sprite in radians
 	 * @param rotation Set rotation to this angle.
 	 */
 	public void setRotation(double rotation) {
 			this.rotation = rotation;
 	}
 	
 	/**
 	 * Rotates the Sprite
 	 * @param rotation Rotate this amount.
 	 */
 	public void rotate(double rotation) {
 			this.rotation += rotation;
 	}
 	
 	public void draw(Graphics g, int x, int y, int squareSize) {
 		if (resizedImage == null || (resizedImage.getHeight(null) != squareSize*scale || resizedImage.getWidth(null) != squareSize*scale) ) {
 			resizedImage = img.getScaledInstance((int) (squareSize*scale), (int) (squareSize*scale), Image.SCALE_FAST);
 		}
 		Graphics2D g2D = (Graphics2D) g;
 		g2D.rotate(rotation, x + dx + resizedImage.getWidth(null)/2, y + dy + resizedImage.getHeight(null)/2);
 		g2D.drawImage(resizedImage, (int) (x + dx*squareSize), (int) (y + dy*squareSize), null);
 		g2D.rotate(-rotation, x + dx + resizedImage.getWidth(null)/2, y + dy + resizedImage.getHeight(null)/2);
 	}
 }
