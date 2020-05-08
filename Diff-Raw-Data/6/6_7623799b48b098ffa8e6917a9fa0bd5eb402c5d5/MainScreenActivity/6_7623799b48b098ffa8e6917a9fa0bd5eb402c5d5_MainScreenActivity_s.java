 package edu.SMU.PingItApp;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 
 public class MainScreenActivity extends Activity {
 
     private static final String tag = "MainScreenActivity";
     private static final int REGISTRATION_REQUEST_CODE = 1000;
     private static final int DAYS_UNTIL_BATTERY_WARNING = 2;
     private DeviceDatabase db;
     private ArrayAdapter<UserTagInfo> adapter;
 
     MenuItem flashlightButton;
 
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
         flashlightButton = menu.findItem(R.id.toggle_flashlight);
         return super.onCreateOptionsMenu(menu);
     }
 
     public void initializeListViewAdapter() {
 
         adapter = new SelectionListArrayAdapter(this, R.layout.item_selection_list_row, new LinkedList<UserTagInfo>());
 
         ListView listView = (ListView)findViewById(R.id.main_screen_item_list_view);
         listView.setAdapter(adapter);
 
         //This listener will start the activity to ping for your stuff
         listView.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 UserTagInfo row = (UserTagInfo)parent.getItemAtPosition(position);
                 onItemSelect(row);
             }
         });
         registerForContextMenu(listView);
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
                 Log.d(tag, "Registration was cancelled");
             }
         }
     }
 
     public void updateListView() {
 
         //Pull all the registered tags, and create new item rows for each one
         List<UserTagInfo> allTags = db.getAllTags();
 
         //Add it to our home screen list
         adapter.clear();
         adapter.addAll(allTags);
         examineTagsForBatteryWarnings(allTags);
 
     }
 
     private void examineTagsForBatteryWarnings(List<UserTagInfo> allTags) {
         for (UserTagInfo tagInfo : allTags) {
             Date registrationDate = tagInfo.getRegistrationDate();
             if (registrationDate != null) {
 
                 Date currentDate = new Date();
                 long differenceInMillis = currentDate.getTime() - registrationDate.getTime();
                 Log.d(tag, "Difference in milliseconds: " + differenceInMillis);
                 long diffDays = differenceInMillis / (24 * 60 * 60 * 1000);
                 Log.d(tag, "Difference in days: " + diffDays);
                 if (diffDays >= DAYS_UNTIL_BATTERY_WARNING) {
                     showBatteryWarningAlertDialog(tagInfo, diffDays);
                     break;
                 }
             }
         }
     }
 
     private void showBatteryWarningAlertDialog(UserTagInfo tagInfo, long days) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         String message = "Warning: The tag on " + tagInfo.getTagName();
         message += " was registered " + days + " days ago. ";
         message += "Please consider replacing the tag";
         builder.setMessage(message);
         final UserTagInfo finalTagInfo = tagInfo;
 
         builder.setNegativeButton(R.string.battery_warning_first_button, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int id) {
                 dialogInterface.cancel();
             }
         });
 
         builder.setNeutralButton(R.string.battery_warning_second_button, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
                 dialogInterface.cancel();
             }
         });
 
         builder.setPositiveButton(R.string.battery_warning_third_button, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
                 db.updateTagRegistrationDate(finalTagInfo);
             }
         });
 
         AlertDialog alertDialog = builder.create();
         alertDialog.show();
     }
 
     public void onItemSelect(UserTagInfo row) {
         String viewText = row.getTagName();
         Log.d(tag, "Picked tag " + viewText);
 
         Intent intent = new Intent(this, FindTagActivity.class);
         intent.putExtra("TagName", viewText);
         startActivity(intent);
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View view,
                                     ContextMenu.ContextMenuInfo menuInfo) {
         if(view.getId()==R.id.main_screen_item_list_view) {
             AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
             menu.setHeaderTitle(adapter.getItem(info.position).getTagName());
             String[] menuItems = {"Delete"};
             for(int i = 0; i < menuItems.length; i++){
                 menu.add(Menu.NONE, i, i, menuItems[i]);
             }
         }
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item){
         AdapterView.AdapterContextMenuInfo info =
                 (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 
         int menuItemIndex = item.getItemId();
 
         if(menuItemIndex == 0) {
             UserTagInfo tagInfo = adapter.getItem(info.position);
             db.deleteTag(tagInfo);
             updateListView();
         }
         return true;
     }
 
     public void toggleFlashlight(MenuItem item) {
         FlashlightController.toggleFlashlight(flashlightButton, this);
     }
 
     public void goToAboutPage(MenuItem item) {
         Intent intent = new Intent(this, AboutPageActivity.class);
         startActivity(intent);
     }
 
     public void goToBuyingPage(MenuItem item) {
         Intent intent = new Intent(this, WhereToBuyActivity.class);
         startActivity(intent);
     }
 }
