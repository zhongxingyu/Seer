 package schematicMap;
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.NoSuchElementException;
 import java.util.PriorityQueue;
 import java.util.Stack;
 import java.util.concurrent.Semaphore;
 
 public class SchematicMap {
 	private static int POINT_SELECTION_RADIUS = 5;
 	private static int SEGMENT_SELECTION_WIDTH = 3;
 	
 	
 	private LinkedList<MapPoint> map;
 	private LinkedList<MapObject> selected;
 	private MapPoint lastAdded;
 	
 	
 	public SchematicMap() {
 		map = new LinkedList<MapPoint>();
 		selected = new LinkedList<MapObject>();
 	}
 	
 	public synchronized void addPoint(int x, int y, Color color) {
 		lastAdded = new MapPoint(x, y, color);
 		map.add(lastAdded);
 		
 	}
 
 	public synchronized void removePoint(int x, int y) {
 		
 		removePoint(pointAt(x,y));
 		
 	}
 	
 	public synchronized void removePoint(MapPoint toRemove) {
 		
 		// remove all adjacent edges from all neighbor nodes
 		for (MapSegment edge : toRemove.getAdjacencies()) {
 			edge.getOpposite(toRemove).removeAdjacency(edge);
 		}
 		
 		// if this is the last added, remove
 		if (toRemove == lastAdded) {
 			lastAdded = null;
 		}
 		
 		// remove from all lists we're keeping here
 		try {
 			selected.remove(toRemove);
 			map.remove(toRemove);
 		} catch (NoSuchElementException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 	public synchronized void deleteSegment(MapSegment ms) {
 		
 		// delete this segment from all adjacent nodes
 		for (MapPoint pt : map) {
 			for (MapSegment seg : pt.getAdjacencies()) {
 				if (seg == ms) {
 					pt.removeAdjacency(ms);
 				}
 			}
 		}
 		
 		// delete from selected cache
 		for (MapObject obj : selected) {
 			if (obj == ms) {
 				selected.remove(ms);
 			}
 		}
 	}
 
 	public synchronized void connectPoints(int x1, int y1, int x2, int y2, Color c) {
 		connectPoints(pointAt(x1,y1), pointAt(x2,y2), c);
 	}
 
 	public synchronized void connectPoints(int x, int y, MapPoint m, Color c) {
 		connectPoints(pointAt(x,y), m, c);
 	}
 
 	public synchronized void connectPoints(MapPoint m1, MapPoint m2, Color c) {
 		
 		MapSegment newSeg = new MapSegment(c, m1, m2);
 		m1.addAdjacency(newSeg);
 		m2.addAdjacency(newSeg);
 		
 		
 	}
 
 	public synchronized Point lastPointAdded()  {
 		
 		Point result = lastAdded.getCoordinates(); 
 		return result;
 		
 	}
 
 
 	public synchronized MapPoint pointAt(int x, int y) {
 		
 		MapPoint result = null;
 		double minDistance = Double.MAX_VALUE;
 		
 		for (MapPoint pt : map) {
 			Point theseCoords = pt.getCoordinates();
 			double thisDistance = theseCoords.distance(new Point(x,y));
 			
 			if (thisDistance <= POINT_SELECTION_RADIUS && thisDistance < minDistance) {
 				result = pt;
 			}
 		}
 		
 		
 		return result;
 	}
 
 	public synchronized MapSegment segmentAt(int x, int y) {
 		
 		MapSegment result = null;
 		double minDistance = Double.MAX_VALUE;
 		Point thisPoint = new Point(x,y);
 		
 		for (MapPoint pt : map) {
 			for (MapSegment seg : pt.getAdjacencies()) {
 				double thisDistance = seg.distanceTo(thisPoint);
 				
 				if (thisDistance <= SEGMENT_SELECTION_WIDTH && thisDistance < minDistance) {
 					result = seg;
 				}
 			}
 		}
 		
 		return result;
 	}
 	
 	/*-------------------------------------------
 	 * Select/Deselect Map Components
 	 *------------------------------------------
 	 */
 	 
 	public synchronized void selectObject(MapObject obj) {
 		Class type = obj.getClass();
 		
 		// if this is a point
 		if (type.equals(MapPoint.class)) {
 			selectPoint((MapPoint) obj);
 		}
 		
 		// if this is a segment
 		else if (type.equals(MapSegment.class)) {
 			selectSegment((MapSegment) obj);
 		}
 	}
 
 
 	public synchronized void selectPoint(int x, int y) {
 		selectPoint(pointAt(x,y));
 	}
 	
 	public synchronized void selectPoint(MapPoint pt) {
 				
 		selected.add(pt);
 		pt.select();
 		
 		
 	}
 	
 	public synchronized void deselectObject(MapObject obj) {
 		Class type = obj.getClass();
 		
 		// if this is a point
 		if (type.equals(MapPoint.class)) {
 			deselectPoint((MapPoint) obj);
 		}
 		
 		// if this is a segment
 		else if (type.equals(MapSegment.class)) {
 			deselectSegment((MapSegment) obj);
 		}
 	}
 	
 	public synchronized void deselectPoint(int x, int y) {
 		deselectPoint(pointAt(x,y));
 	}
 	
 	public synchronized void deselectPoint(MapPoint pt) {
 				
 		selected.remove(pt);
 		pt.deselect();
 		
 	}
 	
 	public synchronized void selectSegment(int x, int y) {
 		selectSegment(segmentAt(x,y));
 	}
 	
 	public synchronized void selectSegment(MapSegment seg) {
 				
 		selected.add(seg);
 		seg.select();
 		
 	}
 	
 	public synchronized void deselectSegment(int x, int y) {
 		deselectSegment(segmentAt(x,y));
 	}
 	
 	public synchronized void deselectSegment(MapSegment seg) {
 				
 		if (seg != null) {
 			seg.deselect();
 			selected.remove(seg);
 		}
 		
 		
 	}
 
 	public synchronized void deselectAll() {
 				
 		for (MapObject obj : selected) {
 			obj.deselect();
 		}
 		
 		int numSelected = selected.size();
 		
 		for(int i = 0; i < numSelected; i++) {
 			selected.removeFirst();
 		}
 		
 	}
 
 	public synchronized void removeSelection() {
 				
 		Stack<MapObject> toRemove = new Stack<MapObject>();
 		
 		toRemove.addAll(selected);
 		
 		int numToRemove = toRemove.size();
 		
 		for(int i = 0; i < numToRemove; i++) {
 			MapObject obj = toRemove.pop();
 			
 			// if this is a point
 			if (obj.getClass().equals(MapPoint.class)) {
 				removePoint((MapPoint) obj);
 				
 			// if this is a segment	
 			} else {
 				deleteSegment((MapSegment) obj);
 			}
 		}
 	}
 
 	public synchronized void updateSelectionColor(Color c) {
 		for (MapObject obj : selected) {
 			obj.setColor(c);
 		}
 	}
 
 
 	public synchronized void updateSelectionLoc(int dx, int dy) {
 		// copy elements of selection into a queue of elements to update
 		LinkedList<MapObject> toUpdate = new LinkedList<MapObject>();
 		for (MapObject obj : selected) {
 			toUpdate.add(obj);
 		}
 		
 		
 		while(!toUpdate.isEmpty()) {
 			
 			MapObject curObj = toUpdate.peekFirst();
 			
 			// if current object is a point
 			// update its location
 			// remove from queue
 			if (curObj.getClass().equals(MapPoint.class)) {
 				MapPoint curPoint = (MapPoint) toUpdate.removeFirst();
 				
 				Point curLoc = curPoint.getCoordinates();
 				curPoint.setCoordinates(curLoc.x + dx, curLoc.y + dy);
 			}
 			
 			// else if it is a segment, update the location of
 			// its endpoints, remove both the segment and its
 			// endpoints from the queue
 			else if (curObj.getClass().equals(MapSegment.class)) {
 				MapSegment curSegment = (MapSegment) toUpdate.removeFirst();
 				
 				MapPoint[] points = curSegment.getMapPoints();
 				
 				for (int i = 0; i < 2; i++) {
 					MapPoint curPoint = points[i];
 					
 					if (toUpdate.contains(curPoint)) {
 						Point curLoc = curPoint.getCoordinates();
 						curPoint.setCoordinates(curLoc.x + dx, curLoc.y + dy);
 						toUpdate.remove(curPoint);
 					}
 				}
 			}
 			
 		}
 	}

 	public synchronized boolean projectSelectionHorizontal() {
 		double totalLength = selectionPathLength();
 		
 		if (totalLength < 0) {
 			return false;
 		}
 		
 		int numPoints = (selected.size() - 1) / 2;
 		
 		if (numPoints < 2) {
 			return false;
 		}
 		
 		MapPoint leftmost = null;
 		int smallestX = Integer.MAX_VALUE;
 		MapPoint rightmost = null;
 		int largestX = Integer.MIN_VALUE;
 		
 		MapPoint leftTerminus = null;
 		
 		// pick leftmost and rightmost selected points
 		for (MapObject curObj : selected) {
 			if (curObj.isPoint()) {
 				MapPoint curPoint = (MapPoint) curObj;
 				
 				int thisX = curPoint.getCoordinates().x;
 				if (thisX < smallestX) {
 					smallestX = thisX;
 					leftmost = curPoint;
 				} 
 				
 				if (thisX > largestX) {
 					rightmost = curPoint;
 					largestX = thisX;
 				}
 				
 				int degree = 0;
 				
 				for (MapSegment curAdj : curPoint.getAdjacencies()) {
 					if (selected.contains(curAdj)) {
 						degree++;
 					}
 				}
 				
 				if (degree == 1) {
 					if (leftTerminus == null) {
 						leftTerminus = curPoint;
 					} else if (leftTerminus.getCoordinates().x > curPoint.getCoordinates().x) {
 						leftTerminus = curPoint;
 					}
 				}
 			}
 		}
 		
 		int finalLength = largestX - smallestX;
 		
 		leftTerminus.setCoordinates(leftmost.getCoordinates().x, leftTerminus.getCoordinates().y);
 	}
 
 
 	public synchronized boolean projectSelectionVertical() {
 		
 	}
 
 	public synchronized boolean projectSelectionPosSlope() {
 		
 	}
 
 	public synchronized boolean projectSelectionNegSlope() {
 		
	}
 
 	public synchronized MapObject[] getSelected() {
 		MapObject[] result = (MapObject[]) selected.toArray();
 		
 		return result;
 	}
 	
 	public synchronized MapObject[] getMapState() {
 		ArrayList<MapObject> dynamicState = new ArrayList<MapObject>();
 		
 		for(MapPoint curPoint : map) {
 			dynamicState.add(curPoint);
 			
 			for (MapSegment curSegment : curPoint.getAdjacencies()) {
 				if (!dynamicState.contains(curSegment) && curSegment != null) {
 					dynamicState.add(curSegment);
 				}
 			}
 		}
 		
 		MapObject[] state = new MapObject[dynamicState.size()];
 		
 		int i = 0;
 		for (MapObject curObj : dynamicState) {
 			state[i] = curObj;
 			i++;
 		}
 		
 		return state;
 	}
 	
 	public synchronized void selectSeries(int startx, int starty, int endx, int endy) {
 		MapPoint start = pointAt(startx, starty);
 		MapPoint end = pointAt(endx, endy);
 		
 		if (start != null && end != null) {
 			MapObject[] path =  shortestPath(start, end);
 			
 			for (MapObject obj : path) {
 				this.selectObject(obj);
 			}
 		}
 	}
 	
 	protected synchronized MapObject[] shortestPath(MapPoint src, MapPoint dst) {
 		if (src == dst) {
 			return new MapObject[0];
 		}
 		
 		PriorityQueue<MapPoint> queue = new PriorityQueue<MapPoint>();
 		PriorityQueue<MapPoint> examined = new PriorityQueue<MapPoint>();
 				
 		// add all to a priority queue
 		// queue is sorted on distance from src
 		for (MapPoint curPoint : map) {
 			curPoint.dijDistance = Double.MAX_VALUE;
 			curPoint.dijPreviousHop = null;
 			
 			queue.offer(curPoint);
 		}
 		
 		queue.remove(src);
 		src.dijDistance = 0;
 		queue.offer(src);
 		
 		MapPoint curPoint = null;
 		
 		while(!queue.isEmpty()) {
 			curPoint = queue.poll();
 			
 			if (curPoint.dijDistance == Double.MAX_VALUE
 					|| curPoint == dst) {
 				break;
 			} 
 			
 			for (MapSegment curAdj : curPoint.getAdjacencies()) {
 				MapPoint curNeighbor = curAdj.getOpposite(curPoint);
 				
 				if (!examined.contains(curNeighbor)) { // if the current neighbor's path hasn't already stabilized
 					double alternateDist = curPoint.dijDistance + curAdj.getLength();
 					
 					if (alternateDist < curNeighbor.dijDistance) {
 						queue.remove(curNeighbor);
 						
 						curNeighbor.dijDistance = alternateDist;
 						curNeighbor.dijPreviousHop = curAdj;
 						
 						queue.offer(curNeighbor);
 					}	
 				}
 			}
 			
 			examined.add(curPoint);
 		}
 		
 		Stack<MapObject> path = new Stack<MapObject>();
 		
 		curPoint = dst;
 		while(curPoint != src) {
 			path.push(curPoint);
 			path.push(curPoint.dijPreviousHop);
 			
 			curPoint = curPoint.dijPreviousHop.getOpposite(curPoint);
 		}
 		
 		path.push(src);
 		
 		int numElements = path.size();
 		MapObject[] resultPath = new MapObject[numElements];
 		
 		for (int i = 0; i < numElements; i++) {
 			resultPath[i] = path.pop();
 		}
 		
 		return resultPath;
 	}	
 	
 	protected synchronized boolean selectionIsColinear() {
 		if (!selectionIsPath()) {
 			return false;
 		}
 		
 		MapPoint leftmost = null;
 		int smallestX = Integer.MAX_VALUE;
 		MapPoint point2 = null;
 		
 		// pick two points from the selection
 		for (MapObject curObj : selected) {
 			if (curObj.isPoint()) {
 				MapPoint curPoint = (MapPoint) curObj;
 				
 				int thisX = curPoint.getCoordinates().x;
 				if (thisX < smallestX) {
 					smallestX = thisX;
 					point2 = leftmost;
 					leftmost = curPoint;
 				} else {
 					point2 = curPoint;
 				}
 			}
 		}
 		
 		// determine the slope of the vector between the points
 		int dx = point2.getCoordinates().x - leftmost.getCoordinates().x;
 		int dy = point2.getCoordinates().y - leftmost.getCoordinates().y;
 		
 		double m = dy/dx;
 		
 		// for each other point in the selection, determine if
 		// the vector between the first point and it are
 		// coincident to the first slope calculated within rounding error
 		
 		int firstX = leftmost.getCoordinates().x;
 		int firstY = leftmost.getCoordinates().y;
 		
 		for (MapObject curObj : selected) {
 			if (curObj.isPoint() && curObj!= leftmost) {
 				MapPoint curPoint = (MapPoint) curObj;
 				
 				dx = curPoint.getCoordinates().x - firstX;
 				dy = curPoint.getCoordinates().y - firstY;
 				
 				// if slopes differ, return false
 				if (Math.abs(m - dy/dx) > .01) {
 					return false;
 				}
 			}
 		}
 		
 		return true;
 	}
 	
 	public synchronized boolean selectionIsPath() {
 		LinkedList<MapObject> selectedQueue = new LinkedList<MapObject>();
 		
 		// copy selected elements, and pick some start point
 		MapPoint startPoint = null;
 		
 		for (MapObject curObj : selected) {
 			if (curObj.isPoint()) {
 				startPoint = (MapPoint) curObj;
 			}
 			
 			selectedQueue.add(curObj);
 		}
 		
 		
 		// if there were no points in the selection, return false
 		if (startPoint == null) {
 			return false;
 		}
 		
 		// from the start point trace a path to an endpoint
 		selectedQueue.remove(startPoint);
 		if (canTracePath(selectedQueue, startPoint)) {
 			
 			// if the queue is exhausted (we must have started with
 			// a terminating point of the path) return true
 			if (selectedQueue.isEmpty()) {
 				return true;
 			} 
 			
 			// else see if se can trace a path to the other terminating
 			// endpoint, if there are no elements left in the selected 
 			// queue after we're done tracing this path, we're done,
 			// else this is not a path
 			if (canTracePath(selectedQueue, startPoint)) {
 				if (selectedQueue.isEmpty()) {
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	protected synchronized boolean canTracePath(LinkedList<MapObject> selectedQueue, MapPoint startPoint) {
 		for (MapSegment curAdj : startPoint.getAdjacencies()) {
 			
 			// if there is an edge in the selection connected to this node
 			if (selectedQueue.contains(curAdj)) {
 				//remove that edge from the queue
 				selectedQueue.remove(curAdj);
 				
 				// if the neighbor along that edge is also in the selection
 				MapPoint curNeighbor = curAdj.getOpposite(startPoint);
 				if (selectedQueue.contains(curNeighbor)) {
 					
 					// see if we can trace a path from this neighbor along unvisited
 					// edges in the selection
 					selectedQueue.remove(curNeighbor);
 					return canTracePath(selectedQueue, curNeighbor);
 				} else {
 					
 					// we've got a hanging edge without a bounding node
 					return false;
 				}
 			}
 		}
 		
 		// this is the endpoint
 		return true;
 	}
 	
 	/**
 	 * @return The collective length of all segments along the selected path,
 	 * or -1 if the selection is not a path
 	 */
 	protected synchronized double selectionPathLength() {
 		if (!selectionIsPath()) {
 			return -1;
 		}
 		
 		double count = 0;
 		
 		for (MapObject curObj : selected) {
 			if (curObj.isSegment()) {
 				MapSegment curSegment = (MapSegment) curObj;
 				
 				count += curSegment.getLength();
 			}
 		}
 		
 		return count;
 	}
 }
