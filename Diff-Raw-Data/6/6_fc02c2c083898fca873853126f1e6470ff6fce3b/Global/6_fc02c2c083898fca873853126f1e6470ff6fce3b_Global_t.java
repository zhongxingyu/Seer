 import static play.mvc.Results.internalServerError;
 import static play.mvc.Results.notFound;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
import controllers.CommonHeaders;

 import models.EMessages;
 import models.data.Link;
 import play.GlobalSettings;
 import play.mvc.Http;
import play.mvc.With;
 import play.mvc.Http.Context;
 import play.mvc.Http.Request;
 import play.mvc.Http.RequestHeader;
 import play.mvc.Result;
 
 /**
  * @author Sander Demeester, Ruben Taelman
  */
@With(CommonHeaders.class)
 public class Global extends GlobalSettings{
 
     private static List<Link> breadcrumbs = new ArrayList<Link>();
     static {
         breadcrumbs.add(new Link("Home", "/"));
         breadcrumbs.add(new Link("Error",""));
     }
 
     public Result onError(Throwable t) {
         return internalServerError(
             views.html.commons.error.render(breadcrumbs, EMessages.get("error.title"), EMessages.get("error.text"))
         );
     }
 
 
     @Override
     public Result onHandlerNotFound(RequestHeader request){
         // NASTY HACK INCOMING!!
         // We have to make our own Context because of a bug that is fixed since Play 2.1 ...
         Context c = new Http.Context((Request)request, new HashMap<String, String>(), new HashMap<String, String>());
         Context.current.set(c);
 
         return notFound(
             views.html.commons.notfound.render(breadcrumbs
                     , request.path())
         );
     }
 }
 
