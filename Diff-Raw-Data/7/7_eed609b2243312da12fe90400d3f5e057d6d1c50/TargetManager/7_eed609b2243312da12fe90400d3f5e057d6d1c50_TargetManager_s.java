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
 package com.theminequest.MineQuest.Target;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.Events.QuestEvent;
import com.theminequest.MineQuest.API.Events.TargetQuestEvent;
 import com.theminequest.MineQuest.API.Group.Group;
 import com.theminequest.MineQuest.API.Quest.Quest;
 import com.theminequest.MineQuest.API.Quest.QuestDetails;
 import com.theminequest.MineQuest.API.Quest.QuestUtils;
 import com.theminequest.MineQuest.API.Target.TargetDetails;
 import com.theminequest.MineQuest.API.Target.TargetDetails.TargetType;
 import com.theminequest.MineQuest.API.Task.QuestTask;
 
 public class TargetManager implements com.theminequest.MineQuest.API.Target.TargetManager {
 
 	public List<LivingEntity> processTargetDetails(Quest questid, TargetDetails t) {
 		if (t.getType() == TargetType.AREATARGET)
 			return areaTarget(questid,t);
 		else if (t.getType() == TargetType.AREATARGETQUESTER)
 			return areaTargetQuester(questid,t);
 		else if (t.getType() == TargetType.TEAMTARGET)
 			return partyTarget(questid,t);
 		else if (t.getType() == TargetType.TARGETTER)
 			return targetter(questid,t);
 		else if (t.getType() == TargetType.TARGETTEREDIT)
 			return targetteredit(questid,t);
 		else if (t.getType() == TargetType.RANDOMTARGET)
 			return randomtarget(questid,t);
 		else if (t.getType() == TargetType.LEADER)
 			return leader(questid,t);
 		return null;
 	}
 
 	/*
 	 * Details: x:y:z:radius
 	 */
 	private List<LivingEntity> areaTarget(Quest q, TargetDetails t) {
 		String[] details = t.getDetails().split(":");
 		double x = Double.parseDouble(details[0]);
 		double y = Double.parseDouble(details[1]);
 		double z = Double.parseDouble(details[2]);
 		double r = Double.parseDouble(details[3]);
 		String world = q.getDetails().getProperty(QuestDetails.QUEST_WORLD);
 		World w = Bukkit.getWorld(world);
 		Location l = new Location(w, x, y, z);
 		return getEntitiesAroundRadius(l, r);
 	}
 
 	/*
 	 * Quester = PLAYER targetID:radius
 	 */
 	private List<LivingEntity> areaTargetQuester(Quest q, TargetDetails t) {
 		List<LivingEntity> all = new ArrayList<LivingEntity>();
 		String[] details = t.getDetails().split(":");
 		int targetID = Integer.parseInt(details[0]);
 		double r = Double.parseDouble(details[1]);
 		List<LivingEntity> es = processTargetDetails(q,QuestUtils.getTargetDetails(q,targetID));
 		for (LivingEntity en : es) {
 			if (en instanceof Player)
 				all.addAll(getEntitiesAroundRadius(en.getLocation(), r));
 		}
 		return all;
 	}
 
 	/*
 	 * this does NOT specify any details (details = "")
 	 */
 	private List<LivingEntity> partyTarget(Quest q, TargetDetails t) {
 		Group team = Managers.getQuestGroupManager().get(q);
 		List<LivingEntity> list = new ArrayList<LivingEntity>();
 		list.addAll(team.getMembers());
 		return list;
 	}
 
 	/*
 	 * eventid1,eventid2,etc...
 	 */
 	private List<LivingEntity> targetter(Quest q, TargetDetails t) {
 		String[] ids = t.getDetails().split(",");
 		List<LivingEntity> toreturn = new ArrayList<LivingEntity>();
 		for (String id : ids) {
 			int i = Integer.parseInt(id);
 			QuestTask ts = q.getActiveTask();
 			Collection<QuestEvent> running = ts.getEvents();
 			for (QuestEvent event : running) {
 				if (event.getEventId() == i) {
					if (event instanceof TargetQuestEvent) {
						toreturn.addAll(((TargetQuestEvent) event).getTargets());
 					}
 				}
 			}
 		}
 		return toreturn;
 	}
 	
 	/*
 	 * editid1,editid2,editid3...
 	 * TODO: actually implement this... even though this doesn't make any sense.
 	 */
 	@Deprecated
 	private List<LivingEntity> targetteredit(Quest q, TargetDetails t) {
 		String[] ids = t.getDetails().split(",");
 		List<LivingEntity> toreturn = new ArrayList<LivingEntity>();
 		return toreturn;
 	}
 	
 	private List<LivingEntity> randomtarget(Quest q, TargetDetails t){
 		String targetid = t.getDetails();
 		List<LivingEntity> toreturn = new ArrayList<LivingEntity>();
 		List<LivingEntity> randomchoosing = processTargetDetails(q,QuestUtils.getTargetDetails(q,Integer.parseInt(targetid)));
 		toreturn.add(randomchoosing.get(new Random().nextInt(randomchoosing.size())));
 		return toreturn;
 	}
 
 	private List<LivingEntity> leader(Quest questid, TargetDetails t) {
 		Group team = Managers.getQuestGroupManager().get(questid);
 		List<LivingEntity> list = new LinkedList<LivingEntity>();
 		list.add(team.getLeader());
 		return list;
 	}
 
 	private static List<LivingEntity> getEntitiesAroundRadius(Location l,
 			double radius) {
 		List<LivingEntity> toreturn = new ArrayList<LivingEntity>();
 		List<LivingEntity> entities = l.getWorld().getLivingEntities();
 		for (LivingEntity e : entities) {
 			Location el = e.getLocation();
 			double distance = el.distance(l);
 			if (distance <= radius)
 				toreturn.add(e);
 		}
 		return toreturn;
 	}
 
 }
