 package eu.mihailvelikov.scarsdale;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import eu.mihailvelikov.scarsdale.R;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 public class CalendarAdapter extends BaseAdapter {
 	private static final int FIRST_DAY_OF_WEEK = Calendar.MONDAY;
 	private final Calendar calendar;
 	private final CalendarItem today;
 	private final CalendarItem selected;
 	private final LayoutInflater inflater;
 
 	private CalendarItem[] days;
 
 	public CalendarAdapter(Context context, Calendar monthCalendar) {
 		calendar = monthCalendar;
 		today = new CalendarItem(monthCalendar);
 		selected = new CalendarItem(monthCalendar);
 		calendar.set(Calendar.DAY_OF_MONTH, 1);
 		inflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	}
 
 	@Override
 	public int getCount() {
 		return days.length;
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return days[position];
 	}
 
 	@Override
 	public long getItemId(int position) {
 		final CalendarItem item = days[position];
 		if (item != null) {
 			return days[position].id;
 		}
 		return -1;
 	}
 
 	@Override
 	public View getView(int position, View view, ViewGroup parent) {
 		if (view == null) {
 			view = inflater.inflate(R.layout.calendar_item, null);
 		}
 		final TextView dayView = (TextView) view.findViewById(R.id.date);
 		final CalendarItem currentItem = days[position];
 
 		if (currentItem == null) {
 			dayView.setClickable(false);
 			dayView.setFocusable(false);
 			// view.setBackgroundDrawable(null);
 			dayView.setText(null);
 		} else {
 			boolean clickable = false;
 			if (currentItem.equals(today) && currentItem.getIsDietDay()) {
 				view.setBackgroundResource(R.drawable.today_diet_day_background);
 				clickable = true;
 			} else if (currentItem.equals(selected) && currentItem.getIsDietDay()) {
 				view.setBackgroundResource(R.drawable.selected_diet_day_background);
 				clickable = true;
 			} else if (currentItem.getIsDietDay()) {
 				view.setBackgroundResource(R.drawable.diet_day_background);
 				clickable = true;
 			} else if (currentItem.equals(today)) {
 				view.setBackgroundResource(R.drawable.today_diet_day_background);
 				clickable = true;
 			} else if (currentItem.equals(selected)) {
 				view.setBackgroundResource(R.drawable.selected_background);
 			} else {
 				view.setBackgroundResource(R.drawable.normal_background);
 			}
 			view.setClickable(!clickable);
 
 			dayView.setText(currentItem.text);
 
 		}
 
 		return view;
 	}
 
 	public final void setSelected(int year, int month, int day) {
 		selected.year = year;
 		selected.month = month;
 		selected.day = day;
 		notifyDataSetChanged();
 	}
 
 	public void refreshDays() {
 		final int year = calendar.get(Calendar.YEAR);
 		final int month = calendar.get(Calendar.MONTH);
 		final int firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK);
 		final int lastDayOfMonth = calendar
 				.getActualMaximum(Calendar.DAY_OF_MONTH);
 		final int blankies;
 		final CalendarItem[] days;
 
 		if (firstDayOfMonth == FIRST_DAY_OF_WEEK) {
 			blankies = 0;
 		} else if (firstDayOfMonth < FIRST_DAY_OF_WEEK) {
 			blankies = Calendar.SATURDAY - (FIRST_DAY_OF_WEEK - 1);
 		} else {
 			blankies = firstDayOfMonth - FIRST_DAY_OF_WEEK;
 		}
 		days = new CalendarItem[lastDayOfMonth + blankies];
 
 		for (int day = 1, position = blankies; position < days.length; position++) {
 			days[position] = new CalendarItem(year, month, day++);
 		}
 
 		this.days = days;
 		notifyDataSetChanged();
 	}
 	
 	public static void setStartDate(final GregorianCalendar date) {
 		CalendarItem.mStartDate = date;
 	}
 	
 	public static void setEndDate(final GregorianCalendar date) {
 		CalendarItem.mEndDate = date;
 	}
 
 	private static class CalendarItem {
 		public int year;
 		public int month;
 		public int day;
 		public String text;
 		public long id;
 		private boolean isDietDay;
 		private static GregorianCalendar mStartDate;
 		private static GregorianCalendar mEndDate;
 
 		// private static GregorianCalendar mStartDate;
 
 		// public CalendarItem(Calendar calendar) {
 		// this(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
 		// calendar.get(Calendar.DAY_OF_MONTH));
 		// }
 
 		public CalendarItem(int year, int month, int day) {
 			this.year = year;
 			this.month = month;
 			this.day = day;
 			this.text = String.valueOf(day);
 			this.id = Long.valueOf(year + "" + month + "" + day);
 			this.setIsDietDay(isDietDay());
 		}
 
 		public CalendarItem(Calendar monthCalendar) {
 			this(monthCalendar.get(Calendar.YEAR), monthCalendar
 					.get(Calendar.MONTH), monthCalendar
 					.get(Calendar.DAY_OF_MONTH));
 		}
 
 		@Override
 		public boolean equals(Object o) {
 			if (o != null && o instanceof CalendarItem) {
 				final CalendarItem item = (CalendarItem) o;
 				return item.year == year && item.month == month
 						&& item.day == day;
 			}
 			return false;
 		}
 
 		protected boolean isDietDay() {
 			GregorianCalendar g = new GregorianCalendar(year, month, day);
 			//GregorianCalendar today = new GregorianCalendar();
 			return g.after(mStartDate) && g.before(mEndDate) || g.equals(mStartDate);
 		}
 
 		public boolean getIsDietDay() {
 			return isDietDay;
 		}
 
 		public void setIsDietDay(boolean isDietDay) {
 			this.isDietDay = isDietDay;
 		}
 	}
 }
