 package net.catharos.port.listener;
 
 import java.util.logging.Level;
 import net.catharos.port.PortPlugin;
 import net.catharos.port.PortSign;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public class InteractionListener implements Listener {
 	public InteractionListener(JavaPlugin plugin) {
 		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 	
 	@EventHandler
 	public void interact(PlayerInteractEvent event) {
 		if(event.isCancelled()) return;
 		
 		Block block = event.getClickedBlock();
 		if(block.getType() != Material.ENCHANTMENT_TABLE && block.getData() != 1) return;
 		
		// Cancel the event
		event.setCancelled(true);
		
 		Location loc = block.getLocation();
 		Player player = event.getPlayer();
 		
 		PortSign sign = PortPlugin.getInstance().getSignMap().get(loc);
 		
 		// Get sign
 		if(sign == null) {
 			try {
 				Block sB = block.getWorld().getBlockAt(loc.add(0, -2, 0));
				if(!(sB instanceof Sign)) throw new Exception("No sign underneath found!");
 
 				Sign signBlock = (Sign) sB;
 
 				if(!signBlock.getLine(0).equalsIgnoreCase("[cPort]")) throw new Exception("No valid cPort sign found!");
 
 				String worldName = signBlock.getLine(1);
 				World world = Bukkit.getServer().getWorld(worldName);
 				if(world == null) throw new Exception("No world with name " + worldName + " found!");
 
 				Location target = getLocationFromString(world, signBlock.getLine(2));
 				if(target == null ) throw new Exception("Invalid location: " + signBlock.getLine(2));
 				
 				sign = new PortSign(target);
 
 				String scriptName = signBlock.getLine(3);
 				if(scriptName != null && !scriptName.isEmpty()) sign.setScript(scriptName);
 					
 				// Save the sign
 				PortPlugin.getInstance().getSignMap().put(loc, sign);
 			} catch( Exception e ) {
				PortPlugin.log("Error porting player " + player.getName() + ": " + e.getMessage());
 				
 				return;
 			}
 		}
 		
 		// Off we go!
 		player.teleport(sign.getTarget());
 		// TODO add denizen script activation
 	}
 	
 	private Location getLocationFromString( World world, String location ) {
 		String[] locs = location.split(",");
 		
 		if(locs.length != 3) return null;
 
 		int x = Integer.parseInt(locs[0].trim());
 		int y = Integer.parseInt(locs[1].trim());
 		int z = Integer.parseInt(locs[2].trim());
 		
 		return new Location(world, x, y, z);
 	}
 }
