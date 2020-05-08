 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 
 package com.spacejunk.sprites;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.io.FileInputStream;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.geom.*;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import com.spacejunk.SoundManager;
 import com.spacejunk.particles.*;
 import com.spacejunk.SpaceJunk;
 import com.spacejunk.util.*;
 import java.util.Random;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.DisplayMode;
 import org.newdawn.slick.Color;
 
 /**
  * 
  * @author Techjar
  */
 public class Sprite0Ship implements Sprite {
     private List<Sprite> sprites;
     private List<Particle> particles;
     private int id, laserSound, selectedPowerup, maxSelectedPowerup;
     private float x, y;
     private long lastFire, hitTime, invTime, invForTime, invFrameTime;
     private long rocketShots, nukeShots, laserPower, maxLaserPower, laserTime, laserForTime;
     private boolean visible, hit, invincible, invState, respawning, fireLaser, laserCharged, nukeShot, autoPowerupSwap;
     private Vector2f laser1, laser2;
     private Map<String, Long> powerupTime, powerups;
     private Texture tex, guntex, rocketTex, laserTex;
     private SoundManager sm;
     private SpaceJunk sj;
     private Particle1Jet jet;
     private Particle3Laser laser1p, laser2p;
     private Color randomColor;
     private Shape bounds, circle18;
     private TickCounter tc;
     private UnicodeFont font;
     private Texture[] powerupTex;
     private Random random;
 
 
     public Sprite0Ship(List sprites, List particles, float x, float y, SoundManager sm, SpaceJunk sj) {
         try {
             this.sj = sj;
             this.tc = sj.getTickCounter();
             this.random = new Random();
             this.sprites = sprites; this.particles = particles;
             this.id = 0; this.x = x; this.y = y;
             this.lastFire = 0; this.hitTime = 0; this.invFrameTime = 0; this.invTime = 0; this.invForTime = 0;
             this.rocketShots = Sprite3Rocket.POWERUP_LIFE; this.fireLaser = false; this.nukeShots = Sprite7Nuke.POWERUP_LIFE;
             this.visible = true; this.hit = false; this.invincible = false; this.invState = true; this.respawning = false;
             this.powerupTime = new HashMap<String, Long>(); this.powerups = new HashMap<String, Long>();
             this.tex = TextureLoader.getTexture("PNG", new FileInputStream("resources/textures/ship.png"), GL_NEAREST);
             this.guntex = TextureLoader.getTexture("PNG", new FileInputStream("resources/textures/gunfire.png"), GL_LINEAR);
             this.rocketTex = TextureLoader.getTexture("PNG", new FileInputStream("resources/textures/rocket.png"), GL_NEAREST);
             this.laserTex = TextureLoader.getTexture("PNG", new FileInputStream("resources/textures/laser.png"), GL_NEAREST);
             this.sm = sm; this.maxLaserPower = 600; this.laserPower = this.maxLaserPower; this.laserCharged = true; this.nukeShot = false; this.autoPowerupSwap = false;
             this.jet = new Particle1Jet(sj, this.x - 26, this.y + 16, 0, true);
             this.randomColor = new Color(0, 0, 0); this.laserSound = -1; this.selectedPowerup = 0; this.maxSelectedPowerup = 2;
             this.laser1 = new Vector2f(0, 0); this.laser2 = new Vector2f(0, 0);
             this.laser1p = new Particle3Laser(this.sj, 0, 0, 0); particles.add(laser1p);
             this.laser2p = new Particle3Laser(this.sj, 0, 0, 0); particles.add(laser2p);
             this.bounds = new Rectangle(this.x - 28, this.y + 2, 22, 28); this.circle18 = new Circle(0, 10, 8);
             //this.bounds = new Polygon(PolygonHitbox.SHIP);
             particles.add(jet);
 
             // Setup map stuff
             powerupTime.put(Sprite3Rocket.KEY_NAME, 0L);
             powerupTime.put(Sprite7Nuke.KEY_NAME, 0L);
 
             // Powerup textures
             this.powerupTex = new Texture[Powerup.TOTAL_POWERUPS];
             for(int i = 0; i < Powerup.TOTAL_POWERUPS; i++) this.powerupTex[i] = TextureLoader.getTexture("PNG", new FileInputStream("resources/textures/powerups/" + Powerup.ALL_POWERUPS[i] + ".png"), GL_LINEAR);
 
             // Load font
             this.font = new UnicodeFont("resources/fonts/batmfa_.ttf", 10, false, false);
             font.addAsciiGlyphs();
             font.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
             font.loadGlyphs();
         }
         catch(Exception e) {
             e.printStackTrace();
             System.exit(0);
         }
     }
     
     public void update() {
         this.fireLaser = false;
         if(this.invincible) this.randomColor = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
         invFrameTime++;
         if(this.invincible) {
             if(invFrameTime >= 2) {
                 invState = !invState;
                 invFrameTime = 0;
             }
 
             if(tc.getTickMillis() - invTime >= invForTime) {
                 this.setInvincible(false);
                 this.respawning = false;
             }
         }
         else if(!invState) {
             invState = true;
         }
 
         jet.setVisible(this.visible);
         if(!this.hit) {
             this.x = Mouse.getX() + (tex.getImageWidth() / 2);
             this.y = (Display.getDisplayMode().getHeight() - Mouse.getY()) - (tex.getImageHeight() / 2);
             jet.setX(this.x - 26); jet.setY(this.y + 16);
             if(!this.respawning) {
                 if(Mouse.isButtonDown(1) && !powerups.isEmpty()) {
                     if(!this.autoPowerupSwap && tc.getTickMillis() - powerupTime.get(Sprite3Rocket.KEY_NAME) >= Sprite3Rocket.SHOT_DELAY && powerups.containsKey(Powerup.ROCKET) && this.selectedPowerup == 2) {
                         powerupTime.put(Sprite3Rocket.KEY_NAME, tc.getTickMillis());
                         sprites.add(new Sprite3Rocket(this.sj, sprites, particles, sm, this.x - 7, this.y + 8, this.rocketTex));
                         this.rocketShots++;
                     }
                     if(!this.autoPowerupSwap && powerups.containsKey(Powerup.LASER) && this.selectedPowerup == 1 && this.laserPower > 0 && this.laserCharged) {
                         this.fireLaser = true;
                         if(this.laserSound < 0) this.laserSound = sm.playSoundEffect("weapon.laser", true);
                     }
                     if(!this.autoPowerupSwap && powerups.containsKey(Powerup.NUKE) && this.selectedPowerup == 0 && !this.nukeShot) {
                         sprites.add(new Sprite7Nuke(this.sj, this.sprites, this.particles, this.sm, this.x - 5, this.y));
                         sm.playSoundEffect("ship.powerup");
                         this.nukeShots++;
                         this.nukeShot = true;
                     }
                 }
                 else if(Mouse.isButtonDown(0)) {
                     if((powerups.containsKey(Powerup.FASTSHOT) && tc.getTickMillis() - lastFire >= 40) || tc.getTickMillis() - lastFire >= 200) {
                         lastFire = tc.getTickMillis();
                         sprites.add(new Sprite1Gunfire(this.sj, sprites, particles, sm, this.x - 16, this.y - 3, this.guntex, powerups.containsKey(Powerup.BIGSHOT)));
                         sprites.add(new Sprite1Gunfire(this.sj, sprites, particles, sm, this.x - 16, this.y + 19, this.guntex, powerups.containsKey(Powerup.BIGSHOT)));
                         sm.playSoundEffect("ship.gunfire");
                     }
                 }
                 
                 if(!Mouse.isButtonDown(1)) {
                     this.nukeShot = false;
                     this.autoPowerupSwap = false;
                 }
             }
         }
         else {
             if((tc.getTickMillis() - hitTime) >= 2000) {
                 this.setVisible(true);
                 this.setInvincible(true); invTime = tc.getTickMillis(); invForTime = 1000;
                 this.hit = false;
                 this.respawning = true;
                 sj.clearScreen();
             }
         }
         bounds.setCenterX(this.x - 17); bounds.setCenterY(this.y + 16);
 
         if(this.fireLaser && this.visible && this.laserPower > 0 && this.laserCharged) {
             this.laserPower = MathHelper.clamp(this.laserPower - 5, 0, this.maxLaserPower);
         }
         else if(!this.fireLaser && this.laserPower < this.maxLaserPower) {
             if(this.laserPower < 1) this.laserCharged = false;
             this.laserPower = MathHelper.clamp(this.laserPower + 2, 0, this.maxLaserPower);
         }
         else if(!this.fireLaser) {
             this.laserCharged = true;
         }
         if(this.laserSound > -1 && !this.fireLaser) {
             sm.stopSoundEffect(this.laserSound);
             this.laserSound = -1;
         }
         laser1p.setVisible(this.fireLaser);
         laser2p.setVisible(this.fireLaser);
     }
 
     public void render() {
         DisplayMode dm = Display.getDisplayMode();
         if(this.fireLaser && this.visible && this.laserPower > 0 && this.laserCharged) {
             processLaser((int)this.x - 11, (int)this.y + 3, 1); laser1p.setLocation(laser1);
             processLaser((int)this.x - 11, (int)this.y + 24, 2); laser2p.setLocation(laser2);
         }
         if(powerups.containsKey(Powerup.LASER)) this.drawProgressBar(dm.getWidth() - 105, dm.getHeight() - 15, 100, 10, (float)this.laserPower / (float)this.maxLaserPower, new Color(1F - ((float)this.laserPower / (float)this.maxLaserPower), (float)this.laserPower / (float)this.maxLaserPower, 0));
 
         if(this.visible) {
             // store the current model matrix
             glPushMatrix();
 
             // bind to the appropriate texture for this sprite
             tex.bind();
 
             // translate to the right location and prepare to draw
             glTranslatef(x, y, 0);
             glRotatef(90, 0, 0, 1);
             if(this.invincible && this.respawning && !this.invState) glColor4f(1, 1, 1, 0);
             else if(this.invincible && this.respawning) glColor4f(1, 1, 1, 0.5F);
             else if(this.invincible) glColor3f(this.randomColor.r, this.randomColor.b, this.randomColor.g);
             else glColor3f(1, 1, 1);
 
             // draw a quad textured to match the sprite
             glBegin(GL_QUADS);
                 glTexCoord2f(0, 0); glVertex2f(0, 0);
                 glTexCoord2f(0, tex.getHeight()); glVertex2f(0, tex.getImageHeight());
                 glTexCoord2f(tex.getWidth(), tex.getHeight()); glVertex2f(tex.getImageWidth(), tex.getImageHeight());
                 glTexCoord2f(tex.getWidth(), 0); glVertex2f(tex.getImageWidth(), 0);
             glEnd();
 
             // restore the model view matrix to prevent contamination
             glPopMatrix();
         }
 
         glPushMatrix();
         int pos = 18; long ptime = 0, pstime = 0, plife = 0;
         if(powerups.containsKey(Powerup.ROCKET)) {
             plife = powerups.containsKey(Powerup.ROCKET) ? powerups.get(Powerup.ROCKET) : Sprite3Rocket.POWERUP_LIFE;
             ptime = this.rocketShots - plife;
             if(ptime >= Sprite3Rocket.POWERUP_LIFE) {
                 this.removePowerup(Powerup.ROCKET);
             }
             else {
                 pstime = Sprite3Rocket.POWERUP_LIFE - ptime;
                 drawPowerupIcon(this.powerupTex[2], dm.getWidth() - pos, 2);
                 glPushMatrix(); font.drawString((dm.getWidth() - (font.getWidth(Long.toString(pstime)) / 2)) - (pos - 8), 18, Long.toString(pstime), Color.white); glPopMatrix();
                 if(this.selectedPowerup == 2) {
                     circle18.setCenterX(dm.getWidth() - pos + 8);
                     glColor3f(1, 1, 0); ShapeRenderer.draw(this.circle18); glColor3f(1, 1, 1);
                 }
             }
         }
         pos += 18;
 
         if(powerups.containsKey(Powerup.FASTSHOT)) {
             plife = powerups.containsKey(Powerup.FASTSHOT) ? powerups.get(Powerup.FASTSHOT) : 0;
             ptime = tc.getTickMillis() - plife;
             if(ptime >= 20000) {
                 this.removePowerup(Powerup.FASTSHOT);
             }
             else {
                 pstime = ((20000 - ptime) / 1000) + 1;
                 drawPowerupIcon(this.powerupTex[0], dm.getWidth() - pos, 2);
                 glPushMatrix(); font.drawString((dm.getWidth() - (font.getWidth(Long.toString(pstime)) / 2)) - (pos - 8), 18, Long.toString(pstime), Color.white); glPopMatrix();
             }
         }
         pos += 18;
 
         if(powerups.containsKey(Powerup.BIGSHOT)) {
             plife = powerups.containsKey(Powerup.BIGSHOT) ? powerups.get(Powerup.BIGSHOT) : 0;
             ptime = tc.getTickMillis() - plife;
             if(ptime >= Sprite1Gunfire.POWERUP_LIFE) {
                 this.removePowerup(Powerup.BIGSHOT);
             }
             else {
                 pstime = ((Sprite1Gunfire.POWERUP_LIFE - ptime) / 1000) + 1;
                 drawPowerupIcon(this.powerupTex[1], dm.getWidth() - pos, 2);
                 glPushMatrix(); font.drawString((dm.getWidth() - (font.getWidth(Long.toString(pstime)) / 2)) - (pos - 8), 18, Long.toString(pstime), Color.white); glPopMatrix();
             }
         }
         pos += 18;
 
         if(powerups.containsKey(Powerup.INVINCIBILITY)) {
             ptime = tc.getTickMillis() - invTime;
             if(ptime >= invForTime) {
                 this.removePowerup(Powerup.INVINCIBILITY);
             }
             else {
                 pstime = ((invForTime - ptime) / 1000) + 1;
                 drawPowerupIcon(this.powerupTex[3], dm.getWidth() - pos, 2);
                 glPushMatrix(); font.drawString((dm.getWidth() - (font.getWidth(Long.toString(pstime)) / 2)) - (pos - 8), 18, Long.toString(pstime), Color.white); glPopMatrix();
             }
         }
         pos += 18;
 
         if(powerups.containsKey(Powerup.LASER)) {
             ptime = tc.getTickMillis() - this.laserTime;
             if(ptime >= this.laserForTime) {
                 this.removePowerup(Powerup.LASER);
             }
             else {
                 pstime = ((this.laserForTime - ptime) / 1000) + 1;
                 drawPowerupIcon(this.powerupTex[4], dm.getWidth() - pos, 2);
                 glPushMatrix(); font.drawString((dm.getWidth() - (font.getWidth(Long.toString(pstime)) / 2)) - (pos - 8), 18, Long.toString(pstime), Color.white); glPopMatrix();
                 if(this.selectedPowerup == 1) {
                     circle18.setCenterX(dm.getWidth() - pos + 8);
                     glColor3f(1, 1, 0); ShapeRenderer.draw(this.circle18); glColor3f(1, 1, 1);
                 }
             }
         }
         pos += 18;
 
         if(powerups.containsKey(Powerup.NUKE)) {
             plife = powerups.containsKey(Powerup.NUKE) ? powerups.get(Powerup.NUKE) : Sprite7Nuke.POWERUP_LIFE;
             ptime = this.nukeShots - plife;
             if(ptime >= Sprite7Nuke.POWERUP_LIFE) {
                 this.removePowerup(Powerup.NUKE);
             }
             else {
                 pstime = Sprite7Nuke.POWERUP_LIFE - ptime;
                 drawPowerupIcon(this.powerupTex[5], dm.getWidth() - pos, 2);
                 glPushMatrix(); font.drawString((dm.getWidth() - (font.getWidth(Long.toString(pstime)) / 2)) - (pos - 8), 18, Long.toString(pstime), Color.white); glPopMatrix();
                 if(this.selectedPowerup == 0) {
                     circle18.setCenterX(dm.getWidth() - pos + 8);
                     glColor3f(1, 1, 0); ShapeRenderer.draw(this.circle18); glColor3f(1, 1, 1);
                 }
             }
         }
         pos += 18;
         glPopMatrix();
     }
 
     public int getID() {
         return this.id;
     }
 
     public void setVisible(boolean visible) {
         this.visible = visible;
     }
 
     public boolean isVisible() {
         return this.visible;
     }
 
     public float getX() {
         return this.x;
     }
 
     public float getY() {
         return this.y;
     }
 
     public void setX(float x) {
         this.x = x;
     }
 
     public void setY(float y) {
         this.y = y;
     }
 
     public void hit() {
         try {
             particles.add(new Particle0Explosion(sj, this.x - 16, this.y + 16, 1500, 0));
             powerups.clear();
             this.invForTime = 0; this.laserForTime = 0;
             this.rocketShots = Sprite3Rocket.POWERUP_LIFE; this.fireLaser = false; this.nukeShots = Sprite7Nuke.POWERUP_LIFE;
             sm.playSoundEffect("ambient.explode.0", false);
         }
         catch(Exception e) {
             e.printStackTrace();
         }
         this.setVisible(false);
         this.hit = true;
         hitTime = tc.getTickMillis();
     }
 
     public boolean isHit() {
         return this.hit;
     }
 
     public void setInvincible(boolean invincible) {
         this.invincible = invincible;
     }
 
     public boolean isInvincible() {
         return this.invincible;
     }
 
     public boolean isRespawning() {
         return this.respawning;
     }
 
     public Shape getBounds() {
         return this.bounds;
     }
 
     public Vector2f getLocation() {
         return new Vector2f(this.x, this.y);
     }
 
     public void addPowerup(String powerup) {
         if(powerup.equals(Powerup.ROCKET)) this.selectedPowerup = 2;
         if(powerup.equals(Powerup.LASER)) this.selectedPowerup = 1;
         if(powerup.equals(Powerup.NUKE)) this.selectedPowerup = 0;
         
         if(powerup.equals(Powerup.ROCKET)) this.rocketShots -= Sprite3Rocket.POWERUP_LIFE;
         if(powerup.equals(Powerup.NUKE)) this.nukeShots -= Sprite7Nuke.POWERUP_LIFE;
         if(powerup.equals(Powerup.INVINCIBILITY)) {
             this.invincible = true;
             this.invForTime += 15000;
             if(!powerups.containsKey(powerup)) this.invTime = tc.getTickMillis();
             if(sm.getCurrentMusic() != "invincible") sm.playMusic("invincible", true);
         }
         if(powerup.equals(Powerup.LASER)) {
             this.laserForTime += 30000;
             if(!powerups.containsKey(powerup)) this.laserTime = tc.getTickMillis();
         }
         if(powerups.containsKey(powerup) && !powerup.equals(Powerup.ROCKET) && !powerup.equals(Powerup.NUKE)) {
             if(powerup.equals(Powerup.FASTSHOT)) powerups.put(powerup, powerups.get(powerup) + 20000);
             else if(powerup.equals(Powerup.BIGSHOT)) powerups.put(powerup, powerups.get(powerup) + Sprite1Gunfire.POWERUP_LIFE);
         }
         else powerups.put(powerup, powerup.equals(Powerup.ROCKET) || powerup.equals(Powerup.NUKE) ? 0 : tc.getTickMillis());
     }
 
     public void removePowerup(String powerup) {
         if(powerups.containsKey(powerup)) {
             while(powerups.containsKey(powerup)) powerups.remove(powerup);
             while(powerups.containsKey(powerup)) powerups.remove(powerup);
             if(powerup.equals(Powerup.INVINCIBILITY)) {
                 sm.stopMusic();
                 this.invForTime = 0;
             }
             if(powerup.equals(Powerup.LASER)) this.laserForTime = 0;
             if((powerup.equals(Powerup.ROCKET) && this.selectedPowerup == 2) || (powerup.equals(Powerup.LASER) && this.selectedPowerup == 1) || (powerup.equals(Powerup.NUKE) && this.selectedPowerup == 0)) {
                 this.selectPowerup(this.selectedPowerup + 1);
                 this.autoPowerupSwap = true;
             }
         }
     }
 
     private void processLaser(int x, int y, int laser) {
         int x2 = 0; Shape b1 = new Rectangle(x, y, 1, 5), b2 = null; Sprite sp = null; boolean found = false;
         for(x2 = 0; x2 < Display.getDisplayMode().getWidth(); x2 += 20) {
             for(int j = 0; j < sprites.size(); j++) {
                 sp = sprites.get(j); b2 = sp.getBounds();
                 if(sp instanceof HostileSprite && b1.intersects(new Rectangle(b2.getMinX(), b2.getMinY(), b2.getWidth(), b2.getHeight()))) {
                     found = true;
                     break;
                 }
             }
             if(found) break;
             b1 = new Rectangle(x, y, x2, 5);
         }
         if(laser == 1) laser1.set(Display.getDisplayMode().getWidth(), y + 2);
         if(laser == 2) laser2.set(Display.getDisplayMode().getWidth(), y + 2);
 
         x2 -= 20; b1 = new Rectangle(x, y, x2, 5);
         if(found && sp != null && b2 != null) {
             for(int j = x2; j < Display.getDisplayMode().getWidth(); j++) {
                 if(sp instanceof HostileSprite && b1.intersects(b2)) {
                    if(laser == 1) laser1.set(j + x, y + 2);
                    if(laser == 2) laser2.set(j + x, y + 2);
                     ((HostileSprite)sp).hit(1);
                     break;
                 }
                 b1 = new Rectangle(x, y, j, 5);
             }
         }
         this.drawTexturedSquare(b1.getX(), b1.getY(), b1.getWidth(), b1.getHeight(), this.laserTex);
     }
 
     private void drawPowerupIcon(Texture texture, int x, int y) {
         // store the current model matrix
         glPushMatrix();
 
         // bind to the appropriate texture for this sprite
         texture.bind();
 
         // translate to the right location and prepare to draw
         glTranslatef(x, y, 0);
         glColor3f(1, 1, 1);
 
         // draw a quad textured to match the sprite
         glBegin(GL_QUADS);
             glTexCoord2f(0, 0); glVertex2f(0, 0);
             glTexCoord2f(0, texture.getHeight()); glVertex2f(0, texture.getImageHeight() / 2);
             glTexCoord2f(texture.getWidth(), texture.getHeight()); glVertex2f(texture.getImageWidth() / 2, texture.getImageHeight() / 2);
             glTexCoord2f(texture.getWidth(), 0); glVertex2f(texture.getImageWidth() / 2, 0);
         glEnd();
 
         // restore the model view matrix to prevent contamination
         glPopMatrix();
     }
 
     private void drawTexturedSquare(float x, float y, float width, float height, Texture texture) {
         // store the current model matrix
         glPushMatrix();
 
         // bind to the appropriate texture for this sprite
         texture.bind();
 
         // translate to the right location and prepare to draw
         glTranslatef(x, y, 0);
         glColor3f(1, 1, 1);
 
         // draw a quad textured to match the sprite
         glBegin(GL_QUADS);
             glTexCoord2f(0, 0); glVertex2f(0, 0);
             glTexCoord2f(0, texture.getHeight()); glVertex2f(0, height);
             glTexCoord2f(texture.getWidth(), texture.getHeight()); glVertex2f(width, height);
             glTexCoord2f(texture.getWidth(), 0); glVertex2f(width, 0);
         glEnd();
 
         // restore the model view matrix to prevent contamination
         glPopMatrix();
     }
 
     private void drawProgressBar(float x, float y, float width, float height, float progress) {
         this.drawProgressBar(x, y, width, height, progress, Color.red, Color.darkGray);
     }
 
     private void drawProgressBar(float x, float y, float width, float height, float progress, Color color1) {
         this.drawProgressBar(x, y, width, height, progress, color1, Color.darkGray);
     }
 
     private void drawProgressBar(float x, float y, float width, float height, float progress, Color color1, Color color2) {
         drawSquare(x, y, width, height, color2);
         drawSquare(x, y, width * progress, height, color1);
     }
 
     private void drawSquare(float x, float y, float width, float height, Color color) {
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
     
     public void processPowerupSelect(int type) {
         if(type == 0) {
             if(Keyboard.getEventKey() == Keyboard.KEY_LEFT) {
                 this.selectedPowerup = MathHelper.loop(this.selectedPowerup - 1, 0, this.maxSelectedPowerup);
                 for(int i = 0; i < this.maxSelectedPowerup && !this.powerupSelectExists(this.selectedPowerup); i++) this.selectedPowerup = MathHelper.loop(this.selectedPowerup - 1, 0, this.maxSelectedPowerup);
             }
             if(Keyboard.getEventKey() == Keyboard.KEY_RIGHT) {
                 this.selectedPowerup = MathHelper.loop(this.selectedPowerup + 1, 0, this.maxSelectedPowerup);
                 for(int i = 0; i < this.maxSelectedPowerup && !this.powerupSelectExists(this.selectedPowerup); i++) this.selectedPowerup = MathHelper.loop(this.selectedPowerup + 1, 0, this.maxSelectedPowerup);
             }
         }
         if(type == 1) {
             int dwheel = Mouse.getDWheel();
             if(dwheel > 0) {
                 this.selectedPowerup = MathHelper.loop(this.selectedPowerup - 1, 0, this.maxSelectedPowerup);
                 for(int i = 0; i < this.maxSelectedPowerup && !this.powerupSelectExists(this.selectedPowerup); i++) this.selectedPowerup = MathHelper.loop(this.selectedPowerup - 1, 0, this.maxSelectedPowerup);
             }
             if(dwheel < 0) {
                 this.selectedPowerup = MathHelper.loop(this.selectedPowerup + 1, 0, this.maxSelectedPowerup);
                 for(int i = 0; i < this.maxSelectedPowerup && !this.powerupSelectExists(this.selectedPowerup); i++) this.selectedPowerup = MathHelper.loop(this.selectedPowerup + 1, 0, this.maxSelectedPowerup);
             }
         }
     }
     
     private void selectPowerup(int powerup) {
         this.selectedPowerup = MathHelper.clamp(powerup, 0, this.maxSelectedPowerup);
         for(int i = 0; i < this.maxSelectedPowerup && !this.powerupSelectExists(this.selectedPowerup); i++) this.selectedPowerup = MathHelper.loop(this.selectedPowerup - 1, 0, this.maxSelectedPowerup);
     }
     
     private boolean powerupSelectExists(int select) {
         if((select == 0 && powerups.containsKey(Powerup.NUKE)) || (select == 1 && powerups.containsKey(Powerup.LASER)) || (select == 2 && powerups.containsKey(Powerup.ROCKET))) return true;
         return false;
     }
 }
