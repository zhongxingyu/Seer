 package game;
 
 /**
  * 
  * @author max <maxdeliso@gmail.com>
  *
  */
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.image.BufferStrategy;
 import java.util.LinkedList;
 
 public class GameThread implements Runnable {
 	public enum GameState {
 		START,
 		PLAYING,
 		HIGH_SCORE,
 		PAUSED;
 	}
 
 	private GameCanvas gameCanvas;
 	private BufferStrategy bufferStrategy;
 	private boolean isRunning;
 	private boolean listeningForInput;
 	private long cycleTime;
 	private GameState gameState;
 	private Point mousePosition;
     private GameTimer gameTimer;
 	
 	private LinkedList<GameInputOperation> inputQueue;
 
 	public GameThread(GameCanvas gameCanvas)
 	{
 		inputQueue = new LinkedList<GameInputOperation>();
 
 		this.gameCanvas = gameCanvas;
 		isRunning = false;
 		listeningForInput = false;
 
 		gameState = GameState.START;
 	}
 
 	public synchronized void enqueueInputOperation( GameInputOperation gio )
 	{
 		if( listeningForInput )
 		{
 			inputQueue.add( gio );
 		}
 	}
 
 	/**
 	 * Main game loop
 	 */
 	public void run()
 	{
 		cycleTime = System.currentTimeMillis();
 		bufferStrategy = gameCanvas.getBufferStrategy();
 		isRunning = true;
 
 		while( isRunning )
 		{
 			updateGameState();
 			updateGraphics();
 			synchFramerate();
 		}
 
 		gameCanvas.setVisible(false);
 		ignoreInput();
 		System.exit(0);
 	}
 
 	public synchronized void acceptInput()
 	{
 		listeningForInput = true;
 	}
 
 	public synchronized void ignoreInput()
 	{
 		listeningForInput = false;
 	}
 
 	public synchronized void notifyResize()
 	{
 		
 	}
 
 	public synchronized void updateMousePosition( Point nmp )
 	{
 		mousePosition = nmp;
 	}
 
 	public void spawnEnemy()
 	{
 		//TODO: fix stub
 	}
 	
 	public void spawnAsteroid()
 	{
 		//TODO: fix stub
 	}
 	
 	public void spawnPowerup()
 	{
 		//TODO: fix stub
 	}
 	/**
 	 * updateGraphics() gets called to update the graphics every frame
 	 *
 	 */
 	private void updateGraphics() {
 		Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
 
 		
 		g.setColor(Color.WHITE);
 		g.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
 
 		if( gameState == GameState.START ) {
 			g.setColor(Color.RED);
 			g.drawString( "Start Screen. click to start.", 15, 15);
 		} else if( gameState == GameState.PLAYING ) {
 			g.setColor(Color.RED);
 			g.drawString( "Game running: " + cycleTime, 15, 15);
 			if( mousePosition != null )
 			{
 				g.fillRect( mousePosition.x-2, mousePosition.y-2, 3, 3);
 			}
 		} else if( gameState == GameState.PAUSED ) {
 			g.setColor(Color.RED);
 			g.drawString( "Game paused", 15, 15);
 		} else if( gameState == GameState.HIGH_SCORE ) {
 			g.setColor(Color.RED);
 			g.drawString( "High score screen", 15, 15);
 		}
 
 		g.dispose();
 		bufferStrategy.show();
 	}
 
 	public void resetGame()
 	{
 		System.out.println("Debug: switching state to START");
 		gameState = GameState.START;
 		gameCanvas.showCursor();
 	}
 	
 	public void startGame()
 	{
 		System.out.println("Debug: switching state to PLAYING");
 		gameState = GameState.PLAYING;
 		gameCanvas.hideCursor();
		gameTimer = new GameTimer( this );
 	}
 	
 	public void pauseGame()
 	{
 		System.out.println("Debug: switching state to PAUSED");
 		gameState = GameState.PAUSED;
 		gameCanvas.showCursor();
 		gameTimer.pause();
 	}
 	
 	public void resumeGame()
 	{
 		System.out.println("Debug: switching state to PLAYING");
 		gameState = GameState.PLAYING;
 		gameCanvas.hideCursor();
 	    gameTimer.resume();
 	}
 	
 	public synchronized void shutdownGame()
 	{
 		gameTimer.kill();
 		isRunning = false;
 	}
 	
 	/**
 	 * updateGameState() gets called to update the game objects every frame
 	 *
 	 */
 	private void updateGameState() {
 		GameInputOperation gio;
 
 		/* maybe put a limit on the number of processed inputs per update */
 		while( inputQueue.size() > 0 ) 
 		{
 			gio = inputQueue.getFirst();
 
 			System.out.println( cycleTime+" "+gio);
 			if( gameState == GameState.START ) {
 				switch( gio )
 				{
 				case MOUSE_CLICK_LEFT:
 				case MOUSE_CLICK_RIGHT:
 					startGame();
 					return;
 				case KEYBOARD_ESC_DOWN:
 					shutdownGame();
 					break;
 				}
 			} else if( gameState == GameState.PLAYING ) {
 				switch( gio )
 				{
 				case MOUSE_MOVED:
 					break;
 				case MOUSE_CLICK_LEFT:
 					break;
 				case MOUSE_CLICK_RIGHT:
 					break;
 				case KEYBOARD_W_DOWN:
 					break;
 				case KEYBOARD_A_DOWN:
 					break;
 				case KEYBOARD_S_DOWN:
 					break;
 				case KEYBOARD_D_DOWN:
 					break;
 				case KEYBOARD_W_UP:
 					break;
 				case KEYBOARD_A_UP:
 					break;
 				case KEYBOARD_S_UP:
 					break;
 				case KEYBOARD_D_UP:
 					break;
 				case KEYBOARD_SPACE_DOWN:
 					break;
 				case KEYBOARD_SPACE_UP:
 					break;
 				case KEYBOARD_P_DOWN:
 					pauseGame();
 					break;
 				case KEYBOARD_P_UP:
 					break;
 				case KEYBOARD_ESC_DOWN:
 					shutdownGame();
 					break;
 				}
 			} else if( gameState == GameState.PAUSED ) {
 				switch( gio )
 				{
 				case KEYBOARD_P_DOWN:
 					resumeGame();
 					break;
 				case KEYBOARD_ESC_DOWN:
 					shutdownGame();
 					break;
 				}
 			} else if( gameState == GameState.HIGH_SCORE ) {
 				switch( gio )
 				{
 				case MOUSE_CLICK_LEFT:
 				case MOUSE_CLICK_RIGHT:
 					resetGame();
 					return;
 				case KEYBOARD_ESC_DOWN:
 					shutdownGame();
 					break;
 				}
 			}
 			inputQueue.remove();
 		}
 	}
 
 	private void synchFramerate() {
 		cycleTime = cycleTime + Game.FRAME_DELAY;
 		long difference = cycleTime - System.currentTimeMillis();
 		try {
 			Thread.sleep(Math.max(0, difference));
 		}
 		catch(InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 }
