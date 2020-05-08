 package com.zolli.rodolffoutilsreloaded.listeners;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Blaze;
 import org.bukkit.entity.Enderman;
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
 
 import com.miykeal.showCaseStandalone.Exceptions.ShopNotFoundException;
 import com.miykeal.showCaseStandalone.ShopInternals.Shop.Activities;
 import com.zolli.rodolffoutilsreloaded.rodolffoUtilsReloaded;
 
 public class entityListener implements Listener {
 	
 	private rodolffoUtilsReloaded plugin;
 	
 	ArrayList<Player> showcaseUsers = new ArrayList<Player>();
 	
 	public entityListener(rodolffoUtilsReloaded instance) {
 		plugin = instance;
 	}
 	
 	private int countPlayersInList(Player pl, ArrayList<Player> al) {
 		
 		int count = 0;
 		
 		for(int i=0 ; i<al.size() ; i++) {
 			
 			Player p = (Player) al.get(i);
 			
 			if(p.getName().equalsIgnoreCase(pl.getName())) {
 				count++;
 			}
 			
 		}
 		
 		return count;
 		
 	}
 	
 	private void DeletePlayerFromList(Player pl, ArrayList<Player> al) {
 		
 		for(int i=0 ; i<al.size() ; i++) {
 			
 			Player p = (Player) al.get(i);
 			
 			if(p.getName().equalsIgnoreCase(pl.getName())) {
 				al.remove(i);
 			}
 			
 		}
 		
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
 				
 				e.setCancelled(true);
 				
 			}
 			
 		}
 		
 		
 	}
 	
 	@EventHandler(priority=EventPriority.HIGH)
 	public void showcase(PlayerInteractEvent e) {
 		
 		Player pl = e.getPlayer();
 		Block b = e.getClickedBlock();
 		
 		try {
 			
 			if(e.getAction() != null && b != null && plugin.scs.getShopHandler().isShopBlock(b) && (plugin.scs.getShopHandler().getShopForBlock(b).getAtivitie() == Activities.BUY)) {
 				
 				showcaseUsers.add(pl);
 				
 				if(countPlayersInList(pl, showcaseUsers) >= 5) {
 					
					pl.kickPlayer("Olvasd el a bolt eltti tbllkat!");
 					DeletePlayerFromList(pl, showcaseUsers);
 					
 				}
 				
 			}
 			
 		} catch (ShopNotFoundException e1) {
 			e1.printStackTrace();
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
 		
 		if((e.getEntity() instanceof Enderman) || (e.getEntity() instanceof Blaze) && plugin.config.getBoolean("onlyxpdropwhenkill")) {
 			
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
