 package org.train.app;
 
 import java.awt.Dimension;
 import java.awt.DisplayMode;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 
 import org.newdawn.slick.AppGameContainer;
 import org.picocontainer.DefaultPicoContainer;
 import org.picocontainer.PicoContainer;
 import org.picocontainer.behaviors.Caching;
 import org.train.entity.MessageBox;
 import org.train.factory.FontFactory;
 import org.train.helper.LevelHelper;
 import org.train.loader.TranslationLoaderFactory;
 import org.train.other.LevelController;
 import org.train.other.ResourceManager;
 import org.train.other.Translator;
 
 public class App {
 
     private DefaultPicoContainer container;
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         App app = new App();
         app.initContainer();
         PicoContainer container = app.getContainer();
 
         Configuration configuration = container.getComponent(Configuration.class);
 
         boolean isFullscreen = Boolean.parseBoolean(configuration.get("fullscreen"));
         Dimension displaySize = app.getDisplaySize(configuration);
 
         AppGameContainer gameContainer = container.getComponent(AppGameContainer.class);
         gameContainer.setShowFPS(false);
         gameContainer.setTargetFrameRate(60);
 
         try {
             gameContainer.setDisplayMode(displaySize.width, displaySize.height, isFullscreen);
             gameContainer.start();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private Dimension getDisplaySize(Configuration configuration) {
         GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
         GraphicsDevice gd = environment.getDefaultScreenDevice();
         DisplayMode actual = gd.getDisplayMode();
 
         int width = Integer.parseInt(configuration.get("width"));
         int height = Integer.parseInt(configuration.get("height"));
 
         boolean matchFound = false;
         if (width > 0 || height > 0) {
             DisplayMode modes[] = gd.getDisplayModes();
             for (DisplayMode displayMode : modes) {
                 if (displayMode.getWidth() == width && displayMode.getHeight() == height) {
                     matchFound = true;
                     break;
                 }
             }
         }
         if (width <= 0 || height <= 0 || !matchFound) {
             configuration.set("width", String.valueOf(actual.getWidth()));
             configuration.set("height", String.valueOf(actual.getHeight()));
             configuration.saveChanges();
             width = actual.getWidth();
             height = actual.getHeight();
         }
 
         return new Dimension(width, height);
     }
 
     public PicoContainer getContainer() {
         return this.container;
     }
 
     public void initContainer() {
         this.container = new DefaultPicoContainer(new Caching());
 
         this.container.addComponent(Configuration.class);
         Configuration config = container.getComponent(Configuration.class);
 
         String translationsPath = config.get("contentPath") + "translations/";
        this.container.addComponent(new TranslationLoaderFactory(translationsPath));
        this.container.addComponent(new Translator(this.container
                .getComponent(TranslationLoaderFactory.class), config.get("language")));
 
         this.container.addComponent(new Game("Train", this.container));
         this.container.addComponent(AppGameContainer.class);
         this.container.addComponent(MessageBox.class);
         this.container.addComponent(LevelController.class);
         this.container.addComponent(ResourceManager.class);
         this.container.addComponent(FontFactory.class);
         this.container.addComponent(LevelHelper.class);
     }
 }
