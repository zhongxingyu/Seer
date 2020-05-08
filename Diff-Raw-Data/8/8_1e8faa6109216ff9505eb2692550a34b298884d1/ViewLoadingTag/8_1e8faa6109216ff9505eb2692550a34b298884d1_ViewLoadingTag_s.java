 package com.levelup.picturecache.loaders.internal;
 
import java.util.HashSet;
 
 import uk.co.senab.bitmapcache.BitmapLruCache;
 import android.graphics.drawable.Drawable;
 
 import com.levelup.picturecache.LogManager;
 import com.levelup.picturecache.PictureCache;
 import com.levelup.picturecache.UIHandler;
 import com.levelup.picturecache.loaders.ViewLoader;
 import com.levelup.picturecache.transforms.bitmap.BitmapTransform;
 import com.levelup.picturecache.transforms.storage.StorageTransform;
 
 public class ViewLoadingTag {
 	public final String url;
 	public final BitmapLruCache bitmapCache;
 	private final StorageTransform storageTransform;
 	private final BitmapTransform displayTransform;
 
 	private boolean isLoaded;
 	private DrawType drawnType;
 
 	// pending draw data
 	private Drawable mPendingDrawable;
 	private String mPendingUrl;
 	private DrawType mPendingDrawType;
 	private DrawInUI mDrawInUI;
 
 	public ViewLoadingTag(BitmapLruCache cache, String url, StorageTransform storageTransform, BitmapTransform displayTransform) {
 		this.bitmapCache = cache;
 		this.url = url;
 		this.displayTransform = displayTransform;
 		this.storageTransform = storageTransform;
 	}
 
 	public void setPendingDraw(Drawable pendingDrawable, String pendingUrl, DrawType drawType) {
 		if (mDrawInUI!=null)
 			mDrawInUI.setPendingDrawable(pendingDrawable, pendingUrl, drawType);
 		else {
 			if (ViewLoader.DEBUG_VIEW_LOADING) LogManager.getLogger().i(PictureCache.LOG_TAG, "temporary store pending draw:"+pendingDrawable+" for "+pendingUrl);
 			this.mPendingDrawable = pendingDrawable;
 			this.mPendingUrl = pendingUrl;
 			this.mPendingDrawType = drawType;
 		}
 	}
 
 	public boolean isUrlLoaded() {
 		return isLoaded;
 	}
 
 	void setUrlIsLoaded(boolean set) {
 		isLoaded = set;
 	}
 
 	DrawType getDrawnType() {
 		return drawnType;
 	}
 
 	DrawType setDrawnType(DrawType drawType) {
 		DrawType oldType = this.drawnType;
 		this.drawnType = drawType;
 		return oldType;
 	}
 
 	public void recoverStateFrom(ViewLoadingTag oldTag) {
 		drawnType = oldTag.drawnType==DrawType.LOADED_DRAWABLE ? null : oldTag.drawnType;
 		mDrawInUI = oldTag.mDrawInUI;
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (this==o) return true;
 		if (!(o instanceof ViewLoadingTag)) return false;
 		ViewLoadingTag tag = (ViewLoadingTag) o;
 		return url!=null && url.equals(tag.url)
 				&& ((displayTransform==null && tag.displayTransform==null) || (displayTransform!=null && displayTransform.equals(tag.displayTransform)))
 				&& ((storageTransform==null && tag.storageTransform==null) || (storageTransform!=null && storageTransform.equals(tag.storageTransform)))
 				;
 	}
 
 	@Override
 	public String toString() {
 		return "ViewTag:"+url+(drawnType!=DrawType.LOADED_DRAWABLE?drawnType:"");
 	}
 
 	private static Runnable batchDisplay;
	private static final HashSet<Runnable> pendingDraws = new HashSet<Runnable>();
 
 	public void drawInView(final ViewLoader<?> viewLoader, boolean immediate) {
 		if (mDrawInUI == null) {
 			if (ViewLoader.DEBUG_VIEW_LOADING) LogManager.getLogger().d(PictureCache.LOG_TAG, viewLoader+" create new DrawInUI with "+mPendingDrawable+" for "+mPendingUrl);
 			mDrawInUI = new DrawInUI(viewLoader);
 			mDrawInUI.setPendingDrawable(mPendingDrawable, mPendingUrl, mPendingDrawType);
 			mPendingDrawable = null;
 			mPendingUrl = null;
 		}
 
 		if (ViewLoader.DEBUG_VIEW_LOADING) LogManager.getLogger().i(PictureCache.LOG_TAG, mDrawInUI+" / "+viewLoader+" drawInView run mDrawInUI bitmap:"+mDrawInUI.mPendingDrawable+" for "+mDrawInUI.mPendingUrl);
 
 		synchronized(viewLoader.getImageView()) {
 			pendingDraws.add(mDrawInUI);
 			if (null == batchDisplay) {
 				batchDisplay = new Runnable() {
 					@Override
 					public void run() {
 						synchronized(viewLoader.getImageView()) {
 							for (Runnable drawRunnable : pendingDraws) {
 								drawRunnable.run();
 							}
 							pendingDraws.clear();
 
 							batchDisplay = null;
 						}
 					}
 				};
 				if (immediate || drawnType==DrawType.LOADED_DRAWABLE || drawnType==DrawType.ERROR || mPendingDrawType==DrawType.DEFAULT) {
 					UIHandler.instance.removeCallbacks(batchDisplay);
 					UIHandler.instance.runOnUiThread(batchDisplay);
 				} else {
 					UIHandler.instance.postDelayed(batchDisplay, 100);
 				}
 			}
 		}
 	}
 
 	public boolean isBitmapPending() {
 		return mPendingDrawable!=null && mPendingDrawType==DrawType.LOADED_DRAWABLE;
 	}
 }
