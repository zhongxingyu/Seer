 package UI;
 
 import java.awt.BorderLayout;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.image.RescaleOp;
 import java.awt.event.MouseMotionListener;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import menu.MainFrame;
 import networking.common.Network;
 
 import logic.FileReader;
 import logic.GameUpdate;
 import logic.Logic;
 import logic.UpdateThread;
 import sound.AudioPlayer;
 import sound.MixingDesk;
 import state.Direction;
 import state.Ramp;
 import state.Structure;
 import state.StructureType;
 import state.Task;
 import state.Tile;
 import state.World;
 import util.UIImageStorage;
 
 //TODO Rotate the view by inverting the draw
 //TODO Hovering over the screen will show a tempory bit on the screen
 
 @SuppressWarnings("serial")
 public class Window extends JPanel implements KeyListener, MouseListener,
 		MouseMotionListener, MouseWheelListener {
 
 	MainFrame frame = null;
 	public void setFrame(MainFrame frame) { this.frame = frame; }
 
 	// mouse x y points on a click
 	private int mouseX = 0;
 	private int mouseY = 0;
 
 	// used for buttons being pushed (direction keys)
 	boolean up = false;
 	boolean down = false;
 	boolean left = false;
 	boolean right = false;
 	private Logic logic;
 
 	private boolean drawTransparent = true;
 
 	Random random = new Random();
 
 	Display display;
 	UpdateThread update;
 
 	public long seed;
 	public Network network;
 	public String fileMap = "resources/map2";
 
 	private MixingDesk mixingDesk;
 
 	private Tile selectedTile1;
 	private Tile selectedTile2;
 
 	public Window() {
 		// startAudio(thread);
 		initialize();
 		// logic = new Logic(display.getWorld());
 	}
 
 	/**
 	 * @param seed
 	 * @param network
 	 * @param fileMap
 	 */
 	public Window(long seed, Network network, String fileMap,
 			MixingDesk mixingDesk) {// TODO //mapfile tpye?
 
 		this.mixingDesk = mixingDesk;
 
 		this.seed = seed;
 		this.network = network;
 		fileMap = this.fileMap;
 
 		// TODO
 		// load map from file given
 		// store network as field
 		// use seed to generate any random events
 		initialize();
 	}
 
 	/**
 	 * Returns one of two random tiles.
 	 *
 	 * @return String
 	 */
 	public String generateRandomTile() {
 		if (random.nextInt(2) == 1)
 			return "tile";
 		else
 			return "tile0";
 	}
 
 	public void initialize() {
 		// Was code to randomly generate a map. Replaced now by reading a map
 		// from a file.
 		// Tile[][] map = FileReader.readMap("resources/map");
 
 		// for(int i = 0; i < 200; i++){
 		// for(int j = 0; j < 200; j++){
 		// map[i][j] = new Tile(generateRandomTile());
 		// }
 		// }
 
 		// set up menu
 
 		// Create a new world with the map read from the file.
 		GameUpdate initialUpdate = new GameUpdate();
 		World world = new World(FileReader.readMap(fileMap), initialUpdate);// resources/map
 		display = new Display(world); // was just new World()
 		FileReader.setStructures(world); // Set up the structures that the file
 											// reader now knows about
 
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		addMouseWheelListener(this);
 		addKeyListener(this);
 		setFocusable(true);
 
 		this.setLayout(new BorderLayout());
 		this.add(display, BorderLayout.CENTER);
 		update = new UpdateThread(world, display, network, initialUpdate);
 		update.start();
 
 		UIImageStorage.add("ButtonHealthOn");
 		UIImageStorage.add("ButtonMuteOn");
 		UIImageStorage.add("ButtonAddDude");
 		UIImageStorage.add("ButtonBGOn");
 
 		UIImageStorage.add("ButtonHealthOff");
 		UIImageStorage.add("ButtonMuteOff");
 		UIImageStorage.add("ButtonAddDudeHover");
 		UIImageStorage.add("ButtonBGOff");
 
 		UIImageStorage.add("IconCrystal");
 		UIImageStorage.add("IconPlants");
 		UIImageStorage.add("IconWood");
 		// setup audio
 
 		if (mixingDesk != null) {
 
 
 			mixingDesk.stopAudio();
 			mixingDesk.addAudioPlayer("InGameMusic.wav", false);
 			world.setAudioPlayer(this.mixingDesk);
 			// audioPlayer.start();
 		}
 
 	}
 
 	/**
 	 * Draws a basic graphic pane needs actual graphical outlines and suchlike
 	 * -Outdated-
 	 *
 	 * @param Graphics
 	 */
 	public void paint(Graphics g) {
 		super.paint(g);
 	}
 
 	private void panMap() {
 		// Pans the map by 1 tile but only while direction keys are currently
 		// being held down
 		if (up)
 			switch (display.getRotation()) {
 			case 0:
 				display.panUp(1);
 				break;
 			case 1:
 				display.panLeft(1);
 				break;
 			case 2:
 				display.panDown(1);
 				break;
 			case 3:
 				display.panRight(1);
 				break;
 			}
 		if (down)
 			switch (display.getRotation()) {
 			case 2:
 				display.panUp(1);
 				break;
 			case 3:
 				display.panLeft(1);
 				break;
 			case 0:
 				display.panDown(1);
 				break;
 			case 1:
 				display.panRight(1);
 				break;
 			}
 		if (right)
 			switch (display.getRotation()) {
 			case 1:
 				display.panUp(1);
 				break;
 			case 2:
 				display.panLeft(1);
 				break;
 			case 3:
 				display.panDown(1);
 				break;
 			case 0:
 				display.panRight(1);
 				break;
 			}
 		if (left)
 			switch (display.getRotation()) {
 			case 3:
 				display.panUp(1);
 				break;
 			case 0:
 				display.panLeft(1);
 				break;
 			case 1:
 				display.panDown(1);
 				break;
 			case 2:
 				display.panRight(1);
 				break;
 			}
 	}
 
 	public static void main(String[] args) {
 		JFrame f = new JFrame("Octo Centauri");
 		f.getContentPane().add(new Window());
 		// f.add(new Window());
 		f.setSize(1920, 1080);
 		f.pack();
 		f.setVisible(true);
 
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 
 	boolean toggleRes = false;
 	@Override
 	public void keyTyped(KeyEvent e) {
 		int code = e.getKeyCode();
 //		switch (code) {
 //		case KeyEvent.VK_T:
 //			display.toggleResRegen(toggleRes);
 //			toggleRes = !toggleRes;
 //			break;
 //		default:
 //			break;
 //		}
 	}
 
 	// gets key events for panning possibly add shortcuts
 	@Override
 	public void keyPressed(KeyEvent e) {
 		int code = e.getKeyCode();
 
 		switch (code) {
 		case KeyEvent.VK_RIGHT:
 		case KeyEvent.VK_KP_RIGHT:
 		case KeyEvent.VK_D:
 			right = true;
 			break;
 		case KeyEvent.VK_LEFT:
 		case KeyEvent.VK_KP_LEFT:
 		case KeyEvent.VK_A:
 			left = true;
 			break;
 		case KeyEvent.VK_UP:
 		case KeyEvent.VK_KP_UP:
 		case KeyEvent.VK_W:
 			up = true;
 			break;
 		case KeyEvent.VK_DOWN:
 		case KeyEvent.VK_KP_DOWN:
 		case KeyEvent.VK_S:
 			down = true;
 			break;
 		case KeyEvent.VK_F:
 			display.toggleStruct();
 			break;
 		case KeyEvent.VK_T:
 			display.toggleResRegen(toggleRes);
 			break;
 		default:
 				break;
 		}
 		panMap();
 		repaint();
 		// display.repaint();
 	}
 
 	// disables a given pan direction
 	@Override
 	public void keyReleased(KeyEvent e) {
 
 		int code = e.getKeyCode();
 
 		switch (code) {
 		case KeyEvent.VK_RIGHT:
 		case KeyEvent.VK_KP_RIGHT:
 		case KeyEvent.VK_D:
 			right = false;
 			break;
 		case KeyEvent.VK_LEFT:
 		case KeyEvent.VK_KP_LEFT:
 		case KeyEvent.VK_A:
 			left = false;
 			break;
 		case KeyEvent.VK_UP:
 		case KeyEvent.VK_KP_UP:
 		case KeyEvent.VK_W:
 			up = false;
 			break;
 		case KeyEvent.VK_DOWN:
 		case KeyEvent.VK_KP_DOWN:
 		case KeyEvent.VK_S:
 			down = false;
 			break;
 		case KeyEvent.VK_R:
 			display.rotate();
 			break;
 		case KeyEvent.VK_ESCAPE:
 			if (JOptionPane.showConfirmDialog(null,  "Do you really want to quit?") == 0)
 				frame.close();
 			break;
 		}
 	}
 
 
 	// mouse commands, awaiting some level of world to play with
 	@Override
 	public void mousePressed(MouseEvent e) {
 		boolean onUI = false;
 		Set<Rectangle> UISpace = display.getUISpace();
 		Point p = e.getPoint();
 		for (Rectangle uiSquare : UISpace) {
 			if (uiSquare.contains(p)) {
 				onUI = true;
 				break;
 			}
 		}
 
 		if (onUI) {
 
 		} else {
 
 			Point point = display.displayToTileCoordinates(e.getX(), e.getY());
 			if (0 == (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK)) {
 				if (e.getButton() == 3)
 				{
 					Tile tile = display.getWorld().getTile(point.x,point.y);
 
 					Structure s = new Structure((int) point.getX(),
 							(int) point.getY(), 0, 1,
 							"Assets/EnvironmentTiles/" + "/BarrenGrass"
 									+ ".png");
 
 					/*
 					 * Copied from Java tutorial. Create a rescale filter op
 					 * that makes the image 50% opaque.
 					 */
 					float[] scales = { 1f, 1f, 1f, 0.5f };
 					float[] offsets = new float[4];
 					RescaleOp rop = new RescaleOp(scales, offsets, null);
 					s.setFilter(rop);
 
 					display.getWorld().addStructure(s);
 					Task t = new Task(tile, "dig");
 					display.getWorld().addTask(t);
 				}
 				else if (0 != (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK))
 				{
 					Structure s = display.getWorld()
 							.getTile(point.x, point.y).getStructure();
 					if (s instanceof Ramp) {
 						((Ramp) s)
 								.setDirection(Direction.values()[(((Ramp) s)
 										.getDirection().ordinal() + 1) % 4]);
 					} else {
 						//TODO
 
 //						display.getWorld().tasks.add(new Task(display.getWorld().getTile((int) point.getX(), (int) point.getY()),
 //								"buildTile","PathRamp"));
 
 						display.getWorld().addStructure(
 								new Ramp(point.x, point.y, 1, 1,
 										"PathRamp",
 										Direction.values()[(display
 												.getRotation() + 2) % 4]));
 					}
 					display.getWorld().getLogic().mapChanged(point.x, point.y);
 			}
 				else if (0 != (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)) {
 
 					// set tile to be somthing
 					// if (e.getButton() == 3) {
 					// Dude d = new Dude("")
 					// Tile t = new Tile("Grass", 0, (int) point.getX(),
 					// (int) point.getY());
 					// display.getWorld().setTile((int) point.getX(),
 					// (int) point.getY(), t);
 				} else {
 
 
 					if (!display.getWorld().getBuildType())
 					{
 						String currentBuild = display.getWorld().getCurrentBuild();
 						if(display.getWorld().hasResources(currentBuild))
 						{
 							display.getWorld().tasks.add(new Task(display.getWorld().getTile((int) point.getX(), (int) point.getY()),
 														"buildTile",currentBuild));// TODO
 
 							Structure s = new Structure((int) point.getX(),
 									(int) point.getY(), 1, 1,
 									"Assets/EnvironmentTiles/"+currentBuild+".png");
 
 							/*
 							 * Copied from Java tutorial. Create a rescale filter op
 							 * that makes the image 50% opaque.
 							 */
 							float[] scales = { 1f, 1f, 1f, 0.5f };
 							float[] offsets = new float[4];
 							RescaleOp rop = new RescaleOp(scales, offsets, null);
 							s.setFilter(rop);
 
 							display.getWorld().addStructure(s);
 						}
 					}
 					else if (display.getWorld().getBuildType())
 					{
 						String currentStruct = display.getWorld().getCurrentStruct();
 						if(display.getWorld().hasResources(currentStruct))
 						{
<<<<<<< HEAD
 							display.getWorld().tasks.add(new Task(display.getWorld().getTile((int) point.getX(), (int) point.getY()),
=======
							display.getWorld().addTask(new Task(display.getWorld().getTile((int) point.getX(), (int) point.getY()),
>>>>>>> 74c8cda99549e2aec21e4edfa538234d0c295989
 														"buildStructure",currentStruct));// TODO
 
 							Structure s = (StructureType.getTypes().get(currentStruct).create(point.x, point.y));
 
 							/*
 							 * Copied from Java tutorial. Create a rescale filter op
 							 * that makes the image 50% opaque.
 							 */
 							float[] scales = { 1f, 1f, 1f, 0.5f };
 							float[] offsets = new float[4];
 							RescaleOp rop = new RescaleOp(scales, offsets, null);
 							s.setFilter(rop);
 
 							display.getWorld().addStructure(s);
 
 							// TODO make tile greyed out, ask man to dig it;
 						}
 
 				}
 				}
 			}
 		}
 
 	this.repaint();
 	}
 
 	@Override
 	public void mouseWheelMoved(MouseWheelEvent e) {
 
 		boolean onUI = false;
 		Set<Rectangle> UISpace = display.getUISpace();
 		Point p = e.getPoint();
 		for (Rectangle uiSquare : UISpace) {
 			if (uiSquare.contains(p)) {
 				onUI = true;
 				break;
 			}
 		}
 		if (onUI) {
 
 		} else {
 			Point point = display.displayToTileCoordinates(e.getX(), e.getY());
 			Tile t = display.getWorld().getTile(point.x, point.y);
 			if (t != null) {
 			t.setHeight(t.getHeight() - e.getWheelRotation());
 			display.getWorld().getLogic().mapChanged(point.x, point.y);
 			}
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		Point p = e.getPoint();
 		boolean onUI = false;
 		Set<Rectangle> UISpace = display.getUISpace();
 		for (Rectangle uiSquare : UISpace) {
 			if (uiSquare.contains(p)) {
 				onUI = true;
 				break;
 			}
 		}
 
 		if (onUI) {
 			Map<String, Rectangle> toggleMap = display.getToggleMap();
 			for (String key : toggleMap.keySet()) {
 				if (toggleMap.get(key).contains(p)) {
 					display.buttonClicked(key).mouseClicked(e);
 				}
 			}
 
 		} else {
 			// map clicked here
 		}
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		// TODO Auto-generated method stubTuple
 
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 
 		boolean onUI = false;
 		Set<Rectangle> UISpace = display.getUISpace();
 		Point p = e.getPoint();
 		for (Rectangle uiSquare : UISpace) {
 			if (uiSquare.contains(p)) {
 				onUI = true;
 				break;
 			}
 		}
 
 		if (onUI) {
 
 			display.unHighlightTile();
 		} else {
 			Point tilePt = display.displayToTileCoordinates(e.getX(), e.getY());
 
 			display.setHighlightedTile(tilePt.x, tilePt.y);
 		}
 	}
 
 	public void startAudio() {
 
 	}
 
 }
