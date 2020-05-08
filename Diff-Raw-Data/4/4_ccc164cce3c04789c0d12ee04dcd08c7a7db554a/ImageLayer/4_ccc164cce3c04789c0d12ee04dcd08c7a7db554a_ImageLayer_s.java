 package net.coobird.paint.image;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
 import net.coobird.paint.BlendingMode;
 
 /*************************
  * TODO Clean up this class!!
  */
 
 /**
  * Represents a image layer within the Untitled Image Manipulation Application
  * @author coobird
  *
  */
 public class ImageLayer
 {
 	private static final int THUMBNAIL_SCALE = 4;
 	
 	private BufferedImage image;
 	private BufferedImage thumbImage;
 	private Graphics2D g;
 	private String caption;
 	private boolean visible;
 	
 	// TODO
 	// Location of ImageLayer wrt some origin
 	private int x;
 	private int y;
 	private int width;
 	private int height;
 	
 	private float alpha = 1.0f;
 	private BlendingMode mode = BlendingMode.LAYER_NORMAL;
 	
 	/**
 	 * ImageLayer must be constructed with a width and height parameter.
 	 * TODO fix this javadoc:
 	 * @see ImageLayer(int,int)
 	 */
 	@SuppressWarnings("unused")
 	private ImageLayer() {};
 	
 	public ImageLayer(BufferedImage image)
 	{
 		initInstance();
 		setImage(image);
 
 		thumbImage = new BufferedImage(
 				this.width / THUMBNAIL_SCALE,
 				this.height / THUMBNAIL_SCALE,
 				BufferedImage.TYPE_INT_ARGB
 		);
 	
 		renderThumbnail();
 	}
 	
 	/**
 	 * Instantiate a new ImageLayer.
 	 * @param width		The width of the ImageLayer.
 	 * @param height	The height of the ImageLayer.
 	 */
 	public ImageLayer(int width, int height)
 	{
 		initInstance();
		setImage(image);
 
 		image = new BufferedImage(
 				width,
 				height,
 				BufferedImage.TYPE_INT_ARGB
 		);
 		
 		thumbImage = new BufferedImage(
 				width / THUMBNAIL_SCALE,
 				height / THUMBNAIL_SCALE,
 				BufferedImage.TYPE_INT_ARGB
 		);
 		
 		renderThumbnail();
 	}
 	
 	/**
 	 * Creates a thumbnail image
 	 */
 	private void renderThumbnail()
 	{
 		// TODO
 		//@resize "image" to "thumbImage"
 		Graphics2D g = thumbImage.createGraphics();
 		
 		g.drawImage(image,
 				0,
 				0,
 				thumbImage.getWidth(),
 				thumbImage.getHeight(),
 				null
 		);
 		
 		g.dispose();		
 	}
 	
 	/**
 	 * Updates the state of the ImageLayer
 	 */
 	public void update()
 	{
 		// Perform action to keep imagelayer up to date
 		// Here, renderThumbnail to synch the image and thumbImage representation
 		renderThumbnail();
 	}
 	
 	/**
 	 * Initializes the instance of ImageLayer.
 	 */
 	private void initInstance()
 	{
 		this.x = 0;
 		this.y = 0;
 		this.setCaption("Untitled Layer");
 	}
 	
 	/**
 	 * TODO
 	 * @return
 	 */
 	public Graphics2D getGraphics()
 	{
 		return g;
 	}
 
 	/**
 	 * Checks if this ImageLayer is set as visible.
 	 * @return	The visibility of this ImageLayer.
 	 */
 	public boolean isVisible()
 	{
 		return visible;
 	}
 
 	/**
 	 * @return the image
 	 */
 	public BufferedImage getImage()
 	{
 		return image;
 	}
 
 	/**
 	 * @param image the image to set
 	 */
 	public void setImage(BufferedImage image)
 	{
 		this.image = image;
 		this.width = image.getWidth();
 		this.height = image.getHeight();
 		this.g = image.createGraphics();
 	}
 
 	/**
 	 * @return the thumbImage
 	 */
 	public BufferedImage getThumbImage()
 	{
 		return thumbImage;
 	}
 
 	/**
 	 * @param thumbImage the thumbImage to set
 	 */
 	public void setThumbImage(BufferedImage thumbImage)
 	{
 		this.thumbImage = thumbImage;
 	}
 
 	/**
 	 * Sets the visibility of this ImageLayer.
 	 * @param visible	The visibility of this ImageLayer.
 	 */
 	public void setVisible(boolean visible)
 	{
 		this.visible = visible;
 	}
 	
 	/**
 	 * Returns the caption of the ImageLayer.
 	 * @return	The caption of the ImageLayer.
 	 */
 	public String getCaption()
 	{
 		return caption;
 	}
 
 	/**
 	 * Sets the caption of the ImageLayer.
 	 * @param caption	The caption to set the ImageLayer to.
 	 */
 	public void setCaption(String caption)
 	{
 		this.caption = caption;
 	}
 
 	/**
 	 * Returns the width of the ImageLayer.
 	 * @return	The width of the ImageLayer.
 	 */
 	public int getWidth()
 	{
 		return image.getWidth();
 	}
 
 	/**
 	 * Returns the height of the ImageLayer.
 	 * @return	The height of the ImageLayer.
 	 */
 	public int getHeight()
 	{
 		return image.getHeight();
 	}
 
 	/**
 	 * @return the x
 	 */
 	public int getX()
 	{
 		return x;
 	}
 
 	/**
 	 * @param x the x to set
 	 */
 	public void setX(int x)
 	{
 		this.x = x;
 	}
 
 	/**
 	 * @return the y
 	 */
 	public int getY()
 	{
 		return y;
 	}
 
 	/**
 	 * @param y the y to set
 	 */
 	public void setY(int y)
 	{
 		this.y = y;
 	}
 	
 	/**
 	 * Sets the ImageLayer offset.
 	 * @param x
 	 * @param y
 	 */
 	public void setLocation(int x, int y)
 	{
 		this.x = x;
 		this.y = y;
 	}
 
 	/**
 	 * @return the alpha
 	 */
 	public float getAlpha()
 	{
 		return alpha;
 	}
 
 	/**
 	 * @param alpha the alpha to set
 	 */
 	public void setAlpha(float alpha)
 	{
 		this.alpha = alpha;
 	}
 
 	/**
 	 * @return the mode
 	 */
 	public BlendingMode getMode()
 	{
 		return mode;
 	}
 
 	/**
 	 * @param mode the mode to set
 	 */
 	public void setMode(BlendingMode mode)
 	{
 		this.mode = mode;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString()
 	{
 		String msg = "Image Layer: " + this.caption + 
 			", x: " + this.x + ", y: " + this.y + 
 			", width: " + this.width + ", height: " + this.height; 
 		
 		return msg;
 	}
 }
