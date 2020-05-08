 /*
  * This file is part of the search package.
  *
  * Copyright (C) 2012, Eric Fritz
  * Copyright (C) 2012, Reed Johnson
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
  * and associated documentation files (the "Software"), to deal in the Software without 
  * restriction, including without limitation the rights to use, copy, modify, merge, publish, 
  * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the 
  * Software is furnished to do so, subject to the following conditions: 
  * 
  * The above copyright notice and this permission notice shall be included in all copies or 
  * substantial portions of the Software. 
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING 
  * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
  * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
  */
 
 package edu.uwm.ai.search;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import processing.core.PApplet;
 import edu.uwm.ai.search.agent.PlayerEntity;
 import edu.uwm.ai.search.util.Point;
 
 /**
  * @author Eric Fritz
  * @author ReedJohnson
  */
 public class World
 {
 	private PApplet parent;
 
 	private final int w;
 	private final int h;
 	private final boolean[] obstacles;
 	private final Random random = new Random();
 	private int[] worldMap;
 
 	public World(PApplet parent, int w, int h)
 	{
 		this.parent = parent;
 
 		this.w = w;
 		this.h = h;
 		this.obstacles = new boolean[w * h];
 		this.worldMap = new int[w * h];
 
 		for (int k = 0; k < 20; k++) {
 			int rw = (int) (Math.random() * 5) + 2;
 			int rh = (int) (Math.random() * 5) + 2;
 
 			int i = (int) (Math.random() * (w - rw / 2));
 			int j = (int) (Math.random() * (h - rh / 2));
 
 			for (int dw = 0; dw < rw; dw++) {
 				for (int dh = 0; dh < rh; dh++) {
 					if (j + dh + 5 > h) {
 						continue;
 					}
 
 					setObstacle(i + dw, j + dh);
 				}
 			}
 		}
 	}
 
 	public Point getRandomFreePoint()
 	{
 		Point p;
 
 		do {
 			p = new Point((int) (random.nextDouble() * w), (int) (random.nextDouble() * h));
 		} while (!isValidPosition(p));
 
 		return p;
 	}
 
 	public int getWidth()
 	{
 		return w;
 	}
 
 	public int getHeight()
 	{
 		return h;
 	}
 
 	public boolean isValidPosition(Point p)
 	{
 		return isValidPosition(p.getX(), p.getY());
 	}
 
 	public boolean isValidPosition(int i, int j)
 	{
 		return i >= 0 && j >= 0 && i < w && j < h && !hasObstacle(i, j);
 	}
 
 	public boolean isAccessableThrough(Point dest, Point origin)
 	{
 		if (!isValidPosition(dest) || !isValidPosition(origin)) {
 			throw new IllegalArgumentException("Points are not valid.");
 		}
 
		int xDist = Math.abs(dest.getX() - origin.getX());
		int yDist = Math.abs(dest.getY() - origin.getY());
 
 		if (xDist > 1 || yDist > 1) {
 			throw new IllegalArgumentException("Points are not adjacent.");
 		}
 
 		// If points are touching there cannot be an obstacle between them.
 
 		if ((xDist == 0 && yDist == 1) || (xDist == 1 && yDist == 0)) {
 			return true;
 		}
 
 		// Otherwise ensure that the diagonal move doesn't go through a corner where two separate
 		// walls are joined.
 
 		boolean ob1 = isValidPosition(new Point(origin.getX() + xDist, origin.getY()));
 		boolean ob2 = isValidPosition(new Point(origin.getX(), origin.getY() + yDist));
 
 		return ob1 || ob2;
 	}
 
 	public boolean hasObstacle(Point p)
 	{
 		return hasObstacle(p.getX(), p.getY());
 	}
 
 	public boolean hasObstacle(int i, int j)
 	{
 		return obstacles[getIndex(i, j)];
 	}
 
 	public void setObstacle(int i, int j)
 	{
 		obstacles[getIndex(i, j)] = true;
 	}
 
 	public int getBlockWidth()
 	{
 		return ((Search) parent).getDisplayWidth() / w;
 	}
 
 	public int getBlockHeight()
 	{
 		return ((Search) parent).getDisplayHeight() / h;
 	}
 
 	public void draw()
 	{
 		parent.fill(200, 200, 200);
 		parent.stroke(0);
 
 		int dimW = getBlockWidth();
 		int dimH = getBlockHeight();
 
 		for (int i = 0; i < w; i++) {
 			for (int j = 0; j < h; j++) {
 				if (hasObstacle(i, j)) {
 					parent.rect(i * dimW, j * dimH, dimW, dimH);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Updates the 2D array worldMap with the cost to reach the player entity in each slot. This is
 	 * used by FloodFillSearch to perform search.
 	 * 
 	 * @param player
 	 *            The user's PlayerEntity
 	 * @returns The number of nodes expanded while updating the map.
 	 */
 	public int updateWorldMap(PlayerEntity player)
 	{
 		Point playerPoint = player.getPoint();
 		LinkedList<Point> openList = new LinkedList<Point>();
 		HashSet<Point> closedList = new HashSet<Point>();
 
 		openList.addLast(playerPoint);
 		// Set player's location to 0 cost.
 		worldMap[getIndex(playerPoint.getY(), playerPoint.getX())] = 0;
 		// The map updating actually resembles regular search to a degree.
 		int cost = 0;
 		while (!openList.isEmpty()) {
 			cost++;
 			Point currentPt = openList.pollFirst();
 			int currentCost = worldMap[getIndex(currentPt.getY(), currentPt.getX())];
 			for (Point newP : getSuccessors(currentPt)) {
 
 				// If the point hasn't already been assigned a value, its cost is its parent's cost
 				// plus one.
 				if (!closedList.contains(newP) && !openList.contains(newP)) {
 					worldMap[getIndex(newP.getY(), newP.getX())] = currentCost + 1;
 					openList.addLast(newP);
 				}
 			}
 			closedList.add(currentPt);
 		}
 		return cost;
 	}
 
 	/**
 	 * Takes a point and returns the valid successors in all directions.
 	 * 
 	 * @param p
 	 *            Point object to use as base for search of successors
 	 * @return A List of valid (i.e. unobstructed) successor points.
 	 */
 	private List<Point> getSuccessors(Point p)
 	{
 		List<Point> successors = new ArrayList<Point>(8);
 		successors.add(new Point(p.getX() + 0, p.getY() - 1));
 		successors.add(new Point(p.getX() + 0, p.getY() + 1));
 		successors.add(new Point(p.getX() - 1, p.getY() + 0));
 		successors.add(new Point(p.getX() + 1, p.getY() + 0));
 		successors.add(new Point(p.getX() + 1, p.getY() + 1));
 		successors.add(new Point(p.getX() + 1, p.getY() - 1));
 		successors.add(new Point(p.getX() - 1, p.getY() + 1));
 		successors.add(new Point(p.getX() - 1, p.getY() - 1));
 
 		return pruneInvalid(successors, p);
 	}
 
 	/**
 	 * Takes in a list of points, determines whether or not they are valid (i.e. unobstructed), and
 	 * returns a list of valid points
 	 * 
 	 * @param points
 	 *            A List of point objects to be checked for validity
 	 * @param p
 	 *            The location of the point from which 'points' was generated
 	 * @return A List of valid points.
 	 */
 	private List<Point> pruneInvalid(List<Point> points, Point p)
 	{
 		List<Point> newPoints = new ArrayList<Point>();
 
 		for (Point succ_p : points) {
 			if (isValidPosition(succ_p) && (succ_p.getX() == p.getX() || succ_p.getY() == p.getY() || isAccessableThrough(succ_p, p))) {
 				newPoints.add(succ_p);
 			}
 		}
 
 		return newPoints;
 	}
 
 	/**
 	 * Takes a Point object and returns its distance from the goal according to the worldMap
 	 * 
 	 * @param loc
 	 *            A Point whose distance from the goal is desired
 	 * @return An integer representing the number of steps from loc to the goal.
 	 */
 	public int getCostOfSquare(Point loc)
 	{
 		return worldMap[getIndex(loc.getY(), loc.getX())];
 	}
 
 	private int getIndex(int i, int j)
 	{
 		return j * h + i;
 	}
 }
