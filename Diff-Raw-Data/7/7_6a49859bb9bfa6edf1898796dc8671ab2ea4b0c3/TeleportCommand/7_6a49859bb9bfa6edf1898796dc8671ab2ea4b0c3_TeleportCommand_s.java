 package com.randrdevelopment.propertygroup.command.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.randrdevelopment.propertygroup.PropertyGroup;
 import com.randrdevelopment.propertygroup.PropertyGroupConfig;
 import com.randrdevelopment.propertygroup.command.BaseCommand;
 import com.randrdevelopment.propertygroup.regions.TeleportUserTools;
 
 public class TeleportCommand extends BaseCommand{
 	private PropertyGroupConfig propertyConfig = null;
 	
 	public TeleportCommand(PropertyGroup plugin) {
         super(plugin);
         name = "Teleport";
         description = "Reloads the configuration.";
         usage = "/property tp";
         minArgs = 1;
         maxArgs = 2;
         identifiers.add("property tp");
         identifiers.add("ptp");
     }
 	
     @Override
     public void execute(CommandSender sender, String[] args) {
     	propertyConfig = plugin.getPropertyConfig();
     	
     	if (args.length == 1) {
     		// Teleport self to location
     	
     		// Validate permissions level
     		if (!sender.hasPermission("propertygroup.teleport"))
     		{
     			sender.sendMessage(plugin.getTag() + ChatColor.RED + "You do not have permission to use this command");
     			return;
     		}
     		
     		// Get property group name and property number
     		String[] pgargs = args[0].split("-");
     		if (pgargs.length != 2) {
     			sender.sendMessage(plugin.getTag() + ChatColor.RED + "Property Group Name is not Valid.");
     			return;
     		}
     		String propertyGroup = pgargs[0];
     		String propertyNumber = pgargs[1];
     		
     		// Validate the property group exists
     		if (propertyConfig.getConfigurationSection(propertyGroup) == null) {
     			sender.sendMessage(plugin.getTag() + ChatColor.RED + "Property Group does not exist.");
     			return;
     		}
     		
     		// Validate the property group is created
     		if (propertyConfig.getBoolean(propertyGroup + ".properties."+propertyNumber+".created") == false) {
     			sender.sendMessage(plugin.getTag() + ChatColor.RED + "Property not created");
     			return;
     		}
     		
     		Location newLocation = getPropertyLocation(propertyGroup, propertyNumber);
     		Player player = (Player)sender;
     		player.teleport(newLocation);
     		
     		sender.sendMessage(plugin.getTag() + "Teleported to property");
     	} else {
     		// Teleport other user to location
     		
     		// See if player is in the invite list
     		
     		
     	}
     }
     
     private Location getPropertyLocation (String propertyGroup, String propertyNumber) {
     	int x = propertyConfig.getInt(propertyGroup+".startlocation.x");
 		int y = propertyConfig.getInt(propertyGroup+".startlocation.y");
 		int z = propertyConfig.getInt(propertyGroup+".startlocation.z");
 		String worldname = propertyConfig.getString(propertyGroup+".startlocation.world");
 		int width = propertyConfig.getInt(propertyGroup+".width");
 		int length = propertyConfig.getInt(propertyGroup+".length");
 		World world = Bukkit.getWorld(worldname);
 		Location tpLocation = new Location(world, x, y, z);
     	tpLocation = TeleportUserTools.getCenterProperty(tpLocation, width, length);
     	int newy = TeleportUserTools.getYLocation(tpLocation);
     	tpLocation.setY(newy);
     	
     	return tpLocation;
     }
 }
