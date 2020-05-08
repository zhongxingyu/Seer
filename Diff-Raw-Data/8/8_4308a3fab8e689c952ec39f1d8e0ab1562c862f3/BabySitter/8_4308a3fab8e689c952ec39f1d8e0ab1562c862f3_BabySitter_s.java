 package models;
 
 import java.sql.Timestamp;
import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 
 import play.Logger;
 import play.data.format.Formats;
 import play.data.validation.Constraints;
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
     
     /**
      * Generic query helper for entity Computer with id Long
      */
     public static Finder<Long,BabySitter> find = new Finder<Long,BabySitter>(Long.class, BabySitter.class); 
     
     /**
      * Return a page of computer
      *
      * @param page Page to display
      * @param pageSize Number of computers per page
      * @param sortBy Computer property used for sorting
      * @param order Sort order (either or asc or desc)
      * @param filter Filter applied on the name column
      */
     public static List<BabySitter> find(String where, Date start, Date end) {
 // Deprecated stuff. Do these again if you have time...
     	Timestamp startTs = new Timestamp(start.getTime());
     	Timestamp endTs = new Timestamp(end.getTime());
     	
     	List<BabySitterAvailable> list = BabySitterAvailable.find.
     			where(Expr.and(Expr.le("start_time", startTs.toString()), 
     					Expr.ge("end_time", endTs.toString()))).findList();
     	// FIXME this should be done in the SQL query...
     	List<BabySitter> sitters = new ArrayList<BabySitter>();
     	for (BabySitterAvailable bsa : list) {    		
     		sitters.add(bsa.babySitter);
     		Logger.debug("added " + bsa.babySitter.firstName);
     	}
     	
     	return sitters;
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
 
