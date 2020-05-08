 package com.avona.games.towerdefence;
 
 public class Enemy extends MovingObject {
 	protected World world;
 	public V2 target;
 	public int health = 100;
 	public int waypointId = 1;
 
 	public Enemy(World world, V2 location) {
 		this.world = world;
 		this.location = location;
		velocity.speed = 200;
 		setWPID(1);
 		System.out.println(velocity);
 		System.out.println(location);
 	}
 
 	public void setWPID(int i) {
 		Util.log("setting to wp" + 1);
 		waypointId = i;
 		target = world.waypoints.get(waypointId);
 		velocity.fromTo(location, target);
 	}
 
 	@Override
 	public void step(float dt) {
 		if (isDead())
 			return;
 
 		velocity.translate(location, dt);
 
 		if (location.x < World.ORIGIN_X) {
 			location.x = World.ORIGIN_X - (location.x - World.ORIGIN_X);
 			velocity.normalisedDirection.x = -velocity.normalisedDirection.x;
 		}
 		if (location.y < World.ORIGIN_Y) {
 			location.y = World.ORIGIN_Y - (location.y - World.ORIGIN_Y);
 			velocity.normalisedDirection.y = -velocity.normalisedDirection.y;
 		}
 		if (location.x > World.WIDTH) {
 			location.x = World.WIDTH - (location.x - World.WIDTH);
 			velocity.normalisedDirection.x = -velocity.normalisedDirection.x;
 		}
 		if (location.y > World.HEIGHT) {
 			location.y = World.HEIGHT - (location.y - World.HEIGHT);
 			velocity.normalisedDirection.y = -velocity.normalisedDirection.y;
 		}
 	}
 
 	public boolean isDead() {
 		return health <= 0;
 	}
 
 	public void inflictDamage(int damage) {
 		health -= damage;
 		System.out.println(health);
 	}
 }
