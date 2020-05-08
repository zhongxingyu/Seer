 package com.chips.dataclientobservers;
 
 import java.util.List;
 import java.util.Observable;
 
 import android.app.Activity;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ExpandableListView;
 import android.widget.Toast;
 
 import com.chips.dataclient.FoodClient;
 
 public class ExpandableFoodClientObserver extends DataClientObserver {
     public ExpandableFoodClientObserver(Activity parentActivity, 
             FoodClient newClient) {
         super(parentActivity);
         client = newClient;
     }
     
     public void setListViewLayout(ExpandableListView destinationView, 
             BaseExpandableListAdapter newAdapter) {
         recordAdapter = newAdapter;
         
         destinationView.setAdapter(recordAdapter);
     }
     
     public void update(Observable dataClient, Object data) {
         recordAdapter.notifyDataSetChanged();
         
         List<?> list = client.getFoodRecords();
         if (list.size() == 0 && client.hasLoadedOnce()) {
             Toast.makeText(parentActivity, 
                     "No items found", 
                     Toast.LENGTH_SHORT).show();
         }
     }
     
     private BaseExpandableListAdapter recordAdapter;
     protected FoodClient client;
 }
