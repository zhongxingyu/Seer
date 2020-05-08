 /**
  * 
  */
 package com.example.helloandroid.prefs;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.example.helloandroid.NextBus;
 import com.example.helloandroid.feed.model.Agency;
 import com.example.helloandroid.feed.model.Direction;
 import com.example.helloandroid.feed.model.Route;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 
 /**
  * @author james
  * 
  */
 public class NextBusObserverConfig {
 	private static final String PREFS_NAME_PREFIX = NextBusObserverConfig.class
 			.getName();
 
 	private static final String PREF_AGENCY = "agency";
 	private static final String PREF_ROUTE = "route";
 	private static final String PREF_DIRECTION = "direction";
 	private static final String PREF_STOP = "stop";
 	private static final String PREF_START_OBSERVING = "start-observing";
 	private static final String PREF_STOP_OBSERVING = "stop-observing";
 
 	private final Context ctx;
 	private final int widgetId;
 	private final String prefsName;
 	private boolean modified = false;
 
 	private NextBusAgency agency;
 	private List<NextBusAgency> agencies;
 	private NextBusRoute route;
 	private List<NextBusRoute> routes;
 	private NextBusDirection direction;
 	private List<NextBusDirection> directions;
 	private NextBusStop stop;
 	private List<NextBusStop> stops;
 
 	/** Seconds in the day */
 	private int startObserving;
 	/** Seconds in the day */
 	private int stopObserving;
 
 	public NextBusObserverConfig(Context ctx, int widgetId) {
 		this.ctx = ctx;
 		this.widgetId = widgetId;
 		this.prefsName = PREFS_NAME_PREFIX + "_" + widgetId;
 		SharedPreferences prefs = ctx.getSharedPreferences(this.prefsName, 0);
 		this.agency = initValue(prefs, new NextBusAgency(), PREF_AGENCY);
 		this.route = initValue(prefs, new NextBusRoute(), PREF_ROUTE);
 		this.direction = initValue(prefs, new NextBusDirection(),
 				PREF_DIRECTION);
 		this.stop = initValue(prefs, new NextBusStop(), PREF_STOP);
 		this.startObserving = prefs.getInt(PREF_START_OBSERVING, 0);
 		this.stopObserving = prefs.getInt(PREF_STOP_OBSERVING, 0);
 	}
 
 	private <T extends NextBusValue> T initValue(SharedPreferences prefs, T v,
 			String key) {
 		String s = prefs.getString(key, null);
 		if (s == null)
 			return null;
 		v.initFromPrefs(s);
 		return v;
 	}
 
 	public void save() {
 		SharedPreferences.Editor prefs = ctx.getSharedPreferences(
 				this.prefsName, 0).edit();
 
 		saveValue(prefs, this.agency, PREF_AGENCY);
 		saveValue(prefs, this.route, PREF_ROUTE);
 		saveValue(prefs, this.direction, PREF_DIRECTION);
 		saveValue(prefs, this.stop, PREF_STOP);
 		prefs.putInt(PREF_START_OBSERVING, startObserving);
 		prefs.putInt(PREF_STOP_OBSERVING, stopObserving);
 
 		prefs.commit();
 	}
 
 	private static void saveValue(Editor prefs, NextBusValue value, String key) {
		String s = value.toPrefsString();
		prefs.putString(key, s);
 	}
 
 	private static boolean safeEquals(Object a, Object b) {
 		if (a == null) {
 			return b == null;
 		} else {
 			return a.equals(b);
 		}
 	}
 
 	public List<NextBusAgency> getAgencies() {
 		if (agencies == null) {
 			agencies = new ArrayList<NextBusAgency>();
 			for (Agency model : NextBus.getAgencies()) {
 				agencies.add(new NextBusAgency().init(model));
 			}
 		}
 		return agencies;
 	}
 
 	public NextBusAgency getAgency() {
 		return agency;
 	}
 
 	public void setAgency(NextBusAgency newAgency) {
 		if (safeEquals(agency, newAgency)) {
 			return;
 		}
 		modified = true;
 		agency = newAgency;
 		clearRoute();
 	}
 
 	private void clearRoute() {
 		routes = null;
 		route = null;
 		clearDirection();
 	}
 
 	public List<NextBusRoute> getRoutes() {
 		if (routes == null && agency != null) {
 			routes = new ArrayList<NextBusRoute>();
 			for (Route model : NextBus.getRoutes(agency.getTag())) {
 				routes.add(new NextBusRoute().init(model));
 			}
 		}
 		return routes;
 	}
 
 	public NextBusRoute getRoute() {
 		return route;
 	}
 
 	public void setRoute(NextBusRoute route) {
 		if (safeEquals(this.route, route)) {
 			return;
 		}
 		modified = true;
 		this.route = route;
 		direction = null;
 		stop = null;
 	}
 
 	private void clearDirection() {
 		directions = null;
 		direction = null;
 		clearStop();
 	}
 
 	public List<NextBusDirection> getDirections() {
 		if (directions == null && routes != null && agency != null) {
 			directions = new ArrayList<NextBusDirection>();
 			for (Direction model : NextBus.getRouteConfig(agency.getTag(), route.getTag())) {
 				directions.add(new NextBusDirection().init(model));
 			}
 		}
 		return directions;
 	}
 
 	public NextBusDirection getDirection() {
 		return direction;
 	}
 
 	public void setDirection(NextBusDirection direction) {
 		if (safeEquals(this.direction, direction)) {
 			return;
 		}
 		modified = true;
 		this.direction = direction;
 		stop = null;
 	}
 
 	private void clearStop() {
 		stops = null;
 		stop = null;
 	}
 
 	public List<NextBusStop> getStops() {
 		if (stops == null && direction != null && getDirection() != null) {
 			// Stops are attached to directions in the route config, not fetched
 			// from the feed separately. Find the selected direction (this.direction
 			// might be from prefs, and thus not have the list of stops).
 			for (NextBusDirection d : directions) {
 				if (d.equals(direction)) {
 					stops = d.getStops();
 					return stops;
 				}
 			}
 		}
 		
 		return stops;
 	}
 
 	public NextBusStop getStop() {
 		return stop;
 	}
 
 	public void setStop(NextBusStop stop) {
 		if (safeEquals(this.stop, stop)) {
 			return;
 		}
 		modified = true;
 		this.stop = stop;
 	}
 
 	public int getStartObserving() {
 		return startObserving;
 	}
 
 	public void setStartObserving(int startObserving) {
 		this.startObserving = startObserving;
 	}
 
 	public int getStopObserving() {
 		return stopObserving;
 	}
 
 	public void setStopObserving(int stopObserving) {
 		this.stopObserving = stopObserving;
 	}
 
 }
