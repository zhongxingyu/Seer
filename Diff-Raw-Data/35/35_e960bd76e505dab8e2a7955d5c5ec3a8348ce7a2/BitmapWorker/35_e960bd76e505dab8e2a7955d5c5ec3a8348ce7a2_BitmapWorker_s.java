 package net.analogyc.wordiary.models;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.util.LruCache;
 import android.widget.ImageView;
 
 import java.lang.ref.WeakReference;
 
 public class BitmapWorker extends Fragment {
 
 	private static final String TAG = "BitmapWorker";
 	private LruCache<String, Bitmap> mMemoryCache;
 
 	protected BitmapWorker() {
 
 		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
 		final int cacheSize = maxMemory / 4;
 
 		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
 			@Override
 			protected int sizeOf(String key, Bitmap bitmap) {
 				// The cache size will be measured in kilobytes rather than
 				// number of items.
 				return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
 			}
 		};
 	}
 
 	public static BitmapWorker findOrCreateBitmapWorker(FragmentManager fm) {
 		BitmapWorker fragment = (BitmapWorker) fm.findFragmentByTag(TAG);
 		if (fragment == null) {
 			FragmentTransaction ft = fm.beginTransaction();
 			fragment = new BitmapWorker();
 			ft.add(fragment, TAG);
 			ft.commit();
 		}
 
 		return fragment;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setRetainInstance(true);
 	}
 
 	public BitmapWorkerTaskBuilder newTask(ImageView imageView, String path) {
 		return new BitmapWorkerTaskBuilder(imageView, path);
 	}
 
 	protected void addBitmapToMemoryCache(String key, Bitmap bitmap) {
 		if (getBitmapFromMemCache(key) == null) {
 			mMemoryCache.put(key, bitmap);
 		}
 	}
 
 	protected Bitmap getBitmapFromMemCache(String key) {
 		return mMemoryCache.get(key);
 	}
 
 	public void clearBitmapFromMemCache(String path) {
 		mMemoryCache.remove("models.EntryAdapter.thumbnails." + path);
 	}
 
 	public BitmapWorkerTaskBuilder createTask(ImageView imageView, String path) {
 		return new BitmapWorkerTaskBuilder(imageView, path);
 	}
 
	protected class BitmapWorkerTaskBuilder {
 
 		protected ImageView imageView;
 		protected String path;
 		protected int targetWidth = 0;
 		protected int targetHeight = 0;
 		protected boolean centerCrop = false;
 		protected boolean highQuality = true;
 
 		public int getTargetWidth() {
 			return targetWidth;
 		}
 
 		public BitmapWorkerTaskBuilder setTargetWidth(int targetWidth) {
 			this.targetWidth = targetWidth;
 			return this;
 		}
 
 		public int getTargetHeight() {
 			return targetHeight;
 		}
 
 		public BitmapWorkerTaskBuilder setTargetHeight(int targetHeight) {
 			this.targetHeight = targetHeight;
 			return this;
 		}
 
 		public boolean isCenterCrop() {
 			return centerCrop;
 		}
 
 		public BitmapWorkerTaskBuilder setCenterCrop(boolean centerCrop) {
 			this.centerCrop = centerCrop;
 			return this;
 		}
 
 		public boolean isHighQuality() {
 			return highQuality;
 		}
 
 		public BitmapWorkerTaskBuilder setHighQuality(boolean highQuality) {
 			this.highQuality = highQuality;
 			return this;
 		}
 
 		public BitmapWorkerTaskBuilder(ImageView imageView, String path) {
 			this.imageView = imageView;
 			this.path = path;
 		}
 
 		public BitmapWorkerTask execute() {
 			BitmapWorkerTask task = new BitmapWorkerTask(imageView, path, targetWidth, targetHeight,
 				centerCrop, highQuality);
 			task.execute();
 			return task;
 		}
 	}
 
 	protected class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
 		private final WeakReference<ImageView> imageViewReference;
 		private final String path;
 		private final int targetWidth;
 		private final int targetHeight;
 		private final boolean centerCrop;
 		private final boolean highQuality;
 
 		public BitmapWorkerTask(ImageView imageView, String path, int targetWidth,
 								int targetHeight, boolean centerCrop, boolean highQuality) {
 			imageViewReference = new WeakReference<ImageView>(imageView);
 			this.path = path;
 			this.targetWidth = targetWidth;
 			this.targetHeight = targetHeight;
 			this.centerCrop = centerCrop;
 			this.highQuality = highQuality;
 		}
 
 		// Resize image in background.
 		@Override
 		protected Bitmap doInBackground(Integer... params) {
 			Bitmap image = getBitmapFromMemCache("models.EntryAdapter.thumbnails." + path);
 
 			if (image != null) {
 				return image;
 			}
 
 			Bitmap bmp;
 
 			// just use lower inSampleSize
 			if (targetWidth != 0 && !highQuality) {
 				// get the image width and height without loading it in memory
 				BitmapFactory.Options options = new BitmapFactory.Options();
 				options.inJustDecodeBounds = true;
 				bmp = BitmapFactory.decodeFile(path, options);
 				int height = options.outHeight;
 				int width = options.outWidth;
 
 				// reduce the amount of data allocated in memory with higher inSampleSize
 				options = new BitmapFactory.Options();
 				options.inSampleSize = 1;
 				if (height > targetHeight || width > targetWidth) {
 					final int heightRatio = Math.round((float) height / (float) targetHeight);
 					final int widthRatio = Math.round((float) width / (float) targetWidth);
 					options.inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
 				}
 
 				bmp = BitmapFactory.decodeFile(path, options);
 			} else {
 				bmp = BitmapFactory.decodeFile(path);
 			}
 
 			if (centerCrop) {
 				// center crop so it's square and pretty
 				// credits to http://stackoverflow.com/a/6909144/644504
 				// for the solution to "center crop" resize
 				if (bmp.getWidth() >= bmp.getHeight()) {
 					bmp = Bitmap.createBitmap(
 						bmp,
 						bmp.getWidth() / 2 - bmp.getHeight() / 2,
 						0,
 						bmp.getHeight(),
 						bmp.getHeight()
 					);
 				} else {
 					bmp = Bitmap.createBitmap(
 						bmp,
 						0,
 						bmp.getHeight() / 2 - bmp.getWidth() / 2,
 						bmp.getWidth(),
 						bmp.getWidth()
 					);
 				}
 			}
 
 			if (targetWidth != 0 && highQuality) {
 				bmp = Bitmap.createScaledBitmap(bmp, targetWidth, targetHeight, true);
 			}
 
 			return bmp;
 		}
 
 		@Override
 		protected void onPostExecute(Bitmap bitmap) {
 			if (imageViewReference != null && bitmap != null) {
 				addBitmapToMemoryCache("models.EntryAdapter.thumbnails." + path, bitmap);
 				final ImageView imageView = imageViewReference.get();
 				if (imageView != null) {
 					imageView.setImageBitmap(bitmap);
 				}
 			}
 		}
 	}
 }
