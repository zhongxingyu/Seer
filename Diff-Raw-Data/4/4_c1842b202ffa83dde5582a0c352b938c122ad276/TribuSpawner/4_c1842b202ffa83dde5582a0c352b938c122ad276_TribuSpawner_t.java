 /*******************************************************************************
  * Copyright or � or Copr. Quentin Godron (2011)
  * 
  * cafe.en.grain@gmail.com
  * 
  * This software is a computer program whose purpose is to create zombie 
  * survival games on Bukkit's server. 
  * 
  * This software is governed by the CeCILL-C license under French law and
  * abiding by the rules of distribution of free software.  You can  use, 
  * modify and/ or redistribute the software under the terms of the CeCILL-C
  * license as circulated by CEA, CNRS and INRIA at the following URL
  * "http://www.cecill.info". 
  * 
  * As a counterpart to the access to the source code and  rights to copy,
  * modify and redistribute granted by the license, users are provided only
  * with a limited warranty  and the software's author,  the holder of the
  * economic rights,  and the successive licensors  have only  limited
  * liability. 
  * 
  * In this respect, the user's attention is drawn to the risks associated
  * with loading,  using,  modifying and/or developing or reproducing the
  * software by the user in light of its specific status of free software,
  * that may mean  that it is complicated to manipulate,  and  that  also
  * therefore means  that it is reserved for developers  and  experienced
  * professionals having in-depth computer knowledge. Users are therefore
  * encouraged to load and test the software's suitability as regards their
  * requirements in conditions enabling the security of their systems and/or 
  * data to be ensured and,  more generally, to use and operate it in the 
  * same conditions as regards security. 
  * 
  * The fact that you are presently reading this means that you have had
  * knowledge of the CeCILL-C license and that you accept its terms.
  ******************************************************************************/
 package graindcafe.tribu;
 
 import graindcafe.tribu.Configuration.Constants;
 import graindcafe.tribu.Configuration.FocusType;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Zombie;
 import org.bukkit.inventory.ItemStack;
 
 import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
 import de.ntcomputer.minecraft.controllablemobs.api.ControllableMobs;
 import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AITargetNearest;
 
 public class TribuSpawner {
 	HashMap<Entity, TribuZombie>	bukkitAssociation	= new HashMap<Entity, TribuZombie>();	;
 	/**
 	 * Is the round finish
 	 */
 	private boolean					finished;
 
 	/**
 	 * Health of zombie to spawn
 	 */
 	private int						health;
 	/**
 	 *  A zombie just spawned
 	 */
 	private boolean					justspawned;
 	/**
 	 * number of zombies to spawn
 	 */
 	private int						totalToSpawn;
 	/**
 	 * Tribu
 	 */
 	private final Tribu				plugin;
 	/**
 	 * Gonna start
 	 */
 	private boolean					starting;
 	/**
 	 * spawned zombies
 	 */
 	private int						alreadySpawned;
 
 	/**
 	 * Referenced zombies
 	 */
 	// private final LinkedList<ControllableMob<Zombie>> zombies;
 
 	/**
 	 * Init the spawner
 	 * @param instance of Tribu
 	 */
 	public TribuSpawner(final Tribu instance) {
 		plugin = instance;
 		alreadySpawned = 0;
 		totalToSpawn = 5;
 		finished = false;
 		starting = true;
 		health = 10;
 		// zombies = new LinkedList<ControllableMob<Zombie>>();
 	}
 
 	/**
 	 * Check that all referenced zombies are alive
 	 * Useful to check if a zombie has been despawned (too far away, killed but not caught) 
 	 * set finished if they are all dead
 	 */
 	public void checkZombies() {
 
 		Iterator<Entry<Entity, TribuZombie>> it = bukkitAssociation.entrySet().iterator();
 		finished = false;
 		while (it.hasNext()) {
 			ControllableMob<Zombie> e = it.next().getValue().getControl();
 			if (e == null || e.getEntity().isDead()) {
 				it.remove();
 				finished = false;
 			}
 		}
 
 	}
 
 	/**
 	 * Delete all zombies and prevent the spawner to continue spawning
 	 */
 	public void clearZombies() {
 		Iterator<Entity> it = bukkitAssociation.keySet().iterator();
 		while (it.hasNext()) {
 			it.next().remove();
 			it.remove();
 		}
 		resetTotal();
 		// zombies.clear();
 	}
 
 	/**
 	 * Despawn a killed zombie
 	 * @param zombie zombie to unreference
 	 * @param drops drops to clear
 	 */
 	public void despawnZombie(final TribuZombie zombie, final List<ItemStack> drops) {
 		if (bukkitAssociation.remove(zombie.getControl().getEntity()) != null) {
 			drops.clear();
 			tryStartNextWave();
 		}
 		// Else The zombie may have been deleted by "removedZombieCallback"
 		/*else {
 			
 			plugin.LogWarning("Unreferenced zombie despawned");
 		}*/
 	}
 
 	/**
 	 * Set that it's finish
 	 */
 	public void finishCallback() {
 		finished = true;
 	}
 
 	// Debug command
 	/**
 	 * This is a debug command returning the location of a living zombie
 	 * It prints info of this zombie on the console or a severe error
 	 * @return location of a living zombie
 	 */
 	public Location getFirstZombieLocation() {
 		if (alreadySpawned > 0)
 			if (!bukkitAssociation.isEmpty()) {
 				plugin.LogInfo("Health : " + bukkitAssociation.get(0).getControl().getEntity().getHealth());
 				plugin.LogInfo("LastDamage : " + bukkitAssociation.get(0).getControl().getEntity().getLastDamage());
 				plugin.LogInfo("isDead : " + bukkitAssociation.get(0).getControl().getEntity().isDead());
 				return bukkitAssociation.get(0).getControl().getEntity().getLocation();
 			} else {
 				plugin.getSpawnTimer().getState();
 				plugin.LogSevere("There is " + bukkitAssociation.size() + " zombie alive of " + alreadySpawned + "/" + totalToSpawn + " spawned . The wave is " + (finished ? "finished" : "in progress"));
 				return null;
 			}
 		else
 			return null;
 	}
 
 	/**
 	 * Get the total quantity of zombie to spawn
 	 * (Not counting zombies killed)
 	 * @return total to spawn
 	 */
 	public int getMaxSpawn() {
 		return totalToSpawn;
 	}
 
 	/**
 	 * Get the number of zombie already spawned
 	 * @return number of zombie already spawned
 	 */
 	public int getTotal() {
 		return alreadySpawned;
 	}
 
 	/**
 	 * Get the first spawn in a loaded chunk
 	 * @return
 	 */
 	public Location getValidSpawn() {
 		for (final Location curPos : plugin.getLevel().getActiveSpawns())
 			if (curPos.getWorld().isChunkLoaded(curPos.getWorld().getChunkAt(curPos))) return curPos;
 		plugin.LogInfo(plugin.getLocale("Warning.AllSpawnsCurrentlyUnloaded"));
 		return null;
 
 	}
 
 	/**
 	 * If the spawner should continue spawning
 	 * @return
 	 */
 	public boolean haveZombieToSpawn() {
 		return alreadySpawned < totalToSpawn;
 	}
 
 	/**
 	 * Check if the living entity is referenced here
 	 * @param ent
 	 * @return if the living entity was spawned by this
 	 */
 	public boolean isSpawned(final LivingEntity ent) {
 		return bukkitAssociation.containsKey(ent);
 	}
 
 	/**
 	 * The wave is completed if there is no zombie to spawn and zombies spawned are dead
 	 * @return is wave completed 
 	 */
 	public boolean isWaveCompleted() {
 		return !haveZombieToSpawn() && bukkitAssociation.isEmpty();
 	}
 
 	/**
 	 * Is currently spawning a zombie ?
 	 * @return
 	 */
 	public boolean justSpawned() {
 		return justspawned;
 	}
 
 	/**
 	 * Kill & unreference a zombie 
 	 * @param e Zombie to despawn
 	 * @param removeReward Reward attakers ?
 	 */
 	public void removedZombieCallback(final Entity e, final boolean removeReward) {
 		if (e != null) {
 			TribuZombie zomb = bukkitAssociation.get(e);
 			if (zomb == null) return;
 			bukkitAssociation.remove(e);
 			alreadySpawned--;
 			if (removeReward) zomb.setNoAttacker();
 			e.remove();
 
 		}
 
 	}
 
 	/**
 	 * Prevent spawner to continue spawning but do not set it as finished
 	 */
 	public void resetTotal() {
 		alreadySpawned = 0;
 		finished = false;
 	}
 
 	/**
 	 * Set health of zombie to spawn
 	 * @param value Health
 	 */
 	public void setHealth(final int value) {
 		health = value;
 	}
 
 	/**
 	 * Set the number of zombie to spawn
 	 * @param count
 	 */
 	public void setMaxSpawn(final int count) {
 		totalToSpawn = count;
 	}
 
 	/**
 	 * Try to spawn a zombie
 	 * @return if zombies still have to spawn (before spawning it)
 	 */
 	@SuppressWarnings("deprecation")
 	public boolean spawnZombie() {
 		if (alreadySpawned < totalToSpawn && !finished) {
 			Location pos = plugin.getLevel().getRandomZombieSpawn();
 			if (pos != null) {
 				if (!pos.getWorld().isChunkLoaded(pos.getWorld().getChunkAt(pos))) {
 					checkZombies();
 
 					pos = getValidSpawn();
 				}
 				if (pos != null) {
 					// Surrounded with justspawned so that the zombie isn't
 					// removed in the entity spawn listener
 					justspawned = true;
 					Zombie zombie;
 					try {
 						zombie = pos.getWorld().spawn(pos, Zombie.class);
 						zombie.setHealth(health);
 						TribuZombie zomb = new TribuZombie(ControllableMobs.assign(zombie, false));
 						if (plugin.config().ZombiesFocus == FocusType.NearestPlayer) zomb.getControl().getAI().addAIBehavior(new AITargetNearest(1, 1000));
 						if (plugin.config().ZombiesFocus == FocusType.DeathSpawn) zomb.getControl().getActions().moveTo(plugin.getLevel().getDeathSpawn());
 						if (plugin.config().ZombiesFocus == FocusType.InitialSpawn) zomb.getControl().getActions().moveTo(plugin.getLevel().getDeathSpawn());
 						if (plugin.config().ZombiesFocus == FocusType.RandomPlayer) //
 							for (int i = 0; i < 5; i++)
 								zomb.getControl().getActions().target(plugin.getRandomPlayer(), true);
 						zomb.getControl().getAttributes().setMaximumNavigationDistance(Double.POSITIVE_INFINITY);
						// Default speed * ( (1/10 + rand() * 1/3 | 1/4) + 3/4 *
						// base )
						zomb.getControl().getProperties().setMovementSpeed(0.25f * ((plugin.config().ZombiesSpeedRandom) ? .1f + (Tribu.getRandom().nextFloat() / 3f) : .25f) + (plugin.config().ZombiesSpeedBase - .25f));
 						bukkitAssociation.put(zombie, zomb);
 						// Rush speed
 						// sun proof
 						// fire proof
 						alreadySpawned++;
 					} catch (final Exception e) {
 						// Impossible to spawn the zombie, maybe because of lack
 						// of
 						// space
 					}
 					justspawned = false;
 				}
 			}
 		} else
 			return false;
 		return true;
 	}
 
 	/**
 	 * Set that the spawner has started
 	 */
 	public void startingCallback() {
 		starting = false;
 	}
 
 	/**
 	 * Try to start the next wave if possible and return if it's starting 
 	 * @return
 	 */
 	public boolean tryStartNextWave() {
 		if (bukkitAssociation.isEmpty() && finished && !starting) {
 			starting = true;
 			plugin.messagePlayers(plugin.getLocale("Broadcast.WaveComplete"));
 			plugin.getWaveStarter().incrementWave();
 			plugin.getWaveStarter().scheduleWave(Constants.TicksBySecond * plugin.config().WaveStartDelay);
 		}
 		return starting;
 	}
 
 	public HashMap<Entity, TribuZombie> getBukkitAssociation() {
 		return bukkitAssociation;
 	}
 
 }
