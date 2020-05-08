 package com.secondhand.model.entity;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import com.secondhand.model.physics.Vector2;
 import com.secondhand.model.powerup.PowerUp;
 import com.secondhand.model.powerup.PowerUpFactory;
 import com.secondhand.model.resource.PlanetType;
 import com.secondhand.model.util.PolygonUtil;
 import com.secondhand.model.util.RandomUtil;
 import com.secondhand.model.util.sat.Circle;
 import com.secondhand.model.util.sat.Polygon;
 import com.secondhand.model.util.sat.PolygonFactory;
 import com.secondhand.model.util.sat.World;
 
 public class RandomLevelGenerator {
 
 	private final int levelNumber;
 
 	public int levelWidth;
 	public int levelHeight;
 	
 	public int playerMaxSize;
 	public Vector2 playerPosition;
 	public float playerInitialSize;
 	
 	private float x;
 	private float y;
 	
 	public List<Entity> entityList;
 	public List<Enemy> enemyList;
 
 	private World world;
 	private final Random rng;
 
 	private final IPowerUpFactory powerUpFactory;
 	
 	
 	public RandomLevelGenerator(final IGameWorld gameWorld, IPowerUpFactory factory) {
 			
 		this.rng = new Random();
 		this.levelNumber = gameWorld.getLevelNumber();
 		this.enemyList = new ArrayList<Enemy>();
 		this.entityList = new ArrayList<Entity>();
 		this.powerUpFactory = factory;
 		factory.setRandom(rng);
 		generateRandomLevel();
 
 	}
 
 	private void placeOutEnemies(final int ENEMIES, final int EATABLE_ENEMIES,
 			final float ENEMY_EXTRA_RADIUS) {
 		Enemy enemy;
 
 		for (int i = 0; i < EATABLE_ENEMIES; ++i) {
 
 			float radius;
 			while (true) {
 				
 				radius = RandomUtil
 						.nextFloat(rng, Enemy.getMinSize(), GameWorld.PLAYER_STARTING_SIZE);
 
 				x = rng.nextInt(this.levelWidth);
 				y = rng.nextInt(this.levelHeight);
 
 				final Circle circle = new Circle(new Vector2(x, y),
 						radius + ENEMY_EXTRA_RADIUS);
 
 				if (world.addToWorld(circle)) {
 					break;
 				}
 			}
 			enemy = new Enemy(new Vector2(x, y), radius);
 			enemy.setMaxSpeed(this.levelNumber*2);
 			entityList.add(enemy);
 			enemyList.add(enemy);
 		}
 		for (int i = EATABLE_ENEMIES; i < ENEMIES; ++i) {
 
 			float radius;
 			radius = RandomUtil.nextFloat(rng, Enemy.getMinSize(),
 					Enemy.getMaxSize());
 
 			while (true) {
 				x = rng.nextInt(this.levelWidth);
 				y = rng.nextInt(this.levelHeight);
 				final Circle circle = new Circle(new Vector2(x, y),
 						radius + ENEMY_EXTRA_RADIUS);
 
 				// addToWorld add the polygon if it is unoccupied otherwise it
 				// returns false
 				if (world.addToWorld(circle)) {
 					break;
 				}
 
 			}
 			enemy = new Enemy(new Vector2(x, y), radius);
 			enemy.setMaxSpeed(2 + (this.levelNumber - 1) * 2);
 			entityList.add(enemy);
 			enemyList.add(enemy);
 
 		}
 
 	}
 
 	private void placeOutObstacles(final int OBSTACLES) {
 
 		for (int i = 0; i < OBSTACLES; ++i) {
 
 			final List<Vector2> edges = PolygonUtil.getRandomPolygon();
 
 			while (true) {
 
 				x = rng.nextInt(this.levelWidth);
 				y = rng.nextInt(this.levelHeight);
 
 				final Polygon poly = new Polygon(new Vector2(x, y),
 						edges);
 
 				if (world.addToWorld(poly)) {
 					break;
 				}
 			}
 
 			entityList.add(new Obstacle(new Vector2(x, y), edges));
 		}
 	}
 
 	private void placeOutPowerUps(final int number, final float POWER_UP_EXTRA_SIZE) {
 		for (int i = 0; i < number; ++i) {
 
 			while (true) {
 
 				x = rng.nextInt(this.levelWidth);
 				y = rng.nextInt(this.levelHeight);
 
 				final Polygon poly = PolygonFactory.createRectangle(
 						new Vector2(x - POWER_UP_EXTRA_SIZE, y-POWER_UP_EXTRA_SIZE), PowerUp.WIDTH + POWER_UP_EXTRA_SIZE,
 						PowerUp.HEIGHT+ POWER_UP_EXTRA_SIZE);
 
 				if (world.addToWorld(poly)) {
 					break;
 				}
 			}
 			
 			entityList.add( (Entity) powerUpFactory.getRandomPowerUp(new Vector2(x,
 					y)));
 		}
 	}
 
 	private void placeOutPlanets(final int NUMBER_EATABLE, final int NUMBER,
 			final float MIN_SIZE, final float MAX_SIZE, final float PLANET_EXTRA_RADIUS) {
 
 		float radius;
 
 		// Start with the smaller planets.
 		for (int i = 0; i < NUMBER_EATABLE; ++i) {
 
 			while (true) {
 				radius = RandomUtil
 						.nextFloat(rng, MIN_SIZE, GameWorld.PLAYER_STARTING_SIZE);
 
 				x = rng.nextInt(this.levelWidth);
 				y = rng.nextInt(this.levelHeight);
 
 				final Circle circle = new Circle(new Vector2(x, y),
 						radius + PLANET_EXTRA_RADIUS);
 
 				if (world.addToWorld(circle)) {
 					break;
 				}
 			}
 			entityList.add(new Planet(new Vector2(x, y), radius,
 					RandomUtil.randomEnum(rng, PlanetType.class)));
 		}
 		for (int i = 0; i < NUMBER; i++) {
 
 			while (true) {
 				radius = RandomUtil.nextFloat(rng, GameWorld.PLAYER_STARTING_SIZE, MAX_SIZE);
 
 				x = rng.nextInt(this.levelWidth);
 				y = rng.nextInt(this.levelHeight);
 
 				final Circle circle = new Circle(new Vector2(x, y),
 						radius + PLANET_EXTRA_RADIUS);
 
 				if (world.addToWorld(circle)) {
 					break;
 				}
 
 			}
 			entityList.add(new Planet(new Vector2(x, y), radius,
 					RandomUtil.randomEnum(rng, PlanetType.class)));
 		}
 	}
 	
 	// returns the cirle hitbox of the player. 
 	private Circle placeOutPlayer() {
 		
 		this.playerInitialSize = GameWorld.PLAYER_STARTING_SIZE;
 		this.playerMaxSize = GameWorld.PLAYER_STARTING_SIZE * (this.levelNumber + 1);
 			
 		final float MINIMUM_DISTANCE_FROM_BORDERS = 30;
 		
 		this.playerPosition = new Vector2();
 		
 		this.playerPosition.x = RandomUtil.nextFloat(rng, 
 				this.playerInitialSize + MINIMUM_DISTANCE_FROM_BORDERS, 
 				this.levelWidth - this.playerInitialSize - MINIMUM_DISTANCE_FROM_BORDERS);
 		this.playerPosition.y = RandomUtil.nextFloat(rng, 
 				this.playerInitialSize + MINIMUM_DISTANCE_FROM_BORDERS, 
 				this.levelHeight - this.playerInitialSize - MINIMUM_DISTANCE_FROM_BORDERS);
 						
 		
 		final float PLAYER_EXTRA_RADIUS = 20;
 		
 		return new Circle(new Vector2(
 				this.playerPosition.x, this.playerPosition.y),
 				this.playerInitialSize + PLAYER_EXTRA_RADIUS);
 	}
 
 	private void generateRandomLevel() {
 		
 		this.levelWidth = 1500 * this.levelNumber;
 		this.levelHeight = 1500 * this.levelNumber;
 		this.world = new World(this.levelWidth, this.levelHeight);
 		
 		// We want to ensure that things are not placed too close to each other.
 		// so we make the collision bodies of these things a little bigger than they
 		// really are.
 		final float PLANET_EXTRA_RADIUS = 20;
 		
 		final float POWER_UP_EXTRA_SIZE = 20;
 		
 		final float ENEMY_EXTRA_RADIUS = 20;
 		
 		world.addToWorld(placeOutPlayer());
 
		
 		// place out entities.
		placeOutObstacles(1 /*levelNumber * 5*/);
 
 		placeOutPlanets(10 * levelNumber, // number player eatable
 
 				4 * levelNumber, // number bigger than player.
 				(GameWorld.PLAYER_STARTING_SIZE / 1.1f),
 				GameWorld.PLAYER_STARTING_SIZE * 7,
 				PLANET_EXTRA_RADIUS);
 
 		placeOutPowerUps(this.levelNumber * 7, POWER_UP_EXTRA_SIZE);
 
 		placeOutEnemies(5 * this.levelNumber,4 * this.levelNumber, ENEMY_EXTRA_RADIUS);
 	}
 }
