 package se.chalmers.tda367.std.core;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.swing.Timer;
 
 import se.chalmers.tda367.std.core.effects.IEffect;
 import se.chalmers.tda367.std.core.enemies.IEnemy;
 import se.chalmers.tda367.std.core.tiles.IBoardTile;
 import se.chalmers.tda367.std.core.tiles.IWalkableTile;
 import se.chalmers.tda367.std.core.tiles.PlayerBase;
 import se.chalmers.tda367.std.core.tiles.towers.IAttackTower;
 import se.chalmers.tda367.std.utilities.Position;
 
 
 /**
  * The class that contains the game logic for wave phase of the game.
  * @author Johan Andersson
  * @modified Emil Edholm (Apr 24, 2012)
  * @date Apr 22, 2012
  */
 
 public class WaveController {
 
 	private GameBoard board;
 	private Player player;
 	private Timer gameLoop;
 	private Timer releaseTimer;
 	private WaveItem nextEnemy;
 	private PlayerBase base; //TODO where to place base in code.
 	private Wave wave;
 	
 
 	public WaveController(GameBoard board, Player player) {
 		this.board = board;
 		this.player = player;
 		init();
 	}
 
 	private void init(){
 		gameLoop = new Timer(1000/60, new GameLoopListener());
 		releaseTimer = new Timer(5000, new ReleaseTimerListener());
 		base = new PlayerBase(2); //TODO, where to place base in code?
 	}
 
 	/**
 	 * Starts a new wave
 	 */
 	public void startWave(Wave wave){
 		this.wave = wave;
 		gameLoop.start();
 		releaseTimer.start();
 	}
 
 	/**
 	 * Ends the running wave
 	 */
 	public void endWave(){
 		gameLoop.stop();
 		releaseTimer.stop();
 	}
 
 	/**
 	 * Releases the next enemy in queue from the wave
 	 */
 	public void releaseEnemy(){
 		if(nextEnemy == null){
 			nextEnemy = wave.getNext();
 		}
 
 		IBoardTile startTile = board.getTileAt(board.getStartPos());
		if(startTile instanceof IWalkableTile  && nextEnemy != null){
 			addEnemy(nextEnemy);
 			nextEnemy = wave.getNext();
 			
 			if(nextEnemy != null){
 				releaseTimer.setInitialDelay(nextEnemy.getDelay());
 				releaseTimer.restart();
 			}
		}else {
			// Stop the timer when all enemies has been "released"
			releaseTimer.stop();
 		}
 	}
 
 	private void addEnemy(WaveItem wi){
 		List<EnemyItem> enemies = board.getEnemies();
 		
 		EnemyItem item = new EnemyItem(wi.getEnemy(), board.getStartPos(), board.getWaypoints());
 		enemies.add(item);
 	}
 
 	/**
 	 * Moves all the enemies on the GameBoard, towards the base.	
 	 */
 	public void moveEnemies(){
 		List<EnemyItem> enemies = board.getEnemies();
 		
 		for (EnemyItem ei : enemies) {
 			ei.moveEnemy();
 			if(ei.getWaypoints().size() == 0){
 				enemyEnteredBase(ei.getEnemy());
 			}
 		}
 	}
 	
 	private void enemyEnteredBase(IEnemy enemy){
 		base.decreaseHealth();
 		enemy.decreaseHealth(1000000);
 		//TODO more flexible implementation
 	}
 
 	/**
 	 * Towers fires at enemies in range.
 	 */
 	public void shootAtEnemiesInRange(){
 		for(int x = 0; x < board.getWidth(); x++){
 			for(int y = 0; y <board.getHeight(); y++){
 				IBoardTile tile = board.getTileAt(x, y);
 				if(tile instanceof IAttackTower){
 					shoot((IAttackTower)tile, new Position(x, y));
 				}
 			}
 		}
 	}
 	
 	
 	//Tower shoot at enemies in range.
 	private void shoot(IAttackTower tile, Position pos) {
 		List<IEnemy> enemies = board.getEnemiesInRadius(pos, tile.getRadius());
 		//TODO, make more advanced logic.
 		if(enemies.size() > 0){
 			enemies.get(0).decreaseHealth(tile.getDmg());
 		}
 //		for(IEffect ie:attackTower.getEffects()){
 //			enemies.get(0).addEffect(ie);	//TODO implements
 //		}
 	}
 
 	/**
 	 * The loop that updates the whole game
 	 */
 	public void updateGame(){
 		moveEnemies();
 		shootAtEnemiesInRange();
 		applyEffects();
 		removeDeadEnemies();
 		checkIfPlayerAlive();
 	}
 
 	private void applyEffects() {
 		List<EnemyItem> enemies = board.getEnemies();
 		
 		for (EnemyItem ei : enemies) {
 			applyEffect(ei);
 		}
 	}
 
 	private void applyEffect(EnemyItem ei) {
 		IEnemy enemy = ei.getEnemy();
 		for (IEffect ie : enemy.getEffects()) {
 			//TODO continue this
 			
 		}
 	}
 
 	private void checkIfPlayerAlive(){
 		if(base.getHealth() <= 0){
 			playerDead();
 		}
 	}
 
 	private void playerDead(){
 		Logger.getLogger("se.chalmers.tda367.std.core").info("Player dead, game over");
 		endWave();
 	}
 
 	/**
 	 * Removes the enemies that are dead from the game board.
 	 */
 	private void removeDeadEnemies(){
 		List<EnemyItem> enemies = board.getEnemies();
 		Iterator<EnemyItem> it = enemies.iterator();
 		
 		while(it.hasNext()){
 			EnemyItem item = it.next();
 			if(item.getEnemy().getHealth() <= 0){
 				it.remove();
 			}
 		}
 	}
 	
 	class ReleaseTimerListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			releaseEnemy();
 		}
 
 	}
 
 
 	class GameLoopListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			updateGame();
 		}
 	}
 
 
 
 
 }
