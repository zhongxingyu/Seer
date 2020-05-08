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
     private final static float MOVEMENT_SPEED = 8f;
     
     private float mechanicX;
     private float mechanicWidth;
     private float moveMechanicTo;
     private float stateTime;
     
     private Animation mechanicAnimation;
     private TextureRegion frame;
     private TextureRegionDrawable notMoving;
     private Direction mechanicDirection;
     
 //    public Mechanic(Drawable drawable, Direction mechanicDirection) {
 //        super(drawable);
 //        this.mechanicDirection = mechanicDirection;
 //    }
 //    
 //    public Mechanic(Texture texture, Direction mechanicDirection) {
 //        super(texture);
 //        this.mechanicDirection = mechanicDirection;
 //    }
     
     public Mechanic(TextureRegion[] sheet, Texture notMoving, Direction mechanicDirection) {
         super(notMoving);
         mechanicAnimation = new Animation(0.05f, sheet);
         this.notMoving = new TextureRegionDrawable(new TextureRegion(notMoving));
         this.mechanicDirection = mechanicDirection;
         stateTime = 0;
     }
     
     @Override
     public void draw (SpriteBatch batch, float parentAlpha) {
 
         
         if (mechanicDirection == Direction.Right) {
             super.draw(batch, parentAlpha);
         }
         else if (mechanicDirection == Direction.Left) {
             mechanicX = this.getX();
             mechanicWidth = this.getWidth();
             this.setX(mechanicX + mechanicWidth);
             this.setScaleX(-1f);
             
             super.draw(batch, parentAlpha);
             
             this.setX(mechanicX);
             this.setScaleX(1f);
         }
     }
     
     /**
      * Returns the direction the mechanic is looking at (left or right)
      * @return 
      */
     public Direction getDirection() {
         return this.mechanicDirection;
     }
     
     /**
      * Sets the direction the mechanic is looking at (left or right)
      * @param mechanicDirection the direction
      */
     public void setDirection(Direction mechanicDirection) {
         this.mechanicDirection = mechanicDirection;
     }
     
     public void moveMechanic() {
         mechanicX = this.getX();
         if (Math.abs(mechanicX - moveMechanicTo) > 0.1) {
             stateTime += Gdx.graphics.getDeltaTime();
             frame = mechanicAnimation.getKeyFrame(stateTime, true);
             this.setDrawable(new TextureRegionDrawable(frame));
            this.setScaleY(0.8f);
             
             if (Math.abs(mechanicX - moveMechanicTo) < MOVEMENT_SPEED) {
                 this.setX(moveMechanicTo);
             }
             else if (mechanicX < moveMechanicTo) {
                 this.setDirection(Direction.Right);
                 mechanicX += MOVEMENT_SPEED;
                 this.setX(mechanicX);
             }
             else if (mechanicX > moveMechanicTo) {
                 this.setDirection(Direction.Left);
                 mechanicX -= MOVEMENT_SPEED;
                 this.setX(mechanicX);
             }
         }
         else {
             this.setDrawable(notMoving);
            this.setScaleY(1f);
         }
     }
     
     public void moveMechanicTo(float moveMechanicTo) {
         this.moveMechanicTo = moveMechanicTo;
     }
 }
