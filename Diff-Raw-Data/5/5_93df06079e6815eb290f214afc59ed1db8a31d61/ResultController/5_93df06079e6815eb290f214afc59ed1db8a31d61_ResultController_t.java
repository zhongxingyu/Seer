 package controllers;
 
 import play.*;
 import play.mvc.*;
 import play.data.*;
 
 import views.html.*;
 import models.*;
 
 /**
  * Manage projects related operations.
  */
 @Security.Authenticated(Secured.class)
 public class ResultController extends Controller {
 
 	static RankingsCalculator rankings = new RankingsCalculator();
 	static CriteriaCalculator crankings = new CriteriaCalculator();
 			
 	public static Result results() {
		return ok(views.html.results.render(User.findByUsername(request().username())));	
 	}
 
 
 }
