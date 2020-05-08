 package eu.trentorise.smartcampus.domain.trentinofamiglia.helper;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import eu.trentorise.smartcampus.domain.discovertrento.GenericPOI;
 
 public class OrganizationsData {
 
 	private Map<String,String> idCoords;
 	
 	private List<GenericPOI> valid;
 	private List<String> errors;
 	
 	public OrganizationsData() {
 		idCoords = new TreeMap<String, String>();
 		valid = new ArrayList<GenericPOI>();
 		errors = new ArrayList<String>();
 	}
 	
 	public static OrganizationsData init() {
 		return new OrganizationsData();
 	}
 	
 	public static OrganizationsData checkValidity(OrganizationsData od, GenericPOI[] pois) {
 		od.valid = new ArrayList<GenericPOI>();
 		od.errors = new ArrayList<String>();
 		
 		Set<String> newIds = new HashSet<String>();
 		for (GenericPOI poi: pois) {
 			newIds.add(poi.getId());
 		}
 		int removed = 0;
 		
 		for (String id: od.idCoords.keySet()) {
 			if (!newIds.contains(id)) {
 				removed++;
 			}
 		}
 		if (removed > 3) {
 			od.errors.add("Too many removed POI (" + removed + ")");
 		}
 		
 		
 		boolean firstTime = od.idCoords.keySet().size() == 0;
 		for (GenericPOI poi: pois) {
 			String id = poi.getId();
 			String value = poi.getPoiData().getLatitude() + "_" + poi.getPoiData().getLongitude();
 			if (!firstTime) {
 				String oldValue = od.idCoords.get(id);
 				if (oldValue == null || oldValue.equals(value) && removed <= 3) {
 					od.valid.add(poi);
 					od.idCoords.put(id, value);
 				}
 				if (oldValue != null && !oldValue.equals(value)) {
 					od.errors.add("Mismatching POI " + poi.getTitle() + "("+ poi.getId() + "): old = " + oldValue + ", new = " + value);
 				}				
 			} else {
 				od.idCoords.put(id, value);
 				od.valid.add(poi);
 			}
 		}
 		
 		return od;
 	}
 	
 	public static GenericPOI[] getValidPOI(OrganizationsData od) {
 		return od.valid.toArray(new GenericPOI[od.valid.size()]);
 	}
 	
 	public static String[] getErrors(OrganizationsData od) {
 		return od.errors.toArray(new String[od.errors.size()]);
 	}
 
	public Map<String, String> getIdCoords() {
 		return idCoords;
 	}
 
	public void setIdCoords(Map<String, String> idCoords) {
		this.idCoords = idCoords;
 	}
 
 	public List<GenericPOI> getValid() {
 		return valid;
 	}
 
 	public void setValid(List<GenericPOI> valid) {
 		this.valid = valid;
 	}
 
 	public List<String> getErrors() {
 		return errors;
 	}
 
 	public void setErrors(List<String> errors) {
 		this.errors = errors;
 	}
 
 }
