 package net.cscott.sdr.anim;
 
 import net.cscott.sdr.Version;
 
 import com.jme.image.Texture;
 import com.jme.input.AbsoluteMouse;
 import com.jme.input.InputHandler;
 import com.jme.input.Mouse;
 import com.jme.math.Vector3f;
 import com.jme.renderer.Renderer;
 import com.jme.scene.Node;
 import com.jme.scene.Text;
 import com.jme.scene.state.AlphaState;
 import com.jme.scene.state.LightState;
 import com.jme.scene.state.TextureState;
 import com.jme.system.DisplaySystem;
 import com.jme.util.TextureManager;
 import com.jmex.game.state.*;
 
/** The {@MenuState} displays a cursor on the screen and an appropriate
  *  menu of options.  It uses ORTHO mode and does not reset the camera,
  *  so some other camera-controlling state should also be active for
  *  background visuals.
  * @author C. Scott Ananian
 * @version $Id: MenuState.java,v 1.1 2006-11-06 03:20:25 cananian Exp $
  */
 public class MenuState extends StandardGameStateDefaultCamera {
 
     public MenuState() {
         super(Version.PACKAGE_NAME+" Menu");
 
         display = DisplaySystem.getDisplaySystem();
         initInput();
         initCursor();
         initText();
 
         rootNode.setLightCombineMode(LightState.OFF);
         rootNode.setRenderQueueMode(Renderer.QUEUE_ORTHO);
         rootNode.updateRenderState();
         rootNode.updateGeometricState(0, true);
     }
         
     /** The cursor node which holds the mouse gotten from input. */
     private Node cursor;
         
     /** Our display system. */
     private DisplaySystem display;
 
     private Text text;
     
     private InputHandler input;
     private Mouse mouse;
 
     /**
      * @see com.jmex.game.state.StandardGameState#onActivate()
      */
     public void onActivate() {
         display.setTitle(Version.PACKAGE_STRING+" Main Menu");
     }
         
     /**
      * Inits the input handler we will use for navigation of the menu.
      */
     protected void initInput() {
         input = new MenuHandler( this );
 
         DisplaySystem display = DisplaySystem.getDisplaySystem();
         mouse = new AbsoluteMouse("Mouse Input", display.getWidth(),
                 display.getHeight());
         mouse.registerWithInputHandler( input );
     }
         
     /**
      * Creates a pretty cursor.
      */
     private void initCursor() {             
         Texture texture =
             TextureManager.loadTexture(
                     MenuState.class.getClassLoader().getResource(
                     "net/cscott/sdr/anim/cursor1.png"),
                     Texture.MM_LINEAR_LINEAR,
                     Texture.FM_LINEAR);
         
         TextureState ts = display.getRenderer().createTextureState();
         ts.setEnabled(true);
         ts.setTexture(texture);
         
         AlphaState alpha = display.getRenderer().createAlphaState();
         alpha.setBlendEnabled(true);
         alpha.setSrcFunction(AlphaState.SB_SRC_ALPHA);
         alpha.setDstFunction(AlphaState.DB_ONE);
         alpha.setTestEnabled(true);
         alpha.setTestFunction(AlphaState.TF_GREATER);
         alpha.setEnabled(true);
         
         mouse.setRenderState(ts);
         mouse.setRenderState(alpha);
         mouse.setLocalScale(new Vector3f(1, 1, 1));
         
         cursor = new Node("Cursor");
         cursor.attachChild( mouse );
         
         rootNode.attachChild(cursor);
     }
     
     /**
      * Inits the button placed at the center of the screen.
      */
     private void initText() {
         text = Text.createDefaultTextLabel( "info" );
         text.print( "press enter" );
         text.getLocalTranslation().set( 100, 100, 0 );
         
         rootNode.attachChild( text );
     }
     
     /**
      * Updates input and button.
      * 
      * @param tpf The time since last frame.
      * @see GameState#update(float)
      */
     protected void stateUpdate(float tpf) {
         input.update(tpf);
         // Check if the button has been pressed.
         rootNode.updateGeometricState(tpf, true);
     }
     
     
 }
