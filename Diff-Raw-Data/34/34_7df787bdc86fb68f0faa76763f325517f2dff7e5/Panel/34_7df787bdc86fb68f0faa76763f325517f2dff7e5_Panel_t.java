 package game;
 
 import java.awt.*;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 
 import javax.swing.*;
 
 public class Panel extends JPanel implements MouseMotionListener, Runnable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 7712717603765541381L;
 
 	private static final String IMS_INFO = "imsInfo.txt";
 
 	protected int pWidth = 900; // size of panel
 	protected int pHeight = 200;
 
 	private static final int NO_DELAYS_PER_YIELD = 16;
 
 	private static final int MAX_FRAME_SKIPS = 5;
 	private int mouseX, mouseY;
 	protected int pCenterX = pWidth / 2;
 	protected int pCenterY = pHeight / 2;
 	private Robot robot;
 
 	private long period; // the amount of time between animate. In nanosec
 
 	// private GameRunner gameTop; //Don't total know what this is for.
 	//
 	private volatile boolean gameOver = false;
 	private volatile boolean isPaused = false;
 	private Thread animator;
 	private volatile boolean running = false; // used to stop the animation thread
 	private long gameStartTime; // when the game started
 	private Graphics dbg;
 	
 	// May end up putting this in sort of a global class that all can access
 	private Pathfinder pathfinder;
 	private MapRender map;
 	private Input input;
 	private Image dbImage = null;
 	private Grid grid;
 	private Dwarfs dwarfs;
 
 	public Panel(GameRunner gr, long period) {
 		this.period = period;
 
 		setDoubleBuffered(false);
 		setPreferredSize(new Dimension(pWidth, pWidth));
 		setBackground(Color.BLACK);
 		setFocusable(true);
 		requestFocus(); // the JPanel now has focus, so receives key events
 
 		addKeyListener(new KeyAdapter() {
 			public void keyPressed(KeyEvent e) {
 				processKey(e);
 			}
 		});
 		addMouseListener(new MouseAdapter() {
 			public void mouseReleased(MouseEvent e) {
 				processMouse(e);
 			}
 
 		});
 
 		addMouseMotionListener(this);
 		// Basically every thing uses grid
 		ImagesLoader imsLoader = new ImagesLoader(IMS_INFO);
 		grid = new Grid();
 		map = new MapRender(imsLoader, grid);
 		dwarfs = new Dwarfs(imsLoader, grid);
 		Dwarfs.Dwarf dwarf = dwarfs.new Dwarf(460, 46);
 		dwarfs.saveDwarf(dwarf);
 		dwarf = dwarfs.new Dwarf(460, 69);
 		map.changeElement(460, 69, 64);
 		map.changeElement(460, 46, 64);
 		dwarfs.saveDwarf(dwarf);
 		pathfinder = new Pathfinder(map, grid);
 		input = new Input(map, dwarfs, pathfinder, grid, this);
 
 	}
 
 	public void mouseMoved(MouseEvent e) {
 
 		int x = e.getX() - pCenterX;
 		int y = e.getY() - pCenterY;
 		
 	
 		if (!isPaused){
 			if (mouseX < 0){
 				mouseX = 0;
 			}else if (mouseX > pWidth){
 				mouseX = pWidth;
 			}else if (mouseY < 0){
 				mouseY = 0;
 			}else if (mouseY > pHeight){
 				mouseY = pHeight;
 			}else{
 				mouseX += x;
 				mouseY += y;
 			}
 				
 
 //			System.out.println("===========================");
 //			System.out.println(x);
 //			System.out.println(y);
 //			
 //			System.out.println("============ Height and width ===========");
 //			System.out.println(pHeight);
 //			System.out.println(pWidth);
 //			System.out.println("======== Mouse X ===============");
 //			System.out.println(mouseX);
 //			System.out.println(mouseY);
 //			System.out.println("======== e.get ==================");
 //			System.out.println("e.getY, e.getX " + e.getY() + ", " + e.getX());
 //			System.out.println("========== Panel Center X =========");
 //			System.out.println(pCenterX);
 //			System.out.println(pCenterY);
 			
 			// Find the center of the Panel
			int mouseCenterX = pCenterX + getLocationOnScreen().x;
			int mouseCenterY = pCenterY + getLocationOnScreen().y;
 			
 			
 //			System.out.println("======= Mouse Center ===========");
 //			System.out.println(mouseCenterX);
 //			System.out.println(mouseCenterY);
 			
 			try {
 				robot = new Robot();
 				robot.mouseMove(mouseCenterX, mouseCenterY);
 			} catch (AWTException e1) {
 				e1.printStackTrace();
 			}
 		}
 
 	}
 
 	private void processMouse(MouseEvent e)
 	// If some one clicks the mouse
 	{
 		if (!isPaused){
 
 			if (e.getButton() == 1) {
 				input.leftClick(mouseX, mouseY);
 			}
 			if (e.getButton() == 3) {
 				input.rightClick(mouseX, mouseY);
 
 			}
 		}
 
 	}
 
 	public void mouseDragged(MouseEvent e) {
 
 	}
 	
 	protected void showMouse(){
 		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 	}
 	
 	protected void hideMouse(){
 		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
 		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
 			    cursorImg, new Point(0, 0), "blank cursor");
 		
 		setCursor(blankCursor);
 		
 	}
 
 	// All the keyboard input is handle here
 	private void processKey(KeyEvent e)
 	// handles termination and game-play keys
 	{
 		int keyCode = e.getKeyCode();
 
 		// termination keys
 		// listen for esc, q, end, ctrl-c on the canvas to
 		// allow a convenient exit from the full screen configuration
 		if ((keyCode == KeyEvent.VK_ESCAPE) || (keyCode == KeyEvent.VK_Q)
 				|| (keyCode == KeyEvent.VK_END)
 				|| ((keyCode == KeyEvent.VK_C) && e.isControlDown())) {
 			running = false;
 		}else if(keyCode == KeyEvent.VK_P){
 			if (isPaused == false){
 				isPaused = true;
 				showMouse();
 			}else{
 				isPaused = false;
 				hideMouse();
 			}
 			
 		}
 		if (keyCode == KeyEvent.VK_1) {
 			input.changeElement(map.stone());
 		}
 		if (keyCode == KeyEvent.VK_2) {
 			input.changeElement(map.floor());
 		}
 		if (keyCode == 37) {
 			input.nudgeLeft();
 		}
 		if (keyCode == 38) {
 			input.nudgeUp();
 		}
 		if (keyCode == 39) {
 			input.nudgeRight();
 		}
 		if (keyCode == 40) {
 			input.nudgeDown();
 		}
 	} // end of processKey()
 
 	@Override
 	public void addNotify()
 	// wait for the JPanel to be added to the JFrame before starting
 	{
 		super.addNotify(); // creates the peer
 		startGame(); // start the thread
 	}
 
 	private void startGame()
 	// initialise and start the thread
 	{
 		if (animator == null || !running) {
 			animator = new Thread(this);
 			animator.start();
 		}
 	} // end of startGame()
 
 	public void stopGame() {
 		running = false;
 	}
 
 	// Basically if you you want things to move and stuff you call this...
 	private void gameUpdate() {
 		if (!isPaused && !gameOver) {
 			input.changeBoxLocation(mouseX, mouseY);
 			dwarfs.move();
 			input.moveMap(mouseX, mouseY);
 		}
 	} // end of gameUpdate()
 
 	public void run() {
 
 		long beforeTime, afterTime, timeDiff, sleepTime;
 		long overSleepTime = 0L;
 		int noDelays = 0;
 		long excess = 0L;
 
 		gameStartTime = System.nanoTime();
 
 		beforeTime = gameStartTime;
 
 		running = true;
 		while (running) {
 			// The three most important things in the game
 			gameUpdate();
 			gameRender();
 			paintScreen();
 
 			afterTime = System.nanoTime();
 			timeDiff = afterTime - beforeTime;
 			sleepTime = (period - timeDiff) - overSleepTime;
 
 			if (sleepTime > 0) { // some time left in this cycle
 				try {
 					Thread.sleep(sleepTime / 1000000L); // nano -> ms
 				} catch (InterruptedException ex) {
 				}
 				overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
 			} else { // sleepTime <= 0; the frame took longer than the period
 				excess -= sleepTime; // store excess time value
 				overSleepTime = 0L;
 
 				if (++noDelays >= NO_DELAYS_PER_YIELD) {
 					Thread.yield(); // give another thread a chance to run
 					noDelays = 0;
 				}
 			}
 
 			beforeTime = System.nanoTime();
 
 			/*
 			 * If frame animation is taking too long, update the game state
 			 * without rendering it, to get the updates/sec nearer to the
 			 * required FPS.
 			 */
 			int skips = 0;
 			while ((excess > period) && (skips < MAX_FRAME_SKIPS)) {
 				excess -= period;
 				gameUpdate(); // update state but don't render
 				skips++;
 			}
 		}
 		map.save();
 		System.exit(0);
 	}
 
 	public void resizeCanves(int width, int height) {
 		createDBImage(width, height);
 		pHeight = height;
 		pWidth = width;
 		pCenterX = pWidth / 2;
 		pCenterY = pHeight / 2;
 		map.resize(width, height);
 	}
 
 	private void createDBImage(int width, int height)
 	// When you create a db Image it will update all or Make all you variables
 	// for the game to run.
 	{
 		dbImage = createImage(width, height);
 		if (dbImage == null) {
 			System.out.println("dbImage is null");
 			return;
 		} else {
 			dbg = dbImage.getGraphics();
 			dbg.fillRect(0, 0, width, height);
 
 		}
 	}
 
 	private void gameRender() {
 
 		if (dbImage == null) {
 			System.out.println("dbImage is null Building");
 			createDBImage(pWidth, pHeight);
 		}
 		map.draw(dbg);
 		dwarfs.draw(dbg);
 		input.draw(dbg, mouseX, mouseY);
 
 	}
 
 	private void paintScreen()
 	// use active rendering to put the buffered image on-screen
 	{
 		Graphics g;
 		try {
 			g = this.getGraphics();
 			if ((g != null) && (dbImage != null))
 				g.drawImage(dbImage, 0, 0, null);
 
 			// Sync the display on some systems.
 			// (on Linux, this fixes event queue problems)
 			Toolkit.getDefaultToolkit().sync();
 
 			g.dispose();
 		} catch (Exception e) {
 			System.out.println("Graphics context error: " + e);
 		}
 	} // end of paintScreen()
 
 }
