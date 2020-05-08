 package models;
 
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 
 import play.data.format.Formats;
 import play.data.validation.Constraints;
 import play.data.validation.Constraints.Email;
 import play.db.ebean.Model;
 
 import com.avaje.ebean.Expr;
 
 /**
  * Computer entity managed by Ebean
  */
 @SuppressWarnings("serial")
 @Entity 
 public class BabySitter extends Model {
 
     @Id
     public Long id;
     
 	@OneToMany(mappedBy="babySitter", cascade=CascadeType.ALL)
 	public List<BabySitterAvailable> availability;
 	
     @Constraints.Required
     public String firstName;
 
 	@Constraints.Required
 	public String lastName;
 
     @Formats.DateTime(pattern="yyyy-MM-dd")
     public Date birthday;
     
     public String video;
     
     public String basicInfo;
     
     public String description;
     
     public List<String> parentRecommendations;
     
     public List<String> sitterRecommendations;
     
     public List<String> friends;
     
     public String location;
     
     public String phone;
     
     @Email
     public String email;
     
     /**
      * Generic query helper for entity Computer with id Long
      */
     public static Finder<Long,BabySitter> find = new Finder<Long,BabySitter>(Long.class, BabySitter.class); 
     
     /**
      * Should the whole method be moved to the BabySitterAvailable?
      */
     public static List<BabySitterAvailable> find(double latitude, double longitude, Date start, Date end) {
     	// Deprecated stuff. Do these again if you have time...
     	Timestamp startTs = new Timestamp(start.getTime());
     	Timestamp endTs = new Timestamp(end.getTime());
     	
     	List<BabySitterAvailable> list = BabySitterAvailable.find.
     			where(Expr.and(Expr.le("start_time", startTs.toString()), 
     					Expr.ge("end_time", endTs.toString()))).findList();
     	
     	for (BabySitterAvailable bsa : list) {
     		// because lazy loading does not work from the templates, we have to cheat like this 
     		// or create getters and setters? http://osdir.com/ml/play-framework/2012-02/msg01672.html
    		bsa.babySitter = BabySitter.find.byId(bsa.babySitter.id);
     	}
     	
     	return list;
     }
     
     public static BabySitter getSitter(String id) {
         return 
 	        find.byId(Long.parseLong(id));
     }
 
 
 	public static List<BabySitter> all() {
 	  return find.all();
 	}
 
 	public static void create(BabySitter bs) {
 	  bs.save();
 	}
 
 	public static void delete(Long id) {
 	  find.ref(id).delete();
 	}    
 
 }
 
