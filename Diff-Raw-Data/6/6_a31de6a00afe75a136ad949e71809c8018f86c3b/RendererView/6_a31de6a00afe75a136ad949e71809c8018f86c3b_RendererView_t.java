 /**
  * Copyright 2011 Rowan Seymour
  * 
  * This file is part of Refract.
  *
  * Refract is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Refract is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Refract. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.ijuru.refract.ui;
 
 import com.ijuru.refract.Complex;
 import com.ijuru.refract.Function;
 import com.ijuru.refract.Palette;
 import com.ijuru.refract.RendererThread;
 import com.ijuru.refract.renderer.NativeRenderer;
 import com.ijuru.refract.renderer.Renderer;
 import com.ijuru.refract.utils.Utils;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Bitmap.Config;
 import android.preference.PreferenceManager;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.ScaleGestureDetector;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.widget.Toast;
 
 /**
  * View which displays the fractal rendering
  */
 public class RendererView extends SurfaceView implements SurfaceHolder.Callback {
 	
 	private Bitmap bitmap;
 	private Renderer renderer;
 	private RendererThread rendererThread;
 	private RendererListener listener;
 	
 	// Rendering parameters
 	private int itersPerFrame;
 
 	// For panning and zooming
 	private GestureDetector panDetector;
 	private ScaleGestureDetector scaleDetector;
 	
 	public RendererView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		
 		getHolder().addCallback(this);
 		
 		panDetector = new GestureDetector(context, new PanListener());
 		scaleDetector = new ScaleGestureDetector(context, new ZoomListener());
 	}
 	
 	/**
 	 * Listener interface
 	 */
 	public interface RendererListener {
 		public void onOffsetChanged(RendererView view, Complex offset);
 		public void onZoomChanged(RendererView view, double zoom);
 		public void onUpdate(RendererView view, int iters);
 	}
 	
 	/**
 	 * @see android.view.SurfaceHolder.Callback#surfaceCreated(SurfaceHolder)
 	 */
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
 		renderer = new NativeRenderer();
 		
 		if (!renderer.allocate(getWidth(), getHeight())) {
 			Toast.makeText(getContext(), "Unable to allocate resources", Toast.LENGTH_LONG).show();
 			renderer = null;
 			return;
 		}
 		
 		// Get rendering parameters from preferences
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
 		Function iterFunc = Function.parseString(prefs.getString("iterfunc", "mandelbrot"));
 		itersPerFrame = Utils.parseInteger(prefs.getString("itersperframe", "5"));
 		Palette palette = Palette.getPresetByName(prefs.getString("palette", "sunset").toLowerCase());
 		
 		renderer.setFunction(iterFunc);
 		renderer.setPalette(palette, 128);
 		
 		// Start renderer thread
 		rendererThread = new RendererThread(this);
 		rendererThread.start();
 		
 		Log.d("refract", "Render surface created [" + getWidth() + ", " + getHeight() + "]");
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// Reallocate renderer and off-screen bitmap only if size has changed
		if (renderer != null && (renderer.getWidth() != width || renderer.getHeight() != height)) {
 			renderer.resize(width, height);
			bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
		}
 		
 		Log.d("refract", "Render surface resized [" + width + ", " + height + "]");
 	}
 	
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		
 		// Stop renderer thread
 		if (rendererThread != null) {
 			boolean retry = true;
 			rendererThread.interrupt();
 			
 			while (retry) {
 				try {
 					rendererThread.join();
 					retry = false;
 				} catch (InterruptedException e) {
 				}
 			}
 		}
 		
 		// Deallocate renderer
 		if (renderer != null) {
 			renderer.free();
 			renderer = null;
 		}
 		
 		Log.d("refract", "Render surface destroyed");
 	}
 	
 	/**
 	 * Updates the renderer
 	 */
 	public void update() {
 		int iters = renderer.iterate(itersPerFrame);
 		
 		// Render into off screen bitmap
 		renderer.render(bitmap);
 		
 		// Lock canvas to draw to it
 		Canvas c = null;
 		try {
 			c = getHolder().lockCanvas();
 			synchronized (getHolder()) {
 				onDraw(c);
 			}
 		} finally {
 			if (c != null) {
 				getHolder().unlockCanvasAndPost(c);
 			}
 		}
 		
 		if (listener != null)
 			listener.onUpdate(RendererView.this, iters);
 		
 		//Log.v("refract", "Render surface updated [" + rendererThread.getLastFrameTime() + "ms]");
 	}
 
 	/**
 	 * @see android.view.View#onDraw(Canvas)
 	 */
 	@Override
 	protected void onDraw(Canvas canvas) {
 		if (renderer != null)
 			canvas.drawBitmap(bitmap, 0, 0, null);
 	}
 
 	/**
 	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
 	 */
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		// Let the gesture detectors handle the event
 		panDetector.onTouchEvent(event);
 		scaleDetector.onTouchEvent(event);
 		
 		return true;
 	}
 	
 	/**
 	 * Listener for drag-pan gestures
 	 */
 	private class PanListener extends GestureDetector.SimpleOnGestureListener {
 		
 		/**
 		 * @see GestureDetector.SimpleOnGestureListener#onScroll(MotionEvent, MotionEvent, float, float)
 		 */
 		@Override
 		public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
 			if (renderer == null)
 				return false;
 			
 			double zoom = renderer.getZoom();
 			Complex offset = renderer.getOffset();
 			Complex delta = new Complex(distanceX / zoom, -distanceY / zoom);
 			renderer.setOffset(offset.add(delta));
 			
 			// Update listener
 			if (listener != null)
 				listener.onOffsetChanged(RendererView.this, offset);
 		
 			return true;
 		}
 	}
 	
 	/**
 	 * Listener for pinch-zoom gestures
 	 */
 	private class ZoomListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
 		/**
 		 * @see ScaleGestureDetector.SimpleOnScaleGestureListener#onScale(ScaleGestureDetector)
 		 */
 		@Override
 		public boolean onScale(ScaleGestureDetector detector) {
 			if (renderer == null)
 				return false;
 			
 			double newZoom = renderer.getZoom() * detector.getScaleFactor();
 			renderer.setZoom(newZoom);
 			
 			// Update listener
 			if (listener != null)
 				listener.onZoomChanged(RendererView.this, newZoom);
 			
 			return true;
 		}
 	}
 	
 	/**
 	 * Resets this view so that it is centered on the origin
 	 */
 	public void reset() {
 		if (renderer != null) {
 			double zoom = renderer.getWidth() / 2;
 			renderer.setOffset(Complex.ORIGIN);
 			renderer.setZoom(zoom);
 			
 			// Update listener
 			if (listener != null) {
 				listener.onOffsetChanged(RendererView.this, Complex.ORIGIN);
 				listener.onZoomChanged(RendererView.this, zoom);
 			}
 		}
 	}
 	
 	/**
 	 * Sets the renderer listener
 	 * @param listener the listener
 	 */
 	public void setRendererListener(RendererListener listener) {
 		this.listener = listener;
 	}
 
 	/**
 	 * Gets the renderer thread
 	 * @return the renderer thread
 	 */
 	public RendererThread getRendererThread() {
 		return rendererThread;
 	}
 }
