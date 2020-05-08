 package de.bdh.krimtd;
 
 import java.util.List;
 
import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Fireball;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class TDListener implements Listener
 {
 	Main m;
 	public TDListener(Main main) 
 	{
 		this.m = main;
 	}
 
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent event)
     {
 		if(event.getPlayer() == null)
 			return;
 		
 		if(event.getBlock().getType() == Material.WOOL)
 		{
 			int lvl = 1;
 			Block tmp = event.getBlock();
 			while(tmp.getRelative(BlockFace.UP).getData() == event.getBlock().getData() && tmp.getRelative(BlockFace.UP).getType() == event.getBlock().getType())
 			{
 				tmp = tmp.getRelative(BlockFace.UP);
 				++lvl;
 			}
 			tmp = event.getBlock();
 			while(tmp.getRelative(BlockFace.DOWN).getData() == event.getBlock().getData() && tmp.getRelative(BlockFace.DOWN).getType() == event.getBlock().getType())
 			{
 				tmp = tmp.getRelative(BlockFace.DOWN);
 				++lvl;
 			}
 			
 			if(TDTower.getType(event.getBlock().getData()) == 5 && lvl > 1)
 			{
 				event.setCancelled(true);
 				event.getPlayer().sendMessage("BlockTower are Maxxed on Level 1");
 			}
 			else if(this.m.getBlockAround(event.getBlock(), Material.WOOL) != null && TDTower.getType(event.getBlock().getData()) != 5)
 			{
 				event.getPlayer().sendMessage("Cannot build so close to each other");
 				event.setCancelled(true);
 			}
 			else if(event.getBlock().getRelative(BlockFace.DOWN).getType() == Material.WOOL && event.getBlock().getRelative(BlockFace.DOWN).getData() != event.getBlock().getData())
 			{
 				event.getPlayer().sendMessage("Cannot build on another tower");
 				event.setCancelled(true);
 			}
			else if(this.m.closeToPoint(event.getBlock().getLocation(),3))
 			{
 				event.getPlayer().sendMessage("Cannot build on the lane");
 				event.setCancelled(true);
 			}
 			else if(lvl > 5)
 			{
 				event.getPlayer().sendMessage("Tower is on max level");
 				event.setCancelled(true);
 			}
 			else
 			{
 				event.getPlayer().sendMessage("Tower is now level "+ lvl);
 				this.m.registerTower(tmp, lvl,event.getPlayer());
 			}
 		}
     }
 	
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event)
     {
 		if(event.getPlayer() == null)
 			return;
 		
 		if(event.getBlock().getType() == Material.WOOL)
 		{
 			int lvl = 1;
 			Block tmp = event.getBlock();
 			while(tmp.getRelative(BlockFace.UP).getData() == event.getBlock().getData() && tmp.getRelative(BlockFace.UP).getType() == event.getBlock().getType())
 			{
 				tmp = tmp.getRelative(BlockFace.UP);
 				tmp.setType(Material.AIR);
 				++lvl;
 			}
 			tmp = event.getBlock();
 			while(tmp.getRelative(BlockFace.DOWN).getData() == event.getBlock().getData() && tmp.getRelative(BlockFace.DOWN).getType() == event.getBlock().getType())
 			{
 				tmp = tmp.getRelative(BlockFace.DOWN);
 				tmp.setType(Material.AIR);
 				++lvl;
 			}
 			
 			this.m.rePayPlayer(tmp, lvl);
 			this.m.unregisterTower(tmp);
 			
 		}
     }
 	
 	@EventHandler
 	public void onDoingDamage(EntityDamageByEntityEvent event)
 	{
 		if(this.m.mob.get(event.getEntity()) != null)
 		{
 			//Und nun nutzen wir unseren eigenen Handler
 			if(event.getDamager() instanceof Player)
 			{
 				this.m.mob.get(event.getEntity()).doDamage(event.getDamage());
 			}
 			
 			//Machen wir sie dann erstmal unsterblich
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerClick(PlayerInteractEvent event)
 	{
 		if(event.getAction() == Action.LEFT_CLICK_AIR)
 		{
 			int mxl = this.m.calcMaxMobLevel(event.getPlayer().getLocation());
 			int nl = 0;
 			if(this.m.MaxMobLevelPerPlayer.get(event.getPlayer()) != null)
 			{
 				nl = this.m.MaxMobLevelPerPlayer.get(event.getPlayer());
 			}
 			
 			if(nl >= mxl)
 				nl = 0;
 			
 			++nl;
 			
 			this.m.MaxMobLevelPerPlayer.put(event.getPlayer(), nl);
 			event.getPlayer().sendMessage("Now hiring mobs with level: "+nl);
 		}
 		
 		
 		//TODO: Spawn Mobs manuell und schmeiss sie in die TDMob
 	}
 	
 	@EventHandler
 	public void onEntityDeath(EntityDeathEvent event)
     {
 		//Nix wird on Death gedroppt
 		event.getDrops().clear();
     }
 	
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event)
 	{
 		this.m.MaxMobLevelPerPlayer.remove(event.getPlayer());
 	}
 	
 	@EventHandler
 	public void onEntityExplode(EntityExplodeEvent event)
 	{
 		event.blockList().clear();
 	}
 	
 	@EventHandler
 	public void onHit(ProjectileHitEvent event)
     {
 		if(event.getEntity() == null)
 			return;
 		
 		if(this.m.shots.get(event.getEntity()) != null)
 		{
 			List<Entity> l = event.getEntity().getNearbyEntities(6.0, 6.0, 6.0);
 			if(l.size() > 0)
 			{
 				for (Entity e: l)
 		    	{
 					if(e instanceof LivingEntity)
 					{
 						if(this.m.mob.get(e) != null) //Gefeuert von einem Tower (Schnee, Rocket)
 						{
 							if(event.getEntity() instanceof Snowball)
 							{
 								//SCHNEEBALL
 								this.m.mob.get(e).slowed = this.m.shots.get(event.getEntity()).getSlowTime();
 								return; //Immer nur einer geht
 							} else if(event.getEntity() instanceof Fireball)
 							{
 								//ROCKET
 								TDTower t = this.m.shots.get(event.getEntity());
 								float range = t.getAERange();
 								if(e.getLocation().distance(event.getEntity().getLocation()) < range)
 									t.doDamage(this.m.mob.get(e));
 							}
 						}
 					}
 		    	}
 			}
 
 		}
     }
 	
 	@EventHandler
 	public void onFire(EntityDamageEvent event)
     {
 		if(event.getCause() == DamageCause.FIRE_TICK || event.getCause() == DamageCause.FIRE)
 		{
 			if(this.m.mob.get(event.getEntity()) != null)
 			{
 				event.setDamage(0);
 			}
 		}
     }
 	
 	@EventHandler
 	public void onAssault(EntityDamageByEntityEvent event)
     {
 		if(event.getDamager() instanceof Arrow)
 		{
 			if(this.m.shots.get(event.getDamager()) != null)
 			{
 				//Gefeuert von einem Tower (Arrow)
 				event.setCancelled(true);
 				if(this.m.mob.get(event.getEntity()) != null)
 				{
 					TDTower t = this.m.shots.get(event.getDamager());
 					
 					if(event.getDamager().getFireTicks() > 0)
 					{
 						int tt = t.getFireTicks() * 20;
 						event.getEntity().setFireTicks(tt);
 						this.m.mob.get(event.getEntity()).fireDamageFrom = t;
 					} else
 						t.doDamage(this.m.mob.get(event.getEntity()));
 				}
 			}
 		}
     }
 	
 	@EventHandler
 	public void onPlayerPickupItem(PlayerPickupItemEvent event) 
 	{
 		if(event.getItem().getItemStack().getType() == Material.GOLD_NUGGET)
 		{
 			event.getItem().remove();
 			event.setCancelled(true);
 			//TODO: Gib spieler Geld
 		}
 		
 		if(event.getItem().getItemStack().getType() == Material.GOLD_INGOT)
 		{
 			event.getItem().remove();
 			event.setCancelled(true);
 			//TODO: Gib spieler Geld
 		}
 		
 		if(event.getItem().getItemStack().getType() == Material.GOLD_BLOCK)
 		{
 			event.getItem().remove();
 			event.setCancelled(true);
 			//TODO: Gib spieler Geld
 		}
 	}
 }
