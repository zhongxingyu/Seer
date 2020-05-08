 package com.glowman.spaceunit.game.strategy;
 
 import android.util.Log;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.math.Vector2;
 
 import com.badlogic.gdx.math.Vector3;
 import com.glowman.spaceunit.Assets;
 import com.glowman.spaceunit.CoordinatesTranslator;
 import com.glowman.spaceunit.core.TouchEvent;
 import com.glowman.spaceunit.game.IShooter;
 import com.glowman.spaceunit.game.balance.RotationSpeedCollector;
 import com.glowman.spaceunit.game.score.Score;
 import com.glowman.spaceunit.game.Shooter;
 import com.glowman.spaceunit.game.balance.EnemySetCollector;
 import com.glowman.spaceunit.game.balance.SpeedCollector;
 import com.glowman.spaceunit.game.mapObject.Bullet;
 import com.glowman.spaceunit.game.mapObject.SpaceObject;
 import com.glowman.spaceunit.game.mapObject.enemy.BehaviourOptionsData;
 import com.glowman.spaceunit.game.mapObject.enemy.Enemy;
 import com.glowman.spaceunit.game.mapObject.Ship;
 import com.glowman.spaceunit.game.mapObject.enemy.EnemyFactory;
 
 import java.util.ArrayList;
 
 /**
  *
  */
 public class GameShootStrategy extends GameStrategy {
 
 	private IShooter _shooter;
 	private Score _score;
 
 	public GameShootStrategy(Ship ship)
 	{
		super(ship);
 		_score = new Score(Score.getScoreTypeByGameType(GameStrategy.SHOOT_GAME), 0);
 		_shooter = new Shooter();
 		BehaviourOptionsData bhOptions = new BehaviourOptionsData(_shooter, _blowController, ship, _impactController);
 		EnemyFactory.init(GameStrategy.SHOOT_GAME, ship, bhOptions);
 		_availableEnemyTypes = EnemySetCollector.getEnemySet(GameStrategy.SHOOT_GAME);
 	}
 
 	@Override
 	public Score getScore() {
 		return _score;
 	}
 
 	@Override
 	protected ArrayList<SpaceObject> getAllSpaceObjects() {
 		ArrayList<SpaceObject> result = super.getAllSpaceObjects();
 		if (_shooter.getBullets() != null) {
 			result.addAll(_shooter.getBullets());
 		}
 		return result;
 	}
 
 	@Override
 	public ArrayList<Sprite> getDrawableObjects() {
 		ArrayList<Sprite> result = super.getDrawableObjects();
 		if (_shooter.getBullets() != null) {
 			for (Bullet bullet : _shooter.getBullets()) {
 				result.add(bullet.getImage());
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public void tick(float delta)
 	{
 		super.tick(delta);
 		_heroShip.tick(delta);
 
 		if (_heroShip.isReadyForShoot())
 		{
 			this.shootBullet();
 		}
 
 		_shooter.tick(delta);
 
 		if (_shooter.getBullets() != null)
 		{
 			this.checkBulletsForRemove();
 			this.checkBulletsHit();
 		}
 		this.checkHeroHit();
 	}
 
 	@Override
 	public void touchUp(TouchEvent touch){
 		if (touch.pointer == _shootingTouch) {
 			_shootingTouch = -1;
 			_heroShip.setShooting(false);
 		}
 	}
 
 	@Override
 	public void touchDown(TouchEvent touch){
 		_shootingTouch = touch.pointer;
 		_heroShip.setShooting(true);
 	}
 	@Override
 	public void touchMove(TouchEvent touch){
 	}
 
 	@Override
 	protected void setEnemyParams(Enemy enemy)
 	{
 		enemy.setRandomBorderPosition();
 		enemy.setGeneralSpeed(SpeedCollector.getEnemySpeed(enemy.getEnemyType(), GameStrategy.RUN_GAME));
 		enemy.setRotationSpeed(RotationSpeedCollector.getEnemyRotation());
 		enemy.moveTo((float) Math.random() * Assets.VIRTUAL_WIDTH,
 				(float) Math.random() * Assets.VIRTUAL_HEIGHT);
 		enemy.setTarget(_heroShip);
 	}
 
 	private void shootBullet()
 	{
 		if (super._shootingTouch == -1) { throw new Error("shooting touch not exist!!"); }
 
 		int touchX = Gdx.input.getX(_shootingTouch);
 		int touchY = Gdx.input.getY(_shootingTouch);
 
 		Vector3 targetPoint =  CoordinatesTranslator.toVirtualView(touchX, touchY);
 
 		_shooter.shoot(_heroShip, new Vector2(targetPoint.x, targetPoint.y));
 	}
 
 	private void checkBulletsForRemove()
 	{
 		float bulletX;
 		float bulletY;
 		for(Bullet bullet : _shooter.getBullets())
 		{
 			bulletX = bullet.getView().getX();
 			bulletY = bullet.getView().getY();
 			if ((bulletX < 0) ||
 					(bulletX > Assets.VIRTUAL_WIDTH) ||
 					(bulletY < 0) ||
 					(bulletY > Assets.VIRTUAL_HEIGHT))
 			{
 				Log.d("hz", "remove bullet!");
 				bullet.setDead();
 			}
 		}
 	}
 
 	private void checkBulletsHit()
 	{
 		if (_enemies == null) { return; }
 		ArrayList<Enemy> enemiesForExplosion = new ArrayList<Enemy>();
 
 		for(Bullet bullet : _shooter.getBullets())
 		{
 			for (int i = 0; i < _enemies.size(); ++i) {
 				if (bullet.getOwner() == _enemies.get(i)) continue;
 
 				if (super.checkObjectsHit(_enemies.get(i), bullet)) {
 					enemiesForExplosion.add(_enemies.get(i));
 					bullet.setDead();
 					if (bullet.getOwner() == _heroShip &&
 							super.getGameStatus() != GameStatus.GAME_OVER) {
 						_score.score++;
 					}
 				}
 			}
 		}
 
 		for(Enemy enemy : enemiesForExplosion)
 		{
 			super.explodeEnemy(enemy);
 		}
 		enemiesForExplosion.clear();
 	}
 
 	private void checkHeroHit() {
 		Enemy enemyToExplode = null;
 		for (Enemy enemy : _enemies) {
 			if (super.checkObjectsHit(_heroShip, enemy)) {
 				enemyToExplode = enemy;
 				super.gameOver();
 				break;
 			}
 		}
 		if (enemyToExplode != null) {
 			super.explodeEnemy(enemyToExplode);
 			super.explodeHero();
 		}
 		else {
 			if (_shooter.getBullets() != null) {
 				for (Bullet bullet : _shooter.getBullets()) {
 					if (bullet.getOwner() != _heroShip &&
 							super.checkObjectsHit(_heroShip, bullet)) {
 						super.explodeHero();
 						bullet.setDead();
 						super.gameOver();
 					}
 				}
 			}
 		}
 	}
 
 
 }
