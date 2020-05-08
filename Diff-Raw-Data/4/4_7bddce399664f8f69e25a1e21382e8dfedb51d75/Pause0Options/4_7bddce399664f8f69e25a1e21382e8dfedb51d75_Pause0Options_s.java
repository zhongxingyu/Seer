 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 
 package com.spacejunk.pause;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.Color;
 import com.spacejunk.util.Bounds;
 import com.spacejunk.SoundManager;
 
 /**
  * 
  * @author Techjar
  */
 public class Pause0Options implements Pause {
     private int y, pressed;
     private boolean active, hovered;
     private String text;
     private UnicodeFont font;
     private Color color;
     private SoundManager sm;
     private Bounds bounds;
 
 
     public Pause0Options(UnicodeFont font, Color color, SoundManager sm) {
         this.y = 0; this.pressed = 0;
         this.active = false; this.hovered = false;
         this.text = "OPTIONS";
         this.sm = sm;
         this.color = color;
         this.font = font;
         this.bounds = new Bounds((Display.getDisplayMode().getWidth() - font.getWidth(this.text)) / 2, this.y, font.getWidth(this.text), font.getHeight(this.text));
     }
 
     public void render() {
         this.bounds = new Bounds((Display.getDisplayMode().getWidth() - font.getWidth(this.text)) / 2, this.y, font.getWidth(this.text), font.getHeight(this.text));
         font.drawString((Display.getDisplayMode().getWidth() - font.getWidth(this.text)) / 2, this.y, this.text, this.color);
     }
 
     public void renderScreen() {
         Bounds mouse = new Bounds(Mouse.getX(), Display.getDisplayMode().getHeight() - Mouse.getY(), 1, 1);
         font.drawString(((Display.getDisplayMode().getWidth() - font.getWidth("MUSIC VOLUME")) / 2) - 100, 100, "MUSIC VOLUME", Color.red);
         font.drawString(((Display.getDisplayMode().getWidth() - font.getWidth("SOUND VOLUME")) / 2) - 105, 130, "SOUND VOLUME", Color.red);
         drawSquare(((Display.getDisplayMode().getWidth() - 200) / 2) + 100, 108, 200, 2, Color.darkGray);
         drawSquare(((Display.getDisplayMode().getWidth() - 200) / 2) + 100, 138, 200, 2, Color.darkGray);
 
        Bounds mus = new Bounds(((Display.getDisplayMode().getWidth() - 200) / 2) + 100 + Math.round(sm.getMusicVolume() * 200F), 100, 8, 18);
        Bounds snd = new Bounds(((Display.getDisplayMode().getWidth() - 200) / 2) + 100 + Math.round(sm.getSoundVolume() * 200F), 130, 8, 18);
         Bounds back = new Bounds((Display.getDisplayMode().getWidth() - font.getWidth("BACK")) / 2, 180, font.getWidth("BACK"), font.getHeight("BACK"));
         drawSlider(((Display.getDisplayMode().getWidth() - 200) / 2) + 100 + Math.round(sm.getMusicVolume() * 200F), 100, 8, 18, mouse.intersects(mus) || this.pressed == 1 ? Color.red.addToCopy(new Color(0, 50, 50)) : Color.red);
         drawSlider(((Display.getDisplayMode().getWidth() - 200) / 2) + 100 + Math.round(sm.getSoundVolume() * 200F), 130, 8, 18, mouse.intersects(snd) || this.pressed == 2 ? Color.red.addToCopy(new Color(0, 50, 50)) : Color.red);
         font.drawString((Display.getDisplayMode().getWidth() - font.getWidth("BACK")) / 2, 180, "BACK", mouse.intersects(back) ? Color.red.addToCopy(new Color(0, 50, 50)) : Color.red);
 
         if((mouse.intersects(mus) || mouse.intersects(snd) || mouse.intersects(back)) && !this.hovered && this.pressed == 0) {
             sm.playSoundEffect("ui.button.rollover", false);
             this.hovered = true;
         }
         else if(!mouse.intersects(mus) && !mouse.intersects(snd) && !mouse.intersects(back)) {
             this.hovered = false;
         }
 
         if(Mouse.isButtonDown(0)) {
             int offset = ((Display.getDisplayMode().getWidth() - 200) / 2) + 100;
             if(mouse.intersects(mus) || this.pressed == 1) {
                 this.pressed = 1;
                 sm.setMusicVolume((float)(Mouse.getX() - offset) / 200F);
             }
             if(mouse.intersects(snd) || this.pressed == 2) {
                 this.pressed = 2;
                 sm.setSoundVolume((float)(Mouse.getX() - offset) / 200F);
             }
             if(mouse.intersects(back)) {
                 sm.playSoundEffect("ui.button.click", false);
                 this.setActive(false);
             }
         }
         else {
             this.pressed = 0;
         }
     }
 
     public Bounds getBounds() {
         return this.bounds;
     }
 
     public Color getColor() {
         return this.color;
     }
 
     public void setColor(Color color) {
         this.color = color;
     }
 
     public void setTextY(int y) {
         this.y = y;
     }
 
     public boolean isActive() {
         return this.active;
     }
 
     public void setActive(boolean active) {
         this.active = active;
     }
 
     public String getText() {
         return this.text;
     }
 
     private void drawSquare(int x, int y, int width, int height, Color color) {
         glPushMatrix();
         glDisable(GL_TEXTURE_2D);
 
         glTranslatef(x, y, 0);
         glColor3f(color.r, color.g, color.b);
         glBegin(GL_QUADS);
             glTexCoord2f(0, 0); glVertex2f(0, 0);
             glTexCoord2f(1, 0); glVertex2f(width, 0);
             glTexCoord2f(1, 1); glVertex2f(width, height);
             glTexCoord2f(0, 1); glVertex2f(0, height);
         glEnd();
 
         glEnable(GL_TEXTURE_2D);
         glPopMatrix();
     }
 
     private void drawSlider(int x, int y, int width, int height, Color color) {
         glPushMatrix();
         glDisable(GL_TEXTURE_2D);
 
         glTranslatef(x, y, 0);
         glTranslatef(-(width >> 1), 0, 0);
         glColor3f(color.r, color.g, color.b);
         glBegin(GL_QUADS);
             glTexCoord2f(0, 0); glVertex2f(0, 0);
             glTexCoord2f(1, 0); glVertex2f(width, 0);
             glTexCoord2f(1, 1); glVertex2f(width, height);
             glTexCoord2f(0, 1); glVertex2f(0, height);
         glEnd();
 
         glEnable(GL_TEXTURE_2D);
         glPopMatrix();
     }
 }
