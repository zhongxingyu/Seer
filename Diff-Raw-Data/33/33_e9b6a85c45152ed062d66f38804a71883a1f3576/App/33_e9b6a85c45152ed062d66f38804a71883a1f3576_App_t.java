 import conf.BootStrap;
 import driver.Mongo;
 import org.vertx.java.core.json.JsonArray;
 import org.vertx.java.core.json.JsonObject;
 import org.vertx.java.deploy.Verticle;
 
 /**
  * @author victor benarbia
  */
 public class App extends Verticle
 {
     @Override
     public void start() throws Exception
     {
         WebServerConfig config = new WebServerConfig();
         config.addInbound(Dashboard.findAll);
         config.addInbound(Editor.save);
         config.addOutbound(Dashboard.refresh);
        config.addOutbound(Dashboard.changes);
 
         // Database
         JsonObject mongoConf = new JsonObject();
         mongoConf.putString("host", "localhost");
         mongoConf.putNumber("port", 27017);
         mongoConf.putString("db_name", "unhook");
 
         container.deployModule("vertx.mongo-persistor-v1.1", mongoConf, 1, new BootStrap(vertx.eventBus()));
 
         container.deployModule("vertx.web-server-v1.0", config.getConf());
 
         // Start verticles
         container.deployVerticle("src/Editor.java");
         container.deployVerticle("src/Dashboard.java");
     }
 
     private class WebServerConfig
     {
        JsonObject conf;
 
        public WebServerConfig()
        {
            // Start http server
            JsonObject webServerConf = new JsonObject();
            webServerConf.putNumber("port", 8080);
            webServerConf.putString("host", "localhost");
            webServerConf.putBoolean("bridge", true);
 
            JsonArray inbound_permitted = new JsonArray();
 
            /* Set security mechanism in place */
            JsonObject mongo = new JsonObject();
            mongo.putString("address", Mongo.address);
            inbound_permitted.add(mongo);
 
            webServerConf.putArray("inbound_permitted", inbound_permitted);
 
            JsonArray outbound_permitted = new JsonArray();
            webServerConf.putArray("outbound_permitted", outbound_permitted);
 
            conf = webServerConf;
        }
 
         public void addInbound(String address)
         {
             JsonObject inbound = new JsonObject();
             inbound.putString("address", address);
             conf.getArray("inbound_permitted").add(inbound);
         }
 
         public void addOutbound(String address)
         {
             JsonObject outbound = new JsonObject();
             outbound.putString("address", address);
             conf.getArray("outbound_permitted").add(outbound);
         }
 
         public JsonObject getConf()
        {
            System.out.println(this.conf.toString());
            return this.conf;
        }
     }
 
 }
