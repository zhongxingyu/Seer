 package com.mitchdev.bukkit.grid.commands;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.mitchdev.bukkit.grid.CommandHandler;
 import com.mitchdev.bukkit.grid.Grid;
 import com.mitchdev.bukkit.grid.Network;
 import com.mitchdev.bukkit.grid.Pad;
 import com.mitchdev.bukkit.grid.Permissions;
 import com.mitchdev.bukkit.grid.Visibility;
 
 public class SetCommand extends CommandHandler {
 
 	public SetCommand() {
 
 	}
 
 	public void doSet(Grid grid, CommandSender sender, Pad pad,
 			String[] commands) {
 
 		/*
 		 * for ( int i = 0; i < commands.length; i++ ) { System.out.println(
 		 * Integer.toString(i) + ": " + commands[i] ); }
 		 */
 
 		if (commands.length == 0) {
 			sender.sendMessage(Grid.getChatPrefix()
 					+ "ERROR: No arguments provided.");
 			sender.sendMessage(Grid.getChatPrefix()
 					+ "See '/grid pad' for help.");
 		} else {
 
 			if (commands[0].equalsIgnoreCase("name")) {
 				// Check that we have permission to use this command.
 				if (sender instanceof Player
 						&& !Permissions.getInstance().hasPermission(
 								(Player) sender, "grid.pad.set.name")) {
 					sender.sendMessage(Grid.getChatPrefix()
 							+ "ERROR: You do not have permission to use this command..");
 					return;
 				}
 				// Otherwise..
 				if (commands.length < 2) {
 					sender.sendMessage(Grid.getChatPrefix()
 							+ "ERROR: Invalid arguments provided.");
 					sender.sendMessage(Grid.getChatPrefix()
 							+ "See '/grid pad' for help.");
 					return;
 				}
 				setPadName(grid, sender, pad, commands[1]);
 			} else if (commands[0].equalsIgnoreCase("visibility")) {
 				setPadVisibility(grid, sender, pad, commands[1]);
 			} else if (commands[0].equalsIgnoreCase("description")) {
 				setPadDescription(grid, sender, pad, commands[1]);
 			} else if (commands[0].equalsIgnoreCase("owner")) {
 				setPadOwner(grid, sender, pad, commands[1]);
 			} else if (commands[0].equalsIgnoreCase("password")) {
 				setPadPassword(grid, sender, pad, commands[1]);
 			} else {
 				sender.sendMessage(Grid.getChatPrefix()
 						+ "ERROR: Unrecognised command..");
 				return;
 			}
 
 		}
 
 	}
 
 	@Override
 	public boolean onCommand(Grid grid, CommandSender sender, Command cmd,
 			String commandLabel, String[] args) {
 
 		if (commandLabel.equalsIgnoreCase("gs")
 				|| (cmd.getName().equalsIgnoreCase("grid") && args.length > 0 && args[0]
 						.equalsIgnoreCase("set"))) {
 
 			if (!(sender instanceof Player)) {
 				sender.sendMessage(Grid.getChatPrefix()
 						+ "This sort of command must be run by a client.");
 				sender.sendMessage(Grid.getChatPrefix()
 						+ "Use 'grid pad <name> set' to access this command from the console.");
 				return true;
 			}
 
 			Player player = (Player) sender;
 
 			Pad pad = grid.getPad(player);
 
			int offset = commandLabel.equalsIgnoreCase("gs") ? 1 : 0;
 
 			if (pad != null) {
 				doSet(grid, sender, pad, (String[]) ArrayUtils.subarray(args,
						1 + offset, args.length));
 			} else {
 				sender.sendMessage(Grid.getChatPrefix() + "No pad found.");
 			}
 
 			return true;
 
 		}
 
 		return false;
 	}
 
 	private Network getNetwork(Grid grid, Pad pad) {
 		String primary = pad.getPrimaryNetwork();
 
 		Network net = null;
 
 		if (primary.length() == 0) {
 			net = grid.getGlobalNetwork();
 		} else {
 			net = grid.getNetwork(primary);
 		}
 		return net;
 	}
 
 	private void setPadDescription(Grid grid, CommandSender sender, Pad pad,
 			String description) {
 
 		if (sender instanceof Player
 				&& !Permissions.getInstance().hasPermission((Player) sender,
 						"grid.pad.set.description")) {
 			sender.sendMessage(Grid.getChatPrefix()
 					+ "You do not have permission to use that command.");
 			return;
 		}
 
 		pad.setDescription(description);
 		sender.sendMessage(Grid.getChatPrefix()
 				+ "SUCCESS: Set description for pad '" + ChatColor.YELLOW
 				+ pad.getName() + ChatColor.RESET + "'");
 
 	}
 
 	/**
 	 * 
 	 * @param grid
 	 * @param sender
 	 * @param pad
 	 * @param newName
 	 * @category Property Modifier
 	 */
 	private void setPadName(Grid grid, CommandSender sender, Pad pad,
 			String newName) {
 
 		if (sender instanceof Player
 				&& !Permissions.getInstance().hasPermission((Player) sender,
 						"grid.pad.set.name")) {
 			sender.sendMessage(Grid.getChatPrefix()
 					+ "You do not have permission to use that command.");
 			return;
 		}
 
 		Network net = getNetwork(grid, pad);
 
 		// Null check..
 		if (net == null) {
 			sender.sendMessage(Grid.getChatPrefix()
 					+ "ERROR: Network not found.");
 			return;
 		}
 
 		for (Pad p : net.getPads()) {
 			if (pad == p) {
 				// Ignore us.
 				continue;
 			}
 			if (p.getName().equalsIgnoreCase(newName)) {
 				sender.sendMessage(Grid.getChatPrefix()
 						+ "ERROR: Name matches existing pad on network.");
 				return;
 			}
 		}
 
 		// Otherwise
 		pad.setName(newName);
 		sender.sendMessage(Grid.getChatPrefix() + "SUCCESS: Renamed pad to '"
 				+ newName + "'");
 
 	}
 
 	private void setPadOwner(Grid grid, CommandSender sender, Pad pad,
 			String owner) {
 
 		if (sender instanceof Player
 				&& !Permissions.getInstance().hasPermission((Player) sender,
 						"grid.pad.set.owner")) {
 			sender.sendMessage(Grid.getChatPrefix()
 					+ "You do not have permission to use that command.");
 			return;
 		}
 		
 	}
 
 	private void setPadPassword(Grid grid, CommandSender sender, Pad pad,
 			String password) {
 
 		if (sender instanceof Player
 				&& !Permissions.getInstance().hasPermission((Player) sender,
 						"grid.pad.set.password")) {
 			sender.sendMessage(Grid.getChatPrefix()
 					+ "You do not have permission to use that command.");
 			return;
 		}
 		
 	}
 
 	private void setPadVisibility(Grid grid, CommandSender sender, Pad pad,
 			String visibility) {
 
 		if (sender instanceof Player
 				&& !Permissions.getInstance().hasPermission((Player) sender,
 						"grid.pad.set.visibility")) {
 			sender.sendMessage(Grid.getChatPrefix()
 					+ "You do not have permission to use that command.");
 			return;
 		}
 		
 		Visibility vis = null;
 
 		if (visibility.equalsIgnoreCase("Visible")) {
 			vis = Visibility.Visible;
 		}
 		if (visibility.equalsIgnoreCase("Hidden")) {
 			vis = Visibility.Hidden;
 		}
 		if (visibility.equalsIgnoreCase("Unlisted")) {
 			vis = Visibility.Unlisted;
 		}
 
 		if (vis == null) {
 			sender.sendMessage(Grid.getChatPrefix()
 					+ "ERROR: Unrecognised command passed to '/grid set visibility'");
 			return;
 		}
 
 		pad.setVisibility(vis);
 		sender.sendMessage(Grid.getChatPrefix()
 				+ "SUCCESS: Successfuly set the visibility of pad '"
 				+ ChatColor.YELLOW + pad.getName() + ChatColor.RESET + "'");
 
 	}
 
 }
