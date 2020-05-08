 
 package agaricus.plugins.IncompatiblePlugin;
 
 import org.bukkit.Location;
 import org.bukkit.World;
import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.v1_5_R3.CraftWorld;
 import org.bukkit.entity.Player;
 
 /**
  * Handler for the /pos sample command.
  * @author SpaceManiac
  */
 public class SamplePosCommand implements CommandExecutor {
     public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
         if (!(sender instanceof Player)) {
             return false;
         }
         Player player = (Player) sender;
 
         World world = player.getWorld();
         Location location = player.getLocation();
         player.sendMessage("getting CraftWorld");
         CraftWorld craftWorld = (CraftWorld)world;
         player.sendMessage("getting handle");
         net.minecraft.server.v1_5_R3.WorldServer worldServer = craftWorld.getHandle();
         player.sendMessage("calling getTileEntity on nms World");
         // this breaks - Caused by: java.lang.NoSuchMethodError: in.getTileEntity(III)Lany;
         // because WorldServer inherits from World, but isn't in mc-dev to obf mappings (since is added by CB)
         worldServer.getTileEntity(location.getBlockX(), location.getBlockY(), location.getBlockZ());
         player.sendMessage("success");
 
         if (split.length == 0) {
             player.sendMessage("You are currently at " + location.getX() +"," + location.getY() + "," + location.getZ() +
                     " with " + location.getYaw() + " yaw and " + location.getPitch() + " pitch");
             return true;
        } else if (split.length == 1) {
            Block block = location.getBlock();
            player.sendMessage("Block="+block+" isEmpty? "+(block != null && block.isEmpty()));
            return true;
         } else if (split.length == 3) {
             try {
                 double x = Double.parseDouble(split[0]);
                 double y = Double.parseDouble(split[1]);
                 double z = Double.parseDouble(split[2]);
 
                 player.teleport(new Location(player.getWorld(), x, y, z));
             } catch (NumberFormatException ex) {
                 player.sendMessage("Given location is invalid");
             }
             return true;
         } else {
             return false;
         }
     }
 }
