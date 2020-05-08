 package max.utility.tomato;
 
 import static java.util.concurrent.TimeUnit.*;
 
 import java.util.Date;
 import java.util.concurrent.*;
 
 import javax.swing.JFrame;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import max.utility.tomato.gui.EndTomato;
 
 public class Countdown {
 
 	public static final Logger logger = LoggerFactory.getLogger(Countdown.class);
 
 	ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public void start() {
 		try {
 			Callable actionToPerform = new Callable<JFrame>() {
 				public JFrame call() throws Exception {
 					EndTomato endTomato = new EndTomato();
 					logger.debug("open endTomato");
 					return endTomato.openWindow();
 				}
 			};
 
 			logger.debug("before timer");
 			ScheduledFuture scheduledFuture = scheduledExecutorService.schedule(actionToPerform, 3, TimeUnit.SECONDS);
 			logger.debug("after timer");
 			
 		} catch (Exception e) {
 			logger.error("#catch_block#", e);
 		}
 
 		scheduledExecutorService.shutdown();
 	}
 
 //	private Callable getActionToPerform() {
 //		Callable actionToPerform = new Callable() {
 //			public Object call() throws Exception {
 //				EndTomato endTomato = new EndTomato();
 //				endTomato.openWindow();
 //				logger.debug("after open end tomato");
 //				return "timer ended...";
 //			}
 //		};
 //		return actionToPerform;
 //	}
 }
