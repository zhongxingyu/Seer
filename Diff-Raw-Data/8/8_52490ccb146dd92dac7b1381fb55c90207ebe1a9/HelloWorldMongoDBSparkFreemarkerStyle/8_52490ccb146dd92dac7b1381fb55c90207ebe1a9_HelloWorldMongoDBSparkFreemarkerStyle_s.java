 package org.einherjer;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.net.UnknownHostException;
 
 import spark.Request;
 import spark.Response;
 import spark.Route;
 import spark.Spark;
 
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 
 import freemarker.template.Configuration;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 public class HelloWorldMongoDBSparkFreemarkerStyle {
 
     //insert something in the DB first. It has to have a "name" key, which is expected by the freemarker template:
     //  use blog;
     //  db.users.save({name:"mongo user"});
     public static void main(String[] args) throws UnknownHostException {
         final Configuration config = new Configuration();
         config.setClassForTemplateLoading(HelloWorldSparkFreemarkerStyle.class, "/");
 
         Mongo mongo = new Mongo("localhost:27017");
         DB db = mongo.getDB("blog");
        final DBCollection postsCollection = db.getCollection("posts");
         
         Spark.get(new Route("/") {
             @Override
             public Object handle(final Request request, final Response response) {
                 StringWriter writer = new StringWriter();
                 try {
                     Template helloTemplate = config.getTemplate("hello.ftl");
                     DBObject document = postsCollection.findOne();
 
                     helloTemplate.process(document, writer); //BasicDBObject extends BasicBSONObject implements DBObject -> BasicBSONObject extends LinkedHashMap
                 }
                 catch (IOException e) {
                     e.printStackTrace();
                     this.halt(500);
                 }
                 catch (TemplateException e) {
                     e.printStackTrace();
                     this.halt(500);
                 }
                 return writer;
             }
         });
     }
 
 }
