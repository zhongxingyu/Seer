 package org.frankversnel.nl.poortjes;
 
 import java.io.FileNotFoundException;
 import java.util.Random;
 
 import org.frankversnel.nl.poortjes.collision.CollisionLevel;
 import org.frankversnel.nl.poortjes.dummy.CandyInstance;
 import org.frankversnel.nl.poortjes.dummy.PlayerWithShape;
 import org.frankversnel.nl.poortjes.gameloop.DefaultGameLoop;
 import org.frankversnel.nl.poortjes.gameloop.GameLoop;
 import org.frankversnel.nl.poortjes.input.Keyboard;
 import org.frankversnel.nl.poortjes.rendering.Processing2DRenderer;
 import org.frankversnel.nl.poortjes.rendering.RenderingManager;
 import org.frankversnel.nl.poortjes.resourceloading.ProcessingShapeLoader;
 
 import processing.core.PApplet;
 
 public class Poortjes extends PApplet {
 	private static final long serialVersionUID = 9178782595328986939L;
 
	private static final int SCREEN_WIDTH = 400;
	private static final int SCREEN_HEIGHT = 400;
 	private static final int BACKGROUND_COLOR = 0;
 
 	private RenderingManager renderManager;
 
 	private GameLoop gameLoop;
 
 	@Override
 	public void setup() {
 		size(SCREEN_WIDTH, SCREEN_HEIGHT);
 		background(BACKGROUND_COLOR);
 		smooth();
 
 		ProcessingShapeLoader shapeLoader = new ProcessingShapeLoader(this);
 		String playerShapeId;
 		String candyId;
 		try {
 			playerShapeId = shapeLoader.load("resources/ship.svg");
 			candyId = shapeLoader.load("resources/candy.svg");
 		} catch (FileNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 
 		renderManager = new RenderingManager(new Processing2DRenderer(g,
 				shapeLoader));
 
 		gameLoop = new DefaultGameLoop();
 		gameLoop.start();
 		
 		CollisionLevel candy = new CollisionLevel(gameLoop);
 		CollisionLevel players = new CollisionLevel(gameLoop, candy);
 
 		GameObject player = new PlayerWithShape(renderManager,
				players, gameLoop, SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, playerShapeId);
 		this.addKeyListener(new Keyboard(player, 'w', 's', 'a', 'd'));
 		
		for(int i = 0; i < 50; i++) {
 			int width = new Random().nextInt(SCREEN_WIDTH);
 			int height = new Random().nextInt(SCREEN_HEIGHT);
 			new CandyInstance(renderManager, candy, gameLoop, width, height,
 					candyId);
 		}
 
		GameObject otherPlayer = new PlayerWithShape(renderManager, players, gameLoop, 300,
 				300, playerShapeId);
		//new EnemyAnimation(otherPlayer, gameLoop);
 	}
 
 	@Override
 	public void draw() {
 		clearScreen();
 		renderManager.processComponents();
 	}
 
 	private void clearScreen() {
 		background(BACKGROUND_COLOR);
 	}
 
 	public static void main(String args[]) {
 		PApplet.main(new String[] { "--present",
 				"org.frankversnel.nl.poortjes.Poortjes" });
 	}
 
 }
