 /*
  * This file is part of MineQuest, The ultimate MMORPG plugin!.
  * MineQuest is licensed under GNU General Public License v3.
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
 package com.theminequest.MQCoreEvents;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.API.CompleteStatus;
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.Events.QuestEvent;
 import com.theminequest.MineQuest.API.Group.Group;
 import com.theminequest.MineQuest.API.Group.QuestGroup;
 import com.theminequest.MineQuest.Group.Party;
 
 public class RewardPermEvent extends QuestEvent {
 	
 	private int taskid;
 	private List<String> permissions;
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.QEvent#parseDetails(java.lang.String[])
 	 * Details:
 	 * [n]: permission
 	 */
 	@Override
 	public void parseDetails(String[] details) {
 		taskid = Integer.parseInt(details[0]);
		details = Arrays.copyOfRange(details, 1, details.length, String[].class);
 		permissions = new ArrayList<String>();
 		for (String d : details){
 			permissions.add(d);
 		}
 	}
 
 	@Override
 	public boolean conditions() {
 		return true;
 	}
 
 	@Override
 	public CompleteStatus action() {
 		QuestGroup g = Managers.getQuestGroupManager().get(getQuest());
 		for (Player p : g.getMembers()){
 			for (String s : permissions){
 				MineQuest.permission.playerAdd(Bukkit.getWorlds().get(0),p.getName(), s);
 			}
 		}
 		return CompleteStatus.SUCCESS;
 	}
 
 	@Override
 	public Integer switchTask() {
 		return taskid;
 	}
 
 }
