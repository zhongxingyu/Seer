 package to.joe.j2mc.info.command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import to.joe.j2mc.core.J2MC_Manager;
 import to.joe.j2mc.core.command.MasterCommand;
 import to.joe.j2mc.info.J2MC_Info;
 
 public class PlayerListCommand extends MasterCommand {
 
     J2MC_Info plugin;
 
     public PlayerListCommand(J2MC_Info Info) {
         super(Info);
         this.plugin = Info;
     }
 
     @Override
     public void exec(CommandSender sender, String commandName, String[] args, Player player, boolean isPlayer) {
         if (!sender.hasPermission("j2mc.core.admin")) {
             int total = 0;
             for(Player derp : plugin.getServer().getOnlinePlayers()){
                 if(!J2MC_Manager.getVisibility().isVanished(derp)){
                     total++;
                 }
             }
             sender.sendMessage(ChatColor.AQUA + "Players (" + total + "/" + plugin.getServer().getMaxPlayers() + "):");
             if (total == 0) {
                 sender.sendMessage(ChatColor.RED + "No one is online :(");
                 return;
             }
             StringBuilder builder = new StringBuilder();
             for (Player pl : plugin.getServer().getOnlinePlayers()) {
                 if (!J2MC_Manager.getVisibility().isVanished(pl)) {
                     String toAdd;
                     toAdd = pl.getDisplayName();
                     if(J2MC_Manager.getPermissions().hasFlag(pl.getName(), 'd')){
                         toAdd = ChatColor.GOLD + pl.getName();
                     }
                     builder.append(toAdd + ChatColor.WHITE + ", ");
                     if (builder.length() > 75) {
                         builder.setLength(builder.length() - (toAdd + ChatColor.WHITE + ", ").length());
                         sender.sendMessage(builder.toString());
                         builder = new StringBuilder();
                         builder.append(toAdd + ChatColor.WHITE + ", ");
                     }
                 }
             }
             builder.setLength(builder.length() - 2);
             sender.sendMessage(builder.toString());
         } else {
             sender.sendMessage(ChatColor.AQUA + "Players (" + plugin.getServer().getOnlinePlayers().length + "/" + plugin.getServer().getMaxPlayers() + "):");
             if (plugin.getServer().getOnlinePlayers().length == 0) {
                 sender.sendMessage(ChatColor.RED + "No one is online :(");
                 return;
             }
             StringBuilder builder = new StringBuilder();
             for (Player pl : plugin.getServer().getOnlinePlayers()) {
                 String toAdd = "";
                 toAdd = ChatColor.GREEN + pl.getName();
                 if(J2MC_Manager.getPermissions().hasFlag(pl.getName(), 't')){
                     toAdd = ChatColor.DARK_GREEN + pl.getName();
                 }
                 if(J2MC_Manager.getPermissions().hasFlag(pl.getName(), 'd')){
                     toAdd = ChatColor.GOLD + pl.getName();
                 }
                if(pl.hasPermission("j2mc-chat.mute")){
                     toAdd = ChatColor.YELLOW + pl.getName();
                 }
                 if(pl.hasPermission("j2mc.core.admin")){
                     toAdd = ChatColor.RED + pl.getName();
                 }
                 if(J2MC_Manager.getVisibility().isVanished(pl)){
                     toAdd = ChatColor.AQUA + pl.getName();
                 }
                 if(J2MC_Manager.getPermissions().hasFlag(pl.getName(), 'N')){
                    toAdd += ChatColor.DARK_AQUA + "";
                 }
                 if(J2MC_Manager.getPermissions().hasFlag(pl.getName(), 'k')){
                     toAdd += ChatColor.RED + "&";
                 }
                 builder.append(toAdd + ChatColor.WHITE + ", ");
                 if (builder.length() > 75) {
                     builder.setLength(builder.length() - (toAdd + ChatColor.WHITE + ", ").length());
                     sender.sendMessage(builder.toString());
                     builder = new StringBuilder();
                     builder.append(toAdd + ChatColor.WHITE + ", ");
                 }
             }
             builder.setLength(builder.length() - 2);
             sender.sendMessage(builder.toString());
         }
     }
 
 }
