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
 
 import java.io.Serializable;
 import java.lang.ref.WeakReference;
 import java.security.NoSuchAlgorithmException;
 import java.util.List;
 
 import de.raptor2101.GalDroid.R;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;
 import de.raptor2101.GalDroid.WebGallery.Tasks.GalleryLoaderTask;
 import de.raptor2101.GalDroid.WebGallery.Tasks.GalleryLoaderTaskListener;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ViewGroup.LayoutParams;
 
 public abstract class GalleryActivity extends Activity {
   private static final String INSTANCE_STATE_CURRENT_INDEX = ".de.raptor2101.GalDroid.CurrentIndex";
   private static final String INSTANCE_STATE_GALLERY_OBJECTS = ".de.raptor2101.GalDroid.GalleryObjects";
   private List<GalleryObject> mGalleryObjects;
   private int mCurrentIndex;
   
   private class GalleryLoadingListener implements GalleryLoaderTaskListener {
 
     public void onDownloadStarted() {
       mProgressDialog.show();
     }
 
     public void onDownloadProgress(int elementCount, int maxCount) {
       mProgressDialog.setMax(maxCount);
       mProgressDialog.setProgress(elementCount);
 
     }
 
     public void onDownloadCompleted(List<GalleryObject> galleryObjects) {
       mProgressDialog.dismiss();
       GalDroidApp app = (GalDroidApp) getApplicationContext();
       app.storeGalleryObjects(getDisplayedGallery(), galleryObjects);
       mGalleryObjects = galleryObjects;
       Log.d("GalleryActivity", "Call onGalleryObjectsLoaded");
       onGalleryObjectsLoaded(galleryObjects);
 
     }
   }
 
   
   
   private ProgressDialog mProgressDialog;
   private GalleryLoaderTaskListener mListener;
 
   private WeakReference<GalleryLoaderTask> mDownloadTask;
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     Log.d("GalleryActivity", "New Activity");
     try {
       initialize();
 
     } catch (NoSuchAlgorithmException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
   }
 
   @SuppressWarnings("unchecked")
   @Override
   protected void onPostCreate(Bundle savedInstanceState) {
     super.onPostCreate(savedInstanceState);
     GalDroidApp app = (GalDroidApp) getApplicationContext();
     
     if(savedInstanceState != null) {
       mCurrentIndex = savedInstanceState.getInt(INSTANCE_STATE_CURRENT_INDEX);
       mGalleryObjects = (List<GalleryObject>) savedInstanceState.getSerializable(INSTANCE_STATE_GALLERY_OBJECTS);
     } else {
       mCurrentIndex = -1;
       mGalleryObjects = null;
     }
       
 
     GalleryObject diplayedGallery = getDisplayedGallery();
 
     // Try to get galleryobjects from application cache
     if (mGalleryObjects == null) {
       mGalleryObjects = app.loadStoredGalleryObjects(diplayedGallery);
     }
 
     // Now we have time, load the object from the remote source
     if (mGalleryObjects == null) {
       mProgressDialog = new ProgressDialog(this);
       mProgressDialog.setTitle(R.string.progress_title_load_gallery);
       mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
       mProgressDialog.setCancelable(false);
       mProgressDialog.dismiss();
 
       WebGallery gallery = app.getWebGallery();
       if (gallery != null) {
         LayoutParams params = this.getWindow().getAttributes();
         gallery.setPreferedDimensions(params.height, params.width);
         mListener = new GalleryLoadingListener();
         GalleryLoaderTask task = new GalleryLoaderTask(gallery, mListener);
         task.execute(diplayedGallery);
         mDownloadTask = new WeakReference<GalleryLoaderTask>(task);
       } else {
         // Without a valid Gallery nothing can be displayed... back to
         // previous activity
         this.finish();
       }
     } else {
       onGalleryObjectsLoaded(mGalleryObjects);
     }
   }
 
   @Override
   protected void onResume() {
     super.onResume();
     Log.d("GalleryActivity", "Resume Activity");
     try {
       initialize();
     } catch (NoSuchAlgorithmException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
 
   }
 
   private void initialize() throws NoSuchAlgorithmException {
     GalDroidApp app = (GalDroidApp) getApplicationContext();
     app.Initialize(this);
   }
 
   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
     savedInstanceState.putSerializable(INSTANCE_STATE_GALLERY_OBJECTS, (Serializable) mGalleryObjects);
     savedInstanceState.putInt(INSTANCE_STATE_CURRENT_INDEX, mCurrentIndex);
     
     super.onSaveInstanceState(savedInstanceState);
   }
 
   public abstract void onGalleryObjectsLoaded(List<GalleryObject> galleryObjects);
 
   public GalleryObject getDisplayedGallery() {
     try {
       return (GalleryObject) getIntent().getExtras().get(GalDroidApp.INTENT_EXTRA_DISPLAY_GALLERY);
     } catch (Exception e) {
       return null;
     }
 
   }
 
   public int getCurrentIndex() {
     return mCurrentIndex;
   }
 
   protected void setCurrentIndex(int index) {
     mCurrentIndex = index;
   }
 
   public boolean areGalleryObjectsAvailable() {
     return mGalleryObjects != null;
   }
 
   public GalleryLoaderTask getDownloadTask() {
     if (mDownloadTask != null) {
       return mDownloadTask.get();
     } else {
       return null;
     }
   }
   
   public void showProgressBar(int textId) {
     mProgressDialog.setTitle(R.string.progress_title_load_gallery);
     mProgressDialog.setMax(100);
     mProgressDialog.setProgress(0);
     mProgressDialog.show();
   }
   
  public void updateProgressBar(int current, int max) {
     mProgressDialog.setMax(max);
     mProgressDialog.setProgress(current);
   }
   
  public void dismissProgressBar() {
     mProgressDialog.dismiss();
   }
 }
