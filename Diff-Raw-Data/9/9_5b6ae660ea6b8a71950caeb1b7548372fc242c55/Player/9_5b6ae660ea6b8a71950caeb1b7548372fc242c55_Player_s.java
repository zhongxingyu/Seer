 package spatial;
 
 import com.jme3.animation.AnimChannel;
 import com.jme3.animation.AnimControl;
 import com.jme3.animation.AnimEventListener;
 import com.jme3.animation.LoopMode;
 import com.jme3.asset.AssetManager;
 import com.jme3.audio.AudioNode;
 import com.jme3.effect.ParticleEmitter;
 import com.jme3.effect.ParticleMesh;
 import com.jme3.light.DirectionalLight;
 import com.jme3.light.Light;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.queue.RenderQueue;
 import com.jme3.scene.Node;
 import control.PlayerControl;
 import java.util.Iterator;
 import state.InGameState;
 import variables.P;
 
 /**
  * Class for keeping everything relating to the player in one place. Currently
  * keeps the spatial (along with the model) and the physics control object which
  * makes the player run and fall.
  *
  * @author dagen
  */
 public class Player extends Node implements AnimEventListener {
 
     private PlayerControl playerControl;
     private Node playerModel;
     private InGameState game;
 
     //animation
     private AnimChannel channel;
     private AnimControl control;
     //end animation
     /**
      * Constructs a
      * <code>Player</code> object which is a
      * <Code>Node</code> with the players 3D-model attached to it. The
      * <code>Player</code> also has a
      * <code>PlayerControl</code> attached.
      * @param assetManager is used for loading the players assets i.e. 3D-model
      * and textures.
      */
     public Player(AssetManager assetManager, InGameState game) {
         super("player");
         this.game = game;
         
         // the player casts shadows
         this.setShadowMode(RenderQueue.ShadowMode.Cast);
 
         // starting position
         this.setLocalTranslation(0.0f, 5.0f, 0.0f);
         
         float hoverHeight = 1;
                 
         // set up the physics control
         playerControl = new PlayerControl(1f, 4f+hoverHeight);
         playerControl.setSpeedFactor(P.speedFactor);
         this.addControl(playerControl);
 
         //Sets the model of the player
 
         //playerModel = (Node) assetManager.loadModel("Models/ghost/ghost2-moreanim-nolightcam.j3o");
        playerModel = (Node) assetManager.loadModel("Models/ghost/ghost04-014cloth003armature003UV001.j3o");
         playerModel.rotate(0, -2.07f, 0);
         control = playerModel.getChild("Sphere").getControl(AnimControl.class);
         channel = control.createChannel();
         playerModel.scale(1.4f);
         playerModel.setLocalTranslation(0f,2.8f+hoverHeight,0f); 
         ParticleEmitter dust = this.getDustParticleEmitter(assetManager);
         playerModel.attachChild(dust);
         dust.move(0.6f, -2.0f, 0f);
         this.attachChild(playerModel);
         //All the code below is for animation of the model
         
         
         control.addListener(this);
         
         channel.setAnim("ArmatureAction.001");
         channel.setLoopMode(LoopMode.Loop);
 
       //End of animation code
         
         // sound
         AudioNode jumpSoundNode = new AudioNode(assetManager, "Sound/Effects/Gun.wav", false);
         jumpSoundNode.setName("jumpsound");
         this.attachChild(jumpSoundNode);
         
         AudioNode pickupPowerup = new AudioNode(assetManager, "Sound/Effects/sfx-powerup-pickup-2.ogg", false);
         pickupPowerup.setName("pickupPowerup");
         this.attachChild(pickupPowerup);
     }
 
     /**
      *
      * @return a reference to the players 3D-model.
      */
     public Node getPlayerModel() {
         return this.playerModel;
     }
     
     public enum Powerup {
         SPEED, INVULN, DOUBLEJUMP
     }
     
     public void updateModelAfterPowerup(Powerup powerup, boolean setting) {
         
         
         ParticleEmitter dust = (ParticleEmitter)this.playerModel.getChild("Emitter");
         if(setting){
             ((AudioNode)this.getChild("pickupPowerup")).playInstance();
         }
         switch(powerup) {
             case SPEED:
                 if (setting) {
                     dust.setHighLife(4f);
                 } else {
                     dust.setHighLife(0.2f);
                 }
                 break;
             case INVULN:
                 game.setInvulnerable(setting);
                 if (setting) {
                     dust.setGravity(0f,20f,0f);
                     dust.setEndSize(3.5f);
                 } else {
                     dust.setGravity(0f,0f,0f);
                     dust.setEndSize(0.1f);
                 }
                 break;
             case DOUBLEJUMP:
                 if (setting) {
                     dust.setEndColor(ColorRGBA.Green);
                 } else {
                     dust.setEndColor(new ColorRGBA(0.05f, 0.05f, 0.05f, 0.5f));
                 }
                 break;
                 
         }
     }
     
     public void updateModelAfterJump() {
         //((AudioNode)this.getChild("jumpsound")).playInstance();
     }
     
     /**
      * Call this to stop the player in its tracks. Useful for a delay before
      * respawning – the player won't fall miles away from the level.
      * @param enabled 
      */
     public void setEnabled(boolean enabled) {
         playerControl.setPaused(!enabled);
         /*if (enabled) {
             this.attachChild(playerModel);
         } else {
             this.detachChild(playerModel);
         }*/
     }
     
     private ParticleEmitter getDustParticleEmitter (AssetManager assetManager) {
              ParticleEmitter fire = StandardParticleEmitter.make(assetManager);
             
     fire.setStartColor(  new ColorRGBA(.40f, 0.40f, 0.40f, 1f));   // bluish grey
     fire.setEndColor(new ColorRGBA(0.05f, 0.05f, 0.05f, 0.5f)); // grey
     fire.getParticleInfluencer().setInitialVelocity(new Vector3f(0, -1, 0));
     fire.setStartSize(3.5f);
     fire.setEndSize(0.1f);
     fire.setGravity(0, 0f, 0);
     fire.setLowLife(0.2f);
     fire.setHighLife(0.2f);
     fire.getParticleInfluencer().setVelocityVariation(0.3f);
     return fire;
     }
     
     //animation function that must be implemented even if unused
      public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
     // unused
   }
     //animation function that must be implemented even if unused 
       public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
     /*if (animName.equals("ArmatureAction")) {
       channel.setAnim("ArmatureAction", 0.50f);
       channel.setLoopMode(LoopMode.DontLoop);
       channel.setSpeed(1f);
     }*/
   }
 }
