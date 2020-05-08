 package net.minecore.mineplot;
 
 import java.util.ArrayList;
 
 import net.minecore.mineplot.player.PlotPlayer;
 import net.minecore.mineplot.plot.InvalidPlotException;
 import net.minecore.mineplot.plot.Plot;
 import net.minecore.mineplot.world.PlotWorld;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 
 public class PlotCommandInterpreter implements CommandExecutor {
 
 	private MinePlot mp;
 	private PlayerPlotSelector pps;
 
 	public PlotCommandInterpreter(PlayerPlotSelector pps, MinePlot minePermit) {
 		this.mp = minePermit;
 		this.pps = pps;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 
 		if (args.length == 0)
 			return false;
 
 		if (sender instanceof ConsoleCommandSender) {
 			mp.log.info("Console cannot use the Plot command");
 			return true;
 		}
 
 		if (args[0].equalsIgnoreCase("list")) {
 
 			if (args.length > 1) {
 				World w = mp.getServer().getWorld(args[1]);
 
 				if (w == null) {
 					sender.sendMessage(ChatColor.RED + "Invalid World!");
 					return true;
 				}
 
 				PlotWorld pw = mp.getPWM().getPlotWorld(w);
 				if (pw == null) {
 					sender.sendMessage(ChatColor.DARK_GRAY
 							+ "That world is not able to have plots.");
 					return true;
 				}
 
 				if (pw.getPlots().size() == 0)
 					sender.sendMessage("You have no plots in that world!");
 				else
 					sender.sendMessage("Your Plots in world " + w.getName()
 							+ ":");
 
 				for (Plot p : pw.getPlots()) {
 					if (p.getOwner().equals(sender.getName()))
 						sender.sendMessage(ChatColor.GREEN + p.toString());
 					else if (p.canUse(sender.getName()))
 						sender.sendMessage(ChatColor.YELLOW + p.toString());
 				}
 
 				return true;
 			} else {
 
 				ArrayList<Plot> plots = mp.getPlotPlayerManager()
 						.getPlotPlayer(sender.getName()).getPlots();
 
 				if (plots.size() == 0)
 					sender.sendMessage("You have no plots!");
 				else
 					sender.sendMessage("Your Plots:");
 
 				for (Plot p : plots) {
 					sender.sendMessage(ChatColor.GREEN + p.toString());
 				}
 
 				return true;
 			}
 		}
 
 		if (args[0].equalsIgnoreCase("buy")) {
 
 			PlotWorld pw = mp.getPWM().getPlotWorld(
 					mp.getServer().getPlayer(sender.getName()).getLocation()
 							.getWorld());
 
 			if (pw == null) {
 				sender.sendMessage("This world does not allow you to buy plots!");
 				return true;
 			}
 
 			PlotPlayer m = mp.getPlotPlayerManager().getPlotPlayer(
 					sender.getName());
 
 			Plot new1;
 
 			if (args.length < 3) {
 
 				Location l1 = pps.getPlayerLocation1(sender.getName()), l2 = pps
 						.getPlayerLocation2(sender.getName());
 
 				if (l1 == null) {
 					sender.sendMessage(ChatColor.RED
 							+ "You haven't set your first location yet!");
 					return true;
 				}
 
 				if (l2 == null) {
 					sender.sendMessage(ChatColor.RED
 							+ "You haven't set your second location yet!");
 					return true;
 				}
 
 				// Get Plot
 				try {
 					new1 = pw.getNewPlot(l1, l2);
 				} catch (InvalidPlotException e) {
 					sender.sendMessage(ChatColor.DARK_RED + "Plot invalid! "
 							+ e.getMessage());
 					return true;
 				}
 
 				// Set name
 				if (args.length == 2) {
 
 					if (m.getPlot(args[1]) != null) {
 						sender.sendMessage(ChatColor.DARK_RED
 								+ "You already own a plot by that name!");
 						return true;
 					}
 
 					new1.setName(args[1]);
 				}
 
 			} else {
 
 				String[] loc1 = args[1].split(":"), loc2 = args[2].split(":");
 
 				if (loc1.length < 2 || loc2.length < 2)
 					return false;
 
 				try {
 					new1 = pw.getNewPlot(Integer.parseInt(loc1[0]),
 							Integer.parseInt(loc1[1]),
 							Integer.parseInt(loc2[0]),
 							Integer.parseInt(loc2[1]));
 				} catch (NumberFormatException e) {
 					return false;
 				} catch (InvalidPlotException e) {
 					sender.sendMessage(ChatColor.DARK_RED + "Plot invalid! "
 							+ e.getMessage());
 					return true;
 				}
 
 				if (args.length >= 4) {
 
 					if (m.getPlot(args[3]) != null) {
 						sender.sendMessage(ChatColor.DARK_RED
 								+ "You already own a plot by that name!");
 						return true;
 					}
 
 					new1.setName(args[3]);
 				}
 			}
 
 			int numPlots = 0;
 			for (Plot p : m.getPlots())
 				if (p.getLocation1().getWorld().equals(pw.getWorld()))
 					numPlots++;
 
 			if (numPlots >= pw.getMaxPlots()) {
 				sender.sendMessage(ChatColor.DARK_RED
 						+ "You already have the maximum number of plots for this world!");
 				return true;
 			}
 
 			double cost = new1.calculateCost();
 			if (!mp.getMineCore().getEconomyManager()
 					.charge((Player) sender, cost)) {
 				sender.sendMessage(ChatColor.DARK_RED
 						+ "You dont have enough money! Costs " + cost);
 				return true;
 			}
 
 			if (!pw.registerPlot(new1)) {
 				sender.sendMessage(ChatColor.DARK_RED + "Couldn't buy plot!");
 				mp.getMineCore().getEconomyManager()
 						.give((Player) sender, cost);
 				return true;
 			}
 
 			if (new1.getName() == null) {
 				String base = "Plot";
 				int num = 1;
 				while (m.getPlot(base + num) != null)
 					num++;
 
 				new1.setName(base + num);
 			}
 
 			sender.sendMessage(ChatColor.GOLD
 					+ "You have bought a new plot! It has been named "
 					+ new1.getName());
 
 			m.addPlot(new1);
 
 			pps.clear(sender.getName());
 
 			new1.createCorners(Material.FENCE);
 
 			return true;
 
 		}
 
 		if (args[0].equalsIgnoreCase("price")) {
 
 			PlotWorld pw = mp.getPWM().getPlotWorld(
 					mp.getServer().getPlayer(sender.getName()).getLocation()
 							.getWorld());
 
 			if (pw == null) {
 				sender.sendMessage("This world does not allow you to buy plots!");
 				return true;
 			}
 
 			Plot new1;
 
 			if (args.length < 2) {
 
 				Location l1 = pps.getPlayerLocation1(sender.getName()), l2 = pps
 						.getPlayerLocation2(sender.getName());
 
 				if (l1 == null) {
 					sender.sendMessage(ChatColor.RED
 							+ "You haven't set your first location yet!");
 					return true;
 				}
 
 				if (l2 == null) {
 					sender.sendMessage(ChatColor.RED
 							+ "You haven't set your second location yet!");
 					return true;
 				}
 
 				// Get Plot
 				try {
 					new1 = pw.getNewPlot(l1, l2);
 				} catch (InvalidPlotException e) {
 					sender.sendMessage(ChatColor.DARK_RED + "Plot invalid! "
 							+ e.getMessage());
 					return true;
 				}
 
 			} else {
 
 				String[] loc1 = args[1].split(":"), loc2 = args[2].split(":");
 
 				if (loc1.length < 2 || loc2.length < 2)
 					return false;
 
 				try {
 					new1 = pw.getNewPlot(Integer.parseInt(loc1[0]),
 							Integer.parseInt(loc1[1]),
 							Integer.parseInt(loc2[0]),
 							Integer.parseInt(loc2[1]));
 				} catch (NumberFormatException e) {
 					return false;
 				} catch (InvalidPlotException e) {
 					sender.sendMessage(ChatColor.DARK_RED + "Plot invalid! "
 							+ e.getMessage());
 					return true;
 				}
 
 			}
 
 			sender.sendMessage(ChatColor.GOLD
 					+ "The price to buy this plot is " + new1.calculateCost());
 
 			return true;
 		}
 		
 		//Tool command
 		if(args[0].equalsIgnoreCase("tool")){
 			sender.sendMessage("The tool is a " + Material.getMaterial(mp.getSelectionTool()));
 			return true;
 		}
 
 		PlotPlayer m = mp.getPlotPlayerManager()
 				.getPlotPlayer(sender.getName());
 		Plot p = m.getPlot(args[0]);
 
 		if (p == null)
 			return false;
 
 		if (args.length == 1) {
 			sender.sendMessage(ChatColor.AQUA + p.toString());
 			return true;
 		}
 
 		if (args[1].equalsIgnoreCase("add")) {
 
 			if (args.length < 3)
 				return false;
 
 			if (p.canUse(args[2])) {
 				sender.sendMessage(ChatColor.YELLOW
 						+ "That person is already allowed!");
 				return true;
 			}
 
 			p.addPlayer(args[2]);
 			sender.sendMessage(ChatColor.GREEN + "Added player " + args[2]
 					+ " to your plot " + p.getName() + "'s allowed list!");
 			return true;
 		}
 
 		if (args[1].equalsIgnoreCase("remove")) {
 
 			if (args.length < 3)
 				return false;
 
 			p.removePlayer(args[2]);
 			sender.sendMessage(ChatColor.DARK_GREEN + "Removed player "
 					+ args[2] + " from your plot " + p.getName()
 					+ "'s allowed list");
 			return true;
 		}
 
 		if (args[1].equalsIgnoreCase("allowed")) {
 
 			String tot = "Allowed: ";
 			for (String s : p.getAllowedPlayers())
 				tot = tot + ChatColor.GOLD + s + ChatColor.WHITE + ", ";
 			sender.sendMessage(tot);
 			return true;
 		}
 
 		if (args[1].equalsIgnoreCase("rename")) {
 
 			if (args.length < 3)
 				return false;
 
 			p.setName(args[2]);
 
 			sender.sendMessage(ChatColor.GREEN + "Plot renamed!");
 			return true;
 
 		}
 		
 		if (args[1].equalsIgnoreCase("sell")) {
 			
 			Block b = p.getLocation1().getWorld().getHighestBlockAt(p.getLocation1());
 			b.setType(Material.SIGN_POST);
 			Sign sign = (Sign) b.getState().getData();
 			sign.setLine(0, "Right click if");
 			sign.setLine(1, "you want to buy");
 			sign.setLine(2, "this plot");
 
 			sender.sendMessage(ChatColor.YELLOW + "Selling Plot!");
 			return true;
 
 		}
 
 		return false;
 	}
 
 }
