 package com.censoredsoftware.Demigods.Engine.Tracked;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import redis.clients.johm.*;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.DemigodsData;
 import com.censoredsoftware.Demigods.Engine.Event.Battle.BattleCombineEvent;
 import com.censoredsoftware.Demigods.Engine.Event.Battle.BattleEndEvent;
 import com.censoredsoftware.Demigods.Engine.Event.Battle.BattleParticipateEvent;
 import com.censoredsoftware.Demigods.Engine.Event.Battle.BattleStartEvent;
 import com.censoredsoftware.Demigods.Engine.PlayerCharacter.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Utility.MiscUtility;
 import com.google.common.collect.Sets;
 
 @Model
 public class TrackedBattle
 {
 	@Id
 	private Long Id;
 	@Reference
 	@Indexed
 	private PlayerCharacter whoStarted;
 	@Reference
 	@Indexed
 	private TrackedLocation startLocation;
 	@CollectionSet(of = PlayerCharacter.class)
 	@Indexed
 	private Set<PlayerCharacter> involvedCharacters;
 	@CollectionSet(of = TrackedLocation.class)
 	@Indexed
 	private Set<TrackedLocation> involvedLocations;
 	@Attribute
	private Long startTime;
 	@Attribute
	private Long endTime;
 	@Attribute
 	private boolean active;
 
 	void setWhoStarted(PlayerCharacter character)
 	{
 		this.whoStarted = character;
 	}
 
 	void setStartLocation(TrackedLocation location)
 	{
 		this.startLocation = location;
 	}
 
 	void setInvolvedCharacters(Set<PlayerCharacter> characters)
 	{
 		this.involvedCharacters = characters;
 	}
 
 	void setInvolvedLocations(Set<TrackedLocation> locations)
 	{
 		this.involvedLocations = locations;
 	}
 
 	void setStartTime(long time)
 	{
 		this.startTime = time;
 	}
 
 	public void setEndTime(long time)
 	{
 		this.endTime = time;
 	}
 
 	public void setActive(boolean active)
 	{
 		this.active = active;
 	}
 
 	public static void save(TrackedBattle battle)
 	{
 		JOhm.save(battle);
 	}
 
 	public static TrackedBattle load(long id) // TODO This belongs somewhere else.
 	{
 		return JOhm.get(TrackedBattle.class, id);
 	}
 
 	public static Set<TrackedBattle> loadAll()
 	{
 		return JOhm.getAll(TrackedBattle.class);
 	}
 
 	public Long getId()
 	{
 		return this.Id;
 	}
 
 	public void initilize()
 	{
 		this.involvedCharacters = Sets.newHashSet();
 		this.involvedLocations = Sets.newHashSet();
 	}
 
 	public void addCharacter(PlayerCharacter character)
 	{
 		this.involvedCharacters.add(character);
 		if(character.getOfflinePlayer().isOnline()) addLocation(character.getOfflinePlayer().getPlayer().getLocation());
 		save(this);
 	}
 
 	public void removeCharacter(PlayerCharacter character)
 	{
 		if(this.involvedCharacters.contains(character)) this.involvedCharacters.remove(character);
 		save(this);
 	}
 
 	public Set<TrackedLocation> getLocations()
 	{
 		return this.involvedLocations;
 	}
 
 	public void addLocation(Location location)
 	{
 		if(!this.involvedLocations.contains(TrackedLocation.getTracked(location))) this.involvedLocations.add(TrackedLocation.getTracked(location));
 		save(this);
 	}
 
 	public void removeLocation(Location location)
 	{
 		if(this.involvedLocations.contains(TrackedLocation.getTracked(location))) this.involvedLocations.remove(TrackedLocation.getTracked(location));
 		save(this);
 	}
 
 	public PlayerCharacter getWhoStarted()
 	{
 		return this.whoStarted;
 	}
 
 	public Set<PlayerCharacter> getInvolvedCharacters()
 	{
 		return this.involvedCharacters;
 	}
 
 	public Long getStartTime()
 	{
 		return this.startTime;
 	}
 
 	public Long getEndTime()
 	{
 		return this.endTime;
 	}
 
 	public boolean isActive()
 	{
 		return this.active;
 	}
 
 	private static final int BATTLEDISTANCE = 16; // TODO
 
 	/**
 	 * Returns the TrackedBattle object with the id <code>battleID</code>.
 	 * 
 	 * @return TrackedBattle
 	 */
 	public static TrackedBattle getBattle(long id)
 	{
 		return TrackedBattle.load(id);
 	}
 
 	/**
 	 * Returns an ArrayList of all Battles.
 	 * 
 	 * @return ArrayList
 	 */
 	public static Set<TrackedBattle> getAll()
 	{
 		return TrackedBattle.loadAll();
 	}
 
 	/**
 	 * Returns an ArrayList of all active Battles.
 	 * 
 	 * @return ArrayList
 	 */
 	public static Set<TrackedBattle> getAllActive()
 	{
 		Set<TrackedBattle> battles = new HashSet<TrackedBattle>();
 		for(TrackedBattle battle : getAll())
 		{
 			if(battle.isActive()) battles.add(battle);
 		}
 		return battles;
 	}
 
 	/**
 	 * Returns an ArrayList of all active battles near <code>location</code>.
 	 * 
 	 * @param location the location to check.
 	 * @return TrackedBattle
 	 */
 	public static TrackedBattle getActiveBattle(Location location)
 	{
 		for(TrackedBattle battle : getAllActive())
 		{
 			if(isNearBattle(battle, location)) return battle;
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the current active TrackedBattle for <code>character</code>.
 	 * 
 	 * @param character the character whose active battle to return.
 	 * @return TrackedBattle
 	 */
 	public static TrackedBattle getActiveBattle(PlayerCharacter character)
 	{
 		for(TrackedBattle battle : getAllActive())
 		{
 			if(isInBattle(battle, character)) return battle;
 		}
 		return null;
 	}
 
 	/**
 	 * Returns true if <code>location</code> is near <code>battle</code>.
 	 * 
 	 * @param battle the battle to compare.
 	 * @param location the location to compare.
 	 * @return boolean
 	 */
 	public static boolean isNearBattle(TrackedBattle battle, Location location)
 	{
 		for(TrackedLocation battleLocation : battle.getLocations())
 		{
 			if(location.distance(battleLocation.toLocation()) <= BATTLEDISTANCE) return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns true if <code>character</code> is involved in the given <code>battle</code>.
 	 * 
 	 * @param battle the battle to check for.
 	 * @param character the character to check.
 	 * @return boolean
 	 */
 	public static boolean isInBattle(TrackedBattle battle, PlayerCharacter character)
 	{
 		for(PlayerCharacter involved : battle.getInvolvedCharacters())
 		{
 			if(involved.equals(character)) return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns true if the <code>location</code> is near any active battle.
 	 * 
 	 * @param location the location to check.
 	 * @return boolean
 	 */
 	public static boolean isNearAnyActiveBattle(Location location)
 	{
 		for(TrackedBattle battle : getAllActive())
 		{
 			if(isNearBattle(battle, location)) return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns true if the <code>character</code> is involved in an active battle.
 	 * 
 	 * @param character the character to check.
 	 * @return boolean
 	 */
 	public static boolean isInAnyActiveBattle(PlayerCharacter character)
 	{
 		for(TrackedBattle battle : getAllActive())
 		{
 			if(isInBattle(battle, character)) return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns true if the <code>command</code> is blocked during battles.
 	 * 
 	 * @param command the command to check.
 	 * @return boolean
 	 */
 	public static boolean isBlockedCommand(String command)
 	{
 		for(String blocked : Demigods.config.getSettingArrayListString("battles.blocked_commands"))
 		{
 			if(command.equalsIgnoreCase(blocked)) return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Checks all battles and sets them to inactive where need-be.
 	 */
 	public static void checkForInactiveBattles()
 	{
 		for(TrackedBattle battle : getAllActive())
 		{
 			long battleId = battle.getId();
 			if(!DemigodsData.hasTimed("battle", String.valueOf(battleId)))
 			{
 				BattleEndEvent battleEvent = new BattleEndEvent(battleId, System.currentTimeMillis());
 				Bukkit.getServer().getPluginManager().callEvent(battleEvent);
 				if(!battleEvent.isCancelled()) battle.setActive(false);
 			}
 		}
 	}
 
 	/**
 	 * Processes the given characters into a battle.
 	 * 
 	 * @param hitChar the character being hit.
 	 * @param hittingChar the character doing to hitting.
 	 */
 	public static void battleProcess(PlayerCharacter hitChar, PlayerCharacter hittingChar)
 	{
 		TrackedBattle battle = null;
 		TrackedBattle otherBattle = null;
 		Player hit = hitChar.getOfflinePlayer().getPlayer();
 		Player hitting = hittingChar.getOfflinePlayer().getPlayer();
 
 		if(isInAnyActiveBattle(hitChar))
 		{
 			battle = getActiveBattle(hitChar);
 			if(isInAnyActiveBattle(hittingChar) && getActiveBattle(hittingChar) != battle) otherBattle = getActiveBattle(hittingChar);
 		}
 		else if(isInAnyActiveBattle(hittingChar))
 		{
 			battle = getActiveBattle(hittingChar);
 			if(isInAnyActiveBattle(hitChar) && getActiveBattle(hitChar) != battle) otherBattle = getActiveBattle(hitChar);
 		}
 		else if(isNearAnyActiveBattle(hit.getLocation()))
 		{
 			battle = getActiveBattle(hit.getLocation());
 			if(isNearAnyActiveBattle(hitting.getLocation()) && getActiveBattle(hitting.getLocation()) != battle) otherBattle = getActiveBattle(hitting.getLocation());
 		}
 		else if(isNearAnyActiveBattle(hitting.getLocation()))
 		{
 			battle = getActiveBattle(hitting.getLocation());
 			if(isNearAnyActiveBattle(hit.getLocation()) && getActiveBattle(hit.getLocation()) != battle) otherBattle = getActiveBattle(hit.getLocation());
 		}
 
 		if(battle == null)
 		{
 			Long startTime = System.currentTimeMillis();
 			int battleID = MiscUtility.generateInt(5);
 			BattleStartEvent battleEvent = new BattleStartEvent(battleID, hitChar, hittingChar, startTime);
 			Bukkit.getServer().getPluginManager().callEvent(battleEvent);
 			if(!battleEvent.isCancelled()) TrackedModelFactory.createTrackedBattle(hittingChar, hitChar, startTime);
 		}
 		else
 		{
 			if(otherBattle == null)
 			{
 				long battleID = battle.getId();
 				BattleParticipateEvent battleEvent = new BattleParticipateEvent(battleID, hitChar, hittingChar);
 				Bukkit.getServer().getPluginManager().callEvent(battleEvent);
 				if(!battleEvent.isCancelled())
 				{
 					battle.addCharacter(hitChar);
 					battle.addCharacter(hittingChar);
 					DemigodsData.saveTimed("battle", String.valueOf(battleID), true, 10);
 				}
 			}
 			else
 			{
 				// Set other battles to inactive
 				battle.setActive(false);
 				otherBattle.setActive(false);
 
 				BattleCombineEvent battleEvent;
 				TrackedBattle combinedBattle;
 				if(battle.getStartTime() < otherBattle.getStartTime())
 				{
 					battleEvent = new BattleCombineEvent(battle, otherBattle, System.currentTimeMillis());
 					Bukkit.getServer().getPluginManager().callEvent(battleEvent);
 					if(!battleEvent.isCancelled())
 					{
 						combinedBattle = TrackedModelFactory.createTrackedBattle(battle.getWhoStarted(), hitChar, battle.getStartTime());
 						combinedBattle.addCharacter(hittingChar);
 					}
 					else return;
 				}
 				else
 				{
 					battleEvent = new BattleCombineEvent(otherBattle, battle, System.currentTimeMillis());
 					Bukkit.getServer().getPluginManager().callEvent(battleEvent);
 					if(!battleEvent.isCancelled())
 					{
 						combinedBattle = TrackedModelFactory.createTrackedBattle(otherBattle.getWhoStarted(), hitChar, otherBattle.getStartTime());
 						combinedBattle.addCharacter(hittingChar);
 					}
 					else return;
 				}
 
 				// Add all involved locations and characters from both other events
 				Set<PlayerCharacter> characters = Sets.newHashSet();
 				Set<TrackedLocation> locations = Sets.newHashSet();
 
 				// TrackedBattle
 				for(PlayerCharacter character : battle.getInvolvedCharacters())
 				{
 					if(!characters.contains(character)) characters.add(character);
 				}
 				for(TrackedLocation location : battle.getLocations())
 				{
 					if(!locations.contains(location)) locations.add(location);
 				}
 
 				// Other TrackedBattle
 				for(PlayerCharacter character : otherBattle.getInvolvedCharacters())
 				{
 					if(!characters.contains(character)) characters.add(character);
 				}
 				for(TrackedLocation location : otherBattle.getLocations())
 				{
 					if(!locations.contains(location)) locations.add(location);
 				}
 
 				// Overwrite data in the new combined battle // TODO Fix this.
 				combinedBattle.setInvolvedCharacters(characters);
 				combinedBattle.setInvolvedLocations(locations);
 				save(combinedBattle);
 			}
 		}
 	}
 }
