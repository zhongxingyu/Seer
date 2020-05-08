 package info.eigenein.openwifi.activities;
 
 import android.app.*;
 import android.content.*;
 import android.os.*;
 import android.util.*;
 import android.view.MenuItem;
 import android.widget.*;
 import com.google.analytics.tracking.android.EasyTracker;
 import info.eigenein.openwifi.R;
 import info.eigenein.openwifi.helpers.*;
 import info.eigenein.openwifi.persistence.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class StatisticsActivity extends ListActivity {
 
     private static final String[] adapterFrom = { "title" , "text" };
 
     private static final int[] adapterTo = { android.R.id.text1, android.R.id.text2 };
 
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         refresh();
 
         if (BuildHelper.isHoneyComb()) {
             getActionBar().setDisplayHomeAsUpEnabled(true);
         }
     }
 
     @Override
     public void onStart() {
         super.onStart();
 
         EasyTracker.getInstance().activityStart(this);
     }
 
     @Override
     public boolean onOptionsItemSelected(final MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 onBackPressed();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public void onStop() {
         super.onStop();
 
         EasyTracker.getInstance().activityStop(this);
     }
 
     private void refresh() {
         // Initialize the dialog.
         final ProgressDialog progressDialog = ProgressDialog.show(
                 this,
                 getString(R.string.dialog_title_refresh_statistics),
                 getString(R.string.dialog_message_refresh_statistics),
                 true
         );
         progressDialog.setCancelable(true);
         progressDialog.setCanceledOnTouchOutside(true);
         // Initialize the refresh task.
         final RefreshAsyncTask task = new RefreshAsyncTask(progressDialog);
         // Set the onCancel listener.
         progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
             @Override
             public void onCancel(final DialogInterface dialogInterface) {
                 task.cancel(true);
                 StatisticsActivity.this.onBackPressed();
             }
         });
         // Show the dialog.
         progressDialog.show();
         // Start the refreshing task.
         task.execute();
     }
 
     private SimpleAdapter createAdapter(
             final long uniqueBssidCount,
             final long uniqueSsidCount,
             final long scanResultCount) {
         // Initialize the list of statistics items.
         final ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();
         // Add the items.
         items.add(createItem(R.string.statistics_unique_bssid_count, Long.toString(uniqueBssidCount)));
         items.add(createItem(R.string.statistics_unique_ssid_count, Long.toString(uniqueSsidCount)));
         items.add(createItem(R.string.statistics_scan_result_count, Long.toString(scanResultCount)));
         // Create the adapter,
         return new SimpleAdapter(
                 this,
                 items,
                 android.R.layout.simple_list_item_2,
                 adapterFrom,
                 adapterTo);
     }
 
     private HashMap<String, String> createItem(final int titleResourceId, final String text) {
         HashMap<String, String> item = new HashMap<String, String>();
         item.put("title", getString(titleResourceId));
         item.put("text", text);
         return item;
     }
 
     private class RefreshAsyncTask extends AsyncTask<Void, Void, Void> {
 
         private final String LOG_TAG = RefreshAsyncTask.class.getCanonicalName();
 
         /**
          * The refreshing progress dialog.
          */
         private final ProgressDialog progressDialog;
 
         private long uniqueBssidCount;
 
         private long uniqueSsidCount;
 
         private long scanResultCount;
 
         public RefreshAsyncTask(final ProgressDialog progressDialog) {
             this.progressDialog = progressDialog;
         }
 
         @Override
         protected Void doInBackground(final Void... voids) {
             final MyScanResult.Dao dao = CacheOpenHelper.getInstance(StatisticsActivity.this).getMyScanResultDao();
             // Query the data.
             if (!isCancelled()) {
                 Log.d(LOG_TAG + ".doInBackground", "dao.getUniqueBssidCount()");
                 uniqueBssidCount = dao.getUniqueBssidCount();
             }
             if (!isCancelled()) {
                 Log.d(LOG_TAG + ".doInBackground", "dao.getUniqueSsidCount()");
                 uniqueSsidCount = dao.getUniqueSsidCount();
             }
             if (!isCancelled()) {
                 Log.d(LOG_TAG + ".doInBackground", "dao.getCount()");
                 scanResultCount = dao.getCount();
             }
             // Return nothing.
             return null;
         }
 
         @Override
         protected void onPostExecute(final Void aVoid) {
             // Do not update UI if cancelled.
             if (isCancelled()) {
                 Log.d(LOG_TAG + ".onPostExecute", "cancelled");
                 return;
             }
             // Update UI.
             final ListAdapter adapter = createAdapter(
                     uniqueBssidCount, uniqueSsidCount, scanResultCount);
             if (adapter != null) {
                 setListAdapter(adapter);
             }
             // Hide the progress dialog.
            progressDialog.dismiss();
         }
     }
 }
