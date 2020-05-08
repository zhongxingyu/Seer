 package riskyspace.view.swingImpl;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.KeyAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JPanel;
 
 import riskyspace.model.Player;
 import riskyspace.model.Position;
 import riskyspace.model.Resource;
 import riskyspace.model.ShipType;
 import riskyspace.model.World;
 import riskyspace.services.Event;
 import riskyspace.services.EventBus;
 import riskyspace.services.EventHandler;
 import riskyspace.view.Clickable;
 import riskyspace.view.camera.Camera;
 import riskyspace.view.camera.CameraController;
 import riskyspace.view.menu.ColonyMenu;
 import riskyspace.view.menu.IMenu;
 import riskyspace.view.menu.RecruitMenu;
 
 public class RenderArea extends JPanel implements EventHandler {
 
 	private static final long serialVersionUID = 8209691542499926289L;
 	
 	/**
 	 * Extra space at the Right and Left sides
 	 * of the screen.
 	 */
 	private static final int EXTRA_SPACE_HORIZONTAL = 3;
 	
 	/**
 	 * Extra space at the Top and Bottom sides
 	 * of the screen.
 	 */
 	private static final int EXTRA_SPACE_VERTICAL = 2;
 	
 	private final World world;
 	
 	/*
 	 * Cameras
 	 */
 	private Camera currentCamera = null;
 	private Map<Player, Camera> cameras = null;
 	private CameraController cc = null;
 	
 	/*
 	 * Side menu settings
 	 */
 	private IMenu colonyMenu = null;
 	private IMenu recruitMenu = null;
 	
 	/*
 	 * Screen measures
 	 */
 	private int width;
 	private int height;
 	private int squareSize;
 	
 	/*
 	 * BufferedImage background
 	 */
 	private BufferedImage background = null;
 	
 	/*
 	 * Planet Textures
 	 */
 	private Map<Integer, Image> metalplanets = new HashMap<Integer, Image>();
 	private Map<Integer, Image> gasplanets = new HashMap<Integer, Image>();
 	private Map<Position, Image> planetTextures = new HashMap<Position, Image>();
 	/*
 	 * Planet Positions to draw planet textures
 	 */
 	private List<Position> planetPositions = new ArrayList<Position>();
 	
 	/*
 	 * Ship Textures
 	 */
 	private Map<String, Image> shipTextures = new HashMap<String, Image>();
 	
 	public RenderArea(World world) {
 		this.world = world;
 		measureScreen();
 		setTextures();
 		createBackground();
 		savePlanets();
 		initCameras();
 		createMenus();
 		EventBus.INSTANCE.addHandler(this);
 		addMouseListener(new ClickHandler());
 	}
 	
 	private void savePlanets() {
 		for (Position pos : world.getContentPositions()) {
 			if (world.getTerritory(pos).hasPlanet()) {
 				if (world.getTerritory(pos).getPlanet().getType() == Resource.METAL) {
 					planetTextures.put(pos, metalplanets.get((int) (Math.random()*4)));
 				} else {
 					planetTextures.put(pos, gasplanets.get((int) (Math.random()*3)));
 				}
 				planetPositions.add(pos);
 			}
 		}
 	}
 
 	private void createMenus() {
 		int menuWidth = height / 3;
 		colonyMenu = new ColonyMenu(width - menuWidth, 0, menuWidth, height);
 		recruitMenu = new RecruitMenu(width - menuWidth, 0, menuWidth, height);
 	}
 
 	private void setTextures() {
 		/*
 		 * Planets
 		 */
 		metalplanets.put(0, Toolkit.getDefaultToolkit().getImage("res/icons/planets/metalplanet_0.png").getScaledInstance(squareSize/2, squareSize/2, Image.SCALE_DEFAULT));
 		metalplanets.put(1, Toolkit.getDefaultToolkit().getImage("res/icons/planets/metalplanet_1.png").getScaledInstance(squareSize/2, squareSize/2, Image.SCALE_DEFAULT));
 		metalplanets.put(2, Toolkit.getDefaultToolkit().getImage("res/icons/planets/metalplanet_2.png").getScaledInstance(squareSize/2, squareSize/2, Image.SCALE_DEFAULT));
 		metalplanets.put(3, Toolkit.getDefaultToolkit().getImage("res/icons/planets/metalplanet_3.png").getScaledInstance(squareSize/2, squareSize/2, Image.SCALE_DEFAULT));
 		gasplanets.put(0, Toolkit.getDefaultToolkit().getImage("res/icons/planets/gasplanet_0.png").getScaledInstance(squareSize/2, squareSize/2, Image.SCALE_DEFAULT));
 		gasplanets.put(1, Toolkit.getDefaultToolkit().getImage("res/icons/planets/gasplanet_1.png").getScaledInstance(squareSize/2, squareSize/2, Image.SCALE_DEFAULT));
 		gasplanets.put(2, Toolkit.getDefaultToolkit().getImage("res/icons/planets/gasplanet_2.png").getScaledInstance(squareSize/2, squareSize/2, Image.SCALE_DEFAULT));
 		
 		/*
 		 * Ships Blue Player
 		 */
 		shipTextures.put("SCOUT_BLUE", Toolkit.getDefaultToolkit().getImage("res/icons/blue/scout.png"));
 		shipTextures.put("HUNTER_BLUE", Toolkit.getDefaultToolkit().getImage("res/icons/blue/hunter.png"));
 		shipTextures.put("DESTROYER_BLUE", Toolkit.getDefaultToolkit().getImage("res/icons/blue/destroyer.png"));
 		shipTextures.put("COLONIZER_BLUE", Toolkit.getDefaultToolkit().getImage("res/icons/blue/colonizer.png"));
 		
 		/*
 		 * Ships Red Player
 		 */
 		shipTextures.put("SCOUT_RED", Toolkit.getDefaultToolkit().getImage("res/icons/red/scout.png"));
 		shipTextures.put("HUNTER_RED", Toolkit.getDefaultToolkit().getImage("res/icons/red/hunter.png"));
 		shipTextures.put("DESTROYER_RED", Toolkit.getDefaultToolkit().getImage("res/icons/red/destroyer.png"));
 		shipTextures.put("COLONIZER_RED", Toolkit.getDefaultToolkit().getImage("res/icons/red/colonizer.png"));
 	}
 	
 	private void createBackground() {
 		int totalWidth = (world.getCols() + 2*EXTRA_SPACE_HORIZONTAL)*squareSize;
 		int totalHeight = (world.getRows() + 2*EXTRA_SPACE_VERTICAL)*squareSize;
 		background = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
 		Graphics2D g2D = background.createGraphics();
 		
 		g2D.setColor(Color.BLACK);
 		g2D.fillRect(0, 0, totalWidth, totalHeight);
 		
 		g2D.setColor(Color.LIGHT_GRAY);
 		/*
 		 * Draw Horizontal lines
 		 */
 		for (int row = 0; row <= world.getRows(); row++) {
 			int x1 = (EXTRA_SPACE_HORIZONTAL) * squareSize;
 			int y1 = (EXTRA_SPACE_VERTICAL + row) * squareSize;
 			int x2 = (EXTRA_SPACE_HORIZONTAL + world.getCols()) * squareSize;
 			int y2 = (EXTRA_SPACE_VERTICAL + row) * squareSize;
 			g2D.drawLine(x1, y1, x2, y2);
 		}
 		/*
 		 * Draw Vertical lines
 		 */
 		for (int col = 0; col <= world.getCols(); col++) {
 			int x1 = (EXTRA_SPACE_HORIZONTAL + col) * squareSize;
 			int y1 = (EXTRA_SPACE_VERTICAL) * squareSize;
 			int x2 = (EXTRA_SPACE_HORIZONTAL + col) * squareSize;
 			int y2 = (EXTRA_SPACE_VERTICAL + world.getRows()) * squareSize;
 			g2D.drawLine(x1, y1, x2, y2);
 		}
 		
 		/*
 		 * Draw stars
 		 */
 		g2D.setColor(Color.WHITE);
 		for (int i = 0; i < 5000; i++) {
 			int x = (int) (Math.random()*(world.getCols()+2*EXTRA_SPACE_HORIZONTAL)*squareSize);
 			int y = (int) (Math.random()*(world.getRows()+2*EXTRA_SPACE_VERTICAL)*squareSize);
 			g2D.fillRect(x, y, 1, 1);
 		}
 	}
 	
 	private void initCameras() {
 		cameras = new HashMap<Player, Camera>();
 		cameras.put(Player.BLUE, new Camera(0.95f,0.95f));
 		cameras.put(Player.RED, new Camera(0.05f,0.05f));
 		currentCamera = cameras.get(Player.BLUE);
 		cc = new CameraController();
 		cc.setCamera(currentCamera);
 		cc.start();
 	}
 	
 	/*
 	 * Measure the screen and set the squareSize
 	 */
 	public void measureScreen() {
 		width = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
 		height = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
 		squareSize = Math.min(width/6,height/6);
 	}
 	
 	public void setPlayer(Player player) {
 		currentCamera = cameras.get(player);
 		cc.setCamera(currentCamera);
 	}
 	
 	public int translateRealX() {
 		return (int) (((world.getCols()+2*EXTRA_SPACE_HORIZONTAL)*squareSize - width)*currentCamera.getX());
 	}
 	
 	public int translateRealY() {
 		return (int) (((world.getRows()+2*EXTRA_SPACE_VERTICAL)*squareSize - height)*currentCamera.getY());
 	}
 
 	public void paintComponent(Graphics g) {
 		/*
 		 * Translate with cameras
 		 */
 		int xTrans = -(int) (((world.getCols()+2*EXTRA_SPACE_HORIZONTAL)*squareSize - width)*currentCamera.getX());
 		int yTrans = -(int) (((world.getRows()+2*EXTRA_SPACE_VERTICAL)*squareSize - height)*currentCamera.getY());
 		g.translate(xTrans, yTrans);
 		
 		// Draw background
 		g.drawImage(background, 0, 0, null);
 		
 		// Draw Colony Marker
 		for (Position pos : world.getContentPositions()) {
 			if (world.getTerritory(pos).hasColony()) {
 				g.setColor(world.getTerritory(pos).getColony().getOwner() == Player.BLUE ?
 						Color.BLUE : Color.RED);
 				g.fillOval((int) ((EXTRA_SPACE_HORIZONTAL + pos.getCol() - 0.5) * squareSize - 2),
 						(int) ((EXTRA_SPACE_VERTICAL + pos.getRow() - 1) * squareSize + 2),
 						squareSize/2 - 5, squareSize/2 - 5);
 			}
 			
 		}
 		
 		// Draw Planets
 		for (Position pos : planetPositions) {
 			g.drawImage(planetTextures.get(pos), (int) ((EXTRA_SPACE_HORIZONTAL + pos.getCol() - 0.5) * squareSize - 4),
 					(int) ((EXTRA_SPACE_VERTICAL + pos.getRow() - 1) * squareSize), null);
 		}
 		
 		// Draw Paths
 		
 		// Draw Fleets
 		for (Position pos : world.getContentPositions()) {
 			if (world.getTerritory(pos).hasFleet()) {
 				Player controller = world.getTerritory(pos).controlledBy();
 				Image image = null;
 				ShipType flagship = world.getTerritory(pos).getFleetsFlagships();
 				image = controller == Player.BLUE ? shipTextures.get(flagship + "_BLUE") : shipTextures.get(flagship + "_RED");
 				g.drawImage(image, (int) ((EXTRA_SPACE_HORIZONTAL + pos.getCol() - 0.75) * squareSize) - image.getWidth(null)/2,
 						(int) ((EXTRA_SPACE_VERTICAL + pos.getRow() - 0.25) * squareSize) - image.getWidth(null)/2, null);
 				if (world.getTerritory(pos).containsColonizer()) {
 					image = controller == Player.BLUE ? shipTextures.get("COLONIZER_BLUE") : shipTextures.get("COLONIZER_RED");
					g.drawImage(image, (int) ((EXTRA_SPACE_HORIZONTAL + pos.getCol() + 0.75) * squareSize) - image.getWidth(null)/2,
 							(int) ((EXTRA_SPACE_VERTICAL + pos.getRow() - 0.25) * squareSize) - image.getWidth(null)/2, null);
 				}
 			}
 		}
 		
 		// Draw menu
 		g.translate(-xTrans, -yTrans);
 		if (colonyMenu.isVisible()) {
 			colonyMenu.draw(g);
 		}
 		if (recruitMenu.isVisible()) {
 			recruitMenu.draw(g);
 		}
 		// draw next btn
 //		next.draw(g);
 	}
 	
 	public Position getPosition(Point point) {
 		int row = ((point.y + translateRealY()) / squareSize) + 1 - EXTRA_SPACE_VERTICAL;
 		int col = ((point.x  + translateRealX()) / squareSize) + 1 - EXTRA_SPACE_HORIZONTAL;
 		return new Position(row, col); 
 	}
 	
 	private boolean isLegalPos(Position pos) {
 		boolean rowLegal = pos.getRow() >= 1 && pos.getRow() <= world.getRows();
 		boolean colLegal = pos.getCol() >= 1 && pos.getCol() <= world.getCols();
 		return rowLegal && colLegal;
 	}
 
 	/*
 	 * Click handling for different parts
 	 */
 	public boolean menuClick(Point point) {
 		if (colonyMenu.isVisible()) {
 			if (colonyMenu instanceof Clickable) {
 				return ((Clickable) colonyMenu).mousePressed(point);
 			}
 		}
 		
 		if (recruitMenu.isVisible()) {
 			if (recruitMenu instanceof Clickable) {
 				return ((Clickable) recruitMenu).mousePressed(point);
 			}
 		}
 		
 		return false;
 	}
 	
 	public boolean shipClick(MouseEvent me) {
 		Point point = me.getPoint();
 		Position pos = getPosition(point);
 		if (isLegalPos(pos)) {
 			int dX = (point.x + translateRealX()) % squareSize;
 			int dY = (point.y + translateRealY()) % squareSize;
 			if (world.getTerritory(pos).hasFleet()) {
				if (dX <= squareSize/2 && dY >= squareSize/2) {
 					if (me.isShiftDown()) {
 						Event evt = new Event(Event.EventTag.ADD_FLEET_SELECTION, pos);
 						EventBus.INSTANCE.publish(evt);
 					} else {
 						Event evt = new Event(Event.EventTag.NEW_FLEET_SELECTION, pos);
 						EventBus.INSTANCE.publish(evt);
 					}
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public boolean colonyClick(Point point) {
 		Position pos = getPosition(point);
 		if (isLegalPos(pos)) {
 			if (world.getTerritory(pos).hasColony()) {
 				Event evt = new Event(Event.EventTag.COLONY_SELECTED, pos);
 				EventBus.INSTANCE.publish(evt);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean pathClick(Point point) {
 		Position pos = getPosition(point);
 		if (isLegalPos(pos)) {
 			Event evt = new Event(Event.EventTag.SET_PATH, pos);
 			EventBus.INSTANCE.publish(evt);
 			return true;
 		}
 		return false;
 	}
 	
 	class ClickHandler implements MouseListener {
 		@Override public void mousePressed(MouseEvent me) {
 			if (me.getButton() == MouseEvent.BUTTON1) {
 				/*
 				 * Check each level of interaction in order.
 				 */
 				if (menuClick(me.getPoint())) {return;}
 				if (shipClick(me)) {return;}
 				if (colonyClick(me.getPoint())) {return;}
 				else {
 					/*
 					 * Click was not in any trigger zone. Call deselect.
 					 */
 					EventBus.INSTANCE.publish(new Event(Event.EventTag.DESELECT, null));
 				}
 			} else if (me.getButton() == MouseEvent.BUTTON3) {
 				if (pathClick(me.getPoint())) {return;}
 				else {
 					/*
 					 * Click was not in any trigger zone. Call deselect.
 					 */
 					EventBus.INSTANCE.publish(new Event(Event.EventTag.DESELECT, null));
 				}
 			}
 		}		
 		@Override public void mouseClicked(MouseEvent me) {}
 		@Override public void mouseEntered(MouseEvent me) {}
 		@Override public void mouseExited(MouseEvent me) {}
 		@Override public void mouseReleased(MouseEvent me) {}
 	}
 
 	@Override
 	public void performEvent(Event evt) {
 		//TODO:
 	}
 }
