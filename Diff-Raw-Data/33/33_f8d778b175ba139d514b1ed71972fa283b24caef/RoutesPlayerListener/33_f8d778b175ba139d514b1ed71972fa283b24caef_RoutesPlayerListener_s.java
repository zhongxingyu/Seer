 package minny.routes.listeners;
 
 import minny.routes.Routes;
 import minny.routes.commands.Router;
 import minny.routes.utils.Config;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 public class RoutesPlayerListener extends Router implements Listener {
 
 	Config config;
 
 	public RoutesPlayerListener(Routes plugin) {
 		super(plugin);
 		config = new Config(plugin, "positions.yml");
 	}
 	
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent e) {
 		if (getAutoRoute(e.getPlayer())) {
 			
 			int current = getCurrentPoint(e.getPlayer());
 			int radius;
 			String route = current + ".";
 			if (config.getConfig().contains(route + "radius")){
 				radius = config.getConfig().getInt(route + "radius");
 			} else {
 				radius = plugin.getConfig().getInt("Auto-Route-Radius");
 			}
 
 			int locX = config.getConfig().getInt(route + "x");
 			int locY = config.getConfig().getInt(route + "y");
 			int locZ = config.getConfig().getInt(route + "z");
 			World locWorld = Bukkit.getWorld(config.getConfig().getString(
 					route + "world"));
 
 			Location loc = new Location(locWorld, locX, locY, locZ, 0, 0);
 			
 			Player player = e.getPlayer();
 			if (inRadius(player, loc, radius)) {
 				nextRoute(player);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void updateMessage(PlayerJoinEvent e) {
 		if (e.getPlayer().isOp()
				|| e.getPlayer().hasPermission("simplebuild.notify")) {
 			Player player = e.getPlayer();
 			if (plugin.isUpdate) {
 				player.sendMessage(ChatColor.RED
						+ "SimpleBuild is out of date! Get the new version at:");
 				player.sendMessage(ChatColor.GRAY
						+ "http://dev.bukkit.org/server-mods/simple-build/");
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent e){
 		if (getAutoRoute(e.getPlayer())){
 			pointTo(e.getPlayer(), String.valueOf(getCurrentPoint(e.getPlayer())));
 		}
 	}
 	
 	private boolean inRadius(Player player, Location loc, double radius)  {
 	    double x = loc.getX() - player.getLocation().getX();
 	    double y = loc.getY() - player.getLocation().getY();
 	    double z = loc.getZ() - player.getLocation().getZ();
 	    double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
 	    if (distance <= radius)
 	        return true;
 	    else
 	        return false;
 	}
 }
