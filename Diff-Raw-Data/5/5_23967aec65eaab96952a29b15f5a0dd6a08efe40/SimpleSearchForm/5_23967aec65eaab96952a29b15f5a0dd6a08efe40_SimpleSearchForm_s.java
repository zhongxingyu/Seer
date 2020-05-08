 package edu.cmu.ebiz.task8.formbean;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.mybeans.form.FormBean;
 
 public class SimpleSearchForm extends FormBean{
 	private String searchPlaces;
 	private String placeTypes;
 	private String longitude;
 	private String latitude;
 	private String searchLocation;
 	
 	public List<String> getValidationErrors() {
 		List<String> errors = new ArrayList<String>();
 		//System.out.println(longitude);
 		
 		return errors;
 	}
 
 	public String getSearchPlaces() {
 		return searchPlaces;
 	}
 
 	public void setSearchPlaces(String searchPlaces) {
 		this.searchPlaces = trimAndConvert(searchPlaces.trim(), "<>\"");
 	}
 
 	public String getPlaceTypes() {
 		return placeTypes;
 	}
 
 	public void setPlaceTypes(String placeTypes) {
 		this.placeTypes = placeTypes;
 	}
 
 	public String getLongitude() {
 		return longitude;
 	}
 
 	public void setLongitude(String longitude) {
 		this.longitude = trimAndConvert(longitude.trim(), "<>\"");
 	}
 
 	public String getLatitude() {
 		return latitude;
 	}
 
 	public void setLatitude(String latitude) {
 		this.latitude = trimAndConvert(latitude.trim(), "<>\"");
 	}
 
 	public String getSearchLocation() {
 		return searchLocation;
 	}
 
 	public void setSearchLocation(String searchLocation) {
 		
 		this.searchLocation = trimAndConvert(searchLocation.trim(), "<>\"");
		String[] tmp = searchLocation.split(" ");
		for (String term : tmp){
			this.searchLocation += "%20" + term ;
		}
 	}
 	
 }
