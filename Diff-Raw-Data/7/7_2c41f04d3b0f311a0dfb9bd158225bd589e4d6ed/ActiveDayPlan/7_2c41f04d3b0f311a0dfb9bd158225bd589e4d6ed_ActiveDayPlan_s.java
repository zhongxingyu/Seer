 package com.kangaroo;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import com.kangaroo.calendar.CalendarAccessAdapter;
 import com.kangaroo.calendar.CalendarEvent;
 import com.kangaroo.task.Task;
 import com.mobiletsm.routing.NoRouteFoundException;
 import com.mobiletsm.routing.Place;
 import com.mobiletsm.routing.Vehicle;
 
 public class ActiveDayPlan extends DayPlan {
 	
 	
 	private CalendarAccessAdapter calendarAccessAdapter = null;
 
 	/*
 	private Context ctx;
 	
 	
 	private void loadTasks()
 	{
 		TaskManager tm = new TaskManager(ctx);
 		tasks = tm.getTasks();
 	}
 	
 	
 	private void saveTasks()
 	{
 		TaskManager tm = new TaskManager(ctx);
 		tm.putTasks((ArrayList<Task>)tasks);
 	}
 	
 	
 	private void loadEvents()
 	{
 		CalendarLibrary cl = new CalendarLibrary(ctx);
 		int calendarId = cl.getCalendar("kangaroo@lordofhosts.de").getId();
 		//if our calendar is not present, return null
 		
 		ArrayList<CalendarEvent> myMap = cl.getTodaysEvents(String.valueOf(calendarId));
 		events.addAll(myMap);
 
 	}
 	
 	
 	private void saveEvents()
 	{
 
 		CalendarLibrary cl = new CalendarLibrary(ctx);
 		int calendarId = cl.getCalendar("kangaroo@lordofhosts.de").getId();
 		
 		//get List with event currently in Calendar
 		ArrayList<CalendarEvent> calendarList = cl.getTodaysEvents(String.valueOf(calendarId));
 		Iterator<CalendarEvent> it = calendarList.iterator();
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
 	*/
 
 
 	public void setCalendarAccessAdapter(CalendarAccessAdapter adapter) {
 		this.calendarAccessAdapter = adapter;
 	}
 	
 	
 	@Override
 	public int checkComplianceWith(Date now, Place here,
 			CalendarEvent destinationEvent, Object vehicle) throws NoRouteFoundException {
 		
 		/* a compliance check should only operate on events */
 		prepareEventAccess(false);
 				
 		try {
 			int result = super.checkComplianceWith(now, here, destinationEvent, vehicle);		
 			/* the routing engine may have affected event places */
 			terminateEventAccess(false);
 			return result;
 		} catch (RuntimeException exception) {
 			/* make sure we step from this level even if a
 			 * runtime exception occurs */
 			terminateEventAccess(true);
 			throw exception;
 		} catch (NoRouteFoundException exception) {
 			/* make sure we step from this level even if a
 			 * runtime exception occurs */
 			terminateEventAccess(true);
 			throw exception;			
 		}
 	}
 
 
 
 	@Override
 	public DayPlanConsistency checkConsistency(Vehicle vehicle, Date now) {
 		/* a consistency check should only operate on events */
 		prepareEventAccess(false);
 		
 		try {
 			DayPlanConsistency result = super.checkConsistency(vehicle, now);			
 			/* the routing engine may have affected event places */
 			terminateEventAccess(false);
 			return result;
 		} catch (RuntimeException exception) {
 			/* make sure we step from this level even if a
 			 * runtime exception occurs */
 			terminateEventAccess(true);
 			throw exception;
 		}		
 	}
 
 
 
 	@Override
 	public List<CalendarEvent> getEvents() {
 		/* prepare to access events */
 		prepareEventAccess(false);
 		
 		try {
 			List<CalendarEvent> result = super.getEvents();			
 			/* this is only a read access, so don't write to the calendar */
 			terminateEventAccess(true);
 			return result;		
 		} catch (RuntimeException exception) {
 			/* make sure we step from this level even if a
 			 * runtime exception occurs */
 			terminateEventAccess(true);
 			throw exception;
 		}		
 	}
 
 
 
 	@Override
 	public CalendarEvent getNextEvent(Date now) {
 		/* prepare to access events */
 		prepareEventAccess(false);
 		
 		try {
 			CalendarEvent result = super.getNextEvent(now);	
 			/* this is only a read access, so don't write to the calendar */
 			terminateEventAccess(true);
 			return result;	
 		} catch (RuntimeException exception) {
 			/* make sure we step from this level even if a
 			 * runtime exception occurs */
 			terminateEventAccess(true);
 			throw exception;
 		}		
 	}
 
 
 
 	@Override
 	public Collection<Task> getTasks() {
 		/* prepare to access tasks */
 		prepareTaskAccess(false);
 		
 		try {
 			Collection<Task> result = super.getTasks();			
 			/* this is only a read access, so don't write to the calendar */
 			terminateTaskAccess(true);
 			return result;
 		} catch (RuntimeException exception) {
 			/* make sure we step from this level even if a
 			 * runtime exception occurs */
 			terminateTaskAccess(true);
 			throw exception;
 		}		
 	}
 	
 	
 	@Override
 	public int getNumberOfEvents() {
 		/* prepare to access events */
 		prepareEventAccess(false);
 		
 		try {
 			int result = super.getNumberOfEvents();
 			/* this is only a read access, so don't write to the calendar */
 			terminateTaskAccess(true);
 			return result;			
 		} catch (RuntimeException exception) {
 			/* make sure we step from this level even if a
 			 * runtime exception occurs */
 			terminateEventAccess(true);
 			throw exception;
 		}		
 	}
 	
 	
 	@Override
 	public int getNumberOfTasks() {
 		/* prepare to access tasks */
 		prepareTaskAccess(false);
 		
 		try {
 			int result = super.getNumberOfTasks();
 			/* this is only a read access, so don't write to the calendar */
 			terminateTaskAccess(true);
 			return result;			
 		} catch (RuntimeException exception) {
 			/* make sure we step from this level even if a
 			 * runtime exception occurs */
 			terminateTaskAccess(true);
 			throw exception;
 		}		
 	}
 
 
 	@Override
 	public void addEvent(CalendarEvent event) {
 		/* we don't have to read the calendar */
 		prepareEventAccess(true);
 		
 		try {
 			super.addEvent(event);
 			terminateEventAccess(false);
 		} catch (RuntimeException exception) {
 			/* make sure we step from this level even if a
 			 * runtime exception occurs */
 			terminateEventAccess(true);
 			throw exception;
 		}	
 	}
 	
 	
 	@Override
 	public void addTask(Task task) {
 		/* we don't have to read the calendar */
 		prepareTaskAccess(true);
 		
 		try {
 			super.addTask(task);
 			terminateTaskAccess(false);
 		} catch (RuntimeException exception) {
 			/* make sure we step from this level even if a
 			 * runtime exception occurs */
 			terminateTaskAccess(true);
 			throw exception;
 		}
 	}
 	
 	
 	
 	@Override
 	public DayPlan optimize(Date now, Place here, Object vehicle) {
 		/* prepare to access events and tasks */
 		prepareEventAccess(false);
 		prepareTaskAccess(false);
 		
 		try {
 			DayPlan result = super.optimize(now, here, vehicle);
 			/* the routing engine may have affected places in events and tasks */
 			terminateTaskAccess(false);
 			terminateEventAccess(false);
 			return result;
 		} catch (RuntimeException exeption) {
 			/* make sure we step from this level even if a
 			 * runtime exception occurs */
 			terminateTaskAccess(true);
 			terminateEventAccess(true);
 			throw exeption;
 		}		
 	}
 
 
 
 	@Override
 	public void setEvents(List<CalendarEvent> events) {
 		/* we don't have to read the calendar */
 		prepareEventAccess(true);
 		
 		try {
 			super.setEvents(events);
 			terminateEventAccess(false);
 		} catch (RuntimeException exception) {
 			/* make sure we step from this level even if a
 			 * runtime exception occurs */
 			terminateEventAccess(true);
 			throw exception;
 		}		
 	}
 
 
 
 	@Override
 	public void setTasks(Collection<Task> tasks) {
 		/* we don't have to read the calendar */
 		prepareTaskAccess(true);
 		
 		try {
 			super.setTasks(tasks);
 			terminateTaskAccess(false);
 		} catch (RuntimeException exception) {
 			/* make sure we step from this level even if a
 			 * runtime exception occurs */
 			terminateTaskAccess(true);
 			throw exception;
 		}		
 	}
 	
 	
 	
 	@Override
 	public String toString() {
 		/* prepare to access events */
 		prepareEventAccess(false);
 		prepareTaskAccess(false);
 		
 		try {
 			String result = super.toString();	
 			/* this is only a read access, so don't write to the calendar */
 			terminateTaskAccess(true);
 			terminateEventAccess(true);
 			return result;	
 		} catch (RuntimeException exception) {
 			/* make sure we step from this level even if a
 			 * runtime exception occurs */
 			terminateTaskAccess(true);
 			terminateEventAccess(true);
 			throw exception;
 		}
 	}	
 	
 	
 	private int eventAccessDepth = 0;
 	
 	
 	private int taskAccessDepth = 0;
 	
 	
 	private void prepareEventAccess(boolean skipLoad) {
 		if (eventAccessDepth++ == 0 && !skipLoad) {
 			//loadEvents();
 			events.clear();
 			events.addAll(calendarAccessAdapter.loadEvents());
 		}
 	}
 	
 	
 	private void terminateEventAccess(boolean skipSave) {
 		if (eventAccessDepth <= 0) {
 			throw new RuntimeException("ActiveDayPlan.terminateEventAccess(): " +
 					"cannot step back from level 0");
 		}
 		if (--eventAccessDepth == 0 && !skipSave) {
 			//saveEvents();
 			calendarAccessAdapter.saveEvents(events);
 		}
 	}
 	
 	
 	private void prepareTaskAccess(boolean skipLoad) {
 		if (taskAccessDepth++ == 0 && !skipLoad) {
 			//loadTasks();
 			tasks.clear();
 			tasks.addAll(calendarAccessAdapter.loadTasks());
 		}
 	}
 	
 	
 	private void terminateTaskAccess(boolean skipSave) {
 		if (taskAccessDepth <= 0) {
			throw new RuntimeException("ActiveDayPlan.terminateTaskAccess(): " +
			"cannot step back from level 0");
 		}
 		if (--taskAccessDepth == 0 && !skipSave) {
 			//saveTasks();
 			calendarAccessAdapter.saveTasks(tasks);
 		}
 	}
 	
 	
 	
 	
 }
