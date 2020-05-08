 package com.github.joukojo.testgame;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.joukojo.testgame.world.core.Moveable;
 import com.github.joukojo.testgame.world.core.WorldCore;
 import com.github.joukojo.testgame.world.core.WorldCoreFactory;
 
 public class CollisionDetectionWorker implements Runnable {
 
 	private final static Logger LOG = LoggerFactory.getLogger(CollisionDetectionWorker.class);
 	private volatile boolean isRunning;
 
 	@Override
 	public void run() {
 		final WorldCore worldCore = WorldCoreFactory.getWorld();
 		setRunning(true);
 		while (isRunning()) {
 			handleBulletCollisions(worldCore);
 
 			handlePlayerCollisions(worldCore);
 
 			Thread.yield();
 		}
 
 	}
 
 	private void handleBulletCollisions(final WorldCore worldCore) {
 		final List<Moveable> bullets = worldCore.getMoveableObjects(Constants.BULLETS);
 
 		if (bullets != null && !bullets.isEmpty()) {
 			for (final Moveable moveableBullet : bullets) {
 				final Bullet bullet = (Bullet) moveableBullet;
 				isBulletInCollisionWithMonster(worldCore, bullet);
 			}
 		}
 	}
 
 	private void handlePlayerCollisions(final WorldCore worldCore) {
 		final Player player = (Player) worldCore.getMoveable(Constants.PLAYER);
 		final List<Moveable> monsters = worldCore.getMoveableObjects(Constants.MONSTERS);
 
 		for (final Moveable moveable : monsters) {
 			final Monster monster = (Monster) moveable;
 			if (monster != null && !monster.isDestroyed() && player != null) {
 				isPlayerAndMonsterInCollision(worldCore, player, monster);
 			}
 
 		}
 	}
 
 	private void isBulletInCollisionWithMonster(final WorldCore worldCore, final Bullet bullet) {
 		final List<Moveable> monsters = worldCore.getMoveableObjects(Constants.MONSTERS);
 		if (monsters != null && !monsters.isEmpty()) {
 			for (final Moveable moveableMonster : monsters) {
 				final Monster monster = (Monster) moveableMonster;
				// FIXME do the location calculation better
 				if (monster != null && bullet != null) {
 					isBulletAndMonsterInCollision(worldCore, bullet, monster);
 				}
 			}
 		}
 	}
 
 	private void isBulletAndMonsterInCollision(final WorldCore worldCore, final Bullet bullet, final Monster monster) {
 		if (!monster.isDestroyed() && !bullet.isDestroyed()) {
 
 			// correct the monster location
 			final int monsterRealX = monster.getLocationX() + 34;
 			final int monsterRealY = monster.getLocationY() + 34;
 
 			final int deltaX = Math.abs(bullet.getLocationX() - monsterRealX);
 			final int deltaY = Math.abs(bullet.getLocationY() - monsterRealY);
			LOG.trace("deltaX {}", deltaX);
			LOG.trace("deltaY {}", deltaY);
 			if (deltaX < 30 && deltaY < 30) {
 				LOG.debug("we have a hit");
 				monster.setDestroyed(true);
 				bullet.setDestroyed(true);
 
 				final Player player = (Player) worldCore.getMoveable(Constants.PLAYER);
 				player.setScore(player.getScore() + 100);
 			}
 		}
 	}
 
 	private void isPlayerAndMonsterInCollision(final WorldCore worldCore, final Player player, final Monster monster) {
 		if (!monster.isDestroyed()) {
 
 			// correct the monster location
 			final int monsterRealX = monster.getLocationX() + 34;
 			final int monsterRealY = monster.getLocationY() + 13;
 
 			final int deltaX = Math.abs(player.getPositionX() - monsterRealX);
 			final int deltaY = Math.abs(player.getPositionY() - monsterRealY);
 			LOG.trace("deltaX {}", deltaX);
 			LOG.trace("deltaY {}", deltaY);
 			if (deltaX < 20 && deltaY < 20) {
 				LOG.debug("we have a hit with monster");
 				monster.setDestroyed(true);
 
 				player.setHealth(player.getHealth() - 1);
 			}
 		}
 	}
 
 	public boolean isRunning() {
 		return isRunning;
 	}
 
 	public void setRunning(final boolean isRunning) {
 		this.isRunning = isRunning;
 	}
 
 }
