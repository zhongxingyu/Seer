 package com.github.SaxSalute.HomeManager;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class WandClickListener implements Listener
 {
 	//Stores the plugin
 	HomeManager plugin;
 	
 	//Constructor
 	public WandClickListener(HomeManager plugin)
 	{
 		//Stores the plugin in the plugin variable
 		this.plugin = plugin;
 	}
 	
 	//Used to set points
 	@EventHandler
 	public void setPoint(PlayerInteractEvent event)
 	{
 		//If the item in hand is a feather
 		if (event.getPlayer().getItemInHand().getType().equals(Material.FEATHER))
 		{
 			//Stores the location of the clicked block
			Location blockLoc = event.getClickedBlock().getLocation();//Null poiner
 			//Stores the point number of the clicked block
 			int pointNumber = (event.getAction() == Action.LEFT_CLICK_BLOCK ? 0 : 1);
 			
 			//If the player hasn't clicked anything with the wand yet
 			if (plugin.selectedLocations.get(event.getPlayer()) == null)
 				//Create an entry in the HashMap using the Player object associated with the event as the key
 				plugin.selectedLocations.put(event.getPlayer(), new Location[2]);
 			//Set the appropriate part of the HashMap to store the clicked location
 			plugin.selectedLocations.get(event.getPlayer())[pointNumber] = blockLoc;
 			//Notify the player of the success
 			event.getPlayer().sendMessage("Location " + pointNumber + " has been set successfully at (" + blockLoc.getBlockX() + ", " + blockLoc.getBlockY() + ", " + blockLoc.getBlockZ() + ")!");
 		}
 	}
 }
