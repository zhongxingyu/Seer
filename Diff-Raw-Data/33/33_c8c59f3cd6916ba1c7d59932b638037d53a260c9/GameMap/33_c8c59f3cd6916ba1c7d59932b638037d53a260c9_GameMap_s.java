 package com.testgame.mechanics.map;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import android.graphics.Point;
 import android.util.Log;
 
 import com.testgame.mechanics.unit.AUnit;
 import com.testgame.player.APlayer;
 
 public class GameMap implements IMap {
 	
 	/**
 	 * Mapping of coordinate to a unit if that unit occupies it.
 	 * Coordinates formatted as strings as in "x, y".
 	 */
 	protected HashMap<String, AUnit> coordMap;
 
 	
 	/**
 	 * X-dimensionality of the map.
 	 */
 	public int xDim;
 	
 	/**
 	 * Y-dimensionality of the map.
 	 */
 	public int yDim;
 	
 	/**
 	 * Constructor. 
 	 */
 	public GameMap(int xDim, int yDim) {
 		this.coordMap = new HashMap<String, AUnit>();
 		this.xDim = xDim;
 		this.yDim = yDim;
 		
 	}
 
 	/**
 	 * Determines if the point (x,y) is occupied by a unit or some other obstacle.
 	 */
 	@Override
 	public boolean isOccupied(int x, int y) {		
 		if (x < 0 || y < 0) {
 			return true;
 		}
 		
 		if (x >= xDim || y >= yDim) {
 			return true;
 		}
 		
 		if (coordMap.get(entry(x, y)) != null) {
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Returns the occupying unit at point (x,y).
 	 */
 	@Override
 	public AUnit getOccupyingUnit(int x, int y) {
 		return coordMap.get(entry(x,y));
 	}
 
 	/**
 	 * Sets the point (x,y) as occupied.
 	 */
 	@Override
 	public void setOccupied(int x, int y, AUnit unit) {
 		if (x <= (xDim - 1) && x >= 0) {
 			if (y <= (yDim - 1) && y >= 0) {
 				coordMap.put(entry(x,y), unit);
 			}
 		}
 	}
 
 	/**
 	 * Sets the point (x,y) as unoccupied.
 	 */
 	@Override
 	public void setUnoccupied(int x, int y) {
 		if (coordMap.get(entry(x, y)) != null)
 			coordMap.remove(entry(x,y));
 	}
 	
 	//TODO: Actually figure out what this does. Some sort of conversion?
 	@Override
 	public String entry(int x, int y) {
 		return x + ", " + y;
 	}
 
 	
 	/**
 	 * Rreturns a path, array-list of points in order, from start to finish
 	 * @param start start point for the A* path.
 	 * @param dest end point for the A* path.
 	 * @return arrayList containing points in the path. 
 	 */
 	public ArrayList<Point> computePath(Point start, Point dest) {
 		ArrayList<Point> path = new ArrayList<Point>();
 		path.add(dest);
 		
 		HashSet<Point> visited = new HashSet<Point>();
 		
 		HashMap<Point, Point> parents = new HashMap<Point, Point>();
 		parents.put(start, null);
 		
 		ArrayList<Point> frontier = new ArrayList<Point>();
 		frontier.add(start);
 		visited.add(start);
 		
 		// run bfs
 		while (frontier.size() > 0) {
 			Point next = frontier.get(0);
 			frontier.remove(0); // pseudo-pop
 			
 			Point[] neighbors = { new Point(next.x + 1, next.y) , new Point(next.x - 1, next.y), new Point(next.x, next.y + 1),
 					new Point(next.x, next.y -1)};
 			
 			for (Point p : neighbors) {
 				if (!visited.contains(p)) {
 					if (!isOccupied(p.x, p.y)) { 
 						visited.add(p);
 						frontier.add(p);
 						parents.put(p, next);
 					} 
 				}
 			}
 		}
 		
 		// compute path.
 		Point child = dest;
 		while (child != null) {
 			Point parent = parents.get(child);
 			if (parent!=null) path.add(0, parent);
 			child = parent;
 		}
		
 		return path;
 	}
 	
 	/**
 	 * BFS search for targets in a units range.
 	 * @param start Point at which attacking unit is located.
 	 * @param range The distance at which someone can attack from.
 	 * @param me unit in which targets need to be in distance of to attack.
 	 * @return Returns an arraylist of all points of which the unit can attack.
 	 */
 	public HashSet<AUnit> bfsTarget(Point start, int range, APlayer me) {
 		HashSet<AUnit> targets = new HashSet<AUnit>();
 		
 		HashSet<Point> visited = new HashSet<Point>();
 		
 		ArrayList<Point> frontier = new ArrayList<Point>();
 		frontier.add(start);
 		visited.add(start);
 		
 		while (frontier.size() > 0) {
 			Point next = frontier.get(0);
 			frontier.remove(0); // pseudo-pop
 			
 			Point[] neighbors = { new Point(next.x + 1, next.y) , new Point(next.x - 1, next.y), new Point(next.x, next.y + 1),
 					new Point(next.x, next.y -1)};
 			
 			
 			
 			for (Point p : neighbors) {
 				if (!visited.contains(p)) {
 					if (manhattanDistance(start, p) > range) {
 						continue;
 					}
 					AUnit occupyingUnit = getOccupyingUnit(p.x, p.y);
 					if (occupyingUnit != null) {
 						if (!occupyingUnit.unitType.equals("Dummy")) { // not just a map tile
 							if (occupyingUnit.getPlayer() != me) { // can do != since checking for instance
 								targets.add(occupyingUnit);
 								occupyingUnit.inSelectedCharactersAttackRange = true;
 							}
 						}
 					}
 					visited.add(p);
 					frontier.add(p);
 				}
 			}
 			
 		}
 		
 		return targets;
 	}
 	
 	/**
 	 * BFS search for all possible moves
 	 * @param start Start point(Where unit is located)
 	 * @param range Range from which the unit can move.
 	 * @return ArrayList of all possible moves.
 	 */
 	public HashSet<Point> bfs(Point start, int range) {
 		Log.d("Range", range+"");
 		HashSet<Point> accessiblePoints = new HashSet<Point>();
 		
 		HashSet<Point> visited = new HashSet<Point>();
 		
 		ArrayList<Point> frontier = new ArrayList<Point>();
 		frontier.add(start);
 		visited.add(start);
 		int i = 0;
 		boolean pendingdepthIncrease = false;
 		int timetoIncrease = 0;
 		while (frontier.size() > 0) {
 			Point next = frontier.get(0);
 			frontier.remove(0); // pseudo-pop
 			if (i > range) { // within range, accessible
 				break;
 			}
 			else accessiblePoints.add(next); // point not accessible, no point to branch out.
 			
 			Point[] neighbors = { new Point(next.x + 1, next.y) , new Point(next.x - 1, next.y), new Point(next.x, next.y + 1),
 					new Point(next.x, next.y -1)};
 			
 			for (Point p : neighbors) {
 				if (!visited.contains(p)) {
 					if (!isOccupied(p.x, p.y)) {
 						if(pendingdepthIncrease == false){
 							pendingdepthIncrease = true;
 							timetoIncrease = frontier.size();
 						}
 						visited.add(p);
 						frontier.add(p);
 					}
 				}
 				
 			}
 			if(timetoIncrease == 0 && pendingdepthIncrease == true){
 				pendingdepthIncrease = false;
 				i++;
 			}
 			timetoIncrease--;
 			
 		}
 		
 		return accessiblePoints;
 	}
 	
 	/**
 	 * Calculates the manhatten distance from point a to b
 	 * @param a Point a
 	 * @param b Point b
 	 * @return Math.abs(a.x - b.x) +  Math.abs(a.y - b.y)
 	 */
 	public static int manhattanDistance(Point a, Point b) {
 		return Math.abs(a.x - b.x) +  Math.abs(a.y - b.y);
 	}
 
 
 	
 
 }
