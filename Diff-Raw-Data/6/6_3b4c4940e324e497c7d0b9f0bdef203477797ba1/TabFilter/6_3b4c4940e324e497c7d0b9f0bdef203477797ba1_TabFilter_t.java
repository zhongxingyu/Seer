 package com.underdusken.kulturekalendar.ui.fragments;
 
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.support.v4.view.MenuItemCompat;
 import android.support.v7.widget.SearchView;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.TextView;
 
 import com.underdusken.kulturekalendar.R;
 import com.underdusken.kulturekalendar.data.EventItem;
 import com.underdusken.kulturekalendar.data.db.DatabaseManager;
 import com.underdusken.kulturekalendar.mainhandler.BroadcastNames;
 import com.underdusken.kulturekalendar.ui.activities.EventsDescription;
 import com.underdusken.kulturekalendar.ui.adapters.AdapterEventsItem;
 import com.underdusken.kulturekalendar.ui.receivers.NotificationUpdateReceiver;
 import com.underdusken.kulturekalendar.utils.ToDo;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
 
 public class TabFilter extends Fragment implements SearchView.OnQueryTextListener {
 
     private static final String TAG = "TabFilter";
     // private receivers
     private NotificationUpdateReceiver notificationUpdateReceiver = null;
 
     // Items for list
     private AdapterEventsItem adapterEventsItem = null;
     private List<EventItem> eventItemList = new ArrayList<EventItem>();
     private List<EventItem> filterEventItem = new ArrayList<EventItem>();
 
     private TextView emptyList;
 
     // ui
     private StickyListHeadersListView lvEvents = null;
 
     //data
     private long lastEventsId = -1;      // for getting only new events
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         // Inflate the layout for this fragment
         return inflater.inflate(R.layout.tab_filter, container, false);
 
     }
 
 
     @Override
     public void onStart() {
         super.onStart();
         loadEventsFromDb();
         if (eventItemList.size() <= 0) {
             Log.d(TAG, eventItemList.size() + "");
             emptyList.setVisibility(View.VISIBLE);
         } else {
             emptyList.setVisibility(View.GONE);
         }
         updateFilter("");
         updateView();
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         lvEvents = (StickyListHeadersListView) getActivity().findViewById(R.id.list_events_filter);
         emptyList = (TextView) getActivity().findViewById(R.id.text_no_events_msg);
 
         notificationUpdateReceiver = new NotificationUpdateReceiver(new Handler(), new ToDo() {
             @Override
             public void doSomething() {
                 loadEventsFromDb();
                 updateFilter("");
                 updateView();
             }
         });
 
         adapterEventsItem = new AdapterEventsItem(this.getActivity(), 0, filterEventItem);
         lvEvents.setAdapter(adapterEventsItem);
 
         lvEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 Intent intent = new Intent(TabFilter.this.getActivity(), EventsDescription.class);
                 intent.putExtra("events_id", filterEventItem.get(i).getId());
                 startActivityForResult(intent, 1);
             }
         });
 
     }
 
     @Override
     public void onResume() {
         super.onResume();
         // Reciever for update notifications
         IntentFilter intentFilterNotificationUpdate = new IntentFilter(BroadcastNames.BROADCAST_NEW_DATA);
         getActivity().registerReceiver(notificationUpdateReceiver, intentFilterNotificationUpdate);
     }
 
    @Override
    public void onPause() {
        super.onPause();
        getActivity().unRegisterReceiver(notificationUpdateReceiver);
    }

     /**
      * callback update search information
      */
     private TextWatcher filterTextWatcher = new TextWatcher() {
 
         @Override
         public void onTextChanged(CharSequence s, int start, int before, int count) {
         }
 
         @Override
         public void beforeTextChanged(CharSequence s, int start, int count, int after) {
         }
 
         @Override
         public void afterTextChanged(Editable s) {
             updateFilter("");
             updateView();
         }
     };
 
     private void updateFilter(String searchText) {
         filterEventItem.clear();
         if (eventItemList != null) {
             SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
             Set<String> categories = prefs.getStringSet("categories", null);
             boolean filterAge = prefs.getBoolean("filter_age", false);
             int age = 0;
             if (filterAge) {
                 age = Integer.parseInt(prefs.getString("age", "99"));
             }
             boolean freeOnly = prefs.getBoolean("filter_free", false);
 
             for (EventItem eventItem : eventItemList) {
                 // Finner alle muligheter for at eventen IKKE skal med:
                 if (categories != null && !categories.contains(eventItem.getCategoryID().toLowerCase())) {
                     continue;
                 }
                 if (filterAge && age < eventItem.getAgeLimit()) {
                     continue;
                 }
                 if (freeOnly && eventItem.getPrice() > 0) {
                     continue;
                 }
                 if (!eventItem.getTitle().toLowerCase().contains(searchText.toLowerCase())) {
                     continue;
                 }
                 filterEventItem.add(eventItem);
             }
         }
     }
 
     private void loadEventsFromDb() {
         DatabaseManager databaseManager = new DatabaseManager(getActivity());
         try {
             databaseManager.open();
             List<EventItem> newEventItemList = databaseManager.getAllFutureEventsItem();
             databaseManager.close();
 
             if (newEventItemList == null) {
                 return;
             }
             if (newEventItemList.size() <= 0) {
                 return;
             }
 
             eventItemList.clear();
             for (EventItem eventItem : newEventItemList) {
                 eventItemList.add(eventItem);
             }
             eventItemList = DatabaseManager.sortEventsByDate(eventItemList);
 
         } catch (SQLException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     // update view
     private void updateView() {
         adapterEventsItem.notifyDataSetChanged();
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.filter, menu);
         inflater.inflate(R.menu.actionbar_menu, menu);
 
         MenuItem searchItem = menu.findItem(R.id.action_search);
         SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
         if (searchView != null) {
             searchView.setQueryHint(getString(R.string.search_current_tab));
             searchView.setOnQueryTextListener(this);
         }
         super.onCreateOptionsMenu(menu, inflater);
     }
 
     @Override
     public boolean onQueryTextSubmit(String s) {
         return false;
     }
 
     @Override
     public boolean onQueryTextChange(String s) {
         updateFilter(s.toLowerCase());
         updateView();
         return false;
     }
 }
