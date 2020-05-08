 /*
  * PathFinder.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.prealpha.aichallenge;
 
 import java.util.List;
 
 import com.prealpha.aichallenge.protocol.GameMap;
 import com.prealpha.aichallenge.protocol.Point;
 
 abstract class PathFinder {
 	private final GameMap map;
 
 	private final Point end;
 
 	private final PathSegment[][] segments;
 
 	private final boolean[][] extended;
 
 	protected PathFinder(GameMap map, Point start, Point end) {
 		this.map = map;
 		this.end = end;
 
 		int rows = map.getRows();
 		int cols = map.getCols();
 		segments = new PathSegment[rows][cols];
 		segments[start.getRow()][start.getCol()] = new PathSegment(start);
 		extended = new boolean[rows][cols];
 	}
 
	protected List<Point> findPath() {
 		while (segments[end.getRow()][end.getCol()] == null) {
 			extend(findShortestSegment());
 		}
 		return segments[end.getRow()][end.getCol()].collapse();
 	}
 
 	private PathSegment findShortestSegment() {
 		PathSegment shortSegment = null;
 		double shortDistance = Double.MAX_VALUE;
 		for (int i = 0; i < map.getRows(); i++) {
 			for (int j = 0; j < map.getCols(); j++) {
 				if (segments[i][j] != null && !extended[i][j]) {
 					double distance = getDistance(segments[i][j]);
 					if (distance < shortDistance) {
 						shortSegment = segments[i][j];
 						shortDistance = distance;
 					}
 				}
 			}
 		}
 		return shortSegment;
 	}
 
 	private void extend(PathSegment segment) {
 		for (Point point : map.getAdjacent(segment.getLocation())) {
 			if (segments[point.getRow()][point.getCol()] == null) {
 				segments[point.getRow()][point.getCol()] = segment
 						.extend(point);
 			}
 		}
 		extended[segment.getLocation().getRow()][segment.getLocation().getCol()] = true;
 	}
 
 	protected abstract double getDistance(PathSegment segment);
 }
