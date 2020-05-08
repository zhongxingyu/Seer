 package cc.game.SteampunkZelda;
 
 import cc.game.SteampunkZelda.entities.Giduuf;
 import cc.game.SteampunkZelda.entities.HostileEntity;
 import cc.game.SteampunkZelda.entities.Entity;
 import cc.game.SteampunkZelda.entities.Player;
 import cc.game.SteampunkZelda.entities.pickups.Pickup;
 import cc.game.SteampunkZelda.screen.GameOverScreen;
 import cc.game.SteampunkZelda.screen.SplashScreen;
 import cc.game.SteampunkZelda.screen.gamescreen.GameScreen;
 import cc.game.SteampunkZelda.screen.Screen;
 import org.lwjgl.util.vector.Vector2f;
 import org.newdawn.slick.*;
 
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Random;
 
 /**
  * User: Calv
  * Date: 21/02/13
  * Time: 17:26
  */
 public class SteampunkZelda extends BasicGame implements Observer {
 
     private static Player player;
     private static ArrayList<Entity> entities = new ArrayList<Entity>();
     private static ArrayList<HostileEntity> hostileEntities = new ArrayList<HostileEntity>();
 
     private static final int DEFAULT_WIDTH = 1280;
     private static final int DEFAULT_HEIGHT = 720;
     private static int MAP_WIDTH;
     private static int MAP_HEIGHT;
 
     private static boolean debugMode = true;
     private static boolean paused = false;
     private Screen screen;
     private GameContainer gameContainer;
     private Camera camera;
 
     private float scaleX;
     private float scaleY;
 
     public static void main(String[] args) {
         SteampunkZelda game = new SteampunkZelda();
         try {
             AppGameContainer container = new AppGameContainer(game);
             container.setResizable(true);
             container.setIcon("res/screens/splash.png");
             container.setDisplayMode(DEFAULT_WIDTH, DEFAULT_HEIGHT, false);
             container.start();
         } catch (SlickException e) {
             e.printStackTrace();
         }
     }
 
     public SteampunkZelda() {
         super("Steampunk Zelda - A Lovely Steampunk Zelda Clone");
     }
 
     /**
      * Returns a list of the entities currently displayed ingame.
      * @return ArrayList of entities ingame.
      */
     public static ArrayList<Entity> getEntities() {
         return entities;
     }
 
     @Override
     public void init(GameContainer gameContainer) throws SlickException {
         this.gameContainer = gameContainer;
         if (!debugMode) gameContainer.setShowFPS(false);
         else gameContainer.setShowFPS(true);
         gameContainer.setTargetFrameRate(60);
         gameContainer.setMaximumLogicUpdateInterval(15);
         gameContainer.setMinimumLogicUpdateInterval(15);
         gameContainer.setAlwaysRender(true);
         this.setScreen(new SplashScreen(this));
     }
 
     /**
      * Sets the current screen of the game.
      * @param screen Screen to be displayed.
      * @throws SlickException
      */
     public void setScreen(Screen screen) throws SlickException {
         if (this.screen != null) {
             this.screen.onStop();
         }
         this.screen = screen;
         if (screen != null)
             screen.onStart();
     }
 
     /**
      * Can only be activated from the character select screen, it will initialise the entities after the player is chosen.
      * @param player Player to be setup into the game.
      */
     public void setupInitialEntities(Player player) {
         addToEntityLists(player);
         Random rnd = new Random();
         int limit = rnd.nextInt(10);
         for (int iter = 0; iter<limit; iter++) {
             addToEntityLists(new Giduuf(this, (int) (DEFAULT_WIDTH * Math.random()), (int) (DEFAULT_HEIGHT * Math.random()), 16, 16));
         }
         limit = rnd.nextInt(10);
         for (int iter = 0; iter<limit; iter++) {
             addToEntityLists(new Pickup(this,(int) (DEFAULT_WIDTH * Math.random()), (int) (DEFAULT_HEIGHT * Math.random()), 16, 16));
         }
     }
 
     /**
      * Adds given entity to the main entity list to be rendered and updated.
      * @param entity Entity to be added to entity list.
      */
     public void addToEntityLists(Entity entity) {
         if (entity instanceof HostileEntity) hostileEntities.add((HostileEntity) entity);
         if (entity instanceof Player) player = (Player) entity;
         entities.add(entity);
     }
 
     @Override
     public void update(GameContainer gameContainer, int deltaTime) throws SlickException {
         if (this.screen != null) this.screen.update(gameContainer, deltaTime);
         if (!paused && (this.screen instanceof GameScreen)) {
             if (this.camera == null) this.camera = new Camera(this, new Vector2f(1280.0F, 720.0F));
             this.screen.update(gameContainer, deltaTime);
             for (int entityIterator = 0; entityIterator < entities.size(); entityIterator++) {
                 Entity entity = entities.get(entityIterator);
                 if (entity != null) {
                     entity.update(gameContainer, deltaTime);
                 }
             }
             if (player.isDead()) {
                 entities.clear();
                 this.setScreen(new GameOverScreen(this));
             }
         }
         checkKeys();
     }
 
     /**
      * Updates checking the keys required for the main game window.
      */
     private void checkKeys() {
         if (gameContainer.getInput().isKeyPressed(Input.KEY_F1)) debugMode = !debugMode;
         if (gameContainer.getInput().isKeyPressed(Input.KEY_ESCAPE)) paused = !paused;
     }
 
     @Override
     public void render(GameContainer gameContainer, Graphics graphics) throws SlickException {
         graphics.resetTransform();
         gameContainer.setShowFPS(debugMode);
         graphics.setColor(Color.white);
         Font f = graphics.getFont();
 
 //      Start of graphics code to scale the window.
         graphics.pushTransform();
         this.scaleX = (gameContainer.getWidth() * 1F / DEFAULT_WIDTH);
         this.scaleY = (gameContainer.getHeight() * 1F / DEFAULT_HEIGHT);
 
         graphics.setDrawMode(graphics.MODE_NORMAL);
         graphics.scale(this.scaleX, this.scaleY);
 
 //      Translate the graphics to camera.
         if (this.camera != null) {
             graphics.translate(-this.camera.getX(), -this.camera.getY());
         }
 
         if (this.screen != null && !(this.screen instanceof GameOverScreen)) this.screen.render(gameContainer);
 
         for (Entity entity : entities) {
             entity.render();
             if (debugMode) {
                 if (entity instanceof Player) graphics.setColor(Color.orange);
                 else graphics.setColor(Color.white);
                 entity.renderRect(graphics);
             }
         }
 
         if (this.screen instanceof GameOverScreen) this.screen.render(gameContainer);
 
         if (paused) {
             graphics.setColor(Color.white);
             String message = "Game Paused";
             graphics.drawString(message, (SteampunkZelda.getWidth() / 2) - (f.getWidth(message) / 2), (SteampunkZelda.getHeight() / 2) - ((f.getHeight(message) / 2) + 100));
         }
 
 //        End of graphics by camera.
         graphics.popTransform();

        if (debugMode && this.screen instanceof GameScreen) renderDebugInfo(gameContainer, graphics);
     }
 
     private void renderDebugInfo(GameContainer gameContainer, Graphics graphics) {
 //            Mouse code
         graphics.setColor(Color.white);
         String mouseX = "Mouse X: " + (gameContainer.getInput().getMouseX() + camera.getX());
         graphics.drawString(mouseX, 10, 25);
         String mouseY = "Mouse Y: " + (gameContainer.getInput().getMouseY() + camera.getY());
         graphics.drawString(mouseY, 10, 40);
 //            Ram code
         Runtime runtime = Runtime.getRuntime();
         double ramMB = Math.pow(1024, 2);
         int rounding = 1000;
         int maxRam = (int) (Math.round((runtime.maxMemory() / ramMB) * rounding) / rounding);
         int freeRam = (int) (Math.round((runtime.freeMemory() / ramMB) * rounding) / rounding);
         int totalRam = (int) (Math.round((runtime.totalMemory() / ramMB) * rounding) / rounding);
         String ram = "Ram Max: " + maxRam + " Free: " + freeRam + " Total: " + totalRam;
         graphics.drawString(ram, 10, 55);
 //            Health code
         String playerHealth = "Health: " + this.getPlayer().getHealth();
        graphics.drawString(playerHealth, this.getWindowSize().getX() * this.scaleX - (graphics.getFont().getWidth(playerHealth) + 10), 10);
     }
 
     @Override
     public void update(Observable o, Object arg) {
 
     }
 
     /**
      * Gives the main game container
      * @return GameContainer Game container being used.
      */
     public GameContainer getGameContainer() {
         return gameContainer;
     }
 
     /**
      * Returns the height of the window.
      * @return int Height of the window.
      */
     public static int getHeight() {
         return DEFAULT_HEIGHT;
     }
 
     /**
      * Returns the width of the window.
      * @return int width of the window.
      */
     public static int getWidth() {
         return DEFAULT_WIDTH;
     }
 
     /**
      * Returns the player being used in the game
      * @return Player The player currently setup in the game.
      */
     public Player getPlayer() {
         return player;
     }
 
     /**
      * Returns the pause value of the game.
      * @return boolean Whether the game is paused or not.
      */
     public static boolean isPaused() {
         return paused;
     }
 
     /**
      * Returns the size of the map as a vector.
      * @return Vector2f Sizes as (width, height)
      */
     public Vector2f getMapSize() {
         return this.screen.getMapSize();
     }
 
     /**
      * Returns the window size as a vector.
      * @return Vector2f Sizes as (width, height)
      */
     public Vector2f getWindowSize() {
         return new Vector2f(DEFAULT_WIDTH, DEFAULT_HEIGHT);
     }
 
     /**
      * Returns whether the game is in debug mode or not.
      * @return boolean Debug variable.
      */
     public boolean getDebug() {
         return debugMode;
     }
 
 
     public float getScaleX() {
         return this.scaleX;
     }
 
     public float getScaleY() {
         return this.scaleY;
     }
 }
