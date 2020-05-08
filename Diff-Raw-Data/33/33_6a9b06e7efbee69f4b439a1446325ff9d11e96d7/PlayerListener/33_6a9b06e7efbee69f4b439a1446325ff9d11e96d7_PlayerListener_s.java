 package com.TeamNovus.Supernaturals.Listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 
 import com.TeamNovus.Supernaturals.SNPlayers;
 import com.TeamNovus.Supernaturals.Models.ItemBag;
 import com.TeamNovus.Supernaturals.Player.SNPlayer;
 import com.TeamNovus.Supernaturals.Player.Class.Power;
 
 public class PlayerListener implements Listener {
 	
 	@EventHandler
 	public void onPlayerLogin(PlayerLoginEvent event) {
		System.out.println(event.getPlayer().getWalkSpeed());
		event.getPlayer().setWalkSpeed(0.1f);
		System.out.println(event.getPlayer().getWalkSpeed());
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {	
 		// Bind all Spells to the BLAZE_ROD
 		if (!(event.getPlayer().getItemInHand().getData().getItemType().equals(Material.BLAZE_ROD)))
 			return;
 
 		// Bind/Switch:
 		if (event.getAction().equals(Action.RIGHT_CLICK_AIR) ||
 				event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
 			SNPlayer player = SNPlayers.i.get(event.getPlayer());
 
 			player.setNextBinding();
 
 			if (player.getSelectedPower() != null) {
 				player.sendMessage(ChatColor.GREEN + "Wand bound to " + ChatColor.YELLOW + player.getSelectedPower().getName() + ChatColor.GREEN + "!");
 			}
 		}
 
 
 		// Cast:
 		if (event.getAction().equals(Action.LEFT_CLICK_AIR)) {
 			SNPlayer player = SNPlayers.i.get(event.getPlayer());
 
 			if (player.getSelectedPower() != null) {
 				Power power = player.getSelectedPower();
 				
 				if (power.getRequired().has(event.getPlayer())) {
 					if (power.cast(event.getPlayer())) {
 						power.getConsume().consume(event.getPlayer());
 					}
 				} else {
 					event.getPlayer().sendMessage(ChatColor.BLUE + "Requires:");
 					if (power.getRequired().getMoneyCost() != 0)
 						event.getPlayer().sendMessage(ChatColor.BLUE + "   Money: " + ChatColor.YELLOW + power.getRequired().getMoneyCost());
 					if (power.getRequired().getExpCost() != 0)
 						event.getPlayer().sendMessage(ChatColor.BLUE + "   Experience: " + ChatColor.YELLOW + power.getRequired().getExpCost());
 					if (power.getRequired().getHealthCost() != 0)
 						event.getPlayer().sendMessage(ChatColor.BLUE + "   Health: " + ChatColor.YELLOW + power.getRequired().getHealthCost());
 					if (power.getRequired().getHungerCost() != 0)
 						event.getPlayer().sendMessage(ChatColor.BLUE + "   Hunger: " + ChatColor.YELLOW + power.getRequired().getHungerCost());
 					if (power.getRequired().getPowerCost() != 0)
 						event.getPlayer().sendMessage(ChatColor.BLUE + "   Power: " + ChatColor.YELLOW + power.getRequired().getPowerCost());
 					if (power.getRequired().getItemBagCost() != new ItemBag())
 						event.getPlayer().sendMessage(ChatColor.BLUE + "   Items: " + ChatColor.YELLOW + power.getRequired().getItemBagCost().toString());
 				}
 			} else {
 				event.getPlayer().sendMessage(ChatColor.RED + "Your wand is not bound to a power!");
 			}
 		}
 	}
 
 }
