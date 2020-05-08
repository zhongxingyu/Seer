 package dk.gabriel333.BukkitInventoryTools;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.getspout.spoutapi.gui.ScreenType;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 //import de.Keyle.MyWolf.MyWolfPlugin;
 import dk.gabriel333.Library.G333Inventory;
 
 public abstract class BITPlayer implements Player {
 
 	public static void sortinventory(SpoutPlayer sPlayer, ScreenType screentype) {
 		// sort the ordinary player inventory
 		Inventory inventory = sPlayer.getInventory();
 		G333Inventory.stackPlayerInventoryItems(sPlayer);
 
 		// sort the SpoutBackpack if it exists and if it is opened.
 		if (BIT.spoutbackpack && screentype == ScreenType.CHEST_INVENTORY) {
 			inventory = BIT.spoutBackpackHandler
 					.getOpenedSpoutBackpack(sPlayer);
			
			G333Inventory.sortInventoryItems(sPlayer, inventory);
 
 		}
 
 		// sort the players MyWolfInventory if exists and if is open.
 		if (BIT.mywolf) {
 			// && screentype == ScreenType.CHEST_INVENTORY) {
 
 			// if the wolf inventory is open then
			//myWolfInventory = MyWolfPlugin.getMyWolf(sPlayer).inv;
 			// if (myWolfInventory != null) {
 
 			// test if myWolfInventory is opened
 
 			// G333Messages.showInfo("WolfInventory is size"
 			// + myWolfInventory.getSize() + " name:"
 			// + myWolfInventory.getName());
 
 			// sorting myWolfInventry
 			// G333Messages.showInfo("Sorting MyWolfInventory... to be done");
 			// for (i = 0; i < myWolfInventory.getSize(); i++) {
 			// for (j = i + 1; j < myWolfInventory.getSize(); j++) {
 			// G333Inventory.moveitemInventory(sPlayer, j, i,
 			// (Inventory) myWolfInventory);
 			//
 			// }
 			// }
 			// G333Inventory.orderInventoryItems(inventory, 0);
 
 			// }
 
 		}
 	}
 }
