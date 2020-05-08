 import java.util.concurrent.TimeUnit;
 
 import models.ObjectToUpdate;
 import play.Application;
 import play.GlobalSettings;
 import play.Logger;
 import play.libs.Akka;
 import akka.util.Duration;
 import controllers.Events;
 
 public class Global extends GlobalSettings {
 
 	@Override
 	public void onStart(Application app) {
		Akka.system().scheduler().scheduleOnce(Duration.create(0, TimeUnit.MILLISECONDS), saveEventRunnable);
 		//Akka.system().scheduler().scheduleOnce(Duration.create(1000, TimeUnit.MILLISECONDS), updateObjectsRunnable);
 
 	}
 
 	Runnable saveEventRunnable = new Runnable() {
 		public synchronized void run() {
 			try {
 				Events.saveEventList();
 			} catch (Exception e) {
 				Logger.error("Error during event Save !!! ", e);
 			}
 			Akka.system().scheduler().scheduleOnce(Duration.create(15, TimeUnit.SECONDS), saveEventRunnable);
 		}
 	};
 
 	Runnable updateObjectsRunnable = new Runnable() {
 		public synchronized void run() {
 			try {
 				ObjectToUpdate.updateObject();
 			} catch (Exception e) {
 				Logger.error("Error during object Update !!! ", e);
 			}
 			Akka.system().scheduler().scheduleOnce(Duration.create(15, TimeUnit.SECONDS), updateObjectsRunnable);
 		}
 	};
 
 }
