 package com.babyswipes;
 
 import java.text.SimpleDateFormat;
 
 import com.babyswipes.BabySwipesDB.BabySwipe;
 
 import android.os.Bundle;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.GridView;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.TextView;
 
 public class BabySwipes extends BaseActivity implements OnItemClickListener {
     private static final String TAG = "BabySwipes";
     ListView theList;
     TextView textNumTags;
     BabySwipesDB myDB;
     GridView theGrid;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_baby_swipes);
 
         theList = (ListView) findViewById(R.id.mainActivityListView);
         theList.setOnItemClickListener(this);
         
         myDB = new BabySwipesDB(getBaseContext());
         
         /*        
         myDB.clearAllData();
         
         myDB.addTagType("medicine");
         myDB.addTagType("feeding");
         myDB.addTagType("diaper");
         myDB.addTagType("nap");
 
         myDB.addSwipe("medicine", 1354077250);
         myDB.addSwipe("medicine", 1354067250);
         myDB.addSwipe("feeding", 1354057250);
         myDB.addSwipe("feeding", 1354047250);
         myDB.addSwipe("diaper", 1354037250);
         myDB.addSwipe("nap", 1354027250);
         */
         
         String text = "" + myDB.getNumberOfTags();
         textNumTags = (TextView) findViewById(R.id.tagNumText);
         textNumTags.setText(text);
         
     	String[] data = new String[10];
         BabySwipe recent[] = myDB.getRecentSwipes(5);
         
         if (recent != null) {
             SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMM d");
             SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm:ss a");
             for(int i=0; i<recent.length; ++i)
             {
             	data[(i*2)] 	= recent[i].tagName;
            	data[(i*2) + 1] = formatter.format(recent[i].swipeTime) + " at " + timeFormat.format(recent[i].swipeTime);
             }
             
             // Pad with empty data so it doesnt crash when adapter is set
             if(recent.length < 5){
             	for(int i = recent.length; i < 5; ++i){
                 	data[(i*2)] 	= "";
                 	data[(i*2) + 1] = "";
             	}
             }
             
             ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
     				android.R.layout.simple_list_item_1, data);
 
             theGrid = (GridView) findViewById(R.id.mainGrid);
             theGrid.setNumColumns(2);
             theGrid.setAdapter(adapter);
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_baby_swipes, menu);
         return true;
     }
     
     //@Override
     public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
         Intent i = null;
         Log.d(TAG, pos + " selected");
         switch(pos) {
         case 0:
             i = new Intent(this, TrainingActivity.class);  
             break;
         case 1:
             i = new Intent(this, DataActivity.class);
             break;   
         case 2:
             i = new Intent(this, ManualEntry.class);
             break;  
         }
         if (i != null) 
             startActivity(i);
     }
 
     
 }
