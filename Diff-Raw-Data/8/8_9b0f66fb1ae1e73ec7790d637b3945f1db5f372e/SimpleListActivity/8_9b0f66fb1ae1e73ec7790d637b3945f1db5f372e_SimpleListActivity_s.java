 package com.marwinxxii.ccardstats.gui;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.marwinxxii.ccardstats.R;
 
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 public class SimpleListActivity extends ListActivity {
     
     private static final int ids[] = {
         R.id.stats_item_text, R.id.stats_item_income, R.id.stats_item_outcome
     };
     
     private static Map<String, List<String[]>> cache = new HashMap<String, List<String[]>>();
     
     protected ProgressDialog progressDialog;
     protected String cacheKey;
     protected int itemLayoutId = android.R.layout.simple_list_item_1;
     protected int[] itemFieldsIds = { android.R.id.text1 };
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         cacheKey = this.getClass().getName();
     }
     
     @Override
     public void onStart() {
         super.onStart();
         setListTitle();
     }
     
     @Override
     public void onResume() {
         super.onResume();
         if (getValuesFromCache() != null) {
             setListAdapter();
             return;
         }
         if (progressDialog != null) progressDialog.show();
         getItems();
     }
     
    @Override
    public void onDestroy() {
        super.onDestroy();
        cache.remove(cacheKey);
    }
    
     protected int getItemLayout() {
         return R.layout.stats_item;
     }
     
     protected int[] getItemFieldsIds() {
         return ids;
     }
     
     public void setListTitle() {}
     
     public void setDialogParams(int titleId, String message) {
         progressDialog = new ProgressDialog(this);
         progressDialog.setTitle(titleId);
         progressDialog.setMessage(message);
         progressDialog.setCancelable(false);
     }
     
     protected void setListAdapter() {
         List<String[]> values = getValuesFromCache();
         if (values != null && values.size() != 0) {
             TextMappingAdapter adapter = new TextMappingAdapter(this, getItemLayout(),
                     getItemFieldsIds(), values);
             mItemsList.setAdapter(adapter);
         } else {
             setNoItemsTextId(R.string.list_activity_noitems);
         }
     }
     
     protected void getItems() {}
     
     protected void onTaskComplete(Map<Integer, double[]> values) {
         setListAdapter();
         progressDialog.dismiss();
     }
     
     protected void cacheValues(List<String[]> values) {
         cache.put(cacheKey, values);
     }
     
     protected List<String[]> getValuesFromCache() {
         return cache.get(cacheKey);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()) {
             case R.id.menu_main_rates:
             default:
                 startActivity(PreferencesActivity.getStartingIntent(this));
                 break;
         }
         return true;
     }
 }
