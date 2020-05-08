 package minny.routes.commands;
 
 import java.util.List;
 
 import minny.routes.Routes;
 import minny.routes.RoutesCommand;
 import minny.routes.utils.Config;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class Router extends RoutesCommand {
 
 	Config config;
 	public Routes plugin;
 
 	public Router(Routes plugin) {
 		this.plugin = plugin;
 		config = new Config(plugin, "positions.yml");
 	}
 
 	public void needOP(CommandSender sender) {
 		sender.sendMessage(ChatColor.getByChar(plugin.getConfig().getString(
 				"Messages.Need-Permission-Colour"))
 				+ plugin.getConfig().getString("Messages.Need-Permission"));
 	}
 
 	public void setNext(CommandSender sender) {
 		config.reloadConfig();
 		int current = config.getConfig().getInt("current");
 		Player player = (Player) sender;
 		Location pos = player.getLocation();
		String route = String.valueOf(current) + ".";
 		List<String> l = config.getConfig().getStringList("poslist");
		l.add(String.valueOf(current));
 		config.getConfig().set(route + "x", pos.getX());
 		config.getConfig().set(route + "y", pos.getY());
 		config.getConfig().set(route + "z", pos.getZ());
 		config.getConfig().set(route + "world", player.getWorld().getName());
 		config.getConfig().set("poslist", l);
		config.getConfig().set("current", current+1);
 
 		config.saveConfig();
 
		sender.sendMessage(ChatColor.DARK_GREEN + "Route " + current
 				+ " has been set");
 
 	}
 
 	public void delPrev(CommandSender sender) {
 		config.reloadConfig();
 		int current = config.getConfig().getInt("current");
 		int del = current - 1;
 		config.getConfig().set(String.valueOf(del), null);
 
 		config.getConfig().set("current", current - 1);
 
 		config.saveConfig();
 
 		sender.sendMessage(ChatColor.DARK_PURPLE + "Route " + del
 				+ " has been removed");
 	}
 
 	public void set(CommandSender sender, String args) {
 		if (isRoute(args)) {
 			if (isInt(args)) {
 
 				Player player = (Player) sender;
 				Location pos = player.getLocation();
 				String route = String.valueOf(args) + ".";
 
 				config.getConfig().set(route + "x", pos.getX());
 				config.getConfig().set(route + "y", pos.getY());
 				config.getConfig().set(route + "z", pos.getZ());
 				config.getConfig().set(route + "world",
 						player.getWorld().getName());
 
 				config.saveConfig();
 
 				sender.sendMessage(ChatColor.DARK_GREEN + "Route " + args
 						+ " has been set");
 			} else {
 				sender.sendMessage(ChatColor.DARK_RED
 						+ "Route must be a number");
 			}
 		} else {
 			sender.sendMessage(ChatColor.DARK_RED
 					+ "That route does not exist!");
 		}
 	}
 
 	public void remove(CommandSender sender, String args) {
 		if (isRoute(args)) {
 			if (isInt(args)) {
 				config.getConfig().set(args, null);
 				config.saveConfig();
 				sender.sendMessage(ChatColor.DARK_PURPLE + "Route " + args
 						+ " has been removed");
 			}
 		} else {
 			sender.sendMessage(ChatColor.DARK_RED
 					+ "That route does not exist!");
 		}
 	}
 
 	public void pointTo(CommandSender sender, String args) {
 		if (isInt(args)) {
 			config.reloadConfig();
 			Player player = (Player) sender;
 			String route = args + ".";
 			int locX = config.getConfig().getInt(route + "x");
 			int locY = config.getConfig().getInt(route + "y");
 			int locZ = config.getConfig().getInt(route + "z");
 			World locWorld = Bukkit.getWorld(config.getConfig().getString(
 					route + "world"));
 
 			Location point = new Location(locWorld, locX, locY, locZ, 0, 0);
 
 			player.setCompassTarget(point);
 			plugin.currentPoint.put(sender.getName(), args);
 		} else {
 			sender.sendMessage(ChatColor.DARK_RED + "Route must be a number");
 		}
 	}
 
 	public int getCurrentPoint(CommandSender sender) {
 		if (plugin.currentPoint.containsKey(sender.getName())) {
 			Object point = plugin.currentPoint.get(sender.getName());
 			return Integer.valueOf(String.valueOf(point));
 		} else {
 			plugin.currentPoint.put(sender.getName(), 0);
 			return 0;
 		}
 	}
 
 	public void startRoute(CommandSender sender) {
 		int currentPoint = getCurrentPoint(sender);
 		int nextPoint = currentPoint;
 		if (isRoute(nextPoint)) {
 			pointTo(sender, String.valueOf(nextPoint));
 			sender.sendMessage(ChatColor.getByChar(plugin.getConfig()
 					.getString("Messages.Route-Next-Colour"))
 					+ plugin.getConfig().getString("Messages.Route-Next"));
 		} else {
 			plugin.autoRoute.remove(sender.getName());
 			Player player = (Player) sender;
 			player.setCompassTarget(player.getWorld().getSpawnLocation());
 			sender.sendMessage(ChatColor.getByChar(plugin.getConfig()
 					.getString("Messages.Route-End-Colour"))
 					+ plugin.getConfig().getString("Messages.Route-End"));
 		}
 	}
 
 	public void nextRoute(CommandSender sender) {
 		int currentPoint = getCurrentPoint(sender);
 		int nextPoint = currentPoint + 1;
 		if (isRoute(nextPoint)) {
 			pointTo(sender, String.valueOf(nextPoint));
 			sender.sendMessage(ChatColor.getByChar(plugin.getConfig()
 					.getString("Route-Next-Colour"))
 					+ plugin.getConfig().getString("Messages.Route-Next"));
 		} else {
 			plugin.autoRoute.remove(sender.getName());
 			Player player = (Player) sender;
 			player.setCompassTarget(player.getWorld().getSpawnLocation());
 			sender.sendMessage(ChatColor.getByChar(plugin.getConfig()
 					.getString("Messages.Route-End-Colour"))
 					+ plugin.getConfig().getString("Messages.Route-End"));
 		}
 	}
 
 	public void backRoute(CommandSender sender) {
 		int currentPoint = getCurrentPoint(sender);
 		int backPoint = currentPoint--;
 		if (isRoute(backPoint)) {
 			pointTo(sender, String.valueOf(backPoint));
 		} else {
 			sender.sendMessage(ChatColor.DARK_RED
 					+ "Something went wrong... Unable to go back...");
 		}
 	}
 
 	public boolean isRoute(String args) {
 		config.reloadConfig();
 		if (config.getConfig().contains(args + ".x")) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean isRoute(int args) {
 		config.reloadConfig();
 		if (config.getConfig().contains(args + ".x")) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public void setDesc(CommandSender sender, String desc, String args) {
 		if (isRoute(args)) {
 			config.getConfig().set(args + ".desc", desc);
 			config.saveConfig();
 		} else {
 			sender.sendMessage(ChatColor.DARK_RED
 					+ "That route does not exist.");
 		}
 	}
 
 	public void setName(CommandSender sender, String desc, String args) {
 		if (isRoute(args)) {
 			config.getConfig().set(args + ".name", desc);
 			config.saveConfig();
 		} else {
 			sender.sendMessage(ChatColor.DARK_RED
 					+ "That route does not exist!");
 		}
 	}
 
 	public void sendDesc(CommandSender sender, String args) {
 		config.reloadConfig();
 		if (isRoute(args)) {
 			if (config.getConfig().contains(args + ".desc")) {
 				String desc = config.getConfig().getString(args + ".desc");
 				sender.sendMessage(ChatColor.getByChar(plugin.getConfig()
 						.getString("Messages.Default-Description-Colour"))
 						+ desc);
 			} else {
 				sender.sendMessage(ChatColor.getByChar(plugin.getConfig()
 						.getString("Messages.Default-Description-Colour"))
 						+ plugin.getConfig().getString(
 								"Messages.Default-Description"));
 			}
 		} else {
 			sender.sendMessage(ChatColor.DARK_RED
 					+ "That route does not exist!");
 		}
 	}
 
 	public void sendDesc(CommandSender sender, int args) {
 		config.reloadConfig();
 		if (isRoute(args)) {
 			if (config.getConfig().contains(args + ".desc")) {
 				String desc = config.getConfig().getString(args + ".desc");
 				sender.sendMessage(ChatColor.getByChar(plugin.getConfig()
 						.getString("Messages.Default-Description-Colour"))
 						+ desc);
 			} else {
 				sender.sendMessage(ChatColor.getByChar(plugin.getConfig()
 						.getString("Messages.Default-Description-Colour"))
 						+ plugin.getConfig()
 								.getString("Messages.Default-Description")
 								.replace("POINT", String.valueOf(args)));
 			}
 		} else {
 			sender.sendMessage(ChatColor.DARK_RED
 					+ "That route does not exist!");
 		}
 	}
 
 	public void sendName(CommandSender sender, int args) {
 		if (isRoute(args)) {
 			if (config.getConfig().contains(args + ".name")) {
 				String desc = config.getConfig().getString(args + ".name");
 				sender.sendMessage(ChatColor.getByChar(plugin.getConfig()
 						.getString("Messages.Default-Name-Colour")) + desc);
 			} else {
 				sender.sendMessage(ChatColor.getByChar(plugin.getConfig()
 						.getString("Messages.Default-Name-Colour"))
 						+ plugin.getConfig().getString("Messages.Default-Name")
 								.replace("POINT", String.valueOf(args)));
 			}
 		} else {
 			sender.sendMessage(ChatColor.DARK_RED
 					+ "That route does not exist!");
 		}
 	}
 
 	public void sendName(CommandSender sender, String args) {
 		if (isRoute(args)) {
 			if (config.getConfig().contains(args + ".name")) {
 				String desc = config.getConfig().getString(args + ".name");
 				sender.sendMessage(ChatColor.getByChar(plugin.getConfig()
 						.getString("Messages.Default-Name-Colour")) + desc);
 			} else {
 				sender.sendMessage(ChatColor.getByChar(plugin.getConfig()
 						.getString("Messages.Default-Name-Colour"))
 						+ plugin.getConfig().getString("Messages.Default-Name")
 								.replace("POINT", String.valueOf(args)));
 			}
 		} else {
 			sender.sendMessage(ChatColor.DARK_RED
 					+ "That route does not exist!");
 		}
 	}
 
 	public boolean getAutoRoute(CommandSender sender) {
 		if (plugin.autoRoute != null
 				&& plugin.autoRoute.contains(sender.getName())) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public void setRadius(CommandSender sender, String route, int radius) {
 		if (isRoute(route)) {
 			config.getConfig().set(route + ".radius", radius);
 		} else {
 			sender.sendMessage(ChatColor.DARK_RED
 					+ "That route does not exist!");
 		}
 	}
 
 	public void sendHelp(CommandSender sender) {
 		if (hasPerm(sender, "routes.autoroute")
 				&& plugin.getConfig().getBoolean("Enable-Auto-Route")) {
 			sender.sendMessage(ChatColor.GRAY + "/route - Enables the autoroute functionality");
 		}
 		if (hasPerm(sender, "routes.next")) {
 			sender.sendMessage(ChatColor.GRAY
 					+ "/route set (route) - Re-sets the specified route.");
 		}
 		if (hasPerm(sender, "routes.next")) {
 			sender.sendMessage(ChatColor.GRAY
 					+ "/route next - Sets the selected route to the next route.");
 		}
 		if (hasPerm(sender, "routes.back")) {
 			sender.sendMessage(ChatColor.GRAY
 					+ "/route back - Sets the selected route to the previous route.");
 		}
 		if (hasPerm(sender, "routes.del")) {
 			sender.sendMessage(ChatColor.GRAY
 					+ "/route del (route) - Deletes the specified route.");
 		}
 		if (hasPerm(sender, "routes.set")) {
 			sender.sendMessage(ChatColor.GRAY
 					+ "/route set (route) - Re-sets the specified route.");
 		}
 		if (hasPerm(sender, "routes.set")) {
 			sender.sendMessage(ChatColor.GRAY + "");
 		}
 		if (hasPerm(sender, "routes.setdesc")) {
 			sender.sendMessage(ChatColor.GRAY
 					+ "/route setdesc (route) - Sets the description of the specified route.");
 		}
 		if (hasPerm(sender, "routes.setname")) {
 			sender.sendMessage(ChatColor.GRAY
 					+ "/route setname (route) - Sets the name of the specified route.");
 		}
 		if (hasPerm(sender, "routes.setradius")) {
 			sender.sendMessage(ChatColor.GRAY
 					+ "/route setradius (route) - Sets the radius of the specified route.");
 		}
 		if (hasPerm(sender, "routes.desc")) {
 			sender.sendMessage(ChatColor.GRAY
 					+ "/route desc <route> - Sends the player the description of the specified route. If no route is supplied, then it will display the description of the current route.");
 		}
 		if (hasPerm(sender, "routes.name")) {
 			sender.sendMessage(ChatColor.GRAY
 					+ "/route name <route> - Sends the player the name of the specified route. If no route is supplied, then it will display the name of the current route.");
 		}
 		if (hasPerm(sender, "routes.info")) {
 			sender.sendMessage(ChatColor.GRAY
 					+ "/route info <route> - Sends the player the info of the specified route. If no route is supplied, then it will display the info of the current route.");
 		}
 		if (hasPerm(sender, "routes.pointto")) {
 			sender.sendMessage(ChatColor.GRAY
 					+ "/route locate (route) - Sets the selected route to the specified route.");
 		}
 		if (hasPerm(sender, "routes.help")) {
 			sender.sendMessage(ChatColor.GRAY
 					+ "/route help - Displays the help for Routes.");
 		}
 	}
 }
