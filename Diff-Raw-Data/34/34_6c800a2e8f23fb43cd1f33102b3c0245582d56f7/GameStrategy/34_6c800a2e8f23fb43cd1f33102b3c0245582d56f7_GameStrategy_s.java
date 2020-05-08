 package com.glowman.spaceunit.game.strategy;
 
 
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.math.Vector2;
 import com.glowman.spaceunit.game.animation.BlowController;
 import com.glowman.spaceunit.game.balance.RespawnFrequencyCollector;
 import com.glowman.spaceunit.core.AnimatedSprite;
 import com.glowman.spaceunit.core.TouchEvent;
 import com.glowman.spaceunit.game.mapObject.SpaceObject;
 import com.glowman.spaceunit.game.mapObject.enemy.Enemy;
 import com.glowman.spaceunit.game.mapObject.Ship;
 import com.glowman.spaceunit.game.mapObject.enemy.EnemyFactory;
 import com.glowman.spaceunit.game.mapObject.impact.ISpaceImpact;
 import com.glowman.spaceunit.game.mapObject.impact.ImpactController;
 
 import java.util.ArrayList;
 
 /**
  *
  */
 public abstract class GameStrategy implements IGameStrategy {
 	public static final int SHOOT_GAME = 0;
 	public static final int RUN_GAME = 1;
 
 	protected BlowController _blowController;
 	protected ImpactController _impactController;
 	protected ArrayList<Enemy> _enemies;
 	protected Ship _heroShip;
 
 	protected String[] _availableEnemyTypes;
 
 	protected int _shootingTouch = -1;
 	protected int _movingTouch = -1;
 
 	private GameStatus _gameStatus;
 	protected float _timeState;
 
	GameStrategy(Ship ship)
 	{
 		_heroShip = ship;
 		_enemies = new ArrayList<Enemy>();
 		_blowController = new BlowController();
 		_impactController = new ImpactController(_blowController);
 		_timeState = 0;
 	}
 
 	@Override
 	public void pauseGame() {
 		_gameStatus = GameStatus.PAUSE;
 	}
 	@Override
 	public void resumeGame() {
 		_gameStatus = GameStatus.IN_PROCESS;
 	}
 	@Override
 	public boolean isPaused() { return _gameStatus == GameStatus.PAUSE; }
 
 	@Override
 	public void startGame() {
 		_gameStatus = GameStatus.IN_PROCESS;
 	}
 	@Override
 	public void stopGame() {
 		//hz what here need to be
 	}
 	@Override
 	public GameStatus getGameStatus() { return _gameStatus; }
 
 	@Override
 	public void tick(float delta) {
 		_timeState += delta;
 		if (_gameStatus == GameStatus.IN_PROCESS) this.createEnemies();
 
 		_blowController.tick(delta);
 		_impactController.tick(delta);
 		this.tickEnemies(delta);
 
 		ArrayList<SpaceObject> spaceObjects = this.getAllSpaceObjects();
 		for (SpaceObject spaceObject : spaceObjects) {
 			_impactController.execute(spaceObject);
 		}
 
 		if (_heroShip.isDead()) {
 			this.gameOver();
 		}
 	}
 
 	protected ArrayList<SpaceObject> getAllSpaceObjects() {
 		ArrayList<SpaceObject> result = new ArrayList<SpaceObject>();
 		result.addAll(_enemies);
 		result.add(_heroShip);
 		return result;
 	}
 
 	@Override
 	public ArrayList<Sprite> getDrawableObjects() {
 		ArrayList<Sprite> result = new ArrayList<Sprite>();
 		for (Enemy enemy : _enemies) {
 			if (!enemy.isDead()) result.add(enemy.getImage());
 		}
 
 		if (_blowController.getBlows() != null) {
 			for (AnimatedSprite animation : _blowController.getBlows()) {
 				result.add(animation);
 			}
 		}
 
 		for (ISpaceImpact impact : _impactController.getImpacts()) {
 			result.add(impact.getImage());
 		}
 
 		if (!_heroShip.isDead()) result.add(_heroShip.getImage());
 
 		return result;
 	}
 
 	private void tickEnemies(float delta) {
 		ArrayList<Enemy> deadEnemies = null;
 		for (Enemy enemy : _enemies)
 		{
 			if (enemy.isDead()) {
 				if (deadEnemies == null) { deadEnemies = new ArrayList<Enemy>(); }
 				deadEnemies.add(enemy);
 			} else {
 				enemy.tick(delta);
 			}
 		}
 		if (deadEnemies != null) {
 			for (Enemy enemy : deadEnemies) { _enemies.remove(enemy); }
 			deadEnemies.clear();
 		}
 	}
 
 	private void createEnemies()
 	{
 		if (_availableEnemyTypes == null || _availableEnemyTypes.length == 0) {
 			return;
 		}
 		String enemyType;
 		for (int i = 0; i < _availableEnemyTypes.length; ++i) {
 			enemyType = _availableEnemyTypes[i];
 			if (Math.random() <
					RespawnFrequencyCollector.getFrequency(enemyType, GameStrategy.SHOOT_GAME, _timeState)) {
 				Enemy enemy = EnemyFactory.createEnemy(enemyType);
 				_enemies.add(enemy);
 				this.setEnemyParams(enemy);
 			}
 		}
 	}
 
 	protected void setEnemyParams(Enemy enemy) {
 		//need to override
 	}
 
 	protected void gameOver() {
 		_gameStatus = GameStatus.GAME_OVER;
 		for (Enemy enemy : _enemies) {
 			enemy.removeAllBehaviours();
 		}
 	}
 
 	protected boolean checkObjectsHit(SpaceObject object1, SpaceObject object2) {
 		if (object1.isDead() || object2.isDead()) { return false; }
 
 		float distance;
 		float radius1, radius2;
 		Vector2 position1, position2;
 		radius1 = object1.getHeight()/2;
 		radius2 = object2.getHeight()/2;
 		position1 = object1.getCenterPosition();
 		position2 = object2.getCenterPosition();
 
 		distance = position1.dst(position2);
 
 		return distance < radius1 + radius2;
 	}
 
 	protected void explodeEnemy(Enemy enemy) {
 		enemy.setGeneralSpeed(0);
 		_enemies.remove(enemy);
 		enemy.explode(_blowController);
 		enemy.setDead();
 	}
 
 	protected void explodeHero() {
 		_heroShip.stopMoving();
 		_heroShip.setGeneralSpeed(0);
 		_heroShip.explode(_blowController);
 		_heroShip.setDead();
 	}
 
 	@Override
 	public abstract void touchUp(TouchEvent touch);
 	@Override
 	public abstract void touchDown(TouchEvent touch);
 	@Override
 	public abstract void touchMove(TouchEvent touch);
 
 }
