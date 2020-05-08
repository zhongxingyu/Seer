 package gov.cdc;
 
 //Author: Paul Brown
 //Originally created on 2013-01-29
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.ArrayAdapter;
 import android.widget.AdapterView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.content.Intent;
 
 
 public class HistoryView extends Activity {
 	
 	private ArrayList<PTTHistoryItem> mHistory;
 	private ArrayAdapter<String> arrayAdapter;
 	
 	SectionedAdapter adapter;
 	
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         
         adapter = new SectionedAdapter() {
 
 			@Override
 			protected View getHeaderView(String caption, int index, View convertView, ViewGroup parent) {
 				convertView = getLayoutInflater().inflate(R.layout.section_header, null);
 				TextView header = (TextView) convertView.findViewById(R.id.completedStepsTextView);
 				header.setText(caption);
 				return convertView;
 			}
 		};
         
         
         
      // Make the application Full-Screen
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         
         setContentView(R.layout.activity_history_view);
 
         final Button doneButton = (Button)findViewById(R.id.doneButton);
         doneButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 //startActivity(new Intent(MyActivity.this,ActivityEins.class));
             	finish();
             }
         });
         
         final ListView myListView = (ListView)findViewById(R.id.my_list_view);
         
         
         // Create the adapter for the completed steps
         final HistoryCompletedAdapter historyItemAdapter = new HistoryCompletedAdapter(this, MainView.mHistory);
         
         // Create the adapter for the current/recommendation step
        // The HistoryCurrentAdapter automatically puts in the info for the current node
         final HistoryCurrentAdapter historyItemAdapter2 = new HistoryCurrentAdapter(this, 0);
         
         
         adapter.addSection("Completed Steps", historyItemAdapter);
        
        String sectionHeaderText = "";
        if (MainView.controller.currentNode.getAnswers().size() == 0) {
        	sectionHeaderText = "Recommendation";
        }
        else {
        	sectionHeaderText = "Current Step";
        }
        adapter.addSection(sectionHeaderText, historyItemAdapter2);
         
         //bind adapter to the AdapterView
         //myListView.setAdapter(historyItemAdapter);
         myListView.setAdapter(adapter);
         //myListView.setAdapter(arrayAdapter);
         
         myListView.setOnItemClickListener(new OnItemClickListener() {
         	
         	@Override
         	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         		Intent i = getIntent();
        		Log.d("TOUCH","touched cell at position: "+Integer.toString(position));
        		Log.d("mHistory.size",Integer.toString(MainView.mHistory.size()));
        		if (position == MainView.mHistory.size()+2) {
        			Log.d("finish","finished with a 2");
        			setResult(2);
        			finish();
        		}
        		else {
        			Log.d("finish","finished with a 1");
	        		// the -1 is to account for the section header. item formerly at 0 is now 1, and needs to point to 0
	        		i.putExtra("nodeToNavigateTo", position-1);
	        		setResult(1,i);
	        		finish();
        		}
         	}
         });
         
         
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_history_view, menu);
         return true;
     }
 }
