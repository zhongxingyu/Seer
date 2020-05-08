 /*
  * Copyright (C) 2006-2013 Samuel Penn, sam@glendale.org.uk
  *
  * BSD.
  */
 package uk.org.glendale.graphics;
 
 import java.awt.*;
 import java.awt.font.FontRenderContext;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.awt.image.ImageObserver;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.StringTokenizer;
 
 import javax.imageio.ImageIO;
 
 //import uk.org.glendale.rpg.utils.Die;
 
 //import com.sun.image.codec.jpeg.JPEGCodec;
 //import com.sun.image.codec.jpeg.JPEGImageEncoder;
 
 /**
  * A wrapper around the Java image manipulation classes. Provides a simple
  * way to create and modify an image and output the result as a JPEG file.
  *
  * @author Samuel Penn
  */
 public class SimpleImage implements ImageObserver {
 	private Image image = null;
 
 	private String fontName = "Verdana";
 
 	/**
 	 * Create a new blank image.
 	 *
 	 * @param width
 	 *            Width of the image.
 	 * @param height
 	 *            Height of the image.
 	 *
 	 * @return Created image.
 	 */
 	public SimpleImage(int width, int height) {
 		System.setProperty("java.awt.headless", "true");
 		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 	}
 
 	public SimpleImage(Image image) {
 		this.image = image;
 	}
 
 	public SimpleImage(File file) {
 		image = Toolkit.getDefaultToolkit().getImage(file.getAbsolutePath());
 		MediaTracker tracker = new MediaTracker(new Container());
 		tracker.addImage(image, 1);
 		try {
 			tracker.waitForID(1);
 		} catch (InterruptedException e) {
 
 		}
 	}
 
 	public static Image createImage(int width, int height, String colour) {
 		BufferedImage image = new BufferedImage(width, height,
 				BufferedImage.TYPE_INT_RGB);
 		Graphics g = image.createGraphics();
 		int red = 0, green = 0, blue = 0, alpha = 255;
 
 		if (colour.startsWith("#")) {
 			colour = colour.substring(1);
 		}
 
 		red = Integer.parseInt(colour.substring(0, 2), 16);
 		green = Integer.parseInt(colour.substring(2, 4), 16);
 		blue = Integer.parseInt(colour.substring(4, 6), 16);
 		if (colour.length() == 8) {
 			alpha = Integer.parseInt(colour.substring(6, 8));
 		}
 
 		g.setColor(new Color(red, green, blue, alpha));
 		g.fillRect(0, 0, width, height);
 
 		return image;
 	}
 
 	private static int var(int colour, int variance) {
 		//colour += Die.die(variance) - Die.die(variance);
 		if (colour < 0) colour = 0;
 		if (colour > 255) colour = 255;
 		return colour;
 	}
 
 	public static Image createImage(int width, int height, String colour, int v) {
 		BufferedImage image = new BufferedImage(width, height,
 				BufferedImage.TYPE_INT_RGB);
 		Graphics g = image.createGraphics();
 		int red = 0, green = 0, blue = 0, alpha = 255;
 
 		if (colour.startsWith("#")) {
 			colour = colour.substring(1);
 		}
 
 		red = Integer.parseInt(colour.substring(0, 2), 16);
 		green = Integer.parseInt(colour.substring(2, 4), 16);
 		blue = Integer.parseInt(colour.substring(4, 6), 16);
 		if (colour.length() == 8) {
 			alpha = Integer.parseInt(colour.substring(6, 8));
 		}
 
 		for (int y=0; y < height; y++) {
 			for (int x=0; x < height; x++) {
 				g.setColor(new Color(var(red, v), var(green, v), var(blue, v), alpha));
 				g.fillRect(x, y, 1, 1);
 			}
 		}
 
 		return image;
 	}
 
 	public static Image createImage(int width, int height, URL url)
 			throws MalformedURLException {
 		Image image = null;
 
 		image = Toolkit.getDefaultToolkit().getImage(url);
 
 		MediaTracker tracker = new MediaTracker(new Container());
 		tracker.addImage(image, 0);
 		try {
 			tracker.waitForID(0);
 		} catch (InterruptedException e) {
 
 		}
 		image = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
 		tracker.addImage(image, 1);
 		try {
 			tracker.waitForID(1);
 		} catch (Exception e) {
 
 		}
 
 		return image;
 	}
 
 	/**
 	 * Create a new plain image with the specified colour. The colour is of the
 	 * form '#rrggbb' or '#rrggbbaa' where aa is the alpha value. The hash is
 	 * optional.
 	 *
 	 * @param width
 	 *            Width of the image.
 	 * @param height
 	 *            Height of the image.
 	 * @param colour
 	 *            RGB or RGBA colour string.
 	 *
 	 * @return Created image.
 	 */
 	public SimpleImage(int width, int height, String colour) {
 		image = createImage(width, height, colour);
 	}
 
 	public Image getImage() {
 		return image;
 	}
 
 	public BufferedImage getBufferedImage() {
 		BufferedImage bimage = null;
 
 		int type = BufferedImage.TYPE_INT_RGB;
 		bimage = new BufferedImage(image.getWidth(null), image.getHeight(null),
 				type);
 
 		// Copy image to buffered image
 		Graphics g = bimage.createGraphics();
 
 		// Paint the image onto the buffered image
 		g.drawImage(image, 0, 0, null);
 		g.dispose();
 
 		return bimage;
 	}
 
 	public int getColour(int x, int y) {
 		BufferedImage bimage = getBufferedImage();
 
 		return bimage.getRGB(x, y);
 	}
 
 	public void paint(URL url, int x, int y, int width, int height)
 			throws MalformedURLException {
 		Graphics g = image.getGraphics();
 		Image i = createImage(width, height, url);
 		g.drawImage(i, x, y, this);
 	}
 
 	public void paint(Image i, int x, int y, int width, int height) {
 		Graphics g = image.getGraphics();
 		g.drawImage(i, x, y, width, height, this);
 	}
 
 	/**
 	 * Get a colour from a string described a hex RGB value.
 	 *
 	 * @param colour
 	 *            Colour, in the form RRGGBB in hex notation.
 	 * @return A Java Color object.
 	 */
 	private Color getColour(String colour) {
 		Color color = null;
 		int r = 0;
 		int g = 0;
 		int b = 0;
 		int	alpha = 255;
 
 		try {
 			if (colour.startsWith("#")) {
 				r = Integer.parseInt(colour.substring(1, 3), 16);
 				g = Integer.parseInt(colour.substring(3, 5), 16);
 				b = Integer.parseInt(colour.substring(5, 7), 16);
 				if (colour.length() > 7) {
 					alpha = Integer.parseInt(colour.substring(7, 9), 16);
 				}
 			} else {
 				StringTokenizer tokens = new StringTokenizer(colour);
 				double d = 0.0;
 				// Red.
 				d = Double.parseDouble(tokens.nextToken());
 				r = (int) (255 * d);
 				// Green.
 				d = Double.parseDouble(tokens.nextToken());
 				g = (int) (255 * d);
 				// Blue.
 				d = Double.parseDouble(tokens.nextToken());
 				b = (int) (255 * d);
 			}
 			color = new Color(r, g, b, alpha);
 		} catch (NumberFormatException e) {
 			// Bad colour.
 			System.out.println("Bad colour [" + colour + "]");
 			color = new Color(0, 0, 0);
 		} catch (IllegalArgumentException e) {
 			System.out.println("Bad colour [" + colour + "] [" + r + "," + g
 					+ "," + b + "] (" + e.getMessage() + ")");
 			color = new Color(0, 0, 0);
 		}
 
 		return color;
 	}
 
 	/**
 	 * Draw a filled circle at the specified pixel location, with the given
 	 * radius and colour.
 	 *
 	 * @param x
 	 *            X position of the circle's centre.
 	 * @param y
 	 *            Y position of the circle's centre.
 	 * @param radius
 	 *            Radius in pixels.
 	 * @param colour
 	 *            Colour as a RRGGBB string.
 	 */
 	public void circle(int x, int y, int radius, String colour) {
 		Graphics2D g = (Graphics2D) image.getGraphics();
 		g.setColor(getColour(colour));
 		// XXX: Bug! The radius is implemented as a diameter!
 		g.fillOval(x - radius / 2, y - radius / 2, radius, radius);
 	}
	
	public void hex(int x, int y, int r, String colour) {
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setColor(getColour(colour));
		
		double ROOT3 = Math.sqrt(3);
		
		int[]	xp = new int[6];
		int[]	yp = new int[6];
		
		int h = (int)(r * ROOT3 / 2.0);
		int	o = r / 2;
		
		xp[0] = x + o;		// Done
		yp[0] = y;			// Done
		
		xp[1] = x + o + r;	// Done
		yp[1] = y;			// Done
		
		xp[2] = x + r * 2; 	// Done
		yp[2] = y + h;		// Done
		
		xp[3] = x + o + r;	// Done
		yp[3] = y + h * 2;	// Done
		
		xp[4] = x + o;
		yp[4] = y + h * 2;	// Done
		
		xp[5] = x;
		yp[5] = y + h;
		
		g.fillPolygon(xp, yp, 6);

	}
 
 	public void line(double x0, double y0, double x1, double y1) {
 		line((int) x0, (int) y0, (int) x1, (int) y1, "#000000", 0f);
 	}
 
 	public void line(int x0, int y0, int x1, int y1) {
 		line(x0, y0, x1, y1, "#000000", 0f);
 	}
 
 	public void line(double x0, double y0, double x1, double y1, String colour, float width) {
 		line((int) x0, (int) y0, (int) x1, (int) y1, colour, width);
 	}
 
 	public void line(double x0, double y0, double x1, double y1, String colour) {
 		line((int) x0, (int) y0, (int) x1, (int) y1, colour, 0f);
 	}
 
 	public void line(int x0, int y0, int x1, int y1, String colour, float width) {
 		Graphics2D g = (Graphics2D) image.getGraphics();
 
 		g.setStroke(new BasicStroke(width));
 		g.setColor(getColour(colour));
 		g.drawLine(x0, y0, x1, y1);
 	}
 
 	public void rectangle(int x, int y, int w, int h, String colour) {
 		Graphics2D g = (Graphics2D) image.getGraphics();
 
 		g.setColor(getColour(colour));
 		g.drawRect(x, y, w, h);
 
 	}
 
 	public void rectangleFill(int x, int y, int w, int h, String colour) {
 		Graphics2D g = (Graphics2D) image.getGraphics();
 
 		g.setColor(getColour(colour));
 		g.fillRect(x, y, w, h);
 	}
 
 	/**
 	 * Draws an isosceles triangle outline. The triangle always has a flat
 	 * base, and is either pointing up (negative height) or down (positive
 	 * height). The length of the base is twice the given width.
 	 *
 	 * @param x			X coordinate of left edge.
 	 * @param y			Y coordinate of left edge.
 	 * @param w			Half width.
 	 * @param h			Height.
 	 * @param colour	Colour to use for outline.
 	 */
 	public void triangle(int x, int y, int w, int h, String colour) {
 		Graphics2D g = (Graphics2D) image.getGraphics();
 
 		g.setColor(getColour(colour));
 		int[]	xp = new int[3];
 		int[]	yp = new int[3];
 
 		xp[0] = x;
 		yp[0] = y;
 
 		xp[1] = x + 2 * w;
 		yp[1] = y;
 
 		xp[2] = x + w;
 		yp[2] = y + h;
 
 		g.drawPolygon(xp, yp, 3);
 	}
 
 	/**
 	 * Draws an isosceles filled triangle. The triangle always has a flat
 	 * base, and is either pointing up (negative height) or down (positive
 	 * height). The length of the base is twice the given width.
 	 *
 	 * @param x			X coordinate of left edge.
 	 * @param y			Y coordinate of left edge.
 	 * @param w			Half width.
 	 * @param h			Height.
 	 * @param colour	Colour to use for outline.
 	 */
 	public void triangleFill(int x, int y, int w, int h, String colour) {
 		Graphics2D g = (Graphics2D) image.getGraphics();
 
 		g.setColor(getColour(colour));
 		int[]	xp = new int[3];
 		int[]	yp = new int[3];
 
 		xp[0] = x;
 		yp[0] = y;
 
 		xp[1] = x + 2 * w;
 		yp[1] = y;
 
 		xp[2] = x + w;
 		yp[2] = y + h;
 
 		g.fillPolygon(xp, yp, 3);
 	}
 
 	public void text(int x, int y, String text, int style, int size,
 			String colour) {
 		Graphics2D g = (Graphics2D) image.getGraphics();
 		g.setColor(getColour(colour));
 		g.setFont(new Font(fontName, style, size));
 		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
 							RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 
 		g.drawString(text, x, y);
 	}
 
 	public int getTextWidth(String text, int style, int size) {
 		Graphics2D g = (Graphics2D) image.getGraphics();
 		Font font = new Font(fontName, style, size);
 
 		FontRenderContext frc = g.getFontRenderContext();
 		Rectangle2D		  box = font.getStringBounds(text, frc);
 		return (int)(box.getMaxX() - box.getMinX());
 /*
 		FontMetrics metrics = g.getFontMetrics(font);
 		if (metrics == null)
 			return 0;
 		return metrics.stringWidth(text);
 */
 	}
 
 	public void arc(int x, int y, int size, String colour) {
 		Graphics2D g = (Graphics2D) image.getGraphics();
 		g.setColor(getColour(colour));
 		g.drawArc(x, y, size, size, -45, 270);
 	}
 
 	public void circleOutline(int x, int y, int size, String colour) {
 		Graphics2D g = (Graphics2D) image.getGraphics();
 		g.setColor(getColour(colour));
 		g.drawArc(x-size/2, y-size/2, size, size, 0, 360);
 	}
 	public void save(File path, Image image) throws IOException {
 		BufferedImage bimage = null;
 		int type = BufferedImage.TYPE_INT_RGB;
 		bimage = new BufferedImage(image.getWidth(null), image.getHeight(null),
 				type);
 
 		// Copy image to buffered image
 		Graphics g = bimage.createGraphics();
 
 		// Paint the image onto the buffered image
 		g.drawImage(image, 0, 0, null);
 
 		OutputStream out = new BufferedOutputStream(new FileOutputStream(path));
 		//JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
 		//encoder.encode(bimage);
 		ImageIO.write(bimage, "jpg", out);
 	}
 
 	public void save(File path, BufferedImage bimage) throws IOException {
 		OutputStream out = new BufferedOutputStream(new FileOutputStream(path));
 		//JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
 		//encoder.encode(bimage);
 		ImageIO.write(bimage, "jpg", out);
 	}
 
 	public void save(File path) throws IOException {
 		BufferedImage bimage = getBufferedImage();
 
 		// while (!ready) { }
 		OutputStream out = new BufferedOutputStream(new FileOutputStream(path));
 		//JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
 		//encoder.encode(bimage);
 		ImageIO.write(bimage, "jpg", out);
 	}
 
 	public ByteArrayOutputStream save() throws IOException {
 		BufferedImage				bimage = getBufferedImage();
 		ByteArrayOutputStream		out = new ByteArrayOutputStream();
 
 		//JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
 		//encoder.encode(bimage);
 		ImageIO.write(bimage, "jpg", out);
 
 		return out;
 	}
 	
 	public SimpleImage getScaled(int width, int height) throws IOException {
 		return new SimpleImage(getBufferedImage().getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH));
 	}
 
 	/**
 	 * Return the specified image. If the path starts with '#', then the image
 	 * is taken to be an RGB colour code, and a new plain image is created of
 	 * that colour. Otherwise, it is assumed to be a filename and is loaded from
 	 * the Jar file.
 	 *
 	 * @param path
 	 *            Path to file or RGB colour code.
 	 * @param width
 	 *            Width of image to return.
 	 * @param height
 	 *            Height of image to return.
 	 *
 	 * @return Image scaled to the specified size.
 	 *
 	 * public Image getImage(String path, int width, int height) throws
 	 * MalformedURLException { Image image = null;
 	 *
 	 * if (path.startsWith("#")) { image = createImage(width, height, path); }
 	 * else { URL url = new URL(resourcePath+"/"+path); image =
 	 * (BufferedImage)Toolkit.getDefaultToolkit().getImage(url); image =
 	 * (BufferedImage)image.getScaledInstance(width, height,
 	 * Image.SCALE_SMOOTH); }
 	 *
 	 * return image; }
 	 */
 
 	public static void main(String[] args) throws Exception {
 		SimpleImage image = new SimpleImage(20, 20, "#FF0000");
 
 		image.save(new File("/home/sam/foo.jpg"));
 
 		image = new SimpleImage(200, 200, "#00FF00");
 		image.paint(new URL("file:/home/sam/foo.jpg"), 20, 20, 20, 20);
 		image.save(new File("/home/sam/foo2.jpg"));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int,
 	 *      int, int, int)
 	 */
 	public boolean imageUpdate(Image img, int info, int x, int y, int width,
 			int height) {
 		if ((info & ImageObserver.FRAMEBITS) > 0) {
 			return false;
 		}
 		// TODO Auto-generated method stub
 		return true;
 	}
 }
