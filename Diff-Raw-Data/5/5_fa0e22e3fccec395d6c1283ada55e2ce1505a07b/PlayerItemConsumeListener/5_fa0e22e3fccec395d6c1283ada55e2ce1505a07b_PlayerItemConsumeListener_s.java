 package com.gildorymrp.charactercards;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerItemConsumeEvent;
 
 import com.gildorymrp.gildorymclasses.CharacterClass;
 import com.gildorymrp.gildorymclasses.GildorymClasses;
 
 public class PlayerItemConsumeListener implements Listener{
 
 	GildorymCharacterCards plugin;
 
 	public PlayerItemConsumeListener(GildorymCharacterCards plugin) {
 		this.plugin = plugin;
 	}	
 	
 	@EventHandler
 	public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
 		if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
 			if (event.getItem() != null) {
 				if (event.getItem().getType() == Material.POTION) {
 					int healingAmount = 0;
					boolean returnMessage = true
 					if (event.getItem().getDurability() == 8261 || event.getItem().getDurability() == 8197) {
 						healingAmount = 1;
 					} else if (event.getItem().getDurability() == 8229 ) {
 						healingAmount = 2;
 					} else {
 						return;
 					}
 					
 					Player player = event.getPlayer();
 					GildorymClasses gildorymClasses = (GildorymClasses) Bukkit.getServer().getPluginManager().getPlugin("GildorymClasses");
 					CharacterCard characterCard = plugin.getCharacterCards().get(player.getName());
 
 					if (characterCard == null) {
 						characterCard = new CharacterCard(0, Gender.UNKNOWN, "", Race.UNKNOWN, gildorymClasses.levels.get(player.getName()), gildorymClasses.classes.get(player.getName()));
 						plugin.getCharacterCards().put(player.getName(), characterCard);
 					}
 
 					CharacterClass clazz = gildorymClasses.classes.get(player.getName());
 					Integer level = gildorymClasses.levels.get(player.getName());
 					Race race = characterCard.getRace();
 
 					Integer maxHealth = CharacterCard.calculateHealth(clazz, race, level);
 
 					if (characterCard.getHealth() + healingAmount <= maxHealth) {
 						characterCard.setHealth(characterCard.getHealth() + healingAmount);
 					} else if (characterCard.getHealth() < maxHealth) {
 						characterCard.setHealth(maxHealth);
					} else if (characterCard.getHealth >= maxHealth){
 						return;
 						/*
 						* There is no reason to calculate anything further,
 						* if you are not healing PVP health.
 						*/
 					}
 					
 					Integer newHealth = plugin.getCharacterCards()
 							.get(player.getName()).getHealth();
 
 					ChatColor healthColor;
 					double healthFraction = newHealth / (double) maxHealth;
 					if (newHealth >= maxHealth) {
 						healthColor = ChatColor.DARK_GREEN;
 					} else if (healthFraction >= 0.75) {
 						healthColor = ChatColor.GREEN;
 					} else if (healthFraction >= 0.5) {
 						healthColor = ChatColor.GOLD;
 					} else if (healthFraction >= 0.25) {
 						healthColor = ChatColor.YELLOW;
 					} else if (healthFraction > 0) {
 						healthColor = ChatColor.RED;
 					} else {
 						healthColor = ChatColor.DARK_RED;
 					}
 
 					GildorymCharacterCards.sendRadiusMessage(player, ChatColor.WHITE + player.getDisplayName()
 							+ ChatColor.GREEN + " has drunk a potion and was healed! "
 							+ ChatColor.WHITE + "(" + healthColor + newHealth + "/"
 							+ maxHealth + ChatColor.WHITE + ")", 24);
 				}
 			}
 		}
 	} 
 }		
