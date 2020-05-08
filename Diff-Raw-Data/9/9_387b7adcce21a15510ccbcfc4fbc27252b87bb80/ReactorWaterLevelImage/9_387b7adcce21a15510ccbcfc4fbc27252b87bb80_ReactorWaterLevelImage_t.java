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
 import eel.seprphase2.Simulator.PlantStatus;
 
 /**
  *
  * @author Simeon
  */
 public class ReactorWaterLevelImage extends Image {
     private final static Texture bottom  = new Texture(Gdx.files.internal("assets\\game\\water\\reactorwaterbottom.png"));
     private final static Texture middle  = new Texture(Gdx.files.internal("assets\\game\\water\\reactorwatermiddle.png"));
     private final static Texture topSheet = new Texture(Gdx.files.internal("assets\\game\\water\\waterspritesheet.png"));
     private final static Texture glass    = new Texture(Gdx.files.internal("assets\\game\\water\\reactorglass.png"));
     private final static float BASE_Y = 147;
     private final static int COLS = 1;
     private final static int ROWS = 60;
     
     private PlantStatus status;
     private Image bottomImage;
     private Image middleImage;
     private Image topImage;
     private Image glassImage;
     private TextureRegion[] topFrames;
     private Animation animation;
     private TextureRegionDrawable drawable;
     private TextureRegion frame;
     
     private float reactorWaterLevel;
     private float waterSize;
     private float stateTime;
     
     public ReactorWaterLevelImage(PlantStatus status) {
         super();
         this.status = status;
         bottomImage = new Image(bottom);
         middleImage = new Image(middle);
         topImage    = new Image();
         glassImage  = new Image(glass);
         drawable = new TextureRegionDrawable();
         
         TextureRegion[][] split = TextureRegion.split(topSheet, topSheet.getWidth() / COLS, topSheet.getHeight() / ROWS);
         topFrames = new TextureRegion[COLS * ROWS];
         int index = 0;
         for (int i=0; i < ROWS; i++) {
             for (int j=0; j < COLS; j++) {
                 topFrames[index] = split[i][j];
                 index++;
             }
         }
         animation = new Animation(0.033f, topFrames);
         
         bottomImage.setPosition(41, 127);
         middleImage.setPosition(50, BASE_Y);
         glassImage.setPosition(47, 136);
         
         bottomImage.setSize(224, 29);
         middleImage.setSize(206, 160);
         topImage.setSize(206, 20);
         
         bottomImage.setColor(1f, 1f, 1f, 0.7f);
         middleImage.setColor(1f, 1f, 1f, 0.7f);
         topImage.setColor   (1f, 1f, 1f, 0.7f);
         glassImage.setColor(1f, 1f, 1f, 0.3f);
         
         stateTime = 0f;
     }
     
     @Override
     public void draw(SpriteBatch batch, float parentAlpha) {
         stateTime += Gdx.graphics.getDeltaTime();
         frame = animation.getKeyFrame(stateTime, true);
         drawable.setRegion(frame);
         topImage.setDrawable(drawable);
         
         reactorWaterLevel = (float) status.reactorWaterLevel().points(); //Don't worry about the cast - Percentage 
                                                                          //can be only between 0 and 100.
        waterSize = 160f;//reactorWaterLevel * 1.6f;
         
         middleImage.setSize(206, waterSize);
         topImage.setPosition(50, BASE_Y + waterSize);
         
         topImage.draw(batch, parentAlpha);
         bottomImage.draw(batch, parentAlpha);
         middleImage.draw(batch, parentAlpha);
         glassImage.draw(batch, parentAlpha);
     }
     
 }
