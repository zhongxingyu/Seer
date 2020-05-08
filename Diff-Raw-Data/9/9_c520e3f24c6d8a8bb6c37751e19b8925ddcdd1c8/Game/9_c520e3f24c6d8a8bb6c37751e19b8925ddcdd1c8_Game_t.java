 package ca.couchware.wezzle2d;
 
 import ca.couchware.wezzle2d.music.MusicManager;
 import ca.couchware.wezzle2d.sound.SoundManager;
 import ca.couchware.wezzle2d.button.*;
 import ca.couchware.wezzle2d.util.*;
 import ca.couchware.wezzle2d.tile.*;
 import ca.couchware.wezzle2d.animation.*;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.event.KeyEvent;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URL;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Properties;
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
     //--------------------------------------------------------------------------
     // Static Attributes
     //--------------------------------------------------------------------------
     
     /**
      * The platform specific newline character.
      */
     public static String NL = System.getProperty("line.separator");
                
     /**
      * The width of the screen.
      */
     final public static int SCREEN_WIDTH = 800;
     
     /**
      * The height of the screen .
      */
     final public static int SCREEN_HEIGHT = 600;
     
     /**
      * The path to the resources.
      */
     final public static String RESOURCES_PATH = "resources";        
 
     /**
      * The path to the fonts.
      */
     final public static String FONTS_PATH = RESOURCES_PATH + "/fonts";
     
     /**
      * The path to the sprites.
      */
     final public static String SPRITES_PATH = RESOURCES_PATH + "/sprites";
     
     /**
      * The path to the sounds.
      */
     final public static String SOUNDS_PATH = RESOURCES_PATH + "/sounds";
     
     /**
      * The path to the music.
      */
     final public static String MUSIC_PATH = RESOURCES_PATH + "/music";
     
     /**
      * The level header path.
      */
     final private static String LEVEL_HEADER_PATH = Game.SPRITES_PATH 
             + "/Header_Level.png";
     
     /**
      * The score header path.
      */
     final private static String SCORE_HEADER_PATH = Game.SPRITES_PATH 
             + "/Header_Score.png";
     
     /**
      * The high score header path.
      */
     final private static String HIGH_SCORE_HEADER_PATH = Game.SPRITES_PATH 
             + "/Header_HighScore.png";       
     
     /**
      * The text color.
      */
     final public static Color TEXT_COLOR = new Color(252, 233, 45);
         
     /**
      * The line score color.
      */
     final public static Color SCORE_LINE_COLOR = new Color(252, 233, 45);
     
     /**
      * The piece score color.
      */
     final public static Color SCORE_PIECE_COLOR = new Color(240, 240, 240);
     
     /**
      * The bomb score color.
      */ 
     final public static Color SCORE_BOMB_COLOR = new Color(255, 127, 0);
   
     /**
      * The background layer.
      */
     final public static int LAYER_BACKGROUND = 0;
     
     /**
      * The board layer.
      */
     final public static int LAYER_TILE = 1;
     
     /**
      * The effects layer.
      */
     final public static int LAYER_EFFECT = 2;
     
     /**
      * The UI layer.
      */
     final public static int LAYER_UI = 3;
     
     /**
      * The build nunber path.
      */
     final public static String BUILD_NUMBER_PATH = 
             RESOURCES_PATH + "/build.number";
     
     //--------------------------------------------------------------------------
     // Public Attributes
     //--------------------------------------------------------------------------
     
     /**
      * The current build number.
      */
     public String buildNumber;
     
     /**
      * The animation manager in charge of animations.
      */
     public AnimationManager animationMan;
     
     /**
 	 * The manager in charge of maintaining the board.
 	 */
 	public BoardManager boardMan;
 	
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
      * The manager in charge of music.
      */
     public MusicManager musicMan;       
     
     /**
      * The Manager in charge of the world.
      */
     public WorldManager worldMan;
     
 	/** 
 	 * The manager in charge of keeping track of the time. 
 	 */
 	public TimerManager timerMan;
 	
     /**
      * The pause button.
      */
     public RectangularBooleanButton pauseButton;
     
     /**
      * The menu button.
      */
     public RectangularBooleanButton soundButton;
     
     /**
      * The exit button.
      */
     public RectangularBooleanButton musicButton;
     
     /**
      * The progress bar.
      */
     public ProgressBar progressBar;
     
     //--------------------------------------------------------------------------
     // Private Attributes
     //--------------------------------------------------------------------------
     
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
     private Set<Integer> tileRemovalSet;
     
     /**
      * If true, uses jump animation instead of zoom.
      */
     private boolean tileRemovalUseJumpAnimation = false;
     
     /**
      * If true, a bomb removal will be activated next loop.
      */
     private boolean activateBombRemoval = false;
     
     /**
      * The set of bomb tile indices that will be removed.
      */
     private Set<Integer> bombRemovalSet;       
     
     /**
      * The number of lines cleared this cycle, where a cycle is defined as the
      * period from the moment the piece is clicked, to when the board finally
      * stops finding lines.
      */
     private int cycleLineCount;
     
     /**
      * The total number of lines cleared.
      */
     private int totalLineCount;
     
     /**
      * If true, the board show animation will be activated next loop.
      */
     private boolean activateBoardShowAnimation = false;
     
     /**
      * If true, the board hide animation will be activated next loop.
      */
     private boolean activateBoardHideAnimation = false;
     
     /**
      * The animation that will indicate whether the board animation is 
      * complete.
      */
     private Animation boardAnimation = null;
     
     /**
      * If true, the game will end next loop.
      */
     private boolean activateGameOver = false;
             
     /**
      * Is a game over routine in progress?
      */
     private boolean gameOverInProgress = false;
     
     /**
      * The number of cascades thus far.
      */
     private int cascadeCount;         
         
 	/**
 	 * The time at which the last rendering looped started from the point of
 	 * view of the game logic.
 	 */
 	private long lastLoopTime;
 	
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
      * The name of the application.
      */
     private String applicationName = "Wezzle";
     
 	/** 
      * The normal title of the window. 
      */
 	private String windowTitle = applicationName;	    
     
     /**
      * The background sprite.
      */
     private Entity background;
     
 	/** 
      * The timer text. 
      */
 	private Label timerLabel;
       
     /**
      * The score header graphic.
      */
     private GraphicEntity scoreHeaderLabel;
     
     /** 
      * The score text.
      */
     private Label scoreLabel;
     
     /**
      * The high score header graphic.
      */
     private GraphicEntity highScoreHeaderLabel;
     
     /** 
      * The high score text. 
      */
     private Label highScoreLabel;
     
     /**
      * The level header graphic.
      */
     private GraphicEntity levelHeader;
     
     /**
      * The level text.
      */
     private Label levelLabel;        
     
     /**
      * The "Paused" text.
      */
     private Label pausedHeaderLabel;
     
     /**
      * The amount of moves, shown on the paused screen.
      */
     private Label pausedMovesLabel;
     
     /**
      * The number of lines, shown on the paused screen.
      */
     private Label pausedLinesLabel;
     
     /**
      * The number of lines per move, shown on the paused screen.
      */
     private Label pausedLinesPerMoveLabel;
     
     /**
      * The version text.
      */
     private Label versionLabel;     
         
     /**
      * The game over header.
      */
     private Label gameOverHeaderLabel;
     
     /**
      * The game over final score header.
      */
     private Label gameOverFinalScoreHeaderLabel;
     
     /**
      * The game over final score.
      */
     private Label gameOverFinalScoreLabel;
     
     /**
      * The game over restart button.
      */
     private RectangularBooleanButton gameOverRestartButton;
     
     /**
      * The game over continue button.
      */
     private RectangularBooleanButton gameOverContinueButton;
     
     //--------------------------------------------------------------------------
     // Constructor
     //--------------------------------------------------------------------------
     
 	/**
 	 * Construct our game and set it running.
 	 * 
 	 * @param renderingType
 	 *            The type of rendering to use (should be one of the contansts
 	 *            from ResourceFactory)
 	 */
 	public Game(int renderingType) 
 	{
         // Get the build number.
         Properties buildProperties = new Properties();            
                
         try
         {
             URL url = this.getClass().getClassLoader()
                 .getResource(BUILD_NUMBER_PATH);  
             
             InputStream in = url.openStream();
             buildProperties.load(in);
             in.close();
             
             buildNumber = 
                     (String) buildProperties.getProperty("build.number");
         }
         catch (Exception e)
         {
             Util.handleException(e);
             Util.handleWarning("Could not find build number at: "
                     + BUILD_NUMBER_PATH + "!",
                     Thread.currentThread());
             buildNumber = "???";
         }
         finally
         {
             if (buildNumber == null)
                 buildNumber = "???";
         }
         
         // Print the build number.
         Util.handleMessage("Wezzle Build " + buildNumber + " @ " + (new Date()),
                 Thread.currentThread());
         
 		// Create a window based on a chosen rendering method.
 		ResourceFactory.get().setRenderingType(renderingType);		        
         
 		final Runnable r = new Runnable()
 		{
 			public void run()
 			{                             
 				window = ResourceFactory.get().getGameWindow();
 				window.setResolution(SCREEN_WIDTH, SCREEN_HEIGHT);
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
         //----------------------------------------------------------------------
         // Initialize attributes.
         //----------------------------------------------------------------------
         
         // Set the cycle line count to 0.
         cycleLineCount = 0;
         
         // Set the total line count to 0.       
         totalLineCount = 0;
         
         // Set cascade count to 0.
         cascadeCount = 0;                
         
         // Initialize line index set.
         tileRemovalSet = new HashSet<Integer>();
         
         // Initialize bomb index set.
         bombRemovalSet = new HashSet<Integer>();
         
         //----------------------------------------------------------------------
         // Initialize managers.
         //----------------------------------------------------------------------
         
         // Create the layer manager.   
         layerMan = new LayerManager(window, 4);        
         
         // Draw the current background.
 		background = new GraphicEntity(SPRITES_PATH + "/Background2.png", 0, 0);
         layerMan.add(background, LAYER_BACKGROUND);
         
         // Create the animation manager.
         animationMan = new AnimationManager();
         
 		// Create the board manager.
 		boardMan = new BoardManager(layerMan, 272, 139, 8, 10);        
         
 		// Create the piece manager.
 		pieceMan = new PieceManager(boardMan);
         layerMan.add(pieceMan.getPieceGrid(), LAYER_EFFECT);
 		window.addMouseListener(pieceMan);
 		window.addMouseMotionListener(pieceMan);	
 	
         // Create the property manager. Must be done before Score manager.
         propertyMan = new PropertyManager();
         
         // Create the score manager.
         scoreMan = new ScoreManager(boardMan, propertyMan);
 
         // Create the world manager.
         worldMan = new WorldManager(propertyMan);
         
         // Generate the game board.
         boardMan.generateBoard(worldMan.getItemList());
         boardMan.setVisible(false);
         startBoardShowAnimation();
         
         // Create the sound manager.
         soundMan = new SoundManager();
         
         // Create the music manager.
         musicMan = new MusicManager(propertyMan);
         
         // Create the move manager.
         moveMan = new MoveManager();
         
         // Create the time manager.
 		timerMan = new TimerManager(worldMan.getInitialTimer());                            
         
         //----------------------------------------------------------------------
         // Initialize buttons.
         //----------------------------------------------------------------------
         
         // Create a new pause button.
         pauseButton = new RectangularBooleanButton(668, 211);
         pauseButton.setNormalOpacity(70);
         pauseButton.setText("Pause");
         pauseButton.getLabel().setSize(18);
         pauseButton.setAlignment(Button.VCENTER | Button.HCENTER);        
         layerMan.add(pauseButton, LAYER_UI);
         window.addMouseListener(pauseButton);
         window.addMouseMotionListener(pauseButton);
         
         // Create the sound on/off button.
         soundButton = new RectangularBooleanButton(668, 299);
         soundButton.setNormalOpacity(70);
         soundButton.setText("Sound ON");
         soundButton.getLabel().setSize(18);
         soundButton.setAlignment(Button.VCENTER | Button.HCENTER);
         layerMan.add(soundButton, LAYER_UI);
         window.addMouseListener(soundButton);
         window.addMouseMotionListener(soundButton);
         
         // Check the properties.
         if (propertyMan.getStringProperty(PropertyManager.KEY_SOUND)
                 .equals(PropertyManager.VALUE_OFF))
         {
             this.pauseSound();
         }
         
         // Create music on/off button.
         musicButton = new RectangularBooleanButton(668, 387);
         musicButton.setNormalOpacity(70);
         musicButton.setText("Music ON");
         musicButton.getLabel().setSize(18);
         musicButton.setAlignment(Button.VCENTER | Button.HCENTER);
         layerMan.add(musicButton, LAYER_UI);
         window.addMouseListener(musicButton);
         window.addMouseMotionListener(musicButton);
         
         // Check the properties.
         if (propertyMan.getStringProperty(PropertyManager.KEY_MUSIC)
                 .equals(PropertyManager.VALUE_OFF))
         {
            this.pauseMusic();
         }
         
         //----------------------------------------------------------------------
         // Initialize labels.
         //----------------------------------------------------------------------
         
         // Create the "Paused" text.
         pausedHeaderLabel = ResourceFactory.get().getLabel(400, 245);        
 		pausedHeaderLabel.setSize(30);
 		pausedHeaderLabel.setAlignment(Label.HCENTER | Label.VCENTER);
 		pausedHeaderLabel.setColor(TEXT_COLOR);
         pausedHeaderLabel.setText("Paused");
         pausedHeaderLabel.setVisible(false);
         layerMan.add(pausedHeaderLabel, LAYER_UI);
         
         pausedMovesLabel = ResourceFactory.get().getLabel(400, 310);        
 		pausedMovesLabel.setSize(18);
 		pausedMovesLabel.setAlignment(Label.HCENTER | Label.VCENTER);
 		pausedMovesLabel.setColor(TEXT_COLOR);
         pausedMovesLabel.setText(moveMan.getMoveCount() + " moves taken");
         pausedMovesLabel.setVisible(false);
         layerMan.add(pausedMovesLabel, LAYER_UI);
         
         pausedLinesLabel = ResourceFactory.get().getLabel(400, 340);        
 		pausedLinesLabel.setSize(18);
 		pausedLinesLabel.setAlignment(Label.HCENTER | Label.VCENTER);
 		pausedLinesLabel.setColor(TEXT_COLOR);
         pausedLinesLabel.setText(totalLineCount + " lines cleared");
         pausedLinesLabel.setVisible(false);
         layerMan.add(pausedLinesLabel, LAYER_UI);
                         
         pausedLinesPerMoveLabel = ResourceFactory.get().getLabel(400, 370);        
 		pausedLinesPerMoveLabel.setSize(18);
 		pausedLinesPerMoveLabel.setAlignment(Label.HCENTER | Label.VCENTER);
 		pausedLinesPerMoveLabel.setColor(TEXT_COLOR);
         pausedLinesPerMoveLabel.setText("0.0 lines per move");
         pausedLinesPerMoveLabel.setVisible(false);
         layerMan.add(pausedLinesPerMoveLabel, LAYER_UI);
               
         // Set up the version text.
 		versionLabel = ResourceFactory.get().getLabel(800 - 10, 600 - 10);        
 		versionLabel.setSize(12);
 		versionLabel.setAlignment(Label.BOTTOM | Label.RIGHT);
 		versionLabel.setColor(TEXT_COLOR);
         versionLabel.setText(applicationName + " Build " + buildNumber);
         layerMan.add(versionLabel, LAYER_UI);
         
 		// Set up the timer text.
 		timerLabel = ResourceFactory.get().getLabel(404, 100);        
 		timerLabel.setSize(50);
 		timerLabel.setAlignment(Label.BOTTOM | Label.HCENTER);
 		timerLabel.setColor(TEXT_COLOR);
         layerMan.add(timerLabel, LAYER_UI);
              
         // Set up the level header.
         levelHeader = new GraphicEntity(LEVEL_HEADER_PATH, 126, 153);
         levelHeader.setAlignment(GraphicEntity.VCENTER | GraphicEntity.HCENTER);
         layerMan.add(levelHeader, LAYER_UI);
         
         // Set up the level text.
         levelLabel = ResourceFactory.get().getLabel(126, 210);        
         levelLabel.setSize(20);
         levelLabel.setAlignment(Label.BOTTOM | Label.HCENTER);
         levelLabel.setColor(TEXT_COLOR);
         layerMan.add(levelLabel, LAYER_UI);        
         
         // Set up the score header.
         highScoreHeaderLabel = 
                 new GraphicEntity(HIGH_SCORE_HEADER_PATH, 127, 278);        
         highScoreHeaderLabel
                 .setAlignment(GraphicEntity.VCENTER | GraphicEntity.HCENTER);        
         layerMan.add(highScoreHeaderLabel, LAYER_UI);
                         
         // Set up the high score text.
         highScoreLabel = ResourceFactory.get().getLabel(126, 337);        
         highScoreLabel.setSize(20);
         highScoreLabel.setAlignment(Label.BOTTOM | Label.HCENTER);
         highScoreLabel.setColor(TEXT_COLOR);
         layerMan.add(highScoreLabel, LAYER_UI);
         
         // Set up the score header.
         scoreHeaderLabel = new GraphicEntity(SCORE_HEADER_PATH, 128, 403);
         scoreHeaderLabel
                 .setAlignment(GraphicEntity.VCENTER | GraphicEntity.HCENTER);
         layerMan.add(scoreHeaderLabel, LAYER_UI);
         
         // Set up the score text.
         scoreLabel = ResourceFactory.get().getLabel(126, 460);        
         scoreLabel.setSize(20);
         scoreLabel.setAlignment(Label.BOTTOM | Label.HCENTER);
         scoreLabel.setColor(TEXT_COLOR);     
         scoreMan.setTargetLevelScore(
                 worldMan.generateTargetLevelScore());
         layerMan.add(scoreLabel, LAYER_UI);
              
         //----------------------------------------------------------------------
         // Initialize progress bar.
         //----------------------------------------------------------------------
         
         // Create the progress bar.
         progressBar = new ProgressBar(393, 501, ProgressBar.WIDTH_200, true);
         progressBar.setAlignment(ProgressBar.VCENTER | ProgressBar.HCENTER);
         progressBar.setProgressMax(scoreMan.getTargetLevelScore());
         layerMan.add(progressBar, LAYER_UI);
         
         //----------------------------------------------------------------------
         // Initialize game over objects.
         //----------------------------------------------------------------------
         
         // Create the game over header.
         gameOverHeaderLabel = ResourceFactory.get().getLabel(400, 181);        
         gameOverHeaderLabel.setSize(26);
         gameOverHeaderLabel.setAlignment(Label.VCENTER | Label.HCENTER);
         gameOverHeaderLabel.setColor(TEXT_COLOR);
         gameOverHeaderLabel.setText("Game over :(");
         gameOverHeaderLabel.setVisible(false);
         layerMan.add(gameOverHeaderLabel, LAYER_UI);
         
         // Create the final score header.
         gameOverFinalScoreHeaderLabel = ResourceFactory.get().getLabel(400, 234);        
         gameOverFinalScoreHeaderLabel.setSize(14);
         gameOverFinalScoreHeaderLabel.setAlignment(Label.VCENTER | Label.HCENTER);
         gameOverFinalScoreHeaderLabel.setColor(TEXT_COLOR);
         gameOverFinalScoreHeaderLabel.setText("Your final score was");
         gameOverFinalScoreHeaderLabel.setVisible(false);
         layerMan.add(gameOverFinalScoreHeaderLabel, LAYER_UI); 
         
         // Create the final score label.
         gameOverFinalScoreLabel = ResourceFactory.get().getLabel(400, 270);        
         gameOverFinalScoreLabel.setSize(30);
         gameOverFinalScoreLabel.setAlignment(Label.VCENTER | Label.HCENTER);
         gameOverFinalScoreLabel.setColor(TEXT_COLOR);
         gameOverFinalScoreLabel.setText("541231");
         gameOverFinalScoreLabel.setVisible(false);
         layerMan.add(gameOverFinalScoreLabel, LAYER_UI); 
         
         // Create restart button.
         gameOverRestartButton = new RectangularBooleanButton(400, 345);        
         gameOverRestartButton.setActiveOpacity(70);
         gameOverRestartButton.setText("Restart");
         gameOverRestartButton.getLabel().setSize(18);
         gameOverRestartButton.setAlignment(Button.VCENTER | Button.HCENTER);
         gameOverRestartButton.setVisible(false);
         layerMan.add(gameOverRestartButton, LAYER_UI);        ;
         
         // Create continue button.
         gameOverContinueButton = new RectangularBooleanButton(400, 406);
         gameOverContinueButton.setActiveOpacity(70);
         gameOverContinueButton.setText("Continue");
         gameOverContinueButton.getLabel().setSize(18);
         gameOverContinueButton.setAlignment(Button.VCENTER | Button.HCENTER);
         gameOverContinueButton.setVisible(false);
         layerMan.add(gameOverContinueButton, LAYER_UI);
         
         // Start the game.
 		startGame();
 	}
 
 	/**
 	 * Start a fresh game, this should clear out any old data and create a new
 	 * set.
 	 */
 	private void startGame()
 	{		
 		lastLoopTime = SystemTimer.getTime();
 	}
     
     private void showGameOverScreen()
     {
         gameOverHeaderLabel.setVisible(true);
         gameOverFinalScoreHeaderLabel.setVisible(true);
         gameOverFinalScoreLabel.setVisible(true);
         gameOverFinalScoreLabel.setText(
                 String.valueOf(scoreMan.getTotalScore()));
         gameOverRestartButton.setVisible(true);
         window.addMouseListener(gameOverRestartButton);
         window.addMouseMotionListener(gameOverRestartButton);
         gameOverContinueButton.setVisible(true);
         window.addMouseListener(gameOverContinueButton);
         window.addMouseMotionListener(gameOverContinueButton);
         
         // Hide the pause and clear any calls to it.
         pauseButton.clicked();
         pauseButton.setActivated(false);
         pauseButton.setVisible(false);
         window.removeMouseListener(pauseButton);
         window.removeMouseMotionListener(pauseButton);
         
     }
     
     private void hideGameOverScreen()
     {
         gameOverHeaderLabel.setVisible(false);
         gameOverFinalScoreHeaderLabel.setVisible(false);
         gameOverFinalScoreLabel.setVisible(false);
         gameOverRestartButton.setVisible(false);
         window.removeMouseListener(gameOverRestartButton);
         window.removeMouseMotionListener(gameOverRestartButton);
         gameOverContinueButton.setVisible(false);
         window.removeMouseListener(gameOverContinueButton);
         window.removeMouseMotionListener(gameOverContinueButton);
         
         // Show the pause button.
         pauseButton.setVisible(true);
         window.addMouseListener(pauseButton);
         window.addMouseMotionListener(pauseButton);
     }
     
     /**
      * A private helper method to handle music pausing.
      */
     private void pauseMusic()
     {
          // Set the button.
         musicButton.setText("Music OFF");                
         musicMan.setPaused(true);
 
         // Set the property.
         propertyMan.setProperty(PropertyManager.KEY_MUSIC, PropertyManager.VALUE_OFF);
         
         // Activate the button.
         musicButton.setActivated(true);
     }
     
     /**
      * A private helper method to handle music resuming.
      */
     private void resumeMusic()
     {
         // Set the button.
         musicButton.setText("Music ON");                
         musicMan.setPaused(false);
 
         // Set the property.
         propertyMan.setProperty(PropertyManager.KEY_MUSIC, PropertyManager.VALUE_ON);
     }
     
      /**
      * A private helper method to handle music pausing.
      */
     private void pauseSound()
     {
          // Set the button.
         soundButton.setText("Sound OFF");                
         soundMan.setPaused(true);
 
         // Set the property.
         propertyMan.setProperty(PropertyManager.KEY_SOUND, 
                 PropertyManager.VALUE_OFF);
         
          // Activate the button.
         soundButton.setActivated(true);
      
     }
     
     /**
      * A private helper method to handle sound resuming.
      */
     private void resumeSound()
     {
         // Set the button.
         soundButton.setText("Sound ON");                
         soundMan.setPaused(false);
 
         // Set the property.
         propertyMan.setProperty(PropertyManager.KEY_SOUND, 
                 PropertyManager.VALUE_ON);
     }
     
      /**
      * A private helper method to handle game pausing.
      */
     private void pauseGame()
     {
         pauseButton.setText("Resume");
         pauseButton.getLabel().setSize(18);
         layerMan.hide(LAYER_TILE);
         layerMan.hide(LAYER_EFFECT);                
         pausedHeaderLabel.setVisible(true);
         pausedMovesLabel.setVisible(true);
         pausedLinesLabel.setVisible(true);
         pausedLinesPerMoveLabel.setVisible(true);
         
         // Calculate lines per move.
         double lpm;
         if (moveMan.getMoveCount() == 0)
             lpm = 0.0;
         else
         {
             lpm = (double) totalLineCount 
                 / (double) moveMan.getMoveCount();
             lpm = lpm * 100;
             lpm = ((double) (int) lpm) / 100.0;
         }
         
         // Set the moves label.
         if (moveMan.getMoveCount() == 1)
             pausedMovesLabel.setText("1 move taken");
         else
             pausedMovesLabel.setText(moveMan.getMoveCount() + " moves taken");
         
         // Set the lines label.
         if (totalLineCount == 1)                    
             pausedLinesLabel.setText("1 line cleared");
         else
             pausedLinesLabel.setText(totalLineCount + " lines cleared");
         
         // set the lines per move label.
         pausedLinesPerMoveLabel.setText(lpm + " lines per move");
             
         // Set button as activated.
         pauseButton.setActivated(true);
     }
     
     /**
      * A private helper method to handle game resuming.
      */
     private void resumeGame()
     {
         pauseButton.setText("Pause");
         pauseButton.getLabel().setSize(18);
         layerMan.show(LAYER_TILE);
         layerMan.show(LAYER_EFFECT);
         pausedHeaderLabel.setVisible(false);
         pausedMovesLabel.setVisible(false);
         pausedLinesLabel.setVisible(false);
         pausedLinesPerMoveLabel.setVisible(false);
 
         // Clear clicks.
         pieceMan.clearMouseButtons();
     }
     
     /**
      * Start a refactor with the given speed.
      * 
      * @param speed
      */
     public void startRefactor(int speed)
     {
         // Set the refactor flag.
         this.activateRefactor = true;
         this.refactorSpeed = speed;
     }
     
     /**
      * Clear the refactor flag.
      */
     public void clearRefactor()
     {
        // Set the refactor flag.
        this.activateRefactor = false;
     }      
    
     /**
      * A method to check whether the board is busy.
      * 
      * @return True if it is, false otherwise.
      */
     public boolean isBusy()
     {
        return (isRefactoring()               
                || isTileRemoving()
                || this.gameOverInProgress == true
                || this.boardAnimation != null);
     }
     
     /**
      * Checks whether a refactor is, or is about to be, in progress.
      */
     public boolean isRefactoring()
     {
         return this.activateRefactor 
                 || this.refactorDownInProgress 
                 || this.refactorLeftInProgress;
     }
     
     /**
      * Checks whether tiles are, or are about to be, removed.
      */
     public boolean isTileRemoving()
     {
         return this.activateLineRemoval               
                || this.activateBombRemoval 
                || this.tileRemovalInProgress;
     }
     
     public void startBoardShowAnimation()
     {
         // Set the flag.
         activateBoardShowAnimation = true;
     }
     
     public void clearBoardShowAnimation()
     {
         // Clear the flag.
         activateBoardShowAnimation = false;
     }
     
     public void startBoardHideAnimation()
     {
         // Set the flag.
         activateBoardHideAnimation = true;
     }
     
     public void clearBoardHideAnimation()
     {
         // Clear the flag.
         activateBoardHideAnimation = false;
     }
     
     public void startGameOver()
     {
         Util.handleMessage("Game over!", Thread.currentThread());
         activateGameOver = true;
     }
     
     public void clearGameOver()
     {
         activateGameOver = false;
     }
 
     public int getCascadeCount()
     {
         return cascadeCount;
     }
 
     public int getCycleLineCount()
     {
         return cycleLineCount;
     }
 
     public int getTotalLineCount()
     {
         return totalLineCount;
     }        
 
 	/**
 	 * Notification that a frame is being rendered. Responsible for running game
 	 * logic and rendering the scene.
 	 */
 	public boolean frameRendering()
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
         
         // If the pause button was just clicked.
         if (pauseButton.clicked() == true)
         {
             // If it was clicked on, then hide the board and
             // show the paused text.
             if (pauseButton.isActivated() == true)
             {
                 this.pauseGame();
             }
             else
             {
                 this.resumeGame();
             }
         }
         // Music button.
         if (musicButton.clicked() == true)
         {
             if (musicButton.isActivated() == true)
             {
                 this.pauseMusic();
             }
             else
             {
                 this.resumeMusic();
             }
         }
         
         // Sound button.
         if (soundButton.clicked() == true)
         {
             if (soundButton.isActivated() == true)
             {
                 this.pauseSound();
             }
             else
             {
                 this.resumeSound();
             }
         }
 		
         // If the music stopped playing, play the next song.
         if (musicMan.isMusicPlaying() == false)
         {
             musicMan.playNext();
         }        
         
         // If the pause button is not on, then we proceed with the
         // normal game loop.
         if (pauseButton.isActivated() == false)
         {   
             // See if it's time to level-up.
             if (pieceMan.isTileDropInProgress() == false
                     && isBusy() == false)
             {
                 // Handle Level up.
                 if (scoreMan.getLevelScore() 
                         >= scoreMan.getTargetLevelScore())
                 {    
                     // Hide piece.                    
                     pieceMan.getPieceGrid().setVisible(false);
                     pieceMan.stopAnimation();
                     
                     Util.handleMessage("Level up!!!", Thread.currentThread());
                     worldMan.levelUp(this);
                     
                     this.activateLineRemoval = true;
                     this.tileRemovalUseJumpAnimation = true;
                     tileRemovalSet.clear();
 
                     int j = boardMan.getRows() - 1;
                     for (int i = 0; i < boardMan.getColumns(); i++)
                     {                         
                         int index = i + (j * boardMan.getColumns());
                         if (boardMan.getTile(index) != null)
                             tileRemovalSet.add(
                                     new Integer(index));
                     }                                        
                     
                     soundMan.play(SoundManager.LEVEL_UP);
                     
                     int x = pieceMan.getPieceGrid().getX() 
                             + boardMan.getCellWidth() / 2;
                     
                     int y = pieceMan.getPieceGrid().getY() 
                             + boardMan.getCellHeight() / 2;
                     
                     Label label = ResourceFactory.get().getLabel(x, y);                                                
                     label.setText("Level Up!");
                     label.setAlignment(Label.LEFT | Label.VCENTER);
                     label.setColor(Game.TEXT_COLOR);
                     label.setSize(26);
                     
                     animationMan.add(new FloatFadeOutAnimation(                            
                             1, 0, layerMan, label));      
                     
                     label = null;
                 }
             } // end if
             
             // See if it's game ovaries.
             if (activateGameOver == true)
             {
                 // Clear flag.
                 clearGameOver();                                
                 
                 // Set in progress flag.
                 gameOverInProgress = true;
                 
                 // Hide the board.
                 startBoardHideAnimation();                
             }
             
             // Check on board animation.
             if (boardAnimation != null && boardAnimation.isDone() == true)
             {
                 // Set animation visible depending on what animation
                 // was just performed.
                 if (boardAnimation instanceof FadeInAnimation)   
                 {
                     boardMan.setVisible(true);
                     pieceMan.getPieceGrid().setVisible(true);
                 }
                 else if (boardAnimation instanceof FadeOutAnimation)
                 {
                     boardMan.setVisible(false);
                     pieceMan.getPieceGrid().setVisible(false);
                 }
                 else
                     throw new RuntimeException(
                             "Unrecognized board animation class.");
                 
                 // Clear the board animation.
                 boardAnimation = null;
                 
                 // Claer mouse button presses.
                 pieceMan.clearMouseButtons();
                 
                 // If game over is in progress, make a new board and start.
                 if (gameOverInProgress == true)
                 {
                     // Draw game over screen.
                     showGameOverScreen();                    
                 }
             }
             else if (boardAnimation != null && boardAnimation.isDone() == false)
             {
                 // Board is still dirty due to animation.
                 boardMan.setDirty(true);
             }
             
             // If the game over is in progress, check to see if a button was 
             // pressed.
             if (gameOverInProgress == true)
             {
                 if (gameOverRestartButton.isActivated() == true
                         || gameOverContinueButton.clicked() == true)
                 {
                     // Hide the screen.
                     hideGameOverScreen();
                     
                     // Clear the flag.
                     gameOverInProgress = false;
                     
                     // Reset a bunch of stuff.
                     if (gameOverRestartButton.isActivated() == true)
                     {
                         // Set the level to 1.
                         worldMan.setLevel(1);
                         
                         // Reset the timer to the initial.
                         timerMan.setInitialTime(worldMan.getInitialTimer());
                     }
 
                     scoreMan.setLevelScore(0);
                     scoreMan.setTargetLevelScore(
                             worldMan.generateTargetLevelScore(
                             worldMan.getLevel()));                
                     progressBar.setProgressMax(
                             scoreMan.getTargetLevelScore());
                     scoreMan.setTotalScore(0);                
                     moveMan.setMoveCount(0);
                     totalLineCount = 0;
                     
                     // Create board and make it invisible.
                     boardMan.setVisible(false);
                     boardMan.generateBoard(worldMan.getItemList());                    
 //                    
                     // Unpause the game.
                     // Don't worry! The game won't pass updates to the 
                     // timer unless the board is shown.  In hindsight, this
                     // is kind of crappy, but whatever, we'll make it prettier
                     // one day.
                     timerMan.setPaused(false);
                     
                     // Reset the timer.
                     timerMan.resetTimer();
                   
                     // Start the board show animation.  This will
                     // make the board visible when it's done.
                     startBoardShowAnimation();
                 
                     // Reset the buttons.
                     gameOverRestartButton.setActivated(false);
                     gameOverContinueButton.setActivated(false);
                     
                 }              
             }
             
             // Check to see if we should be showing the board.
             if (activateBoardShowAnimation == true)
             {
                 // Start board show animation.
                 boardAnimation = boardMan.animateShow(animationMan);     
                 boardMan.setDirty(true);
                 
                 // Hide the piece.
                 pieceMan.getPieceGrid().setVisible(false);                               
                 
                 // Clear flag.
                 clearBoardShowAnimation();                                
             }
             
             // Check to see if we should be hiding the board.
             if (activateBoardHideAnimation == true)
             {
                 // Start board hide animation.
                 boardAnimation = boardMan.animateHide(animationMan); 
                 boardMan.setDirty(true);
                 
                 // Hide the piece.
                 pieceMan.getPieceGrid().setVisible(false);                               
                 
                 // Clear flag.
                 clearBoardHideAnimation();                                
             }                        
             
             
             
             // See if we need to activate the refactor.
             if (activateRefactor == true)
             {            
                 // Hide piece.
                 pieceMan.getPieceGrid().setVisible(false);
 
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
                     
                     cycleLineCount += boardMan.findXMatch(tileRemovalSet);
                     cycleLineCount += boardMan.findYMatch(tileRemovalSet);
 
                     // If there are matches, score them, remove 
                     // them and then refactor again.
                     if (tileRemovalSet.size() > 0)
                     {                                       
                         // Activate the line removal.
                         activateLineRemoval = true;                 
                     }
                     else
                     {
                        // Make sure the tiles are not still dropping.
                         if (pieceMan.isTileDropInProgress() == false)
                         {                            
                             pieceMan.loadRandomPiece();   
                             pieceMan.getPieceGrid().setVisible(true);
 
                             // Unpause the timer.
                             timerMan.resetTimer();
                             timerMan.setPaused(false);
 
                             // Reset the mouse.
                             pieceMan.clearMouseButtons();
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
 
                 // Increment cascade.
                 cascadeCount++;
                 
                 // Calculate score.
                 final int deltaScore = scoreMan.calculateLineScore(
                         tileRemovalSet, 
                         ScoreManager.TYPE_LINE,
                         cascadeCount);                               
                 
                 // Show the SCT.
                 XYPosition p = boardMan.determineCenterPoint(tileRemovalSet);
                 Label label = ResourceFactory.get().getLabel(p.x, p.y);                
                 label.setText(String.valueOf(deltaScore));
                 label.setAlignment( Label.HCENTER | Label.VCENTER);
                 label.setColor(SCORE_LINE_COLOR);
                 label.setSize(scoreMan.determineFontSize(deltaScore));
                 
                 animationMan.add(new FloatFadeOutAnimation(                                             
                         0, -1, layerMan, label));
                 
                 // Release references.
                 p = null;
                 label = null;                
                 
                 // Play the sound.
                 soundMan.play(SoundManager.LINE);
 
                 // Make sure bombs aren't removed (they get removed
                 // in a different step).
                 boardMan.scanBombs(tileRemovalSet, bombRemovalSet);
                 tileRemovalSet.removeAll(bombRemovalSet);                
                 
                 // Start the line removal animations if there are any
                 // non-bomb tiles.
                 if (tileRemovalSet.size() > 0)
                 {
                     int i = 0;
                     for (Iterator it = tileRemovalSet.iterator(); it.hasNext(); )
                     {
                         TileEntity t = boardMan.getTile((Integer) it.next());
                                             
                         if (tileRemovalUseJumpAnimation == true)
                         {
                             i++;
                             int angle = i % 2 == 0 ? 70 : 180 - 70;                        
                             t.setAnimation(new JumpFadeOutAnimation(
                                 0.3, angle, 0.001, 800, layerMan, t));
                         }
                         else                        
                             t.setAnimation(new ZoomOutAnimation(t));                        
                     }
                     
                     // Clear the animation flag.
                     tileRemovalUseJumpAnimation = false;
 
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
                 
                 // Increment cascade.
                 cascadeCount++;
 
                 // Used below.
                 int deltaScore = 0;
                 
                 // Get the tiles the bombs would affect.
                 boardMan.processBombs(bombRemovalSet, tileRemovalSet);
                 deltaScore = scoreMan.calculateLineScore(
                         tileRemovalSet, 
                         ScoreManager.TYPE_BOMB, 
                         cascadeCount);
                 
                 // Show the SCT.
                 XYPosition p = boardMan.determineCenterPoint(tileRemovalSet);
                 Label label = ResourceFactory.get().getLabel(p.x, p.y);
                 label.setText(String.valueOf(deltaScore));
                 label.setAlignment( Label.HCENTER | Label.VCENTER);
                 label.setColor(SCORE_BOMB_COLOR);
                 label.setSize(scoreMan.determineFontSize(deltaScore));
                 
                 animationMan.add(new FloatFadeOutAnimation(                         
                         0, -1, layerMan, label));
                 
                 // Release references.
                 p = null;
                 label = null;                
                                 
                 // Play the sound.
                 soundMan.play(SoundManager.BOMB);
 
                 // Extract all the new bombs.
                 Set newBombRemovalSet = new HashSet<Integer>();
                 boardMan.scanBombs(tileRemovalSet, newBombRemovalSet);                                                
                 newBombRemovalSet.removeAll(bombRemovalSet);
 
                 // Remove all tiles that aren't new bombs.
                 tileRemovalSet.removeAll(newBombRemovalSet);
 
                 // Start the line removal animations.
                 for (Iterator it = tileRemovalSet.iterator(); it.hasNext(); )
                 {
                     TileEntity t = boardMan.getTile((Integer) it.next());
 
                     if (t instanceof BombTileEntity)                    
                         t.setAnimation(new ExplosionAnimation(t, layerMan));                                            
                     else
                         t.setAnimation(new JiggleFadeOutAnimation(t));
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
                         startRefactor(200);
                 }  
             }
             
             // See if we should clear the cascade count.
             if (isRefactoring() == false 
                     && isTileRemoving() == false
                     && pieceMan.isTileDropInProgress() == false)
                 cascadeCount = 0;
 
             // Animate all the pieces.
             boardMan.animate(delta);    
             
             // Animation all animations.
             animationMan.animate(delta);
 
             // Handle the timer.
             if (boardMan.isVisible() == true)
                 timerMan.incrementInternalTime(delta);
 
             // Check to see if we should force a piece commit.
             if (timerMan.getTime() < 0)
             {
                 // Commit the piece.
                 timerMan.setTime(0);
                 pieceMan.initiateCommit(this);            
             }
 
             // Update piece manager logic and then draw it.
             pieceMan.updateLogic(this);
                        
             // Draw the timer text.
             timerLabel.setText(String.valueOf(timerMan.getTime()));		
 
             // Draw the score text.
             scoreLabel.setText(String.valueOf(scoreMan.getTotalScore()));
 
             // Draw the high score text.
             highScoreLabel.setText(String.valueOf(scoreMan.getHighScore()));
 
             // Set the level text.
             levelLabel.setText(String.valueOf(worldMan.getLevel()));
             
             // Update the progress bar.
             progressBar.setProgress(scoreMan.getLevelScore());
             
             // Reset the line count.
             totalLineCount += cycleLineCount;
             cycleLineCount = 0;
         }                
                 
         // Whether or not the frame was updated.
         boolean updated = false;
         
         // If the background is dirty, then redraw everything.
         if (background.isDirty() == true)
         {            
             layerMan.draw();
             background.setDirty(false);
             updated = true;
         }
         // Otherwise, only draw what needs to be redrawn.
         else
         {                       
             updated = layerMan.drawRegion(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);         
         }
 		
 		// if escape has been pressed, stop the game
 		if (window.isKeyPressed(KeyEvent.VK_ESCAPE))
 		{
 			System.exit(0);
 		}
         
         // If up or down have been pressed
         if (window.isKeyPressed(KeyEvent.VK_UP))
 		{
 			musicMan.increaseVolume();
 		}
         
         if (window.isKeyPressed(KeyEvent.VK_DOWN))
 		{
 			musicMan.decreaseVolume();
 		}
         
         return updated;
 	}
 
 	/**
 	 * Notification that the game window has been closed
 	 */
 	public void windowClosed()
 	{
         if (this.propertyMan != null)
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
      * Notification that the game window has been deactivated in some way.
      */
     public void windowDeactivated()
     {
        // Don't pause game if we're showing the game over screen.
        if (gameOverInProgress == false)
            this.pauseGame();
        
         this.background.setDirty(true);
     }
     
      /**
      * Notification that the game window has been reactivated in some way.
      */
     public void windowActivated()
     {
         //Force a background redraw.
         if (this.background != null)
            this.background.setDirty(true);        
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
 //        System.setProperty("sun.java2d.translaccel", "true");
 //        System.setProperty("sun.java2d.ddforcevram", "true");   
         
         try
         {
             Game g = new Game(ResourceFactory.JAVA2D);
             g.startRendering();		
         }
         catch (Exception e)
         {
             Util.handleException(e);
         }
 	}
     
   
 }
