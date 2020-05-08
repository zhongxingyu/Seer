 package com.appirio.mobile.aau.nativemap;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.cordova.DroidGap;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 
 import com.appirio.aau.R;
 import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapsInitializer;
 import com.google.android.gms.maps.model.BitmapDescriptor;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 
 public class MapManager {
 
 	private Context ctx;
 	private LatLng initialPos = new LatLng(37.789238, -122.401407);
 	private JSONArray busRoutes;
 	private APIProxy mapProxy;
 	private GoogleMap map;
 	private boolean mapAvailable;
 	private BitmapDescriptor stopBitmap;
 	private Map<String, BitmapDescriptor> routeIconMap;
 	private BitmapDescriptor defaultBusIcon;
 	private List<Marker> vehicleMarkers = new ArrayList<Marker>();
 	private MapUpdater mapUpdater;
 	private RoutesParser routesParser;
 	private TransitMapInfoWindowAdapter infoWindowAdapter;
 	private List<Polyline> routesPolylineShown;
 	private List<Route> routesShown;
 
 	public ArrayList<RouteStopSchedule> getSchedule(String stopName) throws AMException {
 		try {
 			JSONObject schedule = mapProxy.getSchedule(stopName);
 			ArrayList<RouteStopSchedule> result = new ArrayList<RouteStopSchedule>();
 			
 			JSONArray routes = schedule.getJSONArray("routes");
 			
 			for(int i = 0; i < routes.length(); i++) {
 				result.add(new RouteStopSchedule((JSONObject) routes.get(i)));
 			}
 			
 			return result;
 		} catch (JSONException e) {
 			e.printStackTrace();
 			
 			throw new AMException(e);
 		}
 	}
 	
 	public List<Route> getRoutesShown() {
 		return routesShown;
 	}
 	
 	public MapManager(Context ctx, GoogleMap map) throws AMException {
 		this.ctx = ctx;
 		mapProxy = new APIProxy((DroidGap)this.ctx);
 		this.map = map;
 		this.mapUpdater = new MapUpdater();
 		
 		// Center and zoom map on initial position
 		try {
			busRoutes = mapProxy.getBusStops();
			routesParser = new RoutesParser(busRoutes);

 			MapsInitializer.initialize(ctx);
 			
 			stopBitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_busstop);
 			defaultBusIcon = BitmapDescriptorFactory.fromResource(R.drawable.marker_busstop_black_suware);
 			mapAvailable = true;
 			
 			// Load bus stop data from Salesforce
 			routeIconMap = new HashMap<String, BitmapDescriptor>();
 			infoWindowAdapter = new TransitMapInfoWindowAdapter(this.ctx, this);
 			routesPolylineShown = new ArrayList<Polyline>();
 			routesShown = new ArrayList<Route>();
 			
 			try {
 				for(Route route : routesParser.getRoutes()) {
 					String markerName = route.getMarkerIcon();
 					
 					if(markerName != null && markerName.length() > 5) {
 						markerName = markerName.toLowerCase().substring(0, markerName.length() - 4);
 					}
 					
 					routeIconMap.put(route.getName(), BitmapDescriptorFactory.fromResource(this.ctx.getResources().getIdentifier(markerName, "drawable", this.ctx.getPackageName())));
 				}
 				
 				for(BusStop stop : routesParser.getStops()) {
 					JSONObject markerInfo = new JSONObject();
 					
 					MarkerOptions mo = new MarkerOptions();
 					
 					mo.icon(stopBitmap);
 					
 					markerInfo.put("type", "stop");
 					markerInfo.put("stopName", stop.getAddress());
 					markerInfo.put("routes", stop.getRoutesString());
 					
 					mo.title(markerInfo.toString());
 					
 					mo.position(new LatLng(stop.getLatitude(), stop.getLongitude()));
 					
 					map.addMarker(mo);
 				}
 				
 				map.setInfoWindowAdapter(infoWindowAdapter);
 				map.setOnInfoWindowClickListener(infoWindowAdapter);
 				map.setMyLocationEnabled(true);
 				
 				/*
 				Set<String> testRoutes = new HashSet<String>();
 				
 				testRoutes.add("D");
 				testRoutes.add("Jerrold");
 				
 				showRoutes(testRoutes);				
 				*/
 				
 				new Thread(mapUpdater).start();
 			} catch (Exception e) {
 				e.printStackTrace();
 				
 				throw new AMException(e);
 			}
 						
 		} catch (GooglePlayServicesNotAvailableException e) {
 			// TODO handle map is not available situation
 			e.printStackTrace();
 			
 			mapAvailable = false;
 		}
 	}
 	
 	public void showMap() {
 		if(mapAvailable) {
 			map.animateCamera(CameraUpdateFactory.newLatLngZoom(initialPos, 14.0f));
 		}
 	}
 	
 	public List<Route> getRoutes() {
 		return routesParser.getRoutes();
 	}
 	
 	public void showRoutes(Set<String> routeNames) throws AMException {
 		for(Polyline route : routesPolylineShown) {
 			route.remove();
 		}
 		
 		routesPolylineShown.clear();
 		routesShown.clear();
 		
 		routesShown = routesParser.getRoutes(routeNames);
 		
 		for(Route r : routesShown) {
 			PolylineOptions route = new PolylineOptions();
 			
 			route.color(r.getRouteColor());
 			
 			for(Waypoint stop : r.getWaypoints()) {
 				LatLng point = new LatLng(stop.getLatitude(), stop.getLongitude());
 				
 				route.add(point);
 			}
 			
 			if(r.getWaypoints().size() > 0) {
 				Waypoint stop = r.getWaypoints().iterator().next();
 				
 				LatLng point = new LatLng(stop.getLatitude(), stop.getLongitude());
 				
 				route.add(point);
 			}
 			
 			routesPolylineShown.add(map.addPolyline(route));
 		}
 		
 		mapUpdater.refreshBusesUI();
 	}
 	
 	public void startAutoUpdate() {
 		mapUpdater.autoRefreshOn = true;
 		
 		new Thread(mapUpdater).start();
 	}
 	
 	public void stopAutoUpdate() {
 		mapUpdater.autoRefreshOn = false;
 	}
 
 	public boolean getAutoUpdate() {
 		return mapUpdater.autoRefreshOn;
 	}
 	
 	public List<BusStop> getBusStops() {
 		return routesParser.getStops();
 	}
 	
 	private class MapUpdater implements Runnable {
 
 		private boolean autoRefreshOn = true;
 		private int autoRefreshInterval = 30000;
 		private List<Vehicle> vehicles;
 
 		private MapUpdater() {
 		}
 		
 		private void refreshBuses() throws AMException {
 			vehicles = new TeletracInfoParser().parse(mapProxy.getVehicles());
 		}
 
 		private void refreshBusesUI() throws AMException {
 			for(Marker m : vehicleMarkers) {
 				m.remove();
 			}
 			
 			vehicleMarkers.clear();
 			
 			if(vehicles != null) {
 				for(Vehicle v: vehicles) {
 					if(v.getVehicleName() != null && v.getRoute() != null) {
 	
 						if(shouldShowBus(v)) {
 							JSONObject markerInfo = new JSONObject();
 							
 							MarkerOptions mo = new MarkerOptions();
 							LatLng pos = new LatLng(v.getLatitude(), v.getLongitude());
 		
 							BitmapDescriptor bd = routeIconMap.get(v.getRoute());
 							
 							if(bd == null) {
 								bd = defaultBusIcon;
 							}
 							
 							mo.position(pos);
 							mo.icon(bd);
 							
 							try {
 								markerInfo.put("type", "bus");
 								markerInfo.put("route", v.getRoute());
 								mo.title(markerInfo.toString());
 							} catch (JSONException e) {
 								e.printStackTrace();
 								
 								mo.title(v.getRoute());
 							}
 							
 							vehicleMarkers.add(map.addMarker(mo));
 						}
 					}
 				}
 			}
 		}
 
 		private boolean shouldShowBus(Vehicle v) {
 			if(routesShown.isEmpty()) {
 				return true;
 			} 
 			
 			for(Route route: routesShown) {
 				if(route.getName().equals(v.getRoute())) {
 					return true;
 				}
 			}
 		
 			return false;
 		}
 
 		@Override
 		public void run() {
 			try {
 				while(this.autoRefreshOn) {
 					this.refreshBuses();
 					((Activity)ctx).runOnUiThread(new Runnable() {
 						
 						@Override
 						public void run() {
 							try {
 								mapUpdater.refreshBusesUI();
 							} catch (AMException e) {
 								e.printStackTrace();
 							}
 						}
 					});
 					Thread.currentThread().sleep(this.autoRefreshInterval);
 				}
 			} catch (Exception e) {
 				// TODO handle issues refreshing buses
 				e.printStackTrace();
 			}
 		}
 		
 	}	
 }
  
