 package controllers;
 
 import java.util.Calendar;
 import java.util.List;
 import models.Idea;
 import models.Topic;
 import models.User;
 import models.Valid;
 import play.mvc.*;
 
 
 
 public class Application extends Controller {
 
     public static void index() {
         List<Topic> topics = Topic.find("byValid", Valid.Y).fetchAll();
         render(topics);
     }
     
 }
