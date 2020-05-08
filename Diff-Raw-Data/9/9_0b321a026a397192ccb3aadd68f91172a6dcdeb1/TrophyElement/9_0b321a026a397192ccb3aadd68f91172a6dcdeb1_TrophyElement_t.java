 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.hopkins.rocknrollracing.views.elements;
 
 import com.hopkins.rocknrollracing.utils.ImageUtils;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 
 /**
  *
  * @author ihopkins
  */
 public class TrophyElement extends AppElement {
     
    public static final String SPRITE_PATH = "images/trophies/%s.png";
     
     protected BufferedImage trophies[];
 
     @Override
     public void load() throws Exception {
         trophies = new BufferedImage[3];
         for(int i = 0; i < trophies.length; i++) {
             trophies[i] = loadSprite(SPRITE_PATH, "" + (i+1));
         }
     }
     
     public void render(Graphics g, int x, int y, int place) {
         g.drawImage(trophies[place], x, y, null);
     }
     
 }
