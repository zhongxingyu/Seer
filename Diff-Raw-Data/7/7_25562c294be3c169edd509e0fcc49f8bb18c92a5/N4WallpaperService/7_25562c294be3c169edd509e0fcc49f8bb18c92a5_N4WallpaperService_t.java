 /*
 Copyright (c) 2012, Patrick O'Leary
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 
 
 1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution. 
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.greentaperacing.olearyp.n4lwp;
 
 import java.util.Random;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.PorterDuff;
 import android.graphics.PorterDuffXfermode;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.opengl.Matrix;
 import android.preference.PreferenceManager;
 import android.service.wallpaper.WallpaperService;
 import android.util.FloatMath;
 import android.view.Display;
 import android.view.SurfaceHolder;
 import android.view.WindowManager;
 
 public class N4WallpaperService extends WallpaperService {
 
 	@Override
 	public Engine onCreateEngine() {
 		return new N4WallpaperEngine();
 	}
 
 	private class N4WallpaperEngine extends Engine implements SensorEventListener {
 
 		private final SensorManager mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
 		private final Sensor mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
 		private final Sensor mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
 		private final float illumMax = 700f;
 		private float[] rotation = {0.0f, 0.0f, 0.0f};
 		private float illum = illumMax;
 
 		private final Random rng = new Random(0);
 
 		private final float[] dotAngles = new float[20];
 		private final Bitmap[] dots = new Bitmap[dotAngles.length];
 
 		private SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(N4WallpaperService.this);
 
 		@Override
 		public void onCreate(SurfaceHolder surfaceHolder) {
 			super.onCreate(surfaceHolder);
 			for(int ii = 0; ii < dotAngles.length; ii++) {
 				dotAngles[ii] = (float) (ii * Math.PI / dotAngles.length);
 			}
 			generateDots();
 			prefs.registerOnSharedPreferenceChangeListener(prefListener);
 		}
 
 
 		private SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
 
 			@Override
 			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 					String key) {
 				generateDots();
 
 			}
 		};
 
 		@Override
 		public void onVisibilityChanged(boolean visible) {
 			if (visible) {
 				mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_NORMAL);
 				if(mLight != null) {
 					mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
 				}
 			} else {
 				mSensorManager.unregisterListener(this);
 			}
 		}
 
 		@Override
 		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 			draw();
 		}
 
 		@Override
 		public void onSurfaceDestroyed(SurfaceHolder holder) {
 			super.onSurfaceDestroyed(holder);
 		}
 
 		@Override
 		public void onDestroy() {
 			super.onDestroy();
 			mSensorManager.unregisterListener(this);
 		}
 
 		private void draw() {
 			SurfaceHolder holder = getSurfaceHolder();
 			Canvas c = null;
 
 			try {
 				c = holder.lockCanvas();
 				if (c != null) {
 					float[] R_wd = new float[16];
 					float[] R_dw = new float[16];
 					SensorManager.getRotationMatrixFromVector(R_wd, rotation);
 					Matrix.transposeM(R_dw, 0, R_wd, 0);
 
 					// Compute vector normal to screen in world coordinates
 					final float[] n_d = {0.0f, 0.0f, 1.0f, 1.0f};
 					float[] n_w = new float[4];
 					Matrix.multiplyMV(n_w, 0, R_dw, 0, n_d, 0);
 
 					// cos(theta) = n_w[2]
 					double theta = Math.acos(n_w[2]);
 
 					// Compute screen up vector in world coordinates
 					final float[] u_d = {0.0f, 1.0f, 0.0f, 1.0f};
 					float[] u_w = new float[4];
 					Matrix.multiplyMV(u_w, 0, R_dw, 0, u_d, 0);
 
 					// Compute projection of world up vector onto screen, expressed in world
 					float[] proj_wu_d_w = {-n_w[2]*n_w[0], -n_w[2]*n_w[1], 1.0f-n_w[2]*n_w[2], 1.0f};
 
 					// cos(psi) = dot(u_w, proj_wu_w)/norm(proj_wu_w)
 					double psi = Math.acos((u_w[0]*proj_wu_d_w[0] + u_w[1]*proj_wu_d_w[1] + u_w[2]*proj_wu_d_w[2])/norm(proj_wu_d_w));
 
 					// We need to give psi a sign, which we can do by checking the projection in display coordinates
 					float[] proj_wu_d_d = new float[4];
 					Matrix.multiplyMV(proj_wu_d_d, 0, R_wd, 0, proj_wu_d_w, 0);
 					if(proj_wu_d_d[0] < 0) {
 						psi = -psi;
 					}
 
 					float illum_adj = 1.0f;
 					if(prefs.getBoolean("luminance_enabled", true) && illum < illumMax) {
 						illum_adj = (illum + 100)/(illumMax + 100);
 					}
 
 					int[] intensity = new int[dotAngles.length];
 					for(int ii = 0; ii < intensity.length; ii++) {
 						intensity[ii] = (int) Math.round(250*Math.abs(Math.sin(theta*2.0) * Math.sin(psi - dotAngles[ii])) * illum_adj + 5);
 					}
 
 					c.drawColor(prefs.getInt("color_bg", Color.BLACK));
 
 					Paint p = new Paint();
 
 					int hMax = c.getHeight();
 					int wMax = c.getWidth();
 
 					final Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
 					@SuppressWarnings("deprecation")
 					final int minScreenDim = Math.min(display.getWidth(), display.getHeight());
 					final int gridSize = minScreenDim/Integer.valueOf(prefs.getString("dot_num_across", "40"));
 					rng.setSeed(0);
 					for(int ii = 0; ii*gridSize < wMax; ii++) {
 						for(int jj = 0; jj*gridSize < hMax; jj++) {
 							int dot = rng.nextInt(dots.length);
 							p.setAlpha((int) intensity[dot]);
 							c.drawBitmap(dots[dot], ii*gridSize, jj*gridSize, p);
 						}
 					}
 				}
 			} finally {
				// If we get IllegalArgumentException, the Surface was destroyed and there's nothing to unlock
				try {
					if (c != null)
						holder.unlockCanvasAndPost(c);
				} catch(IllegalArgumentException e) {}
 			}
 		}
 
 		@Override
 		public void onAccuracyChanged(Sensor sensor, int accuracy) {
 			// pass
 		}
 
 		@Override
 		public void onSensorChanged(SensorEvent event) {
 			if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
 				rotation  = event.values;
 				draw();
 			} else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
 				illum = event.values[0];
 			}
 		}
 
 		private float norm(float[] vector) {
 			return FloatMath.sqrt(vector[0] * vector[0] + vector[1] * vector[1]
 					+ vector[2] * vector[2]);
 		}
 
 		private void generateDots() {
 			for(int ii = 0; ii < dotAngles.length; ii++) {
 				dots[ii] = makeDot(dotAngles[ii]);
 			}
 		}
 
 		private Bitmap makeDot(float angle) {
 			final int color_fg = prefs.getInt("color_fg", 0xFFFFFF);
 
 			final Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
 			@SuppressWarnings("deprecation")
 			final int minScreenDim = Math.min(display.getWidth(), display.getHeight());
 			final int gridSize = minScreenDim/Integer.valueOf(prefs.getString("dot_num_across", "40"));
 			final float dotSize = ((float) gridSize)*(Float.valueOf(prefs.getString("dot_fill_pct", "80")))/100.0f;
 
 			Bitmap b = Bitmap.createBitmap(gridSize, gridSize, Bitmap.Config.ARGB_8888);
 			Canvas c = new Canvas(b);
 			Paint p = new Paint();
 			p.setColor(color_fg);
 			p.setAntiAlias(true);
 
 			c.drawCircle(gridSize/2.0f, gridSize/2.0f, dotSize/2.0f, p);
 
 			p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
 			p.setStrokeWidth(dotSize/8);
 
 			android.graphics.Matrix R_if = new android.graphics.Matrix();
 			R_if.setRotate(angle*180f/(float) Math.PI, gridSize/2.0f, gridSize/2.0f);
 			float[] lineEnds = {
 					gridSize/2.0f, 0,
 					gridSize/2.0f, gridSize,
 					gridSize/2.0f-dotSize/4.0f, 0,
 					gridSize/2.0f-dotSize/4.0f, gridSize,
 					gridSize/2.0f+dotSize/4.0f, 0,
 					gridSize/2.0f+dotSize/4.0f, gridSize,
 			};
 			R_if.mapPoints(lineEnds);
 			c.drawLines(lineEnds, p);
 
 			return b;
 		}
 	}
 
 }
