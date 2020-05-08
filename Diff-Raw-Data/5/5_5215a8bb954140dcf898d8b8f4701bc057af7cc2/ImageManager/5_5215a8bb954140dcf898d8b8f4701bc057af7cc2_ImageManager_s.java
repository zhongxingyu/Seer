 package com.adtworker.mail;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.SystemClock;
 import android.util.Log;
 import android.view.View;
 
 public class ImageManager {
 
 	public enum IMAGE_PATH_TYPE {
 		LOCAL_ASSETS, // assets/
 		PHONE_STORAGE, // /mnt/sdcard
 		EXTERNAL_SD_CARD, // /mnt/sdcard-ext
 		PACKAGE_PRIVATE, // /data/data/$package_name
 		REMOTE_HTTP_URL, // http://xxx
 		IMAGE_PATH_TYPE_LEN
 	}
 
 	private final String TAG = "ImageManager";
 	private final String IMAGE_SUBFOLDER_IN_ASSETS = "pics";
 	private final Context mContext;
 	private boolean mInitListFailed = false;
 	private boolean mInitInProcess = false;
 	private int mCurrentImageIndex = INVALID_PIC_INDEX;
 	private final int[] mCurrentIndexArray = new int[IMAGE_PATH_TYPE.IMAGE_PATH_TYPE_LEN
 			.ordinal()];
 	private final int mSearchPageNum = 1;
 	private final int mSearchPageSize = 80;
 	private Bitmap mCurrentBitmap = null;
 
 	public final static int INVALID_PIC_INDEX = -1;
 
 	public IMAGE_PATH_TYPE mImagePathType;
 	private IMAGE_PATH_TYPE mImagePathTypeLast;
 	private Map<IMAGE_PATH_TYPE, ArrayList<AdtImage>> mImageListMap = new HashMap<IMAGE_PATH_TYPE, ArrayList<AdtImage>>();
 	public ArrayList<AdtImage> mImageList = null;
 	private final ArrayList<String> mQueryKeywords = new ArrayList<String>();
 
 	private static ImageManager mImageManager = null;
 	public static ImageManager getInstance(Context context) {
 		if (null == mImageManager && null != context) {
 			mImageManager = new ImageManager(context);
 		}
 		return mImageManager;
 	}
 
 	public ImageManager(Context context) {
 		mContext = context;
 		for (int i = 0; i < mCurrentIndexArray.length; i++)
 			mCurrentIndexArray[i] = INVALID_PIC_INDEX;
 		mCurrentImageIndex = INVALID_PIC_INDEX;
 
 		mImagePathType = IMAGE_PATH_TYPE.LOCAL_ASSETS;
 		setImagePathType(mImagePathType);
 	}
 
 	public void setImagePathType(IMAGE_PATH_TYPE type) {
		dumpCurrentIndexArray();
 		mCurrentIndexArray[mImagePathType.ordinal()] = mCurrentImageIndex;
		dumpCurrentIndexArray();
 		mImagePathTypeLast = mImagePathType;
 		mImagePathType = type;
 		initImageList();
 	}
 
 	private void dumpCurrentIndexArray() {
 		String strDump = "";
 		for (int i = 0; i < mCurrentIndexArray.length; i++) {
 			strDump += String.valueOf(mCurrentIndexArray[i]) + " ";
 		}
 		Log.v(TAG, "mCurrentIndexArray = " + strDump);
 	}
 
 	public IMAGE_PATH_TYPE getImagePathType() {
 		return mImagePathType;
 	}
 
 	public boolean isInitListFailed() {
 		return mInitListFailed;
 	}
 
 	public boolean isInitInProcess() {
 		return mInitInProcess;
 	}
 
 	public void setQueryKeyword(String args) {
 		mQueryKeywords.clear();
 		String[] words = args.split(" ");
 		for (String word : words) {
 			mQueryKeywords.add(word);
 		}
 	}
 	public int getImageListSize() {
 		return mImageList.size();
 	}
 
 	private boolean isValidIndex(int index) {
 		if (index < 0 || index >= getImageListSize())
 			return false;
 		else
 			return true;
 	}
 	public void setCurrent(int index) {
 		if (!isValidIndex(index)) {
 			int size = getImageListSize();
 			index = index + size % size;
 		}
 
 		mCurrentImageIndex = index;
 		if (mCurrentBitmap != null) {
 			mCurrentBitmap = null;
 		}
 	}
 
 	public boolean isCurrentAsset() {
 		return mImageList.get(mCurrentImageIndex).isAsset;
 	}
 
 	public String getCurrentStr() {
 		return mImageList.get(mCurrentImageIndex).urlFull;
 	}
 
 	public String getCurrentStrTb() {
 		return mImageList.get(mCurrentImageIndex).urlThumb;
 	}
 
 	public String getCurrentStrLocal() {
 		return getCachedFilename(mImageList.get(mCurrentImageIndex).urlFull);
 	}
 
 	public String getImageStr(int step) {
 		mCurrentImageIndex = getNextSteppedIndex(step);
 		return getCurrentStr();
 	}
 
 	public int getCurrent() {
 		return mCurrentImageIndex;
 	}
 
 	public int getNext() {
 		return getNextSteppedIndex(1);
 	}
 
 	public int getPrev() {
 		return getNextSteppedIndex(-1);
 	}
 
 	public int getNextSteppedIndex(int step) {
 		int size = getImageListSize();
 		return (size + mCurrentImageIndex + step) % size;
 	}
 
 	private Bitmap getBitmap(String strImage) {
 		Bitmap bitmap = null;
 		try {
 			if (mImagePathType == IMAGE_PATH_TYPE.LOCAL_ASSETS) {
 				InputStream is = mContext.getAssets().open(strImage);
 				bitmap = BitmapFactory.decodeStream(is);
 			} else if (mImagePathType == IMAGE_PATH_TYPE.REMOTE_HTTP_URL) {
 				bitmap = getBitmapFromSDCard(strImage);
 
 				if (bitmap == null) {
 					String tbUrl = getCurrentStrTb();
 					Bitmap bm = getBitmapFromSDCard(tbUrl);
 					if (bm == null) {
 						bm = getBitmapFromUrl(tbUrl);
 					}
 					new loadImageTask().execute(strImage,
 							Integer.toString(mCurrentImageIndex));
 					bitmap = bm;
 				} else {
 					((WatchActivity) mContext).mProgressIcon
 							.setVisibility(View.GONE);
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return bitmap;
 	}
 
 	public Bitmap getCurrentBitmap() {
 		if (mCurrentBitmap == null) {
 			String imgstr = getCurrentStr();
 			mCurrentBitmap = getBitmap(imgstr);
 		}
 		return mCurrentBitmap;
 	}
 
 	public Bitmap getImageBitmap(int step) {
 		String imgstr = getImageStr(step);
 		mCurrentBitmap = getBitmap(imgstr);
 		return mCurrentBitmap;
 	}
 
 	public Bitmap getPosBitmap(int pos, boolean tbFirst) {
 		if (pos < 0 || pos >= getImageListSize())
 			return null;
 
 		AdtImage adt = mImageList.get(pos);
 		String url = adt.urlFull;
 		if (tbFirst && adt.hasThumb) {
 			url = adt.urlThumb;
 		}
 		Bitmap bitmap = null;
 		try {
 			if (mImagePathType == IMAGE_PATH_TYPE.LOCAL_ASSETS) {
 				InputStream is = mContext.getAssets().open(url);
 				BitmapFactory.Options opt = new BitmapFactory.Options();
 				if (tbFirst)
 					opt.inSampleSize = 4;
 				else
 					opt.inSampleSize = 1;
 				bitmap = BitmapFactory.decodeStream(is, null, opt);
 			} else if (mImagePathType == IMAGE_PATH_TYPE.REMOTE_HTTP_URL) {
 				bitmap = getBitmapFromSDCard(url);
 				if (bitmap == null) {
 					bitmap = getBitmapFromUrl(url);
 				}
 				if (tbFirst && adt.hasThumb && bitmap == null) {
 					url = adt.urlFull;
 					bitmap = getBitmapFromSDCard(url, 4);
 				}
 				if (!tbFirst && bitmap == null) {
 					bitmap = getBitmapFromUrl(url);
 					if (bitmap != null)
 						writeBitmap2AppCache(bitmap, url);
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return bitmap;
 	}
 
 	private void initImageList() {
 		if (mImageListMap.get(mImagePathType) != null) {
 			mImageList = mImageListMap.get(mImagePathType);
 			return;
 		}
 		switch (mImagePathType) {
 			case LOCAL_ASSETS :
 				mInitListFailed = true;
 				ArrayList<AdtImage> tempImageList = new ArrayList<AdtImage>();
 				ArrayList<String> arrayList = getAssetsImagesList(IMAGE_SUBFOLDER_IN_ASSETS);
 				for (int i = 0; i < arrayList.size(); i++) {
 					AdtImage image = new AdtImage(arrayList.get(i), true);
 					tempImageList.add(image);
 				}
 				arrayList.clear();
 				if (tempImageList.size() != 0) {
 					mImageListMap.put(IMAGE_PATH_TYPE.LOCAL_ASSETS,
 							tempImageList);
 					mImageList = mImageListMap
 							.get(IMAGE_PATH_TYPE.LOCAL_ASSETS);
 					mCurrentImageIndex = mCurrentIndexArray[mImagePathType
 							.ordinal()];
 					mInitListFailed = false;
 				} else {
 					mImagePathType = mImagePathTypeLast;
 				}
 				break;
 
 			case REMOTE_HTTP_URL :
 				new getImagesTask().execute();
 				break;
 		}
 	}
 	private class getImagesTask
 			extends
 				AsyncTask<Void, Void, ArrayList<AdtImage>> {
 		WatchActivity activity = (WatchActivity) mContext;
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			activity.mProgressBar.setProgress(0);
 			activity.mProgressBar.setVisibility(View.VISIBLE);
 			activity.mProgressBar.setMax(100);
 			activity.EnableNextPrevButtons(false);
 			mInitListFailed = true;
 			mInitInProcess = true;
 		}
 		@Override
 		protected ArrayList<AdtImage> doInBackground(Void... params) {
 			int count = 0;
 			ArrayList<AdtImage> tempImageList = new ArrayList<AdtImage>();
 			try {
 				for (int k = 0; k < mQueryKeywords.size(); k++) {
 					Log.d(TAG, "querying word " + mQueryKeywords.get(k));
 					String keyword = URLEncoder.encode(mQueryKeywords.get(k),
 							"GBK");
 
 					for (int i = 0; i < mSearchPageNum; i++) {
 
 						List<AdtImage> temp = ImageSearchAdapter.getImgList(
 								keyword, 480, 800, i + 1, i * mSearchPageSize);
 
 						for (int j = 0; j < temp.size(); j++) {
 							activity.mProgressBar.setProgress(++count);
 							AdtImage img = temp.get(j);
 							img.urlFull = URLDecoder.decode(img.urlFull);
 							img.urlThumb = URLDecoder.decode(img.urlThumb);
 
 							if (img.urlFull.toLowerCase().endsWith(".gif"))
 								continue;
 
 							if (count % 50 == 0) {
 								Log.d(TAG, "Adding " + count + ") url="
 										+ img.urlFull + ", tbUrl="
 										+ img.urlThumb);
 							}
 							tempImageList.add(img);
 						}
 					}
 				}
 				Log.d(TAG, "image list size = " + tempImageList.size());
 
 				activity.mProgressBar.setProgress(mSearchPageNum
 						* mSearchPageSize);
 				if (tempImageList.size() != 0) {
 					mInitListFailed = false;
 				}
 
 			} catch (Exception e) {
 				e.printStackTrace();
 				return null;
 			}
 
 			return tempImageList;
 		}
 		@Override
 		protected void onPostExecute(ArrayList<AdtImage> result) {
 			super.onPostExecute(result);
 			mInitInProcess = false;
 			activity.EnableNextPrevButtons(true);
 			SystemClock.sleep(200);
 			activity.mProgressBar.setVisibility(View.GONE);
 
 			if (!mInitListFailed) {
 				mImageListMap.put(IMAGE_PATH_TYPE.REMOTE_HTTP_URL, result);
 				mImageList = mImageListMap.get(mImagePathType);
 				new loadAllImageTask().execute();
 			} else {
 				mImagePathType = mImagePathTypeLast;
 			}
 			mCurrentImageIndex = mCurrentIndexArray[mImagePathType.ordinal()];
 		}
 	}
 
 	private class loadAllImageTask extends AsyncTask<Void, Void, Void> {
 		@Override
 		protected Void doInBackground(Void... params) {
 			if (getImageListSize() == 0)
 				return null;
 
 			for (int i = 0; i < mImageList.size(); i++) {
 				AdtImage img = mImageList.get(i);
 				String url;
 				if (img.hasThumb) {
 					url = img.urlThumb;
 				} else {
 					url = img.urlFull;
 				}
 
 				Bitmap bitmap = getBitmapFromSDCard(url);
 				if (bitmap == null) {
 					bitmap = getBitmapFromUrl(url);
 					if (bitmap != null) {
 						writeBitmap2AppCache(bitmap, url);
 					} else {
 						Log.e(TAG, "image " + i + " is null.");
 					}
 				}
 			}
 			return null;
 		}
 
 	}
 
 	private class loadImageTask extends AsyncTask<String, Integer, Bitmap> {
 		WatchActivity activity = (WatchActivity) mContext;
 		int loadingImgID = INVALID_PIC_INDEX;
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			activity.mProgressIcon.setVisibility(View.VISIBLE);
 			// activity.EnableNextPrevButtons(false);
 		}
 
 		@Override
 		protected Bitmap doInBackground(String... params) {
 			loadingImgID = Integer.parseInt(params[1]);
 			publishProgress(0);
 			Bitmap bitmap;
 			String url = params[0];
 			// bitmap = getBitMapFromSDCard(url);
 			// if (bitmap != null) {
 			// Log.d(TAG, "got bitmap from AppCache.");
 			// publishProgress(100);
 			// return bitmap;
 			// }
 			bitmap = getBitmapFromUrl(url);
 			if (bitmap != null) {
 				Log.d(TAG, "got bitmap from Remote: " + url);
 				writeBitmap2AppCache(bitmap, url);
 				publishProgress(100);
 				return bitmap;
 			}
 
 			// Log.e(TAG, "failed to get image " + url
 			// + ", Using default instead.");
 			// bitmap = BitmapFactory.decodeResource(mContext.getResources(),
 			// R.drawable.no_image);
 			publishProgress(100);
 			return bitmap;
 		}
 		@Override
 		protected void onProgressUpdate(Integer... progress) {
 			activity.mProgressIcon.setProgress(progress[0]);
 		}
 		@Override
 		protected void onPostExecute(Bitmap result) {
 			super.onPostExecute(result);
 			if (result != null && loadingImgID == mCurrentImageIndex) {
 				activity.setImageView(result);
 				mCurrentBitmap = result;
 			}
 			if (loadingImgID == mCurrentImageIndex) {
 				activity.mProgressIcon.setVisibility(View.GONE);
 			}
 			// activity.EnableNextPrevButtons(true);
 		}
 	}
 	public Bitmap getBitmapFromUrl(String url) {
 
 		Bitmap bitmap = null;
 		URL u = null;
 		HttpURLConnection conn = null;
 		InputStream is = null;
 
 		try {
 			u = new URL(url);
 		} catch (MalformedURLException e2) {
 			Log.e(TAG, "incorrect url " + url);
 			e2.printStackTrace();
 			return null;
 		}
 		try {
 			conn = (HttpURLConnection) u.openConnection();
 		} catch (IOException e1) {
 			Log.e(TAG, "IOException on connecting " + url);
 			e1.printStackTrace();
 			return null;
 		}
 		conn.setConnectTimeout(5000);
 		try {
 			is = conn.getInputStream();
 			BitmapFactory.Options options = new BitmapFactory.Options();
 			options.inSampleSize = 1;
 			bitmap = BitmapFactory.decodeStream(is, null, options);
 			is.close();
 		} catch (IOException e) {
 			Log.e(TAG, "IOException on loading " + url);
 			e.printStackTrace();
 			conn.disconnect();
 			return null;
 		}
 		conn.disconnect();
 		return bitmap;
 	}
 	public Bitmap getBitmapFromSDCard(String url) {
 		Bitmap bitmap = null;
 		try {
 			FileInputStream fis = new FileInputStream(getFile(url));
 			bitmap = BitmapFactory.decodeStream(fis);
 			fis.close();
 		} catch (Exception e) {
 			return bitmap;
 		}
 		return bitmap;
 	}
 
 	public Bitmap getBitmapFromSDCard(String url, int inSampleSize) {
 		Bitmap bitmap = null;
 		try {
 			FileInputStream fis = new FileInputStream(getFile(url));
 			BitmapFactory.Options opt = new BitmapFactory.Options();
 			opt.inSampleSize = inSampleSize;
 			bitmap = BitmapFactory.decodeStream(fis, null, opt);
 			fis.close();
 		} catch (Exception e) {
 			return bitmap;
 		}
 		return bitmap;
 	}
 
 	private void writeInputStream2AppCache(InputStream is, String url) {
 		try {
 			FileOutputStream fos = new FileOutputStream(getFile(url), false);
 			int len = 0;
 			byte[] b = new byte[is.available()];
 
 			while ((len = is.read(b)) != -1) {
 				fos.write(b, 0, len);
 			}
 			if (null != is) {
 				is.close();
 			}
 			if (null != fos) {
 				fos.close();
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void writeBitmap2AppCache(Bitmap bitmap, String url) {
 		if (bitmap == null)
 			return;
 
 		try {
 			FileOutputStream fos = new FileOutputStream(getFile(url), false);
 			byte[] bitmapByte = Bitmap2Byte(bitmap);
 			ByteArrayInputStream bis = new ByteArrayInputStream(bitmapByte);
 			int len = 0;
 			byte[] b = new byte[bis.available()];
 			while ((len = bis.read(b)) != -1) {
 				fos.write(b, 0, len);
 			}
 			if (null != bis) {
 				bis.close();
 			}
 			if (null != fos) {
 				fos.close();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private File getFile(String url) {
 		File file = null;
 		try {
 			file = new File(getCachedFilename(url));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return file;
 	}
 
 	private String getCachedFilename(String url) {
 		String path = Utils.getAppCacheDir(mContext);
 		return path + File.separator + url.hashCode();
 
 		// return path + File.separator + getHostname(url) + "."
 		// + getImageFilename(url);
 	}
 
 	private String getHostname(String imgPath) {
 		int start = imgPath.indexOf("/") + 1;
 		start = imgPath.indexOf("/", start) + 1;
 		int end = imgPath.indexOf("/", start);
 		if (start != -1 && end != -1)
 			return imgPath.substring(start, end);
 		else
 			return null;
 	}
 
 	private String getImageFilename(String imgPath) {
 		int start = imgPath.lastIndexOf("/");
 		int end = imgPath.lastIndexOf("?");
 		if (start != -1 && end == -1) {
 			return imgPath.substring(start + 1);
 		} else if (start != -1 && end != -1) {
 			return imgPath.substring(start + 1, end - 1);
 		} else {
 			return null;
 		}
 	}
 
 	private void unzipFile(String targetPath, String zipFilePath,
 			boolean isAssets) {
 
 		try {
 			File zipFile = new File(zipFilePath);
 			InputStream is;
 			if (isAssets) {
 				is = mContext.getAssets().open(zipFilePath);
 			} else {
 				is = new FileInputStream(zipFile);
 			}
 			ZipInputStream zis = new ZipInputStream(is);
 			ZipEntry entry = null;
 			while ((entry = zis.getNextEntry()) != null) {
 				String zipPath = entry.getName();
 				try {
 
 					if (entry.isDirectory()) {
 						File zipFolder = new File(targetPath + File.separator
 								+ zipPath);
 						if (!zipFolder.exists()) {
 							zipFolder.mkdirs();
 						}
 					} else {
 						File file = new File(targetPath + File.separator
 								+ zipPath);
 						if (!file.exists()) {
 							File pathDir = file.getParentFile();
 							pathDir.mkdirs();
 							file.createNewFile();
 						}
 
 						FileOutputStream fos = new FileOutputStream(file);
 						int bread;
 						while ((bread = zis.read()) != -1) {
 							fos.write(bread);
 						}
 						fos.close();
 
 					}
 
 				} catch (Exception e) {
 					continue;
 				}
 			}
 			zis.close();
 			is.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private boolean isValidImage(File file) {
 		if (file.isFile()) {
 			String fileName = file.getName();
 			return isValidImage(fileName);
 		}
 		return false;
 	}
 
 	private boolean isValidImage(String filename) {
 		filename = filename.toLowerCase();
 		if (filename.endsWith(".jpg") || filename.endsWith(".png")
 				|| filename.endsWith(".bmp")) {
 			return true;
 		}
 		return false;
 	}
 
 	private ArrayList<String> getImagesList(String path) {
 		ArrayList<String> arrayList = new ArrayList<String>();
 		File[] files = new File(path).listFiles();
 		for (File file : files) {
 			if (file.isDirectory()) {
 				ArrayList<String> tmpArrayList = getImagesList(path
 						+ File.separator + file.getName());
 				for (int i = 0; i < tmpArrayList.size(); i++) {
 					arrayList.add(file.getName() + File.separator
 							+ tmpArrayList.get(i));
 				}
 			}
 			if (isValidImage(file)) {
 				arrayList.add(file.getName());
 			}
 		}
 		return arrayList;
 	}
 
 	private ArrayList<String> getAssetsImagesList(String path) {
 		if (path == null)
 			path = "";
 
 		ArrayList<String> arrayList = new ArrayList<String>();
 		try {
 			String[] filenames = mContext.getAssets().list(path);
 			for (int i = 0; i < filenames.length; i++) {
 				String filepath = path + File.separator + filenames[i];
 				if (isValidImage(filepath))
 					arrayList.add(filepath);
 				else {
 					ArrayList<String> tmp = getAssetsImagesList(filepath);
 					for (int j = 0; j < tmp.size(); j++)
 						arrayList.add(tmp.get(j));
 				}
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return arrayList;
 	}
 
 	private byte[] Bitmap2Byte(Bitmap bm) {
 
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
 		return baos.toByteArray();
 	}
 
 	private Bitmap Byte2Bitmap(byte[] b) {
 		if (b.length == 0) {
 			return null;
 		}
 		return BitmapFactory.decodeByteArray(b, 0, b.length);
 	}
 }
