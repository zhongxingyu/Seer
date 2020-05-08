 /**
  * 
  */
 package com.example.helloandroid.prefs;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 
 import com.example.helloandroid.api.ServiceProvider;
 import com.example.helloandroid.feed.model.Agency;
 import com.example.helloandroid.feed.model.Direction;
 import com.example.helloandroid.feed.model.Route;
 import com.example.helloandroid.provider.contract.WidgetConfiguration;
 
 /**
  * @author james
  * 
  */
 public class NextBusObserverConfig {
 	private final Context ctx;
 	private final int widgetId;
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
 		
 		// Load from database
         String selection = WidgetConfiguration.WIDGET_ID + " = ?";
         String[] selectionArgs = { String.valueOf(widgetId) };
         Cursor cursor = ctx.getContentResolver().query(WidgetConfiguration.CONTENT_URI, null, selection, selectionArgs, null);
         if (cursor.moveToFirst()) {
         	String agencyTag = cursor.getString(cursor.getColumnIndex(WidgetConfiguration.AGENCY));
         	String routeTag = cursor.getString(cursor.getColumnIndex(WidgetConfiguration.ROUTE));
         	String directionTag = cursor.getString(cursor.getColumnIndex(WidgetConfiguration.DIRECTION));
         	String stopTag = cursor.getString(cursor.getColumnIndex(WidgetConfiguration.STOP));
 
         	this.agency = new NextBusAgency().init(ServiceProvider.getAgency(ctx, agencyTag));
     		this.route = new NextBusRoute().init(ServiceProvider.getRoute(ctx, agencyTag, routeTag));
     		this.direction = new NextBusDirection().init(ServiceProvider.getDirection(ctx, agencyTag, directionTag));
     		this.stop = new NextBusStop().init(ServiceProvider.getStop(ctx, agencyTag, stopTag));
     		
         	this.startObserving = cursor.getInt(cursor.getColumnIndex(WidgetConfiguration.START_TIME));
         	this.stopObserving = cursor.getInt(cursor.getColumnIndex(WidgetConfiguration.END_TIME));
         } else {
         	
         	this.startObserving = -1;
         	this.stopObserving = -1;
         }
         cursor.close();
 
 		if (this.startObserving == this.stopObserving && this.startObserving != -1) {
 			this.startObserving = this.stopObserving = -1;
 			modified = true;
 		}
 		
 	}
 
 	public void save() {
 		
 		// Persist to database
 		ContentValues values = new ContentValues();
 		values.put(WidgetConfiguration.WIDGET_ID, widgetId);
 		values.put(WidgetConfiguration.AGENCY, agency.getTag());
 		values.put(WidgetConfiguration.ROUTE, route.getTag());
 		values.put(WidgetConfiguration.DIRECTION, direction.getTag());
 		values.put(WidgetConfiguration.STOP, stop.getTag());
 		values.put(WidgetConfiguration.START_TIME, startObserving);
 		values.put(WidgetConfiguration.END_TIME, stopObserving);
 
 		// Check for existing preferences
 		String selection = WidgetConfiguration.WIDGET_ID + " = ?";
 		String[] selectionArgs =  { String.valueOf(widgetId) };
 		Cursor cursor = ctx.getContentResolver().query(WidgetConfiguration.CONTENT_URI, null, selection, selectionArgs, null);
 		if (cursor.moveToFirst()) {
 			ctx.getContentResolver().update(WidgetConfiguration.CONTENT_URI, values, selection, selectionArgs);
 		} else {
 			ctx.getContentResolver().insert(WidgetConfiguration.CONTENT_URI, values);
 		}
 		cursor.close();
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
 			for (Agency model : ServiceProvider.getAgencies(ctx)) {
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
 			for (Route model : ServiceProvider.getRoutes(ctx, agency.getTag())) {
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
		clearDirection();
 	}
 
 	private void clearDirection() {
 		directions = null;
 		direction = null;
 		clearStop();
 	}
 
 	public List<NextBusDirection> getDirections() {
 		if (directions == null && route != null && agency != null) {
 			directions = new ArrayList<NextBusDirection>();
 			for (Direction model : ServiceProvider.getRouteConfig(ctx, agency.getTag(), route.getTag())) {
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
		clearStop();
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
