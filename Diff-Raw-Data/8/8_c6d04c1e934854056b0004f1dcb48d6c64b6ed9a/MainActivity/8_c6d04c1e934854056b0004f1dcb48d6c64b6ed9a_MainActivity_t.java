 package org.ktln2.android.callstat;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.*;

 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.support.v4.content.CursorLoader;
import android.database.Cursor;

 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.ListView;
 import android.widget.LinearLayout;
 import android.view.LayoutInflater;
 import android.os.Bundle;
 import android.provider.CallLog.Calls;

 import android.graphics.Color;
 import com.jjoe64.graphview.BarGraphView;
 import com.jjoe64.graphview.GraphViewSeries;
 import com.jjoe64.graphview.GraphView.GraphViewData;
 import com.jjoe64.graphview.GraphView;
 import com.jjoe64.graphview.GraphViewStyle;
 
 import com.google.ads.*;
 
 
 public class MainActivity extends SherlockFragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
     private CallStatAdapter mAdapter;
     private ListView mListView;
     private AdView mAdView;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.main);
 
         mListView = (ListView)findViewById(R.id.list);
 
         mListView.setEmptyView(findViewById(android.R.id.empty));
 
         mListView.setAdapter(mAdapter);
 
         getSupportLoaderManager().initLoader(0, null, this);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         int ordering_type = 0;
         switch (item.getItemId()) {
             case R.id.ordering_total_duration:
                 ordering_type = CallStatAdapter.CALL_STAT_ADAPTER_ORDERING_TOTAL_DURATION;
                 break;
             case R.id.ordering_total_calls:
                 ordering_type = CallStatAdapter.CALL_STAT_ADAPTER_ORDERING_TOTAL_CALLS;
                 break;
             case R.id.ordering_avg_duration:
                 ordering_type = CallStatAdapter.CALL_STAT_ADAPTER_ORDERING_AVG_DURATION;
                 break;
             case R.id.ordering_max_duration:
                 ordering_type = CallStatAdapter.CALL_STAT_ADAPTER_ORDERING_MAX_DURATION;
                 break;
             case R.id.ordering_min_duration:
                 ordering_type = CallStatAdapter.CALL_STAT_ADAPTER_ORDERING_MIN_DURATION;
                 break;
         }
         mAdapter.order(ordering_type);
 
         return super.onOptionsItemSelected(item);
     }
 
     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         CursorLoader cl = new CursorLoader(
             this,              // context
             Calls.CONTENT_URI, // Uri for the Call log
             null,              // projection
             null,              // selection
             null,              // selectionArgs
             null               // sortOrder
         );
 
         return cl;
     }
 
     /*
      * When the Cursor is loaded save the value retrieved in an Hashmap
      * with as key the number/contact and as value the duration.
      */
     private StatisticsMap getValues(Cursor cursor) {
         StatisticsMap hm = new StatisticsMap();
 
         // otherwise CursorIndexOutOfBoundsException: Index -1 requested, with a size of 147
         cursor.moveToFirst();
         while (!cursor.isLast()) {
             long duration = cursor.getLong(cursor.getColumnIndexOrThrow(Calls.DURATION));
             String number = cursor.getString(cursor.getColumnIndexOrThrow(Calls.NUMBER));
 
             cursor.moveToNext();
 
             hm.put(number, duration, this);
         }
 
         return hm;
     }
 
     public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
         LayoutInflater inflater =
             (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
         mListView.addHeaderView(inflater.inflate(R.layout.call_stat_surface, null));
 
 
         StatisticsMap hashmap = getValues(cursor);
 
 
         int delta = 60;
         int[] bins = hashmap.getBinsForDurations(60);
         int nBins = bins.length;
 
         GraphViewData[] data = new GraphViewData[nBins];
 
         for (int cycle = 0 ; cycle < nBins ; cycle++) {
             data[cycle] = new GraphViewData(cycle, bins[cycle]);
         }
 
         GraphViewSeries durationSeries = new GraphViewSeries(data);
 
         GraphView graphView = new BarGraphView(this, "# of calls of given minutes");
         graphView.setGraphViewStyle(new GraphViewStyle(Color.BLACK, Color.BLACK, Color.DKGRAY));
         graphView.setViewPort(0, 20);
         //graphView.setScrollable(true);
         graphView.addSeries(durationSeries); // data
         ((LinearLayout)findViewById(R.id.graph_container)).addView(graphView);
         ((TextView)findViewById(R.id.n_calls)).setText(hashmap.getTotalCalls() + " calls");
         ((TextView)findViewById(R.id.n_contacts)).setText(hashmap.getTotalContacts() + " contacts");
         ((TextView)findViewById(R.id.total_duration)).setText(
             DateUtils.formatElapsedTimeNG(hashmap.getTotalDuration())
         );
         ((TextView)findViewById(R.id.avg_duration)).setText(
             DateUtils.formatElapsedTimeNG(hashmap.getTotalDuration()/hashmap.getTotalCalls())
                 + " in average"
         );
         mAdapter = new CallStatAdapter(this, hashmap);
         ((ListView)findViewById(R.id.list)).setAdapter(mAdapter);
         mAdapter.notifyDataSetChanged();
     }
 
     public void onLoaderReset(Loader<Cursor> loader) {
     }
 }
