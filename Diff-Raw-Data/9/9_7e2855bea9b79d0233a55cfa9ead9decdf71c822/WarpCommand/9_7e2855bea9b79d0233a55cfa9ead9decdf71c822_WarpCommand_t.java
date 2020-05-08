 package to.joe.j2mc.teleport.command;
 
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import to.joe.j2mc.core.command.MasterCommand;
 import to.joe.j2mc.teleport.J2MC_Teleport;
 
 public class WarpCommand extends MasterCommand {
 
     public WarpCommand(J2MC_Teleport plugin) {
         super(plugin);
     }
 
     @Override
     public void exec(CommandSender sender, String commandName, String[] args, Player player, boolean isPlayer) {
         if (isPlayer) {
             if (args.length == 0) {
                 final HashMap<String, Location> warps = ((J2MC_Teleport) this.plugin).getWarps("");
                 if (warps.size() == 0) {
                     player.sendMessage(ChatColor.RED + "No warps available");
                 } else {
                     final StringBuilder warpsList = new StringBuilder();
                     for (final String warpName : warps.keySet()) {
                         warpsList.append(warpName);
                        warpsList.append(", ");
                     }
                    warpsList.setLength(warpsList.length() - 2);
                     player.sendMessage(ChatColor.RED + "Warps: " + ChatColor.WHITE + warpsList);
                     player.sendMessage(ChatColor.RED + "To go to a warp, say /warp warpname");
                 }
             } else {
                 final Location target = ((J2MC_Teleport) this.plugin).getNamedWarp("", args[0]);
                 if ((target != null)) {
                     player.sendMessage(ChatColor.RED + "Welcome to: " + ChatColor.LIGHT_PURPLE + target);
                     this.plugin.getLogger().info(ChatColor.AQUA + "Player " + player.getName() + " went to warp " + target);
                     ((J2MC_Teleport) this.plugin).teleport(player, target);
                 } else {
                     player.sendMessage(ChatColor.RED + "Warp does not exist. For a list, say /warp");
                 }
             }
         }
     }
 }
