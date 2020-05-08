 package com.github.dublekfx.HeadHunter;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Skeleton.SkeletonType;
 import org.bukkit.entity.Zombie;
 import org.bukkit.event.Listener;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.inventory.meta.SkullMeta;
 
 
 public final class HeadHunter extends JavaPlugin implements Listener	{
 		
 	@Override
 	public void onEnable()	{
 		this.saveDefaultConfig();
 		getLogger().info("HeadHunter enabled!");
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 	
 	@Override
 	public void onDisable()	{
 		getLogger().info("HeadHunter disabled!");
 	}
 	
 	public boolean onCommand (CommandSender sender, Command cmd, String label, String[] args){
 		if (cmd.getName().equalsIgnoreCase("hh"))	{
 			if ( (sender instanceof Player && sender.hasPermission("headhunter.set")) || !(sender instanceof Player) )	{
 				if (args.length == 1)	{
 					if (args[0].equalsIgnoreCase("reload"))	{
 						reloadConfig();
 						saveDefaultConfig();
 						sender.sendMessage(ChatColor.DARK_GREEN +"Config reloaded!");
 						return true;
 					}
 					if (args[0].equalsIgnoreCase("display"))	{
 						sender.sendMessage("Looting One: " + this.getConfig().getDouble("looting.1"));
 						sender.sendMessage("Looting Two: " + this.getConfig().getDouble("looting.2"));
 						sender.sendMessage("Looting Three: " + this.getConfig().getDouble("looting.3"));
 						return true;
 					}
 				}
 				if (args.length == 3)	{
 					if (args[0].equalsIgnoreCase("looting"))	{
 						double newChance = Double.parseDouble(args[2]);
 						this.getConfig().set(args[0] + "." + args[1], newChance);
 						sender.sendMessage(ChatColor.DARK_GREEN + "Looting " + args[1] + " probability set to " + newChance);
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 
 	@EventHandler
 	public void onEntityDeathEvent (EntityDeathEvent event) {
 		//System.out.println("event");
 		LivingEntity entity = event.getEntity();
 		Player killer;
 		try {
 			killer = entity.getKiller();
 			//System.out.println("killer is " + killer.getName());
 		} catch (NullPointerException e) {
 			return;
 		}
 		try {
 			if(killer.getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS)>0){
 				//System.out.println("checking node: looting."+killer.getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS));
 				//System.out.println("result: "+getConfig().getDouble("looting." + killer.getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS)));
 				if(((100 * Math.random() > getConfig().getDouble("looting." + killer.getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS))) ? true : false))
 						return;
 				} else return;
 		} catch (NullPointerException e) {
 			return;
 		}
 		//System.out.println("maths = success!");
 		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 2);
 		if(entity instanceof Zombie){
 			skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 2);
 			//System.out.println("zombie killed, skull set");
 		} else if(entity instanceof Skeleton){
 			if(((Skeleton)entity).getSkeletonType().equals(SkeletonType.NORMAL)){
 				skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 0);
 				//System.out.println("skeleton killed, skull set");
 			} else {
 				skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 1);
 				//System.out.println("wither skeleton killed, skull set");
 			}
 		} else if(entity instanceof Player){
 			skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
 			SkullMeta sm = (SkullMeta) skull.getItemMeta();
 			sm.setOwner(((Player) entity).getName());
 			skull.setItemMeta(sm);
 			//System.out.println("player"+((Player)entity).getName()+" killed, skull set");
 			if(((Player) entity).getName().equals("benzrf")){
 				killer.sendMessage(ChatColor.DARK_PURPLE+"THANK YOU SO MUCH. I "+ChatColor.DARK_RED+"<3"+ChatColor.DARK_PURPLE+" you.");
 				entity.getWorld().dropItemNaturally(entity.getLocation(), new ItemStack(Material.DIAMOND, 20));
 			}
 		} else if(entity instanceof Creeper){
 			skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 4);
 			//System.out.println("creeper killed, skull set");
 		} else
 			return;
 		
 		entity.getWorld().dropItemNaturally(entity.getLocation(), skull);
 		//System.out.println("skull dropped, method complete");
 	}
 }
