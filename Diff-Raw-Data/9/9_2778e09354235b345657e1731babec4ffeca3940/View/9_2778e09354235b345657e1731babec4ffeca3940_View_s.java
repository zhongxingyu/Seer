 package kth.csc.inda.ads;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class View extends JFrame implements KeyListener {
 
 	private static final long serialVersionUID = 5692460536621794238L;
 	private static final Color EMPTY_COLOR = Color.WHITE;
 	private static FieldView fieldView;
 	public JPanel main;
 	private ArrayList<NSnake> players;
 
 	/**
 	 * Creates the view.
 	 * 
 	 * @param height
 	 *            The height in number of squares.
 	 * @param width
 	 *            The width in number of squares.
 	 */
 	public View(int height, int width) {
 		setTitle("Achtung die Schlange");
 		setBackground(Color.WHITE);
 		fieldView = new FieldView(height, width);
 
 		main = new JPanel(new CardLayout());
 		main.add(fieldView, "Field");
 		getContentPane().add(main, BorderLayout.CENTER);
 
 		addKeyListener(this);
 
 		// Pack everything so the stuff fits nicely together.
 		pack();
 		// Center it.
 		setLocationRelativeTo(null);
 		// makes sure application exits when window is closed
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		// Make it visible.
 		setVisible(true);
 	}
 
 	/**
 	 * Set the players used in the class.
 	 * 
 	 * @param players
 	 *            The players to be used in the game.
 	 */
 	public void SetPlayers(ArrayList<NSnake> players) {
 		this.players = players;
 	}
 
 	/**
 	 * Show the current status of the field.
 	 * 
 	 * @param field
 	 *            The field whose status is to be displayed.
 	 */
 	public void showStatus(NField field) {
 		if (!isVisible()) {
 			setVisible(true);
 		}
 		fieldView.preparePaint();
 		for (int row = 0; row < field.getDepth(); row++) {
 			for (int col = 0; col < field.getWidth(); col++) {
 				ActAndDraw seg = field.getObjectAt(row, col);
 				if (seg != null) {
 					fieldView.drawMark(col, row, seg.getColor());
 				} else {
 					fieldView.drawMark(col, row, EMPTY_COLOR);
 				}
 			}
 		}
 		fieldView.repaint();
 	}
 
 	/**
 	 * Provide a graphical view of a rectangular field. This is a nested class
 	 * (a class defined inside a class) which defines a custom component for the
 	 * user interface. This component displays the field. This is rather
 	 * advanced GUI stuff - you can ignore this for your project if you like.
 	 */
 	public static class FieldView extends JPanel {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -8066453167601983635L;
 
 		private final int GRID_VIEW_SCALING_FACTOR = 6;
 
 		private int gridWidth, gridHeight;
 		private int xScale, yScale;
 		Dimension size;
 		private Graphics g;
 		private Image fieldImage;
 
 		/**
 		 * Create a new FieldView component.
 		 */
 		public FieldView(int height, int width) {
 			gridHeight = height;
 			gridWidth = width;
 			size = new Dimension(0, 0);
 		}
 
 		/**
 		 * Tell the GUI manager how big we would like to be.
 		 */
 		public Dimension getPreferredSize() {
 			return new Dimension(gridWidth * GRID_VIEW_SCALING_FACTOR,
 					gridHeight * GRID_VIEW_SCALING_FACTOR);
 		}
 
 		/**
 		 * Prepare for a new round of painting. Since the component may be
 		 * resized, compute the scaling factor again.
 		 */
 		public void preparePaint() {
 			if (!size.equals(getSize())) { // if the size has changed...
 				size = getSize();
 				fieldImage = fieldView.createImage(size.width, size.height);
 				g = fieldImage.getGraphics();
 
 				xScale = size.width / gridWidth;
 				if (xScale < 1) {
 					xScale = GRID_VIEW_SCALING_FACTOR;
 				}
 				yScale = size.height / gridHeight;
 				if (yScale < 1) {
 					yScale = GRID_VIEW_SCALING_FACTOR;
 				}
 			}
 		}
 
 		/**
 		 * Paint on grid location on this field in a given color.
 		 */
 		public void drawMark(int x, int y, Color color) {
 			g.setColor(color);
 			g.fillRect(x * xScale, y * yScale, xScale - 1, yScale - 1);
 		}
 
 		/**
 		 * The field view component needs to be redisplayed. Copy the internal
 		 * image to screen.
 		 */
 		public void paintComponent(Graphics g) {
 			if (fieldImage != null) {
 				Dimension currentSize = getSize();
 				if (size.equals(currentSize)) {
 					g.drawImage(fieldImage, 0, 0, null);
 				} else {
 					// Rescale the previous image.
 					g.drawImage(fieldImage, 0, 0, currentSize.width,
 							currentSize.height, null);
 				}
 			}
 		}
 	}
 
 	/**
 	 * The logic for key presses. Selfexplanatory....
 	 */
 	@Override
 	public void keyPressed(KeyEvent e) {
 		int key = e.getKeyCode();
 		System.out.println(e.getKeyChar());
 		for (NSnake p : players) {
 			Controls c = p.getControls();
 
 			if (key == c.down()) {
 				p.turn(Direction.DOWN);
 			} else if (key == c.up()) {
 				p.turn(Direction.UP);
 			} else if (key == c.right()) {
 				p.turn(Direction.RIGHT);
 			} else if (key == c.left()) {
 				p.turn(Direction.LEFT);
 			}
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 	}
 }
