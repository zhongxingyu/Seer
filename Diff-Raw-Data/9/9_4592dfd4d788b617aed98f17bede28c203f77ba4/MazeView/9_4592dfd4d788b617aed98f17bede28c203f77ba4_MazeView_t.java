 package uk.ac.ox.cs.sokobanexam.ui;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.geom.RoundRectangle2D;
 
 import javax.swing.JComponent;
 
 import uk.ac.ox.cs.sokobanexam.model.sprites.Room;
 import uk.ac.ox.cs.sokobanexam.util.Point;
 
 /**
  * MazeView is responsible for listening to changes in the MazeModel and
  * painting them on the screen.
  * Since MazeView is visible on the screen, controllers may listen to MazeView
  * for notifications on user actions.
  */
 public class MazeView extends JComponent implements MazeChangeListener, SelectionChangeListener {
 	private static final long serialVersionUID = 7808955267765088933L;
 	
 	private static final int GRID_SIZE = 50;
 	private static final double HOVER_PADDING = 0.1;
 	private static final double HOVER_INNER_PADDING = 0.05;
 	private static final double HOVER_ARC = 0.15;
 	private static final double HOVER_TEXT_SIZE = 0.15;
 	
 	private MazeModel mModel;
 	
 	public MazeView(MazeModel model) {
 		setModel(model);
 		setFocusable(true);
 	}
 	
 	/**
 	 * Converts a position with MazeView to a point in the domain model.
 	 * The coordinates must be pixel based and have origin in the upper left corner of the MazeView.
 	 * @param x			the x-axis coordinate 
 	 * @param y			the y-axis coordinate
 	 * @return 			A point located at (x,y) if one exists inside the maze.
 	 *         			Otherwise null is returned 
 	 */
 	public Point pos2Point(int x, int y) {
 		Point point = Point.at(x/GRID_SIZE, y/GRID_SIZE);
 		if (mModel.getMaze().getPoints().contains(point))
 			return point;
 		return null;
 	}
 	/**
 	 * The pixel coordinates based rectangle representing the bounds of the specified point,
 	 * if the point was to be drawn on the MazeView. 
 	 * @param point		the point to draw
 	 * @return			A rectangle containing exactly the point, as would be drawn on screen.
 	 * 					Or if point is null this returns the empty rectangle.
 	 */
 	public Rectangle point2Rect(Point point) {
 		if (point != null)
 			return new Rectangle(point.x*GRID_SIZE, point.y*GRID_SIZE, GRID_SIZE, GRID_SIZE);
 		// It is useful to be able to return the empty rectangle, because we often might want
 		// to find the union of rectangles between two points, of which one of them might not
 		// be set.
 		return new Rectangle();
 	}
 	
 	/**
 	 * Gets the model currently viewed by the MazeView.
 	 * @return			The views model
 	 */
 	public MazeModel getModel() {
 		return mModel;
 	}
 	/**
 	 * Sets a new model and updates the MazeView to fit its maze.
 	 * @param model		A new model to view
 	 */
 	public void setModel(MazeModel model) {
 		this.mModel = model;
 		setPreferredSize(new Dimension(
 				model.getMaze().getWidth()*GRID_SIZE,
 				model.getMaze().getHeight()*GRID_SIZE));
 		model.addMazeChangeListener(this);
 		model.addSelectionChangeListener(this);
 	}
 	
 	@Override
     public void paint(Graphics g) {
 		Graphics2D g2 = (Graphics2D) g;
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 		        RenderingHints.VALUE_ANTIALIAS_ON);
 		SpritePainter painter = new SpritePainter(g2, GRID_SIZE);
 		for (Room room : mModel.getMaze().getRooms())
 			room.accept(painter);
 		
 		if (mModel.isSelectionVisible() && mModel.getSelected() != null) {
 			Rectangle r = point2Rect(mModel.getSelected());
 			g2.setColor(new Color(0xdd66bb88, true));
 			g2.setStroke(new BasicStroke(2f));
 			g2.drawRect(r.x+1, r.y+1, r.width-3, r.height-3);
 		}
 		
 		if (mModel.getHighlighted() != null) {
 			Rectangle r = point2Rect(mModel.getHighlighted());
 			g2.setColor(new Color(0x4488ffbb, true));
 			g2.fillRect(r.x, r.y, r.width, r.height);
 		}
 		
 		if (mModel.getHoverMessage() != null) {
 			// The black background
 			double pad = HOVER_PADDING*Math.min(getHeight(), getWidth());
 			double arc = HOVER_ARC*Math.min(getHeight(), getWidth());
 			g2.setColor(new Color(0x66000000, true));
 			g2.fill(new RoundRectangle2D.Double(pad, pad, getWidth()-2*pad, getHeight()-2*pad, arc, arc));
 			
 			// The white line
 			g2.setColor(Color.WHITE);
 			g2.setStroke(new BasicStroke(3f));
 			double inpad = HOVER_INNER_PADDING*Math.min(getHeight(), getWidth());
 			pad += inpad;
			arc -= inpad; // Align the rounded corners
 			g2.draw(new RoundRectangle2D.Double(pad, pad, getWidth()-2*pad, getHeight()-2*pad, arc, arc));
 			
 			// The text
 			String message = mModel.getHoverMessage();
 			int size = (int)(HOVER_TEXT_SIZE*Math.min(getHeight(), getWidth()));
 			Font font = new Font("Sans", Font.BOLD, size);
 			FontMetrics metrics = g2.getFontMetrics(font);
 			g2.setFont(font);
 			g2.drawString(mModel.getHoverMessage(),
 					(getWidth()-metrics.stringWidth(message))/2,
 					(getHeight()+metrics.getHeight())/2-metrics.getMaxDescent());
 		}
     }
 	
 	@Override
 	public void onChange(MazeModel model) {
 		repaint();
 	}
 	@Override
 	public void onSelectionChanged(MazeModel model, Point from, Point to) {
 		Rectangle bonds = point2Rect(from);
 		bonds.add(point2Rect(to));
 		repaint(bonds);
 	}
 	@Override
 	public void onHighlightChanged(MazeModel mazeModel, Point from, Point to) {
 		Rectangle bonds = point2Rect(from);
 		bonds.add(point2Rect(to));
 		repaint(bonds);
 	}
 	@Override
 	public void onSelectionVisibilityChanged(MazeModel model) {
 		repaint(point2Rect(model.getSelected()));
 	}
 }
