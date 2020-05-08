 /*
  * GalDroid - a webgallery frontend for android
  * Copyright (C) 2011  Raptor 2101 [raptor2101@gmx.de]
  *		
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  
  */
 
 package de.raptor2101.GalDroid.Activities;
 
 import java.security.NoSuchAlgorithmException;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import de.raptor2101.GalDroid.R;
 import de.raptor2101.GalDroid.Config.GalDroidPreference;
 import de.raptor2101.GalDroid.WebGallery.ImageCache;
 import de.raptor2101.GalDroid.WebGallery.Tasks.CleanUpCacheTask;
 import de.raptor2101.GalDroid.WebGallery.Tasks.SyncronizeCacheTask;
 import de.raptor2101.GalDroid.WebGallery.Tasks.CacheTaskListener;
 
 public class GalleryListingActivitiy extends ListActivity implements CacheTaskListener {
   private ProgressDialog mProgressDialog;
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     registerForContextMenu(getListView());
   }
 
   @Override
   public boolean onContextItemSelected(MenuItem item) {
 
     AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
     TextView view = (TextView) info.targetView;
 
     String galleryName = view.getText().toString();
     if (item.getItemId() == R.id.item_edit_gallery) {
       Intent intent = new Intent(this, EditGalleryActivity.class);
       intent.putExtra(GalDroidApp.INTENT_EXTRA_GALLERY_PROVIDER, galleryName);
       this.startActivity(intent);
     } else {
       GalDroidPreference.deleteGallery(galleryName);
       recreate();
     }
     return true;
   }
 
   @Override
   public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
     MenuInflater inflater = getMenuInflater();
     inflater.inflate(R.menu.gallery_context_menu, menu);
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     MenuInflater inflater = getMenuInflater();
     inflater.inflate(R.menu.gallery_menu, menu);
     return true;
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     if (item.getItemId() == R.id.item_create_new) {
       Intent intent = new Intent(this, EditGalleryActivity.class);
       this.startActivity(intent);
     } else {
       CreateProgressDialog(R.string.menu_synchronize_cache);
       GalDroidApp app = (GalDroidApp) getApplication();
       SyncronizeCacheTask task = new SyncronizeCacheTask(app.getImageCache(), this);
       task.execute();
     }
     return true;
   }
 
   private void CreateProgressDialog(int titleRecouceId) {
     mProgressDialog = new ProgressDialog(this);
     mProgressDialog.setTitle(titleRecouceId);
     mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
     mProgressDialog.setCancelable(false);
     mProgressDialog.dismiss();
   }
 
   @Override
   protected void onListItemClick(ListView l, View v, int position, long id) {
     TextView view = (TextView) v;
     String galleryName = view.getText().toString();
 
     Intent intent = new Intent(this, GridViewActivity.class);
     intent.putExtra(GalDroidApp.INTENT_EXTRA_GALLERY_PROVIDER, galleryName);
 
     this.startActivity(intent);
   }
 
   @Override
   protected void onResume() {
     try {
       GalDroidApp app = (GalDroidApp) getApplication();
       app.Initialize(this);
      setListAdapter(new ArrayAdapter<String>(this, R.layout.gallery_listing_activity, GalDroidPreference.getGalleryNames()));

      ImageCache cache = app.getImageCache();
      if (cache.isCleanUpNeeded()) {
        CreateProgressDialog(R.string.menu_cleanup_cache);
        CleanUpCacheTask task = new CleanUpCacheTask(cache, this);
        task.execute();
       }
     } catch (NoSuchAlgorithmException e) {
       e.printStackTrace();
     }
 
     super.onResume();
   }
 
   public void onCacheOperationStart(int elementCount) {
     mProgressDialog.setMax(elementCount);
     mProgressDialog.setProgress(0);
     mProgressDialog.show();
 
   }
 
   public void onCacheOperationProgress(int elementCount) {
     mProgressDialog.setProgress(elementCount);
 
   }
 
   public void onCacheOperationDone() {
     mProgressDialog.dismiss();
     mProgressDialog = null;
   }
 }
