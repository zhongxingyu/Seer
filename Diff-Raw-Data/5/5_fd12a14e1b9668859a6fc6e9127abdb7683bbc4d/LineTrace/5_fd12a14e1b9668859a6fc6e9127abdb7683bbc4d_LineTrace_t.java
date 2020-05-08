 package org.dawb.workbench.plotting.system.swtxy;
 
 import org.csstudio.swt.xygraph.dataprovider.IDataProvider;
 import org.csstudio.swt.xygraph.dataprovider.ISample;
 import org.csstudio.swt.xygraph.dataprovider.Sample;
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.csstudio.swt.xygraph.figures.Trace;
 import org.csstudio.swt.xygraph.linearscale.AbstractScale.LabelSide;
 import org.csstudio.swt.xygraph.linearscale.Range;
 import org.csstudio.swt.xygraph.util.SWTConstants;
 import org.eclipse.draw2d.AbstractBackground;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.swt.SWT;
 
 /**
  * Trace with drawPolyline(...) for faster rendering.
  * 
  * @author fcp94556
  *
  */
 public class LineTrace extends Trace {
 	
 	protected String internalName; 
 	
 	public LineTrace(String name, Axis xAxis, Axis yAxis, IDataProvider dataProvider) {
 		super(name, xAxis, yAxis, dataProvider);
 	}
 	
 	
 	/**
 	 * Called externally.
 	 * @param graphics
 	 * @param p1
 	 * @param p2
 	 */
 	public void drawLine(Graphics graphics, Point p1, Point p2){
 		
 		final Trace.TraceType traceType = getTraceType();
 		if (traceType == Trace.TraceType.STEP_HORIZONTALLY  ||
             traceType == Trace.TraceType.STEP_VERTICALLY    ||
             traceType == Trace.TraceType.BAR) {
             
 
 		    switch (traceType) {
 		    case STEP_HORIZONTALLY:
 
 			    graphics.pushState();
 		    	graphics.setLineStyle(SWTConstants.LINE_SOLID);
 		    	Point ph = new Point(p2.x, p1.y);
 		    	graphics.drawLine(p1, ph);
 		    	graphics.drawLine(ph, p2);
 		    	graphics.popState();
 		    	return;
 		    	
 		    case STEP_VERTICALLY:
 			    graphics.pushState();
 	    	    graphics.setLineStyle(SWTConstants.LINE_SOLID);
 		    	Point pv = new Point(p1.x, p2.y);
 		    	graphics.drawLine(p1, pv);
 		    	graphics.drawLine(pv, p2);
 		    	graphics.popState();
 		    	return;	
 		    	
 			case BAR:
 			    graphics.pushState();
 				if (use_advanced_graphics)
 					graphics.setAlpha(getAreaAlpha());
 				graphics.setLineStyle(SWTConstants.LINE_SOLID);
 				graphics.drawLine(p1, p2);
 				graphics.popState();
 				return;	
 		    }
 		}
 
 		final PointList points = new PointList(2);
 		points.addPoint(p1);
 		points.addPoint(p2);
 	    drawLine(graphics, points, true);
 	}
 	
 	/**Draw line with the line style and line width of the trace.
 	 * @param graphics
 	 * @param p1
 	 * @param p2
 	 */
 	private void drawLine(Graphics graphics, PointList points, boolean recordState){
 		
 		if (recordState) graphics.pushState();
 		
 		final Trace.TraceType traceType = getTraceType();
 		switch (traceType) {
 		case SOLID_LINE:
 			graphics.setLineStyle(SWTConstants.LINE_SOLID);
 			graphics.drawPolyline(points);
 			break;
 		case DASH_LINE:
 			graphics.setLineStyle(SWTConstants.LINE_DASH);
 			graphics.drawPolyline(points);
 			break;
 		case AREA:
 			switch (getBaseLine()) {
 			case NEGATIVE_INFINITY:
 				yAxis.getValuePosition(yAxis.getRange().getLower(), false);
 				break;
 			case POSITIVE_INFINITY:
 				yAxis.getValuePosition(yAxis.getRange().getUpper(), false);
 				break;
 			default:
 				yAxis.getValuePosition(0, false);
 				break;
 			}
 			if (use_advanced_graphics) graphics.setAlpha(getAreaAlpha());
 			graphics.setBackgroundColor(getTraceColor());
 			
 			// now sort out the Start and end of the plot, we need a point at the baseline for both
 			// these can both be added at the end to complete the polygon
 			int yValue = yAxis.getValuePosition(yAxis.getRange().getLower(), false);
 			
 			Point endBase = new Point(points.getLastPoint().x,yValue); 
 			Point startBase = new Point(points.getFirstPoint().x,yValue); 
 			
 			points.addPoint(endBase);
 			points.addPoint(startBase);
 			
 			graphics.fillPolygon(points);
 			break;
 			
 		default:
 			break;
 		}
 		if (recordState) graphics.popState();
 	}
 	
 	@Override
 	protected void paintFigure(Graphics graphics) {
 		
 		if (isDisposed()) return; // We are 
 
 		if (isOpaque()) graphics.fillRectangle(getBounds());
 		if (getBorder() instanceof AbstractBackground) ((AbstractBackground) getBorder()).paintBackground(this, graphics, NO_INSETS);
 		
 		graphics.pushState();
 		
 		if (use_advanced_graphics) graphics.setAntialias(isAntiAliasing()? SWT.ON : SWT.OFF);
 		graphics.setForegroundColor(getTraceColor());
 		graphics.setLineWidth(getLineWidth());
 		ISample predp = null;
 		boolean predpInRange = false;
 		Point dpPos = null;
 		hotSampleist.clear();
 		if(traceDataProvider == null)
 			throw new RuntimeException("No DataProvider defined for trace: " + name); //$NON-NLS-1$
 		// Lock data provider to prevent changes while painting
 		synchronized (traceDataProvider)
         {
     		if(traceDataProvider.getSize()>0){
     		    // Is only a sub-set of the trace data visible?
     			final int startIndex, endIndex;
     			if(traceDataProvider.isChronological()){
     			    final Range indexRange = getIndexRangeOnXAxis();
     				if(indexRange == null){
     				    startIndex = 0;
     					endIndex = -1;
     				}else{
     					startIndex = (int) indexRange.getLower();
     					endIndex = (int) indexRange.getUpper();
     				}
     			}
     			else
     			{   // Cannot optimize range, use all data points
     			    startIndex = 0;
                     endIndex = traceDataProvider.getSize()-1;
     			}
     			
     			
     			final PointList pointList = new PointList();
     			
     			for (int i=startIndex; i<=endIndex; i++)
     			{
     			    ISample dp = traceDataProvider.getSample(i);
                     final boolean dpInXRange = xAxis.getRange().inRange(dp.getXValue());
     				// Mark 'NaN' samples on X axis
     				final boolean valueIsNaN = Double.isNaN(dp.getYValue());
                     if (dpInXRange  &&  valueIsNaN)
                     {
     					Point markPos = new Point(xAxis.getValuePosition(dp.getXValue(), false),
     							yAxis.getValuePosition(xAxis.getTickLablesSide() == LabelSide.Primary?
     									yAxis.getRange().getLower() : yAxis.getRange().getUpper(), false));
     					graphics.setBackgroundColor(traceColor);
     					graphics.fillRectangle(markPos.x -MARKER_SIZE/2, markPos.y - MARKER_SIZE/2, MARKER_SIZE, MARKER_SIZE);
     					Sample nanSample = new Sample(dp.getXValue(),xAxis.getTickLablesSide() == LabelSide.Primary?
     							yAxis.getRange().getLower() : yAxis.getRange().getUpper(),
     							dp.getYPlusError(), dp.getYMinusError(),
     							Double.NaN, dp.getXMinusError(), dp.getInfo());
     					hotSampleist.add(nanSample);
     				}
                     
     				// Is data point in the plot area, also check to see if it is within a general area
                     // to draw lines slightly outside the main range?     
                     // TODO FIXME - Put back in || here so that line plots do not behave badly
                     // Mark to review why the changes broke line plots...
                     boolean dpInRange = dpInXRange && yAxis.getRange().inRange(dp.getYValue());
                     
                     
     				//draw point
     				if(dpInRange){
     					dpPos = new Point(xAxis.getValuePosition(dp.getXValue(), false),
     							yAxis.getValuePosition(dp.getYValue(), false));
     					hotSampleist.add(dp);
     					drawPoint(graphics, dpPos);
     					if(errorBarEnabled && !drawYErrorInArea)
     						drawErrorBar(graphics, dpPos, dp);
     				}
     				if(traceType == Trace.TraceType.POINT && !drawYErrorInArea)
     					continue; // no need to draw line			
     				
     				//draw line
     				if(traceType == Trace.TraceType.BAR){
     					switch (baseLine) {
     					case NEGATIVE_INFINITY:
     						predp = new Sample(dp.getXValue(), yAxis.getRange().getLower());
     						break;
     					case POSITIVE_INFINITY:
     						predp = new Sample(dp.getXValue(), yAxis.getRange().getUpper());
     						break;
     					default:
     						predp = new Sample(dp.getXValue(), 0);
     						break;
     					}
     					predpInRange = xAxis.getRange().inRange(predp.getXValue()) && yAxis.getRange().inRange(predp.getYValue());
     				}
     				
     				// deal with the first point of the plot, and then continue
     				if(predp == null)
     				{   // No previous data point from which to draw a line
     					predp = dp;
     					predpInRange = dpInRange;
     					continue;
     				}
     				
     				// Save original dp info because handling of NaN or
     				// axis intersections might patch it
     				final ISample origin_dp = dp; 
     				final boolean origin_dpInRange = dpInRange;
 
     				// In 'STEP' modes, if there was a value, now there is none,
     				// continue that last value until the NaN location
     				if (valueIsNaN  &&  !Double.isNaN(predp.getYValue()) &&
                         (traceType == Trace.TraceType.STEP_HORIZONTALLY  ||
                          traceType == Trace.TraceType.STEP_VERTICALLY))
                     {   // Patch 'y' of dp, re-compute dpInRange for new 'y'
     				    dp = new Sample(dp.getXValue(), predp.getYValue());
     				    dpInRange = yAxis.getRange().inRange(dp.getYValue());
                     }
     				
     				boolean plot_dp = false;
     				
    				//if(traceType != Trace.TraceType.AREA) {
     				    if(!predpInRange && !dpInRange){ //both are out of plot area
     						ISample[] dpTuple = getIntersection(predp, dp);
     						if(dpTuple[0] == null || dpTuple[1] == null){ // no intersection with plot area
     							predp = origin_dp;
     							predpInRange = origin_dpInRange;
     							predpInRange = origin_dpInRange;
     							continue;
     						}else{
     							predp = dpTuple[0];
     							dp = dpTuple[1];
     							predpInRange = true;
     							dpInRange = true;
     							plot_dp = true;
     						}
     					} else if(!predpInRange || !dpInRange){ // one in and one out
     						//calculate the intersection point with the boundary of plot area.
     						if(!predpInRange){
     							predp = getIntersection(predp, dp)[0];
     							if(predp == null){ // no intersection
     								predp = origin_dp;
     								predpInRange = origin_dpInRange;
     								continue;
     							} else {
     								predpInRange = true;
     							}
     						} else{
     							dp = getIntersection(predp, dp)[0];
     							if(dp == null){ // no intersection
     								predp = origin_dp;
     								predpInRange = origin_dpInRange;
     								continue;
     							}else {
     								dpInRange = true;
     								plot_dp = true;
     							}
     						}
     					}
    				//}
     				
     				final Point predpPos = new Point(xAxis.getValuePosition(predp.getXValue(), false),
     								                 yAxis.getValuePosition(predp.getYValue(), false));
     				dpPos = new Point(xAxis.getValuePosition(dp.getXValue(), false),
     								  yAxis.getValuePosition(dp.getYValue(), false));
     						
     				if(!dpPos.equals(predpPos)){
     					if(errorBarEnabled && drawYErrorInArea && traceType!=Trace.TraceType.BAR)
     						drawYErrorArea(graphics, predp, dp, predpPos, dpPos);
     					
     					if (traceType == Trace.TraceType.STEP_HORIZONTALLY  ||
                             traceType == Trace.TraceType.STEP_VERTICALLY    ||
                             traceType == Trace.TraceType.BAR) {
                             drawLine(graphics, predpPos, dpPos);  	
                         } else {
                         	if(predpInRange) pointList.addPoint(predpPos);
                         	if (plot_dp || (i==endIndex && dpInRange)) pointList.addPoint(dpPos);
                         }
     					
     				}
     				
     				predp = origin_dp;
     				predpInRange = origin_dpInRange;
     			}
     			
     			drawLine(graphics, pointList, false);
     		}
         }
 		graphics.popState();
 	}
 
 	public void dispose() {
 		removeAll();
 		getHotSampleList().clear();
 		name=null;
 		internalName=null;
 		traceDataProvider=null;
 		xAxis=null;	
 		yAxis=null;	
 		traceColor=null;
 		traceType=null;
 		baseLine=null;
 		pointStyle=null;
 		yErrorBarType=null;
 		xErrorBarType=null;
 		errorBarColor=null;
 		xyGraph=null;
 	}
 
 	public boolean isDisposed() {
 		return xyGraph==null;
 	}
 
 
 	public String getInternalName() {
 		if (internalName!=null) return internalName;
 		return getName();
 	}
 
 
 	public void setInternalName(String internalName) {
 		this.internalName = internalName;
 	}
 
 }
