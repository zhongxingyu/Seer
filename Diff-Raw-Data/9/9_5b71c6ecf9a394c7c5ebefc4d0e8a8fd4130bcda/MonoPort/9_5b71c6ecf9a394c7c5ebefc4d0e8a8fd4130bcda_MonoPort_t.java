 /**
  *
  * @author Reebdoog
  *
  * MonoPort (v.0.5)
  * A Plugin by Reebdoog
  * Compatible with hey0 mod build 117
  * Source code borrowed from the Balls plugin and Grief Alert
  * ArrayList idea from Cuboid
  */
 
 import java.io.IOException;
 
 public class MonoPort extends Plugin {
 
     static final MonoPortListener listener = new MonoPortListener();
 
     public void enable() {
         addCustomCommands();
        System.out.println(listener.getDateTime()+" [INFO] MonoPort v.0.61 plugin enabled");
         if (MonoPortListener.properties == null) {
            MonoPortListener.properties = new PropertiesFile("monoport.properties");
         } else {
             try {
                 MonoPortListener.properties.load();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         listener.checkDataFile();
         if (!listener.loadPortalsIntoArray())
         {
             System.out.println(listener.getDateTime()+" [ERROR] MonoPort plugin : Couldn't load all portals");
         }
     }
 
     public void disable() {
         removeCustomCommands();
         System.out.println(listener.getDateTime()+" [INFO] MonoPort plugin disabled");
     }
 
     public void initialize() {
         //Add Hooks
         etc.getLoader().addListener(PluginLoader.Hook.COMMAND, listener, this, PluginListener.Priority.MEDIUM);
         etc.getLoader().addListener(PluginLoader.Hook.BLOCK_CREATED, listener, this, PluginListener.Priority.MEDIUM);
         etc.getLoader().addListener(PluginLoader.Hook.PLAYER_MOVE, listener, this, PluginListener.Priority.MEDIUM);
     }
 
     public void addCustomCommands(){
         etc.getInstance().addCommand("/setdest", "- Sets a portal Destination");
         etc.getInstance().addCommand("/showportallinks", "- Shows a list of Portal Links");
         etc.getInstance().addCommand("/showdestinations", "- Shows a list of Possible Destinations");
         etc.getInstance().addCommand("/makeportal", "- Makes a portal");
         //etc.getInstance().addCommand("/modifyportal", "- Modifies a portal");
         etc.getInstance().addCommand("/delportal", "- Deletes a portal");
         etc.getInstance().addCommand("/addportaldest", "- Adds another destination to a portal");
         etc.getInstance().addCommand("/remportaldest", "- Removes a destination from a portal");
     }
 
     public void removeCustomCommands(){
         etc.getInstance().removeCommand("/setdest");
         etc.getInstance().removeCommand("/showportallinkst");
         etc.getInstance().removeCommand("/showdestinations");
         etc.getInstance().removeCommand("/makeportal");
         //etc.getInstance().removeCommand("/modifyportal");
         etc.getInstance().removeCommand("/delportal");
         etc.getInstance().removeCommand("/addportaldest");
         etc.getInstance().removeCommand("/remportaldest");
     }
 }
