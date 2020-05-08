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
 
 import java.util.Arrays;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.API.CompleteStatus;
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.Events.QuestEvent;
 import com.theminequest.MineQuest.API.Group.Group;
 import com.theminequest.MineQuest.API.Group.QuestGroup;
 
 public class RewardCmdEvent extends QuestEvent {
 	
 	private int taskid;
 	private String[] cmds;
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.QEvent#parseDetails(java.lang.String[])
 	 * [0] taskid
 	 * [n] command
 	 * * %p in commands are substituted with player name.
 	 */
 	@Override
 	public void parseDetails(String[] details) {
 		taskid = Integer.parseInt(details[0]);
		cmds = Arrays.copyOfRange(details, 1, details.length, String[].class);
 	}
 
 	@Override
 	public boolean conditions() {
 		return true;
 	}
 
 	@Override
 	public CompleteStatus action() {
 		QuestGroup g = Managers.getQuestGroupManager().get(getQuest());
 		for (Player p : g.getMembers()){
 			for (String s : cmds){
 				String mod = s.replaceAll("%p", p.getName());
 				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), mod);
 			}
 		}
 		return CompleteStatus.SUCCESS;
 	}
 
 	@Override
 	public Integer switchTask() {
 		return taskid;
 	}
 
 }
