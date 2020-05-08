 package musician101.controlcreativemode.lib;
 
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class WarningMessages
 {
 	/** Warning message when a player in Creative mode attacks a mob or player. */
 	public static String getAttackWarning(Player player, Entity entity)
 	{
 		String attackedEntity = "";
 		if (entity instanceof Player)
 			attackedEntity = ((Player) entity).getName();
 		else
 			attackedEntity = "a " + entity.getType().toString();
 		
 		return Messages.PREFIX_INFO_WARNING + player.getName() + " attacked " + attackedEntity + " at X: " + player.getLocation().getBlockX() + ", Y: "
 				+ player.getLocation().getBlockY() + ", Z: " + player.getLocation().getBlockZ() + ".";
 	}
 	
 	/** Warning message when a player places a block. */
 	public static String getBlockWarning(Player player, Block block)
 	{
		return Messages.PREFIX_INFO_WARNING + player.getName() + " placed " + block.getType().toString() + " at X: " + block.getLocation().getBlockX() + ", Y: "
 				+ block.getLocation().getBlockY() + ", Z: " + block.getLocation().getBlockZ() + ".";
 	}
 
 	/** Warning message when a player right clicks a block. */
 	public static String getBlockInteractWarning(Player player, String material, Location location)
 	{
 		return Messages.PREFIX_INFO_WARNING + player.getName() + " opened a " + material + " at X: " + location.getBlockX() + ", Y: " + location.getBlockY() + ", Z: " + location.getBlockZ() + ".";
 	}
 	
 	/** Warning message when a player uses a water/lava bucket. */
 	public static String getBucketWarning(Player player, Material material, Location location)
 	{
		return Messages.PREFIX_INFO_WARNING + player.getName() + " placed a " + material + " at X: " + location.getBlockX() + ", Y: " + location.getBlockY()
 				+ ", Z: " + location.getBlockZ();
 	}
 	
 	/** Warning message when a TNT Minecart is placed. */
 	public static String getCartWarning(Player player, ItemStack item, Location location)
 	{
 		return Messages.PREFIX_INFO_WARNING + player.getName() + " has placed an " + item.getType().toString() + " at X: " + location.getBlockX() + ", Y: " + location.getBlockY() + ", Z: " + location.getBlockZ() + ".";
 	}
 	
 	/** Warning message when a player right clicks a mob. */
 	public static String getEntityInteractWarning(Player player, EntityType entity, Location location)
 	{
 		return Messages.PREFIX_INFO_WARNING + player.getName() + " has interacted with a " + entity.toString() + " at X: " + location.getBlockX() + ", Y: " + location.getBlockY() + ", Z: " + location.getBlockZ() + ".";
 	}
 	
 	/** Warning message when a player drops an item. */
 	public static String getItemDropWarning(Player player, String material, Location location)
 	{
 		return Messages.PREFIX_INFO_WARNING + player.getName() + " has dropped a " + material + " at X: " + Math.round(location.getX()) + ", Y: " + Math.round(location.getY()) + ", Z: " + Math.round(location.getZ()) + ".";
 	}
 	
 	/** Warning message when a player changes gamemodes with items in their inventory. */
 	public static String getItemKeptWarning(Player player)
 	{
 		return Messages.PREFIX_INFO_WARNING + player.getName() + " has kept items in their inventory when switching modes.";
 	}
 	
 	/** Notification message when a player uses /creative or /survival. */
 	public static String getModeMsg(GameMode gm)
 	{
 		return Messages.PREFIX_GAMEMODE + "You are now in " + gm.toString() + ".";
 	}
 	
 	/** Warning message when a player changes gamemode using the plugin's commands. */
 	public static String getModeWarning(Player player, GameMode gm)
 	{
 		return Messages.PREFIX_INFO_WARNING + player.getName() + " is now in " + gm.toString() + ".";
 	}
 	
 	/** Warning message when a player right clicks a throwable item. */
 	public static String getThrownItemWarning(Player player, ItemStack item, Location location)
 	{
 		return Messages.PREFIX_INFO_WARNING + player.getName() + " threw a " + item.getType().toString() + " at X: " + Math.round(location.getX()) + ", Y: " + Math.round(location.getY()) + ", Z: " + Math.round(location.getZ()) + ".";
 	}
 	
 	/**  Warning message when a player uses a spawn egg. */
 	public static String getSpawnWarning(Player player, short data, Location location)
 	{
 		String mob = "";
 		if (data == 50)
 			mob = "a CREEPER";
 		else if (data == 51)
 			mob = "a SKELETON";
 		else if (data  == 52)
 			mob = "a SPIDER";
 		else if (data == 54)
 			mob = "a ZOMBIE";
 		else if (data == 55)
 			mob = "a SLIME";
 		else if (data == 56)
 			mob = "a GHAST";
 		else if (data == 57)
 			mob = "a ZOMBIE PIGMAN";
 		else if (data == 58)
 			mob = "an ENDERMAN";
 		else if (data == 59)
 			mob = "a CAVE SPIDER";
 		else if (data == 60)
 			mob = "a SILVERFISH";
 		else if (data == 61)
 			mob = "a BLAZE";
 		else if (data == 62)
 			mob = "a MAGMA CUBE";
 		else if (data == 65)
 			mob = "a BAT";
 		else if (data == 66)
 			mob = "a WITCH";
 		else if (data == 90)
 			mob = "a PIG";
 		else if (data == 91)
 			mob = "a SHEEP";
 		else if (data == 92)
 			mob = "a COW";
 		else if (data == 93)
 			mob = "a CHICKEN";
 		else if (data == 94)
 			mob = "a SQUID";
 		else if (data == 95)
 			mob = "a WOLF";
 		else if (data == 96)
 			mob = "a MOOSHROOM";
 		else if (data == 97)
 			mob = "a SNOW GOLEM";
 		else if (data == 98)
 			mob = "an OCELOT";
 		else if (data == 99)
 			mob = "a IRON GOLEM";
 		else if (data == 120)
 			mob = "a VILLAGER";
 		else
 			mob = "an UNIDENTIFIED MOB";
 		
 		return Messages.PREFIX_INFO_WARNING + player.getName() + " spawned " + mob + " at X: " + location.getBlockX() + ", Y: " + location.getBlockY() + ", Z: " + location.getBlockZ() + ".";
 	}
 }
