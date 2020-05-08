 package model;
 
 import org.vertx.java.core.json.JsonArray;
 import org.vertx.java.core.json.JsonObject;
 
 import java.util.Date;
 
 /**
  * Create an article from definition
  */
 public class Article
 {
     public final static String name = "article";
 
     public static JsonObject create(String title, String content, String tags)
     {
         JsonObject doc = new JsonObject();
         doc.putString("title", title);
         doc.putString("content", content);
         doc.putString("startTime", new Date().toString());
         doc.putString("lastUpdate", null);
         doc.putArray("tags", toTag(tags));
         return doc;
     }
 
     public static JsonObject create(JsonObject article)
     {
         String tags = article.getString("tags");
         article.removeField("tags");
         article.putArray("tags", toTag(tags));
         return article;
     }
 
     private static JsonObject findByTags(String[] tags)
     {
         //TODO Find all articles who matchs with the current tags
         JsonArray criteria = new JsonArray();
         for(String tag : tags)
         {
             criteria.add(tag);
         }
 
         //TODO Sort by date
 
         //TODO Enable pagination
         return new JsonObject();
     }
 
     private static JsonArray toTag(String tags)
     {
         JsonArray json = new JsonArray();
         for (String s : tags.split(","))
         {
             json.add(s.trim());
         }
         return json;  //To change body of created methods use File | Settings | File Templates.
     }
 }
