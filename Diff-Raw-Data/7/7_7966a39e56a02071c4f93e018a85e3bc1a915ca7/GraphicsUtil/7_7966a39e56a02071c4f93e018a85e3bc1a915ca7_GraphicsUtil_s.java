 /*******************************************************************************
  * Copyright (c) 2010 Oak Ridge National Laboratory.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  ******************************************************************************/
 package org.csstudio.swt.xygraph.util;
 
 import org.eclipse.draw2d.FigureUtilities;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Device;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.graphics.Transform;
 import org.eclipse.swt.widgets.Display;
 
 /**Utility function for graphics operations.
  * @author Xihui Chen
  *
  */
 public final class GraphicsUtil {
 
 	/**Draw vertical text.
 	 * @param graphics draw2D graphics.
 	 * @param text text to be drawn.
 	 * @param x the x coordinate of the text, which is the left upper corner.
 	 * @param y the y coordinate of the text, which is the left upper corner.
 	 */
 	public static final void drawVerticalText(Graphics graphics, String text, int x, int y, boolean upToDown){
 		try {
 			graphics.pushState();
 			graphics.translate(x, y);
 			if(upToDown){
 				graphics.rotate(90);
 				graphics.drawText(text, 0, -FigureUtilities.getTextExtents(text, graphics.getFont()).height);
 			}else{
 				graphics.rotate(270);			
 				graphics.drawText(text, -FigureUtilities.getTextWidth(text, graphics.getFont()), 0);
 			}
 			graphics.popState();
 		} catch (Exception e) {//If rotate is not supported by the graphics.
 			graphics.popState();
 			final Dimension titleSize = FigureUtilities.getTextExtents(text, graphics.getFont());
 
 			final int w = titleSize.height;
 			final int h = titleSize.width +1;
 			Image image = new Image(Display.getCurrent(),w, h);			
 					try {
 					    final GC gc = createGC(image);	
 					    final Color titleColor = graphics.getForegroundColor();
 					    RGB transparentRGB = new RGB(240, 240, 240);		
 										
 						gc.setBackground(XYGraphMediaFactory.getInstance().getColor(transparentRGB));
 						gc.fillRectangle(image.getBounds());
 						gc.setForeground(titleColor);
 						gc.setFont(graphics.getFont());
 						final Transform tr = new Transform(Display.getCurrent());
 						if(!upToDown){
 							tr.translate(0, h);
 							tr.rotate(-90);
 							setTransform(gc, tr);
 						}else{
 							tr.translate(w, 0);
 							tr.rotate(90);
 							setTransform(gc, tr);
 						}
 						gc.drawText(text, 0, 0);
 						tr.dispose();
 						gc.dispose();
 						final ImageData imageData = image.getImageData();				
 						image.dispose();
 						imageData.transparentPixel = imageData.palette.getPixel(transparentRGB);
 						image = new Image(Display.getCurrent(), imageData);				
 								
 						graphics.drawImage(image, x, y);
 						
 					} finally{
 						image.dispose();		
 					}
 		}
 	}
 	
 	
 	/** Draw vertical text.
 	 * @param graphics draw2D graphics.
 	 * @param text text to be drawn.
 	 * @param location the left upper corner coordinates of the text.
 	 */
 	public static final void drawVerticalText(Graphics graphics, String text, Point location, boolean upToDown){
 		drawVerticalText(graphics, text, location.x, location.y, upToDown);
 	}
 	
 	/**
 	 * Used for single sourcing, returns null if called in RAP Context.
 	 * @param image
 	 * @return
 	 */
	public static GC createGC(Image image) {
 		try {
		    return GC.class.getConstructor(Image.class).newInstance(image);
 		} catch (Exception ne) {
 			return null;
 		}
 	}
 	
 	public static void setTransform(GC gc, Transform transform) {
 		try {
 			GC.class.getMethod("setTransform", Transform.class).invoke(gc, transform);
 		} catch (Exception ne) {
 			return;
 		}
 	}
 
 
 	public static Cursor createCursor(Device device, ImageData imageData, int width, int height) {
 		try {
 			return Cursor.class.getConstructor(Device.class, ImageData.class, int.class, int.class).newInstance(device, imageData, width, height);
 		} catch (Exception ne) {
 			return null;
 		}
 	}
 }
