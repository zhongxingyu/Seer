 package frankversnel.processing;
 
 import java.io.FileNotFoundException;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 import frankversnel.processing.collision.CollisionManager;
 import frankversnel.processing.dummy.PlayerWithCircle;
 import frankversnel.processing.dummy.PlayerWithShape;
 import frankversnel.processing.gameloop.DefaultGameLoop;
 import frankversnel.processing.gameloop.GameLoop;
 import frankversnel.processing.rendering.Processing2DRenderer;
 import frankversnel.processing.rendering.RenderingManager;
 import frankversnel.processing.resourceloading.ProcessingShapeLoader;
 
 import processing.core.*;
 
 public class Poortjes extends PApplet {
 	/**
 	 *
 	 */
 	private static final long serialVersionUID = 9178782595328986939L;
 	
	private static final Logger logger = LoggerFactory.getLogger(Poortjes.class);

 	private static final int SCREEN_WIDTH = 400;
 	private static final int SCREEN_HEIGHT = 400;
 	private static final int BACKGROUND_COLOR = 0;
 	
 	private RenderingManager renderManager;
 	private CollisionManager collisionManager;
 	
 	private GameLoop gameLoop;
 	
 	public void setup() {
 		size(SCREEN_WIDTH, SCREEN_HEIGHT);
 	    background(BACKGROUND_COLOR);
 	    smooth();
 	    
 	    ProcessingShapeLoader shapeLoader = new ProcessingShapeLoader(this);
 	    String shapeId;
 	    try {
			shapeId  = shapeLoader.load("drawing.svg");
 		} catch (FileNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 	    
 	    renderManager = new RenderingManager(new Processing2DRenderer(g, shapeLoader));
 	    
 	    collisionManager = new CollisionManager();
 	    
 	    gameLoop = new DefaultGameLoop();
 	    gameLoop.addActionListener(collisionManager);
 	    gameLoop.start();
 	    
 	    new PlayerWithCircle(renderManager, gameLoop);
 	    new PlayerWithShape(renderManager, gameLoop, shapeId);
     }
 
     public void draw() {
     	clearScreen();
     	renderManager.processComponents();
     }
 
     private void clearScreen() {
     	background(BACKGROUND_COLOR);
     }
 
     public static void main(String args[]) {
     	PApplet.main(new String[] { "--present", "frankversnel.processing.Poortjes" });
     }
 
 }
