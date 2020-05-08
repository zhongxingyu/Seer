 package tests;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import math.Vector2;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 
 import ability.Abilities;
 import ability.AbilityInfo;
 
 import com.google.common.collect.ImmutableList;
 import components.*;
 
 import content.ContentManager;
 import death.DeathResolveSystem;
 import debugsystems.DebugAttackRenderSystem;
 import debugsystems.DebugRadiationRenderSystem;
 import debugsystems.DebugSpatialRenderSystem;
 import entityFramework.IEntity;
 import entityFramework.IEntityManager;
 import entitySystems.*;
 
 import scripting.PlayerScript;
 import utils.Camera2D;
 import utils.Circle;
 import utils.Rectangle;
 import utils.Timer;
 
 import gameMap.*;
 import graphics.*;
 
 public class RadiationTest {
 
 	private SystemTester tester;
 	private SpriteBatch graphics;
 	private final Rectangle screenDim;
 	private final boolean isFullScreen;
 	private MapTester mapTester;
 	private Camera2D camera;
 
 	public static void main(String[] args) throws IOException, LWJGLException {
 		new RadiationTest(new Rectangle(0, 0, 800, 600), true).start();
 	}
 
 	public RadiationTest(Rectangle screenDim, boolean fullScreen)
 			throws IOException {
 		this.screenDim = screenDim;
 		this.isFullScreen = fullScreen;
 	}
 
 	public final void start() throws IOException {
 		try {
 			Display.setDisplayMode(new DisplayMode(screenDim.Width,
 					screenDim.Height));
 			Display.setFullscreen(isFullScreen);
 			Display.create();
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 		}
 		this.graphics = new SpriteBatch(this.screenDim);
 		this.tester = new SystemTester();
 		this.mapTester = new MapTester(this.graphics);
 		this.camera = new Camera2D(new Rectangle(0, 0, 10000, 10000), screenDim);
 		initializeSystems();
 		initializeEntities(this.tester.getWorld().getEntityManager());
 
 		tester.startTesting();
 
 		while (!Display.isCloseRequested()) {
 			tester.updateWorld(1.0f / 60f);
 
 			Timer.updateTimers(1f / 60f);
 
 			graphics.clearScreen(new Color(157, 150, 101, 255));
 			graphics.begin(null, camera.getTransformation());
 			mapTester.draw();
 			tester.renderWorld();
 
 			graphics.end();
 
 			tester.getWorld().getEntityManager().destoryKilledEntities();
 
 			if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
 				camera.move(new Vector2(5, 0));
 			}
 			if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
 				camera.move(new Vector2(-5, 0));
 			}
 			if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
 				camera.move(new Vector2(0, -5));
 			}
 			if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
 				camera.move(new Vector2(0, 5));
 			}
 			if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
 				camera.zoomIn(0.001f);
 			}
 
 			Display.update();
 			Display.sync(60);
 		}
 
 		Display.destroy();
 	}
 
 	public void initializeSystems() {
 		tester.addLogicSubSystem(new RadiationSystem(this.tester.getWorld()));
 		tester.addLogicSubSystem(new InputSystem(this.tester.getWorld(),
 				this.camera));
 		tester.addLogicSubSystem(new ScriptSystem(this.tester.getWorld()));
 		tester.addLogicSubSystem(new PhysicsSystem(this.tester.getWorld()));
 		tester.addLogicSubSystem(new CollisionSystem(this.tester.getWorld()));
 		tester.addLogicSubSystem(new CameraControlSystem(
 				this.tester.getWorld(), camera));
 		tester.addLogicSubSystem(new AttackResolveSystem(this.tester.getWorld()));
 		tester.addLogicSubSystem(new RegenSystem(this.tester.getWorld(), 0.5f));
 		tester.addLogicSubSystem(new ExistanceSystem(this.tester.getWorld()));
 		tester.addRenderSubSystem(new HealthBarRenderSystem(this.tester
 				.getWorld(), this.graphics));
 		tester.addLogicSubSystem(new DeathSystem(this.tester.getWorld()));
 		tester.addLogicSubSystem(new DeathResolveSystem(this.tester.getWorld()));
 		tester.addRenderSubSystem(new RenderingSystem(this.tester.getWorld(),
 				this.graphics));
 		tester.addRenderSubSystem(new DebugAttackRenderSystem(this.tester
 				.getWorld(), this.graphics));
 		tester.addRenderSubSystem(new DebugSpatialRenderSystem(this.tester
 				.getWorld(), this.graphics));
 		tester.addRenderSubSystem(new HUDRenderingSystem(
 				this.tester.getWorld(), this.graphics, "Player"));
 		tester.addRenderSubSystem(new DebugRadiationRenderSystem(this.tester.getWorld(), this.graphics));
 	}
 
 	public void initializeEntities(IEntityManager manager) {
 
 		IEntity player = manager.createEmptyEntity();
 		player.addToGroup("Enemies");
 		player.addToGroup(CameraControlSystem.GROUP_NAME);
 
 		player.setLabel("Player");
 		PhysicsComp physComp = new PhysicsComp();
 		InputComp inpComp = new InputComp();
 		TransformationComp posComp = new TransformationComp();
 		posComp.setPosition(new Vector2(0,0));
 		SpatialComp spatComp = new SpatialComp(new Circle(Vector2.Zero, 30f), false);
 
 		HealthComp healthComp = new HealthComp(100, 2, 89);
 		AbilityComp apComp = new AbilityComp();
 		DeathComp deathComp = new DeathComp();
 
 		WeaponComp weaponComp = new WeaponComp();
 		weaponComp.setPrimaryAbility(new AbilityInfo(Abilities.None));
 
 		ImmutableList<Frame> frames = Frame.generateFrames(new Vector2(0, 0),
 				new Vector2(83 + 1.0f / 3.0f, 75), 24, true);
 		Animation animation = new Animation(frames, 0.1f);
 		Map<String, Animation> animations = new HashMap<String, Animation>();
 		animations.put("walk", animation);
 		BehaviourComp scriptComp = new BehaviourComp(new PlayerScript());
 
 		// rendcomp
 		RenderingComp rendComp = new RenderingComp();
 		rendComp.setColor(Color.White);
 		rendComp.setTexture(ContentManager
 				.loadTexture("pinkiewalkweaponspriteSCALED.png"));
 
 		player.addComponent(rendComp);
 		player.addComponent(inpComp);
 		player.addComponent(new StatusComp());
 		player.addComponent(physComp);
 		player.addComponent(posComp);
 		player.addComponent(spatComp);
 		player.addComponent(healthComp);
 		player.addComponent(apComp);
 		player.addComponent(deathComp);
 		player.addComponent(weaponComp);
 		player.addComponent(scriptComp);
 		player.refresh();
 		
 		//RADIATING BARREL
 		IEntity radEntity = manager.createEmptyEntity();
 		radEntity.setLabel("Radiating barrel");
 		
 		RenderingComp radRendComp = new RenderingComp();
 		radRendComp.setColor(Color.White);
 		radRendComp.setTexture(ContentManager.loadTexture("barrels.png"));
 		
 		TransformationComp radPosComp = new TransformationComp();
 		radPosComp.setPosition(new Vector2(Display.getHeight()/2, Display.getWidth()/2));
 		radPosComp.setOrigin(radRendComp.getTexture().getBounds().getCenter());
 		radPosComp.setScale(0.5f, 0.5f);
		
 		SpatialComp radSpatComp = new SpatialComp(new Circle(Vector2.Zero, 95f), false);

 		PhysicsComp radPhysComp = new PhysicsComp();
 		radPhysComp.setMass(250f);
 		
 		int radLevel = 3;
 		RadiationComp radComp = new RadiationComp(new Circle(Vector2.Zero, radLevel*radSpatComp.getBounds().getRadius()), radLevel);
 		
 		radEntity.addComponent(radRendComp);
 		radEntity.addComponent(radComp);
 		radEntity.addComponent(radPosComp);
 		radEntity.addComponent(radSpatComp);
 		radEntity.addComponent(radPhysComp);
 		radEntity.refresh();
 	}
 }
