 package net.alexben.Slayer.Utilities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import net.alexben.Slayer.Core.Slayer;
 
 import org.bukkit.*;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Firework;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.meta.FireworkMeta;
 
 /**
  * Utility that handles miscellaneous methods.
  */
 public class MiscUtil
 {
 	// Define variables
 	private static String pluginName = Slayer.plugin.getDescription().getName();
 	private static ChatColor pluginColor = ChatColor.RED;
 	private static final Logger log = Logger.getLogger("Minecraft");
 
 	/**
 	 * Returns the logger for the current plugin instance.
 	 * 
 	 * @return the logger instance.
 	 */
 	public static Logger getLog()
 	{
 		return log;
 	}
 
 	/**
 	 * Returns the string with key <code>key</code> from the Strings.yml
 	 * FileConfiguration.
 	 * 
 	 * @return String
 	 */
 	public static String getString(String key)
 	{
		if(Slayer.plugin.getConfig().getString(key) == null) return null;
		return ChatColor.translateAlternateColorCodes('&', Slayer.plugin.getConfig().getString(key));
 	}
 
 	/**
 	 * Returns the value of the item/entity with the name <code>name</code> from the values.yml FileConfiguration.
 	 * 
 	 * @param name the name to whose value to look for.
 	 * @return Integer
 	 */
 	public static int getValue(String name)
 	{
 		// Unimplemented as of now
 		return 0;
 	}
 
 	/**
 	 * Sends <code>msg</code> to the console with type <code>type</code>.
 	 * 
 	 * @param type the type of message.
 	 * @param msg the message to send.
 	 */
 	public static void log(String type, String msg)
 	{
 		if(type.equalsIgnoreCase("info")) log.info("[" + pluginName + "] " + msg);
 		else if(type.equalsIgnoreCase("warning")) log.warning("[" + pluginName + "] " + msg);
 		else if(type.equalsIgnoreCase("severe")) log.severe("[" + pluginName + "] " + msg);
 	}
 
 	/**
 	 * Sends a server-wide message prepended with the plugin name if <code>tag</code> is true.
 	 * 
 	 * @param tag if true, the message is prepended with "[net.alexben.Slayer]"
 	 * @param msg the message to send.
 	 */
 	public static void serverMsg(boolean tag, String msg)
 	{
 		if(tag)
 		{
 			Bukkit.getServer().broadcastMessage(pluginColor + "[" + pluginName + "] " + ChatColor.RESET + msg);
 		}
 		else Bukkit.getServer().broadcastMessage(msg);
 
 	}
 
 	/**
 	 * Sends a message to a player prepended with the plugin name.
 	 * 
 	 * @param player the player to message.
 	 * @param msg the message to send.
 	 */
 	public static void sendMsg(OfflinePlayer player, String msg)
 	{
 		player.getPlayer().sendMessage(pluginColor + "[" + pluginName + "] " + ChatColor.RESET + msg);
 	}
 
 	/**
 	 * Sends a message to a player prepended with "SL Admin".
 	 * 
 	 * @param player the player to message.
 	 * @param msg the message to send.
 	 */
 	public static void sendAdminMsg(OfflinePlayer player, String msg)
 	{
 		player.getPlayer().sendMessage(pluginColor + "[SL Admin] " + ChatColor.RESET + msg);
 	}
 
 	/**
 	 * Sends a no permission error to <code>player</code> and always returns true.
 	 * 
 	 * @param player the player to message.
 	 * @return boolean
 	 */
 	public static boolean noPermission(Player player)
 	{
 		sendMsg(player, ChatColor.RED + "You don't have permission to use that command.");
 		return true;
 	}
 
 	/**
 	 * Returns true if <code>player</code> has the permission called <code>permission</code>.
 	 * 
 	 * @param player the player to check.
 	 * @param permission the permission to check for.
 	 * @return boolean
 	 */
 	public static boolean hasPermission(OfflinePlayer player, String permission)
 	{
 		return player == null || player.getPlayer().hasPermission(permission);
 	}
 
 	/**
 	 * Returns true if <code>player</code> has the permission called <code>permission</code> or is an OP.
 	 * 
 	 * @param player the player to check.
 	 * @param permission the permission to check for.
 	 * @return boolean
 	 */
 	public static boolean hasPermissionOrOP(OfflinePlayer player, String permission)
 	{
 		return player == null || player.isOp() || player.getPlayer().hasPermission(permission);
 	}
 
 	/**
 	 * Returns an ArrayList of all net.alexben.Slayer participants.
 	 * 
 	 * @return ArrayList
 	 */
 	public static ArrayList<OfflinePlayer> getAllParticipants()
 	{
 		ArrayList<OfflinePlayer> players = new ArrayList<OfflinePlayer>();
 
 		for(Map.Entry<String, HashMap<String, Object>> player : DataUtil.getAllData().entrySet())
 		{
 			players.add(Bukkit.getPlayer(player.getKey()));
 		}
 
 		return players;
 	}
 
 	/**
 	 * Shoots a random firework at the <code>location</code>.
 	 * 
 	 * @param location the location to launch the firework from.
 	 */
 	public static void shootRandomFirework(Location location)
 	{
 		// Define variables
 		Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
 		FireworkMeta fireworkMeta = firework.getFireworkMeta();
 		Random r = new Random();
 
 		// Get random type
 		int rt = r.nextInt(4) + 1;
 		FireworkEffect.Type type = FireworkEffect.Type.BALL;
 		if(rt == 1) type = FireworkEffect.Type.BALL;
 		if(rt == 2) type = FireworkEffect.Type.BALL_LARGE;
 		if(rt == 3) type = FireworkEffect.Type.BURST;
 		if(rt == 4) type = FireworkEffect.Type.CREEPER;
 		if(rt == 5) type = FireworkEffect.Type.STAR;
 
 		// Get random colors
 		Color color1 = getColor(r.nextInt(17) + 1);
 		Color color2 = getColor(r.nextInt(17) + 1);
 
 		// Create the effect
 		FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(color1).withFade(color2).with(type).trail(r.nextBoolean()).build();
 
 		// Apply the effect
 		fireworkMeta.addEffect(effect);
 
 		// Get random power
 		int power = r.nextInt(2) + 1;
 		fireworkMeta.setPower(power);
 
 		// Apply everything
 		firework.setFireworkMeta(fireworkMeta);
 	}
 
 	/**
 	 * Returns a color based on i <code>i</code>.
 	 * 
 	 * @param i the number of color to return.
 	 * @return Color
 	 */
 	public static Color getColor(int i)
 	{
 		Color color = null;
 
 		if(i == 1) color = Color.AQUA;
 		if(i == 2) color = Color.BLACK;
 		if(i == 3) color = Color.BLUE;
 		if(i == 4) color = Color.FUCHSIA;
 		if(i == 5) color = Color.GRAY;
 		if(i == 6) color = Color.GREEN;
 		if(i == 7) color = Color.LIME;
 		if(i == 8) color = Color.MAROON;
 		if(i == 9) color = Color.NAVY;
 		if(i == 10) color = Color.OLIVE;
 		if(i == 11) color = Color.ORANGE;
 		if(i == 12) color = Color.PURPLE;
 		if(i == 13) color = Color.RED;
 		if(i == 14) color = Color.SILVER;
 		if(i == 15) color = Color.TEAL;
 		if(i == 16) color = Color.WHITE;
 		if(i == 17) color = Color.YELLOW;
 
 		return color;
 	}
 }
