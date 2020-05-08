 package com.vaggs.Route;
 
 import static com.vaggs.Utils.OfyService.ofy;
 
 import java.util.Iterator;
 import java.util.List;
 
 import com.google.appengine.labs.repackaged.com.google.common.collect.Lists;
 import com.google.appengine.labs.repackaged.org.json.JSONArray;
 import com.google.appengine.labs.repackaged.org.json.JSONException;
 import com.google.appengine.labs.repackaged.org.json.JSONObject;
 import com.google.appengine.labs.repackaged.org.json.JSONTokener;
 import com.googlecode.objectify.annotation.Embed;
 import com.googlecode.objectify.annotation.Entity;
 import com.googlecode.objectify.annotation.Id;
 import com.googlecode.objectify.cmd.LoadType;
 import com.vaggs.Utils.LatLng;
 
 /**
  * A route for a plane to follow
  * @author Hawkwood
  *
  */
 @Embed
 @Entity
 public class Route implements Iterable<Waypoint>{
 	@Id Long id; 
 	private List<Waypoint> route = null;
 	String name;
 	
 	private Route() {
		this("");
 	}
 	
 	private Route(String name) {
 		route = Lists.newArrayList();
 		this.name = name; 
 	}
 	
 	public static Route ParseRouteByTaxiways(Waypoint start, String str) {
 		if(start == null || str == null || str.isEmpty())
			return new Route();
 		Route route = new Route();
 		char[] chars = str.toCharArray();
 		LoadType<Taxiway> q = ofy().load().type(Taxiway.class);
 		Taxiway prevTaxiway = q.id(chars[0]).get();
 		for(int i = 1; i < chars.length; i++) {
 			Taxiway taxiway = q.id(chars[i]).get();
 			Waypoint pt = prevTaxiway.intersection(taxiway);
 			route.addWaypoints(prevTaxiway.PtsBetween(start, pt));
 			prevTaxiway = taxiway;
 			start = pt;
 		}
 		route.addWaypoints(prevTaxiway.PtsBetween(start, null));
 		return route;
 	}
 	
 	public static Route ParseRouteByWaypoints(JSONArray obj, String routeName) throws JSONException {
 		Route route = new Route(routeName);
 		List<Waypoint> waypoints = Lists.newArrayList();
 		int numWaypoints = obj.length();
 		for(int i=0; i<numWaypoints; i++) {
 			JSONObject point = obj.getJSONObject(i);
 			LatLng loc = new LatLng(point.getDouble("Lat"), point.getDouble("Lng"));
 			Waypoint p = new Waypoint(loc, point.getBoolean("Holdshort"), false);
 			waypoints.add(p);
 		}
 		route.addWaypoints(waypoints);
 		return route;
 	}
 	
 	/**
 	 * TODO: change to private
 	 * Adds all waypoints in a list to the route
 	 */
 	public void addWaypoints(List<Waypoint> pts) {
 		for(Waypoint pt : pts)
 			route.add(pt);
 	}
 
 	
 	/**
 	 * Get an iterator to the route in order of waypoints
 	 * @return the iterator
 	 */
 	@Override
 	public Iterator<Waypoint> iterator() {
 		if (null != route) {
 			return route.iterator();
 		}
 		return null;
 	}
 
 	/**
 	 * @param name the name to set
 	 */
 	public String getName() {
 		return name;
 	}
 }
