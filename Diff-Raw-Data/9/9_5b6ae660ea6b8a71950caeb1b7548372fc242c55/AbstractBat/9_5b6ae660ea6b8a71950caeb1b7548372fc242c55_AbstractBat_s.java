 package spatial.hazard;
 
 import com.jme3.animation.AnimChannel;
 import com.jme3.animation.AnimControl;
 import com.jme3.animation.AnimEventListener;
 import com.jme3.animation.LoopMode;
 import com.jme3.asset.AssetManager;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import java.util.Random;
 import spatial.PlayerInteractor;
 
 /**
  * An abstract AbstractFireball. Any class extending this one will make a PlayerInteractor
  * that looks like a fireball.
  * 
  * @author jonatankilhamn
  */
 public abstract class AbstractBat extends PlayerInteractor implements AnimEventListener {
     
     //animation
     protected AnimChannel channel;
     protected AnimControl control;
     
     private static Node modelForBat = null;
     
     @Override
     protected Spatial createModel(AssetManager assetManager) {
         
         if (modelForBat == null) {
            modelForBat = (Node) assetManager.loadModel ("Models/bat/bat02-002mirror006anim3fly.j3o");
             
             Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
             mat.setBoolean("UseMaterialColors",true);
             mat.setColor("Diffuse", ColorRGBA.Orange);
 
             modelForBat.setMaterial(mat);
             modelForBat.rotate (+1.6f,+1.4f,0); //flying towards player, slightly tilted up
         }
         Node model = (Node) modelForBat.clone();
 
         control = model.getChild("Sphere").getControl(AnimControl.class);
         channel = control.createChannel();
         
         control.addListener(this);
         
         channel.setAnim("ArmatureAction");
         channel.setLoopMode(LoopMode.Loop);
         
         float mod = (float) (new Random().nextInt(5)-2) * 0.25f;
         
         channel.setSpeed(2f+mod);
         
         
         return model;
     }
     //animation function that must be implemented even if unused
     public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
     }
     //animation function that must be implemented even if unused
     public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
     }
     
     @Deprecated
     public Material getRandomColorMaterial (AssetManager assetManager) {
          Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                   
          Random r = new Random();
          int i = r.nextInt(2);
          mat.setBoolean("UseMaterialColors",true);
          ColorRGBA[] c = {ColorRGBA.Cyan, ColorRGBA.Red, ColorRGBA.White, ColorRGBA.Orange, ColorRGBA.Yellow};
          mat.setColor("Diffuse", c[i]);
          return mat;
     }
 }
