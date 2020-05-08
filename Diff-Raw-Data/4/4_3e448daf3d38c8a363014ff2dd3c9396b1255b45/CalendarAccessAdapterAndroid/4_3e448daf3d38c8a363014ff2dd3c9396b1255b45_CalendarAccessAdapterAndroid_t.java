 package com.kangaroo.calendar;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 
 import com.kangaroo.task.Task;
 import com.kangaroo.task.TaskLibrary;
 
 public class CalendarAccessAdapterAndroid implements CalendarAccessAdapter 
 {
 
 	private Context context = null;
 	private String calendarName;
 	private String preferencesName = "kangaroo_config";
 	private SharedPreferences prefsPrivate = null;
 	
	public CalendarAccessAdapterAndroid(Context con)
 	{ 
		this.context = con;
 		prefsPrivate = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
 		calendarName = prefsPrivate.getString("calendar_in_use", "kangaroo@lordofhosts.de");
 	}
 	
 	@Override
 	public void setContext(Object context) 
 	{
 		this.context = (Context)context;
 	}
 	
 	@Override
 	public List<CalendarEvent> loadEvents() {
 		CalendarLibrary cl = new CalendarLibrary(context);
 		int calendarId = cl.getCalendar(calendarName).getId();
 		ArrayList<CalendarEvent> myMap = cl.getTodaysEvents(String.valueOf(calendarId));
 		return myMap;
 	}
 	
 
 	@Override
 	public Collection<Task> loadTasks() {
 		TaskLibrary tm = new TaskLibrary(context, calendarName);
 		return tm.getTasks();
 	}
 	
 
 	@Override
 	public void saveEvents(List<CalendarEvent> events) {
 		CalendarLibrary cl = new CalendarLibrary(context);
 		int calendarId = cl.getCalendar(calendarName).getId();
 		
 		//get List with event currently in Calendar (for this day)
 		ArrayList<CalendarEvent> calendarList = cl.getTodaysEvents(String.valueOf(calendarId));
 		Iterator<CalendarEvent> it = calendarList.iterator();
 		
 		//delete all the events, because we have no way of checking which ones have changed.
 		while(it.hasNext())
 		{
 			cl.deleteEventFromBackend(it.next());
 		}
 		
 		it = events.iterator();
 		while(it.hasNext())
 		{
 			cl.insertEventToBackend(it.next());
 		}
 	}
 	
 
 	@Override
 	public void saveTasks(Collection<Task> tasks) {
 		TaskLibrary tm = new TaskLibrary(context, calendarName);
 		ArrayList<Task> tasksToPut = new ArrayList<Task>();
 		tasksToPut.addAll(tasks);
 		tm.putTasks(tasksToPut);
 	}
 }
