 package controllers;
 
 import play.*;
 import play.mvc.*;
 import play.data.*;
 import play.i18n.*;
 
 import models.*;
 import views.html.*;
 
 
 
 /**
  * Return HTTP Server index.
  */
 @Security.Authenticated(Secured.class)
 public class Index extends Controller {
 	
 	/**
 	 * Returns either applicantView or recruiterView, based on role of the logged in User.
 	 * 
 	 * @return - Rendered index page as response.
 	 */
 	public static Result index() {
 		String username = Http.Context.current().request().username();
 		User user = User.findByUsername(username);
 		switch(user.role) {
 			case Applicant:
 				return ok(applicantView.render(user, form(UserCompetenceController.CompetenceProfileForm.class), form(UserAvailabilityController.AvailabilityForm.class)));
 			case Recruiter:
 				return ok(recruiterView.render(user));
 			default:
 				return internalServerError(Messages.get("title.error"));
 		}
 	}
 
	public static Result pageNotFound() {
		return badRequest(error.render(Messages.get("error.notfound")));
 	}
 }
 
