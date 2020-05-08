 import org.vertx.java.core.json.JsonObject;
 import org.vertx.java.deploy.Verticle;
 
 public class ApplicationStarter extends Verticle {
     public void start() {
         // Get application config
         JsonObject appConfig = container.getConfig();
         // Get config of multiple verticles
         JsonObject verticle1Config = appConfig.getObject("verticle1_conf");
         JsonObject verticle2Config = appConfig.getObject("verticle2_conf");
         // Deploy the verticles
         container.deployVerticle("verticle.java");
         container.deployVerticle("verticle1.js", verticle1Config);
         container.deployWorkerVerticle("verticle2.rb", verticle2Config, 10);
     }
 }
