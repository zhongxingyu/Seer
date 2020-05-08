 package bar.kisskiss.holden.model;
 
 import bar.kisskiss.holden.model.actors.Friend;
 import bar.kisskiss.holden.model.actors.Holden;
 import bar.kisskiss.holden.model.general.InteractObject;
 
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 
 public class World {
 
 	/** The blocks making up the world **/
 	Array<InteractObject> interactObject = new Array<InteractObject>();
 	/** Our player controlled hero **/
 	Holden holden;
 	Friend friend;
 	Target target;
 	Camera camera;
	Level level;
	
 	public Camera getCamera() {
 		return camera;
 	}
 
 	Rectangle drawableArea = new Rectangle();
 	Array<InteractObject> drawableObjects = new Array<InteractObject>();
 	
 	 public Rectangle getDrawableArea() {
 		return drawableArea;
 	}
 
 	public void setDrawableArea(Rectangle drawableArea) {
 		this.drawableArea = drawableArea;		
 		// AK TODO: TODO:!
 		level.getBlocksInRect( drawableArea, drawableObjects );
 		
 	}
 	
 
 	public void setDrawableObjects(Array<InteractObject> drawableObjects) {
 		// AK TODO: TODO:!
 		//this.drawableObjects = drawableObjects;
 	}
 	
 	public Array<Rectangle> getCollisionRects() {
 		return collisionRects;
 	}
 
 
 	public Array<InteractObject> getInteractObject() {
 		return interactObject;
 	}
 
 
 	public Level getLevel() {
 		return level;
 	}
 
 	Array<Rectangle> collisionRects = new Array<Rectangle>();
 
 	 /** Return only the blocks that need to be drawn **/
 	public Array<InteractObject> getDrawableObjects() {		
 		return drawableObjects;
 	}
 	
 	
 	public Target getTarget() {
 		return target;
 	}
 
 	public Friend getFriend() {
 		return friend;
 	}
 
 	public Holden getHolden() {
 		return holden;
 	}
 
 	// --------------------
 
 	public World() {
 		createDemoWorld();
 	}
 
 	private void createDemoWorld() {
 		holden = new Holden(new Vector2(55, 25), 5);
 		friend = new Friend(new Vector2(20, 40), 10);
 		target = new Target(new Vector2(50, 35));		
 		level = new Level(1000, 500);
 		camera = new Camera(new OrthographicCamera(400f, 300f));		
 	}
 
 }
