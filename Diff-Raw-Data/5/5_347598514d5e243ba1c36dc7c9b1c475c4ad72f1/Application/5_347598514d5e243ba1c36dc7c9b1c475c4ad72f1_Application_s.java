 package controllers;
 
 import models.Topic;
 import org.joda.time.DateTime;
 import play.*;
 import play.data.format.Formatters;
 import static play.data.format.Formatters.*;
 
 import play.mvc.*;
 import play.data.Form;
 import static play.data.Form.*;
 import play.data.validation.Constraints;
 import play.mvc.*;
 
 import views.html.*;
 
 import java.util.Locale;
 
 public class Application extends Controller {
 
     final static Form<Topic> topicForm = form(Topic.class);
 
     public static Result index() {
         Topic result = Topic.findByTitle("Crisis");
         if (result == null) {
             result = Topic.find.byId(1l);
         }
         return ok(index.render(result.title, result.getFullJson().toString()));
     }
 
     public static Result show(String title) {
         Topic result = Topic.findByTitle(title);
         if (result == null) {
             return index();
         }
         return ok(index.render(result.title, result.getFullJson().toString()));
     }
 
     public static Result getTopicJson() {
         Form<Topic> t = topicForm.bindFromRequest();
         Topic topic = Topic.findByTitle(t.get().title);
         if (topic == null) {
             return ok("{}");
         }
         return ok(topic.getFullJson());
     }
 
     public static Result adminTools() {
 
         String sessionData = session().get("administrator");
         boolean isAdmin = sessionData != null && sessionData.equals("yes");
 
         if (!isAdmin) {
             return ok(enter_password.render());
         } else {
             return ok(edit_topics.render());
         }
     }
 
     public static class Password {
         @Constraints.Required
         public String password;
     }
 
     final static Form<Password> passwordForm = form(Password.class);
 
     public static Result verifyPassword() {
        Form<Password> form = passwordForm.bindFromRequest();
 
        if(!form.hasErrors() && form.get().password.equals(Play.application().configuration().getString("password"))) {
            session("administrator", "yes");
           return ok("... proceed");
        }
 
       return ok("No");
     }
 
 
     public static Result postTopic() {
         Form<Topic> form = topicForm.bindFromRequest();
 
         String supers = form.field("supers").valueOr("");
         String subs  = form.field("subs").valueOr("");
         String title = Topic.handleForm(form, supers, subs);
         if (title == null)
             return redirect("/admin_tools");
         else
             return redirect("/topic/" + title);
     }
   
 }
