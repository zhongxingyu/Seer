 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 
 package com.spacejunk;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.util.glu.GLU.*;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.util.Random;
 import java.util.List;
 import java.util.ArrayList;
 import java.net.URL;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.Sys;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.font.effects.*;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.geom.*;
 import org.newdawn.slick.openal.SoundStore;
 import de.matthiasmann.twl.utils.PNGDecoder;
 import com.spacejunk.sprites.*;
 import com.spacejunk.particles.*;
 import com.spacejunk.util.*;
 import com.spacejunk.pause.*;
 import com.spacejunk.error.*;
 import com.spacejunk.title.*;
 import java.util.Calendar;
 
 /**
  * Actual SpaceJunk game object, contains the main code that makes the game work.
  * @author Techjar
  */
 public class SpaceJunk {
     private static int DISPLAY_WIDTH, DISPLAY_HEIGHT, DIFFICULTY;
     private static boolean FULLSCREEN;
     private static DisplayMode DISPLAY_MODE;
     private int score, deaths, startCountdown, prevStartCountdown, fpsDisp, curLevel, nextRand, nextPowerupRand, lastMouseX, lastMouseY, pauseScreen, titleScreen;
     private long lastAsteroid, lastPowerup, fpsDispTime, time, startTime, lastFrame, highScore, fpsLastFrame, fps, astCollTime;
     private float titleFade;
     private boolean mouseClicked, runGame, renderCollision, prevMusicPlaying, firstPowerup, onTitle, astColl, asteroidCollision;;
     private String pauseHover, titleHover;
     private UnicodeFont batmfa20, batmfa37, batmfa60, batmfa100;
     private SoundManager soundManager;
     private Random random = new Random();
     private Texture bg;
     private Texture[] atex;
     private List<Pause> pauseMenu;
     private List<Title> titleMenu;
     private List<Sprite> sprites, titleSprites;
     private List<Sprite2Asteroid> asteroids;
     private List<PowerupSprite> powerups;
     private List<Particle> particles;
     private TickCounter tc, startTc;
 
 
     /**
      * Creates a new SpaceJunk game instance.
      * @param difficulty
      * @param mode
      * @param fullscreen
      * @param vSync
      * @param musicVolume
      * @param soundVolume
      * @param renderColl
      * @throws LWJGLException
      * @throws SlickException
      * @throws FileNotFoundException
      * @throws IOException
      * @throws JFOSException
      */
     public SpaceJunk(int difficulty, DisplayMode mode, boolean fullscreen, boolean vSync, float musicVolume, float soundVolume, boolean renderColl) throws LWJGLException, SlickException, FileNotFoundException, IOException, JFOSException, InterruptedException {
         // OS check here to be mean...
         if(OS.getOS() == OS.UNKNOWN) {
             Sys.alert("Unsupported OS", "Sorry, your operating system is not compatible with Junk from Outer Space.");
             System.exit(0);
         }
         
        // MEGA IMPORTANT SHUTDOWN HOOK!!!!!
         Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
 
         // Setup sound manager
         soundManager = new SoundManager();
         soundManager.setMusicVolume(musicVolume);
         soundManager.setSoundVolume(soundVolume);
 
         // Store these
         DIFFICULTY = difficulty;
         DISPLAY_WIDTH = mode.getWidth();
         DISPLAY_HEIGHT = mode.getHeight();
         FULLSCREEN = fullscreen;
         DISPLAY_MODE = mode;
 
         // Default stuff
         score = 0; deaths = 0; curLevel = 1; nextRand = 0; nextPowerupRand = 0; pauseScreen = 0; titleScreen = 0; runGame = false; startCountdown = 0;
         lastAsteroid = 0; lastPowerup = 0; time = 0; startTime = 0; lastFrame = 0; pauseHover = ""; titleHover = ""; titleFade = 0.0F;
         onTitle = true; mouseClicked = false; renderCollision = renderColl; prevMusicPlaying = false; firstPowerup = true; prevStartCountdown = 0;
         sprites = new ArrayList<Sprite>(); titleSprites = new ArrayList<Sprite>(); asteroids = new ArrayList<Sprite2Asteroid>();
         powerups = new ArrayList<PowerupSprite>(); particles = new ArrayList<Particle>();
         atex = new Texture[5]; highScore = 0; fps = 0; fpsDisp = 0; fpsDispTime = 0; fpsLastFrame = 0; astColl = true; astCollTime = 0;
         asteroidCollision = true; // Should asteroids bounce off eachother? WARNING: MAY LAG ON HIGH DIFFICULTY/LEVEL!!!
         tc = new TickCounter(60); startTc = new TickCounter(60);
 
         // Display
         Display.setDisplayMode(mode);
         Display.setFullscreen(fullscreen);
         Display.setVSyncEnabled(vSync);
         Display.setTitle("Junk from Outer Space");
 
         // Setup icons
         ByteBuffer[] icons = new ByteBuffer[4];
         icons[0] = createIcon(new File("resources/icons/16.png").toURI().toURL());
         icons[1] = createIcon(new File("resources/icons/32.png").toURI().toURL());
         icons[2] = createIcon(new File("resources/icons/32.png").toURI().toURL());
         icons[3] = createIcon(new File("resources/icons/128.png").toURI().toURL());
         Display.setIcon(icons);
 
         // Create display
         Display.create();
 
         // Load fonts
         batmfa20 = new UnicodeFont("resources/fonts/batmfa_.ttf", 20, false, false);
         batmfa20.addAsciiGlyphs();
         batmfa20.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
         batmfa20.getEffects().add(new OutlineEffect(1, java.awt.Color.LIGHT_GRAY));
         //batmfa20.getEffects().add(new ShadowEffect(java.awt.Color.DARK_GRAY, 3, 3, 0.3F));
         batmfa20.loadGlyphs();
 
         batmfa37 = new UnicodeFont("resources/fonts/batmfa_.ttf", 37, false, false);
         batmfa37.addAsciiGlyphs();
         batmfa37.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
         batmfa37.getEffects().add(new GradientEffect(java.awt.Color.LIGHT_GRAY, java.awt.Color.BLACK, 1));
         batmfa37.getEffects().add(new OutlineEffect(1, java.awt.Color.GRAY));
         batmfa37.loadGlyphs();
 
         batmfa60 = new UnicodeFont("resources/fonts/batmfa_.ttf", 60, false, false);
         batmfa60.addAsciiGlyphs();
         batmfa60.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
         batmfa60.getEffects().add(new OutlineEffect(2, java.awt.Color.LIGHT_GRAY));
         batmfa60.loadGlyphs();
 
         batmfa100 = new UnicodeFont("resources/fonts/batmfa_.ttf", 100, false, true);
         batmfa100.addAsciiGlyphs();
         batmfa100.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
         batmfa100.getEffects().add(new GradientEffect(java.awt.Color.LIGHT_GRAY, java.awt.Color.BLACK, 1));
         batmfa100.getEffects().add(new OutlineEffect(2, java.awt.Color.GRAY));
         batmfa100.loadGlyphs();
 
         // Keyboard
         Keyboard.create();
 
         // Mouse
         Mouse.create();
        Mouse.setGrabbed(false);
 
         /*batmfa20 = new UnicodeFont("resources/fonts/batmfa_.ttf", 20, false, false);
         batmfa20.addAsciiGlyphs();
         batmfa20.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
         batmfa20.loadGlyphs();*/
 
         // Setup pause menu
         pauseMenu = new ArrayList<Pause>();
         pauseMenu.add(new Pause2Resume(batmfa20, Color.white, soundManager, this));
         pauseMenu.add(new Pause3Title(batmfa20, Color.white, soundManager, this));
         pauseMenu.add(new Pause0Options(batmfa20, Color.white, soundManager, this));
         pauseMenu.add(new Pause1Quit(batmfa20, Color.white, soundManager, this));
 
         // Setup title menu
         titleMenu = new ArrayList<Title>();
         titleMenu.add(new Title2StartGame(batmfa20, Color.white, soundManager, this));
         titleMenu.add(new Title0Options(batmfa20, Color.white, soundManager, this));
         titleMenu.add(new Title1Quit(batmfa20, Color.white, soundManager, this));
     }
 
     /**
      * Sets up this SpaceJunk instance.
      */
     public void create() {
         // Init
         this.init();
 
         // OpenGL
         this.initGL();
         this.resizeGL();
     }
 
     /**
      * Destroys this SpaceJunk instance.
      */
     public void destroy() {
         Mouse.destroy();
         Keyboard.destroy();
         Display.destroy();
     }
 
     /**
      * Runs this SpaceJunk instance.
      */
     public void run() {
         while(!Display.isCloseRequested()) {
             if(Display.isVisible()) {
                 this.processKeyboard();
                 this.processMouse();
                 this.update();
                 this.render();
                 fps = Math.round((double)tc.getTickRate() / Math.max(((double)System.nanoTime() - (double)fpsLastFrame) / (1000000000D / (double)tc.getTickRate()), 1D));
                 fpsLastFrame = System.nanoTime(); tc.setFps((int)fps);
                 lastFrame = tc.getTickMillis();
             }
             else {
                 if(Display.isDirty()) {
                     this.render();
                 }
                 try {
                     Thread.sleep(100);
                 }
                 catch(InterruptedException e) {
                 }
             }
             Display.update();
             Display.sync(tc.getTickRate());
         }
     }
 
     private void init() {
         try {
             bg = TextureLoader.getTexture("JPG", new FileInputStream("resources/textures/bg.jpg"), GL_LINEAR);
             atex[0] = TextureLoader.getTexture("PNG", new FileInputStream("resources/textures/asteroid0.png"), GL_LINEAR);
             atex[1] = TextureLoader.getTexture("PNG", new FileInputStream("resources/textures/asteroid1.png"), GL_LINEAR);
             atex[2] = TextureLoader.getTexture("PNG", new FileInputStream("resources/textures/asteroid2.png"), GL_LINEAR);
             atex[3] = TextureLoader.getTexture("PNG", new FileInputStream("resources/textures/asteroid3.png"), GL_LINEAR);
             atex[4] = TextureLoader.getTexture("PNG", new FileInputStream("resources/textures/asteroid4.png"), GL_LINEAR);
             soundManager.playMusic("title", true);
             startTime = tc.getTickMillis();
             highScore = Long.parseLong(ConfigManager.getProperty("high-score").toString());
             this.generateTitleAsteroids();
         }
         catch(Exception e) {
             e.printStackTrace();
             System.exit(0);
         }
     }
 
     private void initGL() {
         // 2D Initialization
         glClearColor(0, 0, 0, 0);
         glDisable(GL_DEPTH_TEST);
         glDisable(GL_LIGHTING);
         glEnable(GL_TEXTURE_2D);
         glEnable(GL_ALPHA_TEST);
         glEnable(GL_BLEND);
         glAlphaFunc(GL_GREATER, 0);
         glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
     }
 
     private void resizeGL() {
         // 2D Scene
         glViewport(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT);
 
         glMatrixMode(GL_PROJECTION);
         glLoadIdentity();
         gluOrtho2D(0.0f, DISPLAY_WIDTH, DISPLAY_HEIGHT, 0.0f);
         glPushMatrix();
 
         glMatrixMode(GL_MODELVIEW);
         glLoadIdentity();
         glPushMatrix();
     }
 
     private void processKeyboard() {
         while(Keyboard.next() && Keyboard.getEventKeyState()) {
             if(!onTitle && runGame) {
                 if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
                     if(Mouse.isGrabbed()) {
                         ConfigManager.setProperty("high-score", this.highScore);
                         lastMouseX = Mouse.getX();
                         lastMouseY = Mouse.getY();
                     }
                     else {
                         Mouse.setCursorPosition(lastMouseX, lastMouseY);
                     }
 
                     Mouse.setGrabbed(!Mouse.isGrabbed());
                     if(Mouse.isGrabbed()) {
                         pauseScreen = 0;
                     }
                 }
                 // Old F-key options code, replaced by much nicer options GUI.
                 /*if(Keyboard.getEventKey() == Keyboard.KEY_F5) soundManager.setSoundVolume(soundManager.getSoundVolume() - 0.1F);
                 if(Keyboard.getEventKey() == Keyboard.KEY_F6) soundManager.setSoundVolume(soundManager.getSoundVolume() + 0.1F);
                 if(Keyboard.getEventKey() == Keyboard.KEY_F7) soundManager.setMusicVolume(soundManager.getMusicVolume() - 0.1F);
                 if(Keyboard.getEventKey() == Keyboard.KEY_F8) soundManager.setMusicVolume(soundManager.getMusicVolume() + 0.1F);
                 if(Keyboard.getEventKey() == Keyboard.KEY_F9) soundManager.playRandomMusic();
                 if(Keyboard.getEventKey() == Keyboard.KEY_F10) soundManager.stopMusic();
                 if(Keyboard.getEventKey() == Keyboard.KEY_F11) changeDisplayMode(FULLSCREEN ? DISPLAY_MODE : Display.getDesktopDisplayMode(), !FULLSCREEN);*/
                 if(Keyboard.getEventKey() == Keyboard.KEY_F1) soundManager.playRandomMusic();
             }
         }
     }
 
     private void processMouse() {
     }
 
     private void render() {
         glClear(GL_COLOR_BUFFER_BIT);
         glLoadIdentity();
 
         drawBg();
         if(!onTitle) {
             if(runGame) {
                 Particle particle = null;
                 for(int i = 0; i < particles.size(); i++) {
                     particle = particles.get(i);
                     if(particle.isVisible() && particle.renderFirst()) particle.render();
                 }
 
                 Sprite sprite = null;
                 for(int i = 0; i < sprites.size(); i++) {
                     sprite = sprites.get(i);
                     if(sprite.isVisible()) {
                         sprite.render();
                         if(renderCollision) ShapeRenderer.draw(sprite.getBounds());
                     }
                 }
 
                 for(int i = 0; i < particles.size(); i++) {
                     particle = particles.get(i);
                     if(particle.isVisible() && !particle.renderFirst()) particle.render();
                 }
 
                 try {
                     glPushMatrix();
                     batmfa20.drawString(5, 1, "Time: " + time, Color.yellow);
                     batmfa20.drawString(5, 21, "Level: " + curLevel, Color.yellow);
                     batmfa20.drawString(5, 41, "Score: " + score, Color.yellow);
                     batmfa20.drawString(5, 61, "Deaths: " + deaths, Color.yellow);
                     batmfa20.drawString(5, DISPLAY_HEIGHT - 21, "FPS: " + fpsDisp, Color.yellow);
                     batmfa20.drawString(5, 81, "Asteroids: " + asteroids.size(), Color.yellow); // DEBUG: Asteroid count display!
                     glPopMatrix();
                 }
                 catch(Exception e) {
                     e.printStackTrace();
                 }
 
                 if(!Mouse.isGrabbed()) processPause();
             }
             else {
                 processCountdown();
             }
         }
         else {
             processTitle();
         }
         if(titleFade < 1) drawTitleFade();
     }
 
     private void update() {
         if(tc.getTickMillis() - fpsDispTime >= 100) {
             fpsDisp = (int)fps;
             fpsDispTime = tc.getTickMillis();
         }
         if(tc.getTickMillis() - astCollTime >= 1000 && !astColl) astColl = true;
         if(!SoundStore.get().isMusicPlaying() && !prevMusicPlaying) {
             if(onTitle) soundManager.playMusic("title", true);
             else if(runGame) soundManager.playRandomMusic();
         }
         prevMusicPlaying = SoundStore.get().isMusicPlaying();
         if(Mouse.isGrabbed() && !onTitle && runGame) {
             if(!Display.isActive()) {
                 ConfigManager.setProperty("high-score", this.highScore);
                 lastMouseX = Mouse.getX();
                 lastMouseY = Mouse.getY();
                 Mouse.setGrabbed(false);
             }
             tc.incTicks();
             time = (tc.getTickMillis() - startTime) / 1000;
             generateAsteroid();
             generatePowerup();
 
             Sprite sprite = null;
             for(int i = 0; i < sprites.size(); i++) {
                 sprite = sprites.get(i);
                 sprite.update();
                 processCollision(sprite);
             }
             for(int i = 0; i < sprites.size(); i++) {
                 sprite = sprites.get(i);
                 if(!(sprite instanceof Sprite0Ship)) {
                     if(!sprite.isVisible()) sprites.remove(sprite);
                     //if(!sprite.isVisible() && sprite instanceof Sprite2Asteroid) asteroids.remove((Sprite2Asteroid)sprite);
                     //if(!sprite.isVisible() && sprite instanceof PowerupSprite) powerups.remove((PowerupSprite)sprite);
                 }
             }
 
             for(int i = 0; i < asteroids.size(); i++) {
                 sprite = asteroids.get(i);
                 if(!sprite.isVisible()) asteroids.remove((Sprite2Asteroid)sprite);
             }
             for(int i = 0; i < powerups.size(); i++) {
                 sprite = powerups.get(i);
                 if(!sprite.isVisible()) powerups.remove((PowerupSprite)sprite);
             }
 
             Particle particle = null;
             boolean skip = false;
             for(int i = 0; i < particles.size(); i++) {
                 particle = particles.get(i);
                 particle.update();
             }
             for(int i = 0; i < particles.size(); i++) {
                 skip = false; particle = particles.get(i);
                 if(particle instanceof Particle1Jet) skip = ((Particle1Jet)particle).isShipJet();
                 if(!particle.isVisible() && !skip) particles.remove(particle);
             }
         }
         curLevel = time < 60 ? ((int)time / 30) + 1 : ((int)time / 60) + 2;
         soundManager.poll((int)(tc.getTickMillis() - lastFrame));
     }
 
     private void drawBg() {
         // store the current model matrix
 	glPushMatrix();
 
 	// bind to the appropriate texture for this sprite
         bg.bind();
 
 	// translate to the right location and prepare to draw
 	glTranslatef(0, 0, 0);
     	glColor3f(1, 1, 1);
 
 	// draw a quad textured to match the sprite
     	glBegin(GL_QUADS);
             glTexCoord2f(0, 0); glVertex2f(0, 0);
 	    glTexCoord2f(0, bg.getHeight()); glVertex2f(0, DISPLAY_HEIGHT);
 	    glTexCoord2f(bg.getWidth(), bg.getHeight()); glVertex2f(DISPLAY_WIDTH, DISPLAY_HEIGHT);
 	    glTexCoord2f(bg.getWidth(), 0); glVertex2f(DISPLAY_WIDTH, 0);
 	glEnd();
 
 	// restore the model view matrix to prevent contamination
 	glPopMatrix();
     }
 
     private void drawTitleFade() {
         // store the current model matrix
 	glPushMatrix();
 
         // disable textures
         glDisable(GL_TEXTURE_2D);
 
 	// translate to the right location and prepare to draw
 	glTranslatef(0, 0, 0);
     	glColor4f(0, 0, 0, 1F - titleFade);
 
 	// draw a quad textured to match the sprite
     	glBegin(GL_QUADS);
             glTexCoord2f(0, 0); glVertex2f(0, 0);
 	    glTexCoord2f(0, 1); glVertex2f(0, DISPLAY_HEIGHT);
 	    glTexCoord2f(1, 1); glVertex2f(DISPLAY_WIDTH, DISPLAY_HEIGHT);
 	    glTexCoord2f(1, 0); glVertex2f(DISPLAY_WIDTH, 0);
 	glEnd();
 
         // enable textures
         glEnable(GL_TEXTURE_2D);
 
 	// restore the model view matrix to prevent contamination
 	glPopMatrix();
     }
 
     private void processPause() {
         // store the current model matrix
 	glPushMatrix();
 
 	// disable textures
         glDisable(GL_TEXTURE_2D);
 
 	// translate to the right location and prepare to draw
 	glTranslatef(0, 0, 0);
     	glColor4f(0, 0, 0, 0.8F);
 
 	// draw a quad textured to match the sprite
     	glBegin(GL_QUADS);
             glTexCoord2f(0, 0); glVertex2f(0, 0);
 	    glTexCoord2f(0, 1); glVertex2f(0, DISPLAY_HEIGHT);
 	    glTexCoord2f(1, 1); glVertex2f(DISPLAY_WIDTH, DISPLAY_HEIGHT);
 	    glTexCoord2f(1, 0); glVertex2f(DISPLAY_WIDTH, 0);
 	glEnd();
 
         // enable textures
         glEnable(GL_TEXTURE_2D);
         
         // mouse processing
         Shape mouse = new Rectangle(Mouse.getX(), DISPLAY_HEIGHT - Mouse.getY(), 1, 1);
 
         // draw text
         batmfa60.drawString((DISPLAY_WIDTH - batmfa60.getWidth("PAUSED")) / 2, 25, "PAUSED", Color.red);
         Pause pause = null;
         if(pauseScreen == 0) {
             int iCount = 0;
             for(int i = 0; i < pauseMenu.size(); i++) {
                 pause = pauseMenu.get(i);
                 pause.setTextY(120 + (30 * i));
                 if(mouse.intersects(pause.getBounds())) {
                     iCount++;
                     if(!pauseHover.equals(pause.getText())) {
                         soundManager.playSoundEffect("ui.button.rollover", false);
                         pauseHover = pause.getText();
                     }
                     if(Mouse.isButtonDown(0) && !mouseClicked) {
                         soundManager.playSoundEffect("ui.button.click", false);
                         pauseScreen = i + 1;
                         if(pause instanceof Pause2Resume) ((Pause2Resume)pause).setLastMouse(lastMouseX, lastMouseY);
                         pause.setMouseClicked(true);
                         pause.setActive(true);
                         pause.renderScreen();
                         break;
                     }
                     pause.setColor(Color.red.addToCopy(new Color(0, 50, 50)));
                     pause.render();
                 }
                 else {
                     pause.setColor(Color.red);
                     pause.render();
                 }
             }
             if(iCount <= 0) pauseHover = "";
         }
         else {
             pause = pauseMenu.get(pauseScreen - 1);
             if(!pause.isActive()) {
                 pauseScreen = 0;
                 mouseClicked = true;
             }
             else pause.renderScreen();
         }
         if(!Mouse.isButtonDown(0) && mouseClicked) mouseClicked = false;
 
 	// restore the model view matrix to prevent contamination
 	glPopMatrix();
     }
 
     private void generateTitleAsteroids() {
         DisplayMode dm = Display.getDisplayMode(); Sprite sprite = null, tsprite = null;
         int count = (dm.getWidth() * dm.getHeight()) / 20000; int texnum = 0;
         for(int i = 0; i < count; i++) {
             texnum = random.nextInt(5);
             sprite = new Sprite6TitleAsteroid(sprites, particles, soundManager, random.nextInt(DISPLAY_WIDTH), random.nextInt(DISPLAY_HEIGHT), this, atex[texnum], texnum);
             tryagain: if(true) {
                 sprite = new Sprite6TitleAsteroid(sprites, particles, soundManager, random.nextInt(DISPLAY_WIDTH), random.nextInt(DISPLAY_HEIGHT), this, atex[texnum], texnum);
                 for(int j = 0; j < titleSprites.size(); j++) {
                     tsprite = titleSprites.get(j);
                     if(sprite.getBounds().intersects(tsprite.getBounds())) break tryagain;
                 }
                 titleSprites.add(sprite);
             }
         }
     }
 
     private void generateAsteroid() {
         if(tc.getTickMillis() - lastAsteroid >= nextRand) {
             int texnum = random.nextInt(5);
             Sprite2Asteroid newSprite = new Sprite2Asteroid(sprites, particles, soundManager, DISPLAY_WIDTH + 64, random.nextInt(DISPLAY_HEIGHT), this, atex[texnum], texnum);
             sprites.add(newSprite); asteroids.add(newSprite);
             lastAsteroid = tc.getTickMillis();
             nextRand = random.nextInt(MathHelper.clamp(10000 / DIFFICULTY, 1, 10000)) / curLevel;
         }
     }
 
     private void generatePowerup() {
         if(tc.getTickMillis() - lastPowerup >= nextPowerupRand) {
             if(!firstPowerup) {
                 PowerupSprite newSprite = new Sprite4Powerup(this, sprites, particles, soundManager, DISPLAY_WIDTH + 32, random.nextInt(DISPLAY_HEIGHT), Powerup.ALL_POWERUPS[random.nextInt(Powerup.TOTAL_POWERUPS)]);
                 sprites.add(newSprite);
                 powerups.add(newSprite);
             }
             else firstPowerup = false;
             lastPowerup = tc.getTickMillis();
             nextPowerupRand = random.nextInt(MathHelper.clamp(500000 / DIFFICULTY, 1, 500000));
         }
     }
 
     private void processCollision(Sprite sprite) {
         if(sprite instanceof PowerupSprite || (sprite instanceof Sprite2Asteroid && !asteroidCollision)) return;
         next: for(int i = 0; i < asteroids.size(); i++) {
             Sprite2Asteroid asteroid = asteroids.get(i);
             Shape b1 = sprite.getBounds(); Shape b2 = asteroid.getBounds();
             if(new Vector2f(b1.getCenterX(), b1.getCenterY()).distance(new Vector2f(b2.getCenterX(), b2.getCenterY())) > (float)(Math.max(b1.getWidth(), b1.getHeight()) + Math.max(b2.getWidth(), b2.getHeight())) / 2) continue next;
             if(sprite instanceof Sprite0Ship) {
                 if(!((Sprite0Ship)sprite).isHit() && !((Sprite0Ship)sprite).isInvincible() && b1.intersects(b2)) {
                     deaths++;
                     this.decScore(DIFFICULTY * 2);
                     ((Sprite0Ship)sprite).hit();
                 }
             }
             else if(sprite instanceof Sprite1Gunfire) {
                 if(sprite.isVisible()) {
                     if(b1.intersects(b2)) {
                         asteroid.hit(((Sprite1Gunfire)sprite).isBig() ? 5 : 1);
                         sprite.setVisible(false);
                     }
                 }
             }
             else if(sprite instanceof Sprite2Asteroid && asteroidCollision) {
                 if(sprite.isVisible()) {
                     if(!sprite.equals(asteroid) && b1.intersects(b2)) {
                         if(fps < 50) {
                             astColl = false; astCollTime = tc.getTickMillis();
                             continue next;
                         }
                         
                         if(sprite.getY() > asteroid.getY()) {
                             ((Sprite2Asteroid)sprite).setYDirection(((Sprite2Asteroid)sprite).getSpeed());
                             asteroid.setYDirection(-asteroid.getSpeed());
                         }
                         else if(sprite.getY() < asteroid.getY()) {
                             ((Sprite2Asteroid)sprite).setYDirection(-((Sprite2Asteroid)sprite).getSpeed());
                             asteroid.setYDirection(asteroid.getSpeed());
                         }
                         else {
                             boolean rand = random.nextBoolean();
                             ((Sprite2Asteroid)sprite).setYDirection(rand ? ((Sprite2Asteroid)sprite).getSpeed() : -((Sprite2Asteroid)sprite).getSpeed());
                             asteroid.setYDirection(!rand ? asteroid.getSpeed() : -asteroid.getSpeed());
                         }
                     }
                 }
             }
             else if(sprite instanceof WeaponSprite) {
                 if(sprite.isVisible()) {
                     if(b1.intersects(b2)) {
                         asteroid.hit(((WeaponSprite)sprite).getDamage());
                         ((WeaponSprite)sprite).impact();
                     }
                 }
             }
         }
         next: for(int i = 0; i < powerups.size() && sprite instanceof Sprite0Ship; i++) {
             PowerupSprite powerup = powerups.get(i);
             Shape b1 = sprite.getBounds(); Shape b2 = powerup.getBounds();
             if(new Vector2f(b1.getCenterX(), b1.getCenterY()).distance(new Vector2f(b2.getCenterX(), b2.getCenterY())) > (float)(Math.max(b1.getWidth(), b1.getHeight()) + Math.max(b2.getWidth(), b2.getHeight())) / 2) continue next;
             if(sprite instanceof Sprite0Ship) {
                 if(!((Sprite0Ship)sprite).isHit() && !((Sprite0Ship)sprite).isRespawning() && b1.intersects(b2)) {
                     ((Sprite0Ship)sprite).addPowerup(powerup.getPowerupType());
                     powerup.setVisible(false);
                     soundManager.playSoundEffect("ship.powerup", false);
                 }
             }
         }
     }
 
     public int getScore() {
         return score;
     }
 
     public void setScore(int score) {
         this.score = score;
         if(this.score > this.highScore) this.highScore = this.score;
     }
 
     public void incScore(int i) {
         score += i;
         if(this.score > this.highScore) this.highScore = this.score;
     }
 
     public void decScore(int i) {
         score -= i;
         if(score < 0) score = 0;
         if(this.score > this.highScore) this.highScore = this.score;
     }
 
     public long getHighScore() {
         return highScore;
     }
 
     public int getDeaths() {
         return deaths;
     }
 
     public void setDeaths(int deaths) {
         this.deaths = deaths;
     }
 
     private static ByteBuffer createIcon(URL url) throws IOException {
         InputStream is = url.openStream();
         try {
             PNGDecoder decoder = new PNGDecoder(is);
             ByteBuffer bb = ByteBuffer.allocateDirect(decoder.getWidth() * decoder.getHeight() * 4);
             decoder.decode(bb, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
             bb.flip();
             return bb;
         } finally {
             is.close();
         }
     }
 
     public TickCounter getTickCounter() {
         return this.tc;
     }
 
     public int getDifficulty() {
         return DIFFICULTY;
     }
 
     public void setDifficulty(int difficulty) {
         DIFFICULTY = difficulty;
         nextRand = random.nextInt(MathHelper.clamp(10000 / DIFFICULTY, 1, 10000)) / curLevel;
         nextPowerupRand = random.nextInt(MathHelper.clamp(500000 / DIFFICULTY, 1, 500000));
     }
 
     public void changeDisplayMode(DisplayMode mode, boolean fullscreen) {
         try {
             if(fullscreen) {
                 Display.setDisplayMode(mode);
                 Display.setFullscreen(true);
             }
             else {
                 Display.setFullscreen(false);
                 Display.setDisplayMode(mode);
             }
             DISPLAY_WIDTH = Display.getDisplayMode().getWidth();
             DISPLAY_HEIGHT = Display.getDisplayMode().getHeight();
             FULLSCREEN = fullscreen;
             resizeGL();
             if(onTitle) {
                 titleSprites.clear();
                 this.generateTitleAsteroids();
             }
         }
         catch(Exception e) {
             e.printStackTrace();
         }
     }
 
     public void clearScreen() {
         Sprite sprite; PowerupSprite powerup;
         for(int i = 0; i < sprites.size(); i++) {
             sprite = sprites.get(i);
             if(sprite instanceof Sprite2Asteroid || sprite instanceof PowerupSprite) sprite.setVisible(false);
         }
     }
 
     private void processTitle() {
         Shape mouse = new Rectangle(Mouse.getX(), DISPLAY_HEIGHT - Mouse.getY(), 1, 1);
         if(titleFade < 1) titleFade = MathHelper.clamp(titleFade + 0.01F, 0, 1);
 
         Sprite sprite = null;
         for(int i = 0; i < titleSprites.size(); i++) {
             sprite = titleSprites.get(i);
             if(sprite.isVisible()) {
                 sprite.update();
                 sprite.render();
             }
         }
         batmfa37.drawString((DISPLAY_WIDTH - batmfa37.getWidth("Junk from Outer Space")) / 2, 25, "Junk from Outer Space", Color.green);
         batmfa37.drawString((DISPLAY_WIDTH - batmfa37.getWidth("High Score: " + highScore)) / 2, (DISPLAY_HEIGHT - batmfa37.getHeight("High Score: " + highScore)) - 10, "High Score: " + highScore, Color.cyan);
 
         Title title = null;
         if(titleScreen == 0) {
             int iCount = 0;
             for(int i = 0; i < titleMenu.size(); i++) {
                 title = titleMenu.get(i);
                 title.setTextY(120 + (30 * i));
                 if(mouse.intersects(title.getBounds()) && titleFade >= 1) {
                     iCount++;
                     if(!titleHover.equals(title.getText())) {
                         soundManager.playSoundEffect("ui.button.rollover", false);
                         titleHover = title.getText();
                     }
                     if(Mouse.isButtonDown(0) && !mouseClicked) {
                         soundManager.playSoundEffect("ui.button.click", false);
                         titleScreen = i + 1;
                         title.setMouseClicked(true);
                         title.setActive(true);
                         title.renderScreen();
                         break;
                     }
                     title.setColor(Color.red.addToCopy(new Color(0, 50, 50)));
                     title.render();
                 }
                 else {
                     title.setColor(Color.red);
                     title.render();
                 }
             }
             if(iCount <= 0) titleHover = "";
         }
         else {
             title = titleMenu.get(titleScreen - 1);
             if(!title.isActive()) {
                 titleScreen = 0;
                 mouseClicked = true;
             }
             else title.renderScreen();
         }
         if(!Mouse.isButtonDown(0) && mouseClicked) mouseClicked = false;
     }
 
     private void processCountdown() {
         startCountdown = 4 - (int)(startTc.getTickMillis() / 1000);
         if(startCountdown < prevStartCountdown && startCountdown >= 0) soundManager.playSoundEffect("ship.powerup", false, startCountdown == 0 ? 1.5F : 1.0F);
         prevStartCountdown = startCountdown; startTc.incTicks();
 
         if(startCountdown < 0) {
             runGame = true;
             return;
         }
         if(startCountdown < 4) batmfa100.drawString((DISPLAY_WIDTH - batmfa100.getWidth(startCountdown == 0 ? "GO!" : Integer.toString(startCountdown))) / 2, (DISPLAY_HEIGHT - batmfa100.getHeight(startCountdown == 0 ? "GO!" : Integer.toString(startCountdown))) / 2, startCountdown == 0 ? "GO!" : Integer.toString(startCountdown), Color.green);
     }
 
     public boolean isOnTitle() {
         return this.onTitle;
     }
 
     public void setOnTitle(boolean onTitle) {
         this.onTitle = onTitle;
         Mouse.setGrabbed(!onTitle);
         if(onTitle) {
             ConfigManager.setProperty("high-score", this.highScore);
             soundManager.playMusic("title", true);
             titleSprites.clear(); runGame = false;
             this.generateTitleAsteroids();
         }
         else {
             soundManager.stopMusic(); runGame = false; startTc.setTicks(0);
             sprites.clear(); asteroids.clear(); powerups.clear(); particles.clear();
             this.setScore(0); this.setDeaths(0); tc.setTicks(0); prevStartCountdown = 0;
             nextRand = random.nextInt(MathHelper.clamp(10000 / DIFFICULTY, 1, 10000)) / curLevel;
             nextPowerupRand = random.nextInt(MathHelper.clamp(500000 / DIFFICULTY, 1, 500000));
             sprites.add(new Sprite0Ship(sprites, particles, 48, DISPLAY_HEIGHT / 2, soundManager, this));
         }
     }
 }
