 package com.github.neothemachine.ardor3d.screenshot;
 
 import java.awt.image.BufferedImage;
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.util.concurrent.Callable;
 
 import com.ardor3d.framework.Canvas;
 import com.ardor3d.framework.DisplaySettings;
 import com.ardor3d.framework.Scene;
 import com.ardor3d.framework.lwjgl.LwjglHeadlessCanvas;
 import com.ardor3d.intersection.PickResults;
 import com.ardor3d.math.Ray3;
 import com.ardor3d.renderer.Renderer;
 import com.ardor3d.renderer.TextureRendererFactory;
 import com.ardor3d.renderer.lwjgl.LwjglTextureRendererProvider;
 import com.ardor3d.scenegraph.Node;
 import com.ardor3d.util.ContextGarbageCollector;
 import com.ardor3d.util.GameTaskQueue;
 import com.ardor3d.util.GameTaskQueueManager;
 import com.ardor3d.util.screen.ScreenExporter;
 import com.github.neothemachine.ardor3d.screenshot.UpdateableCanvas.CanvasUpdate;
 
 /**
  * Work in progress
  * 
  * @author maik
  *
  */
 public class LwjglHeadlessScreenshotCanvas implements ScreenshotCanvas, Scene {
 
 	private final IntDimension size;
 	private final LwjglHeadlessCanvas canvas;
 	private final Renderer renderer;
 	
 	private final Node root = new Node();
 	
 	private final ScreenShotBufferExporter screenShotExp = new ScreenShotBufferExporter();
 	
     private boolean isShotRequested = false;
     private final Object shotFinishedMonitor = new Object();
 	
 	public LwjglHeadlessScreenshotCanvas(IntDimension size) {
 		
 		this.size = size;
 		int aaSamples = 0;
 		
         final DisplaySettings settings = new DisplaySettings(size.getWidth(), size.getHeight(),
         		24, 1, 8, 8, 0, aaSamples, false, false);
 		
 		this.canvas = new LwjglHeadlessCanvas(settings, this);
 		this.renderer = this.canvas.getRenderer();
 		
 		GameTaskQueueManager.getManager(this).getQueue(GameTaskQueue.UPDATE).setExecuteMultiple(true);
         GameTaskQueueManager.getManager(this).getQueue(GameTaskQueue.RENDER).setExecuteMultiple(true);
         
         // Don't know if this is necessary, probably not, but it doesn't hurt.
         // For our own queues, we need it because we only want to render exactly two frames
         // and don't wait until all queued actions are executed. 
         // The internal queue here is only used internally when disposing the canvas and
         // deleting textures etc., and at least the javadoc says that only ONE frame
         // needs to be rendered, so they probably don't enqueue more than one action.
         this.queueCanvasUpdate(new CanvasUpdate() {
 			@Override
 			public void update(Canvas canvas) {
 				GameTaskQueueManager.getManager(canvas.getCanvasRenderer().getRenderContext()).
 	    			getQueue(GameTaskQueue.RENDER).setExecuteMultiple(true);
 			}
 		});
         
         TextureRendererFactory.INSTANCE.setProvider(new LwjglTextureRendererProvider());
 	}
 	
 	@Override
 	public void queueSceneUpdate(final SceneGraphUpdate update) {
 		GameTaskQueueManager.getManager(this).update(new Callable<Void>() {
 			@Override
 			public Void call() throws Exception {
 				update.update(root);
 				root.updateGeometricState(0);
 				return null;
 			}
 		});
 	}
 
 	@Override
 	public void queueCanvasUpdate(final CanvasUpdate update) {
 		GameTaskQueueManager.getManager(this).render(new Callable<Void>() {
 			@Override
 			public Void call() throws Exception {
 				// FIXME we don't have a canvas, what now??
 //				update.update(canvas);
 				return null;
 			}			
 		});
 	}
 
 	@Override
 	public IntDimension getSize() {
 		return this.size;
 	}
 
 	@Override
 	public void setSize(IntDimension size) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public BufferedImage takeShot() {
         // only works after the 2nd frame
 		// FIXME we don't have a frame handler, what now??
 //        _frameHandler.updateFrame();
         isShotRequested = true;
 //        _frameHandler.updateFrame();
     	
 		synchronized (shotFinishedMonitor) {
         	while (isShotRequested) {
 	    		try {
 	    			shotFinishedMonitor.wait();
 	    		} catch (InterruptedException e) {
 	    		}
         	}
 		}
 		
     	return screenShotExp.getLastImage();
 	}
 
 	@Override
 	public void dispose() {
 		// TODO do we need to dispose something?
 	}
 
 	@Override
 	public void addUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
 		
 	}
 
 	@Override
 	public boolean renderUnto(Renderer renderer) {
 		
         GameTaskQueueManager.getManager(this).getQueue(GameTaskQueue.UPDATE)
     	.execute();
 
     	GameTaskQueueManager.getManager(this).getQueue(GameTaskQueue.RENDER)
         .execute(renderer);
 
 		// necessary because internal ardor3d code relies on this queue
 		// it happens after our own queue so that dispose() works correctly
 		// see http://ardor3d.com/forums/viewtopic.php?f=13&t=1020&p=16253#p16253
 //		GameTaskQueueManager.getManager(canvas.getRenderer().getRenderContext()).
 //			getQueue(GameTaskQueue.RENDER).execute(renderer);
 		
 		// Clean up card garbage such as textures, vbos, etc.
 		ContextGarbageCollector.doRuntimeCleanup(renderer);
 			
 		root.draw(renderer);
 		
 		if (isShotRequested) {
 		    // force any waiting scene elements to be rendered.
 		    renderer.renderBuckets();
 		    ScreenExporter.exportCurrentScreen(canvas.getRenderer(), screenShotExp);
 		    synchronized (shotFinishedMonitor) {
 		    	isShotRequested = false;
 		    	this.shotFinishedMonitor.notifyAll();
 			}
 		}
 		return true;
 		
 	}
 
 	@Override
 	public PickResults doPick(Ray3 pickRay) {
		throw new UnsupportedOperationException();
 	}
 
 }
