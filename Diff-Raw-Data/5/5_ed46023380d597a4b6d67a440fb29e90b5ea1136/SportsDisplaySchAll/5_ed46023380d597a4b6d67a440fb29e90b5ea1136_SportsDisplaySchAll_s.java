 package org.umece.android.umaine.sports;
 
 import java.util.List;
 
 import org.umece.android.umaine.R;
 import org.umece.android.umaine.R.id;
 import org.umece.android.umaine.R.layout;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class SportsDisplaySchAll extends Activity {
 
	SportsGrabData sgd;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.sprots_activity_sch_all);
 
 		/* Find Tablelayout defined in myTableLayout.xml */
 		TableLayout tl = (TableLayout) findViewById(R.id.myTableLayout);
 
 //		String[] testData = { "hello1", "hello2", "hello3", "hello4", "hello5","asdf","sadfa","dasfalsfje","143243","adfsa0832","dafadsfadsfoiehfoihfage","dafewfj;eljfa","000","10" };
 		
 		List<String>retval = sgd.postEventType("all");
 		
 		int current = 0;
 		for (String s : retval) {
 			current++;
 			
 			String [] row = s.split(";");
 
 			sgd.setPostDate(row[0]);
 			sgd.setPostEvent(row[1]);
 			sgd.setPostLocation(row[2]);
 			sgd.setPostTime(row[3]);
 /*		}
 		
 		//  
 		for (int current = 0; current < sgd.getRow().length; current++) {*/
 			// Create a TableRow and give it an ID
 			TableRow tr = new TableRow(this);
 			tr.setId(current);
 			tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
 					LayoutParams.FILL_PARENT));
 
 			// Create a TextView to house the name of the province
 			TextView labelDate = new TextView(this);
 			labelDate.setId(100 + current);
 			labelDate.setText(sgd.getPostDate());
 			labelDate.setTextColor(Color.WHITE);
 			labelDate.setLayoutParams(new LayoutParams(
 					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 			tr.addView(labelDate);
 
 			// Create a TextView to house the value of the after-tax income
 			TextView LableEvent = new TextView(this);
 			LableEvent.setId(200+current);
 			LableEvent.setText(sgd.getPostEvent());
 			LableEvent.setTextColor(Color.WHITE);
 			LableEvent.setLayoutParams(new LayoutParams(
 					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 			tr.addView(LableEvent);
 
 			// Create a TextView to house the name of the province
 			TextView labelLocation = new TextView(this);
 			labelLocation.setId(300 + current);
 			labelLocation.setText(sgd.getPostLocation());
 			labelLocation.setTextColor(Color.WHITE);
 			labelLocation.setLayoutParams(new LayoutParams(
 					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 			tr.addView(labelLocation);
 
 			// Create a TextView to house the value of the after-tax income
 			TextView labelTime = new TextView(this);
 			labelTime.setId(400+current);
 			labelTime.setText(sgd.getPostTime());
 			labelTime.setTextColor(Color.WHITE);
 			labelTime.setLayoutParams(new LayoutParams(
 					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 			tr.addView(labelTime);
 
 			// Add the TableRow to the TableLayout
 			tl.addView(tr, new TableLayout.LayoutParams(
 					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 		}
 	}
 
 }
