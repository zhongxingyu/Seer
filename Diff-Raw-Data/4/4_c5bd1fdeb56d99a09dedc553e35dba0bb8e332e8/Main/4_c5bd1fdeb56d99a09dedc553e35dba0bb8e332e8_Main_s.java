 package edgruberman.bukkit.fixchunkholes;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import edgruberman.bukkit.messagemanager.MessageLevel;
 import edgruberman.bukkit.messagemanager.MessageManager;
 
 public final class Main extends JavaPlugin {
     
     /**
      * Prefix for all permissions used in this plugin.
      */
     private static final String PERMISSION_PREFIX = "fixchunkholes";
     
     static ConfigurationFile configurationFile;
     static MessageManager messageManager;
     
     private static int radiusDefault;
     private static int radiusMaximum;
     private static int frequency;
     
     private Map<Player, Long> lastRefresh = new HashMap<Player, Long>();
     
     public void onLoad() {
         Main.messageManager = new MessageManager(this);
         Main.messageManager.log("Version " + this.getDescription().getVersion());
         
         Main.configurationFile = new ConfigurationFile(this);
     }
 	
     public void onEnable() {
         Main.radiusDefault = this.getConfiguration().getInt("radius.default", Main.radiusDefault);
         Main.messageManager.log("Default Radius: " + Main.radiusDefault, MessageLevel.CONFIG);
         
         Main.radiusMaximum = this.getConfiguration().getInt("radius.maximum", Main.radiusMaximum);
         Main.messageManager.log("Maximum Radius: " + Main.radiusMaximum, MessageLevel.CONFIG);
         
         Main.frequency = this.getConfiguration().getInt("frequency", Main.frequency);
         Main.messageManager.log("Frequency (seconds): " + Main.frequency, MessageLevel.CONFIG);
         
         new TeleportMonitor(this);
         
         Main.messageManager.log("Plugin Enabled");
     }
     
     public void onDisable() {
         this.lastRefresh.clear();
         
         Main.messageManager.log("Plugin Disabled");
     }
     
     public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
         Main.messageManager.log(
                 ((sender instanceof Player) ? ((Player) sender).getName() : "[CONSOLE]")
                     + " issued command: " + label + " " + Main.join(args)
                 , MessageLevel.FINE
         );
         
         if (!sender.hasPermission(PERMISSION_PREFIX + "." + label)) {
             Main.messageManager.respond(sender, "You do not have permission to use this command.", MessageLevel.RIGHTS);
             return true;
         }
         
         if (!(sender instanceof Player)) {
             Main.messageManager.respond(sender, "You must be a player to use this command.", MessageLevel.RIGHTS);
             return true;
         }
         
         Player player = (Player) sender;
         Long last = this.lastRefresh.get(player);
         if (last == null) last = 0L;
         if (!player.hasPermission(PERMISSION_PREFIX + "." + label + ".override.frequency")
                 && (System.currentTimeMillis() - last) < (Main.frequency * 1000)) {
             Main.messageManager.respond(sender, "You can only use /" + label + " once every " + Main.frequency + " seconds.", MessageLevel.RIGHTS);
             return true;
         }
         
         this.lastRefresh.put(player, System.currentTimeMillis());
         
         Integer radius = Main.radiusDefault;
         if (args.length != 0 && Main.isInteger(args[0])) {
             radius = Integer.parseInt(args[0]);
             
             if (!player.hasPermission(PERMISSION_PREFIX + "." + label + ".override.radius")
                     && radius > Main.radiusMaximum) {
                 Main.messageManager.respond(sender, "Radius \"" + radius + "\" too large; Maximum allowed is " + Main.radiusMaximum + ".", MessageLevel.SEVERE);
                 return true;
             }
         }
         
         int refreshed = 0;
         
         // Refresh centered at chunk player is standing in.
         refreshed += Main.refreshChunk(player.getLocation().getBlock(), radius);
         
         // Check if player is pointing at a chunk to refresh.
         // Do not refresh chunk player is pointing at if already refreshed because they were standing in it.
        Block block = player.getTargetBlock(null, 100);
         if (block != null && !block.getChunk().equals(player.getLocation().getBlock().getChunk())) 
             refreshed += Main.refreshChunk(block, radius);
         
         Main.messageManager.respond(sender, "Refreshed " + refreshed + " chunk" + (refreshed == 1 ? "" : "s") + ".", MessageLevel.STATUS);
         
         return true;
     }
     
     static int refreshChunk(final Block block) {
         return Main.refreshChunk(block, Main.radiusDefault);
     }
     
     private static int refreshChunk(final Block block, final Integer radius) {
         int refreshed = 0;
         
         if (block == null || block.getChunk() == null) return refreshed;
         
         // Refresh chunk at center of request first.
         Main.refreshChunk(block.getWorld(), block.getChunk().getX(), block.getChunk().getZ());
         refreshed++; 
         
         if (radius <= 0) return refreshed;
         
         // Refresh chunks spirally outward.
         for ( int i = 1; i <= radius; i++ ) {
             // north row, west to east
             for ( int dZ = i; dZ >= -i; dZ-- ) {
                 Main.refreshChunk(block.getWorld(), block.getChunk().getX() - i, block.getChunk().getZ() + dZ);
                 refreshed++;
             }
             
             // east row, north to south
             for ( int dX = -i + 1; dX <= i; dX++ ) {
                 Main.refreshChunk(block.getWorld(), block.getChunk().getX() + dX, block.getChunk().getZ() - i);
                 refreshed++;
             }
             
             // south row, east to west
             for ( int dZ = -i + 1; dZ <= i; dZ++ ) {
                 Main.refreshChunk(block.getWorld(), block.getChunk().getX() + i, block.getChunk().getZ() + dZ);
                 refreshed++;
             }
             
             // west row, south to north
             for ( int dX = i - 1; dX >= -i + 1; dX-- ) {
                 Main.refreshChunk(block.getWorld(), block.getChunk().getX() + dX, block.getChunk().getZ() + i);
                 refreshed++;
             }
         }
         
         return refreshed;
     }
     
     private static void refreshChunk(final World world, final int chunkX, final int chunkZ) {
         Main.messageManager.log("Refreshing chunk in \"" + world.getName() + "\" at cX:" + chunkX + " cZ:" + chunkZ, MessageLevel.FINE);
         world.refreshChunk(chunkX, chunkZ);
     }
     
     private static boolean isInteger(final String s) {   
         try {   
             Integer.parseInt(s);   
             return true;   
         }   
         catch(Exception e) {   
             return false;   
         }   
     }
     
     /**
      * Concatenate all string elements of an array together with a space.
      * 
      * @param s string array
      * @return concatenated elements
      */
     private static String join(final String[] s) {
         return join(Arrays.asList(s), " ");
     }
     
     /**
      * Combine all the elements of a list together with a delimiter between each.
      * 
      * @param list list of elements to join
      * @param delim delimiter to place between each element
      * @return string combined with all elements and delimiters
      */
     private static String join(final List<String> list, final String delim) {
         if (list == null || list.isEmpty()) return "";
      
         StringBuilder sb = new StringBuilder();
         for (String s : list) sb.append(s + delim);
         sb.delete(sb.length() - delim.length(), sb.length());
         
         return sb.toString();
     }
 }
