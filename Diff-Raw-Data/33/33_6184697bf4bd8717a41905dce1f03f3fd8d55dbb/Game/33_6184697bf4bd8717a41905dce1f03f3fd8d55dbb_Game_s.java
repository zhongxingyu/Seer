 package com.avona.games.towerdefence;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 public class Game {
 	public List<Enemy> enemies = new LinkedList<Enemy>();
 	public List<Tower> towers = new LinkedList<Tower>();
 	public List<Particle> particles = new LinkedList<Particle>();
 	public List<WaveListener> waveCompletedListeners = new LinkedList<WaveListener>();
 	public List<WaveListener> waveBegunListeners = new LinkedList<WaveListener>();
 
 	public TimeTrack gameTime;
 	public TimedCodeManager timedCodeManager;
 
 	public World world;
 
 	/**
 	 * Currently running wave.
 	 */
 	public Wave currentWave;
 
 	/**
 	 * The user has indicated, that the next wave should be started when ready.
 	 */
 	public boolean startNextWave = false;
 
 	public int killed = 0;
 	public int escaped = 0;
 
	public int money = 250;
 
 	/**
 	 * Debugging value that counts the number of enemies that have left the game
 	 * area.
 	 */
 	public int leftBuilding = 0;
 
 	/**
 	 * Which type of tower to build - if any.
 	 */
 	public Tower selectedBuildTower = null;
 
 	/**
 	 * Currently selected, existing tower. We will typically show the properties
 	 * of that tower.
 	 */
 	public LocationObject selectedObject = null;
 
 	private EnemyDeathGivesMoney enemyDeathGivesMoney = new EnemyDeathGivesMoney(
 			this);
 
 	public Game(TimeTrack gameTime, TimedCodeManager timedCodeManager) {
 		this.gameTime = gameTime;
 		this.timedCodeManager = timedCodeManager;
 		world = new World();
 
 		selectedBuildTower = new Tower(timedCodeManager, 1);
 	}
 
 	public boolean canBuildTowerAt(V2 location) {
 		return money >= selectedBuildTower.price;
 	}
 
 	public void addTowerAt(V2 location) {
 		Tower t = selectedBuildTower.copy();
 		t.location = new V2(location);
 		money -= t.price;
 		towers.add(t);
 	}
 
 	public void startWave() {
 		int level = 1;
 		if (currentWave != null) {
 			if (!currentWave.isCompleted()) {
 				// Wait for the wave to complete before starting a new one.
 				startNextWave = true;
 				return;
 			}
 			level = currentWave.getLevel() + 1;
 		}
 
 		startNextWave = false;
 		currentWave = new Wave(this, timedCodeManager, level);
 		for (WaveListener l : waveBegunListeners) {
 			l.onWave(level);
 		}
 	}
 
 	public void onWaveCompleted(int level) {
 		for (WaveListener l : waveCompletedListeners) {
 			l.onWave(level);
 		}
 		if (startNextWave)
 			startWave();
 	}
 
 	public void spawnEnemy(int level) {
 		final V2 location = world.waypoints.get(0).copy();
 		final Enemy e = new Enemy(world, location, level);
 		e.eventListeners.add(enemyDeathGivesMoney);
 		enemies.add(e);
 	}
 
 	public Tower closestTowerWithinRadius(V2 location, float range) {
 		return (Tower) closestStationaryWithinRadius(towers, location, range);
 	}
 
 	public Enemy closestEnemyWithinRadius(V2 location, float range) {
 		return (Enemy) closestStationaryWithinRadius(enemies, location, range);
 	}
 
 	@SuppressWarnings("unchecked")
 	// does not seem to be cast/type/...-able
 	public static Object closestStationaryWithinRadius(final List objects,
 			final V2 location, final float range) {
 		for (final Object o : objects) {
 			if (((LocationObject) o).collidesWith(location, range))
 				return o;
 		}
 		return null;
 	}
 
 	public void updateWorld(final float dt) {
 		/**
 		 * Step all objects first. This will cause them to move.
 		 */
 		for (Enemy e : enemies) {
 			e.step(dt);
 		}
 		for (Tower t : towers) {
 			t.step(dt);
 		}
 
 		Iterator<Particle> piter = particles.iterator();
 		while (piter.hasNext()) {
 			final Particle p = piter.next();
 			p.step(dt);
 			if (p.isDead()) {
 				piter.remove();
 			}
 		}
 
 		/**
 		 * Look for each tower, if it's ready to shoot and if an enemy ready to
 		 * shoot. If so, create a new particle. The tower is free to create any
 		 * particle in its shootTowards() method.
 		 */
 
 		for (Tower t : towers) {
 			Enemy enemy = t.findSuitableEnemy(enemies);
 			if (enemy != null) { // policy found some enemy
 				final Particle p = t.shootTowards(enemy);
 				if (p != null) {
 					particles.add(p);
 				}
 			}
 		}
 
 		/**
 		 * Check for any particle collisions and handle damage.
 		 */
 		Iterator<Enemy> eiter = enemies.iterator();
 		while (eiter.hasNext()) {
 			final Enemy e = eiter.next();
 			if (e.collideWithParticles(particles, dt)) {
 				// Enemy dead.
 				killed += 1;
 				eiter.remove();
 				continue;
 			}
 
 			final V2 w = world.waypoints.get(e.waypointId);
 			if (Collision.movingCircleCollidedWithCircle(e.location,
 					e.velocity, e.radius, w, V2.ZERO, 1, dt)) {
 				e.setWPID(e.waypointId + 1);
 				if (e.escaped) {
 					escaped += 1;
 					eiter.remove();
 					continue;
 				}
 			}
 
 			if (e.location.x < World.ORIGIN_X || e.location.y < World.ORIGIN_Y
 					|| e.location.x > World.WIDTH
 					|| e.location.y > World.HEIGHT) {
 				if (!e.left) {
 					e.left = true;
 					leftBuilding += 1;
 					Util.log("enemy " + e + " has left the building");
 				}
 			} else if (e.left) {
 				e.left = false;
 				leftBuilding -= 1;
 				Util.log("enemy " + e + " has reentered the building!");
 			}
 		}
 	}
 }
