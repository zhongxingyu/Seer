 package controllers;
 
 import models.Tag;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.With;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: root
  * Date: 4/17/12
  * Time: 8:52 AM
  * To change this template use File | Settings | File Templates.
  */
 public class Tags extends Controller {
 
     /**
      * Check whether the project has the user as a member
      */
     @Before
     static void checkAccess() {
         Projects.checkMembership();
     }
 
     /**
      * return up to limit # of suggested tags that contains q
      *
     * @param project
      * @param q
      * @param limit
      */
    public static void suggest(Long project, String q, Integer limit) {
         if (limit == null) {
             limit = 8;
         } 
 
         String returnString = "";
         if (StringUtils.isNotEmpty(q)) {
            List<Tag> tags = Tag.find("project.id=? and text like '%" + StringEscapeUtils.escapeSql(q) + "%'", project).fetch(limit);
             for (Tag tag : tags) {
                 returnString += tag.text + "|" + tag.id + "\n";
             }
         }
         renderText(returnString);
     }
 }
