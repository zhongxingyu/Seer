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
 import lamprey.seprphase3.GUI.Screens.Direction;
 
 /**
  *
  * @author Simeon
  */
 public class Mechanic extends Image {
     Texture mechanicRun;
     Texture mechanicStand;
     TextureRegion[] runFrames;
     TextureRegion[] standFrames;
     Animation runAnimation;
     Animation standAnimation;
     
     private final static int RUN_COLS = 5;
     private final static int RUN_ROWS = 4;
     private final static int STAND_COLS = 5;
     private final static int STAND_ROWS = 6;
     private final static float MOVEMENT_SPEED = 7f;
     private final static float MOVEMENT_FREQUENCY = 0.015f;
     
     private float mechanicX;
     private float mechanicWidth;
     private float scaleToUse;
     private float destination;
     private float stateTime;
     private float deltaSum;
     private float delta;
     
     private TextureRegion frame;
     private Direction mechanicDirection;
         
     public Mechanic() {
         super();
         mechanicRun   = new Texture(Gdx.files.internal("assets\\game\\mechrunspritesheet.png"));
         mechanicStand = new Texture(Gdx.files.internal("assets\\game\\mechstandspritesheet.png"));
         
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
         
         this.mechanicDirection = Direction.Right;
         stateTime = 0;
         deltaSum = 0;
     }
     
     @Override
     public void draw (SpriteBatch batch, float parentAlpha) {
         mechanicX = this.getX();
         delta = Gdx.graphics.getDeltaTime();
         stateTime += delta;
         deltaSum  += delta;
         if (Math.abs(mechanicX - destination) > 0.1) {
             while (deltaSum > MOVEMENT_FREQUENCY) {
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
                 deltaSum -= MOVEMENT_FREQUENCY;
             }
             frame = runAnimation.getKeyFrame(stateTime, true);
             this.setDrawable(new TextureRegionDrawable(frame));
             this.setSize(100f, 130f);
             scaleToUse = 0.9f;
         }
         else {
             frame = standAnimation.getKeyFrame(stateTime, true);
             this.setDrawable(new TextureRegionDrawable(frame));
             this.setSize(100f, 130f);
             scaleToUse = 1f;
             deltaSum = 0f;
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
 }
