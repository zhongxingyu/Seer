 package controllers;
 
 import models.Contact;
 import models.Producer;
import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
import views.html.application.index;
 
 public class Application extends Controller {
 
 	public static Result index() {
		return ok(index.render(new Producer(123, new Contact(null, null, null, null))));
 	}
 	
 	public static Result settings() {
 		return ok(views.html.application.settings.render("settings"));
 	}
 
 	public static Result login() {
 		return TODO;
 	}
 	
 	public static Result authenticate() {
 		return TODO;
 	}
 
 	public static Result logout() {
 		return TODO;
 	}
 	
 	public static Result saveContacts() {
 		return TODO;
 	}
 	
 	public static Result search(String term) {
 		return TODO;
 	}
 	
 	public static Result help() {
 		return TODO;
 	}
 
 }
