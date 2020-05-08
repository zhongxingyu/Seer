 package org.rsbot.script.util;
 
 import org.rsbot.script.methods.Game;
 import org.rsbot.script.methods.MethodContext;
 import org.rsbot.script.methods.Skills;
 import org.rsbot.script.wrappers.*;
 import org.rsbot.util.GlobalConfiguration;
 
 import javax.imageio.ImageIO;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.awt.image.RenderedImage;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.logging.Logger;
 
 /**
  * @author Fletch To 99, jtryba
  * @version 1.01
  */
 public class PaintUtil {
 	public PaintUtil(MethodContext context, Graphics render) {
 		ctx = context;
 		g2 = (Graphics2D) render;
 	}
 
 	private final LinkedList<MouseSquarePathPoint> mouseSquarePath = new LinkedList<MouseSquarePathPoint>();
 	private final LinkedList<MouseCirclePathPoint> mouseCirclePath = new LinkedList<MouseCirclePathPoint>();
 	private final LinkedList<MousePathPoint> mousePath = new LinkedList<MousePathPoint>();
 	private final LinkedList<MouseClick> mouseClick = new LinkedList<MouseClick>();
 	private final LinkedList<MousePicPoint> mousePic = new LinkedList<MousePicPoint>();
 	private MethodContext ctx;
 	private Graphics2D g2;
 	private BufferedImage img = null;
 
 	private class MousePathPoint extends Point {
 		private static final long serialVersionUID = 1L;
 
 		private final int toColor(final double d) {
 			return Math.min(255, Math.max(0, (int) d));
 		}
 
 		private long finishTime;
 		private double lastingTime;
 
 		public MousePathPoint(int x, int y, int lastingTime) {
 			super(x, y);
 			this.lastingTime = lastingTime;
 			finishTime = System.currentTimeMillis() + lastingTime;
 		}
 
 		public boolean isUp() {
 			return System.currentTimeMillis() > finishTime;
 		}
 
 		public double toTime(final double d) {
 			return d * ((finishTime - System.currentTimeMillis()) / lastingTime);
 		}
 	}
 
 	private class MousePicPoint extends Point {
 		private static final long serialVersionUID = 1L;
 		private long finishTime;
 
 		public MousePicPoint(int x, int y, int lastingTime) {
 			super(x, y);
 			finishTime = System.currentTimeMillis() + lastingTime;
 		}
 
 		public boolean isUp() {
 			return System.currentTimeMillis() > finishTime;
 		}
 	}
 
 	private class MouseCirclePathPoint extends Point {
 		private static final long serialVersionUID = 1L;
 
 		private int toColor(final double d) {
 			return Math.min(255, Math.max(0, (int) d));
 		}
 
 		private long finishTime;
 		private double lastingTime;
 
 		public MouseCirclePathPoint(int x, int y, int lastingTime) {
 			super(x, y);
 			this.lastingTime = lastingTime;
 			finishTime = System.currentTimeMillis() + lastingTime;
 		}
 
 		public boolean isUp() {
 			return System.currentTimeMillis() > finishTime;
 		}
 
 		public double toTime(final double d) {
 			return d * ((finishTime - System.currentTimeMillis()) / lastingTime);
 		}
 	}
 
 	private class MouseSquarePathPoint extends Point {
 		private static final long serialVersionUID = 1L;
 
 		private int toColor(final double d) {
 			return Math.min(255, Math.max(0, (int) d));
 		}
 
 		private long finishTime;
 		private double lastingTime;
 
 		public MouseSquarePathPoint(int x, int y, int lastingTime) {
 			super(x, y);
 			this.lastingTime = lastingTime;
 			finishTime = System.currentTimeMillis() + lastingTime;
 		}
 
 		public boolean isUp() {
 			return System.currentTimeMillis() > finishTime;
 		}
 
 		public double toTime(final double d) {
 			return d * ((finishTime - System.currentTimeMillis()) / lastingTime);
 		}
 	}
 
 	private class MouseClick extends Point {
 		private static final long serialVersionUID = 1L;
 
 		private int toColor(final double d) {
 			return Math.min(255, Math.max(0, (int) d));
 		}
 
 		private long finishTime;
 		private double lastingTime;
 
 		public MouseClick(int x, int y, int lastingTime) {
 			super(x, y);
 			finishTime = System.currentTimeMillis() + lastingTime;
 		}
 
 		public boolean isUp() {
 			return System.currentTimeMillis() > finishTime;
 		}
 
 		public double toTime() {
 			return 256 * ((finishTime - System.currentTimeMillis()) / lastingTime);
 		}
 	}
 
 	/**
 	 * Draws an RSModel
 	 *
 	 * @param model RSModel to draw
 	 * @param color Color to draw the RSModel
 	 * @param alpha The opacity of the color.
 	 * @author jtryba
 	 */
 	public void drawModel(final RSModel model, final Color color, final int alpha) {
 		if (model != null) {
 			g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
 			for (Polygon p1 : model.getTriangles()) {
 				g2.fillPolygon(p1);
 			}
 		}
 	}
 
 	/**
 	 * Gets a image of your choice from the internet.
 	 *
 	 * @param fileName What to save the image as, the file name.
 	 * @param save     The option to save the file on the local computer.
 	 * @param url      The url location for the image.
 	 * @author Fletch To 99
 	 */
 	public Image getImage(final String fileName, final boolean save, final String url) {
 		Logger log = Logger.getLogger(this.getClass().getName());
		File dir = new File(GlobalConfiguration.Paths.getScriptCacheDirectory() + "/image_io");
 		try {
 			if (!dir.exists()) {
				if (!dir.mkdir()) {
 					return null;
 				}
 			}
 		} catch (Exception e) {
 			return null;
 		}
 		ImageIO.setCacheDirectory(dir);
 		if (save) {
 			try {
 				File f = new File(GlobalConfiguration.Paths.getScriptCacheDirectory() + "/" + fileName);
 				File loc = new File(GlobalConfiguration.Paths.getScriptCacheDirectory() + "/");
 				if (loc.exists()) {
 					if (f.exists()) {
 						log.info("Successfully loaded Image from scripts folder.");
 						return ImageIO.read(f.toURI().toURL());
 					}
 				}
 				Image img = ImageIO.read(new URL(url));
 				if (img != null) {
 					if (!loc.exists()) {
 						loc.mkdir();
 					}
 					ImageIO.write((RenderedImage) img, "PNG", f);
 					log.info("Saved Image to Scripts folder successfully.");
 					return img;
 				}
 			} catch (IOException e) {
 				log.severe("No Internet Connection or Broken Image Link");
 			}
 		} else if (!save) {
 			try {
 				return ImageIO.read(new URL(url));
 			} catch (MalformedURLException e) {
 			} catch (IOException e) {
 				log.severe("No Internet Connection or Broken Image Link");
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the runtime of the script.
 	 *
 	 * @param startTime When the script started (System.currentTimeMillis())
 	 * @author Fletch To 99
 	 */
 	public String getRuntime(final long startTime) {
 		return Timer.format(System.currentTimeMillis() - startTime);
 	}
 
 	/**
 	 * Gets the hourly amount for an item.
 	 *
 	 * @param input     The item you want to get the hourly amount for.
 	 * @param startTime when the script started (System.currentTimeMillis())
 	 * @author Fletch To 99
 	 */
 	public int getHourly(final int input, final long startTime) {
 		double millis = System.currentTimeMillis() - startTime;
 		return (int) ((input / millis) * 3600000);
 	}
 
 	/**
 	 * Draws a simple paint over the chat.
 	 *
 	 * @param skill     The number of the skill wanting to display. E.g Skills.MAGIC
 	 * @param startXP   The amount of xp the person started with related to the chosen
 	 *                  skill.
 	 * @param amount    The amount of the item. (E.X. Bows Fletched)
 	 * @param startTime The time the script started
 	 * @param textColor The color for the text in the script.
 	 * @param status    The current state the script is in. (E.X. Fletching: logs)
 	 * @author Fletch To 99, jtryba
 	 */
 	public void drawPaint(final int skill, final int startXP, final int amount, final long startTime, final Color textColor, final String status) {
 		drawPaint(skill, startXP, amount, startTime, textColor, status, false, false);
 	}
 
 	/**
 	 * Draws a simple paint over the chat.
 	 *
 	 * @param skill              The number of the skill wanting to display. E.g Skills.MAGIC
 	 * @param startXP            The amount of xp the person started with related to the chosen
 	 *                           skill.
 	 * @param amount             The amount of the item. (E.X. Bows Fletched)
 	 * @param startTime          The time the script started
 	 * @param textColor          The color for the text in the script.
 	 * @param status             The current state the script is in. (E.X. Fletching: logs)
 	 * @param drawHideShowButton Draw a show/hide button?
 	 * @author Fletch To 99, jtryba
 	 */
 	public void drawPaint(final int skill, final int startXP, final int amount, final long startTime, final Color textColor, final String status, final boolean drawHideShowButton) {
 		drawPaint(skill, startXP, amount, startTime, textColor, status, drawHideShowButton, false);
 	}
 
 	/**
 	 * Draws a simple paint over the chat.
 	 *
 	 * @param skill              The number of the skill wanting to display. E.g Skills.MAGIC
 	 * @param startXP            The amount of xp the person started with related to the chosen
 	 *                           skill.
 	 * @param amount             The amount of the item. (E.X. Bows Fletched)
 	 * @param startTime          The time the script started
 	 * @param textColor          The color for the text in the script.
 	 * @param status             The current state the script is in. (E.X. Fletching: logs)
 	 * @param drawHideShowButton Draw a show/hide button?
 	 * @param isHidden           Draw main paint?
 	 * @author Fletch To 99, jtryba
 	 */
 	public void drawPaint(final int skill, final int startXP, final int amount, final long startTime, final Color textColor, final String status, final boolean drawHideShowButton, final boolean isHidden) {
 		if (!isHidden) {
 			g2.setFont(new Font("Arial", 1, 15));
 			g2.setColor(new Color(220, 202, 169));
 			g2.fillRect(6, 344, 507, 129);
 			g2.setColor(textColor);
 			g2.drawString("Time Running: " + getRuntime(startTime), 60, 372);
 			g2.drawString("Exp Gained: " + (ctx.skills.getCurrentExp(skill) - startXP), 60, 391);
 			g2.drawString("Exp/H: " + getHourly(ctx.skills.getCurrentExp(skill) - startXP, startTime), 60, 409);
 			g2.drawString("Done:  " + amount, 60, 426);
 			g2.drawString("Items/Hr:  " + getHourly(amount, startTime), 60, 444);
 			long ttl = ctx.skills.getTimeTillNextLevel(skill, startXP, System.currentTimeMillis() - startTime);
 			if (ttl != -1) {
 				g2.drawString("Estimated TTL: " + Timer.format(ttl), 10, 40);
 			} else {
 				g2.drawString("Estimated TTL: 00:00:00", 10, 40);
 			}
 			g2.drawString("Status:  " + status, 60, 466);
 			if (img == null) {
 				img = (BufferedImage) getImage(String.valueOf(skill) + ".png", true, "http://dl.dropbox.com/u/23938245/Scripts/Paint%20Class/capes/" + skill + ".png");
 			} else {
 				g2.drawImage(img, 440, 330, null);
 			}
 			drawProgressBar(skill, 4, 3, 512, 18, Color.RED, Color.GREEN, textColor, 127);
 			drawMouse(textColor, 10, true, 1500);
 		}
 		if (drawHideShowButton) {
 			g2.setColor(getInverseColor(textColor));
 			if (isHidden) {
 				g2.fillRect(22, 344, 90, 14);
 			}
 			Rectangle HIDE = new Rectangle(6, 344, 14, 14);
 			g2.fillRect(HIDE.x, HIDE.y, HIDE.width, HIDE.height);
 			g2.setColor(Color.black);
 			g2.drawRect(HIDE.x, HIDE.y, HIDE.width, HIDE.height);
 			g2.setColor(textColor);
 			g2.setFont(new Font("Arial", 1, 15));
 			g2.drawString("Show/Hide", 23, 356);
 		}
 	}
 
 	/**
 	 * Gets an inverse color for the specific color.
 	 *
 	 * @param color The color to inverse.
 	 * @return The inversed color.
 	 */
 	public Color getInverseColor(Color color) {
 		return new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue());
 	}
 
 	/**
 	 * Draws a gradient progress bar using the co-ordinates, dimensions and
 	 * skill provided. This also displays current level in the skill, percent
 	 * till the next level & exp needed to reach the next level
 	 *
 	 * @param skill     The number of the skill wanting to display. E.g Skills.MAGIC
 	 * @param x         The "x" co-ordinate.
 	 * @param y         The "y" co-ordinate.
 	 * @param width     The width of the progress bar.
 	 * @param height    The height of the progress bar.
 	 * @param colorBase The base color, normally red.
 	 * @param colorOver The overlay color, normally green.
 	 * @param textColor The text color.
 	 * @param alpha     The opacity of the bar. Range: (0 - 255)
 	 * @author Fletch To 99
 	 */
 	public void drawProgressBar(final int skill, final int x, final int y, final int width, final int height, final Color colorBase, final Color colorOver, final Color textColor, final int alpha) {
 		GradientPaint base = new GradientPaint(x, y, new Color(200, 200, 200, alpha), x, y + height + 3, colorBase);
 		GradientPaint overlay = new GradientPaint(x, y, new Color(200, 200, 200, alpha), x, y + height + 3, colorOver);
 		g2.setPaint(base);
 		g2.fillRect(x, y, width, height);
 		g2.setPaint(overlay);
 		g2.fillRect(x, y, (int) (width * (ctx.skills.getPercentToNextLevel(skill) / 100.0)), height);
 		g2.setColor(Color.BLACK);
 		g2.drawRect(x, y, width, height);
 		g2.setFont(new Font("Arial", 1, ((width / 35) + (height / 5))));
 		String progress = ctx.skills.getPercentToNextLevel(skill) + "% to " + (ctx.skills.getCurrentLevel(skill) + 1) + " " + Skills.SKILL_NAMES[skill] + " | " + ctx.skills.getExpToNextLevel(skill) + " XP Until level";
 		g2.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 150));
 		g2.drawString(progress, x + ((width - (g2.getFontMetrics().stringWidth(progress))) / 2), (int) (y + ((g2.getFontMetrics().getHeight() / 2) + (height / 4) * 1.65)));
 	}
 
 	/**
 	 * Draws a 3D progress bar using the co-ordinates, dimensions and skill
 	 * provided. This also displays current level in the skill, percent till the
 	 * next level & exp needed to reach the next level
 	 *
 	 * @param skill     The number of the skill wanting to display. E.g Skills.MAGIC
 	 * @param x         The "x" co-ordinate.
 	 * @param y         The "y" co-ordinate.
 	 * @param width     The width of the progress bar.
 	 * @param height    The height of the progress bar.
 	 * @param color     The base color, normally r
 	 * @param textColor The text color.
 	 * @param alpha     The opacity of the bar. Range: (0 - 255)
 	 * @author Fletch To 99
 	 */
 	public void draw3DProgressBar(final int skill, final int x, final int y, final int width, final int height, final Color color, final Color textColor, final int alpha) {
 		g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
 		g2.fillRect(x - (width / 100), y - (width / 100), width + (width / 40), height + (width / 40));
 		g2.fill3DRect(x, y, (int) (width * (ctx.skills.getPercentToNextLevel(skill) / 100.0)), height, true);
 		g2.setColor(Color.BLACK);
 		g2.drawRect(x - (width / 100), y - (width / 100), width + (width / 40), height + (width / 40));
 		g2.setFont(new Font("Arial", 1, ((width / 35) + (height / 5))));
 		String progress = ctx.skills.getPercentToNextLevel(skill) + "% to " + (ctx.skills.getCurrentLevel(skill) + 1) + " " + Skills.SKILL_NAMES[skill] + " | " + ctx.skills.getExpToNextLevel(skill) + " XP Until level";
 		g2.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 150));
 		g2.drawString(progress, x + ((width - (g2.getFontMetrics().stringWidth(progress))) / 2), (int) (y + ((g2.getFontMetrics().getHeight() / 2) + (height / 4) * 1.65)));
 	}
 
 	/**
 	 * Draws a oval where the clients cursor is.
 	 *
 	 * @param color       Color of the mouse to draw.
 	 * @param diameter    The diameter of the circle.
 	 * @param click       Paint a string saying click where the user/script clicks.
 	 * @param lastingTime The length of the time "click" will appear for, (0 for false
 	 *                    click).
 	 * @author Fletch To 99
 	 */
 	public void drawMouse(final Color color, final int diameter, final Boolean click, final int lastingTime) {
 		Point m = ctx.mouse.getLocation();
 		Point p = ctx.mouse.getPressLocation();
 		g2.setColor(color);
 		g2.drawOval(m.x - 1, m.y - 1, 2, 2);
 		g2.drawOval(m.x - diameter / 2, m.y - diameter / 2, diameter, diameter);
 		if (click) {
 			while (!mouseClick.isEmpty() && mouseClick.peek().isUp()) {
 				mouseClick.remove();
 			}
 			MouseClick click1 = new MouseClick(p.x, p.y, lastingTime);
 			if (mouseClick.isEmpty() || !mouseClick.getLast().equals(click1)) {
 				mouseClick.add(click1);
 			}
 			MouseClick lastPoint = null;
 			for (MouseClick a : mouseClick) {
 				if (lastPoint != null) {
 					g2.setFont(new Font("Airal", 0, 15));
 					g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), a.toColor(a.toTime())));
 					g2.drawString("click", a.x - 13, a.y - diameter / 2 - 3);
 				}
 				lastPoint = a;
 			}
 		}
 	}
 
 	/**
 	 * Draws a crosshair to the mouse.
 	 *
 	 * @param color The color to draw the crosshair.
 	 * @author Fletch To 99
 	 */
 	public void drawMouseCrosshair(final Color color) {
 		int gW = ctx.game.getWidth();
 		int gH = ctx.game.getHeight();
 		Point localPoint = ctx.mouse.getLocation();
 		g2.setColor(color);
 		g2.drawLine(0, localPoint.y, gW, localPoint.y);
 		g2.drawLine(localPoint.x, 0, localPoint.x, gH);
 	}
 
 	/**
 	 * Draws a line where the clients cursor is.
 	 *
 	 * @param color       Color of the line to draw.
 	 * @param lastingTime The time for the line to stay on the screen.
 	 * @author Fletch To 99
 	 */
 	public void drawMouseLine(final Color color, final int lastingTime) {
 		Point m = ctx.mouse.getLocation();
 		while (!mousePath.isEmpty() && mousePath.peek().isUp()) {
 			mousePath.remove();
 		}
 		MousePathPoint mp = new MousePathPoint(m.x, m.y, lastingTime);
 		if (mousePath.isEmpty() || !mousePath.getLast().equals(mp)) {
 			mousePath.add(mp);
 		}
 		MousePathPoint lastPoint = null;
 		for (MousePathPoint a : mousePath) {
 			if (lastPoint != null) {
 				g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), a.toColor(a.toTime(256))));
 				g2.drawLine(a.x, a.y, lastPoint.x, lastPoint.y);
 			}
 			lastPoint = a;
 		}
 	}
 
 	/**
 	 * Draws circles in a line where the mouse is.
 	 *
 	 * @param color       Color of the line to draw.
 	 * @param lastingTime The time for the line to stay on the screen.
 	 * @param diameter    The diameter of the circle.
 	 * @author Fletch To 99
 	 */
 	public void drawCircleMouseLine(final Color color, final int lastingTime, final int diameter) {
 		Point m = ctx.mouse.getLocation();
 		while (!mouseCirclePath.isEmpty() && mouseCirclePath.peek().isUp()) {
 			mouseCirclePath.remove();
 		}
 		MouseCirclePathPoint mp = new MouseCirclePathPoint(m.x, m.y, lastingTime);
 		if (mouseCirclePath.isEmpty() || !mouseCirclePath.getLast().equals(mp)) {
 			mouseCirclePath.add(mp);
 		}
 		MouseCirclePathPoint lastPoint = null;
 		for (MouseCirclePathPoint a : mouseCirclePath) {
 			if (lastPoint != null) {
 				g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), a.toColor(a.toTime(256))));
 				g2.fillOval(a.x - a.toColor(a.toTime(diameter)) / 2, a.y - a.toColor(a.toTime(diameter)) / 2, a.toColor(a.toTime(diameter)), a.toColor(a.toTime(diameter)));
 				g2.setColor(new Color(0, 0, 0, a.toColor(a.toTime(256))));
 				g2.drawOval(a.x - a.toColor(a.toTime(diameter)) / 2, a.y - a.toColor(a.toTime(diameter)) / 2, a.toColor(a.toTime(diameter)), a.toColor(a.toTime(diameter)));
 			}
 			lastPoint = a;
 		}
 	}
 
 	/**
 	 * Draws pictures in a line where the mouse is in the center of the picture.
 	 *
 	 * @param image       The image to draw.
 	 * @param lastingTime The time for the image to stay on the screen.
 	 * @author Fletch To 99
 	 */
 	public void drawPicMouseLine(final Image image, final int lastingTime) {
 		int h = image.getHeight(null);
 		int w = image.getWidth(null);
 		Point m = ctx.mouse.getLocation();
 		while (!mousePic.isEmpty() && mousePic.peek().isUp()) {
 			mousePic.remove();
 		}
 		MousePicPoint mp = new MousePicPoint(m.x, m.y, lastingTime);
 		if (mousePic.isEmpty() || !mousePic.getLast().equals(mp)) {
 			mousePic.add(mp);
 		}
 		MousePicPoint lastPoint = null;
 		for (MousePicPoint a : mousePic) {
 			if (lastPoint != null) {
 				g2.drawImage(image, a.x - w / 2, a.y - h / 2, null);
 			}
 			lastPoint = a;
 		}
 	}
 
 	/**
 	 * Draws pictures in a line where the mouse is.
 	 *
 	 * @param image       The image to draw.
 	 * @param lastingTime The time for the image to stay on the screen.
 	 * @param offsetX     The offset (x) where the mouse point is.
 	 * @param offsetY     The offset (Y) where the mouse point is.
 	 * @author Fletch To 99
 	 */
 	public void drawPicMouseLine(final Image image, final int lastingTime, final int offsetX, final int offsetY) {
 		Point m = ctx.mouse.getLocation();
 		while (!mousePic.isEmpty() && mousePic.peek().isUp()) {
 			mousePic.remove();
 		}
 		MousePicPoint mp = new MousePicPoint(m.x, m.y, lastingTime);
 		if (mousePic.isEmpty() || !mousePic.getLast().equals(mp)) {
 			mousePic.add(mp);
 		}
 		MousePicPoint lastPoint = null;
 		for (MousePicPoint a : mousePic) {
 			if (lastPoint != null) {
 				g2.drawImage(image, a.x - offsetX, a.y - offsetY, null);
 			}
 			lastPoint = a;
 		}
 	}
 
 	/**
 	 * Draws squares in a line where the mouse is.
 	 *
 	 * @param color       Color of the line to draw.
 	 * @param lastingTime The time for the line to stay on the screen.
 	 * @param sideLength  The side length of the square.
 	 * @author Fletch To 99
 	 */
 	public void drawSquareMouseLine(final Color color, final int lastingTime, final int sideLength) {
 		Point m = ctx.mouse.getLocation();
 		while (!mouseSquarePath.isEmpty() && mouseSquarePath.peek().isUp()) {
 			mouseSquarePath.remove();
 		}
 		MouseSquarePathPoint mp = new MouseSquarePathPoint(m.x, m.y, lastingTime);
 		if (mouseSquarePath.isEmpty() || !mouseSquarePath.getLast().equals(mp)) {
 			mouseSquarePath.add(mp);
 		}
 		MouseSquarePathPoint lastPoint = null;
 		for (MouseSquarePathPoint a : mouseSquarePath) {
 			if (lastPoint != null) {
 				g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), a.toColor(a.toTime(256))));
 				g2.fillRect(a.x - a.toColor(a.toTime(sideLength)) / 2, a.y - a.toColor(a.toTime(sideLength)) / 2, a.toColor(a.toTime(sideLength)), a.toColor(a.toTime(sideLength)));
 				g2.setColor(new Color(0, 0, 0, a.toColor(a.toTime(256))));
 				g2.drawRect(a.x - a.toColor(a.toTime(sideLength)) / 2, a.y - a.toColor(a.toTime(sideLength)) / 2, a.toColor(a.toTime(sideLength)), a.toColor(a.toTime(sideLength)));
 			}
 			lastPoint = a;
 		}
 	}
 
 	/**
 	 * Draws the object of your choice.
 	 *
 	 * @param object Target object to color.
 	 * @param color  Color to color the model.
 	 * @param alpha  The opacity of the color.
 	 * @author Fletch To 99, jtryba
 	 */
 	public void drawObject(final RSObject object, final Color color, final int alpha) {
 		if (object != null) {
 			drawModel(object.getModel(), color, alpha);
 			drawTileMM(object.getLocation(), color, alpha);
 		}
 	}
 
 	/**
 	 * Draws the object of your choice.
 	 *
 	 * @param object    Target object to color.
 	 * @param color     Color to color the model.
 	 * @param alpha     The opacity of the color.
 	 * @param drawModel True if you wish to draw the RSModel
 	 * @author Fletch To 99, jtryba
 	 */
 	public void drawObject(final RSObject object, final Color color, final int alpha, final boolean drawModel) {
 		if (object != null) {
 			if (drawModel) {
 				drawModel(object.getModel(), color, alpha);
 			} else {
 				drawTileOnScreen(object.getLocation(), color, alpha);
 			}
 			drawTileMM(object.getLocation(), color, alpha);
 		}
 	}
 
 	/**
 	 * Draws a npc of your choice.
 	 *
 	 * @param npc   Target NPC id to color.
 	 * @param color Color to color the model.
 	 * @param alpha The opacity of the color.
 	 * @author Fletch To 99, jtryba
 	 */
 	public void drawNpc(final int npc, final Color color, final int alpha) {
 		RSNPC NPC = ctx.npcs.getNearest(npc);
 		if (NPC != null) {
 			drawModel(NPC.getModel(), color, alpha);
 			drawTileMM(NPC.getLocation(), color, alpha);
 		}
 	}
 
 	/**
 	 * Draws the nearest npc of your choice.
 	 *
 	 * @param npc   Target NPC id to color.
 	 * @param color Color to color the model.
 	 * @param alpha The opacity of the color.
 	 * @author Fletch To 99, jtryba
 	 */
 	public void drawNpc(final int npc, final Color color, final int alpha, final boolean drawModel) {
 		RSNPC NPC = ctx.npcs.getNearest(npc);
 		if (NPC != null) {
 			if (drawModel) {
 				drawModel(NPC.getModel(), color, alpha);
 			} else {
 				drawTileOnScreen(NPC.getLocation(), color, alpha);
 			}
 			drawTileMM(NPC.getLocation(), color, alpha);
 		}
 	}
 
 	/**
 	 * Draws a npc of your choice.
 	 *
 	 * @param NPC   Target NPC to color.
 	 * @param color Color to color the model.
 	 * @param alpha The opacity of the color.
 	 * @author Fletch To 99, jtryba
 	 */
 	public void drawNpc(final RSNPC NPC, final Color color, final int alpha) {
 		if (NPC != null) {
 			drawTileOnScreen(NPC.getLocation(), color, alpha);
 			drawTileMM(NPC.getLocation(), color, alpha);
 		}
 	}
 
 	/**
 	 * Draws a npc of your choice.
 	 *
 	 * @param NPC       Target NPC to color.
 	 * @param color     Color to color the model.
 	 * @param alpha     The opacity of the color.
 	 * @param drawModel True if you wish to draw the RSModel
 	 * @author Fletch To 99, jtryba
 	 */
 	public void drawNpc(final RSNPC NPC, final Color color, final int alpha, final boolean drawModel) {
 		if (NPC != null) {
 			if (drawModel) {
 				drawModel(NPC.getModel(), color, alpha);
 			} else {
 				drawTileOnScreen(NPC.getLocation(), color, alpha);
 			}
 			drawTileMM(NPC.getLocation(), color, alpha);
 		}
 	}
 
 	/**
 	 * Draws a player of your choice.
 	 *
 	 * @param player Target player to color.
 	 * @param color  Color to color the model.
 	 * @param alpha  The opacity of the color.
 	 * @author Fletch To 99, jtryba
 	 */
 
 	public void drawPlayer(final RSPlayer player, final Color color, final int alpha) {
 		if (player != null) {
 			drawModel(player.getModel(), color, alpha);
 			drawTileMM(player.getLocation(), color, alpha);
 		}
 	}
 
 	/**
 	 * Draws a player of your choice.
 	 *
 	 * @param player    Target player to color.
 	 * @param color     Color to color the model.
 	 * @param alpha     The opacity of the color.
 	 * @param drawModel True if you wish to draw the RSModel
 	 * @author Fletch To 99, jtryba
 	 */
 	public void drawPlayer(final RSPlayer player, final Color color, final int alpha, final boolean drawModel) {
 		if (player != null) {
 			if (drawModel) {
 				drawModel(player.getModel(), color, alpha);
 			} else {
 				drawTileOnScreen(player.getLocation(), color, alpha);
 			}
 			drawTileMM(player.getLocation(), color, alpha);
 		}
 	}
 
 	/**
 	 * Draws all items in the inventory.
 	 *
 	 * @param color Color to color the item.
 	 * @author Fletch To 99
 	 */
 	public void drawItems(final Color color) {
 		if (ctx.game.getCurrentTab() == Game.TAB_INVENTORY) {
 			g2.setFont(new Font(null, Font.BOLD, 8));
 			g2.setColor(color);
 			RSItem[] curItem = ctx.inventory.getItems();
 			for (int i = 0; i < 28; i++) {
 				if (curItem[i].getID() != -1) {
 					Rectangle bounds = curItem[i].getComponent().getArea();
 					String[] name = curItem[i].getName().trim().split(">");
 					g2.draw(bounds);
 					g2.drawString(name[1], bounds.x - 5, bounds.y + bounds.height - 5);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Draws the items in the inventory.
 	 *
 	 * @param color Color to color the item.
 	 * @author Fletch To 99
 	 */
 	public void drawItems(final int[] items, final Color color) {
 		if (ctx.game.getCurrentTab() == Game.TAB_INVENTORY) {
 			g2.setFont(new Font(null, Font.BOLD, 8));
 			g2.setColor(color);
 			for (RSItem i : ctx.inventory.getItems(items)) {
 				if (i.getID() != -1) {
 					Rectangle bounds = i.getComponent().getArea();
 					String[] name = i.getName().trim().split(">");
 					g2.draw(bounds);
 					g2.drawString(name[1], bounds.x - 5, bounds.y + bounds.height - 5);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Draws the neasrest GroundItem matching id
 	 *
 	 * @param id    id to draw
 	 * @param color Color to draw the item
 	 * @author jtryba
 	 */
 	public void drawNearestGroundItem(final int id, final Color color, final int alpha) {
 		RSGroundItem item = ctx.groundItems.getNearest(id);
 		if (item != null) {
 			drawTileOnScreen(item.getLocation(), color, alpha);
 			drawTileMM(item.getLocation(), color, alpha);
 		}
 	}
 
 	/**
 	 * Draws a specific RSGroundItem (screen and minimap)
 	 *
 	 * @param groundItem RSGroundItem to draw
 	 * @param color      Color to draw the RSGroundItem
 	 * @author jtryba
 	 */
 	public void drawGroundItem(final RSGroundItem groundItem, final Color color, final int alpha) {
 		if (groundItem != null) {
 			drawTileOnScreen(groundItem.getLocation(), color, alpha);
 			drawTileMM(groundItem.getLocation(), color, alpha);
 		}
 	}
 
 	/**
 	 * Draws RSGroundItems (screen and minimap)
 	 *
 	 * @param ids   array of ids to draw
 	 * @param color Color to draw groundItem
 	 * @author jtryba
 	 */
 	public void drawGroundItems(final int[] ids, final Color color, final int alpha) {
 		final Filter<RSGroundItem> filter = new Filter<RSGroundItem>() {
 			public boolean accept(RSGroundItem gi) {
 				return gi != null && gi.getItem() != null && idMatch(gi.getItem().getID());
 			}
 
 			public boolean idMatch(final int id) {
 				for (int i = 0; i < ids.length; i++) {
 					if (ids[i] == id) {
 						return true;
 					}
 				}
 				return false;
 			}
 		};
 		RSGroundItem[] items = ctx.groundItems.getAll(filter);
 		for (RSGroundItem item : items) {
 			drawTileOnScreen(item.getLocation(), color, alpha);
 			drawTileMM(item.getLocation(), color, alpha);
 		}
 	}
 
 	/**
 	 * Draws the RSTiles on tiles on the minimap.
 	 *
 	 * @param tiles The array of tiles to color.
 	 * @param color Color to color the tile.
 	 * @author Fletch To 99
 	 */
 	public void drawTilesMM(final RSTile[] tiles, final Color color) {
 		for (RSTile tile : tiles) {
 			if (tile != null) {
 				final int tX = tile.getX(), tY = tile.getY();
 				Point p1 = ctx.calc.worldToMinimap(tX - 0.4, tY - 0.4);
 				Point p2 = ctx.calc.worldToMinimap(tX - 0.4, tY + 0.4);
 				Point p3 = ctx.calc.worldToMinimap(tX + 0.4, tY + 0.4);
 				Point p4 = ctx.calc.worldToMinimap(tX + 0.4, tY - 0.4);
 				if (p1.x != -1 && p2.x != -1 && p3.x != -1 && p4.x != -1) {
 					int[] allX = new int[]{p1.x, p2.x, p3.x, p4.x};
 					int[] allY = new int[]{p1.y, p2.y, p3.y, p4.y};
 					g2.setColor(color);
 					g2.fillPolygon(allX, allY, 4);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Draws an RSTile on the minimap.
 	 *
 	 * @param tile  The tile to color.
 	 * @param color Color to color the model.
 	 * @author Fletch To 99
 	 */
 	public void drawTileMM(final RSTile tile, final Color color, final int alpha) {
 		if (tile != null) {
 			final int tX = tile.getX(), tY = tile.getY();
 			Point p1 = ctx.calc.worldToMinimap(tX - 0.4, tY - 0.4);
 			Point p2 = ctx.calc.worldToMinimap(tX - 0.4, tY + 0.4);
 			Point p3 = ctx.calc.worldToMinimap(tX + 0.4, tY + 0.4);
 			Point p4 = ctx.calc.worldToMinimap(tX + 0.4, tY - 0.4);
 			if (p1.x != -1 && p2.x != -1 && p3.x != -1 && p4.x != -1) {
 				int[] allX = new int[]{p1.x, p2.x, p3.x, p4.x};
 				int[] allY = new int[]{p1.y, p2.y, p3.y, p4.y};
 				g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
 				g2.fillPolygon(allX, allY, 4);
 			}
 		}
 	}
 
 	/**
 	 * Draws an RSTile on the minimap
 	 *
 	 * @param tiles Tiles to draw
 	 * @param color Color to draw the tiles
 	 * @author jtryba
 	 */
 	public void drawTilesOnScreen(final RSTile[] tiles, final Color color, final int alpha) {
 		if (tiles != null) {
 			for (RSTile tile : tiles) {
 				drawTileOnScreen(tile, color, alpha);
 			}
 		}
 	}
 
 	/**
 	 * Draws an RSTile on the screen
 	 *
 	 * @param tile  Tile to draw
 	 * @param color Color to draw the tile
 	 * @author Fletch to 99, jtryba
 	 */
 	public void drawTileOnScreen(final RSTile tile, final Color color, final int alpha) {
 		Point southwest = ctx.calc.tileToScreen(tile, 0, 0, 0);
 		Point southeast = ctx.calc.tileToScreen(tile, 1, 0, 0);
 		Point northwest = ctx.calc.tileToScreen(tile, 0, 1, 0);
 		Point northeast = ctx.calc.tileToScreen(tile, 1, 1, 0);
 		if (ctx.calc.pointOnScreen(southwest) && ctx.calc.pointOnScreen(southeast) && ctx.calc.pointOnScreen(northwest) && ctx.calc.pointOnScreen(northeast)) {
 			g2.setColor(Color.BLACK);
 			g2.drawPolygon(new int[]{(int) northwest.getX(), (int) northeast.getX(), (int) southeast.getX(), (int) southwest.getX()}, new int[]{(int) northwest.getY(), (int) northeast.getY(), (int) southeast.getY(), (int) southwest.getY()}, 4);
 			g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
 			g2.fillPolygon(new int[]{(int) northwest.getX(), (int) northeast.getX(), (int) southeast.getX(), (int) southwest.getX()}, new int[]{(int) northwest.getY(), (int) northeast.getY(), (int) southeast.getY(), (int) southwest.getY()}, 4);
 		}
 	}
 
 	/**
 	 * Draws an RSArea on the screen
 	 *
 	 * @param area  RSArea to draw
 	 * @param color Color to draw the RSArea
 	 * @author jtryba
 	 */
 	public void drawRSAreaOnScreen(RSArea area, Color color, final int alpha) {
 		RSTile[] array = area.getTileArray();
 		for (final RSTile tile : array) {
 			if (tile == null) {
 				continue;
 			}
 			if (!ctx.calc.tileOnScreen(tile)) {
 				continue;
 			}
 			drawTileOnScreen(tile, color, alpha);
 		}
 	}
 
 	/**
 	 * Draws an RSArea on the minimap
 	 *
 	 * @param area  RSArea to draw
 	 * @param color Color to draw the RSArea
 	 * @author jtryba
 	 */
 	public void drawRSAreaMM(RSArea area, Color color, final int alpha) {
 		RSTile[] array = area.getTileArray();
 		for (final RSTile tile : array) {
 			if (tile == null) {
 				continue;
 			}
 			drawTileMM(tile, color, alpha);
 		}
 	}
 
 	/*
 	public void drawWebOnScreen(int alpha) {
 		Collection<TileFlags> tiles = Web.map.values();
 		for (TileFlags t : tiles) {
 			if (t != null) {
 				if (t.isWalkable()) {
 					drawTileOnScreen(t, Color.GREEN, alpha);
 				} else if (t.isQuestionable()) {
 					drawTileOnScreen(t, Color.YELLOW, alpha);
 				} else if (t.isWater()) {
 					drawTileOnScreen(t, Color.CYAN, alpha);
 				}
 			}
 		}
 	}
 
 	public void drawWebMM(int alpha) {
 		Collection<TileFlags> tiles = Web.map.values();
 		for (TileFlags t : tiles) {
 			if (t != null) {
 				if (t.isWalkable()) {
 					drawTileMM(t, Color.GREEN, alpha);
 				} else if (t.isQuestionable()) {
 					drawTileMM(t, Color.YELLOW, alpha);
 				} else if (t.isWater()) {
 					drawTileMM(t, Color.CYAN, alpha);
 				} else if (!t.isWater() && !t.isWalkable() && !t.isQuestionable()) {
 					drawTileMM(t, Color.RED, alpha);
 				}
 			}
 		}
 	}*/
 }
