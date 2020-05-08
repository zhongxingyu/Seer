 package de.bdh.krimtd;
 
 import java.util.List;
 
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
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
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 
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
 		
 		
 		if(!event.getPlayer().hasPermission("td.admin"))
 			event.setCancelled(true);
     }
 	
 	@EventHandler
 	public void onRespawn(PlayerRespawnEvent event)
     {
 		ItemStack egg = new ItemStack(Material.MONSTER_EGG);
 		egg.setDurability((short) 93);
 		event.getPlayer().getInventory().addItem(new ItemStack(Material.WOOL));
 		event.getPlayer().getInventory().addItem(egg);
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
 			
 			this.m.rePayPlayer(tmp, lvl,event.getPlayer());
 			this.m.unregisterTower(tmp);
 			
 		} else if(!event.getPlayer().hasPermission("td.admin"))
 		{
 			event.setCancelled(true);
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
 		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem() == null && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.WOOL)
 		{
 			int type = TDTower.getType(event.getClickedBlock().getData());
 			event.getPlayer().sendMessage(ChatColor.AQUA+TDTower.name(type)+" Tower Level: ");
 		}
 		else if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem() != null && event.getItem().getType() == Material.WOOL && event.getClickedBlock() != null)
 		{
 			int lvl = 1;
 			Block tmp = event.getClickedBlock().getRelative(BlockFace.DOWN);
 			while(tmp.getRelative(BlockFace.UP).getData() == event.getItem().getData().getData() && tmp.getRelative(BlockFace.UP).getType() == event.getItem().getType())
 			{
 				tmp = tmp.getRelative(BlockFace.UP);
 				++lvl;
 			}
 			tmp = event.getClickedBlock();
 			while(tmp.getRelative(BlockFace.DOWN).getData() == event.getItem().getData().getData() && tmp.getRelative(BlockFace.DOWN).getType() == event.getItem().getType())
 			{
 				tmp = tmp.getRelative(BlockFace.DOWN);
 				++lvl;
 			}
 			
			
 			if(TDTower.getType(event.getClickedBlock().getData()) == 5 && lvl > 1)
 			{
 				event.setCancelled(true);
 				event.getPlayer().sendMessage(ChatColor.RED+"Block Tower are maxxed on Level 1");
 			}
 			else if(this.m.getBlockAround(event.getClickedBlock(), Material.WOOL) != null && TDTower.getType(event.getItem().getData().getData()) != 5)
 			{
 				event.getPlayer().sendMessage(ChatColor.RED+"Cannot build so close to each other");
 				event.setCancelled(true);
 			}
 			else if(event.getClickedBlock().getType() == Material.WOOL && event.getClickedBlock().getData() != event.getItem().getData().getData())
 			{
 				event.getPlayer().sendMessage(ChatColor.RED+"Cannot build on another tower");
 				event.setCancelled(true);
 			}
 			else if(this.m.closeToPoint(event.getClickedBlock().getLocation(),3) && TDTower.getType(event.getItem().getData().getData()) != 5)
 			{
 				event.getPlayer().sendMessage(ChatColor.RED+"Cannot build on the lane");
 				event.setCancelled(true);
 			}
 			else if(TDTower.getType(event.getItem().getData().getData()) == 5 && this.m.isAboveWayPoint(event.getClickedBlock().getRelative(BlockFace.UP)))
 			{
 				event.getPlayer().sendMessage(ChatColor.RED+"Cannot build on a waypoint");
 				event.setCancelled(true);
 			}
 			else if(lvl > 5)
 			{
 				event.getPlayer().sendMessage(ChatColor.RED+"Tower is on max level");
 				event.setCancelled(true);
 			}
 			else
 			{
				if(tmp.getType() != Material.WOOL)
					tmp = tmp.getRelative(BlockFace.UP);
				
 				int type = TDTower.getType(event.getItem().getData().getData());
 				int mon = 0;
 				if(this.m.Money.get(event.getPlayer()) != null)
 					mon = this.m.Money.get(event.getPlayer());
 				int price = TDTower.getPrice(type, lvl);
 				if(price > mon)
 				{
 					event.getPlayer().sendMessage(ChatColor.RED+"You can't afford to build this tower.");
 					event.setCancelled(true);
 				} else
 				{
 					event.getPlayer().sendMessage(TDTower.name(type)+" Tower is level "+ (lvl));
 					this.m.registerTower(tmp, lvl,event.getPlayer());
 					this.m.Money.put(event.getPlayer(),(mon-price));
 					//Place Block
 					Block n = event.getClickedBlock().getWorld().getHighestBlockAt(event.getClickedBlock().getLocation());
 					n.setTypeIdAndData(Material.WOOL.getId(), event.getItem().getData().getData(), false);
 					event.setCancelled(true);
 				}
 			}
 		}
 		else if(event.getAction() == Action.LEFT_CLICK_AIR && event.getItem() != null && event.getItem().getType() == Material.WOOL)
 		{
 			int tp = event.getItem().getData().getData();
 			int ntp = 0;
 			
 			if(tp == 0)
 			{
 				ntp = 11;
 			}
 			else if(tp == 11) //BLUE - EISTOWER
 			{
 				ntp = 14;
 			} else if(tp == 14) //ROT - Flammentower
 			{
 				ntp = 15;
 			} else if(tp == 15) //SCHWARZ - AEDD
 			{
 				ntp = 8;
 			} else if(tp == 8) // GRAU - Granaten
 			{
 				ntp = 13;
 			} else if(tp == 13) // GRÜN - BlockTower
 			{
 				ntp = 1;
 			} else if(tp == 1) // ORANGE - LevelTower
 			{
 				ntp = 10;
 			} else if(tp == 10) // Purple - HeavyDamage
 			{
 				ntp = 0;
 			}
 			
 			int type = TDTower.getType(ntp);
 			event.getPlayer().sendMessage(ChatColor.AQUA+"Now building: "+TDTower.name(type)+ " for "+TDTower.getPrice(type, 1));
 			if(ntp == 1)
 				event.getPlayer().sendMessage(ChatColor.AQUA+"You need this Tower to spawn higher level monsters");
 			event.getPlayer().getItemInHand().setDurability((short)ntp);
 		}
 		else if(event.getAction() == Action.LEFT_CLICK_AIR && event.getItem() != null && event.getItem().getType() == Material.MONSTER_EGG)
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
 			event.getPlayer().sendMessage(ChatColor.AQUA+"Now hiring mobs with level: "+nl);
 		} else if(event.getAction() == Action.LEFT_CLICK_BLOCK &&  event.getItem() != null && event.getItem().getType() == Material.MONSTER_EGG)
 		{
 			event.setCancelled(true);
 			short oldDur = event.getItem().getDurability();
 			short newDur = 50;
 			int nl = 1;
 			
 			if(this.m.MaxMobLevelPerPlayer.get(event.getPlayer()) != null)
 			{
 				nl = this.m.MaxMobLevelPerPlayer.get(event.getPlayer());
 			}
 			
 			if(oldDur == 93)
 			{
 				newDur = 91;
 				event.getPlayer().sendMessage(ChatColor.GREEN+"Now Spawning Lvl "+nl+" Sheeps with "+TDMob.getHP(TDMob.getType(newDur), nl)+" HP for "+TDMob.getPrice(TDMob.getType(newDur), nl));
 			}
 			else if(oldDur == 91)
 			{
 				newDur = 90;
 				event.getPlayer().sendMessage(ChatColor.GREEN+"Now Spawning Lvl "+nl+" Pigs with "+TDMob.getHP(TDMob.getType(newDur), nl)+" HP for "+TDMob.getPrice(TDMob.getType(newDur), nl));
 			}
 			else if(oldDur == 90)
 			{
 				newDur = 92;
 				event.getPlayer().sendMessage(ChatColor.GREEN+"Now Spawning Lvl "+nl+" Cows with "+TDMob.getHP(TDMob.getType(newDur), nl)+" HP for "+TDMob.getPrice(TDMob.getType(newDur), nl));
 			}
 			else if(oldDur == 92)
 			{
 				newDur = 95;
 				event.getPlayer().sendMessage(ChatColor.GREEN+"Now Spawning Lvl "+nl+" Wolf with "+TDMob.getHP(TDMob.getType(newDur), nl)+" HP for "+TDMob.getPrice(TDMob.getType(newDur), nl));
 			}
 			else if(oldDur == 95)
 			{
 				newDur = 120;
 				event.getPlayer().sendMessage(ChatColor.GREEN+"Now Spawning Lvl "+nl+" Villager with "+TDMob.getHP(TDMob.getType(newDur), nl)+" HP for "+TDMob.getPrice(TDMob.getType(newDur), nl));
 			}
 			else if(oldDur == 120)
 			{
 				newDur = 51;
 				event.getPlayer().sendMessage(ChatColor.GREEN+"Now Spawning Lvl "+nl+" Skeleton with "+TDMob.getHP(TDMob.getType(newDur), nl)+" HP for "+TDMob.getPrice(TDMob.getType(newDur), nl));
 			}
 			else if(oldDur == 51)
 			{
 				newDur = 54;
 				event.getPlayer().sendMessage(ChatColor.GREEN+"Now Spawning Lvl "+nl+" Zombie with "+TDMob.getHP(TDMob.getType(newDur), nl)+" HP for "+TDMob.getPrice(TDMob.getType(newDur), nl));
 			}
 			else if(oldDur == 54)
 			{
 				newDur = 96;
 				event.getPlayer().sendMessage(ChatColor.GREEN+"Now Spawning Lvl "+nl+" Moshroom with "+TDMob.getHP(TDMob.getType(newDur), nl)+" HP for "+TDMob.getPrice(TDMob.getType(newDur), nl));
 			}
 			else if(oldDur == 96)
 			{
 				newDur = 50;
 				event.getPlayer().sendMessage(ChatColor.GREEN+"Now Spawning Lvl "+nl+" Creeper with "+TDMob.getHP(TDMob.getType(newDur), nl)+" HP for "+TDMob.getPrice(TDMob.getType(newDur), nl));
 			}
 			else if(oldDur == 50)
 			{
 				newDur = 66;
 				event.getPlayer().sendMessage(ChatColor.GREEN+"Now Spawning Lvl "+nl+" Witch with "+TDMob.getHP(TDMob.getType(newDur), nl)+" HP for "+TDMob.getPrice(TDMob.getType(newDur), nl));
 			}
 			else if(oldDur == 66)
 			{
 				newDur = 99;
 				event.getPlayer().sendMessage(ChatColor.GREEN+"Now Spawning Lvl "+nl+" IronGolem with "+TDMob.getHP(TDMob.getType(newDur), nl)+" HP for "+TDMob.getPrice(TDMob.getType(newDur), nl));
 			}
 			else if(oldDur == 99)
 			{
 				newDur = 64;
 				event.getPlayer().sendMessage(ChatColor.GREEN+"Now Spawning Lvl "+nl+" Withers with "+TDMob.getHP(TDMob.getType(newDur), nl)+" HP for "+TDMob.getPrice(TDMob.getType(newDur), nl));
 			}
 			else if(oldDur == 64)
 			{
 				newDur = 93;
 				event.getPlayer().sendMessage(ChatColor.GREEN+"Now Spawning Lvl "+nl+" Chickens with "+TDMob.getHP(TDMob.getType(newDur), nl)+" HP for "+TDMob.getPrice(TDMob.getType(newDur), nl));
 			}
 			event.getPlayer().getItemInHand().setDurability(newDur);
 			event.getPlayer().sendMessage(ChatColor.AQUA+"Income for each spawned Monster: "+TDMob.getIncomeHeight(TDMob.getType(newDur), nl));
 			
 		} else if(event.getAction() == Action.RIGHT_CLICK_BLOCK &&  event.getItem() != null && event.getItem().getType() == Material.MONSTER_EGG)
 		{
 			event.setCancelled(true);
 			//Spawn Mob with Level
             Location to = this.m.findNextPoint(event.getClickedBlock().getLocation());
 			if(to != null)
 			{
 				int lvl = 1;
 				if(this.m.MaxMobLevelPerPlayer.get(event.getPlayer()) != null)
 					lvl = this.m.MaxMobLevelPerPlayer.get(event.getPlayer());
 				
 				
 				int typid = TDMob.getType(event.getItem().getDurability());
 				int money = 0;
 				if(this.m.Money.get(event.getPlayer()) != null)
 				{
 					money = this.m.Money.get(event.getPlayer());
 				}
 				int price = TDMob.getPrice(typid, lvl);
 				
 				if(money < price)
 				{
 					event.getPlayer().sendMessage(ChatColor.RED+"You don't have enough money to spawn this mob.");
 				} else
 				{
 					EntityType type = TDMob.getBukkitType(typid);
 					Location l = event.getClickedBlock().getLocation();
 					l.setY(l.getY()+2);
 					
 					LivingEntity mob = (LivingEntity) event.getPlayer().getWorld().spawnEntity(l, type);
 		            mob.setHealth(1);
 		            
 					TDMob m;
 					
 					if(this.m.mob.get(mob) == null)
 					{
 						m = new TDMob(this.m,mob,lvl);
 						m.target = to;
 						this.m.moveMob(mob, to, m.getSpeed());
 						
 						int inc = 10;
 						if(this.m.Income.get(event.getPlayer()) != null)
 							inc = this.m.Income.get(event.getPlayer());
 						inc += TDMob.getIncomeHeight(typid, lvl);
 						this.m.Income.put(event.getPlayer(), inc);
 						this.m.Money.put(event.getPlayer(), (money - price));
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onEntityDeath(EntityDeathEvent event)
     {
 		//Nix wird on Death gedroppt
 		event.getDrops().clear();
     }
 	
 	@EventHandler
 	public void onBlockDrop(PlayerDropItemEvent event)
 	{
 		//Wir können nichts aus dem Inventar droppen
 		event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event)
 	{
 		this.m.MaxMobLevelPerPlayer.remove(event.getPlayer());
 		this.m.Money.remove(event.getPlayer());
 		this.m.Income.remove(event.getPlayer());
 	}
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event)
 	{
 		this.m.Income.put(event.getPlayer(), 10);
 		this.m.Money.put(event.getPlayer(), 0);
 		event.getPlayer().sendMessage(ChatColor.GREEN+"Welcome to KrimTD (Tower Defense) - Powered by www.worldofminecraft.de");
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
 		int oldMoney = 0;
 		if(this.m.Money.get(event.getPlayer()) != null)
 			oldMoney = this.m.Money.get(event.getPlayer());
 		if(event.getItem().getItemStack().getType() == Material.GOLD_NUGGET)
 		{
 			event.getItem().remove();
 			event.setCancelled(true);
 			this.m.Money.put(event.getPlayer(), (oldMoney+event.getItem().getItemStack().getAmount()));
 		}
 		
 		else if(event.getItem().getItemStack().getType() == Material.GOLD_INGOT)
 		{
 			event.getItem().remove();
 			event.setCancelled(true);
 			this.m.Money.put(event.getPlayer(), (oldMoney+event.getItem().getItemStack().getAmount()*10));
 		}
 		
 		else if(event.getItem().getItemStack().getType() == Material.GOLD_BLOCK)
 		{
 			event.getItem().remove();
 			event.setCancelled(true);
 			this.m.Money.put(event.getPlayer(), (oldMoney+event.getItem().getItemStack().getAmount()*100));
 		}
 	}
 }
