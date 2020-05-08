 package es.clementin.ludum;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 
 import com.jme3.math.Vector2f;
 
 enum GoalType {
 	BEAK,
 	HEIGHT,
 	HARDINESS
 };
 
 enum ShrubType {
 	INTRICATE,
 	TALL,
 	SPIKY
 };
 
 public class Main {
 	
 	static final int WORLD_WIDTH = 40;
 	static final int WORLD_HEIGHT = 20;
 	static final int TILE_SIZE = 32;
 
 	static final int WIDTH = WORLD_WIDTH * TILE_SIZE;
 	static final int HEIGHT = WORLD_HEIGHT * TILE_SIZE;
 	
 	static Textures textures;
 	
 	static Random rand = new Random();
 		
 	static ArrayList<Bird> birds;
 	static ArrayList<Bird> deadBirds;
 	static ArrayList<Bird> newBirds;
 	
 	static ArrayList<TallShrub> tallShrubs;
 	static ArrayList<SpikyShrub> spikyShrubs;
 	static ArrayList<IntricateShrub> intricateShrubs;
 	
 	static FontManager fm;
 	
 	static boolean gameOver;
 	static boolean started = false;
 	static boolean paused = false;
 	
 	static World world;
 	
 	final static String NAME = "Galapagos";
 	final static GoalType goalType = GoalType.values()[rand.nextInt(GoalType.values().length)];
 	final static int UI_HEIGHT = 100;
 	static String goalMessage;
 	static String currentMessage;
 	
 	static float currentBest = 1;
 	
 	static float targetHeight = 2;
 	static float targetBeakLength = 2;
 	static float targetHardiness = 2;
 	static float actualTarget;
 	
 	// The initial number of birds
 	static final int numBirds = 10;
 	
 	// The initial number of shrubs of each type
	static final int numShrubs = 10;
 	
 	static GrassTile selectedTile = null;
 	
 	static Inventory inventory;
 	
 	public static void main(String[] args){
 		
 		try {
 			Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT + UI_HEIGHT));
 			Display.setTitle(NAME);
 			Display.create();
 			Display.setVSyncEnabled(true);
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 		}
 		
 		
 		switch(goalType){
 			case BEAK:
 				goalMessage = String.format("Goal: get a bird with a beak over %sm long", targetBeakLength);
 				currentMessage = "Currently the longest beak is %sm";
 				actualTarget = targetBeakLength;
 				break;
 			case HEIGHT:
 				goalMessage = String.format("Goal: get a bird over %sm tall", targetHeight);
 				currentMessage = "Currently the tallest bird is %sm tall";
 				actualTarget = targetHeight;
 				break;
 			case HARDINESS:
 				goalMessage = String.format("Goal: get a bird with a hardiness of %s", targetHardiness);
 				currentMessage = "Currently the most hardy bird has a hardiness rating of %s";
 				actualTarget = targetHardiness;
 				break;
 		}
 		
 		textures = new Textures();
 
 		GL11.glClearDepth(10f);
 	    GL11.glClearColor(0.f, 0.f, 0.f, 1.0f);
 		
 		GL11.glMatrixMode(GL11.GL_PROJECTION);
 		GL11.glLoadIdentity();
 		GL11.glOrtho(0, WIDTH, HEIGHT + UI_HEIGHT, 0, 1, -1);
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 
 		GL11.glEnable(GL11.GL_BLEND);
 		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 
 		GL11.glViewport(0, 0, WIDTH, HEIGHT + UI_HEIGHT);
 		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
 		
 		world = new World();
 		
 		init();
 		
 		fm = new FontManager();
 		
 		while(!Display.isCloseRequested()){
 			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 			GL11.glLoadIdentity();
 			
 			Input.check();
 			
 			if(!started){
 				fm.drawTitle();
 				fm.drawTutorial();
 				fm.drawStart();
 			}
 			else {
 				if(!gameOver && !paused){
 					logic();
 					render();
 				}
 			
 			}
 			
 			Display.update();
 			Display.sync(60);
 		}
 		Display.destroy();
 		System.exit(0);
 	}
 	
 	public static void logic(){
 		deadBirds.clear();
 		newBirds.clear();
 		
 		for(Bird b:birds){
 			b.age++;
 			
 			// If the bird is eating then call the eat method...
 			if(b.eating){
 				b.eatFood();
 			}
 			// ...otherwise it's free to move
 			
 			else {
 				// If it's already got a destination then move along the path to get there...
 				if(b.pathfinding){
 					b.animate();
 				}
 				// ...otherwise give it a new destination...
 				else {
 					// ...if it's hungry enough to warrant one
 					if(b.fullness < 0.5f){
 						b.findFood();
 						b.moveToFood();
 					}
 				}
 			}
 			
 			
 			b.spawnOffspring();
 			
 			b.getHungry();
 			
 		}
 		
 		for(Bird b:deadBirds){
 			birds.remove(b);
 		}
 		for(Bird b:newBirds){
 			birds.add(b);
 		}
 	}
 	
 	public static void render(){
 		if(birds.size() == 0){
 			gameOver = true;
 			fm.drawLose();
 			fm.drawRestart();
 		}
 		
 		if(currentBest >= actualTarget){
 			gameOver = true;
 			fm.drawWin();
 			fm.drawRestart();
 		}
 		
 		world.draw();
 		
 		if(selectedTile != null){
 			selectedTile.drawBorder();
 			selectedTile.draw();
 		}
 		
 		for(Bird b:birds){
 			b.draw();
 		}
 		
 		inventory.draw();
 		
 		fm.drawGoal();			
 	}
 	
 	public static void init(){
 		gameOver = false;
 		
 		inventory = new Inventory();
 		
 		birds = new ArrayList<Bird>();
 		deadBirds = new ArrayList<Bird>();
 		newBirds = new ArrayList<Bird>();
 
 		tallShrubs = new ArrayList<TallShrub>();
 		spikyShrubs = new ArrayList<SpikyShrub>();
 		intricateShrubs = new ArrayList<IntricateShrub>();
 		
 		for(int i = 0; i < numBirds; i++){
 			Vector2f position = world.freeSpaces.get(rand.nextInt(world.freeSpaces.size()));
 			birds.add(new Bird((int) position.x, (int) position.y));
 			world.freeSpaces.remove(position);
 		}
 		
 		for(int i = 0; i < 3; i++){
 			for(int j = 0; j < numShrubs; j++){
 				Vector2f position = world.freeSpaces.get(rand.nextInt(world.freeSpaces.size()));
 				GrassTile t = (GrassTile) world.tiles[(int) position.y][(int) position.x];
 				Shrub s = null;
 				switch(i){
 					case 0:
 						s = new TallShrub((int) position.x, (int) position.y);
 						tallShrubs.add((TallShrub) s);
 						t.shrubType = ShrubType.TALL;
 						break;
 					case 1:
 						s = new IntricateShrub((int) position.x, (int) position.y);
 						intricateShrubs.add((IntricateShrub) s);
 						t.shrubType = ShrubType.INTRICATE;
 						break;
 					case 2:
 						s = new SpikyShrub((int) position.x, (int) position.y);
 						spikyShrubs.add((SpikyShrub) s);
 						t.shrubType = ShrubType.SPIKY;
 						break;
 				}
 				
 				t.shrub = s;
 				world.freeSpaces.remove(position);
 			}
 		}
 	}
 }
