 package play.modules.carbonate;
 
 import play.Logger;
 import play.Play;
 import play.PlayPlugin;
 
 /**
  * @author huljas
  */
 public class MigrationPlugin extends PlayPlugin {
 
     public void onApplicationStart() {
        String pattern = (String) Play.configuration.get("carbonate.path");
         if (pattern != null) {
             Logger.info("Running migrations from path " + pattern);
             MigrationUtils.runMigrations(pattern);
         } else {
             Logger.info("Missing configuration 'carbonate.path', database migrations will be ignored!");
         }
     }
 
 }
