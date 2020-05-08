 package controllers;
 
 import java.io.IOException;
 import java.util.concurrent.TimeoutException;
 
 import play.*;
 import play.cache.Cache;
 import play.data.*;
 import play.mvc.*;
 import play.libs.Json;
 
 import views.html.*;
 
 import models.Screenshot;
 import models.Query;
 import models.memento.MementoSearchBean;
 import models.memento.MementoQuery;
 import models.memento.MementoTimeGraph;
 
 public class Application extends Controller {
 
   // Timeout for caches - one hour, in seconds.
   private static final int CACHE_TIMEOUT = 60*60;
 
  public static Result index() {
     return redirect(routes.Application.findMementos());
   }
   
   /**
    * Return Mementos as JSON:
    */
   public static Result findMementosApi(String url, String archive) {
     MementoQuery msb = doQuery(url,archive);
     //
     if (request().accepts("text/html")) {
           return ok("html "+url+"\n"+msb+"\n");
         } else if (request().accepts("application/json")) {
           return ok(Json.toJson(msb));
         } else {
           return badRequest();
        }
   }
 
   static Form<Query> queryForm = form(Query.class);
 
   public static Result queryMementos() {
     return ok(
       views.html.search.render(new MementoQuery(), queryForm)
     );
   }
 
   public static Result findMementos() {
     Form<Query> urlParam = queryForm.bindFromRequest();
     if(urlParam.hasErrors()) {
         return badRequest(
           views.html.search.render(new MementoQuery(), urlParam)
         );
     }
     Query q = urlParam.get();
     return redirect(routes.Application.findMementosFor(q.url,q.archive));
   }
 
   public static Result findMementosRedirect(String url) {
 	return redirect(routes.Application.findMementosFor(url,""));
   }
   
   public static Result findMementosFor(String url, String archive) {
     MementoQuery msb = doQuery(url, archive);
     // Check for warnings:
     if( msb.getErrorMessage() != null )
       flash("success", msb.getErrorMessage() );
     // Rebuild a Query object.
     Query q = new Query();
     q.url = url;
     q.archive = "";
     return ok(
       views.html.search.render(msb, queryForm.fill(q))
     );
   }
 
   /**
    * 
    */
   private static MementoQuery doQuery(String url, String archive) {
 	String queryId = "Mementos."+archive+"."+url;
     MementoQuery msb = (MementoQuery) Cache.get(queryId);
     if( msb == null ) {
       msb = new MementoQuery();
       msb.setUrlAndArchive(url,archive);
       Cache.set(queryId, msb, CACHE_TIMEOUT);
     }
     return msb;
   }
 
   /**
    * 
    * @param url
    * @param archive
    * @return
    */
   public static Result apiTimeGraph(String url, String archive) {
     MementoQuery msb = doQuery(url, archive);
     return ok(Json.toJson(MementoTimeGraph.makeYearwiseData(msb)));
   }
   
   /**
    * 
    * @param url
    * @return
    */
   public static Result apiScreenshot(String url) {
 	try {
 		Screenshot shot = (Screenshot) Cache.get("Screenshot."+url);
 		if( shot == null ) {
 			Logger.debug("Taking screenshot of "+url);
 			shot = Screenshot.getThumbnailPNG(url);
 			Logger.debug("Taken screenshot of "+url);
 			Cache.set("Screenshot."+url, shot, CACHE_TIMEOUT);
 		}
 		return ok(shot.screenshot).as("image/png");
 	} catch (Exception e) {
 		return badRequest("FAIL");
 	}
   }
 
 }
