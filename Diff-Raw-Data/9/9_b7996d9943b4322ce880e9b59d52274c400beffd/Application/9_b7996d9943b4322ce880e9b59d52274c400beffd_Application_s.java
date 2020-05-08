 package controllers;
 
 import java.util.Date;
 import java.util.List;
 
 import models.Matche;
 import models.Utilisateur;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Security.Authenticated;
 import views.html.index;
 
 @Authenticated(Secured.class)
 public class Application extends Controller {
 
   
 	  public static Result index() {
 		  Date maintenant = new Date();
 		  List<Matche> prochainMatchs = Matche.find.where().gt("dateMatche", maintenant).orderBy().asc("dateMatche").findList();
 		  
		  Matche prochainMatch = prochainMatchs.get(0);
	
 		  return ok(index.render(Utilisateur.findByPseudo(request().username()),Utilisateur.find.orderBy().desc("points").findList(),prochainMatch)
 				  );
 		  
 	  }
   
   
 }
