 /*
  * Copyright 2013 Michael McKnight. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice, this list of
  *       conditions and the following disclaimer.
  *
  *    2. Redistributions in binary form must reproduce the above copyright notice, this list
  *       of conditions and the following disclaimer in the documentation and/or other materials
  *       provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation are those of the
  * authors and contributors and should not be interpreted as representing official policies,
  * either expressed or implied, of anybody else.
  */
 
 package com.forgenz.mobmanager.spawner.tasks.spawnfinder;
 
 import java.util.HashMap;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import com.forgenz.mobmanager.MMComponent;
 import com.forgenz.mobmanager.P;
 import com.forgenz.mobmanager.spawner.config.Mob;
 import com.forgenz.mobmanager.spawner.config.Region;
 import com.forgenz.mobmanager.spawner.config.SpawnerConfig;
 import com.forgenz.mobmanager.spawner.util.MobReference;
 import com.forgenz.mobmanager.spawner.util.PlayerMobCounter;
 
 /**
  * Initiates the finding of spawns for mobs
  */
 public class SpawnFinder extends BukkitRunnable
 {
 	private final SpawnerConfig cfg;
 	
 	private final Queue<Player> playerQueue = new ConcurrentLinkedQueue<Player>();
 	private int ticksLeft;
 	
 	private final SpawnAttemptExecutor spawnAttemptExecutor = new SpawnAttemptExecutor(this, playerQueue);
 	
 	private final ConcurrentHashMap<String, PlayerMobCounter> playerMobs; 
 	private final ConcurrentHashMap<String, HashMap<String, PlayerMobCounter>> groupedPlayerMobs;
 	
 	public SpawnFinder()
 	{
 		// Fetch spawner config
 		cfg = MMComponent.getSpawner().getConfig();
 		
		playerMobs = new ConcurrentHashMap<String, PlayerMobCounter>(cfg.spawnFinderThreads);
		groupedPlayerMobs = new ConcurrentHashMap<String, HashMap<String, PlayerMobCounter>>(cfg.spawnFinderThreads);
 		
 		this.ticksLeft = cfg.ticksPerSpawn;
 		
 		runTaskTimer(P.p(), 1L, 2L);
 	}
 	
 	@Override
 	public void run()
 	{
 		// Initialise the task
 		if (ticksLeft-- == cfg.ticksPerSpawn)
 		{
 			for (Player player : Bukkit.getOnlinePlayers())
 				playerQueue.add(player);
 			return;
 		}
 		
 		spawnAttemptExecutor.execute(ticksLeft);
 		
 		// Reset everything
 		if (ticksLeft == 0)
 		{
 			playerQueue.clear();
 			ticksLeft = cfg.ticksPerSpawn;
 		}
 	}
 	
 	/**
 	 * Fetches the number of mobs which the player has spawned
 	 * 
 	 * @param player The player involved
 	 * @param mobLimitTimeout The timeout for when a mob is removed
 	 * 
 	 * @return The number of mobs which the player has spawned
 	 */
 	public int getMobCount(Player player, int mobLimitTimeout)
 	{
 		PlayerMobCounter limiter = playerMobs.get(player.getName());
 		
 		return limiter != null ? limiter.getMobCount() : 0;
 	}
 	
 	public boolean withinGroupedLimit(Player player, Region region, Mob mob)
 	{
 		if (mob.playerLimitGroup.length() <= 0)
 			return true;
 		
 		int limit = region.getPlayerGroupMobLimit(mob.playerLimitGroup);
 		
 		if (limit <= 0)
 			return true;
 		
 		HashMap<String, PlayerMobCounter> playerLimiters = groupedPlayerMobs.get(player.getName());
 		
 		if (playerLimiters == null)
 			return true;
 		
 		synchronized (playerLimiters)
 		{
 			PlayerMobCounter limiter = playerLimiters.get(mob.playerLimitGroup);
 			return limiter != null ? limiter.withinLimit(limit, region.playerMobCooldown) : true;
 		}
 	}
 	
 	/**
 	 * Clears mobs which count towards the players limit
 	 * 
 	 * @param player
 	 */
 	public void removeMobs(Player player)
 	{
 		playerMobs.remove(player.getName());
 		groupedPlayerMobs.remove(player.getName());
 	}
 
 	/**
 	 * Adds the placeholder to spawn limits to prepare for spawning a mob
 	 * 
 	 * @param player The player involved
 	 * @param mob The mob config used to create the entity
 	 * @param mobRef The entity to add
 	 * @return True if the mob is allowed to spawn
 	 */
 	public boolean addSpawnedMob(Player player, Mob mob, MobReference mobRef)
 	{
 		// Fetch the players mob list
 		PlayerMobCounter limiter = playerMobs.get(player.getName());
 			
 		// If the limiter doesn't exist create it
 		if (limiter == null)
 			playerMobs.put(player.getName(), limiter = new PlayerMobCounter(player));
 		
 		// Track the entity
 		if (!limiter.add(mobRef))
 			return false;
 		
 		// Check for grouped limiters
 		if (mob.playerLimitGroup.length() > 0)
 		{
 			HashMap<String, PlayerMobCounter> playerLimiters = groupedPlayerMobs.get(player.getName());
 			
 			if (playerLimiters == null)
 				groupedPlayerMobs.put(player.getName(), playerLimiters = new HashMap<String, PlayerMobCounter>());
 			
 			synchronized (playerLimiters)
 			{
 				PlayerMobCounter groupedLimiter = playerLimiters.get(mob.playerLimitGroup);
 				
 				if (groupedLimiter == null)
 					playerLimiters.put(mob.playerLimitGroup, groupedLimiter = new PlayerMobCounter(player));
 				
 				if (!groupedLimiter.add(mobRef))
 					return false;
 			}
 		}
 		return true;
 	}
 }
