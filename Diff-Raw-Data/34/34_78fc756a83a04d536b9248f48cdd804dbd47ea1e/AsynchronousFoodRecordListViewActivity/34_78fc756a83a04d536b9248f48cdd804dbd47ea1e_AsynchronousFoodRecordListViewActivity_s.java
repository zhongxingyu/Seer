 package com.chips;
 
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.chips.dataclient.FoodClient;
 import com.chips.datarecord.FoodRecord;
 
 public abstract class AsynchronousFoodRecordListViewActivity 
         extends Activity implements Observer {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         client = new FoodClient();
     }
     
     @Override
     protected void onPause() {
         super.onPause();
         client.deleteObserver(this);
     }
     
     @Override
     protected void onResume() {
         super.onResume();
         client.addObserver(this);
         update(null,null);
         client.refreshClient();
     }
     
     public void loadFoundItems() {
         ListView foundItemsView = getListView();
         
         recordAdapter = new ArrayAdapter<FoodRecord>(this,
                 android.R.layout.simple_list_item_1, 
                     client.getFoodRecords());
         
         foundItemsView.setAdapter(recordAdapter);
     }
     
     public void update(Observable dataClient, Object data) {
         recordAdapter.notifyDataSetChanged();
         
         List<?> list = client.getFoodRecords();
        if (list.size() == 0) {
             Toast.makeText(this, 
                     "No items found", 
                     Toast.LENGTH_SHORT).show();
         }
     }
     
     protected abstract ListView getListView();
     
     private ArrayAdapter<FoodRecord> recordAdapter;
     protected FoodClient client;
 }
