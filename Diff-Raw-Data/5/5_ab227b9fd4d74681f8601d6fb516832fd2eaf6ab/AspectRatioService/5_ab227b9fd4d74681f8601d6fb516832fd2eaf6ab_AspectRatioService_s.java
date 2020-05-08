 package ambibright.engine;
 
 import java.awt.Rectangle;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ambibright.ressources.CurrentBounds;
 import ambibright.ihm.AspectRatioDebugFrame;
 import ambibright.engine.capture.RgbColor;
 import ambibright.engine.capture.Image;
 import ambibright.config.Config;
 
 /**
  * Checks for changes in aspect ratio and update the bounds if any
  */
 public class AspectRatioService implements Runnable {
 
 	private static final Logger logger = LoggerFactory.getLogger(AspectRatioService.class);
 	// With some br iso, the ratio of the video is 16:9 but there are black
 	// bands included in it and the main black color used is (16, 16,
 	// 16) with the last few lines with varying dark color.
	private static final int blackLimit = 60;
 	private final CurrentBounds currentBounds;
 	private final Config config;
 	private final AspectRatioDebugFrame debugFrame;
 	/**
 	 * RgbColor used to store the value. We create only one instance and update
 	 * it to avoid the creation of million of objects.
 	 */
 	private final RgbColor color = new RgbColor();
 	private Rectangle lastScreenBounds;
 
 	public AspectRatioService(CurrentBounds currentBounds, Config config) {
 		this.currentBounds = currentBounds;
 		this.config = config;
 		this.lastScreenBounds = currentBounds.getFullscreenBounds();
 		this.debugFrame = new AspectRatioDebugFrame();
 	}
 
 	@Override
 	public void run() {
 		logger.debug("Checking if aspect ratio changed");
 
 		// we retrieve the fullscreen bounds and screen device in the same lock
 		// to be
 		// sure we have coherent datas
 		int screenDevice;
 		Rectangle fullscreenBounds;
 		currentBounds.readLock();
 		try {
 			screenDevice = currentBounds.getScreenDeviceNoLock();
 			fullscreenBounds = currentBounds.getFullscreenBoundsNoLock();
 		} finally {
 			currentBounds.readUnlock();
 		}
 
 		// Get current image
 		Image image = config.getScreenCapture().captureScreen(fullscreenBounds, screenDevice);
 
 		// Detect top
 		int y = fullscreenBounds.height / 4;
 		for (int testX = 0; testX < fullscreenBounds.width; testX += fullscreenBounds.width / 5) {
 			for (int testY = 0; testY < fullscreenBounds.height / 4; testY++) {
 				if (!isBlack(image.getRGB(testX, testY, color))) {
 					y = Math.min(y, testY);
 					break;
 				}
 			}
 		}
 
 		// Detect left
 		int x = fullscreenBounds.width / 4;
 		for (int testY = 0; testY < fullscreenBounds.height; testY += fullscreenBounds.height / 5) {
 			for (int testX = 0; testX < fullscreenBounds.width / 4; testX++) {
 				if (!isBlack(image.getRGB(testX, testY, color)) || !isBlack(image.getRGB(fullscreenBounds.width - testX - 1, testY, color))) {
 					x = Math.min(x, testX);
 					break;
 				}
 			}
 		}
 
 		// Flushing the image
 		image.flush();
 
 		Rectangle newBounds = new Rectangle(fullscreenBounds.x + x, fullscreenBounds.y + y, fullscreenBounds.width - (2 * x), fullscreenBounds.height - (2 * y));
 		if (!lastScreenBounds.equals(newBounds)) {
 			logger.info("Aspect ratio changed. New bounds : {}", newBounds);
 			lastScreenBounds = newBounds;
 			currentBounds.updateBounds(newBounds);
 			if (config.isAspectRatioDebug()) {
 				debugFrame.showAspectRatioChange(newBounds);
 			}
 		} else {
 			logger.debug("Aspect ratio didn't change");
 		}
 	}
 
 	private boolean isBlack(RgbColor color) {
		return (color.red() + color.blue() + color.green()) <= blackLimit;
 	}
 }
