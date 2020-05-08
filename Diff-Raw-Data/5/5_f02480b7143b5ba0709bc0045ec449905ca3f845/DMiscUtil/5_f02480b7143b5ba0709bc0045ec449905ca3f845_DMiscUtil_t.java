 package com.legit2.Demigods.Utilities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.util.BlockIterator;
 
 import com.legit2.Demigods.Demigods;
 
 public class DMiscUtil
 {
 	public static Demigods plugin;
 	
 	// Define variables
 	private static String plugin_name = "Demigods";
 	private static Logger log = Logger.getLogger("Minecraft");
 	
 	public DMiscUtil(Demigods instance)
 	{
 		plugin = instance;
 	}
 
 	/*
 	 *  getPlugin() : Returns an instance of the plugin.
 	 */
 	public static Demigods getPlugin()
 	{
 		return plugin;
 	}
 	
 	/*
 	 *  getImmortalList() : Gets list of currently immortal players.
 	 */
 	public static ArrayList<Integer> getImmortalList()
 	{		
 		// Define variables
 		ArrayList<Integer> immortalList = new ArrayList<Integer>();
 		HashMap<Integer, HashMap<String, Object>> characters = DDataUtil.getAllChars();
 		
 		for(Entry<Integer, HashMap<String, Object>> character : characters.entrySet())
 		{
 			int charID = character.getKey();
 			HashMap<String, Object> data = character.getValue();
 			
 			if(data.get("char_immortal") != null && DObjUtil.toBoolean(data.get("char_immortal"))) immortalList.add(charID);
 		}
 		
 		return immortalList;
 	}
 	
 	/*
 	 *  areAllied() : Returns true if (String)player is allied with (String)otherPlayer.
 	 */
 	public static boolean areAllied(Player player1, Player player2)
 	{
 		String playerAlliance = DPlayerUtil.getCurrentAlliance(player1);
 		String otherPlayerAlliance = DPlayerUtil.getCurrentAlliance(player2);
 		
 		if(playerAlliance.equals(otherPlayerAlliance)) return true;
 		else return false;
 	}
 	
 	/*
 	 *  customDamage() : Creates custom damage for (LivingEntity)target from (LivingEntity)source with ammount (int)amount.
 	 */
 	public static void customDamage(LivingEntity source, LivingEntity target, int amount, DamageCause cause)
 	{
 		if(target instanceof Player)
 		{
 			if(source instanceof Player)
 			{
 				target.setLastDamageCause(new EntityDamageByEntityEvent(source, target, cause, amount));
 			}
 			else target.damage(amount);
 		}
 		else target.damage(amount);
 	}
 	
 	/*
 	 *  taggedMessage() : Sends tagged message (String)msg to the (CommandSender)sender.
 	 */
 	public static void taggedMessage(CommandSender sender, String msg)
 	{
		sender.sendMessage(ChatColor.YELLOW + "[" + plugin_name + "] " + msg);
 	}
 	
 	/*
 	 *  taggedMessage() : Sends tagged message (String)msg to the (CommandSender)sender.
 	 */
 	public static void customTaggedMessage(CommandSender sender, String title, String msg)
 	{
		sender.sendMessage(ChatColor.YELLOW + "[" + title + "] " + msg);
 	}
 	
 	/*
 	 *  info() : Sends console message with "info" tag.
 	 */
 	public static void info(String msg)
 	{
 		log.info("[" + plugin_name + "] " + msg);
 	}
 	
 	/*
 	 *  warning() : Sends console message with "warning" tag.
 	 */
 	public static void warning(String msg)
 	{
 		log.warning("[" + plugin_name + "] " + msg);
 	}
 	
 	/*
 	 *  severe() : Sends console message with "severe" tag.
 	 */
 	public static void severe(String msg)
 	{
 		log.severe("[" + plugin_name + "] " + msg);
 	}
 	
 	/*
 	 *  serverMsg() : Send (String)msg to the server chat.
 	 */
 	public static void serverMsg(String msg)
 	{
 		plugin.getServer().broadcastMessage(msg);
 	}
 	
 	/*
 	 *  hasPermission() : Checks if (Player)player has permission (String)permission.
 	 */
 	public static boolean hasPermission(Player player, String permission)
 	{
 		if(player == null) return true;
 		return player.hasPermission(permission);
 	}
 	
 	/*
 	 *  hasPermissionOrOP() : Checks if (Player)player has permission (String)permission, or is OP.
 	 */
 	public static boolean hasPermissionOrOP(Player player, String permission)
 	{
 		if(player == null) return true;
 		if(player.isOp()) return true;
 		return player.hasPermission(permission);
 	}
 	
 	/*
 	 *  noPermission() : Command sender does not have permission to run command.
 	 */
 	public static boolean noPermission(Player player)
 	{
 		player.sendMessage(ChatColor.RED + "You do not have permission to run this command.");
 		return true;
 	}
 	
 	/*
 	 *  noConsole() : Sends a permission denial message to the console.
 	 */
 	public static boolean noConsole(CommandSender sender)
 	{
 		sender.sendMessage("This command can only be executed by a player.");
 		return true;
 	}
 	
 	/*
 	 *  noPlayer() : Sends a permission denial message to the console.
 	 */
 	public static boolean noPlayer(CommandSender sender)
 	{
 		sender.sendMessage("This command can only be executed by the console.");
 		return true;
 	}
 	
 	/*
 	 *  canUseDeity() : Checks is a player can use a specfic deity and returns a message
 	 */
 	public static boolean canUseDeity(Player player, String deity)
 	{		
 		// Check the player for DEITYNAME
 		if(!DCharUtil.hasDeity(DPlayerUtil.getCurrentChar(player), deity))
 		{
 			player.sendMessage(ChatColor.RED + "You haven't claimed " + deity + "! You can't do that!");
 			return false;
 		}
 		else if(!DCharUtil.isImmortal(player))
 		{
 			player.sendMessage(ChatColor.RED + "You can't do that, mortal!");
 			return false;
 		}
 		return true;
 	}
 	
 	/*
 	 *  canUseDeitySilent() : Checks is a player can use a specfic deity without returning a message.
 	 */
 	public static boolean canUseDeitySilent(Player player, String deity)
 	{		
 		// Check the player for DEITYNAME
 		if(!DCharUtil.hasDeity(DPlayerUtil.getCurrentChar(player), deity)) return false;
 		else if(!DCharUtil.isImmortal(player)) return false;
 		else return true;
 	}
 	
 	/*
 	 *  canTarget() : Checks if PVP is allowed in (Location)fallback for (Entity)player.
 	 */
     public static boolean canTarget(Entity player, Location fallback)
     {     
     	if(!(player instanceof Player)) return true;
     	else if(DDataUtil.hasPlayerData((Player) player, "temp_was_PVP") && DConfigUtil.getSettingBoolean("use_dynamic_pvp_zones")) return true;
     	else return !DZoneUtil.zoneNoPVP(fallback);
     }
     public static boolean canTarget(Entity player)
     {     
     	Location location = player.getLocation();
     	return canTarget(player, location);
     }
     
     /*
 	 *  autoTarget() : Returns the LivingEntity a (Player)player is targeting.
 	 */
     public static LivingEntity autoTarget(Player player)
     {
     	BlockIterator iterator = new BlockIterator(player.getWorld(), player.getLocation().toVector(), player.getEyeLocation().getDirection(), 0, 100);
         
         while (iterator.hasNext())
         {
             Block item = iterator.next();
             for(Entity entity : player.getNearbyEntities(100, 100, 100))
             {
             	if(entity instanceof LivingEntity)
             	{
 	                int acc = 2;
 	                for(int x = -acc; x < acc; x++)
 	                {
 	                    for(int z = -acc; z < acc; z++)
 	                    {
 	                        for(int y = -acc; y < acc; y++)
 	                        {
 	                            if(entity.getLocation().getBlock().getRelative(x, y, z).equals(item)) return (LivingEntity) entity;
 	                        }
 	                    }
 	                }
             	}
             }
         }
         return null;
     }
     
     /*
 	 *  playerStuckToggle() : Toggles holding a player's feet in place.
 	 */
     public static void playerStuckToggle(Player player)
     {
     	if(DDataUtil.hasPlayerData(player, "temp_player_hold")) DDataUtil.removePlayerData(player, "temp_player_hold");
     	else DDataUtil.savePlayerData(player, "temp_player_hold", true);
     }
 }
