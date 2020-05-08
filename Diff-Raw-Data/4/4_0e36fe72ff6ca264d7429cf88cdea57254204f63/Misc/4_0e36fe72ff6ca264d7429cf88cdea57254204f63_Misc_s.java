 package net.erbros.HoldGuest;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.util.Vector;
 
 class Misc {
     HoldGuest plugin;
     
     FileConfiguration config;
     FileConfiguration msgConfig;
     FileConfiguration locationConfig;
     File configFile;
     File msgConfigFile;
     File locationConfigFile;
     
 
     protected Misc (HoldGuest plugin) {
         this.plugin = plugin;
         this.configFile = new File(plugin.getDataFolder(), "config.yml");
         this.msgConfigFile = new File(plugin.getDataFolder(), "msgConfig.yml");
         this.locationConfigFile = new File(plugin.getDataFolder(), "location.yml");
         
         // Perhaps this is the first run? Try.
         try {
             firstRun();
         } catch (Exception e) {
             e.printStackTrace();
         }
         // And load the configs
         config = new YamlConfiguration();
         msgConfig = new YamlConfiguration();
         locationConfig = new YamlConfiguration();
         loadConfig();
         
     }
     
 
     protected void loadConfig() {
         
         loadYamls();
 
         // Aaand, lets get the vars
         plugin.holdradius = config.getDouble("radius_block", 30);
         plugin.cacheage = config.getDouble("check_moving_sec", 1.5);
         
         
         plugin.x = locationConfig.getDouble("spawnhold.x", 0);
         plugin.y = locationConfig.getDouble("spawnhold.y", 0);
         plugin.z = locationConfig.getDouble("spawnhold.z", 0);
         
         if(isVector(plugin.x,plugin.y,plugin.z)) {
             plugin.vector = getVector(plugin.x,plugin.y,plugin.z);
         }
         
         // Is the plugin configured?
         if(plugin.x == 0 || plugin.y == 0 || plugin.z == 0) {
             plugin.log.info( customMessage("holdguestheader"));
             plugin.log.info("The plugin isn't configured and will not work correcly");
             plugin.log.info("until an admin have set the zone center.");
             plugin.active = false;
         }
         List<World> worlds = plugin.getServer().getWorlds();
         boolean exist = false;
         for(World w : worlds) {
             if(w.getName().equalsIgnoreCase(locationConfig.getString("spawnhold.world", "world"))) {
                 exist = true;
                 plugin.world = w;
             }
         }
         
         if(exist == false) {
             plugin.log.info( customMessage("holdguestheader"));
             plugin.log.info("The plugin isn't configured and will not work correcly");
             plugin.log.info("until an admin have set the zone center.");
             plugin.active = false;
         }
         
         // Let's save all the configs, so we are sure they are being made.
         saveYamls();
         
     }
     
     protected String customMessage (String mid) {
         if (mid.equalsIgnoreCase("keepinside")) 
         {
             return msgConfig.getString("keepinside", "Read the rules and then ask for permission.");
         } 
         else if( mid.equalsIgnoreCase("holdguestheader")) 
         {
             return msgConfig.getString("holdguestheader", "--- HoldGuest ---");
         } 
         else if( mid.equalsIgnoreCase("centerhelp")) 
         {
             return msgConfig.getString("centerhelp", "/holdguest center - Set center.");
         } 
         else if( mid.equalsIgnoreCase("radiushelp")) 
         {
             return msgConfig.getString("radiushelp", "/holdguest radius <blocks> - Set radius.");
         } 
         else if( mid.equalsIgnoreCase("centerset")) 
         {
             return msgConfig.getString("centerset", "Center have now been set.");
         } 
         else if( mid.equalsIgnoreCase("radiusset")) 
         {
             return msgConfig.getString("radiusset", "Radius have now been set.");
         } 
         else if( mid.equalsIgnoreCase("noconsole")) 
         {
             return msgConfig.getString("noconsole", "You can't do that trough the console.");
         } 
         else if( mid.equalsIgnoreCase("reloaded")) 
         {
             return msgConfig.getString("reloaded", "The configuration was reloaded.");
         }
         return "Failed to find reply";
     }
     
     protected void setCenter (Player p) {
         Location l = p.getLocation();
         plugin.x = l.getX();
         plugin.y = l.getY();
         plugin.z = l.getZ();
         plugin.world = l.getWorld();
         plugin.vector = new Vector(plugin.x, plugin.y, plugin.z);
         
         locationConfig.set("spawnhold.x", plugin.x);
         locationConfig.set("spawnhold.y", plugin.y);
         locationConfig.set("spawnhold.z", plugin.z);
        locationConfig.set("world",plugin.world.getName());
         saveYamls();
     }
 
     
     
     protected Vector getMinimumVector (Vector vec, double distance, World world) {
         Location loc = vec.toLocation(world);
         loc = loc.add(distance, distance, distance);
         vec = loc.toVector();
         return vec;
     }
 
     protected Vector getMaximumVector (Vector vec, double distance, World world) {
         Location loc = vec.toLocation(world);
         loc = loc.add(distance, distance, distance);
         vec = loc.toVector();
         return vec;
     }
     
     protected Vector getVector (double x, double y, double z) {
         Vector vec = new Vector();
         
         vec.setX(x);
         vec.setY(y);
         vec.setZ(z);
         
         return vec;
     }
     
     protected boolean isVector (double x, double y, double z) {
         if(x != 0 && y != 0 && z != 0) {
             return true;
         }
         return false;
     }
     
     protected void setRadius (double radius) {
         config.set("holdradius", radius);
         plugin.holdradius = radius;
         plugin.saveConfig();
     }
     
     private void firstRun() throws Exception {
         if(!configFile.exists()){
             configFile.getParentFile().mkdirs();
             copy(plugin.getResource("config.yml"), configFile);
         }
         if(!msgConfigFile.exists()){
             msgConfigFile.getParentFile().mkdirs();
             copy(plugin.getResource("msgConfig.yml"), msgConfigFile);
         }
         if(!locationConfigFile.exists()){
             locationConfigFile.getParentFile().mkdirs();
             copy(plugin.getResource("location.yml"), locationConfigFile);
         }
     }
     
     private void copy(InputStream in, File file) {
         try {
             OutputStream out = new FileOutputStream(file);
             byte[] buf = new byte[1024];
             int len;
             while((len=in.read(buf))>0){
                 out.write(buf,0,len);
             }
             out.close();
             in.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     protected void saveYamls() {
         try {
             config.save(configFile);
             msgConfig.save(msgConfigFile);
             locationConfig.save(locationConfigFile);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
     protected void loadYamls() {
         try {
             config.load(configFile);
             msgConfig.load(msgConfigFile);
             locationConfig.load(locationConfigFile);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
