 package controllers;
 
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.about;
 import views.html.map;
 
 public class Application extends Controller {
 
 	public static Result index() {
 		return redirect(routes.Application.map());
 	}
 
 	public static Result map() {
 		return ok(map.render(null));
 	}
 
 	public static Result focusedMap(String userId) {
 		return ok(map.render(userId));
 	}
 
 	public static Result about() {
		return ok(about.render(false));
	}

	public static Result mobileAbout() {
		return ok(about.render(true));
 	}
 }
