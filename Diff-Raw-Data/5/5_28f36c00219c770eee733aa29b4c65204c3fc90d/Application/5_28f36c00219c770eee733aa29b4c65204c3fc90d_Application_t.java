 package controllers;
 
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.about;
 import views.html.about_en;
 import views.html.map;
 
 public class Application extends Controller {
 
 	public static Result index() {
 		return redirect(routes.Application.map());
 	}
 
 	public static Result map() {
 		return ok(views.html.map.render(null, false));
 	}
 
	public static Result focusedMap(final String userId) {
		return ok(views.html.map.render(userId, false));
 	}
 
 	public static Result fullscreenMap() {
 		return ok(views.html.map.render("_USER_ID_", true));
 	}
 
 	public static Result about(String lang) {
 		if (lang.equals("en")) {
 			return ok(about_en.render(false));
 		} else {
 			return ok(about.render(false));			
 		}
 	}
 
 	public static Result mobileAbout(String lang) {
 		if (lang.equals("en")) {
 			return ok(about_en.render(true));
 		} else {
 			return ok(about.render(true));			
 		}
 	}
 }
