 package controllers;
 
 import java.util.List;
 
 import models.Contact;
 import play.mvc.Controller;
 
 public class Application extends Controller {
 
 	public static void index() {
 		render();
 	}
 
 	public static void index(List<Contact> contacts) {
 		render(contacts);
 	}
 
 	public static void inviteCallback() {
 
 		new Contact("jwermuth@gmail.com", "Jesper").save();
 		new Contact("whabula@gmail.com", "Whabula").save();
 		new Contact("wrinkle@gmail.com", "John Smith").save();
 
 		// return rv;
 		List<Contact> contacts = Contact.findAll();
 		render("@index", contacts);
 	}
 
 	public static void contactsByEmail(String term) {
 		List<Contact> contacts = Contact.find("byEmailLike", "%" + term + "%")
 				.fetch();
		renderJSON(contacts);
 	}
 
 }
