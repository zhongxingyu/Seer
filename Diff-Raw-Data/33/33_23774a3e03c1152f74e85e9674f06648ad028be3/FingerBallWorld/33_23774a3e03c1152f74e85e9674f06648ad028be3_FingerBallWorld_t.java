 package beothorn.labs.core;
 
 import static playn.core.PlayN.graphics;
 
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.World;
 
 import playn.core.Graphics;
 import beothorn.fingerball.DimensionMeters;
 import beothorn.fingerball.DimensionPixels;
 import beothorn.fingerball.MetersToPixels;
 
 public class FingerBallWorld {
 
 	private static final DimensionMeters worldDimension = new DimensionMeters(2.31f,1.73f);
 	private static final Vec2 gravity = new Vec2(0.0f, 0.05f);
 	private World world;
 	private final MetersToPixels metersToPixels;
 	private final Graphics graphics;
 	private Ball ball;
 
 	public FingerBallWorld() {
 		this.graphics = graphics();
 		DimensionPixels screenDimensions = new DimensionPixels(graphics.width(), graphics.height());
 		this.metersToPixels = new MetersToPixels(screenDimensions, worldDimension);
 		
 		createWorld();
 		createBoundaries();
 		createBall();
 	}
 
 	private void createBall() {
 		ball = new Ball(world, metersToPixels, 1, 1);
 	}
 
 	private void createWorld() {
 		boolean doNotSimulateInactiveBodies = true;
 		world = new World(gravity, doNotSimulateInactiveBodies);
 		world.setWarmStarting(true);
 		world.setAutoClearForces(true);
 	}
 
 	private void createBoundaries() {
		createCeiling();
		createFloor();
		createLeftWall();
		createRightWall();
	}

	private void createFloor() {
 		createWall(new Vec2(0, worldDimension.height), new Vec2(worldDimension.width, worldDimension.height));
	}

	private void createRightWall() {
		createWall(new Vec2(worldDimension.width, 0), new Vec2(worldDimension.width, worldDimension.height));
	}

	private void createLeftWall() {
 		createWall(new Vec2(0, 0), new Vec2(0, worldDimension.height));
	}

	private void createCeiling() {
 		createWall(new Vec2(0, 0), new Vec2(worldDimension.width, 0));
 	}
 
 	private void createWall(Vec2 start, Vec2 end) {
 		Body ground = world.createBody(new BodyDef());
 		PolygonShape groundShape = new PolygonShape();
 		groundShape.setAsEdge(start, end);
 		ground.createFixture(groundShape, 0.0f);
 	}
 
 	public void update() {
 		world.step(0.33f, 10, 10);
 		ball.update();
 	}
 }
