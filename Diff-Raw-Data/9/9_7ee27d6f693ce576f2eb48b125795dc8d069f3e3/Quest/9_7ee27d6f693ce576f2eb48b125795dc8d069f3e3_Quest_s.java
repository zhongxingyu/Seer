 /**
  * This file, Quest.java, is part of MineQuest:
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
 package com.theminequest.MineQuest.Quest;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.World.Environment;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.BukkitEvents.CompleteStatus;
 import com.theminequest.MineQuest.BukkitEvents.QuestCompleteEvent;
 import com.theminequest.MineQuest.BukkitEvents.QuestStartedEvent;
 import com.theminequest.MineQuest.BukkitEvents.TaskCompleteEvent;
 import com.theminequest.MineQuest.Editable.AreaEdit;
 import com.theminequest.MineQuest.Editable.CertainBlockEdit;
 import com.theminequest.MineQuest.Editable.CoordinateEdit;
 import com.theminequest.MineQuest.Editable.Edit;
 import com.theminequest.MineQuest.Editable.InsideAreaEdit;
 import com.theminequest.MineQuest.Editable.ItemInHandEdit;
 import com.theminequest.MineQuest.Editable.OutsideAreaEdit;
 import com.theminequest.MineQuest.EventsAPI.NamedQEvent;
 import com.theminequest.MineQuest.EventsAPI.QEvent;
 import com.theminequest.MineQuest.Group.Group;
 import com.theminequest.MineQuest.Group.Team;
 import com.theminequest.MineQuest.Target.TargetDetails;
 import com.theminequest.MineQuest.Tasks.Task;
 import com.theminequest.MineQuest.Utils.ChatUtils;
 import com.theminequest.MineQuest.Utils.TimeUtils;
 
 public class Quest {
 
 	public final QuestDescription details;
 
 	public final long questid;
 	private int currenttask;
 
 	private CompleteStatus finished;
 	private Task activeTask;
 
 
 	/*
 	 * Constructor will start the quest for the user.
 	 */
 	protected Quest(long questid, QuestDescription id) {
 		details = id;
 		this.questid = questid;
 		currenttask = -1;
 		activeTask = null;
 
 		// sort the tasks, events, and targets in order of id.
 		// because we have absolutely 0 idea if someone would skip numbers...
 
 		// load the world if necessary/move team to team leader
 		if (Bukkit.getWorld(details.world) == null) {
 			WorldCreator w = new WorldCreator(details.world);
 			if (details.nether)
 				w = w.environment(Environment.NETHER);
 			Bukkit.createWorld(w);
 		}
 		if (details.loadworld) {
 			try {
 				details.world = QuestWorldManip.copyWorld(Bukkit.getWorld(details.world))
 						.getName();
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		// enable edits
 		for (Edit e : details.editables.values())
 			e.startEdit(questid);
 
 		QuestStartedEvent event = new QuestStartedEvent(this);
 		Bukkit.getPluginManager().callEvent(event);
 	}
 
 	private Integer getFirstKey(Set<Integer> s) {
 		int first = Integer.MAX_VALUE;
 		Iterator<Integer> it = s.iterator();
 		while (it.hasNext()) {
 			int i = it.next();
 			if (i < first)
 				first = i;
 		}
 		return first;
 
 	}
 
 	private ArrayList<Integer> getSortedKeys(Set<Integer> s) {
 		ArrayList<Integer> a = new ArrayList<Integer>();
 		Iterator<Integer> i = s.iterator();
 		while (i.hasNext())
 			a.add(i.next());
 		Collections.sort(a);
 		return a;
 	}
 
 	/**
 	 * Get all possible events
 	 * 
 	 * @return all possible events (# association)
 	 */
 	public Set<Integer> getEventNums() {
 		return details.events.keySet();
 	}
 
 	public boolean startQuest(){
 		return startTask(getFirstKey(details.tasks.keySet()));
 	}
 
 	/**
 	 * Start a task of the quest.
 	 * 
 	 * @param taskid
 	 *            task to start
 	 * @return true if task was started successfully
 	 */
 	public boolean startTask(int taskid) {
 		if (taskid == -1) {
 			finishQuest(CompleteStatus.SUCCESS);
 			return true;
 		}
 		if (!details.tasks.containsKey(taskid))
 			return false;
 		if (activeTask!=null && !activeTask.isComplete())
 			activeTask.cancelTask();
 		currenttask = taskid;
 		String[] eventnums = details.tasks.get(taskid);
 		List<Integer> eventnum = new ArrayList<Integer>();
 		for (String e : eventnums) {
 			eventnum.add(Integer.parseInt(e));
 		}
 		activeTask = new Task(questid, taskid, eventnum);
 		activeTask.start();
 		return true;
 	}
 
 	public boolean isInstanced(){
 		return details.loadworld;
 	}
 
 	public Task getActiveTask() {
 		return activeTask;
 	}
 
 	// passed in from QuestManager
 	public void onTaskCompletion(TaskCompleteEvent e) {
 		if (e.getQuestID() != questid)
 			return;
 		if (e.getResult()==CompleteStatus.CANCELED)
 			return;
 		else if (e.getResult()==CompleteStatus.FAILURE){
 			finishQuest(CompleteStatus.FAILURE);
 			return;
 		}
 		// TODO this is lovely and all, but tasks should trigger other tasks...
 		// I'll just call the next task, and if the next task isn't available,
 		// finish the quest
 
 		List<Integer> sortedkeys = getSortedKeys(details.tasks.keySet());
 		int loc = sortedkeys.indexOf(e.getID());
 		if (loc == sortedkeys.size() - 1) {
 			finishQuest(CompleteStatus.SUCCESS);
 			return;
 		}
 		loc++;
 		startTask(sortedkeys.get(loc));
 	}
 
 	public void finishQuest(CompleteStatus c) {
 		finished = c;
 		if (!activeTask.isComplete())
 			activeTask.cancelTask();
 		for (Edit e : details.editables.values())
 			e.dismantle();
 		TimeUtils.unlock(Bukkit.getWorld(details.world));
 		Group g = MineQuest.groupManager.getGroup(MineQuest.groupManager
 				.indexOfQuest(this));
 		QuestCompleteEvent event = new QuestCompleteEvent(questid, c, g);
 		Bukkit.getPluginManager().callEvent(event);
 	}
 
 	public void unloadQuest() throws IOException {
 		if (details.loadworld)
 			QuestWorldManip.removeWorld(Bukkit.getWorld(details.world));
 	}
 
 	public CompleteStatus isFinished() {
 		return finished;
 	}
 
 	public String getEvent(Integer id) {
 		if (!details.events.containsKey(id))
 			throw new IllegalArgumentException("No such event ID!");
 		return details.events.get(id);
 	}
 
 	/**
 	 * Get the "YOU CAN'T EDIT THIS PLACE" message...
 	 * 
 	 * @return cannot edit message
 	 */
 	public String getEditMessage() {
 		return details.editMessage;
 	}
 
 	/**
 	 * Retrieve the current task ID.
 	 * 
 	 * @return Current Task ID.
 	 */
 	public int getCurrentTaskID() {
 		return currenttask;
 	}
 
 	public String[] getTaskDetails(int id) {
 		return details.tasks.get(id);
 	}
 
 	public long getID() {
 		return questid;
 	}
 
 	public String getWorld() {
 		return details.world;
 	}
 
 	/**
 	 * Retrieve the target specification.
 	 * 
 	 * @param id
 	 *            target ID
 	 * @return specification, or <code>null</code> if there is no such target
 	 *         id.
 	 */
 	public TargetDetails getTarget(int id) {
 		return details.targets.get(id);
 	}
 
 	public Location getSpawnLocation() {
 		return new Location(Bukkit.getWorld(details.world), details.spawnPoint[0],
 				details.spawnPoint[1], details.spawnPoint[2]);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object arg0) {
 		if (!(arg0 instanceof Quest))
 			return false;
 		Quest q = (Quest) arg0;
 		return (q.questid == this.questid);
 	}
 
 	@Override
 	public String toString() {
 		String tr = details.toString() + "\n";
 		if (activeTask!=null){
 			tr += ChatUtils.formatHeader("Current Tasks") + "\n";
 			for (QEvent e : activeTask.getEvents()){
 				if (e instanceof NamedQEvent){
 					String description = ((NamedQEvent)e).getDescription();
 					if (e.isComplete()==null)
 						tr += ChatColor.GREEN + "- " + description + "\n";
 					else
 						tr += ChatColor.GRAY + "- " + description + "\n";
 				}
 			}
 		}
 		return tr;
 	}
 
 	public String getName() {
 		return details.displayname;
 	}
 
 	public String getDescription() {
 		return details.displaydesc;
 	}
 
 }
