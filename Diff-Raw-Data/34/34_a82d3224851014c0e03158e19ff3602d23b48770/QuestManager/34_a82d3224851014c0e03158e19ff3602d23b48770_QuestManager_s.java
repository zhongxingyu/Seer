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
 package com.theminequest.MineQuest.Quest;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 import com.theminequest.MineQuest.I18NMessage;
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.API.CompleteStatus;
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.BukkitEvents.QuestCompleteEvent;
 import com.theminequest.MineQuest.API.BukkitEvents.TaskCompleteEvent;
 import com.theminequest.MineQuest.API.Edit.Edit;
 import com.theminequest.MineQuest.API.Group.QuestGroup;
 import com.theminequest.MineQuest.API.Group.QuestGroup.QuestStatus;
 import com.theminequest.MineQuest.API.Quest.Quest;
 import com.theminequest.MineQuest.API.Quest.QuestDetailsUtils;
 import com.theminequest.MineQuest.API.Quest.QuestParser;
 import com.theminequest.MineQuest.API.Quest.QuestUtils;
 import com.theminequest.MineQuest.API.Tracker.LogStatus;
 import com.theminequest.MineQuest.API.Tracker.QuestStatisticUtils;
 import com.theminequest.MineQuest.API.Tracker.QuestStatisticUtils.QSException;
 import com.theminequest.MineQuest.API.Tracker.SnapshotStatistic;
 import com.theminequest.MineQuest.Quest.Parser.AcceptTextHandler;
 import com.theminequest.MineQuest.Quest.Parser.CancelTextHandler;
 import com.theminequest.MineQuest.Quest.Parser.DescriptionHandler;
 import com.theminequest.MineQuest.Quest.Parser.EditHandler;
 import com.theminequest.MineQuest.Quest.Parser.EditMessageHandler;
 import com.theminequest.MineQuest.Quest.Parser.CompleteTextHandler;
 import com.theminequest.MineQuest.Quest.Parser.FailedTextHandler;
 import com.theminequest.MineQuest.Quest.Parser.InstanceHandler;
 import com.theminequest.MineQuest.Quest.Parser.LoadWorldHandler;
 import com.theminequest.MineQuest.Quest.Parser.NameHandler;
 import com.theminequest.MineQuest.Quest.Parser.RepeatableHandler;
 import com.theminequest.MineQuest.Quest.Parser.RequirementHandler;
 import com.theminequest.MineQuest.Quest.Parser.ResetHandler;
 import com.theminequest.MineQuest.Quest.Parser.SpawnHandler;
 import com.theminequest.MineQuest.Quest.Parser.TargetHandler;
 import com.theminequest.MineQuest.Quest.Parser.TaskHandler;
 import com.theminequest.MineQuest.Quest.Parser.WorldHandler;
 
 import static com.theminequest.MineQuest.API.Quest.QuestDetails.*;
 
 
 public class QuestManager implements Listener, com.theminequest.MineQuest.API.Quest.QuestManager {
 
 	protected final String locationofQuests;
 	private LinkedHashMap<Long,Quest> quests;
 	private Map<String, Map<String, Quest>> mwQuests;
 	private List<QuestDetails> descriptions;
 	private long questid;
 	private final QuestParser parser;
 
 	public QuestManager(){
 		Managers.log("[Quest] Starting Manager...");
 		quests = new LinkedHashMap<Long,Quest>();
 		mwQuests = new HashMap<String, Map<String, Quest>>();
 		descriptions = new ArrayList<QuestDetails>();
 		questid = 0;
 		parser = new QuestParser();
 		locationofQuests = MineQuest.configuration.questConfig
 				.getString("questfolderlocation",
 						Managers.getActivePlugin().getDataFolder().getAbsolutePath()
 						+File.separator+"quests");
 		File f = new File(locationofQuests);
 		if (!f.exists() || !f.isDirectory()){
 			f.delete();
 			f.mkdirs();
 		}
 		parser.addClassHandler("accepttext", AcceptTextHandler.class);
 		parser.addClassHandler("canceltext", CancelTextHandler.class);
 		parser.addClassHandler("description", DescriptionHandler.class);
 		parser.addClassHandler("edit", EditHandler.class);
 		parser.addClassHandler("editmessage", EditMessageHandler.class);
 		parser.addClassHandler("event", com.theminequest.MineQuest.Quest.Parser.EventHandler.class);
 		parser.addClassHandler("failedtext", FailedTextHandler.class);
 		parser.addClassHandler("finishtext", CompleteTextHandler.class);
 		parser.addClassHandler("instance", InstanceHandler.class);
 		parser.addClassHandler("loadworld", LoadWorldHandler.class);
 		parser.addClassHandler("name", NameHandler.class);
 		parser.addClassHandler("repeatable", RepeatableHandler.class);
 		parser.addClassHandler("requirement",RequirementHandler.class);
 		parser.addClassHandler("reset", ResetHandler.class);
 		parser.addClassHandler("spawn", SpawnHandler.class);
 		parser.addClassHandler("target", TargetHandler.class);
 		parser.addClassHandler("task", TaskHandler.class);
 		parser.addClassHandler("world", WorldHandler.class);
 	}
 
 	@Override
 	public synchronized void reloadQuests(){
 		Managers.log("[Quest] Reload Triggered. Starting reload...");
 		descriptions.clear();
 		File file = new File(locationofQuests);
 		for (File f : file.listFiles(new FilenameFilter(){
 
 			@Override
 			public boolean accept(File dir, String name) {
 				return name.endsWith(".quest");
 			}
 
 		})){
 			loadQuest(f);
 		}
 	}
 
 	@Override
 	public synchronized void reloadQuest(String name){
 		Managers.log("[Quest] Reload Triggered for Quest " + name + ". Attempting reload...");
 		String newname = name + ".quest";
 		File f = new File(locationofQuests);
 		File[] sorted = f.listFiles(new FilenameFilter(){
 
 			@Override
 			public boolean accept(File dir, String name) {
 				return name.endsWith(".quest");
 			}
 
 		});
 		Arrays.sort(sorted);
 		File lookfor = new File(locationofQuests + File.separator + newname);
 		int loc = Arrays.binarySearch(sorted, lookfor);
 		if (loc>=sorted.length || loc<0)
 			throw new IllegalArgumentException("Can't find this quest!");
 		if (getDetails(name)!=null)
 			descriptions.remove(getDetails(name));
 		loadQuest(sorted[loc]);
 	}
 
 	private synchronized void loadQuest(File f){
 		try {
 			QuestDetails d = new QuestDetails(f);
 			descriptions.add(d);
 			Managers.log("[Quest] Loaded " + d.getProperty(com.theminequest.MineQuest.API.Quest.QuestDetails.QUEST_NAME)+".");
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			Managers.log(Level.SEVERE, "[Quest] Failed to load "+f.getName()+"!");
 		}
 	}
 
 	@Override
 	public com.theminequest.MineQuest.API.Quest.QuestDetails getDetails(
 			String name) {
 		for (QuestDetails d : descriptions)
 			if (d.getProperty(com.theminequest.MineQuest.API.Quest.QuestDetails.QUEST_NAME).equals(name))
 				return d;
 		return null;
 	}
 
 	@Override
 	public List<String> getListOfDetails() {
 		List<String> s = new LinkedList<String>();
 		for (QuestDetails d : descriptions){
 			s.add((String) d.getProperty(com.theminequest.MineQuest.API.Quest.QuestDetails.QUEST_NAME));
 		}
 		return s;
 	}
 
 	@Override
 	public Quest startQuest(com.theminequest.MineQuest.API.Quest.QuestDetails d, String ownerName){
 		if (d==null)
 			throw new IllegalArgumentException(new NullPointerException());
 		d = QuestDetailsUtils.getCopy(d);
 		Quest q;
 		if (d.getProperty(QUEST_LOADWORLD)){
 			q = com.theminequest.MineQuest.Quest.Quest.newInstance(questid,d,ownerName);
 			quests.put(questid,q);
 			questid++;
 		} else {
 			q = com.theminequest.MineQuest.Quest.Quest.newInstance(-1,d,ownerName);
 			Map<String, Quest> qs = mwQuests.get(ownerName);
 			if (qs == null) {
 				qs = new LinkedHashMap<String, Quest>();
 				mwQuests.put(ownerName, qs);
 			}
 			qs.put((String) q.getDetails().getProperty(QUEST_NAME), q);
 		}
 		return q;
 	}
 
 	@Override
 	public Quest getQuest(long currentquest) {
 		if (quests.containsKey(currentquest))
 			return quests.get(currentquest);
 		return null;
 	}
 	
 	@Override
 	public Quest[] getMainWorldQuests(Player player){
 		Map<String, Quest> qs = mwQuests.get(player.getName());
 		if (qs == null)
 			return new Quest[0];
 		return qs.values().toArray(new Quest[qs.size()]);
 	}
 	
 	@Override
 	public Quest getMainWorldQuest(String player, String questName) {
 		Map<String, Quest> qs = mwQuests.get(player);
 		if (qs == null)
 			return null;
 		return qs.get(questName);
 		
 	}
 	
 	@Override
 	public void removeMainWorldQuest(String player, String questName) {
 		Map<String, Quest> qs = mwQuests.get(player);
 		if (qs == null)
 			return;
 		qs.remove(questName);
 	}
 
 	@Override
 	@EventHandler
 	public void taskCompletion(TaskCompleteEvent e){
 		e.getQuest().onTaskCompletion(e);
 	}
 
 	@Override
 	@EventHandler
 	public void onQuestCompletion(QuestCompleteEvent e){
 		if (e.getResult()!=CompleteStatus.CANCELED){
 			com.theminequest.MineQuest.API.Quest.Quest q = e.getQuest();
 			String questname = q.getDetails().getProperty(QUEST_NAME);
 			String questfinish = q.getDetails().getProperty(QUEST_COMPLETE);
 			if (questfinish == null)
 				questfinish = I18NMessage.Quest_COMPLETE.getValue();
 			boolean failed = (e.getResult()==CompleteStatus.FAILURE);
 			String color = ChatColor.GRAY + "[done] ";
 			switch (e.getResult()) {
 			case FAILURE:
 				color = ChatColor.RED + "[fail] ";
 				questfinish = q.getDetails().getProperty(QUEST_FAIL);
 				if (questfinish == null)
 					questfinish = I18NMessage.Quest_FAIL.getValue();
 				break;
 			case SUCCESS:
 				color = ChatColor.GREEN + "[complete] ";
 				break;
 			case WARNING:
 				color = ChatColor.YELLOW + "[warning] ";
 				break;
 			default:
 				break;
 			}
 			for (Player p : e.getGroup().getMembers()){
 				p.sendMessage(color + questfinish);
 				try {
 					if (!failed)
 						QuestStatisticUtils.completeQuest(p.getName(), questname);
 					else
 						QuestStatisticUtils.failQuest(p.getName(), questname);
 				} catch (QSException ignored) {}
 			}
 		}
 		//quests.put(e.getQuestId(), null);
 	}
 
 	@Override
 	@EventHandler
 	public void onBlockPlaceEvent(BlockPlaceEvent e){
 		Player p = e.getPlayer();
 		long indexof = Managers.getQuestGroupManager().indexOf(p);
 		if (indexof==-1)
 			return;
 		QuestGroup g = Managers.getQuestGroupManager().get(indexof);
 		if (g.getQuestStatus()==QuestStatus.INQUEST){
 			com.theminequest.MineQuest.API.Quest.Quest q = g.getQuest();
 			// by default, I don't allow this to happen.
 			e.setCancelled(true);
 			Map<Integer,Edit> edits = q.getDetails().getProperty(QUEST_EDITS);
 			for (Edit edit : edits.values()){
 				edit.onBlockPlace(e);
 				if (!e.isCancelled())
 					return;
 			}
 			e.getPlayer().sendMessage(ChatColor.YELLOW+"[!] " + q.getDetails().getProperty(QUEST_EDITMESSAGE));
 		}
 	}
 
 	@Override
 	@EventHandler
 	public void onBlockDamageEvent(BlockDamageEvent e){
 		Player p = e.getPlayer();
 		long indexof = Managers.getQuestGroupManager().indexOf(p);
 		if (indexof==-1)
 			return;
 		QuestGroup g = Managers.getQuestGroupManager().get(indexof);
 		if (g.getQuestStatus()==QuestStatus.INQUEST){
 			com.theminequest.MineQuest.API.Quest.Quest q = g.getQuest();
 			// by default, I don't allow this to happen.
 			e.setCancelled(true);
 			Map<Integer,Edit> edits = q.getDetails().getProperty(QUEST_EDITS);
 			for (Edit edit : edits.values()){
 				edit.onBlockDamage(e);
 				if (!e.isCancelled())
 					return;
 			}
 			e.getPlayer().sendMessage(ChatColor.YELLOW+"[!] " + q.getDetails().getProperty(QUEST_EDITMESSAGE));
 		}
 	}
 
 	@Override
 	@EventHandler
 	public void onPlayerRespawnEvent(PlayerRespawnEvent e){
 		Player p = e.getPlayer();
 		long indexof = Managers.getQuestGroupManager().indexOf(p);
 		if (indexof==-1)
 			return;
 		QuestGroup g = Managers.getQuestGroupManager().get(indexof);
 		if (g.getQuestStatus()==QuestStatus.INQUEST)
 			e.setRespawnLocation(QuestUtils.getSpawnLocation(g.getQuest()));
 	}
 
 	@Override
 	public QuestParser getParser() {
 		return parser;
 	}
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent e){
 		List<SnapshotStatistic> snapshots = Managers.getStatisticManager().getAllStatistics(e.getPlayer().getName(), SnapshotStatistic.class);
 		LinkedHashMap<String, Quest> qs = new LinkedHashMap<String, Quest>();
 		for (SnapshotStatistic s : snapshots) {
 			Quest q = s.getSnapshot().recreateQuest();
 			String questName = q.getDetails().getProperty(QUEST_NAME);
 			int taskId = s.getSnapshot().getLastTaskID();
 			if (!q.startTask(taskId))
 				Managers.log(Level.SEVERE, "Starting task "+taskId+" for "+questName+" failed during login!");
 			
 			qs.put(questName, q);
 		}
 		mwQuests.put(e.getPlayer().getName(), qs);
 		
 		// consistency check
 		Map<String,Date> check = QuestStatisticUtils.getQuests(e.getPlayer().getName(), LogStatus.ACTIVE);
 		for (String s : check.keySet()){
 			if (getMainWorldQuest(e.getPlayer().getName(),s)==null){
 				try {
 					QuestStatisticUtils.dropQuest(e.getPlayer().getName(), s);
 				} catch (QSException e1) { e1.printStackTrace(); }
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.LOW)
 	public void onPlayerQuit(PlayerQuitEvent e){
 		Map<String, Quest> qs = mwQuests.remove(e.getPlayer().getName());
 		if (qs == null)
 			return;
 		for (Quest q : qs.values()){
 			q.finishQuest(CompleteStatus.CANCELED);
 			q.cleanupQuest();
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.LOW)
 	public void onPlayerKick(PlayerKickEvent e){
 		Map<String, Quest> qs = mwQuests.remove(e.getPlayer().getName());
 		if (qs == null)
 			return;
 		for (Quest q : qs.values()){
 			q.finishQuest(CompleteStatus.CANCELED);
 			q.cleanupQuest();
 		}
 	}
 }
