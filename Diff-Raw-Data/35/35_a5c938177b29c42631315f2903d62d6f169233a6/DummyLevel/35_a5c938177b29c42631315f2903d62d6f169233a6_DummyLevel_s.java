 package platformer.core.model.level.impl;
 
 import java.util.Collection;
 import java.util.LinkedList;
 
 import platformer.core.model.GameObject;
 import platformer.core.model.Level;
 import platformer.core.model.condition.Condition;
 import platformer.core.model.condition.FalseCondition;
 import platformer.core.model.condition.NotCondition;
 import platformer.core.model.condition.PlayerAliveCondition;
 import platformer.core.model.gameobject.impl.LevelTile;
 import platformer.core.model.systems.impl.physics.PhysicsSystem;
 import platformer.core.model.systems.impl.physics.bodies.Square;
 
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.physics.box2d.Body;
 
 public class DummyLevel implements Level {
 
 	private static final int TILE_WIDTH = 20;
 	private Collection<GameObject> gameObjects;
 
 	public DummyLevel() {
 		gameObjects = new LinkedList<GameObject>();
 	}
 
 	public void initialize(PhysicsSystem physicsSystem) {
		for (int i = 0; i < 200; i++) {
 			Vector3 position = new Vector3(i * TILE_WIDTH, 0, 0);
 			Rectangle bounds = new Rectangle(0, 0, TILE_WIDTH, TILE_WIDTH);
 			Body squareBody = physicsSystem.createBody(Square.class.getName(),
 					position, bounds);
 			gameObjects.add(new LevelTile(position, bounds, squareBody));
 		}
 
 		// Generate some platforms
		for (int i = 0; i < 500; i++) {
			Vector3 position = new Vector3(MathUtils.random(100, 5000),
					MathUtils.random(100, 5000), 0);
 
			int max = MathUtils.random(10, 30);
 			for (int j = 1; j < max; j++) {
 				String textureName;
 				if (j == 1) {
 					textureName = "grass_left";
 				} else if (j == (max - 1)) {
 					textureName = "grass_right";
 				} else {
 					textureName = "grass_single";
 				}
 				Rectangle bounds = new Rectangle(0, 0, TILE_WIDTH, TILE_WIDTH);
 				position.x = position.x + TILE_WIDTH;
 				Body squareBody = physicsSystem.createBody(
 						Square.class.getName(), position, bounds);
 				gameObjects.add(new LevelTile(position, bounds, squareBody,
 						textureName));
 			}
 		}
 	}
 
 	@Override
 	public Collection<GameObject> getGameObjects() {
 		return gameObjects;
 	}
 
 	@Override
 	public Vector2 getGravity() {
 		return new Vector2(0, -9.8f);
 	}
 
 	@Override
 	public Condition getWinningCondition() {
 		return new FalseCondition();
 	}
 
 	@Override
 	public Condition getLosingCondition() {
 		return new NotCondition(new PlayerAliveCondition("player"));
 	}
 
 }
