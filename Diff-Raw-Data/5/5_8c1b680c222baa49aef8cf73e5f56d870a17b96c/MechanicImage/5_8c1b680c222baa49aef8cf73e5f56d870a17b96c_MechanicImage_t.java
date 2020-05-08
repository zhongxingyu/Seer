 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lamprey.seprphase3.GUI.Images;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import eel.seprphase2.Simulator.CannotRepairException;
 import eel.seprphase2.Simulator.KeyNotFoundException;
 import eel.seprphase2.Simulator.PlantController;
 import lamprey.seprphase3.GUI.Screens.Direction;
 
 /**
  *
  * @author Simeon
  */
 public class MechanicImage extends Image {
     private static final int RUN_COLS = 5;
     private static final int RUN_ROWS = 4;
     private static final int STAND_COLS = 5;
     private static final int STAND_ROWS = 6;
     private static final int REPAIR_COLS = 5;
     private static final int REPAIR_ROWS = 4;
     private static final float MOVEMENT_SPEED = 7f;
     private static final float MOVEMENT_FREQUENCY = 0.015f;
     private static final float RUN_Y = 80f;
     private static final float STATIC_Y = 75f;
     
     private Texture mechanicRun;
     private Texture mechanicStand;
     private Texture mechanicRepairing;
     private TextureRegion[] runFrames;
     private TextureRegion[] standFrames;
     private TextureRegion[] repairingFrames;
     private Animation runAnimation;
     private Animation standAnimation;
     private Animation repairingAnimation;
     private TextureRegionDrawable drawable;
    
     private PlantController controller;
     private CurrentlyRepairing currentlyRepairing;
     private float mechanicX;
     private float mechanicWidth;
     private float scaleToUse;
     private float destination;
     private float stateTime;
     private float deltaMovement;
     private float deltaRepairing;
     private float delta;
     private boolean moving;
 
     private TextureRegion frame;
     private Direction mechanicDirection;
         
     public MechanicImage(PlantController controller) {
         super();
         this.controller = controller;
         mechanicRun       = new Texture(Gdx.files.internal("assets\\game\\spritesheets\\mechrunspritesheet.png"));
         mechanicStand     = new Texture(Gdx.files.internal("assets\\game\\spritesheets\\mechstandspritesheet.png"));
         mechanicRepairing = new Texture(Gdx.files.internal("assets\\game\\spritesheets\\mechhammerspritesheet.png"));
         
         TextureRegion[][] split = TextureRegion.split(mechanicRun, mechanicRun.getWidth() / RUN_COLS, mechanicRun.getHeight() / RUN_ROWS);
         runFrames = new TextureRegion[RUN_COLS * RUN_ROWS];
         int index = 0;
         for (int i=0; i < RUN_ROWS; i++) {
             for (int j=0; j < RUN_COLS; j++) {
                 runFrames[index] = split[i][j];
                 index++;
             }
         }
         runAnimation = new Animation(0.033f, runFrames);
         
         split = TextureRegion.split(mechanicStand, mechanicStand.getWidth() / STAND_COLS, mechanicStand.getHeight() / STAND_ROWS);
         standFrames = new TextureRegion[STAND_COLS * STAND_ROWS];
         index = 0;
         for (int i=0; i < STAND_ROWS; i++) {
             for (int j=0; j < STAND_COLS; j++) {
                 standFrames[index] = split[i][j];
                 index++;
             }
         }
         standAnimation = new Animation(0.06f, standFrames);
         
         split = TextureRegion.split(mechanicRepairing, mechanicRepairing.getWidth() / REPAIR_COLS, mechanicRepairing.getHeight() / REPAIR_ROWS);
         repairingFrames = new TextureRegion[REPAIR_COLS * REPAIR_ROWS];
         index = 0;
         for (int i=0; i < REPAIR_ROWS; i++) {
             for (int j=0; j < REPAIR_COLS; j++) {
                 repairingFrames[index] = split[i][j];
                 index++;
             }
         }
        repairingAnimation = new Animation(0.03f, repairingFrames);
         
         this.mechanicDirection = Direction.Right;
         stateTime = 0;
         deltaMovement = 0;
         deltaRepairing = 0;
         drawable = new TextureRegionDrawable();
         currentlyRepairing = CurrentlyRepairing.None;
         moving = false;
     }
     
     @Override
     public void draw (SpriteBatch batch, float parentAlpha) {
         mechanicX = this.getX();
         delta = Gdx.graphics.getDeltaTime();
         stateTime += delta;
         deltaMovement  += delta;
         deltaRepairing += delta;
         
         if (currentlyRepairing != CurrentlyRepairing.None && !moving && deltaRepairing > 0.33) {
             try {
                 if (currentlyRepairing == CurrentlyRepairing.Condenser) {
                     controller.repairCondenser();
                 }
                 else if (currentlyRepairing == CurrentlyRepairing.Turbine) {
                     controller.repairTurbine();
                 }
                 else if (currentlyRepairing == CurrentlyRepairing.Pump1) {
                     controller.repairPump(1);
                 }
                 else if (currentlyRepairing == CurrentlyRepairing.Pump2) {
                     controller.repairPump(2);
                 }
             }
             catch(CannotRepairException e) {
                 currentlyRepairing = CurrentlyRepairing.None;
             }
             catch(KeyNotFoundException e) {
             }
 
             deltaRepairing -= 0.33;
         }
 
         
         if (Math.abs(mechanicX - destination) > 0.1) {
             while (deltaMovement > MOVEMENT_FREQUENCY) {
                 if (Math.abs(mechanicX - destination) < MOVEMENT_SPEED) {
                     this.setX(destination);
                 }
                 else if (mechanicX < destination) {
                     this.setDirection(Direction.Right);
                     mechanicX += MOVEMENT_SPEED;
                     this.setX(mechanicX);
                 }
                 else if (mechanicX > destination) {
                     this.setDirection(Direction.Left);
                     mechanicX -= MOVEMENT_SPEED;
                     this.setX(mechanicX);
                 }
                 moving = true;
                 deltaMovement -= MOVEMENT_FREQUENCY;
             }
             this.setY(RUN_Y);
             frame = runAnimation.getKeyFrame(stateTime, true);
             drawable.setRegion(frame);
             this.setDrawable(drawable);
             this.setSize(100f, 130f);
             scaleToUse = 0.9f;
         }
        else if (currentlyRepairing != CurrentlyRepairing.None) {
             this.setY(STATIC_Y);
             frame = repairingAnimation.getKeyFrame(stateTime, true);
             drawable.setRegion(frame);
             this.setDrawable(drawable);
             this.setSize(100f, 130f);
             scaleToUse = 1f;
             deltaMovement = 0f;
             moving = false;
         }
         else {
             this.setY(STATIC_Y);
             frame = standAnimation.getKeyFrame(stateTime, true);
             drawable.setRegion(frame);
             this.setDrawable(drawable);
             this.setSize(100f, 130f);
             scaleToUse = 1f;
             deltaMovement = 0f;
             moving = false;
         }
         
         if (mechanicDirection == Direction.Right) {
             this.setScale(scaleToUse);
             super.draw(batch, parentAlpha);
         }
         else if (mechanicDirection == Direction.Left) {
             mechanicX = this.getX();
             mechanicWidth = this.getWidth();
             this.setX(mechanicX + mechanicWidth);
             this.setScaleX(-scaleToUse);
             this.setScaleY(scaleToUse);
             
             super.draw(batch, parentAlpha);
             
             this.setX(mechanicX);
             this.setScaleX(scaleToUse);
         }
     }
     
     /**
      * Returns the direction the mechanic is looking at (left or right)
      * @return 
      */
     private Direction getDirection() {
         return this.mechanicDirection;
     }
     
     /**
      * Sets the direction the mechanic is looking at (left or right)
      * @param mechanicDirection the direction
      */
     private void setDirection(Direction mechanicDirection) {
         this.mechanicDirection = mechanicDirection;
     }
     
     public void moveMechanicTo(float destination) {
         this.destination = destination;
     }
     
     public void setRepairing(CurrentlyRepairing currentlyRepairing) {
         this.currentlyRepairing = currentlyRepairing;
     }
 }
