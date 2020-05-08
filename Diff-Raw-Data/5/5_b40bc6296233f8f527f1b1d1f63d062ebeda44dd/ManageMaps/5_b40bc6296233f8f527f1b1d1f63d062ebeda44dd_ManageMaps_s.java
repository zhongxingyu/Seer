 package de.jensnistler.routemap.activities;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import android.app.ListActivity;
import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.Toast;
 import de.jensnistler.routemap.R;
 import de.jensnistler.routemap.helper.MapDataSource;
 import de.jensnistler.routemap.helper.MapAdapter;
 import de.jensnistler.routemap.helper.MapDownloader;
 import de.jensnistler.routemap.helper.MapListUpdater;
 import de.jensnistler.routemap.helper.MapModel;
 
 public class ManageMaps extends ListActivity {
     private static String URL_MAP_LIST = "http://static.jensnistler.de/maps.json";
 
     private MapDataSource mDataSource;
     private MapAdapter mAdapter;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.manage_maps);
 
         mDataSource = new MapDataSource(this);
         mDataSource.open();
 
         ArrayList<MapModel> values = mDataSource.getAllMaps();
         mAdapter = new MapAdapter(
             this,
             android.R.layout.simple_list_item_1,
             values
         );
 
         ListView listView = (ListView) findViewById(android.R.id.list);
         listView.setAdapter(mAdapter);
 
         if (values.isEmpty()) {
             new MapListUpdater(this, mAdapter, mDataSource).execute(URL_MAP_LIST);
         }
 
         registerForContextMenu(listView);
     }
 
     public void onListItemClick(ListView l, View v, int position, long id) {
         super.onListItemClick(l, v, position, id);
 
         MapModel selection = (MapModel) l.getItemAtPosition(position);
         if (selection.getUpdated() != 0) {
             Toast.makeText(this, getResources().getString(R.string.loadingX) + " " + selection.getDescription(), Toast.LENGTH_LONG).show();
 
             SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
             SharedPreferences.Editor editor = settings.edit();
             editor.putString("mapFile", selection.getKey());
             editor.commit();
 
            Intent mainActivity = new Intent(getBaseContext(), Main.class);
            startActivity(mainActivity);
         }
         else {
             v.showContextMenu();
         }
     }
 
     private MapModel contextSelection;
 
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
         ListView view = (ListView) v;
         contextSelection = (MapModel) view.getItemAtPosition(info.position);
 
         menu.setHeaderTitle(contextSelection.getDescription());
         if (contextSelection.getUpdated() != 0) {
             if (contextSelection.getDate() < contextSelection.getUpdated()) {
                 menu.add(Menu.NONE, 1, 1, R.string.update);
             }
             menu.add(Menu.NONE, 2, 2, R.string.remove);
         }
         else {
             menu.add(Menu.NONE, 3, 3, R.string.download);
         }
     }
 
     public boolean onContextItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             // remove
             case 2:
                 File cacheDir = getExternalCacheDir();
                 if (null == cacheDir || !cacheDir.canWrite()) {
                     Toast.makeText(this, R.string.cannotWriteToCache, Toast.LENGTH_LONG).show();
                     return true;
                 }
                 File cacheFile = new File(cacheDir, contextSelection.getKey().replace("/", "_") + ".map");
                 cacheFile.delete();
 
                 mAdapter.remove(contextSelection);
                 contextSelection.setUpdated(0);
                 mDataSource.saveMap(contextSelection);
                 mAdapter.add(contextSelection);
                 mAdapter.setNotifyOnChange(true);
                 mAdapter.notifyDataSetChanged();
                 break;
 
             // update
             case 1:
             // download
             case 3:
                 new MapDownloader(this, mAdapter, mDataSource).execute(contextSelection);
                 break;
         }
 
         return true;
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.managemaps, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_updatemaps:
                 new MapListUpdater(this, mAdapter, mDataSource).execute(URL_MAP_LIST);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 }
