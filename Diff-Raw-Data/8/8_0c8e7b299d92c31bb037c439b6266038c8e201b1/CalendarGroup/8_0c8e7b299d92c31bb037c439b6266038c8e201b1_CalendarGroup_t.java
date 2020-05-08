 package calendar;
 import java.util.ArrayList;
 import java.util.Collection;
 
 public interface CalendarGroup {
 
 	ArrayList<CalendarSlotsImpl> getCalendars();
 	void addCalendar(CalendarSlotsImpl c);
 	void addCalendars(ArrayList<CalendarSlotsImpl> cals);
 	void clearCalendars();
 }
