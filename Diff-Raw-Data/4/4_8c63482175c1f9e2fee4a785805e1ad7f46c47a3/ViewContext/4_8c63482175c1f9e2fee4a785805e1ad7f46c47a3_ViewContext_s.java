 package net.vincentpetry.nodereviver.view;
 
 import net.vincentpetry.nodereviver.model.GameContext;
 import android.content.res.Resources;
 import android.graphics.Typeface;
 import android.util.DisplayMetrics;
 
 public class ViewContext {
     private Typeface typeface;
     private SpriteManager spriteManager;
     private int width;
     private int height;
 
     private float fontHeightNormal;
     private float fontHeightBig;
     private float scaling;
 
     private GameContext gameContext;
 
     public ViewContext(Resources resources, GameContext gameContext){
         this.gameContext = gameContext;
         this.typeface = Typeface.createFromAsset(resources.getAssets(), "fonts/DejaVuSansMono.ttf");
         this.spriteManager = new SpriteManager(resources);
 
         switch (resources.getDisplayMetrics().densityDpi) {
             case DisplayMetrics.DENSITY_LOW:
                fontHeightNormal = 9.0f;
                fontHeightBig = 11.0f;
                 scaling = 0.5f;
                 break;
             default:
             case DisplayMetrics.DENSITY_MEDIUM:
                 fontHeightNormal = 12.0f;
                 fontHeightBig = 15.0f;
                 scaling = 1.0f;
                 break;
             case DisplayMetrics.DENSITY_HIGH:
                 fontHeightNormal = 15.0f;
                 fontHeightBig = 18.0f;
                 scaling = 1.0f;
                 break;
         }
     }
 
     public Typeface getTypeface(){
         return typeface;
     }
 
     public float getFontHeightBig(){
         return fontHeightBig;
     }
 
     public float getFontHeightNormal(){
         return fontHeightNormal;
     }
 
     public SpriteManager getSpriteManager(){
         return spriteManager;
     }
 
     public void setSize(int width, int height){
         this.width = width;
         this.height = height;
     }
 
     public int getWidth(){
         return width;
     }
 
     public int getHeight(){
         return height;
     }
 
     public GameContext getGameContext(){
         return gameContext;
     }
 
     public float getScaling(){
         return scaling;
     }
 }
