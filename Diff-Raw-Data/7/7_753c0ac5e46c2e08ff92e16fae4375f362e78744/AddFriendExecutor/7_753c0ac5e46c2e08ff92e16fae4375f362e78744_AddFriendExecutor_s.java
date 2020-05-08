 package tmc.BetterProtected.executors;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import tmc.BetterProtected.domain.Player;
 import tmc.BetterProtected.services.PlayerRepository;
 
 public class AddFriendExecutor implements CommandExecutor {
 
     @Override
     public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
         String playerName = commandSender.getName();
         String friendName = args[0];
 
         if (commandSender.isOp() && args.length == 2) {
             if(!PlayerRepository.validPlayerName(friendName)) {
                 sendInvalidPlayerMessageTo(commandSender, friendName);
                 return true;
             }
             if (!PlayerRepository.validPlayerName(args[1])) {
                 sendInvalidPlayerMessageTo(commandSender, args[1]);
                 return true;
             }
             playerName = args[1];
         } else if (args.length < 1 || args.length > 1) return false;
 
         Player player = PlayerRepository.findByName(playerName);
 
         if (!PlayerRepository.validPlayerName(playerName)) {
             sendInvalidPlayerMessageTo(commandSender, playerName);
         } else if (!PlayerRepository.validPlayerName(friendName)) {
             sendInvalidPlayerMessageTo(commandSender, friendName);
         } else {
             boolean worked = player.addFriend(friendName);
             if (worked) {
                 PlayerRepository.save(player);
                 commandSender.sendMessage(ChatColor.DARK_PURPLE + friendName + ChatColor.WHITE +
                         " has been added to " + ChatColor.DARK_PURPLE + playerName + ChatColor.WHITE +"'s friends.");
             } else {
                 commandSender.sendMessage(ChatColor.DARK_PURPLE + friendName + ChatColor.WHITE +
                         " is already a friend of " + ChatColor.DARK_PURPLE + playerName + ChatColor.WHITE + ".");
             }
         }
         return true;
     }
 
     private void sendInvalidPlayerMessageTo(CommandSender commandSender, String friendName) {
         commandSender.sendMessage(ChatColor.DARK_PURPLE + friendName + ChatColor.WHITE +
                 " is not a valid player name or is not in the system.");
     }
 }
