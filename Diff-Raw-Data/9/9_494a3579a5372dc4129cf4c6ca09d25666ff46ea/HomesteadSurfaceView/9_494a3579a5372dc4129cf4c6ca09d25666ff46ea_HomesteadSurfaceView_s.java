 /*
  *  Copyright (C) 2012 Simon Robinson
  * 
  *  This file is part of Com-Me.
  * 
  *  Com-Me is free software; you can redistribute it and/or modify it 
  *  under the terms of the GNU Lesser General Public License as 
  *  published by the Free Software Foundation; either version 3 of the 
  *  License, or (at your option) any later version.
  *
  *  Com-Me is distributed in the hope that it will be useful, but WITHOUT 
  *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
  *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General 
  *  Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with Com-Me.
  *  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ac.robinson.mediatablet.view;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.ListIterator;
 import java.util.Random;
 import java.util.UUID;
 import java.util.Vector;
 
 import ac.robinson.mediatablet.MediaTablet;
 import ac.robinson.mediatablet.R;
 import ac.robinson.mediatablet.provider.HomesteadItem;
 import ac.robinson.mediatablet.provider.HomesteadManager;
 import ac.robinson.util.BitmapUtilities;
 import ac.robinson.util.DebugUtilities;
 import ac.robinson.util.ImageCacheUtilities;
 import ac.robinson.util.UIUtilities;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory.Options;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.ColorFilter;
 import android.graphics.LightingColorFilter;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.drawable.Drawable;
 import android.os.SystemClock;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.MotionEvent;
 import android.view.SoundEffectConstants;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 
 import com.larvalabs.svgandroid.SVG;
 import com.larvalabs.svgandroid.SVGParser;
 
 public class HomesteadSurfaceView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {
 
 	private HomesteadTouchListener mTouchListener;
 	private boolean mAddMode = false;
 
 	// for drawing the correct panorama images
 	private final int IMAGES_TO_DRAW = 6; // *caution* odd numbers change home alignment (like changing panorama image)
 	private final boolean DRAW_DEBUG_DATA = false;
 	private boolean mPanoramaLoaded;
 	private int mPanoramaImageIndex;
 	private float mX, mY, mCurrentDrawX, mScreenCentreX, mCurrentTouchX, mCurrentTouchY;
 	private int mPanoramaImageSize;
 	private float mPanoramaHalfImageSize;
 	private int mPanoramaImageCount;
 	private int mScreenWidth, mScreenHeight, mScreenHalfWidth, mBitmapWidth, mBitmapHeight;
 	private Paint mPaint = BitmapUtilities.getPaint(Color.BLACK, 1); // debugging
 
 	// was SoftReference, but it is cleared too aggressively - now we clear manually when out of sight
 	private static final HashMap<String, LoadingBitmap> mTileCache = new HashMap<String, LoadingBitmap>();
 	private static final ArrayList<String> mTilesToLoad = new ArrayList<String>();
 
 	private SurfaceDrawThread mDrawThread;
 	private SurfaceLoadThread mLoadThread;
 	public static final int TRANSITION_STARTING = 0;
 	public static final int TRANSITION_RUNNING = 1;
 	public static final int TRANSITION_NONE = 2;
 
 	// the default icons to show when fading in images
 	private LoadingBitmap mPanoramaLoadingIcon;
 	private LoadingBitmap mHomesteadLoadingIcon;
 
 	// for drawing and tracking the homesteads
 	private final Vector<HomesteadItem> mHomesteadItems = new Vector<HomesteadItem>();
 	private HomesteadItem mCurrentTouchHomestead = null;
 	private HomesteadItem mNewHomestead = null;
 	private int mHomesteadIconSize, mHomesteadHalfIconWidth, mHomesteadHalfIconHeight;
 	private ColorFilter mHomesteadSelectedFilter;
 
 	// for gesture detection and scrolling friction
 	private float mSpeed = 0;
 	private float mScroll = 0;
 
 	// TODO: move to MediaTablet.java
 	private static final int SWIPE_MIN_DISTANCE = 200;
 	private static final int SWIPE_MAX_OFF_PATH = 250;
 	private static final int SWIPE_THRESHOLD_VELOCITY = 300;
 	private final int screenDensity = getResources().getDisplayMetrics().densityDpi;
 	private final int REL_SWIPE_MIN_DISTANCE = (int) (SWIPE_MIN_DISTANCE * screenDensity / 160.0f);
 	private final int REL_SWIPE_MAX_OFF_PATH = (int) (SWIPE_MAX_OFF_PATH * screenDensity / 160.0f);
 	private final int REL_SWIPE_THRESHOLD_VELOCITY = (int) (SWIPE_THRESHOLD_VELOCITY * screenDensity / 160.0f);
 
 	private GestureDetector mGestureDetector;
 
 	// internal id used for storing cached background images - if this is changed, panorama images will be regenerated
 	public static final String BACKGROUND_IMAGE_NAME = "panorama";
 	private static final String HOMESTEAD_DEFAULT_NAME = "homestead";
 	private static final String HOMESTEAD_ADD_NEW_NAME = "add-homestead";
 
 	public static String getBackgroundCacheFileName(int imageNumber) {
 		return BACKGROUND_IMAGE_NAME + "-" + imageNumber;
 	}
 
 	private class LoadingBitmap {
 		public int mTransitionState = TRANSITION_STARTING;
 		public long mStartTimeMillis;
 		public int mFrom = 0;
 		public int mTo = 255;
 		public int mAlpha;
 		public final int mDuration = MediaTablet.ANIMATION_FADE_TRANSITION_DURATION;
 		public final Paint mEndPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
 		public final Bitmap mBitmap;
 
 		public LoadingBitmap(Bitmap loadingTile) {
 			mBitmap = loadingTile;
 		}
 	}
 
 	public HomesteadSurfaceView(Context context) {
 		super(context);
 		initialiseView(context);
 	}
 
 	public HomesteadSurfaceView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		initialiseView(context);
 	}
 
 	public HomesteadSurfaceView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		initialiseView(context);
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		// setWillNotDraw(false); // don't do this - documentation say we should but it breaks the view
 		if (!mPanoramaLoaded) {
 			loadImages();
 		}
 
 		if (!mDrawThread.isAlive()) {
 			mDrawThread = new SurfaceDrawThread(this);
 		}
 		mDrawThread.setRunning(true);
 		mDrawThread.start();
 
 		if (!mLoadThread.isAlive()) {
 			mLoadThread = new SurfaceLoadThread(this);
 		}
 		mLoadThread.setRunning(true);
 		mLoadThread.start();
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		onDetachedFromWindow(); // note: not guaranteed to be called
 	}
 
 	private void initialiseView(Context context) {
 		mPanoramaLoaded = false;
 		mX = 0;
 		mY = 0;
 		mCurrentDrawX = 0;
 		mCurrentTouchX = 0;
 		mCurrentTouchY = 0;
 
 		mDrawThread = new SurfaceDrawThread(this);
 		mLoadThread = new SurfaceLoadThread(this);
 		mGestureDetector = new GestureDetector(context, new HomesteadViewGestureDetector());
 
 		setOnTouchListener(this);
 		getHolder().addCallback(this);
 		setFocusable(true);
 	}
 
 	public boolean imagesLoaded() {
 		return mPanoramaLoaded;
 	}
 
 	public void requestLoadImages() {
 		// TODO: ignore repeat requests
 		loadImages();
 	}
 
 	public static void forceImageReload() {
 		// TODO: delete only those images that exist; this is a hack
 		for (int i = 0; i < 100; i++) {
 			File imageFile = new File(MediaTablet.DIRECTORY_THUMBS, getBackgroundCacheFileName(i));
 			if ((imageFile).exists()) {
 				imageFile.delete();
 			}
 		}
 	}
 
 	private void loadImages() {
 		Context context = getContext();
 		SharedPreferences panoramaSettings = context.getSharedPreferences(MediaTablet.APPLICATION_NAME,
 				Context.MODE_PRIVATE);
 		String panoramaPath = panoramaSettings.getString(context.getString(R.string.key_panorama_file), null);
		boolean useSample = context.getString(R.string.sample_panorama_identifier).equals(panoramaPath);
		if (panoramaPath == null || (!useSample && !new File(panoramaPath).exists())) {
 			mPanoramaLoaded = false;
 			return;
 		}
 
 		int panoramaWidth = panoramaSettings.getInt(context.getString(R.string.key_panorama_width), -1);
 		int panoramaHeight = panoramaSettings.getInt(context.getString(R.string.key_panorama_height), -1);
 		boolean createCachedImages = panoramaWidth <= 0 || panoramaHeight <= 0
 				|| !(new File(MediaTablet.DIRECTORY_THUMBS, getBackgroundCacheFileName(0))).exists(); // TODO: test all?
 
 		mScreenWidth = getWidth();
 		mScreenHeight = getHeight();
 		mScreenHalfWidth = (int) (mScreenWidth / 2.0);
 		mBitmapWidth = mScreenHeight;
 		mBitmapHeight = mScreenHeight;
 
 		if (createCachedImages) {
 			Options imageDimensions;
 			if (useSample) {
 				imageDimensions = BitmapUtilities.getImageDimensions(getResources(), R.drawable.sample_panorama);
 			} else {
 				imageDimensions = BitmapUtilities.getImageDimensions(panoramaPath);
 			}
 			panoramaWidth = imageDimensions.outWidth;
 			panoramaHeight = imageDimensions.outHeight;
 			SharedPreferences.Editor prefsEditor = panoramaSettings.edit();
 			prefsEditor.putInt(context.getString(R.string.key_panorama_width), panoramaWidth);
 			prefsEditor.putInt(context.getString(R.string.key_panorama_height), panoramaHeight);
 			prefsEditor.apply();
 		}
 
 		mPanoramaImageCount = (panoramaWidth / panoramaHeight) + 1;
 		mPanoramaImageSize = mPanoramaImageCount * mBitmapWidth;
 		mPanoramaHalfImageSize = mPanoramaImageSize / 2f;
 		mPanoramaImageIndex = 0;
 
 		mPanoramaLoadingIcon = new LoadingBitmap(Bitmap.createBitmap(mBitmapWidth, mBitmapHeight,
 				ImageCacheUtilities.mBitmapFactoryOptions.inPreferredConfig));
 		mPanoramaLoadingIcon.mBitmap.eraseColor(Color.BLACK);
 
 		if (createCachedImages) {
 			if (MediaTablet.DEBUG) {
 				Log.d(DebugUtilities.getLogTag(this), "Panorama images not found - regenerating cache");
 			}
 			// TODO: move everything from here to a background thread?
 			Drawable panoramaDrawable = null;
 			boolean panoramaFailed = false;
 			try {
 				if (useSample) {
 					panoramaDrawable = getResources().getDrawable(R.drawable.sample_panorama);
 				} else {
 					panoramaDrawable = Drawable.createFromPath(panoramaPath);
 				}
 			} catch (Throwable t) {
 				UIUtilities.showToast(getContext(), R.string.error_loading_panorama);
 				panoramaFailed = true;
 			}
 			if (!panoramaFailed && panoramaDrawable != null) {
 				Bitmap tiledBitmap = null;
 				Canvas tiledBitmapCanvas = null;
 				for (int i = 0, n = panoramaWidth; i < n; i += panoramaHeight) {
 					if (tiledBitmap == null) {
 						tiledBitmap = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight,
 								ImageCacheUtilities.mBitmapFactoryOptions.inPreferredConfig);
 						tiledBitmapCanvas = new Canvas(tiledBitmap);
 						if (panoramaHeight != mBitmapHeight) {
 							tiledBitmapCanvas.scale(mBitmapWidth / (float) panoramaHeight, mBitmapHeight
 									/ (float) panoramaHeight, 0, 0);
 						}
 					}
 					tiledBitmapCanvas.drawColor(Color.BLACK);
 					panoramaDrawable.setBounds(-i, 0, panoramaWidth - i, panoramaHeight);
 					panoramaDrawable.draw(tiledBitmapCanvas); // see: http://stackoverflow.com/a/3705169
 					saveCachedImage(tiledBitmap, getBackgroundCacheFileName(i / panoramaHeight),
 							MediaTablet.ICON_CACHE_TYPE);
 				}
 				tiledBitmapCanvas = null;
 				if (tiledBitmap != null) {
 					tiledBitmap.recycle();
 				}
 				tiledBitmap = null;
 				panoramaDrawable = null;
 			}
 		}
 
 		Resources resources = getResources();
 		mHomesteadIconSize = resources.getDimensionPixelSize(R.dimen.homestead_icon_total_size);
 		if (createCachedImages) {
 			Bitmap iconBitmap = Bitmap.createBitmap(mHomesteadIconSize, mHomesteadIconSize,
 					ImageCacheUtilities.mBitmapFactoryOptions.inPreferredConfig);
 			Canvas iconBitmapCanvas = new Canvas(iconBitmap);
 
 			int iconSize = resources.getDimensionPixelSize(R.dimen.homestead_icon_size);
 			int iconStart = Math.round((mHomesteadIconSize - iconSize) / 2f);
 			Rect drawRect = new Rect(iconStart, iconStart, iconStart + iconSize, iconStart + iconSize);
 
 			// using SVG so that we don't need resolution-specific icons
 			SVG audioSVG = SVGParser.getSVGFromResource(resources, R.raw.ic_homestead);
 			iconBitmapCanvas.drawPicture(audioSVG.getPicture(), drawRect);
 			saveCachedImage(iconBitmap, HOMESTEAD_DEFAULT_NAME, Bitmap.CompressFormat.PNG);
 
 			audioSVG = SVGParser.getSVGFromResource(resources, R.raw.ic_homestead_new);
 			iconBitmapCanvas.drawPicture(audioSVG.getPicture(), drawRect);
 			saveCachedImage(iconBitmap, HOMESTEAD_ADD_NEW_NAME, Bitmap.CompressFormat.PNG);
 		}
 		mHomesteadLoadingIcon = new LoadingBitmap(mLoadThread.loadIcon(HOMESTEAD_DEFAULT_NAME, false));
 		mHomesteadHalfIconWidth = mHomesteadIconSize / 2;
 		mHomesteadHalfIconHeight = mHomesteadIconSize / 2;
 		mHomesteadSelectedFilter = new LightingColorFilter(context.getResources().getColor(R.color.icon_selected), 1);
 
 		float startPosition = panoramaSettings.getFloat(context.getString(R.string.key_panorama_position), 1);
 		mX = (startPosition <= 0 ? startPosition : -(new Random().nextInt(mPanoramaImageSize)));
 		mY = 0;
 		mSpeed = 0;
 
 		context = null;
 		mPanoramaLoaded = true;
 	}
 
 	public void saveCachedImage(Bitmap bitmap, String fileName, Bitmap.CompressFormat fileFormat) {
 		File outputImageFile = new File(MediaTablet.DIRECTORY_THUMBS, fileName);
 		boolean success = false;
 		if (bitmap != null && MediaTablet.DIRECTORY_THUMBS != null) {
 			success = BitmapUtilities.saveBitmap(bitmap, fileFormat, MediaTablet.ICON_CACHE_QUALITY, outputImageFile);
 		}
 		if (MediaTablet.DEBUG) {
 			if (bitmap == null || !success) {
 				Log.e(DebugUtilities.getLogTag(this), "Failed to save cache image " + outputImageFile);
 			}
 		}
 	}
 
 	public void onDetachedFromWindow() {
 		Context context = getContext();
 		SharedPreferences panoramaSettings = context.getSharedPreferences(MediaTablet.APPLICATION_NAME,
 				Context.MODE_PRIVATE);
 		SharedPreferences.Editor prefsEditor = panoramaSettings.edit();
 		prefsEditor.putFloat(context.getString(R.string.key_panorama_position), mX);
 		prefsEditor.apply();
 
 		mDrawThread.setRunning(false);
 		mLoadThread.setRunning(false);
 
 		boolean retry = true;
 		while (retry) {
 			try {
 				mDrawThread.join();
 				retry = false;
 			} catch (InterruptedException e) {
 			}
 		}
 
 		retry = true;
 		while (retry) {
 			try {
 				mLoadThread.join();
 				retry = false;
 			} catch (InterruptedException e) {
 			}
 		}
 
 		mTileCache.clear();
 		mTilesToLoad.clear();
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		canvas.drawColor(Color.BLACK);
 		if (!mPanoramaLoaded) {
 			return;
 		}
 
 		mX = mX + mSpeed + mScroll;
 		mX = (mX - mPanoramaImageSize) % mPanoramaImageSize;
 
 		mPanoramaImageIndex = (int) ((-mX + mBitmapWidth) / mBitmapWidth);
 		mCurrentDrawX = (mX + (mPanoramaImageIndex * mBitmapWidth)) - mBitmapWidth * (IMAGES_TO_DRAW / 2);
 		mPanoramaImageIndex %= mPanoramaImageCount;
 
 		// draw the tiles (fading in as they load)
 		for (int i = (mPanoramaImageIndex + mPanoramaImageCount - IMAGES_TO_DRAW) % mPanoramaImageCount, n = 0; n < mPanoramaImageCount; i = (i + 1)
 				% mPanoramaImageCount) {
 			if (n <= IMAGES_TO_DRAW) {
 				drawLoadingBitmap(canvas, loadBitmap(getBackgroundCacheFileName(i), false), null, false, mCurrentDrawX,
 						mY);
 				mCurrentDrawX += mBitmapWidth;
 			} else {
 				mTileCache.remove(getBackgroundCacheFileName(i)); // try to save some memory
 			}
 			n += 1;
 		}
 
 		float hDiff = 0;
 		float hXPos;
 		mScreenCentreX = -(mX + (mBitmapWidth * (IMAGES_TO_DRAW / 2)) - mPanoramaImageSize - mScreenHalfWidth)
 				% mPanoramaImageSize;
 		for (HomesteadItem h : mHomesteadItems) {
 			hDiff = getXPointDifference(mScreenCentreX, h.getXPosition());
 			if (Math.abs(hDiff) <= mScreenHalfWidth + mHomesteadHalfIconWidth) {
 				hXPos = mScreenHalfWidth - hDiff - mHomesteadHalfIconWidth; // TODO: fix buggy horizontal positioning
 				if (mAddMode && h.equals(mNewHomestead)) { // default add homestead icon
 					drawLoadingBitmap(canvas, loadBitmap(HOMESTEAD_ADD_NEW_NAME, true), mHomesteadSelectedFilter, true,
 							hXPos, h.getYPosition() - mHomesteadHalfIconHeight);
 				} else {
 					drawLoadingBitmap(canvas, loadBitmap(h.getCacheId(), true),
 							h.getSelected() ? mHomesteadSelectedFilter : h.getColourFilter(), // should we combine?
 							true, hXPos, h.getYPosition() - mHomesteadHalfIconHeight);
 				}
 			}
 		}
 
 		if (DRAW_DEBUG_DATA) {
 			drawDebugData(canvas);
 		}
 	}
 
 	private float getXPointDifference(float p1, float p2) {
 		return ((p1 + mPanoramaHalfImageSize - p2) % mPanoramaImageSize) - mPanoramaHalfImageSize;
 	}
 
 	private void drawDebugData(Canvas canvas) {
 		canvas.drawText("MX: " + mX + " " + mY, 10, 20, mPaint);
 		canvas.drawText("TouchPos: " + mCurrentTouchX + " " + mCurrentTouchY, 10, 40, mPaint);
 		canvas.drawText("DrawMX: " + mCurrentDrawX, 10, 60, mPaint);
 		canvas.drawText("Index: " + mPanoramaImageIndex + ", centre: " + mScreenCentreX, 10, 80, mPaint);
 		canvas.drawText("TouchPoint: (" + getTouchedXPoint() + "," + getTouchedYPoint() + ")", 10, 100, mPaint);
 		if (getSelectedHomestead() != null) {
 			canvas.drawText(getSelectedHomestead().getInternalId(), 10, 120, mPaint);
 		} else {
 			canvas.drawText("null", 10, 120, mPaint);
 		}
 	}
 
 	private LoadingBitmap loadBitmap(String bitmapFile, boolean isHomestead) {
 		LoadingBitmap bitmap = mTileCache.get(bitmapFile);
 		if (bitmap != null) {
 			return bitmap;
 		} else {
 			mTileCache.remove(bitmapFile); // not loaded yet (or removed) - load into cache
 		}
 		mTilesToLoad.remove(bitmapFile);
 		mTilesToLoad.add(0, bitmapFile); // start or end?
 		return (isHomestead ? mHomesteadLoadingIcon : mPanoramaLoadingIcon);
 	}
 
 	private void drawLoadingBitmap(Canvas canvas, LoadingBitmap loadingBitmap, ColorFilter filter, boolean isHomestead,
 			float xPosition, float yPosition) {
 
 		boolean done = true;
 		switch (loadingBitmap.mTransitionState) {
 			case TRANSITION_STARTING:
 				loadingBitmap.mStartTimeMillis = SystemClock.uptimeMillis();
 				done = false;
 				loadingBitmap.mTransitionState = TRANSITION_RUNNING;
 				break;
 
 			case TRANSITION_RUNNING:
 				if (loadingBitmap.mStartTimeMillis >= 0) {
 					float normalized = (float) (SystemClock.uptimeMillis() - loadingBitmap.mStartTimeMillis)
 							/ loadingBitmap.mDuration;
 
 					done = normalized >= 1.0f;
 					loadingBitmap.mAlpha = (int) (loadingBitmap.mFrom + (loadingBitmap.mTo - loadingBitmap.mFrom)
 							* Math.min(normalized, 1.0f));
 
 					if (done) {
 						loadingBitmap.mTransitionState = TRANSITION_NONE;
 					}
 				}
 				break;
 		}
 
 		final Paint paint = loadingBitmap.mEndPaint;
 		final int alpha = loadingBitmap.mAlpha;
 		if (isHomestead) {
 			paint.setAlpha(255 - alpha);
 			canvas.drawBitmap(mHomesteadLoadingIcon.mBitmap, xPosition, yPosition, paint);
 			paint.setAlpha(0xFF);
 		}
 		if (alpha > 0) {
 			paint.setAlpha(alpha);
 			paint.setColorFilter(filter);
 			canvas.drawBitmap(loadingBitmap.mBitmap, xPosition, yPosition, paint);
 			paint.setColorFilter(null);
 			paint.setAlpha(0xFF);
 		}
 	}
 
 	public void refreshHomesteads() {
 		HomesteadManager.loadHomesteads(getContext().getContentResolver(), mHomesteadItems);
 	}
 
 	public void setScrollSpeed(int scroll) {
 		if (scroll != 0 && Integer.signum(scroll) != Integer.signum(Math.round(mSpeed))) {
 			mSpeed = -mSpeed;
 		}
 		mScroll = scroll;
 	}
 
 	public float getCurrentXPosition() {
 		return mX;
 	}
 
 	public void setCurrentXPosition(float newPosition) {
 		if (newPosition <= 0) {
 			mX = newPosition;
 		}
 	}
 
 	public void registerTouchListener(HomesteadTouchListener touchListener) {
 		mTouchListener = touchListener;
 	}
 
 	public void setEditMode(boolean editMode) {
 		mAddMode = editMode;
 	}
 
 	public HomesteadItem getTemporaryHomestead() {
 		return mNewHomestead;
 	}
 
 	public void updatePhysics() {
 		// TODO: add proper physics here...
 		mSpeed *= 0.95; // was 0.981;
 	}
 
 	public String getIconToLoad() {
 		if (mTilesToLoad.size() > 0) {
 			return mTilesToLoad.get(0);
 		}
 		return null;
 	}
 
 	public void addLoadedTile(String iconFile, Bitmap loadedIcon) {
 		// don't let load failures block other requests
 		if (loadedIcon != null) {
 			LoadingBitmap b = new LoadingBitmap(loadedIcon);
 			mTileCache.put(iconFile, b);
 		}
 		mTilesToLoad.remove(iconFile);
 	}
 
 	private int getTouchedXPoint() {
 		return !mPanoramaLoaded ? 0
 				: (int) (-mX - (mBitmapWidth * (IMAGES_TO_DRAW / 2)) + mPanoramaImageSize + mCurrentTouchX)
 						% mPanoramaImageSize;
 	}
 
 	private int getTouchedYPoint() {
 		return !mPanoramaLoaded ? 0 : (int) (-mY + mCurrentTouchY);
 	}
 
 	public HomesteadItem getSelectedHomestead() {
 		return mCurrentTouchHomestead;
 	}
 
 	private HomesteadItem getTouchedHomestead() {
 		int touchedX = getTouchedXPoint();
 		int touchedY = getTouchedYPoint();
 		// for (HomesteadItem h : mHomesteadItems) { //instead, reverse the list to get items in correct draw order
 		for (ListIterator<HomesteadItem> it = mHomesteadItems.listIterator(mHomesteadItems.size()); it.hasPrevious();) {
 			final HomesteadItem h = it.previous();
 			if (Math.abs(getXPointDifference(touchedX, h.getXPosition())) <= mHomesteadHalfIconWidth) {
 				int hY = h.getYPosition();
 				if (touchedY >= hY - mHomesteadHalfIconHeight && touchedY <= hY + mHomesteadHalfIconHeight) {
 					return h;
 				}
 			}
 		}
 		return null;
 	}
 
 	public boolean onTouch(View v, MotionEvent event) {
 		return mGestureDetector.onTouchEvent(event);
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		switch (event.getAction()) {
 
 			case MotionEvent.ACTION_DOWN:
 				mSpeed = 0; // stop the gravity scroll
 				mCurrentTouchX = event.getX();
 				mCurrentTouchY = event.getY();
 				mCurrentTouchHomestead = getTouchedHomestead();
 				for (HomesteadItem h : mHomesteadItems) {
 					h.setSelected(false);
 				}
 				if (mCurrentTouchHomestead != null) {
 					if (mCurrentTouchHomestead != mNewHomestead && mNewHomestead != null) {
 						mHomesteadItems.remove(mNewHomestead);
 						mNewHomestead = null;
 					}
 					mCurrentTouchHomestead.setSelected(true);
 				}
 				return true;
 
 			case MotionEvent.ACTION_MOVE:
 				mX = mX + event.getX() - mCurrentTouchX;
 				mX = (mX - mPanoramaImageSize) % mPanoramaImageSize;
 
 				mCurrentTouchX = event.getX();
 				mCurrentTouchY = event.getY();
 				return true;
 
 			case MotionEvent.ACTION_UP:
 				mCurrentTouchX = event.getX();
 				mCurrentTouchY = event.getY();
 				if (mCurrentTouchHomestead != null) {
 					// to check for finger movement during swipes
 					if (mCurrentTouchHomestead == getTouchedHomestead() && mTouchListener != null) {
 						playSoundEffect(SoundEffectConstants.CLICK); // play the default button click (respects prefs)
 						mTouchListener.homesteadTouched(mCurrentTouchHomestead);
 					}
 					if (!mAddMode) {
 						mCurrentTouchHomestead.setSelected(false);
 					}
 				}
 				mCurrentTouchHomestead = null;
 				return true;
 		}
 		return false;
 	}
 
 	private class HomesteadViewGestureDetector extends SimpleOnGestureListener {
 
 		@Override
 		public boolean onDoubleTap(MotionEvent e) {
 			if (mAddMode) {
 				// TODO: stop them double tapping on an existing homestead
 				if (mCurrentTouchHomestead != null) {
 					mCurrentTouchHomestead.setSelected(false);
 				}
 				if (mNewHomestead == null) {
 					mNewHomestead = new HomesteadItem(UUID.randomUUID().toString(), getTouchedXPoint(),
 							getTouchedYPoint());
 					mHomesteadItems.add(mNewHomestead);
 				} else {
 					mNewHomestead.setXPosition(getTouchedXPoint());
 					mNewHomestead.setYPosition(getTouchedYPoint());
 				}
 				mCurrentTouchHomestead = mNewHomestead;
 				mCurrentTouchHomestead.setSelected(true);
 				return true;
 			}
 			return false;
 		}
 
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 			try {
 				if (Math.abs(e1.getY() - e2.getY()) > REL_SWIPE_MAX_OFF_PATH) {
 					return false;
 				}
 
 				if (Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) {
 					if (e1.getX() - e2.getX() > REL_SWIPE_MIN_DISTANCE) { // right to left swipe
 						mSpeed = velocityX / 50;
 						return true;
 					} else if (e2.getX() - e1.getX() > REL_SWIPE_MIN_DISTANCE) { // left to right swipe
 						mSpeed = velocityX / 50;
 						return true;
 					}
 				}
 			} catch (Exception e) {
 			}
 			return false;
 		}
 	}
 
 	public interface HomesteadTouchListener {
 		void homesteadTouched(HomesteadItem touchedHomestead);
 	}
 }
