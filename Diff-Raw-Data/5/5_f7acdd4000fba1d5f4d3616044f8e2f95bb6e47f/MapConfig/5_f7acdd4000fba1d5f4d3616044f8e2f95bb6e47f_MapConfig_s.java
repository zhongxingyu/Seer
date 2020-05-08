 package de.rallye.model.structures;
 
 import org.codehaus.jackson.annotate.JsonCreator;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public class MapConfig {
 
 	public static final String NAME = "name";
 	public static final String LOCATION = "location";
 	public static final String ZOOM_LEVEL = "zoomLevel";
 	public static final String BOUNDS = "bounds";
 
 	final public String name;
 	final public LatLng location;
 	final public float zoomLevel;
 	final public List<LatLng> bounds;
 
	public MapConfig(String name, LatLng location, float zoomLevel, List<LatLng> bounds) {
 		this.name = name;
 		this.location = location;
 		this.zoomLevel = zoomLevel;
 		this.bounds = bounds;
 	}
 
     public static List<LatLng> getBounds(Set<String> bounds) {
         List<LatLng> res = new ArrayList<LatLng>();
         for (String s: bounds) {
             res.add(LatLng.fromString(s));
         }
         return res;
     }
 
     public Set<String> getBoundsAsSet() {
         Set<String> res = new HashSet<String>();
 
         for(LatLng loc: bounds) {
             res.add(loc.toString());
         }
         return res;
     }
 	
 	@Override
 	public String toString() {
 		return "Location : "+ name +"\nLocation: "+ location.toString() +"\nZoom: "+ zoomLevel +"\nBounds: "+ bounds.toString();
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (this == o) return true;
 		if (o == null || getClass() != o.getClass()) return false;
 
 		MapConfig that = (MapConfig) o;
 
 		if (Float.compare(that.zoomLevel, zoomLevel) != 0) return false;
 		if (!location.equals(that.location)) return false;
 		if (!name.equals(that.name)) return false;
 		if (!bounds.equals(that.bounds)) return false;
 
 		return true;
 	}
 }
