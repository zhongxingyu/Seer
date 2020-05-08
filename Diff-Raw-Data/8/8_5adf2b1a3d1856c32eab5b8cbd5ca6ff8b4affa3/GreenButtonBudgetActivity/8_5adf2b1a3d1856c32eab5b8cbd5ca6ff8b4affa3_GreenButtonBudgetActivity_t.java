 package com.bryanmarty.greenbutton;
 
 import java.net.ContentHandler;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import com.bryanmarty.greenbutton.data.IntervalReading;
 import com.bryanmarty.greenbutton.database.TrackManager;
 
 import android.app.Activity;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.os.Bundle;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class GreenButtonBudgetActivity extends Activity {
 
 	protected int lastMonthUsage = 0;
 	protected int currentMonthUsage = 0;
 	protected int currentDayOfMonth = 0;
 	
 	protected boolean needsToUpdateGBData = false;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	
 	    setContentView(R.layout.budget);
 	    
 	    setUpDate();
 	    setUpUsage();
 	    setUpMeter();
 	}
 
 	@Override
 	protected void onStart() {		
 		super.onStart();
 		
 	}
 	
 	protected void setUpDate()
 	{
 		String[] monthName = {"January", "February",
 				"March", "April", "May", "June", "July",
 				"August", "September", "October", "November",
 				"December"};
 		TextView txtDaysRemaining = (TextView)this.findViewById(R.id.txtDaysRemaining);
 		
 		Calendar c = Calendar.getInstance();
 		int daysInMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
 		int dayInMonth = c.get(Calendar.DAY_OF_MONTH);
 		currentDayOfMonth = dayInMonth;
 		int daysLeft = daysInMonth - dayInMonth;
		String dayORdays = "";
		if (daysLeft == 1)
			dayORdays = " day";
		else
			dayORdays = " days";
		
		txtDaysRemaining.setText("" + daysLeft + dayORdays + " left in " + monthName[c.get(Calendar.MONTH)]);
 	}
 	
 	protected void setUpUsage()
 	{
 		LinkedList<IntervalReading> result = new LinkedList<IntervalReading>();
 		LinkedList<IntervalReading> result2 = new LinkedList<IntervalReading>();
 		Date dateMostRecentEntry = Calendar.getInstance().getTime();
 		Date dateStartThisMonth;
 		Date dateStartLastMonth;
 		Date dateEndLastMonth;
 		
 		Future<Date> futureDateMostRecentEntry = TrackManager.getLastDate();
 		try {
 				dateMostRecentEntry = futureDateMostRecentEntry.get();
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 			
 		Calendar c = Calendar.getInstance();
 		c.setTimeInMillis(dateMostRecentEntry.getTime());
 		c.set(Calendar.DAY_OF_MONTH, 1);
 		c.set(Calendar.HOUR_OF_DAY, 0);
 		c.set(Calendar.MINUTE, 0);
 		dateStartThisMonth = c.getTime();
 		
 		c.roll(Calendar.MONTH, -1);
 		dateStartLastMonth = c.getTime();
 		int numDayslastMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
 		c.add(Calendar.DAY_OF_MONTH, numDayslastMonth);
 		dateEndLastMonth = c.getTime();
 		
 		Future<LinkedList<IntervalReading>> future = TrackManager.getReadingsBetween(dateStartLastMonth, dateEndLastMonth);
 		try {
 			result = future.get();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		int usagePrevious = 0;
 		for(IntervalReading r : result)
 		{
 			usagePrevious += r.getValue();
 		}
 				
 		Future<LinkedList<IntervalReading>> future2 = TrackManager.getReadingsBetween(dateStartThisMonth, dateMostRecentEntry);
 		try {
 			result2 = future2.get();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		if(result2.size() == 0 || dateMostRecentEntry.before(Calendar.getInstance().getTime()))
 			needsToUpdateGBData = true;
 		
 		int usageCurrent = 0;
 		for(IntervalReading r : result2 )
 		{
 			usageCurrent += r.getValue();
 		}
 		
 		lastMonthUsage = (int)((double)usagePrevious / 1000.0);
 		currentMonthUsage = (int)((double)usageCurrent / 1000.0);
 		
 	}
 	
 	protected void setUpMeter()
 	{
 		TextView txtEnergyUsed = (TextView)this.findViewById(R.id.txtPercentEnergyUsed);
 		
 		ImageView imMeter = (ImageView)this.findViewById(R.id.imMeter);
 		
 		double percentUsage = ((double)currentMonthUsage) / ((double) lastMonthUsage);
 		
 		if(needsToUpdateGBData)
 		{
 			txtEnergyUsed.setTypeface(null, Typeface.ITALIC);
 			txtEnergyUsed.setText("No GB Data available this month, please add current data first.");
 		}
 		else
 		{
 			if(percentUsage < 1.0)
 				txtEnergyUsed.setText("" + (int)(percentUsage * 100) + "% energy used");
 			else
 				txtEnergyUsed.setText("Budget exceeded by " + String.valueOf((int)(percentUsage * 100) - 100) + "%");
 		}
 		//double percentMonth = ((double)currentDayOfMonth) / ((double)Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH));
 		
 		if(percentUsage > 0.85)
 		{
 			imMeter.setImageResource(R.drawable.budget_meter_0_hdpi);
 		}
 		else if (percentUsage > 0.7)
 		{
 			imMeter.setImageResource(R.drawable.budget_meter_1_hdpi);
 		}
 		else if (percentUsage > 0.55)
 		{
 			imMeter.setImageResource(R.drawable.budget_meter_2_hdpi);
 		}
 		else if (percentUsage > 0.40)
 		{
 			imMeter.setImageResource(R.drawable.budget_meter_3_hdpi);
 		}
 		else if (percentUsage > 0.25)
 		{
 			imMeter.setImageResource(R.drawable.budget_meter_4_hdpi);
 		}
 		else
 		{
 			imMeter.setImageResource(R.drawable.budget_meter_5_hdpi);
 		}
 		
 	}
 }
