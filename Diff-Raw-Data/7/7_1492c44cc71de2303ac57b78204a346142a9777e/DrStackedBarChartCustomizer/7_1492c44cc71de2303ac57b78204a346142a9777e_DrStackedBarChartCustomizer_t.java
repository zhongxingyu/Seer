 package com.ovirt.reports.jasper;
 
 import java.awt.Shape;
 import java.awt.geom.Rectangle2D;
 import java.text.DecimalFormat;
 
 import net.sf.jasperreports.engine.JRChart;
 import net.sf.jasperreports.engine.JRChartCustomizer;
 
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.LegendItem;
 import org.jfree.chart.LegendItemCollection;
 import org.jfree.chart.axis.AxisLocation;
 import org.jfree.chart.axis.CategoryAxis;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.block.BlockBorder;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.renderer.category.StackedBarRenderer;
 
 public class DrStackedBarChartCustomizer implements JRChartCustomizer {
 
     @Override
     public void customize(JFreeChart chart, JRChart jasperChart) {
         StackedBarRenderer renderer = (StackedBarRenderer) chart.getCategoryPlot().getRenderer();
         CategoryPlot categoryPlot = renderer.getPlot();
         renderer.setMaximumBarWidth(0.1);
         categoryPlot.setNoDataMessage("No Data Available");
         categoryPlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
 
 //        Widen the categories so those dots won't show up in the category.
         CategoryAxis domainaxis = categoryPlot.getDomainAxis();
         domainaxis.setTickMarksVisible(true);
         domainaxis.setMaximumCategoryLabelLines(2);
 
 
         LegendItemCollection chartLegend = categoryPlot.getLegendItems();
         LegendItemCollection res = new LegendItemCollection();
         Shape square = new Rectangle2D.Double(0,0,5,5);
         for (int i = 0; i < chartLegend.getItemCount(); i++) {
            LegendItem item = chartLegend.get(i);
            String label = item.getLabel();
            if (label.length() > 18)
            {
                label = label.substring(0,10).concat("...").concat(label.substring(label.length() - 5, label.length()));
            }
            if (label != "" && label != "/Rx Rate" && label != "/Tx Rate")
            {
            res.add(new LegendItem(label, item.getDescription(), item.getToolTipText(), item.getURLText(), true, square, true, item.getFillPaint(), item.isShapeOutlineVisible(), item.getOutlinePaint(), item.getOutlineStroke(), false, item.getLine(), item.getLineStroke(), item.getLinePaint()));
            }
         }
         categoryPlot.setFixedLegendItems(res);
         chart.getLegend().setFrame(BlockBorder.NONE);
 
         ValueAxis rangeAxis = categoryPlot.getRangeAxis();
         if (rangeAxis instanceof NumberAxis) {
             NumberAxis axis = (NumberAxis) rangeAxis;
             axis.setNumberFormatOverride(new DecimalFormat("###,###,###.#"));
             axis.setUpperBound(axis.getUpperBound()+1);
             axis.setAutoRangeMinimumSize(1.0);
         }
     }
 }
