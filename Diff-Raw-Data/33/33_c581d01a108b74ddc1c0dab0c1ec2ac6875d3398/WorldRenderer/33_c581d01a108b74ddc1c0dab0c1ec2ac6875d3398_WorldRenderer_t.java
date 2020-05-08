 package com.flipptor.orbgame;
 
 import box2dLight.PointLight;
 import box2dLight.RayHandler;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.physics.box2d.World;
 import com.flipptor.orbgame.definitions.PlayerBodyDef;
 import com.flipptor.orbgame.definitions.PlayerFixtureDef;
 
 public class WorldRenderer {
 	World world;
 	OrthographicCamera camera;
 	OrbGame game;
 	float width, height;
 	Box2DDebugRenderer renderer;
 	RayHandler handler;
 	PointLight light;
 	
 	EntityHandler entityHandler;
 	
 	Vector3 ppos;
 	PlayerEntity p;
 	
 	public WorldRenderer(OrbGame game) {
 		this.game = game;
 		
 		width = Gdx.graphics.getWidth()/10;
 		height = Gdx.graphics.getHeight()/10;
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 		
 		world = new World(new Vector2(0,0), false);
 		
 		//createPlayer();
 		entityHandler = new EntityHandler(world);
 		
 		camera = new OrthographicCamera(width, height);
 		camera.position.set(width*0.5f, height*0.5f, 0);
 		camera.update();
 		
 		renderer = new Box2DDebugRenderer();
 		handler = new RayHandler(world);
 		handler.setCombinedMatrix(camera.combined);
 		
 		//light = new PointLight(handler, 40000, Color.GRAY, 50, p.getBody().getPosition().x, p.getBody().getPosition().y);
 		PointLight l = new PointLight(handler, 40000, Color.GRAY, 50, width/2, height/2);
 		
 	}
 	
 	public void render() {
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		renderer.render(world, camera.combined);
 		renderer.setDrawBodies(true);
 		handler.render();
 		
 		//light.setPosition(p.body.getPosition());
 		
 		handler.update();
 		//p.update();
 		entityHandler.update();
 		if(Gdx.input.isKeyPressed(Keys.ESCAPE)){
 			Gdx.app.exit();
 		}
 		world.step(1/60f, 6, 2);
 	}
 	
 	public void createPlayer() {
 		p = new PlayerEntity(world, new PlayerBodyDef(new Vector2(width/2, height/2)));
		p.getBody().createFixture(PlayerFixtureDef.INSTANCE);
 	}
 }
