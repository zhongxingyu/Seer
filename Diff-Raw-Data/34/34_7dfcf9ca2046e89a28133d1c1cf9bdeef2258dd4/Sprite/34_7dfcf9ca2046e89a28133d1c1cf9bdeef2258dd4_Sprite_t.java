 package toritools.entity.sprite;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 
 import toritools.entity.Entity;
 import toritools.math.Vector2;
 
 public class Sprite {
 	private int xSplit = 1, ySplit = 1, x = 0, y = 0;// w, h;
 	private Image image;
 
 	private Vector2 bRight;
 	public int timeStretch = 1;
 	public float sizeOffset = 0;
 
 	/**
 	 * Create a sprite with given image;
 	 * 
 	 * @param image
 	 *            an image
 	 * @param xTiles
 	 *            tiles win x direction
 	 * @param yTiles
 	 *            tiles in y direction
 	 */
 	public Sprite(final Image image, final int xTiles, final int yTiles) {
 		this.image = image;
 		this.xSplit = xTiles;
 		this.ySplit = yTiles;
 		bRight = new Vector2(image.getWidth(null) / xSplit, image.getHeight(null) / ySplit);
 	}
 
 	/**
 	 * Use this constructor if you plan on overriding draw(); Everything will be
 	 * null.
 	 */
 	public Sprite() {
 
 	}
 
 	public void nextFrame() {
 		x = ++x % (xSplit * timeStretch);
 	}
 
 	public void setFrame(final int frame) {
		x = frame * timeStretch % (xSplit * timeStretch);
 	}
 
 	public void setCycle(final int cycle) {
 		y = cycle % ySplit;
 	}
 
 	public void set(final int x, final int y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	/**
 	 * Override this to implement your own drawing mechanism!
 	 * @param g
 	 * @param self
 	 * @param position
 	 * @param dimension
 	 */
 	public void draw(final Graphics g, final Entity self, final Vector2 position, final Vector2 dimension) {
 		int x = this.x / timeStretch;
 		Vector2 dim = dimension.add(sizeOffset * 2);
 		Vector2 pos = position.sub(sizeOffset);
 		g.drawImage(image, (int) pos.x, (int) pos.y, (int) pos.x + (int) dim.x,
 				(int) pos.y + (int) dim.y, x * (int) bRight.x, y
 						* (int) bRight.y, x * (int) bRight.x + (int) bRight.x,
 				y * (int) bRight.y + (int) bRight.y, null);
 	}
 
 	public Image getImage() {
 		return image;
 	}
 	
 	public Dimension getTileDimension() {
 		return new Dimension(x, y);
 	}
 }
