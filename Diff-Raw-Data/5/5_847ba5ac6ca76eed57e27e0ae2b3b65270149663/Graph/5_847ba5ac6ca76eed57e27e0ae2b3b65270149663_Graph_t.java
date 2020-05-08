 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fi.lolcatz.profiler;
 
 import java.awt.Color;
 import org.jfree.chart.*;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.data.xy.XYDataset;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 import org.jfree.ui.ApplicationFrame;
 import org.jfree.ui.RefineryUtilities;
 
 public class Graph extends ApplicationFrame {
 
     public Graph(final String title, Output<?> out, Output<?> param) {
         super(title);
         final XYDataset dataset = createDataset(out, param);
         final JFreeChart chart = createChart(dataset);
         final ChartPanel chartPanel = new ChartPanel(chart);
         chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
         setContentPane(chartPanel);
 
     }
     
     public void init(){
         this.pack();
         RefineryUtilities.centerFrameOnScreen(this);
         this.setVisible(true);
     }
 
     /*
      * Creates the dataset from two Output objects.
      */
     private XYDataset createDataset(Output<?> out, Output<?> param) {
         final XYSeries series1 = new XYSeries("Projected");
         for (int i = 0; i < out.getInput().size(); i++) {
             series1.add(out.getTime().get(i), out.getSize().get(i));
         }
         final XYSeries series2 = new XYSeries("Actual");
        for (int i = 0; i < param.getInput().size(); i++) {
             series2.add(param.getTime().get(i), param.getSize().get(i));
         }
 
         final XYSeriesCollection dataset = new XYSeriesCollection();
         dataset.addSeries(series1);
         dataset.addSeries(series2);
 
         return dataset;
 
     }
     /*
      * Builds and returns the chart from a custom dataset.
      */
     private JFreeChart createChart(final XYDataset dataset) {
 
         final JFreeChart chart = ChartFactory.createXYLineChart(
                 "Runtime chart", // chart title
                 "X", // x axis label
                 "Y", // y axis label
                 dataset, // data
                 PlotOrientation.VERTICAL,
                 true, // include legend
                 true, // tooltips
                 false // urls
                 );
 
         chart.setBackgroundPaint(Color.white);
 
         //        final StandardLegend legend = (StandardLegend) chart.getLegend();
         //      legend.setDisplaySeriesShapes(true);
 
         final XYPlot plot = chart.getXYPlot();
         plot.setBackgroundPaint(Color.lightGray);
         //    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
         plot.setDomainGridlinePaint(Color.white);
         plot.setRangeGridlinePaint(Color.white);
 
         final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
         renderer.setSeriesLinesVisible(0, true);
         renderer.setSeriesShapesVisible(0, false);
         renderer.setSeriesLinesVisible(1, true);
        renderer.setSeriesShapesVisible(1, false);    
         plot.setRenderer(renderer);
 
         final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
         rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
 
         return chart;
 
     }
 }
