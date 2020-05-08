 import java.awt.CardLayout;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.HashMap;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 /**
  * The Class View - contains whole logic used for GUI. Generates keyboard events
  * and sends them to Controller's event queue. It also able to directly
  * communicate with model - uses it to get current game state and get snapshots
  * of tetrion for drawing purposes.
  * 
  */
 public class View implements KeyListener
 {
 
 	/**
 	 * The Class MenuFrame - JPanel used for basic game menu.
 	 */
 	class MenuFrame extends JPanel
 	{
 
 		/** The Constant serialVersionUID. */
 		private static final long serialVersionUID = 1L;
 
 		/** Buffered image used for background. */
 		BufferedImage background;
 
 		/** Buffered image used for menu, with "START GAME" selected. */
 		BufferedImage selectedStart;
 
 		/** Buffered image used for menu, with "QUIT" selected. */
 		BufferedImage selectedQuit;
 
 		/** Buffered image used for showing key configuration. */
 		BufferedImage controlsWindow;
 
 		/**
 		 * Instantiates a new menu frame and loads images needed for it. Shuts
 		 * program down when loading fails
 		 */
 		public MenuFrame()
 		{
 			try
 			{
 				this.background = ImageIO.read(this.getClass().getResource(
 						"/images/bg.png"));
 				this.selectedStart = ImageIO.read(this.getClass().getResource(
 						"/images/menu_ss.png"));
 				this.selectedQuit = ImageIO.read(this.getClass().getResource(
 						"/images/menu_qs.png"));
 				this.controlsWindow = ImageIO.read(this.getClass().getResource(
 						"/images/controls.png"));
 			}
 			catch (IOException e)
 			{
 				System.exit(1);
 			}
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
 		 */
 		@Override
 		public void paintComponent(Graphics g)
 		{
 			super.paintComponent(g);
 			Graphics2D g2 = (Graphics2D) g;
 			g2.drawImage(this.background, 0, 0, null);
 			if (View.this.controller != null)
 			{
 				if (View.this.controller.getMenuState() == Controller.MenuItem.START)
 				{
 					g2.drawImage(this.selectedStart, 0, 0, null);
 				}
 				else
 				{
 					g2.drawImage(this.selectedQuit, 0, 0, null);
 				}
 				g2.drawImage(this.controlsWindow, 0, 0, null);
 			}
 		}
 	}
 
 	/**
 	 * The Class TetrisFrame - used for drawing game during play - tetrion, alive
 	 * tetromino, score, current level, and when needed, game over and pause
 	 * screens. Loads all images needed during creation, when it fails whole
 	 * program is shut down.
 	 */
 	class TetrisFrame extends JPanel
 	{
 
 		/** The Constant serialVersionUID. */
 		private static final long serialVersionUID = 1L;
 
 		/** Hashmap containing buffered images of different colors blocks. */
 		HashMap<Integer, BufferedImage> colors;
 
 		/** Hashmap containing buffered images of numbers 0-9, plus greyed-out 0. */
 		HashMap<Integer, BufferedImage> numbers;
 
 		/** Buffered background. */
 		BufferedImage background;
 
 		/** Buffered image of "GAME OVER" screen. */
 		BufferedImage gameLost;
 
 		/** Buffered image of game overlay i.e. "LEVEL" and "SCORE" texts. */
 		BufferedImage gameOverlay;
 
 		/** Buffered image of "GAME PAUSED" screen. */
 		BufferedImage gamePaused;
 
 		/**
 		 * Instantiates a new tetris frame, loads needed graphics into memory, shut
 		 * down program on fail.
 		 */
 		public TetrisFrame()
 		{
 			this.colors = new HashMap<Integer, BufferedImage>();
 			this.numbers = new HashMap<Integer, BufferedImage>();
 			try
 			{
 				this.background = ImageIO.read(this.getClass().getResource(
 						"/images/bg.png"));
 				this.gameLost = ImageIO.read(this.getClass().getResource(
 						"/images/gameover.png"));
 				this.gamePaused = ImageIO.read(this.getClass().getResource(
 						"/images/pause.png"));
 				this.gameOverlay = ImageIO.read(this.getClass().getResource(
 						"/images/game_overlay.png"));
 				this.colors.put(-2,
 						ImageIO.read(this.getClass().getResource("/images/ghost.png")));
 				this.colors.put(-1,
 						ImageIO.read(this.getClass().getResource("/images/white.png")));
 				this.colors.put(0,
 						ImageIO.read(this.getClass().getResource("/images/black.png")));
 				this.colors.put(1,
 						ImageIO.read(this.getClass().getResource("/images/blue.png")));
 				this.colors.put(2,
 						ImageIO.read(this.getClass().getResource("/images/red.png")));
 				this.colors.put(3,
 						ImageIO.read(this.getClass().getResource("/images/green.png")));
 				this.colors.put(4,
 						ImageIO.read(this.getClass().getResource("/images/teal.png")));
 				this.colors.put(5,
 						ImageIO.read(this.getClass().getResource("/images/orange.png")));
 				this.colors.put(6,
 						ImageIO.read(this.getClass().getResource("/images/yellow.png")));
 				this.colors.put(7,
 						ImageIO.read(this.getClass().getResource("/images/purple.png")));
 				this.numbers
 						.put(
 								-1,
 								ImageIO.read(this.getClass().getResource(
 										"/images/inactive_0.png")));
 				this.numbers.put(0,
 						ImageIO.read(this.getClass().getResource("/images/0.png")));
 				this.numbers.put(1,
 						ImageIO.read(this.getClass().getResource("/images/1.png")));
 				this.numbers.put(2,
 						ImageIO.read(this.getClass().getResource("/images/2.png")));
 				this.numbers.put(3,
 						ImageIO.read(this.getClass().getResource("/images/3.png")));
 				this.numbers.put(4,
 						ImageIO.read(this.getClass().getResource("/images/4.png")));
 				this.numbers.put(5,
 						ImageIO.read(this.getClass().getResource("/images/5.png")));
 				this.numbers.put(6,
 						ImageIO.read(this.getClass().getResource("/images/6.png")));
 				this.numbers.put(7,
 						ImageIO.read(this.getClass().getResource("/images/7.png")));
 				this.numbers.put(8,
 						ImageIO.read(this.getClass().getResource("/images/8.png")));
 				this.numbers.put(9,
 						ImageIO.read(this.getClass().getResource("/images/9.png")));
 			}
 			catch (IOException e)
 			{
 				System.exit(1);
 			}
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
 		 */
 		@Override
 		public void paintComponent(Graphics g)
 		{
 			super.paintComponent(g);
 			Graphics2D g2 = (Graphics2D) g;
 			int[][] game = View.this.model.getSnapshot();
 			g2.drawImage(this.background, 0, 0, null);
 			g2.drawImage(this.gameOverlay, 0, 0, null);
 			int score = View.this.model.getScore();
 			int level = View.this.model.getLevel();
 			for (int i = 0; i < 8; ++i)
 			{
 				if ((score != 0) || (i == 0))
 				{
 					g2.drawImage(this.numbers.get(score % 10), 13 + (29 * (7 - i)), 25,
 							null);
 				}
 				else
 				{
 					g2.drawImage(this.numbers.get(-1), 13 + (29 * (7 - i)), 25, null);
 				}
 				score = score / 10;
 			}
 			for (int i = 0; i < 2; ++i)
 			{
 				if ((level != 0) || (i == 0))
 				{
 					g2.drawImage(this.numbers.get(level % 10), 180 + (29 * (4 - i)), 25,
 							null);
 				}
 				level = level / 10;
 			}
 			for (int y = 0; y < Model.HEIGHT; ++y)
 			{
 				for (int x = 0; x < Model.WIDTH; ++x)
 				{
 					g2.drawImage(this.colors.get(game[y][x]), 30 * (x + 1),
 							60 + (30 * (y + 1)), null);
 				}
 			}
 			if (View.this.model.getState() == Model.State.PAUSE)
 			{
 				g2.drawImage(this.gamePaused, 0, 0, null);
 			}
 			else if (View.this.model.getState() == Model.State.GAMEOVER)
 			{
 				g2.drawImage(this.gameLost, 0, 0, null);
 			}
 		}
 	}
 
 	/** The model reference. */
 	Model model;
 
 	/** The controller reference. */
 	Controller controller;
 
 	/** The game window, should contain tetris- and menuFrame. */
 	JFrame gameWindow;
 
 	/** The tetris frame. */
 	TetrisFrame tetrisFrame;
 
 	/** The menu frame. */
 	MenuFrame menuFrame;
 
 	/** The graphics used for drawing. */
 	Graphics g;
 
 	/**
 	 * Instantiates a new view. Uses CardLayout for easy change between visible
 	 * JPanel-based frames.
 	 * 
 	 * @param m
 	 *          - model reference
 	 */
 	View(Model m)
 	{
 		this.model = m;
 		this.controller = null;
 		this.gameWindow = new JFrame("Tetris");
 		this.gameWindow.setLayout(new CardLayout());
 		this.tetrisFrame = new TetrisFrame();
 		this.menuFrame = new MenuFrame();
 		this.gameWindow.add(this.menuFrame);
 		this.gameWindow.add(this.tetrisFrame);

		this.gameWindow.setSize(new Dimension(360, 720));
 		this.gameWindow.setResizable(false);
 		this.gameWindow.setFocusable(true);
 		this.gameWindow.addKeyListener(this);
 		this.gameWindow.setVisible(true);
 		this.gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 
 	/**
 	 * Forces gui redraw.
 	 */
 	synchronized void drawGame()
 	{
 		this.gameWindow.repaint();
 		this.gameWindow.revalidate();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
 	 */
 	@Override
 	public void keyPressed(KeyEvent arg0)
 	{
 		this.controller.putUserCommand(arg0.getKeyCode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
 	 */
 	@Override
 	public void keyReleased(KeyEvent arg0)
 	{
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
 	 */
 	@Override
 	public void keyTyped(KeyEvent arg0)
 	{
 	}
 
 	/**
 	 * Sets the controller.
 	 * 
 	 * @param c
 	 *          the new controller
 	 */
 	public void setController(Controller c)
 	{
 		this.controller = c;
 	}
 
 	/**
 	 * Show menu frame (and hide tetris frame at the same time).
 	 */
 	void showMenuFrame()
 	{
 		this.menuFrame.setVisible(true);
 		this.tetrisFrame.setVisible(false);
 		this.drawGame();
 	}
 
 	/**
 	 * Show tetris frame (and hide menu frame at the same time).
 	 */
 	void showTetrisFrame()
 	{
 		this.menuFrame.setVisible(false);
 		this.tetrisFrame.setVisible(true);
 		this.drawGame();
 	}
 }
