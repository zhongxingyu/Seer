 package edgruberman.bukkit.simpletemplate;
 
 import edgruberman.bukkit.messagemanager.MessageManager;
 
 public final class Main extends org.bukkit.plugin.java.JavaPlugin {
     
     /**
      * Prefix for all permissions used in this plugin.
      */
     public static final String PERMISSION_PREFIX = "simpletemplate";
     
     public static MessageManager messageManager;
     
     static ConfigurationFile configurationFile;
     
    @Override
     public void onLoad() {        
         Main.messageManager = new MessageManager(this);
         Main.messageManager.log("Version " + this.getDescription().getVersion());
         
         Main.configurationFile = new ConfigurationFile(this);
         
         // TODO: Add plugin load code here.
     }
 	
    @Override
     public void onEnable() {
         this.loadConfiguration();
         
         // TODO: Add plugin enable code here.
         
         new edgruberman.bukkit.simpletemplate.commands.Name(this);
         
         Main.messageManager.log("Plugin Enabled");
     }
     
    @Override
     public void onDisable() {
         // TODO: Add plugin disable code here.
         
         Main.messageManager.log("Plugin Disabled");
     }
     
     public void loadConfiguration() {
         Main.configurationFile.load();
         
         // TODO: Load configuration settings here.
     }
 }
