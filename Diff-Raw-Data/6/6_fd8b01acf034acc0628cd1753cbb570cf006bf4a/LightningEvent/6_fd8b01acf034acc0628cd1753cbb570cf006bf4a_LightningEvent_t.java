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
 package com.theminequest.MQCoreEvents.EnvEvent;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.LivingEntity;
 
 import com.theminequest.MineQuest.API.CompleteStatus;
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.Events.QuestEvent;
 import com.theminequest.MineQuest.API.Quest.Quest;
 import com.theminequest.MineQuest.API.Quest.QuestDetails;
 import com.theminequest.MineQuest.API.Quest.QuestUtils;
 
 public class LightningEvent extends QuestEvent {
 	
 	private long delay;
 	private long starttime;
 	
 	private boolean targeted;
 	private int targetid;
 	private Location loc;
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.QEvent#parseDetails(java.lang.String[])
 	 * HANDLE NORMAL + TARGETING
 	 */
 	@Override
 	public void parseDetails(String[] details) {
 		if (details[0].equalsIgnoreCase("T")){
 			targeted = true;
 			delay = Long.parseLong(details[1]);
 			targetid = Integer.parseInt(details[2]);
 		}else{
 			targeted = false;
 			delay = Long.parseLong(details[0]);
 			String worldname = getQuest().getDetails().getProperty(QuestDetails.QUEST_WORLD);
 			World w = Bukkit.getWorld(worldname);
			double x = Double.parseDouble(details[1]);
			double y = Double.parseDouble(details[2]);
			double z = Double.parseDouble(details[3]);
 			loc = new Location(w,x,y,z);
 		}
 		starttime = System.currentTimeMillis();
 	}
 
 	@Override
 	public boolean conditions() {
 		if (starttime+delay>System.currentTimeMillis())
 			return false;
 		return true;
 	}
 
 	@Override
 	public CompleteStatus action() {
 		String worldname = getQuest().getDetails().getProperty(QuestDetails.QUEST_WORLD);
 		World w = Bukkit.getWorld(worldname);
 		if (targeted){
 			List<LivingEntity> targets = Managers.getTargetManager().processTargetDetails(getQuest(), QuestUtils.getTargetDetails(getQuest(), targetid));
 			for (LivingEntity e : targets){
 				w.strikeLightning(e.getLocation());
 			}
 		} else {
 			w.strikeLightning(loc);
 		}
 		return CompleteStatus.SUCCESS;
 	}
 
 	@Override
 	public Integer switchTask() {
 		return null;
 	}
 
 }
