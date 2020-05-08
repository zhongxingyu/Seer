 package Core;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Queue;
 
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 
 import environment.Tiles;
 
 public class Canvas extends JPanel implements KeyListener {
 	private GUI gui;
 	private Player player;
 
 	public int SCREEN_WIDTH = this.getWidth();
 	public int SCREEN_HEIGHT = this.getHeight();
 	
 	private int pressLeft = KeyEvent.VK_LEFT;
 	private int pressRight = KeyEvent.VK_RIGHT;
 	private int pressUP = KeyEvent.VK_UP;
 	private int pressDown = KeyEvent.VK_DOWN;
 	
 	private boolean pressed = false;
 	private boolean blocked = false;
 	
 	private Queue<String> walkLeft = new ArrayDeque<String>();
 	private Queue<String> walkRight = new ArrayDeque<String>();
 	private Queue<String> walkUp = new ArrayDeque<String>();
 	private Queue<String> walkDown = new ArrayDeque<String>();
 	
 	public void setupAnimations(){
 			walkLeft.add("man_left.png");
 			walkLeft.add("man_left_walk1.png");
 			walkLeft.add("man_left.png");
 			walkLeft.add("man_left_walk2.png");
 			walkRight.add("man_right.png");
 			walkRight.add("man_right_walk1.png");
			walkRight.add("man_right.png");
 			walkRight.add("man_right_walk2.png");
 			walkUp.add("man_up.png");
 			walkUp.add("man_up_walk1.png");
 			walkUp.add("man_up.png");
 			walkUp.add("man_up_walk2.png");
 			walkDown.add("man_down.png");
 			walkDown.add("man_down_walk1.png");
 			walkDown.add("man_down.png");
 			walkDown.add("man_down_walk2.png");
 		}
 	
 	private Tiles tiles;
 	
 	private MainGame main;
 
 	public Canvas(GUI parent, Tiles tiles) {
 		
 		setupAnimations();
 		gui = parent;
 		this.tiles = tiles;
 		this.setSize(parent.getWidth(), parent.getHeight());
 		this.SCREEN_WIDTH = parent.getWidth();
 		this.SCREEN_HEIGHT = parent.getHeight();
 		this.setBackground(Color.white);
 		this.addMouseMotionListener(new MouseMotionListener() {
 
 			@Override
 			public void mouseMoved(MouseEvent e) {
 				canvasMouseMoved(e);
 			}
 
 			@Override
 			public void mouseDragged(MouseEvent e) {
 			}
 		});
 		this.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				canvasMouseClicked(e);
 			}
 		});
 
 		//create player
 		BufferedImage img = null;
 		String filename = "man_up.png";		
 		try{
 
 			img = ImageIO.read(new FileInputStream("src"+File.separatorChar+"data"+File.separatorChar+filename));
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 		this.player = new Player(img,this);
 	}
 	
 	public void setMain(MainGame main){
 		this.main = main;
 	}
 	
 	
 	
 	// basic logic for movement:
 	// they try to move in a direction, compute what their bounding box
 	// would be and whether that box would be colliding with a wall.
 	// if it wouldn't collide, then you move player and check if they're
 	// now touching an item.
 	private void canvasKeyPressed(KeyEvent e) {
 
 		int STEP_SIZE = player.STEP_SIZE;
 		pressed = true; 
 		
 		if (e.getKeyCode() == pressDown) {
 			String path = walkDown.poll();
 			player.setImage(path);
 			walkDown.add(path);
 			
 			if (!main.checkWallCollision(0, STEP_SIZE)) {
 				main.moveDown();
 				main.checkItemCollision();
 			}
 			
 		} else if (e.getKeyCode() == pressLeft) {
 			String path = walkLeft.poll();
 			player.setImage(path);
 			walkLeft.add(path);
 			
 			if (!main.checkWallCollision(-STEP_SIZE, 0)) {
 				main.moveLeft();
 				main.checkItemCollision();
 			}
 
 		} else if (e.getKeyCode() == pressUP) {
 			String path = walkUp.poll();
 			player.setImage(path);
 			walkUp.add(path);
 			
 			if (!main.checkWallCollision(0, -STEP_SIZE)) {
 				main.moveUp();
 				main.checkItemCollision();
 			}
 			
 		} else if (e.getKeyCode() == pressRight) {
 			String path = walkRight.poll();
 			player.setImage(path);
 			walkRight.add(path);
 			
 			if (!main.checkWallCollision(STEP_SIZE, 0)) {
 				main.moveRight();
 				main.checkItemCollision();
 			}
 		}
 		gui.repaint();
 	}
 	
 	private void canvasKeyReleased(KeyEvent e){
 		
 
 		if (e.getKeyCode() == pressDown) {
 			player.setImage("man_down.png");
 		} else if (e.getKeyCode() == pressLeft) {
 			player.setImage("man_left.png");
 		} else if (e.getKeyCode() == pressUP) {
 			player.setImage("man_up.png");
 		} else if (e.getKeyCode() == pressRight) {
 			player.setImage("man_right.png");
 		}
 
 		pressed = false;
 		gui.repaint();
 		
 		
 	}
 
 	private void canvasMouseMoved(MouseEvent e) {
 		gui.repaint();
 	}
 
 	private void canvasMouseClicked(MouseEvent e) {
 		gui.repaint();
 	}
 
 	public void paint(Graphics g) {
 		//super.paint(g);
 
 
 		//tiles.draw(g,main.getOffsetX(), main.getOffsetY(), SCREEN_WIDTH, SCREEN_HEIGHT);
 		g.setColor(Color.WHITE);
 		g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
 		tiles.draw(g,main.getOffsetX(), main.getOffsetY(), SCREEN_WIDTH, SCREEN_HEIGHT);
 		player.draw(g);
 		
 		//Draw Score here
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		canvasKeyPressed(e);
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		canvasKeyReleased(e);
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @return the pressLeft
 	 */
 	public int getPressLeft() {
 		return pressLeft;
 	}
 
 	/**
 	 * @param pressLeft
 	 *            the pressLeft to set
 	 */
 	public void setPressLeft(int pressLeft) {
 		this.pressLeft = pressLeft;
 	}
 
 	/**
 	 * @return the pressRight
 	 */
 	public int getPressRight() {
 		return pressRight;
 	}
 
 	/**
 	 * @param pressRight
 	 *            the pressRight to set
 	 */
 	public void setPressRight(int pressRight) {
 		this.pressRight = pressRight;
 	}
 
 	/**
 	 * @return the pressUP
 	 */
 	public int getPressUP() {
 		return pressUP;
 	}
 
 	public Player getPlayer(){
 		return this.player;
 	}
 	
 	/**
 	 * @param pressUP
 	 *            the pressUP to set
 	 */
 	public void setPressUP(int pressUP) {
 		this.pressUP = pressUP;
 	}
 
 	/**
 	 * @return the pressDown
 	 */
 	public int getPressDown() {
 		return pressDown;
 	}
 
 	/**
 	 * @param pressDown
 	 *            the pressDown to set
 	 */
 	public void setPressDown(int pressDown) {
 		this.pressDown = pressDown;
 	}
 
 }
