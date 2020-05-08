 package com.jpii.navalbattle.util.toaster;
 
 import java.awt.*;
 
 public class Animation extends Thread {
 	SingleToaster toaster;
 
 	public Animation(SingleToaster toaster) {
 		this.toaster = toaster;
 	}
 
 	/**
 	 * Animate vertically the toaster. The toaster could be moved from
 	 * bottom to upper or to upper to bottom
 	 * 
 	 * @param posx
 	 * @param fromy
 	 * @param toy
 	 * @throws InterruptedException
 	 */
 	protected void animateVertically(int posx, int fromY, int toY)
 			throws InterruptedException {
 
 		toaster.setLocation(posx, fromY);
 		if (toY < fromY) {
 			for (int i = fromY; i > toY; i -= toaster.t.getStep()) {
 				toaster.setLocation(posx, i);
 				Thread.sleep(toaster.t.getStepTime());
 			}
 		} else {
 			for (int i = fromY; i < toY; i += toaster.t.getStep()) {
 				toaster.setLocation(posx, i);
 				Thread.sleep(toaster.t.getStepTime());
 			}
 		}
 		toaster.setLocation(posx, toY);
 	}
 
 	public void run() {
 		try {
 			boolean animateFromBottom = true;
 			GraphicsEnvironment ge = GraphicsEnvironment
 					.getLocalGraphicsEnvironment();
 			Rectangle screenRect = ge.getMaximumWindowBounds();
			toaster.toFront();
			try {
				toaster.setAlwaysOnTop(true);
			}
			catch (Throwable thr){
				
			}
 
 			int screenHeight = (int) screenRect.height;
 
 			int startYPosition;
 			int stopYPosition;
 
 			if (screenRect.y > 0) {
 				animateFromBottom = false; // Animate from top!
 			}
 
 			toaster.t.maxToasterInSceen = screenHeight / toaster.t.getToasterHeight();
 
 			int posx = (int) screenRect.width - toaster.t.getToasterWidth() - 1;
 
 			toaster.setLocation(posx, screenHeight);
 			toaster.setVisible(true);
 			if (toaster.t.useAlwaysOnTop) {
 				toaster.setAlwaysOnTop(true);
 			}
 
 			if (animateFromBottom) {
 				startYPosition = screenHeight;
 				stopYPosition = startYPosition - toaster.t.getToasterHeight() - 1;
 				if (toaster.t.currentNumberOfToaster > 0) {
 					stopYPosition = stopYPosition
 							- (toaster.t.maxToaster % toaster.t.maxToasterInSceen * toaster.t.getToasterHeight());
 				} else {
 					toaster.t.maxToaster = 0;
 				}
 			} else {
 				startYPosition = screenRect.y - toaster.t.getToasterHeight();
 				stopYPosition = screenRect.y;
 
 				if (toaster.t.currentNumberOfToaster > 0) {
 					stopYPosition = stopYPosition
 							+ (toaster.t.maxToaster % toaster.t.maxToasterInSceen * toaster.t.getToasterHeight());
 				} else {
 					toaster.t.maxToaster = 0;
 				}
 			}
 
 			toaster.t.currentNumberOfToaster++;
 			toaster.t.maxToaster++;
 
 			animateVertically(posx, startYPosition, stopYPosition);
 			Thread.sleep(toaster.t.getDisplayTime());
 			animateVertically(posx, stopYPosition, startYPosition);
 
 			toaster.t.currentNumberOfToaster--;
 			toaster.setVisible(false);
 			toaster.dispose();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
