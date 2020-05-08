 package se.chalmers.kangaroo.model;
 
 import java.awt.geom.Rectangle2D;
 
 import se.chalmers.kangaroo.constants.Constants;
 import se.chalmers.kangaroo.utils.GameTimer;
 
 /**
  * A class to represent the model of a platform game.
  * 
  * @author arvidk
  * @modifiedby simonal
  * 
  */
 public class GameModel {
 
 	/*
 	 * The kangaroo that the player controlls.
 	 */
 	private Kangaroo kangaroo;
 	/*
 	 * The current level that the player are playing.
 	 */
 	private GameMap map;
 	/*
 	 * The amount of times the player have died.
 	 */
 	private int deathCount;
 	/*
 	 * Will keep track of how long the player have played.
 	 */
 	private GameTimer timer;
 	/*
 	 * Will check if the game is running. Not sure if we need it.
 	 */
 	private boolean isRunning = false;
 	/*
 	 * The gameMap. Makes it able to check collition
 	 */
 	private GameMap gameMap;
 	/*
 	 * Avariable to keep track of the kangaroos old position
 	 */
 	private Position oldPos;
 
 	private int jGlobal;
 
 	/**
 	 * A method to start the game.
 	 */
 	// public void start() {
 	// isRunning = true;
 	// while (isRunning) {
 	// this.update();
 	// }
 	// }
 
 	/**
 	 * Will make it able to pause the game
 	 */
 	// public void pause() {
 	// if (isRunning = true) {
 	// isRunning = false;
 	// } else {
 	// this.start();
 	// }
 	// }
 
 	/**
 	 * A method to stop the game, and thereby quit it.
 	 */
 	// public void stop() {
 	// isRunning = false;
 	// }
 
 	public GameModel() {
 		gameMap = new GameMap("../maps/betamap.tmx");
 		kangaroo = new Kangaroo(new Position(10, 186));
 		timer = new GameTimer();
 		timer.start();
 	}
 
 	/**
 	 * A method to update the game.
 	 */
 	public void update() {
 		oldPos = kangaroo.getPosition();
 		kangaroo.move();
 		updateCreatures();
 		checkCollition();
 	}
 
 	private void updateCreatures() {
 		for (int i = 0; i < gameMap.getCreatureSize(); i++) {
 			Creature c = gameMap.getCreatureAt(i);
 			Rectangle2D cRect = c.getPolygon().getBounds2D();
 			if (!(gameMap.getTile((int) (cRect.getMinX() / 32),
 					(int) (cRect.getMinY() / 32) + 1).isCollidable())
 					|| !(gameMap.getTile((int) (cRect.getMaxX() / 32),
 							(int) (cRect.getMinY() / 32) + 1).isCollidable())
 					|| (gameMap.getTile((int) (cRect.getMinX() / 32),
 							(int) (cRect.getMinY() / 32))).isCollidable()
 					|| gameMap.getTile((int) (cRect.getMaxX() / 32),
 							(int) (cRect.getMinY() / 32)).isCollidable()) {
 				c.changeDirection();
 			}
 			c.updateCreature();
 
 		}
 
 	}
 
 	/**
 	 * Checks if a polygon collides with a tile or a creature.
 	 */
 	private void checkCollition() {
 		creatureCollition();
 		itemCollition();
 		tileCollition();
 		iObjectCollition();
 		try {
 			changeFalling();
 		} catch (ArrayIndexOutOfBoundsException e) {
 			restartLevel();
 		}
 	}
 
 	private void iObjectCollition() {
 		int x = kangaroo.getPosition().getX() / Constants.TILE_SIZE;
 		int y = kangaroo.getPosition().getY() / Constants.TILE_SIZE;
 		for (int i = x; i < x + 2; i++)
 			for (int j = y; j < y + 3; j++)
 				if (gameMap.getIObjectAt(i, j) != null)
 					gameMap.getIObjectAt(i, j).onCollision();
 	}
 
 	/**
 	 * Uses the polygon of the kangaroo and looks if it intersects with a
 	 * creature. If so, the kangaroo will either kill the creature or die.
 	 */
 	private void creatureCollition() {
		int nbrOfCreatures = gameMap.getCreatureSize();
		if (nbrOfCreatures != 0) {
			for (int i = 0; i < nbrOfCreatures; i++) {
 				Creature creature = gameMap.getCreatureAt(i);
 				if (kangaroo.getPolygon().getBounds2D()
 						.intersects(creature.getPolygon().getBounds2D())) {
 					if (creature.isKillable()
 							&& kangaroo.getVerticalSpeed() > 0) {
 						gameMap.killCreature(creature);
 						kangaroo.setVerticalSpeed(-6.5f);
 					} else {
 						deathCount++;
 						restartLevel();
 					}
 				}
 			}
		}
 	}
 
 	/**
 	 * If the kangaroo collides with a tile, the kangaroo shall be moved to its
 	 * old position so it looks like it stops at the tile. If the kangaroo hits
 	 * the ground its vertical speed shall be resetted.
 	 */
 	private void tileCollition() {
 		int oldX = oldPos.getX() / Constants.TILE_SIZE;
 		int oldY = oldPos.getY() / Constants.TILE_SIZE;
 		int x = kangaroo.getPosition().getX() / Constants.TILE_SIZE;
 		int y = kangaroo.getPosition().getY() / Constants.TILE_SIZE;
 		for (int i = x; i < x + 2; i++) {
 			for (int j = y; j < y + 3; j++) {
 				Tile tile = gameMap.getTile(i, j);
 
 				if (tile.isCollidable()) {
 					int tileMaxY = (int) tile.getPolygon().getBounds2D()
 							.getMaxY();
 					int tileMinY = (int) tile.getPolygon().getBounds2D()
 							.getMinY();
 					int kangMinY = (int) kangaroo.getPolygon().getBounds2D()
 							.getMinY();
 					int kangMaxY = (int) kangaroo.getPolygon().getBounds2D()
 							.getMaxY();
 					if (oldX != x && Math.abs(tileMaxY - kangMinY) > 2) {
 						kangaroo.setPosition(new Position(oldPos.getX(),
 								kangMinY));
 						kangaroo.setFalling(true);
 					} else if (kangMinY < tileMaxY
 							&& kangaroo.getVerticalSpeed() < 0
 							&& kangMinY > tileMinY) {
 						kangaroo.setPosition(new Position(kangaroo
 								.getPosition().getX(), tileMaxY-1));
 						kangaroo.setVerticalSpeed(0f);
 						kangaroo.setFalling(true);
 					}
 					if (oldY != y
 							&& tileMaxY > kangaroo.getPolygon().getBounds2D()
 									.getMaxY()
 							&& kangaroo.getVerticalSpeed() > kangaroo
 									.getPolygon().getBounds2D().getMaxY()
 									- tileMinY && kangaroo.getPolygon().getBounds2D().getMaxX() > tile.getPolygon().getBounds2D().getMinX()) {
 						kangaroo.setPosition(new Position(kangaroo
 								.getPosition().getX(), tileMinY - 66));
 						kangaroo.setVerticalSpeed(0f);
 						kangaroo.setFalling(false);
 					}
 
 				}
 			}
 		}
 	}
 
 	private void itemCollition() {
 		int x = kangaroo.getPosition().getX() / Constants.TILE_SIZE;
 		int y = kangaroo.getPosition().getY() / Constants.TILE_SIZE;
 		for (int i = x; i < x + 2; i++)
 			for (int j = y; j < y + 3; j++) {
 				Item item = gameMap.getItemAt(i, j);
 				if (item != null)
 					kangaroo.setItem(item);
 			}
 	}
 
 	private void changeFalling() {
 		Rectangle2D kangBounds = kangaroo.getPolygon().getBounds2D();
 		if ((!gameMap.getTile((int) (kangBounds.getMaxX() / 32),
 				(int) (kangBounds.getMaxY() / 32) + 1).isCollidable())
 				&& !gameMap.getTile((int) (kangBounds.getMinX() / 32),
 						(int) (kangBounds.getMaxY() / 32) + 1).isCollidable()) {
 			kangaroo.setFalling(true);
 		}
 
 	}
 
 	/**
 	 * Restarts the level. Will be used when the kangaroo dies.
 	 */
 	public void restartLevel() {
 		deathCount++;
 		kangaroo.reset();
 		gameMap.resetItems();
 		gameMap.resetCreatures();
 	}
 
 	/**
 	 * 
 	 * @return the amount of times the player has died.
 	 */
 	public int getDeathCount() {
 		return deathCount;
 	}
 
 	/**
 	 * 
 	 * @return the kangaroo
 	 */
 	public Kangaroo getKangaroo() {
 		return kangaroo;
 	}
 
 	/**
 	 * 
 	 * @return the gameMap
 	 */
 	public GameMap getGameMap() {
 		return gameMap;
 	}
 
 	/**
 	 * 
 	 * @return the time that has elapsed for the player.
 	 */
 	public double getTime() {
 		return timer.getElapsedTime();
 	}
 
 }
