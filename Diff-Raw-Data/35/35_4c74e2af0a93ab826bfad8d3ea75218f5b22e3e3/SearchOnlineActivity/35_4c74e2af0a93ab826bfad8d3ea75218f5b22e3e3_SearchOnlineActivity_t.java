 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.activity.searchonline;
 
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.activity.cachelist.CacheListActivity;
 import com.google.code.geobeagle.gpsstatuswidget.GpsStatusWidgetDelegate;
 import com.google.code.geobeagle.gpsstatuswidget.InflatedGpsStatusWidget;
 import com.google.code.geobeagle.gpsstatuswidget.UpdateGpsWidgetRunnable;
 import com.google.inject.Injector;
 
 import roboguice.activity.GuiceActivity;
 
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 
 public class SearchOnlineActivity extends GuiceActivity {
 
     private SearchOnlineActivityDelegate mSearchOnlineActivityDelegate;
     private UpdateGpsWidgetRunnable mUpdateGpsWidgetRunnable;
 
     SearchOnlineActivityDelegate getMSearchOnlineActivityDelegate() {
         return mSearchOnlineActivityDelegate;
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.search);
 
         Log.d("GeoBeagle", "SearchOnlineActivity onCreate");

         Injector injector = this.getInjector();
        final InflatedGpsStatusWidget mInflatedGpsStatusWidget = injector
                .getInstance(InflatedGpsStatusWidget.class);
         GpsStatusWidgetDelegate gpsStatusWidgetDelegate = injector
                 .getInstance(GpsStatusWidgetDelegate.class);
         mUpdateGpsWidgetRunnable = injector.getInstance(UpdateGpsWidgetRunnable.class);
         mInflatedGpsStatusWidget.setDelegate(gpsStatusWidgetDelegate);
         mInflatedGpsStatusWidget.setBackgroundColor(Color.BLACK);
 
         mSearchOnlineActivityDelegate = injector.getInstance(SearchOnlineActivityDelegate.class);
         mSearchOnlineActivityDelegate.configureWebView();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.search_online_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         startActivity(new Intent(this, CacheListActivity.class));
         return true;
     }
 
     @Override
     protected void onPause() {
         Log.d("GeoBeagle", "SearchOnlineActivity onPause");
         mSearchOnlineActivityDelegate.onPause();
        // Must call super so that context scope is cleared only after listeners
        // are removed.
        super.onPause();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         Log.d("GeoBeagle", "SearchOnlineActivity onResume");
         mSearchOnlineActivityDelegate.onResume();
         mUpdateGpsWidgetRunnable.run();
     }
 }
