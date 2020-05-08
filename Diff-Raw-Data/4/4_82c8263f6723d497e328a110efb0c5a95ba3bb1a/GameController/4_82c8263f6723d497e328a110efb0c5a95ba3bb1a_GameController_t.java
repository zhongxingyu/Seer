 package controller;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.util.Calendar;
 
 import model.GameModel;
 import model.geometrical.Position;
 import model.sprites.EnemyFactory;
 import model.sprites.Player;
 
 /**
  * Controls a specified model.
  * The controller will not start in a running state, so <code>start()</code> must be
  * called for the controller to start updating its model. However, other
  * methods will still work.
  * 
  * @author
  *
  */
 public class GameController extends Thread {
 
 	private final int SLEEP = 1000 / 60;
 	private GameModel model;
 	private Input input;
 	private long startTime = Calendar.getInstance().getTimeInMillis();
 	private int ticks;
 	private boolean isRunning = true;
 	
 	/**
 	 * Creates a new gameController.
 	 * @param model The model which the gameController controls.
 	 * @param input The input which the gameController collects input from.
 	 */
 	public GameController(GameModel model, Input input) {
 		this.model = model;
 		this.input = input;
 		
 		model.getWorld().addSprite(EnemyFactory.createEasyEnemy(new Position (55, 55)));
 		model.getWorld().addSprite(EnemyFactory.createMediumEnemy(new Position (45, 45)));
 		
 	}
 	
 	/**
 	 * Update's the game a specific amount of times per second.
 	 */
 	@Override
 	public void run() {
 		while (true){
 			runControler();
 		}
 	}
 	private void runControler(){
 
 		while(isRunning) {
 			this.update();
 			ticks++;
 			try{
 				Thread.sleep(SLEEP);
 			}catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	/**
 	 * Gives the number of updates since start.
 	 * @return the number of updates since start.
 	 */
 	public int getNbrOfUpdates() {
 		return this.ticks;
 	}
 	
 	/**
 	 * Gives the time in ms the controller has existed.
 	 * @return the time in ms the controller has existed.
 	 */
 	public long getMsSinceStart() {
 		long time = Calendar.getInstance().getTimeInMillis();
 		return time - this.startTime;
 	}
 	
 	/**
 	 * Sets the direction of the player towards the mouse' position.
 	 * @param x The x-coordinate of the mouse' position.
 	 * @param y The y-coordinate of the mouse' position.
 	 */
 	public void handleMouseAt(float x, float y) {
 		float dx = model.getPlayer().getCenter().getX() - x;
 		float dy = model.getPlayer().getCenter().getY() - y;
 		float dir = (float)Math.atan(dy/dx);
 		if(dx < 0) {
 			dir -= (float)(Math.PI);
 		}
 		model.getPlayer().setDirection(-dir + (float)Math.PI);
 	}
 		
 	/**
 	 * Updates the model.
 	 */
 	public void update() {
 		//playerMove
 		this.updatePlayerPosition();
 				
 		//playerShoot
 		if(input.mousePressed(MouseEvent.BUTTON1)){
 			model.playerShoot();
 		}
 		
 		//playerReload
 		if(input.isPressed(KeyEvent.VK_R)){
 			model.getPlayer().reloadActiveWeapon();
 		}
 		
 		//gameOver?
 		if(model.getPlayer().getHealth() <= 0){
 			//TODO
 			System.out.println("Game over, Tid: " + getMsSinceStart()/1000 + "s");
 			this.pause(true);
 		}
 		
 		model.update();
 	}
 	
 	/*
 	 * Updates the player's position.
 	 */
 	private void updatePlayerPosition() {
 		if(input.isPressed(KeyEvent.VK_W) && input.isPressed(KeyEvent.VK_D)) {
 			model.getPlayer().setMoveDir((float)(Math.PI/4));
 		}else if(input.isPressed(KeyEvent.VK_D) && input.isPressed(KeyEvent.VK_S)) {
 			model.getPlayer().setMoveDir((float)(-Math.PI/4));
 		}else if(input.isPressed(KeyEvent.VK_A) && input.isPressed(KeyEvent.VK_S)) {
 			model.getPlayer().setMoveDir((float)(-Math.PI*3/4));
 		}else if(input.isPressed(KeyEvent.VK_W) && input.isPressed(KeyEvent.VK_A)) {
 			model.getPlayer().setMoveDir((float)(Math.PI*3/4));
 		}else if(input.isPressed(KeyEvent.VK_W)) {
 			model.getPlayer().setMoveDir((float)(Math.PI/2));
 		}else if(input.isPressed(KeyEvent.VK_A)) {
 			model.getPlayer().setMoveDir((float)(Math.PI));
 		}else if(input.isPressed(KeyEvent.VK_S)) {
 			model.getPlayer().setMoveDir((float)(-Math.PI/2));
 		}else if(input.isPressed(KeyEvent.VK_D)) {
 			model.getPlayer().setMoveDir(0f);
 		}else{
 			model.getPlayer().setState(Player.State.STANDING);
 		}
 		
 		if(input.isPressed(KeyEvent.VK_ESCAPE)){
 			System.out.println("ESCAPE pressed");
 			MenuController.pauseMenu();
 		}
 			
 
 	}
 	/**
 	 * Pauses the thread from running depending on the input parameter.
 	 * @param b the parameter. if true the trhead will not execute.
 	 */
 	public void pause(boolean b){
 		isRunning=!b;
 //		run();
 	}
 }
