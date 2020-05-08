 package com.papagiannis.tuberun.plan;
 
 import java.io.Serializable;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.location.Location;
 
 /*
  * This class contains the requirements for a given plan request. The UI must translate
  * user choices to one object of this class.
  * The fetcher issues the request and then assigns to plan the resulting Routes. 
  */
 public class Plan implements Serializable {
 	private static final long serialVersionUID = 2L;
 	private static final SimpleDateFormat timeFormat = new SimpleDateFormat(
 			"HH:mm");
 	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
 			"yyyyMMdd");
 
 	private String destination = "";
 	private transient ArrayList<String> destinationAlternatives = new ArrayList<String>();
 	private Point destinationType = Point.STATION;
 	private String startingString = "";
 	private transient ArrayList<String> startingAlternatives = new ArrayList<String>();
 	private Point startingType = Point.LOCATION;
 	private transient Location startingLocation = null;
 
 	private Date timeDepartureLater = null;
 	private Date timeArrivalLater = null;
 	private Date travelDate = null;
 
 	private transient boolean useTube = true;
 	private transient boolean useBuses = true;
 	private transient boolean useDLR = true;
 	private transient boolean useRail = true;
 	private transient boolean useBoat = true;
 
 	private ArrayList<Route> routes = new ArrayList<Route>();
 	private transient String error = "";
 
 	private Boolean isStored = false;
 
 	public Plan() {
 	}
 
 	public void clearRoutes() {
 		routes.clear();
 	}
 
 	public void addRoute(Route route) {
 		routes.add(route);
 	}
 
 	public Plan copyRoutesFrom(Plan from) {
 		routes.clear();
 		routes.addAll(from.routes);
 		return this;
 	}
 
 	public ArrayList<Route> getRoutes() {
 		return routes;
 	}
 
 	public String getRequestString() {
 		// return the GET request string according to the JP API.
 		StringBuilder sb = new StringBuilder();
 		try {
 			sb.append("language=en");
 			sb.append("&sessionID=0");
 			if (timeDepartureLater != null) {
 				sb.append("&itdTripDateTimeDepArr=dep");
 				if (travelDate != null) {
 					sb.append("&itdDate=");
 					sb.append(dateFormat.format(travelDate));
 				}
 				sb.append("&itdTime=");
 				sb.append(timeFormat.format(timeDepartureLater));
 			} else if (timeArrivalLater != null) {
 				sb.append("&itdTripDateTimeDepArr=arr");
 				if (travelDate != null) {
 					sb.append("&itdDate=");
 					sb.append(dateFormat.format(travelDate));
 				}
 				sb.append("&itdTime=");
 				sb.append(timeFormat.format(timeArrivalLater));
 			}
 
 			sb.append("&name_destination=");
 			sb.append(URLEncoder.encode(destination, "utf-8"));
 			sb.append("&place_destination=London");
 			sb.append("&type_destination=");
 			sb.append(Point.toRequestString(destinationType));
 
 			sb.append("&name_origin=");
 			if (startingType == Point.LOCATION) {
 				sb.append(startingLocation.getLongitude());
 				sb.append(":");
 				sb.append(startingLocation.getLatitude());
 				sb.append(":WGS84[DD.ddddd]");
 
 			} else {
 				sb.append(URLEncoder.encode(startingString, "utf-8"));
 			}
 			sb.append("&place_origin=London");
 			sb.append("&type_origin=");
 			sb.append(Point.toRequestString(startingType));
 
 			if (!useBoat || !useBuses || !useDLR || !useRail || !useTube) {
 				sb.append("&excludedMeans=checkbox");
 				if (!useBoat)
 					sb.append("&exclMOT_9");
 				if (!useBuses)
 					sb.append("&exclMOT_5");
 				if (!useDLR)
 					sb.append("&exclMOT_1");
 				if (!useRail)
 					sb.append("&exclMOT_0");
 				if (!useTube)
 					sb.append("&exclMOT_2");
 			}
 
 		} catch (Exception e) {
 
 		}
 		String reply = sb.toString();
 		return reply;
 	}
 
 	@SuppressWarnings("deprecation")
 	public boolean isValid() {
 		error = "";
 		if (destination == null || destination.equals(""))
 			error += "The destination cannot be empty. ";
 		if (startingType == Point.LOCATION && startingLocation == null)
 			error += "Your current location is not yet known. ";
 		if (startingType != Point.LOCATION && startingString.equals(""))
 			error += "The starting point cannot be empty. ";
 		if (travelDate != null) {
 			if (timeArrivalLater != null) {
 				timeArrivalLater.setDate(travelDate.getDate());
 				timeArrivalLater.setMonth(travelDate.getMonth());
 				timeArrivalLater.setYear(travelDate.getYear());
 			}
 			if (timeDepartureLater != null) {
 				timeDepartureLater.setDate(travelDate.getDate());
 				timeDepartureLater.setMonth(travelDate.getMonth());
 				timeDepartureLater.setYear(travelDate.getYear());
 			}
 		}
 		boolean todayTravel = travelDate == null
 				|| dateFormat.format(travelDate).equals(
 						dateFormat.format(new Date()));
 		if (!todayTravel) {
 			if (timeDepartureLater != null
 					&& timeDepartureLater.before(new Date())) {
 				error += "The time of departure must be sometime in the future. ";
 			}
 			if (timeArrivalLater != null && timeArrivalLater.before(new Date())) {
 				error += "The time of arrival must be sometime in the future. ";
 
 			}
 		}
 		return error.equals("");
 	}
 
 	// Stupid getters and setters
 
 	public String getError() {
 		return error;
 	}
 
 	public String getDestination() {
 		return destination;
 	}
 
 	public void setDestination(String destination) {
 		this.destination = destination;
 	}
 
 	public Point getDestinationType() {
 		return destinationType;
 	}
 
 	public void setDestinationType(Point destinationType) {
 		this.destinationType = destinationType;
 	}
 
 	public String getStartingString() {
 		return startingString;
 	}
 
 	public void setStartingString(String startingString) {
 		this.startingString = startingString;
 	}
 
 	public Point getStartingType() {
 		return startingType;
 	}
 
 	public void setStartingType(Point startingType) {
 		this.startingType = startingType;
 	}
 
 	public Location getStartingLocation() {
 		return startingLocation;
 	}
 
 	public void setStartingLocation(Location startingLocation) {
 		this.startingLocation = startingLocation;
 	}
 
 	public Date getTimeDepartureLater() {
 		return (timeDepartureLater == null) ? new Date() : timeDepartureLater;
 	}
 
 	public void setTimeDepartureLater(Date timeDepartureLater) {
 		this.timeDepartureLater = timeDepartureLater;
 		if (timeDepartureLater != null)
 			this.timeArrivalLater = null;
 	}
 
 	public Date getTimeArrivalLater() {
 		return (timeArrivalLater == null) ? new Date() : timeArrivalLater;
 	}
 
 	public void setTimeArrivalLater(Date timeArrivalLater) {
 		this.timeArrivalLater = timeArrivalLater;
 		if (timeArrivalLater != null)
 			this.timeDepartureLater = null;
 	}
 
 	public void setTravelDate(Date travelDate) {
 		this.travelDate = travelDate;
 	}
 
 	public Date getTravelDate() {
 		if (travelDate == null)
 			return new Date();
 		else
 			return travelDate;
 	}
 
 	public boolean isUseTube() {
 		return useTube;
 	}
 
 	public void setUseTube(boolean useTube) {
 		this.useTube = useTube;
 	}
 
 	public boolean isUseBuses() {
 		return useBuses;
 	}
 
 	public void setUseBuses(boolean useBuses) {
 		this.useBuses = useBuses;
 	}
 
 	public boolean isUseDLR() {
 		return useDLR;
 	}
 
 	public void setUseDLR(boolean useDLR) {
 		this.useDLR = useDLR;
 	}
 
 	public boolean isUseRail() {
 		return useRail;
 	}
 
 	public void setUseRail(boolean useRail) {
 		this.useRail = useRail;
 	}
 
 	public boolean isUseBoat() {
 		return useBoat;
 	}
 
 	public void setUseBoat(boolean useBoat) {
 		this.useBoat = useBoat;
 	}
 
 	public void addAlternativeDestination(String destination) {
 		destinationAlternatives.add(destination);
 	}
 
 	public ArrayList<String> getAlternativeDestinations() {
 		return destinationAlternatives;
 	}
 
 	public void addAlternativeOrigin(String destination) {
 		startingAlternatives.add(destination);
 	}
 
 	public ArrayList<String> getAlternativeOrigins() {
 		return startingAlternatives;
 	}
 
 	public void copyAlterativeDestinationsFrom(Plan result) {
 		for (String s : result.getAlternativeDestinations())
 			destinationAlternatives.add(s);
 
 	}
 
 	public void copyAlterativeOriginsFrom(Plan result) {
 		for (String s : result.getAlternativeOrigins())
 			startingAlternatives.add(s);
 	}
 
 	public boolean hasAlternatives() {
 		return destinationAlternatives.size() > 0
 				|| startingAlternatives.size() > 0;
 	}
 
 	public void clearAlternativeDestinations() {
 		destinationAlternatives.clear();
 
 	}
 
 	public void clearAlternativeOrigins() {
 		startingAlternatives.clear();
 
 	}
 
 	public Boolean isStored() {
 		return isStored;
 	}
 
 	public Plan setStored(Boolean isStored) {
 		this.isStored = isStored;
 		return this;
 	}
 
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		if (getStartingType() == Point.LOCATION)
 			sb.append("Current GPS Location");
 		else
 			sb.append(getStartingString());
 		sb.append(" to ");
 		sb.append(getDestination());
 		return sb.toString();
 	}
 
 	public String toStringWithTotalRoutes() {
 		return toString() + " (" + routes.size() + " routes)";
 	}
 
	public Plan copyBasicInfo() {
		Plan result=new Plan();
		result.destination=destination;
		result.destinationType=destinationType;
		result.startingLocation=startingLocation;
		result.startingString=startingString;
		result.startingType=startingType;
		result.timeArrivalLater=timeArrivalLater;
		result.timeDepartureLater=timeDepartureLater;
		result.travelDate=travelDate;
		result.useBoat=useBoat;
		result.useBuses=useBuses;
		result.useDLR=useDLR;
		result.useRail=useRail;
		result.useTube=useTube;
		return result;
	}

 }
