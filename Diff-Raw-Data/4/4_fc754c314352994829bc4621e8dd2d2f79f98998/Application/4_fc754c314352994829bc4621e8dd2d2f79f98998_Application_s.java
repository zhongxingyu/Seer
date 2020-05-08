 package controllers;
 
 import java.util.Date;
 
 import models.Tweet;
 import play.*;
 import play.libs.Json;
 import play.mvc.*;
 import views.html.*;
 import play.data.Form;
 
 public class Application extends Controller {
 
     public static Result index() {
         return ok(index.render("Your new application is ready."));
     }
     
     public static Result allTweet()
     {
     	if (request().accepts("text/html")) return ok(views.html.wall.render(Tweet.findAll()));
     	else if(request().accepts("application/json")) return ok(Json.toJson(Tweet.findAll()));
     	return badRequest();
     }
 
    public static void createTweet()
     {
     	Form<Tweet> form = Form.form(Tweet.class).bindFromRequest();
     	Tweet tweet = new Tweet();
     	tweet.setComment(form.field("comment").value());
     	tweet.setUsername(form.field("username").value());
     	tweet.setCreationDate(new Date());
     	Tweet.create(tweet);
     }
 }
