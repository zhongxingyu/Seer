 package App.view;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 import Resources.MiniGameGFX;
 
 /**
  * This class drives the minigame sequence when travelling between planets.
  * @author Andrew Wilder
  */
 public class MiniGameScreen extends JPanel implements KeyListener, ActionListener {
 	
 	// Prevents serializable warning
 	private static final long serialVersionUID = -3027504169648377464L;
 
 	// Provides interaction between the KeyListener and ActionListener functionalities of this class
 	private boolean holdingLeft, holdingRight;
 	
 	// Constants that govern the minigame's physics
 	private static final int SHIP_SIZE = 64,
                              ASTEROID_SIZE = 42,
                              ASTEROID_COUNT = 25,
                              SAFETY_DIST = 200,
                              SHIP_GFX_SIZE = 64,
                              ASTEROID_GFX_SIZE = 42;
 	private static final double SPEED_CAP = 5.5,
                                 TURN_AMOUNT = 0.055,
                                 TOLERANCE = 7.5,
                                 ASTEROID_SPEED = 1.35,
                                 ASTEROID_ROTATION = 0.015;
 	
 	// An ArrayList to hold the Asteroid objects
 	private ArrayList<Asteroid> asteroids;
 	private ArrayList<Point> stars;
 	
 	// Variables that control the player's ship
 	private double shipX, shipY, shipAngle, shipSpeed;
 	
 	// A Timer object meant to trigger frames of gameplay in the minigame
 	private Timer timer;
 	
 	// Instances of the graphics used by the minigame
 	private BufferedImage shipGFX, asteroidGFX;
 	
 	/**
 	 * Construct this minigame screen with the appropriate variables.
 	 * @param frame A link to the instance of Display that this minigame with make interactions with.
 	 */
 	public MiniGameScreen() {
 		
 		// Set up listeners
 		timer = new Timer(30, this);
 		setFocusable(true);
 		addKeyListener(this);
 		setDoubleBuffered(true);
 		
 		// Set up BufferedImage graphics
 		asteroidGFX = new BufferedImage(ASTEROID_GFX_SIZE, ASTEROID_GFX_SIZE, BufferedImage.TYPE_INT_ARGB);
 		asteroidGFX.setRGB(0, 0, asteroidGFX.getWidth(), asteroidGFX.getHeight(), MiniGameGFX.AsteroidGFX, 0, asteroidGFX.getWidth());
 		shipGFX = new BufferedImage(SHIP_GFX_SIZE, SHIP_GFX_SIZE, BufferedImage.TYPE_INT_ARGB);
 		shipGFX.setRGB(0, 0, shipGFX.getWidth(), shipGFX.getHeight(), MiniGameGFX.ShipGFX, 0, shipGFX.getWidth());
 	}
 
 	/**
 	 * Simulate the gameplay for one frame.
 	 * @param e The instance of ActionEvent associated with this event trigger. Unused.
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 
 		// Update ship angle
 		if(holdingLeft) {
 			shipAngle += 2 * Math.PI - TURN_AMOUNT;
 		}
 		if(holdingRight) {
 			shipAngle += TURN_AMOUNT;
 		}
 		if(shipAngle > 2 * Math.PI) {
 			shipAngle -= 2 * Math.PI;
 		} else if(shipAngle < 0) {
 			shipAngle += 2 * Math.PI;
 		}
 		
 		// Update ship speed
 		if(shipSpeed < SPEED_CAP) {
 			shipSpeed += 0.1;
 		}
 		
 		// Update ship position
 		shipX += Math.cos(shipAngle) * shipSpeed;
 		shipY += Math.sin(shipAngle) * shipSpeed;
 		
 		// Detect collision with asteroids and update their position
 		for(Asteroid a : asteroids) {
 			if(Point2D.distance(shipX, shipY, a.x, a.y) - (SHIP_SIZE + ASTEROID_SIZE) / 2 < -TOLERANCE) {
 				endGame(false);
 			} else {
 				a.x += Math.cos(a.a) * ASTEROID_SPEED;
 				
 				// Leaving off the sides puts it in the middle, top or bottom
 				if(a.x + ASTEROID_SIZE / 2 < 0 || a.x - ASTEROID_SIZE / 2 > getWidth()) {
 
 					a.x = getWidth() / 2;
 					if(a.a < Math.PI) {
 						a.y = ASTEROID_SIZE / 2 + getHeight();
 					} else {
 						a.y = -ASTEROID_SIZE / 2;
 					}
 				}
 				a.y += Math.sin(a.a) * ASTEROID_SPEED;
 				
 				// Leaving off the top or bottom wraps the asteroid
 				if(a.y + ASTEROID_SIZE / 2 < 0) {
 					a.y += ASTEROID_SIZE + getHeight();
 				} else if(a.y - ASTEROID_SIZE / 2 > getHeight()) {
 					a.y -= ASTEROID_SIZE + getHeight();
 				}
 				
 				// Rotate the asteroid
 				if(a.direction) {
 					a.r += ASTEROID_ROTATION;
 					if(a.r >= 2 * Math.PI) {
 						a.r -= 2 * Math.PI;
 					}
 				} else {
 					a.r -= ASTEROID_ROTATION;
 					if(a.r < 0) {
 						a.r += 2 * Math.PI;
 					}
 				}
 			}
 		}
 		
 		// Detect flying out of bounds
 		if(shipX + SHIP_SIZE / 2 < 0) {
 			endGame(false);
 		} else if(shipY + SHIP_SIZE / 2 < 0) {
 			endGame(false);
 		} else if(shipY - SHIP_SIZE / 2 > getHeight()) {
 			endGame(false);
 		} else if(shipX - SHIP_SIZE / 2 > getWidth()) {
 			endGame(true);
 		}
 		
 		repaint();
 	}
 	
 	/**
 	 * Draw the graphics of the entities for the game.
 	 * @param g The instance of Graphics object associated with this Component.
 	 */
 	@Override
 	public void paintComponent(Graphics g) {
 		
 		// Draw the background graphics
 		Graphics2D screen = (Graphics2D)g;
 		screen.setColor(Color.BLACK);
 		screen.fillRect(0,  0, getWidth(), getHeight());
 		screen.setColor(Color.WHITE);
 		for(Point p : stars) {
 			screen.drawRect(p.x, p.y, 0, 0);
 		}
 		
 		// Draw asteroids
 		for(Asteroid a : asteroids) {
 			AffineTransform orig = screen.getTransform();
 			screen.rotate(a.r, (int)a.x, (int)a.y);
 			screen.drawImage(asteroidGFX, (int)(a.x - ASTEROID_SIZE / 2), (int)(a.y - ASTEROID_SIZE / 2), null);
 			screen.setTransform(orig);
 		}
 		
 		// Draw ship
 		AffineTransform orig = screen.getTransform();
 		screen.rotate(shipAngle, shipX, shipY);
 		screen.drawImage(shipGFX, (int)(shipX - SHIP_SIZE / 2), (int)(shipY - SHIP_SIZE / 2), null);
 		screen.setTransform(orig);
 		
 		// Clean up
 		Toolkit.getDefaultToolkit().sync();
 		screen.dispose();
 	}
 	
 	/**
 	 * Set up the initial variable values and Asteroid list, then start the simulation.
 	 */
 	public void startGame() {
 		
 		// Set up the variables used by the minigame
 		shipX = SHIP_SIZE / 2;
 		shipY = getHeight() / 2;
 		shipAngle = 0;
 		shipSpeed = 0;
 		asteroids = new ArrayList<Asteroid>();
 		stars = new ArrayList<Point>();
 		holdingLeft = false;
 		holdingRight = false;
 		
 		// Randomize the initial locations of the Asteroids
 		Random rand = new Random();
		for(int i = 0; i < ASTEROID_COUNT * getWidth() * getHeight() / 480000; ++i) {
 			Point p = new Point(rand.nextInt(getWidth()), rand.nextInt(getHeight()));
 			if(Point2D.distance(shipX, shipY, p.x, p.y) < SAFETY_DIST) {
 				--i;
 			} else {
 				asteroids.add(new Asteroid(p.x, p.y, rand.nextDouble() * 2 * Math.PI, rand.nextDouble() * 2 * Math.PI, rand.nextBoolean()));
 			}
 		}
 		
 		// Generate stars
		for(int i = 0; i < getWidth() * getHeight() / 480; ++i) {
 			stars.add(new Point(rand.nextInt(getWidth()), rand.nextInt(getHeight())));
 		}
 		
 		// Start the simulation and focus the keyboard input on this panel
 		timer.start();
 	}
 	
 	/**
 	 * Ends the game; called by the actionPerformed method.
 	 * @param success true if the Player won, false if they crashed or flew out of bounds.
 	 */
 	private void endGame(boolean success) {
 		timer.stop();
 		Display.exitGame(success);
 	}
 
 	/**
 	 * Tells the timer-triggered actionPerformed() method to rotate the ship, if left
 	 * or right is pressed.
 	 * @param e The instance of KeyEvent associated with this event call.
 	 */
 	@Override
 	public void keyPressed(KeyEvent e) {
 		
 		// Determine if the player pressed either left or right
 		if(e.getKeyCode() == KeyEvent.VK_LEFT) {
 			holdingLeft = true;
 		} else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
 			holdingRight = true;
 		}
 	}
 
 	/**
 	 * Tells the timer-triggered actionPerformed() method to stop rotating the ship, if
 	 * left or right is released.
 	 * @param e The instance of KeyEvent associated with this event call.
 	 */
 	@Override
 	public void keyReleased(KeyEvent e) {
 		
 		// Determine if the player released either left or right
 		if(e.getKeyCode() == KeyEvent.VK_LEFT) {
 			holdingLeft = false;
 		} else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
 			holdingRight = false;
 		}
 	}
 
 	/**
 	 * This method is unused; it exists to fulfill the implementation of KeyListener.
 	 * @param arg0 The instance of KeyEvent associated with this event call.
 	 */
 	@Override
 	public void keyTyped(KeyEvent arg0) {}
 
 	/**
 	 * This inner class is simply an association of doubles and a boolean that make
 	 * up the properties of an Asteroid object on the minigame screen.
 	 * @author Andrew Wilder
 	 */
 	private class Asteroid {
 		
 		// X position, Y position, angle of travel (radians), rotational amount (radians)
 		public double x, y, a, r;
 		
 		// Direction the Asteroid is spinning
 		boolean direction;
 		
 		/**
 		 * This constructor creates a new Asteroid object from specified parameters.
 		 * @param x The initial X position of the Asteroid.
 		 * @param y The initial Y position fo the Asteroid.
 		 * @param a The initial direction (in radians) that the Asteroid will travel in.
 		 * @param r The initial rotational amount (in radians) from which the Asteroid begins spinning.
 		 * @param direction The direction the Asteroid will spin.
 		 */
 		public Asteroid(double x, double y, double a, double r, boolean direction) {
 			this.x = x;
 			this.y = y;
 			this.a = a;
 			this.r = r;
 			this.direction = direction;
 		}
 	}
 }
