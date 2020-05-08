 /*
  * GameMap.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.prealpha.aichallenge.protocol;
 
import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
import java.util.List;
 import java.util.Set;
 
 public final class GameMap {
 	private final int rows;
 
 	private final int cols;
 
 	private final Ilk[][] ilk;
 
 	private final Set<Point> myAnts = new HashSet<Point>();
 
 	private final Set<Point> enemyAnts = new HashSet<Point>();
 
 	private final Set<Point> myHills = new HashSet<Point>();
 
 	private final Set<Point> enemyHills = new HashSet<Point>();
 
 	private final Set<Point> foodTiles = new HashSet<Point>();
 
 	/**
 	 * Creates a new {@code GameMap} with the specified dimensions. All tiles
 	 * are initially assumed to be land.
 	 * 
 	 * @param rows
 	 *            game map height
 	 * @param cols
 	 *            game map width
 	 */
 	GameMap(int rows, int cols) {
 		this.rows = rows;
 		this.cols = cols;
 		ilk = new Ilk[rows][cols];
 		for (Ilk[] row : ilk) {
 			Arrays.fill(row, Ilk.LAND);
 		}
 	}
 
 	/**
 	 * Returns game map height.
 	 * 
 	 * @return game map height
 	 */
 	public int getRows() {
 		return rows;
 	}
 
 	/**
 	 * Returns game map width.
 	 * 
 	 * @return game map width
 	 */
 	public int getCols() {
 		return cols;
 	}
 
 	/**
 	 * Returns ilk at the specified location.
 	 * 
 	 * @param point
 	 *            location on the game map
 	 * 
 	 * @return ilk at the <cod>tile</code>
 	 */
 	public Ilk getIlk(Point point) {
 		return this.ilk[point.getRow()][point.getCol()];
 	}
 
 	/**
 	 * Returns ilk at the location in the specified direction from the specified
 	 * location.
 	 * 
 	 * @param point
 	 *            location on the game map
 	 * @param direction
 	 *            direction to look up
 	 * 
 	 * @return ilk at the location in <code>direction</code> from
 	 *         <cod>tile</code>
 	 */
 	public Ilk getIlk(Point point, Aim direction) {
 		Point newTile = getPoint(point, direction);
 		return this.ilk[newTile.getRow()][newTile.getCol()];
 	}
 
 	/**
 	 * Returns location in the specified direction from the specified location.
 	 * 
 	 * @param point
 	 *            location on the game map
 	 * @param direction
 	 *            direction to look up
 	 * 
 	 * @return location in <code>direction</code> from <cod>tile</code>
 	 */
 	public Point getPoint(Point point, Aim direction) {
 		int row = (point.getRow() + direction.getRowDelta()) % rows;
 		if (row < 0) {
 			row += rows;
 		}
 		int col = (point.getCol() + direction.getColDelta()) % cols;
 		if (col < 0) {
 			col += cols;
 		}
 		return new Point(row, col);
 	}
 
	public Set<Point> getAdjacent(Point center) {
		Set<Point> adjacent = new HashSet<Point>(4);
		for (Aim direction : Aim.values()) {
			adjacent.add(getPoint(center, direction));
		}
		return adjacent;
	}

 	/**
 	 * Returns a set containing all my ants locations.
 	 * 
 	 * @return a set containing all my ants locations
 	 */
 	public Set<Point> getMyAnts() {
 		return Collections.unmodifiableSet(myAnts);
 	}
 
 	/**
 	 * Returns a set containing all enemy ants locations.
 	 * 
 	 * @return a set containing all enemy ants locations
 	 */
 	public Set<Point> getEnemyAnts() {
 		return Collections.unmodifiableSet(enemyAnts);
 	}
 
 	/**
 	 * Returns a set containing all my hills locations.
 	 * 
 	 * @return a set containing all my hills locations
 	 */
 	public Set<Point> getMyHills() {
 		return Collections.unmodifiableSet(myHills);
 	}
 
 	/**
 	 * Returns a set containing all enemy hills locations.
 	 * 
 	 * @return a set containing all enemy hills locations
 	 */
 	public Set<Point> getEnemyHills() {
 		return Collections.unmodifiableSet(enemyHills);
 	}
 
 	/**
 	 * Returns a set containing all food locations.
 	 * 
 	 * @return a set containing all food locations
 	 */
 	public Set<Point> getFoodTiles() {
 		return Collections.unmodifiableSet(foodTiles);
 	}
 
 	/**
 	 * Calculates distance between two locations on the game map.
 	 * 
 	 * @param p1
 	 *            one location on the game map
 	 * @param p2
 	 *            another location on the game map
 	 * @return distance between {@code p1} and {@code p2}
 	 */
 	public double getDistance(Point p1, Point p2) {
 		int rowDelta = getRowDelta(p1, p2);
 		int colDelta = getColDelta(p1, p2);
 		return Math.sqrt(rowDelta * rowDelta + colDelta * colDelta);
 	}
 
 	public int getManhattanDistance(Point p1, Point p2) {
 		return getRowDelta(p1, p2) + getColDelta(p1, p2);
 	}
 
 	private int getRowDelta(Point p1, Point p2) {
 		int rowDelta = Math.abs(p1.getRow() - p2.getRow());
 		return Math.min(rowDelta, rows - rowDelta);
 	}
 
 	private int getColDelta(Point p1, Point p2) {
 		int colDelta = Math.abs(p1.getCol() - p2.getCol());
 		return Math.min(colDelta, cols - colDelta);
 	}
 
 	/**
 	 * Returns one or two orthogonal directions from one location to the
 	 * another.
 	 * 
 	 * @param p1
 	 *            one location on the game map
 	 * @param p2
 	 *            another location on the game map
 	 * @return orthogonal directions from {@code p1} to {@code p2}
 	 */
	public List<Aim> getDirections(Point p1, Point p2) {
		List<Aim> directions = new ArrayList<Aim>();
 		if (p1.getRow() < p2.getRow()) {
 			if (p2.getRow() - p1.getRow() >= rows / 2) {
 				directions.add(Aim.NORTH);
 			} else {
 				directions.add(Aim.SOUTH);
 			}
 		} else if (p1.getRow() > p2.getRow()) {
 			if (p1.getRow() - p2.getRow() >= rows / 2) {
 				directions.add(Aim.SOUTH);
 			} else {
 				directions.add(Aim.NORTH);
 			}
 		}
 		if (p1.getCol() < p2.getCol()) {
 			if (p2.getCol() - p1.getCol() >= cols / 2) {
 				directions.add(Aim.WEST);
 			} else {
 				directions.add(Aim.EAST);
 			}
 		} else if (p1.getCol() > p2.getCol()) {
 			if (p1.getCol() - p2.getCol() >= cols / 2) {
 				directions.add(Aim.EAST);
 			} else {
 				directions.add(Aim.WEST);
 			}
 		}
 		return directions;
 	}
 
 	public Set<Point> getAdjacent(Point center) {
 		Set<Point> adjacent = new HashSet<Point>();
 		for (Aim direction : Aim.values()) {
			Point target = getTile(center, direction);
 			if (getIlk(target).isPassable()) {
 				adjacent.add(target);
 			}
 		}
 		return adjacent;
 	}
 
 	/**
 	 * Clears game state information about my ants locations.
 	 */
 	void clearMyAnts() {
 		for (Point myAnt : myAnts) {
 			this.ilk[myAnt.getRow()][myAnt.getCol()] = Ilk.LAND;
 		}
 		myAnts.clear();
 	}
 
 	/**
 	 * Clears game state information about enemy ants locations.
 	 */
 	void clearEnemyAnts() {
 		for (Point enemyAnt : enemyAnts) {
 			this.ilk[enemyAnt.getRow()][enemyAnt.getCol()] = Ilk.LAND;
 		}
 		enemyAnts.clear();
 	}
 
 	/**
 	 * Clears game state information about my hills locations.
 	 */
 	void clearMyHills() {
 		myHills.clear();
 	}
 
 	/**
 	 * Clears game state information about enemy hills locations.
 	 */
 	void clearEnemyHills() {
 		enemyHills.clear();
 	}
 
 	void clearFoodTiles() {
 		foodTiles.clear();
 	}
 
 	/**
 	 * Updates game state information about new ants and food locations.
 	 * 
 	 * @param tile
 	 *            location on the game map to be updated
 	 * @param ilk
 	 *            ilk to be updated
 	 */
 	void update(Point tile, Ilk ilk) {
 		this.ilk[tile.getRow()][tile.getCol()] = ilk;
 
 		switch (ilk) {
 		case FOOD:
 			foodTiles.add(tile);
 			break;
 		case MY_ANT:
 			myAnts.add(tile);
 			break;
 		case ENEMY_ANT:
 			enemyAnts.add(tile);
 			break;
 		}
 	}
 
 	/**
 	 * Updates game state information about hills locations.
 	 * 
 	 * @param tile
 	 *            location on the game map to be updated
 	 * @param owner
 	 *            owner of hill
 	 */
 	void updateHills(Point tile, int owner) {
 		if (owner > 0) {
 			enemyHills.add(tile);
 		} else {
 			myHills.add(tile);
 		}
 	}
 }
