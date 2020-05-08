 package com.ovirt.reports.jasper;
 
 
 import java.awt.Color;
 import java.awt.Font;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.block.BlockBorder;
 import org.jfree.chart.plot.Marker;
 import org.jfree.chart.plot.ValueMarker;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.ui.RectangleAnchor;
 import org.jfree.ui.TextAnchor;
 
 import net.sf.jasperreports.engine.JRChart;
 import net.sf.jasperreports.engine.JRChartCustomizer;
 
 public class ScatterChartCustomizer implements JRChartCustomizer {
 
     public void customize(JFreeChart chart, JRChart jasperChart) {
             XYPlot categoryPlot = chart.getXYPlot();
             Marker ymarker = new ValueMarker(50);
             ymarker.setLabel("50% Usage");
            ymarker.setLabelFont(new Font("DejaVu Sans", Font.BOLD, 11));
             ymarker.setPaint(Color.black);
             ymarker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
             ymarker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
             Marker xmarker = new ValueMarker(50);
             xmarker.setLabel("50% Usage");
            xmarker.setLabelFont(new Font("DejaVu Sans", Font.BOLD, 11));
             xmarker.setPaint(Color.black);
             xmarker.setLabelAnchor(RectangleAnchor.BOTTOM_RIGHT);
             xmarker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
             categoryPlot.addDomainMarker(ymarker);
             categoryPlot.addRangeMarker(xmarker);
             categoryPlot.setNoDataMessage("No Data Available");
             float[] red = new float[3];
             Color.RGBtoHSB(255, 0, 0, red);
             for (int i=1; i <= categoryPlot.getDataset().getSeriesCount(); i++)
             {
                     if (categoryPlot.getDataset().getSeriesKey(i-1).toString().contains("Deleted".subSequence(0, 4)))
                     {
                         categoryPlot.getRenderer().setSeriesPaint(i-1, Color.getHSBColor(red[0], red[1], red[2]));
                     }
             }
             categoryPlot.getDomainAxis().setTickMarksVisible(true);
             chart.getLegend().setFrame(BlockBorder.NONE);
     }
 }
