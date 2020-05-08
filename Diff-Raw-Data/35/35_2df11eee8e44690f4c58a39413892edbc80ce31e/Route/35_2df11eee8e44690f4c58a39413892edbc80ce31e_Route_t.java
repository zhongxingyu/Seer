 package de.christianzunker.mobilecitygate.beans;
 
 import java.util.List;
 
 public class Route {
 	
 	private String name = null;
 	private Double length = null;
 	private String description = null;
 	private int id = 0;
 	private String mapKML = null;
 	private int routeType = 0;
 	private String locale = "";
 	private String lengthUnit = "";
 	private double startLon = 0.0;
 	private double startLat = 0.0;
	private double endLon = 0.0;
	private double endLat = 0.0;
 	private String color = "";
 	
 	private List<Poi> pois = null;
 	
 	
 	
 	
 	
 	public String getColor() {
 		return color;
 	}
 	public void setColor(String color) {
 		this.color = color;
 	}
 	public double getStartLon() {
 		return startLon;
 	}
 	public void setStartLon(double startLon) {
 		this.startLon = startLon;
 	}
 	public double getStartLat() {
 		return startLat;
 	}
 	public void setStartLat(double startLat) {
 		this.startLat = startLat;
 	}
 	public String getLengthUnit() {
 		return lengthUnit;
 	}
 	public void setLengthUnit(String lengthUnit) {
 		this.lengthUnit = lengthUnit;
 	}
 	public String getDescription() {
 		return description;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	public String getLocale() {
 		return locale;
 	}
 	public void setLocale(String locale) {
 		this.locale = locale;
 	}
 	public List<Poi> getPois() {
 		return pois;
 	}
 	public void setPois(List<Poi> pois) {
 		this.pois = pois;
 	}
 	public int getRouteType() {
 		return routeType;
 	}
 	public void setRouteType(int routeType) {
 		this.routeType = routeType;
 	}
 	public String getMapKML() {
 		return mapKML;
 	}
 	public void setMapKML(String mapKML) {
 		this.mapKML = mapKML;
 	}
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public Double getLength() {
 		return length;
 	}
 	public void setLength(Double length) {
 		this.length = length;
 	}
	public double getEndLon() {
		return endLon;
	}
	public void setEndLon(double endLon) {
		this.endLon = endLon;
	}
	public double getEndLat() {
		return endLat;
	}
	public void setEndLat(double endLat) {
		this.endLat = endLat;
	}
	
 }
