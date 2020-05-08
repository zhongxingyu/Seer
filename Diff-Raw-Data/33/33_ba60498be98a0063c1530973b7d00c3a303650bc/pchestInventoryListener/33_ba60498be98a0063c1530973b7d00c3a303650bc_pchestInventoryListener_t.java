 package nl.rodey.personalchest;
 
 import java.io.File;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.getspout.spoutapi.event.inventory.InventoryCloseEvent;
 import org.getspout.spoutapi.event.inventory.InventoryListener;
 
 public class pchestInventoryListener extends InventoryListener {	
     private final pchestMain plugin;
 	private pchestManager chestManager;
 	
     public ItemStack[] chestContents=null;
 
 	public pchestInventoryListener(pchestMain plugin, pchestManager chestManager) {
 		this.plugin = plugin;
 		this.chestManager = chestManager;
 	}
 
 	@Override
 	public void onInventoryClose(InventoryCloseEvent event) {
 		
 		if(event.getLocation() != null)
 		{
 			String locX = String.format("%.0f", event.getLocation().getX());			
 			String locY = String.format("%.0f", event.getLocation().getY());		
 			String locZ = String.format("%.0f", event.getLocation().getZ());
 			
 			String blockFilename = locX + "_" + locY + "_" + locZ;
 			String blockWorldName = event.getLocation().getWorld().getName();
 			String playerName = event.getPlayer().getName();
 			
 			Location blockLoc = event.getLocation();
 			
 			Block block = blockLoc.getBlock();
 
			if (event.getBottomInventory() != null) {
				Inventory inv = event.getInventory();
		        chestContents = inv.getContents();
				
				File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName);
				File chestFile = new File(worldDataFolder , blockFilename + ".chest");
				
				if (!chestFile.exists())
				{
					return;
				}
				else
				{				
					chestManager.createPersonal(playerName, chestContents, block);
				}
 
				chestManager.removeChestOpened(block);
			}
 		}
 	}
 }
