 package net.minecore.mineplot;
 
 import java.util.ArrayList;
 
 import net.minecore.mineplot.player.PlotPlayer;
 import net.minecore.mineplot.plot.InvalidPlotException;
 import net.minecore.mineplot.plot.Plot;
 import net.minecore.mineplot.world.PlotWorld;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 
 public class PlotCommandInterpreter implements CommandExecutor {
 
 	private MinePlot mp;
 
 	public PlotCommandInterpreter(MinePlot minePermit) {
 		this.mp = minePermit;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,	String label, String[] args) {
 		
		if(args.length == 0)
			return false;
		
 		if(sender instanceof ConsoleCommandSender){
 			mp.log.info("Console cannot use the Plot command");
 			return true;
 		}
 		
 		if(args[0].equalsIgnoreCase("list")){
 			
 			
 			if(args.length > 1){
 				World w = mp.getServer().getWorld(args[1]);
 				
 				if(w == null){
 					sender.sendMessage(ChatColor.RED + "Invalid World!");
 					return true;
 				}
 				
 				PlotWorld pw = mp.getPWM().getPlotWorld(w);
 				if(pw == null){
 					sender.sendMessage(ChatColor.DARK_GRAY + "That world is not able to have plots.");
 					return true;
 				}
 				
 				if(pw.getPlots().size() == 0)
 					sender.sendMessage("You have no plots in that world!");
 				else
 					sender.sendMessage("Your Plots in world " + w.getName() + ":");
 				
 				for (Plot p : pw.getPlots()){
 					if(p.getOwner().equals(sender.getName()))
 						sender.sendMessage(ChatColor.GREEN + p.toString());
 					else if(p.canUse(sender.getName()))
 						sender.sendMessage(ChatColor.YELLOW + p.toString());
 				}
 				
 				return true;
 			} else {
 				
 				ArrayList<Plot> plots = mp.getPlotPlayerManager().getPlotPlayer(sender.getName()).getPlots();
 				
 				if(plots.size() == 0)
 					sender.sendMessage("You have no plots!");
 				else
 					sender.sendMessage("Your Plots:");
 				
 				for (Plot p : plots){
 					sender.sendMessage(ChatColor.GREEN + p.toString());
 				}
 				
 				return true;
 			}
 		}
 		
 		if(args[0].equalsIgnoreCase("buy")){
 			
 			//if(sender.hasPermission("mineplot.buy"))
 			
 			if(args.length < 3)
 				return false;
 			
 			PlotWorld pw = mp.getPWM().getPlotWorld(mp.getServer().getPlayer(sender.getName()).getLocation().getWorld());
 			
 			if(pw == null){
 				sender.sendMessage("This world does not allow you to buy plots!");
 				return true;
 			}
 			
 			String[] loc1 = args[1].split(":"), loc2 = args[2].split(":");
 			
 			if(loc1.length < 2 || loc2.length < 2)
 				return false;
 			
 			Plot new1;
 			try {
 				new1 = pw.getNewPlot(Integer.parseInt(loc1[0]), Integer.parseInt(loc1[1]), Integer.parseInt(loc2[0]), Integer.parseInt(loc2[1]));
 			} catch (NumberFormatException e){
 				return false;
 			} catch (InvalidPlotException e) {
 				sender.sendMessage(ChatColor.DARK_RED + "Plot invalid! " + e.getMessage());
 				return true;
 			}
 			
 			PlotPlayer m = mp.getPlotPlayerManager().getPlotPlayer(sender.getName());
 			
 			int numPlots = 0;
 			for(Plot p : m.getPlots())
 				if(p.getLocation1().getWorld().equals(pw.getWorld()))
 					numPlots++;
 			
 			if(numPlots >= pw.getMaxPlots()){
 				sender.sendMessage(ChatColor.DARK_RED + "You already have the maximum number of plots for this world!");
 				return true;
 			}
 			
 			if(args.length >= 4){
 				
 				if(m.getPlot(args[3]) != null){
 					sender.sendMessage(ChatColor.DARK_RED + "You already own a plot by that name!");
 					return true;
 				}
 				
 				new1.setName(args[3]);
 			} else {
 				String base = "Plot";
 				int num = 1;
 				while(m.getPlot(base + num) != null)
 					num++;
 				
 				new1.setName(base + num);
 			}
 			
 			double cost = new1.calculateCost();
 			if(!mp.getMineCore().getEconomyManager().charge((Player)sender, cost)){
 				sender.sendMessage(ChatColor.DARK_RED + "You dont have enough money! Costs " +  cost);
 				return true;
 			}
 			
 			if(!pw.registerPlot(new1)){
 				sender.sendMessage(ChatColor.DARK_RED + "Couldn't buy plot!");
 				mp.getMineCore().getEconomyManager().give((Player)sender, cost);
 				return true;
 			}
 			
 			sender.sendMessage(ChatColor.GOLD + "You have bought a new plot! It has been named " + new1.getName());
 			
 			m.addPlot(new1);
 			
 			new1.createCorners(Material.FENCE);
 			
 			return true;
 			
 		}
 		
 		if(args[0].equalsIgnoreCase("price")){
 			if(args.length < 3)
 				return false;
 			
 			PlotWorld pw = mp.getPWM().getPlotWorld(mp.getServer().getPlayer(sender.getName()).getLocation().getWorld());
 			
 			if(pw == null){
 				sender.sendMessage("This world does not allow you to buy plots!");
 				return true;
 			}
 			
 			String[] loc1 = args[1].split(":"), loc2 = args[2].split(":");
 			
 			if(loc1.length < 2 || loc2.length < 2)
 				return false;
 			
 			Plot new1;
 			try {
 				new1 = pw.getNewPlot(Integer.parseInt(loc1[0]), Integer.parseInt(loc1[1]), Integer.parseInt(loc2[0]), Integer.parseInt(loc2[1]));
 			} catch (NumberFormatException e){
 				return false;
 			} catch (InvalidPlotException e) {
 				sender.sendMessage(ChatColor.DARK_RED + "Plot invalid! " + e.getMessage());
 				return true;
 			}
 			
 			sender.sendMessage(ChatColor.GOLD + "The price to buy this plot is " + new1.calculateCost());
 			
 			return true;
 		}
 
 		if(args.length > 0){
 			PlotPlayer m = mp.getPlotPlayerManager().getPlotPlayer(sender.getName());
 			Plot p = m.getPlot(args[0]);
 			
 			if(p == null)
 				return false;
 			
 			if(args.length == 1){
 				sender.sendMessage(ChatColor.AQUA + p.toString());
 				return true;
 			}
 			
 			if(args[1].equalsIgnoreCase("add")){
 				
 				if(args.length < 3)
 					return false;
 				
 				if(p.canUse(args[2])){
 					sender.sendMessage(ChatColor.YELLOW + "That person is already allowed!");
 					return true;
 				}
 				
 				p.addPlayer(args[2]);
 				sender.sendMessage(ChatColor.GREEN + "Added player " + args[2] + " to your plot " + p.getName() + "'s allowed list!");
 				return true;
 			}
 			
 			if(args[1].equalsIgnoreCase("remove")){
 				
 				if(args.length < 3)
 					return false;
 				
 				p.removePlayer(args[2]);
 				sender.sendMessage(ChatColor.DARK_GREEN + "Removed player " + args[2] + " from your plot " + p.getName() + "'s allowed list");
 				return true;
 			}
 			
 			if(args[1].equalsIgnoreCase("allowed")){
 				
 				
 				String tot = "Allowed: ";
 				for(String s : p.getAllowedPlayers())
 					tot = tot + ChatColor.GOLD + s + ChatColor.WHITE + ", ";
 				sender.sendMessage(tot);
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 }
