 package il.ac.tau.team3.common;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.Inheritance;
 import javax.jdo.annotations.InheritanceStrategy;
 import javax.jdo.annotations.NotPersistent;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 import com.beoui.geocell.GeocellManager;
 import com.beoui.geocell.annotations.Geocells;
 import com.beoui.geocell.annotations.Latitude;
 import com.beoui.geocell.annotations.Longitude;
 import com.beoui.geocell.model.Point;
 
 @PersistenceCapable
 @Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
 public class GeneralLocation implements Serializable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6359704904127673323L;
 	
 	@NotPersistent
 	private SPGeoPoint spGeoPoint;
 	
 	private String name;
 	
 	@PrimaryKey
 	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
 	private Long id;
 	
 	@JsonIgnore
 	@Persistent
 	@Longitude
 	double longitude;
 
 	@JsonIgnore
 	@Persistent
 	@Latitude
 	double latitude;
 
 	@JsonIgnore
 	@Persistent
 	@Geocells
 	private List<String> geocells = new ArrayList<String>();
 	
 	
 	public void setGeocells(List<String> geocellsP) {
 		this.geocells = geocellsP;
 	}
 
 	public Long getId() {
         return id;
     }
 	
 	public void setId(Long id) {
         this.id = id;
     }
 
 	@JsonIgnore
 	public double getLongitude() {
 		return longitude;
 	}
 
 	
 	public void setLongitude(double longitude) {
 		this.longitude = longitude;
 	}
 
 	@JsonIgnore
 	public double getLatitude() {
 		return latitude;
 	}
 
 	public void setLatitude(double latitude) {
 		this.latitude = latitude;
 	}
 
 	@JsonIgnore
 	public List<String> getGeocells() {
 		return geocells;
 	}
 
 	public void setGeocells(double lat,double longt) {
		Point p = new Point(latitude, longitude);
 		geocells = GeocellManager.generateGeoCell(p);
 		this.longitude = longt;
 		this.latitude = lat;
 	}
 	
 	public GeneralLocation(){
 	
 	}
 	
 	public GeneralLocation(GeneralLocation obj)	{
 		closeUserData(obj);
 		setId(obj.getId());
 		
 	}
 	
 
 	public GeneralLocation(SPGeoPoint spGeoPoint, String name) {
 		this.spGeoPoint = spGeoPoint;
 		this.name = name;
 	}
 	public void setSpGeoPoint(SPGeoPoint spGeoPoint) {
 		this.spGeoPoint = spGeoPoint;
 		setGeocells(spGeoPoint.getLatitudeInDegrees(), spGeoPoint.getLongitudeInDegrees());
 	}
 	public SPGeoPoint getSpGeoPoint() {
 		if (null == spGeoPoint)	{
 			spGeoPoint = new SPGeoPoint((int)(latitude * 1000000), (int)(longitude * 1000000));
 		}
 		return spGeoPoint;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public void closeUserData(GeneralLocation obj) {
 		setLatitude(obj.getLatitude());
 		setLongitude(obj.getLongitude());
 		setName(obj.getName());
 		setGeocells(obj.getLatitude(), obj.getLongitude());
 		
 	}
 }
