 /*
  * This file is part of Common Implementation + API for MineQuest, This provides the common classes for the MineQuest official implementation of the API..
  * Common Implementation + API for MineQuest is licensed under GNU General Public License v3.
  * Copyright (C) The MineQuest Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.theminequest.common.quest.v1;
 
 import static com.theminequest.common.util.I18NMessage._;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import com.theminequest.api.quest.Quest;
 import com.theminequest.api.quest.QuestDetails;
 import com.theminequest.api.quest.QuestDetailsUtils;
 import com.theminequest.api.requirement.QuestRequirement;
 import com.theminequest.api.targeted.QuestTarget;
 import com.theminequest.common.quest.CommonQuestDetails;
 
 public class V1QuestDetails extends CommonQuestDetails {
 	
 	private static final long serialVersionUID = -8388475009904440931L;
 
 	/**
 	 * Represents the MQ <= 1 style tasking system.
 	 * Return a boolean.
 	 */
 	public static final String V1_OLDSTYLETASK = "v1.oldtask";
 	
 	/**
 	 * Represents currently running events.
 	 * Returns a CopyOnWriteArraySet of Integers.
 	 */
 	public static final String V1_OLDSTYLERUNNING = "v1.running";
 	
 	/**
 	 * Represents task descriptions.
 	 * Returns a HashMap&lt;Integer, String&gt;.
 	 */
 	public static final String V2_TASKDESCRIPTIONS = "v2.descriptions";
 
 	public V1QuestDetails(File questFile) {
		super(questFile.getName().substring(0, questFile.getName().lastIndexOf(".quest")));
 		setProperty(QuestDetails.QUEST_FILE, questFile);
 		// DEFAULTS start
		setProperty(QuestDetails.QUEST_DISPLAYNAME, getName());
 		setProperty(QuestDetails.QUEST_DESCRIPTION, _("No description provided..."));
 		setProperty(QuestDetails.QUEST_ACCEPT, _("Quest Accepted!"));
 		setProperty(QuestDetails.QUEST_ABORT, _("Quest Aborted!"));
 		setProperty(QuestDetails.QUEST_COMPLETE, _("Quest Completed!"));
 		setProperty(QuestDetails.QUEST_FAIL, _("Quest Failed!"));
 		setProperty(QuestDetails.QUEST_SPAWNRESET, true);
 		
 		double[] spawnPoint = new double[3];
 		spawnPoint[0] = 0;
 		spawnPoint[1] = 64;
 		spawnPoint[2] = 0;
 		setProperty(QuestDetails.QUEST_SPAWNPOINT, spawnPoint);
 		
 		setProperty(QuestDetails.QUEST_EDITMESSAGE, _("&7You can't edit this part of the world."));
 		setProperty(QuestDetails.QUEST_WORLD, "world");
 		setProperty(QuestDetails.QUEST_LOADWORLD, false);
 		
 		setProperty(QuestDetails.QUEST_TASKS, new HashMap<Integer, Integer[]>(0));
 		setProperty(QuestDetails.QUEST_EVENTS, new HashMap<Integer, String>(0));
 		
 		setProperty(QuestDetails.QUEST_WORLDFLAGS, 0);
 		setProperty(QuestDetails.QUEST_REQUIREMENTDETAILS, new HashMap<Integer, QuestRequirement>());
 		setProperty(QuestDetails.QUEST_GETREQUIREMENTS, new LinkedList<Integer>());
 		setProperty(QuestDetails.QUEST_STARTREQUIREMENTS, new LinkedList<Integer>());
 		
 		setProperty(QuestDetails.QUEST_TARGET, new HashMap<Integer, QuestTarget>());
 		
 		setProperty(V1_OLDSTYLETASK, false);
 		setProperty(V2_TASKDESCRIPTIONS, new HashMap<Integer, String>());
 	}
 	
 	
 	@Override
 	public Quest generateQuest(long questId, String questOwner) {
 		QuestDetails copy = QuestDetailsUtils.getCopy(this);
 		
 		return new V1Quest(questId, copy, questOwner);		
 	}
 	
 }
