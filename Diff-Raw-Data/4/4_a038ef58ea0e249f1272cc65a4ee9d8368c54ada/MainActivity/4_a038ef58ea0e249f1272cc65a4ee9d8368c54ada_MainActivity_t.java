 package me.ingeniando.conditionalalarm;
 
 import java.util.List;
 import java.util.Random;
 import java.util.ArrayList;
 
 import android.util.Log;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.widget.Button;
 
 public class MainActivity extends ListActivity {
 
 	private Alarm alarm_to_delete = new Alarm();
 	private AlarmsDataSource datasource;
 	private ArrayAdapter<Alarm> alarmsAdapter;
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.activity_main);
 
     final Context context = this;
     datasource = new AlarmsDataSource(this);
     datasource.open();
 
     ArrayList<Alarm> alarmList = datasource.getAllAlarms();
 
     // Use the SimpleCursorAdapter to show the elements in a ListView
     alarmsAdapter = new ArrayAdapter<Alarm>(this,
         android.R.layout.simple_list_item_1, alarmList);
     //Log.w("alarmsAdapter: ", alarmsAdapter.toString());
     setListAdapter(alarmsAdapter);
     
     // TO TEST THE ALARMS!
     // Save the new alarm to the database
     //Alarm alarm = new Alarm();
     //alarm = datasource.createAlarm("Test text", 07, 15, 1, 1);
     //alarmsAdapter.add(alarm);
     ListView lv = getListView();
     //Log.w("on: ", "listview created");
     lv.setOnItemLongClickListener(new OnItemLongClickListener(){
 	    @Override
 	    public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int row, long arg3) {
 	    	//Log.w("on: ", "enter!!!");
 	    	if (getListAdapter().getCount() > 0) {
 	            //Log.w("row: ", ""+row + " " +arg3);
 	            alarm_to_delete = (Alarm) getListAdapter().getItem(row);
 	    		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
 	    		// set title
 	    		alertDialogBuilder.setTitle(alarm_to_delete.toString());
 	     	    // set dialog message
 	    		alertDialogBuilder
 	    			.setMessage("Do you want to delete it?")
 	    			.setCancelable(false)
 	    			.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
 	    				public void onClick(DialogInterface dialog,int id) {
 	    					datasource.deleteAlarm(alarm_to_delete);
 	    		            alarmsAdapter.remove(alarm_to_delete);
 	    				}
 	    			})
 	    			.setNegativeButton("No",new DialogInterface.OnClickListener() {
 	    				public void onClick(DialogInterface dialog,int id) {
 	    					dialog.cancel();
 	    				}
 	    			});
 	    			// create alert dialog
 	    			AlertDialog alertDialog = alertDialogBuilder.create();
 	     	    	// show it
 	    			alertDialog.show();
 	          }
 	       //Log.w("onItemLongClickListener: ", "enter again!!");
 	       alarmsAdapter.notifyDataSetChanged();
 	       return true;
        }});
   
   }
   
   
   @Override
   protected void onListItemClick(ListView l, View v, int position, long id) {	
 	  super.onListItemClick(l, v, position, id);
 	  Alarm alarm = alarmsAdapter.getItem(position);
 	  long alarmId = alarm.getId();
 	  /*Log.w("onListemItemClick: ", alarm.toString());
 	  Toast.makeText(getApplicationContext(), 
               "Alarm Id: " + alarmId + "", Toast.LENGTH_LONG).show();*/
 	  Intent i = new Intent(getApplicationContext(), ViewSingleAlarmActivity.class);
       // sending data to new activity
 	  String alarmIdString = ""+alarmId;
       i.putExtra("alarmId", alarmIdString);
       startActivity(i);
 	}
   
   // Will be called via the onClick attribute
   // of the buttons in main.xml
   public void onClick(View view) {
     @SuppressWarnings("unchecked")
     ArrayAdapter<Alarm> adapter = (ArrayAdapter<Alarm>) getListAdapter();
     Alarm alarm = null;
     switch (view.getId()) {
     case R.id.add_alarm:
   	  Intent i = new Intent(getApplicationContext(), CreateAlarmActivity.class);
       // sending data to new activity
 	  //String alarmIdString = ""+alarmId;
       i.putExtra("alarmId", "TEST");
       startActivity(i);
       //String[] alarms = new String[] { "07:55", "08:30", "09:00" };
       //int nextInt = new Random().nextInt(3);
       // Save the new alarm to the database
       //alarm = datasource.createAlarm(alarms[nextInt]);
       //adapter.add(alarm);
       break;
     case R.id.delete_alarm:
       if (getListAdapter().getCount() > 0) {
         alarm = (Alarm) getListAdapter().getItem(0);
         datasource.deleteAlarm(alarm);
         adapter.remove(alarm);
       }
       break;
     }
   }
 
   @Override
   protected void onResume() {
     datasource.open();
     Alarm alarm = new Alarm();
     try {
     	alarm = datasource.getLastAlarmById();
     	Alarm last_alarm = new Alarm();
     	//Log.w("New alarm ID: ", alarm.getId() + "");
     	if (getListAdapter().getCount() > 0) {
    		last_alarm = (Alarm) getListAdapter().getItem((getListAdapter().getCount() - 1));
    		Log.w("ITEMS: ", "" + (getListAdapter().getCount() - 1));
     		if (alarm.getId() != last_alarm.getId()) {
     			alarmsAdapter.add(alarm);
     			alarmsAdapter.notifyDataSetChanged();
     		}	
     	} else {
     		alarmsAdapter.add(alarm);
 			alarmsAdapter.notifyDataSetChanged();
     	}
     } catch (Exception e){
     	Log.w("onResume: ", "NO ALARMS ALREADY");
     }
     super.onResume();
   }
 
   @Override
   protected void onPause() {
     datasource.close();
     super.onPause();
   }
 
 } 
