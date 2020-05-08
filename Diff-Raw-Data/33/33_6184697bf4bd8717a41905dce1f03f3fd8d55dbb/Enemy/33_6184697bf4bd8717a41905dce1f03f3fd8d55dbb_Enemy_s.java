 package com.avona.games.towerdefence;
 
 import java.util.LinkedList;
 import java.util.List;
 
 public class Enemy extends MovingObject {
 	public V2 target;
 	public int health;
 	public int maxHealth;
 	public int level;
 	public int waypointId = 1;
 	public boolean escaped = false;
 	public boolean left = false;
 	public int worth;
 	public List<EnemyEventListener> eventListeners = new LinkedList<EnemyEventListener>();
 	public EnemyParticleCollisionSelector enemyParticleCollisionSelection = new OnlyTargetEnemyParticleCollidor();
 
 	public Enemy(World world, V2 location, int level) {
 		this.level = level;
		this.maxHealth = 50 + 20 * level;
 		this.health = this.maxHealth;
		this.worth = 25 + level;
 		this.world = world;
 		this.location = location;
		this.velocity.setLength(80 + 3 * level);
 		setWPID(1);
 	}
 
 	public boolean collideWithParticles(final List<Particle> particles,
 			final float dt) {
 		return enemyParticleCollisionSelection.collideWithParticles(this,
 				particles, dt);
 	}
 
 	public void setWPID(int i) {
 		if (waypointId + 1 < world.waypoints.size()) {
 			waypointId = i;
 			target = world.waypoints.get(waypointId);
 			velocity.setDirection(location, target);
 		} else {
 			escape();
 		}
 	}
 
 	@Override
 	public void step(float dt) {
 		if (isDead())
 			return;
 
 		location.addWeighted(velocity, dt);
 	}
 
 	public boolean isDead() {
 		return health <= 0;
 	}
 
 	public void inflictDamage(int damage) {
 		health -= damage;
 		if (isDead()) {
 			die();
 		}
 	}
 
 	public void die() {
 		for (EnemyEventListener l : eventListeners) {
 			l.onDeathEvent(this);
 		}
 	}
 
 	public void escape() {
 		escaped = true;
 		for (EnemyEventListener l : eventListeners) {
 			l.onEscapeEvent(this);
 		}
 	}
 }
