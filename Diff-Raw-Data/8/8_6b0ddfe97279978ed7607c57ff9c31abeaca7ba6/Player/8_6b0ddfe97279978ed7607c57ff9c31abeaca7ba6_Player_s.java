 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package character;
 
 import com.jme3.bullet.collision.PhysicsRayTestResult;
 import com.jme3.input.KeyInput;
 import com.jme3.input.controls.AnalogListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.material.Material;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.shape.Line;
 import java.util.List;
 import mygame.Game;
 
 /**
  *
  * @author Lena
  */
 public class Player extends Character {
 
     public static enum State {
 
         INVINCIBLE, AGIKITY_POWER_UP, ATTACK_POWER_UP, DEFAULT
     };
     
     private State state = State.DEFAULT;
     private int distanceAttack;
     public String distanceAttackAnimation;
     private Game app;
     
     private final int HORIZONTAL_SPEED = 7;    // the movement speed to the left and right
     private final float JUMP_DELAY = 1.f;       // the interval between jumps (seconds)
     private final float JUMP_DURATION = 0.5f;   // how long is the player jumping (seconds)?
     private final float JUMP_SPEED = 20;        // the upwards speed when jumping
     private final float GRAVITY = -10.f;        // the downwards speed when falling
     private boolean moveLeft = false;       // currently moving left?
     private boolean moveRight = false;      // currently moving right?
     private float currentJumpDelay = 0;     // seconds since last jump
     private boolean jumping = false;        // is currently jumping?
     private boolean canJump = false;        // can currently jump (cannot when falling e.g.)
 
     public Player(Game app, String name, int distanceAttack, int directAttack) {
         super(name, directAttack);
         this.distanceAttack = distanceAttack;
         this.app = app;
         
         initKeys();
         spawnCharacter();
     }
 
     public int distanceAttack() {
         //       this.channel.setAnim("DistanceAttack", 0.1f);
         return this.distanceAttack;
     }
 
     public void changeState(State state) {
         this.state = state;
     }
 
     public State getState() {
         return state;
     }
     
     public void update(float tpf) {
         handleMovement(tpf);
     }
     
     private void handleMovement(float tpf) {
         // move player left or right if necessary
         if (moveLeft) {
             model.move(-HORIZONTAL_SPEED * tpf, 0, 0);
         }
 
         if (moveRight) {
             model.move(HORIZONTAL_SPEED * tpf, 0, 0);
         }
 
         moveLeft = moveRight = false;
 
         // count seconds since last jump
         if (currentJumpDelay < JUMP_DELAY) {
             currentJumpDelay += tpf;
         }
 
         // if jumping duration is over, disable jump
         if (currentJumpDelay > JUMP_DURATION) {
             jumping = false;
         }
 
         // if currently jumping, move the player upwards
         if (jumping) {
             model.move(0, tpf * JUMP_SPEED, 0);
         }
 
         // test if player is above ground
         Vector3f start = getPlayerLocation();
        Vector3f end = getPlayerLocation().add(new Vector3f(0, -0.1f, 0));
         List<PhysicsRayTestResult> physRayResults = app.getBulletAppState().getPhysicsSpace().rayTest(start, end);
 
         // draw helper line
         Geometry g = new Geometry("line", new Line(start, end));
         Material m = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
         g.setMaterial(m);
         app.getRootNode().detachChildNamed("line");
         app.getRootNode().attachChild(g);
 
         // if test failed -> player is falling because he is not above ground
         if (physRayResults.isEmpty()) {
             model.move(0, tpf * GRAVITY, 0);
             canJump = false;    // player can't jump while falling
         } else {
             canJump = true;
         }
     }
 
     private void handleRangedAttack() {
         System.out.println("RangeAttack");
     }
 
     private void handleMeleeAttack() {
         System.out.println("MeleeAttack");
     }
 
     private void spawnCharacter() {
         model = (Node) app.getAssetManager().loadModel("Models/Level/Heaven/cloud_small_1v2.j3o");
         model.setName("Player");
         model.scale(0.3f);
        model.setLocalTranslation(0.f, 7.f, 0);
         app.getRootNode().attachChild(model);
 
     }
 
     public Vector3f getPlayerLocation() {
         return model.getLocalTranslation();
     }
 
     public void respawn() {
         // TODO: do clean up, refill hearts etc.
         // ...
 
         app.getRootNode().detachChildNamed("Player");
         spawnCharacter();
     }
 
     private void initKeys() {
         app.getInputManager().addMapping("Left", new KeyTrigger(KeyInput.KEY_LEFT));
         app.getInputManager().addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT));
         app.getInputManager().addMapping("Jump", new KeyTrigger(KeyInput.KEY_A));
         app.getInputManager().addMapping("RangeAttack", new KeyTrigger(KeyInput.KEY_S));
         app.getInputManager().addMapping("MeleeAttack", new KeyTrigger(KeyInput.KEY_D));
         app.getInputManager().addMapping("Escape", new KeyTrigger(KeyInput.KEY_ESCAPE));
 
         app.getInputManager().addListener(analogListener, new String[]{"Left", "Right", "Jump", "RangeAttack", "MeleeAttack", "Escape"});
     }
     private AnalogListener analogListener = new AnalogListener() {
         public void onAnalog(String name, float value, float tpf) {
             // move left
             if (name.equals("Left")) {
                 moveLeft = true;
             }
 
             // move right
             if (name.equals("Right")) {
                 moveRight = true;
             }
 
             // jump
             if (name.equals("Jump")) {
                 if (currentJumpDelay >= JUMP_DELAY && canJump) {
                     currentJumpDelay = 0;
                     jumping = true;
                 }
             }
 
             // range attack
             if (name.equals("RangeAttack")) {
                 handleRangedAttack();
             }
 
             // melee attack
             if (name.equals("MeleeAttack")) {
                 handleMeleeAttack();
             }
 
             // escape/pause/exit
             if (name.equals("Escape")) {
                 System.out.println("Escape");
             }
         }
     };
 }
