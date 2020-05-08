 package edu.mit.mobile.android.livingpostcards;
 /*
  * Copyright (C) 2012-2013  MIT Mobile Experience Lab
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation version 2
  * of the License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.hardware.Camera;
 import android.hardware.Camera.AutoFocusCallback;
 import android.hardware.Camera.Parameters;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Camera.Size;
 import android.location.Location;
 import android.location.LocationListener;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.ActionBarSherlock;
 
 import edu.mit.mobile.android.imagecache.ImageCache;
 import edu.mit.mobile.android.imagecache.ImageCache.OnImageLoadListener;
 import edu.mit.mobile.android.livingpostcards.CameraPreview.OnPreviewStartedListener;
 import edu.mit.mobile.android.livingpostcards.auth.Authenticator;
 import edu.mit.mobile.android.livingpostcards.data.Card;
 import edu.mit.mobile.android.livingpostcards.data.CardMedia;
 import edu.mit.mobile.android.locast.Constants;
 import edu.mit.mobile.android.locast.data.CastMedia.CastMediaInfo;
 import edu.mit.mobile.android.locast.data.MediaProcessingException;
 import edu.mit.mobile.android.location.IncrementalLocator;
 import edu.mit.mobile.android.maps.GeocodeTask;
 import edu.mit.mobile.android.widget.MultiLevelButton;
 import edu.mit.mobile.android.widget.MultiLevelButton.OnChangeLevelListener;
 
 public class CameraActivity extends FragmentActivity implements OnClickListener,
         OnImageLoadListener, OnCheckedChangeListener, LoaderCallbacks<Cursor>, OnTouchListener {
 
     private static final String TAG = CameraActivity.class.getSimpleName();
 
     public static final String ACTION_ADD_PHOTO = "edu.mit.mobile.android.ACTION_ADD_PHOTO";
 
     private final ActionBarSherlock mSherlock = ActionBarSherlock.wrap(this);
 
     private Camera mCamera;
     private CameraPreview mPreview;
 
     private FrameLayout mPreviewHolder;
 
     private ImageCache mImageCache;
 
     private Uri mCard;
 
     private Uri mCardDir;
 
     private ImageView mOnionSkin;
 
     private View mCaptureButton;
 
     private MultiLevelButton mOnionskinToggle;
 
     private IncrementalLocator mLocator;
 
     protected Location mLocation;
 
     private Uri mRecentImage;
 
     private static final int LOADER_CARD = 100, LOADER_CARDMEDIA = 101;
 
     private static final String[] CARD_MEDIA_PROJECTION = new String[] { CardMedia._ID,
             CardMedia.COL_LOCAL_URL, CardMedia.COL_MEDIA_URL };
 
     private static final int MSG_RELOAD_CARD_AND_MEDIA = 100;
     private static final int MSG_START_AUTOFOCUS = 101;
 
     private static final String INSTANCE_CARD = "edu.mit.mobile.android.livingpostcards.INSTANCE_CARD";
 
     private static class MyHandler extends Handler {
         private final CameraActivity mActivity;
 
         public MyHandler(CameraActivity activity) {
             mActivity = activity;
         }
 
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
                 case MSG_RELOAD_CARD_AND_MEDIA:
                     final LoaderManager lm = mActivity.getSupportLoaderManager();
                     lm.restartLoader(LOADER_CARD, null, mActivity);
                     lm.restartLoader(LOADER_CARDMEDIA, null, mActivity);
                     break;
 
                 case MSG_START_AUTOFOCUS:
                     mActivity.onShutterHalfwayPressed();
                     break;
             }
         }
     }
 
     private final Handler mHandler = new MyHandler(this);
 
     private final OnChangeLevelListener mOnionskinChangeLevel = new OnChangeLevelListener() {
 
         @Override
         public int onChangeLevel(MultiLevelButton b, int curLevel) {
             int newLevel;
             switch (curLevel) {
                 case 0:
                     newLevel = 25;
                     break;
                 case 25:
                     newLevel = 50;
                     break;
                 case 50:
                     newLevel = 75;
                     break;
                 default:
                     newLevel = 0;
                     break;
             }
             setOnionSkinVisible(newLevel);
             return newLevel;
         }
     };
 
     private final OnPreviewStartedListener mOnPreviewStartedListener = new OnPreviewStartedListener() {
 
         @Override
         public void onPreviewStarted() {
             Log.d(TAG, "onPreviewStarted");
             autoFocus();
         }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         mSherlock.requestFeature(Window.FEATURE_NO_TITLE);
 
         mSherlock.setContentView(R.layout.activity_camera);
 
         mPreviewHolder = (FrameLayout) findViewById(R.id.camera_preview);
 
         mOnionSkin = (ImageView) findViewById(R.id.onion_skin_image);
 
         mCaptureButton = findViewById(R.id.capture);
         mCaptureButton.setOnClickListener(this);
         mCaptureButton.setOnTouchListener(this);
 
         mOnionskinToggle = (MultiLevelButton) findViewById(R.id.onion_skin_toggle);
         mOnionskinToggle.setOnChangeLevelListener(mOnionskinChangeLevel);
         ((CompoundButton) findViewById(R.id.grid_toggle)).setOnCheckedChangeListener(this);
 
         findViewById(R.id.done).setOnClickListener(this);
 
         setFullscreen(true);
 
         mLocator = new IncrementalLocator(this);
         mImageCache = ImageCache.getInstance(this);
 
         processIntent(getIntent());
 
         if (savedInstanceState != null) {
             final Uri card = savedInstanceState.getParcelable(INSTANCE_CARD);
             loadCard(card);
         }
     }
 
     private void processIntent(Intent intent) {
         final String action = intent.getAction();
 
         if (ACTION_ADD_PHOTO.equals(action)) {
             mCardDir = null;
             loadCard(intent.getData());
 
         } else if (Intent.ACTION_INSERT.equals(action)) {
             mCard = null;
             mCardDir = intent.getData();
 
         } else {
             Toast.makeText(this, "Unable to handle requested action", Toast.LENGTH_LONG).show();
             setResult(RESULT_CANCELED);
             finish();
         }
     }
 
     private void loadCard(Uri card) {
         mCard = card;
 
         mHandler.sendEmptyMessage(MSG_RELOAD_CARD_AND_MEDIA);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putParcelable(INSTANCE_CARD, mCard);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
 
         mLocator.removeLocationUpdates(mLocationListener);
 
         mImageCache.unregisterOnImageLoadListener(this);
         mPreview.setOnPreviewStartedListener(null);
         if (mCamera != null) {
             mCamera.stopPreview();
         }
 
         mPreviewHolder.removeAllViews();
         mPreview = null;
 
         releaseCamera();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         mLocator.requestLocationUpdates(mLocationListener);
         mImageCache.registerOnImageLoadListener(this);
 
         mCamera = getCameraInstance();
 
         if (mCamera != null) {
             mPreview = new CameraPreview(this, mCamera);
             mPreview.setForceAspectRatio((float) 640 / 480);
             mPreviewHolder.addView(mPreview);
             mPreview.setOnPreviewStartedListener(mOnPreviewStartedListener);
 
         } else {
             Toast.makeText(this, R.string.err_initializing_camera, Toast.LENGTH_LONG).show();
             setResult(RESULT_CANCELED);
             finish();
         }
 
         setOnionSkinVisible(mOnionskinToggle.getLevel());
     }
 
     public void setFullscreen(boolean fullscreen) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             mPreviewHolder.setSystemUiVisibility(  fullscreen ? View.SYSTEM_UI_FLAG_LOW_PROFILE : 0);
         }
 
         if (fullscreen) {
             final Window w = getWindow();
             w.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
             w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
         } else {
             final Window w = getWindow();
             w.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
             w.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         }
     }
 
     /**
      * Loads the given image in the onion skin. This requests the image cache to load it, so it
      * returns immediately.
      *
      * @param image
      */
     private void showOnionskinImage(Uri image) {
         try {
             final Drawable d = mImageCache.loadImage(R.id.camera_preview, image, 640, 480);
             if (d != null) {
                 loadOnionskinImage(d);
             }
         } catch (final IOException e) {
             e.printStackTrace();
         }
     }
 
     private void setOnionSkinVisible(int level) {
 
         mOnionSkin.setVisibility(level > 0 ? View.VISIBLE : View.GONE);
 
         setOnionskinAlphaPercent(level);
     }
 
     private void invalidateOnionskinImage() {
         mOnionSkin.setImageDrawable(null);
         mOnionskinToggle.setEnabled(false);
     }
 
     @SuppressWarnings("deprecation")
     private void setOnionskinAlphaPercent(int percent) {
         mOnionSkin.setAlpha((int) (percent / 100.0 * 255));
     }
 
     private void loadOnionskinImage(Drawable image) {
         mOnionSkin.setImageDrawable(image);
         setOnionskinAlphaPercent(mOnionskinToggle.getLevel());
         mOnionskinToggle.setEnabled(true);
     }
 
     @Override
     public void onImageLoaded(final int id, Uri imageUri, Drawable image) {
         if (R.id.camera_preview == id) {
 
             loadOnionskinImage(image);
         }
     }
 
     // ////////////////////////////////////////////////////////////
     // /// camera
     // ///////////////////////////////////////////////////////////
 
     private void releaseCamera() {
         if (mCamera != null) {
             mCamera.release(); // release the camera for other applications
             mCamera = null;
         }
     }
 
     /** A safe way to get an instance of the Camera object. */
     public static Camera getCameraInstance() {
         Camera c = null;
         try {
             c = Camera.open(); // attempt to get a Camera instance
             final Parameters params = c.getParameters();
             final Size s = getBestPictureSize(640, 480, params);
             params.setPictureSize(s.width, s.height);
             if (Constants.DEBUG) {
                 Log.d(TAG, "best picture size is " + s.width + "x" + s.height);
             }
             c.setParameters(params);
         } catch (final Exception e) {
             Log.e(TAG, "Error acquiring camera", e);
         }
         return c; // returns null if camera is unavailable
     }
 
     private volatile boolean mDelayedCapture;
 
     private void capture() {
         mCaptureButton.setEnabled(false);
 
         if (mAutofocusStarted) {
             // don't capture while autofocusing. Capture will be done on the callback.
             mDelayedCapture = true;
             return;
         }
 
         invalidateOnionskinImage();
 
         try {
             mCamera.takePicture(null, null, mPictureCallback);
             // make this error non-fatal.
         } catch (final RuntimeException re) {
             Toast.makeText(CameraActivity.this, R.string.err_camera_take_picture_failed,
                     Toast.LENGTH_LONG).show();
             Log.e(TAG, "Error taking picture", re);
             setReadyToCapture();
         }
     }
 
     private final PictureCallback mPictureCallback = new PictureCallback() {
 
         @Override
         public void onPictureTaken(byte[] data, Camera camera) {
             mCamera.cancelAutoFocus();
             mCamera.startPreview();
             setFullscreen(true);
             savePicture(data);
         }
     };
 
     /**
      * Saves the picture as a jpeg to disk and adds it as a media item. This method starts a task
      * and returns immediately.
      *
      * @param data
      */
     private void savePicture(byte[] data) {
         new SavePictureTask().execute(data);
     }
 
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.capture:
                 capture();
                 break;
             case R.id.done:
                 setResult(RESULT_OK);
                 finish();
                 // when a new card is added, show the editor immediately afterward.
                 if (mCard != null && Intent.ACTION_INSERT.equals(getIntent().getAction())) {
                     startActivity(new Intent(Intent.ACTION_EDIT, mCard));
                 }
                 break;
         }
     }
 
     @Override
     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         switch (buttonView.getId()) {
 
             case R.id.grid_toggle:
                 findViewById(R.id.grid).setVisibility(isChecked ? View.VISIBLE : View.GONE);
         }
     }
 
     @Override
     public boolean onTouch(View v, MotionEvent event) {
         switch (v.getId()) {
             case R.id.capture:
                 switch (event.getActionMasked()) {
                     case MotionEvent.ACTION_DOWN:
                         mHandler.sendEmptyMessageDelayed(MSG_START_AUTOFOCUS, 500);
                         break;
                     case MotionEvent.ACTION_UP:
                         mHandler.removeMessages(MSG_START_AUTOFOCUS);
                         break;
                 }
                 return false;
             default:
                 return false;
         }
     }
 
     private volatile boolean mAutofocusStarted = false;
 
     /**
      * Called when the shutter button has been pressed and held halfway.
      */
     private void onShutterHalfwayPressed() {
         autoFocus();
     }
 
     private synchronized void autoFocus() {
         if (!mAutofocusStarted) {
             mAutofocusStarted = true;
             mCamera.autoFocus(mAutoFocusCallback);
         }
     }
 
     AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback() {
 
         @Override
         public void onAutoFocus(boolean success, Camera camera) {
             mAutofocusStarted = false;
             if (mDelayedCapture) {
                 if (success) {
                     capture();
                 } else {
                     setReadyToCapture();
                 }
                 mDelayedCapture = false;
             }
         }
     };
 
     // /////////////////////////////////////////////////////////////////////
     // content loading
     // /////////////////////////////////////////////////////////////////////
 
     @Override
     public Loader<Cursor> onCreateLoader(int loader, Bundle args) {
 
         switch (loader) {
             case LOADER_CARD:
                 return new CursorLoader(this, mCard, null, null, null, null);
 
             case LOADER_CARDMEDIA:
                 return new CursorLoader(this, Card.MEDIA.getUri(mCard), CARD_MEDIA_PROJECTION,
                         null, null, CardMedia._ID + " DESC LIMIT 1");
 
             default:
                 return null;
         }
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
         switch (loader.getId()) {
             case LOADER_CARD:
                 if (c.moveToFirst()) {
                     setTitle(Card.getTitle(this, c));
 
                 }
                 break;
 
             case LOADER_CARDMEDIA:
                 showLastPhoto(c);
                 break;
         }
     }
 
     @Override
     public void setTitle(CharSequence title) {
         super.setTitle(title);
         ((TextView) findViewById(R.id.title)).setText(title);
     }
 
     /**
      * Given a card media cursor, load the most recent photo. This assumes that the cursor was
      * queried such that the most recent item is last in the cursor (the default sort does this).
      *
      * @param cardMedia
      */
     private void showLastPhoto(Cursor cardMedia) {
         if (cardMedia.moveToLast()) {
             String localUrl = cardMedia
                     .getString(cardMedia.getColumnIndex(CardMedia.COL_LOCAL_URL));
             if (localUrl == null) {
                 localUrl = cardMedia.getString(cardMedia
                         .getColumnIndexOrThrow(CardMedia.COL_MEDIA_URL));
             }
             if (localUrl != null) {
                 showOnionskinImage(Uri.parse(localUrl));
             }
         }
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> arg0) {
         invalidateOnionskinImage();
     }
 
     private void createNewCard() {
         final ContentValues cv = new ContentValues();
 
         cv.put(Card.COL_TITLE, "");
 
         if (mRecentImage != null) {
             cv.put(Card.COL_THUMBNAIL, mRecentImage.toString());
         }
 
         if (mLocation != null) {
             cv.put(Card.COL_LATITUDE, mLocation.getLatitude());
             cv.put(Card.COL_LONGITUDE, mLocation.getLongitude());
         }
         final Uri card = Card.createNewCard(this,
                 Authenticator.getFirstAccount(this, Authenticator.ACCOUNT_TYPE), cv);
 
         final Intent intent = new Intent(CameraActivity.ACTION_ADD_PHOTO, card);
 
         processIntent(intent);
     }
 
     private final LocationListener mLocationListener = new LocationListener() {
 
         @Override
         public void onStatusChanged(String provider, int status, Bundle extras) {
 
         }
 
         @Override
         public void onProviderEnabled(String provider) {
             // TODO Auto-generated method stub
 
         }
 
         @Override
         public void onProviderDisabled(String provider) {
             // TODO Auto-generated method stub
 
         }
 
         @Override
         public void onLocationChanged(Location location) {
             mLocation = location;
             showLocationAsText(location);
         }
     };
 
     /**
      * Saves the given jpeg bytes to disk and adds an entry to the CardMedia list. Pictures are
      * stored to external storage under {@link StorageUtils#EXTERNAL_PICTURE_SUBDIR}.
      *
      */
     private class SavePictureTask extends AsyncTask<byte[], Long, Uri> {
         private Exception mErr;
 
         @Override
         protected void onPreExecute() {
             CameraActivity.this.setProgressBarIndeterminateVisibility(true);
             super.onPreExecute();
         }
 
         @Override
         protected Uri doInBackground(byte[]... data) {
             if (data == null || data.length == 0 || data[0] == null || data[0].length == 0) {
                 mErr = new IllegalArgumentException("data was null or empty");
                 return null;
             }
             final File externalPicturesDir = StorageUtils
                     .getExternalPictureDir(CameraActivity.this);
 
            if (externalPicturesDir == null) {
                mErr = new RuntimeException("no external storage available");
                return null;
            }

             final String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss.SSSZ", Locale.US)
                     .format(new Date());
             final File outFile = new File(externalPicturesDir, "IMG_" + timeStamp + ".jpg");
 
             externalPicturesDir.mkdirs();
 
             try {
                 final FileOutputStream fos = new FileOutputStream(outFile);
                 fos.write(data[0]);
                 fos.close();
 
                 final Uri mediaUri = Uri.fromFile(outFile);
                 mRecentImage = mediaUri;
                 if (mCard == null && mCardDir != null) {
                     createNewCard();
                 }
 
                 mImageCache.scheduleLoadImage(0, mediaUri, 640, 480);
 
                 final CastMediaInfo cmi = CardMedia.addMediaToCard(CameraActivity.this,
                         Authenticator.getFirstAccount(CameraActivity.this),
                         Card.MEDIA.getUri(mCard), mediaUri);
 
                 return cmi.castMediaItem;
 
             } catch (final IOException e) {
                 mErr = e;
                 return null;
             } catch (final RuntimeException re) {
                 mErr = re;
                 return null;
             } catch (final MediaProcessingException e) {
                 mErr = e;
                 return null;
             }
         }
 
         @Override
         protected void onPostExecute(Uri result) {
             if (mErr != null) {
                 Log.e(TAG, "error writing file", mErr);
                 Toast.makeText(CameraActivity.this, R.string.err_camera_take_picture_failed,
                         Toast.LENGTH_LONG).show();
             }
 
             setReadyToCapture();
         }
     }
 
     private void setReadyToCapture() {
         CameraActivity.this.setProgressBarIndeterminateVisibility(false);
         if (mCamera != null) {
             mCaptureButton.setEnabled(true);
             mCamera.startPreview();
         } else {
             mCaptureButton.setEnabled(false);
         }
     }
 
     private GeocodeTask mGeocodeTask;
 
     /**
      * Displays the given location as text by reverse geocoding it. The result is displayed
      * asynchronously.
      *
      * @param location
      */
     protected void showLocationAsText(Location location) {
         if (mGeocodeTask != null) {
             mGeocodeTask.cancel(true);
         }
         mGeocodeTask = new GeocodeTask(this, (TextView) findViewById(R.id.location));
         mGeocodeTask.execute(location);
     }
 
     /***
      * Copyright (c) 2008-2012 CommonsWare, LLC Licensed under the Apache License, Version 2.0 (the
      * "License"); you may not use this file except in compliance with the License. You may obtain a
      * copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by
      * applicable law or agreed to in writing, software distributed under the License is distributed
      * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      * See the License for the specific language governing permissions and limitations under the
      * License. From _The Busy Coder's Guide to Advanced Android Development_
      * http://commonsware.com/AdvAndroid
      */
 
     /**
      * Finds the highest resolution picture size that fits within the given width and height.
      *
      * @param width
      * @param height
      * @param parameters
      * @return
      */
     public static Camera.Size getBestPictureSize(int width, int height, Camera.Parameters parameters) {
         Camera.Size result = null;
 
         for (final Camera.Size size : parameters.getSupportedPictureSizes()) {
             if (size.width <= width && size.height <= height) {
                 if (result == null) {
                     result = size;
                 } else {
                     final int resultArea = result.width * result.height;
                     final int newArea = size.width * size.height;
 
                     if (newArea > resultArea) {
                         result = size;
                     }
                 }
             }
         }
 
         return result;
     }
 
     @Override
     public void onImageLoaded(long id, Uri imageUri, Drawable image) {
         // TODO Auto-generated method stub
 
     }
 }
