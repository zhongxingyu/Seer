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
 
 import com.ijuru.refract.R;
 import com.ijuru.refract.renderer.Complex;
 import com.ijuru.refract.renderer.Function;
 import com.ijuru.refract.renderer.Mapping;
 import com.ijuru.refract.renderer.Renderer;
 import com.ijuru.refract.renderer.RendererFactory;
 import com.ijuru.refract.renderer.RendererListener;
 import com.ijuru.refract.renderer.RendererParams;
 import com.ijuru.refract.utils.Utils;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Bitmap.Config;
 import android.graphics.PointF;
 import android.graphics.RectF;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 /**
  * View which displays a rendering
  */
 public class RendererView extends SurfaceView implements SurfaceHolder.Callback, MultiTouchGestureDetector.OnMultiTouchGestureListener {
 	
 	private Bitmap bitmap;
 	private Renderer renderer;
 	private RendererThread rendererThread;
 	private RendererListener listener;
 
 	// Rendering parameters
 	private RendererParams params = new RendererParams(Function.MANDELBROT, Complex.ORIGIN, 200);
 	private int itersPerFrame = 5;
 	private Mapping paletteMapping;
 
 	// For panning and zooming
 	private boolean navigationEnabled;
 	private MultiTouchGestureDetector navigationDetector;
 	private RendererParams bitmapParams;
 	
 	/**
 	 * Constructs a renderer view whose renderer scales it's internal storage with the view
 	 * @param context the context
 	 * @param attrs the attributes from the layout resource
 	 */
 	public RendererView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		
 		getHolder().addCallback(this);
 		
 		// Get custom attribute values
 		TypedArray arrAttrs = getContext().obtainStyledAttributes(attrs, R.styleable.RendererView);
 		navigationEnabled = arrAttrs.getBoolean(R.styleable.RendererView_navigationEnabled, true);
 		arrAttrs.recycle();
 		
 		// Optionally enable touch based navigation
 		if (navigationEnabled)
 			navigationDetector = new MultiTouchGestureDetector(this);
 	}
 	
 	/**
 	 * @see android.view.SurfaceHolder.Callback#surfaceCreated(SurfaceHolder)
 	 */
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {		
 		int rendererWidth = getDesiredRendererWidth(getWidth());
 		int rendererHeight = getDesiredRendererHeight(getHeight());
 		
 		params.setZoom(getWidth() / 2);
 		bitmap = Bitmap.createBitmap(rendererWidth, rendererHeight, Config.ARGB_8888);
 		renderer = RendererFactory.createRenderer();
 		
 		// Allocate resources for renderer
 		if (!renderer.allocate(rendererWidth, rendererHeight)) {
 			if (listener != null)
 				listener.onRendererAllocationFailed(this, renderer);
 			return;
 		}
 		
 		if (listener != null)
 			listener.onRendererCreated(this, renderer);
 		
 		// Start renderer thread
 		rendererThread = new RendererThread(this);
 		rendererThread.start();
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {	
 		int rendererWidth = getDesiredRendererWidth(width);
 		int rendererHeight = getDesiredRendererHeight(height);
 		
 		// Reallocate renderer and off-screen bitmap only if size has changed 
 		if (renderer.getWidth() != rendererWidth || renderer.getHeight() != rendererHeight) {
 			renderer.resize(width, height);
 			bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
 		}
 	}
 	
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {	
 		// Stop renderer thread
 		stopRendering();
 		
 		// Notify listener that renderer is about to be destroyed
 		if (listener != null)
 			listener.onRendererDestroy(this, renderer);
 		
 		// Deallocate renderer
 		renderer.free();
 	}
 	
 	/**
 	 * Updates the renderer
 	 */
 	public void update() {	
 		// Only iterate if we're not panning/zooming
 		if (navigationDetector == null || !navigationDetector.isInProgress()) {
 			
 			// Copy params so that we know exactly what params have been iterated
 			bitmapParams = (RendererParams)params.clone();
 			int iters = renderer.iterate(bitmapParams.getFunction(), bitmapParams.getOffset(), bitmapParams.getZoom(), itersPerFrame);
 			
 			if (listener != null)
 				listener.onRendererIterated(this, renderer, iters);
 			
 			// Render into off screen bitmap
 			renderer.render(bitmap, paletteMapping);
 		}
 		
 		// Lock canvas to draw to it
 		Canvas canvas = null;
 		try {
 			canvas = getHolder().lockCanvas();
 			synchronized (getHolder()) {
				if (canvas != null)
					onDraw(canvas);
 			}
 		} finally {
			if (canvas != null)
 				getHolder().unlockCanvasAndPost(canvas);
 		}
 	}
 
 	/**
 	 * @see android.view.View#onDraw(Canvas)
 	 */
 	@Override
 	protected void onDraw(Canvas canvas) {		
 		// Are the params rendered in the bitmap the same as the renderer's params?
 		if (params.equals(bitmapParams)) {
 			canvas.drawBitmap(bitmap, 0, 0, null);
 		}
 		else {
 			// Calculate the complex space covered by the bitmap
 			Complex bitmap_c1 = pixelsToComplex(bitmapParams, new PointF(0, 0));
 			Complex bitmap_c2 = pixelsToComplex(bitmapParams, new PointF(bitmap.getWidth(), bitmap.getHeight()));
 			
 			// Map those complex points back into pixel space according to the current renderer params
 			PointF bitmap_p1 = complexToPixels(params, bitmap_c1);
 			PointF bitmap_p2 = complexToPixels(params, bitmap_c2);
 			
 			// Draw pre-navigation bitmap where render would be
 			canvas.drawARGB(255, 0, 0, 0);
 			canvas.drawBitmap(bitmap, null, new RectF(bitmap_p1.x, bitmap_p1.y, bitmap_p2.x, bitmap_p2.y), null);
 		}
 	}
 	
 	/**
 	 * Stops the rendering thread and doesn't return until it does
 	 */
 	public void stopRendering() {
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
 	}
 
 	/**
 	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
 	 */
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if (navigationEnabled)
 			// Let the gesture detector handle the event
 			return navigationDetector.onTouchEvent(event);
 		
 		return false;
 	}
 		
 	/**
 	 * @see com.ijuru.refract.ui.MultiTouchGestureDetector.OnMultiTouchGestureListener#onMultiTouchGesture(PointF[], PointF[])
 	 */
 	@Override
 	public void onMultiTouchGesture(PointF[] prevPoints, PointF[] currPoints) {
 		if (prevPoints.length == 1)
 			panGesture(prevPoints[0], currPoints[0]);
 		else if (prevPoints.length == 2)
 			zoomGesture(prevPoints[0], currPoints[0], prevPoints[1], currPoints[1]);
 		
 		if (listener != null)
 			listener.onRendererParamsChanged(this, renderer);
 	}
 	
 	/**
 	 * Handles single finger pan gesture
 	 * @param startPoint the start point
 	 * @param endPoint the end point
 	 */
 	private void panGesture(PointF startPoint, PointF endPoint) {
 		Complex startC = pixelsToComplex(params, startPoint);
 		Complex endC = pixelsToComplex(params, endPoint);
 		Complex diff = endC.sub(startC);
 		params.setOffset(params.getOffset().sub(diff));
 	}
 	
 	/**
 	 * Handles two finger pan and zoom gesture
 	 * @param startPoint1 the start point of first finger
 	 * @param endPoint1 the end point of first finger
 	 * @param startPoint2 the start point of second finger
 	 * @param endPoint2 the end point of second finger
 	 */
 	private void zoomGesture(PointF startPoint1, PointF endPoint1, PointF startPoint2, PointF endPoint2) {
 		float startDist = Utils.distanceBetween(startPoint1, startPoint2);
 		float endDist = Utils.distanceBetween(endPoint1, endPoint2);
 		
 		float scaleFactor = endDist / startDist;
 		
 		// Map previous points into complex space using current params
 		Complex prevC1 = pixelsToComplex(params, startPoint1);
 		Complex prevC2 = pixelsToComplex(params, startPoint2);
 		
 		// Update params zoom factor
 		params.setZoom(params.getZoom() * scaleFactor);
 		
 		// Map current points into complex space using updated params
 		Complex currC1 = pixelsToComplex(params, endPoint1);
 		Complex currC2 = pixelsToComplex(params, endPoint2);
 		
 		// Calculate mid-points and their diff
 		Complex prevMP = prevC1.add(prevC2).scale(0.5);
 		Complex currMP = currC1.add(currC2).scale(0.5);
 		Complex diff = currMP.sub(prevMP);
 		
 		// Update params offset
 		params.setOffset(params.getOffset().sub(diff));
 	}
 	
 	/**
 	 * Gets the renderer parameters
 	 * @return the parameters
 	 */
 	public RendererParams getRendererParams() {
 		return params;
 	}
 	
 	/**
 	 * Sets the renderer parameters
 	 * @return the parameters
 	 */
 	public void setRendererParams(RendererParams params) {
 		this.params = params;
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
 	
 	/**
 	 * Gets the off-screen bitmap
 	 * @return the bitmap
 	 */
 	public Bitmap getBitmap() {
 		return bitmap;
 	}
 	
 	/**
 	 * Gets the desired renderer width
 	 * @param viewWidth the width of the view
 	 * @return the width
 	 */
 	protected int getDesiredRendererWidth(int viewWidth) {
 		return viewWidth;
 	}
 	
 	/**
 	 * Gets the desired renderer height
 	 * @param viewHeight the height of the view
 	 * @return the height
 	 */
 	protected int getDesiredRendererHeight(int viewHeight) {
 		return viewHeight;
 	}
 	
 	/**
 	 * Gets the number of iterations per frame
 	 * @return the number of iterations
 	 */
 	public int getIterationsPerFrame() {
 		return itersPerFrame;
 	}
 
 	/**
 	 * Sets the number of iterations per frame
 	 * @param itersPerFrame the number of iterations
 	 */
 	public void setIterationsPerFrame(int itersPerFrame) {
 		this.itersPerFrame = itersPerFrame;
 	}
 	
 	/**
 	 * Gets the palette mapping mode
 	 * @return the mapping
 	 */
 	public Mapping getPaletteMapping() {
 		return paletteMapping;
 	}
 
 	/**
 	 * Sets the palette mapping mode
 	 * @param paletteMapping the mapping
 	 */
 	public void setPaletteMapping(Mapping mapping) {
 		this.paletteMapping = mapping;
 	}
 
 	/**
 	 * Converts the pixel space point to coordinate in complex-space
 	 * @param params the renderer params
 	 * @param p the point in pixel space
 	 * @return the coordinate in complex space
 	 */
 	protected Complex pixelsToComplex(RendererParams params, PointF p) {
 		Complex offset = params.getOffset();
 		double inv_zoom = 1 / params.getZoom();
 		int half_w = getWidth() / 2;
 		int half_h = getHeight() / 2;
 		double re = (p.x - half_w) * inv_zoom + offset.re;
 		double im = (half_h - p.y) * inv_zoom + offset.im;
 		return new Complex(re, im);
 	}
 	
 	/**
 	 * Converts the pixel space point to coordinate in complex space
 	 * @param params the renderer params
 	 * @param c the coordinate in complex space
 	 * @return the point in pixel space
 	 */
 	private PointF complexToPixels(RendererParams params, Complex c) {
 		Complex offset = params.getOffset();
 		double zoom = params.getZoom();
 		int half_w = getWidth() / 2;
 		int half_h = getHeight() / 2;
 		double x = (c.re - offset.re) * zoom + half_w;
 		double y = half_h - (c.im - offset.im) * zoom;
 		return new PointF((float)x, (float)y);
 	}
 }
