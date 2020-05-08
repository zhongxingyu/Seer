 package fi.haju.haju3d.client;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import com.google.inject.Singleton;
 
 @Singleton
 public class ClientSettings {
   
   private static final String SCREEN_WIDTH_KEY = "screenWidth";
   private static final String SCREEN_HEIGHT_KEY = "screenHeight";
   private static final String CHUNK_RENDER_DISTANCE_KEY = "chunkRenderDistance";
   
   private static final String CONFIG_FILE_NAME = "config.properties";
   
   private final Properties properties = new Properties();
   
   private int screenWidth;
   private int screenHeight;
   private int chunkRenderDistance;
   
   public void init() {
     load();
     screenWidth = loadInt(SCREEN_WIDTH_KEY, 800);
    screenHeight = loadInt(SCREEN_HEIGHT_KEY, 600);
     chunkRenderDistance = loadInt(CHUNK_RENDER_DISTANCE_KEY, 4);
     // Save missing properties as defaults
     save();
   }
 
   private void load() {
     try {
       properties.load(new FileInputStream(CONFIG_FILE_NAME));
     } catch (FileNotFoundException e) {
     } catch (IOException e) {
       throw new RuntimeException(e);
     }
   }
   
   private void save() {
     try {
       properties.store(new FileOutputStream(CONFIG_FILE_NAME), null);
     } catch (IOException e) {
       throw new RuntimeException(e);
     }
   }
 
   public int getScreenWidth() {
     return screenWidth;
   }
 
   public int getScreenHeight() {
     return screenHeight;
   }
   
   public int getChunkRenderDistance() {
     return chunkRenderDistance;
   }
 
   private int loadInt(String key, int defval) {
    if(!properties.containsKey(key)) {
      properties.setProperty(key, Integer.toString(defval));
    }
     return Integer.parseInt(properties.getProperty(key));
   }
   
 }
