 package info.plugmania.mazemania.listeners;
 
 
 import info.plugmania.mazemania.MazeMania;
 import info.plugmania.mazemania.Util;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 
 public class PlayerListener implements Listener {
 
 	MazeMania plugin;
 	public PlayerListener(MazeMania instance) {
 		plugin = instance;
 	}
 
 	@EventHandler
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
 		if(event.isCancelled()) return;
 		if(!plugin.arena.playing.contains(event.getPlayer())) return;
 		if(!plugin.hasPermission(event.getPlayer(), "arena.command")) event.setCancelled(true);
 	}
 		@EventHandler
		public void Creeper(EntityExplodeEvent  event){
			if(event.getEntity() instanceof Creeper || event.getEntity() instanceof TNTPrimed);
			event.setCancelled(true);
 		}
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent event){
 		Player player = event.getEntity();
 		if(!plugin.arena.playing.contains(player)) return;
 		
 		plugin.arena.playing.remove(player);
 		Bukkit.broadcastMessage(Util.formatBroadcast(player.getName() + " has died in the maze!"));
 		if(plugin.arena.playing.isEmpty()){
 			plugin.arena.setPlaying(false);
 			Bukkit.broadcastMessage(Util.formatBroadcast("Maze game forfited, all players left!"));
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event){
 		Player player = event.getPlayer();
 		if(!plugin.arena.playing.contains(player)) return;
 		
 		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
 			
 			if(event.getClickedBlock().getType().equals(Material.MAGMA_CREAM)){
 				plugin.mazeCommand.setCommand.selected.put(player, event.getClickedBlock().getLocation());
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onChestInteract(PlayerInteractEvent event){
 		Player player = event.getPlayer();
 		if(!plugin.arena.playing.contains(player)) return;
 		
 		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
 			if(event.getClickedBlock().getType().equals(Material.CHEST)){
 				event.setCancelled(true);
 				
 				Inventory appleInventory = plugin.getServer().createInventory(null, 54, "Maze Chest");
 				ItemStack apple = new ItemStack(Material.APPLE, 1);
 				appleInventory.setItem(13, apple);
 				appleInventory.setItem(21, apple);
 				appleInventory.setItem(23, apple);
 				appleInventory.setItem(31, apple);
 				ItemStack goldenApple = new ItemStack(Material.GOLDEN_APPLE, 1);
 				appleInventory.setItem(22, goldenApple);
 				
 				player.openInventory(appleInventory);
 			}
 		}
 	}
 	
 }
