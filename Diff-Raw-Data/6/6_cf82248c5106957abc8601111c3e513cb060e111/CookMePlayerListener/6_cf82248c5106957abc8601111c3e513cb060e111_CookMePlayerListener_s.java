 import java.sql.Timestamp;
 import java.util.Random;
 
 
 /**
  * CookMePlayerListener
  * Handles the players activities!
  *
  * Refer to the forum thread:
  * http://bit.ly/cookmebukkit
  * Refer to the dev.bukkit.org page:
  * http://bit.ly/cookmebukkitdev
  *
  * @author xGhOsTkiLLeRx
  * @thanks nisovin for his awesome code snippet!
  *
  */
 
 public class CookMePlayerListener extends PluginListener {
     private CookMe plugin;
     private boolean message = true;
     private Random random = new Random();
 
     public CookMePlayerListener(CookMe instance) {
 	plugin = instance;
     }
 
     public boolean onEat(Player player, Item item) {
 	String effect;
 	Timestamp now = new Timestamp(System.currentTimeMillis());
 	// Check if player is affected
 	//TODO if (!player.hasPermission("cookme.safe")) {
 	if (!player.isAdmin()) {
 	    // Check for item & right clicking
 	    if (sameItem(player.getItemStackInHand().getItemId())) {
 		// If the player is in cooldown phase cancel it
 		if (!plugin.cooldownManager.hasCooldown(player, now)) {
 		    // Check for food level
 		    if (player.getFoodLevel() != 20) {
 			// Make a temp double and a value between 0 and 99
 			double temp = 0;
 			int i = 0;
 			// Get the number for the effect
 			for(i = 0; i < plugin.percentages.length; i++) {
 			    temp += plugin.percentages[i];
 			    if (random.nextInt(100) <= temp) break;
 			}
 			// EffectStrenght, Duration etc.
 			int randomEffectStrength = random.nextInt(16);
 			int randomEffectTime = (random.nextInt((plugin.maxDuration - plugin.minDuration)  + 1)  +  plugin.minDuration) * 1000;
 			// Player gets random damage, stack minus 1
 			if (i == 0) {
 			    int randomDamage = random.nextInt(9) +1;
 			    effect = plugin.localization.getString("damage");
 			    message(player, effect);
 			    decreaseItem(player);
 			    player.applyDamage(PluginLoader.DamageType.ENTITY, randomDamage);
 			}			
 			// Player dies, stack minus 1
 			if (i == 1) {
 			    effect = plugin.localization.getString("death");
 			    message(player, effect);
 			    decreaseItem(player);
 			    player.setHealth(0);
 			}
 			// Random venom damage (including green hearts :) )
 			if (i == 2) {
 			    effect = plugin.localization.getString("venom");
 			    message(player, effect);
 			    decreaseItem(player);
 			    player.addPotionEffect(PotionEffect.getNewPotionEffect(PotionEffect.Type.POISON, randomEffectTime, randomEffectStrength));
 			}
 			// Food bar turns green (poison)
 			if (i == 3) {
 			    effect = plugin.localization.getString("hungervenom");
 			    message(player, effect);
 			    decreaseItem(player);
 			    player.addPotionEffect(PotionEffect.getNewPotionEffect(PotionEffect.Type.HUNGER, randomEffectTime, randomEffectStrength));
 			}
 			// Sets the food level down. Stack minus 1
 			if (i == 4) {
			    int currentFoodLevel = player.getFoodLevel(), randomFoodLevel = random.nextInt(currentFoodLevel);
 			    effect = plugin.localization.getString("hungerdecrease");
 			    message(player, effect);
 			    decreaseItem(player);
 			    player.setFoodLevel(randomFoodLevel);
 			}
 			// Confusion
 			if (i == 5) {
 			    effect = plugin.localization.getString("confusion");
 			    message(player, effect);
 			    decreaseItem(player);
 			    player.addPotionEffect(PotionEffect.getNewPotionEffect(PotionEffect.Type.CONFUSION, randomEffectTime, randomEffectStrength));
 			}
 			// Blindness
 			if (i == 6) {
 			    effect = plugin.localization.getString("blindness");
 			    message(player, effect);
 			    decreaseItem(player);
 			    player.addPotionEffect(PotionEffect.getNewPotionEffect(PotionEffect.Type.BLINDNESS, randomEffectTime, randomEffectStrength));
 			}
 
 			// Weakness
 			if (i == 7) {
 			    effect = plugin.localization.getString("weakness");
 			    message(player, effect);
 			    decreaseItem(player);
 			    player.addPotionEffect(PotionEffect.getNewPotionEffect(PotionEffect.Type.WEAKNESS, randomEffectTime, randomEffectStrength));
 
 			}
 			// Slowness
 			if (i == 8) {
 			    effect = plugin.localization.getString("slowness");
 			    message(player, effect);
 			    decreaseItem(player);
 			    player.addPotionEffect(PotionEffect.getNewPotionEffect(PotionEffect.Type.SLOW_DOWN, randomEffectTime, randomEffectStrength));
 
 			}
 			// Slowness for blocks
 			if (i == 9) {
 			    effect = plugin.localization.getString("slowness_blocks");
 			    message(player, effect);
 			    decreaseItem(player);
 			    player.addPotionEffect(PotionEffect.getNewPotionEffect(PotionEffect.Type.DIG_SLOW, randomEffectTime, randomEffectStrength));
 
 			}
 			// Instant Damage
 			if (i == 10) {
 			    effect = plugin.localization.getString("instant_damage");
 			    message(player, effect);
 			    decreaseItem(player);
 			    player.addPotionEffect(PotionEffect.getNewPotionEffect(PotionEffect.Type.HARM, randomEffectTime, randomEffectStrength));
 
 			}
 			// Refusing
 			if (i == 11) {
 			    effect = plugin.localization.getString("refusing");
 			    message(player, effect);
 			    return true;
 			}
 			// Wither effect
 			if (i == 12) {
 			    effect = plugin.localization.getString("wither");
 			    message(player, effect);
 			    decreaseItem(player);
 			    player.addPotionEffect(PotionEffect.getNewPotionEffect(PotionEffect.Type.WITHER, randomEffectTime, randomEffectStrength));
 			}
 
 			// Add player to cooldown list
 			if (plugin.cooldown != 0) plugin.cooldownManager.addPlayer(player);
 		    }
 		}
 	    }
 	}
 	return false;
     }
 
     private void message(Player player, String message) {
 	if (plugin.messages) {
 	    plugin.message(player, message, null, null);
 	}
     }
 
     // Is the item in the list? Yes or no
     private boolean sameItem(int item) {
 	for (String itemName : plugin.itemList) {
 	    // Get the Material
 	    try {
 		Item.Type mat = Item.Type.valueOf(itemName);
 		// Get ID & compare
 		if (mat.getId() == item) return true;
 	    }
 	    // Not valid
 	    catch (IllegalArgumentException e) {
 		// Prevent spamming
 		if (message) {
 		    CookMe.log.warning("Couldn't load the foods! Please check your config!");
 		    CookMe.log.warning("The following item id/name is invalid: " + itemName);
 		    message = false;
 		}
 		// Go on
 		continue;
 	    }
 	}
 	return false;
     }
 
     // Sets the raw food -1
     private void decreaseItem (Player player) {
 	Item afterEating = player.getItemStackInHand();
 	if (afterEating.getAmount() == 1) player.setItemInHand(null);
 	else {
 	    afterEating.setAmount(afterEating.getAmount() - 1);
 	    player.setItemInHand(afterEating);
 	}
 	player.updateInventory();
     }
 }
