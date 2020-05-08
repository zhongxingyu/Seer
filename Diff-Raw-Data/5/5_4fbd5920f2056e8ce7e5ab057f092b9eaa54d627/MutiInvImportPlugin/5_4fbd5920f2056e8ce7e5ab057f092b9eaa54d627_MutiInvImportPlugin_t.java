 package uk.co.marcuscobden.mutiinvimport;
 
 import java.lang.reflect.Field;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Logger;
 
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.NBTTagCompound;
 import net.minecraft.server.WorldNBTStorage;
 import net.minecraft.server.WorldServer;
 
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
 import uk.co.tggl.pluckerpluck.multiinv.MultiInvPlayerData;
 
 public class MutiInvImportPlugin extends JavaPlugin
 {
 	static final Logger log = Logger.getLogger("Minecraft");
 	static String pluginName;
 	
 	private MultiInv multiinv;
 	
 	public void onDisable()
 	{
 		log.info("[" + pluginName + "] Plugin disabled.");
 	}
 
 	public void onEnable()
 	{
 		pluginName = this.getDescription().getName();
 		
 		multiinv = (MultiInv) getServer().getPluginManager().getPlugin("MultiInv");
 		if (multiinv == null)
 		{
 			this.getServer().getPluginManager().disablePlugin(this);
 			return;
 		}
 		
 		log.info("[" + pluginName + "] Plugin enabled.");
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args)
 	{
 		
		if (! command.getName().equals("multiinvimport"))
 			return false;
 		
 		if (args.length != 3)
 		{
 			sender.sendMessage("Incorrect arguments.");
 			return false;
 		}
 		String player_name = args[0];
 		String from_world_name = args[1];
 		String to_world_group_name = args[1];
 		
 		OfflinePlayer offlinePlayer = this.getServer().getOfflinePlayer(player_name);
 		
 		WorldServer world_from = ((CraftWorld) getServer().getWorld(from_world_name)).getHandle();
 		WorldNBTStorage s = (WorldNBTStorage) world_from.getDataManager().getPlayerFileData();
 		
 		NBTTagCompound player_nbt = s.getPlayerData(offlinePlayer.getName());
 
 		if (player_nbt == null) {
 			sender.sendMessage("No player NBT found...");
 			return true;
 		}
 		
 		boolean isPlayerinTargetWorldGroup;
 		
 		if (! offlinePlayer.isOnline())
 			isPlayerinTargetWorldGroup = false;
 		else
 		{
 			CraftPlayer onlinePlayer = ((CraftPlayer)offlinePlayer);
 			
 			String worldname = onlinePlayer.getLocation().getWorld().getName();
 			
 			ConcurrentHashMap<String, String> sharesMap;
 			try
 			{
				Field erk = multiinv.getClass().getDeclaredField("sharesMap");
 				erk.setAccessible(true);
 				sharesMap = (ConcurrentHashMap<String, String>) erk.get(null);
 			} catch (Exception e)
 			{
 				sender.sendMessage("Failed to poke around inside MultiInv...");
 				e.printStackTrace();
 				return true;
 			}
 			
 			isPlayerinTargetWorldGroup = sharesMap.containsKey(worldname) ? sharesMap.get(sharesMap).equals(to_world_group_name) : false;
 		}
 		
 		if (isPlayerinTargetWorldGroup)
 		{
 			EntityPlayer player = ((CraftPlayer)offlinePlayer).getHandle();
 			player.inventory.b(player_nbt.getList("Inventory"));
 		}
 		else
 		{
 			Player player = new FakePlayer(player_name, player_nbt);
 			
 			MultiInvPlayerData.storeCurrentInventory(player, to_world_group_name);
 		}
 		
 		sender.sendMessage("Success!");
 		
 		return true;
 	}
 }
