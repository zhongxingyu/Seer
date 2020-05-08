 package clock.sched;
 
 import java.io.UnsupportedEncodingException;
 
 import javax.xml.datatype.Duration;
 
 import android.content.Context;
 import android.util.Log;
 import clock.db.DbAdapter;
 import clock.db.Event;
 import clock.db.Event.eComparison;
 import clock.exceptions.CantGetLocationException;
 import clock.exceptions.EventsCollideException;
 import clock.exceptions.GoogleWeatherException;
 import clock.exceptions.IllegalAddressException;
 import clock.exceptions.InternetDisconnectedException;
 import clock.exceptions.OutOfTimeException;
 import clock.outsources.GoogleTrafficHandler.TrafficData;
 import clock.outsources.GoogleWeatherHandler;
 import clock.outsources.dependencies.WeatherModel;
 
 /**
  * Manager All alarms service in DataBase 
  * And the Android alarm services.
  * Manage all the internal alarm scheduling logic.
  *
  */
 public class AlarmsManager 
 {
 	/**
 	 * Handles arrangment times updating and recieving.
 	 * @author Idan
 	 *
 	 */
 	private class ArrangeTimeManager 
 	{				
 		private Context context;
 		private DbAdapter db;
 		private String[] dryWeather = new String[]{"Clear",	"Sunny", "Partly Sunny", "Mostly Sunny", "Partly Cloudy",
 			"Mostly Cloudy", "Cloudy", 	"Mist", "Overcast", "Dust", "Fog", "Smoke", "Haze"};
 		public ArrangeTimeManager(Context context, DbAdapter db) 
 		{
 			super();
 			this.context = context;
 			this.db = db;
 		}
 
 		/**
 		 * Should be called after user has left to the event.
 		 */
 		public void UserGotOut(Event event, int arrangmentTime, WeatherModel weatherData)
 		{
 			eCondition enumCondition = conditionToEnum(weatherData.getCondition());
 			db.open();
 			db.addRecord(event, arrangmentTime, enumCondition, Integer.parseInt(weatherData.getTemperature()));
 			db.close();
 		}
 		
 		private eCondition conditionToEnum(String condition) 
 		{
 			eCondition enumCondition = eCondition.WET;
 			for (String constCondition : dryWeather) {
 				if(condition.equals(constCondition))
 				{
 					enumCondition = eCondition.DRY;
 					continue;
 				}
 			}
 			return enumCondition;
 		}
 
 		/**
 		 * 
 		 * @return The arrangment time in mintues.
 		 */
 		public int GetArrangmentTime(Event event, WeatherModel weatherData)
 		{
 			eCondition contidion = conditionToEnum(weatherData.getCondition());
 			int tempeture = Integer.parseInt(weatherData.getTemperature());
 			db.open();
 			int arrTime = db.getArrangmentTime(event.getDayName(), contidion, tempeture);
 			db.close();
 			return arrTime;
 		}
 	}
 	
 	/**
 	 * Weather conditions enum
 	 * @author Idan
 	 *
 	 */
 	public enum eCondition{
 		WET,
 		DRY
 	}
 	
 	private DbAdapter dbAdapter;
 	private Context context;
 	private Event latestEvent;
 	private ArrangeTimeManager arrangeTimeManager;
 	
 	public AlarmsManager(Context context, DbAdapter dbAdapter) 
 	{
 		super();
 		this.dbAdapter = dbAdapter;
 		this.context = context;
 		this.arrangeTimeManager = new ArrangeTimeManager(context, dbAdapter);
 	}
 	
 	/**
 	 * Informs the alarm manager about a new event.
 	 * saves the alarm in db, manage the alarm set/cancel in case needed.
 	 * @param newEvent 
 	 * @param addToDB - if true the event will also be added to the DB,
 	 * 					else only alarm logic will imply (called after updated event saved)
 	 * @throws Exception 
 	 **/
 	private void newEvent(Event newEvent, boolean isItemSelectedFromList, boolean addToDB) throws Exception
 	{		
 		if (!GoogleAdapter.isInternetConnected(context)) {
 			throw new InternetDisconnectedException();
 		}
 		
 		if (!isItemSelectedFromList && GoogleAdapter.getSuggestions(newEvent.getLocation()).isEmpty()) {
 			throw new IllegalAddressException();
 		}
 		
 		TrafficData trafficData = GoogleAdapter.getTrafficData(context, newEvent, null);
 		long durationTime = trafficData.getDuration();
 		if (durationTime < 0) {
 			throw new CantGetLocationException();
 		}
 		
 		int arrageTime = getArrangmentTime(newEvent);
 		Log.d("ALARM", "Arrange time in mili is: " + arrageTime);
 		int timeToGoOut = newEvent.timeFromNow(durationTime + arrageTime);
 		if(newEvent.isAfterNow() && timeToGoOut < 0) { // its not possible to get there ! 
 			throw new OutOfTimeException();
 		}  
 //		checkIfEventsColide(newEvent, timeToGoOut);
 		dbAdapter.open();
 		latestEvent = dbAdapter.getNextEvent();
 		if(addToDB){
 			dbAdapter.insertEvent(newEvent);	
 		}
 		
 		dbAdapter.close();
 		if(newEvent.isAfterNow() &&
 						(latestEvent == null || Event.compareBetweenEvents(newEvent, latestEvent) == eComparison.BEFORE))
 		{
 			if (latestEvent != null)
 			{
 				ClockHandler.cancelEventAlarm(context, latestEvent);
 				LocationHandler.cancelLocationListener(context, latestEvent);
 			}
 			
 			this.latestEvent = newEvent;
 			ClockHandler.setAlarm(context, latestEvent, ((int)durationTime + arrageTime));
 			LocationHandler.setLocationListener(context, latestEvent, trafficData.getDistance());
 		}
 	}
 	
 	/**
 	 * Check if there's an event which collides with the new event.
 	 * @param newEvent
 	 * @param timeToGoOut 
 	 * @throws EventsCollideException - if there's an event colidion throws an exception
 	 */
 	private void checkIfEventsColide(Event newEvent, int timeToGoOut) throws EventsCollideException 
 	{
 		Event oneBefore = dbAdapter.getOneBefore(Event.getSqlTimeRepresent(newEvent));
 		//TODO: we should get the travel duration from oneBefore to newEvet,
 		// Then we should do: if (durationTime - (newEvent Time  - oneBefore Time)) 
 //		if(oneBefore != null)
 //		{
 //			int timeToGoOneBefore = oneBefore.timeFromNow(0);
 //			if (timeToGoOut < timeToGoOneBefore)
 //			{
 //				throw new EventsCollideException(oneBefore);
 //			}
 //			
 //		}
 		
 	}
 
 	/**
 	 * 
 	 * @param newEvent
 	 * @return arrangemt time in milli: -1 if no suggestion was found.
 	 * @throws UnsupportedEncodingException
 	 * @throws GoogleWeatherException 
 	 */
 	public int getArrangmentTime(Event newEvent) throws GoogleWeatherException 
 	{
 		int arrangeTime = 0;
 		int daysFromNow = newEvent.daysFromNow();
 		if(daysFromNow < 3 && newEvent.getWithAlarmStatus() == true) // only if event time < 3 days from now we have weather data.
 		{
 			GoogleWeatherHandler gw = new GoogleWeatherHandler();
 			try {
 				//$$ Problem here.
 				//WeatherModel weather = gw.processWeatherRequest(newEvent.getLocation());
 				WeatherModel weather = gw.getWeatherModel();
 				weather.setCondition("Clear");
 				weather.setTemperature("28");
 				arrangeTime = arrangeTimeManager.GetArrangmentTime(newEvent, weather);
 			} catch (Exception e) {
 				throw new GoogleWeatherException();
 			}
 					
 		}
 		
 		return arrangeTime;
 	}
 
 	public void UserGotOut(Event event, int arrangmentTime, WeatherModel weatherData)
 	{
 		this.arrangeTimeManager.UserGotOut(event, arrangmentTime, weatherData);
 	}
 	
 	/**
 	 * Request the alarm manager to delete an event.
 	 * Means the event will be deleted from the database,
 	 * If alarm was schedule for the event it will be cancelled,
 	 * And a new alarm for the next event will be schedule.
 	 * @param event - the event for deletion
 	 * @param alsoFromDB - if true the event will be deleted from the DB,
 	 * 						if false, only alarm logic imply. 
 	 * @throws Exception
 	 */
 	private void deleteEvent(Event event, boolean alsoFromDB) throws Exception
 	{
 		dbAdapter.open();
 		latestEvent = dbAdapter.getNextEvent();	
 		if (latestEvent != null && latestEvent.equals(event))
 		{
 			// Pull latest event from DB after the current one has been deleted
 			latestEvent = dbAdapter.getOneAfter(Event.getSqlTimeRepresent(event));
 			if (latestEvent != null)
 			{
 				try
 				{
 					TrafficData trafficData = GoogleAdapter.getTrafficData(context, latestEvent, null);
 					int timeToArrange = getArrangmentTime(latestEvent);
 					long duration = trafficData.getDuration();
 					if(duration < -1) {
 						Log.e("Alarm manager", "Can't get location");
 						throw new CantGetLocationException();
 					}
 					
 					ClockHandler.setAlarm(context, latestEvent, (int)(timeToArrange + trafficData.getDuration()));
 					LocationHandler.setLocationListener(context, latestEvent, trafficData.getDistance());
 				}
 				catch (Exception ex) {
 					Log.e("Alarm manager", "Delete event has failed");
 					dbAdapter.close();
 					throw ex;
 				}
 			}
 			
 			ClockHandler.cancelEventAlarm(context, event);
 			LocationHandler.cancelLocationListener(context, event);
			if(alsoFromDB){
				dbAdapter.deleteEvent(event);
			}
 		}
 		dbAdapter.close();
 	}
 	
 	public void newEvent(Event newEvent, boolean isItemSelectedFromList) throws Exception
 	{
 		this.newEvent(newEvent, isItemSelectedFromList, true);
 	}
 	
 	public void deleteEvent(Event event) throws Exception
 	{
 		this.deleteEvent(event, true);
 	}
 	
 	public void updateStart(Event event) throws Exception
 	{
 		Log.d("ALARM", "on UpdateStart");
 		this.deleteEvent(event, false);
 	}
 	
 	public void updateFinish(Event event, boolean isItemSelectFromList) throws Exception 
 	{
 		Log.d("ALARM", "on UpdateFinish");
 		dbAdapter.updateEvent(event);
 		this.newEvent(event, isItemSelectFromList, false);
 	}
 	
 }
