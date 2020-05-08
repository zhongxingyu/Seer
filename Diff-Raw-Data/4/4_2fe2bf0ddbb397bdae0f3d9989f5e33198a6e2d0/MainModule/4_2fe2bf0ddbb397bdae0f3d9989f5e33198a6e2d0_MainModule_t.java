 package tendiwa.modules;
 
 import tendiwa.core.*;
 import tendiwa.drawing.*;
 import tendiwa.resources.CharacterTypes;
 
 import java.awt.*;
 
 import static tendiwa.core.DSL.*;
 import static tendiwa.core.World.create;
 
 public class MainModule extends Module implements WorldProvider {
 
 public MainModule() {
 	DefaultDrawingAlgorithms.register(EnhancedRectangle.class, DrawingRectangle.withColorLoop(Color.GRAY, Color.BLACK, Color.BLUE));
 	DefaultDrawingAlgorithms.register(RectangleSystem.class, DrawingRectangleSystem
 		.withColors(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW));
 	DefaultDrawingAlgorithms.register(
 		RectangleSidePiece.class,
 		DrawingRectangleSidePiece.withColor(Color.MAGENTA));
 	DefaultDrawingAlgorithms.register(Segment.class, DrawingSegment.withColor(Color.BLUE));
 	DefaultDrawingAlgorithms.register(Chunk.class, DrawingTerrain.defaultAlgorithm());
 	DefaultDrawingAlgorithms.register(World.class, DrawingWorld.defaultAlgorithm());
 
 	ResourcesRegistry.registerDrawer(new TestLocationDrawer());
 //	ResourcesRegistry.registerDrawer(new Forest());
 	ResourcesRegistry.registerDrawer(new Ocean());
 }
 
 public static void main(String[] args) {
 	MainModule mainModule = new MainModule();
 	TestCanvas canvas = canvas();
 	canvas.draw(mainModule.createWorld());
 }
 
 @Override
 public World createWorld() {
	World world = World.create(new SuseikaWorld(), 400, 300);
	world.setPlayerCharacter(new PlayerCharacter(world.getDefaultPlane(), 200, 150, "Suseika", CharacterTypes.human, "warrior"));
 	return world;
 }
 }
