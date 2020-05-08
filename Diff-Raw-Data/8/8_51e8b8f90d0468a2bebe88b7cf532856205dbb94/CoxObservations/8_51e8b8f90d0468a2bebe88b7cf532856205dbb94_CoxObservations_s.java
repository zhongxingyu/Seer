 package siver.cox;
 
 import java.util.HashMap;
 
 import siver.boat.Boat;
 import siver.boat.BoatNavigation;
 import siver.river.River.NoLaneFound;
 import siver.river.lane.Lane;
 import siver.river.lane.LaneEdge;
 import siver.river.lane.LaneNode;
 
 public class CoxObservations {
 	private Cox cox;
 	private Boat boat;
 	private BoatNavigation navigator;
 	private static final int MAX_VIEWING_DISTANCE = 4;
 	private static final int CLEAR_BOUNDARY = 3;
 	private static final double OVERTAKING_SPEED_DIFFERENCE = 1.0;
 	private static final String BLOCKED_EDGE_KEY = "blocked_edge";
 	private static final String VISION_DISTANCE_KEY = "vision_distance";
 	
 	public CoxObservations(Cox cox, Boat boat, BoatNavigation navigator) {
 		this.cox = cox;
 		this.boat = boat;
 		this.navigator = navigator;
 	}
 	
 	public boolean atRiversEnd() { 
 		LaneNode node = navigator.getDestinationNode();
 		LaneEdge next_edge = node.getLane().getNextEdge(node, navigator.headingUpstream());
 		return next_edge == null;
 	}
 	
 	public boolean belowDesiredSpeed() {
 		return boat.getGear() < cox.desired_gear();
 	}
 	
 	public boolean aboveDesiredSpeed() {
 		return boat.getGear() > cox.desired_gear();
 	}
 	
 	public int boatGear() {
 		return boat.getGear();
 	}
 	
 	public boolean nearbyBoatInfront() {
 		//check that cannot see as far as CLEAR_BOUNDARY and that the limiting factor is a blocked edge (not the end of the river)
 		HashMap<String,Object> vision = look(navigator.getLane(), true);
 		return ((Integer) vision.get(VISION_DISTANCE_KEY) < CLEAR_BOUNDARY) && 
 				((LaneEdge) vision.get(BLOCKED_EDGE_KEY) != null);
 	}
 	
 	private HashMap<String,Object> look(Lane lane, boolean infront) {
 		LaneNode nodeToCheckFrom;
 		
 		if(lane == navigator.getLane() ) {
 			//for our current lane, just check from the node ahead
 			nodeToCheckFrom = navigator.getDestinationNode();
 		} else {
 			//for another lane, check from the start (i.e. next node in reverse direction) of the nearest edge (so this will include the nearest edge)
 			nodeToCheckFrom = lane.edgeNearest(boat.getLocation()).getNextNode(!navigator.headingUpstream());
 		}
 		HashMap<String,Object> vision = new HashMap<String,Object>();
 		
 		Integer edges_ahead = 0;
 		LaneNode node = nodeToCheckFrom;
 		
 		vision.put(BLOCKED_EDGE_KEY, null);
 		vision.put(VISION_DISTANCE_KEY, edges_ahead);
 		while(edges_ahead < MAX_VIEWING_DISTANCE) {
 			LaneEdge edge = node.getLane().getNextEdge(node, (infront == navigator.headingUpstream()));
 			//if there is no further edge then there no blocked edge infront but viewing distance is restricted (because we're at the end of the river)
 			if(edge == null) {
 				return vision;
 			}
 			
 			//as soon as we find an empty edge in front we can set the blocked edge to it and return
 			if(!edge.isEmpty()) {
 				vision.put(BLOCKED_EDGE_KEY, edge);
 				return vision;
 			}
 			//otherwise move on to next edge
 			node = edge.getNextNode((infront == navigator.headingUpstream()));
 			edges_ahead++;
 			vision.put(VISION_DISTANCE_KEY, edges_ahead);
 		}
 		return vision;
 	}
 	
 	public boolean slowBoatInfront() {
 		LaneEdge occupiedEdgeAhead = (LaneEdge) look(navigator.getLane(), true).get(BLOCKED_EDGE_KEY);
 		if(occupiedEdgeAhead == null) {
			// return false is there is no occupied edge ahead
 			return false;
 		}
 		
 		for(Cox coxInfront : occupiedEdgeAhead.getCoxes()) {			
 			if(cox.getNavigator().headingUpstream() == coxInfront.getNavigator().headingUpstream()) {
 				//if boats are travelling in the same direction then want the difference in their speeds to be greater than limit
				return (Math.abs(boat.getSpeed() - coxInfront.getBoat().getSpeed()) > OVERTAKING_SPEED_DIFFERENCE);
 			} else {
 				//if boats are travelling in the different direction then want the sum of their speeds to be greater than limit
				return (Math.abs(boat.getSpeed()) + Math.abs(coxInfront.getBoat().getSpeed()) > OVERTAKING_SPEED_DIFFERENCE);
 			}
 			
 		}
 		return false;
 	}
 	
 	public boolean laneToLeftIsClear() {
 		try {
 			Lane laneToLeft = boat.getRiver().getLaneToLeftOf(navigator.getLane(), navigator.headingUpstream());
 			return laneIsClear(laneToLeft);
 		} catch (NoLaneFound e) {
 			//if there is no lane to the left, then it is not clear
 			return false;
 		}
 	}
 	
 	public boolean laneToRightIsClear() {
 		try {
 			Lane laneToRight = boat.getRiver().getLaneToRightOf(navigator.getLane(), navigator.headingUpstream());
 			return laneIsClear(laneToRight);
 		} catch (NoLaneFound e) {
 			//if there is no lane to the left, then it is not clear
 			return false;
 		}
 	}
 	
 	private boolean laneIsClear(Lane lane) {
 		//return true only if there's nothing ahead and nothing behind
 		//very simplistic at the moment, only checks existence, not relative speed
 		return (((Integer) look(lane, true).get(VISION_DISTANCE_KEY)) >= CLEAR_BOUNDARY) && 
 				(((Integer) look(lane, false).get(VISION_DISTANCE_KEY)) >= CLEAR_BOUNDARY);
 	}
 	
 	public boolean changingLane() {
 		return navigator.changingLane();
 	}
 	
 	public boolean outingOver() {
 		return (boat.total_distance_covered() >= cox.getGoalDistance()) && (navigator.getDestinationNode().equals(navigator.getLane().getStartNode()));
 	}
 }
