 package org.dawb.common.ui.image;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 
 import org.dawb.common.ui.plot.axis.IAxis;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.FigureUtilities;
 import org.eclipse.draw2d.MouseEvent;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.widgets.Display;
 
 public class CursorUtils {
 
 	private final static Color TRANSPARENT_COLOR = new Color(null, new RGB(123,0,23));
 	private final static int   CURSOR_SIZE = 28;
 	private final static Color BLACK_COLOR = ColorConstants.black;
 	private final static Color WHITE_COLOR = ColorConstants.white;
 	
     private static NumberFormat intensityFormat =  new DecimalFormat("############.0##");
 
     /**
      * 
      * @param me
      * @param axis
      * @param yAxis
      * @param image -  may be null
      * @return
      */
 	public static Cursor getPositionCursor(MouseEvent me, IAxis xAxis, IAxis yAxis, IImageTrace imageTrace) {
 		
 		double xCoordinate = xAxis.getPositionValue(me.x);
 		double yCoordinate = yAxis.getPositionValue(me.y);
 		double intensity   = Double.NaN;
 		if (imageTrace!=null) {
			xCoordinate = Math.floor(xCoordinate);
			yCoordinate = Math.floor(yCoordinate);
 		    intensity   = imageTrace.getData().getDouble((int)yCoordinate, (int)xCoordinate);
 		    
 		    
 			try {
 				double[] axisPnt = imageTrace.getPointInAxisCoordinates(new double[]{xCoordinate, yCoordinate});
 			    xCoordinate = axisPnt[0];
 			    yCoordinate = axisPnt[1];
 			} catch (Exception ignored) {
 				// It is not fatal for the custom axes not to work.
 			}
 		}
 		
 		StringBuilder buf = new StringBuilder();
 		if (!Double.isNaN(intensity)) {
 			buf.append(intensityFormat.format(intensity));
 			//buf.append("  ");
 		}
 		buf.append("\n[");
 		buf.append(xAxis.format(xCoordinate));
 		buf.append(", ");
 		buf.append(yAxis.format(yCoordinate));
 		buf.append("]");
 		
 		Dimension size = FigureUtilities.getTextExtents(buf.toString(), Display.getDefault().getSystemFont());
 		Image image = new Image(Display.getDefault(), size.width + CURSOR_SIZE, size.height + CURSOR_SIZE);
 		
 		GC gc = new GC(image);
 		//gc.setAlpha(0);
 		gc.setBackground(TRANSPARENT_COLOR);					
 		gc.fillRectangle(image.getBounds());
 		gc.setForeground(BLACK_COLOR);
 		
 		// Draw cross
 		final PointList x = new PointList(CURSOR_SIZE);
 		final PointList y = new PointList(CURSOR_SIZE);
 		for (int i = 0; i < CURSOR_SIZE; i++) {
 			x.addPoint(new Point(i, CURSOR_SIZE/2));
 			y.addPoint(new Point(CURSOR_SIZE/2, i));
 		}
 		drawPointList(x, gc, false);
 		drawPointList(y, gc, true);
 		
 		// Draw position
 		gc.setBackground(WHITE_COLOR);
 		gc.setForeground(BLACK_COLOR);
 		gc.fillRectangle(CURSOR_SIZE, CURSOR_SIZE, 
 				image.getBounds().width-CURSOR_SIZE, 
 				image.getBounds().height-CURSOR_SIZE);					
 		gc.drawText(buf.toString(), CURSOR_SIZE, CURSOR_SIZE, true);
 		
 		ImageData imageData = image.getImageData();
 		imageData.transparentPixel = imageData.palette.getPixel(TRANSPARENT_COLOR.getRGB());
 		
 		Cursor positionCursor = new Cursor(Display.getCurrent(), imageData, CURSOR_SIZE/2 ,CURSOR_SIZE/2);
 		gc.dispose();
 		image.dispose();
 
 		return positionCursor;
 	}
 
 	
 	/**
 	 * Icon for the cursor 
 	 * @param pensize
 	 * @param square
 	 * @return
 	 */
 	public static Cursor getPenCursor(int pensize, ShapeType shape) {
 		
 		if (shape==ShapeType.NONE) return null;
 
 		final Image image  = new Image(Display.getCurrent(), pensize+4, pensize+4);
 		final GC    gc     = new GC(image, SWT.NONE);
 
 		gc.setBackground(TRANSPARENT_COLOR);
 		gc.fillRectangle(image.getBounds());
 
 		
 		// Draw a cross for the center.
 		final PointList x = new PointList(pensize+4);
 		final PointList y = new PointList(pensize+4);
 		for (int i = 0; i < pensize+4; i++) {
 			x.addPoint(new Point(i, (int)((pensize+4)/2)));
 			y.addPoint(new Point((int)((pensize+4)/2), i));
 		}
 		drawPointList(x, gc, false);
 		drawPointList(y, gc, true);
 		
         // Draw the shape.
 		gc.setForeground(ColorConstants.black);
 		switch (shape) {
 		case SQUARE:
 			gc.drawRectangle(2,2,pensize,pensize);
 			break;
 		case TRIANGLE:
 			final PointList pl = new PointList();
 			pl.addPoint(2+(pensize/2), 2);
 			pl.addPoint(2+pensize, 2+pensize);
 			pl.addPoint(2, 2+pensize);
 			gc.drawPolygon(pl.toIntArray());
 			break;
 		case CIRCLE:
 			gc.drawOval(2,2,pensize,pensize);
 			break;
 		default:
 			break;
 		}
 		
 		ImageData imageData = image.getImageData();
 		imageData.transparentPixel = imageData.palette.getPixel(TRANSPARENT_COLOR.getRGB());
 
 		Cursor ret = new Cursor(Display.getDefault(), imageData, (pensize+4)/2, (pensize+4)/2);
 		gc.dispose();
         image.dispose();
         
         return ret;
 	}
 
 
 	private static void drawPointList(PointList pl, GC gc, boolean isY) {
 		int xOff = isY?0:1;
 		int yOff = isY?1:0;
 		for (int i = 0; i < pl.size(); i++) {
 			gc.setForeground((i%2==0) ? ColorConstants.black : ColorConstants.white); 
 			gc.drawLine(pl.getPoint(i).x, pl.getPoint(i).y, pl.getPoint(i).x+xOff, pl.getPoint(i).y+yOff);
 		}		
 	}
 
 
 }
