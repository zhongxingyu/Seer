 /**
  * This file, Task.java, is part of MineQuest:
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
 package com.theminequest.MineQuest.Tasks;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.BukkitEvents.CompleteStatus;
 import com.theminequest.MineQuest.BukkitEvents.TaskCompleteEvent;
 import com.theminequest.MineQuest.EventsAPI.QEvent;
 import com.theminequest.MineQuest.Quest.Quest;
 import com.theminequest.MineQuest.Quest.QuestManager;
 
 public class Task {
 
 	private boolean started;
 	private boolean complete;
 	private long questid;
 	private int taskid;
 	private LinkedHashMap<Integer,QEvent> collection;
 
 	/**
 	 * Task for a Quest.
 	 * 
 	 * @param questid
 	 *            Associated Quest
 	 * @param taskid
 	 *            Task ID
 	 * @param events
 	 *            Event numbers that must be completed
 	 */
 	public Task(long questid, int taskid, List<Integer> events) {
 		System.out.println("9");
 		started = false;
 		System.out.println("10");
 		complete = false;
 		System.out.println("11");
 		this.questid = questid;
 		System.out.println("12");
 		this.taskid = taskid;
 		System.out.println("13");
 		collection = new LinkedHashMap<Integer,QEvent>();
 		System.out.println("14");
 		for (int e : events){
 			System.out.println("15 REPEAT");
 			collection.put(e, null);
 		}
 		System.out.println("17");
 	}
 
 	public synchronized void start() {
 		System.out.println("19");
 		if (started)
 			return;
 		System.out.println("20");
 		started = true;
 		System.out.println("21");
 		Quest quest = MineQuest.questManager.getQuest(questid);
 		System.out.println("22");
 		Iterator<Integer> i = collection.keySet().iterator();
 		System.out.println("23");
 		while (i.hasNext()){
 			System.out.println("24 REPEAT");
 			Integer event = i.next();
 			System.out.println("Got here.");
 			System.out.println("Event #: " + event);
 			String d = quest.getEvent(event);
 			System.out.println("Splitting...");
 			String[] eventdetails = d.split(":");
 			String recombined = "";
 			for (int r=1; r<eventdetails.length; r++){
 				recombined+=eventdetails[r];
 				if (r!=(eventdetails.length-1));
 					recombined+=":";
 			}
 			QEvent e = MineQuest.eventManager.getNewEvent(eventdetails[0], questid, event, recombined);
			collection.put(event, e);
 		}
 		
 		i = collection.keySet().iterator();
 		while (i.hasNext()){
 			collection.get(i.next()).fireEvent();
 		}
 	}
 
 	public synchronized void cancelTask() {
 		if (complete || !started)
 			return;
 		complete = true;
 		for (QEvent e : collection.values()) {
 			e.complete(CompleteStatus.CANCELED);
 		}
 		TaskCompleteEvent e = new TaskCompleteEvent(questid, taskid,
 				CompleteStatus.CANCELED);
 		Bukkit.getPluginManager().callEvent(e);
 	}
 
 	public synchronized void finishEvent(int eventid,
 			CompleteStatus completeStatus) {
 		if (!complete && started && collection.containsKey(eventid)) {
 			if (completeStatus == CompleteStatus.FAILURE) {
 				for (QEvent event : collection.values())
 					event.complete(CompleteStatus.CANCELED);
 				complete = true;
 				TaskCompleteEvent e = new TaskCompleteEvent(questid, taskid,
 						CompleteStatus.FAILURE);
 				Bukkit.getPluginManager().callEvent(e);
 			} else
 				checkCompletion();
 		}
 	}
 
 	private synchronized void checkCompletion() {
 		for (Integer eventid : collection.keySet()) {
 			if (collection.get(eventid).isComplete()==null)
 				return;
 		}
 		complete = true;
 		TaskCompleteEvent e = new TaskCompleteEvent(questid, taskid,
 				CompleteStatus.SUCCESS);
 		Bukkit.getPluginManager().callEvent(e);
 	}
 
 	public synchronized boolean isComplete() {
 		return complete;
 	}
 
 	public long getQuestID() {
 		return questid;
 	}
 
 	public int getTaskID() {
 		return taskid;
 	}
 
 	public Collection<QEvent> getEvents() {
 		return collection.values();
 	}
 
 }
