 package ch.zhaw.jroute.model.algorithm;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import ch.zhaw.jroute.model.Way;
 import ch.zhaw.jroute.model.Waypoint;
 
 public class SaveOfDjikstraAlgo {
 
 	private static List<Way> allWays = new ArrayList<Way>();
 	private static Set<Waypoint> allPoints = new HashSet<Waypoint>();
 	private static List<Waypoint> redWaypoints = new ArrayList<Waypoint>();
 	private static Set<Waypoint> greenWaypoints = new HashSet<Waypoint>();
 
 	public static void main(String[] args) {
 
 		Waypoint A = new Waypoint("A");
 		Waypoint B = new Waypoint("B");
 		Waypoint C = new Waypoint("C");
 		Waypoint D = new Waypoint("D");
 		Waypoint E = new Waypoint("E");
 		Waypoint F = new Waypoint("F");
 		Waypoint G = new Waypoint("G");
 		Waypoint H = new Waypoint("H");
 		Waypoint I = new Waypoint("I");
 
 		// set start and finish
 		Waypoint startWaypoint = A;
 		Waypoint finish = G;
 
 		Way way1 = new Way(1, A, B, 2);
 		Way way2 = new Way(2, A, C, 5);
 		Way way3 = new Way(3, A, E, 7);
 		Way way4 = new Way(4, B, E, 8);
 		Way way5 = new Way(5, D, H, 6);
 		Way way6 = new Way(6, C, F, 15);
 		Way way7 = new Way(7, E, A, 9);
 		Way way8 = new Way(8, B, G, 1);
 		Way way9 = new Way(9, D, I, 6);
 
 		allWays.add(way1);
 		allWays.add(way2);
 		allWays.add(way3);
 		allWays.add(way4);
 		allWays.add(way5);
 		allWays.add(way6);
 		allWays.add(way7);
 		allWays.add(way8);
 		allWays.add(way9);
 
 		for (Way ways : allWays) {
 			allPoints.add(ways.getStart());
 			allPoints.add(ways.getEnd());
 		}
 
 		// preparation for run
 		greenWaypoints.clear();
 		redWaypoints.clear();
 		Waypoint nextWaypoint;
 
 		// preparation for all points
 		for (Waypoint all : allPoints) {
 			all.setDistanceToStart(Integer.MAX_VALUE);
 			all.setWaypointBefore(null);
 		}
 
 		// preparation for startpoint
 		redWaypoints.add(startWaypoint);
 		startWaypoint.setDistanceToStart(0);
 		startWaypoint.setWaypointBefore(startWaypoint);
 		nextWaypoint = startWaypoint;
 
 		// worst-case => dijkstra (points * points)
 		int runs = allPoints.size() * allPoints.size();
 
 		for (int i = 0; i < runs; i++) {
 
 			if (redWaypoints.isEmpty()) {
 				break;
 			}
 
 			nextWaypoint = getShortestDistanceRedWaypoint(redWaypoints);
 
 			for (Way nextWay : getConnectedForwardWays(nextWaypoint, allWays)) {
 
 				Waypoint connectedForwardWaypoint = nextWay.getEnd();
 				if (!greenWaypoints.contains(connectedForwardWaypoint)) {
 					redWaypoints.add(connectedForwardWaypoint);
 				}
 
 				double connectedForwardDistance = nextWay.getDistance();
 				double connectedForwardWaypointDistanceToStart = connectedForwardWaypoint
 						.getDistanceToStart();
 				double additionDistance = (nextWaypoint.getDistanceToStart() + connectedForwardDistance);
 				if (additionDistance < connectedForwardWaypointDistanceToStart) {
 					connectedForwardWaypoint.setWaypointBefore(nextWaypoint);
 					connectedForwardWaypoint
 							.setDistanceToStart(additionDistance);
 				}
 				redWaypoints.remove(nextWaypoint);
 				greenWaypoints.add(nextWaypoint);
 			}
 
 			for (Way nextWay : getConnectedBackwardWays(nextWaypoint, allWays)) {
 
 				Waypoint connectedBackwardWaypoint = nextWay.getStart();
 				if (!greenWaypoints.contains(connectedBackwardWaypoint)) {
 					redWaypoints.add(connectedBackwardWaypoint);
 				}
 
 				double connectedBackwardDistance = nextWay.getDistance();
 				double connectedBackwardWaypointDistanceToStart = connectedBackwardWaypoint
 						.getDistanceToStart();
 				double additionDistance = (nextWaypoint.getDistanceToStart() + connectedBackwardDistance);
 				if (additionDistance < connectedBackwardWaypointDistanceToStart) {
 					connectedBackwardWaypoint.setWaypointBefore(nextWaypoint);
 					connectedBackwardWaypoint
 							.setDistanceToStart(additionDistance);
 				}
 				redWaypoints.remove(nextWaypoint);
 				greenWaypoints.add(nextWaypoint);
 			}
 
 		}
 
 		System.out.println(greenWaypoints.size());
 
 		for (Waypoint points : greenWaypoints) {
 			System.out.println("point: " + points.getName() + " vorgaenger: "
 					+ points.getWaypointBefore() + " distanz: "
 					+ points.getDistanceToStart());
 		}
 
 		ArrayList<Waypoint> shortestPath = new ArrayList<Waypoint>();
 		ArrayList<Way> shortestWayPath = new ArrayList<Way>();
 		shortestPath.add(finish);
 
 		System.out.println("get shortest path from " + startWaypoint.getName()
 				+ " to " + finish.getName() + " with distance: "
 				+ finish.getDistanceToStart() + "");
 
 		for (int b = 0; b < greenWaypoints.size(); b++) {
 
 			// stop the loop, if the beforePoint is the startPoint
 			if (finish.getWaypointBefore().equals(startWaypoint)) {
 				shortestPath.add(startWaypoint);
 				shortestWayPath.add(getWay(startWaypoint.getWaypointBefore(),
 						finish, allWays));
 				break;
 			}
 
 			// add the beforePoint to the list
 			shortestPath.add(finish.getWaypointBefore());
 			shortestWayPath.add(getWay(finish, finish.getWaypointBefore(),
 					allWays));
 
 			// set the beforePoint as activePoint for the next looping
 			Waypoint next = finish.getWaypointBefore();
 			finish = next;
 
 		}
 
 		Collections.reverse(shortestPath);
 		Collections.reverse(shortestWayPath);
 
 		for (Waypoint path : shortestPath) {
 			System.out.println(path.getName());
 		}
 
 		for (Way ways : shortestWayPath) {
 			System.out.println(ways.getWayID());
 		}
 
 	}
 
 	static Way getWay(Waypoint point1, Waypoint point2, List<Way> ways) {
 		for (Way way : ways) {
 			if ((way.getStart().equals(point1) && way.getEnd().equals(point2))
 					|| (way.getStart().equals(point2) && way.getEnd().equals(
 							point1))) {
 				return way;
 			}
 		}
 		return null;
 	}
 
 	static Comparator<Waypoint> sortByDistance = new Comparator<Waypoint>() {
 		public int compare(Waypoint point1, Waypoint point2) {
 			return (int) (point1.getDistanceToStart() - point2
 					.getDistanceToStart());
 		}
 	};
 
 	static Waypoint getShortestDistanceRedWaypoint(List<Waypoint> redWaypoints) {
 		Collections.sort(redWaypoints, sortByDistance);
 		return redWaypoints.get(0);
 	}
 
 	static List<Way> getConnectedBackwardWays(Waypoint waypoint, List<Way> ways) {
 		List<Way> connectedBackwardWays = new ArrayList<Way>();
 		for (Way way : ways) {
 			if (way.getEnd().equals(waypoint)) {
 				connectedBackwardWays.add(way);
 			}
 		}
 		return connectedBackwardWays;
 	}
 
 	static List<Way> getConnectedForwardWays(Waypoint waypoint, List<Way> ways) {
 		List<Way> connectedForwardWays = new ArrayList<Way>();
 		for (Way way : ways) {
 			if (way.getStart().equals(waypoint)) {
 				connectedForwardWays.add(way);
 			}
 		}
 		return connectedForwardWays;
 	}
 
 }
