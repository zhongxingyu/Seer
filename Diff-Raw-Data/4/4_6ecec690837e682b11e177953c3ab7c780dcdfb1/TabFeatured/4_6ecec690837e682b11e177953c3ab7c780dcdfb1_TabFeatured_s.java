 package com.underdusken.kulturekalendar.ui.fragments;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 
 import com.underdusken.kulturekalendar.R;
 import com.underdusken.kulturekalendar.data.EventItem;
 import com.underdusken.kulturekalendar.data.db.DatabaseManager;
 import com.underdusken.kulturekalendar.mainhandler.BroadcastNames;
 import com.underdusken.kulturekalendar.ui.activities.EventsDescription;
 import com.underdusken.kulturekalendar.ui.adapters.AdapterEventsItem;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class TabFeatured extends Fragment {
 
     private static final String TAG = "TabFeatured";
     // UI handlers
     private Handler activityHandler = null;
     // private recievers
     private NotificationUpdateReciever notificationUpdateReciever = new NotificationUpdateReciever();
 
     private AdapterEventsItem adapterEventsItem = null;
     private List<EventItem> eventItemList = new ArrayList<EventItem>();
     private ListView eventList;
     private ProgressBar progressBar;
 
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         // Inflate the layout for this fragment
         Log.d(TAG, "onCreateView");
         return inflater.inflate(R.layout.featured, container, false);
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         eventList = (ListView) getActivity().findViewById(R.id.list_events_featured);
         progressBar = (ProgressBar) getActivity().findViewById(R.id.featured_progress);
 
         activityHandler = new Handler();
 
         adapterEventsItem = new AdapterEventsItem(this.getActivity(), 0, eventItemList);
         eventList.setAdapter(adapterEventsItem);
 
         eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 Intent intent = new Intent(TabFeatured.this.getActivity(), EventsDescription.class);
                 intent.putExtra("events_id", eventItemList.get(i).getId());
                 startActivityForResult(intent, 1);
             }
         });
     }
 
     @Override
     public void onStart() {
         super.onStart();
         new DatabaseTask().execute();
         updateView();
     }
 
     @Override
     public void onResume() {
         super.onResume();
         IntentFilter intentFilterNotificationUpdate = new IntentFilter(BroadcastNames.BROADCAST_NEW_DATA);
         getActivity().registerReceiver(notificationUpdateReciever, intentFilterNotificationUpdate);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         getActivity().unregisterReceiver(notificationUpdateReciever);
     }
 
     private void updateView() {
         adapterEventsItem.notifyDataSetChanged();
         if (eventItemList.size() <= 0) {
             progressBar.setVisibility(View.VISIBLE);
         } else {
             progressBar.setVisibility(View.GONE);
         }
         Log.d(TAG, "View is updated");
     }
 
 
     class NotificationUpdateReciever extends BroadcastReceiver {
         @Override
         public void onReceive(Context context, Intent arg1) {
             activityHandler.post(new Runnable() {
                 @Override
                 public void run() {
                     new DatabaseTask().execute();
                     updateView();
                 }
             });
         }
     }
 
     private class DatabaseTask extends AsyncTask<Void, Void, List<EventItem>> {
 
         @Override
         protected void onPostExecute(List<EventItem> eventItems) {
             super.onPostExecute(eventItems);
             if (eventItems == null) {
                 Log.w(TAG, "Failed to update. EventItems is null.");
                 return;
             }
             Log.d(TAG, "onPostExecute");
             adapterEventsItem.clear();
            adapterEventsItem.addAll(eventItems);
             updateView();
         }
 
         @Override
         protected List<EventItem> doInBackground(Void... params) {
             Log.d(TAG, "Starting doInBackground");
             List<EventItem> list = new ArrayList<EventItem>();
             DatabaseManager databaseManager = new DatabaseManager(getActivity());
             try {
                 databaseManager.open();
                 List<EventItem> newEventItemList = databaseManager.getAllFutureEventsItem();
                 databaseManager.close();
                 if (newEventItemList == null) {
                     throw new IllegalStateException("DatabaseManager.getAllFutureEventsItem returned null.");
                 }
                 if (newEventItemList.size() <= 0) {
                     return null;
                 }
                 for (EventItem eventItem : newEventItemList) {
                     if (eventItem.getIsRecommended()) {
                         list.add(eventItem);
                     }
                 }
                 DatabaseManager.sortEventsByDate(list);
             } catch (SQLException e) {
                 e.printStackTrace();
             }
             return list;
         }
     }
 
 
 }
