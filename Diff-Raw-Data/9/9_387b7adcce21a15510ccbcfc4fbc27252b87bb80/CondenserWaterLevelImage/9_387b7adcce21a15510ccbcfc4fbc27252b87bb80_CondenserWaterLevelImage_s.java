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
 public class CondenserWaterLevelImage extends Image {
     private final static Texture bottom   = new Texture(Gdx.files.internal("assets\\game\\water\\condenserwaterbottom.png"));
     private Texture middle;//   = new Texture(Gdx.files.internal("assets\\game\\water\\condenserwatermiddle.png"));
     private final static Texture topSheet = new Texture(Gdx.files.internal("assets\\game\\water\\waterspritesheet.png"));
     private final static Texture glass    = new Texture(Gdx.files.internal("assets\\game\\water\\condenserglass.png"));
     private final static float BASE_Y = 150;
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
     
     private float condenserWaterLevel;
     private float waterSize;
     private float stateTime;
     
     public CondenserWaterLevelImage(PlantStatus status) {
         super();
         this.status = status;
         middle   = new Texture(Gdx.files.internal("assets\\game\\water\\condenserwatermiddle.png"));
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
         
         bottomImage.setPosition(562, 133);
         middleImage.setPosition(565, BASE_Y);
         glassImage.setPosition(563, 137);
         
         bottomImage.setSize(190, 27);
         middleImage.setSize(182, 159);
         topImage.setSize(182, 20);
         
         bottomImage.setColor(1f, 1f, 1f, 0.7f);
         middleImage.setColor(1f, 1f, 1f, 0.7f);
         topImage.setColor(1f, 1f, 1f, 0.7f);
         glassImage.setColor(1f, 1f, 1f, 0.3f);
         
         stateTime = 0f; //Makes animation in reactor and condenser at different frames
     }
     
     @Override
     public void draw(SpriteBatch batch, float parentAlpha) {
         stateTime += Gdx.graphics.getDeltaTime();
         frame = animation.getKeyFrame(stateTime, true);
         drawable.setRegion(frame);
         topImage.setDrawable(drawable);
 
         condenserWaterLevel = (float) status.condenserWaterLevel().points(); //Don't worry about the cast - Percentage 
                                                                          //can be only between 0 and 100.
        waterSize = condenserWaterLevel * 1.6f;
         
         middleImage.setSize(182, waterSize);
         topImage.setPosition(565, BASE_Y + waterSize);
         
         topImage.draw(batch, parentAlpha);
         bottomImage.draw(batch, parentAlpha);
         middleImage.draw(batch, parentAlpha);
         glassImage.draw(batch, parentAlpha);
     }
 }
