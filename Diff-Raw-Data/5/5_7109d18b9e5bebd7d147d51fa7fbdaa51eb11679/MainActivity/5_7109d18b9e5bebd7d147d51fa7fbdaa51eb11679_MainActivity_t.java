 package com.manutenfruits.interurbanos;
 
 import android.app.SearchManager;
 import android.content.Context;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.Adapter;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.TextView;
 
 import com.manutenfruits.interurbanos.model.BusLine;
 import com.manutenfruits.interurbanos.model.BusModel;
 
 import java.util.ArrayList;
 
 public class MainActivity extends Activity implements SearchView.OnQueryTextListener{
 
     private ListView busListView;
 
     private SeparatedListAdapter mainAdapter;
     private BusLinesAdapter favAdapter;
     private BusLinesAdapter busAdapter;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         ArrayList<BusLine> busLines = BusModel.getData();
         ArrayList<BusLine> favLines = BusModel.getFavorites();
 
         mainAdapter = new SeparatedListAdapter(this);
         favAdapter = mainAdapter.addSection("Favorites", new BusLinesAdapter(this, favLines, mainAdapter));
         busAdapter = mainAdapter.addSection("All", new BusLinesAdapter(this, busLines, mainAdapter));
 
         busListView = (ListView) findViewById(R.id.buslist);
 
         busListView.setAdapter(mainAdapter);
     }
 
     @Override
     protected void onResume() {
         favAdapter.setDataSet(BusModel.getFavorites());
        mainAdapter.notifyDataSetChanged();
         super.onResume();
     }
 
     @Override
     protected void onRestart() {
         favAdapter.setDataSet(BusModel.getFavorites());
        mainAdapter.notifyDataSetChanged();
         super.onRestart();
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()){
             case R.id.search_bus:
 //                openSearch();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
 
         SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
 
         SearchView searchView = (SearchView) menu.findItem(R.id.search_bus).getActionView();
 
         searchView.setOnQueryTextListener(this);
 
         return true;
     }
 
     @Override
     public boolean onQueryTextSubmit(String query) {
         return false;
     }
 
     @Override
     public boolean onQueryTextChange(String newText) {
         mainAdapter.filter(newText);
         return true;
     }
 }
