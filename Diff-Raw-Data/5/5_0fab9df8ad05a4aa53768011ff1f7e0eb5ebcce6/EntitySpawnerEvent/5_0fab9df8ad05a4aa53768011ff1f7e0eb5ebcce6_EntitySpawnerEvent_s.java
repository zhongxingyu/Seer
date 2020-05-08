 /*
  * This file is part of MineQuest-NPC, Additional Events for MineQuest.
  * MineQuest-NPC is licensed under GNU General Public License v3.
  * Copyright (C) 2012 The MineQuest Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.theminequest.MQCoreEvents.EntityEvent;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Tameable;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 import com.theminequest.MineQuest.API.CompleteStatus;
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.Events.DelayedQuestEvent;
 import com.theminequest.MineQuest.API.Group.QuestGroup;
 import com.theminequest.MineQuest.API.Quest.QuestDetails;
 import com.theminequest.MineQuest.API.Utils.MobUtils;
 
 public class EntitySpawnerEvent extends DelayedQuestEvent {
 	
 	private long delay;
 	
 	private World w;
 	private Location loc;
 	private EntityType t;
 	private boolean dropItems;
 	
 	private LivingEntity entity;
 	
 	private volatile boolean scheduled;
 	private final Object scheduledLock = new Object();
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.QEvent#parseDetails(java.lang.String[])
 	 * [0] Delay in MS
 	 * [1] X
 	 * [2] Y
 	 * [3] Z
 	 * [4] Mob Type
 	 * [5] dropItems;
 	 */
 	@Override
 	public void parseDetails(String[] details) {
 		delay = Long.parseLong(details[0]);
 		String worldname = getQuest().getDetails().getProperty(QuestDetails.QUEST_WORLD);
 		w = Bukkit.getWorld(worldname);
 		double x = Double.parseDouble(details[1]);
 		double y = Double.parseDouble(details[2]);
 		double z = Double.parseDouble(details[3]);
 		loc = new Location(w,x,y,z);
 		t = MobUtils.getEntityType(details[4]);
		dropItems = (!details[5].toLowerCase().startsWith("f"));
 		entity = null;
 		scheduled = false;
 	}
 
 	@Override
 	public boolean delayedConditions() {
 		if (!scheduled && (entity == null || entity.isDead() || !entity.isValid())) {
 			synchronized (scheduledLock) {
 				if (!scheduled) {
 					scheduled = true;
 					Bukkit.getScheduler().scheduleSyncDelayedTask(Managers.getActivePlugin(), new Runnable() {
 						public void run() {
 							if (isComplete() == null) {
 								entity = (LivingEntity) w.spawnEntity(loc, t);
 							}
 							scheduled = false;
 						}
 					});
 				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public CompleteStatus action() {
 		// It should NEVER get here.
 		return CompleteStatus.FAILURE;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.QEvent#entityDeathCondition(org.bukkit.event.entity.EntityDeathEvent)
 	 */
 	@Override
 	public boolean entityDeathCondition(EntityDeathEvent e) {
 		if (entity == null)
 			return false;
 		if (entity.equals(e.getEntity())) {
 			boolean inParty = false;
 			
 			// if people outside the party kill mob, give no xp or items to prevent exploiting
 			LivingEntity el = (LivingEntity) e.getEntity();
 			if (el.getLastDamageCause() instanceof EntityDamageByEntityEvent) {			
 				EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) el.getLastDamageCause();
 				Player p = null;
 				if (edbee.getDamager() instanceof Player) {
 					p = (Player) edbee.getDamager();
 				} else if (edbee.getDamager() instanceof Projectile) {
 					Projectile projectile = (Projectile) edbee.getDamager();
 					if (projectile.getShooter() instanceof Player) {
 						p = (Player) projectile.getShooter();
 					}
 				} else if (edbee.getDamager() instanceof Tameable) {
 					Tameable tameable = (Tameable) edbee.getDamager();
 					if (tameable.getOwner() instanceof Player) {
 						p = (Player) tameable.getOwner();
 					}
 				}
 				
 				if (p != null) {
 					QuestGroup g = Managers.getQuestGroupManager().get(getQuest());
 					List<Player> team = g.getMembers();
 					if (team.contains(p))
 						inParty = true;
 				}
 			}
 			
 			// outside of party gives no drops
 			if (!dropItems || !inParty) {
 				e.setDroppedExp(0);
 				e.getDrops().clear();
 			}
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.QEvent#cleanUpEvent()
 	 */
 	@Override
 	public void cleanUpEvent() {
 		if (entity!=null && !entity.isDead())
 			entity.setHealth(0);
 	}
 
 	@Override
 	public Integer switchTask() {
 		return null;
 	}
 
 	@Override
 	public long getDelay() {
 		return delay;
 	}
 
 }
