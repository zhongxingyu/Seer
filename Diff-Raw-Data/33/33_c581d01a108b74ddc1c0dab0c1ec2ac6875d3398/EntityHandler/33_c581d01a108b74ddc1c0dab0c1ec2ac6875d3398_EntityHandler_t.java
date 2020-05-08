 package com.flipptor.orbgame;
 
 import java.util.LinkedList;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.World;
 import com.flipptor.orbgame.definitions.EnemyBodyDef;
 import com.flipptor.orbgame.definitions.EnemyFixtures;
 import com.flipptor.orbgame.definitions.PlayerBodyDef;
 import com.flipptor.orbgame.definitions.PlayerFixtureDef;
 
 public class EntityHandler {
 	private static final float WIDTH = Gdx.graphics.getWidth()/10;
 	private static final float HEIGHT = Gdx.graphics.getHeight()/10;
 	
 	private LinkedList<Entity> enemyList, creditList;
 	private PlayerEntity player;
 	private float dX, dY;
 	//private World world;
 	
 	public EntityHandler(World world) {
 		enemyList = new LinkedList<Entity>();
 		creditList = new LinkedList<Entity>();
 		player = new PlayerEntity(world, 
 				new PlayerBodyDef(new Vector2(WIDTH/2, HEIGHT/2)));
		player.getBody().createFixture(PlayerFixtureDef.INSTANCE);
 		
 		// TODO remove later.
 		enemyList.add(new EnemyEntity(world, new EnemyBodyDef(new Vector2(WIDTH*1.3f/2, HEIGHT*1.3f/2))));
		enemyList.getFirst().getBody().createFixture(EnemyFixtures.MEDIUM.fixtureDef);
 	}
 	
 	public void update() {
 		moveEntities();
 		player.update();
 	}
 	
 	private void moveEntities() {
 		Vector2 pos = player.getBody().getPosition();
 		dX = pos.x - WIDTH/2;
 		dY = pos.y - HEIGHT/2;
 		player.getBody().setTransform(WIDTH/2, HEIGHT/2, 0);
 		for(Entity e : enemyList) {
 			e.move(-dX, -dY);
 		}
 		for(Entity e : creditList) {
 			e.move(-dX, -dY);
 		}
 	}
 }
