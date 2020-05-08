 package de.dustplanet.cookme;
 
 import java.sql.Timestamp;
 import java.util.Random;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.player.PlayerItemConsumeEvent;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 /**
  * CookMePlayerListener
  * Handles the players activities!
  * 
  * Refer to the forum thread:
  * http://bit.ly/cookmebukkit
  * 
  * Refer to the dev.bukkit.org page:
  * http://bit.ly/cookmebukkitdev
  * 
  * @author xGhOsTkiLLeRx
  * thanks nisovin for his awesome code snippet!
  * 
  */
 
 public class CookMePlayerListener implements Listener {
     private CookMe plugin;
     private boolean message = true;
     private Random random = new Random();
 
     public CookMePlayerListener(CookMe instance) {
 	plugin = instance;
     }
 
     @EventHandler
     public void onPlayerConsumeItem(PlayerItemConsumeEvent event) {
 	String effect;
 	Player player = event.getPlayer();
 	Timestamp now = new Timestamp(System.currentTimeMillis());
 	// Check if player is affected
 	if (!player.hasPermission("cookme.safe")) {
 	    // Check for item & right clicking
 	    if (sameItem(player.getItemInHand().getTypeId())) {
 		// If the player is in cooldown phase cancel it
 		if (!plugin.cooldownManager.hasCooldown(player, now)) {
 		    // Make a temp double and a value between 0 and 99
 		    double temp = 0;
 		    int i = 0;
 		    // Get the number for the effect
 		    for (i = 0; i < plugin.percentages.length; i++) {
 			temp += plugin.percentages[i];
 			if (random.nextInt(100) <= temp)
 			    break;
 		    }
 		    // EffectStrenght, Duration etc.
 		    int randomEffectStrength = random.nextInt(16);
 		    int randomEffectTime = (random.nextInt((plugin.maxDuration - plugin.minDuration) + 1) + plugin.minDuration);
 		    // Player gets random damage, stack minus 1
 		    if (i == 0) {
 			int randomDamage = random.nextInt(9) + 1;
 			effect = plugin.localization.getString("damage");
 			message(player, effect);
 			player.damage(randomDamage);
 		    }
 		    // Player dies, stack minus 1
 		    else if (i == 1) {
 			effect = plugin.localization.getString("death");
 			message(player, effect);
 			player.setHealth(0);
 		    }
 		    // Random venom damage (including green hearts :) )
 		    else if (i == 2) {
 			effect = plugin.localization.getString("venom");
 			message(player, effect);
 			player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, randomEffectTime, randomEffectStrength));
 		    }
 		    // Food bar turns green (poison)
 		    else if (i == 3) {
 			effect = plugin.localization.getString("hungervenom");
 			message(player, effect);
 			player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, randomEffectTime, randomEffectStrength));
 		    }
 		    // Sets the food level down. Stack minus 1
 		    else if (i == 4) {
			int currentFoodLevel = player.getFoodLevel();
			int randomFoodLevel = 0;
			if (currentFoodLevel != 0) {
			    randomFoodLevel = random.nextInt(currentFoodLevel);
			}
 			effect = plugin.localization.getString("hungerdecrease");
 			message(player, effect);
 			player.setFoodLevel(randomFoodLevel);
 		    }
 		    // Confusion
 		    else if (i == 5) {
 			effect = plugin.localization.getString("confusion");
 			message(player, effect);
 			player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, randomEffectTime, randomEffectStrength));
 		    }
 		    // Blindness
 		    if (i == 6) {
 			effect = plugin.localization.getString("blindness");
 			message(player, effect);
 			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, randomEffectTime, randomEffectStrength));
 		    }
 
 		    // Weakness
 		    else if (i == 7) {
 			effect = plugin.localization.getString("weakness");
 			message(player, effect);
 			player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, randomEffectTime, randomEffectStrength));
 
 		    }
 		    // Slowness
 		    else if (i == 8) {
 			effect = plugin.localization.getString("slowness");
 			message(player, effect);
 			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, randomEffectTime, randomEffectStrength));
 
 		    }
 		    // Slowness for blocks
 		    else if (i == 9) {
 			effect = plugin.localization.getString("slowness_blocks");
 			message(player, effect);
 			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, randomEffectTime, randomEffectStrength));
 
 		    }
 		    // Instant Damage
 		    else if (i == 10) {
 			effect = plugin.localization.getString("instant_damage");
 			message(player, effect);
 			player.addPotionEffect(new PotionEffect(PotionEffectType.HARM, randomEffectTime, randomEffectStrength));
 
 		    }
 		    // Refusing
 		    else if (i == 11) {
 			effect = plugin.localization.getString("refusing");
 			message(player, effect);
 			event.setCancelled(true);
 		    }
 		    // Wither effect
 		    else if (i == 12) {
 			effect = plugin.localization.getString("wither");
 			message(player, effect);
 			player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, randomEffectTime, randomEffectStrength));
 		    }
 
 		    // Add player to cooldown list
 		    if (plugin.cooldown != 0) {
 			plugin.cooldownManager.addPlayer(player);
 		    }
 		    
 		    // Cancel event and reduce food
 		    event.setCancelled(true);
 		    if (i != 11) {
 			decreaseItem(player);
 		    }
 		}
 	    }
 	}
     }
 
     private void message(Player player, String message) {
 	if (plugin.messages) {
 	    plugin.message(null, player, message, null, null);
 	}
     }
 
     // Is the item in the list? Yes or no
     private boolean sameItem(int item) {
 	for (String itemName : plugin.itemList) {
 	    // Get the Material
 	    Material mat = Material.matchMaterial(itemName);
 	    // Not valid
 	    if (mat == null) {
 		// Prevent spamming
 		if (message) {
 		    plugin.getLogger().warning("Couldn't load the foods! Please check your config!");
 		    plugin.getLogger().warning("The following item id/name is invalid: " + itemName);
 		    message = false;
 		}
 		// Go on
 		continue;
 	    }
 	    // Get ID & compare
 	    if (mat.getId() == item) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     // Sets the raw food -1
     private void decreaseItem (Player player) {
 	ItemStack afterEating = player.getItemInHand();
 	if (afterEating.getAmount() == 1) {
 	    player.setItemInHand(null);
 	} else {
 	    afterEating.setAmount(afterEating.getAmount() - 1);
 	    player.setItemInHand(afterEating);
 	}
     }
 }
