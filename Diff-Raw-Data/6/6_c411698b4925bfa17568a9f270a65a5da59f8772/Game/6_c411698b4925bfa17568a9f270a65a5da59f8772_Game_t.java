 /*
  *  Wezzle
  *  Copyright (c) 2007-2010 Couchware Inc.  All rights reserved.
  */
 package ca.couchware.wezzle2d;
 
 import ca.couchware.wezzle2d.tracker.Chain;
 import ca.couchware.wezzle2d.ManagerHub.Manager;
 import ca.couchware.wezzle2d.ResourceFactory.LabelBuilder;
 import ca.couchware.wezzle2d.animation.AnimationAdapter;
 import ca.couchware.wezzle2d.animation.FadeAnimation;
 import ca.couchware.wezzle2d.animation.IAnimation;
 import ca.couchware.wezzle2d.animation.MoveAnimation;
 import ca.couchware.wezzle2d.audio.Sound;
 import ca.couchware.wezzle2d.difficulty.GameDifficulty;
 import ca.couchware.wezzle2d.event.GameEvent;
 import ca.couchware.wezzle2d.graphics.IDrawer;
 import ca.couchware.wezzle2d.graphics.IPositionable.Alignment;
 import ca.couchware.wezzle2d.manager.Achievement;
 import ca.couchware.wezzle2d.manager.BoardManager.AnimationType;
 import ca.couchware.wezzle2d.manager.LayerManager.Layer;
 import ca.couchware.wezzle2d.manager.ListenerManager.Listener;
 import ca.couchware.wezzle2d.manager.Settings.Key;
 import ca.couchware.wezzle2d.menu.Loader;
 import ca.couchware.wezzle2d.menu.MainMenu;
 import ca.couchware.wezzle2d.tracker.Tracker;
 import ca.couchware.wezzle2d.transition.CircularTransition;
 import ca.couchware.wezzle2d.transition.ITransition;
 import ca.couchware.wezzle2d.tutorial.BasicTutorial;
 import ca.couchware.wezzle2d.tutorial.BombTutorial;
 import ca.couchware.wezzle2d.tutorial.GravityTutorial;
 import ca.couchware.wezzle2d.tutorial.ITutorial;
 import ca.couchware.wezzle2d.tutorial.RocketTutorial;
 import ca.couchware.wezzle2d.tutorial.RotateTutorial;
 import ca.couchware.wezzle2d.tutorial.StarTutorial;
 import ca.couchware.wezzle2d.ui.AchievementNotification;
 import ca.couchware.wezzle2d.ui.ITextLabel;
 import ca.couchware.wezzle2d.util.CouchLogger;
 import ca.couchware.wezzle2d.util.ImmutablePosition;
 import ca.couchware.wezzle2d.util.ImmutableRectangle;
 import edu.stanford.ejalbert.BrowserLauncher;
 import java.applet.Applet;
 import java.applet.AppletContext;
 import java.awt.Canvas;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.MessageDigest;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.EnumSet;
 import java.util.List;
 import paulscode.sound.SoundSystem;
 import paulscode.sound.SoundSystemConfig;
 import paulscode.sound.SoundSystemException;
 import paulscode.sound.codecs.CodecJOgg;
 import paulscode.sound.codecs.CodecWav;
 import paulscode.sound.libraries.LibraryLWJGLOpenAL;
 
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
 public class Game implements IWindowCallback
 {
     //--------------------------------------------------------------------------
     // Final & Static Members
     //--------------------------------------------------------------------------
     
     /** The manager hub. */
     final private ManagerHub hub = ManagerHub.newInstance();
     /** The width of the screen. */
     final public static int SCREEN_WIDTH = 800;
     /** The height of the screen  */
     final public static int SCREEN_HEIGHT = 600;
     /** A rectangle the size of the screen. */
     final public static ImmutableRectangle SCREEN_RECTANGLE =
             new ImmutableRectangle(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
     /** Is the game running as an applet? */
     final private static boolean APPLET = false;
     /** The name of the application. */
     final public static String APPLICATION_NAME = "Wezzle";
     /** The version of the application. */
     final public static int APPLICATION_VERSION_MAJOR = 1;
     final public static int APPLICATION_VERSION_MINOR = 3;
     final public static String APPLICATION_DISTRIBUTION = APPLET ? "Web" : "Full";
     final public static String APPLICATION_VERSION =
             String.format("%d.%d (%s)",
             APPLICATION_VERSION_MAJOR, APPLICATION_VERSION_MINOR, APPLICATION_DISTRIBUTION);
     /** The full title of the game. */
     final public static String TITLE = APPLICATION_NAME + " " + APPLICATION_VERSION;
     /** The copyright. */
     final public static String COPYRIGHT = "\u00A9 2010 Couchware Inc.";
 
     //--------------------------------------------------------------------------
     // Members
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
     /** The tracker. */
     private Tracker tracker;
     /** The window that is being used to render the game. */
     private IWindow win;
 
     //--------------------------------------------------------------------------
     // Private Members
     //--------------------------------------------------------------------------
     
     /** The parent of the game (used in applet mode). */
     final private Canvas parent;
     /** The current build number. */
     final private String BUILD_NUMBER = "N/A";
     /** The current drawer. */
     private IDrawer drawer;
     /** The normal title of the window. */
     private String windowTitle = APPLICATION_NAME;
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
     /** A list that keeps track of the chains in moves. */
     List<Chain> chainList = new ArrayList<Chain>();
 
     /** The targets that may be transitioned to. */
     public enum TransitionTarget
     {
         NOTHING, GAME, MENU
     }
     /** The transition target. */
     private TransitionTarget transitionTo = TransitionTarget.NOTHING;
     /**
      * The transition variable.  This is the transition animation that is used
      * to transition from the menu to the game and vice-versa.
      */
     private ITransition transition;
     /**
      * The game difficulty setting.
      */
     private GameDifficulty difficulty = GameDifficulty.NORMAL;
 
    private static SoundSystem soundSystem;
 
     static
     {
         SoundSystemConfig.setSoundFilesPackage("");
         try
         {
             soundSystem = new SoundSystem(LibraryLWJGLOpenAL.class);
             SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);
             SoundSystemConfig.setCodec("wav", CodecWav.class);
             SoundSystemConfig.setCodec("ogg", CodecJOgg.class);
             
         }
         catch (SoundSystemException ex)
         {
             CouchLogger.get().recordException(Game.class, ex);
         }
     }
 
     public static SoundSystem getSoundSystem()
     {
         return soundSystem;
     }
 
     /**
      * Construct our game and set it running.
      *
      * @param renderingType
      *            The type of rendering to use (should be one of the contansts
      *            from ResourceFactory)
      */
     public Game(Canvas parent, ResourceFactory.Renderer renderer)
     {
         this.parent = parent;
 
         ResourceFactory.get().setRenderer(renderer);
         win = ResourceFactory.get().createWindow(parent);
         win.setResolution(SCREEN_WIDTH, SCREEN_HEIGHT);
         win.setGameWindowCallback(this);
         win.setTitle(windowTitle);
     }
 
     /**
      * Check if the game is running in the more restrictive applet mode.
      *
      * @return True if the game is running as an applet, false otherwise.
      */
     public static boolean isApplet()
     {
         return APPLET;
     }
 
     /**
      * Start the game.
      */
     public void start()
     {
         win.start();
     }
 
     /**
      * Send a message to the rendering window telling it to stop.  Keep in mind
      * that this won't instantly stop the rendering, but will cause it to stop
      * fairly quickly.
      */
     public void stop()
     {
         win.stop();
     }
 
     /**
      * Open a url in a new browser window.
      *
      * @param urlString
      */
     public void openURLinBrowser(String urlString)
     {
         if (APPLET)
         {
             Applet applet = (Applet) parent.getParent();
             AppletContext context = applet.getAppletContext();
 
             try
             {
                 context.showDocument(new URL(urlString), "_blank");
             } catch (MalformedURLException ex)
             {
                 CouchLogger.get().recordException(Game.class, ex);
             }
         } else
         {
             try
             {
                 BrowserLauncher launcher = new BrowserLauncher();
                 launcher.openURLinBrowser(urlString);
             } catch (Exception ex)
             {
                 CouchLogger.get().recordException(Game.class, ex);
             }
         }
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
                 win.clearMouseEvents();
 
                 // Shut off the music.
                 //hub.musicMan.stopAtGain(0.0);
                 hub.musicMan.stop();
 
                 resetGame(true);
 
                 // Create the main menu.
                 mainMenu = new MainMenu(win, hub);
                 mainMenu.setDisabled(true);
 
                 // Create the layer manager transition animation.
                 transitionTo = TransitionTarget.MENU;
                 this.transition = new CircularTransition.Builder(mainMenu).minRadius(10).speed(400).build();
                 setDrawer(transition);
 
                 this.transition.addAnimationListener(new AnimationAdapter()
                 {
                     @Override
                     public void animationFinished()
                     {
                         mainMenu.setDisabled(false);
                     }
                 });
 
                 // Queue in the animation manager.
                 hub.gameAnimationMan.add(transition);
 
                 break;
         }
     }
 
     /**
      * Initialize various members.
      */
     private void initializeCoreManagers()
     {
         // Create the UI.
         ui = UI.newInstance(win, this, hub);
         hub.listenerMan.registerListener(Listener.GAME, this.ui);
         hub.listenerMan.registerListener(Listener.LEVEL, this.ui);
         hub.listenerMan.registerListener(Listener.PIECE, this.ui);
         hub.listenerMan.registerListener(Listener.SCORE, this.ui);
         hub.listenerMan.registerListener(Listener.TIMER, this.ui);
 
         // Get the singleton.
         refactorer = new Refactorer(this);
 
         // Get the singleton.
         tileDropper = TileDropper.get();
 
         // Get the singleton.
         tileRemover = TileRemover.get();
 
         // Setup the tracker.
         tracker = Tracker.newInstance(hub.listenerMan);
         hub.listenerMan.registerListener(Listener.SCORE, tracker);
 
         // Make the tile remover listen for level events.
         hub.listenerMan.registerListener(Listener.LEVEL, this.tileRemover);
     }
 
     /**
      * Initialize the tutorials.
      */
     public void initializeTutorials(boolean isActivated)
     {
         List<ITutorial> tutorials = new ArrayList<ITutorial>();
         tutorials.add(new BasicTutorial(win, refactorer));
         tutorials.add(new RotateTutorial(win, refactorer));
         tutorials.add(new RocketTutorial(win, refactorer));
 
         if (!isApplet())
         {
             tutorials.add(new GravityTutorial(win, refactorer));
             tutorials.add(new BombTutorial(win, refactorer));
             tutorials.add(new StarTutorial(win, refactorer));
         }
 
         for (ITutorial t : tutorials)
         {
             if (isActivated || !t.hasRun(hub))
             {
                 hub.tutorialMan.add(t);
             }
         }
     }
 
     /**
      * Initialize the common elements for the game.
      */
     public void initialize()
     {
         // Make sure the listener and settings managers are ready.
         hub.initialize(win, this,
                 EnumSet.of(Manager.ANIMATION, Manager.LISTENER, Manager.SETTINGS));
 
         // Set the log level.
         CouchLogger.get().setLogLevel(hub.settingsMan.getString(Key.DEBUG_LOG_LEVEL));
 
         // Print the build number.        
         Class cls = this.getClass();
         CouchLogger.get().recordMessage(cls, "Date: " + (new Date()));
         CouchLogger.get().recordMessage(cls, "Wezzle Build: " + BUILD_NUMBER);
         CouchLogger.get().recordMessage(cls, "Wezzle Version: " + APPLICATION_VERSION);
         CouchLogger.get().recordMessage(cls, "Java Version: " + System.getProperty("java.version"));
         CouchLogger.get().recordMessage(cls, "OS Name: " + System.getProperty("os.name"));
         CouchLogger.get().recordMessage(cls, "OS Architecture: " + System.getProperty("os.arch"));
         CouchLogger.get().recordMessage(cls, "OS Version: " + System.getProperty("os.version"));
 
 
         // Create the loader.
         loader = new Loader(win, hub.settingsMan, "Loading Wezzle...");
         setDrawer(loader);
 
         // Preload the sprites.
         if (Game.isApplet())
         {
             loader.addTasks(resourceFactory.preloadSprites());
         }
 
         // Preload fonts.
         final int MIN_FONT = 10;
         final int MAX_FONT = 50;
         for (int i = MIN_FONT; i <= MAX_FONT; i++)
         {
             final int size = i;
             loader.addTask(new Runnable()
             {
                 public void run()
                 {
                     new ResourceFactory.LabelBuilder(0, 0).size(size).build();
                 }
             });
         }
 
         // Initialize managers.
         loader.addTask(new Runnable()
         {
             public void run()
             {
                 hub.initialize(win, Game.this, EnumSet.allOf(Manager.class));
                 hub.layerMan.setDisabled(true);
             }
         });
 
         // Initialize the core managers.
         loader.addTask(new Runnable()
         {
             public void run()
             {
                 initializeCoreManagers();
             }
         });
     }
 
     public void update()
     {
         // If the loader is running, bypass all the rendering to show it.        
         if (this.drawer == loader)
         {
             // Animate all animations.
             if (hub.gameAnimationMan != null)
             {
                 hub.gameAnimationMan.animate();
             }
 
             // Update the logic.
             loader.updateLogic(this, hub);
 
             if (loader.getState() == Loader.State.FINISHED)
             {
                 // Remove the loader.
                 loader = null;
 
                 // Empty the mouse events.
                 win.clearMouseEvents();
 
                 // Create the main menu.
                 mainMenu = new MainMenu(win, hub);
                 setDrawer(mainMenu);
             } else
             {
                 return;
             }
         }
 
         // If the main menu is running, bypass all rendering to show it.
         if (this.drawer == mainMenu)
         {
             // Animate all animations.
             if (hub.gameAnimationMan != null)
             {
                 hub.gameAnimationMan.animate();
             }
 
             // Update the main menu logic.
             mainMenu.updateLogic(this, hub);
 
             if (mainMenu.getState() == MainMenu.State.FINISHED)
             {
                 // Empty the mouse events.
                 win.clearMouseEvents();
 
                 // Remove the loader.
                 mainMenu.dispose();
                 mainMenu = null;
 
                 // Create the layer manager transition animation.
                 this.transitionTo = TransitionTarget.GAME;
                 this.transition = new CircularTransition.Builder(hub.layerMan).minRadius(10).speed(400).build();
                 setDrawer(transition);
 
                 this.transition.addAnimationListener(new AnimationAdapter()
                 {
                     @Override
                     public void animationFinished()
                     {
                         hub.layerMan.setDisabled(false);
                     }
                 });
 
                 // Queue in the animation manager.
                 hub.gameAnimationMan.add(transition);
 
                 // Start the music.
                 hub.musicMan.play();
 
                 // See if the music is off.
                 if (hub.settingsMan.getBool(Key.USER_MUSIC) == false)
                 {
                     hub.musicMan.setPaused(true);
                 }
             } else
             {
                 // Fire the mouse events.
                 win.fireMouseEvents();
                 win.updateKeyPresses();
 
                 // Draw using the loader.
                 return;
             }
         } // end if
 
         // See if the main menu transition is in progress
         if (this.drawer == transition)
         {
             // Animate all animations.
             if (hub.gameAnimationMan != null)
             {
                 hub.gameAnimationMan.animate();
             }
 
             // Otherwise see if the transition is over.
             if (!transition.isFinished())
             {
                 return;
             } else
             {
                 switch (transitionTo)
                 {
                     case GAME:
                         setDrawer(hub.layerMan);
                         break;
 
                     case MENU:
                         setDrawer(mainMenu);
                         break;
 
                     case NOTHING:
                         throw new IllegalStateException("Transition target was not set");
                 }
 
                 transitionTo = TransitionTarget.NOTHING;
                 transition = null;
             }
         } // end if
 
         // Update UI.
         ui.updateLogic(this, hub);
 
         // Check on board animation.
         if (boardAnimation != null && boardAnimation.isFinished())
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
                         //hub.pieceMan.showShadowPieceGrid();
                         hub.pieceMan.startAnimation(hub.timerMan);
                         break;
 
                     case OUT:
 
                         hub.boardMan.setVisible(false);
                         hub.pieceMan.hidePieceGrid();
                         hub.pieceMan.hideShadowPieceGrid();
                         //hub.pieceMan.hideShadowPieceGrid();
                         break;
 
                     default:
 
                         throw new IllegalStateException(
                                 "Unrecogonized fade animation type");
                 }
             } else
             {
                 throw new RuntimeException(
                         "Unrecognized board animation class");
             }
 
             // Clear the board animation.
             boardAnimation = null;
 
             // Clear mouse button presses.
             hub.pieceMan.clearMouseButtonSet();
 
             // If game over is in progress, make a new board and start.
             if (gameOverInProgress)
             {
                 // Show the game over screen.
                 ui.showGameOverGroup(hub.groupMan);
 
                 // Clear the flag.
                 gameOverInProgress = false;
             }
         } else
         {
             if (boardAnimation != null && !boardAnimation.isFinished())
             {
                 // Board is still dirty due to animation.
                 hub.boardMan.setDirty(true);
             }
         }
 
         // Update all the group logic.
         hub.groupMan.updateLogic(this, hub);
 
         // Uphdate the music manager logic.        
         hub.musicMan.updateLogic(this, hub);        
 
         // Check to see if we should be showing the board.
         if (activateBoardShowAnimation)
         {
             // Hide the piece.            
             hub.pieceMan.hidePieceGrid();
             hub.pieceMan.hideShadowPieceGrid();
             hub.pieceMan.stopAnimation();
 
             // Start board show animation.            
             boardAnimation = hub.boardMan.animateShow(boardAnimationType);
             hub.boardMan.setDirty(true);
 
             // Clear flag.
             clearBoardShowAnimation();
         }
 
         // Check to see if we should be hiding the board.
         if (activateBoardHideAnimation)
         {
             // Hide the piece.
             hub.pieceMan.hidePieceGrid();
             hub.pieceMan.hideShadowPieceGrid();
             hub.pieceMan.stopAnimation();
 
             // Start board hide animation.            
             boardAnimation = hub.boardMan.animateHide(boardAnimationType);
             hub.boardMan.setDirty(true);
 
             // Clear flag.
             clearBoardHideAnimation();
         }
 
         // If the pause button is not on, then we proceed with the
         // normal game loop.
         if (!hub.groupMan.isActivated())
         {
             updateBoard();
         }
 
         // Animate the UI animation manager.
         hub.uiAnimationMan.animate();
 
         // Fire all the queued mouse events.
         win.fireMouseEvents();
         win.updateKeyPresses();
 
         // The keys.      
         if (win.isKeyPressed('R') && !isApplet())
         {
             hub.settingsMan.loadExternalSettings();
             CouchLogger.get().recordMessage(this.getClass(), "Reloaded external settings");
         }
 
         // Check the achievements.
         //hub.achievementMan.evaluate(this, hub);
 
         if (!hub.tutorialMan.isTutorialRunning()
                 && hub.achievementMan.isNewAchievementCompleted())
         {
             // Report to log.
             hub.achievementMan.reportNewlyCompleted();
 
             List<Achievement> achievementList = hub.achievementMan.getNewlyCompletedAchievementList();
             for (Achievement ach : achievementList)
             {
                 AchievementNotification notif = new AchievementNotification.Builder(win, 0, 0, ach).alignment(EnumSet.of(Alignment.MIDDLE, Alignment.CENTER)).build();
 
                 hub.notificationMan.offer(notif);
             }
         }
 
         // Handle notifications.
         hub.notificationMan.updateLogic(this, hub);
 
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
         } else
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
 
                 hub.levelMan.incrementLevel();
 
                 hub.soundMan.play(Sound.LEVEL_UP);
 
                 ImmutablePosition pos = hub.pieceMan.getCursorPosition();
                 int x = pos.getX() + hub.boardMan.getCellWidth() / 2;
                 int y = pos.getY() + hub.boardMan.getCellHeight() / 2;
 
                 final ITextLabel label = new LabelBuilder(x, y).alignment(EnumSet.of(Alignment.MIDDLE, Alignment.LEFT)).color(hub.settingsMan.getColor(Key.GAME_COLOR_PRIMARY)).size(hub.settingsMan.getInt(Key.SCT_LEVELUP_TEXT_SIZE)).text(hub.settingsMan.getString(Key.SCT_LEVELUP_TEXT)).build();
 
                 IAnimation a1 = new FadeAnimation.Builder(FadeAnimation.Type.OUT, label).wait(hub.settingsMan.getInt(Key.SCT_SCORE_FADE_WAIT)).duration(hub.settingsMan.getInt(Key.SCT_SCORE_FADE_DURATION)).minOpacity(hub.settingsMan.getInt(Key.SCT_SCORE_FADE_MIN_OPACITY)).maxOpacity(hub.settingsMan.getInt(Key.SCT_SCORE_FADE_MAX_OPACITY)).build();
 
                 IAnimation a2 = new MoveAnimation.Builder(label).duration(hub.settingsMan.getInt(Key.SCT_LEVELUP_MOVE_DURATION)).speed(hub.settingsMan.getInt(Key.SCT_LEVELUP_MOVE_SPEED)).theta(hub.settingsMan.getInt(Key.SCT_LEVELUP_MOVE_THETA)).build();
 
                 a2.addAnimationListener(new AnimationAdapter()
                 {
                     @Override
                     public void animationStarted()
                     {
                         hub.layerMan.add(label, Layer.EFFECT);
                     }
 
                     @Override
                     public void animationFinished()
                     {
                         hub.layerMan.remove(label, Layer.EFFECT);
                     }
                 });
 
                 hub.gameAnimationMan.add(a1);
                 hub.gameAnimationMan.add(a2);
                 a1 = null;
                 a2 = null;
             }
         } // end if
 
         // See if it's game ovaries.
         if (activateGameOver)
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
         hub.gameAnimationMan.animate();
 
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
         return activateGameOver == true
                 || gameOverInProgress == true
                 || activateBoardShowAnimation == true
                 || activateBoardHideAnimation == true
                 || this.boardAnimation != null;
     }
 
     /**
      * Checks to see if the tiles are being manipulated in any way.
      *
      * @return
      */
     public boolean isTileManipulating()
     {
         return refactorer.isRefactoring()
                 || tileRemover.isTileRemoving()
                 || tileDropper.isTileDropping();
     }
 
     public boolean isCompletelyBusy()
     {
         return isContextManipulating() || isTileManipulating();
     }
 
     public boolean shouldHidePieceGrid()
     {
         return isCompletelyBusy() || hub.tutorialMan.isTutorialMenuShowing();
     }
 
     /**
      * Checks whether tiles are, or are about to be, removed.
      */
     public void startBoardShowAnimation(AnimationType type)
     {
         // Set the flag.
         if (activateBoardHideAnimation == true)
         {
             throw new IllegalStateException(
                     "Attempted to show board while it is being hidden");
         }
 
         if (activateBoardShowAnimation == true)
         {
             throw new IllegalStateException(
                     "Attempted to show board while it is already being shown");
         }
 
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
         if (activateBoardShowAnimation)
         {
             throw new IllegalStateException(
                     "Attempted to hide board while it is being shown");
         }
 
         if (activateBoardHideAnimation)
         {
             throw new IllegalStateException(
                     "Attempted to hide board while it is already being hidden");
         }
 
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
                 "",
                 hub.scoreMan.getTotalScore(),
                 hub.levelMan.getLevel(),
                 this.getDifficulty());
 
         // Notify of game over.
         hub.listenerMan.notifyGameOver(new GameEvent(this,
                 this.getDifficulty(),
                 hub.levelMan.getLevel(),
                 hub.scoreMan.getTotalScore()));
 
         // Activate the game over process.
         this.activateGameOver = true;
     }
 
     public void resetGame(boolean restartActivated)
     {
         // The level we reset to.
         int level = hub.levelMan.getLevel();
 
         // Reset the tracker.
         this.tracker.resetState();
 
         // Reset a bunch of stuff.
         if (restartActivated)
         {
             // Reset the board manager.
             hub.boardMan.resetState();
 
             // Reset the world manager.
             hub.levelMan.resetState();
             level = hub.levelMan.getLevel();
 
             // Reset the item manager.
             hub.itemMan.resetState();
             hub.itemMan.evaluateRules(this, hub);
         }
 
         // Notify all listeners of reset.
         hub.listenerMan.notifyGameReset(new GameEvent(this,
                 this.getDifficulty(),
                 level,
                 hub.scoreMan.getLevelScore()));
 
         // Reset the stat man.
         hub.statMan.resetState();
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
             if (hub.settingsMan != null && !isApplet())
             {
                 hub.settingsMan.saveSettings();
             }
             if (hub.musicMan != null)
             {
                 hub.musicMan.stopAll();
             }
             CouchLogger.get().recordMessage(this.getClass(), "Music manager stopped");
             if (hub.soundMan != null)
             {
                 hub.soundMan.stopAll();
             }
             CouchLogger.get().recordMessage(this.getClass(), "Sound manager stopped");
 
             soundSystem.cleanup();
             
         } catch (Exception e)
         {
             CouchLogger.get().recordException(this.getClass(), e, true /* Fatal */);
         }
     }
 
     /**
      * Notification that the game window has been deactivated in some way.
      */
     public void windowDeactivated()
     {
         // Don't auto-pause game if:
         //   a) The game's layer manager is not the drawer.
         //   b) Any sort of screen other than the game is showing (i.e. game over).
         //   c) A tutorial is playing (as they don't have a time limit).
         boolean autoPause = hub.settingsMan.getBool(Key.USER_AUTO_PAUSE);
         if (autoPause && this.drawer == hub.layerMan
                 && (hub.groupMan != null && !hub.groupMan.isActivated())
                 && (hub.tutorialMan != null && !hub.tutorialMan.isTutorialRunning()))
         {
             ui.showPauseGroup(hub.groupMan);
         }
     }
 
     /**
      * Notification that the game window has been reactivated in some way.
      */
     public void windowActivated()
     {
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
 
     public GameDifficulty getDifficulty()
     {
         return this.difficulty;
     }
 
     public void setDifficulty(GameDifficulty difficulty)
     {
         this.difficulty = difficulty;
     }
 
     public Tracker getTracker()
     {
         return tracker;
     }
     /**
      * The secret part of the registration code.  Shhh.  Don't tell anyone.
      */
     final static String SECRET_CODE = "minsquibbion";
 
     /**
      * Validates the license information for Wezzle.
      * @return True if the license is validated, false otherwise.
      */
     public static boolean validateLicenseInformation(String serialNumber, String licenseKey)
     {
         if (isApplet())
         {
             return true;
         }
 
         if (null == serialNumber || null == licenseKey)
         {
             return false;
         }
 
         final String plainText = serialNumber + SECRET_CODE;
 
         byte[] defaultBytes = plainText.getBytes();
         try
         {
             MessageDigest algorithm = MessageDigest.getInstance("MD5");
             algorithm.reset();
             algorithm.update(defaultBytes);
             byte messageDigest[] = algorithm.digest();
 
             StringBuffer hexBuffer = new StringBuffer();
             for (int i = 0; i < messageDigest.length; i++)
             {
                 int hex = 0xFF & messageDigest[i];
                 hexBuffer.append(String.format("%02x", hex));
             }
 
             if (licenseKey.toLowerCase().equals(hexBuffer.toString().toLowerCase()))
             {
                 return true;
             }
         } catch (Exception e)
         {
             CouchLogger.get().recordException(Game.class, e, true /* Fatal */);
         }
 
         return false;
     }
 }
