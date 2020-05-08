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
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.theminequest.bukkit.quest;
 
 import static com.theminequest.common.util.I18NMessage._;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
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
 
 import com.theminequest.api.CompleteStatus;
 import com.theminequest.api.Managers;
 import com.theminequest.api.group.Group;
 import com.theminequest.api.group.Group.QuestStatus;
 import com.theminequest.api.platform.entity.MQPlayer;
 import com.theminequest.api.quest.Quest;
 import com.theminequest.api.quest.QuestDetails;
 import com.theminequest.api.quest.QuestDetailsUtils;
 import com.theminequest.api.quest.QuestManager;
 import com.theminequest.api.quest.QuestUtils;
 import com.theminequest.api.quest.handler.QuestHandler;
 import com.theminequest.api.statistic.LogStatus;
 import com.theminequest.api.statistic.QuestStatisticUtils;
 import com.theminequest.api.statistic.QuestStatisticUtils.QSException;
 import com.theminequest.api.statistic.SnapshotStatistic;
 
 public class BukkitQuestManager implements Listener, QuestManager {
 	
 	protected final String locationofQuests;
 	private LinkedHashMap<Long, Quest> quests;
 	private Map<String, Map<String, Quest>> mwQuests;
 	private List<QuestDetails> descriptions;
 	
 	private long questid;
 	private Object idLock;
 	
 	public BukkitQuestManager() {
 		Managers.log("[Quest] Starting Manager...");
 		quests = new LinkedHashMap<Long, Quest>();
 		mwQuests = new HashMap<String, Map<String, Quest>>();
 		descriptions = new ArrayList<QuestDetails>();
 		questid = 0;
 		idLock = new Object();
 		
 		try {
 			locationofQuests = Managers.getPlatform().getConfigurationFile().getString("quest.folder", Managers.getPlatform().getResourceDirectory().getCanonicalPath() + File.separator + "quests");
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		File f = new File(locationofQuests);
 		if (!f.exists() || !f.isDirectory()) {
 			f.delete();
 			f.mkdirs();
 		}
 				
 	}
 	
 	@Override
 	public synchronized void reloadQuests() {
 		
 		Managers.log("[Quest] Reload Triggered. Starting reload...");
 		descriptions.clear();
 		loadFile(new File(locationofQuests));
 	}
 	
 	private void loadFile(File f) {
 		if (f.isFile())
 			loadQuest(f);
 		else {
 			for (File inside : f.listFiles())
 				loadFile(inside);
 		}
 	}
 	
 	@Override
 	public synchronized void reloadQuest(String name) {
 		Managers.logf("[Quest] Reloading %s...", name);
 		
 		QuestDetails old = getDetails(name);
 		if (old != null) {
 			File file = old.getProperty(QuestDetails.QUEST_FILE);
 			descriptions.remove(old);
 			loadQuest(file);
 			return;
 		}
 		
 		File newFile = new File(locationofQuests, name);
 		if (!newFile.exists())
 			throw new IllegalArgumentException("No such file " + name + " and not previously loaded!");
 		loadQuest(newFile);
 	}
 	
 	private synchronized void loadQuest(File f) {
 		try {
 			String filename = f.getName();
 			
 			if (filename.endsWith("~")) {
 				Managers.logf(Level.WARNING, "Ignoring %s.", filename);
 				return;
 			}
 			
 			QuestHandler<?> handle = Managers.getQuestHandlerManager().getQuestHandler(filename.substring(filename.lastIndexOf(".") + 1));
 			
 			if (handle == null) {
 				Managers.logf(Level.SEVERE, "Unable to find handler for %s (called from %s).", filename.substring(filename.lastIndexOf(".") + 1), filename);
 				return;
 			}
 			
 			QuestDetails d = handle.parseQuest(f);
 			descriptions.add(d);
 			Managers.logf("[Quest] Loaded %s from %s.", d.getName(), f.getCanonicalPath());
 		} catch (IOException e) {
 			e.printStackTrace();
 			Managers.logf(Level.SEVERE, "[Quest] Failed to load %s!", f.getName());
 		}
 	}
 	
 	@Override
 	public QuestDetails getDetails(String name) {
 		for (QuestDetails d : descriptions)
 			if (d.getName().equals(name))
 				return d;
 		return null;
 	}
 	
 	@Override
 	public Set<String> getListOfDetails() {
 		Set<String> s = new HashSet<String>();
 		for (QuestDetails d : descriptions)
 			s.add(d.getName());
 		return s;
 	}
 	
 	@Override
 	public Quest startQuest(QuestDetails d, String ownerName) {
 		if (d == null)
 			throw new IllegalArgumentException(new NullPointerException());
 		d = QuestDetailsUtils.getCopy(d);
 		Quest q;
 		if (d.getProperty(QuestDetails.QUEST_LOADWORLD)) {
 			synchronized (idLock) {
 				q = d.generateQuest(questid, ownerName);
 				quests.put(questid, q);
 				questid++;
 			}
 		} else {
 			q = d.generateQuest(-1, ownerName);
 			Map<String, Quest> qs = mwQuests.get(ownerName);
 			if (qs == null) {
 				qs = new LinkedHashMap<String, Quest>();
 				mwQuests.put(ownerName, qs);
 			}
 			qs.put(q.getDetails().getName(), q);
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
 	public Quest[] getMainWorldQuests(MQPlayer player) {
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
 	public void completeQuest(Quest q) {
 		if ((q.isFinished() != CompleteStatus.CANCELED) && (q.isFinished() != CompleteStatus.IGNORE)) {
 			String questname = q.getDetails().getName();
 			String questfinish = q.getDetails().getProperty(QuestDetails.QUEST_COMPLETE);
 			boolean failed = (q.isFinished() == CompleteStatus.FAIL || q.isFinished() == CompleteStatus.ERROR);
 			String color = ChatColor.GRAY + "[done] ";
 			switch (q.isFinished()) {
 			case FAIL:
 				color = ChatColor.RED + "[" + _("fail") + "]";
 				questfinish = q.getDetails().getProperty(QuestDetails.QUEST_FAIL);
 				break;
 			case ERROR:
 				color = ChatColor.BOLD + "" + ChatColor.RED + "[! " + _("error") + " !] ";
 				questfinish = "A serious error has occured and the quest has been stopped. Alert an administrator.";
 				break;
 			case SUCCESS:
 				color = ChatColor.GREEN + "[" + _("complete") + "] ";
 				break;
 			case WARNING:
 				color = ChatColor.YELLOW + "[" + _("warning") + "] ";
 				break;
 			default:
 				break;
 			}
 			for (MQPlayer p : Managers.getGroupManager().get(q).getMembers()) {
 				p.sendMessage(color + questfinish);
 				try {
 					if (!failed)
 						QuestStatisticUtils.completeQuest(p.getName(), questname);
 					else
 						QuestStatisticUtils.failQuest(p.getName(), questname);
 				} catch (QSException ignored) {
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onBlockPlaceEvent(BlockPlaceEvent e) {
 		Player p = e.getPlayer();
 		long indexof = Managers.getGroupManager().indexOf(Managers.getPlatform().toPlayer(p));
 		if (indexof == -1)
 			return;
 		Group g = Managers.getGroupManager().get(indexof);
 		if (g.getQuestStatus() == QuestStatus.INQUEST) {
 			Quest q = g.getQuest();
 			// by default, I don't allow this to happen.
 			e.setCancelled(true);
 			// FIXME edit goes here
 			e.getPlayer().sendMessage(ChatColor.YELLOW + "[!] " + q.getDetails().getProperty(QuestDetails.QUEST_EDITMESSAGE));
 		}
 	}
 	
 	// Non-Override
 	
 	@EventHandler
 	public void onBlockDamageEvent(BlockDamageEvent e) {
 		Player p = e.getPlayer();
 		long indexof = Managers.getGroupManager().indexOf(Managers.getPlatform().toPlayer(p));
 		if (indexof == -1)
 			return;
 		Group g = Managers.getGroupManager().get(indexof);
 		if (g.getQuestStatus() == QuestStatus.INQUEST) {
 			Quest q = g.getQuest();
 			// by default, I don't allow this to happen.
 			e.setCancelled(true);
 			
 			// FIXME edits
 			
 			e.getPlayer().sendMessage(ChatColor.YELLOW + "[!] " + q.getDetails().getProperty(QuestDetails.QUEST_EDITMESSAGE));
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerRespawnEvent(PlayerRespawnEvent e) {
 		Player p = e.getPlayer();
 		long indexof = Managers.getGroupManager().indexOf(Managers.getPlatform().toPlayer(p));
 		if (indexof == -1)
 			return;
 		Group g = Managers.getGroupManager().get(indexof);
 		if (g.getQuestStatus() == QuestStatus.INQUEST)
 			e.setRespawnLocation((Location) Managers.getPlatform().fromLocation(QuestUtils.getSpawnLocation(g.getQuest())));
 	}
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent e) {
 		List<SnapshotStatistic> snapshots = Managers.getStatisticManager().getAllStatistics(e.getPlayer().getName(), SnapshotStatistic.class);
 		LinkedHashMap<String, Quest> qs = new LinkedHashMap<String, Quest>();
 		for (SnapshotStatistic s : snapshots) {
 			Quest q = s.getSnapshot().recreateQuest();
 			String questName = q.getDetails().getName();
 			
 			int taskId = s.getSnapshot().getLastTaskID();
 			if (taskId != -1) {
 				q.startTask(taskId);
 			} else
 				q.startQuest();
 			
 			qs.put(questName, q);
 		}
 		mwQuests.put(e.getPlayer().getName(), qs);
 		
 		// consistency check
 		Map<String, Date> check = QuestStatisticUtils.getQuests(e.getPlayer().getName(), LogStatus.ACTIVE);
 		for (String s : check.keySet())
 			if (getMainWorldQuest(e.getPlayer().getName(), s) == null)
 				try {
 					QuestStatisticUtils.dropQuest(e.getPlayer().getName(), s);
 				} catch (QSException e1) {
 					e1.printStackTrace();
 				}
 	}
 	
 	@EventHandler(priority = EventPriority.LOW)
 	public void onPlayerQuit(PlayerQuitEvent e) {
 		Map<String, Quest> qs = mwQuests.remove(e.getPlayer().getName());
 		if (qs == null)
 			return;
 		for (Quest q : qs.values()) {
 			q.finishQuest(CompleteStatus.IGNORE);
 			q.cleanupQuest();
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.LOW)
 	public void onPlayerKick(PlayerKickEvent e) {
 		Map<String, Quest> qs = mwQuests.remove(e.getPlayer().getName());
 		if (qs == null)
 			return;
 		for (Quest q : qs.values()) {
 			q.finishQuest(CompleteStatus.IGNORE);
 			q.cleanupQuest();
 		}
 	}
 }
