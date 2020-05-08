 package controllers;
 
 import com.google.common.base.Splitter;
 import models.CollectionTools;
 import models.domain.Talk;
 import org.apache.commons.lang.StringUtils;
 import play.Play;
 import play.mvc.Controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static models.CollectionTools.collect;
 
 public class Application extends Controller {
 
     public static void index() {
         List<Talk> talks = Talk.filter("year =", 2011).order("-plays").asList();
        Iterable<String> allTags = collect(talks, Talk.findTags());
        List<String> tags = CollectionTools.findMostPopularElements(allTags, 100);

         List<String> filteredTags = new ArrayList<String>();
 
         for(String tag : tags) {
             if(StringUtils.trim(tag).indexOf(" ") == -1){
                 filteredTags.add(tag);
             }
         }
 
         Iterable<String> years = Splitter.on(",").split(Play.configuration.getProperty("years"));

         render(talks, filteredTags, years);
     }
 
 }
