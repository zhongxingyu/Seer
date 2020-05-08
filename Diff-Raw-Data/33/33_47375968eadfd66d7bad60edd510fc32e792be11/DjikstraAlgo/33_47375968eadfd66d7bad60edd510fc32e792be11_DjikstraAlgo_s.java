 package ch.zhaw.jroute.model.algorithm;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import ch.zhaw.jroute.model.Way;
 import ch.zhaw.jroute.model.WayStatusEnum;
 import ch.zhaw.jroute.model.Waypoint;
 
 public class DjikstraAlgo implements IShortestPathAlgorithm {
 	List<Way> allWays = new ArrayList<Way>();
 	Set<Waypoint> allPoints = new HashSet<Waypoint>();
 	List<Waypoint> redWaypoints = new ArrayList<Waypoint>();
 	Set<Waypoint> greenWaypoints = new HashSet<Waypoint>();
 
 	public DjikstraAlgo() {
 	}
 
 	@Override
 	public List<Way> getShortestPath(Waypoint start, Waypoint end,
 			List<Way> allWaysForInterface) {
 
 		Set<Waypoint> allPointsForInterface = new HashSet<Waypoint>();
 		List<Waypoint> redWaypointsForInterface = new ArrayList<Waypoint>();
 		Set<Waypoint> greenWaypointsForInterface = new HashSet<Waypoint>();
 		List<Waypoint> shortestWaypointPathForInterface = new ArrayList<Waypoint>();
 		List<Way> shortestWayForInterface = new ArrayList<Way>();
 
 		for (Way ways : allWaysForInterface) {
 			allPointsForInterface.add(ways.getStart());
 			allPointsForInterface.add(ways.getEnd());
 		}
 
 		// set startpoint
 		Waypoint startWaypointForInterface = start;
 
 		// preparation for run
 		greenWaypointsForInterface.clear();
 		redWaypointsForInterface.clear();
 		Waypoint nextWaypointForInterface;
 
 		// preparation for all points
 		for (Waypoint all : allPointsForInterface) {
 			all.setDistanceToStart(Integer.MAX_VALUE);
 			all.setWaypointBefore(null);
 		}
 
 		// preparation for startpoint
 		redWaypointsForInterface.add(startWaypointForInterface);
 		startWaypointForInterface.setDistanceToStart(0);
 		startWaypointForInterface.setWaypointBefore(startWaypointForInterface);
 		nextWaypointForInterface = startWaypointForInterface;
 
 		// worst-case => dijkstra (points * points)
 		int runs = allPointsForInterface.size() * allPointsForInterface.size();
 
 		for (int i = 0; i < runs; i++) {
 
 			if (redWaypointsForInterface.isEmpty()) {
 				break;
 			}
 
 			nextWaypointForInterface = getShortestDistanceRedWaypoint(redWaypointsForInterface);
 
 			for (Way nextWay : getConnectedForwardWays(
 					nextWaypointForInterface, allWaysForInterface)) {
 
 				Waypoint connectedForwardWaypoint = nextWay.getEnd();
 				if (!greenWaypointsForInterface
 						.contains(connectedForwardWaypoint)) {
 					redWaypointsForInterface.add(connectedForwardWaypoint);
 				}
 
 				double connectedForwardDistance = nextWay.getDistance();
 				double connectedForwardWaypointDistanceToStart = connectedForwardWaypoint
 						.getDistanceToStart();
 				double additionDistance = (nextWaypointForInterface
 						.getDistanceToStart() + connectedForwardDistance);
 				if (additionDistance < connectedForwardWaypointDistanceToStart) {
 					connectedForwardWaypoint
 							.setWaypointBefore(nextWaypointForInterface);
 					connectedForwardWaypoint
 							.setDistanceToStart(additionDistance);
 				}
 				redWaypointsForInterface.remove(nextWaypointForInterface);
 				greenWaypointsForInterface.add(nextWaypointForInterface);
 			}
 
 			for (Way nextWay : getConnectedBackwardWays(
 					nextWaypointForInterface, allWaysForInterface)) {
 
 				Waypoint connectedBackwardWaypoint = nextWay.getStart();
 				if (!greenWaypointsForInterface
 						.contains(connectedBackwardWaypoint)) {
 					redWaypointsForInterface.add(connectedBackwardWaypoint);
 				}
 
 				double connectedBackwardDistance = nextWay.getDistance();
 				double connectedBackwardWaypointDistanceToStart = connectedBackwardWaypoint
 						.getDistanceToStart();
 				double additionDistance = (nextWaypointForInterface
 						.getDistanceToStart() + connectedBackwardDistance);
 				if (additionDistance < connectedBackwardWaypointDistanceToStart) {
 					connectedBackwardWaypoint
 							.setWaypointBefore(nextWaypointForInterface);
 					connectedBackwardWaypoint
 							.setDistanceToStart(additionDistance);
 				}
 				redWaypointsForInterface.remove(nextWaypointForInterface);
 				greenWaypointsForInterface.add(nextWaypointForInterface);
 			}
 
 		}
 
 		Waypoint finishForInterface = end;
 		shortestWaypointPathForInterface.add(finishForInterface);
 
 		for (int b = 0; b < greenWaypointsForInterface.size(); b++) {
 
 			// stop the loop, if the beforePoint is the startPoint
 			if (finishForInterface.getWaypointBefore().equals(
 					startWaypointForInterface)) {
 				shortestWaypointPathForInterface.add(startWaypointForInterface);
 				Way newWay = getWay(
 						startWaypointForInterface.getWaypointBefore(),
 						finishForInterface, allWaysForInterface);
 				shortestWayForInterface.add(newWay);
 				newWay.setStatus(WayStatusEnum.result);
 				break;
 			}
 
 			// add the beforePoint to the list
 			shortestWaypointPathForInterface.add(finishForInterface
 					.getWaypointBefore());
			shortestWayForInterface
					.add(getWay(finishForInterface,
							finishForInterface.getWaypointBefore(),
							allWaysForInterface));
 
 			// set the beforePoint as activePoint for the next looping
 			Waypoint nextForInterface = finishForInterface.getWaypointBefore();
 			finishForInterface = nextForInterface;
 
 		}
 
 		for (Way way : allWays) {
 			if (way.getStatus() != WayStatusEnum.result) {
 				way.setStatus(WayStatusEnum.noResult);
 			}
 		}
 
 		Collections.reverse(shortestWaypointPathForInterface);
 		Collections.reverse(shortestWayForInterface);
 
 		return shortestWayForInterface;
 	}
 
 	Way getWay(Waypoint point1, Waypoint point2, List<Way> ways) {
 		for (Way way : ways) {
 			if ((way.getStart().equals(point1) && way.getEnd().equals(point2))
 					|| (way.getStart().equals(point2) && way.getEnd().equals(
 							point1))) {
 				return way;
 			}
 		}
 		return null;
 	}
 
 	Comparator<Waypoint> sortByDistance = new Comparator<Waypoint>() {
 		public int compare(Waypoint point1, Waypoint point2) {
 			return (int) (point1.getDistanceToStart() - point2
 					.getDistanceToStart());
 		}
 	};
 
 	Waypoint getShortestDistanceRedWaypoint(List<Waypoint> redWaypoints) {
 		Collections.sort(redWaypoints, sortByDistance);
 		return redWaypoints.get(0);
 	}
 
 	List<Way> getConnectedBackwardWays(Waypoint waypoint, List<Way> ways) {
 		List<Way> connectedBackwardWays = new ArrayList<Way>();
 		for (Way way : ways) {
 			if (way.getEnd().equals(waypoint)) {
 				connectedBackwardWays.add(way);
 			}
 		}
 		return connectedBackwardWays;
 	}
 
 	List<Way> getConnectedForwardWays(Waypoint waypoint, List<Way> ways) {
 		List<Way> connectedForwardWays = new ArrayList<Way>();
 		for (Way way : ways) {
 			if (way.getStart().equals(waypoint)) {
 				connectedForwardWays.add(way);
 			}
 		}
 		return connectedForwardWays;
 	}
 
 }
