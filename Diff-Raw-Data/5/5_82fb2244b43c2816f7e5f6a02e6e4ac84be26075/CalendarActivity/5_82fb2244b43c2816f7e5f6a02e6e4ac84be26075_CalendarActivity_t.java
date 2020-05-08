 /**
  * Calendar
  * 
  * @Process: In development 
  * TODO: 1. Make a list below the calendar (Ian)
  *       2. Access to the database (Ian)
  *       3. A little mark on the cell which has workout record (Darren)
  *          (got an idea. I will implement it soon)
  *       
  * Last update: May 29 12
  */
 
 package com.vorsk.crossfitr;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class CalendarActivity extends Activity implements OnClickListener {
 
 	private ImageView calJournalButton, preMonth, nextMonth;
 	private Button currentMonth;
 	private TextView nameofWorkout, durationtime;
 
 	private ListView calendarList;
 
 	private GridView calView;
 	private GridAdapter gridAdapter; // inner class to handle adapter
 	private Calendar derpCal;
 	private int month, year;
 	private final DateFormat dateFormatter = new DateFormat();
 	private static final String dateTemplate = "MMMM yyyy";
 
 	private static final String tag = "CalendarActivity";
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.calendar_main);
 
 		// declare a new calendar object with built-in calendar
 		derpCal = Calendar.getInstance(Locale.getDefault());
 		month = derpCal.get(Calendar.MONTH) + 1;
 		year = derpCal.get(Calendar.YEAR);
 
 		preMonth = (ImageView) this.findViewById(R.id.preMonth);
 		preMonth.setOnClickListener(this);
 		// ImageView is used like a button
 
 		currentMonth = (Button) this.findViewById(R.id.currentMonth);
 		currentMonth.setText(dateFormatter.format(dateTemplate, derpCal.getTime()));
 
 		nextMonth = (ImageView) this.findViewById(R.id.nextMonth);
 		nextMonth.setOnClickListener(this);
 
 		nameofWorkout = (TextView) this.findViewById(R.id.cal_workoutname);
 		durationtime = (TextView) this.findViewById(R.id.cal_durationtime);
 
 		calView = (GridView) this.findViewById(R.id.calendargrid);
 		gridAdapter = new GridAdapter(getApplicationContext(), month, year);
 		gridAdapter.notifyDataSetChanged();
 		calView.setAdapter(gridAdapter);
 	}
 
 	public void onClick(View view) {
 		if (view == preMonth) {
 			if (month <= 1) {
 				month = 12;
 				year--;
 			} else {
 				month--;
 			}
 			setGridAdapterToDate(month, year);
 		}
 
 		if (view == nextMonth) {
 			if (month > 11) {
 				month = 1;
 				year++;
 			} else
 				month++;
 		}
 		setGridAdapterToDate(month, year);
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 		// Activity shut down
 	}
 
 	private void setGridAdapterToDate(int month, int year) {
 		gridAdapter = new GridAdapter(getApplicationContext(), month, year);
 		derpCal.set(year, month - 1, derpCal.get(Calendar.DAY_OF_MONTH));
 		// Field number for get and set indicating the day of the month.
 		// This is a synonym for DATE. The first day of the month has value 1.
 
 		currentMonth.setText(dateFormatter.format(dateTemplate, derpCal.getTime()));
 		gridAdapter.notifyDataSetChanged();
 		calView.setAdapter(gridAdapter);
 	}
 
 	/**
 	 * Name: CalendarListActivity Inner class to handle the calendar list adapter
 	 * TODO:: ListView Starts from there 
 	 */
 	public class ListHelper extends Activity {
 
 		public void onCreate(Bundle savedInstanceState){
 			setContentView(R.layout.calendar_listhelper);
 			
 			ListView listview = (ListView) findViewById(R.id.calendar_list);		
 			
 		}
 	}
 	
 	/**
 	 * Name: GridAdapter Inner class to handle the calendar grid adapter
 	 * 
 	 */
 
 	public class GridAdapter extends BaseAdapter implements OnClickListener {
 
 		private static final String tag = "GridAdapter";
 
 		private final Context cal_context;
 		private List<String> list;
 		private static final int DAY_OFFSET = 1;
 		private final String[] weekdays = new String[] { "Sun", "Mon", "Tue",
 				"Wed", "Thu", "Fri", "Sat" };
 		// Strings for day
 		private final String[] months = { "January", "February", "March", "April",
 				"May", "June", "July", "August", "September", "October", "November",
 				"December" }; // Strings for month
 		private final int[] daysOfMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31,
 				30, 31 }; // the number of days of each month
 		private final int month, year;
 		private int daysInMonth, prevMonthDays;
 		private int currentDayOfMonth;
 		private int currentWeekDay;
 		private Button gridcell;
 		private int currentMonth_value, currentYear_value;
 		private String buttonControl_color = null;
 		private boolean viewControl = false;
 		private Button buttonControl = null;
 
 		private final SimpleDateFormat dateFormatter = new SimpleDateFormat(
 				"dd-MMM-yyyy");
 
 		public GridAdapter(Context context, int month, int year) {
 			super();
 			this.cal_context = context;
 			this.list = new ArrayList<String>();
 			this.month = month;
 			this.year = year;
 
 			Calendar tempcal = Calendar.getInstance();
 			setCurrentDayOfMonth(tempcal.get(Calendar.DAY_OF_MONTH));
 			setCurrentWeekDay(tempcal.get(Calendar.DAY_OF_WEEK));
 			currentMonth_value = tempcal.get(Calendar.MONTH) + 1;
 			currentYear_value = tempcal.get(Calendar.YEAR);
 			createMonth(month, year);
 
 		}
 
 		private String getMonthAsString(int i) {
 			return months[i];
 		}
 
 		private int getNumberOfDaysOfMonth(int i) {
 			return daysOfMonth[i];
 		}
 
 		public String getItem(int arg0) {
 			return list.get(arg0);
 		}
 
 		public int getCount() {
 			return list.size();
 		}
 
 		public long getItemId(int arg0) {
 			return arg0;
 		}
 
 		private void createMonth(int mon, int year) {
 
 			int trailingSpaces = 0;
 			int daysInPrevMonth = 0;
 			int prevMonth = 0;
 			int prevYear = 0;
 			int nextMonth = 0;
 			int nextYear = 0;
 
 			int currentMonth = mon - 1;
 			String NameofCurrentMonth = getMonthAsString(currentMonth);
 			daysInMonth = getNumberOfDaysOfMonth(currentMonth);
 
 			GregorianCalendar cal = new GregorianCalendar(year, currentMonth, 1);
 
 			if (currentMonth == 11) {
 				prevMonth = currentMonth - 1;
 				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
 				nextMonth = 0;
 				prevYear = year;
 				nextYear = year + 1;
 			} else if (currentMonth == 0) {
 				prevMonth = 11;
 				prevYear = year - 1;
 				nextYear = year;
 				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
 				nextMonth = 1;
 			} else {
 				prevMonth = currentMonth - 1;
 				nextMonth = currentMonth + 1;
 				nextYear = year;
 				prevYear = year;
 				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
 			}
 
 			int currentWeekDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
 			trailingSpaces = currentWeekDay;
 
 			if (cal.isLeapYear(cal.get(Calendar.YEAR)) && month == 2) {
 				daysInMonth++;
 			}
 
 			for (int i = 0; i < trailingSpaces; i++) {
 				list.add(String.valueOf((daysInPrevMonth - trailingSpaces + DAY_OFFSET)
 						+ i)
 						+ "-GREY" + "-" + getMonthAsString(prevMonth) + "-" + prevYear);
 			}
 
 			int indexCount = trailingSpaces - 1;
 			String tempString;
 
 			// Current Month Days
 			for (int i = 1; i <= daysInMonth; i++) {
 				list.add(String.valueOf(i) + "-WHITE" + "-"
 						+ getMonthAsString(currentMonth) + "-" + year);
 				indexCount++;
 
 				if (indexCount % 7 == 6) {
 					tempString = String.valueOf(i) + "-BLUE" + "-"
 							+ getMonthAsString(currentMonth) + "-" + year;
 					list.set(indexCount, tempString);
 				} else if (indexCount % 7 == 0) {
 					tempString = String.valueOf(i) + "-RED" + "-"
 							+ getMonthAsString(currentMonth) + "-" + year;
 					list.set(indexCount, tempString);
 				}
 
 				if (i == getCurrentDayOfMonth() && month == currentMonth_value
 						&& year == currentYear_value) {
 					tempString = String.valueOf(i) + "-YELLOW" + "-"
 							+ getMonthAsString(currentMonth) + "-" + year;
 					list.set(indexCount, tempString);
 				}
 			}
 
 			// Leading Month days
 			for (int i = 0; i < list.size() % 7; i++) {
				list.add(String.valueOf(i + 1) + "-GREY" + "-"
 						+ getMonthAsString(nextMonth) + "-" + nextYear);
 			}
 			
 			for(int i = 0; i < list.size(); i++){
 				Log.d(tag, "Index ["+i+"] :" + list.get(i));
 			}
 
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View row = convertView;
 			if (row == null) {
 				LayoutInflater inflater = (LayoutInflater) cal_context
 						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				row = inflater.inflate(R.layout.calendar_gridcell, parent, false);
 			}
 
 			// Get a reference to the Day
 			gridcell = (Button) row.findViewById(R.id.calendar_day_gridcell);
 			gridcell.setOnClickListener(this);
 
 			String[] day_color = list.get(position).split("-");
 
 			// set color for days
 			gridcell.setText(day_color[0]);
 			gridcell.setTag(day_color[1] + "-" + day_color[0] + "-" +  day_color[2] + "-" + day_color[3]);
 			
 			gridcell.setTextColor(colorChanger(day_color[1]));
 			
 			return row;
 		}
 
 		// TODO: Need to implement onClick method to retrieve data and put those on
 		// the list
 		public void onClick(View view) {
 			Button clickedButton = (Button) view;
 			if(buttonControl != null && buttonControl_color != null){
 				buttonControl.setTextColor(colorChanger(buttonControl_color));
 			}
 		
 			clickedButton.setOnClickListener(this);
 			clickedButton.setTextColor(getResources().getColor(
 						R.color.static_text_green));
 			
 			buttonControl = clickedButton;
 			String tempHelper = (String) buttonControl.getTag();
 			String[] colorHelper = tempHelper.split("-");
 			buttonControl_color = colorHelper[0];
 
 			
 			Log.d(tag,"getId() : " + view.getId());
 		}
 		
 		private int colorChanger(String sColor){
 			
			if (sColor.equals("GREY")) {
 				return Color.GRAY;
 			}
 
 			if (sColor.equals("BLUE")) {
 				return getResources().getColor(
 						R.color.static_text_blue);
 			}
 			if (sColor.equals("YELLOW")) {
 				return getResources().getColor(
 						R.color.static_text_yellow);
 			}
 			if (sColor.equals("RED")) {
 				return getResources().getColor(
 						R.color.static_text_red);
 			}
 			else{
 				return Color.WHITE;
 			}
 			
 		}
 
 		public int getCurrentDayOfMonth() {
 			return currentDayOfMonth - 1;
 		}
 
 		private void setCurrentDayOfMonth(int currentDayOfMonth) {
 			this.currentDayOfMonth = currentDayOfMonth;
 		}
 
 		public void setCurrentWeekDay(int currentWeekDay) {
 			this.currentWeekDay = currentWeekDay;
 		}
 
 		public int getCurrentWeekDay() {
 			return currentWeekDay;
 		}
 
 	}
 }
