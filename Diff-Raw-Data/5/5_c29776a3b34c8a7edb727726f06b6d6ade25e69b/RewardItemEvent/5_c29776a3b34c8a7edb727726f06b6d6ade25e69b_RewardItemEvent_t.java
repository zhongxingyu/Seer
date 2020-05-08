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
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.API.CompleteStatus;
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.Events.QuestEvent;
 import com.theminequest.MineQuest.API.Group.Group;
 import com.theminequest.MineQuest.API.Group.QuestGroup;
 
 public class RewardItemEvent extends QuestEvent {
 	
 	private int taskid;
 	private LinkedHashMap<Integer,Integer> items;
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.QEvent#parseDetails(java.lang.String[])
 	 * [n] itemid,qty
 	 */
 	@Override
 	public void parseDetails(String[] details) {
 		items = new LinkedHashMap<Integer,Integer>();
 		taskid = Integer.parseInt(details[0]);
		for (int i=1; i<details.length; i++){
			String[] d = details[i].split(",");
 			items.put(Integer.parseInt(d[0]),Integer.parseInt(d[1]));
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
 			for (int i : items.keySet()){
 				p.getInventory().addItem(new ItemStack(i,items.get(i)));
 			}
 		}
 		return CompleteStatus.SUCCESS;
 	}
 
 	@Override
 	public Integer switchTask() {
 		return taskid;
 	}
 
 }
