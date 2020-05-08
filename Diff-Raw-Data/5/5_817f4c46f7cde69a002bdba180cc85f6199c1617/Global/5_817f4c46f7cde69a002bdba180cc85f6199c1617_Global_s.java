 import java.util.Date;
 import java.util.concurrent.TimeUnit;
 
 import play.Application;
 import play.GlobalSettings;
 import play.Logger;
 import play.libs.Akka;
 import akka.util.Duration;
 import controllers.Events;
 
 public class Global extends GlobalSettings {
 
 	@Override
 	public void onStart(Application app) {
 		// Akka.system().scheduler().schedule(
 		// Duration.create(0, TimeUnit.MILLISECONDS),
 		// Duration.create(15, TimeUnit.SECONDS),
 		// new Runnable() {
 		// public void run() {
 		// Events.saveEventList();
 		// }
 		// }
 		// );
 
 		Akka.system().scheduler().scheduleOnce(Duration.create(0, TimeUnit.MILLISECONDS), saveEventRunnable);
 
 	}
 
 	Runnable saveEventRunnable = new Runnable() {
 		public synchronized void run() {
 			//Logger.debug("1 "+new Date());
 			Events.saveEventList();
 			// Logger.debug("2 "+new Date());
 			Akka.system().scheduler().scheduleOnce(Duration.create(15, TimeUnit.SECONDS), saveEventRunnable);
 			// Logger.debug("3 "+new Date());
 		}
 	};
 
 }
