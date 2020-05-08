 package net.cscott.sdr.anim;
 
 import java.lang.reflect.Method;
 import java.util.logging.Level;
 
 import com.jme.app.FixedFramerateGame;
 import com.jme.image.Texture;
 import com.jme.input.InputHandler;
 import com.jme.input.KeyBindingManager;
 import com.jme.input.KeyInput;
 import com.jme.input.MouseInput;
 import com.jme.input.joystick.JoystickInput;
 import com.jme.light.PointLight;
 import com.jme.math.Vector3f;
 import com.jme.renderer.Camera;
 import com.jme.renderer.ColorRGBA;
 import com.jme.renderer.Renderer;
 import com.jme.scene.Node;
 import com.jme.scene.state.LightState;
 import com.jme.scene.state.WireframeState;
 import com.jme.scene.state.ZBufferState;
 import com.jme.system.DisplaySystem;
 import com.jme.system.JmeException;
 import com.jme.util.GameTaskQueue;
 import com.jme.util.GameTaskQueueManager;
 import com.jme.util.LoggingSystem;
 import com.jme.util.TextureManager;
 import com.jme.util.Timer;
 import com.jme.util.geom.Debugger;
 
 /** <code>SdrBaseGame</code> is a fixed-framerate base game class.  It sets up
  * the <code>BaseGame</code> in a manner similar to <code>SimpleGame</code>,
  * but defers the really application-specific stuff to subclasses.
  * @author C. Scott Ananian
  * @version $Id: SdrBaseGame.java,v 1.4 2006-10-25 17:46:46 cananian Exp $
  */
 public abstract class SdrBaseGame extends FixedFramerateGame {
     /** Our camera. */
     protected Camera cam;
     /** Root node of our scene graph. */
     protected Node rootNode;
     /** Handles mouse/keyboard. */
     protected InputHandler input;
     /** High resolution timer for tempo-independent animation. */
     protected Timer timer;
     /** True if the renderer should display the depth buffer. */
     protected boolean showDepth = false;
     /** True if the renderer should display bounds. */
     protected boolean showBounds = false;
     /** True if the rnederer should display normals. */
     protected boolean showNormals = false;
     /** A wirestate to turn on and off for the rootNode. */
     protected WireframeState wireState;
     /** A lightstate to turn on and off for the rootNode. */
     protected LightState lightState;
       
     /** Updates the timer, input, and update queue.  Checks 'quit' key.
      * @param interpolation unused
      */
     protected final void update( float interpolation /*unused*/ ) {
         /* Update/calculate framerate. */
         timer.update();
         float tpf = timer.getTimePerFrame();
         /* Check for key/mouse updates. */
         input.update( tpf );
         /* Execute anything on the update queue. */
         GameTaskQueueManager.getManager().getQueue(GameTaskQueue.UPDATE)
             .execute();
         /* --- Here we could deal with some standard input commands --- */
         if (KeyBindingManager.getKeyBindingManager()
             .isValidCommand("toggle_wire", false)) {
             wireState.setEnabled(!wireState.isEnabled());
             rootNode.updateRenderState();
         }
         if (KeyBindingManager.getKeyBindingManager()
             .isValidCommand("toggle_lights", false)) {
             lightState.setEnabled(!lightState.isEnabled());
             rootNode.updateRenderState();
         }
         if (KeyBindingManager.getKeyBindingManager()
             .isValidCommand("toggle_bounds", false)) {
             showBounds = !showBounds;
         }
         if (KeyBindingManager.getKeyBindingManager()
             .isValidCommand("toggle_depth", false )) {
             showDepth = !showDepth;
         }
         if (KeyBindingManager.getKeyBindingManager()
             .isValidCommand("toggle_normals", false )) {
             showNormals = !showNormals;
         }
         if (KeyBindingManager.getKeyBindingManager()
             .isValidCommand( "exit", false ) ) {
             finish();
         }
         /* Call subclass update. */
         sdrUpdate();
         /* Update controllers/render states/transforms/bounds for rootNode. */
         rootNode.updateGeometricState(tpf, true);
     }
     /** Defined in subclasses for custom updating. */
     protected void sdrUpdate() { }
 
     /** This is called for every frame in BaseGame.start(), after update().
      * @param interpolation unused in this implementation.
      */
     protected final void render( float interpolation ) {
         Renderer r = display.getRenderer();
         /* Clears tracking info for # tri/vertices (do we care?) */
         r.clearStatistics();
         /* Clears the previously rendered frame. */
         r.clearBuffers();
         /* Execute anything on the render queue. */
         GameTaskQueueManager.getManager().getQueue(GameTaskQueue.RENDER)
             .execute();
         /* Draw the rootnode & all its children. */
         r.draw(rootNode);
         /* Call derived sdrRender() in children. */
         sdrRender();
         /* Show debugging information, if we feel like it. */
         doDebug(r);
     }
     /** Defined in subclasses for custom rendering. */
     protected void sdrRender() { }
 
     protected void doDebug(Renderer r) {
         if (showBounds)
             Debugger.drawBounds( rootNode, r, true );
         if (showNormals)
             Debugger.drawNormals( rootNode, r );
         if (showDepth) {
             r.renderQueue();
             Debugger.drawBuffer(Texture.RTT_SOURCE_DEPTH,
                                 Debugger.NORTHEAST, r);
         }
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
             /** Create an appropriate camera */
             cam = display.getRenderer().createCamera
                 ( display.getWidth(), display.getHeight() );
         } catch ( JmeException e ) {
             /**
              * If the displaysystem can't be initialized correctly, exit
              * instantly.
              */
             e.printStackTrace();
             System.exit( 1 );
         }
         /** Set a black background. */
         display.getRenderer().setBackgroundColor( ColorRGBA.black );
 
         /** Set up how our camera sees. */
         cam.setFrustumPerspective( 45.0f, (float) display.getWidth()
                                    / (float) display.getHeight(), 1, 1000 );
         cam.setParallelProjection( false );
         Vector3f loc = new Vector3f( 0.0f, 0.0f, 25.0f );
         Vector3f left = Vector3f.UNIT_X.negate();
         Vector3f up = new Vector3f(Vector3f.UNIT_Y);
         Vector3f dir = Vector3f.UNIT_Z.negate();
         
         /** Move our camera to a correct place and orientation. */
         cam.setFrame( loc, left, up, dir );
         /** Signal that we've changed our camera's location/frustum. */
         cam.update();
         /** Assign the camera to this renderer. */
         display.getRenderer().setCamera( cam );
 
         /** Create a basic input controller. */
         SdrFirstPersonHandler firstPersonHandler = new SdrFirstPersonHandler
             ( cam, 50, 1 ) {
             @Override
             public void onAction() { onCamera(); }
         };
         input = firstPersonHandler;
 
         /** Get a high resolution timer for FPS updates. */
         timer = Timer.getTimer();
 
         /**
          * Signal to the renderer that it should keep track of rendering
          * information.
          */
         display.getRenderer().enableStatistics( true );
 
         /* for debugging */
         /** Assign key T to action "toggle_wire". */
         KeyBindingManager.getKeyBindingManager().set( "toggle_wire",
                                                       KeyInput.KEY_T );
         /** Assign key L to action "toggle_lights". */
         KeyBindingManager.getKeyBindingManager().set( "toggle_lights",
                                                       KeyInput.KEY_L );
         /** Assign key B to action "toggle_bounds". */
         KeyBindingManager.getKeyBindingManager().set( "toggle_bounds",
                                                       KeyInput.KEY_B );
         /** Assign key N to action "toggle_normals". */
         KeyBindingManager.getKeyBindingManager().set( "toggle_normals",
                                                       KeyInput.KEY_N );
         KeyBindingManager.getKeyBindingManager().set( "exit",
                                                       KeyInput.KEY_ESCAPE );
         KeyBindingManager.getKeyBindingManager().set( "toggle_depth",
                                                       KeyInput.KEY_F3 );
 
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
     /** This method is called whenever the camera is moved. */
     public void onCamera() { }
 
     /**
      * Creates rootNode, lighting, and other basic render
      * states. Called in BaseGame.start() after initSystem().
      */
     protected void initGame() {
         /** Create rootNode */
         rootNode = new Node( "rootNode" );
 
         /** Create a wirestate to toggle on and off. Starts disabled
          * with default width of 1 pixel. */
         wireState = display.getRenderer().createWireframeState();
         wireState.setEnabled( false );
         rootNode.setRenderState( wireState );
 
         /** Create a ZBuffer to display pixels closest to the camera
          * above farther ones. */
         ZBufferState buf = display.getRenderer().createZBufferState();
         buf.setEnabled( true );
         buf.setFunction( ZBufferState.CF_LEQUAL );
         rootNode.setRenderState( buf );
 
         // ---- LIGHTS
         /** Set up a basic, default light. */
         PointLight light = new PointLight();
         light.setDiffuse( new ColorRGBA( 0.75f, 0.75f, 0.75f, 0.75f ) );
         light.setAmbient( new ColorRGBA( 0.5f, 0.5f, 0.5f, 1.0f ) );
         light.setLocation( new Vector3f( 100, 100, 100 ) );
         light.setEnabled( true );
 
         /** Attach the light to a lightState and the lightState to rootNode. */
         lightState = display.getRenderer().createLightState();
         lightState.setEnabled( true );
         lightState.attach( light );
         rootNode.setRenderState( lightState );
 
         /** Let derived classes initialize. */
         sdrInitGame();
 
         timer.reset();
 
         /** Update geometric and rendering information for the rootNode. */
         rootNode.updateGeometricState( 0.0f, true );
         rootNode.updateRenderState();
     }
 
     /**
      * Called near end of initGame(). Must be defined by derived classes.
      */
     protected abstract void sdrInitGame();
 
     /** Unused. */
     protected void reinit() {
         //do nothing
     }
 
     /** Cleans up the keyboard. */
     protected void cleanup() {
         LoggingSystem.getLogger().log( Level.INFO, "Cleaning up resources." );
 
         TextureManager.doTextureCleanup();
         KeyInput.destroyIfInitalized();
         MouseInput.destroyIfInitalized();
         JoystickInput.destroyIfInitalized();
     }
 
     /** Calls the quit of BaseGame to clean up the display and then
      * closes the JVM. */
     protected void quit() {
         super.quit();
         System.exit( 0 );
     }
 }
