 package worlddemo;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JPanel;
 
 import movedemo.MoveDemo;
 import movedemo.Position;
 
 
 public class TestPanel extends JPanel implements MouseListener, KeyListener {
 	
 	private double width, height;
 	public static Point camera = new Point(300, 100);
 	public static int squareSize;
 	private Point[] stars = new Point[10000];
 	private Position selectedPos;
 	private Dimension sizeOfGrid;
 	private int startX;
 	private int startY;
 	private Position[] path = null;
 	private Map<String, Image> images = null;
 	
 	
 	private World world = null;
 	
 	public TestPanel() {
 		
 		/*
 		 * For this test create world here, should
 		 * be created elsewhere and sent as parameter
 		 */
 		world = new World();
 		
 		measureScreen();
 		for (int i = 0; i < stars.length; i++) {
 			stars[i] = new Point((int) (Math.random()*squareSize*26), (int) (Math.random()*squareSize*26));
 		}
 		sizeOfGrid = new Dimension(20, 20);
 		startX = 3;
 		startY = 2;
 		addMouseListener(this);
 		loadImages();
 	}
 	
 	/**
 	 * Laddar in de bilder som används i en hashmap så att de inte behöver laddas från en fil
 	 * vid varje utritning.
 	 */
 	private void loadImages() {
 		images = new HashMap<String, Image>();
 		images.put("HEAD", Toolkit.getDefaultToolkit().createImage("res/head.png").getScaledInstance(squareSize, squareSize, Image.SCALE_DEFAULT));
 		images.put("STRAIGHT", Toolkit.getDefaultToolkit().createImage("res/straight.png").getScaledInstance(squareSize, squareSize, Image.SCALE_DEFAULT));
 		images.put("START", Toolkit.getDefaultToolkit().createImage("res/start.png").getScaledInstance(squareSize, squareSize, Image.SCALE_DEFAULT));
 		images.put("TURN", Toolkit.getDefaultToolkit().createImage("res/turn.png").getScaledInstance(squareSize, squareSize, Image.SCALE_DEFAULT));
 	}
 	
 	
 	public void paintComponent(Graphics g) {
 		Point cursorLocation = MouseInfo.getPointerInfo().getLocation();
 		int selX = cursorLocation.x + camera.x;
 		int selY = cursorLocation.y + camera.y;
 		
 		/*
 		 * Draw a black background
 		 */
 		g.setColor(Color.BLACK);
 		g.fillRect(0, 0,(int) width,(int) height);
 		
 		/*
 		 * Translate the canvas to show what the camera is currently looking at
 		 */
 		g.translate(-camera.x, -camera.y);
 		
 		/*
 		 * Draw white "stars" on random positions set when started 
 		 */
 		g.setColor(Color.WHITE);
 		for (int i = 0; i < stars.length; i++) {
 			g.fillRect(stars[i].x, stars[i].y, 1, 1);
 		}
 		
 		/*
 		 * Draw a grid marking the different Zones/Positions
 		 */
 		g.setColor(new Color(130,130,130));
 		for (int x = startX; x <= sizeOfGrid.width+startX; x++) {
 			g.drawLine(x * squareSize, startY*squareSize, x * squareSize, squareSize*(sizeOfGrid.width+startY));
 		}
 		for (int y = startY; y <= sizeOfGrid.height+startY; y++) {
 			g.drawLine(startX*squareSize, y * squareSize, squareSize*(sizeOfGrid.height+startX), y * squareSize);
 		}
 		
 		/*
 		 * Draw a greenish rectangle to show what Zone/Position the user is
 		 * currently hovering the mouse over.
 		 */
 		if (legalPos(new Point(selX,selY))) {
 			Position pos = getPos(new Point(selX,selY));
 			fillRect(pos, g, new Color(110, 170, 10, 170));
 		}
 		
 		/*
 		 * If a position is selected draw this.
 		 */
 		if (selectedPos != null) {
 			fillRect(selectedPos, g, new Color(240, 255, 0, 100));
 		}
 		
 		/*
 		 * If the path is not null, call method to draw it
 		 * TODO: Draw several paths
 		 */
 		if (path != null) {
 			drawPaths((Graphics2D) g, path);
 		}
 		
 		// Draw the ships
 		drawShips(g);
 	}
 	
 	private void drawShips(Graphics g) {
 		/* 
 		 * public variables used, create methods
 		 * TODO: fix a nice graphic
 		 */
 		g.setColor(Color.LIGHT_GRAY);
 		for (int i = 1; i <= world.rows; i++) {
 			for (int j = 1; j <= world.cols; j++) {
 				if (world.hasFleet(new Position(i, j))) {
 					g.fillOval((j+2)*squareSize + squareSize/6, (i+1)*squareSize + 2*squareSize/3, 30, 30);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Draw all paths
 	 */
 	private void drawPaths(Graphics2D g2D, Position[] path) {
 		for (int i = 0; i < path.length; i++) {
 			
 			Position previous = i == 0 ? null: path[i-1];; 
 			Position current = path[i]; 
 			Position next = i + 1 < path.length ? path[i+1]: null;
 			
 			String key = getArrow(previous, next);
 			double rotation = getRotation(previous, current, next);
 			
 			g2D.rotate(rotation, (path[i].getCol()+2.5)*squareSize, (path[i].getRow()+1.5)*squareSize);
 			g2D.drawImage(images.get(key), (path[i].getCol()+2)*squareSize, (path[i].getRow()+1)*squareSize, null);
 			g2D.rotate(-rotation, (path[i].getCol()+2.5)*squareSize, (path[i].getRow()+1.5)*squareSize);
 		}
 	}
 	
 	/**
 	 * Returns the rotation that should be used to draw the image based
 	 * on previous and next positions of the path
 	 * @param previous The Position before the one where you want draw in the path
 	 * @param current The Position where you want rotation for an arrow image
 	 * @param next The Position after the one where you want draw in the path
 	 * @return The rotation angle in radians.
 	 */
 	/*
 	 * TODO: Move to another class "Worth saving, may need some changes"
 	 */
 	private double getRotation(Position previous, Position current, Position next) {
 		/*
 		 * Variables for determining where the previous and next positions
 		 * are relative to the current position
 		 */
 		boolean prevAbove = false;
 		boolean prevUnder = false;
 		boolean prevLeft = false;
 		boolean prevRight = false;
 		
 		boolean nextRight = false;
 		boolean nextUnder = false;
 		boolean nextLeft = false;
 		boolean nextAbove = false;
 		
 		/*
 		 * Only calculate values if there is a previous
 		 * respective next position in.
 		 */
 		if (previous != null) {
 			prevAbove = previous.getRow() < current.getRow();
 			prevUnder = previous.getRow() > current.getRow();
 			prevLeft = previous.getCol() < current.getCol();
 			prevRight = previous.getCol() > current.getCol();
 		}
 		if (next != null) {
 			nextRight = next.getCol() > current.getCol();
 			nextUnder = next.getRow() > current.getRow();
 			nextLeft = next.getCol() < current.getCol();
 			nextAbove = next.getRow() < current.getRow();
 		}
 		/*
 		 * If previous is null then only determine rotation based on 
 		 * next position.
 		 * >> Path is always of length 2 at least, therefore no point can
 		 * have neither previous or next location.
 		 */
 		if (previous == null) {
 			if (nextAbove) {
 				return 3*Math.PI/2;
 			} else if (nextUnder) {
 				return Math.PI/2;
 			} else if (nextLeft) {
 				return Math.PI;
 			} else if (nextRight) {
 				return 0;
 			}
 		}
 		/*
 		 * If next is null then only determine rotation based on 
 		 * previous position.
 		 */
 		if (next == null) {
 			if (prevAbove) {
 				return Math.PI/2;
 			} else if (prevUnder) {
 				return 3*Math.PI/2;
 			} else if (prevLeft) {
 				return 0;
 			} else if (prevRight) {
 				return Math.PI;
 			}
 		}
 		/*
 		 * Return rotation based on where the previous and next locations are.
 		 */
 		if (prevAbove) {
 			if (nextUnder) {
 				return Math.PI/2;
 			} else if (nextLeft) {
 				return Math.PI/2;
 			} else if (nextRight) {
 				return Math.PI;
 			}
 		} else if (nextAbove) {
 			if (prevUnder) {
 				return Math.PI/2;
 			} else if (prevLeft) {
 				return Math.PI/2;
 			} else if (prevRight) {
 				return Math.PI;
 			}
 		} else if (prevUnder) {
 			if (nextAbove) {
 				return Math.PI/2;
 			} else if (nextLeft) {
 				return 0;
 			} else if (nextRight) {
 				return 3*Math.PI/2;
 			}
 		} else if (nextUnder) {
 			if (prevAbove) {
 				return Math.PI/2;
 			} else if (prevLeft) {
 				return 0;
 			} else if (prevRight) {
 				return 3*Math.PI/2;
 			}
 		}
 		/*
 		 * Return 0 to make the compiler happy, will never run
 		 * unless previous == current || current == next which
 		 * is wrong usage.
 		 */
 		return 0;
 	}
 	
 	/**
 	 * Returns the String key for each arrow part based on previous and next positions in a path.
 	 * @param previous The Position previous to the one to draw.
 	 * @param next The Position after the one to draw.
 	 * @return a String key that will give a picture from the images MAP
 	 */
 	/*
 	 * Could perhaps return the image instead?
 	 * Advantages/Disadvantages?
 	 */
 	private String getArrow(Position previous, Position next) {
 		if (previous == null) {
 			return "START";
 		} else if (next == null) {
 			return "HEAD";
 		} else if (previous.getCol() != next.getCol() && previous.getRow() != next.getRow()) {
 			return "TURN";
 		} else {
 			return "STRAIGHT";
 		}
 	}
 	
 	/*
 	 * Fills a rectangle with a Color on the sent Graphics representing a Position
 	 */
 	private void fillRect(Position pos, Graphics g, Color c) {
 		g.setColor(c);
 		g.fillRect((pos.getCol() + 2)*squareSize + 1, (pos.getRow() + 1)*squareSize + 1, squareSize -1, squareSize -1);
 	}
 	
 	/*
 	 * Checks if a Point is a legal position
 	 * ?? Should be called before trying to aquire a Position from the point
 	 */
 	private boolean legalPos(Point loc) {
 		if (loc.x <= squareSize * startX || loc.x >= squareSize * (sizeOfGrid.width+startX) || loc.y <= squareSize * startY || loc.y >= squareSize * (sizeOfGrid.height+startY)) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	/*
 	 * Measure the screen and set the squareSize
 	 */
 	public void measureScreen() {
 		width = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
 		height = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
 		squareSize = Math.min((int) (width/6),(int) (height/6));
 	}
 
 	/*
 	 * Returns a Position from a Point.
 	 */
 	private Position getPos(Point p) {
 		int col = (p.x) / squareSize - 2;
 		int row = (p.y) / squareSize - 1;
 		return new Position(row, col);
 	}
 
 	@Override public void mouseClicked(MouseEvent me) {}
 	@Override public void mouseEntered(MouseEvent me) {}
 	@Override public void mouseExited(MouseEvent me) {}
 	@Override public void mouseReleased(MouseEvent me) {}
 	@Override public void mousePressed(MouseEvent me) {
 		Point p = new Point(me.getPoint().x + camera.x, me.getPoint().y+ camera.y);
 		if (legalPos(p)) {
 			Position pos = getPos(p);
 			if (me.getButton() == MouseEvent.BUTTON1) {
 				selectedPos = pos;
 				path = null;
 			}
 			if (selectedPos != null && me.getButton() == MouseEvent.BUTTON3) {
 				if (!pos.equals(selectedPos)) {
 					path = MoveDemo.calcPath2(selectedPos, pos);
 				} else {
 					path = null;
 				}
 			}
 		} else {
 			if (me.getButton() == MouseEvent.BUTTON1) {
 				selectedPos = null;
 				path = null;
 			}
 		}
 	}
 
 	@Override public void keyReleased(KeyEvent event) {}
 	@Override public void keyTyped(KeyEvent event) {}
 	@Override public void keyPressed(KeyEvent event) {
 		if (event.getKeyCode() == KeyEvent.VK_SPACE) {
 			if (path != null) {
 				world.moveShips(path[0], path[path.length-1]);
 				/*
 				 *  Place holder for "ship still selected"
 				 *  also remove path.
 				 */
 				selectedPos = path[path.length-1];
 				path = null;
 			}
 		}
 	}
 }
