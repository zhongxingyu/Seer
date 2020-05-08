 package mta13438;
 
 import java.nio.IntBuffer;
 import java.util.ArrayList;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.Sys;
 import org.lwjgl.openal.AL10;
 import org.lwjgl.openal.AL11;
 import org.lwjgl.openal.ALC10;
 import org.lwjgl.openal.ALC11;
 import org.lwjgl.openal.ALCcontext;
 import org.lwjgl.openal.ALCdevice;
 import org.lwjgl.openal.EFX10;
 import org.lwjgl.opengl.Display;
 
 public class Loader {
 
 	static Level tutorialLevel = new Level(new ArrayList<Room>(), 0, 0, 0);
 	private static Controls controls = new Controls();
 	private static Player player = new Player(new Point(25,315,10),0.2f,0.01f,10);
 	private static Point playerPos = new Point(0,0,0);
 	private static long lastFrame;
 	private static int delta = getDelta();
 	private static long lastFPS;
 	private static int fps;
 	private static int currentRoom;
 	private static int tempCurrentRoom = -1;
 	private static boolean renderRoom = false;
 	private static boolean collision = false;
	private static boolean playStartSequence = false;
 	private static boolean takeInput = true;
 	private static boolean playSounds = true;
 	private static long startTime;
 	private static long time;
 	private static int counter;
 	private static Event scareEvent = new Event(new Point(100, 0, 10), 20, 90, 0, MATERIALS.WATER);
	private static Entity guard = new Entity(new Point(25,315,1),0.2f,(float)Math.PI);
 
 	private static Sound guardVoice = new Sound(SOUNDS.GUARD, player.getPos(), false, true, 10.0f);
 	private static Sound playerVoice = new Sound(SOUNDS.PLAYERVOICE, player.getPos(), false, true, 10.0f);
 	private static Sound openDoorSound = new Sound(SOUNDS.GODOOR,new Point (0,0,0), false, true);
 	private static Sound trapDeathSound = new Sound(SOUNDS.TRAP_DEATH,new Point (0,0,0), false, true, 0.5f);
 	private static Sound monsterDeathSound = new Sound(SOUNDS.MONSTER_DEATH,new Point (0,0,0), false, true, 0.5f);
 	private static Sound test = new Sound(SOUNDS.MENU_MUSIC,new Point (230,320,0), true, true, 10000.0f);
 
 	private static Sound walkSound = new Sound(SOUNDS.FOOTSTEP_STONE, player.getPos(), true, true);
 	private static Sound walkWaterSound = new Sound(SOUNDS.FOOTSTEP_WATER, player.getPos(), true, true);
 	private static boolean playing = false;
 
 	final static int effectSlot = EFX10.alGenAuxiliaryEffectSlots();
 	final static int reverbEffect = EFX10.alGenEffects();
 	static ALCdevice openALDevice = null;
 	static ALCcontext openALContext = null;
 	static IntBuffer attribs = new BufferUtils().createIntBuffer(4);
 	static int iSend = 0; 
 
 	public void start() {
 		initOpenAL();
 
 		DebugInterface.Initialize(800, 600); // Width and Length of display
 		Menu mainMenu = new Menu();
 		getDelta();
 		lastFPS = getTime();
 	}
 	// Loads the tutoral level. Rooms and obstacles are added to the level.
 	private static void loadTutorialLevel() {
 		tutorialLevel.addRoomList(new Room(10, 20, 20, new Point(0,5,10), new Point(10, 15, 10), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(60, 50, 30, new Point(0,25,10), new Point(60, 5, 10), MATERIALS.ROCK));
 		//tutorialLevel.addRoomList(new Room(50, 10, 20, new Point(0,5,0), new Point(50, 5, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(150, 90, 60, new Point(0,45,10), new Point(150, 65, 10), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(110, 110, 40, new Point(0,55,10), new Point(90, 0, 10), MATERIALS.ROCK));
 		//tutorialLevel.addRoomList(new Room(40, 10, 20, new Point(0,5,0), new Point(35, 0, 0), MATERIALS.ROCK));
 		//tutorialLevel.addRoomList(new Room(10, 50, 20, new Point(5,50,0), new Point(5, 0, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(70, 70, 40, new Point(35,70,10), new Point(35,0, 10), MATERIALS.ROCK));
 		//tutorialLevel.addRoomList(new Room(10, 20, 20, new Point(5,20,0), new Point(5, 0, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(50, 80, 40, new Point(25,80,10), new Point(25, 0, 10), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(30, 20, 40, new Point(15,20,10), new Point(15, 20, 10), MATERIALS.ROCK));
 		tutorialLevel.getRoomList().get(2).addObsList(new Water(new Point(20, 20, 0), 20, 50, 0, MATERIALS.WATER));
 		tutorialLevel.getRoomList().get(2).addObsList(scareEvent);
 		tutorialLevel.getRoomList().get(3).addObsList(new EnvironmentObs(new Point(20, 5, 0),SOUNDS.RAT,false,true));
 		tutorialLevel.getRoomList().get(0).addObsList(new EnvironmentObs(new Point(0, 0, 0),SOUNDS.RAT,false,true));
 		tutorialLevel.getRoomList().get(5).addObsList(new EnvironmentObs(new Point(5, 20, 0),SOUNDS.RAT,false,true));
 		//tutorialLevel.getRoomList().get(3).addObsList(new EnvironmentObs(new Point(40, 0, 0),SOUNDS.MONSTER_CELL_01,true,true));
 		//tutorialLevel.getRoomList().get(4).addObsList(new Monster(new Point(60, 70, 0), 20, 20, 0, MATERIALS.ROCK,SOUNDS.MONSTER1));
 		tutorialLevel.getRoomList().get(2).addObsList(new EnvironmentObs(new Point(40, 80, 0),SOUNDS.WATERDROP2,true,true));
 		tutorialLevel.getRoomList().get(2).addObsList(new EnvironmentObs(new Point(110, 30, 0),SOUNDS.WATERDROP1,true,true));
 		tutorialLevel.getRoomList().get(5).addObsList(new Monster(new Point(20, 20, 10), 20, 20, 0, MATERIALS.ROCK,SOUNDS.MONSTER1));
 		tutorialLevel.getRoomList().get(3).addObsList(new Monster(new Point(40, 25, 10), 20, 20, 0, MATERIALS.ROCK,SOUNDS.MONSTER2));
 		tutorialLevel.getRoomList().get(4).addObsList(new Trap(new Point(20, 20, 10), 30, 30, 0, MATERIALS.ROCK));
 		//tutorialLevel.getRoomList().get(9).addObsList(new TrapGuillotine(new Point(0, 30, 0), 50, 10, 0, MATERIALS.ROCK));
 		tutorialLevel.autoLevelGenerator(new Point(10,300,0));
 		System.out.println("Loaded level.");
 		for(int i = 0; i < tutorialLevel.getRoomList().get(2).getObsList().size(); i++){
 		System.out.println(tutorialLevel.getRoomList().get(2).getObsList().get(i).toString());
 		}
 
 		initializeReverb();
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
 		if(takeInput == true){
 			input();
 		} 
 		if(playStartSequence == true){
 			System.out.println(time - startTime);
 			if(time < (startTime+25800)){
 				takeInput = false;
 				playSounds = false;
 				if(counter == 0){
 					startTime = getTime();
 					counter++;
 				}
 				guard.draw();
 				guardVoice.update(guard.getPos());
 				guardVoice.play();
 				playerVoice.play();
 				time = getTime();
 			} else if (time >= (startTime+25800) && time < (startTime+29600)){
 				guard.backward(0.7f);
 				guard.turnRight(0.2f);
 				guard.draw();
 				guardVoice.update(guard.getPos());
 				time = getTime();
 			} else if (time >= (startTime+29600) && time < (startTime+39500)){
 				time = getTime();
 				//player.foward(0.1f);
 				//player.draw();
 			} else if (time >= (startTime+39500) && time < (startTime+48000)){
 				time = getTime();
 				playSounds = true;
 				takeInput = true;
 				player.setSpeed(0.0f);
 			} else if (time >= (startTime+48000) && time < (startTime+56000)){
 				time = getTime();
 			} else if (time >= (startTime+56000)){
 				time = getTime();
 				playStartSequence = false;
 				System.out.println("ss");
 				takeInput = true;
 				player.setSpeed(0.2f);
 				counter = 0;
 			}
 		}
 		if(currentRoom == 6){
 			test.play();
 		}
 		//System.out.println(test.getPos().getX() - player.getPos().getX() + " and " + (test.getPos().getY() - player.getPos().getY()));
 
 		if(tempCurrentRoom != currentRoom){
 			tempCurrentRoom = currentRoom;
 			//updateReverb(tutorialLevel.getRoomList().get(currentRoom).getRt60());
 			for (int i = 0; i < tutorialLevel.getRoomList().get(currentRoom).getObsList().size(); i++) {
 				tutorialLevel.getRoomList().get(currentRoom).getObsList().get(i).getLoopSound().update(tutorialLevel.getRoomList().get(currentRoom).getObsList().get(i).getLoopSound().getPos());					
 			}
 
 		}
 
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
 		if(playSounds == true){
 			for (int i = 0; i < tutorialLevel.getRoomList().get(currentRoom).getObsList().size(); i++) {
 				if (tutorialLevel.getRoomList().get(currentRoom).getObsList().get(i).getEmitSound() == true && currentRoom > 0 && currentRoom < 6){
 					tutorialLevel.getRoomList().get(currentRoom).getObsList().get(i).getLoopSound().play();
 				}
 			}
 		} else {
 			for (int i = 0; i < tutorialLevel.getRoomList().size(); i++){	
 				for(int j = 0; j < tutorialLevel.getRoomList().get(i).getObsList().size(); j++){
 					tutorialLevel.getRoomList().get(i).getObsList().get(j).getLoopSound().stop();
 				} 
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
 		//Scare event in room 2
 		if(currentRoom == 2){
 			if(scareEvent.isTrigger() == true && scareEvent.isActive() == true){
 				scareEvent.getScareSound().update(new Point(scareEvent.getPos().getX(),scareEvent.getPos().getY() + scareEvent.getDy(),0));
 				scareEvent.getScareSound().play();
 				scareEvent.setActive(false);
 			}
 		} else {
 			scareEvent.getScareSound().stop();
 		}
 		// play trap death sound
 		if(tutorialLevel.isTrapDeath() == true){
 			if(counter == 0){
 				startTime = getTime();
 				counter++;
 			}
 			time = getTime();
 			if(time < startTime +5000){
 				playSounds = false;
 				takeInput = false;
 				trapDeathSound.update(player.getPos());
 				trapDeathSound.play();
 			} else if(time <= startTime + 5500){
 				trapDeathSound.stop();
 				player.setSpeed(0.0f);
 			} else if(time > startTime + 5500){
 				counter = 0;
 				player.setSpeed(0.2f);
 				tutorialLevel.setTrapDeath(false);
 				tutorialLevel.setGoThroughDoor(true);
 				player.kill(tutorialLevel);
 			}
 		}
 		//tutorialLevel.updateSpawnPoint(player, tutorialLevel);
 		//System.out.println(tutorialLevel.getSpawnPoint().toString());
 		//play monster death sound
 		if(tutorialLevel.isMonsterDeath() == true){
 			if(counter == 0){
 				startTime = getTime();
 				counter++;
 			}
 			time = getTime();
 			if(time < startTime +5000){
 				playSounds = false;
 				takeInput = false;
 				monsterDeathSound.update(player.getPos());
 				monsterDeathSound.play();
 			} else if(time <= startTime + 5500){
 				monsterDeathSound.stop();
 				player.setSpeed(0.0f);
 			} else if(time > startTime + 5500){
 				counter = 0;
 				player.setSpeed(0.2f);
 				tutorialLevel.setMonsterDeath(false);
 				tutorialLevel.setGoThroughDoor(true);
 				player.kill(tutorialLevel);
 			}
 		}
 		// play close door sound when entering new room
 		if(tutorialLevel.isGoThroughDoor() == true){
 			if(counter == 0){
 				startTime = getTime();
 				counter++;
 			}
 			time = getTime();
 			if(time < startTime +5000){
 				playSounds = false;
 				takeInput = false;
 				openDoorSound.update(player.getPos());
 				openDoorSound.play();
 				//System.out.println(time - startTime);
 			} else if(time <= startTime + 5500){
 				//System.out.println(time);
 				openDoorSound.stop();
 				takeInput = true;
 				playSounds = true;
 				player.setSpeed(0.0f);
 			} else if(time > startTime + 5500){
 				counter = 0;
 				player.setSpeed(0.2f);
 				tutorialLevel.setGoThroughDoor(false);
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
 
 	public static void initializeReverb() {
 		EFX10.alEffecti(reverbEffect, EFX10.AL_EFFECT_TYPE, EFX10.AL_EFFECT_REVERB);
 		//AL10.alListenerf(EFX10.AL_METERS_PER_UNIT, 0.10f);
 		EFX10.alEffectf(reverbEffect, EFX10.AL_METERS_PER_UNIT, 0.10f);
 		EFX10.alAuxiliaryEffectSloti(effectSlot, EFX10.AL_EFFECTSLOT_EFFECT, reverbEffect);
 
 
 	}
 	public static void updateReverb(float[] rt60) {
 		float decayTime, HFRatio;
 		float temp = 0;
 
 		for (int i = 0; i < rt60.length; i++) {
 			temp += rt60[i];
 		}
 		decayTime = temp / rt60.length;
 
 		temp = (rt60[0] + rt60[1]) / 2;
 		HFRatio = ((rt60[4] + rt60[5]) / 2) / temp;
 
 		EFX10.alEffectf(reverbEffect, EFX10.AL_REVERB_DECAY_TIME, decayTime);
 		EFX10.alEffectf(reverbEffect, EFX10.AL_REVERB_DECAY_HFRATIO, HFRatio);
 		EFX10.alAuxiliaryEffectSloti(effectSlot, EFX10.AL_EFFECTSLOT_EFFECT, reverbEffect);
 
 		for (int i = 0; i < tutorialLevel.getRoomList().get(currentRoom).obsList.size(); i++) {
 			tutorialLevel.getRoomList().get(currentRoom).getObsList().get(i).getLoopSound().loadReverb(effectSlot);
 		}
 	}
 	public static void initOpenAL() {
 		
 		openALDevice = ALC10.alcOpenDevice(null);
 		System.out.println("Device was set up.");
 
 		if(ALC10.alcIsExtensionPresent(openALDevice, "ALC_EXT_EFX") == false)return;		
 		System.out.println("EFX Extension found!"); 
 
 		openALContext = ALC10.alcCreateContext(openALDevice, null);
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
 		if(player.isWalking() == true){
 			if(player.isInWater()==true){
 				walkSound.stop();
 				walkWaterSound.update(new Point(player.getPos().getX(),player.getPos().getY(),0));
 				if(walkWaterSound.isPlaying == false){
 					walkWaterSound.play();
 				}
 				//stop normal walk
 				//start water walk
 			}
 			else{
 				walkWaterSound.stop();
 				walkSound.update(new Point(player.getPos().getX(),player.getPos().getY(),0));
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
