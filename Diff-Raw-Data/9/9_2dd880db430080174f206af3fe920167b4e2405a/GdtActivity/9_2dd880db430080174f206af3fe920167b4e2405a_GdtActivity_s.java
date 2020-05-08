 /*
  * GdtActivity.java
  *
  * Copyright (c) 2011 Rickard Edström
  * Copyright (c) 2011 Sebastian Ärleryd
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package gdt;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 import java.util.List;

 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.AssetFileDescriptor;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.media.MediaPlayer;
 import android.net.Uri;
import android.opengl.GLSurfaceView; 
 import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
 
 public abstract class GdtActivity extends Activity {
 	private GdtView _view;
 	
 	@Override
 	protected void onCreate(final Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);		 
 		
 		requestWindowFeature(Window.FEATURE_NO_TITLE); 
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		 
 		_view = new GdtView(this);
 		setContentView(_view); 
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onPause();
 		_view.doResume();
 	}
 	@Override
 	protected void onPause() {
 		super.onPause();
 		_view.doPause();
 	}
 
 	@Override
 	protected void onRestart() {
 		Native.resumeEvents();
 		super.onRestart();
 	}
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
 		_view.onResume();
 		_view.doStart();
 	}
 	@Override
 	protected void onStop() {
 		super.onStop();
 		_view.onPause();
 		_view.doStop();
 		Native.suspendEvents();		
 	}
 } 
  
 final class GdtView extends GLSurfaceView { 
 	private final Object lock = new Object();
 	private int _w = -1;
 	private int _h = -1;
 	private boolean _newGL;
 	private boolean _hasDelayedActive = false;
 	
 	public void doResume() {
 		synchronized(lock) {
 			if (_w == -1)
 				_hasDelayedActive = true;
 			else
 				activate();
 		} 
 	}
 	public void doPause() {
 		synchronized(lock) { Native.inactive(); }		
 	}
 	
 	public void doStart() {
 		synchronized(lock) {
 			if (_w != -1)
 				show();
 		}		
 	}
 	public void doStop() {
 		synchronized(lock) { Native.hidden(); }
 	}	
 	
 	public GdtView(final Context ctx) {
 		super(ctx);
 		synchronized(lock) { Native.init(ctx); }
 		setEGLContextClientVersion(2);
 		setRenderer(new Renderer() {
 			public void onSurfaceCreated(GL10 _, EGLConfig __) {
 				synchronized(lock) { _newGL = true; }
 			}
 			public void onSurfaceChanged(GL10 _, int width, int height) {
 				synchronized(lock) {
 					if(_w != width && _h != height) {
 						_w = width;
 						_h = height;
 						show();
 					} 
 				}
 			} 
 			public void onDrawFrame(GL10 _) { 
 				synchronized(lock) { Native.render(); }
 			} 
 		});
 	} 
 
 
 	public boolean onTouchEvent(final MotionEvent ev) {
 		synchronized(lock) { 
 			Native.eventTouch(ev.getAction(), ev.getX(), ev.getY());
 		}
 		return true;
 	}
 
 	private void show() {
 		Native.visible(_newGL, _w, _h);		
 		_newGL = false;
 		if (_hasDelayedActive) 
 			activate();
 	}		
 	
 	private void activate() {
 		Native.active();
 	_hasDelayedActive = false;		
 	}
 }
 
 final class Native {
 	private static Context _ctx;
 
 	static {
 		System.loadLibrary("native");
 	}
 	
 	private Native() { } 
 	
 	static SensorEventListener _accelerometerListener = new SensorEventListener() {
 	@Override
 	public void onSensorChanged(SensorEvent e) {
 		eventAccelerometer(e.timestamp/(double)1e9, e.values[0], e.values[1], e.values[2]);
 	}
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 	}
 	};
 	
 	
 	static native void initialize(String cacheDir, String storageDir);
 	static native void render();
 	static native void hidden();
 	static native void active(); 
 	static native void inactive(); 
 	static native void eventTouch(int what, float x, float y);
 	static native void eventAccelerometer(double t, float x, float y, float z);
 	static native void visible(boolean newSurface, int width, int height);	
 	
 	static InputMethodManager _inputMethodManager;
 	static SensorManager _sensorManager;
 	static Sensor _accelerometer;
 	static boolean _subscribedAccelerometer = false;
 	
 	static void init(Context ctx) {
 		_ctx = ctx;
 		_inputMethodManager = (InputMethodManager) _ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
 		_sensorManager = (SensorManager)_ctx.getSystemService(Context.SENSOR_SERVICE);
 		initialize(_ctx.getCacheDir().getPath(), _ctx.getFilesDir().getPath());
 	}
 	
 	static Object[] openAsset(final String fileName) {
 		final Object[] a = new Object[2];
 		try { 
 			final AssetFileDescriptor fd = _ctx.getAssets().openFd(fileName);
 			final FileInputStream stream = fd.createInputStream();
 			final FileChannel channel = stream.getChannel();
 			a[0] = channel.map(FileChannel.MapMode.READ_ONLY, channel.position(), fd.getLength());// the bytebuffer
 			a[1] = channel; // this will be closed when resource is unloaded
 			return a;		 
 		} catch (final IOException e) {
 			return null;
 		} 
 	}	 
 	
 	static boolean cleanAsset(final Object arr) { 
 		try {
 			Object[] objArr = (Object[])arr;
 			((FileChannel)(objArr[1])).close();
 		} catch (final Throwable t) {
 			return false;
 		}
 		return true;
 	}
 	
 	static Object playerCreate(final String fileName) {
 		MediaPlayer p = new MediaPlayer();
 		
 		try {
 			AssetFileDescriptor fd = null;
 			try {
 				fd = _ctx.getAssets().openFd(fileName);
 				p.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());				
 			} finally {
 				if (fd != null) fd.close();
 			}
 			
 			p.prepare();
 		} catch (Exception ex) {
 			return null;
 		}
 		
 		return p;
 	}
 	
 	static void playerDestroy(final MediaPlayer player) {
 		if (player != null) player.release();
 	}
 	
 	static boolean playerPlay(final MediaPlayer player) {
 		try { 
 			player.start();
 		} catch (Exception ex) {
 			return false;
 		}
 		return true;
 	}
 	
 	static void openUrl(final String url) {
 		Intent intent = new Intent(Intent.ACTION_VIEW);
 		intent.setData(Uri.parse(url));
 		_ctx.startActivity(intent);
 	}
 	static void setKbdMode(int mode) {
 		if (mode == 0)
 			_inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
 	else
 			_inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
 	}
 	
 	static void subscribeAccelerometer(boolean subscribe) {
 		if (subscribe)
 			_sensorManager.registerListener(_accelerometerListener, _accelerometer, SensorManager.SENSOR_DELAY_GAME);
 		else
 			_sensorManager.unregisterListener(_accelerometerListener);
 	}
 	
 	static void gcCollect() {
 		System.gc();
 	}
 	
 	static void eventSubscribe(int eventId, boolean subscribe) {
 		if (eventId == 0) { // accelerometer
 			if (_accelerometer == null) {
 				List<Sensor> xs = _sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
 				if (xs.isEmpty())
 					return;
 				_accelerometer = xs.get(0);
 			}
 			subscribeAccelerometer(subscribe);
 			_subscribedAccelerometer = subscribe;	 
 		}
 	}
 	
 	public static void suspendEvents() {
 		if (_subscribedAccelerometer)
 			subscribeAccelerometer(false);
 	}
 	
 	public static void resumeEvents() {
 		if (_subscribedAccelerometer)
 			subscribeAccelerometer(true);
 	}	
 }
