 package calendar;
 import java.util.ArrayList;
 import java.util.Collection;
 
 public interface CalendarGroup {
<<<<<<< HEAD
 
	ArrayList<CalendarImpl> getCalendars();
	void addCalendar(CalendarImpl c);
	void addCalendars(ArrayList<CalendarImpl> cals);
=======
 	ArrayList<CalendarSlotsImpl> getCalendars();
 	void addCalendar(CalendarSlotsImpl c);
 	void addCalendars(ArrayList<CalendarSlotsImpl> cals);
>>>>>>> 18c2a1c6e9f02f0cce5246eadbb52c689c4fdbbd
 	void clearCalendars();
 }
