 package net.minecore.mineplot;
 
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class PlayerPlotSelector implements Listener {
 
 	private int tool;
 	private Map<String, Location> location1, location2;
 
 	public PlayerPlotSelector(int tool) {
 		this.tool = tool;
 		
 		location1 = new TreeMap<String, Location>();
 		location2 = new TreeMap<String, Location>();
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent e){
 		
 		if(e.getMaterial().getId() != tool)
 			return;
 		
 		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
 			location2.put(e.getPlayer().getName(), e.getClickedBlock().getLocation());
 			e.getPlayer().sendMessage(ChatColor.BLUE + "Selected corner two!");
 		} else if(e.getAction().equals(Action.LEFT_CLICK_BLOCK)){
 			location1.put(e.getPlayer().getName(), e.getClickedBlock().getLocation());
 			e.getPlayer().sendMessage(ChatColor.BLUE + "Selected corner one!");
 		}
 	}
 	
 	public Location getPlayerLocation1(String name){
 		return location1.get(name);
 	}
 	
 	public Location getPlayerLocation1(Player p){
 		return getPlayerLocation1(p.getName());
 	}
 	
 	public Location getPlayerLocation2(String name){
 		return location2.get(name);
 	}
 	
 	public Location getPlayerLocation2(Player p){
 		return getPlayerLocation2(p.getName());
 	}
 
 }
