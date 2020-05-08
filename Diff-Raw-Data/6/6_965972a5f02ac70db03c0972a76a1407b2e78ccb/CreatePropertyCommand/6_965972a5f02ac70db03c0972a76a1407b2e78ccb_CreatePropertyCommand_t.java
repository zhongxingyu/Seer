 package com.randrdevelopment.propertygroup.command.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 
 import com.randrdevelopment.propertygroup.command.BaseCommand;
 import com.randrdevelopment.propertygroup.PropertyGroup;
 import com.randrdevelopment.propertygroup.PropertyGroupConfig;
 import com.randrdevelopment.propertygroup.regions.RegionTools;
 import com.randrdevelopment.propertygroup.regions.SchematicTools;
 import com.randrdevelopment.propertygroup.regions.TeleportUserTools;
 
 public class CreatePropertyCommand extends BaseCommand{
 	private PropertyGroupConfig propertyConfig = null;
 	
 	public CreatePropertyCommand(PropertyGroup plugin) {
         super(plugin);
         name = "CreateProperty";
         description = "Creates a Property Manually.";
         usage = "/property createproperty <groupname> [user]";
         minArgs = 1;
         maxArgs = 2;
         identifiers.add("property createproperty");
     }
 	
     @Override
     public void execute(CommandSender sender, String[] args) {
     	propertyConfig = plugin.getPropertyConfig();
     	String propertyGroup = args[0].toLowerCase();
     	
     	// Validate permissions level
     	if (!sender.hasPermission("propertygroup.createproperty"))
     	{
     		sender.sendMessage(plugin.getTag() + ChatColor.RED + "You do not have permission to use this command");
     		return;
     	}
     	
     	// Verify Property Group Exists
     	if (propertyConfig.getConfigurationSection(propertyGroup) == null)
 		{
 			sender.sendMessage(plugin.getTag()+ChatColor.RED+"Property Group '"+propertyGroup+"' does not exist.");
 			return;
 		}
     		
     	// Lets get the row and column of the next free property..
     	int Rows = propertyConfig.getInt(propertyGroup+".rows");
 		int Cols = propertyConfig.getInt(propertyGroup+".cols");
 		
     	int qty = Rows * Cols;
     	boolean noproperties = true;
 		
     	for(int i=1; i<=qty; i++){
     		if (propertyConfig.getBoolean(propertyGroup+".properties."+i+".created") == false){
     			noproperties = false;
 				
     			// Found an empty spot..
     			int x = propertyConfig.getInt(propertyGroup+".startlocation.x");
     			int y = propertyConfig.getInt(propertyGroup+".startlocation.y");
     			int z = propertyConfig.getInt(propertyGroup+".startlocation.z");
 				String worldname = propertyConfig.getString(propertyGroup+".startlocation.world");
 				int width = propertyConfig.getInt(propertyGroup+".width");
 				int length = propertyConfig.getInt(propertyGroup+".length");
 				int height = propertyConfig.getInt(propertyGroup+".height");
 				int spacing = propertyConfig.getInt(propertyGroup+".propertyspacing");
 				int row = propertyConfig.getInt(propertyGroup+".properties."+i+".row");
 				int col = propertyConfig.getInt(propertyGroup+".properties."+i+".col");
 				
 				// Set Starting Point...
 				x = ((width + spacing) * (row - 1)) + x;
 				z = ((length + spacing) * (col - 1)) + z;
 				
 				int blocks = length * width * height;
 					
 				SchematicTools.reload(propertyGroup, worldname, x, y, z, blocks);
 				
 				sender.sendMessage(plugin.getTag()+"Created Property");
 				
 				// Get target player name
 				String playerName = null;
 				String playerDisplayName = null;
 				if (args.length == 2) {
 					playerName = args[1];
 					Player targetPlayer = Bukkit.getServer().getPlayer(playerName);
					if (targetPlayer != null){
						playerName = targetPlayer.getName();
						playerDisplayName = targetPlayer.getDisplayName();
					}
 				}
 				
 				// Create Region if configured to do so...
 				if (propertyConfig.getBoolean(propertyGroup+".createregion")) {	
 					if (!RegionTools.createProtectedRegion(propertyGroup+"-"+i, worldname, x, x+width-1, 0, 255, z, z+length-1, playerName, propertyConfig, propertyGroup))
 						sender.sendMessage(ChatColor.RED+"Error creating region...");
 					else
 						sender.sendMessage(ChatColor.AQUA+"Created worldguard region: "+propertyGroup+"-"+i);
 				}
 				
 				// Teleport User if configured to do so...
 				if (propertyConfig.getBoolean(propertyGroup+".userteleport")) {
 					if (playerName != null) {
 						// Validate user is online...
 						Player targetPlayer = Bukkit.getServer().getPlayer(playerName);
 				        if (targetPlayer == null) {
 				        	sender.sendMessage(ChatColor.RED+playerName+" is not online! Unable to teleport.");
 				        } else {
 				        	World world = Bukkit.getWorld(worldname);
 				        	Location tpLocation = new Location(world, x, y, z);
 				        	tpLocation = TeleportUserTools.getCenterProperty(tpLocation, width, length);
 				        	int newy = TeleportUserTools.getYLocation(tpLocation);
 				        	tpLocation.setY(newy);
 				        	targetPlayer.teleport(tpLocation);
 				        	sender.sendMessage(ChatColor.AQUA+playerDisplayName+" teleported to new property.");
 				        }
 					}
 				}
 				
 				propertyConfig.set(propertyGroup+".properties."+i+".created", true);
 				if (playerName != null) {
 					propertyConfig.set(propertyGroup+".properties."+i+".owner", playerName);
 				}
 				
 				propertyConfig.save();
 					
 				break;
 			}
 		}
 		
 		if (noproperties){
 			sender.sendMessage(plugin.getTag()+ChatColor.RED+"Property Group '"+propertyGroup+"' is full.");
 		}
     }
 }
