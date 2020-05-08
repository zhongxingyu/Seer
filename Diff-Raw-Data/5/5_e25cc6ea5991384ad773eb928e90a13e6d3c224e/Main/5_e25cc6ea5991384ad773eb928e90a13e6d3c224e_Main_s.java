 package main;
 
 import jinngine.collision.SAP2;
 import jinngine.geometry.Box;
 import jinngine.math.Vector3;
 import jinngine.physics.Body;
 import jinngine.physics.ContactTrigger;
 import jinngine.physics.DefaultDeactivationPolicy;
 import jinngine.physics.DefaultScene;
 import jinngine.physics.constraint.contact.ContactConstraint;
 import jinngine.physics.force.GravityForce;
 import jinngine.physics.solver.NonsmoothNonlinearConjugateGradient;
 import models.*;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.PixelFormat;
 import org.lwjgl.util.glu.GLU;
 import org.lwjgl.util.vector.Vector3f;
 import org.newdawn.slick.opengl.Texture;
 
 import javax.swing.*;
 import java.util.ArrayList;
 
 import static main.Settings.*;
 import static main.Utilities.*;
 import static org.lwjgl.opengl.GL11.*;
 
 // TODO: Save achievements of user to xml/scores.xml on each ending of the game (ESC, finish, end), the example is given
 
 public class Main {
     private static Main main;
 
     private MainCamera camera;
     private Terrain terrain;
     private UserObject user;
     private Crosshair crosshair;
     private jinngine.physics.Scene scene;
     private jinngine.physics.Body userBody;
 
     private ArrayList<House> houses = new ArrayList<House>();
     private ArrayList<Zombie> liveZombies = new ArrayList<Zombie>();
     private ArrayList<DeadZombie> deadZombies = new ArrayList<DeadZombie>();
     private Texture zombieTexture;
     private Texture bombTexture;
 
     // HUD
     private ArrayList<Char> zombiesHUDKilled = new ArrayList<Char>();
     private ArrayList<Char> zombiesHUDEscaped = new ArrayList<Char>();
     private Texture[] numbersTexture;
 
     // Bombs
     private ArrayList<Bomb> bombs = new ArrayList<Bomb>();
     private long bombTimer = System.currentTimeMillis();
 
     // Variables
     private long zombieIncreaseTimer = System.currentTimeMillis();
     private int zombieID = 0;
     private int numberOfZombiesEscaped = 0;
     private int numberOfZombiesKilled = 0;
     private int numberOfZombiesAtOnce = 1;
     private float lengthOfCity;
     private int specialWeaponNumber = 0;
     private int zombieIncreaseIntervalSkill = 1;
     private boolean reset = false;
     private boolean fullScreen = true;
 
     public static void main(String[] args) {
         main = new Main();
         main.startLoop();
     }
 
     private void startLoop() {
 
         if (!reset) {
             if (JOptionPane.showConfirmDialog(null, "Would You Like To Run In Fullscreen Mode?",
                     "Start Fullscreen?", JOptionPane.YES_NO_OPTION) == 1) {
                 fullScreen = false;
             }
         }
 
         String[] options = new String[] {"Easy", "Medium", "Hard"};
         int skill = JOptionPane.showOptionDialog(null, "Select your skills.", "Skills",
                     JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[2]);
 
         if (skill == 0) {
             zombieIncreaseIntervalSkill = zombieIncreaseInterval;
         } else if (skill == 1) {
             zombieIncreaseIntervalSkill = zombieIncreaseInterval / 2;
         } else if (skill == 2){
             zombieIncreaseIntervalSkill = zombieIncreaseInterval / 3;
         } else {
             zombieIncreaseIntervalSkill = zombieIncreaseInterval;
         }
 
         try {
             if (!CreateGLWindow(windowTitle, windowWidth, windowHeight, fullScreen)) {
                 // Quit If Window Was Not Created
                 throw new Exception();
             }
 
             initializeObjects();
             //hide the mouse
             Mouse.setGrabbed(true);
 
             long FPSSync = System.currentTimeMillis();
             while (!Keyboard.isKeyDown(exitKey) && !Display.isCloseRequested() && Display.isActive()) {
 
                 if (numberOfZombiesEscaped > 100) {
                     resetGame();
                 }
 
                 if (System.currentTimeMillis() - FPSSync < 10) {
                     continue;
                 }
 
                 FPSSync = System.currentTimeMillis();
 
                 resetDisplay();
                 applyPhysics();
                 render();
                 processInput();
 
                 Display.update();
             }
 
            updateScore("test", numberOfZombiesKilled, numberOfZombiesEscaped);
 
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             Display.destroy();
         }
         System.exit(0);
     }
 
     private boolean CreateGLWindow(String windowTitle, int windowWidth, int windowHeight, boolean fullScreen) throws LWJGLException {
         DisplayMode bestMode = null;
         for (DisplayMode d: Display.getAvailableDisplayModes()) {
             if (d.getWidth() == windowWidth && d.getHeight() == windowHeight && d.getFrequency() <= 85) {
                 if (bestMode == null || (d.getBitsPerPixel() >= bestMode.getBitsPerPixel() && d.getFrequency() > bestMode.getFrequency())) bestMode = d;
             }
         }
 
         Display.setDisplayMode(bestMode);
         Display.create(new PixelFormat(8, 8, 8, 4));
         Display.setFullscreen(fullScreen);
         Display.setTitle(windowTitle + " " + version);
         ReSizeGLScene(windowWidth, windowHeight);
         // Enable Smooth Shading
         glShadeModel(GL_SMOOTH);
         // Don't render hidden faces
         glEnable(GL_CULL_FACE);
         // Depth Buffer Setup
         glClearDepth(1f);
         // Enables Depth Testing
         glEnable(GL_DEPTH_TEST);
         // The Type Of Depth Testing To Do
         glDepthFunc(GL_LEQUAL);
         // Really Nice Perspective Calculations
         glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
         return true;
     }
 
     private void ReSizeGLScene(int windowWidth, int windowHeight) {
         if (windowHeight == 0) {
             windowHeight = 1;
         }
         // Reset The Current Viewport
         glViewport(0, 0, windowWidth, windowHeight);
 
         // Select The Projection Matrix
         glMatrixMode(GL_PROJECTION);
         // Reset The Projection Matrix
         glLoadIdentity();
 
         // Calculate The Aspect Ratio Of The Window
         GLU.gluPerspective(45f, windowWidth / (float) windowHeight, 0.1f, 60f);
 
         // Select The Modelview Matrix
         glMatrixMode(GL_MODELVIEW);
         // Reset The Modelview Matrix
         glLoadIdentity();
     }
 
     private void addZombie() {
         if (System.currentTimeMillis() - zombieIncreaseTimer >= zombieIncreaseIntervalSkill) {
             numberOfZombiesAtOnce += zombieIncreaseSizeInterval;
             zombieIncreaseTimer = System.currentTimeMillis();
         }
 
         if (liveZombies.size() < numberOfZombiesAtOnce) {
             zombieID = (zombieID == Integer.MAX_VALUE) ? 0 : zombieID + 1;
             Zombie zombie = new Zombie(
                     new Body("Zombie" + zombieID, new Box(zombieSize * 2, zombieSize * 2, zombieSize * 2)),zombieTexture);
 
             zombie.scale(zombieSize, zombieSize, zombieSize);
             zombie.getBody().setPosition(new Vector3(
                     (float) Math.random() * mainRoadWidth,
                     0,
                     user.getPosition().z - 30 - Math.random() * 40
             ));
 
             scene.addForce(new GravityForce(zombie.getBody()));
             scene.addBody(zombie.getBody());
 
             liveZombies.add(zombie);
         }
 
         for(Zombie z: liveZombies) {
             z.getBody().setPosition(new Vector3(
                     z.getBody().getPosition().x,
                     z.getBody().getPosition().y,
                     z.getBody().getPosition().z + zombieObjectSpeed
             ));
         }
         removeUnseenZombies();
     }
 
     private void removeUnseenZombies(){
         for (int i = 0; i < liveZombies.size(); i++) {
             if (liveZombies.get(i).getBody().getPosition().z > user.getPosition().z + 20) {
                 scene.removeBody(liveZombies.get(i).getBody());
                 liveZombies.remove(i--);
                 numberOfZombiesEscaped++;
                 addZombieHUDEscaped();
             }
         }
     }
 
     private void resetDisplay() {
         glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
         glClearColor(0, 1, 1, 1);
     }
 
     private void render() {
         camera.render3D();
         terrain.render3D();
         user.render3D();
         crosshair.render3D();
 
         for (Bomb bomb : bombs) {
             bomb.render3D();
         }
         for(House house: houses) {
             house.render3D();
         }
         for(Zombie zombie: liveZombies) {
             zombie.render3D();
         }
         for(DeadZombie zombie: deadZombies) {
             zombie.render3D();
         }
         for(Char c: zombiesHUDEscaped) {
             c.render3D();
         }
         for(Char c: zombiesHUDKilled) {
             c.render3D();
         }
 
         addZombie();
     }
 
     private void applyPhysics() {
         try {
             scene.tick();
             user.setPosition(
                     (float) userBody.getPosition().x,
                     (float) userBody.getPosition().y,
                     (float) userBody.getPosition().z
             );
 
             for (Bomb bomb : bombs) {
                 bomb.setPosition(
                         (float) bomb.getBody().getPosition().x,
                         (float) bomb.getBody().getPosition().y,
                         (float) bomb.getBody().getPosition().z
                 );
             }
 
             for (Zombie zombie : liveZombies) {
                 zombie.setPosition(
                         (float) zombie.getBody().getPosition().x,
                         (float) zombie.getBody().getPosition().y,
                         (float) zombie.getBody().getPosition().z
                 );
             }
         } catch (Exception e) {
             System.err.println("ConcurrentModificationException");
         }
     }
 
     private void initializeObjects() {
         scene = new DefaultScene(new SAP2(), new NonsmoothNonlinearConjugateGradient(44), new DefaultDeactivationPolicy());
         scene.setTimestep(0.01);
 
         // Load textures
         zombieTexture = loadTextures("RGTI_zombies/textures/zombie.png");
         bombTexture = loadTextures("RGTI_zombies/textures/bomb.png");
         numbersTexture = new Texture[10];
         for(int i = 0; i <= 9; i++) {
             numbersTexture[i] = loadTextures("RGTI_zombies/textures/numbers/"+i+".png");
         }
         // Create houses
         lengthOfCity = initializeHouses();
 
         // Create terrain
         terrain = new Terrain(loadTextures("RGTI_zombies/textures/background.png"));
         terrain.scale(mainRoadWidth / 2, 0, lengthOfCity / 2);
         terrain.translate(mainRoadWidth / 2, -0.5f, lengthOfCity / 2);
 
         // Create user
         user = new UserObject(loadTextures("RGTI_zombies/textures/user.png"));
         user.scale(userSize[0], userSize[1], userSize[2]);
         user.translate(mainRoadWidth / 2f, 1f, -10f);
 
         // Create crosshair
         crosshair = new Crosshair(3f);
         crosshair.scale(0.5f, 0.5f, 0.5f);
         crosshair.setRotation(new Vector3f(90f, 0, 0));
         crosshair.translate(mainRoadWidth / 2f, -0.5f, -10.5f);
 
         // Create camera
         camera = new MainCamera();
         camera.translate(-user.getPosition().x, -user.getPosition().y - 4f, -user.getPosition().z - 23f);
         addToScene();
 
         // Create HUDs
         addZombieHUDKilled();
         addZombieHUDEscaped();
     }
 
     private void addZombieHUDKilled() {
         // Start left HUD, display number of zombies killed
         zombiesHUDKilled.clear();
         float position = 0.5f;
         for(char numberChar: Integer.toString(numberOfZombiesKilled).toCharArray()) {
             Char newChar = new Char(numbersTexture[numberChar-48]);
             zombiesHUDKilled.add(newChar);
             newChar.scale(0.3f, 0.5f, 0.3f);
             newChar.setPosition(position, (float) userBody.getPosition().y, (float) userBody.getPosition().z + 2);
             position += 0.5;
         }
     }
 
     private void addZombieHUDEscaped() {
         // Start right HUD, display number of zombies escaped
         zombiesHUDEscaped.clear();
         String string = Integer.toString(numberOfZombiesEscaped);
         float position = -string.length() * 0.5f;
         for(char numberChar: string.toCharArray()) {
             Char newChar = new Char(numbersTexture[numberChar-48]);
             zombiesHUDEscaped.add(newChar);
             newChar.scale(0.3f, 0.5f, 0.3f);
             newChar.setPosition(position + mainRoadWidth, (float) userBody.getPosition().y, (float) userBody.getPosition().z + 2);
             position += 0.5;
         }
     }
 
     private float initializeHouses() {
         float[] positionLeft = {-minHouseWidth, 0, 0},
                 positionRight = {mainRoadWidth + minHouseWidth, 0, 0};
         float[] width;
         House house;
         Texture houseTexture = loadTextures("RGTI_zombies/textures/houseWall.png");
 
         for (int i = 0; i < 200; i++) {
             // LEFT HOUSE
             house = new House(new float[]{(float) Math.random(), (float) Math.random(), (float) Math.random()}, houseTexture);
             width = new float[]{
                     minHouseWidth,
                     (float) Math.random() * houseHeightBounds + minimalHouseHeight,
                     (float) Math.random() * houseLengthBounds + minimalHouseLength
             };
             // Position house
             positionLeft[2] -= width[2];
             house.scale(width[0], width[1], width[2]);
             house.translate(positionLeft[0], width[1] - 2, positionLeft[2]);
             houses.add(house);
             positionLeft[2] -= width[2] + spaceBetweenHouses;
 
             // RIGHT HOUSE
             house = new House(new float[]{(float) Math.random(), (float) Math.random(), (float) Math.random()}, houseTexture);
             width = new float[]{
                     minHouseWidth,
                     (float) Math.random() * houseHeightBounds + minimalHouseHeight,
                     (float) Math.random() * houseLengthBounds + minimalHouseLength
             };
             // Position house
             positionRight[2] -= width[2];
             house.scale(width[0], width[1], width[2]);
             house.translate(positionRight[0], width[1] - 2, positionRight[2]);
             houses.add(house);
             positionRight[2] -= width[2] + spaceBetweenHouses;
         }
         return Math.min(positionLeft[2], positionRight[2]);
     }
 
     private void addToScene() {
         // Add bodies to scene for collision detection
         Body floorBody = new Body("floor", new Box(1000, 5, 10000));
         floorBody.setPosition(new Vector3(0, -3, 0));
         floorBody.setFixed(true);
 
         userBody = new Body("userBody", new Box(0.5f, 0.5f, 0.5f));
         userBody.setPosition(new Vector3(mainRoadWidth / 2f, 5f, -10f));
         userBody.setFixed(true);
 
         Body leftHousesBody = new Body("leftHouses", new Box(minHouseWidth, minimalHouseHeight, -lengthOfCity));
         leftHousesBody.setPosition(new Vector3(-minHouseWidth + 0.6, 0, 0));
         leftHousesBody.setFixed(true);
 
         Body rightHousesBody = new Body("rightHouses", new Box(minHouseWidth, minimalHouseHeight, -lengthOfCity));
         rightHousesBody.setPosition(new Vector3(mainRoadWidth + minHouseWidth - 0.6, 0, 0));
         rightHousesBody.setFixed(true);
 
         scene.addBody(floorBody);
         scene.addBody(userBody);
         scene.addBody(leftHousesBody);
         scene.addBody(rightHousesBody);
     }
 
     protected void processInput() {
         user.rotate(0, 1f, 0);
         if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) && (user.getPosition().x > user.getScale().y || godMode)) {
             user.translate(-userObjectSpeed, 0, 0);
             crosshair.translate(-userObjectSpeed, 0, 0);
             userBody.setPosition(new Vector3(user.getPosition().x, user.getPosition().y, user.getPosition().z));
             camera.translate(userObjectSpeed, 0, 0);
 
             // Is the user is far enough from both buildings not to collide
             if(user.getPosition().x > user.getScale().x && user.getPosition().x < mainRoadWidth - user.getScale().x) {
                 user.setRotation(new Vector3f(user.getRotation().x, user.getRotation().y, 0));
             } else {
                 user.setRotation(new Vector3f(0, 0, 90));
             }
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) && (user.getPosition().x < mainRoadWidth - user.getScale().y || godMode)) {
             user.translate(userObjectSpeed, 0, 0);
             crosshair.translate(userObjectSpeed, 0, 0);
             userBody.setPosition(new Vector3(user.getPosition().x, user.getPosition().y, user.getPosition().z));
             camera.translate(-userObjectSpeed, 0, 0);
 
             // Is the user is far enough from both buildings not to collide
             if(user.getPosition().x < mainRoadWidth - user.getScale().x && user.getPosition().x > user.getScale().x) {
                 user.setRotation(new Vector3f(user.getRotation().x, user.getRotation().y, 0));
             } else {
                 user.setRotation(new Vector3f(0, 0, 90));
             }
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
             user.translate(0, 0, -userObjectSpeed);
             crosshair.translate(0, 0, -userObjectSpeed);
             userBody.setPosition(new Vector3(user.getPosition().x, user.getPosition().y, user.getPosition().z));
             camera.translate(0, 0, userObjectSpeed);
 
             if (user.getPosition().z <= lengthOfCity + 30) {
                 float distanceBetweenCameraAndUser = user.getPosition().z + camera.getPosition().z;
                 user.setPosition(user.getPosition().x,user.getPosition().y,0f);
                 camera.setPosition(camera.getPosition().x,camera.getPosition().y,0f);
                 user.translate(0, 0, -10f);
                 camera.translate(0f, 0f, -user.getPosition().z + distanceBetweenCameraAndUser);
                 userBody.setPosition(user.getPosition().x,user.getPosition().y,user.getPosition().z);
                 liveZombies.clear();
             }
 
             // Move HUDs
             for(Char c: zombiesHUDEscaped) {
                 c.translate(0, 0, -userObjectSpeed);
             }
             for(Char c: zombiesHUDKilled) {
                 c.translate(0, 0, -userObjectSpeed);
             }
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) && (user.getPosition().z < -10)) {
             user.translate(0, 0, userObjectSpeed);
             crosshair.translate(0, 0, userObjectSpeed);
             userBody.setPosition(new Vector3(user.getPosition().x, user.getPosition().y, user.getPosition().z));
             camera.translate(0, 0, -userObjectSpeed);
 
             // Move HUDs
             for(Char c: zombiesHUDEscaped) {
                 c.translate(0, 0, userObjectSpeed);
             }
             for(Char c: zombiesHUDKilled) {
                 c.translate(0, 0, userObjectSpeed);
             }
         }
 
 
         if (Keyboard.isKeyDown(Keyboard.KEY_X) && !Keyboard.isKeyDown(Keyboard.KEY_Z) && user.getPosition().y <= houseHeightBounds) {
             userBody.setPosition(new Vector3(user.getPosition().x, user.getPosition().y + 0.05, user.getPosition().z));
             camera.translate(0, -0.05f, -0.1f);
 
             // Move HUDs
             addZombieHUDEscaped();
             addZombieHUDKilled();
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_Z) && user.getPosition().y >= 1.0f && !Keyboard.isKeyDown(Keyboard.KEY_X)) {
             userBody.setPosition(new Vector3(user.getPosition().x, user.getPosition().y - 0.05, user.getPosition().z));
             camera.translate(0, 0.05f, 0.1f);
 
             // Move HUDs
             addZombieHUDEscaped();
             addZombieHUDKilled();
         }
 
         // Special weapon 1, NUKE
         if(Keyboard.isKeyDown(Keyboard.KEY_N) && maxNukes > 0) {
             specialWeaponNumber = 1;
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
             if (bombs.size() > maxBombs) {
                 scene.removeBody(bombs.get(0).getBody());
                 bombs.remove(0);
             }
 
             if ((System.currentTimeMillis() - bombTimer) <= 1000 / maxBombThrowingSpeed) {
                 return;
             }
 
             bombTimer = System.currentTimeMillis();
 
             float bombSizeTemp = bombSize;
             if(specialWeaponNumber == 1) {
                 bombSizeTemp = nukeSize;
                 maxNukes --;
             }
             specialWeaponNumber = 0;
             float fixedBombSizeBody = bombSizeTemp * 1.2f;
 
             Bomb bomb = new Bomb(new Body("Bomb" + bombs.size(), new Box(fixedBombSizeBody, fixedBombSizeBody, fixedBombSizeBody)), bombTexture);
             bomb.scale(bombSizeTemp, bombSizeTemp, bombSizeTemp);
             bomb.translate(5f, 1f, 0);
 
             scene.addTrigger(new ContactTrigger(bomb.getBody(), 0.000001f, new ContactTrigger.Callback() {
                 @Override
                 public void contactAboveThreshold(jinngine.physics.Body body, ContactConstraint contactConstraint) {
                     for (Zombie z : liveZombies) {
                         if (z.getBody().identifier.equals(body.identifier)) {
                             int currZombieHealth = z.getZombieHealth() - bombMaxPower;
                             if (currZombieHealth == 0) {
                                 scene.removeBody(z.getBody());
 
                                 DeadZombie deadZombie = new DeadZombie();
                                 deadZombie.scale(z.getScale().x * 5, z.getScale().y, z.getScale().z * 5);
                                 deadZombie.setPosition(z.getPosition().x - deadZombie.getScale().x / 2, terrain.getPosition().y + 0.01f, z.getPosition().z);
                                 deadZombies.add(deadZombie);
 
                                 numberOfZombiesKilled++;
                                 addZombieHUDKilled();
                                 liveZombies.remove(z);
                             } else {
                                 z.setZombieHealth(currZombieHealth);
                             }
                             break;
                         }
                     }
                 }
 
                 @Override
                 public void contactBelowThreshold(jinngine.physics.Body body, ContactConstraint contactConstraint) {}
             }));
 
             scene.addForce(new GravityForce(bomb.getBody()));
             scene.addBody(bomb.getBody());
             bomb.getBody().setPosition(new Vector3(
                     userBody.getPosition().x,
                     userBody.getPosition().y - 0.3,
                     userBody.getPosition().z
             ));
             bombs.add(bomb);
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
             resetGame();
         }
 
         // Toggle Fullscreen / Windowed Mode
         if (Keyboard.isKeyDown(changeWindowModeKey)) {
             fullScreen = !fullScreen;
             try {
                 Display.setFullscreen(fullScreen);
             } catch (LWJGLException e) {
                 e.printStackTrace();
             }
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_E)) {}
         if (Keyboard.isKeyDown(Keyboard.KEY_D)) {}
         if (Keyboard.isKeyDown(Keyboard.KEY_W)) {}
         if (Keyboard.isKeyDown(Keyboard.KEY_S)) {}
     }
 
     private void resetGame() {
        updateScore("test", numberOfZombiesKilled, numberOfZombiesEscaped);
         Display.destroy();
         main = new Main();
         main.reset = true;
         main.fullScreen = fullScreen;
         main.startLoop();
     }
 
 }
