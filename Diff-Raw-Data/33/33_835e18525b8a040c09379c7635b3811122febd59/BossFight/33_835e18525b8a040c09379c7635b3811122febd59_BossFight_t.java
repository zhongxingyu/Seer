 package demos;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import math.Vector2;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 
 import ability.BulletAbility;
 
 import com.google.common.collect.ImmutableList;
 
 import components.AnimationComp;
 import components.TransformationComp;
 
 import content.ContentManager;
 import content.EntityArchetypeLoader;
 
 import death.PPDeathAction;
 import debugsystems.DebugAttackRenderSystem;
 import debugsystems.DebugSpatialRenderSystem;
 import entityFramework.IEntity;
 import entityFramework.IEntityArchetype;
 import entityFramework.IEntityManager;
 import entitySystems.AnimationSystem;
 import entitySystems.AttackResolveSystem;
 import entitySystems.BasicAISystem;
 import entitySystems.CameraControlSystem;
 import entitySystems.CharacterControllerSystem;
 import entitySystems.CollisionSystem;
 import entitySystems.DeathResolveSystem;
 import entitySystems.DeathSystem;
 import entitySystems.ExistanceSystem;
 import entitySystems.HUDRenderingSystem;
 import entitySystems.HealthBarRenderSystem;
 import entitySystems.InputSystem;
 import entitySystems.MapCollisionSystem;
 import entitySystems.PhysicsSystem;
 import entitySystems.RegenSystem;
 import entitySystems.RenderingSystem;
 import entitySystems.TrixieAISystem;
 
 import gameMap.MapTester;
 import graphics.Animation;
 import graphics.Color;
 import graphics.Frame;
 import graphics.SpriteBatch;
 import tests.SystemTester;
 import utils.Camera2D;
 import utils.Circle;
 import utils.Rectangle;
 import utils.Timer;
 
 public class BossFight {
 	private SystemTester tester;
 	private SpriteBatch graphics;
 	private final Rectangle screenDim;
 	private final boolean isFullScreen;
 	private MapTester mapTester;
 	private Camera2D camera;
 	
 	public static void main(String[] args) throws IOException {
 		EntityArchetypeLoader.initialize();
 		new BossFight(new Rectangle(0,0,800,600), false).start();
 	}
 	
 	public BossFight(Rectangle screenDim, boolean fullScreen) {
 		this.screenDim = screenDim;
 		this.isFullScreen = fullScreen;
 	}
 	
 	public final void start() throws IOException  {
 		try {
 			Display.setDisplayMode(new DisplayMode(screenDim.Width,screenDim.Height));
 			Display.setFullscreen(isFullScreen);
 			Display.create();
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 		}
 		this.graphics = new SpriteBatch(this.screenDim);	
 		this.tester = new SystemTester();
 		this.mapTester = new MapTester(this.graphics);
 		this.camera = new Camera2D(new Rectangle(0,0,(int) (Display.getWidth()*1.5),(int) (Display.getHeight()*1.5)), screenDim);
 		System.out.print((Display.getHeight()*1.5) +"x"+ (int) (Display.getWidth()*1.5));
 		initializeSystems();
 		initializeEntities(this.tester.getWorld().getEntityManager());
 
 		tester.startTesting();
 
 		while(!Display.isCloseRequested()) {
 			tester.updateWorld(1.0f / 60f);
 
 			Timer.updateTimers(1f/60f);
 			
 			graphics.clearScreen(new Color(157, 150, 101, 255));
 			graphics.begin(null, camera.getTransformation());
 			mapTester.draw();
 			tester.renderWorld();
 
 			graphics.end();
 
 			tester.getWorld().getEntityManager().destoryKilledEntities();
 
 			if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
 				camera.move(new Vector2(5, 0));
 			} 
 			if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
 				camera.move(new Vector2(-5, 0));
 			} 
 			if(Keyboard.isKeyDown(Keyboard.KEY_UP)) {
 				camera.move(new Vector2(0, -5));
 			} 
 			if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
 				camera.move(new Vector2(0, 5));
 			} 
 			if(Keyboard.isKeyDown(Keyboard.KEY_Z)) {
 				camera.zoomIn(0.001f);
 			} 
 			if(Keyboard.isKeyDown(Keyboard.KEY_X)) {
 				camera.zoomOut(0.001f);
 			} 
 
 			Display.update();
 			Display.sync(60);
 		}
 
 		Display.destroy(); 
 	}
 	
 	public void initializeSystems() {
 		tester.addLogicSubSystem(new CharacterControllerSystem(this.tester.getWorld()));
 		tester.addLogicSubSystem(new InputSystem(this.tester.getWorld(), this.camera));
 		tester.addLogicSubSystem(new PhysicsSystem(this.tester.getWorld()));
 		tester.addLogicSubSystem(new CollisionSystem(this.tester.getWorld()));
 		tester.addLogicSubSystem(new CameraControlSystem(this.tester.getWorld(), camera));
 		tester.addLogicSubSystem(new AttackResolveSystem(this.tester.getWorld()));
 		tester.addLogicSubSystem(new RegenSystem(this.tester.getWorld(), 0.5f));
 		tester.addLogicSubSystem(new MapCollisionSystem(this.tester.getWorld(), new Vector2(this.camera.worldBounds.Width, this.camera.worldBounds.Height)));
 		tester.addLogicSubSystem(new TrixieAISystem(this.tester.getWorld()));
 		tester.addLogicSubSystem(new ExistanceSystem(this.tester.getWorld()));
 		tester.addLogicSubSystem(new BasicAISystem(this.tester.getWorld()));
 		tester.addRenderSubSystem(new HealthBarRenderSystem(this.tester.getWorld(), this.graphics));
 		tester.addRenderSubSystem(new AnimationSystem(this.tester.getWorld()));
 		tester.addLogicSubSystem(new DeathSystem(this.tester.getWorld()));
 		tester.addLogicSubSystem(new DeathResolveSystem(this.tester.getWorld()));
 		tester.addRenderSubSystem(new RenderingSystem(this.tester.getWorld(), this.graphics));
 		tester.addRenderSubSystem(new DebugAttackRenderSystem(this.tester.getWorld(), this.graphics));
 		tester.addRenderSubSystem(new DebugSpatialRenderSystem(this.tester.getWorld(),this.graphics));
 		tester.addRenderSubSystem(new HUDRenderingSystem(this.tester.getWorld(), this.graphics, "Player"));
 	}
 	
 	public void initializeEntities(IEntityManager manager) {
 		
 		//PLAYER
 		IEntityArchetype playerArch = ContentManager.loadArchetype("Player.archetype"); 
 		
 		IEntity player = manager.createEntity(playerArch);
 		
 		player.addToGroup("Friends");
 		player.addToGroup(CameraControlSystem.GROUP_NAME);
 		
 		player.setLabel("Player");
<<<<<<< HEAD
 
=======
		
>>>>>>> Sorta fixed the text drawing
 		player.getComponent(AnimationComp.class).getActiveAnimation().getTimer().Start();
 		player.refresh();
 		
 		//TRIXIE
 		IEntity trixie = manager.createEntity(ContentManager.loadArchetype("Trixie.archetype"));
 		trixie.addToGroup("Enemies");
 		trixie.getComponent(TransformationComp.class).setPosition(new Vector2(100,100));
 		trixie.setLabel("Trixie");
 		
 		trixie.getComponent(AnimationComp.class).getActiveAnimation().getTimer().Start();
 		
 		trixie.refresh();
 		
 		
 	}
 }
