 package ca.couchware.wezzle2d;
 
 import ca.couchware.wezzle2d.util.Util;
 import ca.couchware.wezzle2d.tile.*;
 import ca.couchware.wezzle2d.animation.*;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.event.KeyEvent;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 /**
  * The main hook of our game. This class with both act as a manager for the
  * display and central mediator for the game logic.
  * 
  * Display management will consist of a loop that cycles round all entities in
  * the game asking them to move and then drawing them in the appropriate place.
  * With the help of an inner class it will also allow the player to control the
  * main ship.
  * 
  * As a mediator it will be informed when entities within our game detect events
  * (e.g. alien killed, played died) and will take appropriate game actions.
  * 
  * @author Kevin Glass
  */
 public class Game extends Canvas implements GameWindowCallback
 {	    
     /**
 	 * The manager in charge of maintaining the board.
 	 */
 	private BoardManager boardMan;
 	
     /**
      * The layer manager.
      */
     public LayerManager layerMan;
     
     /** 
      * The manager in charge of the moves. 
      */
     public MoveManager moveMan;	
     
 	/**
 	 * The manager in charge of moving the piece around with the
 	 * pointer and drawing the piece to the board.
 	 */
 	public PieceManager pieceMan;
 	
     /** 
      * The manager in charge of loading and saving properties.
      */
     public PropertyManager propertyMan;
     
      /** 
       * The manager in charge of score.
       */
     public ScoreManager scoreMan;
 
     /** 
      * The manager in charge of sound.
      */
     public SoundManager soundMan;
     
 	/** 
 	 * The manager in charge of keeping track of the time. 
 	 */
 	public TimeManager timeMan;
 	
     /**
      * If true, refactor will be activated next loop.
      */
 	private boolean activateRefactor = false;
     
     /**
      * If true, the board is currently being refactored downwards.
      */
 	private boolean refactorDownInProgress = false;
     
     /**
      * If true, the board is currently being refactored leftward.
      */
 	private boolean refactorLeftInProgress = false;
     
     /**
      * The speed of the upcoming refactor.
      */
     private int refactorSpeed = 0;
     
     /**
      * If true, a line removal will be activated next loop.
      */
     private boolean activateLineRemoval = false;
     
     /**
      * If true, a line removal is in progress.
      */
     private boolean tileRemovalInProgress = false;
     
     /**
      * The set of tile indices that will be removed.
      */
     private Set tileRemovalSet;
     
     /**
      * If true, a bomb removal will be activated next loop.
      */
     private boolean activateBombRemoval = false;
     
     /**
      * The set of bomb tile indices that will be removed.
      */
     private Set bombRemovalSet;
     
 	/**
 	 * The time at which the last rendering looped started from the point of
 	 * view of the game logic.
 	 */
 	private long lastLoopTime = SystemTimer.getTime();
 	
 	/** 
      * The window that is being used to render the game. 
      */
 	private GameWindow window;
 
 	/** 
      * The time since the last record of FPS. 
      */
 	private long lastFramesPerSecondTime = 0;
 	
 	/** 
      * The recorded FPS. 
      */
 	private int framesPerSecond;
 
 	/** 
      * The normal title of the window. 
      */
 	private String windowTitle = "Wezzle";
 	
     /**
      * The text color.
      */
     private Color textColor = new Color(252, 233, 45);
     
 	/** 
      * The timer text. 
      */
 	private Text timerText;
         
     /** 
      * The score text.
      */
     private Text scoreText;
     
     /** 
      * The high score text. 
      */
     private Text highScoreText;
     
     /**
      * The level text.
      */
     private Text levelText;
     
     /** 
      * The move count text.
      */
     private Text moveCountText;
         
 	/**
 	 * Construct our game and set it running.
 	 * 
 	 * @param renderingType
 	 *            The type of rendering to use (should be one of the contansts
 	 *            from ResourceFactory)
 	 */
 	public Game(int renderingType) 
 	{
 		// Create a window based on a chosen rendering method.
 		ResourceFactory.get().setRenderingType(renderingType);		
 
 		final Runnable r = new Runnable()
 		{
 			public void run()
 			{
 				window = ResourceFactory.get().getGameWindow();
 				window.setResolution(800, 600);
 				window.setGameWindowCallback(Game.this);
 				window.setTitle(windowTitle);
 			}		
 		};
 		
 		try
 		{
 			javax.swing.SwingUtilities.invokeAndWait(r);
 		}
 		catch (InterruptedException e)
 		{
 			Util.handleException(e);
 		}
 		catch (InvocationTargetException e)
 		{
 			Util.handleException(e);
 		}		
 	}
 
 	public void startRendering()
 	{
 		window.startRendering();
 	}
     
 	/**
 	 * Initialize the common elements for the game
 	 */
 	public void initialize()
 	{
         // Initialize line index set.
         tileRemovalSet = new HashSet();
         
         // Initialize bomb index set.
         bombRemovalSet = new HashSet();
         
         // Create the layer manager with 2 initial layers.
         layerMan = new LayerManager(2);
         
 		// Create the board manager.
 		boardMan = new BoardManager(layerMan, 272, 139, 8, 10);
 		boardMan.generateBoard(40, 10, 6);
 		
 		// Create the piece manager.
 		pieceMan = new PieceManager(boardMan);
         layerMan.add(pieceMan, 1);
 		window.addMouseListener(pieceMan);
 		window.addMouseMotionListener(pieceMan);	
 	
         // Create the property manager. Must be done before Score manager.
         propertyMan = new PropertyManager();
         
         // Create the score manager.
         scoreMan = new ScoreManager(boardMan, propertyMan);
 
         // Create the sound manager.
         soundMan = new SoundManager();
         
         // Create the move manager.
         moveMan = new MoveManager();
                                                                                           
 		// Set up the timer text.
 		timerText = ResourceFactory.get().getText();
         timerText.setXYPosition(400, 100);
 		timerText.setSize(50);
 		timerText.setAnchor(Text.BOTTOM | Text.HCENTER);
 		timerText.setColor(textColor);
         layerMan.add(timerText, 0);
                 
         // Set up the score text.
         scoreText = ResourceFactory.get().getText();
         scoreText.setXYPosition(126, 400); 
         scoreText.setSize(20);
         scoreText.setAnchor(Text.BOTTOM | Text.HCENTER);
         scoreText.setColor(textColor);
         layerMan.add(scoreText, 0);
         
         // Set up the high score text.
         highScoreText = ResourceFactory.get().getText();
         highScoreText.setXYPosition(126, 270);
         highScoreText.setSize(20);
         highScoreText.setAnchor(Text.BOTTOM | Text.HCENTER);
         highScoreText.setColor(textColor);
         layerMan.add(highScoreText, 0);
         
         // Set up the level text.
         levelText = ResourceFactory.get().getText();
         levelText.setXYPosition(669, 270);
         levelText.setSize(20);
         levelText.setAnchor(Text.BOTTOM | Text.HCENTER);
         levelText.setColor(textColor);
         layerMan.add(levelText, 0);
         
         // Set up the move count text.
         moveCountText = ResourceFactory.get().getText();
         moveCountText.setXYPosition(669, 400);
         moveCountText.setSize(20);
         moveCountText.setAnchor(Text.BOTTOM | Text.HCENTER);
         moveCountText.setColor(textColor);
         layerMan.add(moveCountText, 0);
                         		
 		// Create the time manager.
 		timeMan = new TimeManager();
 
 		// Setup the initial game state.
 		startGame();
 	}
 
 	/**
 	 * Start a fresh game, this should clear out any old data and create a new
 	 * set.
 	 */
 	private void startGame()
 	{		
 		// Nothing here yet.
 	}
     
     public void runRefactor(int speed)
     {
         // Set the refactor flag.
         this.activateRefactor = true;
         this.refactorSpeed = speed;
     }
     
    public  void clearRefactor()
    {
        // Set the refactor flag.
        this.activateRefactor = false;
    }
    
    /**
     * A method to check if any refactoring is happening.
     * 
     * @return Whether any refactoring is happing.
     */
    public boolean isRefactoring()
    {
        return (this.refactorDownInProgress || this.refactorLeftInProgress
                || this.tileRemovalInProgress
                || this.activateBombRemoval || this.activateLineRemoval 
                || this.activateRefactor);
    }
 
 	/**
 	 * Notification that a frame is being rendered. Responsible for running game
 	 * logic and rendering the scene.
 	 */
 	public void frameRendering()
 	{
 		SystemTimer.sleep(lastLoopTime + 10 - SystemTimer.getTime());
 
 		// Work out how long its been since the last update, this
 		// will be used to calculate how far the entities should
 		// move this loop.
 		long delta = SystemTimer.getTime() - lastLoopTime;
 		lastLoopTime = SystemTimer.getTime();
 		lastFramesPerSecondTime += delta;
 		framesPerSecond++;
 
 		// Update our FPS counter if a second has passed.
 		if (lastFramesPerSecondTime >= 1000)
 		{
 			window.setTitle(windowTitle + " (FPS: " + framesPerSecond + ")");
 			lastFramesPerSecondTime = 0;
 			framesPerSecond = 0;
 		}
 		
 		// See if we need to activate the refactor.
 		if (activateRefactor == true)
 		{            
             // Hide piece.
             pieceMan.setVisible(false);
             
 			// Start down refactor.
 			boardMan.startShiftDown(refactorSpeed);
 			refactorDownInProgress = true;
 			
 			// Clear flag.
 			clearRefactor();
 		}
 		
 		// See if we're down refactoring.
 		if (refactorDownInProgress == true)
 		{
 			if (boardMan.moveAll(delta) == false)
 			{			
                 // Clear down flag.
 				refactorDownInProgress = false;
                 
 				// Synchronize board.
 				boardMan.synchronize();							
 				
 				// Start left refactor.
 				boardMan.startShiftLeft(refactorSpeed);
 				refactorLeftInProgress = true;								
 			}
 		} // end if
 		
 		// See if we're left refactoring.
 		if (refactorLeftInProgress == true)
 		{
 			if (boardMan.moveAll(delta) == false)
 			{
                 // Clear left flag.
 				refactorLeftInProgress = false;
                 
 				// Synchronize board.
 				boardMan.synchronize();		
                 
                 // Look for matches.
                 tileRemovalSet.clear();
                 boardMan.findXMatch(tileRemovalSet);
                 boardMan.findYMatch(tileRemovalSet);
                 
                 // If there are matches, score them, remove them and then
                 // refactor again.
                 if (tileRemovalSet.size() > 0)
                 {                                       
                     // Activate the line removal.
                     activateLineRemoval = true;                 
                 }
                 else
                 {
                    // Make sure the tiles are not still dropping.
                     if(pieceMan.isTileDropping() == false)
                     {
                         pieceMan.loadRandomPiece();   
                         pieceMan.setVisible(true);
                         
                         //Unpause the timer.
                         timeMan.resetTimer();
                         timeMan.unPause();
                         
                         // Reset the mouse.
                         pieceMan.setMouseLeftReleased(false);
                         pieceMan.setMouseRightReleased(false);
                     }
                 }
 			} // end if
             
             // Notify piece manager.
             pieceMan.notifyRefactored();
 		} // end if
 		       
         // If a line removal was activated.
         if (activateLineRemoval == true)
         {
             // Clear flag.
             activateLineRemoval = false;
 
             // Calculate score and play the sound.
             scoreMan.calculateLineScore(tileRemovalSet, 
                     ScoreManager.TYPE_LINE, 1);
             soundMan.play(SoundManager.LINE);
 
             // Make sure bombs aren't removed (they get removed
             // in a different step).
             bombRemovalSet = boardMan.scanBombs(tileRemovalSet);
             tileRemovalSet.removeAll(bombRemovalSet);
 
             // Start the line removal animations if there are any
             // non-bomb tiles.
             if (tileRemovalSet.size() > 0)
             {
                 for (Iterator it = tileRemovalSet.iterator(); it.hasNext(); )
                 {
                     TileEntity t = boardMan.getTile((Integer) it.next());
                     t.setAnimation(new ZoomOutAnimation(t));
                 }
 
                 // Set the flag.
                 tileRemovalInProgress = true;
             }
             // Otherwise, start the bomb processing.
             else
             {
                 activateBombRemoval = true;
             }
         }
                         
         // If a bomb removal is in progress.
         if (activateBombRemoval == true)
         {
             // Clear the flag.
             activateBombRemoval = false;
             
             // Get the tiles the bombs would affect.
             tileRemovalSet = boardMan.processBombs(bombRemovalSet);
             scoreMan.calculateLineScore(tileRemovalSet, 
                     ScoreManager.TYPE_BOMB, 1);
 
             // Extract all the new bombs.
             Set newBombRemovalSet = boardMan.scanBombs(tileRemovalSet);                                                
             newBombRemovalSet.removeAll(bombRemovalSet);
 
             // Remove all tiles that aren't new bombs.
             tileRemovalSet.removeAll(newBombRemovalSet);
             
             // Start the line removal animations.
             for (Iterator it = tileRemovalSet.iterator(); it.hasNext(); )
             {
                 TileEntity t = boardMan.getTile((Integer) it.next());
                 
                 if (t instanceof BombTileEntity)
                 {
                     t.setAnimation(new ExplosionAnimation(t, layerMan));
                     this.soundMan.play(SoundManager.BOMB);
                 }
                 else
                     t.setAnimation(new JiggleFadeAnimation(t));
             }
 
             // If other bombs were hit, they will be dealt with in another
             // bomb removal cycle.
             bombRemovalSet = newBombRemovalSet;
             
             // Set the flag.
             tileRemovalInProgress = true;
         }
         
         // If a line removal is in progress.
         if (tileRemovalInProgress == true)
         {
             // Animation completed flag.
             boolean animationInProgress = false;
             
             // Check to see if they're all done.
             for (Iterator it = tileRemovalSet.iterator(); it.hasNext(); )
             {
                 if (boardMan.getTile((Integer) it.next()).getAnimation()
                         .isDone() == false)
                 {
                     animationInProgress = true;
                 }
             }
             
             if (animationInProgress == false)
             {
                 // Remove the tiles from the board.
                 boardMan.removeTiles(tileRemovalSet);
                 
                 // Bomb removal is completed.
                 tileRemovalInProgress = false;
                 
                 // See if there are any bombs in the bomb set.
                 // If there are, activate the bomb removal.
                 if (bombRemovalSet.size() > 0)
                     activateBombRemoval = true;
                 // Otherwise, start a new refactor.
                 else                
                     runRefactor(200);
             }  
         }
         
         // Animate all the pieces.
         boardMan.animateAll(delta);
         		
         // Update piece manager logic and then draw it.
         pieceMan.updateLogic(this);
 		
 		// Draw the timer text.
 		timerText.setText(String.valueOf(timeMan.getTime()));		
                 
         // Draw the score text.
         scoreText.setText(String.valueOf(scoreMan.getTotalScore()));
         
         // Draw the high score text.
         highScoreText.setText(String.valueOf(scoreMan.getHighScore()));
         
         // Set the level text.
         levelText.setText("N/A");
         //levelText.setText(String.valueOf(worldMan.getCurrentLevel()));
         
         // Draw the move count text.
         moveCountText.setText(String.valueOf(moveMan.getCurrentMoveCount()));
         
         // Draw the layer manager.
         layerMan.draw();
 		
 		// Handle the timer.
 		timeMan.incrementInternalTime(delta, this);
 		
 		// if escape has been pressed, stop the game
 		if (window.isKeyPressed(KeyEvent.VK_ESCAPE))
 		{
 			System.exit(0);
 		}
 	}
 
 	/**
 	 * Notification that the game window has been closed
 	 */
 	public void windowClosed()
 	{
         if(this.propertyMan != null)
         {
             try
             {
                 propertyMan.saveProperties();
             }
             catch(Exception e)
             {
                 Util.handleException(e);
             }
         }
         else
         {
             // Do nothin. The property man is not yet initialized.
         }
 		System.exit(0);
 	}
     
 	/**
 	 * The entry point into the game. We'll simply create an instance of class
 	 * which will start the display and game loop.
 	 * 
 	 * @param argv
 	 *            The arguments that are passed into our game
 	 */
 	public static void main(String argv[])
 	{		
 		Game g = new Game(ResourceFactory.JAVA2D);
 		g.startRendering();		
 	}
 }
