 package models;
 
 import java.util.*;
 
 import javax.persistence.*;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 
 import com.avaje.ebean.*;
 
 /**
  * Platform entity managed by Ebean
  */
 @Entity
 @Table(name = "platform") 
 public class Platform extends Model {
 
 	@Column(name="Platform_ID")
     @Id
 	@GeneratedValue
     public Long id;
     
 	@Column(name="Platform_Name")
     @Constraints.Required
     public String name;
     
     /**
      * Generic query helper for entity Computer with id Long
      */
     public static Finder<Long,Platform> find = new Finder<Long,Platform>(Long.class, Platform.class); 
     
     /**
      * Return a page of Platform
      *
      * @param page Page to display
      * @param pageSize Number of computers per page
      * @param sortBy Computer property used for sorting
      * @param order Sort order (either or asc or desc)
      * @param filter Filter applied on the name column
      */
     public static Page<Platform> page(int page, int pageSize, String sortBy, String order, String filter) {
         return 
             find.where()
                 .ilike("name", "%" + filter + "%")
                 .orderBy(sortBy + " " + order)
                 .findPagingList(pageSize)
                 .getPage(page);
     }
     /**
      * Creates a list of Platforms
      * @return a list of all the Platforms
      */
     public static List<Platform> getList() {
     	return find.all();
     }
     
     /**
      * 
      * @return name of the platform.
      */
     public String getPlatformName() {
     	return name;
     }
 	
 	//Used for creating a list of all companies. Implemented in createForm.scala.html in @select
     public static Map<String,String> options() {
         LinkedHashMap<String,String> options = new LinkedHashMap<String,String>();
         for(Platform c: Platform.find.orderBy("name").findList()) {
 			if(c.name.contains("Unknown")){//Skip "Unknown Platform" when listing options
 				continue;
 			}
             options.put(c.id.toString(), c.name);
         }
         return options;
     }
 	/**
 	 * This method takes a platform ID and returns the platform
 	 * 
 	 * @param platformID ID to find platform
 	 *
 	 * @return Platform
 	 */
 	public static Platform getByID(Long platformID){
 		Platform platform=find.where()
 			.eq("id",platformID)
 			.findUnique();
 		return platform;
 	}
     
 	/**
 	 * This method takes a string that is a file path and extracts the platform of the run from it
 	 * @param folderPath A path like "\\snowtest\Regression\java\13.5-Build2.1-MSOffice\Issues.13.5-Build2-MSOffice"
 	 * @return Platform corresponding to that folder path. In this example, would be java platform
 	 */
 	public static Platform getPlatformFromPath(String folderPath){
 		int indexOfPlatform = 4; //Which slot in the filepath the platform is specified in (0\1\2\3\4...)
 		
		if(folderPath.toLowerCase().contains("/d/")){
 			indexOfPlatform = 5;
 		}
 		
 		String[] platform = folderPath.split("\\\\"); //Expand as an array with "\" as the delimiter
 		
 		Platform plat = find.where()
 				.ilike("name",platform[indexOfPlatform])
 				.findUnique();
 		if(plat==null){
 			Platform newPlat = find.where()
 				.ilike("name","Unknown Platform")
 				.findUnique();
 			return newPlat;
 		}
 		return plat;
 	}
 }
 
