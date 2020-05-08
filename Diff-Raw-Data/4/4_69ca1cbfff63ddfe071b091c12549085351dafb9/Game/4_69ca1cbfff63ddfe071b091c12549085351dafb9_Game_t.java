 package de.propra12.gruppe04.dynamiteboy.Game;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Toolkit;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import de.propra12.gruppe04.dynamiteboy.Item.Bomb;
 import de.propra12.gruppe04.dynamiteboy.Item.Exit;
 import de.propra12.gruppe04.dynamiteboy.Item.Item;
 import de.propra12.gruppe04.dynamiteboy.Map.Map;
 import de.propra12.gruppe04.dynamiteboy.Menu.ScoreMenu;
 
 public class Game extends JPanel {
	private int playerStartPos[][] = new int[2][2];
 	private Player player[] = new Player[2];
 	private Map map;
 	private JFrame frame;
 	private int numberOfPlayers;
 	// Playerconstants
 	private final int PLAYER1 = 0, PLAYER2 = 1;
 
 	public Game(JFrame frame, int numberOfPlayers) {
 		// SET UP
 		this.numberOfPlayers = numberOfPlayers;
 		this.map = new Map(640, 480,
 				"src/de/propra12/gruppe04/dynamiteboy/Map/Map1.xml");
 		this.frame = frame;
 		createPlayers(numberOfPlayers);
 		setFocusable(true);
 		this.addKeyListener(new KAdapter());
 	}
 
 	public Map getMap() {
 		return map;
 	}
 
 	public Player getPlayer(int playerIndex) {
 		return this.player[playerIndex];
 	}
 
 	public void createPlayers(int numberOfPlayers) {
 		playerStartPos[0][0] = 32;
 		playerStartPos[0][1] = 32;
 		playerStartPos[1][0] = 581;
 		playerStartPos[1][1] = 416;
 		for (int i = 0; i < numberOfPlayers; i++) {
 			player[i] = new Player(i, playerStartPos[i][0],
 					playerStartPos[i][1]);
 		}
 	}
 
 	/**
 	 * Plants a bomb on current grid-position
 	 */
 
 	public void plantBomb(int playerIndex) {
 		Bomb bomb = new Bomb(
 				player[playerIndex]
 						.getGridX(player[playerIndex].getxPos() + 16),
 				player[playerIndex].getGridY(player[playerIndex].getyPos() + 16),
 				false, map);
 		Thread bombThread = new Thread(bomb);
 		bombThread.start();
 
 	}
 
 	/**
 	 * Checks if item is at field with pixel-coordinates x and y and handles it.
 	 * 
 	 * @param x
 	 * @param y
 	 */
 	public void itemHandling(int x, int y) {
 		Item item = map.getFieldByPixel(x, y).getItem();
 		if (item instanceof Exit) {
 			this.setVisible(false);
 			ScoreMenu m = new ScoreMenu(frame);
 		}
 	}
 
 	// KEY HANDLING AND PAINT METHODS DOWN FROM HERE
 
 	private class KAdapter extends KeyAdapter {
 		public void keyReleased(KeyEvent e) {
 			player1KeyReleased(e);
 			player[0].move();
 			if (numberOfPlayers > 1) {
 				player2KeyReleased(e);
 				player[1].move();
 			}
 		}
 
 		public void keyPressed(KeyEvent e) {
 			player1KeyPressed(e);
 			player[0].move();
 			if (numberOfPlayers > 1) {
 				player2KeyPressed(e);
 				player[1].move();
 			}
 		}
 	}
 
 	public void paintField(Graphics g2d) {
 		for (int y = 0; y < 480; y += 32) {
 			for (int x = 0; x < 640; x += 32) {
 				g2d.drawImage(map.getFieldByPixel(x, y).getImageIcon()
 						.getImage(), x, y, this);
 
 			}
 		}
 	}
 
 	public void paintPlayer(Graphics g2d, int playerIndex) {
 		g2d.drawImage(player[playerIndex].getImage(),
 				player[playerIndex].getxPos(), player[playerIndex].getyPos(),
 				this);
 	}
 
 	public void paint(Graphics g) {
 		super.paint(g);
 		Graphics g2d = (Graphics2D) g;
 		paintField(g2d);
 
 		for (int i = 0; i < numberOfPlayers; i++) {
 			paintPlayer(g2d, i);
 		}
 
 		Toolkit.getDefaultToolkit().sync();
 		g.dispose();
 		// Redraw with 30fps
 		try {
 			TimeUnit.SECONDS.sleep(1 / 30);
 			repaint();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void playerMoveLeft(int pIndex) {
 		if (map.getFieldByPixel(player[pIndex].getxPos(),
 				player[pIndex].getyPos()).isBlocked() == false
 				&& map.getFieldByPixel(player[pIndex].getxPos(),
 						player[pIndex].getyPos() + 30).isBlocked() == false) {
 			itemHandling(player[pIndex].getxPos(), player[pIndex].getyPos());
 			player[pIndex].setDx(-4);
 			player[pIndex].setDy(0);
 		}
 	}
 
 	public void playerMoveRight(int pIndex) {
 		if (map.getFieldByPixel(player[pIndex].getxPos() + 28,
 				player[pIndex].getyPos()).isBlocked() == false
 				&& map.getFieldByPixel(player[pIndex].getxPos() + 28,
 						player[pIndex].getyPos() + 28).isBlocked() == false) {
 			itemHandling(player[pIndex].getxPos(), player[pIndex].getyPos());
 			player[pIndex].setDx(4);
 			player[pIndex].setDy(0);
 		}
 	}
 
 	public void playerMoveUp(int pIndex) {
 
 		if (map.getFieldByPixel(player[pIndex].getxPos(),
 				player[pIndex].getyPos()).isBlocked() == false
 				&& map.getFieldByPixel(player[pIndex].getxPos() + 22,
 						player[pIndex].getyPos()).isBlocked() == false) {
 			itemHandling(player[pIndex].getxPos(), player[pIndex].getyPos());
 			player[pIndex].setDy(-4);
 			player[pIndex].setDx(0);
 		}
 	}
 
 	public void playerMoveDown(int pIndex) {
 		if (map.getFieldByPixel(player[pIndex].getxPos(),
 				player[pIndex].getyPos() + 30).isBlocked() == false
 				&& map.getFieldByPixel(player[pIndex].getxPos() + 22,
 						player[pIndex].getyPos() + 32).isBlocked() == false) {
 			itemHandling(player[pIndex].getxPos(), player[pIndex].getyPos());
 			player[pIndex].setDy(4);
 			player[pIndex].setDx(0);
 		}
 	}
 
 	public void player1KeyPressed(KeyEvent e) {
 
 		int key = e.getKeyCode();
 
 		if (key == KeyEvent.VK_LEFT) {
 			playerMoveLeft(PLAYER1);
 		}
 
 		if (key == KeyEvent.VK_RIGHT) {
 			playerMoveRight(PLAYER1);
 		}
 
 		if (key == KeyEvent.VK_UP) {
 			playerMoveUp(PLAYER1);
 		}
 
 		if (key == KeyEvent.VK_DOWN) {
 			playerMoveDown(PLAYER1);
 		}
 
 		if (key == KeyEvent.VK_ENTER) {
 			plantBomb(PLAYER1);
 		}
 
 	}
 
 	public void player2KeyPressed(KeyEvent e) {
 
 		int key = e.getKeyCode();
 
 		if (key == KeyEvent.VK_A) {
 			playerMoveLeft(PLAYER2);
 		}
 
 		if (key == KeyEvent.VK_D) {
 			playerMoveRight(PLAYER2);
 		}
 
 		if (key == KeyEvent.VK_W) {
 			playerMoveUp(PLAYER2);
 		}
 
 		if (key == KeyEvent.VK_S) {
 			playerMoveDown(PLAYER2);
 		}
 
 		if (key == KeyEvent.VK_SPACE) {
 			plantBomb(PLAYER2);
 		}
 
 	}
 
 	/**
 	 * 
 	 * @param e
 	 *            takes key event (released) and stops changing player position
 	 */
 	public void player1KeyReleased(KeyEvent e) {
 		int key = e.getKeyCode();
 
 		if (key == KeyEvent.VK_LEFT) {
 			player[PLAYER1].setDx(0);
 		}
 
 		if (key == KeyEvent.VK_RIGHT) {
 			player[PLAYER1].setDx(0);
 		}
 
 		if (key == KeyEvent.VK_UP) {
 			player[PLAYER1].setDy(0);
 		}
 
 		if (key == KeyEvent.VK_DOWN) {
 			player[PLAYER1].setDy(0);
 		}
 
 	}
 
 	public void player2KeyReleased(KeyEvent e) {
 		int key = e.getKeyCode();
 
 		if (key == KeyEvent.VK_A) {
 			player[PLAYER2].setDx(0);
 		}
 
 		if (key == KeyEvent.VK_D) {
 			player[PLAYER2].setDx(0);
 		}
 
 		if (key == KeyEvent.VK_W) {
 			player[PLAYER2].setDy(0);
 		}
 
 		if (key == KeyEvent.VK_S) {
 			player[PLAYER2].setDy(0);
 		}
 
 	}
 
 }
