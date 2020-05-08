 package net.chiisana.builddit.command;
 
 import net.chiisana.builddit.controller.BuildditPlot;
 import net.chiisana.builddit.controller.Plot;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.util.HashSet;
 
 public class PlotCommand implements CommandExecutor {
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("plot"))
 		{
 			if (!(sender instanceof Player))
 			{
 				sender.sendMessage("Builddit Plot only have in game commands.");
 				return true;
 			}
 
 			Player player = (Player)sender;
 
 			String subCmd;
 			if (args.length == 0)
 			{
 				// No length, output help
 				subCmd = "help";
 			} else {
 				subCmd = args[0];
 			}
 
 			if (subCmd.equalsIgnoreCase("help"))
 			{
 				/*
 				int page;
 				if (args.length < 2)
 				{
 					page = 1;
 				} else {
 					page = Integer.parseInt(args[1]);
 					page = page > 0 ? page : 1;         // No page 0, not a number from user.
 				}
 				sender.sendMessage("Builddit Plot Commands (Page " + page + " of Y)"); // For future
 				*/
 				player.sendMessage("Builddit Plot Commands");
 				player.sendMessage("====================================");
 				player.sendMessage(" claim   - attempt to claim the plot.");
 				player.sendMessage(" unclaim - unclaim the plot.");
 				player.sendMessage(" clear   - clear (regenerate) the plot (warning: no undo).");
 				player.sendMessage(" reset   - clear + unclaim the plot (warning: no undo).");
 				player.sendMessage(" auth <name>   - authorizes <name> to work on the plot.");
 				player.sendMessage(" unauth <name> - unauthorizes <name> to work on the plot.");
 				return true;
 			}
 
 			Plot currentPlot = BuildditPlot.getInstance().getPlotAt(player.getLocation());
 			if (currentPlot == null)
 			{
 				player.sendMessage("Unable to acquire plot on your current position. Database server may be down right now. Please contact the server admin and try again later.");
 				return true;
 			}
 			if (subCmd.equalsIgnoreCase("claim"))
 			{
 				String result = this._claim(currentPlot, player);
 				player.sendMessage(result);
 				return true;
 			}
 			else if (subCmd.equalsIgnoreCase("unclaim"))
 			{
 				String result = this._unclaim(currentPlot, player);
 				player.sendMessage(result);
 				return true;
 			}
 			else if (subCmd.equalsIgnoreCase("clear"))
 			{
 				String result = this._clear(currentPlot, player);
 				player.sendMessage(result);
 				return true;
 			}
 			else if (subCmd.equalsIgnoreCase("reset"))
 			{
 				String result = this._clear(currentPlot, player);
 				player.sendMessage(result);
 				result = this._unclaim(currentPlot, player);
 				player.sendMessage(result);
 				return true;
 			}
 			else if (subCmd.equalsIgnoreCase("auth"))
 			{
 				if (args.length < 2)
 				{
 					player.sendMessage("You must specify who you are authorizing. Example usage: ");
 					player.sendMessage("/plot auth huang_a  -- this authorizes huang_a to work on the plot.");
 					return true;
 				}
 
 				String target = args[1];
 				if (currentPlot.authorize(target, player))
 				{
 					// Also add permissions to all connected plots
 					HashSet<Plot> connectedPlots = currentPlot.getConnectedPlots();
 					for (Plot plot : connectedPlots)
 					{
 						plot.authorize(target, player);
 					}
 					player.sendMessage(target + " has been added to the authorized users list.");
 					return true;
 				} else {
 					player.sendMessage("You do not own the plot, so you cannot modify the authorized users list.");
 					return true;
 				}
 			}
 			else if (subCmd.equalsIgnoreCase("unauth"))
 			{
 				if (args.length < 2)
 				{
 					player.sendMessage("You must specify who you are unauthorizing. Example usage: ");
 					player.sendMessage("/plot unauth huang_a  -- this unauthorizes huang_a to work on the plot.");
 				}
 
 				String target = args[1];
 				if (currentPlot.unauthorize(target, player))
 				{
 					// Also remove permissions from all connected plots
 					HashSet<Plot> connectedPlots = currentPlot.getConnectedPlots();
 					for (Plot plot : connectedPlots)
 					{
 						plot.unauthorize(target, player);
 					}
 					player.sendMessage(target + " has been removed from the authorized users list.");
 					return true;
 				} else {
 					player.sendMessage("You do not own the plot, so you cannot modify the authorized users list.");
 					return true;
 				}
 			}
 			else if (subCmd.equalsIgnoreCase("list-auth"))
 			{
 				if (currentPlot.getOwner().equals(player.getName()))
 				{
 					player.sendMessage("People authorized to edit this plot: ");
 					String authorizedList = "";
 					for(String authorized : currentPlot.getAuthorized())
 					{
 						authorizedList = authorizedList + ", " + authorized;
 					}
					authorizedList = authorizedList.substring(2);   // truncate the first ", "
 					player.sendMessage(authorizedList);
 				}
 			}
 			else if (subCmd.equalsIgnoreCase("test-connected"))
 			{
 				HashSet<Plot> connectedPlots = currentPlot.getConnectedPlots();
 				player.sendMessage("Connected Plots (" + connectedPlots.size() + "): ");
 				for(Plot plot : connectedPlots)
 				{
 					player.sendMessage(plot.toString());
 				}
 			}
 		}
 		return false;
 	}
 
 	private String _claim(Plot plot, Player player) {
 		// Claiming a plot is pretty straight forward: try to claim it, and let player know result
 		switch(plot.claim(player)) {
 			case 1:
 				HashSet<Plot> connectedPlot = plot.getConnectedPlots();
 				for (Plot neighbour : connectedPlot)
 				{
 					// Claiming a connected plot, inherit authorizations accordingly
 					plot.copyAuthFrom(neighbour);
 					break;
 				}
 				return "You have successfully claimed the plot.";
 			case 0:
 				return "Plot is already owned by " + plot.getOwner() + ".";
 			case -1:
 				return "Database server is unavailable at this time. Please try again later.";
 			default:
 				return "You should never be seeing this message. Something went wrong, blame the developer.";
 		}
 	}
 
 	private String _unclaim(Plot plot, Player player) {
 		// Unclaiming is a bit less straight forward: only allow if player owns it or is admin
 		switch(plot.unclaim(player)) {
 			case 1:
 				return "You have successfully unclaimed the plot.";
 			case 0:
 				return "You do not own the plot, so you cannot unclaim it.";
 			case -1:
 				return "Database server is unavailable at this time. Please try again later.";
 			default:
 				return "You should never be seeing this message. Something went wrong, blame the developer.";
 		}
 	}
 
 	private String _clear(Plot plot, Player player) {
 		// Clearing the plot: only allow if player owns it or is admin
 		if (plot.clear(player))
 		{
 			return "Plot content have been cleared.";
 		} else {
 			return "You do not own the plot, so you cannot clear it.";
 		}
 	}
 }
