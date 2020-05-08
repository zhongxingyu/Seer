 package controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.OneToMany;
 
 import controllers.CRUD.ObjectType;
 
 import models.Location;
 import models.User;
 import play.*;
 import play.db.Model;
 import play.exceptions.TemplateNotFoundException;
 import play.mvc.*;
 
 public class Locations extends CRUD {
 
 //	@OneToMany(cascade=CascadeType.ALL, mappedBy="user")
 //	public List<Location> locations = new ArrayList<Location>();
 	
     public static void list(int page, String search, String searchFields, String orderBy, String order) {
         ObjectType type = ObjectType.get(getControllerClass());
         notFoundIfNull(type);
         if (page < 1) {
             page = 1;
         }
 
         User user = User.find("byEmail", Security.connected()).first();
        Long count = Location.count("byUser", user);
        
         List<Location> objects = Location.findByUser(user.email);
        
         Long totalCount = type.count(null, null, (String) request.args.get("where"));
         
         render(type, objects, count, totalCount, page, orderBy, order, user);
     }
     
 	public static void blank() {
         User connectedUser = User.find("byEmail", Security.connected()).first();
         render(connectedUser);
     }
 
 }
