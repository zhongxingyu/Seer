 package entity;
 
 import display.HUD;
 import environment.Model;
 import org.lwjgl.Sys;
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector3f;
 import org.lwjgl.util.vector.Vector4f;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Owner
  * Date: 02/05/13
  * Time: 10:50 AM
  * To change this template use File | Settings | File Templates.
  */
 public class Player extends Entity {
 
     public HUD hud;
     public float health;
     public Vector3f fatalCrashPos;
     public Vector3f offset;
     public Boolean turning, rollActive;
     public float boundaryDirection;
     private boolean crashed, posTilt;
     public int score = 0;
     public byte lastHitBy;
     private int crashTilt, barrelRollTilt;
     private float brCooldown, invincibleTime;
 
     public Player(Model model) {
         super(model);
     }
 
     public void Render() {
         super.Render();
     }
 
     public void Update() {
         if (state == State.Dead) {
 //            score--;
             respawn();
         }
 
         if (health <= 0 && state != State.Dead) {
             state = State.FatalCrash;
             doFatalCrash();
         }
        else if((Sys.getTime() - brCooldown) / 1000 < 2)
         {
             state = State.Invincible;
             doABarrelRoll();
         }
         else if(state == State.Invincible && !turning)
         {
             state = State.Alive;
         }
         else
         {
             if (crashed)
                 doCrash();
             if(turning)
                 doTurn();
 
             model.updateRotation(-hud.crosshairPos.y * .1f, -hud.crosshairPos.x * .1f, -hud.crosshairPos.x * .1f);
         }
     }
 
     public void respawn() {
         //TODO: respawn code
     }
 
     public void crash() {
         crashed = true;
     }
     public void barrelRoll()
     {
         if((Sys.getTime() - brCooldown) / 1000 > 5)
         {
             brCooldown = Sys.getTime();
             barrelRollTilt = 0;
         }
     }
 
     private void doTurn()
     {
         hud.setCrosshairX((int)(boundaryDirection / Math.abs(boundaryDirection)));
     }
 
     private void doFatalCrash() {
         model.updateRotation(model.pitch - .5f, model.yaw, model.roll + 5f);
         model.updatePosition(model.transX - .05f, model.transY, model.transZ - .2f);
     }
 
     private void doCrash() {
         int maxTilt = 30;
         int step = 10;
 
         if (crashTilt == maxTilt)
             posTilt = false;
         else if (crashTilt == -maxTilt)
             posTilt = true;
 
         crashTilt += posTilt ? step : -step;
 
         if (posTilt && crashTilt == 0)
             crashed = false;
 
         float crashPitch = crashTilt > 0 ? crashTilt : 0;
 
         model.updateRotation(crashPitch, 0.0f, crashTilt);
     }
 
     private void doABarrelRoll()
     {
         if(barrelRollTilt < 60)
         {
             barrelRollTilt++;
             model.updateRotation(model.pitch, model.yaw, model.roll+ 6f);
         }
     }
 
     public void setHealth(float value)
     {
         if(state != State.Invincible)
         {
             health -= value;
         }
     }
 
     public void Initialize() {
         super.Initialize();
         hud = new HUD();
         posTilt = false;
         turning = false;
         rollActive = true;
         crashTilt = 0;
         state = State.Alive;
         fatalCrashPos = new Vector3f();
         offset = new Vector3f();
         health = 1;
         model.updatePosition(0,0,-5);
         health = 1;
         model.updatePosition(0,0,-5);
        model.updateRotation(0, 0, 0);
         brCooldown = Sys.getTime();
     }
 
     public void setOffset(float yaw, float pitch, Vector3f pos) {
         //Account for initial model movement, may have to adjust others values in the future
         Vector4f position = new Vector4f(pos.x, pos.y, pos.z, 1);
 
         Matrix4f rotate = new Matrix4f();
         rotate.setIdentity();
 
         Matrix4f.rotate((float) Math.toRadians(pitch), new Vector3f(1.0f, 0.0f, 0.0f), rotate, rotate);
         Matrix4f.transform(rotate, position, position);
 
         rotate.setIdentity();
 
         Matrix4f.rotate((float) Math.toRadians(yaw), new Vector3f(0.0f, 1.0f, 0.0f), rotate, rotate);
         Matrix4f.transform(rotate, position, position);
 
         offset = new Vector3f(position.x, position.y, -position.z);
     }
 
 
 }
