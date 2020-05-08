 package com.lnu.todomorrow;
 
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 
 import com.androidplot.xy.LineAndPointFormatter;
 import com.androidplot.xy.PointLabelFormatter;
 import com.androidplot.xy.SimpleXYSeries;
 import com.androidplot.xy.XYPlot;
 import com.androidplot.xy.XYSeries;
 import com.androidplot.xy.XYStepMode;
 import com.lnu.todomorrow.dao.TaskDAO;
import com.lnu.todomorrow.utils.Goal;
 import com.lnu.todomorrow.utils.Task;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 
 public class GoalOverview extends Activity {
 	private static final String TAG = GoalOverview.class.getSimpleName();
 
 	private XYPlot plot;
 	
 	private static TaskDAO taskDAO;
 	
	private Goal thisGoal;
	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.goal_overview);
 		
		thisGoal = (Goal) getIntent().getSerializableExtra("goal");
 		taskDAO = new TaskDAO(this);
 			
 		createTaskPlot();
         
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
         
 		Log.d(TAG, "onCreate()");
 	}
 
 
 	/**
 	 * 
 	 */
 	private void createTaskPlot() {
 		plot = (XYPlot) findViewById(R.id.goal_overview_plot);
 //		
 //		// Create a couple arrays of y-values to plot:
         Number[] series1Numbers = {1, 3, 5, 2, 0, 4, 0};
      
        taskDAO.open();
        List<Task> tasks = taskDAO.getAllTasksByGoal(goal);
        
         Calendar cal = Calendar.getInstance();
         int today = cal.get(Calendar.DAY_OF_YEAR);
         
         Number[] days = {
         		today-6,
         		today-5,
         		today-4,
         		today-3,
         		today-2,
         		today-1,
         		today
         };
         
         
         
 		XYSeries s1 = new SimpleXYSeries(Arrays.asList(days),
 				Arrays.asList(series1Numbers), "Tasks completed");
 //		 XYSeries s1 = new SimpleXYSeries(
 //               Arrays.asList(series1Numbers),        
 //               SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, 
 //               "Tasks completed");   
 
 
        // Create a formatter to use for drawing a series using LineAndPointRenderer
         // and configure it from xml:
         LineAndPointFormatter series1Format = new LineAndPointFormatter();
         series1Format.setPointLabelFormatter(new PointLabelFormatter());
         series1Format.configure(getApplicationContext(),
                 R.xml.line_point_formatter_with_plf1);
         
         plot.addSeries(s1, series1Format);
 
 //        // reduce the number of range labels
         plot.setTicksPerRangeLabel(5);
         //plot.getGraphWidget().setDomainLabelOrientation(-45);
 
         plot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
         plot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 1);
         //for debug
         //plot.getLayoutManager().setMarkupEnabled(true);
 	}
 	
 }
