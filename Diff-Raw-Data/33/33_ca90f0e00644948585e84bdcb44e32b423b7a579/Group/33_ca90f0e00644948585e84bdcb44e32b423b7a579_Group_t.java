 package com.theminequest.MineQuest.Group;
 
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import com.theminequest.MineQuest.Quest.Quest;
 
 public interface Group extends Comparable<Group> {
 	
 	/*
 	 * Basic Group Commands
 	 */
 	Player getLeader();
 	void setLeader(Player p) throws GroupException;
 	List<Player> getPlayers();
 	void add(Player p) throws GroupException;
 	void remove(Player p) throws GroupException;
 	boolean contains(Player p);
 	void invite(Player p) throws GroupException;
 
 	/*
 	 * Group Configuration Commands
 	 */
 	int getCapacity();
 	void setCapacity(int capacity) throws GroupException;
 	/*
 	 * Group identification
 	 */
 	long getID();
 	
 	/*
 	 * Group questing
 	 */
 	void startQuest(String q) throws GroupException;
 	void enterQuest() throws GroupException;
 	void exitQuest() throws GroupException;
 	void abandonQuest() throws GroupException;
 	Quest getQuest();
 	boolean isInQuest();
 	void teleportPlayers(Location l);
 	void recordCurrentLocations();
 	void moveBackToLocations() throws GroupException;
 	
 	/*
 	 * GroupManager usage
 	 */
	void lockGroup();
 
 }
