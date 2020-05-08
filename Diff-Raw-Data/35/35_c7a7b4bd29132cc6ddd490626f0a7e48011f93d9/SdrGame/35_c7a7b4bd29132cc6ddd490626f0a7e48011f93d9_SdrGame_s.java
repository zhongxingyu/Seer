 package net.cscott.sdr.anim;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.cscott.sdr.calls.Dancer;
 import net.cscott.sdr.calls.ExactRotation;
 import net.cscott.sdr.calls.Formation;
 import net.cscott.sdr.calls.FormationMapper;
 import net.cscott.sdr.calls.Position;
 import net.cscott.sdr.calls.StandardDancer;
 import net.cscott.sdr.util.Fraction;
 
 import com.jme.bounding.BoundingBox;
 import com.jme.image.Texture;
 import com.jme.input.KeyBindingManager;
 import com.jme.input.KeyInput;
 import com.jme.math.FastMath;
 import com.jme.math.Matrix3f;
 import com.jme.math.Vector3f;
 import com.jme.renderer.ColorRGBA;
 import com.jme.scene.Skybox;
 import com.jme.scene.shape.Quad;
 import com.jme.scene.state.MaterialState;
 import com.jme.scene.state.TextureState;
 import com.jme.util.LoggingSystem;
 import com.jme.util.TextureManager;
 
 public class SdrGame extends SdrBaseGame {
 
     private Skybox skybox;
     private List<AnimDancer> dancers = new ArrayList<AnimDancer>(8);
     private final static Vector3f camCaller = new Vector3f(0, -8.5f, 9.4f);
     private final static Vector3f camStartup = new Vector3f(8,20,50);
     private final static Vector3f camCeiling = new Vector3f(0, -.1f, 11);
     private Vector3f camTarget = camCaller;
    private Vector3f oldCamDirection=new Vector3f();
     private Vector3f oldCamLocation=new Vector3f();
     private float oldCamTime = 0;
     private static final float SLEW_TIME = 2.0f; /* seconds */
 
     private float initialTime = 0;
     
     public void onCamera() {
        oldCamDirection.set(cam.getDirection());
         oldCamLocation.set(cam.getLocation());
         oldCamTime = timer.getTimeInSeconds();
     }
 
     public SdrGame() {
         LoggingSystem.getLogger().setLevel(java.util.logging.Level.OFF);
         URL url = SdrGame.class.getClassLoader().getResource      
             ("net/cscott/sdr/anim/splash.png");
         this.setDialogBehaviour
 	    (FIRSTRUN_OR_NOCONFIGFILE_SHOW_PROPS_DIALOG, url);
     }
   
     protected void sdrUpdate() {
         // check for camera movement commands.
         if (KeyBindingManager.getKeyBindingManager().isValidCommand
             ("camCaller",true)) {
             camTarget = camCaller;
             onCamera();
         }
         if (KeyBindingManager.getKeyBindingManager().isValidCommand
             ("camCeiling",true)) {
             camTarget = camCeiling;
             onCamera();
         }
     
         // move cam towards target /////////////
 
         // allow more time for the move if the target is far away.
         float dist = oldCamLocation.distance(camTarget);
         float slew = SLEW_TIME*Math.min(4,Math.max(Math.abs(dist/5), 1));
         // move towards desired cam location
         float interp = Math.min((timer.getTimeInSeconds()-oldCamTime)/slew, 1);
         // now apply acceleration factor.
         interp = (interp<.5f)?(2*interp*interp):(interp*(4-2*interp)-1);
         
        Vector3f location = new Vector3f(oldCamLocation);
        location.interpolate(camTarget,interp);
        cam.setLocation(location);
         // keep skybox with cam.
        skybox.setLocalTranslation(location);
             
         // target direction is from current location towards 0,0,0
         // with upvector 0,0,1 (maybe should slew from current location?)
        Vector3f targetDirection = Vector3f.ZERO.subtract(location).normalizeLocal();
        Vector3f direction = new Vector3f(cam.getDirection());
        direction.interpolate(targetDirection, interp);

        Vector3f worldUp = Vector3f.UNIT_Z; // world up vector
        Vector3f left = new Vector3f(worldUp).crossLocal(direction)
	    .normalizeLocal();
        Vector3f up = new Vector3f(direction).crossLocal(left)
	    .normalizeLocal();
        cam.setAxes(left, up, direction);
         
         // update dancer locations.
         Fraction beats = Fraction.valueOf((timer.getTimeInSeconds()-initialTime)*2);
         for (AnimDancer ad : dancers)
             ad.update(beats);
     }
 
     /**
      * builds the trimesh.
      * @see SdrBaseGame#initGame()
      */
     protected void sdrInitGame() {
         setFrameRate(20); // limit frame rate: save our CPU for speech
         display.setTitle(net.cscott.sdr.Version.PACKAGE_STRING);
         cam.setLocation(camStartup);
         cam.lookAt(new Vector3f(0,0,0), new Vector3f(0,0,1));
         onCamera(); // initialize camera tracking.
 
 	// pretty sky box
         skybox = new Skybox("skybox", 10, 10, 10);
  
         Texture north = TextureManager.loadTexture(
             SdrGame.class.getClassLoader().getResource(
             "net/cscott/sdr/anim/north.jpg"),
             Texture.MM_LINEAR,
             Texture.FM_LINEAR);
         Texture south = TextureManager.loadTexture(
             SdrGame.class.getClassLoader().getResource(
             "net/cscott/sdr/anim/south.jpg"),
             Texture.MM_LINEAR,
             Texture.FM_LINEAR);
         Texture east = TextureManager.loadTexture(
             SdrGame.class.getClassLoader().getResource(
             "net/cscott/sdr/anim/east.jpg"),
             Texture.MM_LINEAR,
             Texture.FM_LINEAR);
         Texture west = TextureManager.loadTexture(
             SdrGame.class.getClassLoader().getResource(
             "net/cscott/sdr/anim/west.jpg"),
             Texture.MM_LINEAR,
             Texture.FM_LINEAR);
         Texture up = TextureManager.loadTexture(
             SdrGame.class.getClassLoader().getResource(
             "net/cscott/sdr/anim/top.jpg"),
             Texture.MM_LINEAR,
             Texture.FM_LINEAR);
         Texture down = TextureManager.loadTexture(
             SdrGame.class.getClassLoader().getResource(
             "net/cscott/sdr/anim/bottom.jpg"),
             Texture.MM_LINEAR,
             Texture.FM_LINEAR);
  
         skybox.setTexture(Skybox.NORTH, north); //up
         skybox.setTexture(Skybox.WEST, west); //east
         skybox.setTexture(Skybox.SOUTH, south);//down
         skybox.setTexture(Skybox.EAST, east);//west
         skybox.setTexture(Skybox.UP, up);//north
         skybox.setTexture(Skybox.DOWN, down);
         skybox.preloadTextures();
 	Matrix3f skyRot = new Matrix3f();
         skyRot.fromAngleNormalAxis(FastMath.PI/2,new Vector3f(1,0,0));
         skybox.setLocalRotation(skyRot);
 	
         rootNode.attachChild(skybox);
 
         // create floor.
         Quad q = new Quad("floor", 10, 10);
         q.setLocalTranslation(q.getCenter().negate());
         q.setModelBound(new BoundingBox());
         q.updateModelBound();
         rootNode.attachChild(q);
 
         TextureState ts = display.getRenderer().createTextureState();
         ts.setEnabled(true);
         ts.setTexture
             (TextureManager.loadTexture
              (SdrGame.class.getClassLoader().getResource
               ("net/cscott/sdr/anim/floor.png"),
               Texture.MM_LINEAR_LINEAR,
               Texture.FM_LINEAR));
         q.setRenderState(ts);
         MaterialState ms = display.getRenderer().createMaterialState();
         ms.setAmbient(new ColorRGBA(1,1,1,1));
         q.setRenderState(ms);
 
         // create dancers
         Formation f = Formation.SQUARED_SET;
         //for (Dancer d : f.dancers()) {
         for (Dancer d : StandardDancer.values()) {
             AnimDancer ad = new CheckerDancer(display, (StandardDancer) d);
             dancers.add(ad);
             rootNode.attachChild(ad.node);
             ad.addPosition(Fraction.ZERO, f.location(d));
         }
         if (false) {
         // dancer[5] is COUPLE 3 GIRL, starting at (-1,3,1/2)
         for (int i=0; i<40; i+=8) {
         dancers.get(5).addPosition(Fraction.valueOf(i+2), new Position(Fraction.valueOf(-3), Fraction.valueOf(1), ExactRotation.THREE_QUARTERS));
         dancers.get(5).addPosition(Fraction.valueOf(i+4), new Position(Fraction.valueOf(-4), Fraction.valueOf(3), ExactRotation.ZERO));
         dancers.get(5).addPosition(Fraction.valueOf(i+6), new Position(Fraction.valueOf(-3), Fraction.valueOf(4), ExactRotation.ONE_QUARTER));
         dancers.get(5).addPosition(Fraction.valueOf(i+8), new Position(Fraction.valueOf(-1), Fraction.valueOf(3), ExactRotation.ONE_HALF));
         }
         } else {
         // help debug FormationMapper
         FormationMapper.main(null);
         for (AnimDancer ad: dancers) {
             ad.addPosition(Fraction.valueOf( 5), FormationMapper.test1.location(ad.dancer));
             ad.addPosition(Fraction.valueOf(10), FormationMapper.test1.location(ad.dancer));
             ad.addPosition(Fraction.valueOf(15), FormationMapper.test2.location(ad.dancer));
             ad.addPosition(Fraction.valueOf(20), FormationMapper.test2.location(ad.dancer));
             ad.addPosition(Fraction.valueOf(25), FormationMapper.test1.location(ad.dancer));
         }
         }
             
         
         initialTime = timer.getTimeInSeconds();
         
         // key bindings.
 
         // Assign F5 to the command "caller camera"
         KeyBindingManager.getKeyBindingManager().set
             ("camCaller", KeyInput.KEY_F5);
         // Assign F6 to the command "ceiling camera"
         KeyBindingManager.getKeyBindingManager().set
             ("camCeiling", KeyInput.KEY_F6);
     }
 }
