 package ss.linearlogic.quizquest;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 
 import ss.linearlogic.quizquest.item.Key;
 import ss.linearlogic.quizquest.item.Potion;
 import ss.linearlogic.quizquest.item.Spell;
 import ss.linearlogic.quizquest.player.Inventory;
 import ss.linearlogic.quizquest.player.Player;
 import ss.linearlogic.quizquest.question.Question;
 import ss.linearlogic.quizquest.question.QuestionManager;
 
 import static org.lwjgl.opengl.GL11.*;
 
 public class QuizQuest {
 	
 	/**
 	 * Whether to repeat the {@link #mainLoop()}
 	 */
 	private static boolean running = true;
 	
 	/**
 	 * Whether to reset, rather than exit, after breaking out of the {@link #mainLoop()})
 	 */
 	private static boolean reset = true;
 	
 	/**
 	 * Width of the game window, in pixels
 	 */
 	private final int screen_width = 480;
 	
 	/**
 	 * Height of the game window, in pixels
 	 */
 	private final int screen_height = 480;
 		
 	/**
 	 * Game object constructor - loads the game window, runs the game (see {@link #mainLoop()}),
 	 * and then destroys the OpenGL context on exit (necessary for a game reset to be possible).
 	 */
 	public QuizQuest() {
 		initializeOpenGL(screen_width, screen_height);
 		mainLoop();
 		Textbox.reset();
 		destroyOpenGL();
 		if (reset) {
 			new QuizQuest();
 		}
 	}
 	
 	/**
 	 * Initialize the window with the provided width and height (in pixels), and loads the OpenGL context.
 	 * @param width Window width (pixels)
 	 * @param height Window height (pixels)
 	 */
 	public void initializeOpenGL(int width, int height) {
 		try {
 			Display.setDisplayMode(new DisplayMode(width, height));
 			Display.setTitle("QuizQuest");
 			Display.setVSyncEnabled(true);
 			Display.create();
 		} catch (LWJGLException ex) {
 			ex.printStackTrace();
 		}
 		
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		glOrtho(0, width, height, 0, -1, 1);
 		glMatrixMode(GL_MODELVIEW);
 		
 		glEnable(GL_BLEND);
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 		
 		glDisable(GL_DEPTH_TEST);
 		glShadeModel(GL_SMOOTH);
 		
 		glClearDepth(1);
 	}
 	
 	/**
 	 * Master loop to handle rendering and logic. Updates the game every frame, with a frame rate of 60 fps.
 	 */
 	public void mainLoop() {
 	
 		Map.initialize("map.txt", "enemies.txt");
 		Map.addTexture("Grass.png", 0);
 		Map.addTexture("Floor.png", 1);
 		Map.addTexture("Wall.png", 2);
 		Map.addTexture("Door.png", 3);
 		Map.addTexture("enemy.png", 4);
 
 		Player.initialize(200, 200, "Pedobear.png");
 		Player.setMaxHealth(100);
 		Player.setHealth(100);
 		Player.setLives(5);
 		
		Inventory.initialize(380, 20, 100, 370, 10, 10, 35, 10, 2, 8);
 		Inventory.addTexture("Key.png", 1);
 		Inventory.addTexture("Potion.png", 2);
 		Inventory.addTexture("Spell.png", 3);
 		
 		Textbox.initializeWithSystemFont();
 		QuestionManager.LoadQuestionsFile("questions.txt");
 		
 		while (running) {
 			if (Display.isCloseRequested() || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
 				if (reset)
 					reset = false;
 				break;
 			}
 			if (Keyboard.isKeyDown(Keyboard.KEY_F5)) {
 				if (!reset)
 					reset = true;
 				break;
 			}
 							
 			//Game rendering/logic area
 			glClear(GL_COLOR_BUFFER_BIT);
 			
 			Map.render();
 			Player.update();
 			Player.render();
 			
 			
 			Textbox.update();
 			Textbox.render();
 			
 			Inventory.update();
 
 			Display.update();
 			Display.sync(60); //The value n in sync(n) is the frame rate
 		}
 	}
 	
 	/**
 	 * Destroys the OpenGL context. Called on quit and reset.
 	 */
 	public void destroyOpenGL() {
 		Display.destroy();
 	}
 	
 	/**
 	 * Entry point - loads the game object.
 	 * @param args Ignore this...
 	 */
 	public static void main(String[] args) {
 		new QuizQuest();
 	}
 }
