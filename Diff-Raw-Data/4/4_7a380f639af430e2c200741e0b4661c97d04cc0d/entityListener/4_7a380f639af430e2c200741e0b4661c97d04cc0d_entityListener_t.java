 package com.zolli.rodolffoutilsreloaded.listeners;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Blaze;
 import org.bukkit.entity.Enderman;
 import org.bukkit.entity.IronGolem;
 import org.bukkit.entity.MushroomCow;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.ExpBottleEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLevelChangeEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import com.zolli.rodolffoutilsreloaded.rodolffoUtilsReloaded;
 
 public class entityListener implements Listener {
 	
 	private rodolffoUtilsReloaded plugin;
 	
 	ArrayList<Player> showcaseUsers = new ArrayList<Player>();
 	public Map<String, Integer> showcase = new HashMap<String, Integer>();
 	
 	public entityListener(rodolffoUtilsReloaded instance) {
 		plugin = instance;
 	}
 	
 	@EventHandler(priority=EventPriority.HIGH)
 	public void givingSoup(PlayerInteractEntityEvent e) {
 		
 		Player pl = e.getPlayer();
 		
 		if((e.getRightClicked() instanceof MushroomCow) && (pl.getItemInHand().getTypeId() == 281)) {
 			
 			MushroomCow mushroom = (MushroomCow) e.getRightClicked();
 			int neededTime = plugin.config.getInt("mushroomSoupRegain", 300);
 			
 			if(((int) (new Date().getTime() / 1000L) > mushroom.getAge() + neededTime * 20) || (pl.isOp() || plugin.perm.has(pl, "rur.mushroomCooldown.exempt"))) {
 				
 				mushroom.setAge((int) (new Date().getTime() / 1000L));
 				
 			} else {
 				
				if(pl.getItemInHand().getTypeId() != 296) {
					e.setCancelled(true);
				}
 				
 			}
 			
 		}
 		
 		
 	}
 	
 	@EventHandler(priority=EventPriority.LOWEST)
 	public void showcase(PlayerInteractEvent e) {
 		
 		Player pl = e.getPlayer();
 		Block b = e.getClickedBlock();
 		
 			
 		if((plugin.scs != null) && (e.getAction() != null) && (b != null) && (plugin.scs.getShopHandler().isShopBlock(b))) {
 				
 			if(showcase.containsKey(pl.getName())) {
 					
 				if((showcase.get(pl.getName()) + plugin.config.getInt("scsdelay")/10) >= (int) (new Date().getTime() / 1000L)) {
 						
 					e.setCancelled(true);
 						
 				} else {
 						
 					showcase.remove(pl.getName());
 					showcase.put(pl.getName(), (int) (new Date().getTime() / 1000L));
 						
 				}
 					
 			} else {
 					
 				showcase.put(pl.getName(), (int) (new Date().getTime() / 1000L));
 					
 			}
 				
 		}
 			
 		
 	}
 	
 	@EventHandler(priority=EventPriority.HIGH)
 	public void levelChange(PlayerLevelChangeEvent e) {
 		
 		Player pl = e.getPlayer();
 		int newLevel = e.getNewLevel();
 		int oldLevel = e.getOldLevel();
 		
 		if((newLevel>oldLevel) && newLevel>0) {
 			
 			if(plugin.config.getBoolean("showeffectonlevelchange")) {
 				pl.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, plugin.config.getInt("effecttimeonlevechange")*10, 10));
 			}
 			
 			if(plugin.config.getBoolean("showlevelchange")) {
 				pl.sendMessage(plugin.messages.getString("common.levelchangemsg").replace("(LVL)", Integer.toString(e.getNewLevel())));
 			}
 		
 		}
 		
 	}
 	
 	@EventHandler(priority=EventPriority.NORMAL)
 	public void noXpDrop(EntityDeathEvent e) {
 		
 		if(((e.getEntity() instanceof IronGolem) || (e.getEntity() instanceof Enderman) || (e.getEntity() instanceof Blaze)) && plugin.config.getBoolean("onlyxpdropwhenkill")) {
 			
 			if(!(e.getEntity().getKiller() instanceof Player)) {
 				
 				e.setDroppedExp(0);
 				
 				if(plugin.config.getBoolean("alsodisabledrops")) {
 					
 					e.getDrops().clear();
 					
 				}
 				
 			}
 			
 		}
 		
 	}
 	
 	@EventHandler(priority=EventPriority.NORMAL)
 	public void loginMessage(PlayerJoinEvent e) {
 		
 		e.setJoinMessage(plugin.messages.getString("login.login").replace("(NAME)", e.getPlayer().getName()));
 		
 	}
 	
 	@EventHandler(priority=EventPriority.NORMAL)
 	public void logoutMessage(PlayerQuitEvent e) {
 		
 		e.setQuitMessage(plugin.messages.getString("login.logout").replace("(NAME)", e.getPlayer().getName()));
 		
 	}
 	
 	@EventHandler(priority=EventPriority.NORMAL)
 	public void xpBottleThrow(ExpBottleEvent e) {
 		
 		e.setExperience(plugin.config.getInt("xpbottlecrafting"));
 		
 	}
 	
 }
