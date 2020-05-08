 package com.theminequest.MineQuest.Group;
 
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.Group.GroupException.GroupReason;
 import com.theminequest.MineQuest.Quest.Quest;
 
 public class SuperTeam implements Group {
 	
 	private long teamid;
 	private List<Team> teams;
 	private int capacity;
 	private Quest quest;
 	private boolean inQuest;
 
 	public SuperTeam(long id, List<Team> t){
 		if (t.size()<=0 || t.size()>MineQuest.groupManager.SUPER_MAX_CAPACITY)
 			throw new IllegalArgumentException(GroupReason.BADCAPACITY.name());
 		teamid = id;
 		teams = t;
 		capacity = MineQuest.groupManager.SUPER_MAX_CAPACITY;
 		quest = null;
 		inQuest = false;
 	}
 
 	@Override
 	public int compareTo(Group arg0) {
 		return (int) (teamid-arg0.getID());
 	}
 
 	@Override
 	public Player getLeader() {
 		return teams.get(0).getLeader();
 	}
 
 	@Override
 	public void setLeader(Player p) throws GroupException {
 		for (Team t : teams) {
 			if (t.contains(p)){
 				if (teams.get(0).compareTo(t)==0){
 					
 				}else {
 					
 				}
 			}
 		}
 	}
 
 	@Override
 	public List<Player> getPlayers() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void add(Player p) throws GroupException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void remove(Player p) throws GroupException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean contains(Player p) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void invite(Player p) throws GroupException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public int getCapacity() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void setCapacity(int capacity) throws GroupException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public long getID() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void startQuest(String q) throws GroupException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void enterQuest() throws GroupException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void exitQuest() throws GroupException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void abandonQuest() throws GroupException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public Quest getQuest() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean isInQuest() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void teleportPlayers(Location l) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void recordCurrentLocations() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void moveBackToLocations() throws GroupException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
	public void lockGroup() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
