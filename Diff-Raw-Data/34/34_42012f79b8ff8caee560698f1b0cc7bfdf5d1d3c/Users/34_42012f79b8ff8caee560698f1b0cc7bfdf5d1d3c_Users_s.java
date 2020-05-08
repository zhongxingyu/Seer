 package controllers;
 
 import java.util.List;
 
 import models.Shipment;
 import models.User;
 import controllers.CRUD.ObjectType;
 import play.*;
 import play.mvc.*;
 
 // @With(Secure.class)
 public class Users extends CRUD {
 
     public static void list(int page, String search, String searchFields, String orderBy, String order) {
         ObjectType type = ObjectType.get(getControllerClass());
         notFoundIfNull(type);
         if (page < 1) {
             page = 1;
         }
 
         User user = User.find("byEmail", Security.connected()).first();

         List<User> objects = User.find("byCompany", user.company).fetch();
         if (user.isAdmin) {
             objects = User.findAll();
         }
 
        Long count = type.count(search, searchFields, (String) request.args.get("where"));
         Long totalCount = type.count(null, null, (String) request.args.get("where"));
         
         render(type, objects, count, totalCount, page, orderBy, order, user);
     }
     
 	public static void blank() {
         User connectedUser = User.find("byEmail", Security.connected()).first();
         render(connectedUser);
     }
 
 }
