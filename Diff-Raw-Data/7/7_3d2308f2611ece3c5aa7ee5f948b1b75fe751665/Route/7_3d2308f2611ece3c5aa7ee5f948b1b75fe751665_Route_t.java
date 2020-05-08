 package com.eatthepath.gtfs;
 
 import java.awt.Color;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.joda.time.Instant;
 import org.json.simple.JSONAware;
 import org.json.simple.JSONObject;
 
 import com.eatthepath.gtfs.realtime.Vehicle;
 
 public class Route implements JSONAware {
 	private final String id;
 	private final Agency agency;
 	private final String shortName;
 	private final String longName;
 	private final String description;
 	private final RouteType routeType;
 	private final URL routeUrl;
 	
 	private final Color color;
 	private final Color textColor;
 	
 	private final ArrayList<Trip> trips;
 	
 	private final transient ArrayList<Vehicle> vehicles;
 	
 	protected Route(final String id, final Agency agency, final String shortName, final String longName, final String description, final RouteType routeType, final URL routeUrl, final Color color, final Color textColor) {
 		if(id == null) {
 			throw new IllegalArgumentException("Route ID must not be null.");
 		}
 		
 		this.id = id;
 		this.agency = agency;
 		
 		if(shortName == null && longName == null) {
 			throw new IllegalArgumentException("One or both of short name or long name must not be null.");
 		}
 		
 		this.shortName = shortName;
 		this.longName = longName;
 		this.description = description;
 		
 		if(routeType == null) {
 			throw new IllegalArgumentException("Route type must not be null.");
 		}
 		
 		this.routeType = routeType;
 		
 		this.routeUrl = routeUrl;
 		
 		this.color = color;
		this.textColor = textColor;
 		
 		this.trips = new ArrayList<Trip>();
 		
 		this.vehicles = new ArrayList<Vehicle>();
 	}
 	
 	protected void addTrip(final Trip trip) {
 		this.trips.add(trip);
 	}
 	
 	public String getShortName() {
 		return this.shortName;
 	}
 	
 	public String getLongName() {
 		return this.longName;
 	}
 	
 	protected Set<Stop> getStopsServed() {
 		final HashSet<Stop> stops = new HashSet<Stop>();
 		
 		for (final Trip trip : trips) {
 			stops.addAll(trip.getStops());
 		}
 		
 		return stops;
 	}
 	
 	public ScheduledTrip getNextScheduledTrip(final Instant earliestStart) {
 		ScheduledTrip earliestScheduledTrip = null;
 		
 		for (final Trip trip : this.trips) {
 			final ScheduledTrip candidateTrip = trip.getNextScheduledTrip(earliestStart);
 			
 			if (candidateTrip != null) {
 				if (earliestScheduledTrip == null || candidateTrip.getScheduledStartTime().isBefore(earliestScheduledTrip.getScheduledStartTime())) {
 					earliestScheduledTrip = candidateTrip;
 				}
 			}
 		}
 		
 		return earliestScheduledTrip;
 	}
 	
 	protected List<Trip> getTrips() {
 		return java.util.Collections.unmodifiableList(this.trips);
 	}
 	
 	public boolean addVehicle(final Vehicle vehicle) {
 		return this.vehicles.add(vehicle);
 	}
 	
 	public boolean removeVehicle(final Vehicle vehicle) {
 		return this.vehicles.remove(vehicle);
 	}
 	
 	protected List<Vehicle> getVehicles() {
 		return java.util.Collections.unmodifiableList(this.vehicles);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public JSONObject toJSONObject() {
 		final JSONObject json = new JSONObject();
         
         json.put("id", this.id);
         json.put("agency", this.agency);
         json.put("shortName", this.shortName);
         json.put("longName", this.longName);
         json.put("description", this.description);
         json.put("routeType", this.routeType.toString());
         json.put("url", this.routeUrl);
        json.put("color", String.format("#%s", Integer.toHexString(this.color.getRGB()).substring(2)));
        json.put("textColor", String.format("#%s", Integer.toHexString(this.textColor.getRGB()).substring(2)));
         
         return json;
 	}
 	
     @Override
     public String toJSONString() {
         return this.toJSONObject().toJSONString();
     }
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((agency == null) ? 0 : agency.hashCode());
 		result = prime * result + ((color == null) ? 0 : color.hashCode());
 		result = prime * result
 				+ ((description == null) ? 0 : description.hashCode());
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result
 				+ ((longName == null) ? 0 : longName.hashCode());
 		result = prime * result
 				+ ((routeType == null) ? 0 : routeType.hashCode());
 		result = prime * result
 				+ ((routeUrl == null) ? 0 : routeUrl.hashCode());
 		result = prime * result
 				+ ((shortName == null) ? 0 : shortName.hashCode());
 		result = prime * result
 				+ ((textColor == null) ? 0 : textColor.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Route other = (Route) obj;
 		if (agency == null) {
 			if (other.agency != null)
 				return false;
 		} else if (!agency.equals(other.agency))
 			return false;
 		if (color == null) {
 			if (other.color != null)
 				return false;
 		} else if (!color.equals(other.color))
 			return false;
 		if (description == null) {
 			if (other.description != null)
 				return false;
 		} else if (!description.equals(other.description))
 			return false;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		if (longName == null) {
 			if (other.longName != null)
 				return false;
 		} else if (!longName.equals(other.longName))
 			return false;
 		if (routeType != other.routeType)
 			return false;
 		if (routeUrl == null) {
 			if (other.routeUrl != null)
 				return false;
 		} else if (!routeUrl.equals(other.routeUrl))
 			return false;
 		if (shortName == null) {
 			if (other.shortName != null)
 				return false;
 		} else if (!shortName.equals(other.shortName))
 			return false;
 		if (textColor == null) {
 			if (other.textColor != null)
 				return false;
 		} else if (!textColor.equals(other.textColor))
 			return false;
 		return true;
 	}
 }
