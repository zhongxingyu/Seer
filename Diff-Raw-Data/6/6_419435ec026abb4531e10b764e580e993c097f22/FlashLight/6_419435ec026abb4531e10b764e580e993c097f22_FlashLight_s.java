 package ykt.BeYkeRYkt.BkrTorchLight.Recode.Listeners.Default;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.inventory.ItemStack;
 
 import ykt.BeYkeRYkt.BkrTorchLight.Recode.BTL;
 
 public class FlashLight implements Listener{
 	
 	public BTL plugin;
 
	public ykt.BeYkeRYkt.BkrTorchLight.Recode.Manager.Items.FlashLight flash;
 	
 	public FlashLight(BTL instance)
 	{
 	  this.plugin = instance;
 	}
 
 	@EventHandler
 	public void onPlayerClick(PlayerInteractEvent event){
 		Player player = event.getPlayer();
 		Location l = player.getLocation();
 		ItemStack item2 = player.getInventory().getItemInHand();
 	    if(player.hasPermission("BTL.light")){
 	          if (item2==null) return;
 	          String name = "FlashLight";
 	          if(item2.getType() != Material.STICK) return;
 	        if(name.equals(item2.getItemMeta().getDisplayName())){
 	    if ((event.getAction() == (Action.RIGHT_CLICK_AIR) || event.getAction() == (Action.RIGHT_CLICK_BLOCK))) {
 	Item item = player.getWorld().dropItem(player.getLocation(), new ItemStack (Material.STICK));
 	plugin.items.put(item, item.getLocation());
 	player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
 	player.updateInventory();
 	player.getWorld().playEffect(l, Effect.SMOKE, 20);
 	flash.createLightSource(l, player, 14);
 	    }
 	    }
 	    }
 	}
 	
 	
 	@EventHandler
 	  public void onPickup(PlayerPickupItemEvent event) {
 		if(plugin.items.containsKey(event.getItem())){
 			event.setCancelled(true);
 		}
 	}
 	
	    	}
