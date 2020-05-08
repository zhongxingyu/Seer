 package com.vacuumhead.bangalore.utils;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Vector;
 
 import com.vacuumhead.bangalore.constants.StationConstants;
 import com.vacuumhead.bangalore.constants.UserConstants;
 
 public class MetroMapData {
 	
 	public static double[][] tokenPurple = {
 		{0.00, 10.00, 12.00, 12.00, 14.00, 15.00},
 		{10.00, 0.00, 10.00, 12.00, 12.00, 14.00},
 		{12.00, 10.00, 0.00, 10.00, 12.00, 12.00},
 		{12.00, 12.00, 10.00, 0.00, 10.00, 12.00},
 		{14.00, 12.00, 12.00, 10.00, 0.00, 10.00},
 		{15.00, 14.00, 12.00, 12.00, 10.00, 0.00}
 	};
 	
 	public static double[][] varshikPurple = {
 		{0.00, 8.50, 10.20, 10.20, 11.90, 12.75},
 		{8.50, 0.00, 8.50, 10.20, 10.20, 11.90},
 		{10.20, 8.50, 0.00, 8.50, 10.20, 10.20},
 		{10.20, 10.20, 8.50, 0.00, 8.50, 10.20},
 		{11.90, 10.20, 10.20, 8.50, 0.00, 8.50},
 		{12.75, 11.90, 10.20, 10.20, 8.50, 0.00}
 	};
 	
 	public static Map<String, Double> getFareBetweenStations(int fromStation, int toStation) {
 		Map<String, Double> map = new HashMap<String, Double>();
 		map.put(UserConstants.tokenUser, getTokenFareBetweenStations(fromStation, toStation));
 		map.put(UserConstants.varshikUser, getVarshikFareBetweenStations(fromStation, toStation));
 		
 		return map;
 		
 	}
 	
 	public static Vector<String> applyBFS(String source, String destination) {
 		
 		class node {
 			String station;
 			Vector<String> path;
 			node(String s, Vector<String> p) {
 				station = s;
 				path = p;
 			}
 		};
 		
 		Vector<String> path = new Vector<String>();
 		Map<String, Boolean> visited = new HashMap();
 		
 		Queue<node> q = new LinkedList<node>();
		Vector<String> temp = new Vector<String>();
		temp.add(source);
		q.add(new node(source, temp));
 		visited.put(source, true);
 		
 		while(!q.isEmpty()) {
 			node t = q.poll();
 			if(t.station.equals(destination)) {
 				return t.path;
 			}
 			Vector<String> adjStation = StationConstants.getAdjacentStationName(t.station);
 			for(int i = 0; i < adjStation.size(); ++i) {
 				if(visited.containsKey(adjStation.get(i))) {
 					continue;
 				}
 				visited.put(adjStation.get(i), true);
 				Vector<String> route = new Vector<String>(t.path);
 				
 				route.add(adjStation.get(i));
 				q.add(new node(adjStation.get(i), route));
 				
 			}
 		}
 		
 		return null;
 	}
 	
 	public static double getVarshikFareBetweenStations(int fromStation, int toStation) {
 		
 		return varshikPurple[fromStation][toStation];
 	}
 	
 	public static double getTokenFareBetweenStations(int fromStation, int toStation) {
 	
 		return tokenPurple[fromStation][toStation];
 	}
 	
 	
 	
 }
