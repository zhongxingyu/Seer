 import model.Article;
 import driver.Mongo;
 import json.JsonOk;
 import org.vertx.java.core.Handler;
 import org.vertx.java.core.eventbus.EventBus;
 import org.vertx.java.core.eventbus.Message;
 import org.vertx.java.core.json.JsonObject;
 import org.vertx.java.deploy.Verticle;
 
 /**
  * Manage interaction with the editor
  */
 public class Editor extends Verticle
 {
     private EventBus eb;
 
     public static final String save = "server.save";
 
     @Override
     public void start() throws Exception
     {
         System.out.println("Start editor verticle.");
 
         // EventBus
         eb = vertx.eventBus();
         eb.registerHandler(save, save());
     }
 
     private Handler<? extends Message> save()
     {
         return new Handler<Message<JsonObject>>()
         {
             @Override
             public void handle(Message<JsonObject> message)
             {
                 System.out.println("Save message received : " + message.body.toString());
 
                 // Create message to save an article
                 JsonObject msg = Mongo.save(Article.name, (JsonObject)message.body);
 
                 // Run mongo
                 eb.send(Mongo.address, msg, new Handler<Message<JsonObject>>()
                 {
                     @Override
                     public void handle(Message<JsonObject> jsonObjectMessage)
                     {
                         // Refresh article
                        eb.send(Dashboard.changes, new JsonOk());
                     }
                 });
 
                 // Ack
                 message.reply(new JsonOk());
             }
         };
     }
 }
