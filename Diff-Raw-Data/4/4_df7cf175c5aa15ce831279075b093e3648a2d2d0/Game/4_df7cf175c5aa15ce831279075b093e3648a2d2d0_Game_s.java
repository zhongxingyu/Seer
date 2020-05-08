 /*
  *  Wezzle
  *  Copyright (c) 2007-2008 Couchware Inc.  All rights reserved.
  */
 
 package ca.couchware.wezzle2d;
 
 import ca.couchware.wezzle2d.ManagerHub.Manager;
 import ca.couchware.wezzle2d.ResourceFactory.LabelBuilder;
 import ca.couchware.wezzle2d.animation.AnimationAdapter;
 import ca.couchware.wezzle2d.animation.FadeAnimation;
 import ca.couchware.wezzle2d.animation.FinishedAnimation;
 import ca.couchware.wezzle2d.animation.IAnimation;
 import ca.couchware.wezzle2d.animation.MetaAnimation;
 import ca.couchware.wezzle2d.animation.MetaAnimation.RunRule;
 import ca.couchware.wezzle2d.animation.MoveAnimation;
 import ca.couchware.wezzle2d.audio.Sound;
 import ca.couchware.wezzle2d.event.GameEvent;
 import ca.couchware.wezzle2d.graphics.IDrawer;
 import ca.couchware.wezzle2d.graphics.IPositionable.Alignment;
 import ca.couchware.wezzle2d.manager.Achievement;
 import ca.couchware.wezzle2d.manager.SettingsManager;
 import ca.couchware.wezzle2d.manager.BoardManager.AnimationType;
 import ca.couchware.wezzle2d.manager.LayerManager.Layer;
 import ca.couchware.wezzle2d.manager.ListenerManager.Listener;
 import ca.couchware.wezzle2d.manager.Settings.Key;
 import ca.couchware.wezzle2d.menu.Loader;
 import ca.couchware.wezzle2d.menu.MainMenu;
 import ca.couchware.wezzle2d.transition.CircularTransition;
 import ca.couchware.wezzle2d.transition.ITransition;
 import ca.couchware.wezzle2d.tutorial.BasicTutorial;
 import ca.couchware.wezzle2d.tutorial.BombTutorial;
 import ca.couchware.wezzle2d.tutorial.GravityTutorial;
 import ca.couchware.wezzle2d.tutorial.RocketTutorial;
 import ca.couchware.wezzle2d.tutorial.StarTutorial;
 import ca.couchware.wezzle2d.ui.AchievementNotification;
 import ca.couchware.wezzle2d.ui.ITextLabel;
 import ca.couchware.wezzle2d.ui.ProgressBar;
 import ca.couchware.wezzle2d.ui.RadioItem;
 import ca.couchware.wezzle2d.ui.SpeechBubble;
 import ca.couchware.wezzle2d.ui.Button;
 import ca.couchware.wezzle2d.util.CouchLogger;
 import ca.couchware.wezzle2d.util.ImmutablePosition;
 import ca.couchware.wezzle2d.util.ImmutableRectangle;
 import java.awt.Canvas;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.EnumSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javazoom.jlgui.basicplayer.BasicPlayer;
 
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
  * @author Cameron McKay
  * @author Kevin Grad 
  * @author Kevin Glass
  */
 public class Game extends Canvas implements IWindowCallback
 {	  
     //--------------------------------------------------------------------------
     // Static Members
     //--------------------------------------------------------------------------                                           
     
     /** The manager hub. */
     final private ManagerHub hub = ManagerHub.get();
           
     /** The width of the screen. */
     final public static int SCREEN_WIDTH = 800;
     
     /** The height of the screen  */
     final public static int SCREEN_HEIGHT = 600;      
     
     /** A rectangle the size of the screen. */
     final public static ImmutableRectangle SCREEN_RECTANGLE = 
             new ImmutableRectangle(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);   
     
     /** The name of the application. */
     final public static String APPLICATION_NAME = "Wezzle";
     
     /** The version of the application. */
     final public static String APPLICATION_VERSION = "Test 7";     
     
     /** The full title of the game. */
     final public static String TITLE = APPLICATION_NAME + " " + APPLICATION_VERSION;
     
     /** The copyright. */
     final public static String COPYRIGHT = "\u00A9 2009 Couchware Inc.";
     
     //--------------------------------------------------------------------------
     // Public Members
     //--------------------------------------------------------------------------                  
     
     /** The loader. */
     public Loader loader;
     
     /** The main menu. */
     public MainMenu mainMenu;
     
     /** The resource factory. */
     private ResourceFactory resourceFactory = ResourceFactory.get();
     
     /** The game UI. */
     private UI ui;          
     
     /** The refactorer. */
     private Refactorer refactorer;
     
     /** The tile dropper. */
     private TileDropper tileDropper;
     
     /** The tile remover. */
     private TileRemover tileRemover;        
     
     /** The window that is being used to render the game. */
     private IWindow window;    
     
     //--------------------------------------------------------------------------
     // Private Members
     //--------------------------------------------------------------------------                                 
     
     /** The current build number. */
     final private String BUILD_NUMBER = "N/A";        
     
     /** The current drawer. */
     private IDrawer drawer;
     
     /** The normal title of the window. */
     private String windowTitle = APPLICATION_NAME;	              
     
     /** The executor used by certain managers. */
     private Executor executor;                   
 
     /** If true */
     private boolean activateBoardShowAnimation = false;
     
     /** If true */
     private boolean activateBoardHideAnimation = false;
    
     /** The board animation type to use. */
     private AnimationType boardAnimationType;
     
     /**
      * The animation that will indicate whether the board animation is 
      * complete.
      */
     private IAnimation boardAnimation = null;
     
     /** If true, the game will end next loop. */
     private boolean activateGameOver = false;   
     
     /** If true, a game over has been activated. */
     private boolean gameOverInProgress = false;	
     
     /** The list of notifications to be shown. */
     private Queue<AchievementNotification> notificationQueue
             = new LinkedList<AchievementNotification>();
     
     /** The current notification animation. */
     private IAnimation notificationAnimation = FinishedAnimation.get();
     
     /** A list that keeps track of the chains in moves. */
     List<Chain> chainList = new ArrayList<Chain>();
     
     /** The targets that may be transitioned to. */
     public enum TransitionTarget
     { NOTHING, GAME, MENU }
     
     /** The transition target. */
     private TransitionTarget transitionTo = TransitionTarget.NOTHING;    
           
     /**
      * The transition variable.  This is the transition animation that is used
      * to transition from the menu to the game and vice-versa.
      */
     private ITransition transition;      
                 
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
 	public Game(ResourceFactory.Renderer renderer) 
 	{
        
         // Print the build number.
         Class cls = this.getClass();
         CouchLogger.get().recordMessage(cls, "Date: " + (new Date()));
         CouchLogger.get().recordMessage(cls, "Wezzle Build: " + BUILD_NUMBER);
         CouchLogger.get().recordMessage(cls, "Wezzle Version: " + APPLICATION_VERSION);
         CouchLogger.get().recordMessage(cls, "Java Version: " + System.getProperty("java.version"));
         CouchLogger.get().recordMessage(cls, "OS Name: " + System.getProperty("os.name"));
         CouchLogger.get().recordMessage(cls, "OS Architecture: " + System.getProperty("os.arch"));
         CouchLogger.get().recordMessage(cls, "OS Version: " + System.getProperty("os.version"));
         
 		// Create a window based on a chosen rendering method.
 		ResourceFactory.get().setRenderer(renderer);		        
         		                      
         window = ResourceFactory.get().getWindow();
         window.setResolution(SCREEN_WIDTH, SCREEN_HEIGHT);
         window.setGameWindowCallback(Game.this);
         window.setTitle(windowTitle);
 	}
 
 	public void start()
 	{
 		window.start();
 	}
             
     public void startBoard()
     {
         hub.boardMan.generateBoard(hub.itemMan.getItemList(), hub.levelMan.getLevel());          
         startBoardShowAnimation(AnimationType.ROW_FADE);
     }                   
     
     public void startTransitionTo(TransitionTarget target)
     {
         switch (target)
         {
             case MENU:
                 
                 // Create the main menu.
                 // Empty the mouse events.
                 window.clearMouseEvents();
 
                 // Shut off the music.
                 hub.musicMan.stop();
 
                 // Create the main menu.
                 mainMenu = new MainMenu(hub);
                 mainMenu.setDisabled(true);
 
                 // Create the layer manager transition animation.
                 transitionTo = TransitionTarget.MENU;
                 this.transition = new CircularTransition.Builder(mainMenu)
                         .minRadius(10).speed(400).end();
                 setDrawer(transition);
                 
                 this.transition.addAnimationListener(new AnimationAdapter()
                 {                    
                     @Override
                     public void animationFinished()
                     { mainMenu.setDisabled(false); }                            
                 });
 
                 // Queue in the animation manager.
                 hub.animationMan.add(transition);
                 
                 break;
         }
     }
     
     /**
      * Initialize various members.
      */
     private void initializeCoreManagers()
     {
         // Create the UI.
         ui = UI.newInstance(hub);        
         hub.listenerMan.registerListener(Listener.LEVEL, this.ui);
         hub.listenerMan.registerListener(Listener.PIECE, this.ui);
         hub.listenerMan.registerListener(Listener.SCORE, this.ui);       
         hub.listenerMan.registerListener(Listener.TIMER, this.ui);
         
         // Get the singleton.
         refactorer = Refactorer.get();
 
         // Get the singleton.
         tileDropper = TileDropper.get();
 
         // Get the singleton.
         tileRemover = TileRemover.get();                  
         
         // Make the tile remover listen for level events.
         hub.listenerMan.registerListener(Listener.LEVEL, this.tileRemover);
     }
     
     /**
      * Initialize the tutorials.
      */
     public void initializeTutorials()
     {        
         // Add the tutorials to it.
         hub.tutorialMan.add(new BasicTutorial(refactorer));
         hub.tutorialMan.add(new GravityTutorial(refactorer));
         hub.tutorialMan.add(new RocketTutorial(refactorer));
         hub.tutorialMan.add(new BombTutorial(refactorer));
         hub.tutorialMan.add(new StarTutorial(refactorer));
     }
     
 	/**
 	 * Initialize the common elements for the game.
 	 */
 	public void initialize()
 	{                
         // Initialize the executor.        
         executor = Executors.newCachedThreadPool();
                 
         // Make sure the listener and settings managers are ready.
         hub.initialize(EnumSet.of(Manager.ANIMATION, Manager.LISTENER, Manager.SETTINGS), this);       
 
         // Create the loader.        
         loader = new Loader("Loading Wezzle...", hub.settingsMan);
         setDrawer(loader);
         
         // Preload the sprites.
         resourceFactory.preloadSprites(loader);                                        
                                 
         // Initialize managers.
         loader.addTask(new Runnable()
         {
            public void run() 
            { 
                hub.initialize(EnumSet.allOf(Manager.class), Game.this);               
                hub.layerMan.setDisabled(true);
            }
         });
                                
         // Initialize the core managers.
         loader.addTask(new Runnable()
         {
            public void run()
            { initializeCoreManagers(); }
         });
 	}                      
     
     public void update()
     {                
         // If the loader is running, bypass all the rendering to show it.        
         if (this.drawer == loader)
         {   
             // Animate all animations.
             if (hub.animationMan != null) hub.animationMan.animate();
             
             // Update the logic.
             loader.updateLogic(this, hub);
             
             if (loader.getState() == Loader.State.FINISHED)
             {
                 // Remove the loader.
                 loader = null;
                 
                 // Empty the mouse events.
                 window.clearMouseEvents();
                                
                 // Create the main menu.
                 mainMenu = new MainMenu(hub);
                 setDrawer(mainMenu);
             }   
             else return;                        
         }
         
         // If the main menu is running, bypass all rendering to show it.
         if (this.drawer == mainMenu)
         {
             // Animate all animations.
             if (hub.animationMan != null) hub.animationMan.animate();
             
             // Update the main menu logic.
             mainMenu.updateLogic(this, hub);
             
             if (mainMenu.getState() == MainMenu.State.FINISHED)
             {                                
                 // Empty the mouse events.
                 window.clearMouseEvents();   
                 
                 // Remove the loader.
                 mainMenu.dispose();
                 mainMenu = null;   
                 
                 // Create the layer manager transition animation.
                 this.transitionTo = TransitionTarget.GAME;
                 this.transition = new CircularTransition.Builder(hub.layerMan)
                         .minRadius(10).speed(400).end();
                 setDrawer(transition);                
                 
                 this.transition.addAnimationListener(new AnimationAdapter() 
                 {
                     @Override
                     public void animationFinished()
                     { hub.layerMan.setDisabled(false); }
                 });
                 
                 // Queue in the animation manager.
                 hub.animationMan.add(transition);
                 
                 // Start the music.
                 hub.musicMan.play();
                 
                 // See if the music is off.
                 if (hub.settingsMan.getBoolean(Key.USER_MUSIC) == false)
                 {
                     hub.musicMan.setPaused(true);
                 }
             }   
             else
             {      
                 // Fire the mouse events.
                 window.fireMouseEvents();
                 window.updateKeyPresses();
                 
                 // Draw using the loader.
                 return;
             }
         } // end if
         
         // See if the main menu transition is in progress
         if (this.drawer == transition)
         {
             // Animate all animations.
             if (hub.animationMan != null) hub.animationMan.animate();
             
             // Otherwise see if the transition is over.
             if (transition.isFinished() == false) return;           
             else 
             {
                 switch(transitionTo)          
                 {
                     case GAME:                                        
                         setDrawer(hub.layerMan);
                         break;
                         
                     case MENU:
                         setDrawer(mainMenu);
                         break;
                         
                     case NOTHING:
                         throw new IllegalStateException("This should not occur.");                        
                 }
                 
                 transitionTo = TransitionTarget.NOTHING;                
                 transition = null;
             }            
         } // end if
                      
         // Update UI.
         ui.updateLogic(this, hub);
         
         // Check on board animation.
         if (boardAnimation != null && boardAnimation.isFinished() == true)
         {
             // Set animation visible depending on what animation
             // was just performed.
             if (boardAnimation instanceof FadeAnimation)   
             {
                 // Cast it to a fade animation.
                 FadeAnimation f = (FadeAnimation) boardAnimation;
                 
                 switch (f.getType())
                 {
                     case IN:
                         
                         hub.boardMan.setVisible(true);
                         hub.pieceMan.showPieceGrid();
                         hub.pieceMan.startAnimation(hub.timerMan);
                         break;
                         
                     case OUT:
                         
                         hub.boardMan.setVisible(false);
                         hub.pieceMan.hidePieceGrid();
                         break;
                         
                     default:
                         
                         throw new IllegalStateException(
                                 "Unrecogonized fade animation type.");
                 }                                               
             }            
             else
             {
                 throw new RuntimeException(
                         "Unrecognized board animation class.");
             }
 
             // Clear the board animation.
             boardAnimation = null;
 
             // Clear mouse button presses.
             hub.pieceMan.clearMouseButtonSet();
 
             // If game over is in progress, make a new board and start.
             if (gameOverInProgress == true)
             {
                 // Show the game over screen.
                 ui.showGameOverGroup(hub.groupMan);
                 
                 // Clear the flag.
                 gameOverInProgress = false;
             }
         }
         else if (boardAnimation != null && boardAnimation.isFinished() == false)
         {
             // Board is still dirty due to animation.
             hub.boardMan.setDirty(true);
         }
         
         // Update all the group logic.
         hub.groupMan.updateLogic(this, hub);
         
         // Uphdate the music manager logic.
         hub.musicMan.updateLogic(this, hub);
 
         // Check to see if we should be showing the board.
         if (activateBoardShowAnimation == true)
         {
             // Hide the piece.            
             hub.pieceMan.hidePieceGrid();
             hub.pieceMan.stopAnimation();
             
             // Start board show animation.            
             boardAnimation = hub.boardMan.animateShow(boardAnimationType);
             hub.boardMan.setDirty(true);
 
             // Clear flag.
             clearBoardShowAnimation();                                
         }
 
         // Check to see if we should be hiding the board.
         if (activateBoardHideAnimation == true)
         {
             // Hide the piece.
             hub.pieceMan.hidePieceGrid();
             hub.pieceMan.stopAnimation();
             
             // Start board hide animation.            
             boardAnimation = hub.boardMan.animateHide(boardAnimationType);
             hub.boardMan.setDirty(true);
                                           
             // Clear flag.
             clearBoardHideAnimation();                                
         }      
         
         // If the pause button is not on, then we proceed with the
         // normal game loop.
         if (hub.groupMan.isActivated() == false)
         {
             updateBoard();
         }
                         
         // Fire all the queued mouse events.
         window.fireMouseEvents();                
         window.updateKeyPresses();
                         
         // The keys.      
         if (window.isKeyPressed('R'))
         {
             hub.settingsMan.loadExternalSettings();
             CouchLogger.get().recordMessage(this.getClass(), "Reloaded external settings.");
         }
         
         // Check the achievements.
         hub.achievementMan.evaluate(this, hub);
         if (!hub.tutorialMan.isTutorialRunning()
                 && hub.achievementMan.isNewAchievementCompleted())
         {
             // Report to log.
             hub.achievementMan.reportNewlyCompleted();
             
             List<Achievement> achievementList = hub.achievementMan.getNewlyCompletedAchievementList();
             for (Achievement ach : achievementList)
             {
                 AchievementNotification notif = new AchievementNotification.Builder(0, 0, ach)
                     .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.CENTER))
                     .end();
                 
                 this.notificationQueue.offer(notif);
             }                                    
         }
         
         // Check to see if there are any notifications to show.
         if (!this.notificationQueue.isEmpty() && this.notificationAnimation.isFinished())
         {                        
             final AchievementNotification notif = this.notificationQueue.remove();
             
             int x = Game.SCREEN_WIDTH + 10 + notif.getWidth() / 2;            
             notif.setPosition(x, 490);
             
             IAnimation slideIn = new MoveAnimation.Builder(notif)
                     .speed(375).minX(670).duration(4000).theta(180).end();
             
             IAnimation fadeOut = new FadeAnimation.Builder(FadeAnimation.Type.OUT, notif)                    
                     .duration(500).end();                        
             
             IAnimation meta = new MetaAnimation.Builder()
                     .runRule(RunRule.SEQUENCE)
                     .add(slideIn)                    
                     .add(fadeOut)
                     .end();    
             
             meta.addAnimationListener(new AnimationAdapter()
             {
                 @Override
                 public void animationStarted()
                 { hub.layerMan.add(notif, Layer.UI); }
                 
                 @Override
                 public void animationFinished()
                 { hub.layerMan.remove(notif, Layer.UI); }
             });
                        
             this.notificationAnimation = meta;
             hub.animationMan.add(meta);
         }
     }
     
 	/**
 	 * Notification that a frame is being rendered. Responsible for running game
 	 * logic and rendering the scene.
      * 
      * @return True if the frame has been updated, false if nothing has been
      * updated.
 	 */
 	public boolean draw()
 	{		      
         // If the background is dirty, then redraw everything.
         if (this.drawer != null)
         {
             return this.drawer.draw();
         }
         else 
         {
             return false;
         }
 	}  
     
     /**
      * Handles the logic and rendering of the game scene.
      * 
      * @param delta The amount of time that has passed since the last
      * board update.
      */
     private void updateBoard()
     {
         // See if it's time to level-up.
         if (!this.isCompletelyBusy())
         {
             // Handle Level up.
             if (hub.scoreMan.getLevelScore() >= hub.scoreMan.getTargetLevelScore())
             {    
                 // Hide piece.                    
                 hub.pieceMan.hidePieceGrid();
                 hub.pieceMan.stopAnimation();
                 hub.timerMan.setPaused(true);
 
                 CouchLogger.get().recordMessage(this.getClass(), "Level up!");
                 //levelMan.levelUp(this);
                
                 hub.levelMan.incrementLevel();
 
                 hub.soundMan.play(Sound.LEVEL_UP);
 
                 ImmutablePosition pos = hub.pieceMan.getCursorPosition();
                 int x = pos.getX() + hub.boardMan.getCellWidth()  / 2;
                 int y = pos.getY() + hub.boardMan.getCellHeight() / 2;
                                                 
                 final ITextLabel label = new LabelBuilder(x, y)
                         .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.LEFT))
                         .color(hub.settingsMan.getColor(Key.GAME_COLOR_PRIMARY))
                         .size(hub.settingsMan.getInt(Key.SCT_LEVELUP_TEXT_SIZE))
                         .text(hub.settingsMan.getString(Key.SCT_LEVELUP_TEXT)).end();
 
                 IAnimation a1 = new FadeAnimation.Builder(FadeAnimation.Type.OUT, label)
                         .wait(hub.settingsMan.getInt(Key.SCT_SCORE_FADE_WAIT))
                         .duration(hub.settingsMan.getInt(Key.SCT_SCORE_FADE_DURATION))
                         .minOpacity(hub.settingsMan.getInt(Key.SCT_SCORE_FADE_MIN_OPACITY))
                         .maxOpacity(hub.settingsMan.getInt(Key.SCT_SCORE_FADE_MAX_OPACITY))
                         .end();                 
                 
                 IAnimation a2 = new MoveAnimation.Builder(label)
                         .duration(hub.settingsMan.getInt(Key.SCT_LEVELUP_MOVE_DURATION))
                         .speed(hub.settingsMan.getInt(Key.SCT_LEVELUP_MOVE_SPEED))
                         .theta(hub.settingsMan.getInt(Key.SCT_LEVELUP_MOVE_THETA))
                         .end(); 
                 
                 a2.addAnimationListener(new AnimationAdapter()
                 {
                     @Override
                     public void animationStarted()
                     { hub.layerMan.add(label, Layer.EFFECT); }
 
                     @Override
                     public void animationFinished()
                     { hub.layerMan.remove(label, Layer.EFFECT); }
                 });
 
                 hub.animationMan.add(a1);
                 hub.animationMan.add(a2);
                 a1 = null;
                 a2 = null;                    
             }
         } // end if
 
         // See if it's game ovaries.
         if (activateGameOver == true)
         {
             // Clear flag.
             activateGameOver = false;                            
 
             // Set in progress flag.
             gameOverInProgress = true;
 
             // Hide the board.
             startBoardHideAnimation(AnimationType.ROW_FADE);                
         }                                                                  
 
         // Run the refactorer.
         this.refactorer.updateLogic(this, hub);
       
         // Update the tile remover.
         this.tileRemover.updateLogic(this, hub);
       
         // See if we should clear the cascade count.
         if (!this.refactorer.isRefactoring()
                 && !this.tileRemover.isTileRemoving()
                 && !this.tileDropper.isTileDropping())
         {
             hub.statMan.resetChainCount();
             hub.statMan.resetLineChainCount();
         }
 
         // Animation all animations.
         hub.animationMan.animate();
 
         // Update the tutorial manager logic. This must be done after the world
         // manager because it relies on the proper items being in the item list.
         hub.tutorialMan.updateLogic(this, hub);
 
         // Handle the timer.
         if (hub.boardMan.isVisible()                
                 && !hub.tutorialMan.isTutorialRunning()
                 && !this.isContextManipulating()
                 && !this.isTileManipulating())
         {
             hub.timerMan.updateLogic(this);
 
             if (hub.timerMan.getCurrrentTime() <= 0)
             {
                 hub.pieceMan.initiateCommit(this, hub);
             }
         }
 
         // Update tile dropper.
         this.tileDropper.updateLogic(this, hub);
         
         // Update piece manager logic and then draw it.
         hub.pieceMan.updateLogic(this, hub);
 
         // Update the item manager logic.
         hub.itemMan.updateLogic(this, hub);            
         
         // Reset the line count.
         //statMan.incrementLineCount(statMan.getCycleLineCount());               
         hub.statMan.resetCycleLineCount();
     }       
       
     /**
      * A method to check whether the board is busy.
      * Note: This method does NOT check to see if the tiles are dropping.
      * 
      * @return True if it is, false otherwise.
      */
     public boolean isContextManipulating()
     {
        return      
                activateGameOver == true
                || gameOverInProgress == true
                || activateBoardShowAnimation == true
                || activateBoardHideAnimation == true               
                || this.boardAnimation != null;
     }       
     
     public boolean isTileManipulating()
     {
         return 
                refactorer.isRefactoring()               
                || tileRemover.isTileRemoving() 
                || tileDropper.isTileDropping();
     }        
     
     public boolean isCompletelyBusy()
     {
         return isContextManipulating() || isTileManipulating();
     }
     
     /**
      * Checks whether tiles are, or are about to be, removed.
      */
 
     
     public void startBoardShowAnimation(AnimationType type)
     {
         // Set the flag.
         if (activateBoardHideAnimation == true)
             throw new IllegalStateException(
                     "Attempted to show board while it is being hidden.");
         
         if (activateBoardShowAnimation == true)
             throw new IllegalStateException(
                     "Attempted to show board while it is already being shown.");
         
         activateBoardShowAnimation = true;
         boardAnimationType = type;
     }
     
     public void clearBoardShowAnimation()
     {
         // Clear the flag.
         activateBoardShowAnimation = false;        
     }
     
     public void startBoardHideAnimation(AnimationType type)
     {
         // Set the flag.
         if (activateBoardShowAnimation == true)
             throw new IllegalStateException(
                     "Attempted to hide board while it is being shown.");
         
         if (activateBoardHideAnimation == true)
             throw new IllegalStateException(
                     "Attempted to hide board while it is already being hidden.");
         
         activateBoardHideAnimation = true;
         boardAnimationType = type;
     }
     
     public void clearBoardHideAnimation()
     {
         // Clear the flag.
         activateBoardHideAnimation = false;
     }
     
     public void startGameOver()
     {
         CouchLogger.get().recordMessage(this.getClass(), "Game over!");
 
         // Add the new score.
         hub.highScoreMan.offerScore(
                 "Unused",
                 hub.scoreMan.getTotalScore(),
                 hub.levelMan.getLevel());
 
         // Notify of game over.
         hub.listenerMan.notifyGameOver(new GameEvent(this, 
                 hub.levelMan.getLevel(),
                 hub.scoreMan.getTotalScore()));
         
         // Activate the game over process.
         this.activateGameOver = true;
     }       
     
     //--------------------------------------------------------------------------
     // Getters and Setters
     //--------------------------------------------------------------------------       
 
     public IDrawer getDrawer()
     {
         return drawer;
     }
 
     public void setDrawer(IDrawer drawer)
     {
         this.drawer = drawer;
     }
     
     //--------------------------------------------------------------------------
     // Window Methods
     //--------------------------------------------------------------------------
     
 	/**
 	 * Notification that the game window has been closed
 	 */
 	public void windowClosed()
 	{                    
         try
         {
             // Save the properites.
             if (hub.settingsMan != null)
             {
                 hub.settingsMan.saveSettings();
             }
 
             // Save the log data.            
             CouchLogger.get().write();
         }
         catch(Exception e)
         {
             CouchLogger.get().recordException(this.getClass(), e);
         }        
         
      	System.exit(0);
 	}        
     
     /**
      * Notification that the game window has been deactivated in some way.
      */
     public void windowDeactivated()
     {
         // Don't pause game if we're showing the game over screen.
        if (hub.groupMan != null && !hub.groupMan.isActivated())
         {
             ui.showPauseGroup(hub.groupMan);
         }
                         
         if (hub.layerMan != null)
         {
             hub.layerMan.forceRedraw();
         }                
     }
     
      /**
      * Notification that the game window has been reactivated in some way.
      */
     public void windowActivated()
     {                
         // Force redraw.
         if (loader != null)
         {
             loader.forceRedraw();
         }
             
         if (hub.layerMan != null)
         {
             hub.layerMan.forceRedraw();
         }
     }
     
     public Executor getExecutor()
     {
         return executor;
     }
 
     public ManagerHub getManagerHub()
     {
         return hub;
     }
     
     public Refactorer getRefactorer()
     {
         return refactorer;
     }
     
     public ResourceFactory getResourceFactory()
     {
         return resourceFactory;
     }
        
     public TileDropper getTileDropper()
     {
         return tileDropper;
     }
 
     public TileRemover getTileRemover()
     {
         return tileRemover;
     }
 
     public UI getUI()
     {
         return ui;
     }
 
     public IWindow getWindow()
     {
         return window;
     }
 
     public List<Chain> getChainList()
     {
         return chainList;
     }
     
     //--------------------------------------------------------------------------
     // Main method
     //--------------------------------------------------------------------------
     
 	/**
 	 * The entry point into the game. We'll simply create an instance of class
 	 * which will start the display and game loop.
 	 * 
 	 * @param argv
 	 *            The arguments that are passed into our game
 	 */
 	public static void main(String argv[])
 	{		              
         // Make sure the setting manager is loaded.       
         SettingsManager settingsMan = SettingsManager.get();      
         
         // Set the default color scheme.
         ResourceFactory.setDefaultLabelColor(settingsMan.getColor(Key.GAME_COLOR_PRIMARY));
         ProgressBar.setDefaultColor(settingsMan.getColor(Key.GAME_COLOR_PRIMARY));
         RadioItem.setDefaultColor(settingsMan.getColor(Key.GAME_COLOR_PRIMARY));
         SpeechBubble.setDefaultColor(settingsMan.getColor(Key.GAME_COLOR_PRIMARY));
         Button.setDefaultColor(settingsMan.getColor(Key.GAME_COLOR_PRIMARY));       
         Achievement.Difficulty.initializeDifficultyColorMap(settingsMan);
 
         // Set the BasicPlayer logger level.
         Logger.getLogger(BasicPlayer.class.getName()).setLevel(Level.OFF);
         
         try
         {
             //Game game = new Game(ResourceFactory.Renderer.JAVA2D);
             Game game = new Game(ResourceFactory.Renderer.LWJGL);
             game.start();		
         }
         catch (Exception e)
         {
             CouchLogger.get().recordException(Game.class, e);
             CouchLogger.get().write();
         }
 	}
   
 }
