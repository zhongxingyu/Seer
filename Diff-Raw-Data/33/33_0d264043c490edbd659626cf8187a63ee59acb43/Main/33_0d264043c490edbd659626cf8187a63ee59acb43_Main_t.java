 package test;
 
 import javax.vecmath.Vector3f;
 
 import engine.Engine;
 import engine.entity.*;
 import engine.stars.Stars;
 import engine.terrain.Terrain;
 
 public class Main {
 
 	private Engine engine;
 
 	private Player player;
 	private Entity model;
 	private Entity model2;
 	private Entity model3;
 	private Entity model4;
 	private Entity model5;
 	private Camera camera;
 
 	public static void main(String args[]) {
 		boolean multiThreaded = false;
 		
 		Main m = new Main();
 		if(multiThreaded == true)
 			m.runMultiThread();
 		else
 			m.runSingleThread();
 	}
 
 	public Main() {
 		engine = new Engine();
 		
 		engine.addModel("box", "resources/models/misc/box.xgl");
 		engine.addModel("box2", "resources/models/misc/box2.xgl");
 		engine.addModel("test", "resources/models/misc/test.xgl");
 		engine.addModel("export", "resources/models/misc/export.xgl");
 		engine.addModel("singlebox", "resources/models/misc/singlebox.xgl");
 		engine.addModel("legoman", "resources/models/misc/legoman.xgl");
 		
 		Terrain terrain = new Terrain(engine);
 		terrain.createTerrain(10);
 		terrain.setPosition(new Vector3f(0,-10, 0));
 		
 		//Make some parallax stars
 		new Stars(engine,1000,10000,200,5,400);
 		
 		engine.addPlayer("player", 1.0f, 0.5f, "box", "default");
 		player = (Player) engine.getEntity("player");
 		player.setPosition(new Vector3f(5, 3, 0));
 		player.setFallSpeed(0);
 		player.setGravity(0);
 		
 		model = engine.addEntity("model1", 1.0f, true, "box", "default");
 		model.setProperty(Entity.NAME, "model1");
 		model.setPosition(new Vector3f(6, 0, 0));
 
 		model2 = engine.addEntity("model2", 1f, true, "export", "default");
 		model2.setPosition(new Vector3f(-6, 0, 0));
 		model2.applyImpulse(new Vector3f(2,0,0), new Vector3f(0,0,0));
 		
 		model3 = engine.addEntity("model3", 1f, false, "singlebox", "default");
 		model3.setPosition(new Vector3f(0, 0, 0));
 		model3.addCollisionFunctions("explode");
 		//model3.getModel().setTransparent();
 
 		model4 = engine.addEntity("model4", 1f, true, "test", "default");
 		model4.setProperty(Entity.NAME, "model4");
 		model4.setPosition(new Vector3f(0, 0, -6));
 		
 		model5 = engine.addEntity("model5", 1f, true, "legoman", "default");
 		model5.setProperty(Entity.NAME, "model5");
 		model5.setPosition(new Vector3f(0, -10, -15));
 		model5.setScale(new Vector3f(0.1f,0.1f,0.1f));
 				
 		camera = engine.addCamera(1f, false, "box2");
 		camera.setDistance(40f);
 		camera.setPosition(new Vector3f(0, 0, 6));
 		camera.focusOn(player);
 		
		System.out.println("Num Entities: " + engine.getEntityList().getEntities().size());
 	}
 
 	public void runMultiThread() {
 		engine.run();
 	}
 	
 	public void runSingleThread() {
 		while(true){
 			engine.renderOnce();
 			engine.physicsOnce();
 		}
 	}
 }
