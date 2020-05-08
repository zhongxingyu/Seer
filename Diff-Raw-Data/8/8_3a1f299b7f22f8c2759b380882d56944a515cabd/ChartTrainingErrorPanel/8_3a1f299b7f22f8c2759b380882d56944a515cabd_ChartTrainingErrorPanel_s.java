 /**
  * Projekt ANNtool 
  *
  * Copyright (c) 2011 github.com/timaschew/jANN
  * 
  * anton
  */
 package de.unikassel.ann.gui.chart;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 
 import javax.swing.JPanel;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.axis.NumberTickUnit;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 
 /**
  * @author anton
  * 
  */
 public class ChartTrainingErrorPanel extends JPanel {
 
 	private static final long serialVersionUID = -8795234383972917391L;
 	private int count = 1;
 	private int iteration = 0;
 	private XYSeries currentSeries;
 	private XYSeriesCollection dataset;
 
 	public ChartTrainingErrorPanel() {
 		setLayout(new BorderLayout());
 
 		currentSeries = new XYSeries("#" + count++);
 		dataset = new XYSeriesCollection();
 		dataset.addSeries(currentSeries);
 
 		JFreeChart chart = ChartFactory.createXYLineChart("Trainingsfehler", // chart title
 				"Iteration", // x axis label
 				"RMSE", // y axis label
 				dataset, // data
 				PlotOrientation.VERTICAL, true, // include legend
 				true, // tooltips
 				false // urls
 				);
 
 		XYPlot plot = (XYPlot) chart.getPlot();
 
 		plot.setBackgroundPaint(Color.white);
 		// plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
 		plot.setDomainGridlinePaint(Color.lightGray);
 		plot.setRangeGridlinePaint(Color.lightGray);
 
 		// change the auto tick unit selection to integer units only...
 		NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
 		xAxis.setTickUnit(new NumberTickUnit(1));
 
 		ChartPanel panel = new ChartPanel(chart);
 		add(panel);
 	}
 
 	public void createNewSeries() {
 		iteration = 0;
 		if (currentSeries.isEmpty()) {
 			return;
 		}
 		dataset.addSeries(currentSeries);
 	}
 
 	public void addToCurrentSeries(final Double error) {
 		iteration++;
 		currentSeries.add(iteration, error);
 	}
 
 }
