 package com.luzi82.randomwallpaper;
 
 import java.nio.ByteBuffer;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.service.wallpaper.WallpaperService;
 import android.view.SurfaceHolder;
 
 public class LiveWallpaper extends WallpaperService {
 
 	// private static final String LOG_TAG = "LiveWallpaper";
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 	}
 
 	@Override
 	public void onDestroy() {
 		clean();
 		super.onDestroy();
 	}
 
 	@Override
 	public Engine onCreateEngine() {
 		// Create the live wallpaper engine
 		return new LiveWallpaperEngine();
 	}
 
 	static final Matrix mi = new Matrix();
 	static final Paint paint = new Paint();
 
 	static int oldWidth = -1;
 	static int oldHeight = -1;
 
 	static byte[] byteAry;
 	static ByteBuffer byteBuffer;
 	static Bitmap bitmap;
 
 	// static int size = -1;
 
 	static void clean() {
 		// Log.d(LOG_TAG, "static synchronized void clean()");
 		synchronized (mi) {
 			// size = -1;
 			oldWidth = -1;
 			oldHeight = -1;
 			byteAry = null;
 			byteBuffer = null;
 			if (bitmap != null)
 				bitmap.recycle();
 			bitmap = null;
 			// cleanBuf();
 		}
 	}
 
 	static void drawCanvas(Canvas c) {
 		// Log.d(LOG_TAG, "static void drawCanvas(Canvas c) start");
 		synchronized (mi) {
 			int nowWidth = c.getWidth();
 			int nowHeight = c.getHeight();
			int s = (nowWidth * nowHeight) << 2;
 			if ((oldHeight != nowHeight) || (oldWidth != nowWidth)) {
 				if (bitmap != null)
 					bitmap.recycle();
 				bitmap = Bitmap.createBitmap(nowWidth, nowHeight,
 						Bitmap.Config.ARGB_8888);
 				oldHeight = nowHeight;
 				oldWidth = nowWidth;
 			}
 			if (byteAry == null || byteAry.length != s) {
				byteAry = new byte[s];
 				byteBuffer = ByteBuffer.wrap(byteAry);
 				// setSize(s);
 				// size = s;
 			}
 			genRandom(byteAry);
 			bitmap.copyPixelsFromBuffer(byteBuffer);
 			c.drawBitmap(bitmap, mi, paint);
 		}
 		// Log.d(LOG_TAG, "static void drawCanvas(Canvas c) end");
 	}
 
 	// static long time = System.currentTimeMillis();
 
 	// static {
 	// paint.setColor(Color.RED);
 	// paint.setTextSize(20);
 	// }
 
 	class LiveWallpaperEngine extends Engine {
 
 		Timer timer;
 
 		@Override
 		public void onCreate(SurfaceHolder holder) {
 			super.onCreate(holder);
 		}
 
 		@Override
 		public void onDestroy() {
 			synchronized (mi) {
 				clearTimer();
 				clean();
 			}
 			super.onDestroy();
 		}
 
 		// Become false when switching to an app or put phone to sleep
 		@Override
 		public void onVisibilityChanged(boolean visible) {
 			super.onVisibilityChanged(visible);
 			synchronized (mi) {
 				// Log.d(LOG_TAG, "onVisibilityChanged=" + visible);
 				if (visible) {
 					createTimer(this);
 				} else {
 					clearTimer();
 					clean();
 				}
 			}
 
 		}
 
 		@Override
 		public void onSurfaceChanged(SurfaceHolder holder, int format,
 				int width, int height) {
 			super.onSurfaceChanged(holder, format, width, height);
 			// Log.d(LOG_TAG, "onSurfaceChanged");
 		}
 
 		@Override
 		public void onSurfaceCreated(SurfaceHolder holder) {
 			super.onSurfaceCreated(holder);
 			// Log.d(LOG_TAG, "onSurfaceCreated");
 		}
 
 		@Override
 		public void onSurfaceDestroyed(SurfaceHolder holder) {
 			super.onSurfaceDestroyed(holder);
 			// Log.d(LOG_TAG, "onSurfaceDestroyed");
 		}
 
 		private void updateCanvas() {
 			// Log.d(LOG_TAG, "updateCanvas start");
 			// long now = System.currentTimeMillis();
 			// int diff = (int) (now - time);
 			// time = now;
 			synchronized (mi) {
 				SurfaceHolder holder = getSurfaceHolder();
 				if (holder != null) {
 					Canvas c = null;
 					try {
 						c = holder.lockCanvas();
 						if (c != null) {
 							drawCanvas(c);
 						}
 					} finally {
 						if (c != null)
 							holder.unlockCanvasAndPost(c);
 					}
 				}
 			}
 			// Log.d(LOG_TAG, "updateCanvas end");
 		}
 
 		void createTimer(final LiveWallpaperEngine engine) {
 			// Log
 			// .d(LOG_TAG,
 			// "static void createTimer(final LiveWallpaperEngine engine) start");
 			synchronized (mi) {
 				if (timer == null) {
 					// Log.d(LOG_TAG, "create timer");
 					timer = new Timer();
 					timer.scheduleAtFixedRate(new TimerTask() {
 						@Override
 						public void run() {
 							engine.updateCanvas();
 						}
 					}, 100, 100);
 				}
 			}
 			// Log
 			// .d(LOG_TAG,
 			// "static void createTimer(final LiveWallpaperEngine engine) end");
 		}
 
 		void clearTimer() {
 			synchronized (mi) {
 				if (timer != null) {
 					timer.cancel();
 					timer = null;
 				}
 			}
 		}
 	}
 
 	static {
 		System.loadLibrary("randompixel");
 		setSeed(System.currentTimeMillis());
 	}
 
 	public static native void setSeed(long seed);
 
 	public static native void genRandom(byte[] out);
 
 }
