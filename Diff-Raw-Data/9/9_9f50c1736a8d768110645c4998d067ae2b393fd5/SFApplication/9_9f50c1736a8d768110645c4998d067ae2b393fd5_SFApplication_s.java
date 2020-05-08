 package se.chalmers.dat255.sleepfighter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 import se.chalmers.dat255.sleepfighter.model.AlarmsManager;
 import se.chalmers.dat255.sleepfighter.utils.message.Message;
 import se.chalmers.dat255.sleepfighter.utils.message.MessageBus;
 import android.app.Application;
 
 /**
  * A custom implementation of Application for SleepFighter.
  */
 public class SFApplication extends Application {
 	private AlarmsManager alarmsManager;
 	private MessageBus<Message> bus;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 
 		this.bus = new MessageBus<Message>();
 		this.alarmsManager = new AlarmsManager();
 		this.alarmsManager.setMessageBus(this.bus);
 
 		// Hard code in some sample alarms
 		// TODO load from database/wherever they are stored
 		Alarm namedAlarm = new Alarm(13, 37);
 		namedAlarm.setName("Named alarm");
 		namedAlarm.setEnabledDays(new boolean[] { true, false, true, false,
 				true, false, true });

 		Alarm alarm2 = new Alarm(8, 30);
 		alarm2.setName("Untitled Alarm");
 		alarm2.setActivated(false);
 
 		Alarm alarm3 = new Alarm(7, 0);
 		alarm3.setName("Untitled Alarm");
 
 		Alarm alarm4 = new Alarm(7, 0);
 		alarm4.setName("Untitled Alarm");
 		alarm4.setEnabledDays(new boolean[7]);
 
 		List<Alarm> alarms = new ArrayList<Alarm>();
 		alarms.add(namedAlarm);
 		alarms.add(alarm2);
 		alarms.add(alarm3);
 		alarms.add(alarm4);
 
 		this.alarmsManager.addAll(alarms);
 	}
 
 	/**
 	 * Returns the AlarmsManager for the application.
 	 * 
 	 * @return the AlarmsManager for the application
 	 */
 	public AlarmsManager getAlarmsManager() {
 		return alarmsManager;
 	}
 
 	/**
 	 * Returns the default MessageBus for the application.
 	 * 
 	 * @return the default MessageBus for the application
 	 */
 	public MessageBus<Message> getBus() {
 		return bus;
 	}
 }
