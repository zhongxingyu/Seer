 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.*;
 import java.util.*;
 
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.image.BufferStrategy;
 import java.awt.Graphics2D;
 import java.awt.BasicStroke;
 import java.awt.BorderLayout;
 import java.awt.*;
 
 import javax.swing.JPanel;
 import javax.swing.BoxLayout;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 
 public class GameController {
 	private final GameState state;
 	private long fallCountdown; // in millisecond
 	private long motionCountdown;
 	private static final long DROP_MOTION_DELAY = 50; // ~ 30 cells / s
 	private long keyPressTime;
 	private boolean rightPressed, leftPressed, rotatePressed, dropPressed;
 	private boolean longKeyPress;
 	
     private final TetrisApplication mainApplication;
     // the content panel for the whole application
 	private final JPanel contentPane;
     // layout manager to switch between menu panel and game panel
     private final CardLayout paneSwitch;
     // the panel for the main menu
 	private final MenuPanel menuPane;
     // the panel for when the game is running
 	private final GamePanel gamePane;
 	// the key listener used when the game is running
 	private final KeyInputHandler gameKeyListener;
 	// used to init the start level for the game
 	private int startLevel;
 	// running is true while the game loop is running
 	private boolean running;
 	public static final Dimension RESOLUTION = new Dimension(900, 640);
 	private static final Dimension BLOCK_SIZE = new Dimension(28, 28);
 	private static final Dimension BOARD_FRAME_SIZE = 
 		new Dimension(BLOCK_SIZE.width * 12, BLOCK_SIZE.height * 22);
 	private static final int MARGIN = BLOCK_SIZE.width;
 	
 	GameController(TetrisApplication mainApplication) {
 		// init the state of the game
 		this.mainApplication = mainApplication;
 		startLevel = 1;
 		state      = new GameState(startLevel);
 		initFallCountdown();
 		initMotionCountdown();
 		
 		// init the sound
 		GameSound.init();
 		
 		// Graphics
 		// init the game panel and menu
 		paneSwitch = new CardLayout();
 		contentPane  = new JPanel(paneSwitch);
 		menuPane   = new MenuPanel(new MenuListener());
 		gamePane   = new GamePanel(state,
 			RESOLUTION.width, RESOLUTION.height,
 			BOARD_FRAME_SIZE.width, BOARD_FRAME_SIZE.height, MARGIN);
        
 		contentPane.add(menuPane, "Menu");
 		contentPane.add(gamePane, "Game"); 
         // contentPane.setFocusable(true);
         // contentPane.requestFocusInWindow();
 		
 		// init the listeners
         gamePane.setFocusable(true);
         gamePane.addComponentListener(new ComponentAdapter() {
                      public void componentShown(ComponentEvent cEvt) {
                         Component src = (Component) cEvt.getSource();
                         src.requestFocusInWindow();
                      }
                   });
         menuPane.setFocusable(true);
         menuPane.addComponentListener(new ComponentAdapter() {
              public void componentShown(ComponentEvent cEvt) {
                 Component src = (Component) cEvt.getSource();
                 src.requestFocusInWindow();
              }
           });
 		// won't be used until the game starts
         gameKeyListener = new KeyInputHandler();
         gamePane.addKeyListener(gameKeyListener);
         gamePane.setFocusTraversalKeysEnabled(false);
         state.addBoardListener(new BoardController());
 		state.addStateListener(new StateController());
 		displayMenuPanel();
 	}
 	
 	public JPanel getContentPane() {
 	    return contentPane;
 	}
 
 	public void displayMenuPanel() {
         SwingUtilities.invokeLater(new Runnable() {
 	        public void run() {
                 paneSwitch.show(contentPane, "Menu");
                 // menuPane.requestFocusInWindow();
 	        }
 	    });
 	}
 	
 	public void displayGamePanel() {
         SwingUtilities.invokeLater(new Runnable() {
 	        public void run() {	            
                 paneSwitch.show(contentPane, "Game");
                 // gamePane.requestFocusInWindow();
 	        }
 	    });
 	}
 	
 	private void startGame() {
 		running = true;
         // init the game state
 		initState();
         // SwingUtilities.invokeLater(new Runnable(){
         //     public void run(){
         //              // use the game key listener
         //          contentPane.addKeyListener(gameKeyListener);
         //              // disable tab focus traversal to be able to use the [TAB] key
         //                 contentPane.setFocusTraversalKeysEnabled(false); 
         //     }
         // });
 		// display the game panel
 		displayGamePanel();
 	}
 	
 	private void exit() {
 	    mainApplication.exit();
 	}
 	
 	private void stopGame() {
 	    // set back the menu listeners
         //      SwingUtilities.invokeLater(new Runnable(){
         //     public void run(){
         //         contentPane.removeKeyListener(gameKeyListener);
         //                 contentPane.setFocusTraversalKeysEnabled(true); 
         //     }
         // });
 	    // display the menu panel
         displayMenuPanel();
 	}
 	
 	private void initState() {
 		state.init(startLevel);
 	}
 	
 	private void gameLoop() {
 		long lastLoopTime = System.currentTimeMillis();
 		long dTime;
 		int delay = 15;
 		while (running) {
 		    // compute the time that has gone since the last frame
 		    dTime = System.currentTimeMillis() - lastLoopTime;
 			lastLoopTime = System.currentTimeMillis();
 		    
 		    // UPDATE STATE
 			if (state.isOver()) {
 		    // go out of the loop if the game is over
 				running = false;
 				break;
 			}
 			
 			if (!state.isPaused()) {
 			// update the state of the game if it is not paused
 				updateState(dTime);
 				handleInputMotion(dTime, lastLoopTime);
 			}
 			// UPDATE GRAPHICS
 			updateGraphics(); // == 
 			// Finally pause for a bit
 			try { 
 				Thread.sleep(delay); 
 			} catch (Exception e) {}
 		}
 	}
 	
 	private void updateGraphics() {
         gamePane.repaint();
        // contentPane.paint();
 	}
 	
 	public void updateState(long dTime) {
 		updateFallCountdown(dTime);
 		if (!isFallTimeOut()) {
 			return;
 		}
 		if (state.isActivePiece()) {
 			if (state.canFallPiece()) {
 				state.moveDownPiece();
 			} else {
 				state.mergePiece();
 			}
 		} else {
 			if (state.canSpawnPiece()) {
 				state.spawnPiece();
 			} else {
 				state.setGameOver();
 			}
 		}
 		resetFallCountdown();
 		GameSound.playFall();
 	}
 	
 	public void updatePieceMotion(MoveType movetype, long dTime) {
 		updateMotionCountdown(dTime);
 		if (!isMotionTimeOut()) {
 			return;
 		}
 		if (state.canMovePiece(movetype)) {
 			state.movePiece(movetype);
 		}
 		resetMotionCountdown(movetype);	
 	}
 	
 	public void handleInputMotion(long dTime, long lastLoopTime) {
 		// update the piece movement
 		// drop motion is triggered instantly
 		if (dropPressed && 
 		!(rightPressed || leftPressed || rotatePressed)) {
 			updatePieceMotion(MoveType.DROP, dTime);
 		}
 	}
 	
 	private void initMotionCountdown() {
 		motionCountdown = 0;
 	}
 	
 	private void initFallCountdown() {
 		fallCountdown = 0;
 	}
 	
 	void updateFallCountdown(double dTime) {
 		fallCountdown -= dTime;
 	}
 	void updateMotionCountdown(double dTime) {
 		motionCountdown -= dTime;
 	}
 	boolean isFallTimeOut() {
 		return fallCountdown <= 0.;
 	}
 	boolean isMotionTimeOut() {
 		return motionCountdown <= 0.;
 	}
 	
 	void resetFallCountdown() {
 		fallCountdown = 50 * (11 - state.getLevel());
 	}
 	
 	void resetMotionCountdown(MoveType movetype) {
 		switch(movetype) {
 			case DROP: {
 				motionCountdown = DROP_MOTION_DELAY;
 				break;	
 			}
 		}
 	}
 	
 	public class MenuListener implements ActionListener {
 	    public void actionPerformed(ActionEvent e) {
 	        String command = e.getActionCommand();
 	        if (command.equals("EXIT")) {
 	            mainApplication.exit();
 	        } else {
 	            // set the start level for the new game
 	            startLevel = Integer.parseInt(command);
 	            // start the new game and loop in a new thread
                 SwingWorker controllerThread = 
                     new SwingWorker<Integer, Void>() {
                         public Integer doInBackground() {
                             startGame();
                             gameLoop();
                             return (new Integer(0));
                         }
 
                         public void done() {
                             stopGame();
                         }
                     };
                 controllerThread.execute();
 	        }
 	    }
 	}
 	
 	public class BoardController implements BoardListener {
 		public void fullRowDetected(FullRowEvent e) {
 			state.killRows(e.fullRows);
 			GameSound.playLineClear(e.nRows);
 			state.incLinesCompleted(e.nRows);
 			state.incScore(e.nRows);
 		}
 		public void land() {
 		}		
 	}
 	
 	public class StateController implements StateListener {
 		public void levelUp() {
 			GameSound.playLevelUp();
 		}
 	}
 	
 	public class KeyInputHandler extends KeyAdapter {
 		public void keyPressed(KeyEvent e) {
 			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
 				// left move
 				leftPressed = true;
 				if (state.canMovePiece(MoveType.LEFT)) {
 					state.movePiece(MoveType.LEFT);
 				}
 			} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
 				// right move
 				rightPressed = true;
 				if (state.canMovePiece(MoveType.RIGHT)) {
 					state.movePiece(MoveType.RIGHT);
 				}
 			} else if (e.getKeyCode() == KeyEvent.VK_UP) {
 				// rotate
 				rotatePressed = true;
 				if (state.canMovePiece(MoveType.ROTATE)) {
 					state.movePiece(MoveType.ROTATE);
 					GameSound.playRotate();
 				}
 			} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
 				// soft drop
 				if (dropPressed == false) {
 					dropPressed = true;
 					GameSound.playDrop(state.getLevel());
 				}
 			} else if (e.getKeyCode() == KeyEvent.VK_TAB){
 				// hard drop
 				GameSound.playHardDrop();
 				state.hardDropPiece();
 			} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
 				// pause
 				if (state.isPaused()) {
 					state.resume();
 				} else {
 					state.pause();
 				}
 			} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
 				// exit
 				running = false;
 			}
 			keyPressTime = e.getWhen();
 		} 
 
 		public void keyReleased(KeyEvent e) {			
 			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
 				leftPressed = false;
 			} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
 				rightPressed = false;
 			} else if (e.getKeyCode() == KeyEvent.VK_UP) {
 				rotatePressed = false;
 			} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
 				dropPressed = false;
 				GameSound.stopDrop();
 			}
 			longKeyPress = false;
 		}
 	}
 }
 
