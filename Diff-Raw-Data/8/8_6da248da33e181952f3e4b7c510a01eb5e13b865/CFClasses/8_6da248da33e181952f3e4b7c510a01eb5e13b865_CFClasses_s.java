 package org.github.craftfortress2;
 import java.util.ArrayList;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.inventory.PlayerInventory;
 public class CFClasses {
 	static ArrayList<PlayerInventory> playerInventories = new ArrayList<PlayerInventory>();
 	static ArrayList<Player> players = new ArrayList<Player>(); // Use the one in CFCE later
 	@EventHandler
 	public void onFoodBarLower(FoodLevelChangeEvent event){
 			if(event.getEntity()instanceof Player){
 				Player player = (Player)event.getEntity();
 				player.setExhaustion(0);
 				player.setFoodLevel(17);
 			}
 		}
 	public static void saveInv(Player player){
 		PlayerInventory inv = player.getInventory();
 		playerInventories.add(inv);
 		players.add(player);
 	}
 	public static void loadInv(Player player){
		PlayerInventory inv = getInv(player);
		player.getInventory().setContents(inv.getContents());
 	}
	public static PlayerInventory getInv(Player player){
 		int index = players.lastIndexOf(player);
 		if (index == -1){
 			return null;
 		}
 		return playerInventories.get(index);
 	}
 }
