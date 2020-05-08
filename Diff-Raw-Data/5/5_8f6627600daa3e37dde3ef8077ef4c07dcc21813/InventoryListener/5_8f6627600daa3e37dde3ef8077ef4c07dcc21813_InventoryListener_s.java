 package net.minekingdom.snaipe.Thieves.listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.ItemStack;
 import org.getspout.spout.inventory.SpoutCraftItemStack;
 import org.getspout.spoutapi.event.inventory.InventoryClickEvent;
 import org.getspout.spoutapi.event.inventory.InventoryCloseEvent;
 import org.getspout.spoutapi.event.inventory.InventorySlotType;
 
 import net.minecraft.server.Packet103SetSlot;
 
 import net.minekingdom.snaipe.Thieves.ItemValues;
 import net.minekingdom.snaipe.Thieves.Language;
 import net.minekingdom.snaipe.Thieves.ThievesPlayer;
 import net.minekingdom.snaipe.Thieves.Thieves;
 import net.minekingdom.snaipe.Thieves.events.ItemStealEvent;
 
 public class InventoryListener implements Listener {
     
     private final Thieves plugin;
     
     public InventoryListener()
     {
         plugin = Thieves.getInstance();
     }
     
     @EventHandler
     public void onInventoryClick(InventoryClickEvent event)
     {
         if ( event.isCancelled() )
             return;
 
         if ( event.getPlayer() == null )
             return;
         
         final ThievesPlayer thief = plugin.getPlayerManager().getPlayer(event.getPlayer());
         final ThievesPlayer target = plugin.getPlayerManager().getTarget(thief);
         
         if ( target != null )
         {
             if ( event.isShiftClick() )
             {
                 event.setCancelled(true);
                 return;
             }
             
             if ( event.getSlotType().equals(InventorySlotType.PACK) )
             {
                 event.setCancelled(true);
                 return;
             }
             
             if ( event.getCursor() != null )
             {
                 if ( event.getSlotType().equals(InventorySlotType.CONTAINER) )
                 {
                     event.setCancelled(true);
                 }
                 return;
             }
             
             if ( event.getItem() == null )
                 return;
             
             final ItemStack item = event.getItem();
             final int slot = event.getSlot();
             
             if ( thief.getMaxItemWealth() < thief.getItemWealth() + ItemValues.valueOf(item.getType()) )
             {
                 thief.sendMessage(ChatColor.RED + Language.cannotStealMore);
                 event.setCancelled(true);
                 return;
             }
             
             int rand = (int)(Math.random()*100) + 1;
             boolean successful = rand <= 100*((float) 1 - ((float) ItemValues.valueOf(item.getType())) / ((float) thief.getThiefLevel() + 9 ));
             
             ItemStealEvent stealEvent = new ItemStealEvent(thief, target, event.getItem(), successful);
             plugin.getServer().getPluginManager().callEvent(stealEvent);
             
             if ( stealEvent.isCancelled() )
             {
                 event.setCancelled(true);
                 return;
             }
             else
             {
                 if ( !stealEvent.isSuccessful() )
                 {
                     target.sendMessage(ChatColor.RED + Language.thiefSpotted);
                     event.setCancelled(true);
                     return;
                 }
                 
                 event.setResult(Result.DENY);
                 
                 thief.addThiefExperience(ItemValues.valueOf(item.getType()));
                 
                 if ( thief.getThiefExperience() > Math.ceil(100*Math.pow(1.6681, thief.getThiefLevel())) )
                 {
                     thief.sendMessage(ChatColor.RED + Language.levelUp);
                     thief.incrementThiefLevel();
                 }
                 
                thief.addItemToWealth(item);
                
                 plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 
 					@Override
 					public void run() 
 					{
 						
 						net.minecraft.server.ItemStack cursor = SpoutCraftItemStack.getCraftItemStack(new ItemStack(item.getType(), 1, item.getDurability(), item.getData().getData())).getHandle();
 			            ((CraftPlayer) thief.getPlayer()).getHandle().inventory.b(cursor);
 			            
 			            if ( item.getAmount() > 1 )
 			            {
 			            	net.minecraft.server.ItemStack clicked = SpoutCraftItemStack.getCraftItemStack(new ItemStack(item.getType(), item.getAmount() - 1, item.getDurability(), item.getData().getData())).getHandle();
 			                ((CraftPlayer) thief.getPlayer()).getHandle().activeContainer.b(slot).c(clicked);
 			            }
 			            else
 			            {
 			                ((CraftPlayer) thief.getPlayer()).getHandle().activeContainer.b(slot).c(null);
 			            }
 			            
 			            ((CraftPlayer) thief.getPlayer()).getHandle().netServerHandler.sendPacket(new Packet103SetSlot(-1, -1, ((CraftPlayer) thief.getPlayer()).getHandle().inventory.l()));
 					}
 		        	
 		        }, 1L);
             }
         }
     }
     
     @EventHandler
     public void onInventoryClose(InventoryCloseEvent event)
     {
         if (event.isCancelled())
             return;
         
         final Player player = event.getPlayer();
         if ( player != null )
         {
             ThievesPlayer thief = plugin.getPlayerManager().getPlayer(player);
             
             if ( plugin.getPlayerManager().isThief(thief) )
             {
                 plugin.getPlayerManager().removeThief(thief);
                 thief.setCooldown(plugin.getSettingManager().getCooldown());
                 thief.setItemWealth(0);
             }
         }
             
     }
 }
