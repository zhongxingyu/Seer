 package controllers;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Constructor;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Calendar;
 import java.util.List;
 
 import org.junit.experimental.categories.Categories.IncludeCategory;
 
 import controllers.CRUD.ObjectType;
 
 import models.Incident;
 import models.IncidentCategory;
 import models.Location;
 import models.User;
 import play.db.Model;
 import play.exceptions.TemplateNotFoundException;
 import play.mvc.Controller;
 import services.Ushahidi;
 
 public class Application extends Controller 
 {
     public static final Ushahidi USHAHIDI = new Ushahidi("https://simpelers.crowdmap.com/api", "andrew@tillnow.com", "qazwsx");
 
     public static void index() 
     {
     	try {
     		List<Incident> all = Incident.find("order by incidentDate desc").fetch();
     		
 			render(all);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     }
     
     public static void newReport() throws Exception
     {
         Incident incident = new Incident();
         List<IncidentCategory> categories = IncidentCategory.findAll();
         List<Location> locations = Location.findAll();
         try {
             render(incident, categories, locations);
         } catch (TemplateNotFoundException e) {
         	e.printStackTrace();
         }
     }
     
     public static void saveReport(Object categoryId, String title, Location locationId, String content, String direction) throws Exception
     {
     	//TODO fix location and category
     	Incident i = new Incident((IncidentCategory) IncidentCategory.find("byName", "Fire").first(),
     			title,
     			content,
    			Calendar.getInstance().getTime(),
     			0,
     			(Location) Location.find("byName", "Cite Soleil").first(),
     			direction,
     			(User) User.find("byFirstName", "john").first()); //HARDCODED!!
         
         // Validate
         validation.valid(i);
         if(validation.hasErrors()) {
             render("@form", i);
         }
         // Save
         i.save();
         index();
     }
 }
