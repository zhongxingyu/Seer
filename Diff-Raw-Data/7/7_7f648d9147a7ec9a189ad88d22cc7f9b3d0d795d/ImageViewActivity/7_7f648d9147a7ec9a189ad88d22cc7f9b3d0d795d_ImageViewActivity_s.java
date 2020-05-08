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
 
 import java.util.List;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Gallery;
 import de.raptor2101.GalDroid.R;
 import de.raptor2101.GalDroid.Activities.Helpers.ActionBarHider;
 import de.raptor2101.GalDroid.Activities.Listeners.ImageViewOnTouchListener;
 import de.raptor2101.GalDroid.Activities.Views.GalleryImageView;
 import de.raptor2101.GalDroid.Activities.Views.ImageInformationView;
 import de.raptor2101.GalDroid.WebGallery.ImageAdapter;
 import de.raptor2101.GalDroid.WebGallery.ImageAdapter.DisplayTarget;
 import de.raptor2101.GalDroid.WebGallery.ImageAdapter.ScaleMode;
 import de.raptor2101.GalDroid.WebGallery.ImageAdapter.TitleConfig;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
 import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTask;
 
 public class ImageViewActivity extends GalleryActivity implements OnItemSelectedListener, OnItemClickListener {
 
   private Gallery mGalleryFullscreen;
   private Gallery mGalleryThumbnails;
 
   private ImageAdapter mAdapterFullscreen, mAdapterThumbnails;
 
   private ImageInformationView mInformationView;
 
   private ActionBarHider mActionBarHider;
   private ImageLoaderTask mImageLoaderTask;
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
 
     setContentView(R.layout.image_view_activity);
     super.onCreate(savedInstanceState);
 
     ActionBar actionBar = getActionBar();
 
     actionBar.setDisplayShowHomeEnabled(false);
     actionBar.setDisplayShowTitleEnabled(false);
     actionBar.hide();
     mActionBarHider = new ActionBarHider(actionBar);
 
     GalDroidApp app = (GalDroidApp) getApplication();
     mImageLoaderTask = new ImageLoaderTask(app.getWebGallery(), app.getImageCache());
 
     mInformationView = (ImageInformationView) findViewById(R.id.viewImageInformations);
     boolean showInfo = getIntent().getExtras().getBoolean(GalDroidApp.INTENT_EXTRA_SHOW_IMAGE_INFO);
     if (showInfo) {
       mInformationView.setVisibility(View.VISIBLE);
     } else {
       mInformationView.setVisibility(View.GONE);
     }
     mInformationView.initialize();
 
     mGalleryFullscreen = (Gallery) findViewById(R.id.singleImageGallery);
     mGalleryThumbnails = (Gallery) findViewById(R.id.thumbnailImageGallery);
     mGalleryThumbnails.setWillNotCacheDrawing(true);
 
     LayoutParams params = this.getWindow().getAttributes();
 
     ImageViewOnTouchListener touchListener = new ImageViewOnTouchListener(mGalleryFullscreen, mGalleryThumbnails, params.height / 5f);
 
     mAdapterFullscreen = new ImageAdapter(this, new Gallery.LayoutParams(params.width, params.height), ScaleMode.ScaleSource, mImageLoaderTask);
     mAdapterFullscreen.setTitleConfig(TitleConfig.HideTitle);
     mAdapterFullscreen.setDisplayTarget(DisplayTarget.FullScreen);
 
     mAdapterThumbnails = new ImageAdapter(this, new Gallery.LayoutParams(100, 100), ScaleMode.DontScale, mImageLoaderTask);
     mAdapterThumbnails.setTitleConfig(TitleConfig.HideTitle);
     mAdapterThumbnails.setDisplayTarget(DisplayTarget.Thumbnails);
 
     mGalleryFullscreen.setAdapter(mAdapterFullscreen);
     mGalleryThumbnails.setAdapter(mAdapterThumbnails);
 
     mGalleryFullscreen.setOnItemClickListener(this);
     mGalleryFullscreen.setOnTouchListener(touchListener);
     mGalleryFullscreen.setOnItemSelectedListener(this);
     mGalleryThumbnails.setOnItemSelectedListener(this);
   }
 
   @Override
   public void onBackPressed() {
     ImageAdapter adapter = (ImageAdapter) mGalleryFullscreen.getAdapter();
     adapter.cleanUp();
     
     Intent resultIntent = new Intent(this, ImageViewActivity.class);
     resultIntent.putExtra(GalDroidApp.INTENT_EXTRA_DISPLAY_INDEX, getCurrentIndex());
     setResult(Activity.RESULT_OK, resultIntent);
 
     super.onBackPressed();
   }
 
   @Override
   protected void onPause() {
     super.onPause();
     
     try {
       mImageLoaderTask.stop(false);
     } catch (InterruptedException e) {
       
     }
   }
   
   @Override
   protected void onResume() {
     super.onResume();
     mImageLoaderTask.start();
   }
   
   @Override
   protected void onStop() {
     super.onStop();
     
     try {
       mImageLoaderTask.cancel(true);
     } catch (InterruptedException e) {
       
     }
    mImageLoaderTask = null;
     
     ImageAdapter adapter = (ImageAdapter) mGalleryFullscreen.getAdapter();
     adapter.cleanUp();
     adapter = (ImageAdapter) mGalleryThumbnails.getAdapter();
     adapter.cleanUp();
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     MenuInflater inflater = getMenuInflater();
     inflater.inflate(R.menu.image_view_options_menu, menu);
     return true;
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     if (item.getItemId() == R.id.item_additional_info_object) {
       if (mInformationView.getVisibility() == View.GONE) {
         mInformationView.setVisibility(View.VISIBLE);
       } else {
         mInformationView.setVisibility(View.GONE);
       }
     }
     return true;
   }
 
   public void onItemSelected(AdapterView<?> gallery, View view, int position, long arg3) {
     setCurrentIndex(position);
 
     if (gallery == mGalleryFullscreen) {
       mGalleryThumbnails.setSelection(position);
     } else {
       mGalleryFullscreen.setSelection(position);
       view = mGalleryFullscreen.getSelectedView();
     }
 
     mInformationView.setGalleryImageView((GalleryImageView) view);
   }
 
   public void onNothingSelected(AdapterView<?> arg0) {
     // Empty Stub, cause nothing to do
   }
 
   @Override
   public void onGalleryObjectsLoaded(List<GalleryObject> galleryObjects) {
     mAdapterFullscreen.setGalleryObjects(galleryObjects);
     mAdapterThumbnails.setGalleryObjects(galleryObjects);
 
     int currentIndex = getCurrentIndex();
     if (currentIndex == -1) {
       GalleryObject currentObject = (GalleryObject) getIntent().getExtras().getSerializable(GalDroidApp.INTENT_EXTRA_DISPLAY_OBJECT);
       currentIndex = galleryObjects.indexOf(currentObject);
     }
 
     mGalleryFullscreen.setSelection(currentIndex);
     mGalleryThumbnails.setSelection(currentIndex);
     mImageLoaderTask.start();
   }
 
   public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
     if (mActionBarHider.isShowing()) {
       mActionBarHider.hide();
     } else {
       mActionBarHider.show();
     }
 
   }
 }
