 package com.unexpected.ant.model.entity;
 
 import com.unexpected.ant.model.Cell;
 import com.unexpected.ant.model.Direction;
 import com.unexpected.ant.model.EntityVisitor;
 import com.unexpected.ant.model.EntityVisitorAdapter;
 
 import java.util.Map;
 
 /**
  * This class represents the echidna which moves on the game field and eats ants.
  */
 public class Echidna extends MovingEntity {
 	private int hunger = 5;
	private int sleepCounter = 0;
 
 	public Echidna(Direction facingDirection) {
 		super(facingDirection);
 	}
 
 	public Echidna() {
 		super();
 	}
 
 	@Override
 	public void action(long tickCount) {
 		if (isHungry() && !isAsleep()) {
 			eat();
 		}
 		if (tickCount % 8 == 0 && !isAsleep()) {
 			move();
 		}
 		if (sleepCounter == 1) {
 			setHunger(5);
 		}
 		if (isAsleep()) {
 			sleepCounter--;
 		}
 	}
 
 	/**
 	 * The echidna eats ants
 	 */
 	public void eat() {
 		getVisitedByEntitiesOnSameCells(new EatVisitor());
 	}
 
 	@Override
 	public void acceptVisitor(EntityVisitor visitor) {
 		visitor.visit(this);
 	}
 
 	/**
 	 * Returns if the echidna is hungry
 	 *
 	 * @return True if hungry, false otherwise
 	 */
 	public boolean isHungry() {
 		return getHunger() > 0;
 	}
 
 	/**
 	 * Returns the level of hunger
 	 *
 	 * @return The level of hunger
 	 */
 	public int getHunger() {
 		return hunger;
 	}
 
 	/**
 	 * Sets the level of hunger
 	 *
 	 * @param hunger the level of hunger
 	 */
 	public void setHunger(int hunger) {
 		this.hunger = hunger;
 	}
 
 	/**
 	 * Returns if the echidna is sleeping
 	 *
 	 * @return True if sleeping, false otherwise
 	 */
 	public boolean isAsleep() {
 		return sleepCounter > 0;
 	}
 
 	/**
 	 * Command the echidna to move
 	 */
 	public void move() {
 		Cell nextCell = decideNextCell();
 		moveTo(nextCell);
 	}
 
 	/**
 	 * Decides about the next cell to move on to
 	 *
 	 * @return The next cell
 	 */
 	public Cell decideNextCell() {
 		for (Map.Entry<Direction, Cell> neighbour : getCell().getNeighboursMap().entrySet()) {
 			Direction direction = neighbour.getKey();
 			Cell cell = neighbour.getValue();
 //          MoveVisitor visitor = new MoveVisitor();
 			cell.getNeighbour(direction);
 		}
 
 		return getCell().getNeighbours().get(0);
 	}
 
 	@Override
 	public double rateCell(Cell cell, Direction direction) {
 		return 1;
 	}
 
 	/**
 	 * Commands the echidna to sleep for the given amount of time
 	 *
 	 * @param amount The amount of time to sleep for
 	 */
 	public void sleep(int amount) {
 		sleepCounter = amount;
 	}
 
 	/**
 	 * The echidna moves to the given cell
 	 *
 	 * @param cell The cell to move to
 	 */
 	public void moveTo(Cell cell) {
 		this.remove();
 		cell.addEntity(this);
 		this.cells.clear();
 		this.cells.add(cell);
 	}
 
 	/**
 	 * This class is part of the visitor pattern for eating ants
 	 */
 	protected class EatVisitor extends EntityVisitorAdapter {
 		public void visit(Ant ant) {
 			hunger--;
 			ant.remove();
 			if (!isHungry()) {
 				sleepCounter = 40;
 			}
 		}
 	}
 
 	protected class SmellVisitor extends com.unexpected.ant.model.entity.SmellVisitor {
 
 		@Override
 		public void visit(AntSmell antSmell) {
 			smell += antSmell.getIntensity() * 2;
 		}
 	}
 }
