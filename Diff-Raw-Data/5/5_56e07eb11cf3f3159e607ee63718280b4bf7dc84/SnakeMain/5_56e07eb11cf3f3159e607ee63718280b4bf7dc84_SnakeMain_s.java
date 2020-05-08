 import java.awt.Graphics;
 import java.util.Random;
 
 import core.*;
 
 public class SnakeMain {
 	public static DirectionalController controller;
 	public static Renderer renderer;
 
 	static int FPS, TPF;
 	
 	public static Snake[] snakes;
 	public static int snakeCount;
 	private static boolean running = true;
 	public static void main(String[] args) {
 		int mapWidth = 100;
 		int mapHeight = 75;
 		
 		for (int i = 0; i < args.length - 1; i++) {
 			if (args[i].equals("-w")) {
 				mapWidth = Integer.parseInt(args[++i]);
 			} else if (args[i].equals("-h")) {
 				mapHeight = Integer.parseInt(args[++i]);
 			}
 		}
 		
 		Init(mapWidth, mapHeight, 2);
 		while (true) {
 			initLevel();
 			run();
 		}
 	}
 	
 	
 	public static void Init(int width, int height, int snakeCount) {
 		int tileWidth = 8;
 		
 		controller = new DirectionalController(2);
 		GameFrame.Init(width * tileWidth, height * tileWidth, controller);
 
 		//renderer = new VortexRenderer(width, height, tileWidth, 127);
 		renderer = new SolidColorRenderer(width, height, tileWidth, 127);
 
 		Map.Init(width, height);
 		
 		SnakeMain.snakeCount = snakeCount;
 		snakes = new Snake[snakeCount];
 		for (int i = 0; i < snakeCount; i++) {
 			snakes[i] = new Snake(1, 1, 10, i);
 		}
 		
 		FPS = 5;
 		TPF = 1000 / FPS;
 	}
 	
 	static int frameCount;
 	
 	public static void initLevel() {
 		
 	}
 	
 	public static void run() {
 		running = true;
 		
 		long t0 = System.currentTimeMillis();
 		long t1;
 		
 		while (running) {
			controller.keyReset();
 			for (Snake snake: snakes) {
 				snake.Update();
 			}
 			render();
 
 			t1 = System.currentTimeMillis();
 			long timePassed = t1 - t0;
 			t0 = t1;
 			frameCount++;
 			if (timePassed < TPF) {
 				try {
 					Thread.sleep(TPF - timePassed);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			} else {
 				t0 += TPF - timePassed;
 			}
 		}
 	}
 
         public static void render(){
 		renderer.clear();
 		for(Snake snake : snakes) {
 			snake.render(renderer, frameCount);
 		}
 		renderer.renderMap(Map.tiles, frameCount);
 		renderer.drawElement(4, 4, 1, (byte)(10));		
 		GameFrame.render(renderer.buffer);//render to screen
 	}
 	
 	public static void Collide(int x, int y, int idx, byte item) {
 		switch (item) {
 			case Map.WALL:
 			snakes[idx].Die();
 			case Map.SNAKE:
 			snakes[idx].Die();
 		}
 	}
 }
