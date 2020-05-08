 package clock.sched;
 
 import android.content.Context;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.util.Log;
 
 import clock.db.DbAdapter;
 import clock.db.Event;
 import clock.db.Event.eComparison;
 import clock.exceptions.CantGetLocationException;
 import clock.exceptions.IllegalAddressException;
 import clock.exceptions.InternetDisconnectedException;
 import clock.outsources.GoogleTrafficHandler;
 import clock.outsources.GoogleWeatherHandler;
 import clock.outsources.GoogleTrafficHandler.TrafficData;
 
 /**
  * Manager All alarms service in DataBase 
  * And the Android alarm services.
  * Manage all the internal alarm scheduling logic.
  *
  */
 public class AlarmsManager 
 {
 	private DbAdapter dbAdapter;
 	private Context context;
 	private Event latestEvent;
 	
 	public AlarmsManager(Context context, DbAdapter dbAdapter) 
 	{
 		super();
 		this.dbAdapter = dbAdapter;
 		this.context = context;
 	}
 	
 	/**
 	 * Informs the alarm manager about a new event.
 	 * saves the alarm in db, manage the alarm set/cancel in case needed.
 	 * @param newEvent 
 	 * @throws Exception 
 	 **/
 	public void newEvent(Event newEvent) throws Exception
 	{		
 		if (!GoogleAdapter.isInternetConnected(context))
 			throw new InternetDisconnectedException();
 		
 		if (!GoogleAdapter.isLegalAddress(newEvent.getLocation()))
 			throw new IllegalAddressException();
 
 		try
 		{
 			TrafficData trafficData = GoogleAdapter.getTrafficData(context, newEvent, null);			
 			dbAdapter.open();
 			refreshLastEvent();
 			dbAdapter.insertEvent(newEvent);
 			if(newEvent.isAfterNow() &&
 							(latestEvent == null || Event.compareBetweenEvents(newEvent, latestEvent) == eComparison.BEFORE))
 			{
 				int arrageTime = newEvent.getWithAlarmStatus() == true ? dbAdapter.getArrangeTime() : 0;
 				if (latestEvent != null)
 				{
 					ClockHandler.cancelEventAlarm(context, latestEvent);
 					LocationHandler.cancelLocationListener(context, latestEvent);
 				}
 				
 				this.latestEvent = newEvent;
 				long durationTime = trafficData.getDuration();
 				if (durationTime < 0)
 				{
 					throw new CantGetLocationException();
 				}
 				ClockHandler.setAlarm(context, latestEvent, ((int)durationTime + arrageTime));
 				LocationHandler.setLocationListener(context, latestEvent, trafficData.getDistance());
 			}
 		}
 		catch (Exception ex)
 		{
 			Log.e("Alarm manager", "Create new event has failed");
 			throw ex;	//Roll exception forward
 		}
 		finally
 		{
 			dbAdapter.close();
 		}
 	}
 
 	/**
 	 * Request the alarm manager to delete an event.
 	 * Means the event will be deleted from the database,
 	 * If alarm was schedule for the event it will be cancelled,
 	 * And a new alarm for the next event will be schedule.
 	 * @param event - the event for deletion
 	 * @throws Exception
 	 */
 	public void deleteEvent(Event event) throws Exception
 	{
 		//TODO: how many refresh and latest and new and what the hell?!?
 		dbAdapter.open();
 		refreshLastEvent();
 		dbAdapter.deleteEvent(event);
		dbAdapter.close();
 		if (latestEvent != null && latestEvent.equals(event))
 		{
			refreshLastEvent();
 			if (latestEvent != null)
 			{
 				try
 				{
 					TrafficData trafficData = GoogleAdapter.getTrafficData(context, latestEvent, null);
 					int timeToArrange = dbAdapter.getArrangeTime();
 					ClockHandler.setAlarm(context, latestEvent, (int)(timeToArrange + trafficData.getDuration()));
 					LocationHandler.setLocationListener(context, latestEvent, trafficData.getDistance());
 				}
 				catch (Exception ex)
 				{
 					Log.e("Alarm manager", "Delete event has failed");
 				}
 			}
 			
 			ClockHandler.cancelEventAlarm(context, event);
 			LocationHandler.cancelLocationListener(context, event);
 		}
 	}
 	
 	/**
 	 * Updating data member (if needed) 'lastestEvent' from the database.
 	 * Should be called with open db connection.
 	 */
 	private void refreshLastEvent()
 	{
 		if (latestEvent == null)
 		{
 			latestEvent = dbAdapter.getNextEvent();		
 		}
 	}
 
 }
