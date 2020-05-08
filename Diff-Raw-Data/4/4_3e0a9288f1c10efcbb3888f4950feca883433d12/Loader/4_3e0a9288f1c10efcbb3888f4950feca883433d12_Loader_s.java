 package mta13438;
 
 import static org.lwjgl.opengl.GL11.GL_LINES;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glLineWidth;
 import static org.lwjgl.opengl.GL11.glVertex2i;
 
 import java.util.ArrayList;
 import org.lwjgl.Sys;
 import org.lwjgl.opengl.Display;
 
 public class Loader {
 
 	static Level tutorialLevel = new Level(new ArrayList<Room>(), 0, 0, 0);
 	private static Controls controls = new Controls();
 	private static Player player = new Player(new Point(140,310,10),0.2f,0.01f,10);
 	private static long lastFrame;
 	private static int delta = getDelta();
 	private static long lastFPS;
 	private static int fps;
 	private static int currentRoom;
 	private static boolean renderRoom = false;
 	private static boolean collision = false;
 	
	private static Sound walkSound = new Sound(SOUNDS.FOOTSTEP_STONE_01, player.getPos(), true);
	private static Sound walkWaterSound = new Sound(SOUNDS.FOOTSTEP_WATER, player.getPos(), true);
 	private static boolean playing = false;
 	
 	
 	public void start() {
 		DebugInterface.Initialize(800, 600); // Width and Length of display
 		Menu mainMenu = new Menu();
 		getDelta();
 		lastFPS = getTime();
 	}
 	// Loads the tutoral level. Rooms and obstacles are added to the level.
 	private static void loadTutorialLevel() {
 		tutorialLevel.addRoomList(new Room(10, 20, 20, new Point(0,5,0), new Point(10, 15, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(60, 50, 30, new Point(0,25,0), new Point(60, 5, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(50, 10, 20, new Point(0,5,0), new Point(50, 5, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(150, 90, 60, new Point(0,45,0), new Point(150, 65, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(110, 110, 40, new Point(0,55,0), new Point(110, 55, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(40, 10, 20, new Point(0,5,0), new Point(35, 0, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(10, 50, 20, new Point(5,50,0), new Point(5, 0, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(70, 70, 40, new Point(35,70,0), new Point(35,0, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(10, 20, 20, new Point(5,20,0), new Point(5, 0, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(50, 80, 40, new Point(25,80,0), new Point(25, 0, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(30, 20, 40, new Point(15,20,0), new Point(15, 20, 0), MATERIALS.ROCK));
 		tutorialLevel.getRoomList().get(3).addObsList(new Water(new Point(20, 20, 0), 20, 50, 0, MATERIALS.WATER));
 		tutorialLevel.getRoomList().get(4).addObsList(new Monster(new Point(40, 45, 0), 20, 20, 0, MATERIALS.ROCK));
 		tutorialLevel.getRoomList().get(7).addObsList(new Trap(new Point(20, 20, 0), 30, 30, 0, MATERIALS.ROCK));
 		tutorialLevel.autoLevelGenerator(new Point(10,300,0));
 	}
 	// Initiates the tutorial level
 	public static void playTutorialLevel(){
 		Display.destroy();
 		loadTutorialLevel();
 		DebugInterface.Initialize(800, 600); // Width and Length of display
 		DebugInterface.InitOpenGL(500,500); // Width and Length inside the display (Scaling of perspective here)
 	}
 	// Renders the tutorial level. 
 	public static void renderTutorialLevel(){
 		input();
 		collision = player.collisionCheck(tutorialLevel, currentRoom);
 
 		if(collision){
 			for (int i = 0; i < tutorialLevel.getRoomList().get(currentRoom).getObsList().size(); i++) {
 				if(player.getPos().getX() > tutorialLevel.getRoomList().get(currentRoom).getObsList().get(i).getPos().getX() && 
 						player.getPos().getX() < tutorialLevel.getRoomList().get(currentRoom).getObsList().get(i).getPos().getX() + tutorialLevel.getRoomList().get(currentRoom).getObsList().get(i).getDx()){
 					if(player.getPos().getY() > tutorialLevel.getRoomList().get(currentRoom).getObsList().get(i).getPos().getY() &&
 						player.getPos().getY() < tutorialLevel.getRoomList().get(currentRoom).getObsList().get(i).getPos().getY() + tutorialLevel.getRoomList().get(currentRoom).getObsList().get(i).getDy()){
 						tutorialLevel.getRoomList().get(currentRoom).getObsList().get(i).collision(player, tutorialLevel, currentRoom);
 					}
 				}
 			}
 		}
 		// Plays all the sounds from objects in the current room.
 		for (int i = 0; i < tutorialLevel.getRoomList().size(); i++){
 			if(tutorialLevel.getRoomList().get(i) != tutorialLevel.getRoomList().get(currentRoom)){
 				for(int j = 0; j < tutorialLevel.getRoomList().get(i).getObsList().size(); j++){
 					tutorialLevel.getRoomList().get(i).getObsList().get(j).getLoopSound().stop();
 				}
 			} 
 		}
 		for (int i = 0; i < tutorialLevel.getRoomList().get(currentRoom).getObsList().size(); i++) {
 			if (tutorialLevel.getRoomList().get(currentRoom).getObsList().get(i).getEmitSound() == true){
 				tutorialLevel.getRoomList().get(currentRoom).getObsList().get(i).getLoopSound().play();
 			}
 		}
 		player.setListener();
 		
 		//Draw the Tutorial Levels rooms
 		tutorialLevel.Draw();
 
 		//Draw the obs of every room
 		for (int i = 0; i < tutorialLevel.getRoomList().size(); i++) {
 			for (int j = 0; j < tutorialLevel.getRoomList().get(i).getObsList().size(); j++) {
 				tutorialLevel.getRoomList().get(i).getObsList().get(j).draw();
 			}
 		}
 
 		//Draw the player
 		player.draw();
 		updateFPS();
 		walkCheck(player);
 	}
 
 	public static void input() {
 
 		controls.takeInput();	
 
 		currentRoom = tutorialLevel.getCurrentRoom(player.getPos());
 		delta = getDelta();
 
 		if(controls.getKEY_UP()){
 			player.foward(delta/10, tutorialLevel, currentRoom);
 		}
 		if(controls.getKEY_DOWN()){
 			player.backward(delta/10, tutorialLevel, currentRoom);
 		}
 		if(controls.getKEY_LEFT()){
 			player.turnLeft(delta/10);
 		}
 		if(controls.getKEY_RIGHT()){
 			player.turnRight(delta/10);
 		}
 	}
 
 
 	public static int getDelta() {
 		long time = getTime();
 		int delta = (int) (time - lastFrame);
 		lastFrame = time;
 		return delta;
 	}
 
 	public static long getTime() {
 		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
 	}
 	
 	public static void updateFPS() {
 		if (getTime() - lastFPS > 1000) {
 			Display.setTitle("FPS: " + fps);
 			fps = 0;
 			lastFPS = getTime();
 		}
 		fps++;
 	}
 	public static void walkCheck(Player player){
 		System.out.println(player.isWalking());
 		if(player.isWalking() == true){
 			if(player.isInWater()==true){
 				walkSound.stop();
 				walkWaterSound.update(player.getPos());
 				if(walkWaterSound.isPlaying == false){
 					walkWaterSound.play();
 				}
 				//stop normal walk
 				//start water walk
 			}
 			else{
 				walkWaterSound.stop();
 				walkSound.update(player.getPos());
 				walkSound.play();
 				//stop water walk
 				//Play normal walk
 			}	
 		} else {
 			walkWaterSound.stop();
 			walkSound.stop();
 		}
 		//Setting the Walking and inWater bools to true to check
 		player.setWalking(false);
 		player.setInWater(false);
 	}
 }
