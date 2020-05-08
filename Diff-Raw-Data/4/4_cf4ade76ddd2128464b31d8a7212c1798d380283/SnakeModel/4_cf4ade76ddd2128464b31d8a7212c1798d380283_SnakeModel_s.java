 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package se.chalmers.dat255.sleepfighter.challenge.rotosnake;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Deque;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import se.chalmers.dat255.sleepfighter.utils.geometry.Dimension;
 import se.chalmers.dat255.sleepfighter.utils.geometry.Direction;
 import se.chalmers.dat255.sleepfighter.utils.geometry.Position;
 
 /**
  * Snake game. Original author Mazdak, modified by Laszlo for SleepFighter.
  */
 public class SnakeModel {
 	/*
 	 * -------------------------------- Position/States.
 	 * --------------------------------
 	 */
 	/** Random Number Generator (RNG) */
 	private final Random rng;
 
 	/** The position of the current snake-food. */
 	private Position currFoodPos;
 
 	/** A list containing all snake positions. */
 	private final Deque<Position> snakePos;
 
 	/** A list containing all empty positions. */
 	private final List<Position> emptyPos;
 
 	/** The direction of the snake. */
 	private Direction direction;
 
 	/** The number of snake-food eaten. */
 	private int score;
 
 	/** The size of the board. */
 	private Dimension boardSize;
 
 	/** The size of a tile. */
 	private int tileSize;
 
 	/** Whether game is over or not. */
 	private boolean gameOver;
 
 	/*
 	 * -------------------------------- Getters.
 	 * --------------------------------
 	 */
 	/**
 	 * Returns the current food position.
 	 * 
 	 * @return Returns the current food position.
 	 */
 	public Position getFoodPosition() {
 		return this.currFoodPos;
 	}
 
 	/**
 	 * Checks if food exists on the board.
 	 * 
 	 * @return True, if there is food.
 	 */
 	public boolean hasFood() {
 		return currFoodPos != null;
 	}
 
 	/**
 	 * Returns an immutable view of the collection of snakes positions.
 	 * 
 	 * @return Collection of snakes positions.
 	 */
 	public Collection<Position> getSnakePositions() {
 		return Collections.unmodifiableCollection(this.snakePos);
 	}
 
 	/**
 	 * Returns an immutable view of the list of empty positions.
 	 * 
 	 * @return List of empty positions.
 	 */
 	public List<Position> getEmptyPositions() {
 		return Collections.unmodifiableList(this.emptyPos);
 	}
 
 	/**
 	 * Returns the size of the game board as a Dimension object.
 	 * 
 	 * @return Size of the game board.
 	 */
 	public Dimension getBoardSize() {
 		return boardSize;
 	}
 
 	public boolean isGameOver() {
 		return gameOver;
 	}
 
 	/**
 	 * Returns whether or not a position is the head of the snake. Since snake
 	 * always has a head this method will never cause NullPointerException.
 	 * 
 	 * @return true if position is the head of snake, otherwise false.
 	 */
 	public boolean isPositionHead(Position pos) {
 		return this.snakePos.peekFirst().equals(pos);
 	}
 
 	/**
 	 * Creates a new model of snake-game.
 	 * 
 	 * @param size
 	 *            The size of the board.
 	 * @param startDirection
 	 *            The direction where snakes head will be pointed at.
 	 * @param rng
 	 *            Random Number Generator.
 	 */
 	public SnakeModel(Dimension size, Direction startDirection, Random rng) {
 
 		// Init RNG.
 		this.rng = rng;
 
 		// Set start direction.
 		this.direction = startDirection;
 
 		// Set board size.
 		this.boardSize = size;
 
 		// Set tile size.
 		this.tileSize = SnakeConstants.getTileSize();
 
 		// Set game status.
 		this.gameOver = false;
 
 		// Blank out the whole gameboard.
 		this.emptyPos = new ArrayList<Position>(size.getWidth()
 				* size.getHeight() - this.tileSize);
 		for (int i = this.tileSize; i < size.getWidth(); i++) {
 			for (int j = this.tileSize; j < size.getHeight(); j++) {
 				this.emptyPos.add(new Position(i, j));
 			}
 		}
 
 		/*
 		 * Insert the snake, starting with first one at the middle.
 		 * 
 		 * In order not to make it too difficult for client we will add
 		 * snake-tiles exceeding the first one to opposite of
 		 * starting-direction.
 		 * 
 		 * Then we must add all exceeding first to the end so that the head (
 		 * index = 0 ) is first in starting direction. The starting direction
 		 * must then be reset to default.
 		 */
 		this.snakePos = new LinkedList<Position>();
		this.addSnake(new Position((int) Math.ceil(size.getWidth() / 2),
				(int) Math.ceil(size.getHeight() / 2)));
 
 		int initLength = SnakeConstants.getInitialSnakeLength();
 		if (initLength > 1) {
 			Direction oldDirection = this.direction;
 			this.direction = this.direction.getOpposite();
 
 			for (int i = 1; i < initLength; i++) {
 				this.addSnake(this.getNextSnakePos());
 			}
 
 			this.direction = oldDirection;
 		}
 
 		// Add starting snake-food.
 		this.addFood();
 	}
 
 	/*
 	 * -------------------------------- Direction logic.
 	 * --------------------------------
 	 */
 	/**
 	 * Update the direction of the snake.
 	 * 
 	 * @param The
 	 *            wanted direction.
 	 */
 	public void updateDirection(Direction newDirection) {
 		// Don't change direction if it is opposite to current one or if it is
 		// contained by snake.
 		// This is one of the features of THIS snake.
 		if (!newDirection.isOpposite(this.direction)
 				|| snakePos.contains(snakePos.peek()
 						.moveDirection(newDirection))) {
 			this.direction = newDirection;
 		}
 	}
 
 	/*
 	 * -------------------------------- Tick handling.
 	 * --------------------------------
 	 */
 	/**
 	 * Updates the model periodically. Central game logic for snake.
 	 * 
 	 * @throws GameOverException
 	 *             When game is over.
 	 */
 	public void tickUpdate() throws GameOverException {
 		// Get the new head-position of snake.
 		Position newHeadPos = this.getNextSnakePos();
 
 		// Check if there's food at the snakes head.
 		// If yes: Award client with score and add a new snake-food (if not
 		// possible or if reached victory condition -> game over).
 		// If not: Transfer the previous snake tail position to empty positions
 		// and remove head from empty positions.
 		if (isCollision(newHeadPos, this.currFoodPos)) {
 			this.score++;
 			if (this.emptyPos.isEmpty()
 					|| score == SnakeConstants.getVictoryCondition()) {
 				this.gameOver();
 			} else {
 				this.addFood();
 			}
 		} else {
 			this.emptyPos.add(this.snakePos.removeLast());
 			this.emptyPos.remove(newHeadPos);
 		}
 
 		// Game Over if snake is out of bounds.
 		if (isOutOfBounds(newHeadPos)) {
 			this.gameOver();
 		}
 
 		// Add head at new position.
 		this.snakePos.addFirst(newHeadPos);
 	}
 
 	/**
 	 * Issues game-over by throwing GameOverException.
 	 * 
 	 * @throws GameOverException
 	 */
 	private void gameOver() throws GameOverException {
 		gameOver = true;
 		throw new GameOverException(this.score);
 	}
 
 	/**
 	 * Transfer first random position from empty ones to currFoodPos.
 	 */
 	private void addFood() {
 		// Randomly select an empty position to remove and set current food
 		// position to that one.
 		this.currFoodPos = this.emptyPos.get(this.rng.nextInt(this.emptyPos
 				.size()));
 	}
 
 	/**
 	 * Helper method for adding new snake tiles to a position.
 	 * 
 	 * @param pos
 	 *            The position in which to add a snake tile.
 	 */
 	private void addSnake(Position pos) {
 		this.emptyPos.remove(pos);
 		this.snakePos.add(pos);
 	}
 
 	/**
 	 * Get next position of the snake.
 	 * 
 	 * @return a new Position object representing next position.
 	 */
 	private Position getNextSnakePos() {
 		return this.snakePos.getFirst().moveDirection(this.direction);
 	}
 
 	/**
 	 * @param newHeadPos
 	 * @param otherPos
 	 * @return true if Snake (newHeadPos) collides with another object
 	 *         (typically fruit) occupying otherPos
 	 */
 	private boolean isCollision(Position newHeadPos, Position otherPos) {
 		boolean collision = false;
 
 		if (otherPos.equals(newHeadPos)) {
 			collision = true;
 		}
 		return collision;
 	}
 
 	/**
 	 * Check if Position pos is out of bounds on Android device.
 	 */
 	private boolean isOutOfBounds(Position pos) {
 		return pos.getX() < tileSize || pos.getY() < tileSize
 				|| pos.getY() > boardSize.getHeight() - tileSize
 				|| pos.getX() > boardSize.getWidth() - tileSize;
 	}
 
 }
