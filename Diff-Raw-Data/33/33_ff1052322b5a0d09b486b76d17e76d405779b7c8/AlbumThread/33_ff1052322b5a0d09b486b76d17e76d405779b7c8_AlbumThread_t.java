 /*
  * ComicsReader is an Android application to read comics
  * Copyright (C) 2011-2012 Cedric OCHS
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package net.kervala.comicsreader;
 
 import java.io.File;
 import java.lang.ref.WeakReference;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 
 public class AlbumThread extends Thread {
 	private Album mAlbum;
 	private final WeakReference<Context> mContext;
 	private final WeakReference<Handler> mHandler;
 	private final WeakReference<FullImageView> mImageView; 
 	private int mCurrentPage = -1;
 	private int mNextPage = -1;
 	private int mPreviousPage = -1;
 	private int mWidth = 0;
 	private boolean mLoadingPage = false;
 	private boolean mHighQuality = false;
 	private boolean mFullScreen = false;
 	private int mZoom = 0;
 	private int mSample = 0;
 	private int mOverlayDuration = 5000;
 	private boolean mDoublePage = false;
 	private boolean mFitToScreen = true;
 	private boolean mPreferencesLoaded = false;
 	private ConcurrentLinkedQueue<Bundle> mActions = new ConcurrentLinkedQueue<Bundle>();
 	private String mError;
 
 	static final int ZOOM_NONE = 0;
 	static final int ZOOM_FIT_WIDTH = 1;
 	static final int ZOOM_FIT_HEIGHT = 2;
 	static final int ZOOM_FIT_SCREEN = 3;
 	static final int ZOOM_100 = 4;
 	static final int ZOOM_50 = 5;
 	static final int ZOOM_25 = 6;
 	
 	public AlbumThread(Context context, Handler handler, FullImageView imageView){
 		mContext = new WeakReference<Context>(context);
 		mHandler = new WeakReference<Handler>(handler);
 		mImageView = new WeakReference<FullImageView>(imageView);
 
 		setPriority(Thread.NORM_PRIORITY);
 	}
 	
 	private Bitmap getPage(int page) {
 		if (mAlbum == null || ComicsParameters.sScreenWidth < 1 || ComicsParameters.sScreenHeight < 1) return null;
 
 		mLoadingPage = true;
 		
 		mAlbum.setHighQuality(mHighQuality);
 		mAlbum.setFitToScreen(mFitToScreen);
 		mAlbum.setScale(mSample);
 
 		boolean divideByTwo = false;
 		int width = -1;
 		int height = -1;
 		
 		switch(mZoom) {
 			case ZOOM_FIT_WIDTH: {
 				width = ComicsParameters.sScreenWidth;
 				if (mDoublePage) {
 					divideByTwo = true;
 				}
 				break;
 			}
 			case ZOOM_FIT_HEIGHT: {
 				height = ComicsParameters.sScreenHeight;
 				break;
 			}
 			case ZOOM_FIT_SCREEN: {
 				width = ComicsParameters.sScreenWidth;
 				if (mDoublePage) {
 					divideByTwo = true;
 				}
 				height = ComicsParameters.sScreenHeight;
 				break;
 			}
 			case ZOOM_50: {
 				width = -2;
 				height = -2;
 				break;
 			}
 			case ZOOM_25: {
 				width = -4;
 				height = -4;
 				break;
 			}
 		}
 		
 		Bitmap bitmap = null;
 
 		if (mDoublePage && page > 0) {
 			bitmap = mAlbum.getDoublePage(page, divideByTwo ? width/2:width, height);
 		} else {
 			bitmap = mAlbum.getPage(page, width, height, false);
 		}
 
 		if (!mLoadingPage) {
 			mError = mContext.get().getString(R.string.error_out_of_memory);
 			return null;
 		}
 		
 		mLoadingPage = false;
 		
 		return bitmap;
 	}
 
 	@Override
 	public void run(){
 		try {
 			while (true) {
 				// thread waits until there are any images to load in the queue
 				if (mActions.size() == 0) {
 					synchronized (mActions) {
 						mActions.wait();
 					}
 				}
 
 				// actions: open, changePage, updateCurrentPage, updateNextPage
 				if (mActions.size() > 0) {
 					Bundle bundle;
 					synchronized (mActions) {
 						bundle = mActions.poll();
 					}
 					
 					String action = bundle.getString("action"); 
 
 					boolean changed = false;
 					boolean showPage = false;
 					
 					if (!mPreferencesLoaded) {
 						doLoadPreferences();
 					}
 					
 					if ("open".equals(action)) {
 						String filename = bundle.getString("filename");
 						
 						if (mAlbum != null) {
 							mAlbum.close();
 						}
 						mAlbum = Album.createInstance(filename);
 						synchronized(mAlbum) {
 							invalidatePage();
 
 							mAlbum.setHighQuality(mHighQuality);
 							mAlbum.setFitToScreen(mFitToScreen);
 							mAlbum.setScale(mSample);
 							
 							if (!mAlbum.open(filename, true)) {
 								mError = mContext.get().getString(R.string.error_no_album_loaded);
 							}else{
 								ComicsParameters.sCurrentOpenAlbum = mAlbum.getFilename();
 							}
 						}
 						updateAlbum();
 					} else if ("changePage".equals(action)) {
 						int page = bundle.getInt("page");
 
 						if (page == -1) {
 							page = mAlbum.getCurrentPage();
 						}
 						
 						// wait until album is loaded
 						if (page >= mAlbum.getNumPages()) {
 							page = mAlbum.getNumPages() - 1;
 						} else if (page < 0) {
 							page = 0;
 						}
 						
 						if (mDoublePage) {
 							if (page > 0) {
 								page -= 1 - (page % 2);
 							} else {
 								page = 0;
 							}
 						}
 						
 						if (page != mCurrentPage) {
 							if (mImageView.get().getNextBitmap() != null && mNextPage == page) {
 								// display the next page which is already preloaded
 								changed = true;
 								showPage = true;
 								mWidth = mImageView.get().getNextBitmap().getWidth();
 								mNextPage = -1;
 								mPreviousPage = mCurrentPage; 
 
 								mImageView.get().post(new Runnable() {
 									public void run() {
 										if (mAlbum.getMaxImagesInMemory() < 2) {
 											mImageView.get().recyclePreviousBitmap();
 										}
 
 										mImageView.get().swapNext();
 									}
 								});
 							} else if (mImageView.get().getPreviousBitmap() != null && mPreviousPage == page) {
 								// display the previous page which is already preloaded
 								changed = true;
 								showPage = true;
 								mWidth = mImageView.get().getPreviousBitmap().getWidth();
 								mPreviousPage = -1;
 								mNextPage = mCurrentPage; 
 
 								mImageView.get().post(new Runnable() {
 									public void run() {
 										if (mAlbum.getMaxImagesInMemory() < 3) {
 											mImageView.get().recycleCurrentBitmap();
 										}
 
 										mImageView.get().swapPrevious();
 									}
 								});
 							} else {
 								if (doUpdateCurrentPage(page)) {
 									changed = true;
 									showPage = true;
 								} else {
 								}
 							}
 							
 							mCurrentPage = page;
 						}
 					} else if ("updateNextPage".equals(action)) {
 						int page = getNextPage();
 						if (page > -1 && (page != mNextPage || mImageView.get().getNextBitmap() == null)) {
 							mImageView.get().recycleNextBitmap();
 
 							final Bitmap bitmap = getPage(page);
 							if (bitmap != null) {
 								mImageView.get().setNextBitmap(bitmap);
 								mNextPage = page;
 							} else {
 							}
 						}
 					} else if ("updatePreviousPage".equals(action)) {
 						int page = getPreviousPage();
 						if (page > -1 && (page != mPreviousPage || mImageView.get().getPreviousBitmap() == null)) {
 							mImageView.get().recyclePreviousBitmap();
 
 							final Bitmap bitmap = getPage(page);
 							if (bitmap != null) {
 								mImageView.get().setPreviousBitmap(bitmap);
 								mPreviousPage = page;
 							} else {
 							}
 						}
 					} else if ("updateCurrentPage".equals(action)) {
 						boolean force = bundle.getBoolean("force");
 						
 						if (force) {
 							mImageView.get().reset();
 							
 							// removing all pending actions
 							mActions.clear();
 						}
 
 						if (mImageView.get().getCurrentBitmap() == null && mCurrentPage > -1) {
 							if (doUpdateCurrentPage(mCurrentPage)) {
 								changed = true;
 								showPage = true;
 							} else {
 							}
 						}
 					} else if ("loadPreferences".equals(action)) {
 						doLoadPreferences();
 					}
 
 					if (showPage && mOverlayDuration > -1) {
 						updatePageNumber();
 					}
 
 					updateBitmap(changed);
 					
 					if (mError != null) {
 						displayError();
 					}
 				}
 
 				if (Thread.interrupted()) break;
 			}
 		} catch (InterruptedException e) {
 			// allow thread to exit
 		}
 	}
 	
 	public Album getAlbum() {
 		return mAlbum;
 	}
 
 	public Uri getAlbumUri() {
 		if (mAlbum == null) return null;
 
 		String filename = mAlbum.getFilename();
 		if (filename == null) return null;
 
 		return Uri.parse(Uri.fromFile(new File(filename)).toString() + "#" + String.valueOf(mCurrentPage));
 	}
 	
 	public boolean isValid() {
 		return mAlbum != null && mAlbum.getNumPages() > 0;
 	}
 
 	public void open(String filename) {
 		Bundle action = new Bundle();
 		action.putString("action", "open");
 		action.putString("filename", filename);
 		addAction(action);
 	}
 	
 	public int getPageWidth() {
 		return Math.max(mWidth, ComicsParameters.sScreenWidth);
 	}
 	
 	public int getCurrentPage() {
 		return mCurrentPage;
 	}
 
 	public int getNextPage() {
 		if (mCurrentPage == 0 && getLastPage() > 0) return 1;
 
 		int nextPage = mCurrentPage + (mDoublePage ? 2:1);
 		
 		return nextPage > getLastPage() ? -1:nextPage;
 	}
 
 	public int getPreviousPage() {
 		if (mCurrentPage == 1) return 0;
 
 		int previousPage = mCurrentPage - (mDoublePage ? 2:1);
 		
 		return previousPage < getFirstPage() ? -1:previousPage;
 	}
 
 	public int getLastPage() {
		if (mAlbum == null) return 0;

 		int page = mAlbum.getNumPages() - 1;
 		
 		if (mDoublePage && (page % 2) == 0) --page;
 		
 		return page;
 		
 	}
 
 	public int getFirstPage() {
 		return 0;
 	}
 
 	/*
 	 * go to specified page
 	 */
 	public void changePage(int page) {
 		Bundle action = new Bundle();
 		action.putString("action", "changePage");
 		action.putInt("page", page);
 		addAction(action);
 	}
 	
 	/*
 	 * go to next page
 	 */
 	public void nextPage() {
 		changePage(getNextPage());
 	}
 
 	/*
 	 * go to previous page
 	 */
 	public void previousPage() {
 		changePage(getPreviousPage());
 	}
 
 	/*
 	 * go to last page
 	 */
 	public void lastPage() {
 		changePage(getLastPage());
 	}
 
 	/*
 	 * go to first page
 	 */
 	public void firstPage() {
 		changePage(getFirstPage());
 	}
 
 	/*
 	 * invalidate pages numbers
 	 */
 	public void invalidatePage() {
 		// cancel previously page loading
 		mLoadingPage = false;
 
 		mCurrentPage = -1;
 		mNextPage = -1;
 		mPreviousPage = -1;
 
 		// release all cached images in FullImageView
 		mImageView.get().reset();
 	}
 	
 	public void updatePreviousPage() {
 		if (mAlbum.getMaxImagesInMemory() > 2) {
 			Bundle action = new Bundle();
 			action.putString("action", "updatePreviousPage");
 			addAction(action);
 		}
 	}
 	
 	public void updateNextPage() {
 		if (mAlbum.getMaxImagesInMemory() > 1) {
 			Bundle action = new Bundle();
 			action.putString("action", "updateNextPage");
 			addAction(action);
 		}
 	}
 
 	public void updateCurrentPage(boolean force) {
 		Bundle action = new Bundle();
 		action.putString("action", "updateCurrentPage");
 		action.putBoolean("force", force);
 		addAction(action);
 	}
 
 	public void loadPreferences() {
 		// cancel previously page loading
 		mLoadingPage = false;
 
 		Bundle action = new Bundle();
 		action.putString("action", "loadPreferences");
 		addAction(action);
 	}
 	
 	private void addAction(Bundle action) {
 		// add item to the queue
 		synchronized (mActions) {
 			mActions.add(action);
 			mActions.notifyAll();
 		}
 
 		// start thread if it's not started yet
 		if (!isAlive()) {
 			start();
 		}
 	}
 
 	private boolean doUpdateCurrentPage(int page) {
 		final Bitmap bitmap = getPage(page);
 		
 		if (bitmap == null) return false;
 	
 		mWidth = bitmap.getWidth();
 
 		mImageView.get().post(new Runnable() {
 			public void run() {
 				mImageView.get().setCurrentBitmap(bitmap);
 			}
 		});
 		
 		return true;
 	}
 	
 	private void doLoadPreferences() {
 		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext.get()); 
 		mHighQuality = prefs.getBoolean("preference_high_quality", false);
 		mFullScreen = prefs.getBoolean("preference_full_screen", false);
 		mZoom = Integer.parseInt(prefs.getString("preference_zoom", "1"));
 		mDoublePage = prefs.getBoolean("preference_double_page", false);
 		mSample = Integer.parseInt(prefs.getString("preference_sample", "0"));
 		mFitToScreen = prefs.getBoolean("preference_fit_to_screen", true);
 		mOverlayDuration = Integer.parseInt(prefs.getString("preference_overlay_duration", "5000"));
 		
 		updateWindow();
 		
 		mPreferencesLoaded = true;
 	}
 	
 	private void updateWindow() {
 		final Message msg = mHandler.get().obtainMessage(ViewerActivity.ACTION_UPDATE_WINDOW);
 		final Bundle b = new Bundle();
 		b.putBoolean("highQuality", mHighQuality);
 		b.putBoolean("fullScreen", mFullScreen);
 		msg.setData(b);
 		mHandler.get().sendMessage(msg);
 	}
 
 	private void updateBitmap(boolean changed) {
 		final Message msg = mHandler.get().obtainMessage(ViewerActivity.ACTION_UPDATE_BITMAP);
 		final Bundle b = new Bundle();
 		b.putBoolean("changed", changed);
 		msg.setData(b);
 		mHandler.get().sendMessage(msg);
 	}
 
 	private void displayError() {
 		final Message msg = mHandler.get().obtainMessage(ViewerActivity.ACTION_DISPLAY_ERROR);
 		Bundle b = new Bundle();
 		b.putString("error", mError);
 		msg.setData(b);
 		mHandler.get().sendMessage(msg);
 		mError = null;
 	}
 	
 	private void updatePageNumber() {
 		final Message msg = mHandler.get().obtainMessage(ViewerActivity.ACTION_UPDATE_PAGE_NUMBER);
 		final Bundle b = new Bundle();
 		b.putInt("page", mCurrentPage);
 		b.putInt("pages", mAlbum.getNumPages());
 		b.putInt("duration", mOverlayDuration);
 		msg.setData(b);
 		mHandler.get().sendMessage(msg);
 
 		final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext.get()).edit();
 		editor.putString("last_file", getAlbumUri().toString());
 		editor.commit();
 	}
 	
 	private void updateAlbum() {
 		final Message msg = mHandler.get().obtainMessage(ViewerActivity.ACTION_UPDATE_ALBUM);
 		mHandler.get().sendMessage(msg);
 	}
 }
