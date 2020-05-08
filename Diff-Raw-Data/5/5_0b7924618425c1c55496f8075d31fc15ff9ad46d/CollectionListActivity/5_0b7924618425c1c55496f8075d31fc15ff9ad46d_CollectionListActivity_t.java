 package eu.pinnoo.garbagecalendar.ui;
 
 import eu.pinnoo.garbagecalendar.ui.preferences.PreferenceActivity;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.analytics.tracking.android.GoogleAnalytics;
 import eu.pinnoo.garbagecalendar.R;
 import eu.pinnoo.garbagecalendar.data.AddressData;
 import eu.pinnoo.garbagecalendar.data.Collection;
 import eu.pinnoo.garbagecalendar.data.CollectionsData;
 import eu.pinnoo.garbagecalendar.data.LocalConstants;
 import eu.pinnoo.garbagecalendar.data.Type;
 import eu.pinnoo.garbagecalendar.data.UserData;
 import eu.pinnoo.garbagecalendar.data.caches.AddressCache;
 import eu.pinnoo.garbagecalendar.data.caches.CollectionCache;
 import eu.pinnoo.garbagecalendar.util.Network;
 import eu.pinnoo.garbagecalendar.util.parsers.CalendarParser;
 import eu.pinnoo.garbagecalendar.util.parsers.StreetsParser;
 import eu.pinnoo.garbagecalendar.util.tasks.CacheTask;
 import eu.pinnoo.garbagecalendar.util.tasks.ParserTask;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  *
  * @author Wouter Pinnoo <pinnoo.wouter@gmail.com>
  */
 public class CollectionListActivity extends Activity {
 
     private boolean loading = false;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         AddressCache.initialize(this);
         CollectionCache.initialize(this);
 
         if (LocalConstants.DEBUG) {
             GoogleAnalytics googleAnalytics = GoogleAnalytics.getInstance(getApplicationContext());
             googleAnalytics.setAppOptOut(true);
         }
     }
 
     @Override
     public void onResume() {
         super.onResume();
         if (!loading) {
             Log.i("============================================[Cache]", "onresume");
             initializeCacheAndLoadData();
         }
     }
 
     public void initializeCacheAndLoadData() {
         new CacheTask(this, "Loading calendar...") {
             @Override
             protected void onPreExecute() {
                 super.onPreExecute();
                 loading = true;
             }
 
             @Override
             protected void onPostExecute(Integer[] result) {
                 super.onPostExecute(result);
                 loading = false;
                 loadStreets();
             }
         }.execute(AddressData.getInstance(), CollectionsData.getInstance(), UserData.getInstance());
     }
 
     public void checkAddress() {
         if (UserData.getInstance().isSet()) {
             loadCollections(UserData.getInstance().isChanged());
         } else {
             loading = true;
             new AlertDialog.Builder(this)
                     .setTitle(getString(R.string.yourLocation))
                     .setMessage(getString(R.string.setAddress))
                     .setCancelable(false)
                     .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     dialog.dismiss();
                     loading = false;
                     Intent i = new Intent(getBaseContext(), PreferenceActivity.class);
                     startActivity(i);
                 }
             })
                     .setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     loading = false;
                     finish();
                 }
             })
                     .create().show();
         }
     }
 
     public void loadStreets() {
         if (!AddressData.getInstance().isSet()) {
             if (!Network.networkAvailable(this)) {
                 new AlertDialog.Builder(this)
                         .setTitle(getString(R.string.noInternetConnection))
                         .setMessage(getString(R.string.needConnection))
                         .setCancelable(false)
                         .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                         finish();
                     }
                 })
                         .create().show();
             } else {
                 new ParserTask(this, "Loading street list...") {
                     @Override
                     protected void onPreExecute() {
                         super.onPreExecute();
                         loading = true;
                     }
 
                     @Override
                     protected void onPostExecute(Integer[] result) {
                         super.onPostExecute(result);
                         loading = false;
                         checkAddress();
                     }
                 }.execute(new StreetsParser());
             }
         } else {
             checkAddress();
         }
     }
 
     public void loadCollections(boolean force) {
         if (!force && CollectionsData.getInstance().isSet()) {
             createGUI();
         } else {
             if (!UserData.getInstance().isSet()) {
                 return;
             }
             if (Network.networkAvailable(this)) {
                 new ParserTask(this, "Loading calendar...") {
                     @Override
                     protected void onPreExecute() {
                         super.onPreExecute();
                         loading = true;
                     }
 
                     @Override
                     protected void onPostExecute(Integer[] result) {
                         super.onPostExecute(result);
                         loading = false;
                         UserData.getInstance().changeCommitted();
                         createGUI();
                     }
                 }.execute(new CalendarParser());
             } else {
                 new AlertDialog.Builder(this)
                         .setTitle(getString(R.string.noInternetConnection))
                         .setMessage(getString(R.string.needConnection))
                         .setCancelable(false)
                         .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                         finish();
                     }
                 })
                         .create().show();
             }
         }
     }
 
     @Override
     public void onStart() {
         super.onStart();
         EasyTracker.getInstance().activityStart(this);
     }
 
     @Override
     public void onStop() {
         super.onStop();
         EasyTracker.getInstance().activityStop(this);
     }
 
     private void createGUI() {
         TableLayout table = (TableLayout) findViewById(R.id.main_table);
         table.removeViews(0, table.getChildCount());
 
         List<Collection> collections = CollectionsData.getInstance().getCollections();
         Iterator<Collection> it = collections.iterator();
         int i = 0;
         while (it.hasNext()) {
             Collection col = it.next();
             if (col.getDate().before(Calendar.getInstance().getTime())) {
                 continue;
             }
 
             addTableRow(col, i);
             i++;
         }
     }
 
     private String beautifyDate(Date date) {
         Date today = Calendar.getInstance().getTime();
         int daysBetween = (int) ((date.getTime() - today.getTime()) / (24 * 60 * 60 * 1000));
 
         if (daysBetween <= 1) {
             return LocalConstants.getDateFormatter(LocalConstants.DateFormatType.MAIN_TABLE, this).format(date)
                     + " ("
                     + getString(R.string.tomorrow)
                     + ")";
        } else if (daysBetween < 6) {
             return LocalConstants.getDateFormatter(LocalConstants.DateFormatType.MAIN_TABLE, this).format(date)
                     + " ("
                     + getString(R.string.thisweek)
                     + " "
                     + LocalConstants.getDateFormatter(LocalConstants.DateFormatType.WEEKDAY, this).format(date)
                     + ")";
        } else if (daysBetween < 13) {
             return LocalConstants.getDateFormatter(LocalConstants.DateFormatType.MAIN_TABLE, this).format(date)
                     + " ("
                     + getString(R.string.nextweek)
                     + " "
                     + LocalConstants.getDateFormatter(LocalConstants.DateFormatType.WEEKDAY, this).format(date)
                     + ")";
         } else {
             return LocalConstants.getDateFormatter(LocalConstants.DateFormatType.MAIN_TABLE, this).format(date);
         }
     }
 
     private void addTableRow(Collection col, int rowNumber) {
         String date = beautifyDate(col.getDate());
         int backgroundColor = rowNumber % 2 == 0 ? LocalConstants.COLOR_TABLE_EVEN_ROW : LocalConstants.COLOR_TABLE_ODD_ROW;
 
         LayoutInflater inflater = getLayoutInflater();
         TableLayout tl = (TableLayout) findViewById(R.id.main_table);
         TableRow tr = (TableRow) inflater.inflate(R.layout.main_table_row, tl, false);
 
         TextView labelDate = (TextView) tr.findViewById(R.id.main_row_date);
         labelDate.setText(date);
         labelDate.setPadding(5, 5, 5, 5);
         labelDate.setTextColor(Color.BLACK);
 
         tr.setBackgroundColor(backgroundColor);
         tr.setOnClickListener(new TableRowListener(col));
 
         boolean hasType = col.hasType(Type.REST);
         TextView labelRest = (TextView) tr.findViewById(R.id.main_row_rest);
         labelRest.setText(hasType ? Type.REST.shortStrValue(this) : "");
         labelRest.setPadding(1, 5, 5, 5);
         labelRest.setBackgroundColor(hasType ? Type.REST.getColor(UserData.getInstance().getAddress().getSector().getType()) : backgroundColor);
 
         hasType = col.hasType(Type.GFT);
         TextView labelGFT = (TextView) tr.findViewById(R.id.main_row_gft);
         labelGFT.setText(hasType ? Type.GFT.shortStrValue(this) : "");
         labelGFT.setPadding(1, 5, 5, 5);
         labelGFT.setBackgroundColor(hasType ? Type.GFT.getColor(UserData.getInstance().getAddress().getSector().getType()) : backgroundColor);
 
         hasType = col.hasType(Type.PMD);
         TextView labelPMD = (TextView) tr.findViewById(R.id.main_row_pmd);
         labelPMD.setText(hasType ? Type.PMD.shortStrValue(this) : "");
         labelPMD.setPadding(1, 5, 5, 5);
         labelPMD.setBackgroundColor(hasType ? Type.PMD.getColor(UserData.getInstance().getAddress().getSector().getType()) : backgroundColor);
 
         hasType = col.hasType(Type.PK);
         TextView labelPK = (TextView) tr.findViewById(R.id.main_row_pk);
         labelPK.setText(hasType ? Type.PK.shortStrValue(this) : "");
         labelPK.setPadding(1, 5, 5, 5);
         labelPK.setBackgroundColor(hasType ? Type.PK.getColor(UserData.getInstance().getAddress().getSector().getType()) : backgroundColor);
 
         hasType = col.hasType(Type.GLAS);
         TextView labelGlas = (TextView) tr.findViewById(R.id.main_row_glas);
         labelGlas.setText(hasType ? Type.GLAS.shortStrValue(this) : "");
         labelGlas.setPadding(1, 5, 5, 5);
         labelGlas.setBackgroundColor(hasType ? Type.GLAS.getColor(UserData.getInstance().getAddress().getSector().getType()) : backgroundColor);
 
         tl.addView(tr);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.refresh:
                 loadCollections(true);
                 return true;
             case R.id.preferences:
                 Intent intent = new Intent();
                 intent.setClass(this, PreferenceActivity.class);
                 startActivityForResult(intent, 0);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 }
