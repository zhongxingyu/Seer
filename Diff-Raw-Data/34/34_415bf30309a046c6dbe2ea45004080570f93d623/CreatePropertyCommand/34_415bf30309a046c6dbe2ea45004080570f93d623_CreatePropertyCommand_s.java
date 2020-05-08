 package com.randrdevelopment.propertygroup.command.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 
 import com.randrdevelopment.propertygroup.command.BaseCommand;
 import com.randrdevelopment.propertygroup.PropertyGroup;
 import com.randrdevelopment.propertygroup.regions.RegionTools;
 import com.randrdevelopment.propertygroup.regions.SchematicTools;
 
 public class CreatePropertyCommand extends BaseCommand{
 	FileConfiguration propertyConfig;
 	
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
     	if (!sender.hasPermission("propertygroup.create"))
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
     	int qty = propertyConfig.getInt(propertyGroup+".qty");
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
				
				Location Start = new Location(null, x, y, z);
				Location End = new Location(null, x+width, y, z+length);
 
				if (!RegionTools.createProtectedRegion(propertyGroup+"-"+row+"-"+col, worldname, Start, End, 10))
 					sender.sendMessage(plugin.getTag()+ChatColor.RED+"Error creating region...");
 				
 				propertyConfig.set(propertyGroup+".properties."+i+".created", true);
 				plugin.savePropertyConfig();
 					
 				break;
 			}
 		}
 		
 		if (noproperties){
 			sender.sendMessage(plugin.getTag()+ChatColor.RED+"Property Group '"+propertyGroup+"' is full.");
 		} else {
 			sender.sendMessage(plugin.getTag()+"Property Created");
 		}
     }
 }
