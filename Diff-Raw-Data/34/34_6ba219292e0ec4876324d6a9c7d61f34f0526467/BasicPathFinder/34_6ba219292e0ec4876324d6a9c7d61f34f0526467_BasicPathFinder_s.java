 /*
  * BasicPathFinder.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.prealpha.aichallenge;
 
 import com.prealpha.aichallenge.protocol.GameMap;
 import com.prealpha.aichallenge.protocol.Point;
 
 class BasicPathFinder extends PathFinder {
 	private final GameMap map;
 
 	private final Point end;
 
 	public BasicPathFinder(GameMap map, Point start, Point end) {
 		super(map, start, end);
 		this.map = map;
 		this.end = end;
 	}
 
 	@Override
 	protected double getDistance(PathSegment segment) {
 		int cost = segment.getGeneration();
 		int heuristic = map.getManhattanDistance(segment.getLocation(), end);
		return cost + (1.001 * heuristic);
 	}
 }
