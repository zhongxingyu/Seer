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
 
 import java.util.*;
 import java.text.*;
 import java.sql.Timestamp;
 
 import com.vorsk.crossfitr.models.WorkoutModel;
 import com.vorsk.crossfitr.models.WorkoutRow;
 import com.vorsk.crossfitr.models.WorkoutSessionModel;
 import com.vorsk.crossfitr.models.WorkoutSessionRow;
 
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
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class CalendarActivity extends Activity implements OnClickListener {
 
 	private ImageView calJournalButton, preMonth, nextMonth;
 	private Button currentMonth;
 
 	private ListView calendarList;
 
 	private GridView calView;
 	private GridAdapter gridAdapter; // inner class to handle adapter
 	private Calendar derpCal;
 	private int month, year;
 	private final DateFormat dateFormatter = new DateFormat();
 	private static final String dateTemplate = "MMMM yyyy";
 	private static final String tag = "CalendarActivity";
 
 	private WorkoutSessionModel model_data;
 	private WorkoutSessionModel[] pulledData;
 	private ListView derp_calendar_list;
 	private ArrayList<WorkoutSessionModel> workoutSessionList;
 	private CalendarList calenderAdapter;
 
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
 
 		calView = (GridView) this.findViewById(R.id.calendargrid);
 		gridAdapter = new GridAdapter(getApplicationContext(), month, year);
 		gridAdapter.notifyDataSetChanged();
 		calView.setAdapter(gridAdapter);
 
 		model_data = new WorkoutSessionModel(this);
 
 		derp_calendar_list = (ListView) findViewById(R.id.calendar_list);
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
 
 		gridAdapter.notifyDataSetChanged();
 		calView.setAdapter(gridAdapter);
 	}
 
 	protected Activity getThis() {
 		return this;
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
 		private int daysInMonth;
 		private int currentDayOfMonth;
 		private int currentWeekDay;
 		private Button gridcell;
 		private int currentMonth_value, currentYear_value;
 		private String buttonControl_color = null;
 		private Button buttonControl = null;
 
 		private WorkoutSessionModel calendar_WSession;
 
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
 
 			calendar_WSession = new WorkoutSessionModel(context);
 
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
 
 				Log.d("Today is : ", "day: " + getCurrentDayOfMonth() + " month: "
 						+ currentMonth_value + " year : " + currentYear_value);
 
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
 
 			for (int i = 0; i < list.size(); i++) {
 				Log.d(tag, "Index [" + i + "] :" + list.get(i));
 			}
 
 		}
 
 		public View getView(int index, View convertView, ViewGroup parent) {
 			View row = convertView;
 			if (row == null) {
 				LayoutInflater inflater = (LayoutInflater) cal_context
 						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				row = inflater.inflate(R.layout.calendar_gridcell, parent, false);
 			}
 
 			// Get a reference to the Day
 			gridcell = (Button) row.findViewById(R.id.calendar_day_gridcell);
 			gridcell.setOnClickListener(this);
 
 			String[] day_color = list.get(index).split("-");
 
 			// set color for days
 			gridcell.setText(day_color[0]);
 
 			gridcell.setTag(day_color[1] + "-" + day_color[0] + "-" + day_color[2]
 					+ "-" + day_color[3]);
 
 			gridcell.setTextColor(colorChanger(day_color[1]));
 
 			Log.d(tag, "gridcell.getText : " + gridcell.getText());
 
 			int numberofRecords = recordChecker(day_color[0], day_color[2],
 					day_color[3]);
 			Log.d(tag, "numberofRecords = " + numberofRecords);
 
 			if (numberofRecords == 0) {
 				gridcell.setBackgroundResource(R.drawable.calendar_cellfiller);
 			} else if (numberofRecords == 1) {
 				gridcell.setBackgroundResource(R.drawable.calendar_has_one_record);
 			} else if (numberofRecords == 2) {
 				gridcell.setBackgroundResource(R.drawable.calendar_has_two_records);
 			} else if (numberofRecords == 3) {
 				gridcell.setBackgroundResource(R.drawable.calendar_has_three_records);
 			} else if (numberofRecords == 4) {
 				gridcell.setBackgroundResource(R.drawable.calendar_has_four_records);
 			} else if (numberofRecords >= 5) {
 				gridcell.setBackgroundResource(R.drawable.calendar_has_five_records);
 			}
 			return row;
 		}
 
 		private String removeColorfromTag(String _target) {
 			String[] tempHelper = _target.split("-");
 			String noColoronIt = new String(tempHelper[1] + "-" + tempHelper[2] + "-"
 					+ tempHelper[3]);
 			return noColoronIt;
 		}
 
 		private int recordChecker(String day, String month, String year) {
 
 			String startDate = new String(day + "-" + month + "-" + year);
 
 			int tempconverter = Integer.parseInt(day) + 1;
 
 			String endDate = new String(Integer.toString(tempconverter) + "-" + month
 					+ "-" + year);
 
 			Log.d(tag, "startDate : " + startDate);
 			Log.d(tag, "endDate : " + endDate);
 
 			try {
 				WorkoutSessionRow[] tempWS = calendar_WSession.getByTime(
 						stampTime(startDate), stampTime(endDate));
 			} catch (NullPointerException e) {
 				return 0;
 			}
 
 			WorkoutSessionRow[] tempWS = calendar_WSession.getByTime(
 					stampTime(startDate), stampTime(endDate));
 
 			Log.d(tag, "tempWS : " + tempWS.getClass().toString());
 			Log.d(tag, "tempWS.length : " + tempWS.length);
 
 			return tempWS.length;
 		}
 
 		private int colorChanger(String sColor) {
 
 			if (sColor.equals("GREY")) {
 				return Color.GRAY;
 			}

 			if (sColor.equals("BLUE")) {
 				return getResources().getColor(R.color.static_text_blue);
 			}
 			if (sColor.equals("YELLOW")) {
 				return getResources().getColor(R.color.static_text_yellow);
 			}
 			if (sColor.equals("RED")) {
 				return getResources().getColor(R.color.static_text_red);
 			} else {
 				return Color.WHITE;
 			}
 
 		}
 
 		public int getCurrentDayOfMonth() {
 
 			if (currentDayOfMonth - 1 <= 0)
 				return 1;
 
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
 
 		public int stampTime(String _sDate) {
 			Log.d("stampTime", "_sDate = " + _sDate);
 			try {
 				SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
 				Log.d("stampTime", "formatter = " + formatter.toString());
 				Date date = (Date) formatter.parse(_sDate);
 				Log.d("stampTime", "date = " + date.toString());
 
 				java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());
 				Log.d("stampTime", "date.getTime() = " + date.getTime());
 				Log.d("stampTime", "timeStampDate = " + timeStampDate);
 				long milliTime = timeStampDate.getTime() / 1000L;
 				Log.d("stampTime", "milliTime = " + milliTime);
 				return (int) milliTime;
 
 			} catch (ParseException e) {
 				return 0;
 			}
 		}
 
 		// TODO: Need to implement onClick method to retrieve data and put those on
 		// the list
 		public void onClick(View view) {
 			Button clickedButton = (Button) view;
 			if (buttonControl != null && buttonControl_color != null) {
 				buttonControl.setTextColor(colorChanger(buttonControl_color));
 				// buttonControl.setBackgroundResource(R.drawable.calendar_cellfiller);
 			}
 
 			clickedButton.setOnClickListener(this);
 			clickedButton.setTextColor(getResources().getColor(
 					R.color.static_text_green));
 			// clickedButton.setBackgroundResource(R.drawable.calendar_bg_frame);
 
 			buttonControl = clickedButton;
 			String tempHelper = (String) buttonControl.getTag();
 			String[] colorHelper = tempHelper.split("-");
 			buttonControl_color = colorHelper[0];
 
 			String[] noColor = ((String) clickedButton.getTag()).split("-");
			int numberofRecord = recordChecker(noColor[0],noColor[1],noColor[2]);
 			
 			
 		
 		}
 
 	}
 
 	/**
 	 * Name: CalendarListActivity Inner class to handle the calendar list adapter
 	 */
 	public class CalendarList extends BaseAdapter {
 
 		private static final String tag = "CalendarList";
 		private final Context listContext;
 		private ArrayList<WorkoutSessionRow> arrayList;
 		private boolean havenoRecord;
 		private LayoutInflater inflater;
 		
 		private TextView itemWorkout;
 		private TextView itemRecord;
 		
 		private WorkoutSessionModel WSModel;
 		
 		public CalendarList(Context _context){
 			this.listContext = _context;
 			this.havenoRecord = true;
 			inflater = (LayoutInflater) _context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);			
 		}
 
 		public CalendarList(Context _context, ArrayList<WorkoutSessionRow> _data){
 			this.listContext = _context;
 			this.arrayList = _data;
 			this.havenoRecord = false;
 			inflater = (LayoutInflater) _context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			WSModel = new WorkoutSessionModel (_context);
 		}
 		public View getView(int position, View convertView, ViewGroup parent) {
 			
 			if(convertView == null)
 				convertView = inflater
 				.inflate(R.layout.custom_list_item, parent, false);
 			
 			itemWorkout = (TextView) convertView.findViewById(R.id.cal_workoutname);
 			itemRecord = (TextView) convertView.findViewById(R.id.cal_record);			
 			
 			if(havenoRecord == true){
 				itemWorkout.setText("No data existing on this day");
 				itemRecord.setText("No data existing on this day");
 			}else{
 			//  itemWorkout.setText(arrayList.get(position).workout_id)
 				
 			}
 			
 			
 			return convertView;
 		}
 
 		public int getCount() {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		public Object getItem(int position) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public long getItemId(int position) {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		
 	}
 }
