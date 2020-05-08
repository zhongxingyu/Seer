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
 package com.theminequest.MineQuest.Group;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.API.CompleteStatus;
 import com.theminequest.MineQuest.API.ManagerException;
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.BukkitEvents.GroupInviteEvent;
 import com.theminequest.MineQuest.API.BukkitEvents.GroupPlayerJoinedEvent;
 import com.theminequest.MineQuest.API.BukkitEvents.GroupPlayerQuitEvent;
 import com.theminequest.MineQuest.API.BukkitEvents.QuestCompleteEvent;
 import com.theminequest.MineQuest.API.Group.Group;
 import com.theminequest.MineQuest.API.Group.QuestGroup;
 import com.theminequest.MineQuest.API.Group.GroupException;
 import com.theminequest.MineQuest.API.Group.GroupException.GroupReason;
 import com.theminequest.MineQuest.API.Group.QuestGroupManager;
 import com.theminequest.MineQuest.API.Quest.Quest;
 import com.theminequest.MineQuest.API.ManagerException.ManagerReason;
 
 public class MQQuestGroupManager implements Listener, QuestGroupManager {
 
 	public final int TEAM_MAX_CAPACITY;
 	public final int SUPER_MAX_CAPACITY;
 	private Map<Long, QuestGroup> groups;
 	private Map<Player, QuestGroup> invitations;
 	private long groupid;
 
 	public MQQuestGroupManager(){
 		Managers.log("[Party] Starting Manager...");
 		groups = Collections.synchronizedMap(new LinkedHashMap<Long,QuestGroup>());
 		invitations = Collections.synchronizedMap(new LinkedHashMap<Player,QuestGroup>());
 		groupid = 0;
 		TEAM_MAX_CAPACITY = MineQuest.configuration.groupConfig.getInt("team_max_capacity", 8);
 		SUPER_MAX_CAPACITY = MineQuest.configuration.groupConfig.getInt("super_max_capacity", 3);
 	}
 
 	public synchronized QuestGroup createNewGroup(List<Player> p){
 		long id = groupid;
 		groupid++;
 		Party party = new Party(groupid,p);
 		groups.put(id, party);
 		return party;
 	}
 
 	public synchronized QuestGroup createNewGroup(Player p){
 		List<Player> group = new ArrayList<Player>();
 		group.add(p);
 		return createNewGroup(group);
 	}
 	
 	public synchronized QuestGroup get(long id){
 		return groups.get(id);
 	}
 	
 
 	@Override
 	public synchronized QuestGroup get(Quest activeQuest) {
 		if (!activeQuest.isInstanced()){
 			// create faux questgroup with fake methods
 			// and return that for events and such to use
 			// get player from getQuestOwner()
 			return new SingleParty(Bukkit.getPlayer(activeQuest.getQuestOwner()),activeQuest);
 		}
		return get(indexOf(activeQuest));
 	}
 
 	/**
 	 * Determine if a player is on a team.
 	 * @param p Player to check for.
 	 * @return Party ID, or -1 if not on team.
 	 */
 	public synchronized long indexOf(Player p){
 		for (long id : groups.keySet()){
 			QuestGroup t = groups.get(id);
 			if (t!=null && t.contains(p))
 				return id;
 		}
 		return -1;
 	}
 	
 	/**
 	 * Determine if a quest is being played by a team
 	 * @param q Quest
 	 * @return Party ID, or -1 if not on a team.
 	 */
 	public synchronized long indexOf(Quest q){
 		for (long id : groups.keySet()){
 			QuestGroup t = groups.get(id);
 			if (t!=null && t.getQuest()!=null && t.getQuest().equals(q))
 				return id;
 		}
 		return -1;
 	}
 	
 	public synchronized void acceptInvite(Player p) throws ManagerException{
 		if (!invitations.containsKey(p))
 			throw new ManagerException(ManagerReason.INVALIDARGS);
 		try {
 			invitations.get(p).add(p);
 		} catch (GroupException e) {
 			throw new ManagerException(e);
 		}
 		invitations.remove(p);
 	}
 	
 	public synchronized boolean hasInvite(Player p) {
 		return (invitations.containsKey(p));
 	}
 	
 	public synchronized void invite(final Player p, Group g) throws ManagerException {
 		if (invitations.containsKey(p))
 			throw new ManagerException(ManagerReason.INVALIDARGS);
 		if (!(g instanceof QuestGroup))
 			throw new ManagerException(ManagerReason.INTERNAL);
 		invitations.put(p, (QuestGroup) g);
 		// TODO Call GroupInviteEvent (remember, 30 seconds to accept invite)
 		GroupInviteEvent event = new GroupInviteEvent(g.getLeader().getName(), p, g.getID());
 		Bukkit.getPluginManager().callEvent(event);
 		Bukkit.getScheduler().scheduleAsyncDelayedTask(Managers.getActivePlugin(), new Runnable(){
 
 			@Override
 			public void run() {
 				denyInvite(p);
 			}
 			
 		}, 600);
 	}
 	
 	public synchronized void denyInvite(Player p) {
 		if (!invitations.containsKey(p)) // accepted; just return.
 			return;
 		invitations.remove(p);
 		// TODO Call TeamInviteExpiredEvent
 		p.sendMessage("Invite deleted!"); // FIXME
 	}
 
 	/*
 	 * Only called by Party objects when everyone leaves the team.
 	 */
 	protected synchronized void removeEmptyQuestGroup(long id){
 		if (groups.get(id)==null)
 			return;
 		if (groups.get(id).getMembers().size()>0)
 			throw new IllegalArgumentException("Party is still full!");
 		try {
 			groups.get(id).setCapacity(0);
 		} catch (GroupException e) {
 			e.printStackTrace();
 		}
 		groups.remove(id);
 	}
 	
 	// FIXME extract this out into GroupManager (Not QuestGroupManager)
 	@EventHandler
 	public synchronized void onGroupPlayerJoinedEvent(GroupPlayerJoinedEvent e){
 		for (Player p : e.getGroup().getMembers())
 			p.sendMessage(ChatColor.GOLD + e.getPlayer().getDisplayName() + " has joined the party.");
 	}
 	
 	@EventHandler
 	public synchronized void onGroupPlayerQuitEvent(GroupPlayerQuitEvent e){
 		for (Player p : e.getGroup().getMembers())
 			p.sendMessage(ChatColor.GOLD + e.getPlayer().getDisplayName() + " has left the party.");
 	}
 	// end FIXME
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public synchronized void onPlayerQuit(PlayerQuitEvent e){
 		processEvent(e);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public synchronized void onPlayerKick(PlayerKickEvent e){
 		processEvent(e);
 	}
 	
 	private synchronized void processEvent(PlayerEvent e){
 		//PlayerDetails p = MineQuest.playerManager.getPlayerDetails(e.getPlayer());
 		//if (p.getTeam()!=-1){
 		//	MineQuest.groupManager.getTeam(p.getTeam()).remove(e.getPlayer());
 		//}
 		long team = indexOf(e.getPlayer());
 		if (team!=-1){
 			try {
 				groups.get(team).remove(e.getPlayer());
 			} catch (GroupException e1) {
 				Managers.log(Level.SEVERE, "Failed to remove player from team: " + e1);
 				Managers.log(Level.WARNING, "Locking group and kicking all players...");
 				for (Player p : groups.get(team).getMembers()){
 					p.sendMessage("[ERROR] Something went wrong and we have to disband your group. :(");
 					try {
 						groups.get(team).remove(p);
 					} catch (GroupException e2) {
 						// ignore
 					}
 					//removeEmptyTeam(team);
 				}
 				
 			} catch (NullPointerException e1) {
 				// ...
 			}
 		}
 	}
 
 	@EventHandler
 	public void onGroupInviteEvent(GroupInviteEvent e){
 		e.getInvited().sendMessage("[INVITE 30 SECS] You've been invited to a group by " + e.getInviterName() + ".");
 		e.getInvited().sendMessage("Accept within 30 seconds with /party accept.");
 	}
 	
 	@EventHandler
 	public synchronized void onQuestCompleteEvent(QuestCompleteEvent e){
 		Quest q = e.getQuest();
 		if (!q.isInstanced() && e.getQuest().isFinished()!=CompleteStatus.CANCELED){
 			try {
 				e.getGroup().finishQuest();
 			} catch (GroupException e1) {
 				e1.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public void disposeGroup(Group group) {
 		for (Player p : group.getMembers()){
 			p.sendMessage(ChatColor.GRAY + "Group is being disposed of!");
 			try {
 				group.remove(p);
 			} catch (GroupException e) {
 				e.printStackTrace();
 			}
 		}
 		Iterator<QuestGroup> i1 = groups.values().iterator();
 		while (i1.hasNext()){
 			Group g = i1.next();
 			if (g.equals(group)){
 				i1.remove();
 				break;
 			}
 		}
 		Iterator<QuestGroup> i2 = invitations.values().iterator();
 		while (i2.hasNext()){
 			Group g = i2.next();
 			if (g.equals(group)){
 				i2.remove();
 				break;
 			}
 		}
 		return;
 	}
 
 	@Override
 	public QuestGroup get(Player p) {
 		return get(indexOf(p));
 	}
 }
