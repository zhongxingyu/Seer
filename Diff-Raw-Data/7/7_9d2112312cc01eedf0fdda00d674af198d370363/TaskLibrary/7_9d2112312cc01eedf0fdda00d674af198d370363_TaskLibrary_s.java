 package com.kangaroo.task;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 
 import android.content.Context;
 
 import com.kangaroo.calendar.CalendarEvent;
 import com.kangaroo.calendar.CalendarLibrary;
 import com.mobiletsm.routing.Place;
 
 public class TaskLibrary 
 {
 	private CalendarLibrary cl;
 	private Context ctx;
 	private String calendarName;
 	
 	//Constructor
 	public TaskLibrary(Context myCtx, String newCalendarName)
 	{
 		calendarName = newCalendarName;
 		ctx = myCtx;
 		cl = new CalendarLibrary(ctx);
 	}
 	
 	//get all Tasks from Calendar
 	public ArrayList<Task> getTasks()
 	{
 		int calendarId = cl.getCalendar(calendarName).getId();
 		
		ArrayList<CalendarEvent> myEventList = cl.getEventsByDate(String.valueOf(calendarId), new Date(0,0,1));
 		
 		CalendarEvent myEvents[] = myEventList.toArray(new CalendarEvent[0]);
 		ArrayList<Task> myTasks = new ArrayList<Task>();
 		for(int i=0; i<myEvents.length; i++)
 		{
 			myTasks.add(Task.deserialize(myEvents[i].getDescription()));
 		}
 		
 		return myTasks;
 	}
 	
 	//put all Tasks in Calendar
 	public int putTasks(ArrayList<Task> tasks)
 	{
 		Iterator<Task> it = tasks.iterator();
 		while(it.hasNext())
 		{
 			addTask(it.next());
 		}
 		return 0;
 	}
 	
 	//add one Task
 	public int addTask(Task myTask)
 	{
 		CalendarEvent temp = getEventForTask(myTask);
 		if(temp.getId().equalsIgnoreCase(""))
 		{
 			//no id set
 			cl.insertEventToBackend(temp);
 		}
 		else
 		{
 			//id is set
 			cl.updateEventInBackend(temp);
 		}
 
 		return 0;
 	}
 	
 	//delete Task
 	public int deleteTask(Task myTask)
 	{
 		cl.deleteEventFromBackend(getEventForTask(myTask));
 		return 0;
 	}
 	
 	private CalendarEvent getEventForTask(Task myTask)
 	{
 
 		int calendarId = cl.getCalendar(calendarName).getId();
 
 			
 		CalendarEvent returnEvent = null;
 		String taskString = myTask.serialize();
		Date startDate = new Date(0,0,1,1,0);
		Date endDate = new Date(0,0,1,1,1);
 
 		returnEvent = new CalendarEvent(myTask.getId(), "task", "", 0.0, 0.0, startDate, endDate, false, false, taskString, calendarId, "GMT", (Place)null);	
 
 		return returnEvent;
 	}
 	
 }
