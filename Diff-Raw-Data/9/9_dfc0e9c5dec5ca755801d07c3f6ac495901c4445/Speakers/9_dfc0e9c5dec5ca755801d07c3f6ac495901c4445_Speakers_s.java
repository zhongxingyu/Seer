 package controllers;
 
 import models.domain.Speaker;
 import play.data.validation.Required;
 import play.mvc.Controller;
 
 /**
  * User: Knut Haugen <knuthaug@gmail.com>
  * 2011-10-01
  */
 public class Speakers extends Controller {
 
     public static void show(@Required String slug) {
         Speaker speaker = Speaker.find("bySlug", slug).first();
 
         if(speaker == null) {
             notFound("The speaker was not found. Sorry");
         }
 
        render(speaker);
     }
 }
