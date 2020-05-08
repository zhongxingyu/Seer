 /*
  *  Copyright © 2011-2013 Brian Groenke
  *  All rights reserved.
  * 
  *  This file is part of the 2DX Graphics Library.
  *
  *  This Source Code Form is subject to the terms of the
  *  Mozilla Public License, v. 2.0. If a copy of the MPL 
  *  was not distributed with this file, You can obtain one at 
  *  http://mozilla.org/MPL/2.0/.
  */
 
 package com.snap2d.gl;
 
 import java.awt.*;
 import java.awt.RenderingHints.Key;
 import java.awt.event.*;
 import java.awt.image.*;
 import java.io.*;
 import java.util.*;
 import java.util.List;
 import java.util.concurrent.*;
 
 import org.bridj.*;
 
 import bg.x2d.*;
 import bg.x2d.ImageUtils;
 import bg.x2d.utils.*;
 
 import com.nativelibs4java.opencl.*;
 import com.nativelibs4java.opencl.CLMem.MapFlags;
 import com.nativelibs4java.opencl.CLMem.Usage;
 import com.nativelibs4java.util.*;
 import com.snap2d.*;
 
 /**
  * Acts as a rendering handle to a Display. This class handles the core game update/render thread.
  * 
  * @author Brian Groenke
  * @since Snapdragon2D 1.0
  * @see com.snap2d.gl.Display
  */
 public class RenderControl {
 
 	public static final String CL_LIB_LOC = Local.getNativeLibraryLocation()
 			+ "/opencl";
 
 	public static final int POSITION_LAST = 0x07FFFFFFF;
 	public static final Color CANVAS_BACK = Color.WHITE;
 
 	public static int stopTimeout = 2000;
 
 	private static final long RESIZE_TIMER = (long) 1.0E8;
 	private static final String CL_PROG = CL_LIB_LOC + "/RenderingKernels.cl",
 			KERNEL1 = "render_px";
 
 	/**
 	 * Determines whether or not auto-resizing should be used. True by default.
 	 */
 	public volatile boolean auto = true;
 	
 	/**
 	 * True if hardware acceleration (VolatileImage) should be used, false otherwise.
 	 */
 	public volatile boolean accelerated = true;
 
 	protected Canvas canvas;
 	protected volatile BufferedImage pri;
 	protected volatile VolatileImage accBuff;
 	protected volatile int[] pixelData;
 	protected volatile long lastResizeFinish;
 	protected volatile boolean applyGamma, updateGamma, scheduledResize;
 
 	protected List<Renderable> rtasks = new ArrayList<Renderable>(),
 			delQueue = new Vector<Renderable>();
 	protected List<QueuedRenderable> addQueue = new Vector<QueuedRenderable>();
 	protected RenderLoop loop;
 	protected AutoResize autoResize;
 	protected Future<?> taskCallback;
 	protected Dimension initSize;
 	protected int buffs;
 	protected float gamma = 1.0f;
 
 	protected GammaTable gammaTable = new GammaTable(gamma);
 	protected Map<RenderingHints.Key, Object> renderOps;
 
 	private Semaphore loopChk = new Semaphore(1, true);
 	private volatile boolean fastRenderingMode;
 
 	// ----- JavaCL components -------- //
 	private CLProgram clProg;
 	private CLKernel renderKernel;
 	private CLQueue clQueue;
 	private CLContext clContext;
 	private CLBuffer<Integer> dataBuff, gBuff;
 	private Pointer<Integer> dPtr, gPtr;
 	private int[] globalWorkSizes;
 
 	// -------------------------------- //
 
 	/**
 	 * Creates a RenderControl object that can be used to render data to a Display. A Canvas object
 	 * is created internally with a managed BufferStrategy.
 	 * 
 	 * @param buffs
 	 *            the number of buffers the BufferStrategy should be created with.
 	 */
 	protected RenderControl(int buffs) {
 		this.canvas = new Canvas();
 		this.buffs = buffs;
 
 		try {
 			initOpenCL();
 		} catch (IOException e1) {
 			System.err.println("[Snap2D] Error initializing OpenCL");
 			e1.printStackTrace();
 		}
 
 		renderOps = new HashMap<RenderingHints.Key, Object>();
 		loop = new RenderLoop();
 		autoResize = new AutoResize();
 
 		canvas.setIgnoreRepaint(true);
 		canvas.addComponentListener(autoResize);
 		canvas.addFocusListener(new FocusListener() {
 
 			@Override
 			public void focusGained(FocusEvent e) {
 				setRenderActive(true);
 			}
 
 			@Override
 			public void focusLost(FocusEvent e) {
 				setRenderActive(false);
 			}
 		});
 	}
 
 	public void startRenderLoop() {
 		taskCallback = ThreadManager.submitJob(loop);
 	}
 
 	public void stopRenderLoop() {
 		loop.running = false;
 		loop.active = false;
 
 		long st = System.currentTimeMillis();
 		while (!taskCallback.isDone()) {
 			if (System.currentTimeMillis() - st > stopTimeout) {
 				taskCallback.cancel(true);
 				break;
 			}
 		}
 	}
 
 	/**
 	 * If true, the rendering loop will actively render to the screen and execute update logic.
 	 * Otherwise, the loop will sleep until it is stopped or set to active again.
 	 * 
 	 * @param active
 	 *            true to enable active rendering/updates, false to disable rendering/updates
 	 */
 	public void setRenderActive(boolean active) {
 		try {
 			loopChk.acquire();
 			loop.active = active;
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} finally {
 			loopChk.release();
 		}
 	}
 
 	/**
 	 * If true, the rendering loop will continue to run, but update ticks will be skipped. False by
 	 * default.
 	 * 
 	 * @param noUpdate
 	 *            true if updates should be disabled, false to enable.
 	 */
 	public void setDisableUpdates(boolean noUpdate) {
 		loop.noUpdate = noUpdate;
 	}
 
 	/**
 	 * Fast rendering mode will draw all Renderables to a hardware accelerated VolatileImage
 	 * and immediately draw it to screen.  This will usually result in a massive performance
 	 * boost over the normal rendering methods at the cost of post processing capabilities.
 	 * All effects that require direct pixel buffer access (i.e. gamma correction, per-pixel lighting,
 	 * etc) will not be active in fast rendering mode.
 	 * @param fastRender true if fast rendering mode should be enabled, false otherwise
 	 */
 	public void setFastRenderingMode(boolean fastRender) {
 		fastRenderingMode = fastRender;
 		System.out.println("[Snap2D] fast_rendering_mode=" + fastRenderingMode);
 	}
 	
 	public boolean isInFastRenderingMode() {
 		return fastRenderingMode;
 	}
 	
 	/**
 	 * Enable/disable hardware accelerated rendering of images. True by default.
 	 * 
 	 * @param accelerated
 	 */
 	public void setUseHardwareAcceleration(boolean accelerated) {
 		this.accelerated = accelerated;
 	}
 
 	/**
 	 * Fully releases system resources used by this RenderControl object and clears all registered
 	 * Renderables. Note that once this method is called, the object is unusable and should be
 	 * released for garbage collection. Continued use of a disposed RenderControl object will cause
 	 * errors.
 	 */
 	public void dispose() {
 		stopRenderLoop();
 		BufferStrategy bs = canvas.getBufferStrategy();
 		if (bs != null) {
 			bs.dispose();
 		}
 		rtasks.clear();
 		addQueue.clear();
 		delQueue.clear();
 		renderOps.clear();
 		pri.flush();
 		if (accBuff != null) {
 			accBuff.flush();
 		}
 
 		// nullify references to potentially significant resource holders to ensure that they are
 		// available
 		// for garbage collection.
 		pixelData = null;
 		canvas = null;
 		loop = null;
 		pri = null;
 		accBuff = null;
 		autoResize = null;
 		taskCallback = null;
 
 		// Manually release OpenCL resources to ensure everything is cleaned up ASAP.
 		if(clContext != null) { // generally a sufficient check to see if OpenCL initialized correctly
 			clProg.release();
 			renderKernel.release();
 			clQueue.release();
 			clContext.release();
 			dataBuff.release();
 			gBuff.release();
 			dPtr.release();
 			gPtr.release();
 		}
 	}
 
 	/**
 	 * Gets the last recorded number of frames rendered per second.
 	 * 
 	 * @return
 	 */
 	public int getCurrentFPS() {
 		return loop.fps;
 	}
 
 	/**
 	 * Gets the last recorded number of updates (ticks) per second.
 	 * 
 	 * @return
 	 */
 	public int getCurrentTPS() {
 		return loop.tps;
 	}
 
 	/**
 	 * Sets the frame rate that the rendering algorithm will target when interpolating.
 	 * 
 	 * @param fps
 	 *            frames per second
 	 */
 	public void setTargetFPS(int fps) {
 		loop.setTargetFPS(fps);
 	}
 
 	/**
 	 * Sets the frequency per second at which the Renderable.update method is called.
 	 * 
 	 * @param tps
 	 *            ticks per second
 	 */
 	public void setTargetTPS(int tps) {
 		loop.setTargetTPS(tps);
 	}
 
 	/**
 	 * Sets the max number of times updates can be issued before a render must occur. If animations
 	 * are "chugging" or skipping, it may help to set this value to a very low value (1-2). Higher
 	 * values will prevent the game updates from freezing.
 	 * 
 	 * @param maxUpdates
 	 *            max number of updates to be sent before rendering.
 	 */
 	public void setMaxUpdates(int maxUpdates) {
 		loop.setMaxUpdates(maxUpdates);
 	}
 
 	/**
 	 * Sets the gamma value that will be applied to all pixels rendered on screen.
 	 * 
 	 * @param gamma
 	 *            a gamma value (0.0 > gamma < 1.0 = darker; gamma > 1.0 = brighter)
 	 */
 	public void setGamma(float gamma) {
 		if (gamma >= 0) {
 			this.gamma = gamma;
 			updateGamma = true;
 		}
 	}
 
 	/**
 	 * Enables/disables gamma correction on the rendered image.
 	 * 
 	 * @param enabled
 	 */
 	public void setGammaCorrectionEnabled(boolean enabled) {
 		applyGamma = enabled;
 	}
 
 	/**
 	 * @return true if gamma correction is enabled, false otherwise.
 	 */
 	public boolean isGammaEnabled() {
 		return applyGamma && !fastRenderingMode;
 	}
 
 	/**
 	 * @return true if hardware acceleration is enabled, false otherwise.
 	 */
 	public boolean isHardwareAccelerated() {
 		return accelerated;
 	}
 
 	/**
 	 * Checks to see if this RenderControl has a loop that is actively rendering/updating.
 	 * 
 	 * @return true if active, false otherwise.
 	 */
 	public boolean isActive() {
 		return loop.active;
 	}
 
 	/**
 	 * Checks to see if this RenderControl has a currently running loop.
 	 * 
 	 * @return true if a loop is running, false otherwise.
 	 */
 	public boolean isRunning() {
 		return loop.running;
 	}
 
 	/**
 	 * Registers the Renderable object with this RenderControl to be rendered on screen. The
 	 * render() method will be called and the RenderData object used to show the image on screen.
 	 * 
 	 * @param r
 	 *            the Renderable object to be called when rendering.
 	 * @param pos
 	 *            the position in the rendering queue to be placed. 0 is the first to be rendered on
 	 *            each frame and LAST is provided as a convenience field to insert at position size
 	 *            - 1 (aka the end of the queue, thus last to be rendered on each frame).
 	 */
 	public synchronized void addRenderable(Renderable r, int pos) {
 		if (pos == POSITION_LAST) {
 			pos = (addQueue.size() == 0) ? rtasks.size() : rtasks.size()
 					+ addQueue.size();
 		}
 		QueuedRenderable qr = new QueuedRenderable();
 		qr.pos = pos;
 		qr.r = r;
 		addQueue.add(qr);
 	}
 
 	/**
 	 * Removes the Renderable object from the queue, if it exists.
 	 * 
 	 * @param r
 	 *            removes the Renderable from the queue.
 	 */
 	public synchronized void removeRenderable(Renderable r) {
 		delQueue.add(r);
 	}
 
 	/**
 	 * Fetches List.size() for the rendering queue.
 	 * 
 	 * @return
 	 */
 	public int getRenderQueueSize() {
 		return rtasks.size();
 	}
 
 	public void setRenderOp(Key key, Object value) {
 		renderOps.put(key, value);
 	}
 
 	public void setRenderOps(Map<Key, ?> hints) {
 		renderOps.putAll(hints);
 	}
 
 	public Object getRenderOpValue(Key key) {
 		return renderOps.get(key);
 	}
 
 	/**
 	 * Blends a translucent pixel with an opaque destination value. Currently unused.
 	 * 
 	 * @param srcA
 	 * @param srcValue
 	 * @param dstValue
 	 * @return
 	 */
 	protected int blend(float srcA, int srcValue, int dstValue) {
 
 		float srcR = ((srcValue & 0x00ff0000) >>> 16) / 255f;
 		float srcG = ((srcValue & 0x0000ff00) >>> 8) / 255f;
 		float srcB = ((srcValue & 0x000000ff)) / 255f;
 
 		// float dstA = ((dstValue & 0xff000000) >>> 24) / 255f;
 		float dstR = ((dstValue & 0x00ff0000) >>> 16) / 255f;
 		float dstG = ((dstValue & 0x0000ff00) >>> 8) / 255f;
 		float dstB = ((dstValue & 0x000000ff)) / 255f;
 
 		srcR *= srcA;
 		srcG *= srcA;
 		srcB *= srcA;
 
 		// final output
 		float R = srcR + dstR * (1 - srcA);
 		float G = srcG + dstG * (1 - srcA);
 		float B = srcB + dstB * (1 - srcA);
 
 		return ((int) (R * 255) << 16) | ((int) (G * 255) << 8)
 				| (int) (B * 255);
 	}
 
 	ExecutorService renderPool = Executors.newFixedThreadPool(Runtime
 			.getRuntime().availableProcessors(), new RenderThreadFactory());
 	ArrayList<RenderRow> rowCache = new ArrayList<RenderRow>();
 
 	int[] testArr;
 
 	/**
 	 * Internal method that is called by RenderLoop to draw rendered data to the screen. The back
 	 * buffer's data is copied to the main image which, if hardware acceleration is enabled, is
 	 * drawn to a VolatileImage. Otherwise, the BufferedImage itself is drawn.
 	 */
 	protected void render(Renderable[] renderables, float interpolation) {
 		// If the component was being resized, cancel rendering until finished (prevents
 		// flickering).
 		if (System.nanoTime() - lastResizeFinish < RESIZE_TIMER) {
 			return;
 		}
 		if (canvas.getParent() == null) {
 			return;
 		}
 		BufferStrategy bs = canvas.getBufferStrategy();
 		if (bs == null) {
 			canvas.createBufferStrategy(buffs);
 			canvas.requestFocus();
 			return;
 		}
 
 		int priHeight = pri.getHeight();
 
 		Graphics2D g = (Graphics2D) bs.getDrawGraphics();
 		g.setRenderingHints(renderOps);
 		g.setColor(CANVAS_BACK);
 		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
 		try {
 			
 			if (fastRenderingMode) {
 				// Check the status of the VolatileImage and update/re-create it if neccessary.
 				if (accBuff == null || accBuff.getWidth() != pri.getWidth()
 						|| accBuff.getHeight() != pri.getHeight()) {
 					accBuff = ImageUtils.createVolatileImage(pri.getWidth(),
 							pri.getHeight());
 					accBuff.setAccelerationPriority(1.0f);
 				}
 				int stat = 0;
 				do {
 					if ((stat = ImageUtils.validateVI(accBuff, g)) != VolatileImage.IMAGE_OK) {
 						if (stat == VolatileImage.IMAGE_INCOMPATIBLE) {
 							accBuff = ImageUtils.createVolatileImage(
 									pri.getWidth(), pri.getHeight());
 							accBuff.setAccelerationPriority(1.0f);
 						}
 					}
 
 					Graphics2D img = accBuff.createGraphics();
 					for (Renderable r : renderables) {
 						r.render(img, interpolation);
 					}
 					img.dispose();
 				} while (accBuff.contentsLost());
 				
 				g.drawImage(accBuff, 0, 0, null);
 			} else {
 				// If acceleration is now disabled but was previously enabled, release system
 				// resources held by
 				// VolatileImage and set the reference to null.
 				if (accBuff != null) {
 					accBuff.flush();
 					accBuff = null;
 				}
 				
 				Graphics2D g2 = pri.createGraphics();
 				for (Renderable r : renderables) {
 					r.render(g2, interpolation);
 				}
 				g2.dispose();
 				
 				if (clContext != null) {
 					Pointer<Integer> dPtr = dataBuff.map(clQueue, MapFlags.Write);
 					dPtr.setInts(pixelData);
 					dataBuff.unmap(clQueue, dPtr);
 					renderKernel.setArgs(pixelData.length, (applyGamma) ? 1 : 0,
 							dataBuff, gBuff);
 					CLEvent exec = renderKernel.enqueueNDRange(clQueue,
 							globalWorkSizes);
 					dPtr = dataBuff.map(clQueue, MapFlags.Read, exec);
 					dPtr.getInts(pixelData);
 					dataBuff.unmap(clQueue, dPtr);
 				} else {
 					Future<?> finalRow = null;
 					for (int y = 0; y < priHeight; y++) {
 
 						if (y >= priHeight || y < 0) {
 							continue;
 						}
 
 						if (y >= rowCache.size()) {
 							rowCache.add(new RenderRow(y));
 						}
 						Future<?> task = renderPool.submit(rowCache.get(y));
 						if (y == priHeight - 1) {
 							finalRow = task;
 						}
 					}
 
 					while (!finalRow.isDone()) {
 						;
 					}
 				}
				if (clContext != null) {
					Pointer<Integer> dPtr = dataBuff.map(clQueue, MapFlags.Write);
					dPtr.setInts(pixelData);
					dataBuff.unmap(clQueue, dPtr);
					renderKernel.setArgs(pixelData.length, (applyGamma) ? 1 : 0,
							dataBuff, gBuff);
					CLEvent exec = renderKernel.enqueueNDRange(clQueue,
							globalWorkSizes);
					dPtr = dataBuff.map(clQueue, MapFlags.Read, exec);
					dPtr.getInts(pixelData);
					dataBuff.unmap(clQueue, dPtr);
				} else {
					Future<?> finalRow = null;
					for (int y = 0; y < priHeight; y++) {

						if (y >= priHeight || y < 0) {
							continue;
						}

						if (y >= rowCache.size()) {
							rowCache.add(new RenderRow(y));
						}
						Future<?> task = renderPool.submit(rowCache.get(y));
						if (y == priHeight - 1) {
							finalRow = task;
						}
					}

					while (!finalRow.isDone()) {
						;
					}
				}
 				g.drawImage(pri, 0, 0, null);
 			}
 		} finally {
 			g.dispose();
 		}
 		
 		if (!bs.contentsLost() && canvas.getParent() != null) {
 			bs.show();
 		}
 	}
 
 	/**
 	 * Used in concurrent rendering.
 	 * 
 	 * @author Brian Groenke
 	 */
 	protected class RenderRow implements Runnable {
 
 		int y;
 
 		RenderRow(int y) {
 			this.y = y;
 		}
 
 		@Override
 		public void run() {
 			int priWidth = pri.getWidth();
 			for (int x = 0; x < priWidth; x++) {
 
 				if (x >= priWidth || x < 0) {
 					continue;
 				}
 
 				int pos = y * priWidth + x;
 				if (pos < 0 || pos >= pixelData.length) {
 					continue;
 				}
 
 				// get foreground pixels (source)
 				int srcValue = pixelData[y * priWidth + x];
 				int b = srcValue & 0xFF;
 				int g = srcValue >> 8 & 0xFF;
 				int r = srcValue >> 16 & 0xFF;
 		int a = srcValue >> 24 & 0xFF;
 		if(applyGamma) {
 			a = gammaTable.applyGamma(a);
 			r = gammaTable.applyGamma(r);
 			g = gammaTable.applyGamma(g);
 			b = gammaTable.applyGamma(b);
 		}
 		srcValue = a;
 		srcValue = (srcValue << 8) + r;
 		srcValue = (srcValue << 8) + g;
 		srcValue = (srcValue << 8) + b;
 		pixelData[pos] = srcValue;
 			}
 		}
 
 	}
 
 	private void initOpenCL() throws IOException {
 		if(!Boolean.getBoolean("com.snap2d.gl.opencl"))
 			return;
 		else if (JavaCL.listPlatforms() == null
 				|| JavaCL.listPlatforms().length == 0) {
 			System.out.println("[Snap2D] No supported OpenCL drivers detected");
 			return;
 		}
 
 		clContext = JavaCL.createBestContext();
 		String progSrc = IOUtils.readText(ClassLoader
 				.getSystemResource(CL_PROG));
 		clContext.setCacheBinaries(true);
 		clProg = clContext.createProgram(progSrc);
 		renderKernel = clProg.createKernel(KERNEL1);
 		clQueue = clContext.createDefaultQueue();
 
 		gPtr = Pointer.allocateInts(256).order(clContext.getByteOrder());
 		gPtr.setInts(gammaTable.getTable());
 		gBuff = clContext.createBuffer(Usage.Input, gPtr, false);
 
 		CLPlatform platform = JavaCL.getBestDevice().getPlatform();
 		System.out.println("[Snap2D] Initialized " + platform.getName() + " "
 				+ platform.getVersion());
 	}
 
 	/**
 	 * Instantiates the BufferedImage used for drawing onto the screen.
 	 * 
 	 * @param wt
 	 * @param ht
 	 */
 	private void createImages(int wt, int ht) {
 
 		pri = ImageUtils.getNativeImage(wt, ht);
 		if (!(pri.getRaster().getDataBuffer() instanceof DataBufferInt)) {
 			pri = new BufferedImage(wt, ht, BufferedImage.TYPE_INT_ARGB_PRE);
 		}
 		
 		pri.setAccelerationPriority((accelerated) ? 1.0f:0.0f);
 
 		pixelData = ColorUtils.getImageData(pri);
 
 		if(clContext == null)
 			return;
 
 		if(dPtr != null)
 			dPtr.release();
 
 		if(dataBuff != null)
 			dataBuff.release();
 
 		dPtr = Pointer.allocateInts(pixelData.length).order(
 				clContext.getByteOrder());
 		dataBuff = clContext.createBuffer(Usage.InputOutput, dPtr, false);
 		globalWorkSizes = new int[] { pixelData.length };
 	}
 
 	/**
 	 * Loop where render/update logic is executed.
 	 * 
 	 * @author Brian Groenke
 	 */
 	protected class RenderLoop implements Runnable {
 
 		// Default values
 		private final double TARGET_FPS = 60,
 				TARGET_TIME_BETWEEN_RENDERS = 1000000000.0 / TARGET_FPS,
 				TICK_HERTZ = 30,
 				TIME_BETWEEN_UPDATES = 1000000000.0 / TICK_HERTZ,
 				MAX_UPDATES_BEFORE_RENDER = 3;
 
 		private final long SLEEP_WHILE_INACTIVE = 100;
 
 		private double targetFPS = TARGET_FPS,
 				targetTimeBetweenRenders = TARGET_TIME_BETWEEN_RENDERS,
 				tickHertz = TICK_HERTZ,
 				timeBetweenUpdates = TIME_BETWEEN_UPDATES,
 				maxUpdates = MAX_UPDATES_BEFORE_RENDER;
 
 		volatile int fps, tps;
 		volatile boolean running, active, noUpdate, printFrames;
 
 		@Override
 		public void run() {
 			Thread.currentThread().setName("snap2d-render_loop");
 
 			ThreadManager.newDaemon(new Runnable() {
 
 				@Override
 				public void run() {
 					Thread.currentThread().setName("snap2d-sleeper_thread");
 					try {
 						if (Local.getPlatform().toLowerCase()
 								.contains("windows")
 								&& Boolean
 								.getBoolean("com.snap2d.gl.force_timer")) {
 							System.out
 							.println("[Snap2D] started windows sleeper daemon");
 							Thread.sleep(Long.MAX_VALUE);
 						}
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			});
 
 			ThreadManager.newDaemon(new Runnable() {
 
 				@Override
 				public void run() {
 					Thread.currentThread().setName("snap2d-fps_out_thread");
 					while (running) {
 						try {
 							Thread.sleep(800);
 							if (!Boolean
 									.getBoolean("com.snap2d.gl.printframes")) {
 								continue;
 							}
 							while (!printFrames) {
 								;
 							}
 							System.out.println(fps + " fps " + tps + " ticks");
 							printFrames = false;
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 
 			});
 
 			double lastUpdateTime = System.nanoTime();
 			double lastRenderTime = System.nanoTime();
 			int lastSecondTime = (int) (lastUpdateTime / 1000000000);
 			int frameCount = 0, ticks = 0;
 			running = true;
 			active = true;
 			Renderable[] renderables = new Renderable[0];
 			System.runFinalization();
 			System.gc();
 			while (running) {
 				try {
 
 					if (addQueue.size() > 0) {
 
 						for (QueuedRenderable qr : addQueue) {
 							rtasks.add(qr.pos, qr.r);
 						}
 						addQueue.clear();
 
 						renderables = rtasks.toArray(new Renderable[rtasks
 						                                            .size()]);
 					}
 
 					if (delQueue.size() > 0) {
 						for (Renderable r : delQueue) {
 							rtasks.remove(r);
 						}
 						delQueue.clear();
 					}
 
 					if (scheduledResize) {
 						autoResize.resize();
 						scheduledResize = false;
 					}
 
 					if (updateGamma) {
 						gammaTable.setGamma(gamma);
 						updateGamma = false;
 
 						if(clContext != null) {
 							Pointer<Integer> gPtr = gBuff.map(clQueue,
 									MapFlags.Write);
 							gPtr.setInts(gammaTable.getTable());
 							gBuff.unmap(clQueue, gPtr);
 						}
 					}
 
 					double now = System.nanoTime();
 					if (active
 							&& (canvas.getWidth() > 0 && canvas.getHeight() > 0)) {
 
 						if (initSize == null) {
 							initSize = new Dimension(canvas.getWidth(),
 									canvas.getHeight());
 						}
 
 						if (pri == null) {
 							createImages(canvas.getWidth(), canvas.getHeight());
 						}
 
 						int updateCount = 0;
 
 						while (now - lastUpdateTime > timeBetweenUpdates
 								&& updateCount < maxUpdates && !noUpdate) {
 
 							for (Renderable r : renderables) {
 								r.update((long) now, (long) lastUpdateTime);
 							}
 
 							lastUpdateTime += timeBetweenUpdates;
 							updateCount++;
 							ticks++;
 						}
 
 						if (now - lastUpdateTime > timeBetweenUpdates
 								&& !noUpdate) {
 							lastUpdateTime = now - timeBetweenUpdates;
 						}
 
 						float interpolation = Math
 								.min(1.0f,
 										(float) ((now - lastUpdateTime) / timeBetweenUpdates));
 						/*
 						 * Graphics2D g = buff.createGraphics(); for(Renderable r:renderables)
 						 * r.render(g, interpolation); g.dispose(); render();
 						 */
 						render(renderables, interpolation);
 						lastRenderTime = now;
 						frameCount++;
 
 						int thisSecond = (int) (lastUpdateTime / 1000000000);
 						if (thisSecond > lastSecondTime) {
 							fps = frameCount;
 							tps = ticks;
 							printFrames = true;
 							frameCount = 0;
 							ticks = 0;
 							lastSecondTime = thisSecond;
 						}
 					}
 
 					loopChk.release();
 					while (now - lastRenderTime < targetTimeBetweenRenders
 							&& now - lastUpdateTime < timeBetweenUpdates) {
 						Thread.yield();
 						now = System.nanoTime();
 					}
 					loopChk.acquire();
 
 					if (!active) {
 						// preserve CPU if loop is currently is currently inactive.
 						// the constant can be lowered to reduce latency when re-focusing.
 						Thread.sleep(SLEEP_WHILE_INACTIVE);
 					}
 				} catch (Exception e) {
 					System.err.println("Snapdragon2D: error in rendering loop");
 					e.printStackTrace();
 				}
 			}
 		}
 
 		protected void setTargetFPS(int fps) {
 			if (fps < 0) {
 				return;
 			}
 			targetFPS = fps;
 			targetTimeBetweenRenders = 1000000000.0 / targetFPS;
 		}
 
 		protected void setTargetTPS(int tps) {
 			if (tps < 0) {
 				return;
 			}
 			tickHertz = tps;
 			timeBetweenUpdates = 1000000000.0 / tickHertz;
 		}
 
 		protected void setMaxUpdates(int max) {
 			if (max > 0) {
 				maxUpdates = max;
 			}
 		}
 	}
 
 	protected class QueuedRenderable {
 		Renderable r;
 		int pos;
 	}
 
 	protected class AutoResize extends ComponentAdapter {
 
 		private volatile int wt, ht;
 
 		@Override
 		public void componentResized(ComponentEvent e) {
 			wt = e.getComponent().getWidth();
 			ht = e.getComponent().getHeight();
 			scheduledResize = true;
 		}
 
 		protected void resize() {
 			if (wt <= 0) {
 				wt = 1;
 			}
 
 			if (ht <= 0) {
 				ht = 1;
 			}
 
 			createImages(wt, ht);
 			if (auto) {
 				Iterator<Renderable> itr = rtasks.listIterator();
 				while (itr.hasNext()) {
 					Renderable r = itr.next();
 					r.onResize(initSize, new Dimension(wt, ht));
 				}
 
 				lastResizeFinish = System.nanoTime();
 			}
 		}
 
 	}
 
 	protected static class RenderThreadFactory implements ThreadFactory {
 
 		volatile static int poolNum;
 
 		RenderThreadFactory() {
 			poolNum++;
 		}
 
 		int nthreads;
 
 		@Override
 		public Thread newThread(Runnable arg0) {
 			Thread t = new Thread(arg0);
 			t.setName("snap2d_render_pool-" + poolNum + "_0" + nthreads);
 			t.setDaemon(true);
 			t.setPriority(Thread.MAX_PRIORITY);
 			nthreads++;
 			return t;
 		}
 	}
 }
