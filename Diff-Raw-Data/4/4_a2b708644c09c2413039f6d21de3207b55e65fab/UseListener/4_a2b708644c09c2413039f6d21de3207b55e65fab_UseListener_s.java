 package fr.frozentux.craftguard2.module.use;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 import fr.frozentux.craftguard2.CraftGuardPlugin;
 import fr.frozentux.craftguard2.PermissionChecker;
 
 public class UseListener implements Listener {
 
 	private CraftGuardPlugin plugin;
 	
 	public UseListener(CraftGuardPlugin plugin){
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void onItemUse(PlayerInteractEvent e){
 		if(!e.getAction().equals(Action.PHYSICAL)){
 			Player p = e.getPlayer();
 			ItemStack i = e.getItem();
			
			boolean denied = !PermissionChecker.check(p, i.getTypeId(), i.getData().getData(), plugin, "use");
 			if(denied){
 				e.setCancelled(denied);
 				p.updateInventory();
 			}
 		}
 	}
 	
 }
