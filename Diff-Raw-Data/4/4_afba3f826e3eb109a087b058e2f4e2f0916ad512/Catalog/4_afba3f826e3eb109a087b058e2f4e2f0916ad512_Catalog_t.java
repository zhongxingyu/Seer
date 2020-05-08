 package controllers;
 
 import java.util.List;
 
 import models.Topic;
 import models.User;
 import play.mvc.Controller;
 
 public class Catalog extends Controller {
 
     public static void show() {
     	List<Topic> topics = Topic.all().fetch();
         renderTemplate("/Catalog/catalog.html", topics);
     }
     
 }
