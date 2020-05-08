 package uk.ac.dur.duchess.ui.activity;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import net.fortuna.ical4j.model.Calendar;
 import uk.ac.dur.duchess.R;
 import uk.ac.dur.duchess.ui.view.FlowLayout;
 import uk.ac.dur.duchess.util.TimeUtils;
import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import edu.emory.mathcs.backport.java.util.Collections;
 
public class TimeActivity extends Activity
 {	
 	private TextView eventNameTextView;
 	private TextView dateRangeTextView;
 	
 	private ImageView prevWeekButton;
 	private ImageView nextWeekButton;
 	
 	private TextView mondayTextView;
 	private TextView tuesdayTextView;
 	private TextView wednesdayTextView;
 	private TextView thursdayTextView;
 	private TextView fridayTextView;
 	private TextView saturdayTextView;
 	private TextView sundayTextView; 
 	
 	private FlowLayout mondayContainer;
 	private FlowLayout tuesdayContainer;
 	private FlowLayout wednesdayContainer;
 	private FlowLayout thursdayContainer;
 	private FlowLayout fridayContainer;
 	private FlowLayout saturdayContainer;
 	private FlowLayout sundayContainer;
 	
 	private Calendar calendar;
 	private java.util.Calendar week;
 	
 	private String eventName;
 	private String eventLocation;
 	
 	private SimpleDateFormat source;
 	private SimpleDateFormat timeFormat;
 	
 	private FlowLayout[] containers;
 	private TextView[] dayTextViews;
 	
 	private String endDate;
 	private String startDate;
 	private String firstWeek;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.time_chooser_layout);
 		
 		eventNameTextView = (TextView) findViewById(R.id.textViewEventName);
 		dateRangeTextView = (TextView) findViewById(R.id.timeViewDateRange);
 		
 		prevWeekButton = (ImageView) findViewById(R.id.prevWeekButton);
 		nextWeekButton = (ImageView) findViewById(R.id.nextWeekButton);
 		
 		mondayTextView    = (TextView) findViewById(R.id.mondayTextView);
 		tuesdayTextView   = (TextView) findViewById(R.id.tuesdayTextView);
 		wednesdayTextView = (TextView) findViewById(R.id.wednesdayTextView);
 		thursdayTextView  = (TextView) findViewById(R.id.thursdayTextView);
 		fridayTextView    = (TextView) findViewById(R.id.fridayTextView);
 		saturdayTextView  = (TextView) findViewById(R.id.saturdayTextView);
 		sundayTextView    = (TextView) findViewById(R.id.sundayTextView);
 		
 		mondayContainer    = (FlowLayout) findViewById(R.id.mondayDateContainer);
 		tuesdayContainer   = (FlowLayout) findViewById(R.id.tuesdayDateContainer);
 		wednesdayContainer = (FlowLayout) findViewById(R.id.wednesdayDateContainer);
 		thursdayContainer  = (FlowLayout) findViewById(R.id.thursdayDateContainer);
 		fridayContainer    = (FlowLayout) findViewById(R.id.fridayDateContainer);
 		saturdayContainer  = (FlowLayout) findViewById(R.id.saturdayDateContainer);
 		sundayContainer    = (FlowLayout) findViewById(R.id.sundayDateContainer);
 		
 		containers = new FlowLayout[] {sundayContainer, mondayContainer, tuesdayContainer,
 				wednesdayContainer, thursdayContainer, fridayContainer, saturdayContainer};
 		
 		dayTextViews = new TextView[] {mondayTextView, tuesdayTextView, wednesdayTextView,
 				thursdayTextView, fridayTextView, saturdayTextView, sundayTextView};
 		
 		try
 		{
 			Bundle e = getIntent().getExtras();
 			eventName = e.getString("event_name");
 			eventLocation = e.getString("event_address");
 			String iCalURL = e.getString("ical_url");
 			startDate = e.getString("event_start_date");
 			endDate = e.getString("event_end_date");
 			
 			eventNameTextView.setText(eventName);
 			calendar = TimeUtils.parseICalFromURL(iCalURL);
 			
 			
 			String value = android.provider.Settings.System.getString
 					(this.getContentResolver(), android.provider.Settings.System.TIME_12_24);
 			
 			String format12 = "hh:mm a";
 			String format24 = "HH:mm";
 			
 			boolean usesAM_PM = value.equals("12");
 			
 			source = new SimpleDateFormat(format24);
 			timeFormat = new SimpleDateFormat(usesAM_PM ? format12 : format24);
 			
 			week = java.util.Calendar.getInstance();
 			
 			week.setTime((new SimpleDateFormat("yyyy-MM-dd")).parse(startDate));
 			week = TimeUtils.getClosestFutureWeek(week);
 			
 			firstWeek = (new SimpleDateFormat("yyyy-MM-dd")).format(week.getTime());
 
 			setupWeekView();
 			
 			prevWeekButton.setOnClickListener(new View.OnClickListener()
 			{
 				@Override
 				public void onClick(View view)
 				{
 					week.add(java.util.Calendar.WEEK_OF_YEAR, -1);
 					week.get(0);
 					
 					try { setupWeekView(); }
 					catch (ParseException e) { e.printStackTrace(); } // TODO
 				}
 			});
 			
 			nextWeekButton.setOnClickListener(new View.OnClickListener()
 			{
 				@Override
 				public void onClick(View view)
 				{
 					week.add(java.util.Calendar.WEEK_OF_YEAR, 1);
 					week.get(0);
 					
 					try { setupWeekView(); }
 					catch (ParseException e) { e.printStackTrace(); } // TODO
 				}
 			});
 		}
 		catch (Exception e) { e.printStackTrace(); }
 	}
 
 	private void setupWeekView() throws ParseException
 	{
 		for(int i = 0; i < containers.length; i++)
 		{
 			containers[i].removeAllViews();
 			containers[i].setVisibility(View.VISIBLE);
 			containers[i].invalidate();
 			dayTextViews[(containers.length + i - 1) % containers.length].setVisibility(View.VISIBLE);
 		}
 		
 		prevWeekButton.setVisibility(View.VISIBLE);
 		prevWeekButton.setClickable(true);
 		nextWeekButton.setVisibility(View.VISIBLE);
 		nextWeekButton.setClickable(true);
 		
 		java.util.Calendar monday = TimeUtils.getMondayOfGivenWeek(week);
 		java.util.Calendar sunday = TimeUtils.getSundayOfGivenWeek(week);
 		
 		dateRangeTextView.setText(       (new SimpleDateFormat("d MMM")).format(monday.getTime()));
 		dateRangeTextView.append(" - " + (new SimpleDateFormat("d MMM yyyy")).format(sunday.getTime()));
 		
 		List<String> unformatted = TimeUtils.getDatesBetween(monday, sunday);
 		
 		List<String> dayTextStrings = new ArrayList<String>();
 		
 		for (String dayText : unformatted)
 		{
 			java.util.Calendar f = java.util.Calendar.getInstance();
 			
 			if(dayText.equals(startDate) || dayText.equals(firstWeek))
 			{
 				prevWeekButton.setVisibility(View.INVISIBLE);
 				prevWeekButton.setClickable(false);
 			}
 			if(dayText.equals(endDate))
 			{
 				nextWeekButton.setVisibility(View.INVISIBLE);
 				nextWeekButton.setClickable(false);
 			}
 				
 			f.setTime((new SimpleDateFormat("yyyy-MM-dd")).parse(dayText));
 			dayTextStrings.add((new SimpleDateFormat("EEEEE, dd MMMMM yyyy")).format(f.getTime()));
 		}
 		
 		for(int i = 0; i < dayTextStrings.size(); i++)
 			dayTextViews[i].setText(dayTextStrings.get(i));
 
 		
 		Map<Integer,List<String>> map = TimeUtils.groupEventsByDay(
 			TimeUtils.getRecurrenceSetForGivenWeek(calendar, eventName,
 					(new SimpleDateFormat("yyyy-MM-dd")).format(week.getTime())));
 		
 		for (Integer i : map.keySet())
 		{
 			List<String> tempList = map.get(i);
 			if (tempList.size() > 1) Collections.sort(tempList);
 		}
 		for (Integer day : map.keySet())
 		{
 			List<String> buttonText = map.get(day);
 			
 			if (buttonText != null)
 				for (final String s : buttonText)
 					containers[day - 1].addView(getTimeButton(s));
 		}
 		
 		if(map.isEmpty()) ((TextView) findViewById(R.id.footerView)).setText("The event is not on this week");
 		else ((TextView) findViewById(R.id.footerView)).setText("");
 		
 		for(int i = 0; i < containers.length; i++)
 			if(containers[i].getChildCount() == 0)
 			{
 				containers[i].setVisibility(View.GONE);
 				dayTextViews[(containers.length + i - 1) % containers.length].setVisibility(View.GONE);
 			}
 	}
 
 	private Button getTimeButton(final String s) throws ParseException
 	{
 		Button b = new Button(this);
 		
 		String startTime = timeFormat.format(source.parse(s.substring(11, 16)));
 		String endTime   = timeFormat.format(source.parse(s.substring(20, 26)));
 		
 		b.setText(startTime + " - " + endTime);
 		b.setBackgroundDrawable(getResources().getDrawable(R.drawable.time_button));
 		b.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.plus, 0);
 		b.setCompoundDrawablePadding(5);
 		
 		b.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				try
 				{
 					java.util.Calendar start = java.util.Calendar.getInstance(); 
 					java.util.Calendar end   = java.util.Calendar.getInstance(); 
 					
 					start.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(s.substring(0, 10) + " " + s.substring(11, 16)));
 					end.setTime  (new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(s.substring(0, 10) + " " + s.substring(20, 26)));
 					
 					Intent intent = new Intent(Intent.ACTION_EDIT);
 					
 					intent.setType("vnd.android.cursor.item/event");
 					intent.putExtra("beginTime", start.getTimeInMillis());
 					intent.putExtra("endTime", end.getTimeInMillis());
 					intent.putExtra("title", eventName);
 					intent.putExtra("eventLocation", eventLocation);
 					
 					startActivity(intent);
 				}
 				catch(ParseException e) { e.printStackTrace(); }
 			}
 		});
 		
 		return b;
 	}
 }
