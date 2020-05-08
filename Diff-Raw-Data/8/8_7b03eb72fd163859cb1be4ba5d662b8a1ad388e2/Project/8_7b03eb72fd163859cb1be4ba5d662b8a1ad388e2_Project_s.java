 package models;
 
 import com.google.code.morphia.annotations.Entity;
 import com.google.code.morphia.annotations.Reference;
 import play.data.validation.Required;
 import play.modules.morphia.Model;
 import utils.Constants.UserType;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @Entity
 public class Project extends Model {
  
 	@Required
 	public String name;
 	
 	@Required
 	public String headline;
 
     @Required
     public String subHeadLine;
 
     public String subHeadLine2;
 
     @Required
     public String content_join;
 
     @Required
     public String content_when;
 
     @Required
     public String content_where;
 
     @Required
     public String content_what;
 
     @Required
     public String content_more;
 
     public String contact_mail;
 
     public String contact_fb;
 
     public String contact_twitter;
 	
 //	@Required
 //	public String description;
 //
 	@Required
 	public String goal;
 	
 	public Boolean nonProfit;
 	
 	@Reference
 	public User owner;
 	
 	@Reference
 	public List<Category> categories;
 
     @Reference
     public List<Image> images;
 	
 	@Reference
 	public City city;
 	
 	/**
 	 * Returns all the possible cities based
 	 * on the owner's Country
 	 * @return List of cities, or empty if owner is not initialized
 	 */
 	public List<City> getPossibleCities(){
 		List<City> result = new ArrayList<City>();
 		if (owner != null && owner.country != null){
 			if (owner.userType == UserType.ADMIN.getId()){
 				result = City.find().order("name").asList();
 			}else{
 				result = City.find("Country", owner.country).order("name").asList();
 			}
 		}	
 		return result;
 	}
 	
 }
