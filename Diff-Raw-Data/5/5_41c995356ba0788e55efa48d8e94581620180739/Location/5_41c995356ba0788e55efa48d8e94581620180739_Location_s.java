 package models;
 
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToMany;
 import javax.persistence.Transient;
 
 import play.db.jpa.Model;
 import play.mvc.Http.Request;
 import util.DistanceTool;
 
 import com.javadocmd.simplelatlng.LatLng;
 import com.sun.org.apache.bcel.internal.generic.LNEG;
 
 @Entity
 public class Location extends Model {
 
 	public Long chainId;
 
 	/** The id of the next location in the chain. Will be null for head */
 	public Long nextLocationId;
 	
 	public Double lat;
 	public Double lon;
 
 	public String pictureUrl;
 
 	public Category category;
 	
 	@OneToMany
 	@JoinColumn(name = "locationId")
 	public List<Comment> comments;
 
 	
 	@Transient
 	public String roughDistance;
 	
 	@Transient
 	public String resourceUrl;
 	
 	
 
 	public void setLatLng(LatLng latLng) {
 		lat = latLng.getLatitude();
 		lon = latLng.getLongitude();
 	}
 	
 	public LatLng asLatLng() {
		// TODO: temp fix for crappy data
		if (lat == null || lon == null) {
			return new LatLng(65.34, 25.23);
		}
		
 		return new LatLng(lat, lon);
 	}
 	
 	@Override
 	public String toString() {
 		return id+" "+category+" "+pictureUrl;
 	}
 
 	public static Location findLatestByChainId(Long chainId) {
 		return find("chainId = ? AND nextLocationId IS null", chainId).first();
 	}
 	
 	public void addComment(Comment comment) {
 		comments.add(comment);
 	}
 
 	public void updateRequestProperties(LatLng userLatLng) {
 		this.roughDistance = DistanceTool.getRoughDistance(userLatLng, asLatLng());
 		this.resourceUrl = getResourceUrl();
 	}
 	
 	public String getResourceUrl() {
 		// TODO: figure out why this isn't working
 		//Map map = M.make("id", location.id).map();
 		//location.resourceUrl = Router.reverse("api.v1.Locations.show", map).url;
 		return Request.current().getBase() + "/api/v1/locations/" + id;	
 	}
 }
