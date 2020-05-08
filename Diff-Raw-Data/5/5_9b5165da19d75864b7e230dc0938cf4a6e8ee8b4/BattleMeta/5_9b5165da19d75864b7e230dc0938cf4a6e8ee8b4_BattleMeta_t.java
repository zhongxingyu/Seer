 package com.censoredsoftware.Demigods.Engine.Object.Battle;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import redis.clients.johm.*;
 
 import com.censoredsoftware.Demigods.Engine.Object.Mob.TameableWrapper;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerCharacter;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 @Model
 public class BattleMeta
 {
 	@Id
 	private Long Id;
 	@CollectionSet(of = Long.class)
 	private Set<Long> involvedPlayers;
 	@CollectionSet(of = Long.class)
 	private Set<Long> involvedTameable;
 	@Attribute
 	private int killCounter;
 	@CollectionMap(key = Long.class, value = Integer.class)
 	private Map<Long, Integer> kills;
 	@CollectionMap(key = Long.class, value = Integer.class)
 	private Map<Long, Integer> deaths;
 	@Reference
 	@Indexed
 	private PlayerCharacter startedBy;
 
 	public static BattleMeta create(BattleParticipant participant)
 	{
 		PlayerCharacter character = participant.getRelatedCharacter();
 
 		BattleMeta meta = new BattleMeta();
 		meta.initialize();
 		meta.setStarter(character);
 		BattleMeta.save(meta);
 		return meta;
 	}
 
 	void setStarter(PlayerCharacter character)
 	{
 		this.startedBy = character;
 		addParticipant(character);
 	}
 
 	void initialize()
 	{
 		this.kills = Maps.newHashMap();
 		this.deaths = Maps.newHashMap();
 		this.involvedPlayers = Sets.newHashSet();
 		this.involvedTameable = Sets.newHashSet();
 		this.killCounter = 0;
 	}
 
 	public void addParticipant(BattleParticipant participant)
 	{
 		if(participant instanceof PlayerCharacter) this.involvedPlayers.add(participant.getId());
 		else this.involvedTameable.add(participant.getId());
 		save(this);
 	}
 
 	public void addKill(BattleParticipant participant)
 	{
 		this.killCounter += 1;
 		PlayerCharacter character = participant.getRelatedCharacter();
		if(this.kills.containsKey(character.getId())) this.kills.put(character.getId(), this.kills.get(character.getId()) + 1);
 		else this.kills.put(character.getId(), 1);
 		save(this);
 	}
 
 	public void addDeath(BattleParticipant participant)
 	{
 		PlayerCharacter character = participant.getRelatedCharacter();
		if(this.deaths.containsKey(character.getId())) this.deaths.put(character.getId(), this.deaths.get(character.getId()) + 1);
 		else this.deaths.put(character.getId(), 1);
 		save(this);
 	}
 
 	public PlayerCharacter getStarter()
 	{
 		return this.startedBy;
 	}
 
 	public Set<BattleParticipant> getParticipants()
 	{
 		return new HashSet<BattleParticipant>()
 		{
 			{
 				for(long Id : involvedPlayers)
 				{
 					add(PlayerCharacter.load(Id));
 				}
 				for(long Id : involvedTameable)
 				{
 					add(TameableWrapper.load(Id));
 				}
 			}
 		};
 	}
 
 	public int getKills()
 	{
 		return this.killCounter;
 	}
 
 	public long getId()
 	{
 		return this.Id;
 	}
 
 	public static BattleMeta load(Long id)
 	{
 		return JOhm.get(BattleMeta.class, id);
 	}
 
 	public static Set<BattleMeta> loadAll()
 	{
 		return JOhm.getAll(BattleMeta.class);
 	}
 
 	public static void save(BattleMeta meta)
 	{
 		JOhm.save(meta);
 	}
 
 	public void delete()
 	{
 		JOhm.delete(BattleMeta.class, getId());
 	}
 }
