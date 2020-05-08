 package com.secondhand.model;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import com.badlogic.gdx.math.Vector2;
 import com.secondhand.debug.MyDebug;
 import com.secondhand.math.PolygonUtil;
 import com.secondhand.model.powerup.EatObstacle;
 import com.secondhand.model.powerup.ExtraLife;
 import com.secondhand.model.powerup.RandomTeleport;
 import com.secondhand.model.powerup.ScoreUp;
 import com.secondhand.model.powerup.Shield;
 import com.secondhand.model.powerup.SpeedUp;
 import com.secondhand.opengl.Circle;
 import com.secondhand.util.RandomUtil;
 
 // TODO: this generation of levels seems to be a bit slow, so we probably need a level loading screen.	
 public class RandomLevelGenerator {
 
 	private final int levelNumber;
 	
 	public final Player player;
 	public final int levelWidth;
 	public final int levelHeight;
 	public final int playerMaxSize;
 	
 	public final List<Entity> entityList;
 	public final List<Enemy> enemyList;
 	
 	private final GameWorld level;
 	
 	
 	RandomLevelGenerator(final Player player, final GameWorld level) {
 		level.getPhysicsWorld();
 		this.levelNumber = level.getLevelNumber();
 		this.level = level;
 		this.player = player;
 		
 		this.levelWidth = 2000 * levelNumber;
 		this.levelHeight = 2000 * levelNumber;
 	
 		this.playerMaxSize = 40 * levelNumber;
 		
 		
 		this.entityList = new ArrayList<Entity>();
 		// to make it easier to place out the entities.
 		entityList.add(player);
 		placeOutLevelEntities();
 		
 		this.enemyList = new ArrayList<Enemy>();
 		placeOutEnemies();
 		
 		// we don't to save it here.
 		entityList.remove(player);
 	}
 	
 
 	private void placeOutEnemies() {
 		enemyList.add(new Enemy(new Vector2(800, 800), 40, level));
 		enemyList.add(new Enemy(new Vector2(500,500), 50, level));
 		enemyList.add(new Enemy(new Vector2(900,400), 45, level));
 
 		for (final Enemy enemy : enemyList) {
 			entityList.add(enemy);
 		}
 
 	}
 
 	private boolean isTooCloseToOtherEntity(final float x, final float y, final float radius) {
 		// setting high values for this constant will cause long level generation times, so be careful.
 		final float MINIMUM_DISTANCE = 60;
 		
 		for(final Entity entity: this.entityList) {
 			if(entity instanceof CircleEntity) {
 				final Circle other = (Circle)entity.getShape();
 				
 				final float dx = Math.abs(x - other.getX());
 				final float dy = Math.abs(y - other.getY());
 				
 				final float dist = (float)Math.sqrt(dx*dx + dy*dy) - radius - other.getRadius();
 				
 				if(dist < MINIMUM_DISTANCE) {
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	
 	private void placeOutLevelEntities() {
 	
 		final float K = 1.2f;
 		
 		final int MINIMUM_PLAYER_EATABLE = 10;
 		int numPlayerEatable = 0;
 		
 		final float MAX_SIZE = 20f * this.levelNumber * K;
 		
 		final float MIN_SIZE = player.getRadius() - 10;
 		if(MIN_SIZE < 0) {
 			MyDebug.e("planet minimum size negative");
 		}
 		
 		final int PLANETS = (int)( 25 * this.levelNumber * K);
 		
 		// make sure they don't get too close to the edges.
 		final int HEIGHT = (int)(this.levelHeight - MAX_SIZE - 50);
 		final int WIDTH = (int)(this.levelWidth - MAX_SIZE - 50);
 		
 		final Random rng = new Random();
 
 		for (int i = 0; i < PLANETS; ++i) {
 			
 			float radius;
 			
 			// first ensure we place out some player eatable ones.
 			if(numPlayerEatable <= MINIMUM_PLAYER_EATABLE) {
 				
 				while(true) {
 					radius = RandomUtil.nextFloat(rng, MIN_SIZE, player.getRadius());
 					if(radius < player.getRadius())
 						break;
 				}
 				numPlayerEatable++;
 				
 			} else {
 				radius = RandomUtil.nextFloat(rng, MIN_SIZE, MAX_SIZE);
 			}
 			
 			float x;
 			float y;
 
 			while (true) {
 
 				x = rng.nextInt(WIDTH);
 				y = rng.nextInt(HEIGHT);
 
 				if(!isTooCloseToOtherEntity(x, y, radius)) {
 					break;
 				}
 			}
 
 			entityList.add(new Planet(new Vector2(x, y), radius, RandomUtil
 					.randomEnum(rng, PlanetType.class), level));
 		}
 		
 		entityList.add(new Obstacle(new Vector2(400, 400), PolygonUtil.getRandomPolygon() , level));		
 		
 		entityList.add(new RandomTeleport(new Vector2(100, 500), level));
 
 		entityList.add(new Shield(new Vector2(20, 500), level));
 
 		entityList.add(new SpeedUp(new Vector2(20, 700), level));
 
 		entityList.add(new ExtraLife(new Vector2(20, 800), level));
 		
 		entityList.add(new ScoreUp(new Vector2(20, 900), level));
 		
 		entityList.add(new EatObstacle(new Vector2(20, 400), level));
 		
 	}
 }
