 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 //Get scala templates
 import views.html.*;
 //Get objects
 import models.*;
 
 public class Application extends Controller {
 	/**
      * This result directly redirect to application home.
      */
     public static Result GO_HOME = redirect(
         routes.Application.listRun(0, "name", "asc", "", "name")
     );
   
   
     public static Result index() {
         return GO_HOME;
     }
   
 	/**
      * Display the paginated list of runs.
      *
      * @param page Current page number (starts from 0)
      * @param sortBy Column to be sorted
      * @param order Sort order (either asc or desc)
      * @param filter Filter applied on page names
      */
     public static Result listRun(int page, String sortBy, String order, String filter, String filterBy) {
         return ok(
             listRuns.render(
                 Run.page(page, 10, sortBy, order, filter, filterBy),
                 sortBy, order, filter, filterBy
             )
         );
     }
 	
 	/**
     * This result directly redirect to run index view.
     */
    public static Result runIndex(){
		return GO_HOME;
	}
	
	
	/**
      * Display the paginated list of pages.
      *
      * @param page Current page number (starts from 0)
      * @param sortBy Column to be sorted
      * @param order Sort order (either asc or desc)
      * @param filter Filter applied on page names
      */
     public static Result listPage(int page, String sortBy, String order, String filter) {
         return ok(
             listPages.render(
                 PageOut.page(page, 10, sortBy, order, filter),
                 sortBy, order, filter
             )
         );
     }
 	
 		/**
      * Display the paginated list of bugs.
      *
      * @param page Current page number (starts from 0)
      * @param sortBy Column to be sorted
      * @param order Sort order (either asc or desc)
      * @param filter Filter applied on page names
      */
     public static Result listBug(int page, String sortBy, String order, String filter) {
         return ok(
             listBugs.render(
                 Bug.page(page, 10, sortBy, order, filter),
                 sortBy, order, filter
             )
         );
     }
 	
 	/**
      * Display the paginated list of pages given a run.
      *
      * @param page Current page number (starts from 0)
      * @param sortBy Column to be sorted
      * @param order Sort order (either asc or desc)
      * @param runID Filter applied on page names
      */
     public static Result listPageByRun(int page, String sortBy, String order, Long runID, String filter) {
         return ok(
             listPagesByRun.render(
                 PageOut.pageFromRun(page, 10, sortBy, order, runID, filter),
                 sortBy, order, runID, filter
             )
         );
     }
 }
