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
 
 package com.google.code.geobeagle;
 
 import com.google.code.geobeagle.ui.cachelist.CacheListDelegate;
 import com.google.code.geobeagle.ui.cachelist.CacheListDelegateDI;
 
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 
 public class CacheList extends ListActivity {
     private CacheListDelegate mCacheListDelegate;
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         return mCacheListDelegate.onContextItemSelected(item) || super.onContextItemSelected(item);
     }
 
     public CacheList(CacheListDelegate cacheListDelegate) {
         mCacheListDelegate = cacheListDelegate;
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         mCacheListDelegate = CacheListDelegateDI.create(this, this.getLayoutInflater());
         mCacheListDelegate.onCreate();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         return mCacheListDelegate.onCreateOptionsMenu(menu);
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         super.onListItemClick(l, v, position, id);
         mCacheListDelegate.onListItemClick(l, v, position, id);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         return mCacheListDelegate.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         mCacheListDelegate.onPause();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         mCacheListDelegate.onResume();
     }
 
 }
