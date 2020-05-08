 package grouppractical.client.demo;
 
 import grouppractical.client.MapListener;
 import grouppractical.client.RobotListener;
 import grouppractical.utils.map.Map;
 import grouppractical.utils.map.Position;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 
 /**
  * A JPanel extension to display a graphical representation of a Map,
  * incorporating a MapListener interface to listen to changes as given through a
  * ClientConnection.
  * 
  * @author Joe Zammit
  * 
  */
 public class MapPanel extends JPanel implements MapListener, RobotListener {
 
 	/**
 	 * UID for class
 	 */
 	private static final long serialVersionUID = -3988331113435056079L;
 	/**
 	 * Width (in pixels) of a single block on a map, as displayed on screen
 	 */
 	private final int PIX_WIDTH;
 	/**
 	 * A BufferedImage storing the current graphical representation of the map
 	 */
 	private BufferedImage img;
 	/**
 	 * The corresponding Graphics2D object for the BufferedImage img
 	 */
 	private Graphics2D graphics;
 	/**
 	 * The height of the map, required to allow correct orientation of the
 	 * y-axis in Swing
 	 */
 	private int mapHeight = 0;
 	/**
 	 * The robot's x coordinate
 	 */
 	private int robotX = 0;
 	/**
 	 * The robot's y coordinate
 	 */
 	private int robotY = 0;
 	/**
 	 * Number of update, as pushed to file system copies of map
 	 */
 	private int updateNo = 0;
 
 	/**
 	 * Constructor for a MapPanel JPanel
 	 * 
 	 * @param unitWidth
 	 *            the desired width (in pixels) of a single block on the map, as
 	 *            displayed on screen
 	 */
 	public MapPanel(int unitWidth) {
 		PIX_WIDTH = unitWidth;
 	}
 
 	/**
 	 * Provides a reference to the current graphical representation of the map
 	 * 
 	 * @return Reference to the BufferedImage graphical representation of the
 	 *         map
 	 */
 	public BufferedImage getMapImage() {
 		return img;
 	}
 
 	/**
 	 * Updates the block of pixels at the given coordinate as appropriate given
 	 * a Graphics2D object and the width of each coordinate block
 	 * 
 	 * @param obstacle
 	 *            whether an obstacle exists at the coordinate
 	 * @param certainty
 	 *            the certainty parameter
 	 * @param g
 	 *            the Graphics2D object to write the update to
 	 * @param x
 	 *            the x coordinate of the block
 	 * @param y
 	 *            the y coordinate of the block
 	 * @param unit
 	 *            the width, in pixels, of each coordinate block in the image
 	 */
 	private void updateImgBlock(Boolean obstacle, short certainty,
 			Graphics2D g, int x, int y, int unit) {
 		// get Colour and AlphaComposite to restore after update
 		Color oldColour = g.getColor();
 		AlphaComposite oldAlphaC = (AlphaComposite) g.getComposite();
 		// set graphics colour
 		if (certainty == 0) {
 			// coordinate not examined
 			g.setColor(Color.BLACK);
 		} else {
 			// coordinate examined - print white, and then over-print red or
 			// grey over for obstacle/no obstacle
 			g.setColor(Color.WHITE);
 			g.fillRect(x * unit, (mapHeight - 1 - y) * unit, unit, unit);
 
 			// set new colour
 			if (obstacle) {
 				g.setColor(Color.RED);
 			} else {
 				g.setColor(Color.GRAY);
 			}
 
 			// set new alpha (transparency parameter)
 			float alpha = 0.5f;
 			if (obstacle) {
 				alpha += (Position.certaintyToPercent(certainty) / 2f);
 			} else {
 				alpha = 1.0f - (Position.certaintyToPercent(certainty) / 2f);
 			}
 			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
 					alpha));
 		}
 
 		// draw to position
 		g.fillRect(x * unit, (mapHeight - 1 - y) * unit, unit, unit);
 
 		// reset AlphaComposite and Colour
 		g.setComposite(oldAlphaC);
 		g.setColor(oldColour);
 	}
 
 	public void updateMapPosition(Position position) {
 		// if graphics null, update map hasn't been called yet, so wait for
 		// server to respond to MAP_LISTENER command. Otherwise update.
 		if (graphics != null) {
 			// update BufferedImage
 			updateImgBlock(position.isOccupied(), position.getCertainty(),
 					(Graphics2D) img.getGraphics(), position.getX(),
 					position.getY(), 1);
 			// update screen
 			this.repaint(position.getX() * PIX_WIDTH,
 					(mapHeight - 1 - position.getY()) * PIX_WIDTH, PIX_WIDTH,
 					PIX_WIDTH);
 		}
 	}
 
 	public void updateMap(Map map) {
 		img = new BufferedImage(map.getWidth(), map.getHeight(),
 				BufferedImage.TYPE_INT_ARGB);
 		// height stored in state to allow correct orientation of y axis
 		mapHeight = map.getHeight();
 		graphics = (Graphics2D) img.getGraphics();
 		// initialise to new graphic (black background)
 		for (int i = 0; i < map.getWidth(); i++)
 			for (int j = 0; j < map.getHeight(); j++)
 				updateMapPosition(new Position(i, j, false, (short) 0));
 		// add any known points in map
 		Iterator<Position> it = map.iterator();
 		while (it.hasNext()) {
 			Position p = it.next();
 			updateMapPosition(p);
 		}
 		// repaint entire panel in case dimensions have changed
 		this.repaint();
 	}
 
 	@Override
 	public void paint(Graphics g) {
 		if (img != null) {
 			g.drawImage(img, 0, 0, PIX_WIDTH * img.getWidth(),
 					PIX_WIDTH * img.getHeight(), null);
 		}
 	}
 
 	@Override
 	public void updateRobotPosition(Position position) {
 		int x = position.getX(), y = position.getY();
 		// if graphics null, update map hasn't been called yet, so wait for
 		// server to respond to MAP_LISTENER command. Otherwise update.
 		if (graphics != null) {
 			// update buffered image
 			Color oldColour = graphics.getColor();
 			// set colour
 			graphics.setColor(Color.GREEN);
 			graphics.fillRect(x * PIX_WIDTH, (mapHeight - 1 - y) * PIX_WIDTH,
 					PIX_WIDTH, PIX_WIDTH);
			// clear current robot position
			updateMapPosition(new Position(robotX,robotY,false,(short)127));
 			// reset colour
 			graphics.setColor(oldColour);
 			this.repaint(x * PIX_WIDTH, (mapHeight - 1 - y) * PIX_WIDTH,
 					PIX_WIDTH, PIX_WIDTH);
 			robotX = x;
 			robotY = y;
 		}
 	}
 
 	@Override
 	public void updateAngle(double degrees, double radians) {
 	}
 
 	@Override
 	public void updateVoltage(float voltage) {
 	}
 
 	public boolean writeImgToFile(File f) {
 		if (img==null) {
 			return false;
 		}
 		else {
 			try {
 				ImageIO.write(img, "png", f);
 				return true;
 			}
 			catch(IOException e0) {
 				System.out.println("Error writing image of map @ update " + updateNo);
 				System.out.println("Error message: "+ e0.getMessage());
 				return false;
 			}
 		}
 	}
 	
 	@Override
 	public void updateLocked(boolean locked) {
 		if (locked == false) {
 			updateNo ++;
 			writeImgToFile(new File("map-imgs\\map"+updateNo+".png"));
 		}
 	}
 
 }
