 package com.rojel.gsgfreiberg;
 
 import java.util.ArrayList;
 
 import org.jsoup.nodes.Document;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.*;
 
 public class GSGFreibergActivity extends Activity implements OnClickListener {
     public static final int FILTER_REQUEST = 1;
 	
 	public Schedule schedule;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         Document page = HTMLHandler.downloadPage(getString(R.string.page_url));
         this.schedule = HTMLHandler.parse(page);
         
         updateList(schedule.getByClass(""));
     }
 	
 	public void updateList(ArrayList<Lesson> lessons) {
         setContentView(R.layout.schedule);
         TableLayout table = (TableLayout) findViewById(R.id.table);
         
 		for(Lesson cancel : lessons) {
 	        TextView date = new TextView(this);
 	        date.setPadding(0, 0, 50, 10);
 	        date.setText(cancel.date);
 	        
 	        TextView classname = new TextView(this);
 	        classname.setPadding(0, 0, 50, 10);
 	        classname.setText(cancel.classname);
 	        
 	        TextView lesson = new TextView(this);
 	        lesson.setPadding(0, 0, 50, 10);
 	        lesson.setText(cancel.lesson);
 	        
 	        Button more = new Button(this);
 	        more.setWidth(100);
 	        more.setPadding(0, 0, 0, 10);
 	        more.setText(R.string.details);
 	        more.setOnClickListener(this);
 	        
 	        TableRow row = new TableRow(this);
 	        
 	        row.addView(date);
 	        row.addView(classname);
 	        row.addView(lesson);
 	        row.addView(more);
 	        
 	        table.addView(row);
         }
 	}
 
 	public void onClick(View v) {
 		if(v instanceof Button) {
 			int index;
 			
 			TableRow viewRow = (TableRow) v.getParent();
 			TableLayout rowTable = (TableLayout) viewRow.getParent();
 			
 			index = rowTable.indexOfChild(viewRow);
			Lesson clickedLesson = schedule.get(index);
 			Intent intent = new Intent(this, DetailsActivity.class);
 			intent.putExtra("lesson", clickedLesson);
 			startActivity(intent);
 		} else if(v instanceof TextView) {
 			
 		}
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		
 		return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 			case R.id.filter:
 				System.out.println("Pressed filter button");
 				startActivityForResult(new Intent(this, FilterActivity.class), FILTER_REQUEST);
 				
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if(requestCode == FILTER_REQUEST) {
 			if(resultCode == RESULT_OK) {
 				System.out.println("Got the following text from filter activity: " + data.getStringExtra("classname"));
 				updateList(schedule.getByClass(data.getStringExtra("classname")));
 			} else if(resultCode == RESULT_CANCELED) {
 				System.out.println("Aborted filter activity.");
 			}
 		}
 	}
 }
