 /*
  * Copyright (C) 2012 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.gallery3d.filtershow;
 
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.app.WallpaperManager;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SeekBar;
 import android.widget.ShareActionProvider;
 import android.widget.ShareActionProvider.OnShareTargetSelectedListener;
 import android.widget.Toast;
 
 import com.android.gallery3d.R;
 import com.android.gallery3d.data.LocalAlbum;
 import com.android.gallery3d.filtershow.cache.ImageLoader;
 import com.android.gallery3d.filtershow.editors.EditorManager;
 import com.android.gallery3d.filtershow.filters.FiltersManager;
 import com.android.gallery3d.filtershow.filters.ImageFilter;
 import com.android.gallery3d.filtershow.filters.ImageFilterBorder;
 import com.android.gallery3d.filtershow.filters.ImageFilterFx;
 import com.android.gallery3d.filtershow.filters.ImageFilterParametricBorder;
 import com.android.gallery3d.filtershow.filters.ImageFilterRS;
 import com.android.gallery3d.filtershow.filters.ImageFilterRedEye;
 import com.android.gallery3d.filtershow.imageshow.ImageCrop;
 import com.android.gallery3d.filtershow.imageshow.ImageDraw;
 import com.android.gallery3d.filtershow.imageshow.ImageFlip;
 import com.android.gallery3d.filtershow.imageshow.ImageRedEyes;
 import com.android.gallery3d.filtershow.imageshow.ImageRotate;
 import com.android.gallery3d.filtershow.imageshow.ImageShow;
 import com.android.gallery3d.filtershow.imageshow.ImageStraighten;
 import com.android.gallery3d.filtershow.imageshow.ImageTinyPlanet;
 import com.android.gallery3d.filtershow.imageshow.ImageZoom;
 import com.android.gallery3d.filtershow.imageshow.MasterImage;
 import com.android.gallery3d.filtershow.presets.ImagePreset;
 import com.android.gallery3d.filtershow.provider.SharedImageProvider;
 import com.android.gallery3d.filtershow.tools.SaveCopyTask;
 import com.android.gallery3d.filtershow.ui.FilterIconButton;
 import com.android.gallery3d.filtershow.ui.FramedTextButton;
 import com.android.gallery3d.filtershow.ui.ImageCurves;
 import com.android.gallery3d.filtershow.ui.Spline;
 import com.android.gallery3d.util.GalleryUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.ref.WeakReference;
 import java.util.Vector;
 
 @TargetApi(16)
 public class FilterShowActivity extends Activity implements OnItemClickListener,
         OnShareTargetSelectedListener {
 
     // fields for supporting crop action
     public static final String CROP_ACTION = "com.android.camera.action.CROP";
     private CropExtras mCropExtras = null;
     MasterImage mMasterImage = MasterImage.getImage();
 
     public static final String TINY_PLANET_ACTION = "com.android.camera.action.TINY_PLANET";
     public static final String LAUNCH_FULLSCREEN = "launch-fullscreen";
     public static final int MAX_BMAP_IN_INTENT = 990000;
     private final PanelController mPanelController = new PanelController();
     private ImageLoader mImageLoader = null;
     private ImageShow mImageShow = null;
     private ImageRedEyes mImageRedEyes = null;
     private ImageDraw mImageDraw = null;
     private ImageStraighten mImageStraighten = null;
     private ImageCrop mImageCrop = null;
     private ImageRotate mImageRotate = null;
     private ImageFlip mImageFlip = null;
     private ImageTinyPlanet mImageTinyPlanet = null;
 
     private View mListFx = null;
     private View mListBorders = null;
     private View mListGeometry = null;
     private View mListColors = null;
     private View mListFilterButtons = null;
     private View mSaveButton = null;
 
     private ImageButton mFxButton = null;
     private ImageButton mBorderButton = null;
     private ImageButton mGeometryButton = null;
     private ImageButton mColorsButton = null;
 
     private LinearLayout listColors = null;
     private LinearLayout listFilters = null;
     private LinearLayout listBorders = null;
 
     private EditorPlaceHolder mEditorPlaceHolder = new EditorPlaceHolder(this);
 
     private static final int SELECT_PICTURE = 1;
     private static final String LOGTAG = "FilterShowActivity";
     protected static final boolean ANIMATE_PANELS = true;
     private static int mImageBorderSize = 4; // in percent
 
     private boolean mShowingHistoryPanel = false;
     private boolean mShowingImageStatePanel = false;
 
     private final Vector<ImageShow> mImageViews = new Vector<ImageShow>();
     private final Vector<View> mListViews = new Vector<View>();
     private final Vector<ImageButton> mBottomPanelButtons = new Vector<ImageButton>();
 
     private ShareActionProvider mShareActionProvider;
     private File mSharedOutputFile = null;
 
     private boolean mSharingImage = false;
 
     private WeakReference<ProgressDialog> mSavingProgressDialog;
     private static final int SEEK_BAR_MAX = 600;
 
     private LoadBitmapTask mLoadBitmapTask;
     private FilterIconButton mNullFxFilter;
     private FilterIconButton mNullBorderFilter;
     private int mIconSeedSize = 140;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setupMasterImage();
         ImageFilterRS.setRenderScriptContext(this);
 
         ImageShow.setDefaultBackgroundColor(getResources().getColor(R.color.background_screen));
         // TODO: get those values from XML.
         ImageZoom.setZoomedSize(getPixelsFromDip(256));
         FramedTextButton.setTextSize((int) getPixelsFromDip(14));
         FramedTextButton.setTrianglePadding((int) getPixelsFromDip(4));
         FramedTextButton.setTriangleSize((int) getPixelsFromDip(10));
         ImageShow.setTextSize((int) getPixelsFromDip(12));
         ImageShow.setTextPadding((int) getPixelsFromDip(10));
         ImageShow.setOriginalTextMargin((int) getPixelsFromDip(4));
         ImageShow.setOriginalTextSize((int) getPixelsFromDip(18));
         ImageShow.setOriginalText(getResources().getString(R.string.original_picture_text));
         mIconSeedSize = getResources().getDimensionPixelSize(R.dimen.thumbnail_size);
 
         Drawable curveHandle = getResources().getDrawable(R.drawable.camera_crop);
         int curveHandleSize = (int) getResources().getDimension(R.dimen.crop_indicator_size);
         Spline.setCurveHandle(curveHandle, curveHandleSize);
         Spline.setCurveWidth((int) getPixelsFromDip(3));
 
         setContentView(R.layout.filtershow_activity);
         ActionBar actionBar = getActionBar();
         actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
         actionBar.setCustomView(R.layout.filtershow_actionbar);
 
         mSaveButton = actionBar.getCustomView();
         mSaveButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View view) {
                 saveImage();
             }
         });
 
         mImageLoader = new ImageLoader(this, getApplicationContext());
 
         listFilters = (LinearLayout) findViewById(R.id.listFilters);
         listBorders = (LinearLayout) findViewById(R.id.listBorders);
         listColors = (LinearLayout) findViewById(R.id.listColorsFx);
 
         mImageShow = (ImageShow) findViewById(R.id.imageShow);
         mImageStraighten = (ImageStraighten) findViewById(R.id.imageStraighten);
         mImageCrop = (ImageCrop) findViewById(R.id.imageCrop);
         mImageRotate = (ImageRotate) findViewById(R.id.imageRotate);
         mImageFlip = (ImageFlip) findViewById(R.id.imageFlip);
         mImageTinyPlanet = (ImageTinyPlanet) findViewById(R.id.imageTinyPlanet);
         mImageRedEyes = (ImageRedEyes) findViewById(R.id.imageRedEyes);
         mImageDraw = (ImageDraw) findViewById(R.id.imageDraw);
 
         mImageCrop.setAspectTextSize((int) getPixelsFromDip(18));
         ImageCrop.setTouchTolerance((int) getPixelsFromDip(25));
         ImageCrop.setMinCropSize((int) getPixelsFromDip(55));
         mImageViews.add(mImageShow);
         mImageViews.add(mImageStraighten);
         mImageViews.add(mImageCrop);
         mImageViews.add(mImageRotate);
         mImageViews.add(mImageFlip);
         mImageViews.add(mImageTinyPlanet);
         mImageViews.add(mImageRedEyes);
 
         mEditorPlaceHolder.setContainer((FrameLayout) findViewById(R.id.editorContainer));
         EditorManager.addEditors(mEditorPlaceHolder);
         mEditorPlaceHolder.setOldViews(mImageViews);
         mEditorPlaceHolder.setImageLoader(mImageLoader);
 
         mListFx = findViewById(R.id.fxList);
         mListBorders = findViewById(R.id.bordersList);
         mListGeometry = findViewById(R.id.geometryList);
         mListFilterButtons = findViewById(R.id.filterButtonsList);
         mListColors = findViewById(R.id.colorsFxList);
         mListViews.add(mListFx);
         mListViews.add(mListBorders);
         mListViews.add(mListGeometry);
         mListViews.add(mListFilterButtons);
         mListViews.add(mListColors);
 
         mFxButton = (ImageButton) findViewById(R.id.fxButton);
         mBorderButton = (ImageButton) findViewById(R.id.borderButton);
         mGeometryButton = (ImageButton) findViewById(R.id.geometryButton);
         mColorsButton = (ImageButton) findViewById(R.id.colorsButton);
 
         mBottomPanelButtons.add(mFxButton);
         mBottomPanelButtons.add(mBorderButton);
         mBottomPanelButtons.add(mGeometryButton);
         mBottomPanelButtons.add(mColorsButton);
 
         mImageShow.setImageLoader(mImageLoader);
         mImageStraighten.setImageLoader(mImageLoader);
         mImageCrop.setImageLoader(mImageLoader);
         mImageRotate.setImageLoader(mImageLoader);
         mImageFlip.setImageLoader(mImageLoader);
         mImageTinyPlanet.setImageLoader(mImageLoader);
         mImageRedEyes.setImageLoader(mImageLoader);
         mImageDraw.setImageLoader(mImageLoader);
 
         mPanelController.setActivity(this);
         mPanelController.setEditorPlaceHolder(mEditorPlaceHolder);
 
         mPanelController.addImageView(findViewById(R.id.imageShow));
         mPanelController.addImageView(findViewById(R.id.imageStraighten));
         mPanelController.addImageView(findViewById(R.id.imageCrop));
         mPanelController.addImageView(findViewById(R.id.imageRotate));
         mPanelController.addImageView(findViewById(R.id.imageFlip));
         mPanelController.addImageView(findViewById(R.id.imageTinyPlanet));
         mPanelController.addImageView(findViewById(R.id.imageRedEyes));
         mPanelController.addImageView(findViewById(R.id.imageDraw));
 
         mPanelController.addPanel(mFxButton, mListFx, 0);
         mPanelController.addPanel(mBorderButton, mListBorders, 1);
 
         mPanelController.addPanel(mGeometryButton, mListGeometry, 2);
         mPanelController.addComponent(mGeometryButton, findViewById(R.id.straightenButton));
         mPanelController.addComponent(mGeometryButton, findViewById(R.id.cropButton));
         mPanelController.addComponent(mGeometryButton, findViewById(R.id.rotateButton));
         mPanelController.addComponent(mGeometryButton, findViewById(R.id.flipButton));
         mPanelController.addComponent(mGeometryButton, findViewById(R.id.redEyeButton));
 
         mPanelController.addPanel(mColorsButton, mListColors, 3);
 
         Vector<ImageFilter> filters = new Vector<ImageFilter>();
         FiltersManager.addFilters(filters, mImageLoader);
 
         for (ImageFilter filter : filters) {
             filter.setParameter(filter.getDefaultParameter());
             filter.setName(getString(filter.getTextId()));
             setupFilterButton(filter, listColors, mColorsButton);
         }
         mPanelController.addFilter(new ImageFilterRedEye());
 
         mPanelController.addView(findViewById(R.id.applyEffect));
         findViewById(R.id.resetOperationsButton).setOnClickListener(
                 createOnClickResetOperationsButton());
 
         ListView operationsList = (ListView) findViewById(R.id.operationsList);
         operationsList.setAdapter(mMasterImage.getHistory());
         operationsList.setOnItemClickListener(this);
         ListView imageStateList = (ListView) findViewById(R.id.imageStateList);
         imageStateList.setAdapter(mMasterImage.getState());
         mImageLoader.setAdapter(mMasterImage.getHistory());
 
         fillListImages(listFilters);
         fillListBorders(listBorders);
 
         SeekBar seekBar = (SeekBar) findViewById(R.id.filterSeekBar);
         seekBar.setMax(SEEK_BAR_MAX);
 
         mImageShow.setSeekBar(seekBar);
         mImageTinyPlanet.setSeekBar(seekBar);
         mPanelController.setRowPanel(findViewById(R.id.secondRowPanel));
         mPanelController.setUtilityPanel(this, findViewById(R.id.filterButtonsList),
                 findViewById(R.id.panelAccessoryViewList),
                 findViewById(R.id.applyEffect));
 
         mPanelController.setCurrentPanel(mFxButton);
         Intent intent = getIntent();
         if (intent.getBooleanExtra(LAUNCH_FULLSCREEN, false)) {
             getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
         }
 
         if (intent.getData() != null) {
             startLoadBitmap(intent.getData());
         } else {
             pickImage();
         }
 
         // Handle behavior for various actions
         String action = intent.getAction();
         if (action.equalsIgnoreCase(CROP_ACTION)) {
             Bundle extras = intent.getExtras();
             if (extras != null) {
                 mCropExtras = new CropExtras(extras.getInt(CropExtras.KEY_OUTPUT_X, 0),
                         extras.getInt(CropExtras.KEY_OUTPUT_Y, 0),
                         extras.getBoolean(CropExtras.KEY_SCALE, true) &&
                                 extras.getBoolean(CropExtras.KEY_SCALE_UP_IF_NEEDED, false),
                         extras.getInt(CropExtras.KEY_ASPECT_X, 0),
                         extras.getInt(CropExtras.KEY_ASPECT_Y, 0),
                         extras.getBoolean(CropExtras.KEY_SET_AS_WALLPAPER, false),
                         extras.getBoolean(CropExtras.KEY_RETURN_DATA, false),
                         (Uri) extras.getParcelable(MediaStore.EXTRA_OUTPUT),
                         extras.getString(CropExtras.KEY_OUTPUT_FORMAT),
                         extras.getBoolean(CropExtras.KEY_SHOW_WHEN_LOCKED, false),
                         extras.getFloat(CropExtras.KEY_SPOTLIGHT_X),
                         extras.getFloat(CropExtras.KEY_SPOTLIGHT_Y));
 
                 if (mCropExtras.getShowWhenLocked()) {
                     getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                 }
                 mImageShow.getImagePreset().mGeoData.setCropExtras(mCropExtras);
 
                 mImageCrop.setExtras(mCropExtras);
                 String s = getString(R.string.Fixed);
                 mImageCrop.setAspectString(s);
                 mImageCrop.setCropActionFlag(true);
                 mPanelController.setFixedAspect(mCropExtras.getAspectX() > 0
                         && mCropExtras.getAspectY() > 0);
             }
             mPanelController.showComponent(findViewById(R.id.cropButton));
         } else if (action.equalsIgnoreCase(TINY_PLANET_ACTION)) {
             mPanelController.showComponent(findViewById(R.id.tinyplanetButton));
         }
     }
 
     private void startLoadBitmap(Uri uri) {
         final View filters = findViewById(R.id.filtersPanel);
         final View loading = findViewById(R.id.loading);
         loading.setVisibility(View.VISIBLE);
         filters.setVisibility(View.INVISIBLE);
         View tinyPlanetView = findViewById(R.id.tinyplanetButton);
         if (tinyPlanetView != null) {
             tinyPlanetView.setVisibility(View.GONE);
         }
         mLoadBitmapTask = new LoadBitmapTask(tinyPlanetView);
         mLoadBitmapTask.execute(uri);
     }
 
     private class LoadBitmapTask extends AsyncTask<Uri, Boolean, Boolean> {
         View mTinyPlanetButton;
         int mBitmapSize;
 
         public LoadBitmapTask(View button) {
             mTinyPlanetButton = button;
             mBitmapSize = getScreenImageSize();
         }
 
         @Override
         protected Boolean doInBackground(Uri... params) {
             if (!mImageLoader.loadBitmap(params[0], mBitmapSize)) {
                 return false;
             }
             publishProgress(mImageLoader.queryLightCycle360());
             return true;
         }
 
         @Override
         protected void onProgressUpdate(Boolean... values) {
             super.onProgressUpdate(values);
             if (isCancelled()) {
                 return;
             }
             final View filters = findViewById(R.id.filtersPanel);
             final View loading = findViewById(R.id.loading);
             loading.setVisibility(View.GONE);
             filters.setVisibility(View.VISIBLE);
             if (values[0]) {
                 mTinyPlanetButton.setVisibility(View.VISIBLE);
             }
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
             if (isCancelled()) {
                 return;
             }
             if (!result) {
                 cannotLoadImage();
             }
 
            Bitmap bmap = mImageLoader.getOriginalBitmapSmall();
             if (bmap != null && bmap.getWidth() > 0 && bmap.getHeight() > 0) {
                 float w = bmap.getWidth();
                 float h = bmap.getHeight();
                 float f = mIconSeedSize / Math.min(w, h);
                 w = w * f;
                 h = h * f;
                 bmap = Bitmap.createScaledBitmap(bmap, (int) w, (int) h, true);
 
                 int num_colors_buttons = listColors.getChildCount();
                 for (int i = 0; i < num_colors_buttons; i++) {
                     FilterIconButton b = (FilterIconButton) listColors.getChildAt(i);
 
                     b.setIcon(bmap);
                 }
                 int num_filters_buttons = listFilters.getChildCount();
                 for (int i = 0; i < num_filters_buttons; i++) {
                     FilterIconButton b = (FilterIconButton) listFilters.getChildAt(i);
 
                     b.setIcon(bmap);
                 }
                 int num_borders_buttons = listBorders.getChildCount();
                 for (int i = 0; i < num_borders_buttons; i++) {
                     FilterIconButton b = (FilterIconButton) listBorders.getChildAt(i);
 
                     b.setIcon(bmap);
                 }
 
             }
             mLoadBitmapTask = null;
             super.onPostExecute(result);
         }
 
     }
 
     @Override
     protected void onDestroy() {
         if (mLoadBitmapTask != null) {
             mLoadBitmapTask.cancel(false);
         }
         super.onDestroy();
     }
 
     private int translateMainPanel(View viewPanel) {
         int accessoryPanelWidth = viewPanel.getWidth();
         int mainViewWidth = findViewById(R.id.mainView).getWidth();
         int mainPanelWidth = mImageShow.getDisplayedImageBounds().width();
         if (mainPanelWidth == 0) {
             mainPanelWidth = mainViewWidth;
         }
         int filtersPanelWidth = findViewById(R.id.filtersPanel).getWidth();
         if (mainPanelWidth < filtersPanelWidth) {
             mainPanelWidth = filtersPanelWidth;
         }
         int leftOver = mainViewWidth - mainPanelWidth - accessoryPanelWidth;
         if (leftOver < 0) {
             return -accessoryPanelWidth;
         }
         return 0;
     }
 
     private int getScreenImageSize() {
         DisplayMetrics metrics = new DisplayMetrics();
         Display display = getWindowManager().getDefaultDisplay();
         Point size = new Point();
         display.getSize(size);
         display.getMetrics(metrics);
         int msize = Math.min(size.x, size.y);
         return (133 * msize) / metrics.densityDpi;
     }
 
     private void showSavingProgress(String albumName) {
         ProgressDialog progress;
         if (mSavingProgressDialog != null) {
             progress = mSavingProgressDialog.get();
             if (progress != null) {
                 progress.show();
                 return;
             }
         }
         // TODO: Allow cancellation of the saving process
         String progressText;
         if (albumName == null) {
             progressText = getString(R.string.saving_image);
         } else {
             progressText = getString(R.string.filtershow_saving_image, albumName);
         }
         progress = ProgressDialog.show(this, "", progressText, true, false);
         mSavingProgressDialog = new WeakReference<ProgressDialog>(progress);
     }
 
     private void hideSavingProgress() {
         if (mSavingProgressDialog != null) {
             ProgressDialog progress = mSavingProgressDialog.get();
             if (progress != null)
                 progress.dismiss();
         }
     }
 
     public void completeSaveImage(Uri saveUri) {
         if (mSharingImage && mSharedOutputFile != null) {
             // Image saved, we unblock the content provider
             Uri uri = Uri.withAppendedPath(SharedImageProvider.CONTENT_URI,
                     Uri.encode(mSharedOutputFile.getAbsolutePath()));
             ContentValues values = new ContentValues();
             values.put(SharedImageProvider.PREPARE, false);
             getContentResolver().insert(uri, values);
         }
         setResult(RESULT_OK, new Intent().setData(saveUri));
         hideSavingProgress();
         finish();
     }
 
     @Override
     public boolean onShareTargetSelected(ShareActionProvider arg0, Intent arg1) {
         // First, let's tell the SharedImageProvider that it will need to wait
         // for the image
         Uri uri = Uri.withAppendedPath(SharedImageProvider.CONTENT_URI,
                 Uri.encode(mSharedOutputFile.getAbsolutePath()));
         ContentValues values = new ContentValues();
         values.put(SharedImageProvider.PREPARE, true);
         getContentResolver().insert(uri, values);
         mSharingImage = true;
 
         // Process and save the image in the background.
         showSavingProgress(null);
         mImageShow.saveImage(this, mSharedOutputFile);
         return true;
     }
 
     private Intent getDefaultShareIntent() {
         Intent intent = new Intent(Intent.ACTION_SEND);
         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
         intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
         intent.setType(SharedImageProvider.MIME_TYPE);
         mSharedOutputFile = SaveCopyTask.getNewFile(this, mImageLoader.getUri());
         Uri uri = Uri.withAppendedPath(SharedImageProvider.CONTENT_URI,
                 Uri.encode(mSharedOutputFile.getAbsolutePath()));
         intent.putExtra(Intent.EXTRA_STREAM, uri);
         return intent;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.filtershow_activity_menu, menu);
         MenuItem showHistory = menu.findItem(R.id.operationsButton);
         if (mShowingHistoryPanel) {
             showHistory.setTitle(R.string.hide_history_panel);
         } else {
             showHistory.setTitle(R.string.show_history_panel);
         }
         MenuItem showState = menu.findItem(R.id.showImageStateButton);
         if (mShowingImageStatePanel) {
             showState.setTitle(R.string.hide_imagestate_panel);
         } else {
             showState.setTitle(R.string.show_imagestate_panel);
         }
         mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.menu_share)
                 .getActionProvider();
         mShareActionProvider.setShareIntent(getDefaultShareIntent());
         mShareActionProvider.setOnShareTargetSelectedListener(this);
 
         MenuItem undoItem = menu.findItem(R.id.undoButton);
         MenuItem redoItem = menu.findItem(R.id.redoButton);
         MenuItem resetItem = menu.findItem(R.id.resetHistoryButton);
         mMasterImage.getHistory().setMenuItems(undoItem, redoItem, resetItem);
         return true;
     }
 
     @Override
     public void onPause() {
         super.onPause();
         if (mShareActionProvider != null) {
             mShareActionProvider.setOnShareTargetSelectedListener(null);
         }
     }
 
     @Override
     public void onResume() {
         super.onResume();
         if (mShareActionProvider != null) {
             mShareActionProvider.setOnShareTargetSelectedListener(this);
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.undoButton: {
                 HistoryAdapter adapter = mMasterImage.getHistory();
                 int position = adapter.undo();
                 mMasterImage.onHistoryItemClick(position);
                 mImageShow.showToast("Undo");
                 invalidateViews();
                 return true;
             }
             case R.id.redoButton: {
                 HistoryAdapter adapter = mMasterImage.getHistory();
                 int position = adapter.redo();
                 mMasterImage.onHistoryItemClick(position);
                 mImageShow.showToast("Redo");
                 invalidateViews();
                 return true;
             }
             case R.id.resetHistoryButton: {
                 resetHistory();
                 return true;
             }
             case R.id.showImageStateButton: {
                 toggleImageStatePanel();
                 return true;
             }
             case R.id.operationsButton: {
                 toggleHistoryPanel();
                 return true;
             }
             case android.R.id.home: {
                 saveImage();
                 return true;
             }
         }
         return false;
     }
 
     public void enableSave(boolean enable) {
         if (mSaveButton != null)
             mSaveButton.setEnabled(enable);
     }
 
     public FilterIconButton setupFilterButton(ImageFilter filter, LinearLayout panel, View button) {
         LayoutInflater inflater =
                 (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         FilterIconButton icon = (FilterIconButton) inflater.inflate(R.layout.filtericonbutton,
                 panel, false);
         String text = filter.getName();
         if (filter instanceof ImageFilterBorder || filter instanceof ImageFilterParametricBorder) {
             text = "";
         }
         icon.setup(text, filter, this, panel);
         icon.setId(filter.getButtonId());
         mPanelController.addComponent(button, icon);
         mPanelController.addFilter(filter);
         panel.addView(icon);
         return icon;
     }
 
     private void fillListImages(LinearLayout listFilters) {
         // TODO: use listview
         // TODO: load the filters straight from the filesystem
 
         ImageFilterFx[] fxArray = new ImageFilterFx[18];
         int p = 0;
 
         int[] drawid = {
                 R.drawable.filtershow_fx_0005_punch,
                 R.drawable.filtershow_fx_0000_vintage,
                 R.drawable.filtershow_fx_0004_bw_contrast,
                 R.drawable.filtershow_fx_0002_bleach,
                 R.drawable.filtershow_fx_0001_instant,
                 R.drawable.filtershow_fx_0007_washout,
                 R.drawable.filtershow_fx_0003_blue_crush,
                 R.drawable.filtershow_fx_0008_washout_color,
                 R.drawable.filtershow_fx_0006_x_process
         };
 
         int[] fxNameid = {
                 R.string.ffx_punch,
                 R.string.ffx_vintage,
                 R.string.ffx_bw_contrast,
                 R.string.ffx_bleach,
                 R.string.ffx_instant,
                 R.string.ffx_washout,
                 R.string.ffx_blue_crush,
                 R.string.ffx_washout_color,
                 R.string.ffx_x_process
         };
 
         ImagePreset preset = new ImagePreset(getString(R.string.history_original)); // empty
         preset.setImageLoader(mImageLoader);
 
         BitmapFactory.Options o = new BitmapFactory.Options();
         o.inScaled = false;
 
         for (int i = 0; i < drawid.length; i++) {
             Bitmap b = BitmapFactory.decodeResource(getResources(), drawid[i], o);
             fxArray[p++] = new ImageFilterFx(b, getString(fxNameid[i]), fxNameid[i]);
         }
 
         ImageFilterFx nullFilter = new ImageFilterFx(null, getString(R.string.none), R.string.none);
         mNullFxFilter = setupFilterButton(nullFilter, listFilters, mFxButton);
         mNullFxFilter.setSelected(true);
 
         for (int i = 0; i < p; i++) {
             setupFilterButton(fxArray[i], listFilters, mFxButton);
         }
 
         // Default preset (original)
         mMasterImage.setPreset(preset, true);
     }
 
     private void fillListBorders(LinearLayout listBorders) {
         // TODO: use listview
         // TODO: load the borders straight from the filesystem
         int p = 0;
         ImageFilter[] borders = new ImageFilter[12];
         borders[p++] = new ImageFilterBorder(null);
 
         Drawable npd1 = getResources().getDrawable(R.drawable.filtershow_border_4x5);
         borders[p++] = new ImageFilterBorder(npd1);
         Drawable npd2 = getResources().getDrawable(R.drawable.filtershow_border_brush);
         borders[p++] = new ImageFilterBorder(npd2);
         Drawable npd3 = getResources().getDrawable(R.drawable.filtershow_border_grunge);
         borders[p++] = new ImageFilterBorder(npd3);
         Drawable npd4 = getResources().getDrawable(R.drawable.filtershow_border_sumi_e);
         borders[p++] = new ImageFilterBorder(npd4);
         Drawable npd5 = getResources().getDrawable(R.drawable.filtershow_border_tape);
         borders[p++] = new ImageFilterBorder(npd5);
         borders[p++] = new ImageFilterParametricBorder(Color.BLACK, mImageBorderSize, 0);
         borders[p++] = new ImageFilterParametricBorder(Color.BLACK, mImageBorderSize,
                 mImageBorderSize);
         borders[p++] = new ImageFilterParametricBorder(Color.WHITE, mImageBorderSize, 0);
         borders[p++] = new ImageFilterParametricBorder(Color.WHITE, mImageBorderSize,
                 mImageBorderSize);
         int creamColor = Color.argb(255, 237, 237, 227);
         borders[p++] = new ImageFilterParametricBorder(creamColor, mImageBorderSize, 0);
         borders[p++] = new ImageFilterParametricBorder(creamColor, mImageBorderSize,
                 mImageBorderSize);
 
         for (int i = 0; i < p; i++) {
             borders[i].setName(getString(R.string.borders));
             FilterIconButton b = setupFilterButton(borders[i], listBorders, mBorderButton);
             if (i == 0) {
                 mNullBorderFilter = b;
                 mNullBorderFilter.setSelected(true);
             }
         }
     }
 
     // //////////////////////////////////////////////////////////////////////////////
     // Some utility functions
     // TODO: finish the cleanup.
 
     public void showOriginalViews(boolean value) {
         for (ImageShow views : mImageViews) {
             views.showOriginal(value);
         }
     }
 
     public void invalidateViews() {
         for (ImageShow views : mImageViews) {
             views.invalidate();
             views.updateImage();
         }
     }
 
     public void hideListViews() {
         for (View view : mListViews) {
             view.setVisibility(View.GONE);
         }
     }
 
     public void hideImageViews() {
         mImageShow.setShowControls(false); // reset
         for (View view : mImageViews) {
             view.setVisibility(View.GONE);
         }
     }
 
     public void unselectBottomPanelButtons() {
         for (ImageButton button : mBottomPanelButtons) {
             button.setSelected(false);
         }
     }
 
     public void unselectPanelButtons(Vector<ImageButton> buttons) {
         for (ImageButton button : buttons) {
             button.setSelected(false);
         }
     }
 
     public void disableFilterButtons() {
         for (ImageButton b : mBottomPanelButtons) {
             b.setEnabled(false);
             b.setClickable(false);
             b.setAlpha(0.4f);
         }
     }
 
     public void enableFilterButtons() {
         for (ImageButton b : mBottomPanelButtons) {
             b.setEnabled(true);
             b.setClickable(true);
             b.setAlpha(1.0f);
         }
     }
 
     // //////////////////////////////////////////////////////////////////////////////
     // imageState panel...
 
     public boolean isShowingHistoryPanel() {
         return mShowingHistoryPanel;
     }
 
     private void toggleImageStatePanel() {
         final View view = findViewById(R.id.mainPanel);
         final View viewList = findViewById(R.id.imageStatePanel);
 
         if (mShowingHistoryPanel) {
             findViewById(R.id.historyPanel).setVisibility(View.INVISIBLE);
             mShowingHistoryPanel = false;
         }
 
         int translate = translateMainPanel(viewList);
         if (!mShowingImageStatePanel) {
             mShowingImageStatePanel = true;
             view.animate().setDuration(200).x(translate)
                     .withLayer().withEndAction(new Runnable() {
                         @Override
                         public void run() {
                             viewList.setAlpha(0);
                             viewList.setVisibility(View.VISIBLE);
                             viewList.animate().setDuration(100)
                                     .alpha(1.0f).start();
                         }
                     }).start();
         } else {
             mShowingImageStatePanel = false;
             viewList.setVisibility(View.INVISIBLE);
             view.animate().setDuration(200).x(0).withLayer()
                     .start();
         }
         invalidateOptionsMenu();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig)
     {
         super.onConfigurationChanged(newConfig);
         if (mShowingHistoryPanel) {
             toggleHistoryPanel();
         }
     }
 
     public void setupMasterImage() {
         HistoryAdapter mHistoryAdapter = new HistoryAdapter(
                 this, R.layout.filtershow_history_operation_row,
                 R.id.rowTextView);
         ImageStateAdapter mImageStateAdapter = new ImageStateAdapter(this,
                 R.layout.filtershow_imagestate_row);
 
         mMasterImage.setHistoryAdapter(mHistoryAdapter);
         mMasterImage.setStateAdapter(mImageStateAdapter);
         mMasterImage.setActivity(this);
     }
 
     // //////////////////////////////////////////////////////////////////////////////
     // history panel...
 
     public void toggleHistoryPanel() {
         final View view = findViewById(R.id.mainPanel);
         final View viewList = findViewById(R.id.historyPanel);
 
         if (mShowingImageStatePanel) {
             findViewById(R.id.imageStatePanel).setVisibility(View.INVISIBLE);
             mShowingImageStatePanel = false;
         }
 
         int translate = translateMainPanel(viewList);
         if (!mShowingHistoryPanel) {
             mShowingHistoryPanel = true;
             view.animate().setDuration(200).x(translate)
                     .withLayer().withEndAction(new Runnable() {
                         @Override
                         public void run() {
                             viewList.setAlpha(0);
                             viewList.setVisibility(View.VISIBLE);
                             viewList.animate().setDuration(100)
                                     .alpha(1.0f).start();
                         }
                     }).start();
         } else {
             mShowingHistoryPanel = false;
             viewList.setVisibility(View.INVISIBLE);
             view.animate().setDuration(200).x(0).withLayer()
                     .start();
         }
         invalidateOptionsMenu();
     }
 
     public void dispatchNullFilterClick() {
         mNullFxFilter.onClick(mNullFxFilter);
         mNullBorderFilter.onClick(mNullBorderFilter);
     }
 
     void resetHistory() {
         dispatchNullFilterClick();
         HistoryAdapter adapter = mMasterImage.getHistory();
         adapter.reset();
         ImagePreset original = new ImagePreset(adapter.getItem(0));
         mMasterImage.setPreset(original, true);
         mPanelController.resetParameters();
         invalidateViews();
     }
 
     // reset button in the history panel.
     private OnClickListener createOnClickResetOperationsButton() {
         return new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 resetHistory();
             }
         };
     }
 
     @Override
     public void onBackPressed() {
         if (mPanelController.onBackPressed()) {
             saveImage();
         }
     }
 
     public void cannotLoadImage() {
         CharSequence text = getString(R.string.cannot_load_image);
         Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
         toast.show();
         finish();
     }
 
     // //////////////////////////////////////////////////////////////////////////////
 
     public float getPixelsFromDip(float value) {
         Resources r = getResources();
         return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                 r.getDisplayMetrics());
     }
 
     public void useFilter(ImageFilter filter) {
         if (mMasterImage.getCurrentFilter() == filter) {
             return;
         }
         ImagePreset oldPreset = mMasterImage.getPreset();
         ImagePreset copy = new ImagePreset(oldPreset);
         mMasterImage.setPreset(copy, true);
         // TODO: use a numerical constant instead.
 
         ImagePreset current = mMasterImage.getPreset();
         ImageFilter existingFilter = current.getFilter(filter.getName());
         if (existingFilter == null) {
             current.add(filter);
         }
         existingFilter = current.getFilter(filter.getName());
         mMasterImage.setCurrentFilter(existingFilter);
         invalidateViews();
     }
 
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position,
             long id) {
         mMasterImage.onHistoryItemClick(position);
         invalidateViews();
     }
 
     public void pickImage() {
         Intent intent = new Intent();
         intent.setType("image/*");
         intent.setAction(Intent.ACTION_GET_CONTENT);
         startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)),
                 SELECT_PICTURE);
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode == RESULT_OK) {
             if (requestCode == SELECT_PICTURE) {
                 Uri selectedImageUri = data.getData();
                 startLoadBitmap(selectedImageUri);
             }
         }
     }
 
     private boolean mSaveToExtraUri = false;
     private boolean mSaveAsWallpaper = false;
     private boolean mReturnAsExtra = false;
     private boolean outputted = false;
 
     public void saveImage() {
         if (mCropExtras != null) {
             if (mCropExtras.getExtraOutput() != null) {
                 mSaveToExtraUri = true;
                 outputted = true;
             }
             if (mCropExtras.getSetAsWallpaper()) {
                 mSaveAsWallpaper = true;
                 outputted = true;
             }
             if (mCropExtras.getReturnData()) {
 
                 mReturnAsExtra = true;
                 outputted = true;
             }
 
             if (outputted) {
                 mImageShow.getImagePreset().mGeoData.setUseCropExtrasFlag(true);
                 showSavingProgress(null);
                 mImageShow.returnFilteredResult(this);
             }
         }
         if (!outputted) {
             if (mImageShow.hasModifications()) {
                 // Get the name of the album, to which the image will be saved
                 File saveDir = SaveCopyTask.getFinalSaveDirectory(this, mImageLoader.getUri());
                 int bucketId = GalleryUtils.getBucketId(saveDir.getPath());
                 String albumName = LocalAlbum.getLocalizedName(getResources(), bucketId, null);
                 showSavingProgress(albumName);
                 mImageShow.saveImage(this, null);
             } else {
                 done();
             }
         }
     }
 
     public void onFilteredResult(Bitmap filtered) {
         Intent intent = new Intent();
         intent.putExtra(CropExtras.KEY_CROPPED_RECT, mImageShow.getImageCropBounds());
         if (mSaveToExtraUri) {
             mImageShow.saveToUri(filtered, mCropExtras.getExtraOutput(),
                     mCropExtras.getOutputFormat(), this);
         }
         if (mSaveAsWallpaper) {
             try {
                 WallpaperManager.getInstance(this).setBitmap(filtered);
             } catch (IOException e) {
                 Log.w(LOGTAG, "fail to set wall paper", e);
             }
         }
         if (mReturnAsExtra) {
             if (filtered != null) {
                 int bmapSize = filtered.getRowBytes() * filtered.getHeight();
                 /*
                  * Max size of Binder transaction buffer is 1Mb, so constrain
                  * Bitmap to be somewhat less than this, otherwise we get
                  * TransactionTooLargeExceptions.
                  */
                 if (bmapSize > MAX_BMAP_IN_INTENT) {
                     Log.w(LOGTAG, "Bitmap too large to be returned via intent");
                 } else {
                     intent.putExtra(CropExtras.KEY_DATA, filtered);
                 }
             }
         }
         setResult(RESULT_OK, intent);
         if (!mSaveToExtraUri) {
             done();
         }
     }
 
     public void done() {
         if (outputted) {
             hideSavingProgress();
         }
         finish();
     }
 
     static {
         System.loadLibrary("jni_filtershow_filters");
     }
 
 }
