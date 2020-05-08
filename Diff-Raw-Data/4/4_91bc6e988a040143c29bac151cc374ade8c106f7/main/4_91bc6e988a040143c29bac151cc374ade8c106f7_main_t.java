 package me.marcoboekholt.godlike;
  
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
  
 public class main extends JavaPlugin {
 	
     protected UpdateChecker updateChecker;   
     
         public void onEnable(){
         	// Create a config file if the user does not have one already
         	if(getConfig().contains("config.version")) {
         		// Set up config fields
        		getConfig().set("updates.enabled", Boolean.valueOf(true));
        		getConfig().set("config.version", Integer.valueOf(1));
         		
         		// Notify the user
         		getLogger().info("Created a new config file. You might want to edit it to fit to your needs.");
             	
             	// Save configuration
             	this.saveConfig();
         	}
         	
         	if(getConfig().getBoolean("updates.enabled", true)) {
 	        	// Check for updates
 	        	UpdateChecker.updateNeeded(this);
         	}
         }
        
         public void onDisable(){
         }
        
         public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
                 Player player = (Player)sender;
                 if(cmd.getName().equalsIgnoreCase("Sword")){
                 	ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1);            
                 	sword.addEnchantment(Enchantment.KNOCKBACK, 2);
                 	sword.addEnchantment(Enchantment.DAMAGE_ALL, 5);
                 	sword.addEnchantment(Enchantment.FIRE_ASPECT, 2);
                 	PlayerInventory pl = player.getInventory();
                 	pl.addItem(sword);
                     player.sendMessage(ChatColor.DARK_RED + ("*~*") + ChatColor.GREEN + (" You've got a sword!"));
                 }
                 else if(cmd.getName().equalsIgnoreCase("Bow")){
                     ItemStack bow = new ItemStack(Material.BOW, 1);
                     ItemStack arrow = new ItemStack(Material.ARROW, 64);
                     bow.addEnchantment(Enchantment.ARROW_DAMAGE, 5);
                     bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
                     bow.addEnchantment(Enchantment.ARROW_FIRE, 1);
                     bow.addEnchantment(Enchantment.ARROW_KNOCKBACK, 2);
                     PlayerInventory pl = player.getInventory();
                     pl.addItem(bow);
                     pl.addItem(arrow);
                     player.sendMessage(ChatColor.DARK_RED + ("*~*") + ChatColor.GREEN + (" You've got a bow and sword!"));
                 }
                 else if(cmd.getName().equalsIgnoreCase("Armour")){
                     ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET, 1);
                     helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                     helmet.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                     helmet.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                     helmet.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                     helmet.addEnchantment(Enchantment.THORNS, 3);
                     helmet.addEnchantment(Enchantment.OXYGEN, 3);
                     helmet.addEnchantment(Enchantment.WATER_WORKER, 1);
                        
                     ItemStack body = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
                     body.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                     body.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                     body.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                     body.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                     body.addEnchantment(Enchantment.THORNS, 3);
                       
                     ItemStack legs = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
                     legs.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                     legs.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                     legs.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                     legs.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                     legs.addEnchantment(Enchantment.THORNS, 3);
                        
                     ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS, 1);
                     boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                     boots.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                     boots.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                     boots.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                     boots.addEnchantment(Enchantment.THORNS, 3);
                     boots.addEnchantment(Enchantment.PROTECTION_FALL, 4);
  
                     PlayerInventory pl = player.getInventory();
                     pl.addItem(helmet);
                     pl.addItem(body);
                     pl.addItem(legs);
                     pl.addItem(boots);
                     player.sendMessage(ChatColor.DARK_RED + ("*~*") + ChatColor.GREEN + (" You've got the armour!"));
                 }
                 else if(cmd.getName().equalsIgnoreCase("Tools")){
                 	ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE, 1);
                 	pickaxe.addEnchantment(Enchantment.DIG_SPEED, 5);
                 	pickaxe.addEnchantment(Enchantment.SILK_TOUCH, 1);
                 	pickaxe.addEnchantment(Enchantment.DURABILITY, 3);
                 	pickaxe.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                 	
                 	ItemStack axe = new ItemStack(Material.DIAMOND_AXE, 1);
                 	axe.addEnchantment(Enchantment.DIG_SPEED, 5);
                 	axe.addEnchantment(Enchantment.SILK_TOUCH, 1);
                 	axe.addEnchantment(Enchantment.DURABILITY, 3);
                 	pickaxe.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                 	
                 	ItemStack spade = new ItemStack(Material.DIAMOND_SPADE, 1);
                 	spade.addEnchantment(Enchantment.DIG_SPEED, 5);
                 	spade.addEnchantment(Enchantment.SILK_TOUCH, 1);
                 	spade.addEnchantment(Enchantment.DURABILITY, 3);
                 	pickaxe.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                 	
                 	ItemStack hoe = new ItemStack(Material.DIAMOND_HOE, 1);
                 	hoe.addEnchantment(Enchantment.DURABILITY, 3);
                 	
                 	PlayerInventory pl = player.getInventory();
                 	pl.addItem(pickaxe);
                 	pl.addItem(axe);
                 	pl.addItem(spade);
                 	pl.addItem(hoe);
                 	player.sendMessage(ChatColor.DARK_RED + ("*~*") + ChatColor.GREEN + (" You've got the tools!"));
                 }
                 else if(cmd.getName().equalsIgnoreCase("God")){
                 	player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 2400, 1));
                 	player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 2400, 1));
                 	player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 2400, 1));
                 	
                 	player.sendMessage(ChatColor.DARK_RED + ("*~*") + ChatColor.GREEN + (" You now have the golden apple effect for about 2 minutes!"));
                 }
                 else if(cmd.getName().equalsIgnoreCase("power")){
                 	player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 2400, 1));
                 	player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 2400, 1));
                 	
                 	player.sendMessage(ChatColor.DARK_RED + ("*~*") + ChatColor.GREEN + (" You now have defence and strength effects for about 2 minutes!"));
                 }
                 else if(cmd.getName().equalsIgnoreCase("fly")){
                 	if(player.getAllowFlight()) {
                 		player.setFlying(false);
                 		player.setAllowFlight(false);
                 		player.sendMessage(ChatColor.DARK_RED + ("*~*") + ChatColor.GREEN + (" Fly powers disabled!"));
                 	} else {
                 		player.setAllowFlight(true);
                 		player.setFlySpeed(0.5F);
                 		player.sendMessage(ChatColor.DARK_RED + ("*~*") + ChatColor.GREEN + (" Fly powers enabled!"));
                 	}
                 }
                 else if(cmd.getName().equalsIgnoreCase("speed")){
                 	player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2400, 1));
                 	
                 	player.sendMessage(ChatColor.DARK_RED + ("*~*") + ChatColor.GREEN + (" You now have a speed potion effect for about 2 minutes!"));
                 }
                 return false;
                
         }
 }
