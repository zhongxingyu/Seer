 package net.stuffrepos.tactics16;
 
 import net.stuffrepos.tactics16.datamanager.DataManager;
 import net.stuffrepos.tactics16.phase.PhaseManager;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import net.stuffrepos.tactics16.animation.GameImage;
 import net.stuffrepos.tactics16.components.Object2D;
 import net.stuffrepos.tactics16.phase.AbstractPhase;
 import net.stuffrepos.tactics16.phase.Phase;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.TrueTypeFont;
 
 /**
  *
 import tactics16.util.ObjectCursor1D;
  * @author Eduardo H. Bogoni <eduardobogoni@gmail.com>
  */
 public class MyGame {
 
     public static final String DEFAULT_TRUE_TYPE_FAMILY_NAME = "Liberation Mono";
     private static final Dimension DEFAULT_SCREEN_SIZE = new Dimension(800, 600);
     private static MyGame instance;
     AppGameContainer app;
     private GameContainer gameContainer;
     private BasicGame game = new BasicGame("Tactics16") {
 
         @Override
         public void init(GameContainer gc) throws SlickException {
             MyGame.this.initResources(gc);
         }
 
         @Override
         public void update(GameContainer gc, int delta) throws SlickException {
             MyGame.this.update(delta);
         }
 
         public void render(GameContainer gc, Graphics graphics) throws SlickException {            
             MyGame.this.render(gc, graphics);
         }
     };
     private DataManager loader;
     private PhaseManager phaseManager = new PhaseManager();
     private KeyMapping keyMapping = new KeyMapping();
     private TrueTypeFont font;
     //private Font font = new Font("Purisa", Font.PLAIN, 12);
     private Object2D screenObject2D = new Object2D() {
 
         public int getTop() {
             return 0;
         }
 
         public int getLeft() {
             return 0;
         }
 
         public int getWidth() {
             return MyGame.this.getWidth();
         }
 
         public int getHeight() {
             return MyGame.this.getHeight();
         }
     };
 
     public static MyGame getInstance() {
         return instance;
     }
 
     public static void createInstance(String dataPath) {
         if (instance == null) {
             try {
                 instance = new MyGame(dataPath);
             } catch (SlickException ex) {
                 throw new RuntimeException(ex);
             }
         }
     }
     private Phase initialPhase;
 
     private MyGame(String dataPath) throws SlickException {
         loader = new DataManager(new File(dataPath));
         keyMapping.setMapping(GameKey.UP, Input.KEY_UP);
         keyMapping.setMapping(GameKey.DOWN, Input.KEY_DOWN);
         keyMapping.setMapping(GameKey.LEFT, Input.KEY_LEFT);
         keyMapping.setMapping(GameKey.RIGHT, Input.KEY_RIGHT);
         keyMapping.setMapping(GameKey.CONFIRM, Input.KEY_ENTER, Input.KEY_SPACE);
         keyMapping.setMapping(GameKey.CANCEL, Input.KEY_ESCAPE, Input.KEY_BACKSLASH);
         keyMapping.setMapping(GameKey.OPTIONS, Input.KEY_F1);
         keyMapping.setMapping(GameKey.PREVIOUS, Input.KEY_PRIOR);
         keyMapping.setMapping(GameKey.NEXT, Input.KEY_NEXT);
     }
 
     public void initResources(GameContainer gameContainer) {
         this.font = new TrueTypeFont(new Font(DEFAULT_TRUE_TYPE_FAMILY_NAME, Font.PLAIN, 12), true);
         this.gameContainer = gameContainer;
         loader.loadDirectory(loader.getDataDirectory());
     }
 
     public void update(long elapsedTime) {
         try {
             this.phaseManager.getCurrentPhase().update(elapsedTime);
         } catch (Exception ex) {
             ex.printStackTrace();
             this.quit();
         }
     }
 
     public void render(GameContainer gc, Graphics g) {
         if (font != null) {
             g.setFont(font);
         }
 
         g.setColor(org.newdawn.slick.Color.black);
         g.fillRect(0, 0, gc.getWidth(), gc.getHeight());
         try {
             this.phaseManager.getCurrentPhase().render(g);
         } catch (Exception ex) {
             ex.printStackTrace();
             this.quit();
         }                
     }
 
     public void quit() {
        if (app != null) {
            app.destroy();
         }
     }
 
     public DataManager getLoader() {
         return this.loader;
     }
 
     public PhaseManager getPhaseManager() {
         return this.phaseManager;
     }
 
     public boolean isKeyPressed(GameKey key) {
         return keyMapping.isKeyPressed(key);
     }
 
     public KeyMapping getKeyMapping() {
         return keyMapping;
     }
 
     public org.newdawn.slick.Font getFont() {
         return font;
     }
 
     public GameImage getImage(File file) {
         try {
             return new GameImage(new Image(file.getAbsolutePath()));
         } catch (SlickException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     public int getHeight() {
         return gameContainer.getHeight();
     }
 
     public int getWidth() {
         return gameContainer.getWidth();
     }
 
     public void start(Phase initalPhase) throws SlickException {
         this.initialPhase = initalPhase;
         phaseManager.change(new BootstrapPhase());
         AppGameContainer app = new AppGameContainer(game);
 
         app.setDisplayMode(
                 DEFAULT_SCREEN_SIZE.width,
                 DEFAULT_SCREEN_SIZE.height,
                 false);
         app.start();
     }
 
     public Object2D getScreenObject2D() {
         return screenObject2D;
     }
 
     public class KeyMapping {
 
         private Map<GameKey, List<Integer>> mapping = new TreeMap<GameKey, List<Integer>>();
 
         private void setMapping(GameKey gameKey, int... keys) {
             List<Integer> keysList = new ArrayList<Integer>();
             for (int key : keys) {
                 keysList.add(key);
             }
             mapping.put(gameKey, keysList);
         }
 
         private boolean isKeyPressed(GameKey gameKey) {
             for (Integer key : mapping.get(gameKey)) {
                 if (gameContainer.getInput().isKeyPressed(key)) {
                     return true;
                 }
             }
             return false;
 
         }
 
         public Collection<Integer> getKeys(GameKey gameKey) {
             return mapping.get(gameKey);
         }
     }
 
     private class BootstrapPhase extends AbstractPhase {
 
         @Override
         public void render(Graphics g) {
             phaseManager.clear();
             phaseManager.change(initialPhase);
         }
     }
 }
