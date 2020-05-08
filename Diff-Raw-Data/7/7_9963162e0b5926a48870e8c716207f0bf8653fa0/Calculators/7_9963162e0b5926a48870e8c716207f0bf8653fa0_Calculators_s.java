 package roborally.utils;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import roborally.basics.Energy;
 import roborally.basics.Orientation;
 import roborally.basics.Position;
 import roborally.model.Node;
 import roborally.model.Robot;
 
 public class Calculators {
 
 	public int calculateManhattan(Position pos1, Position pos2){
 		return (int) (Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getY() - pos2.getY()));
 	} 
 		
 	
 	public HashMap<Position,Node> aStar(Robot a, Position pos){
 		HashMap<Position,Node> openSet = new HashMap<Position,Node>(); 
 		// de experimentele posities die nog gevalueerd moeten/kunnen worden
 		Node startNode = new Node(a.getPosition(), 0 , getHCost(a.getPosition(), a.getOrientation(),pos),a.getOrientation(), null);
 		openSet.put(a.getPosition(), startNode);
 		// de startPositie aan de open list toevoegen
 		HashMap<Position,Node> closedSet = new HashMap<Position, Node>(); 
 		// de lijst met al gevalueerde posities
 		HashMap<Position,Node> travelledSet = new HashMap<Position,Node>();
 		// de afgelegde weg 
 		
 		while ( !openSet.isEmpty()){
 			Node currentNode = getMinimalFNode(openSet);
 			if (pos.getNeighbours().contains(currentNode.getPosition())){
 				return travelledSet;
 			}
 			openSet.remove(currentNode.getPosition());
 	        closedSet.put(currentNode.getPosition(), currentNode);
 	        travelledSet.put(currentNode.getPosition(), currentNode);
 	        
 	        ArrayList<Position> neighbours = currentNode.getPosition().getNeighbours();
 	        for (Position neighbour : neighbours){
 	        	if (closedSet.containsKey(neighbour))
 	        		continue;
 	        	
 	        	double tentativeGScore = getGCost(currentNode,neighbour);
 	        	boolean tentativeIsBetter = false;
 	        	
 	        	if (!openSet.containsKey(neighbour)){
 	        		openSet.put(neighbour,new Node(neighbour,getGCost(currentNode, neighbour),getHCost(neighbour, getNodeOrientation(currentNode, neighbour),pos),
 	        				getNodeOrientation(currentNode, neighbour),currentNode));
 	        		tentativeIsBetter = true;
 	        	}
 	        	else if (tentativeGScore < openSet.get(neighbour).getGCost())
 	        		tentativeIsBetter = true;
 	        	else
 	        		tentativeIsBetter = false;	        		
 	        	
 	        	if (tentativeIsBetter == true){
 	        		openSet.get(neighbour).setParent(currentNode);
 	        		openSet.get(neighbour).setGCost(tentativeGScore);
 	        	}
 	        }
 			
 		}
 		return closedSet;
 
 	}
 	
 	private Orientation getNodeOrientation(Node currentNode, Position pos) {
 		Position previousPosition = currentNode.getPosition();
 		if (previousPosition.getX() == pos.getX()){
 			if (previousPosition.getY() > pos.getY())
 				return Orientation.DOWN;
 			return Orientation.UP;
 		}
 		if (previousPosition.getX() > pos.getX())
 			return Orientation.LEFT;
 		return Orientation.RIGHT;
 	}
 
 
 	private int getHCost(Position position, Orientation orientation, Position pos) {
		Energy manHattanCost = new Energy(Energy.MOVE_COST*calculateManhattan(position, pos));
		Energy turnCost = new Energy(Energy.TURN_COST*getTurns(new Node(position,orientation),pos));
 		Energy cost = Energy.energySum(manHattanCost, turnCost);
 		return cost.getEnergy();
 	}
 
 	private double getGCost(Node currentNode, Position pos) {
		double gCost = currentNode.getGCost() + Energy.MOVE_COST + Energy.TURN_COST*getTurns(currentNode, pos);
 		return gCost;
 	}
 	/**
 	 * methode voor het aantal turns terug te geven om van een node met oreinatie m naar een nabijgelegen node te 
 	 * bewegen (vlak naast)
 	 *  
 	 * @param node
 	 * @return
 	 */
 	private int getTurns(Node node, Position pos){
 		int result = 0;
 		if(node.getPosition().getX() == pos.getX() && node.getPosition().getY() == pos.getY())
 			return result;
 		/*
 		 * In dit gedeelte kijken we de verhouding van de huidige robot met zijn bestemming na. Hier worden alle gevallen overlopen.
 		 * Om dit visueel voor te stellen staan er letters die de posities voorstellen. De hoekpunten zijn in wijzerszin A, B, C en D.
 		 * Vervolgens worden de middens van elke rand voorgesteld met E, F, G en H.
 		 */
 		if(pos.getX() == node.getPosition().getX() && pos.getY() < node.getPosition().getY()){
 			// E
 			switch(node.getOrientation()){
 			case RIGHT:
 			case LEFT:
 				result = 1;
 				break;
 			case DOWN:
 				result = 2;
 				break;
 			}
 		}else if(pos.getX() == node.getPosition().getX() && pos.getY() > node.getPosition().getY()){
 			// G
 			switch(node.getOrientation()){
 			case LEFT:
 			case RIGHT:
 				result = 1;
 				break;
 			case UP:
 				result = 2;
 				break;
 			}
 		}else if(pos.getX() > node.getPosition().getX() && pos.getY() == node.getPosition().getY()){
 			// F
 			switch(node.getOrientation()){
 			case DOWN:
 			case UP:
 				result = 1;
 				break;
 			case LEFT:
 				result = 2;
 				break;
 			}
 		}else if(pos.getX() > node.getPosition().getX() && pos.getY() < node.getPosition().getY()){
 			// B
 			switch(node.getOrientation()){
 			case RIGHT:
 			case UP:
 				result = 1;
 				break;
 			case DOWN:
 			case LEFT:
 				result = 2;
 				break;
 			}
 		}else if(pos.getX() > node.getPosition().getX() && pos.getY() > node.getPosition().getY()){
 			// C
 			switch(node.getOrientation()){
 			case DOWN:
 			case RIGHT:
 				result = 1;
 				break;
 			case LEFT:
 			case UP:
 				result = 2;	
 				break;
 			}
 		}else if(pos.getX() < node.getPosition().getX() && pos.getY() == node.getPosition().getY()){
 			// H
 			switch(node.getOrientation()){
 			case UP:
 			case DOWN:
 				result = 1;
 				break;
 			case RIGHT:
 				result = 2;
 				break;
 			}
 		}else if(pos.getX() < node.getPosition().getX() && pos.getY() < node.getPosition().getY()){
 			// A
 			switch(node.getOrientation()){
 			case UP:
 			case LEFT:
 				result = 1;
 				break;
 			case RIGHT:
 			case DOWN:
 				result = 2;
 				break;
 			}
 		}else if(pos.getX() < node.getPosition().getX() && pos.getY() > node.getPosition().getY()){
 			// D
 			switch(node.getOrientation()){
 			case LEFT:
 			case DOWN:
 				result = 1;
 				break;
 			case UP:
 			case RIGHT:
 				result = 2;
 				break;
 			}
 		}
 		return result;
 	}
 	
 	public Node getMinimalFNode(HashMap<Position, Node> map){
 		Collection<Node>c = map.values();
 		Iterator<Node> itr = c.iterator();
 		Node minimalNode = itr.next();
 		for (Node node : c){
 			if (node.getFCost() < minimalNode.getFCost())
 				minimalNode = node;
 		}
 		return minimalNode;
 	}
 	
 }
