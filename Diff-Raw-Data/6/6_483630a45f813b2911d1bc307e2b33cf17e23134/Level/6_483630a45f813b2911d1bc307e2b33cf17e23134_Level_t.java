 package logic;
 
 import logic.entity.*;
 import logic.entity.Bot;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import main.Game;
import main.Global;
 import org.jbox2d.callbacks.QueryCallback;
 import org.jbox2d.collision.AABB;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Fixture;
 import org.jbox2d.dynamics.World;
 import org.jbox2d.dynamics.joints.DistanceJointDef;
 import org.jbox2d.dynamics.joints.RevoluteJointDef;
 import org.lwjgl.util.vector.Vector2f;
 
 public class Level {
	private static int defaultWidth = Global.RESOLUTION_X;
	private static int defaultHeight = Global.RESOLUTION_Y;
 	private int widthLevel;
 	private int heightLevel;
     private Game game;
 	private World world = null;
 	private AABB aabb = null;
 	// physic constans
 	// TODO add support resourses manager
 	public static float gravity = -3.8f;
 
 	// list of all object
 	private List<GameObject> gameObjects = new ArrayList<GameObject>();
 	// list of objects which will be added to main list
 	private List<GameObject> gameObjectsToAdd = new ArrayList<GameObject>();
 	// list of objects which will be deleted from main list
 	private List<GameObject> gameObjectsToDelete = new ArrayList<GameObject>();
 
 	private static Random random = new Random();
 
 	public Level(Game game) {
 		EmmiterEffects.init(this);
         this.game = game;
 		this.widthLevel = Level.defaultWidth;
 		this.heightLevel = Level.defaultHeight;
 		this.aabb = new AABB(new Vec2(), new Vec2(this.widthLevel / 30,
 				600 / 30));
 		this.world = new World(new Vec2(0, gravity), false);
 		world.queryAABB(new QueryCallback() {
 
 			@Override
 			public boolean reportFixture(Fixture fixture) {
 				// TODO Auto-generated method stub
 				return true;
 			}
 		}, aabb);
 
 		world.setContactListener(new CollisionListener(this));
 	}
 
 	public List<GameObject> getGameObjects() {
 		return gameObjects;
 	}
 
 	public List<GameObject> getNotAddedGameObjects() {
 		return gameObjectsToAdd;
 	}
 
 	public List<GameObject> getNotDeletedGameObjects() {
 		return gameObjectsToDelete;
 	}
 
 	// this is method for running game in test mode
 	public void testInitLevel() {
 
 
 
 
 		Ship ship = new Ship(this, 100f, 500f);
 		game.getPlayer().setControlledObject(ship);
 		gameObjects.add(ship);
 
 		Ship ship2 = new Ship(this, 500f, 500f);
 		//Bot bot = new Bot(ship2);
 		gameObjects.add(ship2);
 	    //gameObjects.add(bot);
 		Wall leftWall = new Wall(this, 5, defaultHeight / 2, 10, defaultHeight);
 		gameObjects.add(leftWall);
 		Wall upWall = new Wall(this, defaultWidth / 2, defaultHeight - 5,
 				defaultWidth, 10);
 		gameObjects.add(upWall);
 		Wall righWall = new Wall(this, defaultWidth - 5, defaultHeight / 2, 10,
 				defaultHeight);
 		gameObjects.add(righWall);
 		Wall downWall = new Wall(this, defaultWidth / 2, 5, defaultWidth, 10);
 		gameObjects.add(downWall);
 
 		// gameObjects.add(new Wall(this, 700, 100, 20, 200));
 		gameObjects.add(new JumpWall(this, 900, 40, 200, 40));
 
 		// add to world a chain
 		gameObjects.add(new Chain(this, upWall.getBody(), new Vector2f(
 				defaultWidth / 2f, defaultHeight - 5), ship2.getBody(),
 				new Vector2f(500 + 50, 500 + 20)));
 		gameObjects.add(new Chain(this, upWall.getBody(), new Vector2f(
 				defaultWidth / 4f, defaultHeight - 5), ship2.getBody(),
 				new Vector2f(500 - 50, 500 + 20)));
 
 
 		 /*gameObjects.add(new Chain(this, downWall.getBody(),
 		 new Vector2f(100, 5), ship.getBody(), new Vector2f(100,
 		 500 - 20)));                          */
 		/*{
 		 TrapRotation tr = new TrapRotation(this, new Vector2f(900f, 300f));
 		 gameObjects.add(tr);
 		 RevoluteJointDef join = new RevoluteJointDef();
 		 join.initialize(tr.getBody(), downWall.getBody(), tr.getBody()
 		 .getPosition());
 		 world.createJoint(join);
 		 } */
 
 
 
 		gameObjects.add(new Asteroid(this, new Vector2f(400f, 300f)));
 		gameObjects.add(new Asteroid(this, new Vector2f(550f, 300f)));
         /*gameObjects.add(new Asteroid(this, new Vector2f(600f, 300f)));
         gameObjects.add(new Asteroid(this, new Vector2f(650f, 300f)));
         gameObjects.add(new Asteroid(this, new Vector2f(700f, 300f)));
         gameObjects.add(new Asteroid(this, new Vector2f(750f, 300f)));
         gameObjects.add(new Asteroid(this, new Vector2f(800f, 300f)));
         gameObjects.add(new Asteroid(this, new Vector2f(850f, 300f)));*/
 
 
 		gameObjects.add(new AeroDynTest(this));
 
         //coment
 
         // /gameObjects.add(new Engine(this,new Vector2f(200f,200f)));
         //gameObjects.add(new Chain(this, ship.getBody(), ship.getPosition(),
         //ship.getBody(), new Vector2f(100, 200)));
 
         //gameObjects.add(new AeroDynTest(this));
         //ManipulareTool mt = new ManipulareTool(this, new Vector2f(670f, 500f));
         //gameObjects.add(mt);
         //gameObjects.add(new Chain(this, upWall.getBody(),
         //new Vector2f(670, 797), mt.getBody(), new Vector2f(670, 480)));
         // gameObjects.add(new Engine(this,new Vector2f(200f,200f)));
         // gameObjects.add(new Chain(this, ship.getBody(), ship.getPosition(),
         // ship.getBody(), new Vector2f(100, 200)));
 
 // ManipulareTool mt = new ManipulareTool(this, new Vector2f(670f, 500f));
 //		gameObjects.add(mt);
 //		gameObjects.add(new Chain(this, upWall.getBody(),
 //				new Vector2f(670, 797), mt.getBody(), new Vector2f(670, 480)));     */
 	}
 
 	public Player getPlayer() {
 		return game.getPlayer();
 	}
 
 	/**
 	 * added by Arthur for testing objects 31.07.12
 	 */
 	// method for adding not added objects after last 'foreach'
 	public void addNotAddedObjects() {
 		if (gameObjectsToAdd.size() != 0)
 			gameObjects.addAll(gameObjectsToAdd);
 		gameObjectsToAdd.clear();
 	}
 
 	public void deleteNotDeletedObjects() {
 		if (gameObjectsToDelete.size() != 0)
 			gameObjects.removeAll(gameObjectsToDelete);
 		gameObjectsToDelete.clear();
 	}
 
 	public boolean isInBorders(Vector2f position) {
 		if (position.x < 0 || position.x > widthLevel || position.y < 0
 				|| position.y > heightLevel)
 			return false;
 		else
 			return true;
 	}
 
 	public World getWorld() {
 		return world;
 	}
 
 	public void clearWorld() {
 		for (GameObject go : gameObjects) {
 			go.destroy();
 		}
 		gameObjects.clear();
 	}
 
 	public float getGravity() {
 		return gravity;
 	}
     public Vector2f getMousePosition(){
         return game.getPositionMouse();
     }
 }
