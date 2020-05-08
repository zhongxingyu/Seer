 /**
  * 
  */
 package com.levelup.picturecache.internal;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.security.InvalidParameterException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import uk.co.senab.bitmapcache.CacheableBitmapDrawable;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.util.FloatMath;
 
 import com.levelup.picturecache.BuildConfig;
 import com.levelup.picturecache.LifeSpan;
 import com.levelup.picturecache.LogManager;
 import com.levelup.picturecache.NetworkLoader;
 import com.levelup.picturecache.PictureCache;
 import com.levelup.picturecache.PictureJob;
import com.levelup.picturecache.PictureJobTransforms;
 import com.levelup.picturecache.UIHandler;
 import com.levelup.picturecache.loaders.ViewLoader;
 
 public class PictureJobList implements Runnable {
 
 	private static final boolean DEBUG_BITMAP_DOWNLOADER = false;
 
 	final String url;
 	private final NetworkLoader networkLoader;
 	private final PictureCache mCache;
 	private final Map<PictureJob,Boolean> mTargetJobs = new HashMap<PictureJob,Boolean>();
 	private final DownloadManager mMonitor;
 
 	/**
 	 * Longest item (URL+key) {@link LifeSpan} 
 	 */
 	private LifeSpan mLifeSpan;
 	/**
 	 * Most recent item (URL+key) date
 	 */
 	private long mItemDate;
 
 	private boolean mCanDownload;
 	private final AtomicBoolean mAborting = new AtomicBoolean();
 
 	private static final int CONNECT_TIMEOUT_DL = 10000; // 10s
 
 	PictureJobList(PictureJob job, PictureCache cache, DownloadManager monitor) {
 		if (job.url==null) throw new NullPointerException("How are we supposed to download a null URL?");
 		this.url = job.url;
 		this.networkLoader = job.networkLoader;
 		this.mCache = cache;
 		this.mMonitor = monitor;
 	}
 
 	LifeSpan getLifeSpan() {
 		return mLifeSpan;
 	}
 	long getItemDate() {
 		return mItemDate;
 	}
 
 	@Override
 	public String toString() {
 		return "BitmapLoader:"+url+"@"+super.hashCode();
 	}
 
 	public void run() {
 		//LogManager.getLogger().v( "start image load in cache: " + mURL);
 		final HashMap<CacheKey,Drawable> targetBitmaps = new HashMap<CacheKey, Drawable>();
 		final HashMap<CacheVariant,Drawable> targetNewBitmaps = new HashMap<CacheVariant, Drawable>();
 		File downloadedToFile = null;
 		boolean downloaded = false;
 		try {
 			BitmapFactory.Options tmpFileOptions = new BitmapFactory.Options();
 			tmpFileOptions.inJustDecodeBounds = false;
 
 			for (;;) {
 				PictureJob target = null;
 				synchronized (this) {
 					for (Entry<PictureJob, Boolean> targetEntry : mTargetJobs.entrySet()) {
 						if (targetEntry.getValue()==null) { // this job has not been processed yet
 							target = targetEntry.getKey();
 						}
 					}
 				}
 				if (null==target)
 					break;
 
 				File fileInCache = mCache.getCachedFile(target.key);
 				boolean bitmapWasInCache = fileInCache!=null;
 				if (!bitmapWasInCache) {
 					// we can't use the older version, download the file and create the stored file again
 					fileInCache = mCache.getCachedFilepath(target.key);
 					if (downloadedToFile==null && mCanDownload) {
 						if (target.getStorageTransform()==null)
 							downloadedToFile = fileInCache;
 						else
 							downloadedToFile = new File(mCache.getAvailaibleTempDir(), "tmp_"+target.key.getFilename());
 					}
 				}
 
 				if (fileInCache!=null) {
 					Drawable displayDrawable;
 					if (!bitmapWasInCache) {
 						displayDrawable = null;
 					} else if (mCache.getBitmapCache()!=null) {
 						displayDrawable = mCache.getBitmapCache().put(keyToBitmapCacheKey(target, url), fileInCache, getOutputOptions(tmpFileOptions.outWidth, tmpFileOptions.outHeight, target.key));
 					} else {
 						displayDrawable = new BitmapDrawable(mCache.getContext().getResources(), fileInCache.getAbsolutePath());
 					}
 
 					if (displayDrawable==null) {
 						// we don't have that final file yet, use the download file to generate it
 						displayDrawable = targetBitmaps.get(target.key);
 						if (displayDrawable==null) {
 							displayDrawable = loadResourceDrawable(url);
 
 							if (displayDrawable!=null) {
 								if (target.getStorageTransform()!=null)
 									displayDrawable = new BitmapDrawable(target.getStorageTransform().transformBitmapForStorage(ViewLoader.drawableToBitmap(displayDrawable)));
 								else
 									bitmapWasInCache = true; // do not store the drawable as a bitmap as it is equal to the source
 							}
 						}
 
 						if (displayDrawable==null && downloadedToFile!=null && !downloaded) {
 							try {
 								downloadInTempFile(downloadedToFile);
 								// we need the dimensions of the downloaded file
 								tmpFileOptions.inJustDecodeBounds = true;
 								BitmapFactory.decodeFile(downloadedToFile.getAbsolutePath(), tmpFileOptions);
 								if (DEBUG_BITMAP_DOWNLOADER && tmpFileOptions.outHeight <= 0) LogManager.getLogger().i(PictureCache.LOG_TAG, this+" failed to get dimensions from "+downloadedToFile);
 								downloaded = true;
 							} finally {
 								if (!downloaded && downloadedToFile!=fileInCache)
 									downloadedToFile.delete();
 							}
 							checkAbort();
 						}
 
 						if (downloaded) {
 							Bitmap bitmap = null;
 							if (mCache.getBitmapCache()!=null) {
 								CacheableBitmapDrawable cachedDrawable = mCache.getBitmapCache().put(keyToBitmapCacheKey(target, url), downloadedToFile, getOutputOptions(tmpFileOptions.outWidth, tmpFileOptions.outHeight, target.key));
 								if (null!=cachedDrawable)
 									bitmap = cachedDrawable.getBitmap();
 							}
 							if (null==bitmap) {
 								bitmap = BitmapFactory.decodeFile(downloadedToFile.getAbsolutePath(), getOutputOptions(tmpFileOptions.outWidth, tmpFileOptions.outHeight, target.key));
 							}
 							if (bitmap!=null) {
 								int finalHeight = target.key.getBitmapHeight(bitmap.getWidth(), bitmap.getHeight());
 								if (finalHeight!=0 && finalHeight != bitmap.getHeight()) {
 									//LogManager.getLogger().v(" source size:"+bmp.getWidth()+"x"+bmp.getHeight());
 									Bitmap newBmp = Bitmap.createScaledBitmap(bitmap, (bitmap.getWidth() * finalHeight) / bitmap.getHeight(), finalHeight, true);
 									/*if (bitmap!=newBmp)
 										bitmap.recycle();*/
 									bitmap = newBmp;
 								}
 
 								if (target.getStorageTransform()!=null)
 									bitmap = target.getStorageTransform().transformBitmapForStorage(bitmap);
 
 								displayDrawable = new BitmapDrawable(mCache.getContext().getResources(), bitmap);
 							}
 						}
 					}
 
 					if (displayDrawable!=null) {
 						targetBitmaps.put(target.key, displayDrawable);
 						if (!bitmapWasInCache) {
 							CacheVariant variant = new CacheVariant(fileInCache, target.key);
 							targetNewBitmaps.put(variant, displayDrawable);
 						}
 					} else {
 						if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().d(PictureCache.LOG_TAG, this+" failed to get a bitmap for:"+target);
 						targetBitmaps.remove(target.key);
 					}
 				}
 
 				if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().i(PictureCache.LOG_TAG, this+" target:"+target+" fileInCache:"+fileInCache+" bitmap:"+targetBitmaps.get(target.key));
 				mTargetJobs.put(target, Boolean.TRUE); // this job is finished
 			}
 
 			downloadedToFile = null;
 		} catch (OutOfMemoryError e) {
 			mCache.getOutOfMemoryHandler().onOutOfMemoryError(e);
 			LogManager.getLogger().e(PictureCache.LOG_TAG, "Failed to load " + url, e);
 			/*} catch (InterruptedException e) {
 			LogManager.getLogger().e(PictureCache.TAG, "Interrupted while loading " + mURL, e);*/
 		} catch (DownloadFailureException e) {
 			// do nothing
 		} catch (Throwable e) {
 			LogManager.getLogger().e(PictureCache.LOG_TAG, "exception on "+url, e);
 		} finally {
 			mAborting.set(true); // after this point new targets are not OK for this job
 			if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().e(PictureCache.LOG_TAG, this+" finished loading targets:"+mTargetJobs+" bitmaps:"+targetBitmaps);
 
 			UIHandler.instance.runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
 					// tell the monitor we are done
 					//LogManager.getLogger().i(PictureCache.TAG, "finished download thread for " + mURL + " bmp:"+bmp + " rbmp:"+rbmp);
 					//LogManager.getLogger().i(PictureCache.TAG, "send display bitmap "+mURL+" aborted:"+abortRequested.get()+" size:"+reqTargets.size());
 					//LogManager.getLogger().i(PictureCache.TAG, "ViewUpdate loop "+mURL+" aborted:"+abortRequested.get()+" size:"+reqTargets.size()+" bmp:"+bmp+" rbmp:"+rbmp);
 					for (Entry<PictureJob, Boolean> targetEntry : mTargetJobs.entrySet()) {
 						if (targetEntry.getValue()==Boolean.TRUE) {
 							PictureJob target = targetEntry.getKey();
 							//LogManager.getLogger().i(PictureCache.TAG, false, "ViewUpdate "+mURL);
 							Drawable drawable = targetBitmaps.get(target.key);
 
 							if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().i(PictureCache.LOG_TAG, this+" display "+drawable+" in "+target.mDisplayHandler+" key:"+target.key);
 							if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().v(PictureCache.LOG_TAG, this+"  targets:"+mTargetJobs+" bitmaps:"+targetBitmaps);
 							//LogManager.getLogger().i(PictureCache.TAG, "display "+mURL+" in "+j+" abort:"+abortRequested);
 							if (drawable!=null) {
 								if (target.getDisplayTransform()!=null) {
 									Bitmap bitmap = ViewLoader.drawableToBitmap(drawable);
 									Bitmap transformedBitmap = target.getDisplayTransform().transformBitmap(bitmap);
 									if (transformedBitmap!=bitmap)
 										drawable = new BitmapDrawable(mCache.getContext().getResources(), transformedBitmap);
 								}
 
 								target.mDisplayHandler.drawBitmap(drawable, url, target.drawCookie, mCache.getBitmapCache(), false);
 							} else
 								target.mDisplayHandler.drawErrorPicture(url, mCache.getBitmapCache());
 						}
 					}
 					mTargetJobs.clear();
 					targetBitmaps.clear();
 				}
 			});
 
 			if (mMonitor!=null)
 				mMonitor.onJobFinishedWithNewBitmaps(this, targetNewBitmaps);
 
 			if (downloadedToFile!=null)
 				downloadedToFile.delete();
 		}
 	}
 
 	/**
 	 * Add a handler for when the URL is downloaded and start the download+processing if it wasn't started
 	 * @param job
 	 * @return {@code false} if the job was not added to this target (if the download is aborting)
 	 */
 	synchronized boolean addJob(PictureJob job) {
 		if (BuildConfig.DEBUG && !job.url.equals(url)) throw new InvalidParameterException(this+" wrong job URL "+job);
 
 		if (mAborting.get()) {
 			if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().w(PictureCache.LOG_TAG, this+ " is aborting");
 			return false;
 		}
 
 		//LogManager.getLogger().i(PictureCache.TAG, "add recipient view "+view+" for " + mURL);
 		if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().e(PictureCache.LOG_TAG, this+" addJob "+job+" key:"+job.key);
 
 		mCanDownload |= job.mConcurrencyHandler.isDownloadAllowed();
 
 		if (mItemDate < job.mFreshDate)
 			mItemDate = job.mFreshDate;
 
 		if (mLifeSpan==null)
 			mLifeSpan = job.mLifeSpan;
 		else if (mLifeSpan.compare(job.mLifeSpan)<0)
 			mLifeSpan = job.mLifeSpan;
 
 		Boolean runningState = mTargetJobs.get(job);
 		if (runningState!=Boolean.TRUE) {
 			mTargetJobs.put(job, null); // mark as pending for processing
 		} else {
 			if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().d(PictureCache.LOG_TAG, this+" job "+job+" already pending");
 		}
 
 		return true;
 	}
 
 	/**
 	 * Remove a render job from the list of targets
 	 * @param job
 	 * @return {@code null} if the job was not handled here, {@code Boolean.TRUE} if the download is going to be aborted
 	 */
 	synchronized boolean removeJob(PictureJob job) {
 		boolean deleted = false;
 
 		if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().e(PictureCache.LOG_TAG, this+" removeJob "+job);
 		if (mTargetJobs.containsKey(job)) {
 			mTargetJobs.put(job, Boolean.FALSE); // mark as invalid
 			deleted = true;
 		}
 		/*for (int i=0;i<mTargetJobs.size();++i) {
 				if (mTargetJobs.get(i).job.mDisplayHandler.equals(job.mDisplayHandler)) {
 					deleted = mTargetJobs.remove(i)!=null;
 					break;
 				}
 			}*/
 
 		if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().e(PictureCache.LOG_TAG, this+" removeJob "+job+" = "+deleted+" remains:"+mTargetJobs.size());
 		/*if (deleted) {
 			//LogManager.getLogger().v(" deleted job view:"+target+" for "+mURL);
 			//target.setLoadingURL(mCache, mURL);
 			job.mDisplayHandler.drawDefaultPicture(url, mCache.getBitmapCache());
 		}*/
 		//else LogManager.getLogger().i(PictureCache.TAG, " keep downloading URL:" + mURL + " remaining views:" + reqViews.size() + " like view:"+reqViews.get(0));
 		return deleted;
 	}
 
 	private BitmapFactory.Options getOutputOptions(int srcWidth, int srcHeight, CacheKey key) {
 		BitmapFactory.Options opts = new BitmapFactory.Options();
 		if (srcHeight <= 0) {
 			LogManager.getLogger().i(PictureCache.LOG_TAG, "could not get the dimension for " + url+" use raw decoding");
 		} else {
 			int finalHeight = key.getBitmapHeight(srcWidth, srcHeight);
 
 			if (finalHeight>0 && srcHeight > finalHeight*2) {
 				//LogManager.getLogger().e(PictureCache.TAG, " Picture scaling by: " + scale +" from Height:" + opts.outHeight + " to "+finalHeight+" for "+mURL);
 				opts.inSampleSize = (int) FloatMath.floor((float)srcHeight / finalHeight);
 			}
 			else opts.inSampleSize = 1;
 		}
 		//opts.inInputShareable = true;
 		//opts.inPurgeable = true;
 
 		return opts;
 	}
 
 	private static class AbortDownload extends DownloadFailureException {
 		private static final long serialVersionUID = 5568245153235248681L;
 	}
 
 	/**
 	 * @throws AbortDownload if we should not download or decode any further
 	 */
 	private synchronized void checkAbort() throws AbortDownload {
 		for (Entry<PictureJob, Boolean> targetEntry : mTargetJobs.entrySet()) {
 			if (targetEntry.getValue()!=Boolean.FALSE) {
 				// there's still a valid job pending to be processed or rendered
 				return;
 			}
 		}
 		if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().i(PictureCache.LOG_TAG, this+ " no more targets, aborting");
 		mAborting.set(true);
 		throw new AbortDownload();
 	}
 
 	private void downloadInTempFile(File tmpFile) throws DownloadFailureException {
 		//LogManager.getLogger().i(PictureCache.TAG, "loading "+mURL);
 		InputStream is = null;
 		try {
 			try {
 				is = mCache.getContext().getContentResolver().openInputStream(Uri.parse(url));
 				//LogManager.getLogger().v("using the content resolver for "+mURL);
 			} catch (FileNotFoundException e) {
 				//LogManager.getLogger().d(PictureCache.TAG, false, "cache error trying ContentResolver on "+mURL);
 				if (null!=networkLoader)
 					is = networkLoader.loadURL(url);

				if (null==is) {
 					URL url = new URL(this.url);
 					URLConnection conn = url.openConnection();
 					conn.setConnectTimeout(CONNECT_TIMEOUT_DL);
 					conn.setUseCaches(false);
 					conn.setRequestProperty("Accept-Encoding", "identity");
 					//LogManager.getLogger().e(PictureCache.TAG, conn.getContentEncoding()+" encoding for "+mURL);
 					checkAbort();
 					try {
 						is = conn.getInputStream();
 					} catch (FileNotFoundException ee) {
 						throw new DownloadFailureException("cache URL not found "+url, e);
 					} catch (Exception ee) {
 						throw new DownloadFailureException("cache error opening "+url, e);
 					}
 				}
 			}
 
 			if (is==null) {
 				throw new DownloadFailureException("impossible to get a stream for "+url);
 			}
 
 			checkAbort();
 			// store the stream in a temp file
 			BufferedInputStream bis = new BufferedInputStream(is);
 			FileOutputStream out = new FileOutputStream(tmpFile);
 			try {
 				byte[] data = new byte[1422];
 				int readAmount = is.read(data);
 				while (readAmount >= 0) {
 					out.write(data, 0, readAmount);
 					checkAbort();
 					readAmount = is.read(data);
 				}
 			} finally {
 				bis.close();
 				out.flush();
 				out.close();
 			}
 
 			//LogManager.getLogger().v(" got direct:"+bmp);
 		} catch (MalformedURLException e) {
 			throw new DownloadFailureException("bad URL " + url, e);
 		} catch (UnknownHostException e) {
 			throw new DownloadFailureException("host not found in "+url, e);
 		} catch (OutOfMemoryError e) {
 			mCache.getOutOfMemoryHandler().onOutOfMemoryError(e);
 			throw new DownloadFailureException("Could not decode image " + url, e);
 		} catch (IOException e) {
 			throw new DownloadFailureException("Could not read " + url, e);
 		} finally {
 			try {
 				if (is!=null)
 					is.close();
 			} catch (IOException e) {
 				LogManager.getLogger().e(PictureCache.LOG_TAG, "Could not close " + is, e);
 			}
 		}
 	}
 
 	public static String keyToBitmapCacheKey(PictureJob job, String url) {
 		final StringBuilder bitmapKey = new StringBuilder(job.key.toString());
 		bitmapKey.append(url);
 		if (job.getStorageTransform() != null)
 			bitmapKey.append(job.getStorageTransform().getVariantPostfix());
 		if (job.getDisplayTransform() != null)
 			bitmapKey.append(job.getDisplayTransform().getVariant());
 		return bitmapKey.toString();
 	}
 
 	private static String resourcePath;
 
 	private synchronized Drawable loadResourceDrawable(String url) {
 		if (resourcePath==null)
 			resourcePath = "android.resource://"+mCache.getContext().getPackageName()+"/";
 		if (!url.startsWith(resourcePath))
 			return null;
 		return mCache.getContext().getResources().getDrawable(Integer.valueOf(url.substring(resourcePath.length())));
 	}
 
 }
