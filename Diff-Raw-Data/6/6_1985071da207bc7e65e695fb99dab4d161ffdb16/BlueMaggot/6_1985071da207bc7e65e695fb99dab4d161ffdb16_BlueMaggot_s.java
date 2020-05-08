 package blueMaggot;
 
 import gfx.MenuTitle;
 import inputhandler.InputHandler;
 import inputhandler.InputHandlerMenu;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 
 import javax.swing.JApplet;
 import javax.swing.JFrame;
 import javax.swing.JLayeredPane;
 import javax.swing.JPanel;
 
 import baseGame.testGame;
 
 /**
  * @author Habitats * this motherfucker starts the game
  */
 public class BlueMaggot extends JFrame implements Runnable {
 
 	InputHandlerMenu input = new InputHandlerMenu(this);
 	public InputHandler inputReal = new InputHandler();
 	private JLayeredPane layeredPane = new JLayeredPane();
 	public MenuTitle menuTitle;
 	JPanel gamePanel = new JPanel();
 	testGame game;
 
 	public BlueMaggot() {
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setPreferredSize(new Dimension(testGame.WIDTH, testGame.HEIGHT));
 		setFocusable(true);
 
 		layeredPane.setBounds(0, 0, testGame.WIDTH, testGame.HEIGHT);
 		layeredPane.setOpaque(false);
 
 		game = new baseGame.testGame(this);
 		menuTitle = new MenuTitle(game);
 		layeredPane.add(gamePanel, new Integer(0));
 		layeredPane.add(menuTitle, new Integer(1));
 
 		add(layeredPane);
 		pack();
 		setLocationRelativeTo(null);
 		setVisible(true);

		run();
 	}
 
 	private void setUpGame() {
 		game.setPreferredSize(testGame.DIMENSION);
 		gamePanel.setLayout(new BorderLayout());
 		gamePanel.setBounds(0, 0, testGame.WIDTH, testGame.HEIGHT);
 		gamePanel.add(game);
 	}
 
 	private void startGame() {
 		game.setVisible(true);
 		game.init();
 	}
 
 	@Override
 	public void run() {
 
 		setUpGame();
 		startGame();
		repaint();
 		game.requestFocus();
 	}
 
 	public void toggleMenu() {
 		if (menuTitle.isVisible()) {
 			testGame.PAUSED = false;
 			menuTitle.setVisible(false);
 		} else {
 			testGame.PAUSED = true;
 			menuTitle.setVisible(true);
 		}
 	}
 
 	public static void main(String[] args) {
 		(new BlueMaggot()).run();
 	}
 
 	public void tick() {
 		// game.requestFocus();
 		if (inputReal.menu.clicked) {
 			inputReal.menu.clicked = false;
 			inputReal.releaseAll();
 			if (!menuTitle.isVisible()) {
 				menuTitle.setVisible(true);
 				testGame.PAUSED = true;
 				System.out.println("dicks");
 			}
 			// System.out.println("asasdasd");
 		}
 //		System.out.println(Thread.currentThread().getThreadGroup().activeCount());
 	}
 }
