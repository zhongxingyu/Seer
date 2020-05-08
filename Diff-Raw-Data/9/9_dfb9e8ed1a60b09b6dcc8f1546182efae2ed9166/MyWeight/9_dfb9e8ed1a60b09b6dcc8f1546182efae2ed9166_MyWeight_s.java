 package com.example.alexfed.myweight;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 import com.jjoe64.graphview.GraphView;
 import com.jjoe64.graphview.GraphView.GraphViewData;
 import com.jjoe64.graphview.GraphViewSeries;
 import com.jjoe64.graphview.LineGraphView;
 public class MyWeight extends Activity { 
 
 	private static final String EXPORT_FILE = "/mnt/sdcard/myweight.csv";
 	private static final int BASIC_BORDER = 20;
 	private static final int BASIC_FONT_SIZE = 14;
 	private static final int BASIC_VLABEL_WIDTH = 100;
 	
 	private GraphView graphView;
 	private LinearLayout layout;
 	private double viewportStart;
 	private double viewportSize;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_my_weight); 
 		
 		//my code
 		graphView = null;
 		layout = (LinearLayout) findViewById(R.id.layout);
 	}
 
 	
 	
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		
 		populateDataFromCsv();
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
 	 */
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
 		
 		viewportStart = savedInstanceState.getDouble("start");
 		viewportSize = savedInstanceState.getDouble("size");
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
 	 */
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	
 		if( graphView != null ){
 			double vp[] = graphView.getViewPort();
 			outState.putDouble("start", vp[0]);
 			outState.putDouble("size", vp[1]);
 		}else{
 			outState.putDouble("start", 0.0);
 			outState.putDouble("size", 0.0);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_my_weight, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		switch (item.getItemId()) {
     		case R.id.menu_settings:
     			return true;
     		case R.id.menu_import_csv:
     			populateDataFromCsv();
     			return true;
     		case R.id.menu_import_db:
     			populateDataFromDB();
     			return true;
     		case R.id.menu_add:
     			testAddSomeWeights();
     			return true;
     		case R.id.menu_clear_db:
     			clearDB();
     			return true;
     		default:
     			return super.onOptionsItemSelected(item);
 		}
    
 	}
 
 	private void populateDataFromCsv(){
 		
 		final ImportExport ie = new ImportExport();
 		GraphViewData[] data = ie.ExportFromCsv(EXPORT_FILE); 
 		if(data == null)
 			return;
 		
 		layout.removeAllViews();
 		
 		graphView = new LineGraphView(this, ""){   
 			   @Override  
 			   protected String formatLabel(double value, boolean isValueX) {  
 			      if (isValueX) {
 			    	  int i = (int) value;
 			    	  return ie.getDateValue(i<=0?0:i-1);
 			    	  //return super.formatLabel(value, isValueX);   
 			      } else {
 			    	  double result = value * 10; 
 			    	  result = Math.round(result);
 			    	  result = result / 10;
 			          return ""+result; 
 			      }
 			   }  
 		};
 			
 		// add data  
 		graphView.addSeries(new GraphViewSeries(data));  
 		
 		if(viewportStart==0.0 && viewportSize==0.0){
 			if(data.length >= 50)
 				graphView.setViewPort(data.length-50, 50);   
 			else
 				graphView.setViewPort(0, data.length); 
 		}else{
 			graphView.setViewPort(viewportStart, viewportSize); 
 		}
 		
 		graphView.setScrollable(true);   
 		// optional - activate scaling / zooming  
 		graphView.setScalable(true);   
 		graphView.setManualYAxisBounds(ie.getMaxValue()+0.1, ie.getMinValue()-0.1);
 		//graphView.setHorizontalLabels(new String[] {"a", "b", "c"});
 		graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.WHITE); 
 		graphView.getGraphViewStyle().setVerticalLabelsColor(Color.WHITE);
  		
 		setLabelParams(graphView);		
 		 
 		layout.addView(graphView);  		
 	}
 	
 	private void populateDataFromDB(){
 		
 		DatabaseHandler db = new DatabaseHandler(this);
 		final int count = db.getWeightsCount();
 		if(count==0){
 			Toast.makeText(getApplicationContext(), R.string.db_empty, Toast.LENGTH_LONG).show();
 			return;
 		}
        
 		layout.removeAllViews();
 		
 		GraphViewData[] data = new GraphViewData[count];
 		List<WeightEntry> weights = db.getAllWeight();
 		final ArrayList<String> dateList = new ArrayList<String>();
 		double maxV = Double.MIN_VALUE;
 		double minV = Double.MAX_VALUE;
 		int i = 0;
 		for (WeightEntry w : weights) {
 			data[i] = new GraphViewData(w.getID(), Double.parseDouble(w.getWeight()));
 			maxV = Math.max(data[i].valueY, maxV);
 			minV = Math.min(data[i].valueY, minV);
 			dateList.add(w.getDate());
 			i++;
 		}
 		
 		graphView = new LineGraphView(this, ""){ 
 			   @Override  
 			   protected String formatLabel(double value, boolean isValueX) {  
 			      if (isValueX) {
 			    	  int i = (int) value;
 			    	  String s = dateList.get(i<=0?0:(i>count)?count-1:i-1);
 			    	  return s.substring(0,6)+s.substring(8);
 			      } else {
 			    	  double result = value * 10; 
 			    	  result = Math.round(result);
 			    	  result = result / 10;
 			          return ""+result; 
 			      }
 			   }  
 		};
 			
 		// add data  
 		graphView.addSeries(new GraphViewSeries(data)); 
 		
 		// TODO:
 		//if(viewportStart==0.0 && viewportSize==0.0){
 			if(data.length >= 50)
 				graphView.setViewPort(data.length-50, 50);   
 			else
 				graphView.setViewPort(0, data.length); 
 		//}else{
 		//	graphView.setViewPort(viewportStart, viewportSize);
 		//}
 		
 		graphView.setScrollable(true);   
 		// optional - activate scaling / zooming  
 		graphView.setScalable(true);   
 		graphView.setManualYAxisBounds(maxV+0.1, minV-0.1);
 		//graphView.setHorizontalLabels(new String[] {"a", "b", "c"});
 		graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.WHITE); 
 		graphView.getGraphViewStyle().setVerticalLabelsColor(Color.WHITE);
 		
 		setLabelParams(graphView);		
 		
 		layout.addView(graphView);  	
 		
 	}
 	
 	private void clearDB(){
 		
 		DatabaseHandler db = new DatabaseHandler(this);
 		if(db.getWeightsCount()==0){
 			Toast.makeText(getApplicationContext(), R.string.db_empty, Toast.LENGTH_LONG).show();
 			return;
 		}
 		
 		List<WeightEntry> weights = db.getAllWeight();
 		
 		for (WeightEntry w : weights) {
 			db.deleteWeight(w);
 		}
 		Toast.makeText(getApplicationContext(), R.string.db_clear, Toast.LENGTH_LONG).show();
 		return;
 	}
 	
 	private void testAddSomeWeights(){
 		DatabaseHandler db = new DatabaseHandler(this);
 		
 		db.addWeight(new WeightEntry("01.01.2013", "71.2"));
         db.addWeight(new WeightEntry("05.01.2013", "72.2"));
         db.addWeight(new WeightEntry("10.01.2013", "71.5"));
         db.addWeight(new WeightEntry("15.01.2013", "71.8"));
 	}
 	
 	private void setLabelParams(GraphView graphView){
 		DisplayMetrics metrics = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(metrics);
         int density = metrics.densityDpi;
 
         if (density==DisplayMetrics.DENSITY_HIGH) {
         	graphView.getGraphViewStyle().setLabelBorder(BASIC_BORDER + 5);
     		graphView.getGraphViewStyle().setLabelFontSize(BASIC_FONT_SIZE + 4);
     		graphView.getGraphViewStyle().setVerticalLabelWidth(BASIC_VLABEL_WIDTH+50);
         }
         else if (density==DisplayMetrics.DENSITY_MEDIUM) {
         	graphView.getGraphViewStyle().setLabelBorder(BASIC_BORDER);
     		graphView.getGraphViewStyle().setLabelFontSize(BASIC_FONT_SIZE);
     		graphView.getGraphViewStyle().setVerticalLabelWidth(BASIC_VLABEL_WIDTH);
         }
         else if (density==DisplayMetrics.DENSITY_LOW) {
         	graphView.getGraphViewStyle().setLabelBorder(BASIC_BORDER - 5);
     		graphView.getGraphViewStyle().setLabelFontSize(BASIC_FONT_SIZE - 4);
     		graphView.getGraphViewStyle().setVerticalLabelWidth(BASIC_VLABEL_WIDTH-50);
         }
         else if (density==DisplayMetrics.DENSITY_XHIGH) {
         	graphView.getGraphViewStyle().setLabelBorder(BASIC_BORDER*2);
     		graphView.getGraphViewStyle().setLabelFontSize(BASIC_FONT_SIZE*2);
     		graphView.getGraphViewStyle().setVerticalLabelWidth(BASIC_VLABEL_WIDTH*2);
         }
         else if (density==DisplayMetrics.DENSITY_XXHIGH) {
         	graphView.getGraphViewStyle().setLabelBorder(BASIC_BORDER*2 + 5);
     		graphView.getGraphViewStyle().setLabelFontSize(BASIC_FONT_SIZE*2 + 4);
     		graphView.getGraphViewStyle().setVerticalLabelWidth(BASIC_VLABEL_WIDTH*2+50);
         }
         else {
         	graphView.getGraphViewStyle().setLabelBorder(BASIC_BORDER);
     		graphView.getGraphViewStyle().setLabelFontSize(BASIC_FONT_SIZE);
     		graphView.getGraphViewStyle().setVerticalLabelWidth(BASIC_VLABEL_WIDTH);
         }
 	}
 
 }
