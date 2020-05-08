 package controllers;
 
 import play.Configuration;
 import play.Logger;
 import play.Play;
import play.api.templates.Html;
 import play.mvc.Controller;
 import play.mvc.Result;
 import redis.clients.jedis.Jedis;
 
 public class Application extends Controller {
   
     public static Result index() {
         Configuration configuration = Play.application().configuration();
         String host = configuration.getString("redis.host");
         int port = configuration.getInt("redis.port");
         String pass = configuration.getString("redis.pass");
 
         Logger.info(host);
         Logger.info(Long.toString(port));
 
         Jedis jedis = new Jedis(host, port);
         if (pass != null) jedis.auth(pass);
 
         Long visits = jedis.incr("visits");
        Html html = views.html.index.render(visits);
        return ok(html);
     }
   
 }
