 package me.lucariatias.plugins.kaisocraft;
 
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class EntityDamageByEntityListener implements Listener {
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
 		
 		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
			if (KaisoCraft.getPlayerGuild(((Player) event.getDamager()).getName()).equals(KaisoCraft.getPlayerGuild(((Player) event.getEntity()).getName()))) {
				event.setCancelled(true);
				((Player) event.getDamager()).sendMessage(ChatColor.AQUA + "You cannot hurt members of your own guild!");
 			}
 		}
 		
 		if (!event.isCancelled()) {
 			if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) {
 				Player player = (Player) event.getDamager();
 				LivingEntity entity = (LivingEntity) event.getEntity();
 				
 				//Set damage
 				event.setDamage((int) (Math.ceil((((KaisoCraft.getEntityLevel(event.getDamager()) + 1) / 2) + 5) * event.getDamage())) / (KaisoCraft.getEntityLevel(event.getEntity()) + 1) / 2);
 				
 				//Print damage info to chat
 				player.sendMessage(ChatColor.AQUA + "==" + ChatColor.DARK_AQUA + "Lvl" + KaisoCraft.getEntityLevel(entity) + " " + event.getEntityType().toString() + ChatColor.DARK_AQUA + "==");
 				player.sendMessage(ChatColor.AQUA + "Damage dealt: " + event.getDamage());
 				if (entity.getHealth() - event.getDamage() > 0) {
 					player.sendMessage(ChatColor.AQUA + "HP Remaining: " + ChatColor.DARK_AQUA + (entity.getHealth() - event.getDamage()) + "/" + entity.getMaxHealth());
 				} else {
 					player.sendMessage(ChatColor.AQUA + "HP Remaining: " + ChatColor.DARK_AQUA + 0 + "/" + entity.getMaxHealth());
 				}
 				
 				//Special effects
 				if (player.getLevel() >= 10) {
 					Random random = new Random();
 					if (random.nextInt(100) <= 7) {
 						((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 5), true);
 						player.sendMessage(ChatColor.AQUA + "Special effect! Weakness!");
 					}
 				}
 				
 				if (player.getLevel() >= 20) {
 					Random random = new Random();
 					if (random.nextInt(100) <= 5) {
 						((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 5), true);
 						player.sendMessage(ChatColor.AQUA + "Special effect! Freeze!");
 					}
 				}
 				
 				if (player.getLevel() >= 30) {
 					Random random = new Random();
 					if (random.nextInt(100) <= 3) {
 						((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 5), true);
 						player.sendMessage(ChatColor.AQUA + "Special effect! Poison!");
 					}
 				}
 				
 				if (player.getLevel() >= 40) {
 					Random random = new Random();
 					if (random.nextInt(100) <= 5) {
 						event.getEntity().setFireTicks(60);
 						player.sendMessage(ChatColor.AQUA + "Special effect! Flame!");
 					}
 				}
 				
 				if (player.getLevel() >= 50) {
 					Random random = new Random();
 					if (random.nextInt(100) <= 3) {
 						player.setHealth(player.getHealth() + (((LivingEntity) event.getEntity()).getMaxHealth() / 2));
 						player.sendMessage(ChatColor.AQUA + "Special effect! Health drain!");
 					}
 				}
 				
 			}
 			
 			if (event.getDamager() instanceof Projectile && event.getEntity() instanceof LivingEntity) {
 				Projectile projectile = (Projectile) event.getDamager();
 				if (projectile.getShooter() instanceof Player) {
 					Player player = (Player) projectile.getShooter();
 					LivingEntity entity = (LivingEntity) event.getEntity();
 					
 					//Set damage
 					event.setDamage((int) (Math.ceil((((KaisoCraft.getEntityLevel(player) + 1) / 2) + 5) * event.getDamage())) / (KaisoCraft.getEntityLevel(event.getEntity()) + 1) / 2);
 					
 					//Print info to chat
 					player.sendMessage(ChatColor.AQUA + "==" + ChatColor.DARK_AQUA + "Lvl" + KaisoCraft.getEntityLevel(entity) + " " + event.getEntityType().toString() + ChatColor.DARK_AQUA + "==");
 					player.sendMessage(ChatColor.AQUA + "Damage dealt: " + event.getDamage());
 					if (entity.getHealth() - event.getDamage() > 0) {
 						player.sendMessage(ChatColor.AQUA + "HP Remaining: " + ChatColor.DARK_AQUA + (entity.getHealth() - event.getDamage()) + "/" + entity.getMaxHealth());
 					} else {
 						player.sendMessage(ChatColor.AQUA + "HP Remaining: " + ChatColor.DARK_AQUA + 0 + "/" + entity.getMaxHealth());
 					}
 					
 					//Special effects
 					if (player.getLevel() >= 10) {
 						Random random = new Random();
 						if (random.nextInt(100) <= 7) {
 							((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 5), true);
 							player.sendMessage(ChatColor.AQUA + "Special effect! Weakness!");
 						}
 					}
 					
 					if (player.getLevel() >= 20) {
 						Random random = new Random();
 						if (random.nextInt(100) <= 5) {
 							((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 5), true);
 							player.sendMessage(ChatColor.AQUA + "Special effect! Freeze!");
 						}
 					}
 					
 					if (player.getLevel() >= 30) {
 						Random random = new Random();
 						if (random.nextInt(100) <= 3) {
 							((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 5), true);
 							player.sendMessage(ChatColor.AQUA + "Special effect! Poison!");
 						}
 					}
 					
 					if (player.getLevel() >= 40) {
 						Random random = new Random();
 						if (random.nextInt(100) <= 5) {
 							event.getEntity().setFireTicks(60);
 							player.sendMessage(ChatColor.AQUA + "Special effect! Flame!");
 						}
 					}
 					
 					if (player.getLevel() >= 50) {
 						Random random = new Random();
 						if (random.nextInt(100) <= 3) {
 							player.setHealth(player.getHealth() + (((LivingEntity) event.getEntity()).getMaxHealth() / 2));
 							player.sendMessage(ChatColor.AQUA + "Special effect! Health drain!");
 						}
 					}
 				} else {
 					event.setDamage((int) (Math.ceil(KaisoCraft.getEntityLevel(((Projectile) event.getDamager()).getShooter())) / (KaisoCraft.getEntityLevel(event.getEntity()) / 2) * event.getDamage()));
 				}
 			}
 			
 			if ((event.getDamager() instanceof LivingEntity) && !(event.getDamager() instanceof Player)) {
 				event.setDamage((int) (Math.ceil((KaisoCraft.getEntityLevel(event.getDamager()) / (KaisoCraft.getEntityLevel(event.getEntity()) + 1)) * event.getDamage())));
 			}
 		}
 	}
 
 }
