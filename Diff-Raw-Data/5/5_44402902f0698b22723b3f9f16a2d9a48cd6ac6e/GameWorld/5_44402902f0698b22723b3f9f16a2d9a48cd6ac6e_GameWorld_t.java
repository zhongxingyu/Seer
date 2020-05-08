 package org.oskar.world;
 
 import org.apache.log4j.*;
 import org.apache.log4j.xml.DOMConfigurator;
 import org.oskar.modules.file.FileSystem;
 import org.oskar.modules.rendering.RenderingSystem;
 import org.oskar.modules.resources.ResourceSystem;
 import org.oskar.modules.window.WindowingSystem;
 import org.w3c.dom.DOMConfiguration;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * @author Oskar Veerhoek
  */
 public class GameWorld {
 
     private WindowingSystem windowingSystem = new WindowingSystem();
     private RenderingSystem renderingSystem = new RenderingSystem();
     private FileSystem fileSystem = new FileSystem();
     private ResourceSystem resourceSystem = new ResourceSystem();
     private Properties properties = new Properties();
     private Map<String, String> stringProperties = new HashMap<String, String>();
     private Map<String, Integer> integerProperties = new HashMap<String, Integer>();
     private boolean isCreated = false;
     private AtomicBoolean flaggedForDestruction = new AtomicBoolean(false);
 
     public void setFlaggedForDestruction(boolean value) {
         this.flaggedForDestruction.set(value);
     }
 
     public boolean getFlaggedForDestruction() {
         return flaggedForDestruction.get();
     }
 
     /**
      * Sets the "created" state to true.
      */
     public GameWorld() {
         isCreated = true;
     }
 
     /**
      * Destroys all the modules that the game world uses.
      */
     public void destroy() {
         info(GameWorld.class, "Destroying game world");
         renderingSystem.destroy();
         windowingSystem.destroy();
         resourceSystem.destroy();
         fileSystem.destroy();
         isCreated = false;
     }
 
     /**
      * Prints out a debug log.
      * @param sender the class from which the log is sent
      * @param log the contents of the log
      */
     public void debug(Class sender, String log) {
         Logger.getLogger(sender).debug(log);
     }
 
     /**
      * Prints out an info log.
      * @param sender the class from which the log is sent
      * @param log the contents of the log
      */
     public void info(Class sender, String log) {
         Logger.getLogger(sender).info(log);
     }
 
     /**
      * Prints out a warning log.
      * @param sender the class from which the log is sent
      * @param log the contents of the log
      */
     public void warn(Class sender, String log) {
         Logger.getLogger(sender).warn(log);
     }
 
     /**
      * Prints out a fatal exception and destroys the game world.
      * @param sender the class from which the log is sent
      * @param e the exception that occurred
      */
     public void fatal(Class sender, Exception e) {
         Logger.getLogger(sender).fatal("", e);
         destroy();
     }
 
     /**
      * Prints out a fatal exception with a log and destroys the game world.
      * @param sender the class from which the log was sent
      * @param log the contents of the log
      * @param e the exception that occurred
      */
     public void fatal(Class sender, String log, Exception e) {
         Logger.getLogger(sender).fatal(log, e);
         destroy();
     }
 
     /**
      * Prints out a fatal log and destroys the game world.
      * @param sender the class from which the log was sent
      * @param log the contents of the log
      */
     public void fatal(Class sender, String log) {
         Logger.getLogger(sender).fatal(log);
         destroy();
     }
 
     /**
      * Prints out an exception.
      * @param sender the class from which the log is sent
      * @param e the exception that occurred
      */
     public void error(Class sender, Exception e) {
         Logger.getLogger(sender).error("", e);
     }
 
     /**
      * Prints out an exception with a log.
      * @param sender the class from which the log is sent
      * @param log the contents of the log
      * @param e the exception that occurred
      */
     public void error(Class sender, String log, Exception e) {
         Logger.getLogger(sender).error(log, e);
     }
 
     /**
      * Prints out an error log.
      * @param sender the class from which the log is sent
      * @param log the contents of the log
      */
     public void error(Class sender, String log) {
         Logger.getLogger(sender).error(log);
     }
 
     /**
     * Sets the appropriate properties and initializes the following modules:
      * - File System
      * - Resource System
      * - Windowing System
      * - Rendering System
      */
     public void create() {
         info(GameWorld.class, "Creating game world");
         debug(GameWorld.class, "Setting properties");
         setProperty("WINDOW_TITLE", "Core OpenGL - Java w/ LWJGL");
         setProperty("WINDOW_WIDTH", 640);
         setProperty("WINDOW_HEIGHT", 480);
         setProperty("RESOURCE_VERTEX_SHADER", "res/shader.vs");
         setProperty("RESOURCE_FRAGMENT_SHADER", "res/shader.fs");
         fileSystem.create(this);
         resourceSystem.create(this);
         windowingSystem.create(this);
         renderingSystem.create(this);
     }
 
     public void setProperty(String key, String value) {
         debug(GameWorld.class, "Setting " + key + " to " + value);
         stringProperties.put(key, value);
     }
 
     public void setProperty(String key, Integer value) {
         debug(GameWorld.class, "Setting " + key + " to " + value);
         integerProperties.put(key, value);
     }
 
     public Integer getIntegerProperty(String key) {
         if (!integerProperties.containsKey(key)) {
             error(GameWorld.class, "Key " + key + " does not exist.");
             return null;
         } else {
             return integerProperties.get(key);
         }
     }
 
     public String getStringProperty(String key) {
         if (!stringProperties.containsKey(key)) {
             error(GameWorld.class, "Key " + key + " does not exist.");
             return null;
         } else {
             return stringProperties.get(key);
         }
     }
 
     public boolean isCreated() {
         return isCreated;
     }
 
     public RenderingSystem getRenderingSystem() {
         return renderingSystem;
     }
 
     public FileSystem getFileSystem() {
         return fileSystem;
     }
 
     public ResourceSystem getResourceSystem() {
         return resourceSystem;
     }
 
     public void run() {
         while (!flaggedForDestruction.get()) {
             renderingSystem.update();
             windowingSystem.update();
         }
     }
 }
