 package Ne0nx3r0.SignShop;
 
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 import java.io.File;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.HashMap;
 import org.bukkit.util.config.Configuration;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 //permissions
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import org.bukkit.plugin.Plugin;
 
 import com.iConomy.*;
 
 public class SignShop extends JavaPlugin{
     private final SignShopPlayerListener playerListener = new SignShopPlayerListener(this);
     private final SignShopBlockListener blockListener = new SignShopBlockListener(this);
 
     private Logger Logger;
 
 //Configurables
     private int AUTO_SAVE_ID = -1;
 
 //Statics
     public static Storage Storage;
     public static HashMap<String,List> Operations;
     public static HashMap<String,HashMap<String,String>> Messages;
     public static HashMap<String,String> Errors;
 
 //iConomy
     public static iConomy iConomy = null;
 
 //Permissions
     public static PermissionHandler permissionHandler;
     public boolean USE_PERMISSIONS = false;
 
 //Logging
     public void log(String message, Level level,int tag){
         Logger.log(level,"[SignShop] ["+tag+"] " + message);
     }
     //seemed simpler to just have two of them.
     public void log(String message, Level level){
         Logger.log(level,"[SignShop] " + message);
     }
 
     public void onEnable(){
// Enable logger
         Logger = Logger.getLogger("Minecraft");
         
// Register events
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Low, this);
         pm.registerEvent(Event.Type.BLOCK_BURN, blockListener, Priority.Low, this);
 
// Ensure the plugin directory exists
        if(!this.getDataFolder().exists()){
            this.getDataFolder().mkdir();
        }

// Load the config file
         File fConfig = new File(this.getDataFolder(),"config.yml");
         if(!fConfig.isFile()){
             try{
                 File jarloc = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getCanonicalFile();
                 if(jarloc.isFile()){
                     JarFile jar = new JarFile(jarloc);
                     JarEntry entry = jar.getJarEntry("config.yml");
                     
                     if(entry != null && !entry.isDirectory()){
                         InputStream in = jar.getInputStream(entry);
                         FileOutputStream out = new FileOutputStream(fConfig);
                         byte[] tempbytes = new byte[512];
                         int readbytes = in.read(tempbytes,0,512);
                         while(readbytes>-1){
                             out.write(tempbytes,0,readbytes);
                             readbytes = in.read(tempbytes,0,512);
                         }
                         out.close();
                         in.close();
 
                         log("Created default config.yml", Level.INFO);
                     }   
                 }
             }catch(Exception e){
                 log("[SignShop] Unable to create default config.yml:" + e, Level.INFO);
             }
         }
 
         Configuration config = new Configuration(fConfig);
         config.load();
 
 //Setup messages
         this.Messages = (HashMap<String,HashMap<String,String>>) config.getProperty("messages");
 
 //Setup errors
         this.Errors = (HashMap<String,String>) config.getProperty("errors");
 
 //Create a storage locker for shops
         this.Storage = new Storage(new File(this.getDataFolder(),"sellers.yml"),this);
 
 //Setup scheduled saves
         int iAutoSaveInterval = config.getInt("autosave_minutes",0);
         if(iAutoSaveInterval > 0){
             iAutoSaveInterval = iAutoSaveInterval*60*20; //convert to minutes
 
             AUTO_SAVE_ID = getServer().getScheduler().scheduleSyncRepeatingTask(
                 this,autosaveCircuits,iAutoSaveInterval,iAutoSaveInterval);
         }
 
 //Setup sign types
         this.Operations = new HashMap<String,List>();
 
         HashMap<String,Integer> validSignOperations = new HashMap<String,Integer>();
 
         validSignOperations.put("takePlayerMoney",1);
         validSignOperations.put("givePlayerMoney",2);
         validSignOperations.put("takePlayerItems",3);
         validSignOperations.put("givePlayerItems",4);
         validSignOperations.put("takeOwnerMoney",5);
         validSignOperations.put("giveOwnerMoney",6);
         validSignOperations.put("takeShopItems",7);
         validSignOperations.put("giveShopItems",8);
         validSignOperations.put("givePlayerRandomItem",10);
         validSignOperations.put("playerIsOp",11);
         validSignOperations.put("setDayTime",12);
         validSignOperations.put("setNightTime",13);
         validSignOperations.put("setRaining",14);
         validSignOperations.put("setClearSkies",16);
         validSignOperations.put("setRedstoneOn",17);
         validSignOperations.put("setRedstoneOff",18);
         validSignOperations.put("setRedStoneOnTemp",19);
         validSignOperations.put("toggleRedstone",20);
         validSignOperations.put("usesChest",21);
         validSignOperations.put("usesLever",22);
         validSignOperations.put("healPlayer",23);
         validSignOperations.put("repairPlayerHeldItem",24);
 
         HashMap<String,String> tempSignOperations = (HashMap<String,String>) config.getProperty("signs");
 
         List tempSignOperationString = new ArrayList();
         List tempSignOperationInt;
         for(String sKey : tempSignOperations.keySet()){
             tempSignOperationString = Arrays.asList(tempSignOperations.get(sKey).split("\\,"));
             tempSignOperationInt = new ArrayList();
 
             for(int i=0;i<tempSignOperationString.size();i++){
                 if(validSignOperations.containsKey((String) tempSignOperationString.get(i))){
                     tempSignOperationInt.add(validSignOperations.get((String) tempSignOperationString.get(i)));
                 }
             }
 
             if(tempSignOperationString.contains("takePlayerItems")
             || tempSignOperationString.contains("givePlayerItems")
             || tempSignOperationString.contains("takeShopItems")
             || tempSignOperationString.contains("giveShopItems")
             || tempSignOperationString.contains("givePlayerRandomItem")){
 
                 tempSignOperationInt.add(validSignOperations.get("usesChest"));
 
             }else if(tempSignOperationString.contains("setRedstoneOn")
             || tempSignOperationString.contains("setRedstoneOff")
             || tempSignOperationString.contains("setRedStoneOnTemp")
             || tempSignOperationString.contains("toggleRedstone")){
 
                 tempSignOperationInt.add(validSignOperations.get("usesLever"));
 
             }
 
             this.Operations.put(sKey, tempSignOperationInt);
         }
 
 //iConomy
         pm.registerEvent(Event.Type.PLUGIN_ENABLE, new server(this), Priority.Monitor, this);
         pm.registerEvent(Event.Type.PLUGIN_DISABLE, new server(this), Priority.Monitor, this);
 
 //Permissions
         setupPermissions();
 
 //Spit out enabled msg
         PluginDescriptionFile pdfFile = this.getDescription();
         log("v" + pdfFile.getVersion() + " enabled", Level.INFO);
 
     }
 
     public void onDisable(){
         this.Storage.Save();
 
         getServer().getScheduler().cancelTask(AUTO_SAVE_ID);
 
         log("disabled", Level.INFO);
     }
 
     private void setupPermissions() {
         Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
 
         if(this.permissionHandler == null){
             if(permissionsPlugin != null){
                 this.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
                 this.USE_PERMISSIONS = true;
                 log("Hooked into Permissions", Level.INFO);
             }
             else{
                 log("Permissions not found, defaulting to OP.", Level.INFO);
             }
         }
     }
 
     //Scheduled save mechanism
     private Runnable autosaveCircuits = new Runnable(){
         public void run(){
             Storage.Save();
         }
     };
 }
