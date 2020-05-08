 package rc.rc.ThaPear.RemoteChests;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 
 public class rcPlayerListener extends PlayerListener
 {
 	public static rcPlugin plugin;
 	public rcPlayerListener(rcPlugin instance)
 	{
 		plugin = instance;
 	}
 
 	@Override
 	public void onPlayerInteract(PlayerInteractEvent event)
 	{
 		if(event.getAction().toString() != "RIGHT_CLICK_BLOCK" || event.getClickedBlock() == null || !(event.getClickedBlock().getState() instanceof Chest))
 		{	return;		}
 		Block block = event.getClickedBlock().getRelative(BlockFace.UP);
 		if(block.getState() instanceof Sign)
 		{
 			String signName = plugin.getSignName( ((Sign) block.getState()).getLines() );
 			if(!signName.equals(""))
 			{
 				if(rcPlugin.permissionsOn && !rcPlugin.Permissions.has(event.getPlayer(), rcPlugin.rclinkopenPerm))
					event.getPlayer().sendMessage(rcPlugin.messagePrefix + ChatColor.RED + "You do not have permission to open this.");
 				else if(!plugin.openChest( event.getPlayer(), signName, true))
 					plugin.createChest(event.getPlayer(), signName);
 				event.setCancelled(true);
 			}
 		}
 	}
 	@Override
 	public void onPlayerJoin(PlayerJoinEvent event)
 	{
 		event.getPlayer().sendMessage(rcPlugin.messagePrefix + "Type /rchelp to learn how to use this plugin.");
 	}
 }
