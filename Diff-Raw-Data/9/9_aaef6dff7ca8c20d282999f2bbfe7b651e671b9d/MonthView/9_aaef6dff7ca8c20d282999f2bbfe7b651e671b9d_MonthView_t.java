 /**
  * Displays the small calendar.
  */
 
 package gui.main;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import javax.swing.JPanel;
 
import java.util.Date;
 
 public class MonthView extends JPanel {
 	
 	public static int CURRENT_MONTH = 0;
 	public static int PREVIOUS_MONTH = -1;
 	public static int NEXT_MONTH = 1;
 	
 	private Date date;
 	
 	private ArrayList<TinyDayBlock> days = new ArrayList<TinyDayBlock>();
 	
 	public MonthView(DatePicker dp, Date date, int mon) {
 		setPreferredSize(new Dimension(200, 185));	//modified to 185 from 165
 		setMaximumSize(new Dimension(200, 185));
 		JPanel panel = new JPanel(new GridLayout(6,7));
 		
 		
 		GregorianCalendar current = new GregorianCalendar();
 		current.setTimeInMillis(System.currentTimeMillis());
 		Date dateToday = new Date(current.get(Calendar.MONTH)+1, current.get(Calendar.DATE), current.get(Calendar.YEAR));
 		
 		this.date = date;
 		
 		GregorianCalendar cal = new GregorianCalendar();
 		cal.set(Calendar.MONTH, date.getMonth() - 1);
 		cal.set(Calendar.DATE, 1);
 		cal.set(Calendar.YEAR, date.getYear());
 		
 		if (mon == MonthView.PREVIOUS_MONTH) {
 			if (cal.get(Calendar.MONTH) > 0) cal.roll(Calendar.MONTH, false);
 			else {
 				cal.roll(Calendar.YEAR, false);
 				cal.roll(Calendar.MONTH, false);
 			}
 		}
 		else if (mon == MonthView.NEXT_MONTH) {
 			if (cal.get(Calendar.MONTH) < 11) cal.roll(Calendar.MONTH, true);
 			else {
 				cal.roll(Calendar.YEAR, true);
 				cal.roll(Calendar.MONTH, true);
 			}
 		
 		}
 			
 		String month = "December";
 		int m = cal.get(Calendar.MONTH) + 1;
 		if (m == 1) month = "January";
 		else if (m == 2) month = "February";
 		else if (m == 3) month = "March";
 		else if (m == 4) month = "April";
 		else if (m == 5) month = "May";
 		else if (m == 6) month = "June";
 		else if (m == 7) month = "July";
 		else if (m == 8) month = "August";
 		else if (m == 9) month = "September";
 		else if (m == 10) month = "October";
 		else if (m == 11) month = "November";
 		
 		//prevDays is the number of boxes in the upper left, before the first of the month, needed since the 
 		//calendar is going to be a 6x7 set of boxes. Calendar.SUNDAY is 1 and so forth, so we use day of week - 1
 		int prevDays = cal.get(Calendar.DAY_OF_WEEK) - 1; 
 		int endDays = 42 - cal.getActualMaximum(Calendar.DAY_OF_MONTH) - prevDays;
 		
 		cal.roll(Calendar.MONTH, false);
 		
 		for (int i = 1; i <= prevDays; i++) {
 			Date d = new Date(cal.get(Calendar.MONTH) + 1, cal.getActualMaximum(Calendar.DAY_OF_MONTH) - prevDays + i, cal.get(Calendar.YEAR));
 			panel.add(new TinyDayBlock(dp, d, Color.LIGHT_GRAY, false));
 		}
 		
 		cal.roll(Calendar.MONTH, true);
 		
 		for (int i = 1; i <= cal.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
 			Date d = new Date(m, i, cal.get(Calendar.YEAR));
 			boolean today = false;
 			if (cal.get(Calendar.MONTH)+1 == dateToday.getMonth() && i == dateToday.getDay() && 
 				cal.get(Calendar.YEAR) == dateToday.getYear()) today = true;
 			TinyDayBlock t = new TinyDayBlock(dp, d, today);
 			if (today) dp.registerCurrentDay(t);
 			panel.add(t);
 			days.add(t);
 		}
 		
 		cal.roll(Calendar.MONTH, true);
 		
 		for (int i = 1; i <= endDays; i++) {
 			Date d = new Date(cal.get(Calendar.MONTH) + 1, i, cal.get(Calendar.YEAR));
 			panel.add(new TinyDayBlock(dp, d, Color.LIGHT_GRAY, false));
 		}
 		
 		TinyMonthHeadingPanel mhp = new TinyMonthHeadingPanel(month, cal.get(Calendar.YEAR) + "", (mon == MonthView.CURRENT_MONTH), dp);
 		JPanel main = new JPanel(new BorderLayout());
 		main.add(mhp, BorderLayout.NORTH);
 		
 		main.add(panel, BorderLayout.CENTER);
 		this.add(main, BorderLayout.CENTER);
 	}
 	
 	public void selectDay(int day) {
 		days.get(day-1).select();
 	}
 	
 	public void deselectDay(int day) {
 		days.get(day-1).deselect();
 	}
 	
 	public Date getDate() {
 		return date;
 	}
 	
 	
 }
