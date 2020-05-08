 package to.joe.j2mc.notes.command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import to.joe.j2mc.core.J2MC_Core;
 import to.joe.j2mc.core.J2MC_Manager;
 import to.joe.j2mc.core.command.MasterCommand;
 import to.joe.j2mc.core.exceptions.BadPlayerMatchException;
 import to.joe.j2mc.notes.J2MC_Notes;
 
 public class NoteCommand extends MasterCommand {
 
     J2MC_Notes plugin;
     
     public NoteCommand(J2MC_Notes notes) {
         super(notes);
         this.plugin = notes;
     }
 
     @Override
     public void exec(CommandSender sender, String commandName, String[] args, Player player, boolean isPlayer) {
         if (isPlayer) {
             if (args.length < 2) {
                 player.sendMessage(ChatColor.RED + "/note username message");
                 return;
             }
             boolean adminMode = false;
             if (commandName.equalsIgnoreCase("anote") && sender.hasPermission("j2mc.core.admin")) {
                 adminMode = true;
             }
             String message = J2MC_Core.combineSplit(1, args, " ");
             Player target = null;
             try {
                 target = J2MC_Manager.getVisibility().getPlayer(args[0], sender);
             } catch (BadPlayerMatchException e) {
                 target = null;
             }
             if (target != null && target.equals(player)) {
                 sender.sendMessage(ChatColor.RED + "I think you're lonely.");
                 return;
             }
             if (target != null) {
                 if (adminMode) {
                     target.sendMessage(ChatColor.AQUA + "HEY " + ChatColor.RED + target.getName() + ChatColor.AQUA + ": " + message);
                     J2MC_Manager.getCore().adminAndLog(ChatColor.AQUA + "Priv <" + ChatColor.DARK_AQUA + sender.getName() 
                             + ChatColor.AQUA + "->" + ChatColor.DARK_AQUA + target.getName() + ChatColor.AQUA + "> " + message);
                 } else {
                    String toSend = ChatColor.WHITE + "<" + player.getDisplayName() + ChatColor.GRAY + "->" + ChatColor.WHITE + target.getDisplayName() + "> " + message;
                     player.sendMessage(toSend);
                     target.sendMessage(toSend);
                     for (Player plr : plugin.getServer().getOnlinePlayers()) {
                         if (plr != null && plr.hasPermission("j2mc.chat.admin.nsa") && (!plr.equals(player) || !plr.equals(target))) {
                             plr.sendMessage(ChatColor.AQUA + "[NSA] " + toSend);
                         }
                     }
                 }
             } else {
                 plugin.manager.AddNote(player.getName(), args[0], message, adminMode);
                 player.sendMessage(ChatColor.AQUA + "Note left for " + args[0]);
                 final String bit = ChatColor.AQUA + "Note <" + ChatColor.DARK_AQUA + player.getName() + ChatColor.AQUA + "->" + ChatColor.DARK_AQUA + args[0] + ChatColor.AQUA + "> " + message;
                 if(adminMode) {
                     plugin.getServer().broadcast(bit, "j2mc.core.admin");
                 } else {
                     plugin.getServer().broadcast(ChatColor.AQUA + "[NSA] " + bit, "j2mc.chat.admin.nsa");
                 }
             }
         }
     }
 
 }
