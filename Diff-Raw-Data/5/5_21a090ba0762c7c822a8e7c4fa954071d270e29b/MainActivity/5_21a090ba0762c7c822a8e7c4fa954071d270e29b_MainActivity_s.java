 package com.github.alexesprit.noisefmtor.activity;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.Toast;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.Window;
 import com.actionbarsherlock.widget.SearchView;
 import com.github.alexesprit.noisefmtor.R;
 import com.github.alexesprit.noisefmtor.adapter.TrackAdapter;
 import com.github.alexesprit.noisefmtor.order.OrdersTable;
 import com.github.alexesprit.noisefmtor.order.Track;
 
 import java.util.ArrayList;
 
 
 public final class MainActivity extends SherlockActivity {
     private TrackAdapter trackAdapter;
     private OrdersTable ordersTable;
     private TrackListLoadTask trackListLoadTask;
     private TrackOrderTask trackOrderTask;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(R.layout.track_list_view);
 
         ordersTable = new OrdersTable();
         trackAdapter = new TrackAdapter(this);
         ListView trackListView = (ListView)findViewById(R.id.track_list);
         trackListView.setAdapter(trackAdapter);
         trackListView.setEmptyView(findViewById(R.id.track_list_list_empty_view));
         trackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                 Track track = trackAdapter.getItem(pos);
                 startTrackOrderTask(track);
             }
         });
         updateTitle();
         updateView(null);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getSupportMenuInflater().inflate(R.menu.track_list_view_menu, menu);
         final MenuItem searchMenuItem = menu.findItem(R.id.menu_search_music);
         SearchView searchView = (SearchView)searchMenuItem.getActionView();
         searchView.setQueryHint(getText(R.string.search_hint));
         searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
             @Override
             public boolean onQueryTextSubmit(String query) {
                 searchMenuItem.collapseActionView();
                 updateView(query);
                 return true;
             }
 
             @Override
             public boolean onQueryTextChange(String s) {
                 return false;
             }
         });
         menu.findItem(R.id.menu_next_page).setEnabled(ordersTable.isForwardingAllowed());
         menu.findItem(R.id.menu_prev_page).setEnabled(ordersTable.isBackwardingAllowed());
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_next_page:
                 ordersTable.setNextPage();
                 performPageChanging();
                 return true;
             case R.id.menu_prev_page:
                 ordersTable.setPrevPage();
                 performPageChanging();
                 return true;
         }
         return false;
     }
 
     private void updateTitle() {
         String template = getText(R.string.subtitle).toString();
         getSupportActionBar().setSubtitle(String.format(template, ordersTable.getPage()));
     }
 
     private void updateView(String query) {
         ordersTable.setQuery(query);
         startTrackListTask();
     }
 
     private void performPageChanging() {
         updateTitle();
         // update state of forward/backward buttons
         invalidateOptionsMenu();
         startTrackListTask();
     }
 
     private void startTrackListTask() {
         if (null != trackListLoadTask) {
             trackListLoadTask.cancel(true);
             trackListLoadTask = null;
         }
         trackListLoadTask = new TrackListLoadTask();
         trackListLoadTask.execute();
     }
 
     private void startTrackOrderTask(Track track) {
         if (null == trackOrderTask || !trackOrderTask.isRunning()) {
             trackOrderTask = new TrackOrderTask();
             trackOrderTask.execute(track);
         }
     }
 
     private void setLoadingState(boolean isLoading) {
         setSupportProgressBarIndeterminateVisibility(isLoading);
     }
 
     private class TrackListLoadTask extends AsyncTask<Void, Void, ArrayList<Track>> {
         @Override
         protected void onPreExecute() {
             trackAdapter.clear();
             setLoadingState(true);
         }
 
         @Override
         protected ArrayList<Track> doInBackground(Void... voids) {
             return ordersTable.getTrackList();
         }
 
         @Override
         protected void onPostExecute(ArrayList<Track> tracks) {
             if (null != tracks) {
                 trackAdapter.setItems(tracks);
             }
             // update state of forward/backward buttons
             invalidateOptionsMenu();
             setLoadingState(false);
         }
     }
 
     private class TrackOrderTask extends AsyncTask<Track, Void, String> {
         @Override
         protected void onPreExecute() {
             setLoadingState(true);
         }
 
         @Override
         protected String doInBackground(Track... params) {
             return ordersTable.orderTrack(params[0]);
         }
 
         @Override
         protected void onPostExecute(String message) {
             if (null != message) {
                 Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
             }
             setLoadingState(false);
         }
 
         private boolean isRunning() {
             return getStatus() == AsyncTask.Status.RUNNING;
         }
     }
 }
