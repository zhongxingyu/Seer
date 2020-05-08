 package com.node.imagedownload;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import com.node.browser.R;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Environment;
 import android.support.v4.util.LruCache;
 import android.util.Log;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 
 public class ImageDownload {
 
 	final int BUFFER_SIZE = 3 * 1024; // 3KB
 	final long EXPIRE_TIME = 5 * 60 * 60 * 1000;// 5小时
 	final int WAIT_TIMEOUT = 20 * 60 * 1000; // 20秒
 	final String baseName = "NodeBrowser";
 	public static final String rootPath = Environment
 			.getExternalStorageDirectory() + "/NodeBrowser/largeImg/";
 
 	private static Bitmap default_icon;
 
 	private static void initData(Context context) {
 		if (default_icon == null) {
 			BitmapFactory.Options options = new BitmapFactory.Options();
 			options.inPreferredConfig = Bitmap.Config.RGB_565;
 			options.inPurgeable = true;
 			options.inInputShareable = true;
 
 			default_icon = BitmapFactory.decodeStream(context.getResources()
 					.openRawResource(R.drawable.ic_launcher), null, options);
 		}
 	}
 
 	/*
 	 * LRUCache内存缓存
 	 */
 	static final LruCache<String, byte[]> mCache = new LruCache<String, byte[]>(
 			80);
 
 	private ImageDownload() {
 	};
 
 	private static ImageDownload mImageDownload;
 
 	public static ImageDownload instance(Context context) {
 		if (mImageDownload == null) {
 			mImageDownload = new ImageDownload();
 			initData(context);
 		}
 		return mImageDownload;
 	}
 
 	/**
 	 * 
 	 * @param item
 	 */
 	private void downImageAsyncWay(ImageDownloadItem item) {
 		// 设置ImageView与Url的键值对
 		ImageUrlStatus.setImageViewUrlPair(item.mImageView, item.mUrl);
 		// 从缓存中获取图片字节流
 		catchDataInCache(item);
 		if (item.mCatchData) {
 			setImgDataFlow(item.mImageView, item.data);
 		} else {
 			if (!item.mSdcardExist) {
 				return;
 			} else {
 				if (ImageUrlStatus.canRequestUrl(item.mUrl)) {
 					startDownloadFromNetOrFile(item);
 				} else {
 					return;
 				}
 			}
 		}
 	}
 
 	/**
 	 * 开始下载网络图片
 	 * 
 	 * @param item
 	 */
 	private void startDownloadFromNetOrFile(ImageDownloadItem item) {
 		// 标注url为正在请求
 		ImageUrlStatus.setUrlRequeststatus(item.mUrl,
 				ImageUrlStatus.STATUS_REQUESTING);
 		AsyncDownloadTask asyncTask = new AsyncDownloadTask();
 		asyncTask.execute(item);
 	}
 
 	/**
 	 * 下载大图片，有progressBar 0-100%
 	 * 
 	 * @param imgUrl
 	 * @param imgview
 	 * @param loadMemCache
 	 *            大图片不推荐使用缓存
 	 * @param progressBar
 	 */
 	public void downLargeImageAsyncWay(String imgUrl, ImageView imgview,
 			boolean loadMemCache, ProgressBar progressBar) {
 		ImageDownloadItem item = new ImageDownloadItem(imgUrl, progressBar,
 				imgview, loadMemCache);
 		downImageAsyncWay(item);
 	}
 
 	/**
 	 * 下载大图片，没有progressBar
 	 * 
 	 * @param imgUrl
 	 * @param imgview
 	 * @param loadMemCache
 	 */
 	public void downLargeImageAsyncWay(String imgUrl, ImageView imgview,
 			boolean loadMemCache) {
 		ImageDownloadItem item = new ImageDownloadItem(imgUrl, null, imgview,
 				loadMemCache);
 		downImageAsyncWay(item);
 	}
 
 	/**
 	 * 下载小图片，没有progressBar
 	 * 
 	 * @param imgUrl
 	 * @param imgView
 	 * @param byMemCache
 	 *            是否走内存缓存 小图片建议走内存缓存
 	 */
 	public void downSmallImageAsyncWay(String imgUrl, ImageView imgView,
 			boolean loadMemCache) {
 		ImageDownloadItem item = new ImageDownloadItem(imgUrl, null, imgView,
 				loadMemCache);
 		downImageAsyncWay(item);
 	}
 
 	/**
 	 * 下载小图片，没有progressbar，默认读取内存缓存
 	 * 
 	 * @param imgUrl
 	 * @param imgView
 	 */
 	public void downSmallImageAsyncWay(String imgUrl, ImageView imgView) {
 		ImageDownloadItem item = new ImageDownloadItem(imgUrl, null, imgView,
 				true);
 		downImageAsyncWay(item);
 	}
 
 	public static class ImageDownloadItem {
 		String mUrl; // 图片地址
 		ProgressBar mProgressBar; // UI进度
 		ImageView mImageView; // 显示图片的控件
 		byte[] data; // 图片流
 		boolean mLoadMemCache; // 是否走内存缓存，大图片不走内存缓存，没什么其他原因，只因为大图片太大了
 		boolean mAddMemCache; // 是否需要将data加入内存缓存t
 		boolean mSdcardExist; // sd卡是否存在
 		boolean mCatchData; // 得到缓存文件
 		boolean mLoadFileBytes; // 加载文件流
 		boolean mLoadNetBytes; // 加载网络流
 		private File mDestnationFile; // 持久化的目标文件位置
 
 		/**
 		 * @param url
 		 *            the image url not null,not empty;
 		 * @param progressBar
 		 *            the progressbar to show 0-100% can be null;
 		 * @param imageView
 		 *            not null;
 		 */
 		public ImageDownloadItem(String url, ProgressBar progressBar,
 				ImageView imageView, boolean loadMemCache) {
 			mUrl = url;
 			mProgressBar = progressBar;
 			mImageView = imageView;
 			mLoadMemCache = loadMemCache;
 		}
 
 		File getDestnationFile() {
 			return mDestnationFile;
 		}
 
 		void setDestnationFile(File mDestnationFile) {
 			this.mDestnationFile = mDestnationFile;
 		}
 	}
 
 	/**
 	 * 图片下载的AsyncTask
 	 * 
 	 * @author zhenchuan
 	 * 
 	 */
 	class AsyncDownloadTask extends
 			AsyncTask<ImageDownloadItem, Integer, ImageDownloadItem> {
 		ImageDownloadItem item;
 
 		public AsyncDownloadTask() {
 		};
 
 		@Override
 		protected ImageDownloadItem doInBackground(ImageDownloadItem... params) {
 			item = params[0];
 			// 请求文件流
 			if (item.mLoadFileBytes) {
 				item.data = getPersisData(item.getDestnationFile());
 				item.mLoadNetBytes = !catchDataFlow(item.data);
 			}
 
 			// 请求网络流
 			if (item.mLoadNetBytes) {
 				item.data = getNetDataWithPersis(item.mUrl,
 						item.getDestnationFile());
 			}
 			item.mAddMemCache = true;// 需要放入LRUCache
 			return item;
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			int progress = values[0];
 			if (item.mProgressBar != null) {
 				item.mProgressBar.setProgress(progress);
 			}
 		}
 
 		@Override
 		protected void onPostExecute(final ImageDownloadItem result) {
			if (!catchDataFlow(result.data)) {
 				ImageUrlStatus.setUrlRequeststatus(result.mUrl,
 						ImageUrlStatus.STATUS_REQUESTED_ERROR);
 				Log.e("zhenchuan", "data is null");
 			} else {
 				// 设置Url的请求状态为请求成功
 				ImageUrlStatus.setUrlRequeststatus(result.mUrl,
 						ImageUrlStatus.STATUS_REQUESTED_SUCCESS);
 				// 防止ImageView被多次引用
 				if (ImageUrlStatus.getImageViewUrl(result.mImageView).equals(
 						result.mUrl)) {
 					setImgDataFlow(result.mImageView, result.data);
 				}
 				// 放入LRUCache
 				// 如果本身就是从LRUCache中取出则不作重复操作
 				if (result.mAddMemCache && result.mLoadMemCache) {
 					mCache.put(result.mUrl, result.data);
 				}
 				// 写入本地storage缓存
 				doAsyncTask(new Runnable() {
 					@Override
 					public void run() {
 						writeDataToFile(result.getDestnationFile(), result.data);
 					}
 				});
 			}
 		}
 
 		/**
 		 * 从网络中获取Url并持久化到本地
 		 * 
 		 * @param mUrl
 		 * @param out
 		 * @return
 		 */
 		private byte[] getNetDataWithPersis(String mUrl, File out) {
 			byte[] datas = null;
 			try {
 				URL url = new URL(mUrl);
 				URLConnection connect = url.openConnection();
 				connect.setReadTimeout(WAIT_TIMEOUT);
 				connect.connect();
 				// to show 0-100% progress
 				int imgLength = connect.getContentLength();
 				// download image
 				InputStream input = new BufferedInputStream(
 						connect.getInputStream());
 
 				int bufferSize;
 				if (imgLength < 100) { // 100B下
 					bufferSize = 50;
 				} else if (imgLength >= 100 && imgLength < 10 * 1024) { // 100B
 																		// ~
 																		// 1MB
 					bufferSize = imgLength / 100;
 				} else { // >1MB
 					bufferSize = 1024;
 				}
 				byte[] buffer = new byte[bufferSize]; // 保证每次读取1%
 				datas = loadInputStreamToBytes(imgLength, buffer, input);
 			} catch (MalformedURLException e) {
 				datas = null;
 				e.printStackTrace();
 			} catch (IOException e) {
 				datas = null;
 				e.printStackTrace();
 			}
 			return datas;
 		}
 
 		/**
 		 * 读取IO流到字节流中
 		 * 
 		 * @param bytesLength
 		 * @param buffer
 		 * @param is
 		 * @return
 		 */
 		public byte[] loadInputStreamToBytes(int bytesLength, byte[] buffer,
 				InputStream is) {
 			byte[] outs = null;
 			ByteArrayOutputStream bos = null;
 			try {
 				int count = 0;
 				int readLength = 0;
 				bos = new ByteArrayOutputStream();
 				while ((count = is.read(buffer)) != -1) {
					bos.write(buffer, 0, count);
 					readLength += count;
 					publishProgress((int) (readLength * 100 / bytesLength));
 				}
 				outs = bos.toByteArray();
 				bos.close();
 				is.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 				if (bos != null) {
 					try {
 						bos.close();
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					}
 				}
 				if (is != null) {
 					try {
 						is.close();
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					}
 				}
 				outs = null;
 			}
 			return outs;
 		}
 
 		/**
 		 * 读取本地文件获取图片缓存
 		 * 
 		 * @param file
 		 * @return
 		 */
 		private byte[] getPersisData(File file) {
 			// 判断是否过期
 			long lastModify = file.lastModified();
 			long current = System.currentTimeMillis();
 			if (lastModify + EXPIRE_TIME < current) {
 				return null;// 过期返回null
 			}
 			byte[] datas = null;
 			FileInputStream fis = null;
 			try {
 				fis = new FileInputStream(file);
 				int fileLength = fis.available();
 				int bufferSize = 0;
 				if (fileLength < 100) { // 100B下
 					bufferSize = 50;
 				} else if (fileLength >= 100 && fileLength < 10 * 1024) { // 100B
 																			// ~
 																			// 1MB
 					bufferSize = fileLength / 100;
 				} else { // >1MB
 					bufferSize = 1024;
 				}
 				byte[] buffer = new byte[bufferSize];
 				datas = loadInputStreamToBytes(fileLength, buffer, fis);
 				fis.close();
 			} catch (FileNotFoundException e) {
 				// 不可能出现FileNotFound情况
 			} catch (IOException e) {
 				if (fis != null) {
 					try {
 						fis.close();
 					} catch (IOException e1) {
 					}
 				}
 			}
 			return datas;
 		}
 
 		private void doAsyncTask(final Runnable run) {
 			AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
 				@Override
 				protected Void doInBackground(Void... params) {
 					run.run();
 					return null;
 				}
 			}.execute();
 		}
 	}
 
 	/**
 	 * 字节流写入文件
 	 * 
 	 * @param destFile
 	 * @param data
 	 */
 	private void writeDataToFile(File destFile, byte[] data) {
 		try {
 			FileOutputStream fos = new FileOutputStream(destFile);
 			fos.write(data, 0, data.length);
 			fos.flush();
 			fos.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * 判断是否抓取到流
 	 * 
 	 * @param datas
 	 * @return
 	 */
 	private boolean catchDataFlow(byte[] datas) {
 		return datas != null && datas.length > 0;
 	}
 
 	/**
 	 * 文件名使用16进制的字符形式表示
 	 * 
 	 * @param url
 	 * @return
 	 */
 	private String toHexName(String url) {
 		return Integer.toHexString((baseName + url).hashCode());
 	}
 
 	/**
 	 * 从内存缓存和本地缓存中抓取图片的字节流 抓取结果存入 \n
 	 * {@code ImageDownloadItem#mSdcardExist(sd卡是否存在)} \n
 	 * {@code ImageDownloadItem#mCatchData(是否抓取到缓存)} \n
 	 * {@code ImageDownloadItem#mAddMemCache(表示从网络获取的字节流是否需要写入LRUCache)} \n
 	 * 
 	 * @param item
 	 */
 	private void catchDataInCache(ImageDownloadItem item) {
 		item.mSdcardExist = true;
 		item.mCatchData = false;
 		item.mAddMemCache = false;
 		// 先获取图片内存缓存
 		if (item.mLoadMemCache) {
 			item.data = mCache.get(item.mUrl);
 			if (catchDataFlow(item.data)) {
 				item.mCatchData = true;
 				item.mAddMemCache = false;
 				item.mSdcardExist = true;
 				item.mLoadFileBytes = false;
 				item.mLoadNetBytes = false;
 				return;
 			}
 		}
 		// 内存中不存在缓存,查找文件,设置状态，最终在AsyncTask中读取文件流
 		item.mImageView.setImageBitmap(default_icon);
 		File file = new File(rootPath + toHexName(item.mUrl));
 		item.setDestnationFile(file);
 		if (file.exists()) {
 			item.mSdcardExist = true;
 			item.mLoadFileBytes = true;
 			item.mLoadNetBytes = false;
 			item.mAddMemCache = true;
 			item.mCatchData = false;
 		}
 		// 文件缓存不存在，设置状态，最终在AsyncTask中读取网络流
 		else {
 			item.mCatchData = false;
 			item.mLoadFileBytes = false;
 			item.mLoadNetBytes = true;
 			item.mAddMemCache = true;
 			try {
 				item.mSdcardExist = file.createNewFile();// 如果创建失败则说明sd卡不存在
 			} catch (IOException e) {
 				item.mSdcardExist = false; // io错误是表示SD卡不存在
 			}
 		}
 	}
 
 	/**
 	 * 设置字节流到图片中
 	 * 
 	 * @param view
 	 * @param datas
 	 */
 	private void setImgDataFlow(ImageView view, byte[] datas) {
 		view.setImageBitmap(BitmapFactory.decodeByteArray(datas, 0,
 				datas.length));
 	}
 }
