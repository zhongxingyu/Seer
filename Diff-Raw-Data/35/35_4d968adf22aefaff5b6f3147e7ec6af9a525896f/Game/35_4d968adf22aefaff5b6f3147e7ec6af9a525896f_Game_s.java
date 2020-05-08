 package net.cscott.sdr.anim;
 
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.Callable;
 import java.util.concurrent.CyclicBarrier;
 import java.util.logging.Level;
 
 import net.cscott.sdr.App;
 import net.cscott.sdr.BeatTimer;
 import net.cscott.sdr.calls.CallDB;
 import net.cscott.sdr.calls.FormationList;
 import net.cscott.sdr.recog.LevelMonitor;
 
 import com.jme.app.FixedFramerateGame;
 import com.jme.image.Image;
 import com.jme.input.KeyBindingManager;
 import com.jme.input.KeyInput;
 import com.jme.input.MouseInput;
 import com.jme.input.joystick.JoystickInput;
 import com.jme.renderer.ColorRGBA;
 import com.jme.system.DisplaySystem;
 import com.jme.system.JmeException;
 import com.jme.util.GameTaskQueue;
 import com.jme.util.GameTaskQueueManager;
 import com.jme.util.LoggingSystem;
 import com.jme.util.TextureManager;
 import com.jme.util.Timer;
 import com.jmex.game.state.GameState;
 import com.jmex.game.state.GameStateManager;
 import com.jmex.game.state.load.TransitionGameState;
 
 public class Game extends FixedFramerateGame {
     /** Get a high resolution timer for FPS updates. */
     private final Timer timer = Timer.getTimer();
     /** Timer sync'ed to music. */
     private BeatTimer beatTimer;
     MenuState menuState;
     VenueState venueState;
     HUDState hudState;
     MusicState musicState;
     private final BlockingQueue<LevelMonitor> rendezvousLM;
     private final BlockingQueue<BeatTimer> rendezvousBT;
     private final CyclicBarrier musicSync, sphinxSync;
 
     /** You must provide the game with {@link BlockingQueue}s from which we can
      * get a {@link BeatTimer} (presumably from the music player thread) and a
      * {@link LevelMonitor} (presumably from the speech-recognition thread).
      */ 
     public Game(BlockingQueue<BeatTimer> rendezvousBT,
             BlockingQueue<LevelMonitor> rendezvousLM,
             CyclicBarrier musicSync,
             CyclicBarrier sphinxSync) {
         LoggingSystem.getLogger().setLevel(java.util.logging.Level.WARNING);
         URL url = SdrGame.class.getClassLoader().getResource      
             ("net/cscott/sdr/anim/splash.png");
         this.setDialogBehaviour
             (FIRSTRUN_OR_NOCONFIGFILE_SHOW_PROPS_DIALOG, url);
         this.rendezvousBT = rendezvousBT;
         this.rendezvousLM = rendezvousLM;
         this.musicSync = musicSync;
         this.sphinxSync = sphinxSync;
     }
     /** Creates display, sets up camera, and binds keys. */
     protected void initSystem() {
         LoggingSystem.getLogger().log( Level.INFO, getVersion());
         try {
             /** Get a DisplaySystem according to startup prefs. */
             display = DisplaySystem.getDisplaySystem(properties.getRenderer());
             LoggingSystem.getLogger().log
                 ( Level.INFO, "Running on: "+display.getAdapter()+"\n"+
                   "Driver version: "+display.getDriverVersion());
             /** Create a window with the startup box's information. */
             display.setTitle("SDR");
             display.createWindow(properties.getWidth(), properties.getHeight(),
                                  properties.getDepth(), properties.getFreq(),
                                  properties.getFullscreen() );
         } catch ( JmeException e ) {
             /**
              * If the displaysystem can't be initialized correctly, exit
              * instantly.
              */
             e.printStackTrace();
             System.exit( 1 );
         }
         /** Set a green background (nicer during initial load) */
         display.getRenderer().setBackgroundColor( new ColorRGBA(.7f,1,.7f,1) );
         // global "screen shot" command
         KeyBindingManager.getKeyBindingManager().set( "screen_shot",
                 KeyInput.KEY_SYSRQ /* The "Print Screen" button */);
         KeyBindingManager.getKeyBindingManager().set( "screen_shot",
                 KeyInput.KEY_SCROLL /* GNOME already has PrtSc bound */);
         // global "quit now" command.
         KeyBindingManager.getKeyBindingManager().set( "exit",
                 KeyInput.KEY_ESCAPE );
 
         /** Close the splash screen, if not already closed. */
         try {
            Class c = Class.forName("java.awt.SplashScreen");
             Method m = c.getMethod("getSplashScreen");
             Object o = m.invoke(null); // SplashScreen instance.
             if (o!=null) {
                 m = c.getMethod("close");
                 m.invoke(o);  // SplashScreen.close().
             }
         } catch (Exception ce) {
             /* ignore: must be running on pre-1.6 jdk */
         }
     }
  
 
     @Override
     protected void initGame() {
         setFrameRate(20); // limit frame rate: save our CPU for speech
         display.setTitle(net.cscott.sdr.Version.PACKAGE_STRING);
 
         // Creates the GameStateManager. Only needs to be called once.
         GameStateManager.create();
         // Adds a new GameState to the GameStateManager. In order for it to get
         // processed (rendered and updated) it needs to get activated.
 
         URL url = SdrGame.class.getClassLoader().getResource      
         ("net/cscott/sdr/anim/loading.png");
         final TransitionGameState loading = new TransitionGameState(10, url);
         loading.setActive(true);
 	loading.setProgress(0,"Loading..."); // center the progress bar.
         GameStateManager.getInstance().attachChild(loading);
 
         final GameTaskQueueManager queueMan =GameTaskQueueManager.getManager();
         new Thread() {
             public void run() {
                 inc("Loading icons...");
                 final Image icon16 = TextureManager.loadImage(
                         SdrGame.class.getClassLoader().getResource(
                                 "net/cscott/sdr/icon16.png"), false);
                 final Image icon32 = TextureManager.loadImage(
                         SdrGame.class.getClassLoader().getResource(
                                 "net/cscott/sdr/icon32.png"), false);
                 final Image icon128= TextureManager.loadImage(
                         SdrGame.class.getClassLoader().getResource(
                                 "net/cscott/sdr/icon128.png"), false);
                 queueMan.update(new Callable<Void>() {
                     public Void call() throws Exception {
                         display.setIcon(new Image[]{ icon128, icon32, icon16});
                         return null;
                     }
                 });
  
                 inc("Loading formations...");
                 // load formations
                 try { Class.forName(FormationList.class.getName());
                 } catch (ClassNotFoundException e) { /* ignore */ }
 
                 inc("Loading call list...");
                 // load call database
                 try { Class.forName(CallDB.class.getName());
                 } catch (ClassNotFoundException e) { /* ignore */ }
 
                 inc("Waiting for music startup...");
                 try {
                     musicSync.await(); // release music thread
                 } catch (Exception e) { assert false : e; } // broken barrier!
                 BeatTimer beatTimer = getBeatTimer();// wait for our beat timer
 
                 inc("Loading venue...");
                 venueState = new VenueState(beatTimer);
                 attach(venueState,true);
                 if (App.DEBUG) attach(new DebugState(),true);
 
                 inc("Waiting for Sphinx...");
                 try {
                     sphinxSync.await(); // release sphinx thread
                 } catch (Exception e) { assert false : e; } // broken barrier!
                 LevelMonitor lm;
                 while(true)
                     try {
                         lm = rendezvousLM.take(); // wait for our LevelMonitor
                         break;
                     } catch (InterruptedException e) { /* try again */ }
                 
                 inc("Loading scrolling notes...");
                 musicState = new MusicState(beatTimer, lm);
                 
                 inc("Creating HUD...");
                 hudState = new HUDState(beatTimer);
                 attach(hudState,false);
                 
                 inc("Loading menus...");
                 menuState = new MenuState(Game.this);
                 queueMan.update(new Callable<Void>() {
                     public Void call() throws Exception {
                         GameStateManager m = GameStateManager.getInstance();
                         musicState.setActive(true);
                         hudState.setActive(false);
                         menuState.setActive(true);
                         m.attachChild(musicState);
                         m.attachChild(hudState);
                         m.attachChild(menuState);
                         m.detachChild(loading);
                         m.attachChild(loading);// reorder to keep on top.
                         return null;
                     }
                 });
 
                 // from now on, make sure that all update tasks are completed
                 // to avoid glitches updating textured quads.
                 GameTaskQueueManager.getManager().getQueue(GameTaskQueue.UPDATE)
                 .setExecuteAll(true);
 
                 inc("Loading complete.");
             }
             private void attach(final GameState state,final boolean active){
                 queueMan.update(new Callable<Void>() {
                     public Void call() throws Exception {
                         state.setActive(active);
                         GameStateManager.getInstance().attachChild(state);
                         return null;
                     }
                 });
             }
             private void inc(final String msg) {
                 queueMan.update(new Callable<Void>() {
                     public Void call() throws Exception {
                         loading.increment(msg); return null;
                     }
                 });
             }
         }.start();
     }
     private float tpf=0;
     @Override
     protected final void update(float _unused) {
         timer.update();
         tpf = timer.getTimePerFrame();
         /* Execute anything on the update queue. */
         GameTaskQueueManager.getManager().getQueue(GameTaskQueue.UPDATE)
             .execute();
         // Update the current game state.
         GameStateManager.getInstance().update(tpf);
     }
     @Override
     protected final void render(float _unused) {
         // Clears the previously rendered information.
         display.getRenderer().clearBuffers();
         /* Execute anything on the render queue. */
         GameTaskQueueManager.getManager().getQueue(GameTaskQueue.RENDER)
         .execute();
         // Render the current game state.
         GameStateManager.getInstance().render(tpf);
         // global printscreen
         if (KeyBindingManager.getKeyBindingManager()
                 .isValidCommand( "screen_shot", false ) ) {
 	    // XXX: we don't get all the game states in the screen shot =(
             display.getRenderer().takeScreenShot("SDRScreenShot");
         }
         // global quit.
         if (KeyBindingManager.getKeyBindingManager()
                 .isValidCommand( "exit", false ) ) {
                 finish();
             }
     }
     
     public BeatTimer getBeatTimer() {
         if (beatTimer==null)
             synchronized(this) {
                 if (beatTimer==null)
                     while (true)
                         try {
                             beatTimer = rendezvousBT.take();
                             break;
                         } catch (InterruptedException e) { /* try again */ }
             }
         assert beatTimer != null;
         return beatTimer;
     }
     
     
     @Override
     protected void cleanup() {
         LoggingSystem.getLogger().log(Level.INFO, "Cleaning up resources.");
         
         // Performs cleanup on all loaded game states.
         GameStateManager.getInstance().cleanup();
 
         TextureManager.doTextureCleanup();
         KeyInput.destroyIfInitalized();
         MouseInput.destroyIfInitalized();
         JoystickInput.destroyIfInitalized();
     }
     /** Calls the quit of BaseGame to clean up the display and then
      * closes the JVM. */
     @Override
     protected void quit() {
         super.quit();
         System.exit( 0 );
     }
 
     @Override
     protected void reinit() {
         // do nothing
     }
 }
