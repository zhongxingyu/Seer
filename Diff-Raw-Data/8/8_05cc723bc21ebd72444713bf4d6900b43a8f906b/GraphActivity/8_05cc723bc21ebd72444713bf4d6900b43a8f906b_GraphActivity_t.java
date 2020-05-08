 package com.example.liveabetes;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.os.Build;
 import android.graphics.Color;
 import com.androidplot.xy.SimpleXYSeries;
 import com.androidplot.series.XYSeries;
 import com.androidplot.xy.*;
 import java.util.Arrays;
 import java.util.List;
 
 public class GraphActivity extends Activity {
 	
 	private XYPlot mySimpleXYPlot;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_graph);
 		
 		//create the GlucoseDatabaseHandler
 		GlucoseDatabaseHandler db = new GlucoseDatabaseHandler(this);
 		Log.d("Reading: ", "Reading all contacts.."); 
 		List<Entry> contacts = db.getAllEntries();       
 
 		Number[] series1Numbers = new Number[contacts.size()];
 		Number[] series2Numbers = new Number[contacts.size()];
 		
 		System.out.println(contacts.size());
 		
 		for (int i = 0; i < contacts.size(); i++) {
 			series1Numbers[i] = contacts.get(i).getID();
 			series2Numbers[i] = Integer.parseInt(contacts.get(i).getValue());
 		}
 		
 		//for (int i = 0; i < contacts.size; )
 
 		// initialize our XYPlot reference:
 		mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
 
 		// Turn the above arrays into XYSeries':
 		XYSeries series1 = new SimpleXYSeries(
 				Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
 				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
 				"Series1");                             // Set the display title of the series
 
 		// same as above
 		XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series2");
 
 		// Create a formatter to use for drawing a series using LineAndPointRenderer:
 		LineAndPointFormatter series1Format = new LineAndPointFormatter(
 				Color.rgb(0, 200, 0),                   // line color
 				Color.rgb(0, 100, 0),                   // point color
				null,(PointLabelFormatter) null);                                  // fill color (none)
 
 		// add a new series' to the xyplot:
 		mySimpleXYPlot.addSeries(series1, series1Format);
 
 		// same as above:
 		mySimpleXYPlot.addSeries(series2,
				new LineAndPointFormatter(Color.rgb(0, 0, 200), Color.rgb(0, 0, 100), null, (PointLabelFormatter) null));
 
 		// reduce the number of range labels
 		mySimpleXYPlot.setTicksPerRangeLabel(3);
 
 		// by default, AndroidPlot displays developer guides to aid in laying out your plot.
 		// To get rid of them call disableAllMarkup():
		//mySimpleXYPlot.disableAllMarkup();//Deprecated not needed anymore
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.graph, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 }
