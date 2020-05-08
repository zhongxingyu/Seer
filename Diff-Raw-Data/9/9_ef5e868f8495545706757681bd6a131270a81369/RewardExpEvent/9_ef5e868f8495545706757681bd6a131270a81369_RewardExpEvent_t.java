 /*
  * This file, RewardExpEvent.java, is part of MineQuest:
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
  */
 package com.theminequest.MQCoreRPG.QEvents;
 
 import org.bukkit.entity.Player;
 
 import com.theminequest.MQCoreRPG.MQCoreRPG;
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.BukkitEvents.CompleteStatus;
 import com.theminequest.MineQuest.EventsAPI.QEvent;
 import com.theminequest.MineQuest.Group.Group;
 import com.theminequest.MineQuest.Group.Team;
 
 public class RewardExpEvent extends QEvent {
 	
 	private int exptogive;
 
 	public RewardExpEvent(long q, int e, String details) {
 		super(q, e, details);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.theminequest.MineQuest.EventsAPI.QEvent#parseDetails(java.lang.String[])
 	 * Details:
 	 * [0] amount of exp to give.
 	 */
 	@Override
 	public void parseDetails(String[] details) {
 		exptogive = Integer.parseInt(details[0]);
 	}
 
 	@Override
 	public boolean conditions() {
 		return true;
 	}
 
 	@Override
 	public CompleteStatus action() {
 		long gid = MineQuest.groupManager.indexOfQuest(MineQuest.questManager.getQuest(getQuestId()));
 		Group g = MineQuest.groupManager.getGroup(gid);
 		for (Player p : g.getPlayers())
			MQCoreRPG.playerManager.getPlayerDetails(p).modifyExperienceBy(exptogive/g.getPlayers().size());
 		return CompleteStatus.SUCCESS;
 	}
 
 }
