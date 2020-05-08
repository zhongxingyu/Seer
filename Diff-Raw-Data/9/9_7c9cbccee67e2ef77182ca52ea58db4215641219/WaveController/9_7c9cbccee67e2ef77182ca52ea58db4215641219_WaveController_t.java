 package se.chalmers.tda367.std.core;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.swing.Timer;
 
 import se.chalmers.tda367.std.core.effects.IEffect;
 import se.chalmers.tda367.std.core.enemies.IEnemy;
 import se.chalmers.tda367.std.core.tiles.IBoardTile;
 import se.chalmers.tda367.std.core.tiles.IWalkableTile;
 import se.chalmers.tda367.std.core.tiles.towers.IAttackTower;
 import se.chalmers.tda367.std.events.TowerShootingEvent;
 import se.chalmers.tda367.std.utilities.EventBus;
 import se.chalmers.tda367.std.utilities.Position;
 
 
 /**
  * The class that contains the game logic for wave phase of the game.
  * @author Johan Andersson
  * @modified Emil Edholm (Apr 27, 2012)
  * @modified Johan Gustafsson (May 12, 2012)
  * @date Apr 22, 2012
  */
 
 class WaveController {
 
 	/** The delay (in milliseconds) before the first enemy is placed on the game board */
 	private static final int INITIAL_WAVE_DELAY = 100;
 	
 	private GameBoard board;
 	private Player player;
 	private Timer releaseTimer;
 	private WaveItem nextEnemy;
 	private Wave wave;
 	
 
 	public WaveController(GameBoard board, Player player) {
 		this.board = board;
 		this.player = player;
 		releaseTimer = new Timer(INITIAL_WAVE_DELAY, new WaveReleaseTimerListener());
 	}
 	
 	// TODO: Add start/stop methods and send appropriate events when called.
 
 	/**
 	 * Starts a new wave
 	 */
 	public void startWave(Wave wave){
 		this.wave = wave;
 		releaseTimer.start();
 	}
 
 	/**
 	 * Stops the release of enemies from the current wave
 	 */
 	public void endWaveRelease(){
 		releaseTimer.stop();
 	}
 	
 	/**
 	 * The loop that updates the the wave related bits of the game.
 	 * Does things like moving the enemies, making the towers shoot at the enemies etc.
 	 * @param delta - the amount of time (in milliseconds) since the last update.
 	 */
 	public void updateWaveRelated(final int delta){
 		if(!isPlayerDead()) {
 			moveEnemies(delta);
 			shootAtEnemiesInRange(delta);
 			applyHealthEffects();
 			decreaseEffectsDuration(delta);
 			removeDeadEnemies();
 		}
 	}
 
 	private void decreaseEffectsDuration(int delta) {
 		List<EnemyItem> enemyItems = board.getModifiableEnemies();
 		List<EnemyItem> tmpEnemyItems = new LinkedList<EnemyItem>(enemyItems);
 		for(EnemyItem ei:tmpEnemyItems){
 			List<IEffect> effects = ei.getEnemy().getEffects();
			Iterator<IEffect> it = effects.iterator();
			while(it.hasNext()){
				IEffect effect = it.next();
 				effect.decrementDuration(delta);
				if(effect.getDuration() < 0.001){
					it.remove();
 				}
 			}
 		}
 	}
 
 	
 	/**
 	 * Releases the next enemy in queue from the wave
 	 */
 	private void releaseEnemy(){
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
 
 	/**
 	 * Add a enemy to the game board from a {@code WaveItem}
 	 */
 	private void addEnemy(WaveItem wi){
 		List<EnemyItem> enemies = board.getModifiableEnemies();
 		
 		EnemyItem item = new EnemyItem(wi.getEnemy(), board.getStartPos().toPosition(), board.getWaypoints());
 		enemies.add(item);
 	}
 
 	/**
 	 * Moves all the enemies on the GameBoard, towards the base.
 	 * @param delta - the amount of time (in milliseconds) since the last update.	
 	 */
 	private void moveEnemies(final int delta){
 		List<EnemyItem> enemies = board.getModifiableEnemies();
 		Iterator<EnemyItem> it = enemies.iterator();
 		
 		while(it.hasNext()) {
 			EnemyItem ei = it.next();
 			
 			ei.moveEnemy(delta);
 			if(ei.getWaypoints().isEmpty()){ 
 				// I.e. the enemy is done walking and has entered the player base.
 				board.getPlayerBase().decreaseHealth();
 				it.remove(); // Remove the enemy from the board.
 
 				// TODO: Send event that player has entered the base?
 			}
 		}
 	}
 	
 	/** Method that controls what happens when an enemy enters the player base. */
 	private void enemyEnteredBase(IEnemy enemy){
 		board.getPlayerBase().decreaseHealth();
 		board.getEnemies().remove(enemy); 
 		// Remove the "offending" enemy from the game board.
 		// TODO: Send event that player has entered the base?
 	}
 	/**
 	 * Towers fires at enemies in range.
 	 * @param delta - the amount of time (in milliseconds) since the last update.
 	 */
 	private void shootAtEnemiesInRange(final int delta){
 		int tileScale = Properties.INSTANCE.getTileScale();
 		for(int x = 0; x < board.getWidth(); x++){
 			for(int y = 0; y <board.getHeight(); y++){
 				IBoardTile tile = board.getTileAt(x, y);
 				if(tile instanceof IAttackTower){
 					IAttackTower attackTower = (IAttackTower) tile;
 					if(attackTower.isAttackReady(delta)) {
 						shoot(attackTower, new Position(x*tileScale, y*tileScale));
 					}
 				}
 			}
 		}
 	}
 	
 	
 	/**
 	 * Handles the individual shots.
 	 */
 	private void shoot(IAttackTower tile, Position pos) {
 		int radius = tile.getRadius() * Properties.INSTANCE.getTileScale();
 		List<EnemyItem> enemies = board.getEnemiesInRadius(pos, radius);
 		tile.shoot(enemies, pos);
 //		
 //		//TODO, make more advanced logic.
 //		if(!enemies.isEmpty()){
 //
 //			enemies.get(0).getEnemy().decreaseHealth(tile.getDmg());
 //		
 //			for(IEffect ie:tile.getEffects()){
 //				enemies.get(0).getEnemy().addEffect(ie);	//TODO refactor
 //			}
 //
 //
 //			EventBus.INSTANCE.post(new TowerShootingEvent(pos, enemies.get(0).getEnemyPos()));
 
 //		}
 	}
 
 	/**
 	 * Apply the effects on the enemies
 	 */
 	private void applyHealthEffects() {
 		List<EnemyItem> enemies = board.getEnemies();
 		
 		for (EnemyItem ei : enemies) {
 			applyHealthEffect(ei);
 		}
 	}
 
 	
 	private void applyHealthEffect(EnemyItem ei) {
 		IEnemy enemy = ei.getEnemy();
 		double healthModifier = 1.0;
 		for (IEffect ie : enemy.getEffects()) {
 			healthModifier = healthModifier * ie.getHealthModifier();
 		}
 		double health = enemy.getHealth() * healthModifier;
 		enemy.decreaseHealth(enemy.getHealth()-(int)health);
 	}
 
 	private boolean isPlayerDead(){
 		if(board.getPlayerBase().getHealth() <= 0){
 			playerDead();
 			return true;
 		}
 		return false;
 	}
 
 	private void playerDead(){
 		Logger.getLogger("se.chalmers.tda367.std.core").info("Player dead, game over");
 		// TODO: Sent event that player is dead.
 	}
 
 	/**
 	 * Removes the enemies that are dead from the game board.
 	 */
 	private void removeDeadEnemies(){
 		List<EnemyItem> enemies = board.getModifiableEnemies();
 		Iterator<EnemyItem> it = enemies.iterator();
 		
 		while(it.hasNext()){
 			EnemyItem item = it.next();
 			if(item.getEnemy().getHealth() <= 0){
 				it.remove();
 				player.addMoney(item.getEnemy().getLootValue());
 			}
 		}
 	}
 	
 	private class WaveReleaseTimerListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			releaseEnemy();
 		}
 
 	}
 }
