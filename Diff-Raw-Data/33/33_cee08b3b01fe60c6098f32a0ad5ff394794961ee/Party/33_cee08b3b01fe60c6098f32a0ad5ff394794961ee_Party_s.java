 package com.theminequest.bukkit.group;
 
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

 import com.theminequest.api.CompleteStatus;
 import com.theminequest.api.Managers;
 import com.theminequest.api.group.Group;
 import com.theminequest.api.group.GroupException;
 import com.theminequest.api.group.GroupException.GroupReason;
 import com.theminequest.api.platform.MQLocation;
 import com.theminequest.api.platform.MQPlayer;
 import com.theminequest.api.platform.event.GroupPlayerJoinedEvent;
 import com.theminequest.api.platform.event.GroupPlayerQuitEvent;
 import com.theminequest.api.quest.Quest;
 import com.theminequest.api.quest.QuestDetails;
 import com.theminequest.api.quest.QuestDetailsUtils;
 import com.theminequest.api.quest.QuestUtils;
 
 public class Party implements Group {
 		
 	private long teamid;
 	private List<MQPlayer> players;
 	private LinkedHashMap<MQPlayer, MQLocation> locations;
 	private int capacity;
 	private Quest quest;
 	private QuestStatus status;
 	private boolean pvp;
 	
 	protected Party(long id, List<MQPlayer> p, int capacity) {
 		if (p.size() <= 0)
 			throw new IllegalArgumentException(GroupReason.BADCAPACITY.name());
 		// ^ never should encounter this unless a third-party tries to, in which
 		// case they get what they deserve.
 		teamid = id;
 		players = Collections.synchronizedList(p);
 		locations = null;
 		quest = null;
 		status = QuestStatus.NOQUEST;
 		this.capacity = capacity;
 	}
 	
 	@Override
 	public synchronized MQPlayer getLeader() {
 		return players.get(0);
 	}
 	
 	@Override
 	public synchronized void setLeader(MQPlayer p) throws GroupException {
 		if (!contains(p))
 			throw new GroupException(GroupReason.NOTONTEAM);
 		players.remove(p);
 		players.add(0, p);
 	}
 	
 	@Override
 	public synchronized List<MQPlayer> getMembers() {
 		return players;
 	}
 	
 	@Override
 	public synchronized void setCapacity(int c) throws GroupException {
 		if (c < players.size())
 			throw new GroupException(GroupReason.BADCAPACITY);
 		capacity = c;
 	}
 	
 	@Override
 	public synchronized int getCapacity() {
 		return capacity;
 	}
 	
 	@Override
 	public long getID() {
 		return teamid;
 	}
 	
 	@Override
 	public synchronized boolean contains(MQPlayer p) {
 		return players.contains(p);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.theminequest.MineQuest.Group.Group#startQuest(com.theminequest.MineQuest
 	 * .Quest.Quest)
 	 * let the backend handle checking valid quests, calling QuestManager,
 	 * etc...
 	 */
 	@Override
 	public synchronized void startQuest(QuestDetails d) throws GroupException {
 		if (quest != null)
 			throw new GroupException(GroupReason.ALREADYONQUEST);
 		// check requirements
 		if (!QuestDetailsUtils.startRequirementsMet(d, getLeader()))
 			throw new GroupException(GroupReason.REQUIREMENTSNOTFULFILLED);
 		quest = Managers.getQuestManager().startQuest(d, getLeader().getName());
 		
 		status = QuestStatus.NOTINQUEST;
 		boolean loadworld = quest.getDetails().getProperty(QuestDetails.QUEST_LOADWORLD);
 		if (!loadworld) {
 			quest.startQuest();
 			status = QuestStatus.MAINWORLDQUEST;
 		}
 	}
 	
 	@Override
 	public synchronized void abandonQuest() throws GroupException {
 		if (quest == null)
 			throw new GroupException(GroupReason.NOQUEST);
 		quest.finishQuest(CompleteStatus.CANCELED);
 		if (status == QuestStatus.INQUEST)
 			exitQuest();
 		Quest q = quest;
 		status = QuestStatus.NOQUEST;
 		quest = null;
 		if (q != null)
 			q.cleanupQuest();
 		
 	}
 	
 	/**
 	 * Get the quest the team is undertaking.
 	 * 
 	 * @return Quest the team is undertaking, or <code>null</code> if the team
 	 *         is not on a quest.
 	 */
 	@Override
 	public synchronized Quest getQuest() {
 		return quest;
 	}
 	
 	@Override
 	public synchronized void teleportPlayers(MQLocation l) {
 		for (MQPlayer p : players)
 			p.teleport(l);
 	}
 	
 	@Override
 	public synchronized void add(MQPlayer p) throws GroupException {
 		if (Managers.getGroupManager().indexOf(p) != -1)
 			throw new GroupException(GroupReason.ALREADYINTEAM);
 		if (players.size() >= capacity)
 			throw new GroupException(GroupReason.BADCAPACITY);
 		if (contains(p))
 			throw new GroupException(GroupReason.ALREADYINTEAM);
 		if (status == QuestStatus.INQUEST)
 			throw new GroupException(GroupReason.INSIDEQUEST);
 		// MineQuest.playerManager.getPlayerDetails(p).setTeam(teamid);
 		
 		GroupPlayerJoinedEvent e = new GroupPlayerJoinedEvent(this, p);
 		Managers.getPlatform().callEvent(e);
 		
 		if (e.isCancelled())
 			throw new GroupException(GroupReason.CANCELLED);
 		
 		players.add(p);
 		
 		Managers.getGroupManager().groupPlayerJoin(this, p);
 	}
 	
 	@Override
 	public synchronized void remove(MQPlayer p) throws GroupException {
 		if (!contains(p))
 			throw new GroupException(GroupReason.NOTONTEAM);
 		GroupPlayerQuitEvent e = new GroupPlayerQuitEvent(this, p);
 		Managers.getPlatform().callEvent(e);
 
 		players.remove(p);
		if (locations != null) {
 			moveBackToLocations(p);
			locations.remove(p);
		}
 		
 		Managers.getGroupManager().groupPlayerQuit(this, p);
 		
 		if (players.size() <= 0) {
 			if (quest != null)
 				abandonQuest();
 			Managers.getGroupManager().disposeGroup(this);
 		}
 	}
 	
 	@Override
 	public synchronized void enterQuest() throws GroupException {
 		if (quest == null)
 			throw new GroupException(GroupReason.NOQUEST);
 		if (status == QuestStatus.INQUEST)
 			throw new GroupException(GroupReason.INSIDEQUEST);
 		if (!quest.isInstanced())
 			throw new GroupException(GroupReason.MAINWORLDQUEST);
 		recordCurrentLocations();
 		status = QuestStatus.INQUEST;
 		teleportPlayers(QuestUtils.getSpawnLocation(quest));
 		quest.startQuest();
 	}
 	
 	public synchronized void recordCurrentLocations() {
 		locations = new LinkedHashMap<MQPlayer, MQLocation>();
 		for (MQPlayer p : players)
 			locations.put(p, p.getLocation());
 	}
 	
 	public synchronized void moveBackToLocations() throws GroupException {
 		for (MQPlayer p : players)
 			moveBackToLocations(p);
 		locations = null;
 	}
 	
 	public synchronized void moveBackToLocations(MQPlayer p) throws GroupException {
 		if (locations == null)
 			throw new GroupException(GroupReason.NOLOCATIONS);
 		p.teleport(locations.get(p));
 		locations.remove(p);
 	}
 	
 	@Override
 	public synchronized void exitQuest() throws GroupException {
 		if (quest == null)
 			throw new GroupException(GroupReason.NOQUEST);
 		if (status != QuestStatus.INQUEST)
 			throw new GroupException(GroupReason.NOTINSIDEQUEST);
 		if (!quest.isInstanced())
 			throw new GroupException(GroupReason.MAINWORLDQUEST);
 		if (quest.isFinished() == null)
 			throw new GroupException(GroupReason.UNFINISHEDQUEST);
 		moveBackToLocations();
 		Quest q = quest;
 		status = QuestStatus.NOQUEST;
 		quest = null;
 		q.cleanupQuest();
 	}
 	
 	@Override
 	public synchronized void finishQuest() throws GroupException {
 		if (quest == null)
 			throw new GroupException(GroupReason.NOQUEST);
 		if (quest.isInstanced())
 			throw new GroupException(GroupReason.NOTMAINWORLDQUEST);
 		if (quest.isFinished() == null)
 			throw new GroupException(GroupReason.UNFINISHEDQUEST);
 		Quest q = quest;
 		quest = null;
 		status = QuestStatus.NOQUEST;
 		q.cleanupQuest();
 	}
 	
 	@Override
 	public int compareTo(Group arg0) {
 		return (int) (teamid - arg0.getID());
 	}
 	
 	@Override
 	public boolean isPVP() {
 		return pvp;
 	}
 	
 	@Override
 	public void setPVP(boolean on) {
 		pvp = on;
 	}
 	
 	@Override
 	public QuestStatus getQuestStatus() {
 		return status;
 	}
 	
 }
