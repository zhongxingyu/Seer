 package com.icbat.game.tradesong.assetReferences;
 
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.icbat.game.tradesong.Tradesong;
 import com.icbat.game.tradesong.utils.Constants;
 
 public enum TextureAssets {
     ITEMS("items"),
     HUD_BG("hud-bg"),
     FRAME("frame"),
     SLIDER_HEAD("slider-icon"),
     SLIDER_BG("slider-bg"),
    ;
 
     private String path;
 
     private TextureAssets(String pathVal) {
         this.path = "sprites/" + pathVal + ".png";
     }
 
     public String getPath() {
         return path;
     }
 
     public TextureRegion getRegion(int x, int y) {
         final int spriteDimension = Constants.SPRITE_DIMENSION.value();
         return new TextureRegion(Tradesong.getTexture(this), x * spriteDimension, y * spriteDimension, spriteDimension, spriteDimension);
     }
 }
 
