 package coggame;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Rectangle;
 
 /**
 * Sprites are Layers designed to
 * display simple frame-based animations,
 * and also provide facilities for transforming
 * frames and performing collision checks with
 * other Sprites and TiledLayers.
 *
 * @author John Earnest
 **/
 public class Sprite extends Layer {
 	
 	public static final int TRANS_NONE = 0;				// no transform
 	public static final int TRANS_MIRROR_HORIZ = 1;		// mirror horizontally
 	public static final int TRANS_MIRROR_VERT = 2;		// mirror vertically
 
 	private final int frameWidth;
 	private final int frameHeight;
 	private final int sheetWidth;
 	private final Image frames;
 
 	private int transform = TRANS_NONE;
 	private int frame = 1;
 	private Rectangle collision;
 
 	/**
 	* Construct a new non-animated Sprite.
 	*
 	* @param image the image for this Sprite
 	**/
 	public Sprite(Image image) {
 		this(image, image.getWidth(null), image.getHeight(null));
 	}
 
 	/**
 	* Construct an animated sprite from a
 	* sheet of tiles.
 	*
 	* @param image the tilesheet for this Sprite
 	* @param frameWidth the width of an animation frame in pixels
 	* @param frameHeight the height of an animation frame in pixels
 	**/
 	public Sprite(Image image, int frameWidth, int frameHeight) {
 		frames = image;
 		this.frameWidth = frameWidth;
 		this.frameHeight = frameHeight;
 		collision = new Rectangle(0, 0, frameWidth, frameHeight);
 		sheetWidth = image.getWidth(null) / frameWidth;
 	}
 
 	/**
 	* Returns the width of this Sprite in pixels.
 	**/
 	public int getWidth()	{ return frameWidth; }
 
 	/**
 	* Returns the height of this Sprite in pixels.
 	**/
 	public int getHeight()	{ return frameHeight; }
 
 	/**
 	* Returns the current animation frame.
 	**/
 	public int getFrame()	{ return frame; }
 
 	/**
 	* Set the current animation frame.
 	* Frames are 1-indexed.
 	*
 	* @param frame the new frame index
 	**/
 	public void setFrame(int frame)	{
 		this.frame = frame;
 	}
 
 	/**
 	* Choose one of several available
 	* transformations to apply when this Sprite is drawn.
 	*
 	* @param transform the transform to apply
 	**/
 	public void setTransform(int transform) {
 		this.transform = transform;
 	}
 
 	/**
 	* Return a Rectangle representing the bounding
 	* box used for collision, relative to the upper-left
 	* corner of the sprite. By default, this bounding box
 	* is initialized to match the size of an animation frame.
 	**/
 	public Rectangle getCollisionBox() {
 		return collision;
 	}
 
 	/**
 	* Set the bounding box used for collision detection. 
 	* The rectangle specified will be treated as relative
 	* to the upper-left corner of the sprite.
 	*
 	* @param collision the new bounding box
 	**/
 	public void setCollisionBox(Rectangle collision) {
 		this.collision = collision;
 	}
 
 	/**
 	* Draw this Sprite.
 	*
 	* @param g the destination Graphics surface
 	**/
 	public void paint(Graphics g) {
 		if (!isVisible() || frame == 0) { return; }
 		final int tx = ((frame - 1) % sheetWidth) * frameWidth;
 		final int ty = ((frame - 1) / sheetWidth) * frameHeight;
 		final int dx = getX();
 		final int dy = getY();
 
 		if (transform == TRANS_NONE) {
 			g.drawImage(frames,
 						dx, dy, dx + frameWidth, dy + frameHeight,
 						tx, ty, tx + frameWidth, ty + frameHeight, null);
 		}
 		else if (transform == TRANS_MIRROR_HORIZ) {
 			g.drawImage(frames,
 						dx + frameWidth, dy, dx, dy + frameHeight,
 						tx, ty, tx + frameWidth, ty + frameHeight, null);
 		}
 		else if (transform == TRANS_MIRROR_VERT) {
 			g.drawImage(frames,
 						dx, dy + frameHeight, dx + frameWidth, dy,
 						tx, ty, tx + frameWidth, ty + frameHeight, null);
 		}
 		else {
 			throw new IllegalStateException("Invalid transform!");
 		}
 	}
 
 	/**
 	* Returns true if the collision box of this
 	* Sprite intersects with the collision box
 	* of the other Sprite.
 	*
 	* @param s the Sprite to check intersection with
 	**/
 	public boolean collidesWith(Sprite s) {
 		Rectangle c = s.getCollisionBox();
 		return collidesWith(c.x + s.getX(), c.y + s.getY(), c.width, c.height);
 	}
 
 	/**
 	* Returns true if the collision box of this
 	* Sprite intersects with any non-zero tiles of
 	* the TiledLayer.
 	*
 	* @param t the TiledLayer to check intersection with.
 	**/
 	public boolean collidesWith(TiledLayer t) {
 		int tx = (getX() - t.getX()) / t.getCellWidth();
 		int ty = (getY() - t.getY()) / t.getCellHeight();
 
 		int range = Math.max(	(collision.width / t.getCellWidth()) + 1,
 								(collision.height / t.getCellHeight()) + 1);
 
 		for(int x = -range; x <= range; x++) {
 			for(int y = -range; y <= range; y++) {
 				if (x + tx < 0 || x + tx >= t.getColumns()) { continue; }
 				if (y + ty < 0 || y + ty >= t.getRows())	{ continue; }
 				if (t.getCell(x + tx, y + ty) == 0)			{ continue; }
 				if (collidesWith(	(x + tx) * t.getCellWidth(),
 									(y + ty) * t.getCellHeight(),
 									t.getCellWidth(),
 									t.getCellHeight())) { return true; }
 			}
 		}
 		return false;
 	}
 
 	/**
 	* Returns true if the collision box of this
 	* Sprite intersects with the rectangular region specified.
 	*
 	* @param x the x-offset in pixels of the upper-left corner of the region
 	* @param y the y-offset in pixels of the upper-left corner of the region
 	* @param w the width in pixels of the region
 	* @param h the height in pixels of the region
 	**/
 	public boolean collidesWith(int x, int y, int w, int h) {
		if (getY() + collision.y + collision.height <  y    )	{ return false; }
		if (getY() + collision.y					>= y + h)	{ return false; }
		if (getX() + collision.x + collision.width	<  x    )	{ return false; }
		if (getX() + collision.x					>= x + w)	{ return false; }
 		return true;
 	}
 }
