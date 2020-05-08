 package edu.SMU.PingItApp;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import java.util.LinkedList;
 import java.util.List;
 
 
 public class MainScreenActivity extends Activity {
 
     private static final String tag = "MainScreenActivity";
     private static final int REGISTRATION_REQUEST_CODE = 1000;
     private DeviceDatabase db;
     private ArrayAdapter<UserTagInfo> adapter;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main_screen);
 
         //Setup the list view on the home page
         db = new DeviceDatabase(this);
         initializeListViewAdapter();
         updateListView();
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_screen, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     public void initializeListViewAdapter() {
 
         adapter = new MainScreenListArrayAdapter(this, R.layout.item_selection_list_row, new LinkedList<UserTagInfo>());
 
         ListView listView = (ListView)findViewById(R.id.main_screen_item_list_view);
         listView.setAdapter(adapter);
 
         //This listener will start the activity to ping for your stuff
         listView.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ItemListRow row = (ItemListRow)parent.getItemAtPosition(position);
                 onItemSelect(row);
             }
         });
     }
 
     public void addNewItem(MenuItem item) {
 
         Log.d(tag, "Starting the Registration activity");
         Intent intent = new Intent(this, NewItemRegistrationActivity.class);
         startActivityForResult(intent, REGISTRATION_REQUEST_CODE);
     }
 
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == REGISTRATION_REQUEST_CODE) {
 
             //Check to see if registration was successful
             if (resultCode == NewItemRegistrationActivity.REGISTRATION_RESPONSE_SUCCESS) {
 
                 //If it was, update the list on the home page
                 String registeredName = data.getStringExtra(getString(R.string.registration_result_name));
                 Toast.makeText(this, "Successfully registered " + registeredName, Toast.LENGTH_SHORT).show();
                 updateListView();
             } else {
                 Toast.makeText(this, "Registration cancelled", Toast.LENGTH_SHORT).show();
             }
         }
     }
 
     public void updateListView() {
 
         //Pull all the registered tags, and create new item rows for each one
         List<UserTagInfo> allTags = db.getAllTags();
 
         //Add it to our home screen list
         adapter.clear();
         adapter.addAll(allTags);
 
     }
 
    public void onItemSelect(ItemListRow row) {
         String viewText = row.getTagName();
         Log.d(tag, "Picked tag " + viewText);
 
         Intent intent = new Intent(this, FindTagActivity.class);
         startActivity(intent);
     }
 }
