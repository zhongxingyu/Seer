 package fr.noogotte.useful_commands.command;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.CommandArgs;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.command.exception.CommandUsageError;
 import fr.noogotte.useful_commands.exception.PlayerNotInServer;
 
 @NestedCommands(name = "useful")
 public class PlayerCommands extends UsefulCommands {
 
 	@Command(name = "gamemode", min = 0, max = 1)
 	public void gamemode(Player player, CommandArgs args) {
 		if(args.length() == 0) {
 			if(player.getGameMode() == GameMode.CREATIVE) {
 				player.setGameMode(GameMode.SURVIVAL);
				player.sendMessage(ChatColor.GREEN + "Vous vous étes mis en" + ChatColor.AQUA + player.getGameMode());
 			} else {
 				player.setGameMode(GameMode.CREATIVE);
				player.sendMessage(ChatColor.GREEN + "Vous vous étes mis en" + ChatColor.AQUA + player.getGameMode());
 			}
 		} else if (args.length() == 1) {
 			Player target = Bukkit.getPlayer(args.get(0));
 			if(target == null) {
 				throw new PlayerNotInServer();
 			} else {
 				if(target.getGameMode() == GameMode.CREATIVE) {
 					target.setGameMode(GameMode.SURVIVAL);
 					player.sendMessage(ChatColor.GREEN + "Vous vous avez mis en " + ChatColor.AQUA + "SURVIVAL" + 
 							ChatColor.GREEN + target.getName());
 				} else {
 					target.setGameMode(GameMode.CREATIVE);
 					player.sendMessage(ChatColor.GREEN + "Vous vous avez mis en " + ChatColor.AQUA + "CREATIVE " +
 							ChatColor.GREEN + target.getName());
 				}
 			}
 		} else {
 			throw new CommandUsageError("Argument " + args.get(0) + " inconnu.");
 		}
 	}
 }
