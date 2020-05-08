 package game;
 
 /**
  * @author Hung <hnl5010@psu.edu>
  * @author Endrit <eqa5029@psu.edu>
  * @author max <maxdeliso@gmail.com>
  *
  */
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.image.BufferStrategy;
 import java.util.Iterator;
 import java.util.concurrent.LinkedBlockingDeque;
 
 public class GameThread implements Runnable {
 	/* the various input operations possible */
 	public enum GameInputOperation {
 		MOUSE_MOVED_LEFT,
 		MOUSE_MOVED_RIGHT,
 		MOUSE_MOVED_UP,
 		MOUSE_MOVED_DOWN,
 		MOUSE_CLICK_LEFT,
 		MOUSE_CLICK_RIGHT,
 		KEYBOARD_W_DOWN,
 		KEYBOARD_A_DOWN,
 		KEYBOARD_S_DOWN,
 		KEYBOARD_D_DOWN,
 		KEYBOARD_W_UP,
 		KEYBOARD_A_UP,
 		KEYBOARD_S_UP,
 		KEYBOARD_D_UP,
 		KEYBOARD_SPACE_DOWN,
 		KEYBOARD_SPACE_UP,
 		KEYBOARD_P_DOWN,
 		KEYBOARD_P_UP,
 		KEYBOARD_F_DOWN,
 		KEYBOARD_F_UP,
 		KEYBOARD_ESC_DOWN;
 	}
 
 	/* the game state enumeration */
 	public enum GameState {
 		START,
 		PLAYING,
 		HIGH_SCORE,
 		PAUSED;
 	}
 
 	/* private member variables */
 	private GameCanvas gameCanvas;
 	private BufferStrategy bufferStrategy;
 	private boolean isRunning;
 	private boolean listeningForInput;
 	private long cycleTime;
 	private GameState gameState;
 	private GameTimer gameTimer;
 	private GameFrame gameFrame;
 	private boolean isShowing;
 	private Point mousePosition;
 	private boolean isWaiting;
 	private long framesPerSecond;
 
 	/* the data structure for passing input between swing and the game thread*/
 	private LinkedBlockingDeque<GameInputOperation> blockingInputQueue;
 	/* the data structure which holds all the game objects */
 	private LinkedBlockingDeque<GameObject> gameObjectList;
 
 	/**
 	 * This function constructs a new game thread.
 	 * One one game thread is needed per instance of the game, but additional
 	 * threads are created via the timer.
 	 * @param gameCanvas
 	 */
 	public GameThread(GameCanvas gameCanvas)
 	{
 		Game.log("initializing game thread");
 		blockingInputQueue = new LinkedBlockingDeque<GameInputOperation>();
 		gameObjectList = new LinkedBlockingDeque<GameObject>();
 
 		this.gameCanvas = gameCanvas;
 		isRunning = false;
 		listeningForInput = false;
 
 		gameState = GameState.START;
 		gameFrame = null;
 		isShowing = true;
 		mousePosition = null;
 		isWaiting = false;
 	}
 
 	/**
 	 * This function gives the game thread a reference to the game frame.
 	 * Note: It must be called before the main loop is entered.
 	 * @param gameFrame
 	 */
 	public void setMainFrame( GameFrame gameFrame )
 	{
 		this.gameFrame = gameFrame;
 	}
 
 	/**
 	 * This function adds a new input operation onto the tail of the input queue.
 	 * @param gio the game input operation which we want to enqueue into the blocking input queue
 	 */
 	public void enqueueInputOperation( GameInputOperation gio )
 	{
 		if( listeningForInput )
 		{
 			blockingInputQueue.add(gio);
 		}
 	}
 
 	/**
 	 * This function gets called by the mouse listener to update the mouse position on the game canvas.
 	 * Whenever this function gets called, a mouse moved event gets sent to the operation queue
 	 * so that the new value is processed.
 	 * @param mp the new mouse position.
 	 */
 	public void updateMousePosition( Point mp )
 	{
 		mousePosition = mp;
 	}
 
 	/**
 	 * This function tells the game thread to enter into a wait state and suspend 
 	 * execution until such time as the frame initialization is complete.
 	 * It is used to delay the execution of the main game loop to avoid a race condition.
 	 */
 	public synchronized void waitForNotify()
 	{
 		try {
 			isWaiting = true;
 			wait();
 		} catch (InterruptedException e) {
 			Game.logError("synchronization error");
 		}
 	}
 
 	/**
 	 * This function releases the lock that the wait function entered into.
 	 * It is called when the rest of the initialization finishes.
 	 */
 	public synchronized void relinquishMonitor()
 	{
 		notify();
 		isWaiting = false;
 	}
 
 	public synchronized boolean isWaiting()
 	{
 		return isWaiting;
 	}
 
 	/**
 	 * Main game loop
 	 */
 	public void run()
 	{
 		long startTime, renderTime;
 		
 		Game.log("entering wait state");
 		waitForNotify();
 
 		Game.log("entering main game loop");
 		cycleTime = System.currentTimeMillis();
 		bufferStrategy = gameCanvas.getBufferStrategy();
 		isRunning = true;
 
 		if( gameFrame == null )
 		{
 			Game.logError("Please set the game frame before invoking the run method");
 			System.exit(1);
 		}
 
 		while( isRunning )
 		{
 			startTime =  System.currentTimeMillis();
 			
 			updateGameState();
 			updateGraphics();
 			synchFramerate();
 			
 			renderTime = System.currentTimeMillis() - startTime;
			framesPerSecond = (long ) ( 1000.0 / renderTime );
 			Game.logFrequent("approx FPS: " + framesPerSecond);
 		}
 
 		Game.log("shutting down...");
 		gameCanvas.setVisible(false);
 		ignoreInput();
 		System.exit(0);
 	}
 
 	/**
 	 * This function tells the input queue to accept new items.
 	 */
 	public synchronized void acceptInput()
 	{
 		Game.log("now accepting input");
 		listeningForInput = true;
 	}
 
 	/**
 	 * This function tells the input queue to ignore any new items.
 	 */
 	public synchronized void ignoreInput()
 	{
 		Game.log("now ignoring input");
 		listeningForInput = false;
 	}
 
 	/**
 	 * This function is called by the frame listener when the frame, and therefore
 	 * the canvas, is resized. Some game objects may be invalidated or need 
 	 * repositioning after this event, and those changes are handled here.
 	 */
 	public synchronized void notifyResize()
 	{
 		Game.log("caught resize notification: " + gameCanvas.getSize());
 		//TODO: wrap game objects outside of new boundaries
 	}
 
 	/** 
 	 * This function gets called by one of the game objects
 	 * and tells the main thread to add a new enemy to the object list
 	 */
 	public void spawnEnemy()
 	{
 		Game.log("Timer event SPAWN_ENEMIES");
 	}
 
 	/** 
 	 * This function gets called by one of the game objects
 	 * and tells the main thread to add a new asteroid to the object list
 	 */
 	public void spawnAsteroid()
 	{
 		Game.log("Timer event SPAWN_ASTEROIDS");
 
 		/* note: the asteroids only get spawned along the left edge of the screen */
 		gameObjectList.add(  
 				new GameAsteroid ( 
 						new Point(
 								(int) (Math.random() * gameCanvas.getSize().width *.2), 
 								(int) (Math.random() * gameCanvas.getSize().height * .9) ), 
 								Math.random() * 2.0, 
 								Math.random() * Math.PI * 2,
 								0.0 ));
 
 	}
 
 	/** 
 	 * This function gets called by one of the game objects
 	 * and tells the main thread to add a new powerup to the object list
 	 */
 	public void spawnPowerup()
 	{
 		Game.log("Timer event SPAWN_POWERUP");
 	}
 	
 	public void fireBullet(){
 		gameObjectList.add(((GameShip)gameObjectList.getFirst()).fire());
 	}
 
 	/**
 	 * This function gets called every frame to draw whatever needs to be
 	 * displayed. Different things get drawn depending on the game state.
 	 * All this function does is attempt to acquire the draw graphics
 	 * and then call render if it succeeds. Failures are reported
 	 * gracefully.
 	 */
 	private void updateGraphics() {
 
 		if( bufferStrategy != null && isRunning && isShowing )
 		{
 			
 			Graphics2D g = null;
 
 			try {
 				g = (Graphics2D) bufferStrategy.getDrawGraphics();
 				render(g);
 			} catch (Exception e ) {
 				Game.logError("Could not acquire graphics: " + e);
 			} finally {
 				g.dispose();
 			}
 
 			bufferStrategy.show();
 		}
 	}
 
 
 	/**
 	 * This function takes the graphics context which the updateGraphics()
 	 * function acquired, and draw to it.
 	 * @param g the graphics context
 	 */
 	private void render( Graphics2D g )
 	{
 		g.setColor(Color.BLACK);
 		g.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
 
 		g.setColor(Color.GRAY);
 		g.drawString( "Canvas dimensions: " + 
 				gameCanvas.getWidth() + " " + 
 				gameCanvas.getHeight(), 15, 30);
 		g.drawString( "Frames per second (approx): " + framesPerSecond, 15, 45);
 
 		switch( gameState )
 		{
 		case START:
 			g.setColor(Color.GRAY);
 			g.drawString( "Start Screen. click to start.", 15, 15);
 			break;
 		case PAUSED:
 			g.setColor(Color.GRAY);
 			g.drawString( "Paused. p resumes" , 15, 15);
 		case PLAYING:
 			Iterator<GameObject> i = gameObjectList.iterator();
 			while( i.hasNext() )
 			{
 				i.next().drawObject(g);
 			}
 			break;
 		case HIGH_SCORE:
 			g.setColor(Color.GRAY);
 			g.drawString( "High score screen", 15, 15);
 			break;
 		}
 	}
 
 	/** 
 	 * This function resets the game state 
 	 * to the title screen
 	 */
 	public void resetGame()
 	{
 		Game.log("switching state to START");
 		gameState = GameState.START;
 		gameCanvas.showCursor();
 	}
 
 	/** 
 	 * This function switches the game state
 	 * to the playing state and starts the game timer.
 	 */
 	public void startGame()
 	{
 		Game.log("switching state to PLAYING");
 		gameState = GameState.PLAYING;
 		gameCanvas.hideCursor();
 		gameTimer = new GameTimer( this );
 
 		gameObjectList.add(  
 				new GameShip ( 
 						new Point(
 								gameCanvas.getSize().width/2, 
 								gameCanvas.getSize().height/2 ), 
 								GameShip.GameShipType.PLAYER_SHIP));
 	}
 
 	/**
 	 * This function switches the game state to paused
 	 * and pauses the game timer.
 	 */
 	public void pauseGame()
 	{
 		if( gameState == GameState.PLAYING )
 		{
 			Game.log("switching state to PAUSED");
 
 			/* clear player movement */
 			gameObjectList.getFirst().accel = 0.0;
 			gameObjectList.getFirst().headingDelta = 0.0;
 
 			gameState = GameState.PAUSED;
 			gameCanvas.showCursor();
 			gameTimer.pause();
 		} else {
 			Game.logError("called pause when not in playing state");
 		}
 	}
 
 	/**
 	 * This function switches the game state back to
 	 * playing from paused.
 	 */
 	public void resumeGame()
 	{
 		if( gameState == GameState.PAUSED )
 		{
 			Game.log("switching state to PLAYING");
 			gameState = GameState.PLAYING;
 			gameCanvas.hideCursor();
 			gameTimer.resume();
 		} else {
 			Game.logError("called resume when not in the paused state");
 		}
 	}
 
 	/**
 	 * This function switches the game state back to
 	 * playing from paused.
 	 */
 	public void stopGame()
 	{
 		Game.log("switching state to HIGH_SCORE");
 		gameState = GameState.HIGH_SCORE;
 		gameCanvas.hideCursor();
 		gameTimer.kill();
 		gameObjectList.clear();
 	}
 
 	
 	/**
 	 * This function flips the main loop flag to false
 	 * so that the next time the main loop executes it will
 	 * fall through and perform cleanup operations. Note
 	 * that this does not immediately terminate the game.
 	 */
 	public synchronized void postGameShutdown()
 	{
 		Game.log("received shutdown request");
 		ignoreInput();
 		gameFrame.destroy();
 
 		if( gameTimer != null )
 		{
 			gameTimer.kill();
 		}
 		isRunning = false;
 	}
 
 	/**
 	 * This function performs updates on all the game objects,
 	 * keeping their state consistent and making them interact in various ways.
 	 * The 'brains' of the game go here. All input is processed here and
 	 * other state information is handled.
 	 */
 	private void updateGameState() {
 		processInputQueue();
 
 		if( isRunning ) /* the input queue might have switched the run state */
 		{
 			switch( gameState )
 			{
 			case START:
 
 				break;
 			case PLAYING:
 				/* update all the game objects */
 				GameObject currentObject;
 				Iterator<GameObject> i = gameObjectList.iterator();
 				while( i.hasNext() )
 				{
 					currentObject = i.next();
 					if(currentObject.isValid){
 						currentObject.update(gameCanvas.getSize());
 					}else{
 		                i.remove();
 					}
 				}
 
 				GameObject go_inside, go_outside;
 				int inside_index, outside_index;
 				inside_index = 0;
 				for (Iterator<GameObject> it_outside = gameObjectList.iterator(); 
 				it_outside.hasNext (); inside_index++) {
 					go_outside = it_outside.next ();
 
 					outside_index = 0;
 					for (Iterator<GameObject> it_inside = gameObjectList.iterator(); 
 					it_inside.hasNext () && outside_index < inside_index; outside_index++ ) {
 						go_inside = it_inside.next();
 
 						if( go_inside.isTouching(go_outside)) {
 							Game.log("COLLISION BETWEEN " + go_inside + " and " + go_outside );
 
 							if( go_inside instanceof GameShip ) {
 								if( go_outside instanceof GameShip ) {
 									
 									// ship hit ship
 								} else if( go_outside instanceof GameAsteroid ) {
 									if( gameObjectList.getFirst().equals(go_inside))
 									{
 										stopGame();
 									}
 									
 								} else if( go_outside instanceof GameBullet ) {
 									// ship hit bullet
 								}	
 							} else if( go_inside instanceof GameAsteroid ) {
 								if( go_outside instanceof GameAsteroid ) {
 									collideAsteroids((GameAsteroid)go_outside, (GameAsteroid)go_inside );
 								} else if( go_outside instanceof GameBullet ) {
 									go_outside.isValid = false;
 									
 									GameAsteroid newAsteroids[] = ((GameAsteroid) go_inside).split();
 									
 									if( newAsteroids != null )
 									{
 										for( int ii = 0; ii < newAsteroids.length; ii++ )
 											gameObjectList.add( newAsteroids[ii]);
 									}
 									//TODO: schedule newAsteroids for inclusion into the gameObjectList
 									
 							
 									go_inside.isValid = false;
 								}
 							} else if( go_inside instanceof GameBullet ) {
 								if( go_outside instanceof GameBullet ) {
 									// bullet hit bullet
 								}
 							}
 
 						} else {
 							Game.logFrequent("No collision between " + go_inside + " and " + go_outside );
 						}
 					}
 				}
 
 				break;
 			case PAUSED:
 				break;
 			case HIGH_SCORE:
 				break;
 			}
 		}
 	}
 
 	
 	public void collideAsteroids(GameAsteroid asteroidOne, GameAsteroid asteroidTwo )
 	{
 		//TODO; code in some physics
 	}
 	
 	public void processMouseRotation( GameInputOperation gio )
 	{
 		switch( gio )
 		{
 		case MOUSE_MOVED_LEFT:
 		
 			break;
 		case MOUSE_MOVED_RIGHT:
 	
 			break;
 		case MOUSE_MOVED_UP:
 			
 			break;
 		case MOUSE_MOVED_DOWN:
 			
 			break;
 		default:
 			Game.logError("invalid input operation sent to processMouseRotation");
 			break;
 		}
 	}
 	
 	private void processInputQueue()
 	{
 		GameInputOperation gio;
 		boolean processedItem;
 		
 		while(  blockingInputQueue.size() > 0 ) 
 		{
 			gio = blockingInputQueue.getFirst();
 			processedItem = false;
 			Game.logFrequent("caught "+gio);
 
 			if( gameState == GameState.START && !processedItem) {
 				switch( gio )
 				{
 				case MOUSE_CLICK_LEFT:
 				case MOUSE_CLICK_RIGHT:
 					processedItem = true;
 					startGame();
 					break;
 				case KEYBOARD_F_DOWN:
 					processedItem = true;
 					gameFrame.toggleFullscreen();
 					break;
 				case KEYBOARD_ESC_DOWN:
 					processedItem = true;
 					postGameShutdown();
 					break;
 				}
 			} else if( gameState == GameState.PLAYING && !processedItem) {
 				switch( gio )
 				{
 				case MOUSE_MOVED_LEFT:
 					processMouseRotation( GameInputOperation.MOUSE_MOVED_LEFT );
 					processedItem = true;
 					break;
 				case MOUSE_MOVED_RIGHT:
 					processMouseRotation( GameInputOperation.MOUSE_MOVED_RIGHT );
 					processedItem = true;
 					break;
 				case MOUSE_MOVED_UP:
 					processMouseRotation( GameInputOperation.MOUSE_MOVED_UP );
 					processedItem = true;
 					break;
 				case MOUSE_MOVED_DOWN:
 					processMouseRotation( GameInputOperation.MOUSE_MOVED_DOWN );
 					processedItem = true;
 					break;
 				case MOUSE_CLICK_LEFT:
 					processedItem = true;
 					fireBullet();
 					break;
 				case MOUSE_CLICK_RIGHT:
 					processedItem = true;
 					break;
 				case KEYBOARD_W_DOWN:
 					processedItem = true;
 					gameObjectList.getFirst().accel = 0.1;
 					break;
 				case KEYBOARD_A_DOWN:
 					processedItem = true;
 					gameObjectList.getFirst().headingDelta = -0.1;
 					break;
 				case KEYBOARD_S_DOWN:
 					processedItem = true;
 					gameObjectList.getFirst().accel = -0.1;
 					break;
 				case KEYBOARD_D_DOWN:
 					processedItem = true;
 					gameObjectList.getFirst().headingDelta = 0.1;
 					break;
 				case KEYBOARD_W_UP:
 					processedItem = true;
 					gameObjectList.getFirst().accel = 0.0;
 					break;
 				case KEYBOARD_A_UP:
 					processedItem = true;
 					gameObjectList.getFirst().headingDelta = 0;
 					break;
 				case KEYBOARD_S_UP:
 					processedItem = true;
 					gameObjectList.getFirst().accel = 0;
 					break;
 				case KEYBOARD_D_UP:
 					processedItem = true;
 					gameObjectList.getFirst().headingDelta = 0;
 					break;
 				case KEYBOARD_SPACE_DOWN:
 					processedItem = true;
 					fireBullet();
 					break;
 				case KEYBOARD_P_DOWN:
 					processedItem = true;
 					pauseGame();
 					break;
 				case KEYBOARD_F_DOWN:
 					processedItem = true;
 					pauseGame();
 					gameFrame.toggleFullscreen();
 					break;
 				case KEYBOARD_ESC_DOWN:
 					processedItem = true;
 					postGameShutdown();
 					break;
 				}
 			} else if( gameState == GameState.PAUSED && !processedItem) {
 				switch( gio )
 				{
 				case KEYBOARD_P_DOWN:
 					processedItem = true;
 					resumeGame();
 					break;
 				case KEYBOARD_F_DOWN:
 					processedItem = true;
 					gameFrame.toggleFullscreen();
 					break;
 				case KEYBOARD_ESC_DOWN:
 					processedItem = true;
 					postGameShutdown();
 					break;
 				}
 			} else if( gameState == GameState.HIGH_SCORE && !processedItem) {
 				switch( gio )
 				{
 				case MOUSE_CLICK_LEFT:
 				case MOUSE_CLICK_RIGHT:
 					processedItem = true;
 					resetGame();
 					break;
 				case KEYBOARD_F_DOWN:
 					processedItem = true;
 					gameFrame.toggleFullscreen();
 					break;
 				case KEYBOARD_ESC_DOWN:
 					processedItem = true;
 					postGameShutdown();
 					break;
 				}
 			}
 			blockingInputQueue.remove();
 		}
 	}
 
 	public Point getAbsoluteScreenMidpoint()
 	{
 		Point midpoint = gameFrame.getLocationOnScreen();
 		
 		midpoint.x += gameFrame.getSize().width/2;
 		midpoint.y += gameFrame.getSize().height/2;
 		
 		return midpoint;
 	}
 	
 	public Dimension getCanvasDimension()
 	{
 		return gameCanvas.getSize();
 	}
 	
 	/**
 	 * This function prevents the updates from occurring too frequently.
 	 */
 	private void synchFramerate() {
 		cycleTime = cycleTime + Game.FRAME_DELAY;
 		long difference = cycleTime - System.currentTimeMillis();
 
 		try {
 			Thread.sleep(Math.max(0, difference));
 		}
 		catch(InterruptedException e) {
 			Game.logError("game thread interrupted whilst attempting to synch framerate");
 		}
 	}
 }
