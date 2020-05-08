 package controllers;
 
 import play.mvc.Controller;
import play.mvc.With;
import controllers.securesocial.SecureSocialPublic;
 
@With(SecureSocialPublic.class)
 public class Application extends Controller {
 
 	public static void index() {
 		render();
 	}
 
 }
