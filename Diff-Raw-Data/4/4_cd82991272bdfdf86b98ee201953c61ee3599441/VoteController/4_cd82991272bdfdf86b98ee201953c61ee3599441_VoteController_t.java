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
 public class VoteController extends Controller {
 
 	public static class VoteCollector {
     
     	public Integer criteriaId;
     	public Long projectId;
     
     }	
 
 	public static Result vote(){
   		return ok(vote.render(Project.findAllProject()
   				      , Criteria.all()
   				      , criteriaForm
  				      , projectForm
   				      , User.findByUsername(request().username()))
   		);
 	}
 
 	public static Result saveProject(Long id){
 		return ok(vote.render(Project.findAllProject()
 				      , Criteria.all()
 				      , criteriaForm
 				      , projectForm
 				      , User.findByUsername(request().username()))
 		);
 	}
 
 	public static Result voteForProject() {
 		return TODO;	
 	}
   
 }
