 package no.HON95.ButtonCommands;
 
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Effect;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.material.Lever;
 
 class RedstoneListener implements Listener
 {
 
 	private final BCMain PLUGIN;
 	Set<String> whiteList = null;
 	boolean enableRedstone = false;
 	boolean ignoreWhiteLists = false;
 	boolean outputInfo = false;
 	boolean recent = false;
 
 	RedstoneListener(BCMain instance)
 	{
 		PLUGIN = instance;
 	}
 
 	@EventHandler
 	public void onBlockRedstoneEvent(BlockRedstoneEvent ev)
 	{
 
 		if (!enableRedstone)
 			return;
 		
 		if (recent) //Check if this is a double post
 			return;
 
 		Block block = ev.getBlock();
 
 		if (!block.isBlockPowered() || block.getType() != Material.WALL_SIGN)
 			return;
 
 		Sign sign = (Sign) block.getState();
 		String[] lines = sign.getLines();
 
 		if (!lines[1].startsWith("/"))
 			return;
 
 		lines[1] = lines[1].replaceFirst("/", "");
 
 		String[] cmd = Misc.concatCmd(lines);
 		cmd[1] = Misc.insertAll(cmd[1], PLUGIN.getServer().getConsoleSender(), block);
 
 		if (cmd[0].equalsIgnoreCase("redstone"))
 			cmd[1] = cmd[1].replaceFirst("redstone ", "");
 		else if (cmd[0].equalsIgnoreCase("r"))
 			cmd[1] = cmd[1].replaceFirst("r ", "");
 		else
 			return;
 
 		if (cmd[1].length() == 0)
 			return;
 
 		if (ignoreWhiteLists || whiteList.contains(cmd[0]))
 		{
 			if (outputInfo)
 				PLUGIN.getLogger().info("Executing redstone console command: " + cmd[1]);
 			PLUGIN.getServer().dispatchCommand(PLUGIN.getServer().getConsoleSender(), cmd[1]);
 		}
 		
 		recent = true; //Set recent to true to prevent a double post
 		
 		// Wait for 1 tick then mark recent as false after the double posting period has ended
 		Bukkit.getScheduler().scheduleSyncDelayedTask(PLUGIN, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				recent = false;
 			}
 		}, 1L);
 	}
 }
