 package gerow.cs200.hw2;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Rectangle2D;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.Random;
 
 import javax.swing.JFrame;
 import javax.swing.Timer;
 
 /**
  * Where most of the execution occurs. This class handles the map and steps
  * through execution appropriately.
  * 
  * @author mike
  * 
  */
 public class Game extends JFrame implements ActionListener, Serializable {
     /**
      * 
      */
     private static final long serialVersionUID = 4903659329744616581L;
     /**
      * The main map for the game.
      */
     private Map map = new Map();
     private int width = 800;
     private int height = 600;
     /**
      * The framerate we will shoot for.
      */
     private int targetFramerate = 33;
     /**
      * A transient variable for another method.
      */
     private double framerate = 30;
     private double lastStartTime = 0;
     private boolean printFramerate = true;
     private boolean paused = false;
     private boolean random = true;
     private boolean gameOver = false;
     private int gameOverScore = 1;
     private Timer t;
     /**
      * The keylistener that will control the player.
      */
     private PlayerController pc;
     private PauseMenuController pauseController;
     private Rectangle2D.Double backgroundRectangle;
     private Rectangle2D.Double darkeningRectangle;
     private Random r = new Random();
     String name;
 
     /**
      * Constructor that spawns the screen and kicks off a lot of timers to keep
      * execution happening properly. It also sets the size of the window and
      * makes it not resizable.
      * 
      * @param name
      */
     public Game(String name) {
 	this.name = name;
 	darkeningRectangle = new Rectangle2D.Double();
 	darkeningRectangle.width = this.width / 2;
 	darkeningRectangle.height = this.height / 2;
 	darkeningRectangle.x = this.width / 4;
 	darkeningRectangle.y = this.height / 4;
 	setTitle("Master Chef's Advanced Chess Simulator: Tactical Edition");
 	// this.setUndecorated(true);
 	MapInterface.setMap(this.map);
 	setSize(this.width, this.height);
 	setLocationRelativeTo(null);
 	setDefaultCloseOperation(EXIT_ON_CLOSE);
 	this.setVisible(true);
 	GameInformation.setGame(this);
 	this.backgroundRectangle = new Rectangle2D.Double(0, 0, this.width,
 		this.height);
 	this.setBounds(0, 0, this.width, this.height);
 	this.setResizable(false);
 	this.pauseController = new PauseMenuController();
 	// Enemy e = new SquidEnemy();
 	// e.setLocation(new Point(620, 160));
 	// this.map.addEnemy(e);
 	// SquidEnemy s = new SquidEnemy();
 	// s.setLocation(new Point(600, 190));
 	// s.setSineWaveSpeed(1.5);
 	// this.map.addEnemy(s);
 	if (this.random) {
 	    this.randomSetup();
 	}
 	this.t = new Timer(1000 / this.targetFramerate, this);
 	t.start();
 	this.createBufferStrategy(2);
     }
 
     /**
      * Get the width of the window
      */
     public int getWidth() {
 	return this.width;
     }
 
     /**
      * Get the height of the window
      */
     public int getHeight() {
 	return this.height;
     }
 
     /**
      * Method used to print the framerate to the screen in a color coded way.
      * 
      * @param g
      */
     public void printFramerate(Graphics2D g) {
 	Font f = new Font("Arial", Font.PLAIN, 12);
 	g.setFont(f);
 	if (this.framerate >= 30) {
 	    g.setColor(Color.green);
 	} else if (this.framerate >= 25) {
 	    g.setColor(Color.orange);
 	} else {
 	    g.setColor(Color.red);
 	}
 	g.drawString(String.valueOf((this.framerate)), this.width - 60, 50);
     }
 
     /**
      * This is run every 1/33 of a second according to a timer. It steps the
      * game logic forward and renders the screen.
      */
     public void run() {
 	// System.out.println("Printing frame");
 	Calendar cal = Calendar.getInstance();
 	double startTime = cal.getTimeInMillis();
 	this.framerate = 1000 / (startTime - this.lastStartTime);
 	if (!this.paused && !this.gameOver) {
 	    this.randomStep(this.map);
 	    this.map.step();
 	}
 	this.repaint();
 	this.lastStartTime = startTime;
 	/*
 	 * while ((frameLength = (cal.getTimeInMillis() - startTime)) < (1000.0
 	 * / this.targetFramerate)) { // System.out.println("Frame length: " +
 	 * frameLength); // System.out.println("Time in millis: " +
 	 * cal.getTimeInMillis()); try { Thread.currentThread();
 	 * Thread.sleep(20); } catch (InterruptedException e) { // TODO
 	 * Auto-generated catch block e.printStackTrace(); } cal =
 	 * Calendar.getInstance(); }
 	 */
     }
 
     /**
      * Controls painting the graphics
      */
     public void paint(Graphics g) {
 	Graphics2D g2 = (Graphics2D) g;
 	// g2.setColor(Color.white);
 	// g2.fill(this.backgroundRectangle);
 	this.map.render(g2);
 	if (this.printFramerate) {
 	    this.printFramerate(g2);
 	}
 	if (this.paused) {
 	    this.printPauseMenu(g2);
 	}
 	this.printName(g2);
 	if (this.gameOver) {
 	    this.printGameOver(g2);
 	}
     }
 
     /**
      * Print the pause menu to the screen.
      * @param g
      */
     public void printPauseMenu(Graphics2D g) {
 	Font f = new Font("Arial", Font.BOLD, 20);
 	g.setColor(new Color(128, 128, 128, 128));
 	g.fill(this.darkeningRectangle);
 	g.setFont(f);
 	g.setColor(Color.WHITE);
 	g.drawString("PAUSED", 355, 175);
     }
 
     /**
      * Pauses the game.  This disables the player controller and enables a pause menu controller.
      */
     public void pause() {
 	if (!this.paused) {
 	    this.removeKeyListener(this.pc);
 	    PauseMenuController p = new PauseMenuController();
 	    this.addKeyListener(p);
 	}
 	this.paused = true;
     }
 
     /**
      * Resume from pause.  Transfers control back over to the appropriate controllers.
      */
     public void resume() {
 	if (this.paused) {
 	    this.removeKeyListener(this.pauseController);
 	    this.addKeyListener(this.pc);
 	    this.paused = false;
 	}
     }
 
     /**
      * Simply calls run in response to the clock.
      */
     public void actionPerformed(ActionEvent e) {
 	this.run();
     }
 
     /**
      * Steps in a random way.  This is in lieu of a level.
      * @param m
      */
     public void randomStep(Map m) {
 	if (m.getFrameNumber() % 10 == 0 && r.nextDouble() < 0.3) {
 	    if (this.r.nextBoolean()) {
 		this.randomSpawnWaveOfStraightEnemies();
 	    } else {
 		this.randomSpawnWaveOfSqidEnemies();
 	    }
 	}
 	if (this.r.nextInt(300) == 1) {
 	    Multiplier5 p = new Multiplier5();
 	    p.setLocation(new Point(r.nextInt(800), r.nextInt(600)));
 	    this.map.addPickup(p);
 	}
     }
 
     /**
      * Sets us up for a random level.
      */
     public void randomSetup() {
 	Player p = new Player();
 	p.setLocation(new Point(375, 500));
 	this.pc = new PlayerController(p);
 	this.addKeyListener(pc);
 	this.map.addPlayer(p);
 	this.map.addBackground(new Background("star_bg.png", 1));
 	this.map.addBackground(new Background("cave_bg.png", 12));
     }
 
     /**
      * THis spawns a random wave of the StraightEnemies
      */
     public void randomSpawnWaveOfStraightEnemies() {
 	int num = r.nextInt(6);
 	int minX = 15;
 	int maxX = this.width - 15;
 	int range = maxX - minX;
 	for (int i = 0; i < num; ++i) {
 	    int x = minX + i * range / num;
 	    int y = 0;
 	    StraightEnemy e = new StraightEnemy(r.nextInt(10) * r.nextDouble()
 		    * (r.nextBoolean() ? -1 : 1), 10 + r.nextInt(5)
 		    * r.nextDouble() + 0.2);
 	    e.setLocation(new Point(x, y));
 	    this.map.addEnemy(e);
 	}
     }
 
     public void randomSpawnWaveOfSqidEnemies() {
 	if (this.r.nextInt(10) == 1) {
 	    SquidEnemy e = new SquidEnemy(this.r.nextInt(6) - 1);
 	    e.setLocation(new Point(400, 10));
 	    this.map.addEnemy(e);
 	}
     }
 
     /**
      * Prints the user's name to the screen.
      * @param g
      */
     public void printName(Graphics2D g) {
 	g.setFont(new Font("Helvetica", Font.PLAIN, 19));
 	g.setColor(Color.white);
 	g.drawString(this.name, 375, 45);
     }
 
     /**
      * Save this object according to the user's name.
      */
     public void save() {
 	FileOutputStream fos = null;
 	try {
 	    fos = new FileOutputStream("saves/" + this.name + ".save");
 	} catch (FileNotFoundException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 	ObjectOutputStream s = null;
 	try {
 	    s = new ObjectOutputStream(fos);
 	} catch (IOException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 	try {
 	    s.writeObject(this);
 	} catch (IOException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 
     }
 
     /**
      * This is used after a serialized load in order to kick off our clocks and set our static objects appropriately.
      */
     public void kickoff() {
 	setSize(this.width, this.height);
 	setLocationRelativeTo(null);
 	setDefaultCloseOperation(EXIT_ON_CLOSE);
 	this.setVisible(true);
 	this.setBounds(0, 0, this.width, this.height);
 	this.setResizable(false);
 	GameInformation.setGame(this);
 	MapInterface.setMap(this.map);
 	this.t = new Timer(1000 / this.targetFramerate, this);
 	t.start();
 	this.createBufferStrategy(2);
     }
 
     /**
      * Prints game over to the screen continuously and displays the final score.
      * @param score
      */
     public void gameOver(int score) {
 	this.gameOver = true;
 	this.gameOverScore = score;
     }
 
     /**
      * Prints the game over message and score to the screen.
      * @param g
      */
     public void printGameOver(Graphics2D g) {
 	g.setFont(new Font("Helvetica", Font.PLAIN, 40));
 	g.drawString("GAME OVER", 200, 200);
 	g.drawString("Score: " + this.gameOverScore, 200, 400);
     }
 }
