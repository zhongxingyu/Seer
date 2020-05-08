 package spia1001.InvFall;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 
 /*
 InvFall Plugin
 
 @author Chris Lloyd (SPIA1001)
 */
 
 public class InvFallPlayerListener extends PlayerListener
 {
 	private PlayerManager playerManager;
 	private InvFall plugin;
 	
 	public InvFallPlayerListener(PlayerManager pm,InvFall p)
 	{
 		playerManager = pm;
 		plugin = p;
 	}
 	public void onPlayerJoin(PlayerJoinEvent event)
 	{
 		Player player = event.getPlayer();
 		if(plugin.permissionWrapper.hasPermission(player,PermissionWrapper.NODE_INVFALL))
 			if(playerManager.playerIsEnabled(player))
				event.setJoinMessage(ChatColor.GREEN + "InvFall is Enabled");
 			else
				event.setJoinMessage(ChatColor.RED + "InvFall is Disabled");
 	}
 	public void onPlayerDropItem(PlayerDropItemEvent event)
 	{
 		Player player = event.getPlayer();
 		if(playerManager.playerIsEnabled(player) && plugin.permissionWrapper.hasPermission(player,PermissionWrapper.NODE_BLOCKFALL))
 			 new ItemFall(player,playerManager.freeFallEnabled(player),0);
 	}
 	public void onPlayerInteract(PlayerInteractEvent event)
 	{
 		Player player = event.getPlayer();
 		if(playerManager.playerIsEnabled(player) && plugin.permissionWrapper.hasPermission(player,PermissionWrapper.NODE_TOOLFALL))
 			new ToolFall(player,playerManager);
 	}
 }
