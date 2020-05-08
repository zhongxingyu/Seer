 package com.eatthepath.gtfs;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.eatthepath.gtfs.realtime.Vehicle;
 import com.eatthepath.jeospatial.util.SimpleGeospatialPoint;
 import com.eatthepath.jeospatial.vptree.VPTree;
 import com.google.transit.realtime.GtfsRealtime.Alert;
 import com.google.transit.realtime.GtfsRealtime.FeedEntity;
 import com.google.transit.realtime.GtfsRealtime.FeedMessage;
 import com.google.transit.realtime.GtfsRealtime.TripUpdate;
 import com.google.transit.realtime.GtfsRealtime.VehiclePosition;
 
 public class TransitSystem {
 	private final String id;
 	
 	private final List<URL> realtimeUrls;
 	private final FeedInfo feedInfo;
 	
 	private final Map<String, Agency> agenciesById;
 	private final Map<String, Stop> stopsById;
 	private final Map<String, Route> routesById;
 	private final Map<String, ServiceSchedule> serviceSchedulesById;
 	
 	private final HashMap<Stop, List<Route>> routesByStop;
 	private final HashMap<String, Trip> tripsById;
 	private final VPTree<Stop> stopTree;
 	
 	private final transient ReadWriteLock realtimeDataLock;
 	
 	private final transient HashMap<URL, Long> realtimeFeedTimestamps;
 	
 	private final transient HashMap<String, Vehicle> vehiclesById;
 	private final transient HashMap<Route, List<Vehicle>> vehiclesByRoute;
 	private final transient HashMap<Trip, List<Vehicle>> vehiclesByTrip;
 	
 	private final Logger log = LoggerFactory.getLogger(TransitSystem.class);
 	
 	protected TransitSystem(final String id, final FeedInfo feedInfo, final Map<String, Agency> agenciesById, final Map<String, Stop> stopsById, final Map<String, Route> routesById, final Map<String, ServiceSchedule> serviceSchedulesById, final List<URL> realtimeUrls) {
 		this.id = id;
 		
 		this.feedInfo = feedInfo;
 		
 		this.agenciesById = agenciesById;
 		this.stopsById = stopsById;
 		this.routesById = routesById;
 		this.serviceSchedulesById = serviceSchedulesById;
 		
 		this.realtimeUrls = realtimeUrls;
 		
 		final ArrayList<Stop> freestandingStops = new ArrayList<Stop>();
 		
 		for (final Stop stop : this.stopsById.values()) {
 			if (stop.getParentStation() == null) {
 				freestandingStops.add(stop);
 			}
 		}
 		
 		this.routesByStop = new HashMap<Stop, List<Route>>();
 		this.tripsById = new HashMap<String, Trip>();
 		
 		for (final Route route : this.routesById.values()) {
 			for (final Stop stop : route.getStopsServed()) {
 				if (!this.routesByStop.containsKey(stop)) {
 					this.routesByStop.put(stop, new ArrayList<Route>());
 				}
 				
 				this.routesByStop.get(stop).add(route);
 				
 				for (final Trip trip : route.getTrips()) {
 					this.tripsById.put(trip.getId(), trip);
 				}
 			}
 		}
 		
 		this.stopTree = new VPTree<Stop>(freestandingStops);
 		
 		this.realtimeDataLock = new ReentrantReadWriteLock();
 		
 		this.realtimeFeedTimestamps = new HashMap<URL, Long>();
 		
 		this.vehiclesById = new HashMap<String, Vehicle>();
 		this.vehiclesByRoute = new HashMap<Route, List<Vehicle>>();
 		this.vehiclesByTrip = new HashMap<Trip, List<Vehicle>>();
 	}
 	
 	public String getId() {
 		return this.id;
 	}
 	
     public FeedInfo getFeedInfo() {
 		return this.feedInfo;
 	}
 
 	public Agency getAgencyById(final String id) {
 		return this.agenciesById.get(id);
 	}
 
 	public Stop getStopById(final String id) {
 		return this.stopsById.get(id);
 	}
 	
 	public Set<Stop> getStopsServedByRoute(final Route route) {
 		return route.getStopsServed();
 	}
 	
 	public Trip getTripById(final String id) {
 		return this.tripsById.get(id);
 	}
 	
 	public Route getRouteById(final String id) {
 		return this.routesById.get(id);
 	}
 	
 	public Set<Route> getRoutesForStop(final Stop stop) {
 		try {
 			return new HashSet<Route>(this.routesByStop.get(stop));
 		} catch(NullPointerException e) {
 			// This can happen if we don't have any routes that service the
 			// given stop; this is most likely to happen for stops that are
 			// actually parent stations. Just return an empty set in this case.
 			return new HashSet<Route>();
 		}
 	}
 	
 	public List<Stop> getStopsInBoundingBox(double west, double east, double north, double south) {
 	    return this.stopTree.getAllPointsInBoundingBox(west, east, north, south);
 	}
 	
 	public Set<Route> getRoutesForNearbyStops(double latitude, double longitude, int maxStops) {
 		HashSet<Route> routes = new HashSet<Route>();
 		List<Stop> stops = this.stopTree.getNearestNeighbors(new SimpleGeospatialPoint(latitude, longitude), maxStops);
 		
 		for(Stop stop : stops) {
 			for(Route route : this.getRoutesForStop(stop)) {
 				routes.add(route);
 			}
 		}
 		
 		return routes;
 	}
 	
 	public Set<Route> getRoutesForStopsInRange(double latitude, double longitude, double radius) {
 	    HashSet<Route> routes = new HashSet<Route>();
         List<Stop> stops = this.stopTree.getAllNeighborsWithinDistance(new SimpleGeospatialPoint(latitude, longitude), radius);
         
         for(Stop stop : stops) {
             for(Route route : this.getRoutesForStop(stop)) {
                 routes.add(route);
             }
         }
         
         return routes;
 	}
 	
 	public Vehicle getVehicleById(final String id) {
 		this.realtimeDataLock.readLock().lock();
 		
 		try {
 			return this.vehiclesById.get(id);
 		} finally {
 			this.realtimeDataLock.readLock().unlock();
 		}
 	}
 	
 	public List<Vehicle> getVehiclesByRoute(final Route route) {
 		this.realtimeDataLock.readLock().lock();
 		
 		try {
			return this.vehiclesByRoute.get(route);
 		} finally {
 			this.realtimeDataLock.readLock().unlock();
 		}
 	}
 	
 	public void fetchAndProcessRealtimeEvents() throws IOException {
 		for (final URL url : this.realtimeUrls) {
 			this.processEventsFromUrl(url);
 		}
 	}
 	
 	private void processEventsFromUrl(final URL url) throws IOException {
 		final InputStream eventInputStream = url.openStream();
 		
 		try {
 			final FeedMessage feedMessage = FeedMessage.parseFrom(eventInputStream);
 			
 			final boolean feedMayContainNewData;
 			
 			if (feedMessage.getHeader().hasTimestamp()) {
 				if (this.realtimeFeedTimestamps.containsKey(url)) {
 					feedMayContainNewData =
 							feedMessage.getHeader().getTimestamp() > this.realtimeFeedTimestamps.get(url);
 				} else {
 					feedMayContainNewData = true;
 				}
 				
 				this.realtimeFeedTimestamps.put(url, feedMessage.getHeader().getTimestamp());
 			} else {
 				// We don't know when the feed was last updated by the provider, so
 				// assume that it may contain new data.
 				feedMayContainNewData = true;
 			}
 			
 			if (feedMayContainNewData) {
 				final boolean isIncremental;
 				
 				if (feedMessage.getHeader().hasIncrementality()) {
 					switch (feedMessage.getHeader().getIncrementality()) {
 						case FULL_DATASET: {
 							isIncremental = false;
 							break;
 						} case DIFFERENTIAL: {
 							isIncremental = true;
 							break;
 						} default: {
 							log.warn(String.format("Unexpected incrementality \"%s\" in feed from \"%s\" for transit system \"%s\"",
 									feedMessage.getHeader().getIncrementality().toString(), url, this.getId()));
 							
 							isIncremental = false;
 						}
 					}
 				} else {
 					isIncremental = false;
 				}
 				
 				this.realtimeDataLock.writeLock().lock();
 				
 				try {
 					if (!isIncremental) {
 						this.flushRealtimeData();
 					}
 					
 					for (final FeedEntity entity : feedMessage.getEntityList()) {
 						if (entity.hasTripUpdate()) {
 							this.processTripUpdate(entity.getTripUpdate());
 						}
 						
 						if (entity.hasVehicle()) {
 							this.processVehiclePosition(entity.getVehicle());
 						}
 						
 						if (entity.hasAlert()) {
 							this.processAlert(entity.getAlert());
 						}
 					}
 				} finally {
 					this.realtimeDataLock.writeLock().unlock();
 				}
 			}
 		} finally {
 			try {
 				eventInputStream.close();
 			} catch (IOException e) {
 				log.error("Failed to close event input stream.", e);
 			}
 		}
 	}
 	
 	private void flushRealtimeData() {
 		this.realtimeDataLock.writeLock().lock();
 		
 		try {
 			this.vehiclesById.clear();
 			this.vehiclesByRoute.clear();
 			this.vehiclesByTrip.clear();
 		} finally {
 			this.realtimeDataLock.writeLock().unlock();
 		}
 	}
 	
 	private void processTripUpdate(final TripUpdate tripUpdate) {
 		// TODO
 	}
 	
 	private void processVehiclePosition(final VehiclePosition vehiclePosition) {
 		this.realtimeDataLock.writeLock().lock();
 		
 		try {
 			Vehicle vehicle = this.getVehicleById(vehiclePosition.getVehicle().getId());
 			
 			if (vehicle == null) {
 				vehicle = new Vehicle(vehiclePosition.getVehicle().getId());
 				
 				if (vehiclePosition.getVehicle().hasId()) {
 					this.vehiclesById.put(vehicle.getId(), vehicle);
 				}
 			}
 			
 			vehicle.setLabel(vehiclePosition.getVehicle().getLabel());
 			vehicle.setLicensePlate(vehiclePosition.getVehicle().getLicensePlate());
 			
 			if (vehiclePosition.hasPosition()) {
 				vehicle.setPosition(new SimpleGeospatialPoint(
 						vehiclePosition.getPosition().getLatitude(), vehiclePosition.getPosition().getLongitude()));
 				
 				vehicle.setBearing(vehiclePosition.getPosition().hasBearing() ? vehiclePosition.getPosition().getBearing() : null);
 				vehicle.setOdometer(vehiclePosition.getPosition().hasOdometer() ? vehiclePosition.getPosition().getOdometer() : null);
 				vehicle.setSpeed(vehiclePosition.getPosition().hasSpeed() ? vehiclePosition.getPosition().getSpeed() : null);
 			}
 			
 			if (vehiclePosition.hasTrip()) {
 				// TODO Deal with frequency-expanded trips
 				if (vehiclePosition.getTrip().hasTripId()) {
 					vehicle.setTrip(this.getTripById(vehiclePosition.getTrip().getTripId()));
 				} else if (vehiclePosition.getTrip().hasRouteId()) {
 					vehicle.setRoute(this.getRouteById(vehiclePosition.getTrip().getRouteId()));
 				} else {
 					vehicle.setTrip(null);
 					vehicle.setRoute(null);
 				}
 				
 				if (vehicle.getTrip() != null) {
 					final Trip trip = vehicle.getTrip();
 					
 					if (!this.vehiclesByTrip.containsKey(trip)) {
 						this.vehiclesByTrip.put(trip, new ArrayList<Vehicle>());
 					}
 					
 					this.vehiclesByTrip.get(trip).add(vehicle);
 				}
 				
 				if (vehicle.getRoute() != null) {
 					final Route route = vehicle.getRoute();
 					
 					if (!this.vehiclesByRoute.containsKey(route)) {
 						this.vehiclesByRoute.put(route, new ArrayList<Vehicle>());
 					}
 					
 					this.vehiclesByRoute.get(route).add(vehicle);
 				}
 			}
 			
 			if (vehiclePosition.hasStopId()) {
 				vehicle.setCurrentStop(this.getStopById(vehiclePosition.getStopId()));
 			} else if (vehiclePosition.hasCurrentStopSequence()) {
 				if (vehicle.getTrip() != null) {
 					final Stop stop = vehicle.getTrip().getStopByOriginalSequenceNumber(vehiclePosition.getCurrentStopSequence());
 					vehicle.setCurrentStop(stop);
 					
 					if (stop == null) {
 						log.warn(String.format(
 								"Vehicle \"%d\" is on trip \"%s\", but trip has no stop with original sequence number %d.",
 								vehicle.getId(), vehicle.getTrip().getId(), vehiclePosition.getCurrentStopSequence()));
 					}
 				} else {
 					log.warn(String.format("Vehicle \"%s\" makes reference to a stop sequence number, but is not associated with a trip.", vehicle.getId()));
 				}
 			}
 			
 			vehicle.setStopStatus(vehiclePosition.getCurrentStatus());
 			vehicle.setCongestionLevel(vehiclePosition.getCongestionLevel());
 		} finally {
 			this.realtimeDataLock.writeLock().unlock();
 		}
 	}
 	
 	private void processAlert(final Alert alert) {
 		// TODO
 	}
 }
