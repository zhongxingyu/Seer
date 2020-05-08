 package de.mibbiodev.ld26.tile;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Texture;
 
 /**
  * @author mibbio
  */
 public class ExitTile extends Tile {
 
     public ExitTile(float x, float y) {
         super(false, x, y);
     }
 
     @Override
     public Texture getTexture(Color scheme) {
        Color exitColor = Color.WHITE;
        exitColor.sub(scheme.cpy().mul(shade));
        pixelMap.setColor(exitColor);
         pixelMap.fill();
         pixelMap.setColor(Color.BLACK);
         tileTexture.draw(pixelMap, 0, 0);
         return tileTexture;
     }
 
     @Override
     public void tick(float tickTime) {
         // TODO Auto-generated method stub
     }
 }
