 package controllers;
 
 import play.*;
 import play.mvc.*;
 import play.data.*;
 import static play.data.Form.*;
 
 import org.codehaus.jackson.JsonNode;
 
 import views.html.action.*;
 import views.html.*;
 
 import models.*;
 
 import java.util.List;
 
 public class ActionController extends Controller {
 
   /**
   * Defines a form wrapping the ActionModel class.
   */ 
   private static Form<ActionModel> form = form(ActionModel.class);
   
   /* Displays all actions */
   public static Result all() {
     List<ActionModel> actions = ActionModel.find.all();
     return ok(list.render(actions));
   } 
 
   /* Renders a form for new actions */
   public static Result newForm() {
     return ok(newForm.render(form));
   }
 
   /* Accepts new actions from a form */
   public static Result newAction() {
     Boolean success = true;
     try {
       form.bindFromRequest().get().save();
     } catch (Exception ex) {
       Logger.error("Error in newAction: " + ex.toString());
       success = false;
     }
 
     if (success)
       return redirect(routes.ActionController.all());
 
     return badRequest("Unable to save new action");
   }
 }
