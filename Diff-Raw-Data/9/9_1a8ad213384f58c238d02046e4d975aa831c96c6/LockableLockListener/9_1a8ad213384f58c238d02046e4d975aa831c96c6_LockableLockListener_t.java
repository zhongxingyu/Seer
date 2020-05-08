 package uk.co.jacekk.bukkit.grouplock.listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import uk.co.jacekk.bukkit.baseplugin.BaseListener;
 import uk.co.jacekk.bukkit.grouplock.Config;
 import uk.co.jacekk.bukkit.grouplock.GroupLock;
 import uk.co.jacekk.bukkit.grouplock.Locker;
 import uk.co.jacekk.bukkit.grouplock.Permission;
 
 public class LockableLockListener extends BaseListener<GroupLock> {
 	
 	public LockableLockListener(GroupLock plugin){
 		super(plugin);
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerIntereact(PlayerInteractEvent event){
 		Block block = event.getClickedBlock();
 		Material type = block.getType();
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 		
		if (player.getItemInHand().getType() == Material.STICK && event.getAction() == Action.LEFT_CLICK_BLOCK && Permission.LOCK.hasPermission(player) && plugin.lockableBlocks.contains(type) && !plugin.config.getStringList(Config.IGNORE_WORLDS).contains(block.getWorld().getName())){
 			String blockName = type.name().toLowerCase().replace('_', ' ');
 			String ucfBlockName = Character.toUpperCase(blockName.charAt(0)) + blockName.substring(1);
 			
 			if (!plugin.locker.isBlockLocked(block)){
 				plugin.locker.lock(block, playerName);
 				player.sendMessage(plugin.formatMessage(ChatColor.GREEN + ucfBlockName + " locked"));
 			}else{
 				String owner = Locker.getOwner(block);
 				
 				if (!Permission.UNLOCK_LOCKED.hasPermission(player) && !owner.equals(playerName)){
 					player.sendMessage(plugin.formatMessage(ChatColor.RED + "That " + blockName + " is locked by " + owner));
 				}else{
 					plugin.locker.unlock(block);
 					player.sendMessage(plugin.formatMessage(ChatColor.GREEN + ucfBlockName + " unlocked"));
 				}
 			}
 			
 			event.setCancelled(true);
 		}
 	}
 	
 }
