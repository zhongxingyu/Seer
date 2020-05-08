 package org.sankozi.rogueland;
 
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
 import com.google.inject.name.Named;
 import java.awt.event.ActionEvent;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import org.apache.log4j.PropertyConfigurator;
 import org.jdesktop.application.Application;
 import org.jdesktop.application.SingleFrameApplication;
 import org.sankozi.rogueland.gui.GuiModule;
 import org.sankozi.rogueland.gui.MainFrame;
 import org.sankozi.rogueland.resources.ResourceProvider;
 
 /**
  * The main class of the application.
  */
 public class RoguelandApplication extends SingleFrameApplication {
 
     Module module = new RoguelandModule();
     Module applicationModule = new ApplicationModule();
 	Module guiModule = new GuiModule();
 
     /**
      * At startup create and show the main frame of the application.
      */
     @Override protected void startup() {
         Injector injector = Guice.createInjector(module, applicationModule, guiModule);
         show(injector.getInstance(MainFrame.class));
     }
 
     /**
      * This method is to initialize the specified window by injecting resources.
      * Windows shown in our application come fully initialized from the GUI
      * builder, so this additional configuration is not needed.
      */
     @Override protected void configureWindow(java.awt.Window root) {
     }
 
     /**
      * A convenient static getter for the application instance.
      * @return the instance of RoguelandApplication
      */
     public static RoguelandApplication getApplication() {
         return Application.getInstance(RoguelandApplication.class);
     }
 
     /**
      * Main method launching the application.
      */
     public static void main(String[] args) {
         PropertyConfigurator.configure(ResourceProvider.getLog4jProperties());
         launch(RoguelandApplication.class, args);
     }
 
     private class ApplicationModule extends AbstractModule {
 
         public ApplicationModule() {
         }
 
         @Override
         protected void configure() {
             bind(Application.class).toInstance(RoguelandApplication.this);
         }
 
         @Provides
         @Named("exit")
         public Action exitAction() {
             return new AbstractAction() {
                 {
                     this.putValue(Action.NAME, "Exit");
                 }
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     RoguelandApplication.this.exit(e);
                 }
             };
         }
     }
 }
