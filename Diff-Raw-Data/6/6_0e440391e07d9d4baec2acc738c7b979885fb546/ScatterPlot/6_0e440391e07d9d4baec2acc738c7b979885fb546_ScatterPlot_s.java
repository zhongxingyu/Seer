 /*
$Revision: 1.11 $
$Date: 2006-10-25 18:49:01 $
 
 The Web CGH Software License, Version 1.0
 
 Copyright 2003 RTI. This software was developed in conjunction with the
 National Cancer Institute, and so to the extent government employees are
 co-authors, any rights in such works shall be subject to Title 17 of the
 United States Code, section 105.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 
 1. Redistributions of source code must retain the above copyright notice, this 
 list of conditions and the disclaimer of Article 3, below. Redistributions in 
 binary form must reproduce the above copyright notice, this list of conditions 
 and the following disclaimer in the documentation and/or other materials 
 provided with the distribution.
 
 2. The end-user documentation included with the redistribution, if any, must 
 include the following acknowledgment:
 
 "This product includes software developed by the RTI and the National Cancer 
 Institute."
 
 If no such end-user documentation is to be included, this acknowledgment shall 
 appear in the software itself, wherever such third-party acknowledgments 
 normally appear.
 
 3. The names "The National Cancer Institute", "NCI", 
 Research Triangle Institute, and "RTI" must not be used to endorse or promote 
 products derived from this software.
 
 4. This license does not authorize the incorporation of this software into any 
 proprietary programs. This license does not authorize the recipient to use any 
 trademarks owned by either NCI or RTI.
 
 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, 
 (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE
 NATIONAL CANCER INSTITUTE, RTI, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package org.rti.webcgh.graphics.widget;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.util.Collection;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 //import org.apache.log4j.Logger;
 import org.rti.webcgh.deprecated.graph.DataPoint;
 import org.rti.webcgh.domain.ArrayDatum;
 import org.rti.webcgh.domain.BioAssay;
 import org.rti.webcgh.domain.ChromosomeArrayData;
 import org.rti.webcgh.domain.Experiment;
 import org.rti.webcgh.domain.Reporter;
 import org.rti.webcgh.graphics.DrawingCanvas;
 import org.rti.webcgh.graphics.PlotBoundaries;
 import org.rti.webcgh.graphics.primitive.Circle;
 import org.rti.webcgh.graphics.primitive.Polyline;
 import org.rti.webcgh.service.util.ChromosomeArrayDataGetter;
 import org.rti.webcgh.units.Orientation;
 import org.rti.webcgh.webui.util.ClickBoxes;
 import org.rti.webcgh.webui.util.MouseOverStripe;
 import org.rti.webcgh.webui.util.MouseOverStripes;
 
 /**
  * A two dimensional plotting space that renders
  * array data as points connected by lines.
  * @author dhall
  *
  */
 public final class ScatterPlot implements PlotElement {
 	
 	//private static final Logger LOGGER = Logger.getLogger(ScatterPlot.class);
     
     // =============================
     //     Constants
     // =============================
     
     /** Default radius of data point in pixels. */
     private static final int DEF_POINT_RADIUS = 3;
     
     /** Radius of selected data points in pixels. */
     private static final int SELECTED_POINT_RADIUS = 5;
     
     /** Default width of regression line in pixels. */
     private static final int DEF_LINE_WIDTH = 1;
     
     /** Width of selected lines in pixels. */
     private static final int SELECTED_LINE_WIDTH = 3;
     
 //    /** Default length of error bar hatch lines in pixels. */
 //    private static final int DEF_ERROR_BAR_HATCH_LENGTH = 6;
     
     /**
      * Default maximum number of constituent points in
      * a regression line polyline.
      */
     private static final int DEF_MAX_NUM_POINTS_IN_LINE = 100;
     
     /**
      * Name of attribute that is used by an SVG <pre><g/></pre>
      * element to indicate information about the elements
      * within that group.  This information is used by
      * Javascript code to set the properties of all
      * elements between the <g></g> tags.
      */
     public static final String GRP_ATT_NAME = "egrp";
     
     /**
      * Possible value used with the SVG <pre><g/></pre> attribute given
      * by constant GRP_ATT_NAME to indicate that elements
      * within the group tags correspond to graph points.
      */
     private static final String POINTS_GRP_ATT_VALUE = "p";
     
     
     // =============================
     //       Attributes
     // =============================
     
     /** Experiments to plot. */
     private final Collection<Experiment> experiments;
     
     /** Chromosome number. */
     private final short chromosome;
     
     /**
      * Gets chromosome array data making the location of those
      * data transparent.  Data may be in memory or on disk.
      */
     private final ChromosomeArrayDataGetter chromosomeArrayDataGetter;
     
     
     /** Width of plot in pixels. */
     private final int width;
     
     /** Height of plot in pixels. */
     private final int height;
     
     /** X-coordinate of plot origin (i.e, upper left-most point). */
     private int x = 0;
     
     /** Y-coordinate of plot origin (i.e., upper left-most point). */
     private int y = 0;
     
     /** Boundaries of plot. */
     private final PlotBoundaries plotBoundaries;
     
     /**
      * Data point object that is reused during plot creation
      * in order to economize memory.
      */
     private final DataPoint reusableDataPoint1 = new DataPoint();
     
     /**
      * Data point object that is reused during plot creation
      * in order to economize memory.
      */
     private final DataPoint reusableDataPoint2 = new DataPoint();
     
     /** Click boxes used for providing interactivity using Javascript. */
     private final ClickBoxes clickBoxes;
     
     /**
      * Mouseover stripes used for providing interactivity
      * using Javascript.
      */
     private final MouseOverStripes mouseOverStripes;
     
     
     // =============================
     //     Getters/setters
     // =============================
     
     /**
      * Get mouseover stripes.
      * @return Mouseover stripes.
      */
     public MouseOverStripes getMouseOverStripes() {
 		return mouseOverStripes;
 	}
 
 	/**
      * Get click boxes.
      * @return Click boxes
      */
     public ClickBoxes getClickBoxes() {
     	return this.clickBoxes;
     }
     
     // ==============================
     //       Constructors
     // ==============================
     
     /**
      * Constructor.
      * @param experiments Experiments to plot
      * @param chromosome Chromosome number
      * @param chromosomeArrayDataGetter Getter for
      * chromosome array data
      * @param width Width of plot in pixels
      * @param height Height of plot in pixels
      * @param plotBoundaries Plot boundaries in native data units
      * (i.e., base pairs vs. some quantitation type)
      */
     public ScatterPlot(final Collection<Experiment> experiments,
     		final short chromosome,
     		final ChromosomeArrayDataGetter chromosomeArrayDataGetter,
             final int width, final int height,
             final PlotBoundaries plotBoundaries) {
     	
     	// Make sure args okay
     	if (experiments == null) {
     		throw new IllegalArgumentException("Experiments cannot be null");
     	}
     	if (chromosomeArrayDataGetter == null) {
     		throw new IllegalArgumentException(
     				"Chromosome array data getter cannot be null");
     	}
         
         // Initialize attributes
         this.experiments = experiments;
         this.chromosome = chromosome;
         this.chromosomeArrayDataGetter = chromosomeArrayDataGetter;
         this.width = width;
         this.height = height;
         this.plotBoundaries = plotBoundaries;
         this.clickBoxes = new ClickBoxes(width, height, DEF_POINT_RADIUS,
         		DEF_POINT_RADIUS);
         this.mouseOverStripes = new MouseOverStripes(
         		Orientation.HORIZONTAL, width, height);
     }
 
 
     // ===================================
     //      PlotElement interface
     // ===================================
     
     /**
      * Return point at top left of element.
      * @return A point
      */
     public Point topLeftPoint() {
         return new Point(this.x, this.y);
     }
     
     /**
      * Paint element.
      * @param canvas A canvas
      */
     public void paint(final DrawingCanvas canvas) {
     	SortedSet<Reporter> reporters = new TreeSet<Reporter>();
     	
         // Paint points and lines
     	BioAssay selected = null;
         for (Experiment exp : this.experiments) {
             for (BioAssay bioAssay : exp.getBioAssays()) {
             	if (bioAssay.isSelected()) {
             		selected = bioAssay;
             	} else {
             		this.paint(canvas, bioAssay, reporters);
             	}
             }
         }
         if (selected != null) {
         	this.paint(canvas, selected, reporters);
         }
         
         // Initialize mouseover stripes
         this.initializeMouseOverStripes(reporters);
     }
     
     /**
      * Paint given bioassay.
      * @param canvas Canvas
      * @param bioAssay Bioassay to paint
      * @param reporters All reporters in all experiments that
      * are being painted
      */
     private void paint(final DrawingCanvas canvas, final BioAssay bioAssay,
     		final SortedSet<Reporter> reporters) {
     	
     	int pointRadius = DEF_POINT_RADIUS;
     	int lineWidth = DEF_LINE_WIDTH;
     	if (bioAssay.isSelected()) {
     		pointRadius = SELECTED_POINT_RADIUS;
     		lineWidth = SELECTED_LINE_WIDTH;
     	}
     	ChromosomeArrayData cad = this.chromosomeArrayDataGetter.
     		getChromosomeArrayData(bioAssay, this.chromosome);
     	if (cad != null) {
                 
             // Points
             canvas.setAttribute(GRP_ATT_NAME, POINTS_GRP_ATT_VALUE);
             this.paintPoints(cad, bioAssay.getColor(), canvas,
             		pointRadius,
             		bioAssay.getId(), reporters);
             
             // Error bars
 //            DrawingCanvas errorBarsTile = tile.newTile();
 //            tile.add(errorBarsTile);
 //            errorBarsTile.setAttribute(GRP_ATT_NAME,
 //            	ERROR_BARS_GRP_ATT_VALUE);
 //            this.paintErrorBars(cad, color, errorBarsTile);
 //        
 //            // Lines
             this.paintLines(cad, bioAssay.getColor(), canvas,
             		lineWidth);
         }
     }
     
     
     /**
      * Initialize mouseover stripes.
      * @param reporters Reporters
      */
     private void initializeMouseOverStripes(
     		final SortedSet<Reporter> reporters) {
     	this.mouseOverStripes.getOrigin().x = this.x;
     	this.mouseOverStripes.getOrigin().y = this.y;
     	if (reporters.size() > 0) {
 	    	MouseOverStripe lastStripe = null;
 	    	Reporter lastReporter = null;
 	    	for (Reporter currentReporter : reporters) {
 	    		long currentStartBp = 0;
 	    		if (lastReporter != null) {
 	    			currentStartBp = (currentReporter.getLocation()
 	    					+ lastReporter.getLocation()) / 2;
 	    		}
 	    		int currentStartPix = (int)
 	    			(this.plotBoundaries.fractionalDistanceFromLeft(
 	    					currentStartBp) * (double) this.width);
 	    		MouseOverStripe currentStripe = new MouseOverStripe();
 	    		currentStripe.setText(currentReporter.getName());
 	    		currentStripe.setStart(currentStartPix);
 	    		if (lastStripe != null) {
 	    			lastStripe.setEnd(currentStartPix - 1);
 	    		}
 	    		lastStripe = currentStripe;
 	    		lastReporter = currentReporter;
 	    	}
 	    	lastStripe.setEnd(this.width);
     	}
     }
     
     /**
      * Paint all points for given chromosome array data.
      * @param cad Chromosome array data
      * @param color Color of points
      * @param drawingCanvas A drawing canvas
      * @param pointRadius Radius of data point in pixels
      * @param bioAssayId ID of bioassay datum comes from
      * @param reporters Sorted set of reporters
      */
     private void paintPoints(final ChromosomeArrayData cad, final Color color,
             final DrawingCanvas drawingCanvas, final int pointRadius,
             final Long bioAssayId,
             final SortedSet<Reporter> reporters) {
     	List<ArrayDatum> arrayData = cad.getArrayData();
     	if (arrayData != null) {
 	        for (ArrayDatum datum : cad.getArrayData()) {
 	            this.paintPoint(datum, color, drawingCanvas, pointRadius,
 	            		bioAssayId);
 	            reporters.add(datum.getReporter());
 	        }
     	}
     }
     
     
 //    /**
 //     * Paint all error bars for given chromosome array data.
 //     * @param cad Chromosome array data
 //     * @param color Color of error bars
 //     * @param drawingCanvas A drawing canvas
 //     */
 //    private void paintErrorBars(final ChromosomeArrayData cad,
 //            final Color color, final DrawingCanvas drawingCanvas) {
 //        for (ArrayDatum datum : cad.getArrayData()) {
 //            this.paintErrorBar(datum, color, drawingCanvas);
 //        }
 //    }
     
     
     
     /**
      * Paint a single data point.
      * @param datum An array datum
      * @param color A color
      * @param drawingCanvas A drawing canvas
      * @param pointRadius Radius of data point
      * @param bioAssayId ID of bioassay datum comes from
      */
     private void paintPoint(final ArrayDatum datum,
             final Color color, final DrawingCanvas drawingCanvas,
             final int pointRadius, final Long bioAssayId) {
         this.reusableDataPoint1.bulkSet(datum);
 	    if (this.plotBoundaries.withinBoundaries(this.reusableDataPoint1)) {
 	        int x = this.transposeX(this.reusableDataPoint1);
 	        int y = this.transposeY(this.reusableDataPoint1);
 	        
 	        // Create point
 	        this.drawPoint(x, y, color, datum.getReporter().getName(),
 	                drawingCanvas, pointRadius);
 	        
 	        // Add click box command
 	        x -= this.x;
 	        y -= this.y;
 	        String command = this.clickBoxes.getClickBoxText(x, y);
 	        if (command == null) {
 	        	this.clickBoxes.addClickBoxText(bioAssayId.toString(), x, y);
 	        }
         }
     }
     
     
 //    /**
 //     * Paint error bars for a single data point.
 //     * @param datum An array datum
 //     * @param color A color
 //     * @param drawingCanvas A drawing canvas
 //     */
 //    private void paintErrorBar(final ArrayDatum datum,
 //            final Color color, final DrawingCanvas drawingCanvas) {
 //        this.reusableDataPoint1.bulkSet(datum);
 //        int x = this.transposeX(this.reusableDataPoint1);
 //        int y = this.transposeY(this.reusableDataPoint1);
 //        if (!Float.isNaN(datum.getError())) {
 //            this.drawErrorBar(x, y, datum.getError(), color, drawingCanvas);
 //        }
 //    }
     
     
     /**
      * Paint all lines for given chromosme array data.
      * This method ultimately uses the SVG <polyline/> element.
      * Due to limitations in SVG viewers regarding the
      * maximum number of individual points in a polyline,
      * the points are broken up into separate polylines
      * that contain no more than some maximum number of
      * ponts.
      * @param cad ChromosomeArrayData
      * @param color A color
      * @param drawingCanvas A drawing canvas
      * @param lineWidth Width of line in pixels
      */
     private void paintLines(final ChromosomeArrayData cad,
             final Color color, final DrawingCanvas drawingCanvas,
             final int lineWidth) {
         Polyline polyline = new Polyline(lineWidth,
                 DEF_MAX_NUM_POINTS_IN_LINE, color);
         for (int i = 1; i < cad.getArrayData().size(); i++) {
             if (i % DEF_MAX_NUM_POINTS_IN_LINE == 0) {
                 drawingCanvas.add(polyline, false);
                 polyline = new Polyline(lineWidth,
                         DEF_MAX_NUM_POINTS_IN_LINE, color);
             }
             ArrayDatum d1 = cad.getArrayData().get(i - 1);
             ArrayDatum d2 = cad.getArrayData().get(i);
             boolean runsOff = false;
             this.reusableDataPoint1.bulkSet(d1);
             this.reusableDataPoint2.bulkSet(d2);
             if (this.plotBoundaries.atLeastPartlyOnPlot(
             		this.reusableDataPoint1, this.reusableDataPoint2)) {
 	            if (!this.plotBoundaries.withinBoundaries(
 	            		this.reusableDataPoint1)
 	                    || !this.plotBoundaries.withinBoundaries(
 	                            this.reusableDataPoint2)) {
 	                if (!this.plotBoundaries.withinBoundaries(
 	                        this.reusableDataPoint2)) {
 	                    runsOff = true;
 	                }
 	                this.plotBoundaries.truncateToFitOnPlot(
 	                        this.reusableDataPoint1,
 	                        this.reusableDataPoint2);
 	            }
 	            int x1 = this.transposeX(this.reusableDataPoint1);
 	            int y1 = this.transposeY(this.reusableDataPoint1);
 	            int x2 = this.transposeX(this.reusableDataPoint2);
 	            int y2 = this.transposeY(this.reusableDataPoint2);
 	            polyline.add(x1, y1, x2, y2);
 	            if (runsOff) {
 	                if (!polyline.empty()) {
 	                    drawingCanvas.add(polyline, false);
 	                    polyline = new Polyline(lineWidth,
 	                            DEF_MAX_NUM_POINTS_IN_LINE, color);
 	                }
 	            }
             }
         }
         if (!polyline.empty()) {
             drawingCanvas.add(polyline, false);
         }
     }
     
     
     /**
      * Draw single data point.
      * @param x X-coordinate of point center in pixels
      * @param y Y-coordinate of point in pixels
      * @param color Color of point
      * @param label Mouseover label for point
      * @param drawingCanvas A drawing canvas
      * @param pointRadius Radius of data point
      */
     private void drawPoint(final int x, final int y, final Color color,
             final String label, final DrawingCanvas drawingCanvas,
             final int pointRadius) {
         Circle circle = new Circle(x, y, pointRadius, color);
         //circle.setToolTipText(label);
         drawingCanvas.add(circle, false);
     }
     
     
 //    /**
 //     * Draw single error bar.
 //     * @param x X-axis position of bar in pixels
 //     * @param y Y-axis position of center of bar in pixels
 //     * @param error Error factor which determines the height of bar
 //     * @param color Color of bar
 //     * @param drawingCanvas A drawing canvas
 //     */
 //    private void drawErrorBar(final int x, final int y, final double error,
 //            final Color color, final DrawingCanvas drawingCanvas) {
 //        
 //        // Compute reference points
 //        int deltaY = (int) ((double) height
 //                * this.plotBoundaries.fractionalHeight(error));
 //        int y1 = y - deltaY / 2;
 //        int y2 = y1 + deltaY;
 //        int x1 = x - (this.errorBarHatchLength / 2);
 //        int x2 = x1 + this.errorBarHatchLength;
 //        
 //        // Vertical line
 //        Line line = new Line(x, y1, x, y2, this.lineWidth, color);
 //        drawingCanvas.add(line, false);
 //        
 //        // Top horizontal line
 //        line = new Line(x1, y1, x2, y1, this.lineWidth, color);
 //        drawingCanvas.add(line, false);
 //        
 //        // Bottom horizontal line
 //        line = new Line(x1, y2, x2, y2, this.lineWidth, color);
 //        drawingCanvas.add(line, false);
 //    }
     
     
     /**
      * Transpose the x-coordinate of given data point from
      * the native units of the plot (i.e., base pairs vs. some
      * quantitation type) to pixels.
      * @param dataPoint A data point
      * @return Transposed x-coordinate in pixels
      */
     private int transposeX(final DataPoint dataPoint) {
         return this.x + (int) ((double) width
                 * this.plotBoundaries.fractionalDistanceFromLeft(dataPoint));
     }
     
     
     /**
      * Transpose the y-coordinate of given data point from
      * the native units of the plot (i.e., base pairs vs. some
      * quantitation type) to pixels.
      * @param dataPoint A data point
      * @return Transposed y-coordinate in pixels
      */
     private int transposeY(final DataPoint dataPoint) {
         return this.y + height - (int) ((double) height
                 * this.plotBoundaries.fractionalDistanceFromBottom(dataPoint));
     }
     
     /**
      * Point at top left used to align with other plot elements.
      * @return A point
      */
     public Point topLeftAlignmentPoint() {
         return new Point(this.x, this.y);
     }
     
     
     /**
      * Point at bottom left used to align with other plot elements.
      * @return A point
      */
     public Point bottomLeftAlignmentPoint() {
         return new Point(this.x, this.y + this.height);
     }
     
     
     /**
      * Point at top right used to align with other plot elements.
      * @return A point
      */
     public Point topRightAlignmentPoint() {
         return new Point(this.x + this.width, this.y);
     }
     
     
     /**
      * Point at bottom right used to align with other plot elements.
      * @return A point
      */
     public Point bottomRightAlignmentPoint() {
         return new Point(this.x + this.width, this.y + this.height);
     }
     
     
     /**
      * Width in pixels.
      * @return Width in pixels
      */
     public int width() {
         return this.width;
     }
     
     
     /**
      * Height in pixels.
      * @return Height in pixels
      */
     public int height() {
         return this.height;
     }
     
     
     /**
      * Move element.
      * @param deltaX Number of pixels horizontally
      * @param deltaY Number of pixels vertically
      */
     public void move(final int deltaX, final int deltaY) {
         this.x += deltaX;
         this.y += deltaY;
         this.clickBoxes.getOrigin().x += deltaX;
         this.clickBoxes.getOrigin().y += deltaY;
         this.mouseOverStripes.getOrigin().x += deltaX;
         this.mouseOverStripes.getOrigin().y += deltaY;
     }
 }
