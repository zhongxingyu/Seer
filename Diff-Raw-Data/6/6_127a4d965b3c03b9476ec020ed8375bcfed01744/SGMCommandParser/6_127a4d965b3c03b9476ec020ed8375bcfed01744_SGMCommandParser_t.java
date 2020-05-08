 package info.tobiaswallin.shortgm;
 
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class SGMCommandParser {
 
 	public boolean Parse(CommandSender sender, Command cmd,
 			String commandLabel, String[] args) {
 		if (this.isPlayer(sender))
 			return this.parsePlayerCommand((Player) sender, args);
 		else
 			return this.parseConsoleCommand(args);
 	}
 
 	private boolean parsePlayerCommand(Player player, String[] args) {
		if (!player.hasPermission(SGMPermissions.SGM)){
			player.sendMessage("You do not have permission do that.");
			return true;
		}
 
 		// Toggle game mode on self
 		if (args.length == 0 && player.hasPermission(SGMPermissions.SET_SELF)) {
 			ShortGameMode.toggleGameMode(player);
 			return true;
 		}
 
 		// Check for argument method
 		if (args.length > 0) {
 			switch (SGMMethod.toMethod(args[0].toUpperCase())) {
 			case SET:
 				// Set game mode based on arguments
 				return setGameMode(player, args);
 			default:
 				// Toggle game mode based on arguments
 				return toggleGameMode(player, args);
 			}
 		}
 
 		return false;
 	}
 
 	private boolean toggleGameMode(Player player, String[] args) {
 		Player other;
 
 		if (args.length == 1) {
 			other = ShortGameMode.thisPlugin.getServer().getPlayer(args[0]);
 			if (other == null) {
 				playerNotFound(player, args[0]);
 				return true;
 			}
 
 			ShortGameMode.toggleGameMode(player, other);
 		} else {
 			SGMLogger.Message(player, "To many arguments! use " + ChatColor.RED
 					+ "/gm [player]");
 		}
 
 		// Always return true, we handle our own usage messages
 		return true;
 	}
 
 	private boolean setGameMode(Player player, String[] args) {
 
 		if (args.length == 3) {
 			OfflinePlayer other = ShortGameMode.thisPlugin.getServer()
 					.getOfflinePlayer(args[1]);
 			if (!other.hasPlayedBefore()) {
 				playerNotFound(player, args[1]);
 				return true;
 			}
 
 			Player target = ShortGameMode.thisPlugin.getServer()
 					.getPlayerExact(args[1]);
 
 			GameMode mode = (args[2].equalsIgnoreCase("1") || args[2]
 					.equalsIgnoreCase("creative")) ? GameMode.CREATIVE
 					: GameMode.SURVIVAL;
 			ShortGameMode.setGameMode(player, target, mode);
 		} else {
 			SGMLogger.Message(player, "To many arguments! use " + ChatColor.RED
 					+ "/gm set [player] [mode]");
 		}
 
 		// Always return true, we handle our own usage messages
 		return true;
 	}
 
 	private boolean parseConsoleCommand(String[] args) {
 		// TODO: Implement console commands
 		SGMLogger.Log(Level.WARNING, "Console commands not yet implemented");
 		return true;
 	}
 
 	private boolean isPlayer(CommandSender sender) {
 		return (sender instanceof Player);
 	}
 
 	private void playerNotFound(Player player, String target) {
 		SGMLogger.Message(player, "Unable to locate " + ChatColor.GREEN
 				+ target + ChatColor.WHITE + ". use " + ChatColor.RED
 				+ "/gm set [player] <gamemode>" + ChatColor.WHITE
 				+ " for offline players.");
 	}
 
 }
