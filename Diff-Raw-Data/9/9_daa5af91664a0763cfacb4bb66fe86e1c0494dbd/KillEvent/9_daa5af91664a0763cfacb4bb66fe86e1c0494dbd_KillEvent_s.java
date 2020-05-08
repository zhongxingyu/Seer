 /**
  * This file, KillEvent.java, is part of MineQuest:
  * A full featured and customizable quest/mission system.
  * Copyright (C) 2012 The MineQuest Team
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  **/
 package com.theminequest.MQCoreEvents.BasicEvents;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.BukkitEvents.CompleteStatus;
 import com.theminequest.MineQuest.EventsAPI.QEvent;
 import com.theminequest.MineQuest.Group.Group;
 import com.theminequest.MineQuest.Group.Team;
 
 public class KillEvent extends QEvent {
 	
 	private List<EntityType> typestokill;
 	private int totaltokill;
 	private int currentkill;
 	private int taskid;
 
 	public KillEvent(long q, int e, String details) {
 		super(q, e, details);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.theminequest.MineQuest.EventsAPI.QEvent#parseDetails(java.lang.String[])
 	 * delay ms DEPRECATED.
 	 * [1]: task id to trigger
 	 * [2]: entities
 	 * [3]: total # to kill
 	 */
 	@Override
 	public void parseDetails(String[] details) {
 		taskid = Integer.parseInt(details[1]);
 		String[] entity = details[2].split(",");
 		typestokill = new ArrayList<EntityType>();
 		for (String e : entity){
 			typestokill.add(EntityType.fromName(e));
 		}
 		totaltokill = Integer.parseInt(details[3]);
 		currentkill = 0;
 	}
 
 	@Override
 	public boolean conditions() {
 		return false;
 	}
 
 	@Override
 	public CompleteStatus action() {
 		return CompleteStatus.SUCCESS;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.EventsAPI.QEvent#entityDamageByEntityCondition(org.bukkit.event.entity.EntityDamageByEntityEvent)
 	 */
 	@Override
 	public boolean entityDamageByEntityCondition(EntityDamageByEntityEvent e) {
 		if (e.getDamager() instanceof Player){
 			Player p = (Player)e.getDamager();
 			for (EntityType t : typestokill){
 				if (e.getEntityType()==t){
 					long gid = MineQuest.groupManager.indexOfQuest(MineQuest.questManager.getQuest(getQuestId()));
 					Group g = MineQuest.groupManager.getGroup(gid);
 					List<Player> team = g.getPlayers();
 					if (team.contains(p)){
 						currentkill++;
 						if (currentkill>=totaltokill)
 							return true;
 						else
 							return false;
 					} else
 						return false;
 				}
 			}				
 		}
 		return false;
 	}
 
 	@Override
 	public Integer switchTask() {
 		return taskid;
 	}
 
 }
