   package edu.chl.codenameg.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.chl.codenameg.model.entity.PlayerCharacter;
 import edu.chl.codenameg.model.levels.*;
 
 public class GameModel {
 
 	private World world;
 	private boolean running;
 	private int selectedLevel;
 	private List<PlayerCharacter> listOfPC;
 
 	public GameModel() {
 		listOfPC = new ArrayList<PlayerCharacter>();
 
 		this.selectLevel(1);
 	}
 
 	private World createTestWorld() {
 		World testWorld = new World();
 		
 		Level l1 = this.getLevel(4);
		ArrayList<Entity> level = (ArrayList<Entity>) l1.getListOfEnteties();
 		for(int i = 0;i<level.size();i++){
 			testWorld.add(level.get(i));
 		}
 		PlayerCharacter pc1 = new PlayerCharacter();
 		pc1.setPosition(l1.getStartPosition());
 		testWorld.add(pc1);
 		listOfPC.removeAll(listOfPC);
 		listOfPC.add(pc1);
 		
 		//listOfPC.add(pc2);
 		
 		return testWorld;
 	}
 
 	public void setWorld(World w) {
 		this.world = w;
 	}
 
 	public void startGame() {
 		this.running = true;
 	}
 
 	public void restartGame() {
 		/*// This should be done using the controller instead...
 		this.endGame();
 		//******************************
 		World testWorld = this.createTestWorld();
 		this.setWorld(testWorld);*/
 		this.endGame();
 		this.selectLevel(selectedLevel);
 	}
 
 	public void pauseGame(World w) {
 		this.running = false;
 	}
 
 	public World getWorld() {
 		return world;
 	}
 	
 	public void endGame() {
 		if (world != null && running) {
 			this.running = false;
 			this.world = null;
 		}
 	}
 	public Level getLevel(int i) {
 		return LevelFactory.getInstance().getLevel(i);
 	}
 	
 	public void selectLevel(int i) {
 		this.selectedLevel = i;
 		World temp = new World(this.getLevel(i));
 		
 		PlayerCharacter pc1 = new PlayerCharacter();
 		pc1.setPosition(this.getLevel(i).getStartPosition());
 		temp.add(pc1);
 		listOfPC.removeAll(listOfPC);
 		listOfPC.add(pc1);
 		
 		this.setWorld(temp);
 		System.out.println("Select level "+i);
 	}
 	
 	public int getSelectedLevel() {
 		return this.selectedLevel;
 	}
 	
 	public void performAction(Action action) {
 		switch (action) {
 		case START_GAME:
 			this.startGame();
 			break;
 		case PAUSE_GAME:
 			this.pauseGame(this.getWorld());
 			break;
 		case RESTART_GAME:
 			this.restartGame();
 			break;
 		case PLAYER_1_MOVE_LEFT:
 			getPlayer(1).move(Direction.LEFT);
 			break;
 		case PLAYER_1_MOVE_RIGHT:
 			getPlayer(1).move(Direction.RIGHT);
 			break;
 		case PLAYER_1_JUMP:
 			getPlayer(1).jump();
 			break;
 		case PLAYER_1_TOGGLE_CROUCH:
 			getPlayer(1).toggleCrouch();
 			break;
 		case PLAYER_2_MOVE_LEFT:
 			if(this.listOfPC.size()>1){
 				getPlayer(2).move(Direction.LEFT);
 				break;
 			}
 		case PLAYER_2_MOVE_RIGHT:
 			if(this.listOfPC.size()>1){
 				getPlayer(2).move(Direction.RIGHT);
 				break;
 			}
 		case PLAYER_2_JUMP:
 			if(this.listOfPC.size()>1){
 				getPlayer(2).jump();
 				break;
 			}
 		case PLAYER_2_TOGGLE_CROUCH:
 			if(this.running && this.listOfPC.size()>1){
 				getPlayer(2).toggleCrouch();
 				break;
 			}
 		case PLAYER_1_TOGGLE_LIFT:
 			getPlayer(1).Togglelift();
 			break;
 		case PLAYER_2_TOGGLE_LIFT:
 			if(this.listOfPC.size()>1){
 				getPlayer(2).Togglelift();
 				break;
 			}
 		default:
 			break;
 		}
 	}
 	
 	public void stopAction(Action action) {
 		switch (action) {
 		case PLAYER_1_MOVE_LEFT:
 			getPlayer(1).stopMove();
 			break;
 		case PLAYER_1_MOVE_RIGHT:
 			getPlayer(1).stopMove();
 			break;
 		case PLAYER_1_JUMP:
 			getPlayer(1).stopJump();
 			break;
 		case START_GAME:
 			this.startGame();
 			break;
 		case PLAYER_1_TOGGLE_CROUCH:
 			getPlayer(1).unToggleCrouch();
 			break;
 		case PLAYER_2_MOVE_LEFT:
 			if(this.listOfPC.size()>1){
 				getPlayer(2).stopMove();
 				break;
 			}
 		case PLAYER_2_MOVE_RIGHT:
 			if(this.listOfPC.size()>1){
 				getPlayer(2).stopMove();
 				break;
 			}
 		case PLAYER_2_JUMP:
 			if(this.listOfPC.size()>1){
 				getPlayer(2).stopJump();
 				break;
 			}
 		case PLAYER_2_TOGGLE_CROUCH:
 			if(this.running && this.listOfPC.size()>1){
 				getPlayer(2).unToggleCrouch();
 				break;
 			}
 		case PLAYER_1_TOGGLE_LIFT:
 			getPlayer(1).unToggleLift();
 			break;
 		case PLAYER_2_TOGGLE_LIFT:
 			if(this.listOfPC.size()>1){
 				getPlayer(2).unToggleLift();
 				break;
 			}
 		default:
 			break;
 		}
 	}
 	
 	public PlayerCharacter getPlayer(int num) {
 		return listOfPC.get(num-1);
 	}
 
 	public void update(int elapsedTime) {
 		if (world != null && running) {
 			world.update(elapsedTime);
 		}
 		if (world.isGameOver()) {
 			if (world.isGameWon()) {
 				System.out.println("Congratulations!");
 			}
 			this.restartGame();
 		}
 //		for (Entity entity : world.getEntities()) {
 //			if (entity instanceof PlayerCharacter) {
 //				PlayerCharacter pc = (PlayerCharacter)entity;
 //				if (pc.hasWonGame()) {
 //					this.restartGame();
 //					System.out.println("Congratulations for winning the game!");
 //				} else if (!(pc.isAlive())) {
 //					this.restartGame(); // TODO Skriv klart
 //				}
 //			}
 //		}
 	}
 }
