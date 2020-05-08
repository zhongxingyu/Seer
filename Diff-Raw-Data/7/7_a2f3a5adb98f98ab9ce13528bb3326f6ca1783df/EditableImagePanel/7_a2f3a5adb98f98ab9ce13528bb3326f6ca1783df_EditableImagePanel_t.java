 package util;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Stack;
 
 import javax.swing.JPanel;
 
 import org.imgscalr.Scalr;
 
 public class EditableImagePanel extends JPanel implements MouseListener,
 		MouseMotionListener {
 
 	/**
 	 * The image to be used
 	 */
 	private BufferedImage image;
 
 	/**
 	 * This determines what to draw on the image panel in the pain method
 	 */
 	private int editingMode;
 
 	/**
 	 * Flag variable for cropping mode. True by default.
 	 */
 	private boolean isNewCropRect = true;
 
 	/**
 	 * The start and end x,y coordinates of cropping rectangle being drawn on
 	 * image for cropping
 	 */
 	private int cropx1, cropx2, cropy1, cropy2;
 
 	/**
 	 * A cropped image
 	 */
 	private BufferedImage croppedImage;
 
 	/**
 	 * Text for the top of the Meme
 	 */
 	private String topText;
 	
 	/**
 	 * Text for the bottom of the Meme
 	 */
 	private String bottomText;
 	
 	/**
 	 * The Font for the Meme
 	 */
 	private Font font;
 	
 	/**
 	 * The Font Size for the Meme
 	 */
 	private int fontSize;
 	
 	/**
 	 * The Font Color for the Meme
 	 */
 	private Color fontColor;
 	
 	/**
 	 * FontMetrics
 	 */
 	private FontMetrics fontMetrics;
 	
 	/**
 	 * When resizing image, use this to let the class decide how to constrain
 	 * proportions
 	 */
 	public static final int RESIZE_CONSTRAIN_PROPORTIONS = 0;
 
 	/**
 	 * When resizing image, use this to constrain proportions based on image
 	 * width
 	 */
 	public static final int RESIZE_CONSTRAIN_WIDTH = 1;
 
 	/**
 	 * When resizing image, use this to constrain proportions based on image
 	 * height
 	 */
 	public static final int RESIZE_CONSTRAIN_HEIGHT = 2;
 
 	/**
 	 * When resizing image, use this to resize width and height regardless of
 	 * width/height ratio
 	 */
 	public static final int RESIZE_FIT_EXACT = 3;
 
 	/**
 	 * Constant for normal image viewing mode
 	 */
 	public static final int MODE_VIEW = 0;
 
 	/**
 	 * Constant for cropping mode
 	 */
 	public static final int MODE_CROP = 1;
 
 	/**
 	 * Constant for adding/editing/deleting text
 	 */
 	public static final int MODE_TEXT = 2;
 
 	/**
 	 * Default constructor
 	 */
 	public EditableImagePanel() {
 		setEditingMode(MODE_VIEW);
 		topText = "";
 		bottomText = "";
 		addMouseListener(this);
 		addMouseMotionListener(this);
 	}
 
 	/**
 	 * Create a new image panel from an image object
 	 * 
 	 * @param image
 	 *            The image object to be used
 	 */
 	public EditableImagePanel(BufferedImage image) {
 		this();
 		this.image = image;
 	}
 
 	/**
 	 * Sets the editing mode of the panel
 	 * 
 	 * @param editingMode
 	 *            The mode to be set
 	 */
 	public void setEditingMode(int editingMode) {
 		this.editingMode = editingMode;
 		repaint();
 	}
 
 	/**
 	 * Returns the mode the image panel is in
 	 * 
 	 * @return The editing mode the panel is in
 	 */
 	public int getEditingMode() {
 		return this.editingMode;
 	}
 
 	public void setImage(BufferedImage image) {
 		this.image = image;
 		setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
 		repaint();
 	}
 
 	/**
 	 * Returns the image being used for the panel
 	 * 
 	 * @return Image object being used for panel
 	 */
 	public BufferedImage getImage() {
 		return image;
 	}
 
 	/**
 	 * Return the width of the image
 	 * 
 	 * @return Width in pixels
 	 */
 	public int getWidth() {
 		return image.getWidth();
 	}
 
 	/**
 	 * Return the height of the image
 	 * 
 	 * @return Height in pixels
 	 */
 	public int getHeight() {
 		return image.getHeight();
 	}
 
 	/**
 	 * Override the JPanel's getPreferredSize() method to return the size of the
 	 * image, not the JPanel
 	 */
 	@Override
 	public Dimension getPreferredSize() {
 		return new Dimension(image.getWidth(), image.getHeight());
 	}
 
 	/**
 	 * This will paint stuff differently according to the mode
 	 * 
 	 * @param g
 	 *            The graphics object
 	 */
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		// draw the image
 		g.drawImage(image, 0, 0, null);
 
 		if (getEditingMode() == MODE_CROP) {
 			drawCropBox(g);
 		} else if (getEditingMode() == MODE_TEXT) {
 			drawTopTextOnImage(g);
 			drawBottomTextOnImage(g);
 		}
 
 	}
 
 	/**
 	 * Draws the CropBox on image
 	 * @param g
 	 */
 	private void drawCropBox(Graphics g) {
 		// get the actual width and height of the drawn rectangle
 		int width = getCropx1() - getCropx2();
 		int height = getCropy1() - getCropy2();
 
 		// get the width and height to use for drawing the rectangle
 		int w = Math.abs(width);
 		int h = Math.abs(height);
 
 		// get the coordinates for placing the rectangle
 		int x = width < 0 ? getCropx1() : getCropx2();
 		int y = height < 0 ? getCropy1() : getCropy2();
 
 		if (!this.isNewCropRect) {
 			// draw a rectangle to show the user the area
 			g.drawRect(x, y, w, h);
 
 			// create a cropped image
 			setCroppedImage(x, y, w, h);
 		}
 	}
 
 	/**
 	 * Sets the cropped image
 	 * @param x
 	 * @param y
 	 * @param width
 	 * @param height
 	 */
 	private void setCroppedImage(int x, int y, int width, int height) {
 		if (width > 0 && height > 0) {
 			croppedImage = image.getSubimage(x, y, width, height);
 		} else {
 			croppedImage = image;
 		}
 	}
 
 	/**
 	 * 
 	 * @return croppedImage
 	 */
 	public BufferedImage getCroppedImage() {
 		return croppedImage;
 	}
 
 	private void drawTopTextOnImage(Graphics g) {
 		//Graphics2D g2d = image.createGraphics();
 
 		g.setFont(font);
 		fontMetrics = g.getFontMetrics();
 		g.setColor(fontColor);
 		
 		ArrayList<String> strings = (ArrayList<String>) StringUtils.wrap(topText, fontMetrics, image.getWidth());
 		
 		int y = getTopFontPosY(fontMetrics);
 		
 		if (strings.size()>1){
 			for(String line : strings){
 				int x = getFontPosX(fontMetrics, line);
 				g.drawString(line, x, y);
 				y = y + fontMetrics.getHeight();
 			}
 		} else {
 			int x = getFontPosX(fontMetrics, topText);
 			g.drawString(topText, x, y);
 		}
 	}
 	
 	private void drawBottomTextOnImage(Graphics g) {
 		//Graphics2D g2d = image.createGraphics();
 		g.setFont(font);
 		fontMetrics = g.getFontMetrics();
 		g.setColor(fontColor);
 		
 		ArrayList<String> strings = (ArrayList<String>) StringUtils.wrap(bottomText, fontMetrics, image.getWidth());
 
		int y = getBottomFontPosY(fontMetrics);
 		
 		if (strings.size()>1){
 			Stack<String> stack = new Stack<String>();
 			for(String line : strings){
 				stack.push(line);
 			}
 			while(!stack.isEmpty()){
 				String line = stack.pop();
 				int x = getFontPosX(fontMetrics, line);
 				g.drawString(line, x, y);
 				y = y - fontMetrics.getHeight();
 			}
 		} else {
 			int x = getFontPosX(fontMetrics, bottomText);
 			g.drawString(bottomText, x, y);
 		}	
 	}
 
 	private int getTopFontPosY(FontMetrics fontMetrics) {
 		int topBuffer = (int) (image.getHeight() * .005);
 		int textHeight = fontMetrics.getHeight();
 		int yPos = topBuffer + textHeight;
 		return yPos;
 	}
 	
	private int getBottomFontPosY(FontMetrics fontMetrics) {
		int yPos = (int) ((int) image.getHeight() - (image.getHeight() * .010) - fontMetrics.getDescent());
 		return yPos;
 	}
 
 	private int getFontPosX(FontMetrics fontMetrics, String text) {
 		int textWidth = fontMetrics.stringWidth(text);
 		int xPos = (image.getWidth() - textWidth) / 2;
 		return xPos;
 	}
 
 	/**
 	 * @return the topText
 	 */
 	public String getTopText() {
 		return topText;
 	}
 
 	/**
 	 * @param topText the topText to set
 	 */
 	public void setTopText(String topText) {
 		this.topText = topText;
 	}
 
 	/**
 	 * @return the bottomText
 	 */
 	public String getBottomText() {
 		return bottomText;
 	}
 
 	/**
 	 * @param bottomText the bottomText to set
 	 */
 	public void setBottomText(String bottomText) {
 		this.bottomText = bottomText;
 	}
 
 	/**
 	 * @return the font
 	 */
 	public Font getFont() {
 		return font;
 	}
 
 	/**
 	 * @param font the font to set
 	 */
 	public void setFont(Font font) {
 		this.font = font;
 	}
 
 	/**
 	 * @return the fontColor
 	 */
 	public Color getFontColor() {
 		return fontColor;
 	}
 
 	/**
 	 * @param fontColor the fontColor to set
 	 */
 	public void setFontColor(Color fontColor) {
 		this.fontColor = fontColor;
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		if (getEditingMode() == MODE_CROP) {
 			setCropx2(e.getX());
 			setCropy2(e.getY());
 			isNewCropRect = false;
 			repaint();
 		}
 
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 	/*	if (getEditingMode() == MODE_TEXT) {
 			repaint();
 		}*/
 	}
 	
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		if (getEditingMode() == MODE_CROP) {
 			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
 		} else if (getEditingMode() == MODE_TEXT) {
 			setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
 		}
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// set back to default when mouse outside image
 		setCursor(Cursor.getDefaultCursor());
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		if (getEditingMode() == MODE_CROP) {
 			setCropx1(e.getX());
 			setCropy1(e.getY());
 			isNewCropRect = true;
 			repaint();
 		} else if (getEditingMode() == MODE_TEXT) {
 
 		}
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		if (getEditingMode() == MODE_CROP) {
 			setCropx2(e.getX());
 			setCropy2(e.getY());
 			repaint();
 		} else if (getEditingMode() == MODE_TEXT) {
 
 		}
 	}
 
 	/**
 	 * @return the cropx1
 	 */
 	public int getCropx1() {
 		return cropx1;
 	}
 
 	/**
 	 * @param cropx1
 	 *            the cropx1 to set
 	 */
 	public void setCropx1(int cropx1) {
 		this.cropx1 = cropx1;
 	}
 
 	/**
 	 * @return the cropx2
 	 */
 	public int getCropx2() {
 		return cropx2;
 	}
 
 	/**
 	 * @param cropx2
 	 *            the cropx2 to set
 	 */
 	public void setCropx2(int cropx2) {
 		this.cropx2 = cropx2;
 	}
 
 	/**
 	 * @return the cropy1
 	 */
 	public int getCropy1() {
 		return cropy1;
 	}
 
 	/**
 	 * @param cropy1
 	 *            the cropy1 to set
 	 */
 	public void setCropy1(int cropy1) {
 		this.cropy1 = cropy1;
 	}
 
 	/**
 	 * @return the cropy2
 	 */
 	public int getCropy2() {
 		return cropy2;
 	}
 
 	/**
 	 * @param cropy2
 	 *            the cropy2 to set
 	 */
 	public void setCropy2(int cropy2) {
 		this.cropy2 = cropy2;
 	}
 
 	/**
 	 * Returns a resized image
 	 * 
 	 * @param width
 	 *            The new width in pixels
 	 * @param height
 	 *            The new height in pixels
 	 * @param resizeMode
 	 *            How to resize the image
 	 * @return An Image
 	 */
 	public BufferedImage getResizedImage(int width, int height, int resizeMode) {
 		BufferedImage resizedImage = null;
 		if (resizeMode == RESIZE_CONSTRAIN_PROPORTIONS) {
 			resizedImage = Scalr.resize(image, width);
 		} else if (resizeMode == RESIZE_CONSTRAIN_WIDTH) {
 			resizedImage = Scalr.resize(image, Scalr.Mode.FIT_TO_WIDTH, width);
 		} else if (resizeMode == RESIZE_CONSTRAIN_HEIGHT) {
 			resizedImage = Scalr
 					.resize(image, Scalr.Mode.FIT_TO_HEIGHT, height);
 		} else if (resizeMode == RESIZE_FIT_EXACT) {
 			resizedImage = Scalr.resize(image, Scalr.Mode.FIT_EXACT, width,
 					height);
 		}
 		return resizedImage;
 	}
 
 }
