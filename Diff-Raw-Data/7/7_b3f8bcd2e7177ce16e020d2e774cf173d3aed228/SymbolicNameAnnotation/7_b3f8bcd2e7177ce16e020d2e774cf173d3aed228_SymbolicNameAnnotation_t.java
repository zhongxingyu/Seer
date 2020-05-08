 /*
  *  StatCvs-XML - XML output for StatCvs.
  *
  *  Copyright by Steffen Pingel, Tammo van Lessen.
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  version 2 as published by the Free Software Foundation.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package de.berlios.statcvs.xml.chart;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Stroke;
 import java.awt.geom.Line2D;
 import java.awt.geom.Rectangle2D;
 
 import net.sf.statcvs.model.SymbolicName;
 
 import org.jfree.chart.annotations.XYAnnotation;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.plot.Plot;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.ui.RectangleEdge;
 import org.jfree.ui.RefineryUtilities;
 import org.jfree.ui.TextAnchor;
 
 
 /**
  * SymbolicNameAnnotation
  * 
  * Provides symbolic name annotations for XYPlots with java.util.Date
  * objects on the domain axis.
  * 
  * @author Tammo van Lessen
  */
 public class SymbolicNameAnnotation implements XYAnnotation {
 
     private final Color linePaint = Color.GRAY;
     private final Color textPaint = Color.DARK_GRAY;
     private final Stroke stroke = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {3.5f}, 0.0f);
     private final Font font = new Font("Dialog", Font.PLAIN, 9);
     
     private SymbolicName symbolicName;
 
     /**
      * Creates an annotation for a symbolic name.
      * Paints a gray dashed vertical line at the symbolic names
      * date position and draws its name at the top left.
      * 
      * @param symbolicName
      */
     public SymbolicNameAnnotation(SymbolicName symbolicName)
     {
         this.symbolicName = symbolicName;
     }
     
     /**
      * @see org.jfree.chart.annotations.XYAnnotation#draw(java.awt.Graphics2D, org.jfree.chart.plot.XYPlot, java.awt.geom.Rectangle2D, org.jfree.chart.axis.ValueAxis, org.jfree.chart.axis.ValueAxis)
      */
     public void draw(Graphics2D g2d, XYPlot xyPlot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis) {
         PlotOrientation orientation = xyPlot.getOrientation();
         
		// don't draw the annotation if symbolic names date is out of axis' bounds.
		if (domainAxis.getUpperBound() < symbolicName.getDate().getTime()
			|| domainAxis.getLowerBound() > symbolicName.getDate().getTime()) {
		
			return;
		}
		
         RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(
                                             xyPlot.getDomainAxisLocation(),
                                             orientation);
         RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(
                                             xyPlot.getRangeAxisLocation(), 
                                             orientation);
 
         float x = (float)domainAxis.translateValueToJava2D(
                                         symbolicName.getDate().getTime(), 
                                         dataArea, 
                                         domainEdge);
         float y1 = (float)rangeAxis.translateValueToJava2D(
                                         rangeAxis.getUpperBound(),
                                         dataArea, 
                                         rangeEdge);
         float y2 = (float)rangeAxis.translateValueToJava2D(
                                         rangeAxis.getLowerBound(), 
                                         dataArea, 
                                         rangeEdge);            
         
         g2d.setPaint(linePaint);
         g2d.setStroke(stroke);
         
         Line2D line = new Line2D.Float(x, y1, x, y2);
         g2d.draw(line);
         
         float anchorX = x;
         float anchorY = y1 + 2;
 
         g2d.setFont(font);
         g2d.setPaint(textPaint);
 
         /*g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                              RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);*/
                              
         RefineryUtilities.drawRotatedString(
             symbolicName.getName(), 
             g2d,
             anchorX, 
             anchorY,
             TextAnchor.BOTTOM_RIGHT,
             TextAnchor.BOTTOM_RIGHT,
             -Math.PI/2
         );
     }
     
 }
