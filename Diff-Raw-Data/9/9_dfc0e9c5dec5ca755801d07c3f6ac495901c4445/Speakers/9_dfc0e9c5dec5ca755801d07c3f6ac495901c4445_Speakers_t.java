 package controllers;
 
 import models.domain.Speaker;
import models.domain.Talk;
 import play.data.validation.Required;
 import play.mvc.Controller;
 
import java.util.List;

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
        List<Talk> talks = Talk.filter("speakers elem", speaker).asList();
 
        render(speaker, talks);
     }
 }
