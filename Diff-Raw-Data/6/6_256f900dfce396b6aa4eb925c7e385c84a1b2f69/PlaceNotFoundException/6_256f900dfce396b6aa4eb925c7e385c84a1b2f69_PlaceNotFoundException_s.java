 /**
  * 
  */
 package winterwell.jgeoplanet;
 
 /**
  * Thrown when a place, specified by a WOE ID or search term could not be found.
  * @author Joe Halliwell <joe@winterwell.com>
  */
 public class PlaceNotFoundException extends GeoPlanetException {
 
 	private static final long serialVersionUID = 6987782024287635157L;
 	
 	private String placeName;
 	
 	PlaceNotFoundException(String placeName) {
 		super(placeName + " could not be located");
 		this.placeName = placeName;
 	}
 	
 	/**
	 * Return the name of the place that could not be found.
	 * This can also be a (string representation of) a WOE ID.
	 * @return representation of place that could not be found
 	 */
 	public String getPlaceName() {
 		return placeName;
 	}
 }
