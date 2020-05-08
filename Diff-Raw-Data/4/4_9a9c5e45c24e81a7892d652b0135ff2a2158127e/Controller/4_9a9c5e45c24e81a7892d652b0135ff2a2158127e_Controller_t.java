 /*
  * The thing that controls everything....
  */
 package controller;
 
 import importing.Parser;
 import importing.XGL_Parser;
 import input.Input;
 
 import javax.vecmath.Vector3f;
 
 import physics.Physics;
 
 import com.bulletphysics.collision.shapes.BoxShape;
 import com.bulletphysics.collision.shapes.CollisionShape;
 import com.bulletphysics.linearmath.DefaultMotionState;
 
 import entity.Camera;
 import entity.Entity;
 import entity.EntityList;
 import render.Renderer;
 
 public class Controller {
 	// the game always runs (except when it doesn't)
 	private static boolean isRunning = true;
 	
 	private Renderer renderer;
 	private Physics physics;
 	
 	private long frames = 0;
 	private Input input;
 
 	private EntityList objectList;
 	
 	public static void main(String[] args) throws Exception {
 		new Controller();
 	}
 
 	public Controller() throws Exception {
 		start();
 		
 		loadLevel();
 	}
 
 	
 	/* Diagram of dependencies (:D).  Arrows mean "depends on"
 	 * 
 	 * 						/------(Renderer)
 	 * 						|			^
 	 * 						V			|
 	 * (Physics) <-- (Entity List)		|
 	 * 						^			|
 	 * 						|			|
 	 * 						\------(Input)
 	 */
 	private void start() {
 		//Instantiate Physics first, as it depends on nothing
 		physics = new Physics();
 		physics_thread.start();
 		
 		//Next is the entity list, since it only depends on the physics
 		objectList = new EntityList(physics);
 		
 		//Renderer has to be after entity list
 		renderer = new Renderer(objectList);
 		render_thread.start();
 		
 		//Input has to be after entity list and after the render thread has been started (Display must be created)
 		input = new Input(objectList);
 		input_thread.start();
 	}
 	
 	/* THREAD DEFINITIONS */
 	// Create the Input Listening thread
 	Thread input_thread = new Thread() {
 		public void run() {
 			//Wait for display to be created
 			try {
 				render_thread.join();
 			} catch (InterruptedException e) {/*Nothing to do, render thread is telling us its done creating display*/}
 			input.init();
 			while(isRunning){
 				input.run();
 			}
 		}
 	};
 	// Create the Physics Listening thread
 	Thread physics_thread = new Thread() {
 		public void run() {
 			while (isRunning) {
 				physics.clientUpdate();
 			}
 		}
 	};
 
 	// Create the vidya thread
 	Thread render_thread = new Thread() {
 		public void run() {
 			renderer.initGL();
 			input_thread.interrupt(); //If input thread is waiting (it should be) let it go
 			while (isRunning) {
 				renderer.draw();
 			}
 		}
 	};
 	
 	public long getFrames() { return frames; }
 	public void resetFrames() {	frames = 0;	}
 	
 	public static void quit() { isRunning = false;	}
 	
 	public void loadLevel() throws Exception{
 		Entity ent;
 		Camera cam;
 
 		//Physics.getInstance().getDynamicsWorld().setGravity(new Vector3f(0.0f,-10.0f,0.0f));
 		
 		Parser p = new XGL_Parser();
 		try{
 			//p.readFile("./lib/legoman.xgl");
			p.readFile("./lib/10010260.xgl");
			//p.readFile("./lib/box2.xgl");
 			//p.readFile("./lib/cath.xgl");
 		}catch(Exception e){
 			//TODO:  What to do here?
 		}
 		
 		//Make a camera
 		CollisionShape boxShape = new BoxShape(new Vector3f(1, 1, 1));
 		cam = new Camera(0.0f, new DefaultMotionState(), boxShape, false);
 		//ent.setLinearVelocity(new Vector3f(10,10,10));
 		
 		objectList.addItem(cam);
 		//ent.setGravity(new Vector3f(0.0f, 0.0f, 0.0f));
 		
 		//Make a cathode
 		boxShape = new BoxShape(new Vector3f(1, 1, 1));
 		ent = new Entity(1.0f, new DefaultMotionState(), boxShape, false);
 		ent.setModel(p.createModel());
 		ent.setPosition(new Vector3f(0.0f,0.0f,-20.0f));
 		objectList.addItem(ent);
 		
 		ent.applyImpulse(new Vector3f(0,0,4), new Vector3f(0,0,1));
 		
 		//Make a green box thing
 		try{
 			//p.readFile("./lib/legoman.xgl");
 			//p.readFile("./lib/10010260.xgl");
 			p.readFile("./lib/box2.xgl");
 			//p.readFile("./lib/cath.xgl");
 		}catch(Exception e){
 			//TODO:  What to do here?
 		}
 		
 		boxShape = new BoxShape(new Vector3f(1, 1, 1));
 		ent = new Entity(1.0f, new DefaultMotionState(), boxShape, false);
 		ent.setModel(p.createModel());
 		ent.setPosition(new Vector3f(0.0f,0.0f,0.0f));
 		objectList.addItem(ent);
 		physics.reduceHull(ent);
 		
 		cam.setDistance(25.0f);
 		cam.focusOn(ent);
 		
 		ent.applyImpulse(new Vector3f(0,0,-4), new Vector3f(0,0,-1));
 	}
 }
