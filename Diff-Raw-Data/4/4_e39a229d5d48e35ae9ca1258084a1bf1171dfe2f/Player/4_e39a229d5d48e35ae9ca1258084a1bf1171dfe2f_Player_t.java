 package com.github.joakimpersson.tda367.model.player;
 
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.github.joakimpersson.tda367.model.constants.Attribute;
 import com.github.joakimpersson.tda367.model.constants.Direction;
 import com.github.joakimpersson.tda367.model.constants.Parameters;
 import com.github.joakimpersson.tda367.model.constants.PointGiver;
 import com.github.joakimpersson.tda367.model.constants.ResetType;
 import com.github.joakimpersson.tda367.model.player.PlayerAttributes.UpgradeType;
 import com.github.joakimpersson.tda367.model.utils.FPosition;
 import com.github.joakimpersson.tda367.model.utils.Position;
 
 /**
  * This class defines a Player in the bomberman-like game.
  * 
  * @author Adrian Bjugrd
  * @modified Viktor Anderling, Joakim Persson
  */
 public class Player {
 	/**
 	 * TimerTask initiated getFireDuration()-milliseconds after player has been
 	 * hit by fire.
 	 */
 	private class HitTask extends TimerTask {
 		/**
 		 * Initiated when player is hit.
 		 */
 		public HitTask() {
 			health--;
 			justHit = true;
 		}
 
 		@Override
 		public void run() {
 			justHit = false;
 		}
 	}
 
 	private final String name;
 	private final Position initialPosition;
 	private Position tilePos;
 	private FPosition gamePos;
 	private PlayerAttributes attr;
 	private PlayerPoints points;
 	private Direction facingDirection;
 	private int bombsPlaced, health, playerIndex;
 	private boolean justHit;
 
 	/**
 	 * Creates a player with a pre-defined position and name.
 	 * 
 	 * @param name
 	 *            The name of the player.
 	 * @param pos
 	 *            The starting position of a player.
 	 */
 	public Player(int playerIndex, String name, Position pos) {
 		this.playerIndex = playerIndex;
 		this.name = name;
 		this.initialPosition = pos;
 		initPlayer();
 	}
 
 	/**
 	 * Method for initialising a player.
 	 */
 	private void initPlayer() {
 		this.attr = new PlayerAttributes();
 		this.points = new PlayerPoints();
 		roundReset();
 	}
 
 	/**
 	 * Method used to reset a players state for a new round.
 	 */
 	private void roundReset() {
 		this.attr.resetAttr(UpgradeType.Round);
 		this.health = getAttribute(Attribute.Health);
 		this.tilePos = initialPosition;
 		this.facingDirection = Direction.SOUTH;
 		this.gamePos = new FPosition(initialPosition.getX() + 0.5F,
 				initialPosition.getY() + 0.5F);
 		this.bombsPlaced = 0;
 		this.justHit = false;
 	}
 
 	/**
 	 * Method used to reset a players state for a new match (3 rounds).
 	 */
 	private void matchReset() {
 		this.attr.resetAttr(UpgradeType.Match);
 		roundReset();
 	}
 
 	public void reset(ResetType type) {
 		switch (type) {
 		case Match:
 			matchReset();
 			break;
 		case Round:
 			roundReset();
 			break;
 		default:
 			// TODO nothing should happen here
 			// it is possible that the game type falls through
 			break;
 		}
 	}
 
 	/**
 	 * Moves a player in specified direction.
 	 * 
 	 * @param dir
 	 *            The direction in which the player will move.
 	 */
 	public void move(Direction dir) {
 		double stepSize = Parameters.INSTANCE.getPlayerStepSize();
 		double newFX = gamePos.getX() + stepSize * dir.getX();
 		double newFY = gamePos.getY() + stepSize * dir.getY();
 		gamePos = new FPosition((float) newFX, (float) newFY);
 		tilePos = new Position((int) newFX, (int) newFY);
 		this.facingDirection = dir;
 	}
 
 	/**
 	 * @return If a player can place a bomb or not.
 	 */
 	public boolean canPlaceBomb() {
 		if (getAttribute(Attribute.BombStack) > this.bombsPlaced) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Increases the current number of bombs placed by the player.
 	 */
 	public void increaseBombsPlaced() {
 		this.bombsPlaced++;
 	}
 
 	/**
 	 * Decreases the current number of bombs placed by the player.
 	 */
 	public void decreaseBombsPlaced() {
 		this.bombsPlaced--;
 	}
 
 	public int getBombsAvailable() {
 		return getAttribute(Attribute.BombStack) - this.bombsPlaced;
 	}
 
 	/**
 	 * Upgrade either a round or match attribute with one level.
 	 * 
 	 * @param attr
 	 *            The attribute to be upgraded
 	 * @param type
 	 *            The type of the upgrade
 	 */
 	public void upgradeAttr(Attribute attr, UpgradeType type) {
 		this.attr.upgradeAttr(attr, type);
 		if (type.equals(UpgradeType.Match)) {
 			this.reloadAttributes();
 		}
 	}
 
 	/**
 	 * Method for getting a players attributes.
 	 * 
 	 * @return A players list of attributes.
 	 */
 	public List<Attribute> getPermanentAttributes() {
 		return this.attr.getAttributes();
 	}
 
 	/**
 	 * Method for getting the value of a specific requested attribute.
 	 * 
 	 * @param attr
 	 *            The type of attribute requested.
 	 * @return The value of the attribute requested.
 	 */
 	public int getAttribute(Attribute attr) {
 		return this.attr.getAttrValue(attr);
 	}
 
 	/**
 	 * Method called when player is hurt by fire.
 	 */
 	public void playerHit() {
 		if (this.justHit == false) {
 			Timer justHitTimer = new Timer();
 			justHitTimer.schedule(new HitTask(),
 					Parameters.INSTANCE.getFireDuration());
 		}
 	}
 
 	/**
 	 * Check whether player is alive.
 	 * 
 	 * @return Player's vitals.
 	 */
 	public boolean isAlive() {
 		return this.health > 0;
 	}
 
 	/**
 	 * Method to get a players score.
 	 * 
 	 * @return The players score.
 	 */
 	public int getScore() {
 		return points.getScore();
 	}
 
 	/**
 	 * Method to get a players available credits.
 	 * 
 	 * @return The players available credits.
 	 */
 	public int getCredits() {
 		return points.getCredits();
 	}
 
 	/**
 	 * Method which uses a players credits.
 	 * 
 	 * @param cost
 	 *            Amount of credits used.
 	 */
 	public void useCredits(int cost) {
 		this.points.useCredits(cost);
 	}
 
 	/**
 	 * This will update a players points with a list of PointGiver's.
 	 * 
 	 * @param pointGivers
 	 *            List containing PointGiver's.
 	 */
 	public void updatePlayerPoints(List<PointGiver> pointGivers) {
 		this.points.update(pointGivers);
 	}
 
 	public void updatePlayerPoints(PointGiver pointGiver) {
 		this.points.update(pointGiver);
 
 	}
 
 	/**
 	 * Get the players name.
 	 * 
 	 * @return Name of the player.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Get the players current health.
 	 * 
 	 * @return Current health of the player.
 	 */
 	public int getHealth() {
 		return health;
 	}
 
 	/**
 	 * Get the current Position of the player in the tile grid.
 	 * 
 	 * @return Which position the player has in the tile grid.
 	 */
 	public Position getTilePosition() {
 		return tilePos;
 	}
 
 	/**
 	 * Get the current FPosition of the player in the game grid.
 	 * 
 	 * @return Where the player is on the game grid.
 	 */
 	public FPosition getGamePosition() {
 		return gamePos;
 	}
 
 	public Direction getDirection() {
 		return facingDirection;
 	}
 
 	@Override
 	public String toString() {
 		return "P[" + this.name + ", " + this.tilePos + ", " + this.health
 				+ " HP]";
 	}
 
 	/**
 	 * Get the players current status whether is has been hit by an exploded
 	 * bomb or not
 	 * 
 	 * @return Whether the players has recently been hit by a bomb
 	 */
 	public boolean isImmortal() {
 		return justHit;
 	}
 
 	/**
 	 * Sets the players immortality to either true or false.
 	 */
 	public void removeImmortality() {
 		justHit = false;
 	}
 
 	/**
 	 * Returns the amount of certain destroyed PointGiver type by this player.
 	 * 
 	 * @param type
 	 *            type of PointGiver tile
 	 * @return The number of destroyed tile type in PointGiver
 	 */
 	public int getDestroyedPointGiver(PointGiver type) {
 		return points.getDestroyedPointGiver(type);
 	}
 
 	public void killPlayer() {
 		this.health = 0;
 	}
 
 	public String getImageString() {
 		return "player/" + playerIndex + "/still-" + facingDirection;
 	}
 
 	public PlayerPoints getPoints() {
 		return points;
 	}
 
 	public int getIndex() {
 		return playerIndex;
 	}
 
 	private void reloadAttributes() {
 		health = getAttribute(Attribute.Health);
 	}
 
 	public void adjustPosition(Direction direction) {
 		if (direction.equals(Direction.NORTH)
 				|| direction.equals(Direction.SOUTH)) {
 			gamePos = new FPosition(tilePos.getX() + 0.5F, gamePos.getY());
 		} else {
 			gamePos = new FPosition(gamePos.getX(), tilePos.getY() + 0.5F);
 		}
 	}
 
 	@Override
 	public int hashCode() {
 		int sum = 0;
 		sum += this.playerIndex * 5;
 		sum += this.name.hashCode() * 7;
 		sum += this.attr.hashCode() * 13;
 		sum += this.tilePos.hashCode() * 17;
 		// TODO jocke this makes the program crash
 		// sum += this.points.hashCode() * 19;
 		return sum;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null || getClass() != obj.getClass()) {
 			return false;
 		}
 		Player other = (Player) obj;
		return this.name.equals(other.name) && this.attr.equals(other.attr)
				&& this.points.equals(other.points) && this.tilePos == other.tilePos
 				&& this.playerIndex == other.playerIndex;
 
 	}
 }
