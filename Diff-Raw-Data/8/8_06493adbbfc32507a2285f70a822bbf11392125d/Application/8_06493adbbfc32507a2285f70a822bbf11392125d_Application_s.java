 package controllers;
 
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.heatmap;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 
 import play.mvc.Result;
 import views.html.index;
 
 @Controller
 public class Application extends FixItBaseController {
 
 	private static Logger _logger = LoggerFactory.getLogger(Application.class);
 
 	public Result index() {
         	return ok(index.render("Your mom's new application is ready."));
 	}
 
     public Result heatmap() {
         return ok(heatmap.render());
     }
 }
