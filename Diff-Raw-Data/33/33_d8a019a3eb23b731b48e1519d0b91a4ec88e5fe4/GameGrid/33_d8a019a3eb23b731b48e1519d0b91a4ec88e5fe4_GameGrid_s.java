 package btlshp.ui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.util.HashMap;
 
 import javax.imageio.ImageIO;
 import javax.swing.JComponent;
 import javax.swing.JPopupMenu;
 
 import btlshp.Btlshp;
 import btlshp.entities.Base;
 import btlshp.entities.Block;
 import btlshp.entities.ConstructBlock;
 import btlshp.entities.Location;
 import btlshp.entities.Map;
 import btlshp.entities.MapNode;
 import btlshp.entities.MineBlock;
 import btlshp.entities.Ship;
 import btlshp.enums.AppState;
 import btlshp.enums.Direction;
 import btlshp.enums.GraphicAlliance;
 import btlshp.enums.Weapon;
 import btlshp.turns.Turn;
 import btlshp.turns.TurnFactory;
 import btlshp.ui.gridmodes.FireGunMode;
 import btlshp.ui.gridmodes.GridMode;
 import btlshp.ui.gridmodes.MoveMode;
 import btlshp.ui.gridmodes.NormalMode;
 import btlshp.ui.gridmodes.PickupMineMode;
 import btlshp.ui.gridmodes.PlaceMineMode;
 import btlshp.ui.gridmodes.RepairMode;
 import btlshp.ui.gridmodes.RotateMode;
 
 public class GameGrid extends JComponent implements MouseListener, MouseMotionListener {
 	private static final long serialVersionUID = 1038483241713085828L;
 	private static final int  rowHeight = 16, colWidth = 16;
 	
 	public enum GridState {
 		TurnStart,
 		MoveShip,
 		RotateShip,
 		FireGun,
 		FireTorpedo,
 		PlaceMine,
 		PickupMine,
 		RepairConstruct
 	};
 	
 	private HashMap<String, BufferedImage> imageCache;
 	private GridState state;
 	
 	private Dimension size;
 	private Color     gridColor, hoverColor, hoverLineColor;
 	private Color     bgColor, sonarColor, radarColor;
 	private Color     explosionColor, shotColor;
 	
 	private Color     actionAreaColor, actionColor, badColor;
 	
 	private boolean   showHover;
 	private int       hoverx, hovery;
 	
 	private int       gridWidth, gridHeight;
 	
 	private Map       map;
 	private GridMode  mode;
 	public GameGrid() {
 		gridWidth = gridHeight = 30;
 		
 		size = new Dimension(gridWidth * colWidth + 2, gridHeight * rowHeight + 2);
 		
 		setPreferredSize(size);
 		setMinimumSize(size);
 		setMaximumSize(size);
 		
 		addMouseMotionListener(this);
 		addMouseListener(this);
 
 		gridColor = new Color(3, 52, 15);
 		hoverColor = new Color(6, 178, 48);
 		hoverLineColor = new Color(4, 70, 17);
 		
 		bgColor = new Color(2, 20, 6);
 		sonarColor = new Color(0, 90, 90);
 		radarColor = new Color(16, 79, 32);
 		
 		explosionColor = new Color(192, 0, 0);
 		shotColor = new Color(192, 60, 0);
 		
 		actionAreaColor = new Color(58, 113, 36);
 		actionColor = new Color(105, 166, 81);
 		badColor = new Color(193, 90, 50);
 			
 		showHover = true;
 		hoverx = hovery = 0;
 		
 		map = null;
 		state = GridState.TurnStart;
 		
 		imageCache = new HashMap<String, BufferedImage>();
 		mode = null;
 		
 	}
 	
 	
 	/**
 	 * Used to set the map for the grid to display.
 	 * 
 	 * @param map           Map to use
 	 */
 	public void setMap(Map map) {
 		this.map = map;
 		this.state = GridState.TurnStart;
 		this.mode = new NormalMode(this, map);
 		repaint();
 	}
 	public Map getMap(){
 		return map;
 	}
 	
 	/**
 	 * @return The current state of the game.
 	 */
 	public GridState getGameState() {
 		return state;
 	}
 	/**
 	 * Fires a torpedo
 	 * @param ship 
 	 */
 	public void fireTorpedo(Ship ship) {
 		map.fireTorpedo(ship);
 	}
 	
 	
 	private void drawDoubleHorizontalLine(Graphics2D g, int y, int x1, int x2) {
 		g.drawLine(x1, y,   x2, y);
 		g.drawLine(x1, y+1, x2, y+1);
 	}
 	
 	private void drawDoubleVerticalLine(Graphics2D g, int x, int y1, int y2) {
 		g.drawLine(x,     y1, x,     y2);
 		g.drawLine(x + 1, y1, x + 1, y2);
 	}
 	
 	
 	private boolean displayGraphic(MapNode n) {
 		if(n.block == null)
 			return false;
 		
 		if(n.block instanceof MineBlock) 
 			return n.hasSonar();
 		
 		if(n.block.getAlliance() != GraphicAlliance.Unfriendly || n.hasRadar() || n.hasSonar() ||
 				(n.block instanceof ConstructBlock && ((ConstructBlock)n.block).getConstruct() instanceof Base))
 			return true;
 		
 		return false;
 	}
 	
 	
 	
 	private BufferedImage getImage(String imageName) {
 		BufferedImage img = imageCache.get(imageName);
 		if(img == null) {
 			try {
 				img = ImageIO.read(new File("images/mapgraphics/" + imageName + ".png"));
 				imageCache.put(imageName, img);
 			} catch (Exception e) { img = null; }
 		}
 		return img;
 	}
 	
 	
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		Graphics2D g2 = (Graphics2D)g;
 		int        x, y;
 		
 		// Draw basic grid lines.
 		g2.setColor(gridColor);
 		for(y = 0; y <= 30; ++y) {
 			drawDoubleHorizontalLine(g2, y * rowHeight, 0, getWidth() - 1);
 		}
 
 		for(x = 0; x <= 30; ++x) {
 			drawDoubleVerticalLine(g2, x * colWidth, 0, getWidth() - 1);		
 		}
 
 		if(showHover) {
 			int hx = hoverx * colWidth;
 			int hy = hovery * rowHeight;
 			
 			// Highlight row/column
 			g2.setColor(hoverLineColor);
 			drawDoubleVerticalLine(g2, hx,      0, getHeight() - 1);
 			drawDoubleVerticalLine(g2, hx + 16, 0, getHeight() - 1);
 			
 			drawDoubleHorizontalLine(g2, hy,      0, getWidth() - 1);
 			drawDoubleHorizontalLine(g2, hy + 16, 0, getWidth() - 1);
 		}
 		
 		if(map != null) {
 			mode.highlightCells();
 			
 			for(y = 0; y < 30; ++y) {
 				for(x = 0; x < 30; ++x) {
 					MapNode n = map.getMapNode(x, y);
 					Color c = n.badAction() ? badColor : n.actionSquare() ? actionColor :
 					          n.actionArea() ? actionAreaColor :
 					          n.hasExplosion() ? explosionColor :
 					          n.hasShot() ? shotColor :
 						      n.hasSonar() ? sonarColor :
 					          n.hasRadar() ? radarColor : bgColor;
 					
 					g2.setColor(c);
 					g2.fillRect(x * colWidth + 2, y * rowHeight + 2, colWidth - 2, rowHeight - 2);
 					
 					if(displayGraphic(n)) {
 						BufferedImage img = getImage(n.block.getGraphicName());
 						
 						if(img != null)
 							g2.drawImage(img, x * colWidth, y * rowHeight, null);
 					}
 				}
 			}
 		}
 		else {
 			g2.setColor(bgColor);
 
 			for(y = 0; y < 30; ++y) {
 				for(x = 0; x < 30; ++x)
 					g2.fillRect(x * colWidth + 2, y * rowHeight + 2, colWidth - 2, rowHeight - 2);
 			}
 		}
 		
 		if(showHover) {
 			int hx = hoverx * colWidth;
 			int hy = hovery * rowHeight;
 			
 			g2.setColor(hoverColor);
 			drawDoubleHorizontalLine(g2, hy,      hx, hx + 17);
 			drawDoubleHorizontalLine(g2, hy + 16, hx, hx + 17);
 			
 			drawDoubleVerticalLine(g2, hx,      hy + 2, hy + 15);
 			drawDoubleVerticalLine(g2, hx + 16, hy + 2, hy + 15);
 		}
 	}
 	
 	
 	public int getHoverx() {
 		return hoverx;
 	}
 	
 	
 	public int getHovery() {
 		return hovery;
 	}
 	
 	
 	/**
 	 * Cancels the current mode and returns to regular mode.
 	 */
 	public void cancelAction() {
 		state = GridState.TurnStart;
 		mode = new NormalMode(this, map);
 		repaint();
 	}
 	
 	
 	/**
 	 * Begins a move ship action.
 	 * 
 	 * @param s   Ship to move.
 	 */
 	public void startShipMove(Ship s) {
 		state = GridState.MoveShip;
 		mode = new MoveMode(s, this, map);
 		repaint();
 	}
 	
 	
 	/**
 	 * Begins a rotate ship action.
 	 * 
 	 * @param s    Ship to rotate
 	 */
 	public void startShipRotate(Ship s) {
 		state = GridState.RotateShip;
 		mode = new RotateMode(s, this, map);
 		repaint();
 	}
 	
 	/**
 	 * Begins a fire guns ship action.
 	 * 
 	 * @param s    ship to fire guns
 	 */
 	public void startFireGun(Ship s) {
 		if(!s.canFireGun())
 			throw new IllegalStateException();
 		
 		state = GridState.FireGun;
 		mode = new FireGunMode(s, this, map);
 		repaint();
 	}
 	
 	/**
 	 * Begins a place a mine ship action.
 	 * 
 	 * @param s    ship to place mine
 	 */
 	public void startPlaceMine(Ship s) {
 		state = GridState.PlaceMine;
 		mode = new PlaceMineMode(s, this, map);
 		repaint();
 	}
 	
 	/**
 	 * Begins a pickup a mine ship action.
 	 * 
 	 * @param s    ship to fire guns
 	 */
 	public void startPickupMine(Ship s) {
 		state = GridState.PickupMine;
 		mode = new PickupMineMode(s, this, map);
 		repaint();
 	}
 	
 	
 	/**
 	 * Begins a repair ship action.
 	 * 
 	 * @param b    base to conduct the repair.
 	 */
 	public void startRepair(Base b) {
 		state = GridState.RepairConstruct;
 		mode = new RepairMode(b, this, map);
 		repaint();
 	}
 	
 	
 	public int gridLocX(int x) {
 		int res = (x - 1) / colWidth;
 		return res < 0 ? 0 : res >= gridWidth ? gridWidth - 1 : res;
 	}
 	
 	
 	public int gridLocY(int y) {
 		int res = (y - 1) / rowHeight;
 		return res < 0 ? 0 : res >= gridHeight ? gridHeight - 1 : res;
 	}
 
 
 	/**
 	 * Updates the highlighted square in the grid.
 	 * @param mouseLoc    Location the mouse has moved to.
 	 */
 	private void updateHover(Point mouseLoc, boolean forceRepaint) {
 		int x = gridLocX(mouseLoc.x);
 		int y = gridLocY(mouseLoc.y);
 		
 		if(x != hoverx || y != hovery || forceRepaint)
 			repaint();
 		
 		hoverx = x;
 		hovery = y;
 	}
 	
 	
 	@Override
 	public void mouseDragged(MouseEvent ev) {
 		updateHover(ev.getPoint(), false);	
 	}
 
 
 
 	@Override
 	public void mouseMoved(MouseEvent ev) {
 		updateHover(ev.getPoint(), false);	
 	}
 
 
 	@Override
 	public void mouseClicked(MouseEvent ev) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void mouseEntered(MouseEvent ev) {
 		showHover = true;
 		updateHover(ev.getPoint(), true);
 	}
 
 
 	@Override
 	public void mouseExited(MouseEvent ev) {
 		showHover = false;
 		repaint();
 	}
 
 
 	@Override
 	public void mousePressed(MouseEvent ev) {
 	}
 
 
 	@Override
 	public void mouseReleased(MouseEvent ev) {
 		if(map == null || Btlshp.getGame().getAppState() != AppState.LocalTurn)
 			return;
 		
 		mode.mouseClick(ev.getX(), ev.getY());
 	}
 }
