 package es.eucm.eadventure.engine.core.control.functionaldata;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import es.eucm.eadventure.common.data.chapter.InfluenceArea;
 import es.eucm.eadventure.common.data.chapter.Trajectory;
 import es.eucm.eadventure.common.data.chapter.Trajectory.Node;
 import es.eucm.eadventure.common.data.chapter.Trajectory.Side;
 
 /**
  * Functional Trajectory
  *
  * @author Eugenio Marchiori
  */
 public class FunctionalTrajectory {
 
 	/**
 	 * The trajectory
 	 */
 	private Trajectory trajectory;
 	
 	/**
 	 * The node the player is currently at
 	 */
 	private Node currentNode;
 	
 	/**
 	 * The side the player is currently at
 	 */
 	private Side currentSide;
 	
 	/**
 	 * The path the player is currently following
 	 */
 	private List<FunctionalSide> currentPath;
 	
 	/**
 	 * The nearest point to the desired one along the x-axis the player can reach
 	 */
 	private int nearestX;
 	
 	/**
 	 * The nearest point to the desired one along the y-axis the player con reach
 	 */
 	private int nearestY;
 	
 	/**
 	 * The speed along the x-axis
 	 */
 	private float speedX;
 	
 	/**
 	 * The speed along the y-axis
 	 */
 	private float speedY;
 	
 	/**
 	 * The scale the player must take in the current position
 	 */
 	private float scale = 1.0f;
 	
 	private boolean getsTo;
 	
 	private List<FunctionalBarrier> barriers;
 	
 	private List<FunctionalSide> sides;
 	
 	private FunctionalElement destinationElement;
 	
 	/**
 	 * Create a new FunctionalTrajectory form a Trajectory
 	 * 
 	 * @param trajectory the trajectory
 	 * @param barriers 
 	 */
 	public FunctionalTrajectory(Trajectory trajectory, ArrayList<FunctionalBarrier> barriers) {
 		this.trajectory = trajectory;
 		sides = new ArrayList<FunctionalSide>();
 		this.barriers = barriers;
 		if (trajectory != null) {
 			for (Side side : trajectory.getSides()) {
 				sides.add(new FunctionalSide(side, trajectory, false));
 				sides.add(new FunctionalSide(side, trajectory, true));
 			}
 			currentSide = null;
 			currentNode = trajectory.getInitial();
 		}
 	}
 	
 	/**
 	 * Indicates if it has a trajectory
 	 * 
 	 * @return boolean indicating if it has a trajectory
 	 */
 	public boolean hasTrajectory() {
 		return trajectory != null;
 	}
 		
 	/**
 	 *  
 	 * @param fromX The present position along the x-axis of the player
 	 * @param fromY The present position along the y-axis of the player
 	 * @param toX The desired position along the x-axis for the player
 	 * @param toY The desired position along the y-axis for the player
 	 */
 	public void updatePathToNearestPoint(float fromX, float fromY, int toX, int toY) {
 		this.currentPath = pathToNearestPoint(fromX, fromY, toX, toY);
 	}
 	
 	private List<FunctionalSide> pathToNearestPoint(float fromX, float fromY, int toX, int toY) {		
 		List<FunctionalSide> currentSides = getCurrentValidSides();
 		
 		List<FunctionalPath> tempPaths = new ArrayList<FunctionalPath>();
 		
 		for (FunctionalSide currentSide : currentSides) {
 			List<FunctionalSide> tempSides = new ArrayList<FunctionalSide>();
 			tempSides.add(currentSide);
 			double xsq = Math.pow(fromX - currentSide.getEndNode().getX(), 2);
 			double ysq = Math.pow(fromY - currentSide.getEndNode().getY(), 2);
 			float dist = (float) Math.sqrt(xsq + ysq);
 			FunctionalPath newPath = new FunctionalPath(dist, Float.MAX_VALUE, tempSides);
 			tempPaths.add(newPath);
 		}
 		
 		List<FunctionalPath> fullPathList = getFullPathList(tempPaths);
 
 		List<FunctionalPath> validPaths;
 		
 		validPaths = getValidPaths(fullPathList, fromX, fromY, toX, toY);
 			
 		Collections.sort(validPaths);
 
 		FunctionalPath bestPath = validPaths.get(validPaths.size() - 1);
 		this.nearestX = (int) bestPath.getDestX();
 		this.nearestY = (int) bestPath.getDestY();
 		this.currentNode = null;
 		this.currentSide = bestPath.getSides().get(0).getSide();
 		this.getsTo = bestPath.isGetsTo();
 //		for (FunctionalSide fs : bestPath.getSides()) {
 //			System.out.print(fs.getSide() + "--->");
 //		}
 //		System.out.println();
 		return bestPath.getSides();
 	}
 
 	private List<FunctionalPath> getValidPaths(List<FunctionalPath> fullPathList, float fromX, float fromY, int toX, int toY) {
 		List<FunctionalPath> validPaths = new ArrayList<FunctionalPath>();
 		
 		for (FunctionalPath tempPath : fullPathList) {
 			double xsq = Math.pow(fromX - tempPath.getSides().get(0).getEndNode().getX(), 2);
 			double ysq = Math.pow(fromY - tempPath.getSides().get(0).getEndNode().getY(), 2);
 			float length = (float) Math.sqrt(xsq + ysq);
 			FunctionalPath newPath = new FunctionalPath(length, Float.MAX_VALUE, new ArrayList<FunctionalSide>());
 			newPath.addSide(0, Float.MAX_VALUE, tempPath.getSides().get(0));
 			
 			float posX = fromX;
 			float posY = fromY;			
 			
 			boolean end = false;
 			int sideNr = 1;
 			while (!end && sideNr <= tempPath.getSides().size()) {
 				
 				Node endNode = newPath.getSides().get(sideNr - 1).getEndNode();
 				float deltaX = endNode.getX() - posX;
 				float deltaY = endNode.getY() - posY;
 				
 				int delta = (int) (Math.abs(deltaX) > Math.abs(deltaY) ? Math.abs(deltaX) : Math.abs(deltaY));
 				
 				for (int i = 0; i < delta && !end; i++) {
 					if (Math.abs(deltaX) > Math.abs(deltaY)) {
 						posX = posX + deltaX / Math.abs(deltaX);
 						posY = posY + deltaY / Math.abs(deltaX);
 					} else {
 						posY = posY + deltaY / Math.abs(deltaY);
 						posX = posX + deltaX / Math.abs(deltaY);
 					}
 					if (inBarrier(posX, posY))
 						end = true;
 					
 					if (destinationElement != null && inInfluenceArea(posX, posY)) {
 						float dist = (float) Math.sqrt(Math.pow(posX - destinationElement.getX(), 2) + Math.pow(posY - destinationElement.getY(), 2));
 						newPath.updateUpTo(dist, posX, posY);
 						newPath.setGetsTo(true);
 					} else if (destinationElement == null){
 						float dist = (float) Math.sqrt(Math.pow(posX - toX, 2) + Math.pow(posY - toY, 2));
 						newPath.updateUpTo(dist, posX, posY);
 					}
 				}
 				
 				validPaths.add(newPath);
 				if (sideNr < tempPath.getSides().size()) {
 					newPath = newPath.newFunctionalPath(tempPath.getSides().get(sideNr).getLenght(), Float.MAX_VALUE, tempPath.getSides().get(sideNr));
 					posX = tempPath.getSides().get(sideNr).getStartNode().getX();
 					posY = tempPath.getSides().get(sideNr).getStartNode().getY();
 				}
 				sideNr++;
 			}
 		}
 		
 		return validPaths;
 	}
 	
 	private boolean inBarrier(float posX, float posY) {
 		boolean temp = false;
 		for (FunctionalBarrier barrier : barriers) {
 			temp = temp || barrier.isInside(posX, posY);
 		}
 		return temp;
 	}
 	
 	private boolean inInfluenceArea(float posX, float posY) {
 		if (destinationElement == null)
 			return false;
 		else {
 			InfluenceArea area = destinationElement.getInfluenceArea();
 			int x1 = (int) (destinationElement.getX() - destinationElement.getWidth() * destinationElement.getScale() / 2);
 			int y1 = (int) (destinationElement.getY() - destinationElement.getHeight() * destinationElement.getScale());
 			int x2 = 0;
 			int y2 = 0;
 			if (!area.isExists()) {
 				x1 = x1 - 20;
 				y1 = y1 - 20;
 				x2 = (int) (x1 + destinationElement.getWidth() * destinationElement.getScale() + 40);
 				y2 = (int) (y1 + destinationElement.getHeight() * destinationElement.getScale() + 40);
 			} else {
 				x1 = x1 + area.getX();
 				y1 = y1 + area.getY();
 				x2 = x1 + area.getWidth();
 				y2 = y1 + area.getHeight();
 			}
 			
 			if (posX > x1 && posX < x2 && posY > y1 && posY < y2)
 				return true;
 		}
 		return false;
 	}
 		
 	private List<FunctionalPath> getFullPathList(List<FunctionalPath> tempPaths) {
 		List<FunctionalPath> fullPathList = new ArrayList<FunctionalPath>();
 		
 		while(!tempPaths.isEmpty()) {	
 			FunctionalPath originalPath = tempPaths.get(0);
 			tempPaths.remove(0);
 			
 			FunctionalSide lastSide = originalPath.getSides().get(originalPath.getSides().size() - 1);
 	
 			boolean continues = false;
 			for (FunctionalSide side : sides) {
 				if (side.getSide() != lastSide.getSide() && side.getStartNode().getID().equals(lastSide.getEndNode().getID())) {
 					FunctionalPath temp = originalPath.newFunctionalPath(side.getLenght(), 0, side);
 					if (temp != null) {
 						tempPaths.add(temp);
 						continues = true;
 					}
 				}
 			}
 			if (!continues)
 				fullPathList.add(originalPath);
 		}
 		
 		return fullPathList;
 	}
 	
 	private List<FunctionalSide> getCurrentValidSides() {
 		List<FunctionalSide> tempList = new ArrayList<FunctionalSide>();
 		if (currentNode != null) {
 			for (FunctionalSide side : sides) {
 				if (side.getStartNode() == currentNode)
 					tempList.add(side);
 			}
 		} else {
 			for (FunctionalSide side : sides) {
 				if (side.getSide() == currentSide)
 					tempList.add(side);
 			}
 		}
 		return tempList;
 	}
 	
 	
 	
 	/**
 	 * Updates the value of speedX, speedY and scale for the player to move
 	 * 
 	 * @param elapsedTime The time elapsed since the last update
 	 * @param x The position of the player along the x-axis
 	 * @param y The position of the player along the y-axis
 	 * @param speed The linear speed of the player
 	 */
 	public void updateSpeeds(long elapsedTime, float x, float y, float speed) {
 		if (currentPath != null) {
 			Node nextNode = getNextNode();
 			moveInDirectionToTheNode(elapsedTime, x, y, speed, nextNode);
 		} else {
 			speedX = 0;
 			speedY = 0;
 		}
 	}
 	
 	/**
 	 * Recalculate speedX, speedY and scale when the player must move in
 	 * the direction of a given node
 	 * 
 	 * @param elapsedTime The time elapsed since the last update
 	 * @param x The position of the player along the x-axis
 	 * @param y The position of the player along the y-axis
 	 * @param nextNode the node the player must reach
  	 * @param speed The linear speed of the player
 	 */
 	private void moveInDirectionToTheNode(long elapsedTime, float x, float y,
 			float speed, Node nextNode) {
 		float distX = nextNode.getX() - x;
 		float distY = nextNode.getY() - y;
 		double dist = Math.sqrt(distX * distX + distY * distY);
 		double rectDist = Math.abs(distX) + Math.abs(distY);
 		float distX2 = nearestX - x;
 		float distY2 = nearestY - y;
 		double dist2 = Math.sqrt(distX2 * distX2 + distY2 * distY2);
 		if (dist2 <= 10) {
 			speedX = 0;
 			speedY = 0;
 			currentPath = null;
 			nearestX = (int) x;
 			nearestY = (int) y;
 		} else if (dist >= speed * elapsedTime / 1000) {
 			speedX = (float) (distX / rectDist * speed);
 			speedY = (float) (distY / rectDist * speed);
 		} else {
 			FunctionalSide currentFunctionalSide = null;
 			for (FunctionalSide side : currentPath) {
 				if (side.getSide() == currentSide)
 					currentFunctionalSide = side;
 			}
 			if (currentPath.indexOf(currentFunctionalSide) < currentPath.size() - 1) {
 				currentSide = currentPath.get(currentPath.indexOf(currentFunctionalSide) + 1).getSide();
 			} else
 				currentPath = null;
 			updateSpeeds(elapsedTime, x, y, speed);
 		}
 		
 		Node endNode = nextNode;
 		Node startNode;
 		if (currentSide.getIDEnd().equals(nextNode.getID())) {
 			startNode = trajectory.getNodeForId(currentSide.getIDStart());
 		} else {
 			startNode = trajectory.getNodeForId(currentSide.getIDEnd());
 		}
 		updateScale(dist, startNode, endNode);
 	}
 
 	
 	/**
 	 * Update the scale of the player, when dist2 has been covered between
 	 * the start and end node.
 	 * 
 	 * @param dist2 The distance already covered
 	 * @param startNode the start node of the side
 	 * @param endNode the end node of the side
 	 */
 	private void updateScale(double dist2, Node startNode, Node endNode) {
 		scale = (float) (startNode.getScale() * dist2 / currentSide.getLength() + endNode.getScale() * (1 - (dist2 / currentSide.getLength())));
 	}
 
 	/**
 	 * Get the next node along the path
 	 * 
 	 * @return the next node along the path
 	 */
 	private Node getNextNode() {
 		FunctionalSide currentFunctionalSide = null;
 		for (FunctionalSide side : currentPath) {
 			if (side.getSide() == currentSide)
 				currentFunctionalSide = side;
 		}
 		if (currentFunctionalSide == null) 
 			return null;
 		return currentFunctionalSide.getEndNode();
 	}
 
 	private class FunctionalPath implements Comparable<FunctionalPath> {
 
 		private float length;
 		
 		private List<FunctionalSide> sides;
 		
 		private float destX;
 		
 		private float destY;
 		
 		private boolean getsTo;
 		
 		private float distance;
 		
 		public FunctionalPath(float length, float distance, List<FunctionalSide> sides) {
 			this.length = length;
 			this.sides = new ArrayList<FunctionalSide>(sides);
 			this.distance = distance;
 			getsTo = false;
 		}
 
 		public void setLength(float length) {
 			this.length = length;
 		}
 
 		public void setGetsTo(boolean b) {
 			getsTo = true;
 		}
 
 		public boolean isGetsTo() {
 			return getsTo;
 		}
 		
 		public void updateUpTo(float dist, float posX, float posY) {
 			if (dist < distance) {
 				destX = posX;
 				destY = posY;
 				distance = dist;
 			}
 		}
 
 		public List<FunctionalSide> getSides() {
 			return sides;
 		}
 		
 		public List<Side> getNormalSides() {
 			List<Side> temp = new ArrayList<Side>();
 			for (FunctionalSide side : sides)
 				temp.add(side.getSide());
 			return temp;
 		}
 		
 		public float getLength() {
 			return length;
 		}
 		
 		private void addSide(float lenght, float distance, FunctionalSide side) {
 			sides.add(side);
 			this.length += lenght;
 			this.distance = distance;
 		}
 
 		public FunctionalPath newFunctionalPath(float length, float distance, FunctionalSide side) {
 			if (sides.contains(side))
 				return null;
 			for (FunctionalSide tempSide : sides)
 				if (tempSide.getSide() == side.getSide())
 					return null;
 			
 			FunctionalPath temp = new FunctionalPath(this.length, this.distance, this.sides);
 			temp.addSide(length, distance, side);
 			return temp;
 		}
 
		@Override
 		public int compareTo(FunctionalPath arg0) {
 			if (this.getsTo && !arg0.getsTo) {
 				return 1;
 			} else if (!this.getsTo && arg0.getsTo) {
 				return -1;
 			}
 			int distDif = (int) (arg0.distance - distance);
 			if (Math.abs(distDif) < 10) {
 				return (int) (length - arg0.length);
 			} else {
 				return  distDif;
 			}
 		}		
 		
 		public float getDestX() {
 			return destX;
 		}
 		
 		public float getDestY() {
 			return destY;
 		}
 	}
 	
 	/**
 	 * Returns the value of speedX
 	 * @return the value of speedX
 	 */
 	public float getSpeedX() {
 		return speedX;
 	}
 
 	/**
 	 * Returns the value of speedY
 	 * 
 	 * @return the value of speedY
 	 */
 	public float getSpeedY() {
 		return speedY;
 	}
 
 	/**
 	 * Returns the x-axis value of the initial node
 	 * 
 	 * @return the x-axis value of the initial node
 	 */
 	public float getInitialX() {
 		return (float) trajectory.getInitial().getX();
 	}
 
 	/**
 	 * Returns the y-axis value of the initial node
 	 * 
 	 * @return the y-axis value of the initial node
 	 */
 	public float getInitialY() {
 		return (float) trajectory.getInitial().getY();
 	}
 
 	/**
 	 * Returns the value of scale
 	 * 
 	 * @return the value of scale
 	 */
 	public float getScale() {
 		return scale;
 	}
 
 	public void setDestinationElement(FunctionalElement element) {
 		this.destinationElement = element;
 	}
 	
 	public boolean canGetTo() {
 		return getsTo;
 	}
 
 	public Node changeInitialNode(int destinyX, int destinyY) {
 		currentSide = null;
 		float minDist = Float.MAX_VALUE;
 		for (Node node : trajectory.getNodes()) {
 			float dx = (float) Math.pow(node.getX() - destinyX, 2);
 			float dy = (float) Math.pow(node.getY() - destinyY, 2);
 			float dist = (float) Math.sqrt(dx + dy);
 			if (dist < minDist) {
 				currentNode = node;
 				minDist = dist;
 			}
 		}
 		return currentNode;
 	}
 }
